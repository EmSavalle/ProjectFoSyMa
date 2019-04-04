package eu.su.mas.dedaleEtu.mas.knowledge;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.time.*;
import java.time.format.DateTimeFormatter;

import dataStructures.tuple.*;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class FSMAgentData {

	public MapRepresentation myMap= new MapRepresentation();
	public ACLMessage msg;
	public ArrayList<String> openNodes;
	public HashSet<String> closedNodes;
	public ArrayList<Entry<String,Integer>> lastComms;
	public ArrayList<Couple<String,Tuple3<String,Integer,Long>>> treasure;
	//TODO ajouter caractéristiques du trésor : quelles capacitées requises, quelles nombre d'agent
	//(((position),(type,taille,date))
	public String edge;
	public boolean switchToMsgSending;
	public boolean inComms;
	public boolean finished;
	public boolean waitingForResponse;
	private boolean verbose;
	private boolean verboseInterblock;
	private boolean verboseMapExchange;
	private boolean verboseMovement;
	public AID inCommsWith;
	public int cptTour=0;
	public int cptAttenteRetour=0;
	public String destination;
	public String objective;
	public Couple<String,String> objectiveAttribute;//Type , quantité
	public int vidage;
	public int myBackpackSize;
	public String type; //Silo / Explorer / Collector 
	public String siloPosition;
	public int initBackPackSize;
	private AbstractDedaleAgent myAgent;
	private int stuckCounter;
	private String previousPosition;
	private int stuckTreshold;
	private String actualProtocol;
	public String desiredPosition;
	private String[] path ;
	private Couple<String, String> allyAttributes;
	private String allyObjective;
	private String[] allyPath ;
	private String allyDestination;
	private int allyBackPackSpace;
	private int allyVidage;
	private String giveWayPosition;
	private String thanksAt;
	private int cptUntilRestart;
	private ArrayList<String> voisin;
	
	public FSMAgentData(String type,int backpack, AbstractDedaleAgent ag){
		this.verbose = false;
		this.verboseInterblock = false;
		this.verboseMapExchange = false;
		this.verboseMovement = true;
		this.vidage = 0;
		this.objective = "explore";
		this.objectiveAttribute = new Couple<String,String>("","");
		this.actualProtocol = "";
		this.desiredPosition = "";
		this.inCommsWith = null;
		this.siloPosition = "";
		this.type = type;
		this.initBackPackSize = 0;
		this.myBackpackSize=backpack;
		this.destination = "";
		this.myMap= new MapRepresentation();
		this.msg = null;
		this.inComms = false;
		this.waitingForResponse= false;
		this.finished = false;
		this.switchToMsgSending=false;
		this.openNodes=new ArrayList<String>();
		this.voisin=new ArrayList<String>();
		this.closedNodes=new HashSet<String>();
		this.lastComms= new java.util.ArrayList<Entry<String,Integer>>();
		this.treasure= new java.util.ArrayList<Couple<String,Tuple3<String,Integer,Long >>>();
		this.edge = "";
		this.myAgent = ag;
		this.stuckCounter = 0;
		this.previousPosition="";
		this.stuckTreshold = 2;
	}
	public int getBackPackSize() {
		return 0;
		//return this.myAgent.getBackPackFreeSpace();
	}
	public void setBackPackSize(int s) {
		this.myBackpackSize = s;		
	}
	public boolean setTreasure(String position, String type, int value , long l) {
		for(int i = 0 ; i < this.getNbTreasure() ; i ++) {
			if(this.treasure.get(i).getLeft() == position) {
				if(this.treasure.get(i).getRight().getThird() > l) {
					Tuple3<String, Integer, Long> v = new Tuple3<String,Integer,Long>(type,value,l);
					Couple<String,Tuple3<String,Integer,Long>> e = new Couple<String,Tuple3<String,Integer,Long>>(position,v);
					this.treasure.set(i, e);
					return true;
				}
				return false;
			}
		}
		Tuple3<String, Integer, Long> v = new Tuple3<String,Integer,Long>(type,value,l);
		Couple<String,Tuple3<String,Integer,Long>> e = new Couple<String,Tuple3<String,Integer,Long>>(position,v);
		this.treasure.add(e);
		return false;
	}
	public boolean verifyTreasure(String myPosition) {
		for(int i = 0 ; i < this.getNbTreasure() ; i ++) {
			if(this.treasure.get(i).getLeft() == myPosition) {
				return true;
			}
		}
		return false;
	}
	public boolean deleteTreasure(String pos) {
		for(int i = 0 ; i < this.getNbTreasure() ; i ++) {
			if(this.treasure.get(i).getLeft() == pos) {
				this.treasure.remove(i);
				return true;
			}
		}
		return false;
		
	}
	public int getNbTreasure() {
		return this.treasure.size();
	}
	public Couple<String,Tuple3<String,Integer,Long>> getTreasure(int i){
		return this.treasure.get(i);
	}
	public String getPositionBestTreasureForMe(String myPos , int size, int type) {
		/*
		 * Different types : 
		 * 0 : get closest treasure
		 * 1 : get the closest treasure that i can empty
		 * 2 : get the best treasure by value
		 * */
		if(this.getNbTreasure() == 0) {
			return "NoTreasure";
		}
		if(type == 0) {
			int iMin = -1;
			int dist = -1;
			String pos = "";
			for(int i = 0 ; i < this.getNbTreasure() ; i++) {
				String p = this.treasure.get(i).getLeft();
				int d = this.dist(myPos, p);
				if(d < dist || iMin ==-1) {
					iMin = i;
					pos = p;
				}
			}
			return pos;
		}
		else if(type == 1) {
			int iMin = -1;
			int dist = -1;
			String pos = "";
			for(int i = 0 ; i < this.getNbTreasure() ; i++) {
				String p = this.treasure.get(i).getLeft();
				int d = this.dist(myPos, p);
				if((this.treasure.get(i).getRight().getSecond() < size  && d < dist) || iMin ==-1) {
					iMin = i;
					pos = p;
				}
			}
			if(iMin == -1) {
				for(int i = 0 ; i < this.getNbTreasure() ; i++) {
					String p = this.treasure.get(i).getLeft();
					int d = this.dist(myPos, p);
					if(d < dist || iMin ==-1) {
						iMin = i;
						pos = p;
					}
				}
			}
			return pos;
		}
		else {
			return this.treasure.get(0).getLeft();
		}
	}
	public String getDestination() {
		return this.destination;
	}
	public void setDestination(String dest) {
		this.destination = dest;
	}
	public String getLastCommsKey(int i) {
		if(i>this.lastComms.size()) {
			return null;
		}
		Entry<String,Integer> co = this.lastComms.get(i); 
		return co.getKey();
	}
	public int getLastCommsValue(int i) {
		if(i>this.lastComms.size()) {
			return -1;
		}
		Entry<String,Integer> co =this.lastComms.get(i); 
		return co.getValue();
	}	 
	
	public void setCpt(int c) {
		this.cptTour = c;
	}
	public int getCpt() {
		return cptTour;
	}
	public void setMsg(ACLMessage m) {
		this.msg = m;
	}
	public ACLMessage getMsg() {
		return this.msg;
	}
	public void addNode(String node) {
		this.myMap.addNode(node);
		this.closedNodes.add(node);
	}
	public void addNode(String node , MapAttribute m) {
		if(m== MapAttribute.open) {
			this.openNodes.add(node);
		}
		this.myMap.addNode(node, m);
	}
	public void removeNode(String node) {
		this.openNodes.remove(node);
	}
	private void cleanOpenNodes() {
		for(int i = 0 ; i < this.openNodes.size() ; i++) {
			if(this.closedNodes.contains(this.openNodes.get(i))) {
				this.openNodes.remove(this.openNodes.get(i));
			}
		}
		
	}
	public void addEdge(String position,String edge) {
		this.edge=this.edge+" "+position+"-"+edge;
		this.myMap.addEdge(position, edge);
	}
	public String[] getNeighbour(String n) {
		List<String> a = new ArrayList<String>();
		String[] e = this.edge.split(" ");
		for(int i = 0 ; i < e.length ; i++) {
			if(e[i].contains(n)) {
				String[] ed = e[i].split("-");
				if(ed[0]==n) {
					a.add(ed[1]);
				}else {
					a.add(ed[0]);
				}
			}
		}
		return (String[]) a.toArray();
		
	}
	public MapRepresentation getMap() {
		return this.myMap;
	}
	public String getEdge(String node) {
		String edges ="";
		for (String e : this.edge.split(" ")) {
			if (e.contains(node)){
				edges= edges+" "+e;
			}
		}
		return edges;
		
	}

	public int dist(String p1, String p2) {
		if(p1.contains("_") && p2.contains("_")) {
			System.out.println("Dist");
			System.out.println(p1+":"+p2);
			int x1 = Integer.parseInt(p1.split("_")[0]);
			int x2 = Integer.parseInt(p2.split("_")[0]);
			int y1 = Integer.parseInt(p1.split("_")[1]);
			int y2 = Integer.parseInt(p2.split("_")[1]);
			return Math.abs((x2-x1)+(y2-y1));
		}else {
			return this.myMap.getShortestPath(p1, p2).size();
		}
		
	}
	public void observation() {
		//TODO A tester
		//TODO verifier fonctionnement observation
		boolean waitForObs=false;
		String myPosition="";
		List<Couple<String,List<Couple<Observation,Integer>>>> lobs = new ArrayList<Couple<String,List<Couple<Observation,Integer>>>>();
		while(waitForObs == false) {
			try {
				myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
				lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
				waitForObs = true;
			}catch (Exception e){
				waitForObs = false;
			}
		}
		
		
		/**
		 * Just added here to let you see what the agent is doing, otherwise he will be too quick
		 */
		try {
			this.myAgent.doWait(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//1) remove the current node from openlist and add it to closedNodes.
		this.addNode(myPosition);
		this.removeNode(myPosition);
	
		//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
		String nextNode=null;
		Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
		this.voisin = new ArrayList<String>();
		System.out.println("I see : ");
		while(iter.hasNext()){
			Couple<String, List<Couple<Observation,Integer>>> it = iter.next();
			String nodeId=it.getLeft();
			System.out.println(it.getRight());
			this.voisin.add(nodeId);
			if (!this.closedNodes.contains(nodeId)){
				if (!this.openNodes.contains(nodeId)){
					this.openNodes.add(nodeId);
					this.addNode(nodeId, MapAttribute.open);
					this.addEdge(myPosition, nodeId);	
					this.addEdge(myPosition,nodeId);
					//this.sendMessage("LINK "+myPosition+" "+ nodeId,false,"Explo");
				}else{
					//the node exist, but not necessarily the edge
					this.addEdge(myPosition, nodeId);
				}
				
			}
			for(int i = 0 ; i < it.getRight().size() ; i++) {
				this.setTreasure(nodeId, it.getRight().get(i).getLeft().toString(), it.getRight().get(i).getRight(), LocalTime.now().toNanoOfDay());
			}
			
			
			//TODO ajouter enregistrement des trésors détécter
		}
	}
	
	public void mapExchangeProtocol() {
		//TODO A tester
		if(this.waitingForResponse == false && this.inComms == false && this.switchToMsgSending == true) {	
			this.actualProtocol="MapExchange";
			this.sendMessage("MapExchange PING", false, "Explo");
			//System.out.println("Sending ping for map exchange");
			this.waitingForResponse = true;
			this.cptAttenteRetour=0;
			this.switchToMsgSending =false;
		}
		else if(this.cptAttenteRetour > 2){
			//System.out.println("Waiting for response ended");
			if(this.verbose==true)
				System.out.println(this.myAgent.getName() + " : Debloquage");

			this.actualProtocol = "";
			this.waitingForResponse = false;
			this.cptAttenteRetour = 0;
		}
		else {
			//System.out.println("Waiting for response");
			//System.out.println(this.data.cptAttenteRetour);
			this.cptAttenteRetour=this.cptAttenteRetour+1;
		}
	}
	//TODO ajouter fonction envoi données tresor et silo
	
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
		    	 if(agentID.getName().contains(this.myAgent.getName()) || this.myAgent.getName().contains(agentID.getName())) {
		    		 if(allWme == true) {
			    		 msg.addReceiver(agentID);  
		    		 }
		    		 
		    	 }
		    	 else {
		    		 msg.addReceiver(agentID);  
		    		 if(this.verbose)
		    			 System.out.println(this.myAgent.getName()+ " : "+agentID.getName()+ " will receive a message");
		    	 }
		     }
		}
			
		//2° compute the random value		
		msg.setContent(content);
		((AbstractDedaleAgent) this.myAgent).sendMessage(msg);
		if(this.verbose)
			System.out.println(this.myAgent.getName()+ ": "+content);
			
		
	}
	public void sendMessage(String content, AID name) {
		//1) receive the message
		final MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);		
		int nbAsked = 0;
	
		final ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setSender(this.myAgent.getAID());
	
	    msg.addReceiver(name);
			
		//2° compute the random value		
		msg.setContent(content);
		((AbstractDedaleAgent) this.myAgent).sendMessage(msg);
		System.out.println(this.myAgent.getName()+ ": "+content);
			
		
	}
	public void treatMessageForMapExchange(ACLMessage msg) {
		//TODO A tester
		String[] parts = msg.getContent().substring(msg.getContent().indexOf(' ') + 1).split("\\s+");
		if(parts[0].contains("ACK")) {
			this.actualProtocol = "";
			if(this.verboseMapExchange)
				System.out.println(this.myAgent.getName() + " : Ack acknowledged");
			this.inComms = false;
			this.inCommsWith = null;
			if(this.type=="explore") {
				this.destination = this.findDestination();
			}
			
			Entry<String,Integer> pair1=new AbstractMap.SimpleEntry<String,Integer>(msg.getSender().getName(),this.cptTour);
			this.lastComms.add(pair1);
		}
		else if(parts[0].contains("PING")) {
			this.actualProtocol = "MapExchange";
			if(this.verboseMapExchange)
				System.out.println(this.myAgent.getName() + " : Ping acknowledged");
			
			boolean alreadyTold = false;
			for(int i = 0 ; i < this.lastComms.size() ; i++) {
				if(this.getLastCommsKey(i) == msg.getSender().getName()) {
					if(this.cptTour - this.getLastCommsValue(i) < 10) {
						alreadyTold = true;
					}
				}
			}
			if(alreadyTold) {
				this.sendMessage("MapExchange ACK", msg.getSender());
			}
			else {
				this.inComms = true;
				this.sendMessage("MapExchange ASK", msg.getSender());
			}
		}
		else if (parts[0].contains("ASK")) {
			this.actualProtocol = "MapExchange";
			this.waitingForResponse = false;
			if(this.verboseMapExchange)
				System.out.println(this.myAgent.getName() + " : Ask acknowledged");
			
			String content = "EXPLORE ";
			if(this.verboseMapExchange)
				System.out.println(this.closedNodes.size());
			if(this.closedNodes.size() != 0) {
				this.inComms = true;
				this.waitingForResponse = false;
				for (int i = 0 ; i<this.closedNodes.size() ; i++) {
					content = content +this.closedNodes.toArray()[i] +"/";
					content.substring(0,content.length()-1);
				}
				this.sendMessage("MapExchange "+content, msg.getSender());
				if(this.verboseMapExchange)
					System.out.println(this.myAgent.getName()+" : Send "+content);
			}
			else {
				if(this.verboseMapExchange)
					System.out.println("Rien a envoyer");
				this.sendMessage("MapExchange ACK", msg.getSender());
				this.inComms = false;
				this.actualProtocol = "";
			}
			
			
			
		}
		else if(parts[0].contains("EXPLORE")) {
			if(this.verboseMapExchange)
				System.out.println(this.myAgent.getName() + " : EXPLORE acknowledged");
			
			this.inComms = true;
			if(this.verboseMapExchange)
				System.out.println(this.myAgent.getName()+" : Received EXPLORE");
			String content = "EXPLAIN ";
			String edgeContent = "";
			String noeud = "";
			String[] noeuds = parts[1].split("/");

			boolean cpt = false;
			for (int i = 0 ; i<noeuds.length ; i++) {
				if(!this.closedNodes.contains(noeuds[i])) {
					cpt=true;
					content = content +noeuds[i] +"/";
					content.substring(0,content.length()-1);
				}
			}
			if(cpt == true) {
				this.sendMessage("MapExchange "+content, msg.getSender());
				if(this.verboseMapExchange)
					System.out.println(this.myAgent.getName()+" : Send "+content);					
			}
			content = "UPDATE ";
			edgeContent = "";
			noeud = "";
			cpt = false;
			for (int i = 0 ; i<this.closedNodes.size() ; i++) {
				noeud = (String) this.closedNodes.toArray()[i].toString();
				if(this.verboseMapExchange)
					System.out.println("a|"+noeud+"|");
				if(!parts[1].contains(noeud)) {
					cpt=true;
					content = content +noeud +"/";
					content.substring(0,content.length()-1);
					String[] e = this.edge.split(" ");
					for (int j = 0 ; j<e.length ; j++) {
						if(e[j].contains(noeud)) {
							if(this.verboseMapExchange)
								System.out.println("b|"+e[j]+"|");
							edgeContent = edgeContent +e[j] +"/";
							edgeContent.substring(0,edgeContent.length()-1);
						}
					}
				}
			}
			if(cpt == true) {
				content= content + " "+edgeContent;
				this.sendMessage("MapExchange "+content, msg.getSender());
				if(this.verboseMapExchange)
					System.out.println(this.myAgent.getName()+" : Send "+content);					
			}
		}else if(parts[0].contains("EXPLAIN")) {
			if(this.verboseMapExchange)
				System.out.println(this.myAgent.getName() + " : EXPLORE acknowledged");
			
			this.inComms = true;
			if(this.verboseMapExchange)
				System.out.println(this.myAgent.getName()+" : Received EXPLORE");
			String content = "UPDATE "+parts[1]+" ";
			System.out.println("c|"+parts[1]+"|");
			String edgeContent = "";
			String noeud = "";
			String[] noeuds = parts[1].split("/");

			boolean cpt = false;
			String[] edges = this.edge.split(" ");
			for (int i = 0 ; i<noeuds.length ; i++) {
				for(int j = 0 ; j < edges.length ; j++) {
					if(edges[j].contains(noeuds[i]))
					{
						if(this.verboseMapExchange)
							System.out.println("d|"+edges[j]+"|");
						edgeContent = edgeContent +edges[j] +"/";
						edgeContent.substring(0,edgeContent.length()-1);
					}
				}
			}
			if(cpt == true) {
				content= content + " "+edgeContent;
				this.sendMessage("MapExchange "+content, msg.getSender());
				if(this.verboseMapExchange)
					System.out.println(this.myAgent.getName()+" : Send "+content);					
			}
			content = "UPDATE ";
			edgeContent = "";
			noeud = "";
			cpt = false;
			for (int i = 0 ; i<this.closedNodes.size() ; i++) {
				noeud = (String) this.closedNodes.toArray()[i].toString();
				if(!parts[1].contains(noeud)) {
					cpt=true;
					if(this.verboseMapExchange)
						System.out.println("e|"+noeud+"|");
					content = content +noeud +"/";
					content.substring(0,content.length()-1);
					String[] e = this.edge.split(" ");
					for (int j = 0 ; j<e.length ; j++) {
						if(e[j].contains(noeud)) {
							if(this.verboseMapExchange)
								System.out.println("f|"+e[j]+"|");
							edgeContent = edgeContent +e[j] +"/";
							content.substring(0,content.length()-1);
						}
					}
				}
			}
			if(cpt == true) {
				content= content + " "+edgeContent;
				this.sendMessage("MapExchange "+content, msg.getSender());
				if(this.verboseMapExchange)
					System.out.println(this.myAgent.getName()+" : Send "+content);					
			}
			this.inComms = false;
			this.actualProtocol = "";
			this.inCommsWith = null;
		}
		else if(parts[0].contains("UPDATE")) {
			if(this.verboseMapExchange)
				System.out.println(this.myAgent.getName() + " : Update acknowledged");
			
			this.inComms = false;
			this.inCommsWith = null;
			this.actualProtocol = "";
			String[] noeuds = parts[1].split("/");
			String[] edges = parts[2].split("/");
			for (int i = 0; i < noeuds.length ; i++) {
				this.addNode(noeuds[i]);
				if(this.openNodes.contains(noeuds[i])) {
					this.openNodes.remove(noeuds[i]);
				}
			}
			for (int i = 0; i < edges.length; i++) {
				String[] e = edges[i].split("-");
				if(this.verboseMapExchange)
					System.out.println("Ajoute noeud et arc entre "+e[0] + " et "+e[1]);
				if(!this.openNodes.contains(e[0]) && !this.closedNodes.contains(e[0])) {
					this.addNode(e[0]);
				}
				if(!this.openNodes.contains(e[1]) && !this.closedNodes.contains(e[1])) {
					this.addNode(e[1], MapAttribute.open);
					this.openNodes.add(e[1]);
				}
				this.addEdge(e[0],e[1]);
				
			}
			this.sendMessage("MapExchange ACK", msg.getSender());
			if(this.type=="explore") {
				this.destination = this.findDestination();
			}
			this.cleanOpenNodes();
		}
		 
	}
	private void treatMessageForInterBlock(ACLMessage msg2) {
		//TODO A tester
		String[] parts = msg2.getContent().substring(msg2.getContent().indexOf(' ') + 1).split("\\s+");
		if(this.verboseInterblock==true) {
			System.out.println("Treating msg for interblock : -"+ msg2.getContent().substring(msg2.getContent().indexOf(' ') + 1)+"-");
			System.out.println("Type of interblock msg : -"+parts[0]+"-");
		}
		if(parts[0].equals("STUCK")) {
			if(parts[1].equals(this.myAgent.getCurrentPosition())) {
				if(this.inCommsWith == msg2.getSender()) {
					//already started no need to continue this line of communication
					System.out.println("Already in comms with "+msg.getSender());
				}else {
					this.actualProtocol = "InterBlock";
					this.inCommsWith = msg2.getSender();
					this.inComms = true;
					this.sendMessage("InterBlock RSTUCK "+this.desiredPosition, msg2.getSender());
					
					if(this.verboseInterblock==true) 
						System.out.println(this.myAgent.getName() + " send RSTUCK");
				}
				
			}
		}else if(parts[0].equals("RSTUCK")) {
			if(this.inCommsWith.equals(msg2.getSender())) {
				//already started no need to continue this line of communication
				if(this.verboseInterblock) 
					System.out.println("Already in comms with "+msg.getSender());
			}else {
				this.inComms = true;
				this.inCommsWith = msg2.getSender();
			}
			String content = "InterBlock OBJECTIVE [";
			content+=this.objective+","+this.objectiveAttribute.getLeft()+","+this.objectiveAttribute.getRight()+","+Integer.toString(this.vidage)+"] ";
			content+=this.destination+" ";
			Object[] a= this.myMap.getShortestPath(this.desiredPosition, this.destination).toArray();
			this.path =  Arrays.copyOf(a, a.length, String[].class);
			content+="[";
			boolean emptyPath = true;
			for(int i = 0 ; i < this.myMap.getShortestPath(this.desiredPosition, this.destination).size() ; i ++) {
				content+=this.myMap.getShortestPath(this.desiredPosition, this.destination).get(i)+"/";
				emptyPath = false;
			}
			if(!emptyPath)
				content = content.substring(0,content.length()-1)+"] ";
			else
				content+="] ";
			content+= Integer.toString(this.getBackPackSize()) ;
			if(this.verboseInterblock) 
				System.out.println(this.myAgent.getName() + " send OBJECTIVE");
			this.sendMessage(content, msg2.getSender());
		}else if(parts[0].equals("OBJECTIVE")) {
			String objAttr = parts[1].substring(1, parts[1].length()-1);
			this.allyObjective = objAttr.split(" ")[0];
			if(this.allyObjective.equals("collect"))
				this.allyAttributes = new Couple<String,String>(objAttr.split(" ")[1],objAttr.split(" ")[2]);
			else
				this.allyAttributes = new Couple<String,String>("","");
			if(this.allyObjective.equals("collect")) {
				this.allyVidage = Integer.parseInt(objAttr.split(" ")[3]);
			}
			this.allyDestination = parts[2];
			this.allyPath = parts[3].split("/");
			this.allyBackPackSpace = Integer.parseInt(parts[4]);
			boolean emptyPath = true;
			String content = "InterBlock ROBJECTIVE [";
			content+=this.objective+","+this.objectiveAttribute.getLeft()+","+this.objectiveAttribute.getRight()+","+Integer.toString(this.vidage)+"] ";
			content+=this.destination+" [";
			for(int i = 0 ; i < this.myMap.getShortestPath(this.desiredPosition, this.destination).size() ; i ++) {
				content+=this.myMap.getShortestPath(this.desiredPosition, this.destination).get(i)+"/";
				emptyPath = false;
			}
			if(!emptyPath)
				content = content.substring(0,content.length()-1)+"] ";
			else
				content+="] ";
			content+= Integer.toString(this.getBackPackSize()) ;
			this.sendMessage(content, msg2.getSender());
			this.actualProtocol="InterBlockWaitSolution";
			if(this.verboseInterblock) 
				System.out.println(this.myAgent.getName() + " send ROBJECTIVE");
		}else if(parts[0].equals("ROBJECTIVE")) {
			if(this.verboseInterblock) 
				System.out.println("ROBJ treat");
			String objAttr = parts[1].substring(1, parts[1].length()-1);
			this.allyObjective = objAttr.split(",")[0];
			if(this.allyObjective.equals("collect"))
				this.allyAttributes = new Couple<String,String>(objAttr.split(" ")[1],objAttr.split(" ")[2]);
			else
				this.allyAttributes = new Couple<String,String>("","");
			if(this.allyObjective.equals("collect")) {
				this.allyVidage = Integer.parseInt(objAttr.split(" ")[3]);
			}
			this.allyDestination = parts[2];
			this.allyPath = parts[3].split("/");
			this.allyBackPackSpace = Integer.parseInt(parts[4]);
			this.actualProtocol="InterBlockFindSolution";
			this.findInterBlockSolution(msg2.getSender());
		}
		else if(parts[0].equals("WAIT")) {
			this.actualProtocol = "InterBlockWaiting";
			this.cptUntilRestart = 0;
			this.thanksAt = parts[1];
		}
		else if(parts[0].equals("GO")) {
			this.actualProtocol = "InterBlockGiveWay";
			this.giveWayPosition = parts[1];
		}
		else if(parts[0].equals("SWAP")) {
			System.out.println("Swap analysed");
			this.actualProtocol="";
			String[] objAttr = parts[1].substring(1, parts[1].length()-1).split(",");
			this.objective = objAttr[0];
			this.destination = parts[2];
			if(this.objective.equals("collect")) {
				this.objectiveAttribute = new Couple<String,String>(objAttr[1],objAttr[2]);
			}
		}else if(parts[0].equals("MapExchange")) {
			System.out.println("Sending map");
			msg2.setContent(msg2.getContent().substring(msg2.getContent().indexOf(' ') + 1));
			this.msg=msg2;
			this.actualProtocol="";
			this.setDestination("");
			this.getMessage();
		}
		else {
			if(this.verboseInterblock) 
				System.out.println("Don't know what to do with : Type of interblock msg : -"+parts[0]+"-");
		}
		
	}
	private String findInterBlockSolution(AID sender) {
		//TODO A tester
		if(this.verboseInterblock) {
			System.out.println("Finding solution");
			System.out.println(this.objective);
			System.out.println(this.allyObjective);
		}
		if(this.objective.equals(this.allyObjective)) {

			if(this.verboseInterblock) 
				System.out.println("equal obj");
			if(this.objective.equals("explore")) {
				this.actualProtocol="MapExchange";
				if(this.verboseInterblock) 
					System.out.println("2 explore colliding swapping map");
				this.sendMessage("InterBlock MapExchange ASK", sender);
				this.setDestination("");
				return "Destination SWAP "+this.objective;
				//TODO remove incommsWith
			}
			else if(this.objective.equals("collect")) {
				if(this.vidage == this.allyVidage && this.vidage == 0) {
					//TODO penser a un meilleur moyen qu'echanger les destinations lorsque les deux se bloquant vont chercher un tresor sans le vider
					this.sendMessage("InterBlock SWAP ["+this.objective+ "," +this.objectiveAttribute.getLeft() +","+this.objectiveAttribute.getRight() +","+Integer.toString(this.vidage)+ "] " + this.destination, sender); 
					this.setDestination(this.allyDestination);
					this.objectiveAttribute=this.allyAttributes;
					if(this.verboseInterblock) 
						System.out.println("SWAPING Obj");
					return "Destination SWAP "+this.objective;
				}
				else if(this.vidage == this.allyVidage && this.vidage == 1 && this.allyBackPackSpace >= Integer.parseInt(this.objectiveAttribute.getRight()) && this.getBackPackSize() >= Integer.parseInt(this.allyAttributes.getRight())) {
					this.sendMessage("InterBlock SWAP ["+this.objective+ "," +this.objectiveAttribute.getLeft() +","+this.objectiveAttribute.getRight() +","+Integer.toString(this.vidage)+ "] " + this.destination, sender);
					this.setDestination(this.allyDestination);
					this.objective=this.allyObjective;
					this.objectiveAttribute = this.allyAttributes;
					if(this.verboseInterblock) 
						System.out.println("SWAPING Obj");
					return "Destination SWAP "+this.objective;
				}
			}
			else {
				if(this.verboseInterblock) 
					System.out.println("Protocole evitement");
				//Protocole d'évitement
				//Test si c'est moi qui bouge
				String[] neigh = this.getNeighbour(this.myAgent.getCurrentPosition());
				String noeudEvitement="";
				String[] ch = this.allyPath.clone();
				List<String> c = Arrays.asList(ch);
				c.remove(0);
				ch = (String[]) c.toArray();
				int dist = 0;
				boolean isThereNodeNotInPath= false;
				String noeudActu="";
				while(isThereNodeNotInPath == false) {
					noeudActu = ch[0];
					dist+=1;
					for(int j = 0 ; j < neigh.length ; j++) {
						if(!Arrays.asList(this.allyPath).contains(neigh[j])) {
							noeudEvitement = neigh[j];
							isThereNodeNotInPath = true;
							break;
						}
					}
					neigh = this.getNeighbour(ch[0]);
					c = Arrays.asList(ch);
					c.remove(0);
					ch = (String[]) c.toArray();
				}
				if(noeudEvitement.equals("")) {
					dist = 100000;
				}
				//Test si c'est lui qui bouge
				String[] neigh1 = this.getNeighbour(this.desiredPosition);
				String noeudEvitement1="";
				String[] ch1 = this.path.clone();
				List<String> c1 = Arrays.asList(ch1);
				c1.remove(0);
				ch1 = (String[]) c1.toArray();
				int dist1 = 0;
				String noeudActu1="";
				boolean isThereNodeNotInPath1= false;
				while(isThereNodeNotInPath1 == false) {
					noeudActu1 = ch1[0];
					dist1+=1;
					for(int j = 0 ; j < neigh1.length ; j++) {
						if(!Arrays.asList(this.path).contains(neigh1[j])) {
							noeudEvitement1 = neigh1[j];
							isThereNodeNotInPath1 = true;
							break;
						}
					}
					neigh1 = this.getNeighbour(ch1[0]);
					c1 = Arrays.asList(ch1);
					c1.remove(0);
					ch1 = (String[]) c1.toArray();
				}
				if(noeudEvitement1.equals("")) {
					dist1 = 100000;
				}
				if(dist <= dist1) {
					//Moi qui bouge
					this.sendMessage("InterBlock WAIT "+noeudActu, this.inCommsWith);
					if(this.verboseInterblock) 
						System.out.println("Wait here while i go at "+noeudActu);
				}else {
					//Toi qui bouge
					this.sendMessage("InterBlock GO " + noeudEvitement1, this.inCommsWith);
					if(this.verboseInterblock) 
						System.out.println("Go here "+noeudActu);
				}
			}
		}
		return "";
	}

	private void treatMessageForInterBlockWaiting(ACLMessage msg2) {
		String[] parts = msg.getContent().substring(msg.getContent().indexOf(' ') + 1).split("\\s+");
		
	}
	private void treatMessageForInterBlockGiveWay(ACLMessage msg2) {
		//TODO A tester
		String[] parts = msg.getContent().substring(msg.getContent().indexOf(' ') + 1).split("\\s+");
		if(parts[0].equals("Thanks")){
			this.actualProtocol="";
			this.inComms=false;
			this.inCommsWith = null;
		}
		
	}
	public void getMessage() {
		final MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		ACLMessage m = null;
		//2) get the message
		if(this.msg != null) {
			m = this.msg;
		}
		else {
			m = this.myAgent.receive(msgTemplate);
			if(m == null) {
				if(this.verbose==true)
					System.out.println(this.myAgent.getName() + " : Pas de message");
			}
			
		}
		this.msg = m;

		while (this.msg != null) {	
			System.out.println(m.getContent() + " Message received");
			if(this.verbose==true)
				System.out.println(this.myAgent.getName()+" : Received "+msg.getContent() +" from "+msg.getSender().getName());	
			
			String[] parts = msg.getContent().split("\\s+");
			System.out.println("Type of msg : -"+parts[0]+"- to treat");
			if(parts[0].contains("InterBlock")) {
				this.treatMessageForInterBlock(this.msg);
			}else if(parts[0].contains("MapExchange")) {
				String[] part = parts.clone();
				this.treatMessageForMapExchange(this.msg);
			}else if(parts[0].contains("InterBlockGiveWay")) {
				this.treatMessageForInterBlockGiveWay(this.msg);
			}else if(parts[0].contains("InterBlockWaiting")) {
				this.treatMessageForInterBlockWaiting(this.msg);
			}
			//Map exchange protocol
			
			this.msg = this.myAgent.receive(msgTemplate);
		}
	}

	public boolean detectIfStuck() {
		//TODO A tester
		if(this.myAgent.getCurrentPosition() == this.previousPosition && this.actualProtocol.equals("")) {
			this.stuckCounter+=1;
			System.out.println("Stuck counter : "+Integer.toString(this.stuckCounter));
			if(this.stuckCounter > this.stuckTreshold) {
				System.out.println("#############Stuck#############");
				return true;
			}
		}else {
			this.stuckCounter=0;
		}
		this.previousPosition = this.myAgent.getCurrentPosition();
		return false;
	}
	public boolean startInterBlockProcedure() {
		//TODO A tester
		if(this.actualProtocol.equals("")) {
			this.actualProtocol="InterBlock";
			this.sendMessage("InterBlock STUCK "+this.desiredPosition,false, "");
			return false;
		}
		return true;
	}
	
	public void movement() {
		//TODO A tester

		if(this.objective=="explore") {
			this.destination = this.findDestination();
		}
		if(this.verboseMovement) {
			System.out.println("Movement :");
			System.out.println(this.myAgent.getCurrentPosition());
			System.out.println(this.destination);
			System.out.println(this.actualProtocol);
			System.out.println(this.type);
			System.out.println(this.objective);
		}
		if(this.detectIfStuck()) {
			this.startInterBlockProcedure();
		}
		this.cptTour++;
		if(this.actualProtocol == "InterBlockWaiting") {
			this.cptUntilRestart +=1;
			if(this.cptUntilRestart > 3) {
				if(this.myAgent.getCurrentPosition()==this.thanksAt) {
					this.sendMessage("InterBlockGiveWay Thanks", this.inCommsWith);
					this.actualProtocol = "";
					this.inComms = false;
					this.inCommsWith = null;
					this.desiredPosition = this.myMap.getShortestPath(this.myAgent.getCurrentPosition(), this.destination).get(0);
					((AbstractDedaleAgent)this.myAgent).moveTo(this.desiredPosition);
				}
				else {
					this.desiredPosition = this.myMap.getShortestPath(this.myAgent.getCurrentPosition(), this.thanksAt).get(0);
					((AbstractDedaleAgent)this.myAgent).moveTo(this.desiredPosition);
				}
				
			}
		}else if(this.actualProtocol == "InterBlockGiveWay") {
			if(this.giveWayPosition != this.myAgent.getCurrentPosition()) {
				//TODO verifier qu'on a toujours une position ou se deplacer
				this.desiredPosition = this.myMap.getShortestPath(this.myAgent.getCurrentPosition(), this.giveWayPosition).get(0);
				((AbstractDedaleAgent)this.myAgent).moveTo(this.desiredPosition);
			}
		}else if(this.actualProtocol == "InterBlock" || this.actualProtocol == "MapExchange" || this.inComms || this.waitingForResponse){
			this.getMessage();
		}else {
			String nextNode=null;
			if(this.destination == "") {
				this.destination = this.findDestination();
			}
			if(this.voisin.contains(this.destination)) {
				nextNode = this.destination;
			}else {
				while (nextNode==null){
					nextNode=this.myMap.getShortestPath(this.myAgent.getCurrentPosition(), this.destination).get(0);
				}
			}
			if(this.verboseMovement) {
				System.out.println("Pre Movement :");
				System.out.println(this.myAgent.getCurrentPosition());
				System.out.println(this.destination);
				System.out.println(this.desiredPosition);
			}
			this.desiredPosition = nextNode;
			((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			
		}
	}
	private String findDestination() {
		if(this.objective.equals("explore")) {
			System.out.println("Find new Dest");
			if(this.openNodes.size()<1) {
				System.out.println("Nothing to check");
				return "";
			}
			int iMin=-1;
			int distMin=0;
			for (int i = 0 ; i < this.openNodes.size() ; i++) {
				int a = this.dist(this.myAgent.getCurrentPosition(),this.openNodes.get(i));
				if(iMin == -1 || a < distMin) {
					iMin = i;
					distMin = a;
				}
			}
			System.out.println("New dest find"+this.openNodes.get(iMin));
			return this.openNodes.get(iMin);
		}
		return "";
	}
}
