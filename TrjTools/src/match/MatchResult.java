package match;

import roadNetwork.Edge;
import roadNetwork.GeoPoint;

public class MatchResult implements Comparable<MatchResult>{
	private Edge edge;
	private double probability;
	private long segId;
	private GeoPoint segStartPoint;
	private GeoPoint segEndPoint;
	
	public Edge getEdge() {
		return edge;
	}
	public void setEdge(Edge edge) {
		this.edge = edge;
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
	
	public MatchResult(Edge edge,long segId,GeoPoint segStartPoint,GeoPoint segEndPoint){
		this.edge = edge;
		this.segId = segId;
		this.segStartPoint = segStartPoint;
		this.segEndPoint = segEndPoint;
	}
	@Override
	public String toString() {
		return "<EdgeId:"+this.edge.getId()+";  SegId:"+this.segId+";  probability:"+this.probability+">  "+this.segStartPoint+"-->"+this.segEndPoint+"\r\n";
	}
	
	
}
