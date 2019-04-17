package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.FSMAgentData;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class MapExchangeBehaviour extends OneShotBehaviour{
	private static final long serialVersionUID = 8567689731496787661L;
	
	private boolean verbose;
	//Compteur de tour passé
	public FSMAgentData data;
	
	public MapExchangeBehaviour(final AbstractDedaleAgent myagent, FSMAgentData data) {
		super(myagent);
		this.verbose = true;
		this.data = data;
		
	}
	@Override
	public void action() {
		this.data.switchToMsgSending = false;
		
		this.data.mapExchangeProtocol();
		this.data.getMessage();
		this.data.movement();
		if(this.data.waitingForResponse == false) {
			//System.out.println("Done");
			done();
		}

			
	}
	public int onEnd() {
		return this.data.endingFunc();
	}
}
