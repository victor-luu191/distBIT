package bit;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.petuum.jbosen.PsTableGroup;
import org.petuum.jbosen.table.DoubleTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.Converters;
import utils.Stats;
import defs.Assignments;
import defs.CountTables;
import defs.Dimensions;

public class BrandItemTopicWorker implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(BrandItemTopicWorker.class);

    private int numWorkers;
    private int numThreads;
    private int workerRank;
    
    private DataSet ds;
    private Dimensions dims;
    
    CountTables countTables;

	private int topicUserTableId;
	private int decisionUserTableId;
	private int itemTopicTableId;
	private int brandTopicTableId;
	private int itemBrandTableId;
    
	
	public void run() {
		int numUserPerWorker = dims.numUser/numWorkers;
		int userBegin = workerRank * numUserPerWorker;
		
		int userEnd = (workerRank == numWorkers - 1)? dims.numUser : (userBegin + numUserPerWorker);
		// TODO Auto-generated method stub
		initTables(countTables, userBegin, userEnd);
	}
	
	private void initTables(CountTables countTables, int userBegin, int userEnd) {
		
		countTables.topicUser = PsTableGroup.getDoubleTable(topicUserTableId);
		countTables.decisionUser = PsTableGroup.getDoubleTable(decisionUserTableId);
		countTables.itemTopic = PsTableGroup.getDoubleTable(itemTopicTableId);
		countTables.brandTopic = PsTableGroup.getDoubleTable(brandTopicTableId);
		countTables.itemBrand = PsTableGroup.getDoubleTable(itemBrandTableId);
		
		
		// finally add marginal counts to the last row
		addMarginCounts(countTables.topicUser);
		addMarginCounts(countTables.decisionUser);
		addMarginCounts( countTables.itemTopic);
		addMarginCounts( countTables.brandTopic);
		addMarginCounts( countTables.itemBrand);
	}

	public static void addMarginCounts(DoubleTable table) {
		
		// TODO
	}
}
