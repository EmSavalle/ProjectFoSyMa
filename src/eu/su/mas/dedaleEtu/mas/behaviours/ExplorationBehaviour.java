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

	
	
	private boolean verbose;
	//Compteur de tour passé
	public FSMAgentData data;

	public ExplorationBehaviour(final AbstractDedaleAgent myagent, FSMAgentData data) {
		super(myagent);
		this.verbose = false;
		this.data = data;
		
	}

	@Override
	public void action() {
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null){
			/*if(this.data.cptTour%2 == 0) {
				this.data.switchToMsgSending = true;
			}
			else {
				System.out.println("Not sending ping");
			}*/
			this.data.observation();
	
			//3) while openNodes is not empty, continues.
			if (this.data.openNodes.isEmpty()){
				//Explo finished
				this.data.finished=true;
				System.out.println("Exploration successufully done, behaviour removed.");
			}else{
				this.data.movement();
			}
		}
		done();
	}
	public int onEnd() {
		return this.data.endingFunc();
	}
}