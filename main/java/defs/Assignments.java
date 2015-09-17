package defs;

import java.util.ArrayList;

public class Assignments {
	public ArrayList<ArrayList<Integer>> topic;
	public ArrayList<ArrayList<Integer>> brand;
	public ArrayList<ArrayList<Boolean>> decision;
	
	public Assignments() {
		topic = new ArrayList<ArrayList<Integer>>();
		brand = new ArrayList<ArrayList<Integer>>();
		decision = new ArrayList<ArrayList<Boolean>>();
	}

	public Assignments(ArrayList<ArrayList<Integer>> topicIndex,
			ArrayList<ArrayList<Integer>> brandIndex,
			ArrayList<ArrayList<Boolean>> decision) {
		super();
		this.topic = topicIndex;
		this.brand = brandIndex;
		this.decision = decision;
	}
	
	
}
