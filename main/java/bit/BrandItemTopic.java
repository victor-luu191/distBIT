package bit;

import java.util.logging.Logger;

import org.petuum.jbosen.PsApplication;
import org.petuum.jbosen.PsTableGroup;

public class BrandItemTopic extends PsApplication {

	BrandItemTopicConfig config ;
	
	private static final int userTopicTableId = 0;
	private static final int userDecisionTableId = 1;
	private static final int topicItemTableId = 2;
	private static final int topicBrandTableId = 3;
	private static final int brandItemTableId = 4;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		
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
	}


	@Override
	public void runWorkerThread(int threadId) {
		// TODO Auto-generated method stub
		
	}

}
