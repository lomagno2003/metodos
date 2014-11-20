package imeav.extractor;

import imeav.utilities.Vec4i;

import java.util.Vector;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

public class DrawPath {
	
	public DrawPath(){
		
	}
	
	
	public void execDrawPath(Mat m, Vector<Vec4i> path) {
		for (int i=0;i<path.size();i++){
			Vec4i l = path.get(i);
			Core.line( m, new Point(l.v0, l.v1), new Point(l.v2, l.v3), new Scalar(255), 1, Core.LINE_AA,0);
		}
		
	}

}
