package bit;

import defs.Dimensions;

class DataReader {

	static Dimensions loadData(String dataDir, DataSet ds) {
		
		// TODO Auto-generated method stub
		int numUser=0;
		int numBrand=0;
		int numItem=0;
		String fAdopt = dataDir + "adopt.csv"  ;
		String fMemberRel = dataDir + "member_rel.csv"  ;
		
		buildDicts(fMemberRel);
		
		loadAdopts(fAdopt);
		Dimensions dims = new Dimensions(numUser, numBrand, numItem);
		return dims;
	}

}
