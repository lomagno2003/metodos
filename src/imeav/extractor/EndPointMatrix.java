package imeav.extractor;

import imeav.utilities.Vec4i;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class EndPointMatrix {
	
	private int endpointMatrixCols;
	private int endpointMatrixRows;

	public EndPointMatrix(int _endpointMatrixCols,int _endpointMatrixRows){
		this.setEndpointMatrixCols(0);
		this.setEndpointMatrixRows(0);
	}
	
	public List< Vector<Integer> > createEndpointMatrix(Vector<Vec4i> listaLineasSelec, int rows, int cols) {
		
		setEndpointMatrixCols(cols);
		setEndpointMatrixRows(rows);
		
		List<Vector<Integer>> matriz = new ArrayList< Vector<Integer> >();
	    for (int i=0;i<rows;i++)
	        for (int j=0;j<cols;j++){
	        	matriz.add(new Vector<Integer>());
	        }

	    for (int i=0;i<listaLineasSelec.size();i++){
	        Vec4i l = listaLineasSelec.get(i);

	        matriz.get( l.v1*cols + l.v0 ).add(new Integer(i));   
	        matriz.get( l.v3*cols + l.v2 ).add(new Integer(i));
	    }

	    return matriz;
	}

	public int getEndpointMatrixCols() {
		return endpointMatrixCols;
	}

	public void setEndpointMatrixCols(int endpointMatrixCols) {
		this.endpointMatrixCols = endpointMatrixCols;
	}

	public int getEndpointMatrixRows() {
		return endpointMatrixRows;
	}

	public void setEndpointMatrixRows(int endpointMatrixRows) {
		this.endpointMatrixRows = endpointMatrixRows;
	}
	
}
