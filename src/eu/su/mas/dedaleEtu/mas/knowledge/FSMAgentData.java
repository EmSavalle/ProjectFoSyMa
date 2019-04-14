package eu.su.mas.dedaleEtu.mas.knowledge;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.lang.reflect.Array;
import java.time.*;
import java.time.format.DateTimeFormatter;

import dataStructures.tuple.*;
import eu.su.mas.dedale.env.EntityCharacteristics;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.mapElements.LockElement.LockType;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class FSMAgentData {

	public MapRepresentation myMap;
	public ACLMessage msg;
	public ArrayList<String> openNodes;
	public HashSet<String> closedNodes;
	public ArrayList<Entry<String,Integer>> lastComms;
	public ArrayList<Couple<String,Tuple4<String,Integer,Long,Tuple3<Integer,Integer,Integer>>>> treasure;//TODO ajouter les niveaux d'ouverture requis
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
	private boolean verboseEtatAgent;
	private boolean verboseObservation;
	private boolean verboseCollect;
	private boolean verboseEtudeTresor;
	public List<Entry<AID,String>> inCommsWith;
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
	private Set<Couple<LockType, Integer>> myExpertise;//[<LockPicking, 1>, <Strength, 1>]
	private int diamondCap;
	private int goldCap;
	private int sizeTreshold;
	
	public FSMAgentData(int backpack, AbstractDedaleAgent ag ,Object[] args){
		System.out.println(args.length);
		System.out.println(args.toString());
		EntityCharacteristics ec = (EntityCharacteristics) args[0];
		this.myExpertise = ec.getExpertise();
		this.type = ec.getMyEntityType().toString();
		System.out.println("#################################################");
		System.out.println(this.type);
		if(this.type.contains("Collect"))
			this.type = "Collect";
		if(this.type.contains("Tanker"))
			this.type = "Silo";
		if(this.type.contains("Explo"))
			this.type = "Explo";
		this.diamondCap = ec.getDiamondCapacity();
		this.goldCap = ec.getGoldCapacity();
		this.verbose = false;
		this.verboseEtatAgent = true;
		this.verboseInterblock = false;
		this.verboseMapExchange = false;
		this.verboseMovement = false;
		this.verboseObservation = false;
		this.verboseCollect = true;
		this.verboseEtudeTresor = true;
		this.vidage = 0;
		this.objectiveAttribute = new Couple<String,String>("","");
		this.actualProtocol = "";
		this.desiredPosition = "";
		this.siloPosition = "";
		if( type.equals("Explo")) {
			this.objective = "explore";
		}else if( type.equals("Collect")) {
			this.objective = "explore";
		}else if( type.equals("Silo")) {
			this.objective = "silo";
		} 
		this.initBackPackSize = 0;
		this.myBackpackSize=backpack;
		this.sizeTreshold = 5;
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
		this.inCommsWith = new ArrayList<Entry<AID,String>>();
		this.treasure= new java.util.ArrayList<Couple<String,Tuple4<String,Integer,Long,Tuple3<Integer,Integer,Integer>>>>();
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
	public boolean setTreasure(String position, String type, int value , long l,int lockisopen,int lockStr, int str) {
		if(this.verboseEtudeTresor) {
			System.out.println("Ajout trésor");
		}
		Tuple3<Integer,Integer,Integer> open = new Tuple3<Integer,Integer,Integer>(lockisopen,lockStr,str);
		for(int i = 0 ; i < this.getNbTreasure() ; i ++) {
			if(this.treasure.get(i).getLeft() == position) {
				if(this.treasure.get(i).getRight().get_3() > l) {
					Tuple4<String, Integer, Long,Tuple3<Integer,Integer,Integer>> v = new Tuple4<String,Integer,Long,Tuple3<Integer,Integer,Integer>>(type,value,l,open);
					Couple<String,Tuple4<String,Integer,Long,Tuple3<Integer,Integer,Integer>>> e = new Couple<String,Tuple4<String,Integer,Long,Tuple3<Integer,Integer,Integer>>>(position,v);
					this.treasure.set(i, e);
					return true;
				}
				return false;
			}
		}
		Tuple4<String, Integer, Long, Tuple3<Integer,Integer,Integer>> v = new Tuple4<String,Integer,Long,Tuple3<Integer,Integer,Integer>>(type,value,l,open);
		Couple<String,Tuple4<String,Integer,Long,Tuple3<Integer,Integer,Integer>>> e = new Couple<String,Tuple4<String,Integer,Long,Tuple3<Integer,Integer,Integer>>>(position,v);
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
	public Couple<String,Tuple4<String,Integer,Long,Tuple3<Integer,Integer,Integer>>> getTreasure(int i){
		return this.treasure.get(i);
	}
	public String getPositionBestTreasureForMe(String myPos , int size, int type) {
		/*
		 * Different types : 
		 * 0 : get closest treasure
		 * 1 : get the closest treasure that i can empty
		 * 2 : get the best treasure by value
		 * */
		//TODO Gerer les coffres fermés
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
				if((d < dist || iMin ==-1) && this.treasure.get(i).getRight().get_2()!= 0){
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
				if((this.treasure.get(i).getRight().get_2() < size  && d < dist) || iMin ==-1) {
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
		this.voisin.clear();
		//this.voisin = new ArrayList<String>();
		if(this.verboseObservation)
			System.out.println("I see : ");
		while(iter.hasNext()){
			Couple<String, List<Couple<Observation,Integer>>> it = iter.next();
			String nodeId=it.getLeft();
			if(this.verboseObservation)
				System.out.println(it.getRight());
			this.voisin.add(nodeId);
			if (!this.closedNodes.contains(nodeId)){
				if (!this.openNodes.contains(nodeId)){
					this.addNode(nodeId, MapAttribute.open);
					this.addEdge(myPosition, nodeId);	
					//this.sendMessage("LINK "+myPosition+" "+ nodeId,false,"Explo");
				}else{
					//the node exist, but not necessarily the edge
					this.addEdge(myPosition, nodeId);
				}
				
			}
			if(!it.getRight().isEmpty()) {
				
				if(it.getRight().get(0).getLeft().toString() != "WIND")
					if(this.myAgent.getCurrentPosition().equals(nodeId)) {
						this.treatTresureOnMyLocation(nodeId,it.getRight());
					}
			}
			
			
			
		}
		this.cleanOpenNodes();
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
			for( int j = 0 ; j < this.inCommsWith.size() ; j++) {
				if(this.inCommsWith.get(j).getKey().equals(msg.getSender()) && this.inCommsWith.get(j).getValue().equals("MapExchange")) {
					System.out.println("AID removed");System.out.println(this.inCommsWith.get(j).getKey());
					this.inCommsWith.remove(j);
				}
			}
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
			boolean alreadyInComm = false;
			for( int j = 0 ; j < this.inCommsWith.size() ; j++) {
				if(this.inCommsWith.get(j).getKey().equals(msg.getSender()) && this.inCommsWith.get(j).getValue().equals("MapExchange")) {
					alreadyInComm = true;
				}
			}
			if(alreadyTold || alreadyInComm) {
				this.sendMessage("MapExchange ACK", msg.getSender());
			}
			else {
				this.inComms = true;
				this.inCommsWith.add(new AbstractMap.SimpleEntry<AID,String>(msg.getSender(),"InterBlock"));
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
				}
				content.substring(0,content.length()-1);
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

			boolean alreadyInComm = false;
			for( int j = 0 ; j < this.inCommsWith.size() ; j++) {
				if(this.inCommsWith.get(j).getKey().equals(msg.getSender()) && this.inCommsWith.get(j).getValue().equals("MapExchange")) {
					System.out.println("AID removed");System.out.println(this.inCommsWith.get(j).getKey());
					this.inCommsWith.remove(j);
				}
			}
		}
		else if(parts[0].contains("UPDATE")) {
			if(this.verboseMapExchange)
				System.out.println(this.myAgent.getName() + " : Update acknowledged");
			
			this.inComms = false;boolean alreadyInComm = false;
			for( int j = 0 ; j < this.inCommsWith.size() ; j++) {
				if(this.inCommsWith.get(j).getKey().equals(msg.getSender()) && this.inCommsWith.get(j).getValue().equals("MapExchange")) {
					System.out.println("AID removed");System.out.println(this.inCommsWith.get(j).getKey());this.inCommsWith.remove(j);
				}
			}
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
		//TODO Ajouter interblock pour ouverture de coffre
		String[] parts = msg2.getContent().substring(msg2.getContent().indexOf(' ') + 1).split("\\s+");
		if(this.verboseInterblock==true) {
			System.out.println("Treating msg for interblock : -"+ msg2.getContent().substring(msg2.getContent().indexOf(' ') + 1)+"-");
			System.out.println("Type of interblock msg : -"+parts[0]+"-");
		}
		if(parts[0].equals("STUCK")) {
			System.out.println("Treating stuck");
			if(parts[1].equals(this.myAgent.getCurrentPosition())) {
				boolean alreadyInComm = false;
				for( int j = 0 ; j < this.inCommsWith.size() ; j++) {
					if(this.inCommsWith.get(j).getKey().equals(msg2.getSender()) && this.inCommsWith.get(j).getValue().equals("InterBlock")) {
						alreadyInComm = true;
					}
				}
				if(alreadyInComm) {
					//already started no need to continue this line of communication
					System.out.println("Already in comms with "+msg.getSender());
				}else {
					this.actualProtocol = "InterBlock";
					this.inCommsWith.add(new AbstractMap.SimpleEntry<AID,String>(msg2.getSender(),"InterBlock"));
					this.inComms = true;
					this.sendMessage("InterBlock RSTUCK "+this.desiredPosition, msg2.getSender());
					
					if(this.verboseInterblock==true) 
						System.out.println(this.myAgent.getName() + " send RSTUCK");
				}
				
			}
		}else if(parts[0].equals("RSTUCK")) {
			boolean alreadyInComm = false;
			for( int j = 0 ; j < this.inCommsWith.size() ; j++) {
				if(this.inCommsWith.get(j).getKey().equals(msg2.getSender()) && this.inCommsWith.get(j).getValue().equals("InterBlock")) {
					alreadyInComm = true;
				}
			}
			if(alreadyInComm) {
				//already started no need to continue this line of communication
				if(this.verboseInterblock) 
					System.out.println("Already in comms with "+msg.getSender());
			}else {
				this.inComms = true;
				this.inCommsWith.add(new AbstractMap.SimpleEntry<AID,String>(msg2.getSender(),"InterBlock"));
			}
			String content = "InterBlock OBJECTIVE [";
			content+=this.objective+","+this.objectiveAttribute.getLeft()+","+this.objectiveAttribute.getRight()+","+Integer.toString(this.vidage)+"] ";
			content+=this.destination+" ";
			if(this.desiredPosition.equals(this.destination) || this.desiredPosition.equals("")) {
				this.path = new String[] {this.destination};
			}else if(this.destination.equals("")){
				System.out.println("Erreur pas de destination");
			}
			else {
				Object[] a= this.myMap.getShortestPath(this.desiredPosition, this.destination).toArray();
				this.path =  Arrays.copyOf(a, a.length, String[].class);
			}
			content+="[";
			boolean emptyPath = true;
			for(int i = 0 ; i < this.path.length ; i ++) {
				content+=this.path[i]+"/";
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
			if(this.desiredPosition.equals(this.destination) || this.desiredPosition.equals("")) {
				this.path = new String[] {this.destination};
			}else if(this.destination.equals("")){
				System.out.println("Erreur pas de destination");
			}
			else {
				Object[] a= this.myMap.getShortestPath(this.desiredPosition, this.destination).toArray();
				this.path =  Arrays.copyOf(a, a.length, String[].class);
			}
			content+="[";
			for(int i = 0 ; i < this.path.length ; i ++) {
				content+=this.path[i]+"/";
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
			for( int j = 0 ; j < this.inCommsWith.size() ; j++) {
				if(this.inCommsWith.get(j).getKey().equals(msg2.getSender()) && this.inCommsWith.get(j).getValue().equals("InterBlock")) {
					System.out.println("AID removed");System.out.println(this.inCommsWith.get(j).getKey());this.inCommsWith.remove(j);
				}
			}
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

				for( int j = 0 ; j < this.inCommsWith.size() ; j++) {
					if(this.inCommsWith.get(j).getKey().equals(sender) && this.inCommsWith.get(j).getValue().equals("InterBlock")) {
						System.out.println("AID removed");System.out.println(this.inCommsWith.get(j).getKey());this.inCommsWith.remove(j);
					}
				}
				this.actualProtocol="MapExchange";
				if(this.verboseInterblock) 
					System.out.println("2 explore colliding swapping map");
				this.sendMessage("InterBlock MapExchange ASK", sender);
				this.setDestination("");
				return "Destination SWAP "+this.objective;
			}
			else if(this.objective.equals("collect")) {
				if(this.vidage == this.allyVidage && this.vidage == 0) {
					for( int j = 0 ; j < this.inCommsWith.size() ; j++) {
						if(this.inCommsWith.get(j).getKey().equals(sender) && this.inCommsWith.get(j).getValue().equals("InterBlock")) {
							System.out.println("AID removed");System.out.println(this.inCommsWith.get(j).getKey());this.inCommsWith.remove(j);
						}
					}
					//TODO penser a un meilleur moyen qu'echanger les destinations lorsque les deux se bloquant vont chercher un tresor sans le vider
					this.sendMessage("InterBlock SWAP ["+this.objective+ "," +this.objectiveAttribute.getLeft() +","+this.objectiveAttribute.getRight() +","+Integer.toString(this.vidage)+ "] " + this.destination, sender); 
					this.setDestination(this.allyDestination);
					this.objectiveAttribute=this.allyAttributes;
					if(this.verboseInterblock) 
						System.out.println("SWAPING Obj");
					return "Destination SWAP "+this.objective;
				}
				else if(this.vidage == this.allyVidage && this.vidage == 1 && this.allyBackPackSpace >= Integer.parseInt(this.objectiveAttribute.getRight()) && this.getBackPackSize() >= Integer.parseInt(this.allyAttributes.getRight())) {
					for( int j = 0 ; j < this.inCommsWith.size() ; j++) {
						if(this.inCommsWith.get(j).getKey().equals(sender) && this.inCommsWith.get(j).getValue().equals("InterBlock")) {
							System.out.println("AID removed");System.out.println(this.inCommsWith.get(j).getKey());this.inCommsWith.remove(j);
						}
					}
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
					this.sendMessage("InterBlock WAIT "+noeudActu, sender);
					if(this.verboseInterblock) 
						System.out.println("Wait here while i go at "+noeudActu);
				}else {
					//Toi qui bouge
					this.sendMessage("InterBlock GO " + noeudEvitement1, sender);
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
			for( int j = 0 ; j < this.inCommsWith.size() ; j++) {
				if(this.inCommsWith.get(j).getKey().equals(msg.getSender()) && this.inCommsWith.get(j).getValue().equals("InterBlock")) {
					System.out.println("AID removed");System.out.println(this.inCommsWith.get(j).getKey());this.inCommsWith.remove(j);
				}
			}
		}
		
	}
	private void treatMessageForTreasureExchange(ACLMessage msg2) {
		String content =msg.getContent().substring(msg.getContent().indexOf(' ') + 1);
		content= content.substring(msg.getContent().indexOf(' ') + 1);
		String[] parts = content.split("\\s+");
		boolean notUpToDate = false;
		for(int i = 0 ; i < parts.length ; i++) {
			String tresor = parts[i].substring(1, parts[i].length()-1);
			String[] valeurs = tresor.split(",");
			String pos = valeurs[0];
			String type = valeurs[1];
			int val = Integer.parseInt(valeurs[2]);
			Long date = Long.valueOf(valeurs[3]);
			int open = Integer.parseInt(valeurs[4]);
			int lock = Integer.parseInt(valeurs[5]);
			int str = Integer.parseInt(valeurs[6]);
			if(this.setTreasure(pos, type, val, date, open, lock, str)) {
				notUpToDate = false;
			}
		}
		if(notUpToDate) {
			this.sendTreasure(msg2.getSender());
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
			}else if(parts[0].contains("Treasure EXCHANGE")) {
				this.treatMessageForTreasureExchange(this.msg);
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
		this.cleanOpenNodes();
		//TODO A tester

		/*if(this.objective=="explore") {
			this.destination = this.findDestination();
		}
		if(this.verboseMovement) {
			System.out.println("Movement :");
			System.out.println(this.myAgent.getCurrentPosition());
			System.out.println(this.destination);
			System.out.println(this.actualProtocol);
			System.out.println(this.type);
			System.out.println(this.objective);
		}*/
		
		if(this.detectIfStuck()) {
			this.startInterBlockProcedure();
		}
		this.cptTour++;
		if(this.actualProtocol == "InterBlockWaiting") {
			this.cptUntilRestart +=1;
			if(this.cptUntilRestart > 3) {
				if(this.myAgent.getCurrentPosition()==this.thanksAt) {
					for(int j = 0 ; j < this.inCommsWith.size() ; j++) {
						if(this.inCommsWith.get(j).getValue().equals("InterBlock")) {
							this.sendMessage("InterBlockGiveWay Thanks",this.inCommsWith.get(j).getKey());
							System.out.println("AID removed");System.out.println(this.inCommsWith.get(j).getKey());
							this.inCommsWith.remove(j);
							break;
						}
							
					}
					this.actualProtocol = "";
					this.inComms = false;
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
			if(this.destination == "" || this.destination == this.myAgent.getCurrentPosition()) {
				if(this.verboseMovement) 
					System.out.println("Searching for new destination");
				this.destination = this.findDestination();
			}
			if(this.destination == "") {
				if(this.verboseMovement) {
					System.out.println("Nowhere to go");
				}
				return;
			}
			if(this.voisin.contains(this.destination)) {
				nextNode = this.destination;
			}else {
				while (nextNode==null || nextNode.equals(this.myAgent.getCurrentPosition())){
					nextNode=this.myMap.getShortestPath(this.myAgent.getCurrentPosition(), this.destination).get(0);
					if(this.myAgent.getCurrentPosition().equals(nextNode)) {
						nextNode=this.myMap.getShortestPath(this.myAgent.getCurrentPosition(), this.destination).get(1);
					}
				}
			}
			if(this.verboseMovement) {
				System.out.println("Pre Movement :");
				System.out.println(this.myAgent.getCurrentPosition());
				System.out.println(this.destination);
				System.out.println(this.desiredPosition);
			}
			if(this.verboseEtatAgent) {
				System.out.println("Agent : "+this.myAgent.getName());
				System.out.println("Destination : " + this.destination);
				System.out.println("Protocole  : " + this.actualProtocol);
				System.out.println("Type  : " + this.type);
				System.out.println("Objective  : " + this.objective);
				System.out.println("Ouvert");
				System.out.println(this.openNodes);
				System.out.println("Fermé");
				System.out.println(this.closedNodes);
				System.out.println(this.inComms);
				System.out.println(this.waitingForResponse);
				if(this.closedNodes.contains(this.destination)) {
					System.out.println("Going some place i've already been");
				}
			}
			if(!nextNode.equals(this.myAgent.getCurrentPosition())) {
				this.desiredPosition = nextNode;
				((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			}			
		}
	}
	private String findDestination() {
		if(this.verboseMovement|| this.verboseEtatAgent)
			System.out.println("Find destination function launched");
		if(this.objective.equals("explore")) {
			if(this.verboseMovement|| this.verboseEtatAgent)
				System.out.println("Find new Dest");
			if(this.openNodes.size()<1) {
				System.out.println("Nothing to check");
				return "";
			}
			int iMin=-1;
			int distMin=0;
			for (int i = 0 ; i < this.openNodes.size() ; i++) {
				int a = this.dist(this.myAgent.getCurrentPosition(),this.openNodes.get(i));
				if(!this.closedNodes.contains(this.openNodes.get(i)) && (iMin == -1 || a < distMin)) {
					iMin = i;
					distMin = a;
				}
			}
			if(this.verboseMovement || this.verboseEtatAgent)
				System.out.println("New dest find : "+this.openNodes.get(iMin));
			return this.openNodes.get(iMin);
		}else if(this.type.equals("Collect")) {
			if(this.getNbTreasure()!=0 && this.actualProtocol =="") {
				this.objective="collect";
			}
			if(this.objective == "collect") {
				return this.getPositionBestTreasureForMe(this.myAgent.getCurrentPosition(), this.myBackpackSize, 1);
			}
			
		}else if(this.objective.equals("silo")) {
			int nb = this.getNbTreasure();
			String dest ="";
			if(nb!=0) {
				if(nb == 1) {
					boolean setDest = false;
					ArrayList<String> v = new ArrayList<String>();
					for(int i = 0 ; i < this.getNeighbour(this.getTreasure(0).getLeft()).length ; i++) {
						v.add(this.getNeighbour(this.getTreasure(0).getLeft())[i]);
					}
					String pos="";
					while(v.size() != 0 || setDest == false) {
						pos=v.get(0);
						v.remove(0);
						if(this.getNeighbour(pos).length>2) {
							dest = pos;
							setDest = true;
						}
					}
					return dest;
				}
				else {
					ArrayList<String> spot = new ArrayList<String>();
					ArrayList<String> treasure = new ArrayList<String>();
					for(int i = 0 ; i < this.getNbTreasure() ; i++) {
						treasure.add(this.getTreasure(i).getLeft());
					}
					for(int i = 0 ; i < treasure.size() ; i++) {
						for(int j = 0 ; j < treasure.size() ; i++) {
							List<String> ch = this.myMap.getShortestPath(treasure.get(i), treasure.get(j));
							for(int k = 0 ; k < ch.size() ; k++) {
								if(this.getNeighbour(ch.get(k)).length > 2) {
									spot.add(ch.get(k));
								}
							}
						}
					}
					int iMin = -1;
					int dMin = -1;
					for(int i = 0 ; i < spot.size() ; i++) {
						int d = 0;
						for(int j = 0 ; j < treasure.size() ; j++) {
							d+=this.myMap.getShortestPath(spot.get(i), treasure.get(j)).size();
						}
						if(d<dMin || iMin == -1) {
							iMin = i;
							dMin = d;
						}
					}
					return spot.get(iMin);
				}
			}else {
				boolean setDest = false;
				ArrayList<String> v = new ArrayList<String>();
				for(int i = 0 ; i < this.getNeighbour(this.myAgent.getCurrentPosition()).length ; i++) {
					v.add(this.getNeighbour(this.myAgent.getCurrentPosition())[i]);
				}
				String pos="";
				while(v.size() != 0 || setDest == false) {
					pos=v.get(0);
					v.remove(0);
					if(this.getNeighbour(pos).length>2) {
						dest = pos;
						setDest = true;
					}
				}
				return dest;
			}
		}else if(this.objective.equals("collect")){
			return this.getPositionBestTreasureForMe(this.myAgent.getCurrentPosition(), this.myBackpackSize, 1);
		}
		return "";
	}
	public int endingFunc() {
		/*
		 * Transition :
		 * Retour onEnd : 
		 * 0 : pas de changement
		 * 1 : passage mapExchange
		 * 2 : passage mode explo
		 * 3 : passage mode collect
		 * 4 : passage mode silo
		 */
		if(this.switchToMsgSending) {
			return 1;
		}else if(this.type == "Explo" || (this.type == "Collect" && this.getNbTreasure() == 0)) {
			return 2;
		}else if(this.type == "Collect" && this.getNbTreasure() != 0) {
			return 3;
		}else if(this.type == "Silo") {
			return 4;
		}else {
			return 0;
		}
	}
	public void treatTresureOnMyLocation(String nodeId, List<Couple<Observation, Integer>> list) {
		//5 [<'Gold',95>, <LockIsOpen, 0>, <LockPicking, 1>, <Strength, 1>]
		/*Observation lp = list.getRight().get(i).getLeft().LOCKPICKING;
		Observation s = it.getRight().get(i).getLeft().STRENGH;
		Observation ls = it.getRight().get(i).getLeft().LOCKSTATUS;
		this.setTreasure(nodeId, it.getRight().get(i).getLeft().toString(), it.getRight().get(i).getRight(), LocalTime.now().toNanoOfDay(),new Tuple3<Integer,Integer,Integer>(ls,s,lp));
		*/
		int lockisopen = 0;
		int lockpicking = 0;
		int strength = 0;
		int size = 0;
		String type ="";
		for(int i = 0 ; i < list.size() ; i++) {
			if(list.get(i).getLeft().toString().equals("Gold")) {
				type = "Gold";
				size= list.get(i).getRight();
			}
				
			if(list.get(i).getLeft().toString().equals("Diamond")) {
				type = "Diamond";
				size= list.get(i).getRight();
			}
			if(list.get(i).getLeft().toString().equals("LockIsOpen"))
				lockisopen = list.get(i).getRight();
			if(list.get(i).getLeft().toString().equals("LockPicking"))
				lockpicking = list.get(i).getRight();
			if(list.get(i).getLeft().toString().equals("Strength"))
				strength = list.get(i).getRight();
		}
		System.out.println("Expertise");
		Object[] a = this.myExpertise.toArray();
		int lock = ((Couple<String,Integer>)a[0]).getRight();
		int str = ((Couple<String,Integer>)a[1]).getRight();
		
		
		if(lockisopen == 0 && strength < str && lockpicking < lock) {
			this.myAgent.openLock(list.get(0).getLeft());
			lockisopen = 1;
		}
		if(lockisopen == 1 && this.objective == "collect" && this.destination.equals(this.myAgent.getCurrentPosition())) {
			if(list.get(0).getRight()>this.myBackpackSize) {
				this.myBackpackSize =0;
				size = 0;
				this.objective = "vidage";
			}else {
				size = size - this.myBackpackSize;
				this.myBackpackSize=this.myBackpackSize-list.get(0).getRight();
				if(this.myBackpackSize < this.sizeTreshold) {
					this.objective = "vidage";
				}else {
					this.objective = "collect";
				}
			}
			this.myAgent.pick();//TODO Finir fonction
			if(this.verboseCollect)
				System.out.println("Picked");
		}
		this.setTreasure(nodeId, type, size, LocalTime.now().toNanoOfDay(), lockisopen,lockpicking,strength);
	}
	public void sendTreasure() {
		//TODO Definir quand envoyer les connaissances des tresors
		String message = "Treasure EXCHANGE ";
		for(int i = 0 ; i < this.getNbTreasure() ; i++) {
			String tresor = "[";
			Couple<String, Tuple4<String, Integer, Long, Tuple3<Integer, Integer, Integer>>> a = this.getTreasure(i);
			String pos = a.getLeft();
			String type = a.getRight().get_1();
			String val = Integer.toString(a.getRight().get_2());
			String time = Long.toString(a.getRight().get_3());
			String open = Integer.toString(a.getRight().get_4().getFirst());
			String lock = Integer.toString(a.getRight().get_4().getSecond());
			String str = Integer.toString(a.getRight().get_4().getThird());
			tresor+=pos+","+type+","+val+","+time+","+open+","+lock+","+str+"] ";
			message+=tresor;
		}

		message.substring(0,message.length()-1);
		this.sendMessage(message, false, "agent");
	}
	public void sendTreasure(AID dest) {
		String message = "Treasure EXCHANGE ";
		for(int i = 0 ; i < this.getNbTreasure() ; i++) {
			String tresor = "[";
			Couple<String, Tuple4<String, Integer, Long, Tuple3<Integer, Integer, Integer>>> a = this.getTreasure(i);
			String pos = a.getLeft();
			String type = a.getRight().get_1();
			String val = Integer.toString(a.getRight().get_2());
			String time = Long.toString(a.getRight().get_3());
			String open = Integer.toString(a.getRight().get_4().getFirst());
			String lock = Integer.toString(a.getRight().get_4().getSecond());
			String str = Integer.toString(a.getRight().get_4().getThird());
			tresor+=pos+","+type+","+val+","+time+","+open+","+lock+","+str+"] ";
			message+=tresor;
		}

		message.substring(0,message.length()-1);
		this.sendMessage(message, dest);
	}
}
