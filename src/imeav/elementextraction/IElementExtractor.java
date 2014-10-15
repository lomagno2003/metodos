package imeav.elementextraction;

import java.util.Vector;

import org.opencv.core.*;
import imeav.utilities.Element;

public abstract class IElementExtractor
{
	public abstract Vector<Element> getBoxes();
	public abstract Mat paintBoxes();
};
