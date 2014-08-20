package imeav.relationextraction;

import java.io.FileNotFoundException;
import java.util.Vector;

import org.opencv.core.*;
import org.opencv.highgui.*;
import org.opencv.imgproc.*;

import imeav.utilities.Relation;


public abstract class RelationExtractor
{

	public abstract Vector<Relation> extract(Mat binary, Mat boxes);
};
