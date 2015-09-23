package bit;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.petuum.jbosen.PsTableGroup;
import org.petuum.jbosen.row.double_.DenseDoubleRow;
import org.petuum.jbosen.row.double_.DoubleRow;
import org.petuum.jbosen.table.DoubleTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import defs.Adoption;
import defs.CountTables;
import defs.Dimensions;
import defs.Distributions;
import defs.AdoptHistory;
import defs.Latent;
import defs.Pair;
import defs.Priors;

public class BrandItemTopicWorker implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(BrandItemTopicWorker.class);

	// global objects
    private int numWorkers;
    private int numThreads;
    private int burnIn;
    private int numIter;
    private int numTopic;
	private int staleness;
    private DataSet ds;
    private int topicUserTableId;
	private int decisionUserTableId;
	private int itemTopicTableId;
	private int brandTopicTableId;
	private int itemBrandTableId;
	private CountTables countTables;
	private Priors priors;
	private String outputPrefix;
	
	// local objects
	private int workerRank;
	private int userBegin; 
	private int userEnd;
	private Latent latent;
	private Pair[] allPairs;
	private Dimensions dims;

	private LossRecorder lossRecorder = new LossRecorder();

	public static class Config {
		public int numWorkers = -1;
        // Worker's rank among all threads across all nodes.
        public int numThreads = -1;
        
        public int burnIn = 1;
        public int numIter = 1;
        public int staleness = 0;
        public DataSet ds = null;	// later change to trainSet
		
		public int topicUserId = 0;
		public int decisionUserId = 1;
		public int itemTopicId = 2;
		public int brandTopicId = 3;
		public int itemBrandId = 4;
//		private TableIds tableIds = new TableIds(topicUserId, decisionUserId, itemTopicId, 
//													brandTopicId, itemBrandId);
		
		public Priors priors = new Priors();
		public int numTopic = 0;
		public String outputPrefix = "";
		public Pair[] allPairs = new Pair[0];
		public Dimensions dims = new Dimensions();
	}
	
	public BrandItemTopicWorker(Config config, int workerRank) {
		// TODO Auto-generated constructor stub
		assert config.numWorkers != -1;
        this.numWorkers = config.numWorkers;

        assert config.numThreads != -1;
        this.numThreads = config.numThreads;
        
        this.burnIn = config.burnIn;
        this.numIter = config.numIter;
        this.numTopic = config.numTopic;
        this.dims = config.dims;
        
        this.staleness = config.staleness;
        
        assert config.ds != null;
        this.ds = config.ds;
        
        this.topicUserTableId = config.topicUserId;
        this.decisionUserTableId = config.decisionUserId;
        this.itemTopicTableId = config.itemTopicId;
        this.brandTopicTableId = config.brandTopicId;
        this.itemBrandTableId = config.itemBrandId;
        
        this.priors = config.priors;
        this.allPairs = config.allPairs;
        
        // workerId
        this.workerRank = workerRank;
	}

	

	private void updateLatents(ArrayList<String> adoptions, int uIndex) {
		for (int adoptIndex = 0; adoptIndex < adoptions.size(); adoptIndex++) {
			
			int itemIndex = ds.itemDict.lookupIndex(adoptions.get(adoptIndex));
			Adoption adopt = new Adoption(adoptIndex, uIndex, itemIndex);
			BrandItemTopicCore.updateTopic(adopt, countTables, latent, priors);
			BrandItemTopicCore.updatePair(adopt, countTables, latent, priors, allPairs);
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
		AdoptHistory adoptHistory = ds.histories.get(uIndex);
		ArrayList<String> adoptions = adoptHistory.getItemIds();
		// create lists of latents for adoptions
		latent.topics.put(uIndex, new ArrayList<Integer>());
		latent.brands.put(uIndex, new ArrayList<Integer>());
		latent.decisions.put(uIndex, new ArrayList<Integer>());
		
		// Add margin counts to tables topicUser and decisionUser 
		// both equal to number of adopts of the user
		countTables.topicUser.inc(dims.numTopic, uIndex, adoptions.size());
		countTables.decisionUser.inc(Dimensions.numDecision, uIndex, adoptions.size());
		
		for (int i=0; i < adoptions.size(); i++) {
			int itemIndex = ds.itemDict.lookupIndex(adoptions.get(i));
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
	
	private void outputCsvToDisk(Distributions distributions, String outputPrefix) throws IOException {
		long diskTimeBegin = System.currentTimeMillis();
		
		String fTopicUser = outputPrefix + "_topicUser.csv";
		write(distributions.topicUser, fTopicUser);
		String fDecisionUser = outputPrefix + "_decisionUser.csv";
		write(distributions.decisionUser, fDecisionUser);
		String fItemTopic = outputPrefix + "_itemTopic.csv";
		write(distributions.itemTopic, fItemTopic);
		String fBrandTopic = outputPrefix + "_brandTopic.csv";
		write(distributions.brandTopic, fBrandTopic);
		String fItemBrand = outputPrefix + "_itemBrand.csv";
		write(distributions.itemBrand, fItemBrand);
		
		long diskTimeElapsed = System.currentTimeMillis() - diskTimeBegin;
		logger.info("Finish outputing to " + outputPrefix + " in "
				+ diskTimeElapsed + " ms");
		
		/**
		 * Later  read/write tables by Hadoop readers/writers to optimize
		 */
		// write topicUser table
//		DoubleTable topicUserTable = PsTableGroup.getDoubleTable(topicUserTableId);
//		int numUser = countTables.dims.numUser;
//		int numRow = numTopic;
//		String fName = outputPrefix + ".topicUser.csv";
//		write(topicUserTable, numRow, numUser, fName);
		//TODO: write decisionUser table
		
		//TODO: write itemTopic table
		
		//TODO: write brandTopic table
		
		//TODO: write itemBrand table
	}



	private void write(double[][] arr, String fName) throws IOException {
		// TODO Auto-generated method stub
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(fName));
		} catch (IOException e) {
			logger.error("Caught IOException: " + e.getMessage());
			System.exit(-1);
		}
		for (int r = 0; r < arr.length; r++) {
			out.write(Arrays.toString(arr[r]) + "\n");
		}
		out.close();
	}

	@SuppressWarnings("unused")
	private void write(DoubleTable table, int numRow, int numCol, String fName)
			throws IOException, Exception {
		
		DecimalFormat doubleFormat = new DecimalFormat("0.###E0");
		StringBuilder ss = null;
		String newline = System.getProperty("line.separator");
		BufferedFileWriter out = null;
		ss = new StringBuilder();
		try {
			
			out = new BufferedFileWriter(fName);
		} catch (IOException e) {
			logger.error("Caught IOException: " + e.getMessage());
			System.exit(-1);
		}
		DoubleRow rowCache = new DenseDoubleRow(numCol);
		for (int i = 0; i < numRow; ++i) {
			rowCache = table.get(i);
			for (int k = 0; k < numCol - 1; ++k) {
				ss.append(doubleFormat.format(rowCache.get(k)) + ",");
			}
			// no comma
			ss.append(doubleFormat.format(rowCache.get(numCol - 1))).append(newline);
		}
		out.write(ss.toString());
		out.close();
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
		 
		DoubleTable topicUser = PsTableGroup.getDoubleTable(topicUserTableId);
		DoubleTable decisionUser = PsTableGroup.getDoubleTable(decisionUserTableId);
		DoubleTable itemTopic = PsTableGroup.getDoubleTable(itemTopicTableId);
		DoubleTable brandTopic = PsTableGroup.getDoubleTable(brandTopicTableId);
		DoubleTable itemBrand = PsTableGroup.getDoubleTable(itemBrandTableId);
		
		countTables = new CountTables(dims, topicUser, decisionUser, itemTopic, brandTopic, itemBrand);
		
		latent = new Latent();
		int numUser = countTables.dims.numUser;
		int numUserPerWorker = numUser/numWorkers;
		userBegin = workerRank * numUserPerWorker;
		userEnd = (workerRank == numWorkers - 1)? numUser : (userBegin + numUserPerWorker);
		
		// Init the partition of tables for [userBegin, userEnd), excluding last user
		// Since each thread initialize part of count tables, use barrier to
        // ensure initialization completes.
		initTables(countTables, latent, userBegin, userEnd);
		PsTableGroup.globalBarrier();
		
		// Burn-in period
		for (int iter=0; iter < burnIn; iter++) {
			for (int uIndex = userBegin; uIndex < userEnd; uIndex++) {
				AdoptHistory instance = ds.histories.get(uIndex);
				ArrayList<String> adoptions = instance.getItemIds();
				updateLatents(adoptions, uIndex);
				PsTableGroup.clock();
			}
		}
		PsTableGroup.globalBarrier();	// sync all count tables to get a better guesses thanks to burn-in
		
		// Actual training period
		for (int iter=0; iter < numIter; iter++) {
			for (int uIndex = userBegin; uIndex < userEnd; uIndex++) {
				AdoptHistory instance = ds.histories.get(uIndex);
				ArrayList<String> adoptions = instance.getItemIds();
				updateLatents(adoptions, uIndex);
				PsTableGroup.clock();
			}
		}
		// TODO: Add loss evaluation here
		PsTableGroup.globalBarrier();	// sync all resulting count tables
		
		Distributions distributions = toDistributions(countTables, priors);
		
		// Print all results.
        if (workerRank == 0) {
            logger.info("\n" + printExpDetails() + "\n" +
                    lossRecorder.printAllLoss());
            if (!outputPrefix.equals("")) {
                try {
					outputCsvToDisk(distributions, outputPrefix);
				} catch (Exception e) {
					logger.error("Failed to output to disk");
					e.printStackTrace();
				}
            }
        }
	}
	
	public Distributions toDistributions(CountTables countTables, Priors priors) {
		
		Dimensions dims = countTables.dims;
		double[][] topicUserProbs = toProbs(countTables.topicUser, dims.numTopic, dims.numUser, 
				priors.theta);
		
		double[][] decisionUserProbs = toProbs(countTables.decisionUser, Dimensions.numDecision, 
				dims.numUser, priors.gamma);
		
		double[][] itemTopicProbs = toProbs(countTables.itemTopic, dims.numItem, dims.numTopic,
				priors.phi);
		
		double[][] brandTopicProbs = toProbs(countTables.brandTopic, dims.numBrand, dims.numTopic,
				priors.alpha);
		
		double[][] itemBrandProbs = toProbs(countTables.itemBrand, dims.numItem, dims.numBrand,
				priors.beta);

		return new Distributions(topicUserProbs, decisionUserProbs, itemTopicProbs, brandTopicProbs, itemBrandProbs);
	}

	private double[][] toProbs(DoubleTable counts, int numRow, int numCol, double prior) {
		double[][] probs = new double[numRow][numCol];
		for (int i = 0; i < numCol; i++) {
			double marginCount = counts.get(numRow, i);
			for (int j = 0; j < numRow; j++) {
				probs[j][i] = counts.get(j, i)/marginCount;
			}
		}

		return probs;
	}
}
