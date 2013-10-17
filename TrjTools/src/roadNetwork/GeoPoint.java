package roadNetwork;

public class GeoPoint {
	private static final int DIVISOR = 10000000;
	public static final int M_PER_LAT = 110000;
    public static final int M_PER_LNG = 70000;
	public static GeoPoint INVALID= new GeoPoint(-1,-1);
	
	private double lat;
	private double lng;
	
	public double getLat() {
		return lat/DIVISOR;
	}

	public void setLat(double lat) {
		this.lat = lat*DIVISOR;
	}

	public double getLng() {
		return lng/DIVISOR;
	}

	public void setLng(double lng) {
		this.lng = lng*DIVISOR;
	}

	public GeoPoint(double lat,double lng){
		this.lat = lat*DIVISOR;
		this.lng = lng*DIVISOR;
	}
	
	public boolean isValid(){
		return !((this.lat==-1)&&(this.lng==-1));
	}

	@Override
	public String toString() {
		return "("+lng+","+lat+")";
	}
	
	public static double getDistance2(GeoPoint p1, GeoPoint p2)
    {
        double height = Math.abs(p2.getLat() - p1.getLat()) * M_PER_LAT; //110km per latitude
        double width = Math.abs(p2.getLng() - p1.getLng()) * M_PER_LNG;	//70km per longitude
        return height * height + width * width;
    }
	
	public static double getDistance(GeoPoint p1, GeoPoint p2)
    {
        return Math.sqrt(getDistance2(p1, p2));
    }
	
	public static double rad(double d)
    {
        return d * Math.PI / 180.0;
    }
	
	public static double getPreciseDistance(GeoPoint pA, GeoPoint pB)
    {
        double latA = pA.getLat(), lngA = pA.getLng();
        double latB = pB.getLat(), lngB = pB.getLng();
        double radLatA = rad(latA);
        double radLatB = rad(latB);
        double a = radLatA - radLatB;
        double b = rad(lngA) - rad(lngB);
        double distance = 2 * Math.asin(Math.sqrt(
            Math.pow(Math.sin(a / 2), 2) + Math.cos(radLatA) * Math.cos(radLatB) * Math.pow(Math.sin(b / 2), 2)));
        distance = distance * 6378137.0;
        distance = (int)(distance * 10000) / 10000;
        return distance;
    }
	
}
