package defs;

import java.util.HashSet;
import java.util.Set;

public class Brand {
	public String id;
	private Set<String> products;	
	
	public Brand(String brandId) {
		
		id = brandId;
		products = new HashSet<String>();
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
		Brand other = (Brand) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public Brand(String id, Set<String> item_ids) {
		super();
		this.id = id;
		this.products = item_ids;
	}

	@Override
	public String toString() {
		return "Brand [id=" + id + ", item_ids=" + products + "]";
	}
	
}
