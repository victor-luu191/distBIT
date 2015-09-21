package bit;

import org.apache.commons.math3.linear.RealVector;

import utils.Converters;
import utils.Stats;
import cc.mallet.util.Randoms;
import defs.Adoption;
import defs.Latent;
import defs.Brand;
import defs.CountTables;
import defs.Dimensions;
import defs.Item;
import defs.Pair;
import defs.Priors;

public class BrandItemTopicCore {
	
	Dimensions dims;
	Priors priors;
	Randoms random;
	CountTables countTables;
	private Latent assigns;
	DataSet ds;
	Pair[] allPairs = buildPairs();
	
	private Pair[] buildPairs() {

		int numBrand = dims.numBrand;
		Pair[] allPairs = new Pair[numBrand + 1];	// all pairs = { (T, b1), ..., (T, bQ), (F,"no brand") }

		for (int bIndex=0; bIndex < numBrand; bIndex++) {

			allPairs[bIndex] = new Pair(true, bIndex);
		}
		int last = allPairs.length - 1;
		allPairs[last] = new Pair(false, -1);	// bIndex = -1 means no actual brand

		return allPairs;
	}
	
	// sample new topic for adoption (@userIndex, @itemIndex, @adoptIndex) and update count tables respectively
	public static void updateTopic(Adoption adopt, CountTables countTables, Latent latent)  {// int cTopic, Adoption adopt, Boolean cDecision, int cBrandIndex
		
		int adoptIndex = adopt.index; 
		int userIndex = adopt.userIndex; 
		int itemIndex = adopt.itemIndex;
		
		int cTopic = latent.topics.get(userIndex).get(adoptIndex);
		int cDecision = latent.decisions.get(userIndex).get(adoptIndex);
		int cBrandIndex = latent.brands.get(userIndex).get(adoptIndex);
		
		countTables.decTopicCount(cTopic, userIndex, itemIndex, cBrandIndex, cDecision);
		int nTopic = sampleNewTopic(userIndex, itemIndex, cDecision, cBrandIndex);
		countTables.incTopicCount(nTopic, userIndex, itemIndex, cBrandIndex, cDecision);
//		assigns.topic.get(userIndex).set(adoptIndex, nTopic);
	}
	
	public void updatePair(Pair cPair, Adoption adopt, int cTopic) {
//		int adoptIndex = adopt.index; 
		int userIndex = adopt.userIndex; 
		int itemIndex = adopt.itemIndex;
		
		countTables.decPairCount(cPair, userIndex, itemIndex, cTopic);
		Pair nPair = sampleNewPair(userIndex, itemIndex, cTopic);
		countTables.incPairCount(nPair, userIndex, itemIndex, cTopic);
		
//		int nBrandIndex = nPair.getBrandIndex();
//		assigns.brand.get(userIndex).set(adoptIndex, nBrandIndex);
//		boolean nDecision = nPair.getDecision();
//		assigns.decision.get(userIndex).set(adoptIndex, nDecision);
	}

	private int sampleNewTopic(int userIndex, int itemIndex, int cDecision, int cBrandIndex) {
		
		RealVector topicCountsOfUser = Converters.toVector(countTables.topicUser.get(userIndex));
		RealVector weights;
		int numTopic = dims.numTopic;
		if (cDecision == 0) {// topic-item

			double[] coOccurWithItem = new double[numTopic]; 
			for (int tIndex=0; tIndex < numTopic; tIndex++) {
				coOccurWithItem[tIndex] = coOccurProbWithItem(tIndex, itemIndex);
			}

			weights = topicCountsOfUser.mapAdd(priors.theta).ebeMultiply(Converters.arr2Vector(coOccurWithItem));

		} else {// topic-brand-item
			double[] coOccurWithBrand = new double[numTopic];
			for (int tIndex=0; tIndex < numTopic; tIndex++) {
				coOccurWithBrand[tIndex] = coOccurProbWithBrand(tIndex, cBrandIndex);
			}

			weights = topicCountsOfUser.mapAdd(priors.theta).ebeMultiply(Converters.arr2Vector(coOccurWithBrand));
		}
		int nTopic = random.nextDiscrete(weights.toArray(), Stats.sum(weights));
		return nTopic;
	}

	private Pair sampleNewPair(int userIndex, int itemIndex, int cTopic) {
		
		double[] weights = estWeights(userIndex, itemIndex, cTopic);
		int index = random.nextDiscrete(weights, Stats.sum(weights));
		
		return allPairs[index];
	}

	private double[] estWeights(int userIndex, int itemIndex, int cTopic) {
		
		double wTopicBased = estTopicBased(userIndex, itemIndex, cTopic);
		
		int numBrand = dims.numBrand;
		int numItem = dims.numItem;
		
		double brandBasedCount = countTables.decisionUser.get(1, userIndex);
		double nom = brandBasedCount + priors.beta;
		double sumBrand4Topic = countTables.brandTopic.get(numBrand, cTopic); 		// getSumBrand4Topic()[cTopic]
		double denom = sumBrand4Topic + numBrand * priors.alpha;
		double scalar = nom/denom;
		double[] coOccurWithItem = new double[numBrand];
		for (int bIndex=0; bIndex < numBrand; bIndex++) {
			if (isBrandOfItem(bIndex, itemIndex)) {
				double brandItemCount = countTables.itemBrand.get(itemIndex, bIndex);
				double bNom = brandItemCount + priors.beta;
				double marginCountOfBrand = countTables.itemBrand.get(numItem, bIndex); 	// getMarginCountOfBrand()[bIndex];
				double bDenom = marginCountOfBrand + numItem * priors.beta;
				coOccurWithItem[bIndex] = bNom/bDenom;
			}
		}
		RealVector tbCounts = Converters.toVector(countTables.brandTopic.get(cTopic));
		RealVector coOccurs = Converters.arr2Vector(coOccurWithItem);
		RealVector wBrandBased = tbCounts.mapAdd(priors.alpha).mapMultiply(scalar).ebeMultiply(coOccurs);
		
		double[] weights = wBrandBased.append(wTopicBased).toArray();
		return weights;
	}
	
	private boolean isBrandOfItem(int bIndex, int itemIndex) {
		
		Item item = (Item) ds.itemDict.lookupObject(itemIndex);
		Brand brand = (Brand) ds.brandDict.lookupObject(bIndex);
		
		return brand.inProducers(item);
	}

	private double estTopicBased(int userIndex, int itemIndex, int cTopic) {
		
		double topicItemCount = countTables.itemTopic.get(itemIndex, cTopic);
		double nom = topicItemCount + priors.phi;
		double marginCountOfTopic = countTables.itemTopic.get(dims.numItem, cTopic);	// getSumItem4Topic()[cTopic];
		double denom = marginCountOfTopic + dims.numItem * priors.phi;
		double coOccur = nom/denom;
		double topicBasedCount = countTables.decisionUser.get(0, userIndex);
		
		return coOccur * (topicBasedCount + priors.gamma);
	}

	private double coOccurProbWithBrand(int topicIndex, int brandIndex) {
		
		double topicBrandCount = countTables.brandTopic.get(brandIndex, topicIndex);
		double nom = topicBrandCount + priors.alpha;
		double marginCountOfTopic = countTables.brandTopic.get(dims.numBrand, topicIndex);	// getSumBrand4Topic()[topicIndex];
		double denom = marginCountOfTopic + dims.numBrand * priors.alpha;
				
		return nom/denom;
	}

	private double coOccurProbWithItem(int topicIndex, int itemIndex) {
		
		double topicItemCount = countTables.itemTopic.get(itemIndex, topicIndex);
		double nom = topicItemCount + priors.phi;
		double marginCountOfTopic = countTables.itemTopic.get(dims.numItem, topicIndex);	// getSumItem4Topic()[topicIndex];
		double denom = marginCountOfTopic + dims.numItem * priors.phi;
		
		return nom/denom;
	}
	
	
}
