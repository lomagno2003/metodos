package imeav.preprocessing;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;


public class BorderExtender extends Preprocessor
{

	public BorderExtender() {
		factor=2;
	    value=new Scalar(255);
	    
	}
	public Mat preprocess(Mat input) {
	    Mat inputGray=new Mat(input.size(),CvType.CV_8UC1);
	    //convertir a escala de grises
	    Imgproc.cvtColor(input,inputGray,Imgproc.COLOR_BGR2GRAY);/////////////////
	    
	    //agrandar bordes
	    Mat agrandada = new Mat(input.rows()+4,input.cols()+4,CvType.CV_8UC1,new Scalar(255));//(imagen.rows+2,imagen.cols+2,CV_8UC1,Scalar(255));
	    Imgproc.copyMakeBorder(inputGray,agrandada,factor,factor,factor,factor,Imgproc.BORDER_CONSTANT,value);

	    
	    return agrandada;
	}

	private int factor;
	private Scalar value;

};
