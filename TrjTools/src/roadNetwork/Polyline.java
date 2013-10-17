package roadNetwork;

import java.util.List;

import util.Constants;

public class Polyline {
	private List<GeoPoint> points;
	
	public MBR getMBR()
    {
        double minLat = Double.POSITIVE_INFINITY, minLng = Double.POSITIVE_INFINITY;
        double maxLat = Double.NEGATIVE_INFINITY, maxLng = Double.NEGATIVE_INFINITY;
        int pointCount = points.size();
        for (int i = 0; i < pointCount; i++)
        {
            minLat = Math.min(minLat, points.get(i).getLat());
            minLng = Math.min(minLng, points.get(i).getLng());
            maxLat = Math.max(maxLat, points.get(i).getLat());
            maxLng = Math.max(maxLng, points.get(i).getLng());
        }
        MBR mbr = new MBR(minLng, minLat, maxLng, maxLat);
        return mbr;
    }
	
	public int getPointsCount(){
		return this.points.size();
	}
	
	public GeoPoint getSegStartPoint(int segId){
		return this.points.get(segId);
	}
	
	public GeoPoint getSegEndPoint(int segId){
		return this.points.get(segId+1);
	}
	
	public double getLength(){
		return getLength(false);
	}
	
	public double getLength(boolean isPrecise)
    {
        double tmpLen = 0;
        for (int i = 0; i < this.points.size() - 1; i++)
        {
            if (isPrecise)
            {
                tmpLen += GeoPoint.getPreciseDistance(points.get(i), points.get(i+1));
            }
            else
            {
                tmpLen += GeoPoint.getDistance(points.get(i), points.get(i+1));
            }
        }
        return tmpLen;
    }
	
	public Polyline(List<GeoPoint> points)
    {
        this.points = points;
    }
	
	public DistanceType projectFrom(GeoPoint p)
    {
		DistanceType dt = new DistanceType();
        dt.type = -1;
        double minDist = Double.POSITIVE_INFINITY;
        dt.projection = GeoPoint.INVALID;
        dt.segid = 0;
        for (int i = 0; i < this.points.size() - 1; i++)
        {
        	DistanceType tempdt = Polyline.projectFrom(points.get(i), points.get(i+1), p);
            double tmpDist = GeoPoint.getDistance2(tempdt.projection, p);

            if (tmpDist <= minDist)
            {
                if (tempdt.type == 0 || dt.type != 0)
                {
                    //good projection is true or tmpType==0
                    dt.type = tempdt.type;
                    minDist = tmpDist;
                    dt.projection = tempdt.projection;
                    dt.segid = i;
                }
            }
        }
        return dt;
    }
	
	public static DistanceType projectFrom(GeoPoint start, GeoPoint end, GeoPoint p)
    {
		DistanceType dt = new DistanceType();
        double vY = end.getLat() - start.getLat();
        double vX = end.getLng() - start.getLng();
        double wY = p.getLat() - start.getLat();
        double wX = p.getLng() - start.getLng();

        //扭转LAT、LNG比例误差
        double vY_m = vY * Constants.M_PER_LAT;	//
        double vX_m = vX * Constants.M_PER_LNG;	//
        double wY_m = wY * Constants.M_PER_LAT;
        double wX_m = wX * Constants.M_PER_LNG;

        double bY, bX;

        double c1 = wY_m * vY_m + wX_m * vX_m;
        double c2 = vY_m * vY_m + vX_m * vX_m;

        dt.projection = GeoPoint.INVALID;

        if (c1 <= 0)
        {
            //when the given point is left of the source point
            dt.projection = start;
        }
        else if (c2 <= c1)
        {
            // when the given point is right of the target point
            dt.projection = end;
        }
        else //between the source point and target point
        {
            double b = c1 / c2;
            bY = start.getLat() + b * vY;
            bX = start.getLng() + b * vX;
            dt.projection = new GeoPoint(bY, bX);
        }
        dt.type = (short)(c1 / c2);
        return dt;
    }
	
	public DistanceType dist2From(GeoPoint p)
    {
		DistanceType dt =  projectFrom(p);
		dt.distance = GeoPoint.getDistance2(dt.projection, p);
        return dt;
    }
}
