package defs;

import java.util.ArrayList;

public class AdoptHistory {
	public String uId;
	private ArrayList<String> item_ids;

	public AdoptHistory(String uId) {
		this.uId = uId;
		setItemIds(new ArrayList<String>());
	}

	public AdoptHistory(String uId, ArrayList<String> item_ids) {
		this.uId = uId;
		this.setItemIds(item_ids);
	}

	public AdoptHistory(String uId, String firstItemId) {
		this.uId = uId;
		setItemIds(new ArrayList<String>());
		item_ids.add(firstItemId);
	}

	public ArrayList<String> getItemIds() {
		return item_ids;
	}

	public void setItemIds(ArrayList<String> item_ids) {
		this.item_ids = item_ids;
	}
}
