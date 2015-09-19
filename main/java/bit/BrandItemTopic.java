package bit;

import java.util.logging.Logger;

import org.petuum.jbosen.PsApplication;
import org.petuum.jbosen.PsTableGroup;
import org.petuum.jbosen.table.DoubleTable;

import defs.CountTables;
import utils.Converters;
import utils.Stats;

public class BrandItemTopic extends PsApplication {

	BrandItemTopicConfig config ;

	private double[][] init_user_topic;
	private double[][] init_user_decision;
	private double[][] init_topic_item;
	private double[][] init_topic_brand;
	private double[][] init_brand_item;
	
	CountTables countTables;

	private double[] marginCountOfUser;
	private double[] sumItem4Topic;
	private double[] sumBrand4Topic;
	private double[] marginCountOfBrand;
	
	private static final int userTopicTableId = 0;
	private static final int userDecisionTableId = 1;
	private static final int topicItemTableId = 2;
	private static final int topicBrandTableId = 3;
	private static final int brandItemTableId = 4;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		
	}

	// initialize counts as 2d arrays
	public void initCounts() {
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
		PsTableGroup.createDenseDoubleTable(userTopicTableId, staleness, numTopic);
		PsTableGroup.createDenseDoubleTable(userDecisionTableId, staleness, numDecision);
		PsTableGroup.createDenseDoubleTable(topicItemTableId, staleness, numItem);
		PsTableGroup.createDenseDoubleTable(topicBrandTableId, staleness, numBrand);
		PsTableGroup.createDenseDoubleTable(brandItemTableId, staleness, numItem);
		
		DoubleTable userTopicTable = PsTableGroup.getDoubleTable(userTopicTableId);
		DoubleTable userDecisionTable = PsTableGroup.getDoubleTable(userDecisionTableId);
		DoubleTable topicItemTable = PsTableGroup.getDoubleTable(topicItemTableId);
		DoubleTable topicBrandTable = PsTableGroup.getDoubleTable(topicBrandTableId);
		DoubleTable brandItemTable = PsTableGroup.getDoubleTable(brandItemTableId);
		
		// copy initialized count arrays into the tables
		Converters.copy2Table(init_user_topic, userTopicTable);
		Converters.copy2Table(init_user_decision, userDecisionTable);
		Converters.copy2Table(init_topic_item, topicItemTable);
		Converters.copy2Table(init_topic_brand, topicBrandTable);
		Converters.copy2Table(init_brand_item, brandItemTable);
		
		marginCountOfUser = getMarginCounts(init_user_topic);
		sumItem4Topic = getMarginCounts(init_topic_item);
		sumBrand4Topic = getMarginCounts(init_topic_brand);
		marginCountOfBrand = getMarginCounts(init_brand_item);
	}


	@Override
	public void runWorkerThread(int threadId) {
		// TODO Auto-generated method stub
		
	}
	
	private double[] getMarginCounts(double[][] counts) {
		int numRow = counts.length;
		double[] marginCounts = new double[numRow];
		
		for (int i = 0; i < marginCounts.length; i++) {
			marginCounts[i] = Stats.sum(counts[i]);
		}
		
		return marginCounts;
	}

}
