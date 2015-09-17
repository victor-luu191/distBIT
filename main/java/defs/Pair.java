package defs;

public class Pair {
	private boolean decision;
	private int brandIndex;
	
	public Pair(boolean d, int bIndex) {
		decision = d;
		brandIndex = bIndex;
	}
	public boolean getDecision() {
		return decision;
	}
	public int getBrandIndex() {
		return brandIndex;
	}
	
	
}
