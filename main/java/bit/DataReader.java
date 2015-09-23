package bit;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;

import cc.mallet.types.Alphabet;
import defs.Brand;
import defs.Dimensions;
import defs.Item;

class DataReader {

	static Dimensions loadData(String dataDir, DataSet ds) {
		
		// TODO Auto-generated method stub
		int numUser=0;
		int numBrand=0;
		int numItem=0;
		String fAdopt = dataDir + "adopt.csv"  ;
		String fMemberRel = dataDir + "member_rel.csv"  ;
		
		buildDicts(ds, fMemberRel);
		
		loadAdopts(fAdopt);
		Dimensions dims = new Dimensions(numUser, numBrand, numItem);
		System.out.println("Finished loading data set");
		return dims;
	}

	private static void buildDicts(DataSet ds, String fMemberRel) {
		ArrayList<Item> items = new ArrayList<Item>();
		ArrayList<Brand> brands = new ArrayList<Brand>();
		HashSet<String> loaded_brands = new HashSet<String>();	// monitor which brands are loaded 
		

		BufferedReader br = new BufferedReader(new FileReader(fMemberRel));
		String line = br.readLine();	// skip header line

		while ((line = br.readLine()) != null) {
			String[] substr = line.split(",");
			String item_id = substr[0];
			String[] brand_ids = substr[1].split("; ");
			String topicId = substr[2];

			items.add(new Item(item_id, brand_ids));
			// deal with brands extracted from current line
			for (String bid : brand_ids) {

				if (!bid.equalsIgnoreCase("NA")) {
					// if this is a new brand then create a new brand obj  for it
					if (!loaded_brands.contains(bid)) {
						brands.add(new Brand(bid));
						loaded_brands.add(bid);
					}
					// Add item_id to the list of items produced by the brand
					int bIndex = Brand.toIndex(bid);
					if (bIndex >= 0) {
						brands.get(bIndex).getItemIds().add(item_id);
					}
				} 
			}
			
			
		}
		br.close();
//		finish loading data from fMemberRel
		ds.itemDict = new Alphabet(items.toArray());
		ds.brandDict = new Alphabet(brands.toArray());
		
	}

}
