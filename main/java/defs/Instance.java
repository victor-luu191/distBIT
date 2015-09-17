package defs;

import java.util.ArrayList;

public class Instance {
	public String uId;
	private ArrayList<String> item_ids;

	public Instance() {
		setItemIds(new ArrayList<String>());
	}

	public Instance(ArrayList<String> item_ids) {
		this.setItemIds(item_ids);
	}

	public ArrayList<String> getItemIds() {
		return item_ids;
	}

	public void setItemIds(ArrayList<String> item_ids) {
		this.item_ids = item_ids;
	}
}
