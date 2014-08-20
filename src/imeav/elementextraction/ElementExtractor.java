package imeav.elementextraction;

import java.util.Vector;

import org.opencv.core.*;
import org.opencv.highgui.*;
import org.opencv.imgproc.*;

import imeav.utilities.Element;

public abstract class ElementExtractor
{


	public abstract Vector<Element> getBoxes();
	public abstract Mat paintBoxes();

};
