package defs;

import java.util.ArrayList;

public class Instance {
	public String uId;
	private ArrayList<String> item_ids;

	public Instance(String uId) {
		this.uId = uId;
		setItemIds(new ArrayList<String>());
	}

	public Instance(String uId, ArrayList<String> item_ids) {
		this.uId = uId;
		this.setItemIds(item_ids);
	}

	public ArrayList<String> getItemIds() {
		return item_ids;
	}

	public void setItemIds(ArrayList<String> item_ids) {
		this.item_ids = item_ids;
	}
}
