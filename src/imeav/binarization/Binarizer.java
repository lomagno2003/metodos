package imeav.binarization;

import org.opencv.core.*;

public interface Binarizer {
	/**
	 * TODO Ver que poronga hace este binarizer
	 * 
	 * @param input
	 * @return
	 */
	public Mat binarize(Mat input);
};
