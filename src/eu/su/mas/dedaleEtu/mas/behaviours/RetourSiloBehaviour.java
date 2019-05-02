package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.FSMAgentData;
import jade.core.behaviours.OneShotBehaviour;

public class RetourSiloBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	
	
	private boolean verbose;
	//Compteur de tour passé
	public FSMAgentData data;

	public RetourSiloBehaviour(final AbstractDedaleAgent myagent, FSMAgentData data) {
		super(myagent);
		this.verbose = false;
		this.data = data;
		
	}
	//TODO le robot s'arrète a 2 cases du tanker au lieu d'une seule
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
			}else {
				if(this.verbose) {
					System.out.println(this.myAgent.getName()+ ": RetourSiloBehaviour Shortest path "+this.data.siloPosition);
				}
				if(this.data.myMap.getShortestPath(myPosition, this.data.siloPosition).size() <= 2) {
					
					if(this.data.siloAID != null) {
						if(this.data.messageForSilo != "") 
							this.data.sendMessage(this.data.messageForSilo, this.data.siloAID);
						this.data.sendMessage("SILO ?", this.data.siloAID);
					}else {
						if(this.data.messageForSilo != "") 
							this.data.sendMessage(this.data.messageForSilo, false,"");
						this.data.sendMessage("SILO ?",false,"");
					}
					//TODO Se mettre en attente
				}else {
					//TODO gerer que faire si on est loin du silo et qu'on ne sais pas ou il est
				}
				this.data.destination=this.data.siloPosition;
				this.data.movement();
			}
		}
		done();
	}
	public int onEnd() {
		return this.data.endingFunc();
	}

}
