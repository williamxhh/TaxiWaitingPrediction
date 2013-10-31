package prediction;

import index.prediction.IndexRecord;
import index.prediction.StatisticsRecord;
import index.prediction.TimeSegment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import roadNetwork.Edge;
import match.MatchResult;
import match.UserLocationMatch;

public class WaitingPrediction {
	private static final Logger LOG = Logger.getLogger(WaitingPrediction.class);
	private static final int MAX_WAITING_MIN = 15;
	private static final int WEEKDAY = 12345;
	private static final int WEEKENDS = 67;
	public static final String PROPS_FILENAME = "WaitingPrediction.properties";
	static Properties sysConfigProps = new Properties();
	static List<String> dateTimeList = new ArrayList<String>();
	static List<PredictResult> predictResult = new ArrayList<PredictResult>();
	static Map<Long, Map<Integer, Map<Integer,StatisticsRecord>>> CACHED_INDEX = new HashMap<Long, Map<Integer, Map<Integer,StatisticsRecord>>>();
	
	static Set<Long> allRelatedEdges = new HashSet<Long>();
	static double allRelatedTraficFlows = 0;

	
	private static void displayHelpInfo()
	{
		System.out.println();
		System.out.println("*****************用户打车概率及等待时间预测********************");
		System.out.println("Usage: java -jar WaitingPrediction.jar [-eidx eidx_file/] [-stat stat_file_path/] [-in inputFile/] [-out result/] ");
		System.out.println();
		System.out.println("where options include:");
		System.out.printf("%1$-30s %2$s", "-e,-eidx","索引文件.edix的文件的位置\n");
		System.out.printf("%1$-30s %2$s", "-s,-stat","统计文件.stat文件所在的目录\n");
		System.out.printf("%1$-30s %2$s", "-i,-in","待预测的输入的文件位置\n");
		System.out.printf("%1$-30s %2$s", "-o,-out","预测结果输出的文件位置\n");
		System.out.println();
	}
	
	private static boolean receiveParmas(String[] args,Properties sysProp)
	{
		for(int i=0;i<args.length;i++)
		{
			String arg= args[i].trim();
			if(arg.equalsIgnoreCase("-help"))
			{
				displayHelpInfo();
				return false;
			}
			if(arg.startsWith("-") && args.length>i+1)
			{
				if(arg.equalsIgnoreCase("-e")||arg.equalsIgnoreCase("-eidx"))
				{
					sysProp.setProperty("eidxFile", args[i+1].trim());
				}
				if(arg.equalsIgnoreCase("-s")||arg.equalsIgnoreCase("-stat"))
				{
					sysProp.setProperty("statFilePath", args[i+1].trim());
				}
				if(arg.equalsIgnoreCase("-i")||arg.equalsIgnoreCase("-input"))
				{
					sysProp.setProperty("testSourceFile", args[i+1].trim());
				}
				if(arg.equalsIgnoreCase("-o")||arg.equalsIgnoreCase("-out"))
				{
					sysProp.setProperty("resultFile", args[i+1].trim());
				}
			}
		}
		return true;
	}
	
	private static boolean checkSysProp(Properties sysProp)
	{
		boolean doContinue=true;
		try
		{
			if(!(new File(sysProp.getProperty("eidxFile"))).exists()){
				doContinue=false;
				throw new Exception("没有找到.eidx文件  请用 -e 指定");
			}
			if(!(new File(sysProp.getProperty("statFilePath"))).exists()){
				doContinue=false;
				throw new Exception("没有找到.stat文件的目录  请用 -s 指定");
			}
			if(!(new File(sysProp.getProperty("testSourceFile"))).exists()){
				doContinue=false;
				throw new Exception("没有找到输入文件  请用 -i 指定");
			}
			File tempFile=new File(sysProp.getProperty("resultFile"));
			tempFile.getParentFile().mkdirs();
		}catch(Exception e)
		{
			doContinue=false;
			LOG.error("配置参数有误,异常抛出：",e);
		}
		if(doContinue==false)
		{
			LOG.error("配置参数有误，请检查！");
		}
		return doContinue;
	}
	
	public static void main(String[] args) throws Exception {

		loadProperties();
		boolean doContinue=true;
		if(args.length>0)
		{
			doContinue=receiveParmas(args,sysConfigProps); //接收外部参数
		}
		if(doContinue==true)
		{
			doContinue=checkSysProp(sysConfigProps);
		}
		if(doContinue==true){
			Calendar cal = Calendar.getInstance();
			loadUserData();
			List<List<MatchResult>> mrList = UserLocationMatch.match();
			if (mrList.size() != dateTimeList.size())
				throw new Exception();
			
			for(List<MatchResult> oneUser: mrList){
				for(MatchResult r:oneUser){
					allRelatedEdges.add(r.getEdge().getId());
				}
			}
			
			
			readIndex();
			List<TimeSegment> allUserTime = new ArrayList<TimeSegment>();
			for(int i=0;i<dateTimeList.size();i++){
				allUserTime.add(parseToTimeSegment(parseDateTime(dateTimeList.get(i))));
			}
			countAllTrafic(mrList, allUserTime);

			Long edgeId = 0l;
			TimeSegment userTime = null;
			for (int i = 0; i < mrList.size(); i++) {
				edgeId = mrList.get(i).get(0).getEdge().getId();
				userTime = allUserTime.get(i);
				predictResult.add(predict(edgeId, userTime));
			}

			for (PredictResult r : predictResult) {
				System.out.println(r);
			}
			System.out.println((Calendar.getInstance().getTimeInMillis()-cal.getTimeInMillis())/1000);
		}
	}
	
	static void countAllTrafic(List<List<MatchResult>> mrList,List<TimeSegment> allUserTime){
		int minutesToCount = 3;
		for(int i=0;i<mrList.size();i++){
			Long edgeId = mrList.get(i).get(0).getEdge().getId();
			for(int j=0;j<minutesToCount;j++){
				if(isWeekDay(allUserTime.get(i).getDateType().getValue())){
					allRelatedTraficFlows+=searchIndex(edgeId, WEEKDAY, allUserTime.get(i).getIndex()+j).getAvrTraficFlow();
				}else if(isWeekends(allUserTime.get(i).getDateType().getValue())){
					allRelatedTraficFlows+=searchIndex(edgeId, WEEKENDS, allUserTime.get(i).getIndex()+j).getAvrTraficFlow();
				}
			}
		}
	}

	static TimeSegment parseToTimeSegment(Calendar cal) {
		Long millis = cal.getTimeInMillis();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		long span = (millis - cal.getTimeInMillis()) / 60000;
		return new TimeSegment((int) span, cal.get(Calendar.DAY_OF_WEEK));
	}

	/**
	 * 将从测试文件中读入的用户时间转为Calendar对象
	 * @param dateTimeStr
	 * @return
	 * @throws ParseException
	 */
	public static Calendar parseDateTime(String dateTimeStr)
			throws ParseException {
		String[] dateArr = dateTimeStr.split(" ")[0].split("-");
		String[] timeArr = dateTimeStr.split(" ")[1].split(":");
		if (dateArr[1].length() == 1)
			dateArr[1] = "0" + dateArr[1];
		if (dateArr[2].length() == 1)
			dateArr[2] = "0" + dateArr[2];
		if (timeArr[0].length() == 1)
			timeArr[0] = "0" + timeArr[0];
		if (timeArr[1].length() == 1)
			timeArr[1] = "0" + timeArr[1];
		if (timeArr[2].length() == 1)
			timeArr[2] = "0" + timeArr[2];

		String dateTime = dateArr[0] + "-" + dateArr[1] + "-" + dateArr[2]
				+ " " + timeArr[0] + ":" + timeArr[1] + ":" + timeArr[2];
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		cal.setTime(df.parse(dateTime));
		return cal;
	}

	/**
	 * 将eidx文件扫一遍，将所有与当前测试文件相关的edge的数据索引都建在内存中，从stat文件中读取数据   缓存到全局map  CACHED_INDEX中
	 * 这个函数处理完以后，CACHED_INDEX中的数据已经是纵向 按工作日和非工作日平滑好了的
	 */
	public static void readIndex() {
		//对所有要读入的edge，先把要建好的内存map结构建好，后面直接读文件累加
		for(Long eid:allRelatedEdges){
			Map<Integer,Map<Integer,StatisticsRecord>> datetypeMap = new TreeMap<Integer,Map<Integer,StatisticsRecord>>();
			Map<Integer,StatisticsRecord> dailyTimeSegMap = new TreeMap<Integer, StatisticsRecord>();
			for(int i=0;i<1440;i++){
				dailyTimeSegMap.put(i, new StatisticsRecord(0, 0, 0));				
			}
			datetypeMap.put(WEEKDAY, dailyTimeSegMap);
			dailyTimeSegMap = new TreeMap<Integer, StatisticsRecord>();
			for(int i=0;i<1440;i++){
				dailyTimeSegMap.put(i, new StatisticsRecord(0, 0, 0));				
			}
			datetypeMap.put(WEEKENDS, dailyTimeSegMap);
			CACHED_INDEX.put(eid, datetypeMap);
		}
		
		//根据eidx文件的索引，到stat文件中读入数据，累加到CACHED_INDEX中
		try {
			BufferedReader reader = new BufferedReader(
					new FileReader(sysConfigProps.getProperty("eidxFile")));
			String line = "";
			while ((line = reader.readLine()) != null) {
				String[] splits = line.split(",");
				Long edgeId = new Long(Long.parseLong(splits[0]));
				if(allRelatedEdges.contains(edgeId)){
					Integer datetype = new Integer(Integer.parseInt(splits[1]));
					Map<Integer,StatisticsRecord> readResult = readStatFile(sysConfigProps.getProperty("statFilePath")+splits[2], Integer.parseInt(splits[3]), Integer.parseInt(splits[4]));
					
					for (Map.Entry<Integer, StatisticsRecord> entry : readResult
							.entrySet()) {
						if (isWeekDay(datetype)) {
							CACHED_INDEX.get(edgeId).get(WEEKDAY).get(entry.getKey()).add(entry.getValue());
						}else if(isWeekends(datetype)){
							CACHED_INDEX.get(edgeId).get(WEEKENDS).get(entry.getKey()).add(entry.getValue());
						}
					}
				}
			}
			reader.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//对CACHED_INDEX中的数据 纵向 按工作日/非工作日进行平均     即对工作日的数据进行除以5平均，对周末的数据进行除以2平均
		for(Long eid:allRelatedEdges){
			for(Map.Entry<Integer, StatisticsRecord>  weekdayRecord:CACHED_INDEX.get(eid).get(WEEKDAY).entrySet()){
				weekdayRecord.getValue().avrWeekday();
			}
			for(Map.Entry<Integer, StatisticsRecord>  weekendsRecord:CACHED_INDEX.get(eid).get(WEEKENDS).entrySet()){
				weekendsRecord.getValue().avrWeekends();
			}
		}
		
	}
	
	static boolean isWeekDay(int datetype){
		return datetype>=DateType.Monday.getValue()&&datetype<=DateType.Friday.getValue();
	}
	
	static boolean isWeekends(int datetype){
		return datetype==DateType.Saturday.getValue()||datetype==DateType.Sunday.getValue();
	}
	
	static Map<Integer,StatisticsRecord> readStatFile(String filename,int lineFrom,int lineTo){
		Map<Integer, StatisticsRecord> records = new HashMap<Integer, StatisticsRecord>();
		
		try {
			RandomAccessFile file = new RandomAccessFile(new File(filename), "r");
			file.seek(lineFrom);
			String line = "";
			while ((line = file.readLine()) != null
					&& file.getFilePointer() < lineTo) {
				String[] splits = line.split(",");
				records.put(Integer.parseInt(splits[2]), new StatisticsRecord(Double.parseDouble(splits[3]), Double.parseDouble(splits[4]),Double.parseDouble(splits[5])));
			}
			file.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return records;
	}

	/**
	 * 根据指定好的edgeid和datetype以及时间片index来读取stat文件，查询相应的edge流量，空载率，平均行驶速度，加载到CACHED_INDEX中
	 * @param edgeId
	 * @param userTime
	 */
//	public static void readIndex(long edgeId, TimeSegment userTime) {
//		try {
//			IndexRecord record = CACHED_INDEX.get(edgeId).get(
//					userTime.getDateType().getValue());
//			RandomAccessFile file = new RandomAccessFile(sysConfigProps.getProperty("statFilePath")
//					+ record.getStat_fileName(), "r");
//			file.seek(record.getLine_from());
//			String line = "";
//			while ((line = file.readLine()) != null
//					&& file.getFilePointer() < record.getLine_to()) {
//				String[] splits = line.split(",");
//				record.getStats().add(
//						new StatisticsRecord(Integer.parseInt(splits[2]), Double
//										.parseDouble(splits[3]), Double
//										.parseDouble(splits[4]), Double
//										.parseDouble(splits[5])));
//			}
//			file.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	public static PredictResult predict(List<MatchResult> mr,TimeSegment userTime){
		PredictResult result = new PredictResult(0.0, MAX_WAITING_MIN);
		
		return result;
	}
	
	
	/**
	 * 根据指定好的edgeid和datetype以及时间片index来进行预测用户的3分钟内打到车的概率和预估等待时间
	 * @param edgeId
	 * @param userTime
	 * @return
	 */
	public static PredictResult predict(long edgeId, TimeSegment userTime) {
		PredictResult result = new PredictResult(0.0, MAX_WAITING_MIN);

		// 车流量和平均速度暂时没用上
		// List<Double> meanFlowList = new ArrayList<Double>();
		// List<Double> meanSpeedList = new ArrayList<Double>();
		List<Double> emptyRatioList = new ArrayList<Double>();
		List<Double> traficFlowList = new ArrayList<Double>();

		for (int i = 0; i < MAX_WAITING_MIN; i++) {
			StatisticsRecord record = null;
			if (isWeekDay(userTime.getDateType().getValue())) {
				record = searchIndex(edgeId, WEEKDAY, userTime.getIndex() + i);
			} else if (isWeekends(userTime.getDateType().getValue())) {
				record = searchIndex(edgeId, WEEKENDS, userTime.getIndex() + i);
			}
			emptyRatioList.add(record.getAvrEmptyRatio());
			traficFlowList.add(record.getAvrTraficFlow());
		}

		/**
		 * v1.0 版本的计算策略
		 * 3分钟内打到车的概率是一个条件概率和，第一分钟打到车，第一分钟没打到第二分钟打到，第一二分钟没有打到，第三分钟打到
		 * 预估等待时间对空载率求积分，累计积分超过1的时候，认为必然打到车了 现在实际上认为空载率在一分钟是恒定的，所以求积分的时候直接累加空载率
		 */
//		result.probability = emptyRatioList.get(0)
//				+ (1 - emptyRatioList.get(0)) * emptyRatioList.get(1)
//				+ (1 - emptyRatioList.get(0)) * (1 - emptyRatioList.get(1))
//				* emptyRatioList.get(2);
		
		
		/**
		 * v2.0 版本的计算策略
		 * 3分钟内打到车的概率是当前里用户最近的这条路上三分钟总的空车数，除以所有relatedEdge中top1的edge在该用户查询时间点三分钟的总流量
		 * 预估等待时间对空载率求积分，累计积分超过1的时候，认为必然打到车了 现在实际上认为空载率在一分钟是恒定的，所以求积分的时候直接累加空载率
		 */
		double avrEmptyTexiCount = traficFlowList.get(0)*emptyRatioList.get(0)+traficFlowList.get(1)*emptyRatioList.get(1)+traficFlowList.get(2)*emptyRatioList.get(2);
		result.probability = avrEmptyTexiCount/allRelatedTraficFlows;
		
		int waitingMin = 0;
		Double sum = 0.0;
		for (; waitingMin < MAX_WAITING_MIN; waitingMin++) {
			sum += emptyRatioList.get(waitingMin);
			if (sum >= 1)
				break;
		}
		result.estimatedWaitingTime = waitingMin + 1;
		if (result.estimatedWaitingTime >= MAX_WAITING_MIN)
			result.estimatedWaitingTime = MAX_WAITING_MIN;
		return result;
	}

	/**
	 * 读取按当前传入的三个索引查到的StatisticsRecord的值，如果当前StatisticsRecord是空值，则用其前后不为空的StatisticsRecord进行线性平滑
	 * @param edgeId
	 * @param weekdayType
	 * @param dailyIndex
	 * @return
	 */
	public static StatisticsRecord searchIndex(Long edgeId,int weekdayType,int dailyIndex){
		Map<Integer,StatisticsRecord> dailyRecord = CACHED_INDEX.get(edgeId).get(weekdayType);
		
		//如果当前StatisticsRecord是空值，则用其前后不为空的StatisticsRecord进行线性平滑
		if(dailyRecord.get(dailyIndex).isEmptyRecord()){
			int lowerBound = dailyIndex;
			int upperBound = dailyIndex;
			do{
				lowerBound--;
			}while(dailyRecord.get(lowerBound).isEmptyRecord()&&lowerBound>0);
			do{
				upperBound++;
			}while(dailyRecord.get(upperBound).isEmptyRecord()&&lowerBound<1440-1);
			StatisticsRecord lowerRecord = dailyRecord.get(lowerBound);
			StatisticsRecord upperRecord = dailyRecord.get(upperBound);
			if(!lowerRecord.isEmptyRecord()||!upperRecord.isEmptyRecord()){
				for(int i=lowerBound+1;i<upperBound;i++){
					int range = i-lowerBound;
					int delta = upperBound-lowerBound;
					double traficFlow = lowerRecord.getAvrTraficFlow()+(upperRecord.getAvrTraficFlow()-lowerRecord.getAvrTraficFlow())*range/delta;
					double emptyRatio = lowerRecord.getAvrEmptyRatio()+(upperRecord.getAvrEmptyRatio()-lowerRecord.getAvrEmptyRatio())*range/delta;
					double traficSpeed = lowerRecord.getAvrTraficSpeed()+(upperRecord.getAvrTraficSpeed()-lowerRecord.getAvrTraficSpeed())*range/delta;
					dailyRecord.get(i).add(new StatisticsRecord(traficFlow, emptyRatio, traficSpeed));
				}
			}
		}
		
		return dailyRecord.get(dailyIndex);
	}
	
	public static void loadProperties() {
		try {
			BufferedReader propsReader = new BufferedReader(
					new InputStreamReader(WaitingPrediction.class.getClass()
							.getResourceAsStream("/" + PROPS_FILENAME)));
			sysConfigProps.load(propsReader);
		} catch (FileNotFoundException e) {
			LOG.error("System properties config file not found");
			e.printStackTrace();
		} catch (IOException e) {
			LOG.error("Error occured in reading system properties config file");
			e.printStackTrace();
		}
	}

	public static String getTestSourceFile(){
		return sysConfigProps.getProperty("testSourceFile","");
	}
	
	static void loadUserData() {
		try {
			 BufferedReader reader = new BufferedReader(new	 FileReader(sysConfigProps.getProperty("testSourceFile")));
//			BufferedReader reader = new BufferedReader(new InputStreamReader(
//					WaitingPrediction.class.getClass().getResourceAsStream(
//							sysConfigProps.getProperty("testSourceFile"))));
			String line = reader.readLine();// 首行是标题，不是实际数据，没有用，此处直接读掉
			while ((line = reader.readLine()) != null) {
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
