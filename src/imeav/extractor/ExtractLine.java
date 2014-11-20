package imeav.extractor;

import imeav.utilities.Vec4i;

import java.util.Vector;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class ExtractLine {
	private Mat cajasDilat;
	private double inclusionRatio;

	public Vector<Vec4i> extractLines(Mat binaria, Mat cajas){
		Imgproc.morphologyEx(cajas,cajasDilat,Imgproc.MORPH_DILATE,Imgproc.getStructuringElement(Imgproc.MORPH_CROSS,new org.opencv.core.Size(5,5)));

		 Mat notbin = new Mat();
		 Core.bitwise_not(binaria,notbin);
		 Mat resultadoHough=Mat.zeros(cajas.rows(),cajas.cols(),CvType.CV_8U);
		 Mat lines = new Mat();
		 Mat lineasSelec=Mat.zeros(cajas.rows(),cajas.cols(),CvType.CV_8U);

		 Vector<Vec4i> listaLineasSelec = new Vector<Vec4i>();

		 Imgproc.HoughLinesP(notbin, lines, 0.5, Math.PI/2000, 10, 20, 10 );
		   for( int i = 0; i < lines.cols(); i++ )
		   {
			   //nombre representativo
			   vectoresPasarOrigen(lines, resultadoHough, cajas, lineasSelec, listaLineasSelec, i);
		   }

		   return listaLineasSelec;
		
		}
	
	private void vectoresPasarOrigen(Mat lines,Mat resultadoHough,Mat cajas,Mat lineasSelec,Vector<Vec4i> listaLineasSelec,int index){

		   //obtiene un arreglo de double
		   double[] lp= lines.get(0,index);
		   //transforma el arreglo en un vector de 4 dimenciones
		   Vec4i l=new Vec4i((int)lp[0],(int)lp[1],(int)lp[2],(int)lp[3]);
	     
	     //OJO SHIFT
	     Core.line( resultadoHough, new Point(l.v0, l.v1), new Point(l.v2, l.v3), new Scalar(255), 1, Core.LINE_AA,0);
	     //Crea una matriz de ceros
	    Mat unaLinea=Mat.zeros(cajas.rows(),cajas.cols(),CvType.CV_8U);
	    Core.line( unaLinea, new Point(l.v0, l.v1), new Point(l.v2, l.v3), new Scalar(255), 1, Core.LINE_AA,0);

	    int areaLinea = Core.countNonZero(unaLinea);

	    Mat conjuncion = new Mat();
	    Core.bitwise_and(unaLinea,cajasDilat,conjuncion);
	    if (Core.countNonZero(conjuncion)/(double)areaLinea<inclusionRatio){
	        Core.line( lineasSelec, new Point(l.v0, l.v1), new Point(l.v2, l.v3), new Scalar(255), 1, Core.LINE_AA , 0);
	        listaLineasSelec.add(l);
	    }
	   
	}
	
}
