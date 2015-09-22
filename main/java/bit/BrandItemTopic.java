package bit;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.petuum.jbosen.PsApplication;
import org.petuum.jbosen.PsTableGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import defs.CountTables;
import defs.Dimensions;
import defs.Pair;
import defs.TableIds;

public class BrandItemTopic extends PsApplication {

	private static final Logger logger = LoggerFactory.getLogger(BrandItemTopic.class);
	
	BrandItemTopicConfig config ;
	BrandItemTopicWorker.Config bitConfig;

	private int numUser;
	private int numBrand;
	private int numItem;

	private static final int topicUserTableId = 0;
	private static final int decisionUserTableId = 1;
	private static final int itemTopicTableId = 2;
	private static final int brandTopicTableId = 3;
	private static final int itemBrandTableId = 4;
	
	public BrandItemTopic(BrandItemTopicConfig config) {
		
		this.config = config;
		// TODO: pass global config to each worker 
		bitConfig = new BrandItemTopicWorker.Config();	// initial config is default config
		bitConfig.burnIn = config.burnIn;
		bitConfig.numIter = config.numIter;
		bitConfig.staleness = config.staleness;
		
		bitConfig.topicUserId = topicUserTableId;
		bitConfig.decisionUserId = decisionUserTableId;
		bitConfig.itemTopicId = itemTopicTableId;
		bitConfig.brandTopicId = brandTopicTableId;
		bitConfig.itemBrandId = itemBrandTableId;
		
		bitConfig.outputPrefix = config.outputPrefix;
		
	}

	public static void main(String[] args) {
		
		final BrandItemTopicConfig  config = new BrandItemTopicConfig();
		final CmdLineParser parser = new CmdLineParser(config);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			logger.error(e.getMessage());
			parser.printUsage(System.err);
			return;
		}
		
		long trainTimeBegin = System.currentTimeMillis();
		BrandItemTopic bitThreads = new BrandItemTopic(config);
		bitThreads.run(config);
		
		if (config.clientId == 0) {
			long trainTimeElapsed = System.currentTimeMillis() - trainTimeBegin;
			logger.info("Finished training after " + trainTimeElapsed + "(ms)");
		}
		
	}

	@Override
	public void initialize() {
		// Configure count tables (containers of counts) with proper dimensions
		int staleness = 0;
		TableIds tableIds = new TableIds(topicUserTableId, decisionUserTableId, itemTopicTableId, 
								brandTopicTableId, itemBrandTableId);
		
		Dimensions dims = new Dimensions(numUser, numBrand, numItem, config.numTopic);
		new CountTables(tableIds, dims, staleness);
		
		// Configure loss table
		LossRecorder.createLossTable();
	}

	@Override
	public void runWorkerThread(int threadId) {

		int numClients = PsTableGroup.getNumClients();
		int numThreads = PsTableGroup.getNumLocalWorkerThreads();
		bitConfig.numWorkers = numClients * numThreads;
		bitConfig.numThreads = numThreads;
		
		int workerRank = numThreads * config.clientId + threadId;
		if (workerRank == 0) {
			logger.info("Starting " + numClients + " nodes, each with "
					+ numThreads + " threads.");
		}
		
		BrandItemTopicWorker worker = new BrandItemTopicWorker(bitConfig, workerRank);
		worker.run();
	}
	
	private static Pair[] buildPairs(int numBrand) {

		Pair[] allPairs = new Pair[numBrand + 1];	// all pairs = { (T, b1), ..., (T, bQ), (F,"no brand") }

		for (int bIndex=0; bIndex < numBrand; bIndex++) {

			allPairs[bIndex] = new Pair(1, bIndex);
		}
		int last = allPairs.length - 1;
		allPairs[last] = new Pair(0, -1);	// bIndex = -1 means no actual brand

		return allPairs;
	}
}
