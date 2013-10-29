package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
/**
 * 此文件用来筛选一定量的出租车GPS数据，用来做出租车与路网数据的经纬度校准，生成csv文件，然后用excel转成xlsx文件，就可以被
 * ArcGIS识别了
 * 
 * @author dell
 *
 */
public class GPSFilter {
	public static void main(String[] args) throws Exception {
		String basedir = "D:\\data\\BigDataContest_TrainData\\1\\20121101\\txt\\";
		/**center point 1**/
		double center_lon = 116.469068;
		double center_lat = 39.9716;
		/**center point 2**/
//		double center_lon = 116.392;
//		double center_lat = 39.907;
		/**center point 3**/
//		double center_lon = 116.295;
//		double center_lat = 39.889;
		
		double maxRange = 0.01;
		
		double lon_fix = 0.006125;
//		double lat_fix = 0.001275;
		double lat_fix = 0.00133;
		/** print the origin data point **/
//		PrintWriter pw = new PrintWriter(new FileWriter("d:\\GPSresult.csv"));
		/** print the modified data point **/
		PrintWriter pwf = new PrintWriter(new FileWriter("d:\\GPSresult_fix.csv"));
		

		File[] fs = new File(basedir).listFiles();
		for (File file : fs) {
			BufferedReader fr = new BufferedReader(new FileReader(file));
			String line = "";
			while ((line = fr.readLine()) != null) {
				String[] sb = line.split(",");
				if (sb.length == 9) {
					double lon = Double.parseDouble(sb[4]);
					double lat = Double.parseDouble(sb[5]);

					if (Math.abs(lon - center_lon) <= maxRange
							&& Math.abs(lat - center_lat) <= maxRange) {
						double newlon = lon+lon_fix;
						double newlat = lat+lat_fix;
						pwf.write(newlon+ "," + newlat + "\r\n");
					}
				}
			}
			fr.close();
		}
		pwf.close();
		System.out.println("Done");
	}
}
