package defs;

import org.petuum.jbosen.table.DoubleTable;

import utils.Converters;
import utils.Stats;

public class CountTables {
	
	private static final int userTopicTableId = 0;
	private static final int userDecisionTableId = 1;
	private static final int topicItemTableId = 2;
	private static final int topicBrandTableId = 3;
	private static final int brandItemTableId = 4;
	
	private DoubleTable userTopic; 
	private DoubleTable userDecision;
	private DoubleTable topicItem; 
	private DoubleTable topicBrand; 
	private DoubleTable brandItem;
	// margin counts
	double[] marginCountOfUser;
	double[] marginCountOfBrand;
	double[] sumItem4Topic;
	double[] sumBrand4Topic;
	
	public CountTables(double[][] user_topic, double[][] user_decision,
						double[][] topic_item, double[][] topic_brand, double[][] brand_item) {
		super();
		this.userTopic = Converters.toTable(user_topic, userTopicTableId);
		this.userDecision = Converters.toTable(user_decision, userDecisionTableId);
		this.topicItem = Converters.toTable(topic_item, topicItemTableId);
		this.topicBrand = Converters.toTable(topic_brand, topicBrandTableId);
		this.brandItem = Converters.toTable(brand_item, brandItemTableId);
		
		marginCountOfUser = initMarginCounts(user_topic);
		sumItem4Topic = initMarginCounts(topic_item);
		sumBrand4Topic = initMarginCounts(topic_brand);
		marginCountOfBrand = initMarginCounts(brand_item);
	}

	private double[] initMarginCounts(double[][] countTable) {
		int numRow = countTable.length;
		double[] marginCounts = new double[numRow];
		// TODO
		for (int i = 0; i < marginCounts.length; i++) {
			marginCounts[i] = Stats.sum(countTable[i]);
		}
		
		return marginCounts;
	}
	
	public DoubleTable getUserTopic() {
		return userTopic;
	}

	public DoubleTable getUserDecision() {
		return userDecision;
	}

	public DoubleTable getTopicItem() {
		return topicItem;
	}

	public DoubleTable getTopicBrand() {
		return topicBrand;
	}

	public DoubleTable getBrandItem() {
		return brandItem;
	}

	public double[] getMarginCountOfUser() {
		return marginCountOfUser;
	}

	public double[] getMarginCountOfBrand() {
		return marginCountOfBrand;
	}

	public double[] getSumItem4Topic() {
		return sumItem4Topic;
	}

	public double[] getSumBrand4Topic() {
		return sumBrand4Topic;
	}

	public void decTopicCount(int cTopic, int userIndex, int itemIndex, int cBrandIndex, Boolean cDecision) {
		
		userTopic.inc(userIndex, cTopic, -1);
		marginCountOfUser[userIndex]--;
		
		if (cDecision == false) {// topic-item
			topicItem.inc(cTopic, itemIndex, -1);
			sumItem4Topic[cTopic]-- ;
		} else {// topic-brand-item
			topicBrand.inc(cTopic, cBrandIndex, -1);
			sumBrand4Topic[cTopic]--;
		}
		
	}
	
	public void incTopicCount(int nTopic, int userIndex, int itemIndex, int cBrandIndex, Boolean cDecision) {
		
		userTopic.inc(userIndex, nTopic, 1);
		marginCountOfUser[userIndex]++;
		if (cDecision == false) {// topic-item
			topicItem.inc(nTopic, itemIndex, 1);
			sumItem4Topic[nTopic]++;
			
		} else {// topic-brand-item
			topicBrand.inc(nTopic, cBrandIndex, 1);
			sumBrand4Topic[nTopic]++;
		}
	}
	
	public void decPairCount(Pair cPair, int userIndex, int itemIndex, int cTopic) {
		
		marginCountOfUser[userIndex]--;
		if (cPair.getDecision() == false) {// currently no brand is used
			userDecision.inc(userIndex, 0, -1);
			topicItem.inc(cTopic, itemIndex, -1);
			sumItem4Topic[cTopic]--;
		} 
		else {//currently brand is used
			userDecision.inc(userIndex, 1, -1);
			int cBrandIndex = cPair.getBrandIndex();
			topicBrand.inc(cTopic, cBrandIndex, -1);
			sumBrand4Topic[cTopic]--;
			brandItem.inc(cBrandIndex, itemIndex, -1);
			marginCountOfBrand[cBrandIndex]--;
		}
	}
	
	public void incPairCount(Pair nPair, int userIndex, int itemIndex, int cTopic) {

		marginCountOfUser[userIndex]++;
		if (nPair.getDecision() == false) {
			userDecision.inc(userIndex, 0, 1);
			
			topicItem.inc(cTopic, itemIndex, 1);
			sumItem4Topic[cTopic]++;
		} else {
			userDecision.inc(userIndex, 1, 1);
			int nBrandIndex = nPair.getBrandIndex();
			topicBrand.inc(cTopic, nBrandIndex, 1);
			sumBrand4Topic[cTopic]++;
			brandItem.inc(nBrandIndex, itemIndex, 1);
			marginCountOfBrand[nBrandIndex]++;
			
		}
	}
}
