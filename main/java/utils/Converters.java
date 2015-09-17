package utils;

import org.apache.commons.math3.linear.RealVector;
import org.petuum.jbosen.PsTableGroup;
import org.petuum.jbosen.row.Row;
import org.petuum.jbosen.row.double_.DenseDoubleRowUpdate;
import org.petuum.jbosen.row.double_.DoubleRowUpdate;
import org.petuum.jbosen.table.DoubleTable;

public class Converters {

	public static RealVector toVector(Row row) {
		// TODO Auto-generated method stub
		return null;
	}

	public static RealVector arr2Vector(double[] arr) {
		// TODO Auto-generated method stub
		return null;
	}

	public static DoubleTable toTable(double[][] arr, int tableId) {
		int staleness = 0;
		int numCol = arr[0].length;
		// TODO Auto-generated method stub
		PsTableGroup.createDenseDoubleTable(tableId, staleness, numCol);
		DoubleTable table = PsTableGroup.getDoubleTable(tableId);
		int numRow = arr.length;
		for (int row = 0; row < numRow; row++) {
			DoubleRowUpdate rowUpdate = new DenseDoubleRowUpdate(numCol);
			for (int col = 0; col < numCol; col++) {
				rowUpdate.set(col, arr[row][col]);
			}
			table.inc(row, rowUpdate);
		}
		return table;
	}
	
}
