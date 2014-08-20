package imeav.binarization;

import org.opencv.core.*;
import org.opencv.highgui.*;
import org.opencv.imgproc.*;

public abstract class Binarizer 
{
	public abstract Mat binarize(Mat input);
};


