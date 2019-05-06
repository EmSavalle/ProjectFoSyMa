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

public class OrderList {
	public ArrayList<Order> list_ordre;
	public boolean verboseOrder = true;
	public OrderList() {
		this.list_ordre = new ArrayList<Order>();
	}
	public void initialiseOrder(ArrayList<Treasure> lT) {
		//Verifie que la liste des ordres contient tous les tr�sor
		//Si non, ajoute les tr�sors a des ordres
		for (int i = 0 ; i < lT.size() ; i ++) {
			boolean init = false;
			for (int j = 0 ; j < this.list_ordre.size() ; j++) {
				if(this.list_ordre.get(j).treasure.getPosition().equals(lT.get(i).getPosition())){
					init = true;
					break;
				}
			}
			if(!init) {
				this.list_ordre.add(new Order(lT.get(i)));
			}
		}
	}
	public void updateOrderIsOpen(String pos) {
		for(int i = 0 ; i < this.list_ordre.size() ; i++) {
			if(this.list_ordre.get(i).treasure.getPosition().equals(pos)) {
				this.list_ordre.get(i).setOpened();
				break;
			}
		}
	}
	public void updateOrderValueTaken(String pos,int value) {
		for(int i = 0 ; i < this.list_ordre.size() ; i++) {
			if(this.list_ordre.get(i).treasure.getPosition().equals(pos)) {
				this.list_ordre.get(i).treasure.reduceValue(value);
				break;
			}
		}
	}
	public void updateOrderList(ArrayList<Treasure> treasure) {
		if(treasure.size()==this.list_ordre.size())
			return;
		for (int i = 0; i < treasure.size() ; i++) {
			boolean set = false;
			for (int j = 0 ; j < this.list_ordre.size() ; j++) {
				if(treasure.get(i).getPosition().equals(this.list_ordre.get(j).treasure.getPosition())) {
					set = true;
				}
			}
			if(set == false) {
				this.list_ordre.add(new Order(treasure.get(i)));
			}
		}
	}
	public String agentGetAffectationOrder(AID agentToAffect, String type, int lStr, int sStr , int gSize, int dSize) {
		int iOrder = -1;
		if(this.verboseOrder) {
			System.out.println("Determine l'ordre d'affectation");
			System.out.println("Nombre ordre dispo : "+Integer.toString(this.list_ordre.size()));
		}
		if(this.verboseOrder && this.list_ordre.size() == 0)
			System.out.println("Determine l'ordre d'affectation : pas d'ordre disponible pour affectation");
		if(type.equals("Explo")) {
			boolean isOrderStarted = false;
			for(int i = 0 ; i < this.list_ordre.size() ; i++) {
				if(this.verboseOrder) {
					System.out.println("Trésor analysé : "+this.list_ordre.get(i).treasure.toString());
					System.out.println("affectation : Ouvert ? "+Boolean.toString(this.list_ordre.get(i).isOpen()));
					System.out.println("affectation : Ouvert ? "+Boolean.toString(this.list_ordre.get(i).treasure.isOpen));
					System.out.println("affectation : loStr needed ? "+Integer.toString(this.list_ordre.get(i).treasure.getLockStrength() - this.list_ordre.get(i).lockStrAllocate));
					System.out.println("affectation : Str needed ? "+Integer.toString(this.list_ordre.get(i).treasure.getStrength() - this.list_ordre.get(i).strAllocate));
					System.out.println("affectation : Usefull ? "+Boolean.toString(this.list_ordre.get(i).isHeUsefull(lStr, sStr)));
				}
				if(!this.list_ordre.get(i).isOpen() && this.list_ordre.get(i).isHeUsefull(lStr, sStr)) {
					if(this.list_ordre.get(i).openingStarted) {
						iOrder = i;
						isOrderStarted = true;
						break;
					}else {
						iOrder = i;
					}
				}
			}
			if(iOrder == -1) {
				if(this.verboseOrder)
					System.out.println("Pas d'ordre d'affectation trouvé");
				return "";
			}
			Couple<Boolean,String> c = this.list_ordre.get(iOrder).sendAgentOpening(agentToAffect, lStr, sStr);
			if(this.verboseOrder)
				System.out.println("#########################\n AFFECTATION TROUVE : "+c.getRight());
			return c.getRight();
		}else{
			//TODO verifier s'il est pr�f�rable d'envoyer un agent collect collecter ou ouvrir 
			boolean isOrderStarted = false;
			for(int i = 0 ; i < this.list_ordre.size() ; i++) {
				if(!this.list_ordre.get(i).isHeUsefull(lStr, sStr, gSize, dSize)) {
					if(this.list_ordre.get(i).openingStarted ||this.list_ordre.get(i).emptyingStarted ) {
						iOrder = i;
						isOrderStarted = true;
						break;
					}else {
						iOrder = i;
					}
				}
			}
			if(iOrder == -1) {
				return "";
			}
			Couple<Boolean,String> c;
			if(this.list_ordre.get(iOrder).isOpen()) {
				 c= this.list_ordre.get(iOrder).sendAgentEmptying(agentToAffect, lStr, sStr);
			}else {
				c = this.list_ordre.get(iOrder).sendAgentOpening(agentToAffect, lStr, sStr);
			}
			
			return c.getRight();
		}
	}
	/* TODO 
	public void updateOrder();
	public void setTreasureOpen(String pos);
	public void setTreasureEmpty(String pos);*/
}
