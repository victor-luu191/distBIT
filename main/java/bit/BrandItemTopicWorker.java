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
import org.petuum.jbosen.row.double_.DoubleColumnIterator;
import org.petuum.jbosen.row.double_.DoubleRow;
import org.petuum.jbosen.table.DoubleTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import defs.AdoptHistory;
import defs.Adoption;
import defs.Dimensions;
import defs.Distributions;
import defs.Item;
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
	private Latent latents;
	private Pair[] allPairs;
	private Dimensions dims;

	private LikelihoodRecorder llRecorder = new LikelihoodRecorder();

	private int period;

	private int numRestart;

	private int numBrandPerTopic;

	// default config
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
		
		public String outputPrefix = "";
		public Pair[] allPairs = new Pair[0];
		public int numTopic = 2;
		public Dimensions dims = new Dimensions();
		public int period = 10;
		public int numRestart = 10;
		public int numBrandPerTopic = 2;
	}
	
	public BrandItemTopicWorker(Config config, int workerRank) {
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
        this.period = config.period;
        
        // workerId
        this.workerRank = workerRank;
        
        llRecorder.registerField("iter");
        llRecorder.registerField("logLikelihood");
        this.outputPrefix = config.outputPrefix;
        this.numRestart = config.numRestart;
        this.numBrandPerTopic = config.numBrandPerTopic;
	}

	public void run() {
		
		int numUser = dims.numUser;
		int numUserPerWorker = numUser/numWorkers;
		if (workerRank == 0) {
			logger.info("Number of users per worker " + numUserPerWorker + ", burnIn " + burnIn + ", numIter " + 
					numIter);
		}
		
		userBegin = workerRank * numUserPerWorker;
		userEnd = (workerRank == numWorkers - 1)? numUser : (userBegin + numUserPerWorker);
		
		System.out.println("Restart " + 0);
		countTables = retrieveCountTables();
		latents = new Latent();
		long initBegin = System.currentTimeMillis();
		initTables(countTables, latents, userBegin, userEnd);
		PsTableGroup.globalBarrier();	// Ensure complete initialization as each thread initialize a part of count tables.
		long initTimeElapsed = System.currentTimeMillis() - initBegin;
		if (workerRank == 0) {
			logger.info("Initialization done after " + initTimeElapsed + "ms");
//			printCounts();
		}
		double totalLL = runSampler();
		saveLearnedDists(0);
		
		for (int r = 1; r < numRestart; r++) {
			
//			Reset counts to 0, 
//			let only one worker do this, otherwise neg counts will occur 
//			as one count can be reduced too many times
			PsTableGroup.globalBarrier();
			if (workerRank == 0) {
				System.out.println("Restart " + r);
				toZeroCounts();
			}
			PsTableGroup.globalBarrier();
			
			// Empty latent lists (to put in new latents later) 
			emptyLatents(userBegin, userEnd);
			
			resetLatentsAndCounts(userBegin, userEnd);
			PsTableGroup.globalBarrier();
			
			totalLL = runSampler();
			saveLearnedDists(r);
		}
	}

	private void toZeroCounts() {
		
		for (int uIndex = 0; uIndex < dims.numUser; uIndex++) {
			// set to 0 all topic counts of the user, including marginal counts
			toZeros(dims.numTopic + 1, uIndex, countTables.topicUser);
			// set to 0 all decision counts of the user, including marginal counts
			toZeros(Dimensions.numDecision + 1, uIndex, countTables.decisionUser);
		}
		
		for (int tIndex = 0; tIndex < dims.numTopic; tIndex++) {
			toZeros(dims.numItem + 1, tIndex, countTables.itemTopic);
			toZeros(dims.numBrand + 1, tIndex, countTables.brandTopic);
		}
		
		for (int bIndex = 0; bIndex < dims.numBrand; bIndex++) {
			toZeros(dims.numItem + 1, bIndex, countTables.itemBrand);
		}
	}

	private void toZeros(int numRow, int col, DoubleTable table) {
		for (int row = 0; row < numRow; row++) {
			double cValue = table.get(row, col);
			table.inc(row, col, -cValue);
		}
	}
	
	private void resetLatentsAndCounts(int userBegin, int userEnd) {
		System.out.println("Reseting latents and counts of user in [" + userBegin + ", " + userEnd + ")");
		for (int uIndex = userBegin; uIndex < userEnd; uIndex++) {
			resetLatentsAndCountsOfUser(uIndex);
		}
	}

	private void resetLatentsAndCountsOfUser(int uIndex) {
		
		Random random = new Random();
		Dimensions dims = countTables.dims;
		AdoptHistory adoptHistory = ds.histories.get(uIndex);
		ArrayList<String> adoptions = adoptHistory.getItemIds();
		
		// Add margin counts to tables topicUser and decisionUser 
		// both margin counts equal to number of adopts of the user
		countTables.topicUser.inc(dims.numTopic, uIndex, adoptions.size());
		countTables.decisionUser.inc(Dimensions.numDecision, uIndex, adoptions.size());
		
		for (int i=0; i < adoptions.size(); i++) {
			int itemIndex = ds.itemDict.lookupIndex(new Item(adoptions.get(i)));
			int topicIndex = random.nextInt(dims.numTopic);
			latents.topics.get(uIndex).add(topicIndex);
			countTables.topicUser.inc(topicIndex, uIndex, 1);
			
			int decision = random.nextInt(Dimensions.numDecision);
			latents.decisions.get(uIndex).add(decision);
			
			if (decision == 0) {// for this adoption, no brand is used
				countTables.decisionUser.inc(0, uIndex, 1);
				int brandIndex = -1;
				latents.brands.get(uIndex).add(brandIndex);
				
				countTables.itemTopic.inc(itemIndex, topicIndex, 1);
				countTables.itemTopic.inc(dims.numItem, topicIndex, 1);	// inc marginal count
				
			} else {
				countTables.decisionUser.inc(1, uIndex, 1);
				int brandIndex = random.nextInt(dims.numBrand);
				latents.brands.get(uIndex).add(brandIndex);
				
				countTables.brandTopic.inc(brandIndex, topicIndex, 1);
				countTables.brandTopic.inc(dims.numBrand, topicIndex, 1);	// inc marginal count
				
				countTables.itemBrand.inc(itemIndex, brandIndex, 1);
				countTables.itemBrand.inc(dims.numItem, brandIndex, 1); // inc marginal count
			}
			
		}
	}

	private void emptyLatents(int userBegin, int userEnd) {
		latents = new Latent();
		// For each user, create his list of latents for adoptions
		for (int uIndex = userBegin; uIndex < userEnd; uIndex++) {
			latents.topics.put(uIndex, new ArrayList<Integer>());
			latents.brands.put(uIndex, new ArrayList<Integer>());
			latents.decisions.put(uIndex, new ArrayList<Integer>());
		}
	}

	private CountTables retrieveCountTables() {
		DoubleTable topicUser = PsTableGroup.getDoubleTable(topicUserTableId);
		DoubleTable decisionUser = PsTableGroup.getDoubleTable(decisionUserTableId);
		DoubleTable itemTopic = PsTableGroup.getDoubleTable(itemTopicTableId);
		DoubleTable brandTopic = PsTableGroup.getDoubleTable(brandTopicTableId);
		DoubleTable itemBrand = PsTableGroup.getDoubleTable(itemBrandTableId);
		CountTables countTables = new CountTables(dims, topicUser, decisionUser, itemTopic, brandTopic, itemBrand);
		return countTables;
	}

	private void saveLearnedDists(int r) {
		PsTableGroup.globalBarrier();	// sync all resulting count tables
		Distributions distributions = toDistributions(countTables, priors);
		// Print all results.
		if (workerRank == 0) {
		    logger.info("\n" + printExpDetails() + "\n" +
		            llRecorder.printAllLoss());
		    
		    String prefix = outputPrefix + "restart" + r + "_" + numIter + "iter";
		    try {
				outputCsvToDisk(distributions, prefix);
			} catch (Exception e) {
				logger.error("Failed to output to disk");
				e.printStackTrace();
			}
		}
	}

	private double runSampler() {
		// Burn-in
		for (int iter=0; iter < burnIn; iter++) {
			for (int uIndex = userBegin; uIndex < userEnd; uIndex++) {
				AdoptHistory history = ds.histories.get(uIndex);
				ArrayList<String> adoptions = history.getItemIds();
				updateLatents(adoptions, uIndex);
			}
			
			PsTableGroup.clock();
			
			// Print out likelihood to see improvements over uniform distributions
			if (workerRank == 0) {
				printLL(iter);
			}
		}
		PsTableGroup.globalBarrier();	// sync all count tables to get a better guesses thanks to burn-in
		
		// Training period
//		long trainBegin = System.currentTimeMillis();
		double totalLL = 0;
		for (int iter=0; iter < numIter; iter++) {
			for (int uIndex = userBegin; uIndex < userEnd; uIndex++) {
				AdoptHistory instance = ds.histories.get(uIndex);
				ArrayList<String> adoptions = instance.getItemIds();
				updateLatents(adoptions, uIndex);
			}
			PsTableGroup.clock();
			
			// For each period, evaluate and record total log likelihood of users in [userBegin, userEnd)  
			if (workerRank == 0) {
				totalLL = printLL(iter);
			}
		}
		return totalLL;
	}

	private double printLL(int iter) {
		
//		int snapshot = iter/period;
		Distributions dists = toDistributions(countTables, priors);
		double totalLL = 0f;
		for (int uIndex = userBegin; uIndex < userEnd; uIndex++) {
			double llOfUserData = BrandItemTopicCore.evalLikelihood(ds, uIndex, dists);
			assert !Double.isNaN(llOfUserData);
			totalLL += llOfUserData;
			
		}
		llRecorder.incMetric(iter, "iter", iter);
		llRecorder.incMetric(iter, "logLikelihood", totalLL);
		llRecorder.printOneMetric(iter);
		
//		llRecorder.incLoss(snapshot, "snapshot", snapshot);
//		llRecorder.incLoss(snapshot, "logLikelihood", totalLL);
//		llRecorder.printOneLoss(snapshot);
		return totalLL;
	}

	private void printCounts() {
		print(countTables.topicUser, dims.numTopic + 1, "topicUser");
		print(countTables.decisionUser, Dimensions.numDecision + 1, "decisionUser");
		print(countTables.itemTopic, dims.numItem + 1, "itemTopic");
		print(countTables.brandTopic, dims.numBrand + 1, "brandTopic");
		print(countTables.itemBrand, dims.numItem + 1, "itemBrand");
	}

	private void print(DoubleTable table, int numRow, String tableName) {
		
		System.out.println("\t" + tableName);
		for (int row = 0; row < numRow; row++) {
			DoubleColumnIterator iter = table.get(row).iterator();
			String line = "";
			while (iter.hasNext()) {
				iter.advance();
				line += iter.getValue() + ",";
			}
			System.out.println(line);
		}
	}

	private void updateLatents(ArrayList<String> adoptions, int uIndex) {
		
		for (int adoptIndex = 0; adoptIndex < adoptions.size(); adoptIndex++) {
			String itemId = adoptions.get(adoptIndex);
			int itemIndex = ds.itemDict.lookupIndex(new Item(itemId));
			Adoption adopt = new Adoption(adoptIndex, uIndex, itemIndex);
			BrandItemTopicCore.updateTopic(adopt, countTables, latents, priors);
			BrandItemTopicCore.updatePair(adopt, countTables, latents, priors, allPairs, ds);
			
//			PsTableGroup.globalBarrier();	// sync changes immediately st effect of each pair update is observable
//			print(countTables.itemTopic, dims.numItem + 1, "itemTopic");
//			print(countTables.itemBrand, dims.numItem + 1, "itemBrand");
			
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
	 * @param latents 
	 */
	private void initValues(CountTables countTables, Latent latents, int uIndex) {
		
		Random random = new Random();
		Dimensions dims = countTables.dims;
		AdoptHistory adoptHistory = ds.histories.get(uIndex);
		ArrayList<String> adoptions = adoptHistory.getItemIds();
		// Create the user's list of latents for adoptions
		latents.topics.put(uIndex, new ArrayList<Integer>());
		latents.brands.put(uIndex, new ArrayList<Integer>());
		latents.decisions.put(uIndex, new ArrayList<Integer>());
		
		// Add margin counts to tables topicUser and decisionUser 
		// both margin counts equal to number of adopts of the user
		countTables.topicUser.inc(dims.numTopic, uIndex, adoptions.size());
		countTables.decisionUser.inc(Dimensions.numDecision, uIndex, adoptions.size());
		
		for (int i=0; i < adoptions.size(); i++) {
			int itemIndex = ds.itemDict.lookupIndex(new Item(adoptions.get(i)));
			int topicIndex = random.nextInt(dims.numTopic);
			latents.topics.get(uIndex).add(topicIndex);	// init latent topic for adoption (u,i)
			countTables.topicUser.inc(topicIndex, uIndex, 1);
			
			int decision = random.nextInt(Dimensions.numDecision);
			latents.decisions.get(uIndex).add(decision);	// init decision for adoption (u,i)
			
			if (decision == 0) {// for this adoption, no brand is used
				countTables.decisionUser.inc(0, uIndex, 1);
				int brandIndex = -1;
				latents.brands.get(uIndex).add(brandIndex);
				
				countTables.itemTopic.inc(itemIndex, topicIndex, 1);
				countTables.itemTopic.inc(dims.numItem, topicIndex, 1);	// inc marginal count
				
			} else {
				countTables.decisionUser.inc(1, uIndex, 1);
				// Choose uniformly from only a fixed subset of brands
				// to ensure that the set of brands of this topic is disjoint from brand sets of other topics
				// Simple way: topic k only contains brands [km. (k+1)m) where m is the number of brand per topic
				int brandIndex = random.nextInt(numBrandPerTopic) + topicIndex * numBrandPerTopic;
				
//				Choose uniformly from all brands,
//				int brandIndex = random.nextInt(dims.numBrand);
				
				
				latents.brands.get(uIndex).add(brandIndex);
				
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
		
		for (int col = 0; col < numCol; col++) {
			double marginCount = counts.get(numRow, col);
			for (int row = 0; row < numRow; row++) {
				probs[row][col] = (counts.get(row, col) + prior)/(marginCount + numRow * prior);
			}
		}

		return probs;
	}
}
