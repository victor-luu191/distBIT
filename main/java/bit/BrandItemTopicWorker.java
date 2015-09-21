package bit;

import java.util.ArrayList;
import java.util.Random;

import org.petuum.jbosen.PsTableGroup;
import org.petuum.jbosen.table.IntTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import defs.CountTables;
import defs.Dimensions;
import defs.Instance;
import defs.Latent;

public class BrandItemTopicWorker implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(BrandItemTopicWorker.class);

    private int numWorkers;
    private int numThreads;
    private int workerRank;
    
    private DataSet trainSet;
    private Dimensions dims;
    
    CountTables countTables;
	private int topicUserTableId;
	private int decisionUserTableId;
	private int itemTopicTableId;
	private int brandTopicTableId;
	private int itemBrandTableId;

	Latent latent;
	private int topicTableId;
	private int brandTableId;
	private int decisionTableId;
	
	private final int  numDecision = 2;
	Random random;

	public void run() {
		// TODO 
		countTables.topicUser = PsTableGroup.getDoubleTable(topicUserTableId);
		countTables.decisionUser = PsTableGroup.getDoubleTable(decisionUserTableId);
		countTables.itemTopic = PsTableGroup.getDoubleTable(itemTopicTableId);
		countTables.brandTopic = PsTableGroup.getDoubleTable(brandTopicTableId);
		countTables.itemBrand = PsTableGroup.getDoubleTable(itemBrandTableId);
		
		latent.topics = PsTableGroup.getIntTable(topicTableId);
		latent.brands = PsTableGroup.getIntTable(brandTableId);
		latent.decisions = PsTableGroup.getIntTable(decisionTableId);
		
		int numUserPerWorker = dims.numUser/numWorkers;
		int userBegin = workerRank * numUserPerWorker;
		int userEnd = (workerRank == numWorkers - 1)? dims.numUser : (userBegin + numUserPerWorker);
		
		// Since each thread initialize part of count tables, use barrier to
        // ensure initialization completes.
		initTables(countTables, latent, userBegin, userEnd);
		PsTableGroup.globalBarrier();
		
		
	}
	/**
	 * Initialize a partition of count tables, this partition corresponds to users from {@code userBegin} 
	 * to {@code userEnd}.
	 * @param countTables
	 * @param latent 
	 * @param userBegin
	 * @param userEnd
	 */
	private void initTables(CountTables countTables, Latent latent, int userBegin, int userEnd) {
		
		for (int uIndex = userBegin; uIndex < userEnd; uIndex++) {
			initValues(countTables, latent, uIndex);
		}
	}

	/**
	 * Initialize counts for the user {@code uIndex} using uniform distributions
	 * put the initial counts into count tables
	 * @param uIndex
	 * @param countTables
	 * @param latent 
	 */
	private void initValues(CountTables countTables, Latent latent, int uIndex) {
		
		Instance instance = trainSet.instances.get(uIndex);
		ArrayList<String> adoptions = instance.getItemIds();
		// Add margin counts to tables topicUser and decisionUser 
		// both equal to number of adopts of the user
		countTables.topicUser.inc(dims.numTopic, uIndex, adoptions.size());
		countTables.decisionUser.inc(numDecision, uIndex, adoptions.size());
		
		for (int i=0; i < adoptions.size(); i++) {
			int itemIndex = trainSet.itemDict.lookupIndex(adoptions.get(i));
			int topicIndex = random.nextInt(dims.numTopic);
			latent.topics.inc(uIndex, itemIndex, topicIndex);	// init topic for adoption (u,i)
			countTables.itemTopic.inc(itemIndex, topicIndex, 1);
			countTables.itemTopic.inc(dims.numItem, topicIndex, 1);	// inc marginal count
			
			int decision = random.nextInt(numDecision);
			latent.decisions.inc(uIndex, itemIndex, decision);	// init decision for adoption (u,i)
			
			if (decision == 0) {// for this adoption, no brand is used
				countTables.decisionUser.inc(0, uIndex, 1);
				latent.brands.inc(uIndex, itemIndex, -1);
				
			} else {
				countTables.decisionUser.inc(1, uIndex, 1);
				int brandIndex = random.nextInt(dims.numBrand);
				latent.brands.inc(uIndex, itemIndex, brandIndex);
				
				countTables.brandTopic.inc(brandIndex, topicIndex, 1);
				countTables.brandTopic.inc(dims.numBrand, topicIndex, 1);	// inc marginal count
				
				countTables.itemBrand.inc(itemIndex, brandIndex, 1);
				countTables.itemBrand.inc(dims.numItem, brandIndex, 1); // inc marginal count
			}
			
		}
	}
	
}
