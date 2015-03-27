package imeav.relationextraction.pointclassifier;

import org.opencv.core.Mat;

/**
 * 
 * @author clomagno
 *
 */
public interface IPointClassifier {
	/**
	 * Este metodo retorna la clase de la matris mat predecida por el
	 * clasificador
	 * 
	 * @param mat
	 * @return
	 */
	public int classify(Mat mat);
}
