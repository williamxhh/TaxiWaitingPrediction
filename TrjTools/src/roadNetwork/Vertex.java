package roadNetwork;

import java.util.ArrayList;
import java.util.List;

public class Vertex {
	private long id;
	private GeoPoint point;
	private final Object syncRoot = new Object();

	private List<Edge> adjacentEdges = new ArrayList<Edge>();

	private List<Edge> outEdges = null;
	private List<Edge> inEdges = null;

	public long getId() {
		return id;
	}

	public double getLat() {
		return this.point.getLat();
	}

	public double getLng() {
		return this.point.getLng();
	}

	public List<Edge> getOutEdges() {
		if (outEdges == null) {
			calculateInOut();
		}
		return outEdges;
	}

	public List<Edge> getInEdges() {
		if (inEdges == null) {
			calculateInOut();
		}
		return inEdges;
	}

	public Vertex(long id, double lat, double lng) {
		this.id = id;
		this.point = new GeoPoint(lat, lng);
	}

	private void calculateInOut() {
		synchronized (syncRoot) {
			if (outEdges == null) {
				int edgeSize = adjacentEdges.size();
				outEdges = new ArrayList<Edge>();
				inEdges = new ArrayList<Edge>();
				for (int i = 0; i < edgeSize; i++) {
					if (adjacentEdges.get(i).getStart() == this) {
						outEdges.add(adjacentEdges.get(i));
					} else {
						inEdges.add(adjacentEdges.get(i));
					}
				}
			}
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Vertex) {
			return ((Vertex) obj).getId() == this.id;
		}
		return false;
	}

	@Override
	public int hashCode() {
		long id = getId();
		return ((int) id) ^ (int) (id >> 32);
	}

	@Override
	public String toString() {
		return "Vertex:" + id + ":(" + point.getLng() + "," + point.getLat()
				+ ")";
	}

	public void registerEdge(Edge e) {
		if (e.getStart() == this || e.getEnd() == this) {
			synchronized (syncRoot) {
				this.adjacentEdges.add(e);
			}
		}
	}
	
	public GeoPoint toPoint(){
		return this.point;
	}
}
