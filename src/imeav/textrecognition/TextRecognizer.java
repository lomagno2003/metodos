package imeav.textrecognition;

import java.io.IOException;
import java.util.Vector;

import org.opencv.core.*;
import org.opencv.highgui.*;
import org.opencv.imgproc.*;

import imeav.utilities.TextBox;



public abstract class TextRecognizer
{
public abstract Vector<TextBox> getText(Mat original, Mat areas);

};
