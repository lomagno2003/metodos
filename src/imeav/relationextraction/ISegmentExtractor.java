package imeav.relationextraction;

import imeav.utilities.Vec4i;

import java.util.List;

import org.opencv.core.Mat;

public interface ISegmentExtractor {

	/**
	 * Given a binary Mat and a boxes Mat, this method returns all the lines
	 * contained in it.
	 * 
	 * @param binaria
	 * @param cajas
	 * @return
	 */
	public abstract List<Vec4i> extractLines(Mat binaria, Mat cajas);

}