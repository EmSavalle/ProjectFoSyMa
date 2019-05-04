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

public class Order {
	public ArrayList<AID> agentDeploye;
	public Treasure treasure;
	public int lockStrAllocate;
	public int strAllocate;
	public int valueToPick;
	public boolean openingStarted;
	public boolean emptyingStarted;
	public boolean verboseOrder = true;
	public boolean orderComplete;
	public Order(Treasure t) {
		this.agentDeploye = new ArrayList<AID>();
		this.treasure = t;
		this.lockStrAllocate = 0;
		this.strAllocate = 0;
		this.valueToPick = t.getValue();
		this.orderComplete = false;
		if(this.verboseOrder) {
			System.out.println("###########################################\nNew Order : "+t.toString());
		}
	}
	public void orderDone(String type) {
		if(type.equals("UNLOCK")) {
			this.treasure.setOpen();
			this.agentDeploye.clear();
			this.openingStarted = false;
		}
	}
	public void orderDone(String type, int val, AID ai) {
		if(type.equals("EMPTY")) {
			this.treasure.reduceValue(val);
			if(this.treasure.value <=0) {
				this.orderComplete = true;
				this.agentDeploye.clear();
			}else {
				this.agentDeploye.remove(ai);
			}
			
		}
		
	}
	public void setOpened() {
		this.treasure.setOpen();
	}
	public void setEmptied() {
		this.treasure.reduceValue(-1);
	}
	public boolean isOpen() {
		return this.treasure.getOpen();
	}
	public boolean isEmpty() {
		return this.treasure.getEmpty();
	}
	public int sizeTreasure() {
		return this.treasure.getValue();
	}
	public Couple<Boolean,String> sendAgentOpening(AID agent,int lockStr,int str) {
		this.openingStarted = true;
		this.agentDeploye.add(agent);
		this.lockStrAllocate += lockStr;
		this.strAllocate +=  str;
		if(this.lockStrAllocate >= this.treasure.lockStr && this.strAllocate >=this.treasure.str) {
			return new Couple<Boolean,String>(true,this.orderMessage("Open",true));
		}
		return new Couple<Boolean,String>(false,this.orderMessage("Open",false));	
	}
	public Couple<Boolean,String> sendAgentEmptying(AID agent,int gSize,int dSize) {
		this.emptyingStarted = true;
		this.agentDeploye.add(agent);
		if(this.treasure.type == "Gold") {
			this.valueToPick = Math.min(0, this.valueToPick-gSize);
		}else if(this.treasure.type == "Diamond") {
			this.valueToPick = Math.min(0, this.valueToPick-dSize);
		}
		
		if(this.valueToPick == 0) {
			return new Couple<Boolean,String>(true,this.orderMessage("Empty",true));
		}
		return new Couple<Boolean,String>(false,this.orderMessage("Empty",false));	
	}
	
	public boolean needAgentForOpening() {
		if(this.lockStrAllocate >= this.treasure.lockStr && this.strAllocate >=this.treasure.str) {
			return false;
		}
		return true;
	}
	public Couple<Integer,Integer> whatIsNeeded(){
		return new Couple<Integer,Integer>(Math.min(0,this.treasure.lockStr-this.lockStrAllocate),Math.min(0,this.treasure.str-this.strAllocate));
	}
	public String orderMessage(String typeOrder,boolean end) {
		//ORDER UNLOCK pos need_other alone lstr str
		String order = "";
		if(typeOrder.equals("Open")) {
			order  = "ORDER UNLOCK "+this.treasure.position+" ";
			order+= Integer.toString(this.agentDeploye.size()-1)+" ";
			if(Math.min(0,this.treasure.lockStr-this.lockStrAllocate) == 0 && Math.min(0,this.treasure.str-this.strAllocate) == 0) {
				order+= "0 ";
			}else {
				order+= "1 ";
			}
			order+=Integer.toString(Math.min(0,this.treasure.lockStr-this.lockStrAllocate))+" "+Integer.toString(Math.min(0,this.treasure.str-this.strAllocate))+" "+ Boolean.toString(end);
			order+=" "+this.treasure.getType();
		}else if(typeOrder.equals("Empty")) {
			order  = "ORDER EMPTY "+this.treasure.position +" "+ Boolean.toString(end);
		}
		return order;
	}
	public boolean isHeUsefull(int lStr, int sStr) {
		if(!this.treasure.getOpen()) {
			if(lStr > 0 && this.lockStrAllocate < this.treasure.lockStr)
				return true;
			if(sStr > 0 &&  this.strAllocate < this.treasure.lockStr)
				return true;
		}
		return false;
		
	}
	public boolean isHeUsefull(int lStr, int sStr,int gSize, int dSize) {
		if(this.isOpen() && this.treasure.getValue()>0) {
			if(this.treasure.getType() == "Gold" && gSize != 0)
				return true;
			if(this.treasure.getType() == "Diamond" && dSize != 0)
				return true;
		}
		if(lStr > 0 && this.lockStrAllocate < this.treasure.lockStr)
			return true;
		if(sStr > 0 &&  this.strAllocate < this.treasure.lockStr)
			return true;
		return false;
	}
}
