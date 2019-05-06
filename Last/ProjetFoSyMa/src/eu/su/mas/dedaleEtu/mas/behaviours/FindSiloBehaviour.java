package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.Random;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.FSMAgentData;
import jade.core.behaviours.OneShotBehaviour;

public class FindSiloBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	
	
	private boolean verbose;
	//Compteur de tour passé
	public FSMAgentData data;

	public FindSiloBehaviour(final AbstractDedaleAgent myagent, FSMAgentData data) {
		super(myagent);
		this.verbose = false;
		this.data = data;
		
	}

	@Override
	public void action() {
		this.data.getMessage();
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		//TODO Le robot ne se deplace plus en cherchant le silo
		if (myPosition!=null){
			this.data.observation();
			if(this.data.lookingForSilo) {
				
				if(this.data.destination == "" && this.data.siloPositionOutdated != false) {
					System.out.println(this.data.myAgent.getName()+" : Bla--------------------------------------------------------------------\n");
					System.out.println(this.data.myAgent.getName()+" : Position silo : " +this.data.siloPosition);
					this.data.destination = this.data.siloPosition;
				}
				this.data.askForSilo();
			}

			this.data.movement();
			
			
		}
		done();
	}
	public int onEnd() {
		return this.data.endingFunc();
	}

}
