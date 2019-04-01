package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
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

	@Override
	public void action() {
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null){

			this.data.observation();
	
			if(this.data.destination == myPosition || this.data.destination == "") {	

				if(this.data.getNbTreasure() != 0) {
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
				this.data.setBackPackSize(this.ag.getBackPackFreeSpace());
				//TODO mise a jour taille sac
				if(this.ag.getBackPackFreeSpace() < this.data.initBackPackSize/4) {
					//TODO verification fonctionnement retour silo
					if(this.data.siloPosition == "") {
						//TODO Find silo
					}
					else {
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
		final MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		System.out.println("End");
		//2) get the message
		ACLMessage msg = this.myAgent.receive(msgTemplate);
		this.data.setMsg( msg);
		if(msg != null) {
			return 1;
		}
		if(this.data.getNbTreasure() == 0) {
			return 2;
		}
		else {
			return 0;
		}
	}
}