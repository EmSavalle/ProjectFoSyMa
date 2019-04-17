package eu.su.mas.dedaleEtu.mas.agents.yours;


import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.CollectorBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ExecuteOrderBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ExploSoloBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ExplorationBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.FindSiloBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.MapExchangeBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.MyExploBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.RetourSiloBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SiloBehaviour;
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
	FSMAgentData data;
	private static final long serialVersionUID = -6431752665590433727L;
	public int cpt=0;

	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time. 
	 * 			1) set the agent attributes 
	 *	 		2) add the behaviours
	 *          
	 */
	public FSMAgent() {
		super();
		System.out.println("Constructeur");
	}
	protected void setup(){

		super.setup();

		Object[] args = getArguments();
		data = new FSMAgentData(10,this,args);

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
		fsm.registerState(new MapExchangeBehaviour(this,data), "MapExchange");
		fsm.registerState(new SiloBehaviour(this,this.data),"Silo");
		fsm.registerState(new RetourSiloBehaviour(this,this.data),"Retour");
		fsm.registerState(new ExecuteOrderBehaviour(this,this.data),"Execute");
		fsm.registerState(new FindSiloBehaviour(this,this.data),"Find");
		//Not switching state
		fsm.registerTransition("Explo", "Explo", 0);
		fsm.registerTransition("MapExchange", "MapExchange", 0);
		fsm.registerTransition("Collect", "Collect", 0);
		fsm.registerTransition("Silo", "Silo", 0);
		fsm.registerTransition("Retour", "Retour", 0);
		fsm.registerTransition("Execute", "Execute", 0);
		fsm.registerTransition("Find", "Find", 0);

		//Switching to map Exchange
		fsm.registerTransition("Explo", "MapExchange", 1);
		fsm.registerTransition("MapExchange", "MapExchange", 1);
		fsm.registerTransition("Collect", "MapExchange", 1);
		fsm.registerTransition("Silo", "MapExchange", 1);
		fsm.registerTransition("Retour", "MapExchange", 1);
		fsm.registerTransition("Execute", "MapExchange", 1);
		fsm.registerTransition("Find", "MapExchange", 1);
		
		//Switching to explo
		fsm.registerTransition("Explo", "Explo", 2);
		fsm.registerTransition("MapExchange", "Explo", 2);
		fsm.registerTransition("Collect", "Explo", 2);
		fsm.registerTransition("Silo", "Explo", 2);
		fsm.registerTransition("Retour", "Explo", 2);
		fsm.registerTransition("Execute", "Explo", 2);
		fsm.registerTransition("Find", "Explo", 2);

		//Switching to Collect
		fsm.registerTransition("Explo", "Collect", 3);
		fsm.registerTransition("MapExchange", "Collect", 3);
		fsm.registerTransition("Collect", "Collect", 3);
		fsm.registerTransition("Silo", "Collect", 3);
		fsm.registerTransition("Retour", "Collect", 3);
		fsm.registerTransition("Execute", "Collect", 3);
		fsm.registerTransition("Find", "Collect", 3);

		//Switching to Silo
		fsm.registerTransition("Explo", "Silo", 3);
		fsm.registerTransition("MapExchange", "Silo", 3);
		fsm.registerTransition("Collect", "Silo", 3);
		fsm.registerTransition("Silo", "Silo", 3);
		fsm.registerTransition("Retour", "Silo", 3);
		fsm.registerTransition("Execute", "Silo", 3);
		fsm.registerTransition("Find", "Silo", 3);
		
		//Switching to Retour
		fsm.registerTransition("Explo", "Retour", 5);
		fsm.registerTransition("MapExchange", "Retour", 5);
		fsm.registerTransition("Collect", "Retour", 5);
		fsm.registerTransition("Silo", "Retour", 5);
		fsm.registerTransition("Retour", "Retour", 5);
		fsm.registerTransition("Execute", "Retour", 5);
		fsm.registerTransition("Find", "Retour", 5);
		
		//Switching to Execute
		fsm.registerTransition("Explo", "Execute", 6);
		fsm.registerTransition("MapExchange", "Execute", 6);
		fsm.registerTransition("Collect", "Execute", 6);
		fsm.registerTransition("Silo", "Execute", 6);
		fsm.registerTransition("Retour", "Execute", 6);
		fsm.registerTransition("Execute", "Execute", 6);
		fsm.registerTransition("Find", "Execute", 6);
		
		//Switching to Find
		fsm.registerTransition("Explo", "Find", 7);
		fsm.registerTransition("MapExchange", "Find", 7);
		fsm.registerTransition("Collect", "Find", 7);
		fsm.registerTransition("Silo", "Find", 7);
		fsm.registerTransition("Retour", "Find", 7);
		fsm.registerTransition("Execute", "Find", 7);
		fsm.registerTransition("Find", "Find", 7);
		
		//TODO add mode Silo
		/*
		 * Transition :
		 * Retour onEnd : 
		 * 0 : pas de changement
		 * 1 : passage mapExchange
		 * 2 : passage mode explo
		 * 3 : passage mode collect
		 * 4 : passage mode silo
		 */
		
		/***
		 * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		 */
		lb.add(fsm);
		
		addBehaviour(new startMyBehaviours(this,lb));
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");

	}
	
	
	
}
