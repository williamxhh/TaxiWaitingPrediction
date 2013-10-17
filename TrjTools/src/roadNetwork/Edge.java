package roadNetwork;

import java.util.ArrayList;
import java.util.List;

public class Edge {
	private long id;
	private Vertex start;
	private Vertex end;
	private double length = -1;
	private MBR mbr;
	private String geoString;
	private Polyline geo;

	private final Object syncRoot = new Object();

	public long getId() {
		return id;
	}

	public Vertex getStart() {
		return start;
	}

	public Vertex getEnd() {
		return end;
	}

	public MBR getMbr() {
		return getGeo().getMBR();
	}

	public String getGeoString() {
		return geoString;
	}

	public void setGeoString(String geoString) {
		this.geoString = geoString;
	}

	public Polyline getGeo() {
		if (geo == null)
        {
            synchronized (syncRoot)
            {
                if (geo == null)
                {
                    List<GeoPoint> points = new ArrayList<GeoPoint>();
                    if (geoString==""||geoString==null)
                    {
                        points.add(this.start.toPoint());
                        points.add(this.end.toPoint());
                    }
                    else
                    {
                        String[] fields = geoString.split("\t");
                        for (int i = 0; i < fields.length; i += 2)
                        {
                            double lat = Double.parseDouble(fields[i]);
                            double lng = Double.parseDouble(fields[i + 1]);
                            points.add(new GeoPoint(lat, lng));
                        }
                        this.geoString = null;
                    }
                    geo = new Polyline(points);
                }
            }
        }
        return geo;
	}

	public void setGeo(Polyline geo) {
		this.geo = geo;
	}

	private double getLength() {
		return getGeo().getLength();
	}
	
	public int getPointsCount(){
		return getGeo().getPointsCount();
	}
	
	public GeoPoint getSegStart(int segId){
		return getGeo().getSegStartPoint(segId);
	}
	
	public GeoPoint getSegEnd(int segId){
		return getGeo().getSegEndPoint(segId);
	}

	public Edge(long id, Vertex start, Vertex end) {
		this.id = id;
		this.start = start;
		this.end = end;
	}

	public Edge(long id, Vertex start, Vertex end, double length) {
		this.id = id;
		this.start = start;
		this.end = end;
		this.length = length;
	}
	
	public DistanceType projectFrom(GeoPoint p)
    {
        return getGeo().projectFrom(p);
    }
	
	public DistanceType dist2From(GeoPoint p)
    {
        return getGeo().dist2From(p);
    }

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
        if (obj != null && obj instanceof Edge)
        {
            result = ((Edge)obj).getId() == getId();
        }
        return result;
	}

	@Override
	public int hashCode() {
		long id = getId();
		return ((int)id) ^ (int)(id >> 32);
	}

	@Override
	public String toString() {
		return "ID:"+this.id+","+this.getStart().getId()+"->"+this.getEnd().getId();
	}
 
    
}
