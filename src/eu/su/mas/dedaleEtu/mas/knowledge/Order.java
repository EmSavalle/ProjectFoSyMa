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
	public int lockStrNeeded;
	public int strNeeded;
	public int valueToPick;
	public boolean openingStarted;
	public boolean emptyingStarted;
	
	public Order(Treasure t) {
		this.agentDeploye = new ArrayList<AID>();
		this.treasure = t;
		this.lockStrNeeded = t.lockStr;
		this.strNeeded = t.str;
		this.valueToPick = t.value;
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
		this.lockStrNeeded = Math.min(this.lockStrNeeded - lockStr,0);
		this.strNeeded = Math.min(this.strNeeded - str,0);
		if(this.lockStrNeeded == 0 && this.strNeeded == 0) {
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
		if(this.lockStrNeeded == 0 && this.strNeeded == 0) {
			return false;
		}
		return true;
	}
	public Couple<Integer,Integer> whatIsNeeded(){
		return new Couple<Integer,Integer>(this.lockStrNeeded,this.strNeeded);
	}
	public String orderMessage(String typeOrder,boolean end) {
		//ORDER UNLOCK pos need_other alone lstr str
		String order = "";
		if(typeOrder.equals("Open")) {
			order  = "ORDER UNLOCK "+this.treasure.position+" ";
			order+= Integer.toString(this.agentDeploye.size()-1)+" ";
			if(this.lockStrNeeded == 0 && this.strNeeded == 0) {
				order+= "0 ";
			}else {
				order+= "1 ";
			}
			order+=Integer.toString(this.lockStrNeeded)+" "+Integer.toString(this.strNeeded)+" "+ Boolean.toString(end);
		}else if(typeOrder.equals("Empty")) {
			order  = "ORDER EMPTY "+this.treasure.position +" "+ Boolean.toString(end);
		}
		return order;
	}
	public boolean isHeUsefull(int lStr, int sStr) {
		if(lStr > 0 && this.lockStrNeeded > 0)
			return true;
		if(sStr > 0 && this.strNeeded > 0)
			return true;
		return false;
	}
	public boolean isHeUsefull(int lStr, int sStr,int gSize, int dSize) {
		if(this.isOpen() && this.treasure.getValue()>0) {
			if(this.treasure.getType() == "Gold" && gSize != 0)
				return true;
			if(this.treasure.getType() == "Diamond" && dSize != 0)
				return true;
		}
		if(lStr > 0 && this.lockStrNeeded > 0)
			return true;
		if(sStr > 0 && this.strNeeded > 0)
			return true;
		return false;
	}
}
