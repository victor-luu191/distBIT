package defs;

public class Dimensions {
	public static final int numDecision = 2;
	public int numUser;
	public int numBrand;
	public int numItem;
	public int numTopic;
	
	public Dimensions() {
		numUser = 0;
		numBrand = 0;
		numItem = 0;
	}

	public Dimensions(int numUser, int numBrand, int numItem, int numTopic) {
		this.numUser = numUser;
		this.numBrand = numBrand;
		this.numItem = numItem;
		this.numTopic = numTopic;
	}

	public Dimensions(int numUser, int numBrand, int numItem) {
		this.numUser = numUser;
		this.numBrand = numBrand;
		this.numItem = numItem;
	}

	@Override
	public String toString() {
		return "Dimensions [numUser=" + numUser + ", numBrand=" + numBrand
				+ ", numItem=" + numItem + ", numTopic=" + numTopic + "]";
	}
}