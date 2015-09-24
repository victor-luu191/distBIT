package bit;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import utils.Converters;
import utils.Stats;
import cc.mallet.util.Randoms;
import defs.Adoption;
import defs.Brand;
import defs.CountTables;
import defs.Dimensions;
import defs.Item;
import defs.Latent;
import defs.Pair;
import defs.Priors;

public class BrandItemTopicCore {
	
	static Randoms random = new Randoms();
	static DataSet ds;
	
	
	/**
	 * sample new topic for adoption {@code adopt},  update count tables and latent vars accordingly
	 * @param adopt
	 * @param countTables
	 * @param latent
	 * @param dims
	 */
	public static void updateTopic(Adoption adopt, CountTables countTables, Latent latent, 
			Priors priors)  {
		
		int adoptIndex = adopt.index; 
		int userIndex = adopt.userIndex; 
		int itemIndex = adopt.itemIndex;
		
		int cTopic = latent.topics.get(userIndex).get(adoptIndex);
		int cDecision = latent.decisions.get(userIndex).get(adoptIndex);
		int cBrandIndex = latent.brands.get(userIndex).get(adoptIndex);
		
		countTables.decTopicCount(cTopic, userIndex, itemIndex, cBrandIndex, cDecision);
		int nTopic = sampleNewTopic(userIndex, itemIndex, cDecision, cBrandIndex, countTables, priors);
		countTables.incTopicCount(nTopic, userIndex, itemIndex, cBrandIndex, cDecision);
		latent.topics.get(userIndex).set(adoptIndex, nTopic);
	}
	
	public static void updatePair(Adoption adopt, CountTables countTables, Latent latent, 
			Priors priors, Pair[] allPairs, DataSet ds) {
		
		int adoptIndex = adopt.index; 
		int userIndex = adopt.userIndex; 
		int itemIndex = adopt.itemIndex;
		
		int cTopic = latent.topics.get(userIndex).get(adoptIndex);
		int cDecision = latent.decisions.get(userIndex).get(adoptIndex);
		int cBrandIndex = latent.brands.get(userIndex).get(adoptIndex);
		
		Pair cPair = new Pair(cDecision, cBrandIndex);
		countTables.decPairCount(cPair, userIndex, itemIndex, cTopic);
		Pair nPair = sampleNewPair(userIndex, itemIndex, cTopic, countTables, priors, allPairs, ds);
		countTables.incPairCount(nPair, userIndex, itemIndex, cTopic);
		
		int nBrandIndex = nPair.getBrandIndex();
		latent.brands.get(userIndex).set(adoptIndex, nBrandIndex);
		int nDecision = nPair.getDecision();
		latent.decisions.get(userIndex).set(adoptIndex, nDecision);
	}

	private static int sampleNewTopic(int userIndex, int itemIndex, int cDecision, int cBrandIndex, 
										CountTables countTables, Priors priors) {
		
		int numTopic = countTables.dims.numTopic;
		RealVector topicCountsOfUser = Converters.toVector(countTables.topicUser.get(userIndex));
		RealVector posteriorCounts = topicCountsOfUser.mapAdd(priors.theta);
		RealVector weights;
		
		if (cDecision == 0) {// topic-item

			double[] coOccurWithItem = new double[numTopic]; 
			for (int tIndex=0; tIndex < numTopic; tIndex++) {
				coOccurWithItem[tIndex] = coOccurProbWithItem(tIndex, itemIndex, countTables, priors);
			}
			weights = posteriorCounts.ebeMultiply(Converters.arr2Vector(coOccurWithItem));

		} else {// topic-brand-item
			double[] coOccurWithBrand = new double[numTopic];
			for (int tIndex=0; tIndex < numTopic; tIndex++) {
				coOccurWithBrand[tIndex] = coOccurProbWithBrand(tIndex, cBrandIndex, countTables, priors);
			}
			System.out.println("co-occur probs with brand estimated");
			weights = posteriorCounts.ebeMultiply(Converters.arr2Vector(coOccurWithBrand));
		}
		int nTopic = random.nextDiscrete(weights.toArray(), Stats.sum(weights));
		return nTopic;
	}

	private static Pair sampleNewPair(int userIndex, int itemIndex, int cTopic, 
			CountTables countTables, Priors priors, Pair[] allPairs, DataSet ds) {
		
		double[] weights = estWeights(userIndex, itemIndex, cTopic, countTables, priors, ds);
		int index = random.nextDiscrete(weights, Stats.sum(weights));
		
		return allPairs[index];
	}

	private static double[] estWeights(int userIndex, int itemIndex, int cTopic, 
			CountTables countTables,  Priors priors, DataSet ds) {
		
		Dimensions dims = countTables.dims;
		double wTopicBased = estTopicBased(userIndex, itemIndex, cTopic, countTables, priors);
		
		int numBrand = dims.numBrand;
		int numItem = dims.numItem;
		
		double brandBasedCount = countTables.decisionUser.get(1, userIndex);
		double nom = brandBasedCount + priors.beta;
		double sumBrand4Topic = countTables.brandTopic.get(numBrand, cTopic); 		
		double denom = sumBrand4Topic + numBrand * priors.alpha;
		double scalar = nom/denom;
		double[] coOccurWithItem = new double[numBrand];
		RealVector tbCounts = new ArrayRealVector(numBrand);
		
		for (int bIndex = 0; bIndex < numBrand; bIndex++) {
			
			tbCounts.setEntry(bIndex, countTables.brandTopic.get(bIndex, cTopic));
			if (isBrandOfItem(bIndex, itemIndex, ds)) {
				double brandItemCount = countTables.itemBrand.get(itemIndex, bIndex);
				double bNom = brandItemCount + priors.beta;
				double marginCountOfBrand = countTables.itemBrand.get(numItem, bIndex); 	
				double bDenom = marginCountOfBrand + numItem * priors.beta;
				coOccurWithItem[bIndex] = bNom/bDenom;
			}
		}
		RealVector coOccurs = Converters.arr2Vector(coOccurWithItem);
		RealVector wBrandBased = tbCounts.mapAdd(priors.alpha).mapMultiply(scalar).ebeMultiply(coOccurs);
		
		double[] weights = wBrandBased.append(wTopicBased).toArray();
		return weights;
	}
	
	private static boolean isBrandOfItem(int bIndex, int itemIndex, DataSet ds) {
		
		Item item = (Item) ds.itemDict.lookupObject(itemIndex);
		Brand brand = (Brand) ds.brandDict.lookupObject(bIndex);
		
		String brandId = brand.id;
		return item.belongTo(brandId);
	}

	private static double estTopicBased(int userIndex, int itemIndex, int cTopic, 
			CountTables countTables, Priors priors) {
		
		Dimensions dims = countTables.dims;
		double topicItemCount = countTables.itemTopic.get(itemIndex, cTopic);
		double nom = topicItemCount + priors.phi;
		double marginCountOfTopic = countTables.itemTopic.get(dims.numItem, cTopic);	// getSumItem4Topic()[cTopic];
		double denom = marginCountOfTopic + dims.numItem * priors.phi;
		double coOccur = nom/denom;
		double topicBasedCount = countTables.decisionUser.get(0, userIndex);
		
		return coOccur * (topicBasedCount + priors.gamma);
	}

	private static double coOccurProbWithBrand(int topicIndex, int brandIndex, 
			CountTables countTables, Priors priors) {
		
		Dimensions dims = countTables.dims;
		double topicBrandCount = countTables.brandTopic.get(brandIndex, topicIndex);
		double nom = topicBrandCount + priors.alpha;
		double marginCountOfTopic = countTables.brandTopic.get(dims.numBrand, topicIndex);	// getSumBrand4Topic()[topicIndex];
		double denom = marginCountOfTopic + dims.numBrand * priors.alpha;
				
		return nom/denom;
	}

	private static double coOccurProbWithItem(int topicIndex, int itemIndex, 
			CountTables countTables, Priors priors) {
		
		Dimensions dims = countTables.dims;
		double topicItemCount = countTables.itemTopic.get(itemIndex, topicIndex);
		double nom = topicItemCount + priors.phi;
		double marginCountOfTopic = countTables.itemTopic.get(dims.numItem, topicIndex);	// getSumItem4Topic()[topicIndex];
		double denom = marginCountOfTopic + dims.numItem * priors.phi;
		
		return nom/denom;
	}
	
	
}
