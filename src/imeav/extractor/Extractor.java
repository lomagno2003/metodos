package imeav.extractor;

import imeav.relationextraction.HoughComparator;
import imeav.utilities.Relation;
import imeav.utilities.Vec4i;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class Extractor {
	
	private Vector<Vec4i> segmentList;
	private Set< Integer > usados;
	private Vector<Relation> listaCaminos;
	private Mat caminos;
	final String matrizYML = "matriz.yml";
	final String labelsYML = "labels.yml";
	
	private void inicForExtract(Mat binary, Mat boxes){
		segmentList = new ExtractLine().extractLines(binary,boxes);

	    //ordeno de mayor longitud a menor
	    Collections.sort(segmentList, new HoughComparator());//Orden descendente quereos!
	    Collections.reverse(segmentList);

	    //reiniciar datos de usados
	    usados.clear();

	    listaCaminos = new Vector<Relation>();

	    caminos = Mat.zeros(boxes.rows(),boxes.cols(),CvType.CV_8U);
	}
	
	private void createListWay(){
		 Path path = new Path();
		 DrawPath dwph = new DrawPath();
		    for (int i=0;i<segmentList.size();i++){
		        //si no est� usado
		    	if (!usados.contains(new Integer(i))){
		            Relation camino = path.getPath(i);
		            listaCaminos.add(camino);

		            dwph.execDrawPath(caminos, camino.getSegments());
		        }
		    }
	}
	
	private String createStringBuilder(String archi){
		
	    String archivo = new String();
	    FileReader fr = null;
	    StringBuilder sb = new StringBuilder();
        String line = new String();
        BufferedReader br = null;
        
		try {
	    	fr = new FileReader(archi);
		    br = new BufferedReader(fr);
		    while (line != null) {
	            sb.append(line);
	            sb.append("\n");
				line = br.readLine();
		    }
		    archivo = sb.toString();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			  catch (IOException e) {
				e.printStackTrace();
			}
	    finally {
	        try {
				br.close();
		} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
		return archivo;
	    
	}
	
	public Vector<Relation> extract(Mat binary, Mat boxes){
	    //inicialización para la extracción
		inicForExtract(binary, boxes);
		//Crea la lista de caminos.
		createListWay();
		//Crea el strignbuilder para poder que se levanta del archivo matrizYML
		String archivo =createStringBuilder(matrizYML);
	    
	    String[] splitted = archivo.split(",");
	    Mat train= new Mat(splitted.length / 7 , 7 , CvType.CV_32F);
   
	    for (int k=0,contador=0;k<splitted.length;k++){
	    	float f = Float.parseFloat(splitted[k]);
	    	int kM = k % 7;
	    	train.put(contador, kM, f);
	    	if (kM == 0)
	    		contador++;
	    }
	    //Crea el strignbuilder para poder que se levanta del archivo labelsYML
	    archivo =createStringBuilder(labelsYML);
	    
	    String[] splitted2 = archivo.split(",");
	    Mat labels= new Mat(1 , splitted2.length , CvType.CV_32F);
	    
	    for (int k=0;k<splitted2.length;k++){
	    	float f = Float.parseFloat(splitted2[k]);
	    	labels.put(0,k, f);
	    }

	    org.opencv.ml.CvNormalBayesClassifier classif = new org.opencv.ml.CvNormalBayesClassifier();
	    //NormalBayesClassifier classif;
	    classif.train(train,labels);
	    for (int i=0;i<listaCaminos.size();i++){
	        Relation c=listaCaminos.get(i);
	        Point p1 =c.getExtreme1();
	        Point p2 = c.getExtreme2();

	        int huMoment=executeHuMoment(p1,binary,classif,i);
	        listaCaminos.get(i).setTipoExt1(huMoment);
	        huMoment=executeHuMoment(p2,binary,classif,i);
	        listaCaminos.get(i).setTipoExt2(huMoment);

	    }

	    return listaCaminos;
		}
	
	private int executeHuMoment(Point p,Mat binary,org.opencv.ml.CvNormalBayesClassifier classif,int index){
		/*
		 * Dado un punto, se aplica el metodo HuMoments a la matriz generada a partir del punto.
		 */
		Mat results = new Mat();
		if (p.x-10>=0&&p.x+10<binary.cols()
		          &&p.y-10>=0&&p.y+10<binary.rows()){
		            Mat ext=binary.colRange((int)p.x-10,(int)p.x+10).rowRange((int)p.y-10,(int)p.y+10);

		            Mat hu = new Mat();
		            
		            Imgproc.HuMoments(Imgproc.moments(ext),hu);

		            Mat huf=new Mat();//de float
		            hu.convertTo(huf,CvType.CV_32FC1);
		            huf=huf.t();

		            classif.predict(huf,results);

		        }
		return (int)results.get(0,0)[0];
	}
	
}
