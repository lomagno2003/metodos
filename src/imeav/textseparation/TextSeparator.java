package imeav.textseparation;

import org.opencv.core.*;
import org.opencv.highgui.*;
import org.opencv.imgproc.*;

public abstract class TextSeparator
{
    public abstract Mat getText(Mat imagen);
    public abstract Mat eraseText(Mat input, Mat text);
};
