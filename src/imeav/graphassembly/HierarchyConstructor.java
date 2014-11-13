package imeav.graphassembly;

import imeav.utilities.Element;

import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class HierarchyConstructor {
	private Size size;

	public HierarchyConstructor(Size size) {
		super();
		this.size = size;
	}

	public void applyHierarchy(List<Element> boxes){
		for (int i = 0; i < boxes.size(); i++)
			for (int j = i + 1; j < boxes.size(); j++) {
				apply(boxes.get(i), boxes.get(j));
			}
	}
	
	private void apply(Element i, Element j){
		Mat iMat = Mat.zeros(size, CvType.CV_8UC1);
		Mat jMat = Mat.zeros(size, CvType.CV_8UC1);
		Rect iR = Imgproc.boundingRect(i.getPoints());
		Rect jR = Imgproc.boundingRect(j.getPoints());
		Core.rectangle(iMat, iR.tl(), iR.br(), new Scalar(255), -1);
		Core.rectangle(jMat, jR.tl(), jR.br(), new Scalar(255), -1);

		Mat And = Mat.zeros(size, CvType.CV_8UC1);
		Core.bitwise_and(iMat, jMat, And);

		if (Core.countNonZero(And) == 0)
			return;

		if (iR.width > jR.width) {
			// cout<<"I contiene a J"<<to_string(i.getId())<<endl;
			j.setPadre(i.getId());
		} else {
			// cout<<"J contiene a I "<<to_string(j.getId())<<endl;
			i.setPadre(j.getId());
		}
		// imshow("I",iMat);imshow("J",jMat);imshow("AND",And);waitKey(0);
		return;
	}
}
