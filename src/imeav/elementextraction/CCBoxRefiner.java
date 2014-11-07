package imeav.elementextraction;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.opencv.core.*;
import org.opencv.highgui.*;
import org.opencv.imgproc.*;
import org.opencv.utils.Converters;

import imeav.utilities.Element;


public class CCBoxRefiner
{
	private Size size;
	private List<MatOfPoint> connectedBoxes;
	private List<MatOfPoint> filteredFP;
	private List< Integer > agregados;
	
	public CCBoxRefiner() {
		connectedBoxes=new ArrayList<MatOfPoint>();
		filteredFP=new ArrayList<MatOfPoint>();
		agregados=new ArrayList<Integer>();
	}

	public void connectBoxes(Mat input) {

	    size=input.size();
		List<MatOfPoint> blobs=new ArrayList<MatOfPoint>();
		FindBlobs(input,blobs);


		Vector<ElementCheck> Checkeos = new Vector<ElementCheck>();
		Vector<Integer> noAnalizar = new Vector<Integer>();
		for (int i=0;i<blobs.size();i++){//Por cada caja

			boolean connecting=false;
			//Vector<Point> ORcontour;

			MatOfPoint blobsI = new MatOfPoint();
			if (!checkReplacement(i,blobsI,Checkeos))
				blobsI=blobs.get(i);

			MatOfPoint ORcontour=new MatOfPoint();
			//j=i+1
			for (int j=i+1;j<blobs.size();j++){//Par de cajas
				//Por cada par de cajas, si esta conectada ya esta
				//ORcontour nuevo contorno de unir las dos cajas
				
				//Converters.
				if (connectedRects(blobs.get(i),blobs.get(j),input.size(),ORcontour,i,j)){
					noAnalizar.add(j);noAnalizar.add(i);
					ElementCheck c = new ElementCheck();
					c.setI(i);
					c.setJ(j);
					c.setPunt(ORcontour);
					Checkeos.add(c);
					connecting=true;
				}

			}

			if (connecting)//Si estan conectados, agregar el contorno resultante
				connectedBoxes.add(ORcontour);

		}


		for (int i=0;i<blobs.size();i++){
			boolean continuar=false;

			for (int g=0;g<noAnalizar.size();g++){
				if (i==noAnalizar.get(g).intValue())
					continuar=true;
			}
			if (continuar) //El blob ya fue agregado, porque se unio con algun otro
				continue;

			//Si no fue agregado, agregar el contorno del blob
			Mat cont=Mat.zeros(input.size(),CvType.CV_8UC1);
			//Vector<Vec4i> hierarchy;
	        List<MatOfPoint> contornos = new ArrayList<MatOfPoint>();
			//Vector< Vector<Point> > outputContours;
			contornos.add(blobs.get(i));
			Imgproc.drawContours(cont,contornos,0,new Scalar(255));
			Mat hierarchy=new Mat();
			List<MatOfPoint> outputContours=new ArrayList<MatOfPoint>();
			Imgproc.findContours(cont,outputContours,hierarchy,Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_NONE);

			//Agregar la caja aislada a ambas; Cajas conectadas y Cajas filtradas
			connectedBoxes.add(outputContours.get(0));
			if (!isIn(i))
				filteredFP.add(outputContours.get(0));

		}

		//Repetidos?


	}


	public Vector<Element> getBoxes() {

	    Vector<Element> boxes=new Vector<Element>();
	    for (int i=0;i<connectedBoxes.size();i++){
	        Element b = new Element();
	        b.setPoints(connectedBoxes.get(i));
			b.setId(i);
	        boxes.add(b);
	    }
	    return boxes;
	}

	public Mat paintBoxes() {

	    Mat retorno=Mat.zeros(size,CvType.CV_8UC1);
	    Imgproc.drawContours(retorno,filteredFP,-1,new Scalar(255));
	    return retorno;
	}

	
	private boolean connectedRects(MatOfPoint i,MatOfPoint j,Size size,MatOfPoint ORcontour,int I,int J) {

		Mat rectsI=Mat.zeros(size,CvType.CV_8UC1);Mat rectsJ=Mat.zeros(size,CvType.CV_8UC1);
	    List<MatOfPoint> contornos = new ArrayList<MatOfPoint>();
	    

	    contornos.add(i);
	    contornos.add(j);
		Imgproc.drawContours(rectsI,contornos,0,new Scalar(255));
		Imgproc.drawContours(rectsJ,contornos,1,new Scalar(255));


		if (superPuestos(rectsI,rectsJ))
			return false;

		//OR
		Mat OR=Mat.zeros(size,CvType.CV_8UC1);
		Core.bitwise_or(rectsI,rectsJ,OR);


		//Mat of point a vector<Point>
		//Converters
		int horizontal = checkHorizontalUnion(OR,contornos,rectsI,rectsJ,ORcontour,I,J);
		if (horizontal!=-1){
			return true;
		}


		//Dilatacion en sentido vertical
		Imgproc.morphologyEx(OR,OR,Imgproc.MORPH_DILATE,Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,new org.opencv.core.Size(1,3)));

	    List<MatOfPoint> blob=new ArrayList<MatOfPoint>();
		//Vector< Vector<Point> > blob;
		FindBlobs(OR,blob);
		if (blob.size()==1){ //Con la dilatacion vertical se convirtieron en un solo BLOB, hay que unirlos

			int filtrar=filtrarCajaFP(contornos,size);
			//Vector<Vec4i> hierarchy;
			Mat hierarchy=new Mat();
			blob.clear();
			//contorno i es 0, contorno j es 1
			if (filtrar==-1){//Ningun contorno que filtrar!
				//SACAR EL CONTORNO DEL OR GENERADO, ESE Vector DE PUNTOS ES EL QUE QUEDA
				Imgproc.morphologyEx(OR,OR,Imgproc.MORPH_ERODE,Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,new org.opencv.core.Size(1,3)));
				Imgproc.findContours(OR,blob,hierarchy,Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_NONE,new Point(0,0));
				//for (int i1=0;i1<blob.get(0).cols();i1++)
				//ORcontour.add(blob.get(0).get(0,i1));
				ORcontour.push_back(blob.get(0));
				

				if (!isIn(I)){
					Imgproc.findContours(rectsI,blob,hierarchy,Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_NONE);
					filteredFP.add(blob.get(0));
					blob.clear();
				}
				if (!isIn(J)){
					Imgproc.findContours(rectsJ,blob,hierarchy,Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_NONE);
					filteredFP.add(blob.get(0));
					blob.clear();
				}

			}
			else if (filtrar==1){//Filtrar contorno 0 == i
				//if (isIn(J))
				Imgproc.findContours(rectsJ,blob,hierarchy,Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_NONE);
				if (!isIn(J))
					filteredFP.add(blob.get(0));
				//for (int i1=0;i1<blob.get(0).size();i1++)
					//ORcontour.add(blob.get(0).get(i1));
				ORcontour.push_back(blob.get(0));
			}
			else {//Filtrar contorno 1 == j
				//if (isIn(I))
				Imgproc.findContours(rectsI,blob,hierarchy,Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_NONE);
				if (!isIn(I))
					filteredFP.add(blob.get(0));
				//for (int i1=0;i1<blob.get(0).size();i1++)
					//ORcontour.add(blob.get(0).get(0,i1));
				ORcontour.push_back(blob.get(0));
			}

			return true;
		}

		return false;//No estan conectados
	}

	private void FindBlobs(Mat binary,List<MatOfPoint> blobs) {
		blobs.clear();

		// Fill the label_image with the blobs
		// 0  - background
		// 1  - unlabelled foreground
		// 2+ - labelled foreground

		Mat label_image=new Mat();
		binary.convertTo(label_image, CvType.CV_32SC1);

		int label_count = 2; // starts at 2 because 0,1 are used already

		//int[] data = null;
		
		for(int y=0; y < label_image.rows(); y++) {
			//int *row = (int*)label_image.ptr(y);
			for(int x=0; x < label_image.cols(); x++) {
				//if(row[x] != 255) {
				if(label_image.get(y, x)[0] != 255) {
					continue;
				}
				

				Rect rect = new Rect();
				Mat mask=new Mat();
				Imgproc.floodFill(label_image, mask, new Point(x,y), new Scalar(label_count), rect, new Scalar(0), 
						new Scalar(0), 8);
				
				
				
				List <Point> blob = new ArrayList <Point> ();

				for(int i=rect.y; i < (rect.y+rect.height); i++) {
					//int *row2 = (int*)label_image.ptr(i);
					for(int j=rect.x; j < (rect.x+rect.width); j++) {
						//if(row2[j] != label_count) {
						if(label_image.get(i, j)[0] != label_count) {
							continue;
						}

						blob.add(new Point(j,i));
					}
				}

				//Mat m=Converters.vector_Point2d_to_Mat(blob);
				MatOfPoint mp= new MatOfPoint();
				mp.fromList(blob);
				//blobs.add(new MatOfPoint(Converters.vector_Point2d_to_Mat(blob)));//OJO!!!
				blobs.add(mp);

				label_count++;
			}
		}
	}


	private boolean superPuestos(Mat rectsI,Mat rectsJ) {

		Mat AND=new Mat();
		Core.bitwise_and(rectsI,rectsJ,AND);
		if (Core.countNonZero(AND)>0) //Comparten area, una contiene la otra
			return true;
		return false;
	}
	
	private int filtrarCajaFP(List<MatOfPoint> contornos,Size size) {
		//Se unian, ver si hay que filtrar una de las dos. Tener en cuenta que no se superponian! (por eso llego aca)
		//Cuando la caja de arriba es bastante mas grande que la de abajo, filtrar

		Rect rI=Imgproc.boundingRect(contornos.get(0));
		Rect rJ=Imgproc.boundingRect(contornos.get(1));

		int ArribaDe=arribaDe(rI,rJ);
		//cout<<"Rect mas arriba"<<to_string(ArribaDe)<<endl;

		double nonZeroI=rI.width*rI.height;//countNonZero(rectI);
		double nonZeroJ=rJ.width*rJ.height;//countNonZero(rectJ);
		double relacionArea=0.4;

		if (ArribaDe==0){
			//rI arriba de rJ
			//cout<<"Relacion area: "<<to_string(nonZeroJ / nonZeroI)<<endl;
			if (nonZeroJ / nonZeroI < relacionArea)
				return 0;
		}
		else {
			//rJ arriba de rI
			//cout<<"Relacion area: "<<to_string(nonZeroI / nonZeroJ)<<endl;
			if (nonZeroI / nonZeroJ < relacionArea)
				return 1;
		}

		return -1;
	}
	
	private int arribaDe(Rect rI,Rect rJ) {
		if (rI.y < rJ.y)
			return 0;
		return 1;
	}
	
	private int izquierdaDe(Rect rI,Rect rJ) {
		return 0;
	}
	
    private boolean checkReplacement(int i,MatOfPoint blobsI,Vector<ElementCheck> Checkeos) {

		for (int g=0;g<Checkeos.size();g++){
			if (i==Checkeos.get(g).getJ()){
				MatOfPoint punt=Checkeos.get(g).getPunt();
				//for (int i1=0;i1<punt.size();i1++)
					//blobsI.add(punt.get(i1));
				blobsI.push_back(punt);
				return true;
			}
		}
		return false;
	}

	private int checkHorizontalUnion(Mat OR,List<MatOfPoint> contornos,Mat rectsI,Mat rectsJ,MatOfPoint ORcontour,
		int I,int J) {

	    //Vector< Vector<Point> > blob;

		Mat ORc=Mat.zeros(OR.size(),CvType.CV_8UC1);
		OR.copyTo(ORc);
		Imgproc.morphologyEx(ORc,ORc,Imgproc.MORPH_DILATE,Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new org.opencv.core.Size(5,1)));


		List<MatOfPoint> blob=new ArrayList<MatOfPoint>();
		FindBlobs(ORc,blob);

		if (blob.size()==1){

			int filtrar=filtrarCajasHorizontalFP(contornos,ORc.size());
			if (filtrar==-1)//Ningun contorno que filtrar! (Despues me fijo si hay que filtrar por la dilatacion vertical)
				return -1;


			Mat hierarchy=new Mat();
			blob.clear();
			//contorno i es 0, contorno j es 1
			if (filtrar==1){//Filtrar contorno 0 == i        ->Agregar j solo
				
				Imgproc.findContours(rectsJ.clone(),blob,hierarchy,Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_NONE);
				if (!isIn(J))
					filteredFP.add(blob.get(0));

				//for (int i=0;i<blob.get(0).cols();i++)//OJO
				//for (int i=0;i<blob.get(0).size();i++)
					//ORcontour.add(blob.get(0).get(i));
					//ORcontour.add(blob.get(0).get(0,i));
				ORcontour.push_back(blob.get(0));

				return 1;
			}
			else {//Filtrar contorno 1 == j ---

				Imgproc.findContours(rectsI.clone(),blob,hierarchy,Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_NONE);
				if (!isIn(I))
					filteredFP.add(blob.get(0));


				//for (int i=0;i<blob.get(0).size();i++)
					//ORcontour.add(blob.get(0).get(i));
				ORcontour.push_back(blob.get(0));


				return 0;
			}
		}
		//No se unieron, nada que filtrar
		return -1;
	}

	private int filtrarCajasHorizontalFP(List<MatOfPoint> contornos,Size size){
		//Rect rI=Imgproc.boundingRect(contornos[0]);
		Rect rI=Imgproc.boundingRect(contornos.get(0));
		Rect rJ=Imgproc.boundingRect(contornos.get(1));

		double nonZeroI=rI.width*rI.height;//countNonZero(rectI);
		double nonZeroJ=rJ.width*rJ.height;//countNonZero(rectJ);
		double relacionArea=0.4;


		if (nonZeroJ / nonZeroI < relacionArea){
			return 0;//Filtrar J!
		}

		if (nonZeroI / nonZeroJ < relacionArea){
			return 1;
		}

		return -1;
	}

	private boolean isIn(int i) {

		for (int j=0;j<agregados.size();j++)
			if (agregados.get(j).intValue()==i)
				return true;
		agregados.add(i);//CAmbia?
		return false;
	}

};

