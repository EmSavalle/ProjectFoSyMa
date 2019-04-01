package eu.su.mas.dedaleEtu.mas.knowledge;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import dataStructures.tuple.*;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.lang.acl.ACLMessage;

public class FSMAgentData {

	public MapRepresentation myMap= new MapRepresentation();;
	public int cpt;
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
	public boolean finished;
	public boolean waitingForResponse;
	public int cptTour=0;
	public int cptAttenteRetour=0;
	public String destination;
	public int myBackpackSize;
	public String type; //Silo / Explorer / Collector 
	public String siloPosition;
	public int initBackPackSize;
	private AbstractDedaleAgent myAgent;
	
	public FSMAgentData(String type,int backpack, AbstractDedaleAgent ag){
		this.siloPosition = "";
		this.type = type;
		this.initBackPackSize = 0;
		this.myBackpackSize=backpack;
		this.destination = "";
		this.myMap= new MapRepresentation();
		this.cpt=0;
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
	}
	public int getBackPackSize() {
		return this.myBackpackSize;
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
		this.cpt = c;
	}
	public int getCpt() {
		return cpt;
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
	//TODO ajouter fonction envoi données tresor et silo
}
