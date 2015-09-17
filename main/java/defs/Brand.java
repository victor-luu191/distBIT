package defs;

import java.util.HashSet;
import java.util.Set;

public class Brand {
	public String id;
	private Set<String> item_ids;	// items this brand produces
	
	public Brand(String brandId) {
		
		id = brandId;
		item_ids = new HashSet<String>();
	}

	public Brand(String id, Set<String> item_ids) {
		super();
		this.id = id;
		this.item_ids = item_ids;
	}

	@Override
	public String toString() {
		return "Brand [id=" + id + ", item_ids=" + item_ids + "]";
	}
	
	public boolean inProducers(Item item) {
		return item_ids.contains(item.id);
	}
}
