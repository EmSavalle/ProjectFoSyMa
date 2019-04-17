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

	@Override
	public void action() {
		// TODO Auto-generated method stub
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null){
			this.data.observation();
			if(this.data.siloPositionOutdated || this.data.siloPosition == ""|| this.data.myMap.getShortestPath(myPosition, this.data.siloPosition).size() < 3 || this.data.siloPosition == "") {
				this.data.siloPositionOutdated = true;
				this.data.lookingForSilo = true;
			}else {
				this.data.destination=this.data.siloPosition;
				this.data.movement();
			}
		}
		done();
	}

}
