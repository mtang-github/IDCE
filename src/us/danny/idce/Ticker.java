package us.danny.idce;

public class Ticker {
	
	private final int max;
	private int tick;
	
	public Ticker(int max) {
		if(max < 0) {
			throw new RuntimeException("ticker max < 0!");
		}
		this.max = max;
		tick = max;
	}
	
	public boolean isTime() {
		--tick;
		if(tick <= 0) {
			tick = max;
			return true;
		}
		return false;
	}
}
