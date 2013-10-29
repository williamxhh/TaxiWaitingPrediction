package index.prediction;

import prediction.DateType;
import prediction.WaitingPrediction;

public class TimeSegment implements Comparable<TimeSegment> {

	public TimeSegment(int index, int dateType) {
		this.index = index;
		this.dateType = DateType.values()[dateType];
	}

	private Integer index = 0;
	private DateType dateType = DateType.AnyDay;

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public DateType getDateType() {
		return dateType;
	}

	public void setDateType(DateType dateType) {
		this.dateType = dateType;
	}

	@Override
	public int compareTo(TimeSegment o) {
		return (dateType.getValue() << 5 + index) - o.hashCode();
	}

	@Override
	public int hashCode() {
		return dateType.getValue() << 5 + index;
	}

	@Override
	public boolean equals(Object seg) {
		if (seg != null && seg instanceof TimeSegment) {
			TimeSegment tseg = (TimeSegment) seg;
			if (this.index.equals(tseg.index)
					&& this.dateType.equals(tseg.dateType))
				return true;
		}
		return false;
	}
}
