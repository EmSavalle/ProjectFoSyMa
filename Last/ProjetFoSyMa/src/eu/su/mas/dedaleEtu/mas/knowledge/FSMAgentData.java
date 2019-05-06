package eu.su.mas.dedaleEtu.mas.knowledge;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.lang.reflect.Array;
import java.time.*;
import java.time.format.DateTimeFormatter;
//TODO Regler bug recup diam mais pas or
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
//TODO Creer a* pour trouver chemin ne passant pas par un lieu
public class FSMAgentData {
	//Map var
	public MapRepresentation myMap;
	public ACLMessage msg;
	public ArrayList<String> openNodes;
	public ArrayList<String> closedNodes;
	public String edge;
	public ArrayList<Treasure> treasure;
	private ArrayList<String> voisin;
	private boolean treasureNear;
	public String destination;
	public boolean exploFinished;
	
	//Var comms
	public ArrayList<Entry<String,Integer>> lastComms;
	public boolean switchToMsgSending;
	public boolean finished;
	public boolean waitingForResponse;
	public List<Entry<AID,String>> inCommsWith;
	public int cptMapExchange;
	public int cptMapExchangeTreshold;
	public int cptInterBlock;
	public int cptInterBlockTreshold;
	
	//Init var
	private Set<Couple<LockType, Integer>> myExpertise;//[<LockPicking, 1>, <Strength, 1>]
	private int diamondCap;
	private int goldCap;
	private int diamondPicked;
	private int goldPicked;
	public Integer lockpickStrength;
	public Integer strength;
	private int sizeTreshold;
	public String objective;
	private String previousObjective;
	public String secondObjective;
	public Couple<String,Integer> objectiveAttribute;//Type , quantité
	public int vidage;
	public int myBackpackSize;
	public String type; //Silo / Explorer / Collector 
	public String desiredPosition;
	public int cptTour=0;
	public int cptAttenteRetour=0;
	public int initBackPackSize;
	public AbstractDedaleAgent myAgent;
	private String actualProtocol;

	//AStar var
	private boolean followingCustomPath;
	private ArrayList<String> myPath;
	private String nextNodeMyPath;
	private boolean myOwnPathStart;
	
	//Silo var
	public String siloPosition;
	public boolean siloPositionOutdated;
	public AID siloAID;
	private long lastSeenSilo;
	public boolean lookingForSilo;
	private int siloPositionSet;
	private int siloTimeUntilRelocate;
	private boolean siloAvailableSent;
	
	
	//Interblock var
	private String[] path ;
	private Couple<String, Integer> allyAttributes;
	private String allyObjective;
	private String[] allyPath ;
	private String allyDestination;
	private int allyBackPackSpace;
	private int allyVidage;
	private String giveWayPosition;
	private String thanksAt;
	private int cptUntilRestart;
	public int stuckCounter;
	private String previousPosition;
	private int stuckTreshold;
	private boolean waitingForOtherInterblock;
	private int cptWaitOtherInterblock;
	
	
	//Order var
	public OrderList list_order;
	public boolean executingOrder;
	public int order_agentnumber;
	private int order_lstrneeded;
	private int order_strneeded;
	private boolean order_startOnArrival;
	private Observation order_chestType;
	public String messageForSilo;
	private boolean order_waitingForOthers;
	private int waitingForOtherInterblockThreshold;
	
	//Verbose 
	public boolean verbose;
	public boolean verboseInterblock;
	public boolean verboseMapExchange;
	public boolean verboseMovement;
	public boolean verboseEtatAgent;
	public boolean verboseObservation;
	public boolean verboseCollect;
	public boolean verboseEtudeTresor;
	public boolean verboseFSMBehaviour;
	public boolean verboseDebug;
	public boolean verboseSilo;
	private boolean verboseMessage;
	private boolean verboseTreasureExchange;
	private boolean verboseOrder;
	private boolean verboseSendMessage;
	private boolean verboseMessage2;
	private boolean verboseAStar;
	private boolean stuckSended;
	private String previousDestination;
	//Int
	public FSMAgentData(int backpack, AbstractDedaleAgent ag ,Object[] args){
		//TODO initialiser la liste des agents sur la carte
		//Init agent var
		EntityCharacteristics ec = (EntityCharacteristics) args[0];
		this.myExpertise = ec.getExpertise();
		this.type = ec.getMyEntityType().toString();
		if(this.type.contains("Collect"))
			this.type = "Collect";
		if(ag.getName().contains("Tanker"))
			this.type = "Silo";
		if(this.type.contains("Explo"))
			this.type = "Explo";
		if( type.equals("Explo")) {
			this.objective = "explore";
		}else if( type.equals("Collect")) {
			this.objective = "explore";
		}else if( type.equals("Silo")) {
			this.objective = "silo";
		} 
		this.secondObjective = "";
		if(this.type != "Silo"){
			
			Set<Couple<LockType, Integer>> exp = ec.getExpertise();
			Iterator<Couple<LockType, Integer>> itr = exp.iterator();
			while(itr.hasNext()){
				  Couple<LockType,Integer> c = itr.next();
				  if(c.getLeft() == LockType.lockpicking)
					  this.lockpickStrength = c.getRight();;
				  if(c.getLeft() == LockType.strength)
					  this.strength = c.getRight();
			}
		}
		this.objectiveAttribute = new Couple<String,Integer>("",0);
		this.diamondCap = ec.getDiamondCapacity();
		this.goldCap = ec.getGoldCapacity();
		this.diamondPicked = this.diamondCap;
		this.goldPicked = this.goldCap;
		this.vidage = 0;
		this.initBackPackSize = backpack;
		this.myBackpackSize=backpack;
		this.sizeTreshold = 5;
		this.destination = "";
		this.myMap= new MapRepresentation();
		this.openNodes=new ArrayList<String>();
		this.closedNodes=new ArrayList<String>();
		this.exploFinished = false;
		this.voisin=new ArrayList<String>();
		this.lastComms= new java.util.ArrayList<Entry<String,Integer>>();
		this.inCommsWith = new ArrayList<Entry<AID,String>>();
		this.treasure= new java.util.ArrayList<Treasure>();
		this.edge = "";
		this.myAgent = ag;
		this.actualProtocol = "";
		this.desiredPosition = "";
		this.nextNodeMyPath="";
		this.myOwnPathStart=false;
		
		//Interblock var 
		this.previousPosition="";
		this.stuckCounter = 0;
		this.stuckTreshold = 2;
		this.waitingForOtherInterblock = false;
		this.cptWaitOtherInterblock = 0;
		this.waitingForOtherInterblockThreshold = 5;
		//Silo var
		this.siloAID = null;
		this.lastSeenSilo = 0;
		this.siloPositionOutdated = true;
		this.lookingForSilo = false;
		this.siloPosition = "";
		this.siloPositionSet = 0;
		this.siloTimeUntilRelocate= 15;
		this.siloAvailableSent = false;
		
		
		//Map exchange var 
		this.msg = null;
		this.waitingForResponse= false;
		this.finished = false;
		this.switchToMsgSending=false;
		this.cptMapExchange=0;
		this.cptMapExchangeTreshold=6;
		this.cptInterBlock = 0;
		this.cptInterBlockTreshold = 6;
		

		
		//Order var
		this.list_order = new OrderList();
		this.executingOrder =false;
		this.order_agentnumber = -1;
		this.order_lstrneeded = -1;
		this.order_strneeded =-1;
		this.order_startOnArrival = false;
		this.messageForSilo = "";
		
		
		//Verbose
		/*if(this.myAgent.getName().contains("Tanker")) {
			this.verbose = false;
			this.verboseEtatAgent = false;
			this.verboseInterblock = true;
			this.verboseMapExchange = true;
			this.verboseMovement = false;
			this.verboseObservation = false;
			this.verboseCollect = false;
			this.verboseEtudeTresor = true;
			this.verboseFSMBehaviour = false;
			this.verboseCollect = false;
			this.verboseSilo = false;
			this.verboseMessage=true;
			this.verboseFSMBehaviour = false;
			this.verboseTreasureExchange = false;
			this.verboseOrder = true;
			this.verboseSendMessage = true;
		}else {*/
			this.verbose = false;
			this.verboseEtatAgent = true;
			this.verboseInterblock = true;
			this.verboseMapExchange = false;
			this.verboseMovement = false;
			this.verboseObservation = false;
			this.verboseEtudeTresor = false;
			this.verboseFSMBehaviour = false;
			this.verboseCollect = true;
			this.verboseSilo = false;
			this.verboseMessage=false;
			this.verboseMessage2=false;
			this.verboseTreasureExchange = false;
			this.verboseOrder = true;
			this.verboseSendMessage = true;
			this.verboseAStar = false;
			this.verboseDebug = false;
		//}
	}
	
	//Gestion map
	public void addNode(String node) {
		if(!this.closedNodes.contains(node)) {
			this.myMap.addNode(node);
			this.closedNodes.add(node);
		}
	}
	public void addNode(String node , MapAttribute m) {
		if(m== MapAttribute.open && !this.openNodes.contains(node)) {
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
		ArrayList<String> newList =new ArrayList<String>();
		for (String element : this.openNodes) { 
	        if (!newList.contains(element) ) { 
	            newList.add(element); 
	        } 
	    } 
		this.openNodes=newList;	
		
	}
	private void cleanClosedNodes() {
		ArrayList<String> newList =new ArrayList<String>();
		for (String element : this.closedNodes) { 
	        if (!newList.contains(element) ) { 
	            newList.add(element); 
	        } 
	    } 
		this.closedNodes=newList;		
	}
	private void cleanEdges() {
		ArrayList<String> newList =new ArrayList<String>();
		for (String element : this.edge.split(" ")) { 
			String str = element;
	        String reverse = "";
	        
	        
	        for(int i = str.length() - 1; i >= 0; i--)
	        {
	            reverse = reverse + str.charAt(i);
	        }
            if (!newList.contains(element) || !newList.contains(reverse)) { 
                newList.add(element); 
            } 
        } 
		this.edge = "";
		for (String e : newList) {
				this.edge= this.edge+" "+e;
		}
	}
	public void addEdge(String position,String edg) {
	    if (!this.edge.contains(position+"-"+edg) || !this.edge.contains(edg+"-"+position)) { 
			this.edge=this.edge+" "+position+"-"+edg;
			this.myMap.addEdge(position, edg);
	    } 
	}
	public ArrayList<String> getNeighbour(String n) {
		ArrayList<String> a = new ArrayList<String>();
		String[] e = this.edge.split(" ");
		for(int i = 0 ; i < e.length ; i++) {
			//System.out.println(e[i]);
			String[] ed = e[i].split("-");
			if(ed.length>1) {
				if(ed[0].equals(n)||ed[1].equals(n)) {
					if(ed[0].equals(n) && !a.contains(ed[1]) && !ed[1].equals(n)) {
						a.add(ed[1]);
					}else if(!a.contains(ed[0])  && !ed[0].equals(n)) {
						a.add(ed[0]);
					}
				}
			}
		}
		return a;
		//return (String[]) a.toArray();
		
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
	public String getDestination() {
		return this.destination;
	}
	public void setDestination(String dest) {
		this.destination = dest;
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
		this.treasureNear = false;
		//this.voisin = new ArrayList<String>();
		if(this.verboseObservation)
			System.out.println(this.myAgent.getName()+" : " +"I see : ");
		while(iter.hasNext()){
			Couple<String, List<Couple<Observation,Integer>>> it = iter.next();
			String nodeId=it.getLeft();
			if(this.verboseObservation)
				System.out.println(this.myAgent.getName()+" : " +it.getRight());
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
					this.treasureNear = true;
					if(this.myAgent.getCurrentPosition().equals(nodeId)) {
						this.treatTresureOnMyLocation(nodeId,it.getRight());
					}
			}
			
			
			
		}
		this.cleanOpenNodes();
	}
	private String findDestination() {
		this.stuckCounter = 0;
		if(this.verboseMovement|| this.verboseEtatAgent)
			System.out.println(this.myAgent.getName()+" : " +"Find destination function launched");
		if(this.objective.equals("explore") || this.secondObjective.equals("explore")) {
			if(this.verboseMovement|| this.verboseEtatAgent)
				System.out.println(this.myAgent.getName()+" : " +"Find new Dest");
			if(this.openNodes.size()<1) {
				System.out.println(this.myAgent.getName()+" : " +"Nothing to check");
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
				System.out.println(this.myAgent.getName()+" : " +"New dest find : "+this.openNodes.get(iMin));
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
					v = this.getNeighbour(this.getTreasure(0).getPosition());
					String pos="";
					while(v.size() != 0 && setDest == false) {
						pos=v.get(0);
						v.remove(0);
						if(this.getNeighbour(pos).size()>2) {
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
						treasure.add(this.getTreasure(i).getPosition());
					}
					for(int i = 0 ; i < treasure.size() ; i++) {
						for(int j = 0 ; j < treasure.size() ; j++) {
							List<String> ch = this.myMap.getShortestPath(treasure.get(i), treasure.get(j));
							for(int k = 0 ; k < ch.size() ; k++) {
								if(this.getNeighbour(ch.get(k)).size() > 2) {
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
					if(iMin == -1)
						return "";
					return spot.get(iMin);
				}
			}else {
				boolean setDest = false;
				ArrayList<String> v = new ArrayList<String>();
				v = this.getNeighbour(this.myAgent.getCurrentPosition());
				String pos="";
				while(v.size() != 0 && setDest == false) {
					pos=v.get(0);
					v.remove(0);
					if(this.getNeighbour(pos).size()>2) {
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
	
	
	public int getBackPackSize() {
		return 0;
		//return this.myAgent.getBackPackFreeSpace();
	}
	public void setBackPackSize(int s) {
		this.myBackpackSize = s;		
	}
	
	//Treasure gestion
	public boolean setTreasure(String position, String type, int value , long l,int lockisopen,int lockStr, int str) {
		if(this.verboseEtudeTresor) {
			System.out.println(this.myAgent.getName()+" : " +"Ajout trésor");
		}
		Tuple3<Integer,Integer,Integer> open = new Tuple3<Integer,Integer,Integer>(lockisopen,lockStr,str);
		for(int i = 0 ; i < this.getNbTreasure() ; i ++) {
			if(this.treasure.get(i).getPosition() == position) {
				// [(pos,[type,Value,date,[open,lockstr,str]]),...]
				if(this.treasure.get(i).getLastSeen() > l) {

					this.treasure.get(i).updateOpen(l, lockisopen);
					this.treasure.get(i).updateValue(l, value);
					return true;
				}
				return false;
			}
		}
		this.treasure.add(new Treasure(position,type, value,lockisopen, lockStr, str,l));
		return false;
	}
	public boolean verifyTreasure(String myPosition) {
		for(int i = 0 ; i < this.getNbTreasure() ; i ++) {
			if(this.treasure.get(i).getPosition() == myPosition) {
				return true;
			}
		}
		return false;
	}
	public boolean deleteTreasure(String pos) {
		for(int i = 0 ; i < this.getNbTreasure() ; i ++) {
			if(this.treasure.get(i).getPosition() == pos) {
				this.treasure.remove(i);
				return true;
			}
		}
		return false;
		
	}
	public int getNbTreasure() {
		return this.treasure.size();
	}
	public int getNbTreasureAccessible(int lStr,int sStr) {
		if(this.myAgent.getBackPackFreeSpace() == 0) {
			return 0;
		}
		int nb = 0;
		for (int i = 0 ; i < this.treasure.size() ; i ++) {
			if(this.treasure.get(i).isOpen ||(this.treasure.get(i).getLockStrength() <= lStr && this.treasure.get(i).getStrength() <= sStr)) {
				nb++;
			}
		}
		return nb;
	}
	public Treasure getTreasure(int i){
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
				String p = this.treasure.get(i).getPosition();
				int d = this.dist(myPos, p);
				if((d < dist || iMin ==-1) && this.treasure.get(i).getValue()!= 0 && (this.treasure.get(i).getLockStrength() <= this.lockpickStrength && this.treasure.get(i).getStrength() <= this.strength|| this.treasure.get(i).isOpen)){
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
				String p = this.treasure.get(i).getPosition();
				int d = this.dist(myPos, p);
				if(((this.treasure.get(i).getValue() < size  && d < dist) || iMin ==-1) && (this.treasure.get(i).getLockStrength() <= this.lockpickStrength && this.treasure.get(i).getStrength() <= this.strength|| this.treasure.get(i).isOpen)) {
					iMin = i;
					pos = p;
				}
			}
			if(iMin == -1) {
				for(int i = 0 ; i < this.getNbTreasure() ; i++) {
					String p = this.treasure.get(i).getPosition();
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
			return this.treasure.get(0).getPosition();
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
		Object[] a = this.myExpertise.toArray();
		int lock = ((Couple<String,Integer>)a[0]).getRight();
		int str = ((Couple<String,Integer>)a[1]).getRight();
		
		
		if(lockisopen == 0 && strength < str && lockpicking < lock) {
			if(this.verboseEtudeTresor)
				System.out.println(this.myAgent.getName()+" : " +"A ouvert un coffre");
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
				System.out.println(this.myAgent.getName()+" : " +"Picked");
		}
		this.setTreasure(nodeId, type, size, LocalTime.now().toNanoOfDay(), lockisopen,lockpicking,strength);
	}
	public void sendTreasure() {
		//TODO Definir quand envoyer les connaissances des tresors
		if(this.getNbTreasure() != 0) {
			String message = "Treasure EXCHANGE ";
			for(int i = 0 ; i < this.getNbTreasure() ; i++) {
				String tresor = "[";
				Treasure a = this.getTreasure(i);
				String pos = a.getPosition();
				String type = a.getType();
				String val = Integer.toString(a.getValue());
				String time = Long.toString(a.getLastSeen());
				String open = Integer.toString(a.getOpenInt());
				String lock = Integer.toString(a.getLockStrength());
				String str = Integer.toString(a.getStrength());
				tresor+=pos+","+type+","+val+","+time+","+open+","+lock+","+str+"] ";
				message+=tresor;
			}
	
			message.substring(0,message.length()-1);
			this.sendMessage(message, false, "");
		}
	}
	public void sendTreasure(AID dest) {
		if(this.getNbTreasure() != 0) {
			String message = "Treasure EXCHANGE ";
			for(int i = 0 ; i < this.getNbTreasure() ; i++) {
				String tresor = "[";
				Treasure a = this.getTreasure(i);
				String pos = a.getPosition();
				String type = a.getType();
				String val = Integer.toString(a.getValue());
				String time = Long.toString(a.getLastSeen());
				String open = Integer.toString(a.getOpenInt());
				String lock = Integer.toString(a.getLockStrength());
				String str = Integer.toString(a.getStrength());
				tresor+=pos+","+type+","+val+","+time+","+open+","+lock+","+str+"] ";
				message+=tresor;
			}
	
			message.substring(0,message.length()-1);
			this.sendMessage(message, dest);
		}
	}
	
	
	public void setCpt(int c) {
		this.cptTour = c;
	}
	public int getCpt() {
		return cptTour;
	}
	public int dist(String p1, String p2) {
		
		if(p1.contains("_") && p2.contains("_")) {
			System.out.println(this.myAgent.getName()+" : " +"Dist");
			System.out.println(p1+":"+p2);
			int x1 = Integer.parseInt(p1.split("_")[0]);
			int x2 = Integer.parseInt(p2.split("_")[0]);
			int y1 = Integer.parseInt(p1.split("_")[1]);
			int y2 = Integer.parseInt(p2.split("_")[1]);
			return Math.abs((x2-x1)+(y2-y1));
		}else {
			try {
				return this.myMap.getShortestPath(p1, p2).size();
			}catch(IndexOutOfBoundsException e) {
				return 10000;
			}
		}
		
	}

	public void mapExchangeProtocol() {
		//TODO A tester
		System.out.println(this.myAgent.getName()+" : " +"cLEAR COMMS "+Boolean.toString(this.inCommsWith.isEmpty()));
		if(this.waitingForResponse == false && this.inCommsWith.isEmpty() && this.switchToMsgSending == true) {	
			this.actualProtocol="MapExchange";
			this.cptMapExchange=0;
			this.sendMessage("MapExchange PING", false, "");
			System.out.println(this.myAgent.getName()+" : " +"Sending ping for map exchange");
			this.waitingForResponse = true;
			this.cptAttenteRetour=0;
			this.switchToMsgSending =false;
		}
		else if(this.cptAttenteRetour > 2){
			//System.out.println(this.myAgent.getName()+" : " +"Waiting for response ended");
			if(this.verbose==true)
				System.out.println(this.myAgent.getName() + " : Debloquage");

			this.actualProtocol = "";
			this.waitingForResponse = false;
			this.cptAttenteRetour = 0;
		}
		else {
			//System.out.println(this.myAgent.getName()+" : " +"Waiting for response");
			//System.out.println(this.data.cptAttenteRetour);
			this.cptAttenteRetour=this.cptAttenteRetour+1;
		}
	}
	//TODO ajouter fonction envoi données tresor et silo
	
	//Gestion des messages
	public void sendMessage(String content, boolean allWme, String name) {
		//1) receive the message

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
		    		 if(this.verboseSendMessage)
		    			 System.out.println(this.myAgent.getName()+ " : "+agentID.getName()+ " will receive a message");
		    	 }
		     }
		}
			
		//2° compute the random value		
		msg.setContent(content);
		((AbstractDedaleAgent) this.myAgent).sendMessage(msg);
		if(this.verboseSendMessage)
			System.out.println(this.myAgent.getName()+ " : Send message : "+content);
			
		
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
		if(this.verboseSendMessage)
			System.out.println(this.myAgent.getName()+ "Send message : "+content+" to : "+name.getName());
			
		
	}
	public void treatMessageForMapExchange(ACLMessage msg) {
		//TODO A tester
		String[] parts = msg.getContent().substring(msg.getContent().indexOf(' ') + 1).split("\\s+");
		if(parts[0].contains("ACK")) {
			this.actualProtocol = "";
			if(this.verboseMapExchange)
				System.out.println(this.myAgent.getName() + " : Ack acknowledged");
			for( int j = 0 ; j < this.inCommsWith.size() ; j++) {
				if(this.verboseDebug) {
					System.out.println("Comms");
					System.out.println(this.inCommsWith.get(j).getKey());
					System.out.println(this.inCommsWith.get(j).getValue());
				}
				this.clearCommsWith(msg.getSender());
			}
			
			Entry<String,Integer> pair1=new AbstractMap.SimpleEntry<String,Integer>(msg.getSender().getName(),this.cptTour);
			this.lastComms.add(pair1);
			this.sendTreasure(msg.getSender());
			if(parts.length>1) {
				this.siloPosition = parts[1];
				this.siloPositionOutdated = false;
				this.lastSeenSilo = LocalTime.now().toNanoOfDay();;
			}
		}
		else if(parts[0].contains("PING")) {
			this.actualProtocol = "MapExchange";
			if(this.verboseMapExchange)
				System.out.println(this.myAgent.getName() + " : Ping acknowledged");
			
			boolean alreadyTold = false;
			/*for(int i = 0 ; i < this.lastComms.size() ; i++) {
				if(this.getLastCommsKey(i) == msg.getSender().getName()) {
					if(this.cptTour - this.getLastCommsValue(i) < 10) {
						alreadyTold = true;
					}
				}
			}*/
			boolean alreadyInComm = false;

			if(alreadyTold || alreadyInComm) {
				this.actualProtocol = "";
				this.clearCommsWith(msg.getSender());
				if(this.myAgent.getName().contains("Tanker")) {
					this.sendMessage("MapExchange ACK "+this.myAgent.getCurrentPosition(), msg.getSender());
				}else {
					this.sendMessage("MapExchange ACK", msg.getSender());
				}
				
			}
			else {
				this.inCommsWith.add(new AbstractMap.SimpleEntry<AID,String>(msg.getSender(),"MapExchange"));
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
				System.out.println(this.myAgent.getName()+" : " +this.closedNodes.size());
			if(this.closedNodes.size() != 0) {
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
					System.out.println(this.myAgent.getName()+" : " +"Rien a envoyer");
				if(this.myAgent.getName().contains("Tanker")) {
					this.sendMessage("MapExchange ACK "+this.myAgent.getCurrentPosition(), msg.getSender());
				}else {
					this.sendMessage("MapExchange ACK", msg.getSender());
				}
				this.actualProtocol = "";

				this.clearCommsWith(msg.getSender());

			}
			
			
			
		}
		else if(parts[0].contains("EXPLORE")) {
			if(this.verboseMapExchange)
				System.out.println(this.myAgent.getName() + " : EXPLORE acknowledged");

			this.inCommsWith.add(new AbstractMap.SimpleEntry<AID,String>(msg.getSender(),"MapExchange"));
			if(this.verboseMapExchange)
				System.out.println(this.myAgent.getName()+" : Received EXPLORE");
			String content = "EXPLAIN ";
			String edgeContent = "";
			String noeud = "";
			String[] noeuds = parts[1].split("/");
			boolean cptTotal = false;
			boolean cpt = false;
			for (int i = 0 ; i<noeuds.length ; i++) {
				if(!this.closedNodes.contains(noeuds[i])) {
					cpt=true;
					cptTotal=true;
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
			String[] noeudRecu = parts[1].split("/");
			
			for (int i = 0 ; i<this.closedNodes.size() ; i++) {
				noeud = (String) this.closedNodes.toArray()[i].toString();
				if(this.verboseMapExchange)
					System.out.println(this.myAgent.getName()+" : " +"a|"+noeud+"|");
				boolean explainNode = true;
				for(int k = 0 ; k < noeudRecu.length ; k ++) {
					if(noeudRecu[k].equals(noeud)) {
						explainNode = false;
					}
				}
				if(explainNode) {
					cpt=true;
					cptTotal=true;
					content = content +noeud +"/";
					content.substring(0,content.length()-1);
					String[] e = this.edge.split(" ");
					for (int j = 0 ; j<e.length ; j++) {
						if(e[j].contains(noeud)) {
							if(this.verboseMapExchange)
								System.out.println(this.myAgent.getName()+" : " +"b|"+e[j]+"|");
							edgeContent = edgeContent +e[j] +"/";
							edgeContent.substring(0,edgeContent.length()-1);
						}
					}
				}
			}
			if(cpt == true) {
				content= "MapExchange "+content + " "+edgeContent;
				this.sendMessage(content, msg.getSender());
				if(this.verboseMapExchange)
					System.out.println(this.myAgent.getName()+" : Send "+content);					
			}
			if(cptTotal == false) {
				this.sendMessage("MapExchange ACK", msg.getSender());
				this.actualProtocol = "";
				this.clearCommsWith(msg.getSender());

			}
		}else if(parts[0].contains("EXPLAIN")) {
			if(this.verboseMapExchange)
				System.out.println(this.myAgent.getName() + " : EXPLAIN acknowledged");

			this.inCommsWith.add(new AbstractMap.SimpleEntry<AID,String>(msg.getSender(),"MapExchange"));
			if(this.verboseMapExchange)
				System.out.println(this.myAgent.getName()+" : Received EXPLAIN");
			String content = "UPDATE "+parts[1]+" ";
			System.out.println(this.myAgent.getName()+" : " +"c|"+parts[1]+"|");
			String edgeContent = "";
			String noeud = "";
			String[] noeuds = parts[1].split("/");

			boolean cpt = true;
			String[] edges = this.edge.split(" ");
			for (int i = 0 ; i<noeuds.length ; i++) {
				for(int j = 0 ; j < edges.length ; j++) {
					if(edges[j].contains(noeuds[i]))
					{
						if(this.verboseMapExchange)
							System.out.println(this.myAgent.getName()+" : " +"d|"+edges[j]+"|");
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
			/*content = "UPDATE ";
			edgeContent = "";
			noeud = "";
			cpt = false;
			for (int i = 0 ; i<this.closedNodes.size() ; i++) {
				noeud = (String) this.closedNodes.toArray()[i].toString();
				if(!parts[1].contains(noeud)) {
					cpt=true;
					if(this.verboseMapExchange)
						System.out.println(this.myAgent.getName()+" : " +"e|"+noeud+"|");
					content = content +noeud +"/";
					content.substring(0,content.length()-1);
					String[] e = this.edge.split(" ");
					for (int j = 0 ; j<e.length ; j++) {
						if(e[j].contains(noeud)) {
							if(this.verboseMapExchange)
								System.out.println(this.myAgent.getName()+" : " +"f|"+e[j]+"|");
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
			}*/
		}
		else if(parts[0].contains("UPDATE")) {
			if(this.verboseMapExchange)
				System.out.println(this.myAgent.getName() + " : Update acknowledged");
			
			boolean alreadyInComm = false;

			this.clearCommsWith(msg.getSender());

			
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
					System.out.println(this.myAgent.getName()+" : " +"Ajoute noeud et arc entre "+e[0] + " et "+e[1]);
				if(!this.openNodes.contains(e[0]) && !this.closedNodes.contains(e[0])) {
					this.addNode(e[0]);
				}
				if(!this.openNodes.contains(e[1]) && !this.closedNodes.contains(e[1])) {
					this.addNode(e[1], MapAttribute.open);
					this.openNodes.add(e[1]);
				}
				this.addEdge(e[0],e[1]);
				
			}
			if(this.myAgent.getName().contains("Tanker")) {
				this.sendMessage("MapExchange ACK "+this.myAgent.getCurrentPosition(), msg.getSender());
			}else {
				this.sendMessage("MapExchange ACK", msg.getSender());
			}
			this.cleanOpenNodes();
		}
		 
	}
	private void treatMessageForInterBlock(ACLMessage msg2) {
		//TODO Ajouter interblock pour ouverture de coffre
		String[] parts = msg2.getContent().substring(msg2.getContent().indexOf(' ') + 1).split("\\s+");
		if(this.verboseInterblock==true) {
			System.out.println(this.myAgent.getName()+" : " +"Treating msg for interblock : -"+ msg2.getContent().substring(msg2.getContent().indexOf(' ') + 1)+"-");
			System.out.println(this.myAgent.getName()+" : " +"Type of interblock msg : -"+parts[0]+"-");
		}
		if(parts[0].equals("OCCUPIED")) {
			this.waitingForOtherInterblock = true;
			this.cptWaitOtherInterblock = 0;
		}
		else if(parts[0].equals("STUCK")) {
			System.out.println(this.myAgent.getName()+" : " +"Treating stuck");
			if(parts[1].equals(this.myAgent.getCurrentPosition())) {
				boolean alreadyInComm = false;
				boolean alreadyStuckWElse = false;
				for( int j = 0 ; j < this.inCommsWith.size() ; j++) {
					if(this.inCommsWith.get(j).getKey().equals(msg2.getSender()) && this.inCommsWith.get(j).getValue().equals("InterBlock")) {
						alreadyInComm = true;
					}
					if(this.inCommsWith.get(j).getValue().equals("InterBlock")) {
						alreadyStuckWElse = true;
					}
				}
				if(alreadyStuckWElse) {
					this.sendMessage("InterBlock OCCUPIED",msg.getSender());
					
				}
				if(alreadyInComm) {
					//already started no need to continue this line of communication
					System.out.println(this.myAgent.getName()+" : " +"Already in comms with "+msg.getSender());
				}else {
					this.actualProtocol = "InterBlock";
					this.cptInterBlock = 0;
					this.inCommsWith.add(new AbstractMap.SimpleEntry<AID,String>(msg2.getSender(),"InterBlock"));
					this.sendMessage("InterBlock RSTUCK "+this.desiredPosition, msg2.getSender());
					
					if(this.verboseInterblock==true) 
						System.out.println(this.myAgent.getName() + " send RSTUCK");
				}
				
			}else {
				if(this.verboseInterblock==true) 
					System.out.println(this.myAgent.getName() + "Stuck not treated, not for me");
			}
		}else if(parts[0].equals("RSTUCK")) {
			if(parts.length == 1 || parts[1].equals(this.myAgent.getCurrentPosition())|| parts[1].equals(this.destination)) {
				boolean alreadyInComm = false;
				for( int j = 0 ; j < this.inCommsWith.size() ; j++) {
					if(this.inCommsWith.get(j).getKey().equals(msg2.getSender()) && this.inCommsWith.get(j).getValue().equals("InterBlock")) {
						alreadyInComm = true;
						break;
					}
				}
				if(alreadyInComm) {
					//already started no need to continue this line of communication
					if(this.verboseInterblock) 
						System.out.println(this.myAgent.getName()+" : " +"Already in comms with "+msg.getSender());
				}else {
					this.inCommsWith.add(new AbstractMap.SimpleEntry<AID,String>(msg2.getSender(),"InterBlock"));
				}
				String content = "InterBlock OBJECTIVE [";
				content+=this.objective+","+this.objectiveAttribute.getLeft()+","+this.objectiveAttribute.getRight()+","+Integer.toString(this.vidage)+"] ";
				if(this.destination.equals("")) {
					content+=this.myAgent.getCurrentPosition();
				}else {
					content+=this.destination;
				}
				if(this.desiredPosition.equals(this.destination) && !this.desiredPosition.equals("")) {
					this.path = new String[] {this.destination};
				}else if(this.destination.equals("")){
					this.path = new String[] {this.myAgent.getCurrentPosition()};
				}
				else {
					Object[] a= this.myMap.getShortestPath(this.desiredPosition, this.destination).toArray();
					this.path =  Arrays.copyOf(a, a.length, String[].class);
				}
				content+=" [";
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
			}else {
				this.sendMessage("InterBlock NOTYOU", msg2.getSender());
				this.clearCommsWith(msg2.getSender());
			}
		}else if(parts[0].equals("NOTYOU")){
			this.actualProtocol ="";
			this.clearCommsWith(msg2.getSender());
		}
		else if(parts[0].equals("OBJECTIVE")) {
			String objAttr = parts[1].substring(1, parts[1].length()-1);
			this.allyObjective = objAttr.split(" ")[0];
			if(this.allyObjective.equals("collect"))
				this.allyAttributes = new Couple<String,Integer>(objAttr.split(" ")[1],Integer.parseInt(objAttr.split(" ")[2]));
			else
				this.allyAttributes = new Couple<String,Integer>("",0);
			if(this.allyObjective.equals("collect")) {
				this.allyVidage = Integer.parseInt(objAttr.split(" ")[3]);
			}
			this.allyDestination = parts[2];
			this.allyPath = parts[3].split("/");
			this.allyBackPackSpace = Integer.parseInt(parts[4]);
			boolean emptyPath = true;
			String content = "InterBlock ROBJECTIVE [";
			content+=this.objective+","+this.objectiveAttribute.getLeft()+","+this.objectiveAttribute.getRight()+","+Integer.toString(this.vidage)+"] ";
			if(this.destination.equals("")) {
				content+=this.myAgent.getCurrentPosition();
			}else {
				content+=this.destination;
			}
			if(this.desiredPosition.equals(this.destination) || this.desiredPosition.equals("")) {
				this.path = new String[] {this.destination};
			}else if(this.destination.equals("")){
				System.out.println(this.myAgent.getName()+" : " +"Erreur pas de destination");
			}
			else {
				Object[] a= this.myMap.getShortestPath(this.desiredPosition, this.destination).toArray();
				this.path =  Arrays.copyOf(a, a.length, String[].class);
			}
			content+=" [";
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
				System.out.println(this.myAgent.getName()+" : " +"ROBJ treat");
			String objAttr = parts[1].substring(1, parts[1].length()-1);
			this.allyObjective = objAttr.split(",")[0];
			if(this.allyObjective.equals("collect"))
				this.allyAttributes = new Couple<String,Integer>(objAttr.split(" ")[1],Integer.parseInt(objAttr.split(" ")[2]));
			else
				this.allyAttributes = new Couple<String,Integer>("",0);
			if(this.allyObjective.equals("collect")) {
				this.allyVidage = Integer.parseInt(objAttr.split(" ")[3]);
			}
			this.allyDestination = parts[2];
			this.allyPath = parts[3].split("/");
			this.allyBackPackSpace = Integer.parseInt(parts[4]);
			//this.actualProtocol="InterBlockFindSolution";
			this.findInterBlockSolution(msg2.getSender());
		}
		else if(parts[0].equals("WAIT")) {
			String myName = this.myAgent.getName();
			String yName = msg2.getSender().getName();
		    String input = myName.substring(myName.length() -7);
		    String input2 = yName.substring(yName.length() -7);
			int myNb = Integer.parseInt(input.substring(0,1));
			int yNb = Integer.parseInt(input2.substring(0,1));
			
			boolean onChange =false;
			if(yName.contains("Tanker")){
				onChange =true;
			}
			if(yName.contains("Collec") && !myName.contains("Collect")) {
				onChange = true;
			}else if(yName.contains("Collect") && myName.contains("Collect") && myNb < yNb) {
				onChange = true;
			}else if(yName.contains("Explo") && myName.contains("Explo") && myNb < yNb) {
				onChange = true;
			}
			if((this.actualProtocol=="" || this.actualProtocol =="InterBlockWaitSolution")||(this.actualProtocol.equals("InterBlockGiveWay") && onChange)) {
				this.actualProtocol = "InterBlockWaiting";
				this.cptUntilRestart = 0;
				this.thanksAt = parts[1];
			}
		}
		else if(parts[0].equals("GO")) {
			String myName = this.myAgent.getName();
			String yName = msg2.getSender().getName();
		    String input = myName.substring(myName.length() -7);
		    String input2 = yName.substring(yName.length() -7);
			int myNb = Integer.parseInt(input.substring(0,1));
			int yNb = Integer.parseInt(input2.substring(0,1));
			boolean onChange =false;
			if(yName.contains("Tanker")){
				onChange =true;
			}
			if(yName.contains("Collec") && !myName.contains("Collect")) {
				onChange = true;
			}else if(yName.contains("Collect") && myName.contains("Collect") && myNb < yNb) {
				onChange = true;
			}else if(yName.contains("Explo") && myName.contains("Explo") && myNb < yNb) {
				onChange = true;
			}
			if((this.actualProtocol=="" || this.actualProtocol =="InterBlockWaitSolution")||(this.actualProtocol.equals("InterBlockWaiting") && onChange)) {
				this.thanksAt = "";
				this.actualProtocol = "InterBlockGiveWay";
				this.giveWayPosition = parts[1];
			}
		}
		else if(parts[0].equals("SWAP")) {
			System.out.println(this.myAgent.getName()+" : " +"Swap analysed");
			this.actualProtocol="";
			String[] objAttr = parts[1].substring(1, parts[1].length()-1).split(",");
			this.objective = objAttr[0];
			this.destination = parts[2];
			if(this.objective.equals("collect")) {
				this.objectiveAttribute = new Couple<String,Integer>(objAttr[1],Integer.parseInt(objAttr[2]));
			}
		}else if(parts[0].equals("MapExchange")) {

			this.clearCommsWith(msg2.getSender());

			
			System.out.println(this.myAgent.getName()+" : " +"Sending map");
			msg2.setContent(msg2.getContent().substring(msg2.getContent().indexOf(' ') + 1));
			this.msg=msg2;
			this.actualProtocol="";
			if(this.objective.contains("explore"))
					this.setDestination("");
			this.getMessage();
		}else if(parts[0].equals("GoingElseWhere")) {

			this.clearCommsWith(msg2.getSender());

			
			this.actualProtocol="";
		}
		else {
			if(this.verboseInterblock) 
				System.out.println(this.myAgent.getName()+" : " +"Don't know what to do with : Type of interblock msg : -"+parts[0]+"-");
		}
		
	}
	private void treatMessageForSilo(ACLMessage msg) {
		String content =msg.getContent().substring(msg.getContent().indexOf(' ') + 1);
		String[] parts =content.split(" "); 
		if(parts[0].contains("?") && this.type.equals("Silo")) {
			if(this.verboseMessage)
				System.out.println(this.myAgent.getName()+ " : received a demand for position" );
			
			this.sendMessage("SILO HERE "+this.myAgent.getCurrentPosition(), msg.getSender());
		}else if(parts[0].contains("?") && !this.type.equals("Silo") && !this.siloPositionOutdated) {
			this.sendMessage("SILO MAYBE "+this.siloPosition+" "+Long.toString(this.lastSeenSilo), msg.getSender());
		}else if(parts[0].contains("HERE")) {
			this.siloPosition = parts[1];
			this.siloAID = msg.getSender();
			this.lastSeenSilo =  LocalTime.now().toNanoOfDay();
			this.siloPositionOutdated = false;
			this.lookingForSilo = false;
			this.sendTreasure(msg.getSender());
			if((this.objective == "WaitingForOrder" ||this.objective == "RetourSilo")&&!this.siloAvailableSent) {
				this.siloAvailableSent = true;
				if(this.verboseOrder)
					System.out.println(this.myAgent.getName()+ " : Sending Silo AVAILABLE########################################" );
				String contenu = "SILO AVAILABLE "+this.type+" "+Integer.toString(this.lockpickStrength)+" "+Integer.toString(this.strength);
				contenu += " "+Integer.toString(this.goldCap)+" "+Integer.toString(this.diamondCap);
				this.sendMessage(contenu,msg.getSender());
			}else if(this.objective == "vidage") {
				this.sendMessage("SILO EMPTYING",msg.getSender());
				
			}
		}else if(parts[0].contains("MAYBE")){
			if(Long.parseLong(parts[2])>this.lastSeenSilo || this.siloPositionOutdated) {
				this.siloPosition = parts[1];
				this.siloAID = msg.getSender();
				this.lastSeenSilo = Long.parseLong(parts[2]);
			}			
		}else if(parts[0].equals("EMPTYING")) {
			this.sendMessage("SILO EMPTYING_ALLOWED",msg.getSender());
		}else if(parts[0].equals("EMPTYING_ALLOWED")){
			boolean empty2 = this.myAgent.emptyMyBackPack("Tanker");
			if(this.verboseCollect) {
				System.out.println(this.myAgent.getName()+" : Emptying myself");
				System.out.println(this.myAgent.getName()+" : Emptying2 achieve ? : "+Boolean.toString(empty2));
			}
			this.objective = this.findMyObjective();
			if(this.verboseCollect) {
				System.out.println(this.myAgent.getName()+" : Going back to :"+this.objective);
			}
		}else if(parts[0].contains("AVAILABLE") && this.type.equals("Silo")){

			this.list_order.updateOrderList(this.treasure);
			String type = parts[1];
			int loStr = Integer.parseInt(parts[2]);
			int sStr = Integer.parseInt(parts[3]);
			int gSize = Integer.parseInt(parts[4]);
			int dSize = Integer.parseInt(parts[5]);
			String order = this.list_order.agentGetAffectationOrder(msg.getSender(), type, loStr, sStr, gSize, dSize);
			if(order == "") {
				this.sendMessage("ORDER NOTHING", msg.getSender());
			}else {
				if(this.verboseOrder) {
					System.out.println(this.myAgent.getName()+" : Order delivered : -"+order+"-");
				}
				this.sendMessage(order,msg.getSender());
			}
		}
	}
	private String findMyObjective() {
		if(this.type == "Collect") {
			if(this.myAgent.getBackPackFreeSpace()<this.sizeTreshold) {
				return "vidage";
			}else {
				if(this.getNbTreasureAccessible(this.lockpickStrength, this.strength)!=0) {
					return "collect";
				}else {
					return "explore";
				}
			}
		}else if(this.type == "Explo"){
			if(this.exploFinished) {
				return "RetourSilo";
			}
			return "explore";//TODO Ajouter la séléction d'objectif pour les types explorateurs
		}else {
			return "explore";
		}
	}

	private void treatMessageForInterBlockWaiting(ACLMessage msg2) {
		String[] parts = msg.getContent().substring(msg.getContent().indexOf(' ') + 1).split("\\s+");
		
	}
	private void treatMessageForInterBlockGiveWay(ACLMessage msg2) {
		//TODO A tester
		String[] parts = msg.getContent().substring(msg.getContent().indexOf(' ') + 1).split("\\s+");
		if(parts[0].equals("Thanks") && this.actualProtocol =="InterBlockWaiting"){
			this.actualProtocol="";
			this.destination = this.previousDestination;

			this.clearCommsWith(msg.getSender());

		}
		
	}
	private void treatMessageForTreasureExchange(ACLMessage msg2) {
		String content =msg.getContent().substring(msg.getContent().indexOf(' ') + 1);
		content= content.substring(msg.getContent().indexOf(' ') + 1);
		String[] parts = content.split("\\s+");
		boolean notUpToDate = false;
		for(int i = 0 ; i < parts.length ; i++) {
			if(this.verboseTreasureExchange) {
				System.out.println(this.myAgent.getName()+" : Treasure received : "+ parts);
			}
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
		this.list_order.updateOrderList(this.treasure);
		if(notUpToDate) {
			this.sendTreasure(msg2.getSender());
		}
	}
	private void treatMessageForOrder(ACLMessage msg2) {
		String content =msg.getContent().substring(msg.getContent().indexOf(' ') + 1);
		String[] parts =content.split(" "); 
		if(parts[0].equals("NOTHING")) {
			this.siloAvailableSent = false;
			//TODO Treat when no order assigned
			this.objective = "explore";
		}
		else if(parts[0].equals("UNLOCK")) {
			this.siloAvailableSent = false;
			this.executingOrder = true;
			this.objective = "Unlock";
			this.destination = parts[1];
			this.order_agentnumber = Integer.parseInt(parts[2]);
			this.order_lstrneeded = Integer.parseInt(parts[3]);
			this.order_strneeded = Integer.parseInt(parts[4]);
			this.order_waitingForOthers = (Integer.parseInt(parts[4])==0);
			this.order_startOnArrival = Boolean.parseBoolean(parts[6]);
			if(parts[7].equals("Gold")) {
				this.order_chestType = Observation.GOLD;
			}else {
				this.order_chestType = Observation.DIAMOND;
			}
			if(this.verboseOrder) {
				System.out.println(this.myAgent.getName()+ " : Order at "+this.destination);
			}
		}else if(parts[0].equals("EMPTY")) {
			this.siloAvailableSent = false;
			this.objective = "Empty";
			this.destination = parts[1];
			this.order_startOnArrival = Boolean.parseBoolean(parts[2]);
			
		}
	}
	private void treatMessageForOrderCompletion(ACLMessage msg2) {
		if(this.executingOrder) {
			String content =msg.getContent().substring(msg.getContent().indexOf(' ') + 1);
			String[] parts =content.split(" "); 
			if(this.verboseOrder) {
				System.out.println(this.myAgent.getName()+" : Received arrived at order pos : "+parts[0]+" mine is at : "+this.destination);
				System.out.println(this.myAgent.getName()+" : I'm at  : "+this.myAgent.getCurrentPosition());
				System.out.println(this.myAgent.getName()+" : my order number : "+this.order_agentnumber);
				System.out.println(this.myAgent.getName()+" : Can i open it? : "+parts[1]);
			}
			if(parts[0].equals(this.destination)) {
				if(this.order_agentnumber == 0 && Boolean.parseBoolean(parts[1])) {
					/*if(this.objectiveAttribute.getLeft().equals("Gold")) {
						this.myAgent.openLock(Observation.GOLD);
					}else if(this.objectiveAttribute.getLeft().equals("Diamond")) {
						this.myAgent.openLock(Observation.DIAMOND);
					}
					this.objective = "RetourSilo";*/
					this.arrivedAtOrderDestinationAtMsgReceived();
				}
			}
		}
	}
	private void treatMessageForOrderComplete(ACLMessage msg2) {
		this.orderDone();
	}

	private void treatMessageForOrderCompleteSilo(ACLMessage msg2) {
		if(this.myAgent.getName().contains("Tanker")) {
			String content =msg.getContent().substring(msg.getContent().indexOf(' ') + 1);
			String[] parts =content.split(" ");
			String type = parts[0];
			String pos = parts[1];
			if(this.type.equals("UNLOCK")) {
				
			}else if(this.type.equals("EMPTY")) {
				int val = Integer.parseInt(parts[2]);
			}
		}
		
	}
	public void getMessage() {
		if(this.verboseMessage2==true)
			System.out.println(this.myAgent.getName()+" : Receiving message");
		this.cleanOpenNodes();
		this.cleanEdges();
		final MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		ACLMessage m = null;
		//2) get the message
		if(this.msg != null) {
			m = this.msg;
		}
		else {
			m = this.myAgent.receive(msgTemplate);
			if(m == null) {
				if(this.verboseMessage2==true)
					System.out.println(this.myAgent.getName() + " : Pas de message");
			}
			
		}
		this.msg = m;

		while (this.msg != null &&this.msg.getContent() != null) {	
			if(this.verboseMessage==true)
				System.out.println(this.myAgent.getName()+" : " +m.getContent() + " Message received");
			if(this.verboseMessage==true)
				System.out.println(this.myAgent.getName()+" : Received "+msg.getContent() +" from "+msg.getSender().getName());	
			
			String[] parts = msg.getContent().split("\\s+");
			System.out.println(this.myAgent.getName()+" : " +"Type of msg : -"+parts[0]+"- to treat");
			if(parts[0].contains("InterBlock")) {
				this.treatMessageForInterBlock(this.msg);
			}else if(parts[0].equals("MapExchange")) {
				String[] part = parts.clone();
				this.treatMessageForMapExchange(this.msg);
			}else if(parts[0].equals("InterBlockGiveWay")) {
				this.treatMessageForInterBlockGiveWay(this.msg);
			}else if(parts[0].equals("InterBlockWaiting")) {
				this.treatMessageForInterBlockWaiting(this.msg);
			}else if(parts[0].equals("Treasure")&&parts[1].equals("EXCHANGE")) {
				this.treatMessageForTreasureExchange(this.msg);
			}else if(parts[0].equals("SILO")) {
				this.treatMessageForSilo(this.msg);
			}else if(parts[0].equals("ORDER")) {
				this.treatMessageForOrder(this.msg);
			}else if(parts[0].equals("ARRIVED_FOR_ORDER")) {
				this.treatMessageForOrderCompletion(this.msg);
			}else if(parts[0].equals("ORDER_COMPLETE")) {
				this.treatMessageForOrderCompleteSilo(this.msg);
			}else if(parts[0].equals("ORDER_DONE")) {
				this.treatMessageForOrderComplete(this.msg);
			}
			//Map exchange protocolanObject

			System.out.println(this.myAgent.getName()+" : " +"Type of msg : -"+parts[0]+"-  treated");
			this.msg = this.myAgent.receive(msgTemplate);
		}
	}

	public void setMsg(ACLMessage m) {
		this.msg = m;
	}
	public ACLMessage getMsg() {
		return this.msg;
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
	
	public void arrivedAtOrderDestination() {
		if(this.order_startOnArrival)
			this.sendMessage("ARRIVED_FOR_ORDER "+this.destination+ " "+ Boolean.toString(this.order_startOnArrival)+" "+this.myAgent.getAID().toString(),false, "");

		if(this.verboseOrder) {
			System.out.println(this.myAgent.getName()+" : Executing order");
			System.out.println(this.myAgent.getName()+" : Type : "+this.objective);
			System.out.println(this.myAgent.getName()+" : Start on arrival : "+Boolean.toString(this.order_startOnArrival));
		}
		if(this.order_agentnumber == 0 && this.order_startOnArrival) {
			if(this.objective == "UNLOCK" ||this.objective == "Unlock") {
				if(this.verboseOrder) {
					System.out.println(this.myAgent.getName()+" : Opening lock @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
				}
				this.myAgent.openLock(this.order_chestType);
				this.updateTreasure(this.myAgent.getCurrentPosition(),"OPEN");
				this.messageForSilo = "ORDER_COMPLETE UNLOCK "+this.destination;
				this.objective = "RetourSilo";
				if(this.siloPositionOutdated == false && this.siloPosition!="") {
					this.destination = this.siloPosition;
				}
			}else if(this.objective == "EMPTY" ||this.objective == "Empty") {
				if(this.verboseOrder) {
					System.out.println(this.myAgent.getName()+" : Emptying chest");
				}
				this.myAgent.pick();
				this.messageForSilo = "ORDER_COMPLETE EMPTY "+this.destination + this.initBackPackSize;
				this.objective = "RetourSilo";
				if(this.siloPositionOutdated == false && this.siloPosition!="") {
					this.destination = this.siloPosition;
				}
			}
			this.executingOrder = false;
		}
	}
	public void arrivedAtOrderDestinationAtMsgReceived() {
		if(this.verboseOrder) {
			System.out.println(this.myAgent.getName()+" : Executing order");
			System.out.println(this.myAgent.getName()+" : Type : "+this.objective);
			System.out.println(this.myAgent.getName()+" : Start on arrival : "+Boolean.toString(this.order_startOnArrival));
		}
		if(this.order_agentnumber == 0) {
			if(this.objective == "UNLOCK" ||this.objective == "Unlock") {
				if(this.myAgent.openLock(this.order_chestType)) {
					this.sendMessage("ORDER_DONE "+this.destination+" "+this.myAgent.getAID().toString(),false, "");
					this.updateTreasure(this.myAgent.getCurrentPosition(),"OPEN");
					this.messageForSilo = "ORDER_COMPLETE UNLOCK "+this.destination;
					this.objective = "RetourSilo";
					this.executingOrder = false;
					if(this.siloPositionOutdated == false && this.siloPosition!="") {
						this.destination = this.siloPosition;
					}
				}else {
					if(this.verboseOrder) {
						System.out.println("\n\n\n"+this.myAgent.getName()+" : Opening FAILED\n\n\n");
					}
				}
			}else if(this.objective == "EMPTY" ||this.objective == "Empty") {
				if(this.verboseOrder) {
					System.out.println(this.myAgent.getName()+" : Emptying chest");
				}
				this.myAgent.pick();
				this.messageForSilo = "ORDER_COMPLETE EMPTY "+this.destination + this.initBackPackSize;
				this.objective = "RetourSilo";
				this.executingOrder = false;
				if(this.siloPositionOutdated == false && this.siloPosition!="") {
					this.destination = this.siloPosition;
				}
			}
			this.executingOrder = false;
		}
	}
	public void orderDone() {
		this.executingOrder = false;
		if(this.objective == "UNLOCK" ||this.objective == "Unlock") {
			this.updateTreasure(this.myAgent.getCurrentPosition(),"OPEN");
		}
		this.objective = "RetourSilo";
		if(this.siloPositionOutdated == false && this.siloPosition!="") {
			this.destination = this.siloPosition;
		}
	}
	public void updateTreasure(String currentPosition, String type) {
		boolean set = false;
		for(int i = 0 ; i < this.getNbTreasure() ; i++) {
			if(this.treasure.get(i).getPosition().equals(currentPosition)) {
				if(type == "OPEN") {
					this.treasure.get(i).setOpen();
				}
			}
		}		
	}
	private void updateTreasure(String currentPosition, String type, int val) {
		boolean set = false;
		for(int i = 0 ; i < this.getNbTreasure() ; i++) {
			if(this.treasure.get(i).getPosition().equals(currentPosition)) {
				if(type == "EMPTYING") {
					this.treasure.get(i).reduceValue(val);
				}
			}
		}		
	}

	//Interblock
	public boolean detectIfStuck() {
		//TODO A tester
		if(!this.myAgent.getCurrentPosition().equals(this.previousPosition) && this.stuckCounter >= 3) {
			this.actualProtocol = "";
			this.stuckCounter = 0;
			this.stuckSended = false;
		}
		if(this.verboseDebug)
			System.out.println(this.myAgent.getName()+ " : detect if stuck function");
		if(this.myAgent.getCurrentPosition().equals(this.destination)) {
			if(this.verboseDebug)
				System.out.println(this.myAgent.getName()+ " : detect if stuck function debug on same pos");
			this.stuckCounter = 0;
			return false;
		}
		if(this.myAgent.getCurrentPosition().equals(this.previousPosition) /*&& this.actualProtocol.equals("")*/) {
			this.stuckCounter+=1;
			System.out.println(this.myAgent.getName()+" : " +"Stuck counter : "+Integer.toString(this.stuckCounter));
			if(this.executingOrder && this.order_agentnumber>= this.myMap.getShortestPath(this.myAgent.getCurrentPosition(), this.destination).size()) {
				this.arrivedAtOrderDestination();
			}
			else if(this.stuckCounter > this.stuckTreshold && this.stuckSended == false) {
				System.out.println(this.myAgent.getName()+" : " +"#############Stuck#############");
				this.stuckSended = true;
				return true;
			}else if(this.stuckSended && this.stuckCounter > this.stuckTreshold*2) {
				String [] forbid = {this.desiredPosition};
				this.myPath = this.parcoursLargeur(this.myAgent.getCurrentPosition(), this.destination, forbid);
				if(this.myPath != null) {
					this.followingCustomPath = true;
				}
				this.stuckCounter = 0;
				this.stuckSended = false;
			}
		}else {
			this.stuckCounter=0;
			this.stuckSended = false;
		}
		this.previousPosition = this.myAgent.getCurrentPosition();
		return false;
	}
	public boolean startInterBlockProcedure() {
		//TODO A tester
		if(this.verboseDebug)
			System.out.println(this.myAgent.getName()+" : " +"Start interblock proc");
		if(this.actualProtocol.equals("")) {
			this.previousObjective = this.objective;
			this.actualProtocol="InterBlock";
			this.cptInterBlock = 0;
			this.sendMessage("InterBlock STUCK "+this.desiredPosition,false, "");
			return false;
		}
		return true;
	}
	private String findInterBlockSolution(AID sender) {
		//TODO A tester
		if(this.verboseInterblock) {
			System.out.println(this.myAgent.getName()+" : " +"Finding solution");
			System.out.println(this.myAgent.getName()+" : " +this.objective);
			System.out.println(this.myAgent.getName()+" : " +this.allyObjective);
		}
		if(this.objective.contains("explore") || this.allyObjective.contains("explore")) {
			
			if(this.verboseInterblock) 
				System.out.println(this.myAgent.getName()+" : " +"equal obj");


			this.clearCommsWith(sender);
			this.actualProtocol="MapExchange";
			this.cptMapExchange=0;
			if(this.verboseInterblock) 
				System.out.println(this.myAgent.getName()+" : " +"2 explore colliding swapping map");
			this.sendMessage("InterBlock MapExchange ASK", sender);
			if(this.objective.contains("explore"))
				this.setDestination("");
			return "Destination SWAP "+this.objective;
		}else if(this.objective.equals("collect") && this.objective.equals(this.allyObjective)) {
			if(this.vidage == this.allyVidage && this.vidage == 0) {
				this.clearCommsWith(sender);
				this.actualProtocol="";
				//TODO penser a un meilleur moyen qu'echanger les destinations lorsque les deux se bloquant vont chercher un tresor sans le vider
				this.sendMessage("InterBlock SWAP ["+this.objective+ "," +this.objectiveAttribute.getLeft() +","+this.objectiveAttribute.getRight() +","+Integer.toString(this.vidage)+ "] " + this.destination, sender); 
				this.setDestination(this.allyDestination);
				this.objectiveAttribute=this.allyAttributes;
				if(this.verboseInterblock) 
					System.out.println(this.myAgent.getName()+" : " +"SWAPING Obj");
				return "Destination SWAP "+this.objective;
			}
			else if(this.vidage == this.allyVidage && this.vidage == 1 && this.allyBackPackSpace >= this.objectiveAttribute.getRight() && this.getBackPackSize() >= this.allyAttributes.getRight()) {

				this.clearCommsWith(sender);
				this.actualProtocol="MapExchange";
				this.sendMessage("InterBlock SWAP ["+this.objective+ "," +this.objectiveAttribute.getLeft() +","+this.objectiveAttribute.getRight() +","+Integer.toString(this.vidage)+ "] " + this.destination, sender);
				this.setDestination(this.allyDestination);
				this.objective=this.allyObjective;
				this.objectiveAttribute = this.allyAttributes;
				if(this.verboseInterblock) 
					System.out.println(this.myAgent.getName()+" : " +"SWAPING Obj");
				return "Destination SWAP "+this.objective;
			}
		}else if(!this.allyDestination.equals(this.desiredPosition)) {
			if(this.verboseInterblock) 
				System.out.println(this.myAgent.getName()+" : " +"Protocole evitement");
			//Protocole d'évitement
			//Test si c'est moi qui bouge
			String[] neigh = this.getNeighbour(this.myAgent.getCurrentPosition()).toArray(new String[0]);
			String noeudEvitement="";
			String[] ch = this.allyPath.clone();
			List<String> c = new LinkedList<String>(Arrays.asList(ch));
			//List<String> c = Arrays.asList(ch);
			if(ch.length>1) {
				ch=this.removeTheElement(ch,0);/*
				c.remove(0);
				ch = (String[]) c.toArray();*/
			}

			int dist = 0;
			boolean isThereNodeNotInPath= false;
			String noeudActu="";
			while(isThereNodeNotInPath == false &&ch.length > 0) {
				noeudActu = ch[0];
				dist+=1;
				for(int j = 0 ; j < neigh.length ; j++) {
					if(!Arrays.asList(this.allyPath).contains(neigh[j]) && !neigh[j].equals(this.siloPosition) && !neigh[j].equals(this.desiredPosition)) {
						noeudEvitement = neigh[j];
						isThereNodeNotInPath = true;
						break;
					}
				}
				neigh = this.getNeighbour(ch[0]).toArray(new String[0]);
				//c = Arrays.asList(ch);
				c = new LinkedList<String>(Arrays.asList(ch));
				if(ch.length>1) {
					ch=this.removeTheElement(ch,0);
					/*c.remove(0);
					ch = (String[]) c.toArray();*/
				}
			}
			if(noeudEvitement.equals("")) {
				dist = 100000;
			}
			//Test si c'est lui qui bouge
			String noeudEvitement1="";
			int dist1 = 0;
			String[] neigh1 = this.getNeighbour(this.desiredPosition).toArray(new String[0]);
			String[] ch1 = this.path.clone();
			//List<String> c1 = Arrays.asList(ch1);
			List<String> c1 =  new LinkedList<String>(Arrays.asList(ch));
			if(ch1.length>1) {
				ch1=this.removeTheElement(ch1,0);
				/*c1.remove(0);
				ch1 = (String[]) c1.toArray();*/
			}

			String noeudActu1="";
			boolean isThereNodeNotInPath1= false;
			while(isThereNodeNotInPath1 == false&&ch1.length > 0) {
				noeudActu1 = ch1[0];
				dist1+=1;
				for(int j = 0 ; j < neigh1.length ; j++) {
					if(!Arrays.asList(this.path).contains(neigh1[j])&& !neigh1[j].equals(this.siloPosition)&& !neigh1[j].equals(this.myAgent.getCurrentPosition())) {
						noeudEvitement1 = neigh1[j];
						isThereNodeNotInPath1 = true;
						break;
					}
				}
				neigh1 = this.getNeighbour(ch1[0]).toArray(new String[0]);
				c1= new LinkedList<String>(Arrays.asList(ch1));
				//c1 = Arrays.asList(ch1);
				if(ch1.length>1) {
					ch1=this.removeTheElement(ch1,0);
					/*c1.remove(0);
					ch1 = (String[]) c1.toArray();*/
				}

			}
			
			if(noeudEvitement1.equals("")) {
				dist1 = 100000;
			}
			if(dist == dist1) {
				//TODO Si les deux n'ont pas de noeud a aller pour debloquer la situation
			}
			if(dist <= dist1) {
				//Moi qui bouge
				this.sendMessage("InterBlock WAIT "+noeudActu, sender);
				this.actualProtocol = "InterBlockGiveWay";
				this.giveWayPosition = noeudEvitement;
				/*this.previousDestination = this.destination;
				this.destination = noeudEvitement;*/
				if(this.verboseInterblock) 
					System.out.println(this.myAgent.getName()+" : " +"Wait here while i go at "+noeudEvitement);
			}else {
				//Toi qui bouge
				this.sendMessage("InterBlock GO " + noeudEvitement1, sender);
				this.thanksAt = noeudActu1;
				this.cptUntilRestart = 0;
				this.actualProtocol="InterblockWaiting";
				if(this.verboseInterblock) 
					System.out.println(this.myAgent.getName()+" : " +"Go here "+noeudActu);
			}
		}else {
			boolean set = false;
			//try {
				String [] forbid = {this.desiredPosition};
				this.myPath = this.parcoursLargeur(this.myAgent.getCurrentPosition(), this.destination, forbid);
				if(this.verboseInterblock)
					System.out.println(this.myAgent.getName() + " : Following my own path : "+this.myPath);
				if(this.myPath != null) {
					this.followingCustomPath = true;
					this.objective = this.previousObjective;
					if(this.myPath.size() >=2)
						set = true;
						this.sendMessage("InterBlock GoingElseWhere", sender);
						this.actualProtocol = "";
						this.clearCommsWith(sender);
				}
			if(this.allyObjective.equals("silo") /*&&*/ || !set) {
				this.followingCustomPath = false;
				String res = this.findDestExplNotGoingThrough(this.desiredPosition);
				if(res != "") {//Si on peux trouver un autre trajet qui ne passe pas par la case bloqué
					this.destination = res;
					List<String> cha = this.myMap.getShortestPath(this.myAgent.getCurrentPosition(), this.destination);
					if(this.verboseInterblock) {
						System.out.println(this.myAgent.getName()+" : Finding another path not passing through tanker : "+this.destination);
						System.out.println(this.myAgent.getName()+" : Path "+cha);
					}
					this.actualProtocol = "";
					//this.actualProtocol = this.previousObjective;
					this.clearCommsWith(sender);
				}else {//TODO Si on ne peux trouver d'autre trajet
					//this.actualProtocol = this.previousObjective;
					this.clearCommsWith(sender);
					this.actualProtocol = "";
				}
				this.sendMessage("InterBlock GoingElseWhere", sender);
				this.stuckCounter = 0;
				if(this.verboseMessage) {
					System.out.println("ExchangeMap with silo");
				}
				this.switchToMsgSending = true;
				
			}
			
		}
		if(this.verboseInterblock) 
			System.out.println(this.myAgent.getName()+" : " +"Protocole evitement : FIN");
		return "";
	}
	
	public void movement() {
		this.getMessage();
		//TODO A tester
		if(this.verboseMessage2) {
			System.out.println(this.myAgent.getName()+" : My comms");
			for( int j = 0 ; j < this.inCommsWith.size() ; j++) {
				System.out.println("Comms for "+this.inCommsWith.get(j).getValue()+" with "+this.inCommsWith.get(j).getKey());
			}
		}
		if(this.verboseEtatAgent && this.type != "Silo") {
			System.out.println(this.myAgent.getName()+" : " +"Pre movement recap");
			System.out.println(this.myAgent.getName()+" : " +"Agent : "+this.myAgent.getName());
			System.out.println(this.myAgent.getName()+" : " +"Position : " + this.myAgent.getCurrentPosition());
			System.out.println(this.myAgent.getName()+" : " +"Destination : " + this.destination);
			System.out.println(this.myAgent.getName()+" : " +"Protocole  : " + this.actualProtocol);
			System.out.println(this.myAgent.getName()+" : " +"Type  : " + this.type);
			System.out.println(this.myAgent.getName()+" : " +"Objective  : " + this.objective);
			System.out.println(this.myAgent.getName()+" : " +"Ouvert");
			System.out.println(this.myAgent.getName()+" : " +this.openNodes);
			System.out.println(this.myAgent.getName()+" : " +this.myAgent.getName()+" : " +"Fermé");
			System.out.println(this.myAgent.getName()+" : " +this.closedNodes);
			System.out.println(this.myAgent.getName()+" : " +this.waitingForResponse);
			System.out.println(this.myAgent.getName()+" : Executing order  " +this.executingOrder);
			System.out.println(this.myAgent.getName()+" : Custom path : " +this.followingCustomPath);
			System.out.println(this.myAgent.getName()+" : Custom path : " +this.myPath);
		}
		if(this.type != "Silo" &&this.actualProtocol!="InterBlock" &&this.actualProtocol!="MapExchange") {
			/*if(this.stuckCounter>0) {
				this.switchToMsgSending = true;
			}*/
			if(this.detectIfStuck()) {
				if(this.verboseDebug)
					System.out.println(this.myAgent.getName()+" : "+"Stuck detected");
				if(this.objective=="Unlock") {
					//TODO Verifier si on est dans une chaine vers l'objectif
				}
				this.startInterBlockProcedure();
			}
		}
		this.cptTour++;
		if(this.actualProtocol == "InterBlockWaiting") {
			if(this.verboseMovement)
				System.out.println(this.myAgent.getName()+" : Movement InterBlockWaiting");
			this.cptUntilRestart +=1;
			if(this.cptUntilRestart > 3) {
				if(this.myAgent.getCurrentPosition().equals(this.thanksAt)) {
					this.sendMessage("InterBlockGiveWay Thanks",false,"");
					this.actualProtocol = "";
					this.desiredPosition = this.myMap.getShortestPath(this.myAgent.getCurrentPosition(), this.destination).get(0);
					int n = 1;
					if(this.myAgent.getCurrentPosition().equals(this.desiredPosition )) {
						this.desiredPosition =this.myMap.getShortestPath(this.myAgent.getCurrentPosition(), this.destination).get(n);
						n+=1;
					}
					((AbstractDedaleAgent)this.myAgent).moveTo(this.desiredPosition);
				}
				else {
					this.desiredPosition = this.myMap.getShortestPath(this.myAgent.getCurrentPosition(), this.thanksAt).get(0);
					int n = 1;
					if(this.myAgent.getCurrentPosition().equals(this.desiredPosition )) {
						this.desiredPosition =this.myMap.getShortestPath(this.myAgent.getCurrentPosition(), this.destination).get(n);
						n+=1;
					}
					((AbstractDedaleAgent)this.myAgent).moveTo(this.desiredPosition);
				}
				
			}
		}else if(this.actualProtocol == "InterBlockGiveWay") {
			if(this.verboseMovement)
				System.out.println(this.myAgent.getName()+" : Movement InterBlockGiveWay");
			if(!this.giveWayPosition.equals(this.myAgent.getCurrentPosition())) {
				//TODO verifier qu'on a toujours une position ou se deplacer
				this.desiredPosition = this.myMap.getShortestPath(this.myAgent.getCurrentPosition(), this.giveWayPosition).get(0);
				if(this.myAgent.getCurrentPosition().equals(this.desiredPosition )) {
					this.desiredPosition =this.myMap.getShortestPath(this.myAgent.getCurrentPosition(), this.giveWayPosition).get(1);
				}
				((AbstractDedaleAgent)this.myAgent).moveTo(this.desiredPosition);
			}
		}else if(this.waitingForOtherInterblock ||  !this.inCommsWith.isEmpty() || this.waitingForResponse || this.actualProtocol == "MapExchange"){
			if(this.verboseMovement) {
				System.out.println(this.myAgent.getName()+" : Movement GetMessage");
				System.out.println(this.myAgent.getName()+" : Protocole :"+this.actualProtocol);
				System.out.println(this.myAgent.getName()+" : In commsWith empty? :"+Boolean.toString(this.inCommsWith.isEmpty()));
				System.out.println(this.myAgent.getName()+" : My comms");
				for( int j = 0 ; j < this.inCommsWith.size() ; j++) {
					System.out.println("Comms for "+this.inCommsWith.get(j).getValue()+" with "+this.inCommsWith.get(j).getKey());
				}
			}
			if(this.actualProtocol=="MapExchange") {

				this.cptMapExchange+=1;
				if(this.cptMapExchange>this.cptMapExchangeTreshold) {
					this.actualProtocol = "";
					for( int j = 0 ; j < this.inCommsWith.size() ; j++) {
						System.out.println("Comms for "+this.inCommsWith.get(j).getValue()+" with "+this.inCommsWith.get(j).getKey());
						if(this.inCommsWith.get(j).getValue()=="MapExchange") {
							this.inCommsWith.remove(j);
						}
					}
				}
			}
			if(this.actualProtocol=="InterBlock") {

				this.cptInterBlock+=1;
				if(this.cptInterBlock>this.cptInterBlockTreshold) {
					this.actualProtocol = "";
				}
			}
			if(this.waitingForOtherInterblock) {
				if(this.verboseInterblock) {
					System.out.println(this.myAgent.getName()+" : Movement GetMessage");
				}
				this.cptWaitOtherInterblock+=1;
				if(this.cptWaitOtherInterblock >= this.waitingForOtherInterblockThreshold) {
					this.waitingForOtherInterblock=false;
					this.cptWaitOtherInterblock=0;
				}
			}
			this.getMessage();
		}else if(this.type == "Silo"){//Gestion déplacement du silo 
			/*this.siloPositionSet+=1;
			if(this.siloPositionSet > this.siloTimeUntilRelocate) {
				if(this.objective == "Repositioning") {
					String nextNode=this.myMap.getShortestPath(this.myAgent.getCurrentPosition(), this.destination).get(0);
					int n = 1;
					if(this.myAgent.getCurrentPosition().equals(nextNode)) {
						nextNode=this.myMap.getShortestPath(this.myAgent.getCurrentPosition(), this.destination).get(n);
						n+=1;
					}
					if(nextNode.equals(this.destination)) {
						this.objective = "Silo";
						this.siloPositionSet = 0;
					}
					if(!nextNode.equals(this.myAgent.getCurrentPosition()) || nextNode != "") {
						this.desiredPosition = nextNode;
						((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
					}	
				}else {
					String newPos = this.findDestination();
					String myPos = this.myAgent.getCurrentPosition();
					if(newPos != "") {
						int newScore = this.determinePositionScore(newPos);
						int oldScore = this.determinePositionScore(myPos);
						if(newPos != "" && !this.destination.equals(myPos) && newScore>oldScore) {//TODO ne pas oublier de changer le sens de < a >
							this.destination = newPos;
							this.objective = "Repositioning";
							if(this.verboseMovement) {
								System.out.println(this.myAgent.getName()+" : Movement Repositioning");
								System.out.println(this.myAgent.getName()+" : Movement Repositioning : Last Score of : "+myPos+" : "+Integer.toString(oldScore));
								System.out.println(this.myAgent.getName()+" : Movement Repositioning : Score of : "+newPos+" : "+Integer.toString(newScore));
							}
						}
					}
					else {
						this.siloPositionSet = 0;
					}
					
				}
			}*/
		}
		else if(this.followingCustomPath && this.myPath != null) {
			if(this.verboseMovement) {
				System.out.println(this.myAgent.getName()+" : " +"Following my own path : " + this.myPath);
			}
			if(this.myPath.size() >0 && (this.nextNodeMyPath.equals(this.myAgent.getCurrentPosition())||this.nextNodeMyPath.equals("")||this.myOwnPathStart) ){
				this.myOwnPathStart = false;
				String nextNode = this.myPath.get(0);
				this.nextNodeMyPath = nextNode;
				this.myPath.remove(0);
				if(!nextNode.equals(this.myAgent.getCurrentPosition()) && nextNode != "") {
					System.out.println(this.myAgent.getName()+" : " +"Following my own path next node : " + this.myPath);
					this.desiredPosition = nextNode;
					((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
				}else {
	
					System.out.println(this.myAgent.getName()+" : " +"Following my own path failed : next node : " + this.myPath);
				}
			}else if(!this.nextNodeMyPath.equals(this.myAgent.getCurrentPosition())) {
				String nextNode = this.nextNodeMyPath;
				System.out.println(this.myAgent.getName()+" : " +"Following my own path : " + this.myPath);
				System.out.println(this.myAgent.getName()+" : " +"Following my own path next node : " + this.nextNodeMyPath);
				this.desiredPosition = nextNode;
				((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			}else {
				this.followingCustomPath = false;
				if(this.verboseMovement) {
					System.out.println(this.myAgent.getName()+" : End of custom path returning to normal movement");
				}
			}
		}else if(this.followingCustomPath && this.myPath != null) {
			this.followingCustomPath = false;
		}else if(!(this.destination.equals(this.myAgent.getCurrentPosition())&& this.executingOrder)){
			String nextNode=null;
			if((this.destination == "" || (this.destination.equals(this.myAgent.getCurrentPosition()))&& !this.executingOrder) ){
				if(this.verboseMovement) 
					System.out.println(this.myAgent.getName()+" : " +"Searching for new destination");
				this.destination = this.findDestination();
				if(this.destination == "") {
					if(this.verboseMovement) {
						System.out.println(this.myAgent.getName()+" : " +"Nowhere to go");
					}
					return;
				}
			}
			if(this.voisin.contains(this.destination)) {
				if(this.verboseMovement)
					System.out.println(this.myAgent.getName()+" : Movement Neighbour");
				nextNode = this.destination;
			}else {
				int n = 1;
				while (nextNode==null || nextNode.equals(this.myAgent.getCurrentPosition())){
					if(this.verboseMovement)
						System.out.println(this.myAgent.getName()+" : Movement ");
					nextNode=this.myMap.getShortestPath(this.myAgent.getCurrentPosition(), this.destination).get(0);
					if(this.myAgent.getCurrentPosition().equals(nextNode)) {
						nextNode=this.myMap.getShortestPath(this.myAgent.getCurrentPosition(), this.destination).get(n);
						n+=1;
					}
				}
			}
			if(nextNode.equals(this.siloPosition) && !this.destination.equals(this.siloPosition)) {
				String [] forbid = {this.siloPosition};
				this.myPath = this.parcoursLargeur(this.myAgent.getCurrentPosition(),this.destination,forbid);
				this.myOwnPathStart = false;
				if(this.myPath != null) {
					this.followingCustomPath = true;
				}
				nextNode = this.myPath.get(0);
				this.nextNodeMyPath = nextNode;
				this.myPath.remove(0);
				if(!nextNode.equals(this.myAgent.getCurrentPosition()) && nextNode != "") {
					System.out.println(this.myAgent.getName()+" : " +"Following my own path next node : " + this.myPath);
					this.desiredPosition = nextNode;
					((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
				}else {
	
					System.out.println(this.myAgent.getName()+" : " +"Following my own path failed : next node : " + this.myPath);
				}
			}
			if(this.verboseEtatAgent && this.type != "Silo") {
				System.out.println(this.myAgent.getName()+" : " +"Pre Pre deplacemennt recap");
				//System.out.println(this.myAgent.getName()+" : " +"Path : "+this.myMap.getShortestPath(this.myAgent.getCurrentPosition(), this.destination));
				System.out.println(this.myAgent.getName()+" : " +"Agent : "+this.myAgent.getName());
				System.out.println(this.myAgent.getName()+" : " +"Position : " + this.myAgent.getCurrentPosition());
				System.out.println(this.myAgent.getName()+" : " +"Destination : " + this.destination);
				System.out.println(this.myAgent.getName()+" : " +"Node   : " + nextNode);
				System.out.println(this.myAgent.getName()+" : " +"Protocole  : " + this.actualProtocol);
				System.out.println(this.myAgent.getName()+" : " +"Type  : " + this.type);
				System.out.println(this.myAgent.getName()+" : " +"Objective  : " + this.objective);
				System.out.println(this.myAgent.getName()+" : " +"Ouvert");
				System.out.println(this.myAgent.getName()+" : " +this.openNodes);
				System.out.println(this.myAgent.getName()+" : " +this.myAgent.getName()+" : " +"Fermé");
				System.out.println(this.myAgent.getName()+" : " +this.closedNodes);
				System.out.println(this.myAgent.getName()+" : " +this.waitingForResponse);
				for( int j = 0 ; j < this.inCommsWith.size() ; j++) {
					if(this.verboseEtatAgent) {
						System.out.println(this.myAgent.getName()+" : " +"Comms");
						System.out.println(this.myAgent.getName()+" : " +this.inCommsWith.get(j).getKey());
						System.out.println(this.myAgent.getName()+" : " +this.inCommsWith.get(j).getValue());
					}
				}
				if(this.closedNodes.contains(this.destination)) {
					System.out.println(this.myAgent.getName()+" : " +"Going some place i've already been");
				}
			}
			if(!nextNode.equals(this.myAgent.getCurrentPosition()) && nextNode != "") {
				this.desiredPosition = nextNode;
				((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			}			
		}else if(this.objective == "RetourSilo" || this.objective == "WaitingForOrder") {
			if(this.destination == "") {
				if(this.siloPositionOutdated) {
					this.findSilo();
				}else {
					this.destination= this.siloPosition;
				}
			}
			if(this.destination != "") {
				int n = 1;
				if(this.verboseMovement)
					System.out.println(this.myAgent.getName()+" : Movement ");
				String nextNode=this.myMap.getShortestPath(this.myAgent.getCurrentPosition(), this.destination).get(0);
				if(this.myAgent.getCurrentPosition().equals(nextNode)) {
					nextNode=this.myMap.getShortestPath(this.myAgent.getCurrentPosition(), this.destination).get(n);
					n+=1;
				}
			}
		}
	}
	//FIXME REtour silo, une fois arrivé proche du silo (a une distance egale au nombre d'agent mobilisé moins son numero) on attend 4tour *( nombre d'agent mobilisé moins son numero)
	public String findDestExplNotGoingThrough(String[] prohibited) {
		String dest ="";
		for(int i = 0 ; i < this.openNodes.size() ; i ++) {
			List<String> ch = this.myMap.getShortestPath(this.myAgent.getCurrentPosition(), this.openNodes.get(i));
		     
		    HashSet<String> set = new HashSet<>();
		     
		    set.addAll(ch);
		     
		    set.retainAll(Arrays.asList(prohibited));
		    if(set.size()==0) {
		    	return this.openNodes.get(i);
		    }
		     
		}
		return "";
	}
	public String findDestExplNotGoingThrough(String prohibited) {
		String dest ="";
		if(this.verboseDebug) {
			System.out.println(this.myAgent.getName() + " : Finding new destination not going through : "+prohibited);
		}
		for(int i = 0 ; i < this.openNodes.size() ; i ++) {
			if(!this.openNodes.get(i).equals(this.myAgent.getCurrentPosition())) {
				List<String> ch = this.myMap.getShortestPath(this.myAgent.getCurrentPosition(), this.openNodes.get(i));
			    if(!ch.contains(prohibited) &&!ch.equals(this.myAgent.getCurrentPosition())) {
			    	return this.openNodes.get(i);
			    }
			}
		}
		return "";
	}
	public int endingFunc() {
		if(this.type == "Silo") {
			if(this.verboseFSMBehaviour)
				System.out.println(this.myAgent.getName()+" : " +"Swapping to Silo");
			return 4;
		}
		/*
		 * Transition :
		 * Retour onEnd : 
		 * 0 : pas de changement
		 * 1 : passage mapExchange
		 * 2 : passage mode explo
		 * 3 : passage mode collect
		 * 4 : passage mode silo
		 * 5 : passage mode retour silo
		 * 66 : passage mode execute order
		 * 7 : passage mode cherche silo
		 */
		if(this.lookingForSilo) {
			if(this.verboseFSMBehaviour)
				System.out.println(this.myAgent.getName()+" : " +"Swapping to Cherche silo");
			return 7;
		}else if(this.switchToMsgSending) {
			if(this.verboseFSMBehaviour)
				System.out.println(this.myAgent.getName()+" : " +"Swapping to MapExchange");
			return 1;
		}else if((this.type == "Explo" || (this.type == "Collect" && this.getNbTreasure() == 0)) && this.objective == "explore") {
			if(this.verboseFSMBehaviour)
				System.out.println(this.myAgent.getName()+" : " +"Swapping to Exploration");
			return 2;
		}/*else if(this.type == "Collect" && this.getNbTreasure() != 0 && this.objective != "vidage" && this.objective != "RetourSilo") {
			if(this.verboseFSMBehaviour)
				System.out.println(this.myAgent.getName()+" : " +"Swapping to Collect");
			return 3;
		
		}*/else if(this.objective == "vidage" ||this.objective == "RetourSilo"||this.objective == "WaitingForOrder") {
			if(this.verboseFSMBehaviour)
				System.out.println(this.myAgent.getName()+" : " +"Swapping to Retour Silo");
			return 5;
		}else if(this.objective == "ExecuteOrder" ||this.objective == "Unlock" ||this.objective == "Empty") {
			if(this.verboseFSMBehaviour)
				System.out.println(this.myAgent.getName()+" : " +"Swapping to Excution Ordre");
			return 6;
		}else {
			if(this.verboseFSMBehaviour)
				System.out.println(this.myAgent.getName()+" : " +"Staying same behaviour ");
			return 0;
		}
	}
	
	//Silo
	public void siloSendPosition() {
		if(this.myAgent.getName().contains("Tanker")) {
			if(this.objective.equals("Repositioning")) {
				this.sendMessage("HERE "+this.destination, false, "");
			}else {
				this.sendMessage("HERE "+this.myAgent.getCurrentPosition(), false, "");
			}
		}else {
			if(this.siloPosition != "") {
				this.sendMessage("SILO MAYBE "+this.siloPosition, false,"");
			}
		}
	}
	public void findSilo() {
		if(this.verboseCollect)
			System.out.println(this.myAgent.getName()+" : " +"Finding Silo func launched");
		this.lookingForSilo = true;
		this.secondObjective = "explore";
	}
	public void askForSilo() {
		if(this.verboseSilo)
			System.out.println(this.myAgent.getName()+" : " +"SILO ?");
		this.sendMessage("SILO ?",false,"");
	}
	public int determinePositionScore(String noeud) {
		this.cleanEdges();
		this.cleanOpenNodes();
		this.cleanClosedNodes();
		//renvoi un score en fonction de l'arité du noeud et de la centralité du noeud par rapport au trésor
		int score = 0;
		if(this.getNeighbour(noeud).size()>=3) {
			System.out.println(this.getNeighbour(noeud));
			score+=10;
		}
		int distMax = -1;
		int distMin = -1;
		for (int i = 0 ; i < this.getNbTreasure() ;i ++) {
			int dist = this.myMap.getShortestPath(noeud,  this.getTreasure(i).getPosition()).size();
			if(dist < distMin || distMin == -1) {
				distMin = dist;
			}
			if(dist > distMax || distMax == -1) {
				distMax = dist;
			}
		}
		score += 10-(distMax-distMin);
		this.observation();
		if(this.treasureNear) {
			score -= 10;
		}
		return score;
		
	}
	public ArrayList<String> parcoursLargeur(String pos,String dest, String[] forbid){
		this.nextNodeMyPath="";
		if(this.verboseAStar) {
			System.out.println(this.myAgent.getName()+" : PLargeur PCC : "+pos+"/"+dest+"/"+this.edge);
		}
		if(Arrays.asList(forbid).contains(dest)) {
			return null;
		}
		ArrayList<String> open = new ArrayList<String>();
		open.add(pos);
		ArrayList<String> marked = new ArrayList<String>();
		Map<String, String> cameFrom = new HashMap<String, String>();
		cameFrom.put(pos,pos);
		String noeudActu = "";
		while(!noeudActu.equals(dest) &&!open.isEmpty()) {

			if(this.verboseAStar) {
				System.out.println(this.myAgent.getName()+" : PLargeur explo : "+noeudActu+open);
			}
			noeudActu = open.get(0);
			open.remove(noeudActu);
			ArrayList<String> voisin = this.getNeighbour(noeudActu);
			if(this.verboseAStar) {
				System.out.println(this.myAgent.getName()+" : PLargeur Voisin : "+noeudActu+" : "+voisin);
			}
			for(int i = 0 ; i < voisin.size();i++) {
				if(!Arrays.asList(forbid).contains(voisin.get(i))){
					if(!marked.contains(voisin.get(i))) {
						open.add(voisin.get(i));
						marked.add(voisin.get(i));
					}
					String oldPath = "";
					String newPath = cameFrom.get(noeudActu)+"/"+voisin.get(i);
					if(cameFrom.containsKey(voisin.get(i))) {
						oldPath = cameFrom.get(voisin.get(i));
						if(newPath.split("/").length < oldPath.split("/").length) {
							cameFrom.put(voisin.get(i),newPath);
						}
					}else {
						oldPath = "";
						cameFrom.put(voisin.get(i),newPath);
					}
					System.out.println(this.myAgent.getName()+" : PLargeur old path : "+oldPath);
					System.out.println(this.myAgent.getName()+" : PLargeur new path : "+newPath);
				}
				System.out.println(this.myAgent.getName()+" : PLargeur redoing path : "+cameFrom);
			}
		}
		System.out.println(this.myAgent.getName()+" : PLargeur redoing path : "+cameFrom);
		if(!noeudActu.equals(dest) || !cameFrom.containsKey(dest))
			return null;
		
		//Retrouver le chemin vers l'objectif
		ArrayList<String> chemin = new ArrayList<String>();
		chemin =  new ArrayList<String>(Arrays.asList(cameFrom.get(dest).split("/")));
		if(chemin.get(0).equals(pos))
			chemin.remove(0);
		if(this.verboseAStar) {
			System.out.println("\n"+this.myAgent.getName()+" : PLargeur PCC : "+pos+"/"+dest+"/"+"\n");
			System.out.println(this.myAgent.getName()+" : PLargeur path : "+chemin);
		}
		this.myOwnPathStart = true;
		return chemin;
		
	}
	public static String[] removeTheElement(String[] arr, int index) 
	{ 
		
		// If the array is empty 
		// or the index is not in array range 
		// return the original array 
		if (arr == null|| index < 0|| index >= arr.length) { 
			return arr; 
		} 
		
		// Create another array of size one less 
		String[] anotherArray = new String[arr.length - 1]; 
		
		// Copy the elements except the index 
		// from original array to the other array 
		for (int i = 0, k = 0; i < arr.length; i++) { 
		
			// if the index is 
			// the removal element index 
			if (i == index) { 
				continue; 
			} 
			
			// if the index is not 
			// the removal element index 
			anotherArray[k++] = arr[i]; 
		} 
		
		// return the resultant array 
		return anotherArray; 
	} 
	public void clearCommsWith(AID ag) {
		for( int j = 0 ; j < this.inCommsWith.size() ; j++) {
			System.out.println(this.myAgent.getName()+" : " +"AID ");System.out.println(this.myAgent.getName()+" : " +this.inCommsWith.get(j).getKey());
			if(this.inCommsWith.get(j).getKey().equals(ag.getName())) {
				System.out.println(this.myAgent.getName()+" : " +"AID removed");
				System.out.println(this.myAgent.getName()+" : " +this.inCommsWith.get(j).getKey());
				this.inCommsWith.remove(j);
			}
		}
	}
}
