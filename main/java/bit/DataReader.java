package bit;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import cc.mallet.types.Alphabet;
import defs.AdoptHistory;
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

	/**
	 * build adopt histories and userDict for {@code ds} 
	 * @param fAdopt
	 * @param ds
	 * @throws IOException 
	 */
	private static void loadAdopts(String fAdopt, DataSet ds) throws IOException {
		// TODO Auto-generated method stub
		BufferedReader br = new BufferedReader(new FileReader(fAdopt));
		String line = br.readLine();	// skip header line
		while ((line = br.readLine()) != null) {
			String[] record = line.split(",");
			String uid = record[0]; 
			
			int last = record.length - 1;
			String item_id = record[last].trim() ;
			boolean addIfNotPresent = true;
//			lookup user in the dictionary and 
//			if he is not present yet, add him  and initialize his history with the first item
			int userIndex = ds.userDict.lookupIndex(uid, addIfNotPresent);	
			if (userIndex == -1) {// new user
				ds.histories.add(new AdoptHistory(uid, item_id));
			}
			else {// if he is already present, append the item to the list of items adopted by the user
				ArrayList<String> adoptedItems = ds.histories.get(userIndex).getItemIds();
				adoptedItems.add(item_id);
			}
		}
		br.close();
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
