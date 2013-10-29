package index.prediction;

import java.util.List;

public class IndexRecord {
	private String stat_fileName;
	private int line_from;
	private int line_to;
	private List<StatisticsRecord> stats;
	
	public IndexRecord(String filename,int from,int to,List<StatisticsRecord> statistics){
		this.stat_fileName = filename;
		this.line_from = from;
		this.line_to = to;
		this.stats = statistics;
	}
	
	public String getStat_fileName() {
		return stat_fileName;
	}
	public void setStat_fileName(String stat_fileName) {
		this.stat_fileName = stat_fileName;
	}
	public int getLine_from() {
		return line_from;
	}
	public void setLine_from(int line_from) {
		this.line_from = line_from;
	}
	public int getLine_to() {
		return line_to;
	}
	public void setLine_to(int line_to) {
		this.line_to = line_to;
	}
	public List<StatisticsRecord> getStats() {
		return stats;
	}
	public void setStats(List<StatisticsRecord> stats) {
		this.stats = stats;
	}
	
	
}
