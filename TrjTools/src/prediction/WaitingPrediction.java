package prediction;

import index.prediction.EdgeIndex;
import index.prediction.EdgeStatInfo;
import index.prediction.TimeSegment;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import match.MatchResult;
import match.UserLocationMatch;

public class WaitingPrediction {
	private static final Logger LOG = Logger.getLogger(WaitingPrediction.class);
	public static final String PROPS_FILENAME ="WaitingPrediction.properties"; 
	static Properties sysConfigProps = new Properties();
	static List<String> dateTimeList = new ArrayList<String>();
	
	public static void main(String[] args) throws Exception {
		loadProperties();
		loadUserData();
		List<List<MatchResult>> mrList = UserLocationMatch.match();
		if(mrList.size()!=dateTimeList.size()) throw new Exception();
		
		for(int i=0;i<mrList.size();i++){
			// TODO: 根据mrList构造top N的 EdgeIndex
			List<EdgeIndex> edgeIndex = new ArrayList<EdgeIndex>();
			// TODO: 根据dateTimeList和getDayOfWeek函数构造TimeSegment
			TimeSegment timeSeg = new TimeSegment();
			
			List<EdgeStatInfo> edgeStatInfo = readIndex(edgeIndex, timeSeg);
			PredictResult predictResult = predict(edgeStatInfo);
		}
		
		
		
	}
	
	public static List<EdgeStatInfo> readIndex(List<EdgeIndex> edgeIndex,TimeSegment timeSeg){
		return null;
	}
	
	public static int getDayOfWeek(String dateTimeStr) throws ParseException{
		
			String[] dateArr = dateTimeStr.split(" ")[0].split("-");
			String[] timeArr = dateTimeStr.split(" ")[1].split(":");
			if(dateArr[1].length()==1) dateArr[1] = "0"+dateArr[1];
			if(dateArr[2].length()==1) dateArr[2] = "0"+dateArr[2];
			if(timeArr[0].length()==1) timeArr[0] = "0"+timeArr[0];
			if(timeArr[1].length()==1) timeArr[1] = "0"+timeArr[1];
			if(timeArr[2].length()==1) timeArr[2] = "0"+timeArr[2];
			
			String dateTime = dateArr[0]+"-"+dateArr[1]+"-"+dateArr[2]+" "+timeArr[0]+":"+timeArr[1]+":"+timeArr[2];
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Calendar cal = Calendar.getInstance();
			cal.setTime(df.parse(dateTime));
			int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)-1;
			if(dayOfWeek==0) dayOfWeek+=7;
			return dayOfWeek;
	}
	
	public static PredictResult predict(List<EdgeStatInfo> edgeStatInfo){
		//TODO:这三个列表只是一个桩，到时候用edgeStatInfo里面的真是数据替换
		List<Double> meanFlowList = new ArrayList<Double>();
		List<Double> meanSpeedList = new ArrayList<Double>();
		List<Double> emptyRatioList = new ArrayList<Double>();
		
		PredictResult result = new PredictResult();
		result.probability = emptyRatioList.get(0) + (1-emptyRatioList.get(0))*emptyRatioList.get(1) + (1-emptyRatioList.get(0))*(1-emptyRatioList.get(1))*emptyRatioList.get(2);
		int waitingMin = 0;
		Double sum = 0.0;
		for(;waitingMin<=15;waitingMin++){
			sum += emptyRatioList.get(waitingMin);
			if(sum>=1) break;
		}
		result.estimatedWaitingTime = waitingMin;
		
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
	
	static void loadUserData(){
		try {
//			BufferedReader reader = new BufferedReader(new FileReader(sysConfigProps.getProperty("testSourceFile")));
			BufferedReader reader = new BufferedReader(new InputStreamReader(WaitingPrediction.class.getClass().getResourceAsStream(sysConfigProps.getProperty("testSourceFile"))));
			String line = reader.readLine();//首行是标题，不是实际数据，没有用，此处直接读掉
			while((line=reader.readLine())!=null){
				dateTimeList.add(line.split("\t")[1]);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
