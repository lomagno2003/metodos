package imeav.textseparation;

import org.opencv.core.*;

public interface ITextSeparator {
	public Mat getText(Mat imagen);
	
	public Mat eraseText(Mat input, Mat text);
};
