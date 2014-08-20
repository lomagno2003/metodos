package imeav.preprocessing;

import org.opencv.core.*;
import org.opencv.highgui.*;
import org.opencv.imgproc.*;

public abstract class Preprocessor
{
public abstract Mat preprocess(Mat input);
}
