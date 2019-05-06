package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.FSMAgentData;
import jade.core.behaviours.OneShotBehaviour;

public class RetourSiloBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	
	
	private boolean verbose;
	//Compteur de tour passé
	public FSMAgentData data;
	public int cptWaitingSilo;
	public RetourSiloBehaviour(final AbstractDedaleAgent myagent, FSMAgentData data) {
		super(myagent);
		this.verbose = true;
		this.data = data;
		this.cptWaitingSilo = 0;
	}
	//TODO le robot s'arrète a 2 cases du tanker au lieu d'une seule
	//TODO Retour silo : parfois l'agent veux aller sur la case du silo quand il a éfféctué un ordre a coté de celui ci
	@Override
	public void action() {
		this.data.getMessage();
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null){
			this.data.observation();
			if(this.data.siloPositionOutdated || (this.data.stuckCounter > 2 && this.data.myMap.getShortestPath(myPosition, this.data.siloPosition).size() < 2) || this.data.siloPosition == "") {
				this.data.siloPositionOutdated = true;
				this.data.lookingForSilo = true;
				this.data.stuckCounter = 0;
				this.cptWaitingSilo=0;
			}else if(!this.data.executingOrder){
				if(this.verbose) {
					System.out.println(this.myAgent.getName()+ ": RetourSiloBehaviour Shortest path "+this.data.siloPosition);
				}
				if(this.data.myMap.getShortestPath(myPosition, this.data.siloPosition).size() <= 2  ) {
					System.out.println(this.myAgent.getName()+ ": Near silo for retour");
					
					if(this.cptWaitingSilo == 0) {
						System.out.println(this.myAgent.getName()+ ": Retour Silo sending message for order's end");
						if(this.data.siloAID != null) {
							if(this.data.messageForSilo != "") {
								System.out.println(this.myAgent.getName()+ ": Sending message to silo for ending order");
								this.data.sendMessage(this.data.messageForSilo, this.data.siloAID);
								this.data.messageForSilo="";}
							this.data.sendMessage("SILO ?", this.data.siloAID);
						}else {
							if(this.data.messageForSilo != "") {
								System.out.println(this.myAgent.getName()+ ": Sending message to silo for ending order");
								this.data.sendMessage(this.data.messageForSilo, false,"");
								this.data.messageForSilo="";}
							this.data.sendMessage("SILO ?",false,"");
						}
					}else if(this.cptWaitingSilo > 10) {
						this.data.siloPositionOutdated = true;
						this.data.siloPosition = "";
						this.cptWaitingSilo=0;
					}
					this.cptWaitingSilo+=1;
					//TODO Se mettre en attente
				}else if(this.data.objective!="Unlock" && this.data.objective!="Empty"){
					this.data.destination=this.data.siloPosition;
					this.cptWaitingSilo=0;
				}
				this.data.movement();
			}
		}
		done();
	}
	public int onEnd() {
		return this.data.endingFunc();
	}

}
