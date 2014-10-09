package imeav.preprocessing;

import org.opencv.core.*;

public interface IPreprocessor {
	public Mat preprocess(Mat input);
}
