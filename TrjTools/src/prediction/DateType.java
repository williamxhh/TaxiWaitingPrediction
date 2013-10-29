package prediction;
public enum DateType {
	AnyDay(0),Sunday(1), Monday(2), Tuesday(3), Wednesday(4), Thursday(5), Friday(6), Saturday(
			7),RestDay(8),WorkDay(9), Holiday(10);

	DateType(int val) {
		this.value = val;
	}
	private Integer value;

	public Integer getValue() {
		return value;
	}
}