package imeav.relationextraction;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.*;
import org.opencv.highgui.*;
import org.opencv.imgproc.*;

import imeav.utilities.Relation;
import imeav.utilities.Vec4i;

public class HoughExtractor implements IRelationExtractor
{
	//public String dirBase;
	private List< Vector<Integer> > endpointMatrix;
	private int endpointMatrixCols,endpointMatrixRows;
	private int tolerance;
	private double inclusionRatio;
	private Vector<Vec4i> segmentList;
	private Set< Integer > usados;
	
	public static void showResult(Mat img,String titulo) {
		
	    MatOfByte matOfByte = new MatOfByte();
	    Highgui.imencode(".jpg", img, matOfByte);
	    byte[] byteArray = matOfByte.toArray();
	    BufferedImage bufImage = null;
	    try {
	        InputStream in = new ByteArrayInputStream(byteArray);
	        bufImage = ImageIO.read(in);
	        JFrame frame = new JFrame();
	        frame.setTitle(titulo);
	        frame.getContentPane().add(new JLabel(new ImageIcon(bufImage)));
	        frame.pack();
	        frame.setVisible(true);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}


	public HoughExtractor() {
		//this.dirBase=dirBase;
		tolerance=10;
	    inclusionRatio=0.3;
	    usados=new HashSet<Integer>();
	}
	
	private void drawPath(Mat m, Vector<Vec4i> path) {
		for (int i=0;i<path.size();i++){
			Vec4i l = path.get(i);
			Core.line( m, new Point(l.v0, l.v1), new Point(l.v2, l.v3), new Scalar(255), 1, Core.LINE_AA,0);
		}
		
	}

public Vector<Relation> extract(Mat binary, Mat boxes){

    segmentList = extractLines(binary,boxes);

    //ordeno de mayor longitud a menor
  

    Collections.sort(segmentList, new HoughComparator());//Orden descendente quereos!
    Collections.reverse(segmentList);


    //crear la matriz de endpoints
    endpointMatrix = createEndpointMatrix(segmentList,boxes.rows(),boxes.cols());

    //reiniciar datos de usados
    usados.clear();

    Vector<Relation> listaCaminos = new Vector<Relation>();

    Mat caminos = Mat.zeros(boxes.rows(),boxes.cols(),CvType.CV_8U);

    //crear la lista de caminos
    for (int i=0;i<segmentList.size();i++){


        //si no est� usado
    	if (!usados.contains(new Integer(i))){
            Relation camino = getPath(i);
            listaCaminos.add(camino);

            drawPath(caminos,camino.getSegments());
        }
    }

    String matrizYML = "matriz.yml";
    String labelsYML = "labels.yml";

    String archivo;
    FileReader fr = null;
	try {
		fr = new FileReader(matrizYML);
	} catch (FileNotFoundException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
    BufferedReader br = new BufferedReader(fr);

    try {
        StringBuilder sb = new StringBuilder();
        String line = new String();
		try {
			line = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        while (line != null) {
            sb.append(line);
            sb.append("\n");
            try {
				line = br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        archivo = sb.toString();
    } finally {
        try {
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    String[] splitted = archivo.split(",");
    Mat train= new Mat(splitted.length / 7 , 7 , CvType.CV_32F);
    
    for (int k=0,contador=0;k<splitted.length;k++){
    	float f = Float.parseFloat(splitted[k]);
    	int kM = k % 7;
    	train.put(contador, kM, f);
    	if (kM == 0)
    		contador++;
    }
    
    try {
		fr = new FileReader(labelsYML);
	} catch (FileNotFoundException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
    BufferedReader br1 = new BufferedReader(fr);

    try {
        StringBuilder sb = new StringBuilder();
        String line = new String();
		try {
			line = br1.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        while (line != null) {
            sb.append(line);
            sb.append("\n");
            try {
				line = br1.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        archivo = sb.toString();
    } finally {
        try {
			br1.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
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

        if (p1.x-10>=0&&p1.x+10<binary.cols()
          &&p1.y-10>=0&&p1.y+10<binary.rows()){
            Mat ext=binary.colRange((int)p1.x-10,(int)p1.x+10).rowRange((int)p1.y-10,(int)p1.y+10);

            Mat hu = new Mat();
            
            Imgproc.HuMoments(Imgproc.moments(ext),hu);

            Mat huf=new Mat();//de float
            hu.convertTo(huf,CvType.CV_32FC1);
            Mat results = new Mat();
            huf=huf.t();

            classif.predict(huf,results);

            listaCaminos.get(i).setTipoExt1((int)results.get(0,0)[0]);

        }
        if (p2.x-10>=0&&p2.x+10<binary.cols()
          &&p2.y-10>=0&&p2.y+10<binary.rows() ){
            Mat ext=binary.colRange((int)p2.x-10,(int)p2.x+10).rowRange((int)p2.y-10,(int)p2.y+10);

            Mat hu = new Mat();
            Imgproc.HuMoments(Imgproc.moments(ext),hu);

            Mat huf=new Mat();
            hu.convertTo(huf,CvType.CV_32FC1);
            Mat results = new Mat();
            huf=huf.t();

            classif.predict(huf,results);
 
            listaCaminos.get(i).setTipoExt2((int)results.get(0,0)[0]);

        }

    }

    return listaCaminos;
	}



private List< Vector<Integer> > createEndpointMatrix(Vector<Vec4i> listaLineasSelec, int rows, int cols) {
	
	endpointMatrixCols=cols;
	endpointMatrixRows=rows;
	
	List<Vector<Integer>> matriz = new ArrayList< Vector<Integer> >();
    for (int i=0;i<rows;i++)
        for (int j=0;j<cols;j++){
        	matriz.add(new Vector<Integer>());
        }

    for (int i=0;i<listaLineasSelec.size();i++){
        Vec4i l = listaLineasSelec.get(i);

        matriz.get( l.v1*cols + l.v0 ).add(new Integer(i));   
        matriz.get( l.v3*cols + l.v2 ).add(new Integer(i));
    }

    return matriz;
}
private Vector<Vec4i> extractLines(Mat binaria, Mat cajas){

Mat cajasDilat = new Mat();
Imgproc.morphologyEx(cajas,cajasDilat,Imgproc.MORPH_DILATE,Imgproc.getStructuringElement(Imgproc.MORPH_CROSS,new org.opencv.core.Size(5,5)));


 Mat notbin = new Mat();
 Core.bitwise_not(binaria,notbin);

 Mat resultadoHough=Mat.zeros(cajas.rows(),cajas.cols(),CvType.CV_8U);
 /////////////////////////////////////
 Mat lines = new Mat();

 Mat lineasSelec=Mat.zeros(cajas.rows(),cajas.cols(),CvType.CV_8U);;

 Vector<Vec4i> listaLineasSelec = new Vector<Vec4i>();

 Imgproc.HoughLinesP(notbin, lines, 0.5, Math.PI/2000, 10, 20, 10 );
   for( int i = 0; i < lines.cols(); i++ )
   {
	   double[] lp= lines.get(0,i);
	   Vec4i l=new Vec4i((int)lp[0],(int)lp[1],(int)lp[2],(int)lp[3]);
     
     //OJO SHIFT
     Core.line( resultadoHough, new Point(l.v0, l.v1), new Point(l.v2, l.v3), new Scalar(255), 1, Core.LINE_AA,0);

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

   return listaLineasSelec;

	
}

private Vector< Integer > getNeighbors(int x, int y, int k ){
	

    //busco los l�mites
    int xmin=x-k/2;
    int xmax=x+k/2;
    int ymin= y-k/2;
    int ymax=y+k/2;
    if (xmin<0) xmin=0;
    if (xmax>=endpointMatrixCols) xmax=endpointMatrixCols-1;
    if (ymin<0) ymin=0;
    if (ymax>=endpointMatrixRows) ymax=endpointMatrixRows-1;

    Vector<Integer> salida = new Vector<Integer>();

    for (int i=ymin;i<=ymax;i++)
        for (int j=xmin;j<xmax;j++)
        	if (!(endpointMatrix.get(i*endpointMatrixCols + j).isEmpty())){
                Vector<Integer> subVector = endpointMatrix.get(i*endpointMatrixCols + j);

                
                for (int w=0;w<subVector.size();w++)
                    salida.add(subVector.get(new Integer(w)));
            }


    return salida;
}

private Relation avanzar(int indiceSegmento, Point salida){

    //devuelve un path que contiene el camino recorrido
    //y como "extreme2" el punto m�s alejado al que llega,
    //como "extreme1" el ptoinicial del recorrido

    Vector<Vec4i> salida1=new Vector<Vec4i>();
    //el segmento actual, incialmente el que viene como par�metro
    Vec4i segmentoActual = segmentList.get(indiceSegmento);
    //es el otro extremo del segmento actual
    Point otroExtremoActual= new Point();
    //Point2f otroExtremoActual;
    //punto de salida, incialmente el que viene como par�metro
    Point ptoSalidaActual=salida;

    int nit=0;
    while (nit<5){
        nit++;

        Point otroExtremoActual_t;
        //calculo otroExtremoActual, y el trasladado
        if (segmentoActual.v0==ptoSalidaActual.x && segmentoActual.v1==ptoSalidaActual.y){
            otroExtremoActual=new Point(segmentoActual.v2,segmentoActual.v3);
            otroExtremoActual_t=new Point(segmentoActual.v2-ptoSalidaActual.x,segmentoActual.v3-ptoSalidaActual.y);
        } else {
            otroExtremoActual=new Point(segmentoActual.v0,segmentoActual.v1);
            otroExtremoActual_t=new Point(segmentoActual.v0-ptoSalidaActual.x,segmentoActual.v1-ptoSalidaActual.y);
        }

        //busco los vecinos
        Vector<Integer> vecinos = getNeighbors((int)ptoSalidaActual.x,(int)ptoSalidaActual.y,tolerance);

        //el extremo m�s alejado, inicialmente es el actual (no avanc� todav�a)
        Point extremoMasAlejado=otroExtremoActual;
        Vec4i segmentoMasAlejado=segmentoActual;
        double mayorDist2 = 0;

        for (int i=0;i<vecinos.size();i++){


            //agrego al vecino a la lista de usados,
            //esto incluye tambi�n al segmento actual ya
            //que es vecino de s� mismo
            usados.add(vecinos.get(i));

            Vec4i v = segmentList.get(vecinos.get(i)); //vecino actual

            //el otro extremo del segmento de entrada

            Point otroExtremoVecino;
            Point otroExtremoVecino_t;

            //busco el otro extremo del punto vecino, y el trasladado
            if (((v.v0-ptoSalidaActual.x)*(v.v0-ptoSalidaActual.x) + (v.v1-ptoSalidaActual.y)*(v.v1-ptoSalidaActual.y))<
                ((v.v2-ptoSalidaActual.x)*(v.v2-ptoSalidaActual.x) + (v.v3-ptoSalidaActual.y)*(v.v3-ptoSalidaActual.y))){
                otroExtremoVecino=new Point(v.v2,v.v3);
                otroExtremoVecino_t=new Point(v.v2-v.v0,v.v3-v.v1);
            } else {
                otroExtremoVecino=new Point(v.v0,v.v1);
                otroExtremoVecino_t=new Point(v.v0-v.v2,v.v1-v.v3);
            }

            double coseno = otroExtremoActual_t.dot(otroExtremoVecino_t)/(Math.sqrt(otroExtremoActual_t.dot(otroExtremoActual_t))*Math.sqrt(otroExtremoVecino_t.dot(otroExtremoVecino_t)));

            //�nicamente si el �ngulo demuestra que este
            //vecino es una continuaci�n probable...
            if (coseno<0.4){
                //veo si es m�s alejado que el m�s alejado hasta ahora, y de ser
                //as� lo reemplazo
                double distancia2 = Math.pow(ptoSalidaActual.x-otroExtremoVecino.x,2)+
                                   Math.pow(ptoSalidaActual.y-otroExtremoVecino.y,2);

                if (distancia2>mayorDist2){
                    mayorDist2=distancia2;
                    extremoMasAlejado=otroExtremoVecino;
                    segmentoMasAlejado=v;
                }
            }
        }

        //si no encontr� un segmento por donde avanzar, termina
        if (segmentoMasAlejado==segmentoActual)
            break;

        //ya que encontr� un segmento actualiza los datos
        //y agrega al vecino a la lista
        segmentoActual=segmentoMasAlejado;
        ptoSalidaActual=extremoMasAlejado;
        salida1.add(segmentoMasAlejado);
    }


    Relation pathSalida = new Relation();
    pathSalida.setSegments(salida1);
    pathSalida.setExtreme1(salida);
    pathSalida.setExtreme2(otroExtremoActual);


    return pathSalida;
	
}
private Relation getPath(int index){

    Vector<Vec4i> caminoUnido = new Vector<Vec4i>();

    Relation pathIzq=
    avanzar(index,new Point(segmentList.get(index).v0,segmentList.get(index).v1));
    Relation pathDer=
    avanzar(index,new Point(segmentList.get(index).v2,segmentList.get(index).v3));

    Vector<Vec4i> izq= pathIzq.getSegments();
    Vector<Vec4i> der= pathDer.getSegments();

    //sandwich
    for (int j=0;j<izq.size();j++)
        caminoUnido.add(izq.get(j));
    caminoUnido.add(segmentList.get(index));
    for (int j=der.size()-1;j>=0;j--)
        caminoUnido.add(der.get(j));

    Relation salida = new Relation();
    salida.setSegments(caminoUnido);
    salida.setExtreme1(pathIzq.getExtreme2());
    salida.setExtreme2(pathDer.getExtreme2());

    return salida;
}


};
