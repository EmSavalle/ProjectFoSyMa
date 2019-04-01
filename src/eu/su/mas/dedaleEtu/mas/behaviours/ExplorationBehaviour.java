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

public class ExplorationBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	
	
	private String edge;
	private boolean verbose;
	//Compteur de tour passé
	private int cptTour;
	private int cptAttenteRetour;
	public FSMAgentData data;

	public ExplorationBehaviour(final AbstractDedaleAgent myagent, FSMAgentData data) {
		super(myagent);
		this.edge = "";
		this.cptTour = 0;
		this.cptAttenteRetour = 0;
		this.verbose = false;
		this.data = data;
		
	}

	@Override
	public void action() {
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		this.cptTour++;
		if (myPosition!=null){
			if(this.cptTour%2 == 0) {
				this.data.switchToMsgSending = true;
			}
			else {
				System.out.println("Not sending ping");
			}
			this.data.observation();
	
			//3) while openNodes is not empty, continues.
			if (this.data.openNodes.isEmpty()){
				//Explo finished
				this.data.finished=true;
				System.out.println("Exploration successufully done, behaviour removed.");
			}else{
				//4) select next move.
				//4.1 If there exist one open node directly reachable, go for it,
				//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
				if(this.data.destination != myPosition || this.data.destination != "") {	
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
				String nextNode = null;
				while (nextNode==null){
					System.out.println("Null");
					//no directly accessible openNode
					//chose one, compute the path and take the first step.
					nextNode=this.data.myMap.getShortestPath(myPosition, this.data.destination).get(0);
				}
				((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			}
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
		if(this.data.switchToMsgSending == true) {
			return 1;
		}
		if(this.data.type=="Collector" && this.data.getNbTreasure() != 0) {
			return 2;
		}
		else {
			return 0;
		}
	}
}