package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.FSMAgentData;
import jade.core.behaviours.OneShotBehaviour;

public class ExecuteOrderBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	
	
	private boolean verbose;
	//Compteur de tour passé
	public FSMAgentData data;

	public ExecuteOrderBehaviour(final AbstractDedaleAgent myagent, FSMAgentData data) {
		super(myagent);
		this.verbose = false;
		this.data = data;
		
	}

	@Override
	public void action() {
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		System.out.println(this.data.destination);
		System.out.println(this.data.myAgent.getCurrentPosition());
		if(this.data.executingOrder && this.data.myAgent.getCurrentPosition().equals(this.data.destination)) {
			this.data.arrivedAtOrderDestination();
		}
		else if(this.data.executingOrder && this.data.order_agentnumber+1> this.data.myMap.getShortestPath(this.data.myAgent.getCurrentPosition(), this.data.destination).size()) {
			this.data.arrivedAtOrderDestination();
		}
		if (myPosition!=null){
			this.data.observation();
			//if(this.data.getNeighbour(myPosition).length <3 || this.data.cptTour) {
				this.data.movement();
			//}
			
		}
		done();
	}
	public int onEnd() {
		return this.data.endingFunc();
	}

}
