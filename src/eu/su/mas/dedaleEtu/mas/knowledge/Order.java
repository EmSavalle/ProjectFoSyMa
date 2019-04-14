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
	public String position;
	public int totalLockStr;
	public int totalStr;
	public int lockStrNeeded;
	public int strNeeded;
	public int value;
	public String type;
	public boolean opened;
	public boolean emptied;
	
	public Order(String p,String t, int v,int initLockStr,int initStr) {
		this.agentDeploye = new ArrayList<AID>();
		this.position = p;
		this.type = t;
		this.value = v;
		this.totalLockStr = initLockStr;
		this.lockStrNeeded = initLockStr;
		this.totalStr = initStr;
		this.strNeeded = initStr;
		this.opened = false;
		this.emptied = false;
	}
	public void setOpened() {
		this.opened = true;
	}
	public void setEmptied() {
		this.emptied = true;
	}
	public boolean isOpen() {
		return this.opened;
	}
	public boolean isEmpty() {
		return this.emptied;
	}
	public int sizeTreasure() {
		return this.value;
	}
	public Couple<Boolean,String> sendAgentOpening(AID agent,int lockStr,int str) {
		this.agentDeploye.add(agent);
		this.lockStrNeeded = Math.min(this.lockStrNeeded - lockStr,0);
		this.strNeeded = Math.min(this.strNeeded - str,0);
		if(this.lockStrNeeded == 0 && this.strNeeded == 0) {
			return new Couple<Boolean,String>(true,this.orderMessage("Open"));
		}
		return new Couple<Boolean,String>(false,this.orderMessage("Open"));	
	}
	
	public boolean needAgentOpen() {
		if(this.lockStrNeeded == 0 && this.strNeeded == 0) {
			return false;
		}
		return true;
	}
	public Couple<Integer,Integer> whatIsNeeded(){
		return new Couple<Integer,Integer>(this.lockStrNeeded,this.strNeeded);
	}
	public String orderMessage(String typeOrder) {
		//ORDER UNLOCK pos need_other alone lstr str
		String order = "";
		if(typeOrder.equals("Open")) {
			order  = "ORDER UNLOCK "+this.position+" ";
			if(this.lockStrNeeded == 0 && this.strNeeded == 0) {
				order+= "0 ";
			}else {
				order+= "1 ";
			}
			if(this.agentDeploye.size() >1) {
				order+= "0 ";
			}else {
				order+= "1 ";
			}
			order+=Integer.toString(this.lockStrNeeded)+" "+Integer.toString(this.strNeeded);
		}else if(typeOrder.equals("Empty")) {
			order  = "ORDER EMPTY "+this.position;
		}
		return order;
	}
}
