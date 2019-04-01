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
	public void sendMessage(String content, boolean allWme, String name) {
		//1) receive the message
		final MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);		
		int nbAsked = 0;

		final ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setSender(this.myAgent.getAID());
		AMSAgentDescription [] agents = null;

	    try {
	        SearchConstraints c = new SearchConstraints();
	        c.setMaxResults ( new Long(-1) );
	        agents = AMSService.search( this.myAgent, new AMSAgentDescription (), c );
	    }
	    catch (Exception e) {  }

	

		for (int i=0; i<agents.length;i++){
		     AID agentID = agents[i].getName();
		     if(agentID.getName().contains(name) ){
		    	 if(agentID.getName().contains(this.getAgent().getName()) || this.getAgent().getName().contains(agentID.getName())) {
		    		 if(allWme == true) {
			    		 msg.addReceiver(agentID);  
		    		 }
		    		 
		    	 }
		    	 else {
		    		 msg.addReceiver(agentID);  
		    		 System.out.println(this.getAgent().getName()+ " : "+agentID.getName()+ " will receive a message");
		    	 }
		     }
		}
			
		//2° compute the random value		
		msg.setContent(content);
		((AbstractDedaleAgent) this.myAgent).sendMessage(msg);
		System.out.println(this.getAgent().getName()+ ": "+content);
			
		
	}
	public void sendMessage(String content, boolean allWme, AID name) {
		//1) receive the message
		final MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);		
		int nbAsked = 0;

		final ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setSender(this.myAgent.getAID());

	    msg.addReceiver(name);
			
		//2° compute the random value		
		msg.setContent(content);
		((AbstractDedaleAgent) this.myAgent).sendMessage(msg);
		System.out.println(this.getAgent().getName()+ ": "+content);
			
		
	}
	public void getMessage() {
		final MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		ACLMessage msg = null;
		//2) get the message
		if(this.data.msg != null) {
			msg = this.data.msg;
		}
		else {
			msg = this.myAgent.receive(msgTemplate);
			if(msg == null) {
				if(this.verbose==true)
					System.out.println(this.getAgent().getName() + " : Pas de message");
			}
			
		}
		while (msg != null) {	
			if(this.verbose==true)
				System.out.println(this.getAgent().getName()+" : Received "+msg.getContent() +" from "+msg.getSender().getName());	
			String[] parts = msg.getContent().split("\\s+");
			/*System.out.println("parts");
			for (int i = 0; i < parts.length; i++) {
				System.out.println(parts[i]);
			}*/
			if(parts[0].contains("ACK")) {
				if(this.verbose==true)
					System.out.println(this.getAgent().getName() + " : Ack acknowledged");
				this.data.inComms = false;
				Entry<String,Integer> pair1=new AbstractMap.SimpleEntry<String,Integer>(msg.getSender().getName(),this.data.cptTour);
				this.data.lastComms.add(pair1);
			}
			else if(parts[0].contains("PING")) {
				if(this.verbose==true)
					System.out.println(this.getAgent().getName() + " : Ping acknowledged");
				
				boolean alreadyTold = false;
				for(int i = 0 ; i < this.data.lastComms.size() ; i++) {
					if(this.data.getLastCommsKey(i) == msg.getSender().getName()) {
						if(this.data.cptTour - this.data.getLastCommsValue(i) < 10) {
							alreadyTold = true;
						}
					}
				}
				if(alreadyTold) {
					this.sendMessage("ACK",false, msg.getSender());
				}
				else {
					this.data.inComms = true;
					this.sendMessage("ASK", false, msg.getSender());
				}
			}
			else if (parts[0].contains("ASK")) {
				this.data.waitingForResponse = false;
				if(this.verbose==true)
					System.out.println(this.getAgent().getName() + " : Ask acknowledged");
				
				String content = "EXPLORE ";
				if(this.verbose==true)
					System.out.println(this.data.closedNodes.size());
				if(this.data.closedNodes.size() != 0) {
					this.data.inComms = true;
					this.data.waitingForResponse = false;
					for (int i = 0 ; i<this.data.closedNodes.size() ; i++) {
						if(i == this.data.closedNodes.size()-1) {
							content = content +this.data.closedNodes.toArray()[i];
						}
						else {
							content = content +this.data.closedNodes.toArray()[i] +"/";
						}
					}
					this.sendMessage(content, false, msg.getSender());
					if(this.verbose==true)
						System.out.println(this.getAgent().getName()+" : Send "+content);
				}
				else {
					if(this.verbose==true)
						System.out.println("Rien a envoyer");
					this.sendMessage("ACK",false, msg.getSender());
					this.data.inComms = false;
				}
				
				
				
			}
			else if(parts[0].contains("EXPLORE")) {
				if(this.verbose==true)
					System.out.println(this.getAgent().getName() + " : EXPLORE acknowledged");
				
				this.data.inComms = true;
				if(this.verbose==true)
					System.out.println(this.getAgent().getName()+" : Received EXPLORE");
				String content = "EXPLAIN ";
				String edgeContent = "";
				String noeud = "";
				String[] noeuds = parts[1].split("/");

				boolean cpt = false;
				for (int i = 0 ; i<noeuds.length ; i++) {
					if(!this.data.closedNodes.contains(noeuds[i])) {
						cpt=true;
						if(i == this.data.closedNodes.size()-1) {
							content = content +noeuds[i];
						}
						else {
							content = content +noeuds[i] +"/";
						}
					}
				}
				if(cpt == true) {
					this.sendMessage(content, false, msg.getSender());
					if(this.verbose==true)
						System.out.println(this.getAgent().getName()+" : Send "+content);					
				}
				content = "UPDATE ";
				edgeContent = "";
				noeud = "";
				cpt = false;
				for (int i = 0 ; i<this.data.closedNodes.size() ; i++) {
					noeud = (String) this.data.closedNodes.toArray()[i].toString();
					System.out.println("a|"+noeud+"|");
					if(!parts[1].contains(noeud)) {
						cpt=true;
						if(i == this.data.closedNodes.size()-1) {
							content = content +noeud;
						}
						else {
							content = content +noeud +"/";
						}
						String[] e = this.data.edge.split(" ");
						for (int j = 0 ; j<e.length ; j++) {
							if(e[j].contains(noeud)) {
								System.out.println("b|"+e[j]+"|");
								if(j == e.length-1) {
									edgeContent = edgeContent +e[j];
								}
								else {
									edgeContent = edgeContent +e[j] +"/";
								}
							}
						}
					}
				}
				if(cpt == true) {
					content= content + " "+edgeContent;
					this.sendMessage(content, false, msg.getSender());
					if(this.verbose==true)
						System.out.println(this.getAgent().getName()+" : Send "+content);					
				}
				this.data.inComms = false;
			}else if(parts[0].contains("EXPLAIN")) {
				if(this.verbose==true)
					System.out.println(this.getAgent().getName() + " : EXPLORE acknowledged");
				
				this.data.inComms = true;
				if(this.verbose==true)
					System.out.println(this.getAgent().getName()+" : Received EXPLORE");
				String content = "UPDATE "+parts[1]+" ";
				System.out.println("c|"+parts[1]+"|");
				String edgeContent = "";
				String noeud = "";
				String[] noeuds = parts[1].split("/");

				boolean cpt = false;
				String[] edges = this.data.edge.split(" ");
				for (int i = 0 ; i<noeuds.length ; i++) {
					for(int j = 0 ; j < edges.length ; j++) {
						if(edges[j].contains(noeuds[i]))
						{
							System.out.println("d|"+edges[j]+"|");
							if(j == edges.length-1) {
								edgeContent = edgeContent +edges[j];
							}
							else {
								edgeContent = edgeContent +edges[j] +"/";
							}
						}
					}
				}
				if(cpt == true) {
					content= content + " "+edgeContent;
					this.sendMessage(content, false, msg.getSender());
					System.out.println(this.getAgent().getName()+" : Send "+content);					
				}
				content = "UPDATE ";
				edgeContent = "";
				noeud = "";
				cpt = false;
				for (int i = 0 ; i<this.data.closedNodes.size() ; i++) {
					noeud = (String) this.data.closedNodes.toArray()[i].toString();
					if(!parts[1].contains(noeud)) {
						cpt=true;
						System.out.println("e|"+noeud+"|");
						if(i == this.data.closedNodes.size()-1) {
							content = content +noeud;
						}
						else {
							content = content +noeud +"/";
						}
						String[] e = this.data.edge.split(" ");
						for (int j = 0 ; j<e.length ; j++) {
							if(e[j].contains(noeud)) {
								System.out.println("f|"+e[j]+"|");
								if(j == e.length-1) {
									edgeContent = edgeContent +e[j];
								}
								else {
									edgeContent = edgeContent +e[j] +"/";
								}
							}
						}
					}
				}
				if(cpt == true) {
					content= content + " "+edgeContent;
					this.sendMessage(content, false, msg.getSender());
					if(this.verbose==true)
						System.out.println(this.getAgent().getName()+" : Send "+content);					
				}
				this.data.inComms = false;
			}
			else if(parts[0].contains("UPDATE")) {
				if(this.verbose==true)
					System.out.println(this.getAgent().getName() + " : Update acknowledged");
				
				this.data.inComms = false;
				String[] noeuds = parts[1].split("/");
				String[] edges = parts[2].split("/");
				for (int i = 0; i < noeuds.length ; i++) {
					this.data.myMap.addNode(noeuds[i]);
					this.data.closedNodes.add(noeuds[i]);
					if(this.data.openNodes.contains(noeuds[i])) {
						this.data.openNodes.remove(noeuds[i]);
					}
				}
				for (int i = 0; i < edges.length; i++) {
					String[] e = edges[i].split("-");
					if(this.verbose==true)
						System.out.println("Ajoute noeud et arc entre "+e[0] + " et "+e[1]);
					if(!this.data.openNodes.contains(e[0]) && !this.data.closedNodes.contains(e[0])) {
						this.data.addNode(e[0]);
					}
					if(!this.data.openNodes.contains(e[1]) && !this.data.closedNodes.contains(e[1])) {
						this.data.addNode(e[1], MapAttribute.open);
						this.data.openNodes.add(e[1]);
					}
					this.data.addEdge(e[0],e[1]);
					
				}
				this.sendMessage("ACK",false, msg.getSender());
			}
			 
			msg = this.myAgent.receive(msgTemplate);
		}
	}
	@Override
	public void action() {
		
		//0) Retrieve the current position
		if(this.data.waitingForResponse == false && this.data.inComms == false && this.data.switchToMsgSending == true) {					
			this.sendMessage("PING", false, "Explo");
			//System.out.println("Sending ping for map exchange");
			this.data.waitingForResponse = true;
			this.data.cptAttenteRetour=0;
			this.data.switchToMsgSending =false;
		}
		else if(this.data.cptAttenteRetour > 2){
			//System.out.println("Waiting for response ended");
			if(this.verbose==true)
				System.out.println(this.getAgent().getName() + " : Debloquage");
			
			this.data.waitingForResponse = false;
			this.data.cptAttenteRetour = 0;
		}
		else {
			//System.out.println("Waiting for response");
			//System.out.println(this.data.cptAttenteRetour);
			this.data.cptAttenteRetour=this.data.cptAttenteRetour+1;
		}
		getMessage();
		if(this.data.inComms == false && this.data.waitingForResponse == false) {
			//System.out.println("Done");
			done();
		}

			
	}
	public int onEnd() {
		//System.out.println("OnEnd");
		if(this.data.inComms == true || this.data.waitingForResponse == true) {
			return 0;
		}
		else {
			return 1;
		}
	}
}
