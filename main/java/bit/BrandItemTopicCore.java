package bit;

import org.apache.commons.math3.linear.RealVector;
import org.petuum.jbosen.table.IntTable;

import utils.Converters;
import utils.Stats;
import cc.mallet.util.Randoms;
import defs.Adoption;
import defs.Assignments;
import defs.Dimensions;
import defs.CountTables;
import defs.HyperParams;
import defs.Pair;

public class BrandItemTopicCore {
	
	Dimensions dims;
	HyperParams hyperParams;
	Randoms random;
	CountTables countTables;
	Assignments assigns;
	
	// sample new topic for adoption (@userIndex, @itemIndex, @adoptIndex) and update count tables respectively
	public void updateTopic(int cTopic, Adoption adopt, Boolean cDecision, int cBrandIndex)  {
		
		int adoptIndex = adopt.index; 
		int userIndex = adopt.userIndex; 
		int itemIndex = adopt.itemIndex;
		
		countTables.decTopicCount(cTopic, userIndex, itemIndex, cBrandIndex, cDecision);
		int nTopic = sampleNewTopic(userIndex, itemIndex, cDecision, cBrandIndex);
		countTables.incTopicCount(nTopic, userIndex, itemIndex, cBrandIndex, cDecision);
		assigns.topic.get(userIndex).set(adoptIndex, nTopic);
	}

	private int sampleNewTopic(int userIndex, int itemIndex, Boolean cDecision, int cBrandIndex) {
		
		RealVector topicCountsOfUser = Converters.toVector(countTables.getUserTopic().get(userIndex));
		RealVector weights;
		int numTopic = dims.getNumTopic();
		if (cDecision == false) {// topic-item

			double[] coOccurWithItem = new double[numTopic]; 
			for (int tIndex=0; tIndex < numTopic; tIndex++) {
				coOccurWithItem[tIndex] = coOccurProbWithItem(tIndex, itemIndex);
			}

			weights = topicCountsOfUser.mapAdd(hyperParams.theta).ebeMultiply(Converters.arr2Vector(coOccurWithItem));

		} else {// topic-brand-item
			double[] coOccurWithBrand = new double[numTopic];
			for (int tIndex=0; tIndex < numTopic; tIndex++) {
				coOccurWithBrand[tIndex] = coOccurProbWithBrand(tIndex, cBrandIndex);
			}

			weights = topicCountsOfUser.mapAdd(hyperParams.theta).ebeMultiply(Converters.arr2Vector(coOccurWithBrand));
		}
		int nTopic = random.nextDiscrete(weights.toArray(), Stats.sum(weights));
		return nTopic;
	}

	public void updatePair(Pair cPair, Adoption adopt, int cTopic) {
		int adoptIndex = adopt.index; 
		int userIndex = adopt.userIndex; 
		int itemIndex = adopt.itemIndex;
		
		countTables.decPairCount(cPair, userIndex, itemIndex, cTopic);
		Pair nPair = sampleNewPair(userIndex, itemIndex, cTopic);
		countTables.incPairCount(nPair, userIndex, itemIndex, cTopic);
		
		int nBrandIndex = nPair.getBrandIndex();
		assigns.brand.get(userIndex).set(adoptIndex, nBrandIndex);
		boolean nDecision = nPair.getDecision();
		assigns.decision.get(userIndex).set(adoptIndex, nDecision);
	}

	private Pair sampleNewPair(int userIndex, int itemIndex, int cTopic) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private double coOccurProbWithBrand(int tIndex, int brandIndex) {
		// TODO Auto-generated method stub
		return 0;
	}

	private double coOccurProbWithItem(int tIndex, int itemIndex) {
		// TODO Auto-generated method stub
		return 0;
	}
}
