package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.FSMAgentData;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CollectorBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	
	
	private String edge;
	private boolean verbose;
	//Compteur de tour passé
	private int cptTour;
	private int cptAttenteRetour;
	public FSMAgentData data;
	public AbstractDedaleAgent ag;

	public CollectorBehaviour(final AbstractDedaleAgent myagent, FSMAgentData data) {
		super(myagent);
		this.ag = myagent;
		this.edge = "";
		this.cptTour = 0;
		this.cptAttenteRetour = 0;
		this.verbose = false;
		this.data = data;
	}
	//TODO pb : le collecteur n'arrive pas a récperer un trésor s'il est a coté du tanker
	@Override
	public void action() {
		System.out.println("Collector behaviour launched");
		if(this.data.verboseCollect) {
			System.out.println(this.data.myAgent.getName() + " Backpack :"+this.data.myBackpackSize+"/"+this.data.initBackPackSize);
			System.out.println(this.data.myAgent.getName() + " Backpack :"+this.data.myAgent.getBackPackFreeSpace());
		}
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null){
			if(this.data.objective.equals("explore") && this.data.getNbTreasureAccessible(this.data.lockpickStrength,this.data.strength)>0)
				this.data.objective = "collect";
			this.data.observation();
			if(this.data.destination == "") {	

				if(this.data.getNbTreasureAccessible(this.data.lockpickStrength,this.data.strength)!= 0) {
					this.data.setDestination(this.data.getPositionBestTreasureForMe(myPosition, this.data.myBackpackSize, 1));
				}else if(this.data.myBackpackSize == 0) {
					if(this.data.siloPosition == "") {
						//TODO Find silo
					}
					this.data.setDestination(this.data.siloPosition);
				}
				else {
					int iMin=-1;
					int distMin=0;
					for (int i = 0 ; i < this.data.openNodes.size() ; i++) {
						int a = this.data.dist(myPosition,this.data.openNodes.get(i));
						if(iMin == -1 || a < distMin) {
							iMin = i;
							distMin = a;
						}
					}
				}
			}
			else if(this.data.destination == myPosition) {
				this.ag.pick();
				if(this.data.verboseCollect) {
					System.out.println(this.data.myAgent.getName() + "Picked");
				}
				
				this.data.setBackPackSize(this.ag.getBackPackFreeSpace());
				//TODO mise a jour taille sac
				if(this.data.verboseCollect)
					System.out.println("Retour Silo");
				if(this.ag.getBackPackFreeSpace() < this.data.initBackPackSize/4) {
					this.data.objective = "vidage";
					//TODO verification fonctionnement retour silo
					if(this.data.siloPosition == "" || this.data.siloPositionOutdated) {
						if(this.data.verboseCollect)
							System.out.println("Finding silo");
						this.data.findSilo();
					}
					else {
						if(this.data.verboseCollect)
							System.out.println("Comming back to silo");
						this.data.setDestination(this.data.siloPosition);
					}
				}
				else {
					this.data.setDestination(this.data.getPositionBestTreasureForMe(myPosition, this.data.myBackpackSize, 1));
				}
			}

			this.data.movement();
		}
		done();
	}
	public int onEnd() {
		return this.data.endingFunc();
	}
}