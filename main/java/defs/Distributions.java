package defs;

public class Distributions {
	public double[][] topicUser;
	public double[][] decisionUser;
	public double[][] itemTopic;
	public double[][] brandTopic;
	public double[][] itemBrand;
	
	public Distributions(double[][] topicUser, double[][] decisionUser,
			double[][] itemTopic, double[][] brandTopic, double[][] itemBrand) {
		
		this.topicUser = topicUser;
		this.decisionUser = decisionUser;
		this.itemTopic = itemTopic;
		this.brandTopic = brandTopic;
		this.itemBrand = itemBrand;
	}
	
	
}
