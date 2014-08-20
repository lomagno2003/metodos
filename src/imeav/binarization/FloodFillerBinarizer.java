package imeav.binarization;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.*;
import org.opencv.highgui.*;
import org.opencv.imgproc.*;
import org.opencv.utils.Converters;

public class FloodFillerBinarizer extends Binarizer
{

	public FloodFillerBinarizer(int threshold, double elongatednessFactor, int pixels_min) {
		thresh=threshold;
		ELONGATEDNESS_FACTOR=elongatednessFactor;
		PIXELS_MIN=pixels_min;
	}

	public Mat binarize(Mat input) {
		Mat floodFilled=input.clone();
		Mat seedMask=Mat.zeros(input.rows() + 2, input.cols() + 2,CvType.CV_8UC1);
		double pixels=floodFilled.rows()*floodFilled.cols();
		Point seed=new Point(0,0);
		//vector<vector<Point>> contornos;

		Mat bordes=Mat.ones(input.rows() , input.cols() ,CvType.CV_8UC1);//*255
		//bordes.mul(bordes, 255);//OJO
		bordes.setTo(new Scalar(255));
		
		/*for (int yy=0;yy<bordes.rows();yy++)
			for (int xx=0;xx<bordes.cols();xx++){
				
				bordes.set
			}*/
		
		
		boolean primerCC=true;

		while (true){
			if (seed.x==-1)//Ya hizo floodfill de toda la imagen
				break;

			Scalar newVal;
			if (input.get((int)seed.y,(int)seed.x)[0]<=thresh)
				newVal=new Scalar(0);
			else
				newVal=new Scalar(255);

			Mat previousSeedMask=seedMask.clone();
			Mat xorr = new Mat();

			Imgproc.floodFill(floodFilled,seedMask,seed,newVal,null,new Scalar(8),new Scalar(8),8 | (255 << 8));



			
			
			
			Core.bitwise_xor(seedMask,previousSeedMask,xorr);

			if (newVal.val[0]==255 && !primerCC){
				//imshow("floo",bordes);waitKey(0);
				buscarBordes(xorr,pixels,bordes);
			}
			else
				primerCC=false;


			//Buscar proxima semilla
			seed=nextSeed(seedMask,seed);
		}



		Mat salida = new Mat();
		Core.bitwise_and(floodFilled,bordes,salida);
		return salida;
	}


	private    int thresh;

	private Point nextSeed(Mat seedMask,Point seed) {
		Point next=new Point(-1,-1);


		for (int y=(int) (seed.y+1);y<seedMask.rows()-1;y++)
			for (int x=1;x<seedMask.cols()-1;x++)
				if (seedMask.get(y,x)[0]==0)
					return new Point(x-1,y-1);
		return next;
	}
	private boolean elongatedObject(Mat xorr) {
		Mat puntos=new Mat();
		Core.findNonZero(xorr,puntos);

		MatOfPoint p=new MatOfPoint(puntos);
		Rect minBoundingRect=Imgproc.boundingRect(p);

		/*//Mostrar el rectangle que genera
		Mat nuevaMat=Mat::zeros(xorr.size(),CV_8UC1);
		rectangle(nuevaMat,minBoundingRect,Scalar(255));
		imshow("nvnn",nuevaMat);waitKey(0);*/
		//cout<<to_string(minBoundingRect.width)<<"   "<<to_string(minBoundingRect.height)<<endl;
		double w=minBoundingRect.width;
		double h=minBoundingRect.height;
		if (w<h){
			//cout<<to_string(w/h)<<endl;
			if (w / h > ELONGATEDNESS_FACTOR)
				return false;
		}
		else{
			//cout<<to_string(h/w)<<endl;
			if (h / w > ELONGATEDNESS_FACTOR)
				return false;
		}

		return true;
	}
	private  void buscarBordes(Mat xorr, double pixels,Mat bordes) {
		//Puntos de nonZeroValues estan desplazados

		if (Core.countNonZero(xorr) < PIXELS_MIN)
			return;

		//Mat puntos;
		//findNonZero(xorr,puntos);
		//Mat nuevaMat=Mat::zeros(xorr.size(),CV_8UC1);
		//rectangle(nuevaMat,minBoundingRect,Scalar(255));
		//imshow("XOR!",xorr);
		//cout<<to_string(countNonZero(xorr))<<endl;
		//waitKey(0);

		if (elongatedObject(xorr)){
			//cout<<"Elongated object!"<<endl;waitKey(0);
			return;}


		/*
		imshow("ELONGATEDNESS",xorr);
		if (elongatedObject(xorr))
		cout<<"Elongated"<<endl;
		else
		cout<<"No elon"<<endl;
		*/
		//waitKey(0);

		

		//findContours de puntos desplazados en uno

		//Vector<Vector<Point> > contours;Vector<Vec4i> hierarchy;

		Mat xorDilate=xorr.clone();
		Imgproc.morphologyEx(xorDilate,xorDilate,Imgproc.MORPH_DILATE,Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,new org.opencv.core.Size(3,3)));

		List<MatOfPoint> contours=new ArrayList<MatOfPoint>();
		Mat hierarchy=new Mat();
		Imgproc.findContours(xorDilate,contours,hierarchy,Imgproc.RETR_CCOMP,Imgproc.CHAIN_APPROX_NONE,new Point(-1,-1));

		if (!contours.get(0).empty())//OJO ACA, ANTES HACIA .SIZE() > 0
			Imgproc.drawContours(bordes,contours,-1,new Scalar(0));


		return;
	}

    //constantes
	private double ELONGATEDNESS_FACTOR;
	private int PIXELS_MIN;

};
