package match;

import roadNetwork.GeoPoint;

public class MatchResult implements Comparable<MatchResult>{
	private long roadId;
	private double probability;
	private long segId;
	private GeoPoint segStartPoint;
	private GeoPoint segEndPoint;
	
	public long getRoadId() {
		return roadId;
	}
	public void setRoadId(long roadId) {
		this.roadId = roadId;
	}
	public double getProbability() {
		return probability;
	}
	public void setProbability(double probability) {
		this.probability = probability;
	}
	public long getSegId() {
		return segId;
	}
	public void setSegId(long segId) {
		this.segId = segId;
	}
	public GeoPoint getSegStartPoint() {
		return segStartPoint;
	}
	public void setSegStartPoint(GeoPoint segStartPoint) {
		this.segStartPoint = segStartPoint;
	}
	public GeoPoint getSegEndPoint() {
		return segEndPoint;
	}
	public void setSegEndPoint(GeoPoint segEndPoint) {
		this.segEndPoint = segEndPoint;
	}
	@Override
	public int compareTo(MatchResult o) {
		if(this.probability>o.getProbability()){
			return 1;
		}else if(this.probability<o.getProbability()){
			return -1;
		}
		return 0;
	}
	
	public MatchResult(long roadId,double probability){
		this.roadId = roadId;
		this.probability = probability;
	}
	@Override
	public String toString() {
		return "<roadid:"+this.roadId+";probability:"+this.probability+">";
	}
	
	
}
