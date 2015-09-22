package bit;

import org.petuum.jbosen.PsApplication;
import org.petuum.jbosen.PsTableGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import defs.CountTables;
import defs.TableIds;

public class BrandItemTopic extends PsApplication {

	private static final Logger logger = LoggerFactory.getLogger(BrandItemTopic.class);
	BrandItemTopicConfig config ;

	CountTables countTables;

	private static final int topicUserTableId = 0;
	private static final int decisionUserTableId = 1;
	private static final int itemTopicTableId = 2;
	private static final int brandTopicTableId = 3;
	private static final int itemBrandTableId = 4;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		
	}

	@Override
	public void initialize() {
		// Configure count tables (containers of counts) with proper dimensions
		int staleness = 0;
		TableIds tableIds = new TableIds(topicUserTableId, decisionUserTableId, itemTopicTableId, 
								brandTopicTableId, itemBrandTableId);
		
		countTables = new CountTables(tableIds, config.dims, staleness);
		
		
		
		
	}


	@Override
	public void runWorkerThread(int threadId) {
		// TODO Auto-generated method stub
		int numClients = PsTableGroup.getNumClients();
		int numThreads = PsTableGroup.getNumLocalWorkerThreads();
		
	}

}
