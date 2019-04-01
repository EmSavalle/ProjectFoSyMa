package eu.su.mas.dedaleEtu.mas.knowledge;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

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
	public ArrayList<Couple<String,Tuple3<String,Integer,Integer>>> treasure;
	//TODO ajouter caractéristiques du trésor : quelles capacitées requises, quelles nombre d'agent
	//(((position),(type,taille,date))
	public String edge;
	public boolean switchToMsgSending;
	public boolean inComms;
	public AID inCommsWith;
	public boolean finished;
	public boolean waitingForResponse;
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
	private boolean verbose;
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
	
	public FSMAgentData(String type,int backpack, AbstractDedaleAgent ag){
		this.vidage = 0;
		this.objective = "explore";
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
		this.closedNodes=new HashSet<String>();
		this.lastComms= new java.util.ArrayList<Entry<String,Integer>>();
		this.treasure= new java.util.ArrayList<Couple<String,Tuple3<String,Integer,Integer>>>();
		this.edge = "";
		this.myAgent = ag;
		this.stuckCounter = 0;
		this.previousPosition="";
		this.stuckTreshold = 2;
	}
	public int getBackPackSize() {
		return this.myAgent.getBackPackFreeSpace();
	}
	public void setBackPackSize(int s) {
		this.myBackpackSize = s;		
	}
	public boolean setTreasure(String position, String type, int value , int date) {
		for(int i = 0 ; i < this.getNbTreasure() ; i ++) {
			if(this.treasure.get(i).getLeft() == position) {
				if(this.treasure.get(i).getRight().getThird() > date) {
					Tuple3<String,Integer,Integer> v = new Tuple3<String,Integer,Integer>(type,value,date);
					Couple<String,Tuple3<String,Integer,Integer>> e = new Couple<String,Tuple3<String,Integer,Integer>>(position,v);
					this.treasure.set(i, e);
					return true;
				}
				return false;
			}
		}
		Tuple3<String,Integer,Integer> v = new Tuple3<String,Integer,Integer>(type,value,date);
		Couple<String,Tuple3<String,Integer,Integer>> e = new Couple<String,Tuple3<String,Integer,Integer>>(position,v);
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
	public Couple<String,Tuple3<String,Integer,Integer>> getTreasure(int i){
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
		int x1 = Integer.parseInt(p1.split("-")[0]);
		int x2 = Integer.parseInt(p2.split("-")[0]);
		int y1 = Integer.parseInt(p1.split("-")[1]);
		int y2 = Integer.parseInt(p2.split("-")[1]);
		return Math.abs((x2-x1)+(y2-y1));
	}
	public void observation() {
		//TODO A tester
		//TODO verifier fonctionnement observation
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
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
		this.addNode(myPosition);
		this.removeNode(myPosition);
	
		this.addNode(myPosition);
		//this.sendMessage("IN "+myPosition,false,"Explo");
	
		//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
		String nextNode=null;
		Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
		while(iter.hasNext()){
			String nodeId=iter.next().getLeft();
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
		    		 System.out.println(this.myAgent.getName()+ " : "+agentID.getName()+ " will receive a message");
		    	 }
		     }
		}
			
		//2° compute the random value		
		msg.setContent(content);
		((AbstractDedaleAgent) this.myAgent).sendMessage(msg);
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
		if(parts[0].contains("MapExchange ACK")) {
			this.actualProtocol = "";
			if(this.verbose==true)
				System.out.println(this.myAgent.getName() + " : Ack acknowledged");
			this.inComms = false;
			Entry<String,Integer> pair1=new AbstractMap.SimpleEntry<String,Integer>(msg.getSender().getName(),this.cptTour);
			this.lastComms.add(pair1);
		}
		else if(parts[0].contains("MapExchange PING")) {
			this.actualProtocol = "MapExchange";
			if(this.verbose==true)
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
		else if (parts[0].contains("MapExchange ASK")) {
			this.actualProtocol = "MapExchange";
			this.waitingForResponse = false;
			if(this.verbose==true)
				System.out.println(this.myAgent.getName() + " : Ask acknowledged");
			
			String content = "EXPLORE ";
			if(this.verbose==true)
				System.out.println(this.closedNodes.size());
			if(this.closedNodes.size() != 0) {
				this.inComms = true;
				this.waitingForResponse = false;
				for (int i = 0 ; i<this.closedNodes.size() ; i++) {
					if(i == this.closedNodes.size()-1) {
						content = content +this.closedNodes.toArray()[i];
					}
					else {
						content = content +this.closedNodes.toArray()[i] +"/";
					}
				}
				this.sendMessage("MapExchange "+content, msg.getSender());
				if(this.verbose==true)
					System.out.println(this.myAgent.getName()+" : Send "+content);
			}
			else {
				if(this.verbose==true)
					System.out.println("Rien a envoyer");
				this.sendMessage("MapExchange ACK", msg.getSender());
				this.inComms = false;
				this.actualProtocol = "";
			}
			
			
			
		}
		else if(parts[0].contains("EXPLORE")) {
			if(this.verbose==true)
				System.out.println(this.myAgent.getName() + " : EXPLORE acknowledged");
			
			this.inComms = true;
			if(this.verbose==true)
				System.out.println(this.myAgent.getName()+" : Received EXPLORE");
			String content = "EXPLAIN ";
			String edgeContent = "";
			String noeud = "";
			String[] noeuds = parts[1].split("/");

			boolean cpt = false;
			for (int i = 0 ; i<noeuds.length ; i++) {
				if(!this.closedNodes.contains(noeuds[i])) {
					cpt=true;
					if(i == this.closedNodes.size()-1) {
						content = content +noeuds[i];
					}
					else {
						content = content +noeuds[i] +"/";
					}
				}
			}
			if(cpt == true) {
				this.sendMessage("MapExchange "+content, msg.getSender());
				if(this.verbose==true)
					System.out.println(this.myAgent.getName()+" : Send "+content);					
			}
			content = "UPDATE ";
			edgeContent = "";
			noeud = "";
			cpt = false;
			for (int i = 0 ; i<this.closedNodes.size() ; i++) {
				noeud = (String) this.closedNodes.toArray()[i].toString();
				System.out.println("a|"+noeud+"|");
				if(!parts[1].contains(noeud)) {
					cpt=true;
					if(i == this.closedNodes.size()-1) {
						content = content +noeud;
					}
					else {
						content = content +noeud +"/";
					}
					String[] e = this.edge.split(" ");
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
				this.sendMessage("MapExchange "+content, msg.getSender());
				if(this.verbose==true)
					System.out.println(this.myAgent.getName()+" : Send "+content);					
			}
			this.inComms = false;
			this.actualProtocol = "";
		}else if(parts[0].contains("EXPLAIN")) {
			if(this.verbose==true)
				System.out.println(this.myAgent.getName() + " : EXPLORE acknowledged");
			
			this.inComms = true;
			if(this.verbose==true)
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
				this.sendMessage("MapExchange "+content, msg.getSender());
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
					System.out.println("e|"+noeud+"|");
					if(i == this.closedNodes.size()-1) {
						content = content +noeud;
					}
					else {
						content = content +noeud +"/";
					}
					String[] e = this.edge.split(" ");
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
				this.sendMessage("MapExchange "+content, msg.getSender());
				if(this.verbose==true)
					System.out.println(this.myAgent.getName()+" : Send "+content);					
			}
			this.inComms = false;
			this.actualProtocol = "";
		}
		else if(parts[0].contains("UPDATE")) {
			if(this.verbose==true)
				System.out.println(this.myAgent.getName() + " : Update acknowledged");
			
			this.inComms = false;
			this.actualProtocol = "";
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
				String[] e = edges[i].split("-");
				if(this.verbose==true)
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
		}
		 
	}

	private void treatMessageForInterBlock(ACLMessage msg2) {
		//TODO A tester
		String[] parts = msg.getContent().substring(msg.getContent().indexOf(' ') + 1).split("\\s+");
		if(parts[0]=="STUCK") {
			if(parts[1]==this.myAgent.getCurrentPosition()) {
				if(this.inCommsWith == msg2.getSender()) {
					//already started no need to continue this line of communication
				}else {
					this.actualProtocol = "InterBlock";
					this.inCommsWith = msg2.getSender();
					this.inComms = true;
					this.sendMessage("RSTUCK "+this.desiredPosition, msg2.getSender());
				}
				
			}
		}else if(parts[0]== "RSTUCK") {
			this.inComms = true;
			this.inCommsWith = msg2.getSender();
			String content = "OBJECTIVE [";
			content+=this.objective+","+this.objectiveAttribute.getLeft()+","+this.objectiveAttribute.getRight()+" "+Integer.toString(this.vidage)+"] ";
			content+=this.destination+" ";
			this.path = (String[])this.myMap.getShortestPath(this.desiredPosition, this.destination).toArray();
			for(int i = 0 ; i < this.myMap.getShortestPath(this.desiredPosition, this.destination).size() ; i ++) {
				content+=this.myMap.getShortestPath(this.desiredPosition, this.destination).get(i)+"/";
			}
			content = content.substring(0,content.length()-1)+" ";
			content+= Integer.toString(this.getBackPackSize()) ;
			this.sendMessage(content, msg2.getSender());
		}else if(parts[0]== "OBJECTIVE") {
			String objAttr = parts[1].substring(1, parts[1].length()-1);
			this.allyObjective = objAttr.split(" ")[0];
			this.allyAttributes = new Couple<String,String>(objAttr.split(" ")[1],objAttr.split(" ")[2]);
			if(this.allyObjective == "collect") {
				this.allyVidage = Integer.parseInt(objAttr.split(" ")[3]);
			}
			this.allyDestination = parts[2];
			this.allyPath = parts[3].split("/");
			this.allyBackPackSpace = Integer.parseInt(parts[4]);
			String content = "ROBJECTIVE [";
			content+=this.objective+","+this.objectiveAttribute+","+Integer.toString(this.vidage)+"] ";
			content+=this.destination+" ";
			for(int i = 0 ; i < this.myMap.getShortestPath(this.desiredPosition, this.destination).size() ; i ++) {
				content+=this.myMap.getShortestPath(this.desiredPosition, this.destination).get(i)+"/";
			}
			content = content.substring(0,content.length()-1)+" ";
			content+= Integer.toString(this.getBackPackSize()) ;
			this.sendMessage(content, msg2.getSender());
			this.actualProtocol="InterBlockWaitSolution";
		}else if(parts[0]== "ROBJECTIVE") {
			String objAttr = parts[1].substring(1, parts[1].length()-1);
			this.allyObjective = objAttr.split(" ")[0];
			this.allyAttributes = new Couple<String,String>(objAttr.split(" ")[1],objAttr.split(" ")[2]);
			if(this.allyObjective == "collect") {
				this.allyVidage = Integer.parseInt(objAttr.split(" ")[3]);
			}
			this.allyDestination = parts[2];
			this.allyPath = parts[3].split("/");
			this.allyBackPackSpace = Integer.parseInt(parts[4]);
			this.actualProtocol="InterBlockFindSolution";
			this.findInterBlockSolution();
		}
		else if(parts[0] == "WAIT") {
			this.actualProtocol = "InterBlockWaiting";
			this.cptUntilRestart = 0;
			this.thanksAt = parts[1];
		}
		else if(parts[0] == "GO") {
			this.actualProtocol = "InterBlockGiveWay";
			this.giveWayPosition = parts[1];
		}
		
	}
	private String findInterBlockSolution() {
		//TODO A tester
		if(this.objective == this.allyObjective) {
			if(this.objective == "explore") {
				this.actualProtocol="MapExchange";
				this.sendMessage("InterBlock SWAP "+this.objective+ " " +this.objectiveAttribute + " " + this.destination, this.inCommsWith);
				this.setDestination(this.allyDestination);
				return "Destination SWAP "+this.objective;
			}
			else if(this.objective == "collect") {
				if(this.vidage == this.allyVidage && this.vidage == 0) {
					//TODO penser a un meilleur moyen qu'echanger les destinations lorsque les deux se bloquant vont chercher un tresor sans le vider
					this.sendMessage("InterBlock SWAP "+this.objective+ " " +this.objectiveAttribute + " " + this.destination, this.inCommsWith);
					this.setDestination(this.allyDestination);
					this.objectiveAttribute=this.allyAttributes;
					return "Destination SWAP "+this.objective;
				}
				else if(this.vidage == this.allyVidage && this.vidage == 1 && this.allyBackPackSpace >= Integer.parseInt(this.objectiveAttribute.getRight()) && this.getBackPackSize() >= Integer.parseInt(this.allyAttributes.getRight())) {
					this.sendMessage("InterBlock SWAP "+this.objective+ " [" +this.objectiveAttribute.getLeft() +this.objectiveAttribute.getRight() +Integer.toString(this.vidage)+ "] " + this.destination, this.inCommsWith);
					this.setDestination(this.allyDestination);
					this.objective=this.allyObjective;
					this.objectiveAttribute = this.allyAttributes;
					return "Destination SWAP "+this.objective;
				}
			}
			else {
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
				if(dist <= dist1) {
					//Moi qui bouge
					this.sendMessage("InterBlock WAIT "+noeudActu, this.inCommsWith);
				}else {
					//Toi qui bouge
					this.sendMessage("InterBlock GO " + noeudEvitement1, this.inCommsWith);
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
		if(parts[0] == "Thanks") {
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
			if(this.verbose==true)
				System.out.println(this.myAgent.getName()+" : Received "+msg.getContent() +" from "+msg.getSender().getName());	
			
			String[] parts = msg.getContent().split("\\s+");
			if(parts[0].contains("MapExchange")) {
				String[] part = parts.clone();
				this.treatMessageForMapExchange(this.msg);
			}else if(parts[0].contains("InterBlock")) {
				this.treatMessageForInterBlock(this.msg);
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
		if(this.myAgent.getCurrentPosition() == this.previousPosition) {
			this.stuckCounter+=1;
			if(this.stuckCounter > this.stuckTreshold) {
				if(this.actualProtocol == "InterBlockGiveWay") {
					//TODO C'est chiant : si on est en train de laisser passer quelqu'un et qu'on se bloque
				}
				return true;
			}
		}
		this.previousPosition = this.myAgent.getCurrentPosition();
		this.stuckCounter = 0;
		return false;
	}
	public boolean startInterBlockProcedure() {
		//TODO A tester
		if(this.actualProtocol=="") {
			this.actualProtocol="InterBlock";
			this.sendMessage("InterBlock STUCK "+this.desiredPosition,false, "");
			return false;
		}
		return true;
	}
	
	public void movement() {
		//TODO A tester
		this.cptTour++;
		if(this.actualProtocol == "InterBlockWaiting") {
			this.cptUntilRestart +=1;
			if(this.cptUntilRestart > 3) {
				if(this.myAgent.getCurrentPosition()==this.thanksAt) {
					this.sendMessage("InterBlockGiveWay Thanks", this.inCommsWith);
					this.actualProtocol = "";
					this.inComms = false;
					this.inCommsWith = null;
					((AbstractDedaleAgent)this.myAgent).moveTo(this.myMap.getShortestPath(this.myAgent.getCurrentPosition(), this.destination).get(0));
				}
				else {
					((AbstractDedaleAgent)this.myAgent).moveTo(this.myMap.getShortestPath(this.myAgent.getCurrentPosition(), this.thanksAt).get(0));
				}
				
			}
		}else if(this.actualProtocol == "InterBlockGiveWay") {
			if(this.giveWayPosition != this.myAgent.getCurrentPosition()) {
				//TODO verifier qu'on a toujours une position ou se deplacer
				((AbstractDedaleAgent)this.myAgent).moveTo(this.myMap.getShortestPath(this.myAgent.getCurrentPosition(), this.giveWayPosition).get(0));
			}
		}else if(this.inComms || this.waitingForResponse) {
			this.getMessage();
		}else if(this.actualProtocol == "InterBlock"){
			this.getMessage();
		}else {
			String nextNode=null;
			while (nextNode==null){
				System.out.println("Null");
				//no directly accessible openNode
				//chose one, compute the path and take the first step.
				nextNode=this.myMap.getShortestPath(this.myAgent.getCurrentPosition(), this.destination).get(0);
			}
			this.desiredPosition = nextNode;
			((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			
		}
	}
}
