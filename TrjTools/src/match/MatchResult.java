package match;

import roadNetwork.Edge;
import roadNetwork.GeoPoint;

public class MatchResult implements Comparable<MatchResult>{
	private Edge edge;
//	private double probability;
	private double distance;
	private long segId;
	
	public Edge getEdge() {
		return edge;
	}
	public void setEdge(Edge edge) {
		this.edge = edge;
	}
//	public double getProbability() {
//		return probability;
//	}
//	public void setProbability(double probability) {
//		this.probability = probability;
//	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public long getSegId() {
		return segId;
	}
	public void setSegId(long segId) {
		this.segId = segId;
	}
	@Override
	public int compareTo(MatchResult o) {
//		if(this.probability>o.getProbability()){
//			return 1;
//		}else if(this.probability<o.getProbability()){
//			return -1;
//		}
		if(this.distance<o.getDistance()){
			return 1;
		}else if(this.distance>o.getDistance()){
			return -1;
		}
		return 0;
	}
	
	public MatchResult(Edge edge,long segId,double distance){
		this.edge = edge;
		this.segId = segId;
		this.distance = distance;
	}
	@Override
	public String toString() {
		return this.edge.getId()+","+this.segId+"#";
	}
	
	
}
