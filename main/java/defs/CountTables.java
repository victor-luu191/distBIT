package defs;

import org.petuum.jbosen.table.DoubleTable;

import utils.Converters;
import utils.Stats;

public class CountTables {
	
	public DoubleTable userTopic; 
	public DoubleTable userDecision;
	public DoubleTable topicItem; 
	public DoubleTable topicBrand; 
	public DoubleTable brandItem;
	// margin counts
	public double[] marginCountOfUser;
	public double[] marginCountOfBrand;
	public double[] sumItem4Topic;
	public double[] sumBrand4Topic;

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
