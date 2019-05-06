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

public class SiloBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	
	
	private boolean verbose;
	//Compteur de tour passé
	public FSMAgentData data;

	public SiloBehaviour(final AbstractDedaleAgent myagent, FSMAgentData data) {
		super(myagent);
		this.verbose = false;
		this.data = data;
		
	}

	@Override
	public void action() {
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null){
			/*if(this.data.cptTour%20 == 0) {
				this.data.switchToMsgSending = true;
			}*/
			this.data.getMessage();
			this.data.observation();
			//if(this.data.getNeighbour(myPosition).length <3 || this.data.cptTour) {
				this.data.movement();
			//}
			
		}//TODO Empecher le tanker de rester sur un tresor
		done();
	}
	public int onEnd() {
		/*final MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		//System.out.println("End");
		//2) get the message
		ACLMessage msg = this.myAgent.receive(msgTemplate);
		this.data.setMsg( msg);
		if(msg != null) {
			return 1;
		}*/
		return this.data.endingFunc();
		/*if(this.data.switchToMsgSending == true) {
			return 1;
		}
		if(this.data.type=="Collector" && this.data.getNbTreasure() != 0) {
			return 2;
		}
		else {
			return 0;
		}*/
	}
}