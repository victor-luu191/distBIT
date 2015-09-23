package bit;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import cc.mallet.types.Alphabet;
import defs.Brand;
import defs.Dimensions;
import defs.Item;

class DataReader {

	static Dimensions loadData(String dataDir, DataSet ds) throws IOException {
		
		// TODO Auto-generated method stub
		int numUser=0;
		int numBrand=0;
		int numItem=0;
		String fAdopt = dataDir + "adopt.csv"  ;
		String fMemberRel = dataDir + "member_rel.csv"  ;
		
		buildItemAndBrandDicts(ds, fMemberRel);
		
		loadAdopts(fAdopt, ds);
		Dimensions dims = new Dimensions(numUser, numBrand, numItem);
		System.out.println("Finished loading data set");
		return dims;
	}

	private static void loadAdopts(String fAdopt, DataSet ds) {
		// TODO Auto-generated method stub
		
	}

	private static void buildItemAndBrandDicts(DataSet ds, String fMemberRel) throws IOException {
		ArrayList<Item> allItems = new ArrayList<Item>();
		Set<Brand> allBrands = new HashSet<Brand>();
		BufferedReader br = new BufferedReader(new FileReader(fMemberRel));
		String line = br.readLine();	// skip header line

		while ((line = br.readLine()) != null) {
			String[] substr = line.split(",");
			String item_id = substr[0];
			String[] brand_ids = substr[1].split("; ");
//			String topicId = substr[2];	// only applicable to synthetic data

			allItems.add(new Item(item_id, Arrays.asList(brand_ids)));
			add(brand_ids, allBrands);
		}
		br.close();
//		finish loading data from fMemberRel
		ds.itemDict = new Alphabet(allItems.toArray());
		ds.brandDict = new Alphabet(allBrands.toArray());
		
	}

	private static void add(String[] brand_ids, Set<Brand> brands) {
		for (String bid : brand_ids) {

			if (!bid.equalsIgnoreCase("NA")) {
				brands.add(new Brand(bid));
			} 
		}
	}

}
