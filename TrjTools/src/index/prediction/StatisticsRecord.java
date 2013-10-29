package index.prediction;

import prediction.DateType;

public class StatisticsRecord {
	private Long edgeId;
	private DateType dateType;
	private int dailytime_index;
	private double avrTraficFlow;
	private double avrEmptyRatio;
	private double avrTraficSpeed;
	
	public StatisticsRecord(Long edgeId,DateType dateType,int index,double traficFlow,double emptyRatio,double traficSpeed){
		this.edgeId = edgeId;
		this.dateType = dateType;
		this.dailytime_index = index;
		this.avrTraficFlow = traficFlow;
		this.avrEmptyRatio = emptyRatio;
		this.avrTraficSpeed = traficSpeed;
	}

	public Long getEdgeId() {
		return edgeId;
	}

	public void setEdgeId(Long edgeId) {
		this.edgeId = edgeId;
	}

	public DateType getDateType() {
		return dateType;
	}

	public void setDateType(DateType dateType) {
		this.dateType = dateType;
	}

	public int getDailytime_index() {
		return dailytime_index;
	}

	public void setDailytime_index(int dailytime_index) {
		this.dailytime_index = dailytime_index;
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
	
	
}
