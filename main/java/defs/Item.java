package defs;

import java.util.*;

public class Item {
	String id;	
	private Set<String> producers;	// brands (co-)producing the item
	
	public Item(String id, Set<String> brandIds) {
		super();
		this.id = id;
		this.producers = brandIds;
	}
	
	public Set<String> getBrands() {
		return producers;
	}

	public void setBrands(Set<String> brandIds) {
		this.producers = brandIds;
	}

	public boolean belongTo(String brandId) {
		return producers.contains(brandId);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Item other = (Item) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Item id " + id + " belongs to brands: " + producers.toString();
	}
	
}
