package bit;

import java.util.ArrayList;
import java.util.Random;

import org.petuum.jbosen.PsTableGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import defs.Adoption;
import defs.CountTables;
import defs.Dimensions;
import defs.Instance;
import defs.Latent;

public class BrandItemTopicWorker implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(BrandItemTopicWorker.class);

	// global objects
    private int numWorkers;
    private int numThreads;
    private int burnIn;
    private int numIter;
    private int numTopic;
	private int staleness;
    private DataSet trainSet;
    private int topicUserTableId;
	private int decisionUserTableId;
	private int itemTopicTableId;
	private int brandTopicTableId;
	private int itemBrandTableId;
	private CountTables countTables;
	private String outputPrefix;
	
	// local objects
	private int workerRank;
	private int userBegin; 
	private int userEnd;
	private Latent latent;

	private LossRecorder lossRecorder = new LossRecorder();

	public static class Config {
		public int numWorkers = -1;
        // Worker's rank among all threads across all nodes.
        public int numThreads = -1;
        
        public int burnIn = 1;
        public int numIter = 1;
        public int staleness = 0;
        public DataSet trainSet = null;
		
		public int topicUserId = 0;
		public int decisionUserId = 1;
		public int itemTopicId = 2;
		public int brandTopicId = 3;
		public int itemBrandId = 4;
//		private TableIds tableIds = new TableIds(topicUserId, decisionUserId, itemTopicId, 
//													brandTopicId, itemBrandId);
		
		public int numTopic = 10;
		public String outputPrefix = "";
	}
	
	public BrandItemTopicWorker(Config config, int workerRank) {
		// TODO Auto-generated constructor stub
		assert config.numWorkers != -1;
        this.numWorkers = config.numWorkers;

        assert config.numThreads != -1;
        this.numThreads = config.numThreads;
        
        this.burnIn = config.burnIn;
        this.numIter = config.numIter;
        
        this.staleness = config.staleness;
        
        assert config.trainSet != null;
        this.trainSet = config.trainSet;
        
        this.topicUserTableId = config.topicUserId;
        this.decisionUserTableId = config.decisionUserId;
        this.itemTopicTableId = config.itemTopicId;
        this.brandTopicTableId = config.brandTopicId;
        this.itemBrandTableId = config.itemBrandId;
        
        // workerId
        this.workerRank = workerRank;
	}

	

	private void updateLatents(ArrayList<String> adoptions, int uIndex) {
		for (int adoptIndex = 0; adoptIndex < adoptions.size(); adoptIndex++) {
			
			int itemIndex = trainSet.itemDict.lookupIndex(adoptions.get(adoptIndex));
			Adoption adopt = new Adoption(adoptIndex, uIndex, itemIndex);
			BrandItemTopicCore.updateTopic(adopt, countTables, latent);
			BrandItemTopicCore.updatePair(adopt, countTables, latent);
		}
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
		
		Random random = new Random(123);
		Dimensions dims = countTables.dims;
		Instance instance = trainSet.instances.get(uIndex);
		ArrayList<String> adoptions = instance.getItemIds();
		// create lists of latents for adoptions
		latent.topics.put(uIndex, new ArrayList<Integer>());
		latent.brands.put(uIndex, new ArrayList<Integer>());
		latent.decisions.put(uIndex, new ArrayList<Integer>());
		
		// Add margin counts to tables topicUser and decisionUser 
		// both equal to number of adopts of the user
		countTables.topicUser.inc(dims.numTopic, uIndex, adoptions.size());
		countTables.decisionUser.inc(Dimensions.numDecision, uIndex, adoptions.size());
		
		for (int i=0; i < adoptions.size(); i++) {
			int itemIndex = trainSet.itemDict.lookupIndex(adoptions.get(i));
			int topicIndex = random.nextInt(dims.numTopic);
			latent.topics.get(uIndex).add(topicIndex);	// init topic for adoption (u,i)
			countTables.itemTopic.inc(itemIndex, topicIndex, 1);
			countTables.itemTopic.inc(dims.numItem, topicIndex, 1);	// inc marginal count
			
			int decision = random.nextInt(Dimensions.numDecision);
			latent.decisions.get(uIndex).add(decision);	// init decision for adoption (u,i)
			
			if (decision == 0) {// for this adoption, no brand is used
				countTables.decisionUser.inc(0, uIndex, 1);
				int brandIndex = -1;
				latent.brands.get(uIndex).add(brandIndex);
				
			} else {
				countTables.decisionUser.inc(1, uIndex, 1);
				int brandIndex = random.nextInt(dims.numBrand);
				latent.brands.get(uIndex).add(brandIndex);
				
				countTables.brandTopic.inc(brandIndex, topicIndex, 1);
				countTables.brandTopic.inc(dims.numBrand, topicIndex, 1);	// inc marginal count
				
				countTables.itemBrand.inc(itemIndex, brandIndex, 1);
				countTables.itemBrand.inc(dims.numItem, brandIndex, 1); // inc marginal count
			}
			
		}
	}
	
	private void outputCsvToDisk(String outputPrefix) {
		// TODO Auto-generated method stub
		
	}

	private String printExpDetails() {
		String exp = "";
        exp += "staleness: " + staleness + "\n";
        exp += "numClients: " + numWorkers / numThreads + "\n";
        exp += "numWorkers: " + numWorkers + "\n";
        exp += "numThreads: " + numThreads + "\n";
        exp += "burnIn: " + burnIn + "\n";
        exp += "numIter: " + numIter + "\n";
        exp += "numTopic: " + numTopic + "\n";
        
        return exp;
	}
	
	public void run() {
		 
		countTables.topicUser = PsTableGroup.getDoubleTable(topicUserTableId);
		countTables.decisionUser = PsTableGroup.getDoubleTable(decisionUserTableId);
		countTables.itemTopic = PsTableGroup.getDoubleTable(itemTopicTableId);
		countTables.brandTopic = PsTableGroup.getDoubleTable(brandTopicTableId);
		countTables.itemBrand = PsTableGroup.getDoubleTable(itemBrandTableId);
		
		latent = new Latent();
		int numUser = countTables.dims.numUser;
		int numUserPerWorker = numUser/numWorkers;
		userBegin = workerRank * numUserPerWorker;
		userEnd = (workerRank == numWorkers - 1)? numUser : (userBegin + numUserPerWorker);
		
		// Since each thread initialize part of count tables, use barrier to
        // ensure initialization completes.
		initTables(countTables, latent, userBegin, userEnd);
		PsTableGroup.globalBarrier();
		
		// Burn-in period
		for (int iter=0; iter < burnIn; iter++) {
			for (int uIndex = userBegin; uIndex < userEnd; uIndex++) {
				Instance instance = trainSet.instances.get(uIndex);
				ArrayList<String> adoptions = instance.getItemIds();
				updateLatents(adoptions, uIndex);
				PsTableGroup.clock();
			}
		}
		PsTableGroup.globalBarrier();	// sync all count tables to get a better guesses thanks to burn-in
		
		// Actual training period
		for (int iter=0; iter < numIter; iter++) {
			for (int uIndex = userBegin; uIndex < userEnd; uIndex++) {
				Instance instance = trainSet.instances.get(uIndex);
				ArrayList<String> adoptions = instance.getItemIds();
				updateLatents(adoptions, uIndex);
				PsTableGroup.clock();
			}
		}
		// TODO: Add loss evaluation here
		PsTableGroup.globalBarrier();	// sync all resulting count tables
		
		// Print all results.
        if (workerRank == 0) {
            logger.info("\n" + printExpDetails() + "\n" +
                    lossRecorder.printAllLoss());
            if (!outputPrefix.equals("")) {
                try {
					outputCsvToDisk(outputPrefix);
				} catch (Exception e) {
					logger.error("Failed to output to disk");
					e.printStackTrace();
				}
            }
        }
	}
}
