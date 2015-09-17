package org.gradle;

import org.apache.commons.math3.linear.RealVector;
import org.junit.Test;
import org.petuum.jbosen.PsTableGroup;
import org.petuum.jbosen.row.double_.DoubleRow;
import org.petuum.jbosen.table.DoubleTable;

import utils.Converters;

public class TableTest {
	@Test
	public void canConvert2Vector() {
		int tableId = 0;
		int staleness = 0;
		int numCol = 2;
		PsTableGroup.createDenseDoubleTable(tableId, staleness, numCol);
		DoubleTable table = PsTableGroup.getDoubleTable(tableId);
		DoubleRow r0 = table.get(0);
		r0.inc(0, 10);
		System.out.println(r0.toString());
		System.out.println(table.get(0).toString());
		
//		assert(Converters.toVector(table.get(0)) instanceof RealVector);
	}
}
