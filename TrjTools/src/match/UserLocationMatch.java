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
	
	public static Properties sysConfigProps = new Properties();
	
	public static void main(String[] args) {
		loadProperties();
		graph = loadData();
		List<List<MatchResult>> result = new ArrayList<List<MatchResult>>();
		for(GeoPoint user:users){
			List<MatchResult> roads = userMatch(user);
			Collections.sort(roads,Collections.reverseOrder());
			result.add(roads);
		}
		System.out.println(result.get(0));
	}
	
	/**
	 * 这个match方法做的事情和main一样，写在这里为了供prediction调用
	 * @return
	 */
	public static  List<List<MatchResult>> match(){
		loadProperties();
		graph = loadData();
		List<List<MatchResult>> result = new ArrayList<List<MatchResult>>();
		for(GeoPoint user:users){
			List<MatchResult> roads = userMatch(user);
			Collections.sort(roads,Collections.reverseOrder());
			result.add(roads);
		}
		return result;
	}
	
	private static void loadProperties(){
		try {
			BufferedReader propsReader = new BufferedReader(new InputStreamReader(UserLocationMatch.class.getClass().getResourceAsStream("/"+PROPS_FILENAME)));
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
//		List<MatchResult> result = new ArrayList<MatchResult>();
		
		if(sysConfigProps.getProperty("fixDelta").equals("true")){
		//以下两行代码是用来修正用户的GPS数据与路网数据的偏移量
			user.setLng(user.getLng()+Double.parseDouble(sysConfigProps.getProperty("longituteDelta")));
			user.setLat(user.getLat()+Double.parseDouble(sysConfigProps.getProperty("latituteDelta")));
			System.out.println(user.getLng());
			System.out.println(user.getLat());
		}
		HashSet<MatchResult> candidateEdges = getCandidateEdges(user,RADIUS);
//		for(MatchResult r :candidateEdges)
//        {
//            double prob = getEmissionProbility(r.getEdge(), user);
//            r.setProbability(prob);
//            result.add(r);
//        }
		List<MatchResult> result = new ArrayList<MatchResult>(candidateEdges);
		return result;
	}
	
//	private static double getEmissionProbility(Edge e, GeoPoint point)
//    {
//        double prob = Double.NEGATIVE_INFINITY;
//        DistanceType dt = e.dist2From(point);
//
//        if (Math.abs(dt.type) < 1)
//        {
//            //penalty
//            if (dt.type != 0)
//            {
//                dt.distance *= 1.44;
//            }
//            prob = -0.5 * dt.distance * sSigma;
//        }
//        else
//        {
//            prob = Double.NEGATIVE_INFINITY;
//        }
//        return prob;
//    }

	private static HashSet<MatchResult> getCandidateEdges(GeoPoint user, double radius)
    {
        double maxRadius = MAX_RADIUS;
        HashSet<Edge> cands = graph.rangeQuery(user, radius, maxRadius);
        HashSet<MatchResult> result = new HashSet<MatchResult>();
        for(Edge e : cands)
        {
        	DistanceType dt = e.projectFrom(user);
            if (dt.type == 0)
            {
            	GeoPoint start;
            	GeoPoint end;
            	//如果edge是一条直线，不是一条折线，那么它的起点终点直接就是起终点
            	if(e.getPointsCount()<=2){
            		start = e.getStart().toPoint();
            		end = e.getEnd().toPoint();
            	}
            	//如果edge是一条折线，那么就根据片段号取到相应路段的起终点
            	else{
            		start = e.getSegStart(dt.segid);
            		end = e.getSegEnd(dt.segid);
            	}
            	if(determineDirection(start, end, user)){
        			result.add(new MatchResult(e, dt.segid, dt.distance,start, end));
        		}
            }
        }
        return result;
    }
	
	/**
	 * 判断user当前位置，与start和end构成的路段的相对位置，以取得用户期望的乘车方向
	 * 
	 * @param start   路段起点
	 * @param end     路段终点
	 * @param user    用户位置
	 * @return    返回true，则表明用户期望打车的方向是start到end，否则则为end到start
	 */
	private static boolean determineDirection(GeoPoint start,GeoPoint end,GeoPoint user){
		if(determineQuadrant(start,end)==1){
			if(determineQuadrant(start, user)==2||determineQuadrant(start,user)==6||determineQuadrant(start,user)==7){
				return false;
			}else if(determineQuadrant(start,user)==4||determineQuadrant(start,user)==5||determineQuadrant(start,user)==8){
				return true;
			}else if(determineQuadrant(start,user)==1){
				if(compareTan(start, end, user)){
					return true;
				}else{
					return false;
				}
			}else if(determineQuadrant(start,user)==3){
				if(compareTan(start, end, user)){
					return false;
				}else{
					return true;
				}
			}
		}else if(determineQuadrant(start,end)==2){
			if(determineQuadrant(start,user)==1||determineQuadrant(start,user)==5||determineQuadrant(start,user)==7){
				return true;
			}else if(determineQuadrant(start,user)==3||determineQuadrant(start,user)==6||determineQuadrant(start,user)==8){
				return false;
			}else if(determineQuadrant(start,user)==2){
				if(compareTan(start, end, user)){
					return true;
				}else{
					return false;
				}
			}else if(determineQuadrant(start,user)==4){
				if(compareTan(start, end, user)){
					return false;
				}else{
					return true;
				}
			}
			
		}else if(determineQuadrant(start,end)==3){
			if(determineQuadrant(start, user)==2||determineQuadrant(start,user)==6||determineQuadrant(start,user)==7){
				return true;
			}else if(determineQuadrant(start,user)==4||determineQuadrant(start,user)==5||determineQuadrant(start,user)==8){
				return false;
			}else if(determineQuadrant(start,user)==1){
				if(compareTan(start, end, user)){
					return false;
				}else{
					return true;
				}
			}else if(determineQuadrant(start,user)==3){
				if(compareTan(start, end, user)){
					return true;
				}else{
					return false;
				}
			}
		}else if(determineQuadrant(start,end)==4){
			if(determineQuadrant(start,user)==1||determineQuadrant(start,user)==5||determineQuadrant(start,user)==7){
				return false;
			}else if(determineQuadrant(start,user)==3||determineQuadrant(start,user)==6||determineQuadrant(start,user)==8){
				return true;
			}else if(determineQuadrant(start,user)==2){
				if(compareTan(start, end, user)){
					return false;
				}else{
					return true;
				}
			}else if(determineQuadrant(start,user)==4){
				if(compareTan(start, end, user)){
					return true;
				}else{
					return false;
				}
			}
		}else if(determineQuadrant(start,end)==5){
			if(user.getLat()>start.getLat()){
				return false;
			}else{
				return true;
			}
		}else if(determineQuadrant(start,end)==6){
			if(user.getLat()>start.getLat()){
				return true;
			}else{
				return false;
			}
		}else if(determineQuadrant(start,end)==7){
			if(user.getLng()>start.getLng()){
				return true;
			}else{
				return false;
			}
		}else if(determineQuadrant(start,end)==8){
			if(user.getLng()>start.getLng()){
				return false;
			}else{
				return true;
			}
		}
		return true;
	}
	
	/**
	 * 判断destination这个点相对于source这个点为原点，经纬度为坐标轴的坐标系中的象限位置
	 * 
	 * @param source  参照系的原点
	 * @param destination   要判断的目标点
	 * @return 1表示第一象限，2表示第二象限，3表示第三象限，4表示第四象限,5表示x轴正半轴，6表示x轴负半轴，7表示y轴正半轴，8表示y轴负半轴
	 * 			返回0的时候表示无法判断了，正常情况下不应该返回0
	 */
	private static int determineQuadrant(GeoPoint source,GeoPoint destination){
		double lat_delta = destination.getLat() - source.getLat();
		double lng_delta = destination.getLng() - source.getLng();
		
		if(lat_delta>0&&lng_delta>0){
			return 1;
		}else if(lat_delta>0&&lng_delta<0){
			return 2;
		}else if(lat_delta<0&&lng_delta<0){
			return 3;
		}else if(lat_delta<0&&lng_delta>0){
			return 4;
		}else if(Math.abs(lat_delta)<10e-8){
			if(lng_delta>0){
				return 5;
			}else{
				return 6;
			}
		}else if(Math.abs(lng_delta)<10e-8){
			if(lat_delta>0){
				return 7;
			}else{
				return 8;
			}
		}
		return 0;
	}
	
	
	/**
	 * 计算以start点为原点，经纬度分别为x，y坐标轴，路段的end端点和user点与start点构成的tan值的大小
	 * 
	 * @param start   路段start端点
	 * @param end     路段end端点
	 * @param user    user位置
	 * @return   返回true表示路段的tan值，比user与start连线的tan值大，否则，返回false
	 */
	private static boolean compareTan(GeoPoint start,GeoPoint end,GeoPoint user){
		double userLngDelta = user.getLng()-start.getLng();
		double userLatDelta = user.getLat()-start.getLat();
		double roadLngDelta = end.getLng()-start.getLng();
		double roadLatDelta = end.getLat()-start.getLat();
		if((roadLatDelta/roadLngDelta)>(userLatDelta/userLngDelta)){
			return true;
		}
		return false;
	}
	
	private static Graph loadData(){
		String edgesFile = sysConfigProps.getProperty("edgesFile");
		String verticesFile = sysConfigProps.getProperty("verticesFile");
		String geosFile = sysConfigProps.getProperty("geosFile");
		Graph g = new Graph(verticesFile, edgesFile, geosFile);
		
		try {
//			BufferedReader reader = new BufferedReader(new FileReader(sysConfigProps.getProperty("testSourceFile")));
			BufferedReader reader = new BufferedReader(new InputStreamReader(UserLocationMatch.class.getClass().getResourceAsStream(sysConfigProps.getProperty("testSourceFile"))));
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
