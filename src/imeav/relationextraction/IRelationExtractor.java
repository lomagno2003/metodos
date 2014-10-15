package imeav.relationextraction;

import java.util.Vector;

import org.opencv.core.*;
import imeav.utilities.Relation;


public interface IRelationExtractor
{

	public Vector<Relation> extract(Mat binary, Mat boxes);
};
