package defs;

import org.petuum.jbosen.table.DoubleTable;

public class CountTables {
	
	Dimensions dims;
	
	public DoubleTable topicUser; 
	public DoubleTable decisionUser;
	public DoubleTable itemTopic; 
	public DoubleTable brandTopic; 
	public DoubleTable itemBrand;
	
	private double[] marginCountOfBrand;
	private double[] sumItem4Topic;
	private double[] sumBrand4Topic;
	
	
	public void decTopicCount(int cTopic, int userIndex, int itemIndex, int cBrandIndex, Boolean cDecision) {
		
		topicUser.inc(cTopic, userIndex, -1);
		topicUser.inc(dims.numTopic, userIndex, -1);
		
		if (cDecision == false) {// topic-item
			itemTopic.inc(itemIndex, cTopic,  -1);
			itemTopic.inc(dims.numItem, cTopic, -1);
		} else {// topic-brand-item
			brandTopic.inc(cBrandIndex, cTopic, -1);
			brandTopic.inc(dims.numBrand, cTopic, -1);
		}
		
	}
	
	public void incTopicCount(int nTopic, int userIndex, int itemIndex, int cBrandIndex, Boolean cDecision) {
		
		topicUser.inc(nTopic, userIndex,  1);
		topicUser.inc(dims.numTopic, userIndex, 1);
		
		if (cDecision == false) {// topic-item
			itemTopic.inc(itemIndex, nTopic,  1);
			itemTopic.inc(dims.numItem, nTopic, 1);
			
		} else {// topic-brand-item
			brandTopic.inc(cBrandIndex, nTopic,  1);
			brandTopic.inc(dims.numBrand, nTopic, 1);
		}
	}
	
	public void decPairCount(Pair cPair, int userIndex, int itemIndex, int cTopic) {
		
		decisionUser.inc(2, userIndex, -1);	// decrease marginal decision counts
		if (cPair.getDecision() == false) {// currently no brand is used
			decisionUser.inc(0, userIndex,  -1);
			itemTopic.inc(cTopic, itemIndex, -1);
			sumItem4Topic[cTopic]--;
		} 
		else {//currently brand is used
			decisionUser.inc(1, userIndex,  -1);
			int cBrandIndex = cPair.getBrandIndex();
			brandTopic.inc(cTopic, cBrandIndex, -1);
			sumBrand4Topic[cTopic]--;
			itemBrand.inc(cBrandIndex, itemIndex, -1);
			marginCountOfBrand[cBrandIndex]--;
		}
	}
	
	public void incPairCount(Pair nPair, int userIndex, int itemIndex, int cTopic) {

		decisionUser.inc(2, userIndex, 1);
		if (nPair.getDecision() == false) {
			decisionUser.inc(userIndex, 0, 1);
			
			itemTopic.inc(cTopic, itemIndex, 1);
			sumItem4Topic[cTopic]++;
		} else {
			decisionUser.inc(userIndex, 1, 1);
			int nBrandIndex = nPair.getBrandIndex();
			brandTopic.inc(cTopic, nBrandIndex, 1);
			sumBrand4Topic[cTopic]++;
			itemBrand.inc(nBrandIndex, itemIndex, 1);
			marginCountOfBrand[nBrandIndex]++;
			
		}
	}
}
