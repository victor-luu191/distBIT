package defs;

import org.petuum.jbosen.PsTableGroup;
import org.petuum.jbosen.table.DoubleTable;

public class CountTables {
	
	public Dimensions dims;
	
	public DoubleTable topicUser; 
	public DoubleTable decisionUser;
	public DoubleTable itemTopic; 
	public DoubleTable brandTopic; 
	public DoubleTable itemBrand;
	
	public CountTables(TableIds tableIds, Dimensions dims, int staleness) {
		
		this.dims = dims;
		// Config count tables
		PsTableGroup.createDenseDoubleTable(tableIds.topicUserId, staleness, dims.numTopic);
		PsTableGroup.createDenseDoubleTable(tableIds.decisionUserId, staleness, Dimensions.numDecision);
		PsTableGroup.createDenseDoubleTable(tableIds.itemTopicId, staleness, dims.numItem);
		PsTableGroup.createDenseDoubleTable(tableIds.brandTopicId, staleness, dims.numBrand);
		PsTableGroup.createDenseDoubleTable(tableIds.itemBrandId, staleness, dims.numItem);
		
	}

	public void decTopicCount(int cTopic, int userIndex, int itemIndex, int cBrandIndex, int cDecision) {
		
		topicUser.inc(cTopic, userIndex, -1);
		topicUser.inc(dims.numTopic, userIndex, -1);
		
		if (cDecision == 0) {// topic-item
			itemTopic.inc(itemIndex, cTopic,  -1);
			itemTopic.inc(dims.numItem, cTopic, -1);
		} else {// topic-brand-item
			brandTopic.inc(cBrandIndex, cTopic, -1);
			brandTopic.inc(dims.numBrand, cTopic, -1);
		}
		
	}
	
	public void incTopicCount(int nTopic, int userIndex, int itemIndex, int cBrandIndex, int cDecision) {
		
		topicUser.inc(nTopic, userIndex,  1);
		topicUser.inc(dims.numTopic, userIndex, 1);
		
		if (cDecision == 0) {// topic-item
			itemTopic.inc(itemIndex, nTopic,  1);
			itemTopic.inc(dims.numItem, nTopic, 1);
			
		} else {// topic-brand-item
			brandTopic.inc(cBrandIndex, nTopic,  1);
			brandTopic.inc(dims.numBrand, nTopic, 1);
		}
	}
	
	public void decPairCount(Pair cPair, int userIndex, int itemIndex, int cTopic) {
		
		if (cPair.getDecision() == 0) {// currently no brand is used
			decisionUser.inc(0, userIndex,  -1);
			decisionUser.inc(2, userIndex, -1);	// decrease marginal decision counts
			
			itemTopic.inc(itemIndex, cTopic,  -1);
			itemTopic.inc(dims.numItem, cTopic, -1);
		} 
		else {//currently brand is used
			decisionUser.inc(1, userIndex,  -1);
			decisionUser.inc(2, userIndex, -1);	// decrease marginal decision counts
			
			int cBrandIndex = cPair.getBrandIndex();
			brandTopic.inc(cBrandIndex, cTopic,  -1);
			brandTopic.inc(dims.numBrand, cTopic, -1);
			
			itemBrand.inc(itemIndex, cBrandIndex,  -1);
			itemBrand.inc(dims.numItem, cBrandIndex, -1);
		}
	}
	
	public void incPairCount(Pair nPair, int userIndex, int itemIndex, int cTopic) {

		
		if (nPair.getDecision() == 0) {
			decisionUser.inc(0, userIndex,  1);
			decisionUser.inc(2, userIndex, 1);	// increment marginal decision counts
			
			itemTopic.inc(itemIndex, cTopic,  1);
			itemTopic.inc(dims.numItem, cTopic, 1);
		} else {
			decisionUser.inc(1, userIndex,  1);
			decisionUser.inc(2, userIndex, 1);	// increment marginal decision counts
			
			int nBrandIndex = nPair.getBrandIndex();
			brandTopic.inc(nBrandIndex, cTopic,  1);
			brandTopic.inc(dims.numBrand, cTopic, 1);
			
			itemBrand.inc(itemIndex, nBrandIndex,  1);
			itemBrand.inc(dims.numItem, nBrandIndex, 1);
			
		}
	}

	public Distributions toDistributions() {
		
		double[][] topicUserProbs = toProbs(topicUser, dims.numTopic, dims.numUser, );
		double[][] decisionUserProbs = toProbs(decisionUser, Dimensions.numDecision, dims.numUser);
		double[][] itemTopicProbs = toProbs(itemTopic, dims.numItem, dims.numTopic);
		double[][] brandTopicProbs = toProbs(brandTopic, dims.numBrand, dims.numTopic);
		double[][] itemBrandProbs = toProbs(itemBrand, dims.numItem, dims.numBrand);
		
		return new Distributions(topicUserProbs, decisionUserProbs, itemTopicProbs, brandTopicProbs, itemBrandProbs);
	}

	private double[][] toProbs(DoubleTable counts, int numRow, int numCol, double prior) {
		// TODO Auto-generated method stub
		double[][] probs = new double[numRow][numCol];
		for (int i = 0; i < numCol; i++) {
			double marginCount = counts.get(numRow, i);
			for (int j = 0; j < numRow; j++) {
				
			}
		}
		
		return probs;
	}
}
