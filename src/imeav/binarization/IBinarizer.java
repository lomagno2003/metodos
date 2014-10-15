package imeav.binarization;

import org.opencv.core.*;

public interface IBinarizer {
	/**
	 * TODO
	 * 
	 * @param input
	 * @return
	 */
	public Mat binarize(Mat input);
};
