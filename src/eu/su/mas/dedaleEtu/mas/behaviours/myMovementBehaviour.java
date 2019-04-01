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

public class myMovementBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	
	
	private String edge;
	private boolean verbose;
	//Compteur de tour passé
	private int cptTour;
	private int cptAttenteRetour;
	public FSMAgentData data;

	public myMovementBehaviour(final AbstractDedaleAgent myagent, FSMAgentData data) {
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
			//List of observable from the agent's current position
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
	
			/**
			 * Just added here to let you see what the agent is doing, otherwise he will be too quick
			 */
			try {
				this.myAgent.doWait(500);
			} catch (Exception e) {
				e.printStackTrace();
			}
			//1) remove the current node from openlist and add it to closedNodes.
			this.data.addNode(myPosition);
			this.data.removeNode(myPosition);

			this.data.addNode(myPosition);
			//this.sendMessage("IN "+myPosition,false,"Explo");
	
			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
			String nextNode=null;
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
			while(iter.hasNext()){
				String nodeId=iter.next().getLeft();
				if (!this.data.closedNodes.contains(nodeId)){
					if (!this.data.openNodes.contains(nodeId)){
						this.data.openNodes.add(nodeId);
						this.data.addNode(nodeId, MapAttribute.open);
						this.data.addEdge(myPosition, nodeId);	
						this.data.addEdge(myPosition,nodeId);
						//this.sendMessage("LINK "+myPosition+" "+ nodeId,false,"Explo");
					}else{
						//the node exist, but not necessarily the edge
						this.data.addEdge(myPosition, nodeId);
					}
					if (nextNode==null) nextNode=nodeId;
				}
			}
	
			//3) while openNodes is not empty, continues.
			if (this.data.openNodes.isEmpty()){
				//Explo finished
				this.data.finished=true;
				System.out.println("Exploration successufully done, behaviour removed.");
			}else{
				//4) select next move.
				//4.1 If there exist one open node directly reachable, go for it,
				//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
				while (nextNode==null){
					System.out.println("Null");
					//no directly accessible openNode
					//chose one, compute the path and take the first step.
					nextNode=this.data.myMap.getShortestPath(myPosition, this.data.openNodes.get(0)).get(0);
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
		else {
			return 0;
		}
	}
}