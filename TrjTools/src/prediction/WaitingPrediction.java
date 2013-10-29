package prediction;

import index.prediction.IndexRecord;
import index.prediction.StatisticsRecord;
import index.prediction.TimeSegment;

import java.io.BufferedReader;
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
	static String EIDX_FILE = "D:\\data\\mergedData\\StatisticResult\\taxiPredict.eidx";
	static String STAT_FILEPATH = "D:\\data\\mergedData\\StatisticResult\\";
	static String PREDICTION_PATH = "D:\\data\\prediction_result.txt";
	static Properties sysConfigProps = new Properties();
	static List<String> dateTimeList = new ArrayList<String>();
	static List<PredictResult> predictResult = new ArrayList<PredictResult>();
	static Map<Long, Map<Integer, IndexRecord>> CACHED_INDEX = new HashMap<Long, Map<Integer, IndexRecord>>();

	public static void main(String[] args) throws Exception {
//		Calendar cal = Calendar.getInstance();
		loadProperties();
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
//		System.out.println((Calendar.getInstance().getTimeInMillis()-cal.getTimeInMillis())/1000);
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

	public static void readIndex() {
		try {
			BufferedReader reader = new BufferedReader(
					new FileReader(EIDX_FILE));
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

	public static void readIndex(long edgeId, TimeSegment userTime) {
		try {
			IndexRecord record = CACHED_INDEX.get(edgeId).get(
					userTime.getDateType().getValue());
			RandomAccessFile file = new RandomAccessFile(STAT_FILEPATH
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

	private static void loadProperties() {
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

	static void loadUserData() {
		try {
			// BufferedReader reader = new BufferedReader(new
			// FileReader(sysConfigProps.getProperty("testSourceFile")));
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					WaitingPrediction.class.getClass().getResourceAsStream(
							sysConfigProps.getProperty("testSourceFile"))));
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
