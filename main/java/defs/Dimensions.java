package defs;

public class Dimensions {
	private int numUser;
	private int numBrand;
	private int numItem;
	private int numTopic;
	
	public Dimensions(int numUser, int numBrand, int numItem, int numTopic) {
		super();
		this.numUser = numUser;
		this.numBrand = numBrand;
		this.numItem = numItem;
		this.numTopic = numTopic;
	}

	public int getNumTopic() {
		return numTopic;
	}

	public void setNumTopic(int numTopic) {
		this.numTopic = numTopic;
	}

	public int getNumUser() {
		return numUser;
	}

	public void setNumUser(int numUser) {
		this.numUser = numUser;
	}

	public int getNumBrand() {
		return numBrand;
	}

	public void setNumBrand(int numBrand) {
		this.numBrand = numBrand;
	}

	public int getNumItem() {
		return numItem;
	}

	public void setNumItem(int numItem) {
		this.numItem = numItem;
	}

	@Override
	public String toString() {
		return "Dimensions [numUser=" + numUser + ", numBrand=" + numBrand
				+ ", numItem=" + numItem + ", numTopic=" + numTopic + "]";
	}
}
