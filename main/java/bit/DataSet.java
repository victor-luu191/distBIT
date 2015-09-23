package bit;

import java.util.ArrayList;

import cc.mallet.types.Alphabet;
import defs.AdoptHistory;
import defs.Item;

public class DataSet {
	public ArrayList<AdoptHistory> histories ;
	// Create maps from Id to index as dictionaries
	public Alphabet userDict;
	public Alphabet itemDict;
	public Alphabet brandDict;
	
	public DataSet() {
		histories = new ArrayList<AdoptHistory>();
		userDict = new Alphabet(String.class);
		itemDict = new Alphabet(String.class);
		brandDict = new Alphabet(String.class);
	}

	public int numAdopts() {
		// TODO Auto-generated method stub
		return 0;
	}
}
