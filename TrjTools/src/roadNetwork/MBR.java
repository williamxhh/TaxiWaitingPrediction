package roadNetwork;

public class MBR {
	final int DIMENSIONS = 2;
	
	private final int LAT_IDX = 1;
	private final int LNG_IDX = 0;
	
	double[] max;
	double[] min;
	
	public MBR (double minLng,double minLat,double maxLng,double maxLat){
		this.max = new double[]{maxLng,maxLat};
		this.min = new double[]{minLng,minLat};
	}
	
	public static MBR EMPTY = new MBR(Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY);
	
	public static MBR ALL = new MBR(Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY);
	
	public void unionWith(MBR mbr)
    {
        this.min[LAT_IDX] = Math.min(mbr.min[LAT_IDX], this.min[LAT_IDX]);
        this.max[LAT_IDX] = Math.max(mbr.max[LAT_IDX], this.max[LAT_IDX]);
        this.min[LNG_IDX] = Math.min(mbr.min[LNG_IDX], this.min[LNG_IDX]);
        this.max[LNG_IDX] = Math.max(mbr.max[LNG_IDX], this.max[LNG_IDX]);
    }
    public void include(GeoPoint p)
    {
        this.min[LAT_IDX] = Math.min(p.getLat(), this.min[LAT_IDX]);
        this.max[LAT_IDX] = Math.max(p.getLat(), this.max[LAT_IDX]);
        this.min[LNG_IDX] = Math.min(p.getLng(), this.min[LNG_IDX]);
        this.max[LNG_IDX] = Math.max(p.getLng(), this.max[LNG_IDX]);
    }
    boolean contain(MBR mbr)
    {
        boolean result = (this.min[LAT_IDX] <= mbr.min[LAT_IDX]) &&
            (this.min[LNG_IDX] <= mbr.min[LNG_IDX]) &&
            (this.max[LAT_IDX] >= mbr.max[LAT_IDX]) &&
            (this.max[LNG_IDX] >= mbr.max[LAT_IDX]);
        return result;
    }
    public boolean cover(GeoPoint p)
    {
        boolean inside = false;
        if (p.getLat() >= this.min[LAT_IDX]
            && p.getLat() <= this.max[LAT_IDX]
            && p.getLng() >= this.min[LNG_IDX]
            && p.getLng() <= this.max[LNG_IDX])
        {
            inside = true;
        }
        return inside;
    }
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
        if (obj instanceof MBR)
        {
            MBR tmp = (MBR)obj;
            result = (tmp.getMaxLat() == getMaxLat() && tmp.getMaxLng() == getMaxLng()
                && tmp.getMinLat() == getMinLat() && tmp.getMinLng() == getMinLng());
        }
        return result;
	}
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	@Override
	public String toString() {
		return "min("+this.min[LNG_IDX]+","+this.min[LAT_IDX]+"),max("+this.max[LNG_IDX]+","+this.max[LAT_IDX]+")";
	}
    
    public double getHeight(){
    	return this.max[LAT_IDX] - this.min[LAT_IDX];
    }
    
    public double getWidth(){
    	return this.max[LNG_IDX] - this.min[LNG_IDX];
    }
    
    public double getMinLat(){
    	return min[LAT_IDX];
    }
    
    public double getMinLng(){
    	return min[LNG_IDX];
    }
    
    public double getMaxLat(){
    	return max[LAT_IDX];
    }
    
    public double getMaxLng(){
    	return max[LNG_IDX];
    }
    
    public GeoPoint getTopLeft()
    {
    	return new GeoPoint(min[LAT_IDX], min[LNG_IDX]);
    }
    
    public GeoPoint getBottomRight()
    {
            return new GeoPoint(max[LAT_IDX], max[LNG_IDX]);
    }
	
}
