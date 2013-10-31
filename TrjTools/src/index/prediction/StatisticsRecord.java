package index.prediction;

public class StatisticsRecord {
	private double avrTraficFlow;
	private double avrEmptyRatio;
	private double avrTraficSpeed;
	
	public StatisticsRecord(double traficFlow,double emptyRatio,double traficSpeed){
		this.avrTraficFlow = traficFlow;
		this.avrEmptyRatio = emptyRatio;
		this.avrTraficSpeed = traficSpeed;
	}

	public double getAvrTraficFlow() {
		return avrTraficFlow;
	}

	public void setAvrTraficFlow(double avrTraficFlow) {
		this.avrTraficFlow = avrTraficFlow;
	}

	public double getAvrEmptyRatio() {
		return avrEmptyRatio;
	}

	public void setAvrEmptyRatio(double avrEmptyRatio) {
		this.avrEmptyRatio = avrEmptyRatio;
	}

	public double getAvrTraficSpeed() {
		return avrTraficSpeed;
	}

	public void setAvrTraficSpeed(double avrTraficSpeed) {
		this.avrTraficSpeed = avrTraficSpeed;
	}
	
	public void add(StatisticsRecord r){
		this.avrTraficFlow = this.avrTraficFlow+r.avrTraficFlow;
		this.avrEmptyRatio = this.avrEmptyRatio+r.avrEmptyRatio;
		this.avrTraficSpeed = this.avrTraficSpeed+r.avrTraficSpeed;
	}
	
	public void avrWeekday(){
		this.avrEmptyRatio = this.avrEmptyRatio/5;
		this.avrTraficFlow = this.avrTraficFlow/5;
		this.avrTraficSpeed = this.avrTraficSpeed/5;
	}
	
	public void avrWeekends(){
		this.avrEmptyRatio = this.avrEmptyRatio/2;
		this.avrTraficFlow = this.avrTraficFlow/2;
		this.avrTraficSpeed = this.avrTraficSpeed/2;
	}
	
	public boolean isEmptyRecord(){
		return this.avrEmptyRatio==0;
	}
}
