package imeav.utilities;

import java.util.Comparator;

/**
 * Este comparador compara 2 vectores de tipo Vec4i.
 * 
 * Al usar este comparador para ordenar una lista, la misma quedara ordenada de
 * menor a mayor.
 * 
 * @author clomagno
 *
 */
public class SegmentLengthComparator implements Comparator<Vec4i> {
	/**
	 * Este metodo compara 2 vectores de tipo Vec4i. En el caso de que el modulo
	 * de s1 sea menor que s2, retorna true, en caso contrario retorna false.
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	private boolean menor(Vec4i s1, Vec4i s2) {
		return Math.pow(s1.v0 - s1.v2, 2) + Math.pow(s1.v1 - s1.v3, 2) < Math
				.pow(s2.v0 - s2.v2, 2) + Math.pow(s2.v1 - s2.v3, 2);
	}

	@Override
	public int compare(Vec4i o1, Vec4i o2) {
		if (menor(o1, o2))
			return -1;
		if (menor(o2, o1))
			return 1;
		return 0;
	}
}