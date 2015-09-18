package utils;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.petuum.jbosen.PsTableGroup;
import org.petuum.jbosen.row.double_.DenseDoubleRowUpdate;
import org.petuum.jbosen.row.double_.DoubleColumnIterator;
import org.petuum.jbosen.row.double_.DoubleRow;
import org.petuum.jbosen.row.double_.DoubleRowUpdate;
import org.petuum.jbosen.table.DoubleTable;

public class Converters {

	public static RealVector toVector(DoubleRow row) {
		// TODO Auto-generated method stub
		RealVector v = new ArrayRealVector();
		DoubleColumnIterator iter = row.iterator();
		iter.advance();
		while (iter.hasNext()) {
			v.append(iter.getValue());
		}
		
		return v;
	}

	public static RealVector arr2Vector(double[] arr) {
		
		return new ArrayRealVector(arr);
	}

	public static DoubleTable toTable(double[][] arr, int tableId) {
		int staleness = 0;
		int numCol = arr[0].length;
		
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
