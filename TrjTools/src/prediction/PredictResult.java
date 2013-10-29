package prediction;

public class PredictResult {
	public double probability;
	public int estimatedWaitingTime;
	
	public PredictResult(double probability,int waitingTime){
		this.probability = probability;
		this.estimatedWaitingTime = waitingTime;
	}
	
	@Override
	public String toString() {
		return "probability:"+probability+"\testimatedWaitingTime:"+estimatedWaitingTime;
	}
	
	
}
