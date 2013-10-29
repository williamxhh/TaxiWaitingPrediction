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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import match.MatchResult;
import match.UserLocationMatch;

public class WaitingPrediction {
	private static final Logger LOG = Logger.getLogger(WaitingPrediction.class);
	private static final int MAX_WAITING_MIN = 15;
	public static final String PROPS_FILENAME = "WaitingPrediction.properties";
	static Properties sysConfigProps = new Properties();
	static List<String> dateTimeList = new ArrayList<String>();
	static List<PredictResult> predictResult = new ArrayList<PredictResult>();
	static Map<Long, Map<Integer, IndexRecord>> CACHED_INDEX = new HashMap<Long, Map<Integer, IndexRecord>>();

	
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
			readIndex();

			Long edgeId = 0l;
			TimeSegment userTime = null;
			for (int i = 0; i < mrList.size(); i++) {
				edgeId = mrList.get(i).get(0).getEdge().getId();
				userTime = parseToTimeSegment(parseDateTime(dateTimeList.get(i)));
				predictResult.add(predict(edgeId, userTime));
			}

			for (PredictResult r : predictResult) {
				System.out.println(r);
			}
			System.out.println((Calendar.getInstance().getTimeInMillis()-cal.getTimeInMillis())/1000);
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
	 * 将eidx文件扫一遍，将其中出现了的索引都建在内存中，以后从stat文件中读取过数据就可以直接缓存在内存中读取   缓存到全局map  CACHED_INDEX中
	 */
	public static void readIndex() {
		try {
			BufferedReader reader = new BufferedReader(
					new FileReader(sysConfigProps.getProperty("eidxFile")));
			String line = "";
			while ((line = reader.readLine()) != null) {
				String[] splits = line.split(",");
				Long edgeId = new Long(Long.parseLong(splits[0]));
				Integer datetype = new Integer(Integer.parseInt(splits[1]));
				if (CACHED_INDEX.containsKey(edgeId)) {
					Map<Integer, IndexRecord> record = CACHED_INDEX.get(edgeId);
					record.put(
							datetype,
							new IndexRecord(splits[2], Integer
									.parseInt(splits[3]), Integer
									.parseInt(splits[4]),
									new ArrayList<StatisticsRecord>()));
				} else {
					Map<Integer, IndexRecord> record = new TreeMap<Integer, IndexRecord>();
					record.put(
							datetype,
							new IndexRecord(splits[2], Integer
									.parseInt(splits[3]), Integer
									.parseInt(splits[4]),
									new ArrayList<StatisticsRecord>()));
					CACHED_INDEX.put(edgeId, record);
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 根据指定好的edgeid和datetype以及时间片index来读取stat文件，查询相应的edge流量，空载率，平均行驶速度，加载到CACHED_INDEX中
	 * @param edgeId
	 * @param userTime
	 */
	public static void readIndex(long edgeId, TimeSegment userTime) {
		try {
			IndexRecord record = CACHED_INDEX.get(edgeId).get(
					userTime.getDateType().getValue());
			RandomAccessFile file = new RandomAccessFile(sysConfigProps.getProperty("statFilePath")
					+ record.getStat_fileName(), "r");
			file.seek(record.getLine_from());
			String line = "";
			while ((line = file.readLine()) != null
					&& file.getFilePointer() < record.getLine_to()) {
				String[] splits = line.split(",");
				record.getStats().add(
						new StatisticsRecord(Long.parseLong(splits[0]),
								DateType.values()[Integer.parseInt(splits[1])],
								Integer.parseInt(splits[2]), Double
										.parseDouble(splits[3]), Double
										.parseDouble(splits[4]), Double
										.parseDouble(splits[5])));
			}
			file.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
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

		for (int i = 0; i < MAX_WAITING_MIN; i++) {
			emptyRatioList.add(0.0);
		}

		if (CACHED_INDEX.get(edgeId).get(userTime.getDateType().getValue()) != null) {

			if (CACHED_INDEX.get(edgeId).get(userTime.getDateType().getValue())
					.getStats().size()==0) {
				readIndex(edgeId, userTime);
			}
			List<StatisticsRecord> stats = CACHED_INDEX.get(edgeId)
					.get(userTime.getDateType().getValue()).getStats();
			for (StatisticsRecord r : stats) {
				int time_delta = r.getDailytime_index() - userTime.getIndex();
				if (time_delta >= 0 && time_delta < MAX_WAITING_MIN) {
					emptyRatioList.set(time_delta, r.getAvrEmptyRatio());
				}else if(time_delta >= MAX_WAITING_MIN){
					break;
				}
			}
			/**
			 *    v1.0 版本的计算策略     
			 *    	3分钟内打到车的概率是一个条件概率和，第一分钟打到车，第一分钟没打到第二分钟打到，第一二分钟没有打到，第三分钟打到
			 *    	预估等待时间对空载率求积分，累计积分超过1的时候，认为必然打到车了   现在实际上认为空载率在一分钟是恒定的，所以求积分的时候直接累加空载率
			 */
			result.probability = emptyRatioList.get(0)
					+ (1 - emptyRatioList.get(0)) * emptyRatioList.get(1)
					+ (1 - emptyRatioList.get(0)) * (1 - emptyRatioList.get(1))
					* emptyRatioList.get(2);
			int waitingMin = 0;
			Double sum = 0.0;
			for (; waitingMin < MAX_WAITING_MIN; waitingMin++) {
				sum += emptyRatioList.get(waitingMin);
				if (sum >= 1)
					break;
			}
			result.estimatedWaitingTime = waitingMin + 1;
		}
		if (result.estimatedWaitingTime >= MAX_WAITING_MIN)
			result.estimatedWaitingTime = MAX_WAITING_MIN;
		return result;
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
