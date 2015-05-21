package imeav.elementextraction;

import java.util.Vector;

import org.opencv.core.*;
import org.opencv.highgui.*;
import org.opencv.imgproc.*;

import imeav.utilities.Element;

public interface IElementExtractor
{
	public Vector<Element> getBoxes();
	public Mat paintBoxes();

};
