package eu.su.mas.dedaleEtu.mas.behaviours;


import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


/**
 * This behaviour allows an agent to explore the environment and learn the associated topological map.
 * The algorithm is a pseudo - DFS computationally consuming because its not optimised at all.</br>
 * 
 * When all the nodes around him are visited, the agent randomly select an open node and go there to restart its dfs.</br> 
 * This (non optimal) behaviour is done until all nodes are explored. </br> 
 * 
 * Warning, this behaviour does not save the content of visited nodes, only the topology.</br> 
 * Warning, this behaviour is a solo exploration and does not take into account the presence of other agents (or well) and indefinitely tries to reach its target node
 * @author hc
 *
 */
public class MyExploBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	private boolean finished = false;
	private boolean inComms = false;
	/**
	 * Current knowledge of the agent regarding the environment
	 */
	private MapRepresentation myMap;

	/**
	 * Nodes known but not yet visited
	 */
	private List<String> openNodes;
	/**
	 * Visited nodes
	 */
	private Set<String> closedNodes;
	private java.util.List<java.util.Map.Entry<String,Integer>> lastComms;
	
	private String edge;
	private boolean envoiASK;
	private boolean verbose;
	//Compteur de tour passé
	private int cptTour;
	private int cptAttenteRetour;

	public MyExploBehaviour(final AbstractDedaleAgent myagent,boolean inComms) {
		super(myagent);
		this.myMap=myMap;
		this.openNodes=new ArrayList<String>();
		this.closedNodes=new HashSet<String>();
		this.lastComms= new java.util.ArrayList<>();
		this.edge = "";
		this.envoiASK = true;
		this.cptTour = 0;
		this.cptAttenteRetour = 0;
		this.verbose = true;
		
	}

	@Override
	public void action() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		/*System.out.println("Au tour de l'agent :"+this.getAgent().getName());
		try {
			System.out.println("Press enter in the console to allow the agent "+this.myAgent.getLocalName() +" to execute its next move");
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		if(this.myMap==null)
			this.myMap= new MapRepresentation();

		//0) Retrieve the current position
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		this.cptTour++;
		if (myPosition!=null){
			getMessage();
			if(this.envoiASK == true) {
				if(this.inComms == false) {
					
					if(this.cptAttenteRetour == 0) {					
						this.sendMessage("PING", false, "Explo");
						this.cptAttenteRetour++;
					}
					else if(this.cptAttenteRetour == 1){
						if(this.verbose==true)
							System.out.println(this.getAgent().getName() + " : Debloquage");
						
						this.envoiASK = false;
						this.cptAttenteRetour = 0;
					}
					else {
						this.cptAttenteRetour++;
					}
					
				}
			}
			else {
				if(this.cptTour%2 == 0) {
					this.envoiASK = true;
				}
				//List of observable from the agent's current position
				List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
	
				/**
				 * Just added here to let you see what the agent is doing, otherwise he will be too quick
				 */
				try {
					this.myAgent.doWait(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
	
				//1) remove the current node from openlist and add it to closedNodes.
				this.closedNodes.add(myPosition);
				this.openNodes.remove(myPosition);
	
				this.myMap.addNode(myPosition);
				//this.sendMessage("IN "+myPosition,false,"Explo");
	
				//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
				String nextNode=null;
				Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
				while(iter.hasNext()){
					String nodeId=iter.next().getLeft();
					if (!this.closedNodes.contains(nodeId)){
						if (!this.openNodes.contains(nodeId)){
							this.openNodes.add(nodeId);
							this.myMap.addNode(nodeId, MapAttribute.open);
							this.myMap.addEdge(myPosition, nodeId);	
							this.edge=this.edge+" "+myPosition+"-"+nodeId;
							//this.sendMessage("LINK "+myPosition+" "+ nodeId,false,"Explo");
						}else{
							//the node exist, but not necessarily the edge
							this.myMap.addEdge(myPosition, nodeId);
						}
						if (nextNode==null) nextNode=nodeId;
					}
				}
	
				//3) while openNodes is not empty, continues.
				if (this.openNodes.isEmpty()){
					//Explo finished
					finished=true;
					System.out.println("Exploration successufully done, behaviour removed.");
				}else if(inComms != true){
					//4) select next move.
					//4.1 If there exist one open node directly reachable, go for it,
					//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
					if (nextNode==null){
						//no directly accessible openNode
						//chose one, compute the path and take the first step.
						nextNode=this.myMap.getShortestPath(myPosition, this.openNodes.get(0)).get(0);
					}
					((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
					System.out.println("Moving to : "+nextNode);
				}
			}

		}
		done();
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

		//2) get the message
		ACLMessage msg = this.myAgent.receive(msgTemplate);
		if(msg == null) {
			if(this.verbose==true)
				System.out.println(this.getAgent().getName() + " : Pas de message");
		}
		while (msg != null) {	
			if(this.verbose==true)
				System.out.println(this.getAgent().getName()+" : Received "+msg.getContent() +" from "+msg.getSender().getName());	
			String[] parts = msg.getContent().split(" |  ");
			/*System.out.println("parts");
			for (int i = 0; i < parts.length; i++) {
				System.out.println(parts[i]);
			}*/
			if(parts[0].contains("ACK")) {
				if(this.verbose==true)
					System.out.println(this.getAgent().getName() + " : Ack acknowledged");
				this.inComms = false;
				Entry<String,Integer> pair1=new AbstractMap.SimpleEntry<String,Integer>(msg.getSender().getName(),this.cptTour);
				this.lastComms.add(pair1);
			}
			else if(parts[0].contains("PING")) {
				if(this.verbose==true)
					System.out.println(this.getAgent().getName() + " : Ping acknowledged");
				
				boolean alreadyTold = false;
				for(int i = 0 ; i < this.lastComms.size() ; i++) {
					if(this.lastComms.get(i).getKey() == msg.getSender().getName()) {
						if(this.cptTour - this.lastComms.get(i).getValue() < 10) {
							alreadyTold = true;
						}
					}
				}
				if(alreadyTold) {
					this.sendMessage("ACK",false, msg.getSender());
				}
				else {
					this.inComms = true;
					//this.envoiASK = true;
					this.sendMessage("ASK", false, msg.getSender());
				}
			}
			else if (parts[0].contains("ASK")) {
				if(this.verbose==true)
					System.out.println(this.getAgent().getName() + " : Ask acknowledged");
				
				String content = "EXPLORE ";
				if(this.verbose==true)
					System.out.println(this.closedNodes.size());
				if(this.closedNodes.size() != 0) {
					this.inComms = true;
					this.envoiASK = true;
					for (int i = 0 ; i<this.closedNodes.size() ; i++) {
						content = content +this.closedNodes.toArray()[i] +"/";
					}
					this.sendMessage(content, false, msg.getSender());
					if(this.verbose==true)
						System.out.println(this.getAgent().getName()+" : Send "+content);
				}
				else {
					if(this.verbose==true)
						System.out.println("Rien a envoyer");
					this.sendMessage("ACK",false, msg.getSender());
					this.inComms = false;
				}
				
				
				
			}
			else if(parts[0].contains("EXPLORE")) {
				if(this.verbose==true)
					System.out.println(this.getAgent().getName() + " : EXPLORE acknowledged");
				
				this.inComms = true;
				this.envoiASK = true;
				if(this.verbose==true)
					System.out.println(this.getAgent().getName()+" : Received EXPLORE");
				String content = "EXPLAIN ";
				String edgeContent = "";
				String noeud = "";
				String[] noeuds = parts[1].split("/");

				boolean cpt = false;
				for (int i = 0 ; i<noeuds.length ; i++) {
					if(!this.closedNodes.contains(noeuds[i])) {
						cpt=true;
						content = content +noeuds[i] +"/";
					}
				}
				if(cpt == true) {
					this.sendMessage(content, false, msg.getSender());
					if(this.verbose==true)
						System.out.println(this.getAgent().getName()+" : Send "+content);					
				}
				content = "";
				edgeContent = "";
				noeud = "";
				cpt = false;
				for (int i = 0 ; i<this.closedNodes.size() ; i++) {
					noeud = (String) this.closedNodes.toArray()[i].toString();
					if(!parts[1].contains(noeud)) {
						cpt=true;
						content = content +noeud +"/";
						String[] e = edge.split("  | ");
						for (int j = 0 ; j<e.length ; j++) {
							if(e[j].contains(noeud)) {
								edgeContent = edgeContent +e[j] +"/";
							}
						}
					}
				}
				if(cpt == true) {
					content= "UPDATE "+content.replace(" ","") + " "+edgeContent.replace(" ","");
					this.sendMessage(content, false, msg.getSender());
					if(this.verbose==true)
						System.out.println(this.getAgent().getName()+" : Send "+content);					
				}
				this.inComms = false;
				this.envoiASK = false;
			}else if(parts[0].contains("EXPLAIN")) {
				if(this.verbose==true)
					System.out.println(this.getAgent().getName() + " : EXPLAIN acknowledged");
				
				this.inComms = true;
				this.envoiASK = true;
				if(this.verbose==true)
					System.out.println(this.getAgent().getName()+" : Received EXPLAIN");
				String content = "UPDATE "+parts[1]+" ";
				String edgeContent = "";
				String noeud = "";
				String[] noeuds = parts[1].split("/");

				boolean cpt = false;
				String[] edges = this.edge.split("  | ");
				for (int i = 0 ; i<noeuds.length ; i++) {
					cpt = true;
					for(int j = 0 ; j < edges.length ; j++) {
						if(edges[j].contains(noeuds[i]))
						{
							edgeContent = edgeContent +edges[j] +"/";
						}
					}
				}
				if(cpt == true) {
					content= content + " "+edgeContent;
					this.sendMessage(content, false, msg.getSender());
					System.out.println(this.getAgent().getName()+" : Send "+content);					
				}/*
				content = "UPDATE ";
				edgeContent = "";
				noeud = "";
				cpt = false;
				for (int i = 0 ; i<this.closedNodes.size() ; i++) {
					noeud = (String) this.closedNodes.toArray()[i].toString();
					if(!parts[1].contains(noeud)) {
						cpt=true;
						if(i == this.closedNodes.size()-1) {
							content = content +noeud;
						}
						else {
							content = content +noeud +"/";
						}
						String[] e = edge.split(" ");
						for (int j = 0 ; j<e.length ; j++) {
							if(e[j].contains(noeud)) {
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
				}*/
				this.inComms = false;
				this.envoiASK = false;
			}
			else if(parts[0].contains("UPDATE")) {
				if(this.verbose==true)
					System.out.println(this.getAgent().getName() + " : Update acknowledged");
				
				this.inComms = false;
				this.envoiASK = false;
				String[] noeuds = parts[1].split("/");
				String[] edges = parts[2].split("/");
				for (int i = 0; i < noeuds.length ; i++) {
					this.myMap.addNode(noeuds[i]);
					this.closedNodes.add(noeuds[i]);
					if(this.openNodes.contains(noeuds[i])) {
						this.openNodes.remove(noeuds[i]);
					}
				}
				for (int i = 0; i < edges.length; i++) {
					if(edges[i]!= ""&&edges[i]!= " ") {						
						System.out.println("Edges : ");
						System.out.println("i"+edges[i]+"i");
						String[] e = edges[i].split("-");
						if(this.verbose==true)
							System.out.println("Ajoute noeud et arc entre "+e[0] + " et "+e[1]);
						if(!this.openNodes.contains(e[0]) && !this.closedNodes.contains(e[0])) {
							this.myMap.addNode(e[0]);
							this.openNodes.add(e[0]);
						}
						if(!this.openNodes.contains(e[1]) && !this.closedNodes.contains(e[1])) {
							this.myMap.addNode(e[1]);
							this.openNodes.add(e[1]);
						}
						this.myMap.addEdge(e[0],e[1]);
					}
					
				}
				this.sendMessage("ACK",false, msg.getSender());
			}
			 
			msg = this.myAgent.receive(msgTemplate);
		}
	}
	@Override
	public boolean done() {
		return finished;
	}

}
