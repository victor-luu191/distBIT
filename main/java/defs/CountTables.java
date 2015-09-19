package defs;

import org.petuum.jbosen.table.DoubleTable;

import utils.Converters;
import utils.Stats;

public class CountTables {
	
	public DoubleTable topicUser; 
	public DoubleTable decisionUser;
	public DoubleTable itemTopic; 
	public DoubleTable brandTopic; 
	public DoubleTable itemBrand;
	// margin counts
	public double[] marginCountOfUser;
	public double[] marginCountOfBrand;
	public double[] sumItem4Topic;
	public double[] sumBrand4Topic;

	public void decTopicCount(int cTopic, int userIndex, int itemIndex, int cBrandIndex, Boolean cDecision) {
		
		topicUser.inc(userIndex, cTopic, -1);
		marginCountOfUser[userIndex]--;
		
		if (cDecision == false) {// topic-item
			itemTopic.inc(cTopic, itemIndex, -1);
			sumItem4Topic[cTopic]-- ;
		} else {// topic-brand-item
			brandTopic.inc(cTopic, cBrandIndex, -1);
			sumBrand4Topic[cTopic]--;
		}
		
	}
	
	public void incTopicCount(int nTopic, int userIndex, int itemIndex, int cBrandIndex, Boolean cDecision) {
		
		topicUser.inc(userIndex, nTopic, 1);
		marginCountOfUser[userIndex]++;
		if (cDecision == false) {// topic-item
			itemTopic.inc(nTopic, itemIndex, 1);
			sumItem4Topic[nTopic]++;
			
		} else {// topic-brand-item
			brandTopic.inc(nTopic, cBrandIndex, 1);
			sumBrand4Topic[nTopic]++;
		}
	}
	
	public void decPairCount(Pair cPair, int userIndex, int itemIndex, int cTopic) {
		
		marginCountOfUser[userIndex]--;
		if (cPair.getDecision() == false) {// currently no brand is used
			decisionUser.inc(userIndex, 0, -1);
			itemTopic.inc(cTopic, itemIndex, -1);
			sumItem4Topic[cTopic]--;
		} 
		else {//currently brand is used
			decisionUser.inc(userIndex, 1, -1);
			int cBrandIndex = cPair.getBrandIndex();
			brandTopic.inc(cTopic, cBrandIndex, -1);
			sumBrand4Topic[cTopic]--;
			itemBrand.inc(cBrandIndex, itemIndex, -1);
			marginCountOfBrand[cBrandIndex]--;
		}
	}
	
	public void incPairCount(Pair nPair, int userIndex, int itemIndex, int cTopic) {

		marginCountOfUser[userIndex]++;
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
