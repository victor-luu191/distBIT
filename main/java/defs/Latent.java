package defs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Latent {
	public Map<Integer, List<Integer>> topics;
	public Map<Integer, List<Integer>> brands;
	public Map<Integer, List<Integer>> decisions;
	
	public Latent() {
		
		topics = new HashMap<Integer, List<Integer>>();
		brands = new HashMap<Integer, List<Integer>>();
		decisions = new HashMap<Integer, List<Integer>>();
	}
	
	
	
	
}
