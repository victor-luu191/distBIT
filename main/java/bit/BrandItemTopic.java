package bit;

import java.util.logging.Logger;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.petuum.jbosen.PsApplication;
import org.petuum.jbosen.PsTableGroup;
import org.petuum.jbosen.row.RowUpdate;
import org.petuum.jbosen.row.double_.DenseDoubleRow;
import org.petuum.jbosen.table.DoubleTable;
import org.petuum.jbosen.table.Table;

import defs.CountTables;
import utils.Converters;
import utils.Stats;

public class BrandItemTopic extends PsApplication {

	BrandItemTopicConfig config ;

	private double[][] init_topicUser;
	private double[][] init_decisionUser;
	private double[][] init_itemTopic;
	private double[][] init_brandTopic;
	private double[][] init_itemBrand;
	
	CountTables countTables;

	private static final int topicUserTableId = 0;
	private static final int decisionUserTableId = 1;
	private static final int itemTopicTableId = 2;
	private static final int brandTopicTableId = 3;
	private static final int itemBrandTableId = 4;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		
	}

	// initialize counts as 2d arrays
	public void initCounts(DataSet ds) {
		// TODO	
	}

	@Override
	public void initialize() {
		// TODO
		// create count tables
		int staleness = 0;
		int numTopic = config.dims.numTopic;
		int numDecision = 2;
		int numItem = config.dims.numItem;
		int numBrand = config.dims.numBrand;
		PsTableGroup.createDenseDoubleTable(topicUserTableId, staleness, numTopic);
		PsTableGroup.createDenseDoubleTable(decisionUserTableId, staleness, numDecision);
		PsTableGroup.createDenseDoubleTable(itemTopicTableId, staleness, numItem);
		PsTableGroup.createDenseDoubleTable(brandTopicTableId, staleness, numBrand);
		PsTableGroup.createDenseDoubleTable(itemBrandTableId, staleness, numItem);
		
		countTables.topicUser = PsTableGroup.getDoubleTable(topicUserTableId);
		countTables.decisionUser = PsTableGroup.getDoubleTable(decisionUserTableId);
		countTables.itemTopic = PsTableGroup.getDoubleTable(itemTopicTableId);
		countTables.brandTopic = PsTableGroup.getDoubleTable(brandTopicTableId);
		countTables.itemBrand = PsTableGroup.getDoubleTable(itemBrandTableId);
		
		// copy initialized count arrays into the tables
		Converters.copy2Table(init_topicUser, countTables.topicUser);
		Converters.copy2Table(init_decisionUser, countTables.decisionUser);
		Converters.copy2Table(init_itemTopic, countTables.itemTopic);
		Converters.copy2Table(init_brandTopic, countTables.brandTopic);
		Converters.copy2Table(init_itemBrand, countTables.itemBrand);
		
		addMarginCounts(init_topicUser, countTables.topicUser);
		addMarginCounts(init_decisionUser, countTables.decisionUser);
		addMarginCounts(init_itemTopic, countTables.itemTopic);
		addMarginCounts(init_brandTopic, countTables.brandTopic);
		addMarginCounts(init_itemBrand, countTables.itemBrand);
	}


	@Override
	public void runWorkerThread(int threadId) {
		// TODO Auto-generated method stub
		
	}
	
	public static void addMarginCounts(double[][] counts, DoubleTable table) {
		
		RealMatrix countMat = new Array2DRowRealMatrix(counts);
		
		int numRow = counts.length;
		int numCol = counts[0].length;
		
		for (int i = 0; i < numCol; i++) {
			table.inc(numRow, i, Stats.sum(countMat.getColumn(i)));
		}
	}

}
