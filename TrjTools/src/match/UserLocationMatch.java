package match;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import roadNetwork.DistanceType;
import roadNetwork.Edge;
import roadNetwork.GeoPoint;
import roadNetwork.Graph;

public class UserLocationMatch {
	private static final Logger LOG = Logger.getLogger(UserLocationMatch.class);
	public static final String PROPS_FILENAME ="UserLocationMatch.properties"; 
	static final int MAX_RADIUS = 200;
    static final int RADIUS = 40;
    static final double sigma = 10;
    static final double sSigma = 1 / (sigma * sigma);
    private static Graph graph = null;
	private static List<GeoPoint> users = new ArrayList<GeoPoint>();
	
	static Properties sysConfigProps = new Properties();
	
	public static void main(String[] args) {
		loadProperties();
		graph = loadData();
		List<List<MatchResult>> result = new ArrayList<List<MatchResult>>();
		for(GeoPoint user:users){
//			long startTimestamp= System.currentTimeMillis();
			List<MatchResult> roads = userMatch(user);
//			System.out.println((System.currentTimeMillis()-startTimestamp)/1000.0+"s");
			Collections.sort(roads,Collections.reverseOrder());
			result.add(roads);
		}
		System.out.println(result.get(0));
	}
	
	private static void loadProperties(){
		String propsPath = UserLocationMatch.class.getClass().getResource("/").getPath();
		try {
			BufferedReader propsReader = new BufferedReader(new FileReader(new File(propsPath+PROPS_FILENAME)));
			sysConfigProps.load(propsReader);
		} catch (FileNotFoundException e) {
			LOG.error("System properties config file not found");
			e.printStackTrace();
		} catch (IOException e) {
			LOG.error("Error occured in reading system properties config file");
			e.printStackTrace();
		}
	}
	
	private static List<MatchResult> userMatch(GeoPoint user){
		List<MatchResult> result = new ArrayList<MatchResult>();
		
		//以下两行代码是用来修正用户的GPS数据与路网数据的偏移量
//		user.setLng(user.getLng()+Double.parseDouble(sysConfigProps.getProperty("longituteDelta")));
//		user.setLat(user.getLat()+Double.parseDouble(sysConfigProps.getProperty("latituteDelta")));
		
		HashSet<Edge> candidateEdges = getCandidateEdges(user,RADIUS);
		for(Edge e :candidateEdges)
        {
            double prob = getEmissionProbility(e, user);
            result.add(new MatchResult(e.getId(),prob));
        }
		return result;
	}
	
	private static double getEmissionProbility(Edge e, GeoPoint point)
    {
        double prob = Double.NEGATIVE_INFINITY;
        DistanceType dt = new DistanceType();
        double distance2 = e.dist2From(point, dt);

        if (Math.abs(dt.type) < 1)
        {
            //penalty
            if (dt.type != 0)
            {
                distance2 *= 1.44;
            }
            prob = -0.5 * distance2 * sSigma;
        }
        else
        {
            prob = Double.NEGATIVE_INFINITY;
        }
        return prob;
    }

	private static HashSet<Edge> getCandidateEdges(GeoPoint p, double radius)
    {
        double maxRadius = MAX_RADIUS;
        HashSet<Edge> cands = graph.rangeQuery(p, radius, maxRadius);
        HashSet<Edge> result = new HashSet<Edge>(cands);
        for(Edge e : cands)
        {
        	DistanceType dt = e.projectFrom(p);
            if (dt.type != 0)
            {
                result.remove(e);
            }
        }
        return result;
    }
	
	private static Graph loadData(){
		String edgesFile = sysConfigProps.getProperty("edgesFile");
		String verticesFile = sysConfigProps.getProperty("verticesFile");
		String geosFile = sysConfigProps.getProperty("geosFile");
		Graph g = new Graph(verticesFile, edgesFile, geosFile);
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(sysConfigProps.getProperty("testSourceFile")));
			String line = reader.readLine();//首行是标题，不是实际数据，没有用，此处直接读掉
			while((line=reader.readLine())!=null){
				String location = line.split("\t")[0];
				String[] userLocation = location.split(",");
				double lng = Double.parseDouble(userLocation[0].substring(1));
				double lat = Double.parseDouble(userLocation[1].substring(0,userLocation[1].length()-1));
				users.add(new GeoPoint(lat, lng));
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return g;
	}
}
