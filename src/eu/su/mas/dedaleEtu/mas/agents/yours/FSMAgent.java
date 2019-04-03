package eu.su.mas.dedaleEtu.mas.agents.yours;


import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.CollectorBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ExploSoloBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ExplorationBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.MapExchangeBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.MyExploBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.myMovementBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.FSMAgentData;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * ExploreSolo agent. 
 * It explore the map using a DFS algorithm.
 * It stops when all nodes have been visited
 *  
 *  
 * @author hc
 *
 */

public class FSMAgent extends AbstractDedaleAgent {

	private static final long serialVersionUID = -6431752665590433727L;
	public int cpt=0;
	public FSMAgentData data = new FSMAgentData("Explo",/*this.getBackPackFreeSpace()*/10,this);

	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time. 
	 * 			1) set the agent attributes 
	 *	 		2) add the behaviours
	 *          
	 */
	protected void setup(){

		super.setup();
		

		List<Behaviour> lb=new ArrayList<Behaviour>();
		
		/************************************************
		 * 
		 * ADD the behaviours of the Dummy Moving Agent
		 * 
		 ************************************************/
		FSMBehaviour fsm = new FSMBehaviour(this) {
			public int onEnd() {
				System.out.println("FSM behaviour terminé");
				myAgent.doDelete();
				return super.onEnd();
			}

		};
		ACLMessage m = null;
		//Ajouter echange de message (Depl doit récupérer un message et laisser la main a mapexchange qui analysera ce message et le traitera
		fsm.registerFirstState(new ExplorationBehaviour(this,this.data),"Explo");
		fsm.registerState(new CollectorBehaviour(this,this.data),"Collect");
		fsm.registerState(new MapExchangeBehaviour(this,data), "Map Exchange");
		fsm.registerTransition("Explo", "Collect", 2);
		fsm.registerTransition("Explo", "Map Exchange", 1);
		fsm.registerTransition("Explo", "Explo", 0);
		fsm.registerTransition("Collect", "Explo", 2);
		fsm.registerTransition("Collect", "Map Exchange", 1);
		fsm.registerTransition("Collect", "Collect", 0);
		fsm.registerTransition("Map Exchange", "Map Exchange", 0);
		fsm.registerTransition("Map Exchange","Explo", 1);
		
		
		/***
		 * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		 */
		lb.add(fsm);
		
		addBehaviour(new startMyBehaviours(this,lb));
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");

	}
	
	
	
}
