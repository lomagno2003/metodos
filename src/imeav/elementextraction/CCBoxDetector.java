package imeav.elementextraction;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.*;
import org.opencv.highgui.*;
import org.opencv.imgproc.*;


public class CCBoxDetector
{
	private int MIN_AREA;
	private int TAM_CONEX;
	private double RECTANGULARIDAD_MIN;

	public CCBoxDetector(int MIN_AREA, int TAM_CONEX, double RECTANGULARIDAD_MIN) {
		 this.MIN_AREA=MIN_AREA;
		 this.TAM_CONEX=TAM_CONEX;
		 this.RECTANGULARIDAD_MIN=RECTANGULARIDAD_MIN;
	}
	
	public static Mat unirDiagonales(Mat input){
		    Mat output=input.clone();
		    for (int y=0;y<input.rows()-1;y++)
		        for (int x=0;x<input.cols()-1;x++){
		            if (input.get(y,x)[0]==0 && input.get(y,x+1)[0]==255
		                && input.get(y+1,x)[0]==255 && input.get(y+1,x+1)[0]==0){
		                    output.get(y,x+1)[0]=0;output.get(y+1,x)[0]=0;}
		            else
		                if (input.get(y,x)[0]==255 && input.get(y,x+1)[0]==0
		                    && input.get(y+1,x)[0]==0 && input.get(y+1,x+1)[0]==255){
		                        output.get(y,x)[0]=0;output.get(y+1,x+1)[0]=0;}
		        }
		    return output;
		}

	public Mat paintBoxes(Mat binary) {
		Mat binariaTemp=unirDiagonales(binary);

	    List<MatOfPoint> puntosDeCajas=encontrarCajas(binariaTemp);

	    Mat cajasSeguras= new Mat(binary.rows(),binary.cols(),CvType.CV_8UC1,new Scalar(0));
	    Imgproc.drawContours(cajasSeguras,puntosDeCajas,-1,new Scalar(255));

	    while (true){

	        Mat cajasSegurasDil = new Mat(binary.rows(),binary.cols(),CvType.CV_8UC1,new Scalar(0));
	        Imgproc.morphologyEx(cajasSeguras,cajasSegurasDil,Imgproc.MORPH_DILATE,Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,new org.opencv.core.Size(TAM_CONEX,TAM_CONEX)));

	        Mat cajasSegurasDil2 = new Mat(binary.rows(),binary.cols(),CvType.CV_8UC1,new Scalar(0));
	        Imgproc.morphologyEx(cajasSeguras,cajasSegurasDil2,Imgproc.MORPH_DILATE,Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,new org.opencv.core.Size(TAM_CONEX,1)));

	        Mat cajasSegurasDil3 = new Mat(binary.rows(),binary.cols(),CvType.CV_8UC1,new Scalar(0));
	        Imgproc.morphologyEx(cajasSeguras,cajasSegurasDil3,Imgproc.MORPH_DILATE,Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,new org.opencv.core.Size(1,TAM_CONEX)));

	        Mat binariaTemp1 = new Mat(); Mat binariaTemp2=new Mat(); Mat binariaTemp3=new Mat();

	        Core.bitwise_or(cajasSegurasDil,binariaTemp,binariaTemp1);
	        Core.bitwise_or(cajasSegurasDil2,binariaTemp,binariaTemp2);
	        Core.bitwise_or(cajasSegurasDil3,binariaTemp,binariaTemp3);

	        puntosDeCajas=encontrarCajas(binariaTemp1);
	        List<MatOfPoint> puntosDeCajas2=encontrarCajas(binariaTemp2);
	        List<MatOfPoint> puntosDeCajas3=encontrarCajas(binariaTemp3);
	        if (puntosDeCajas.size()==0&&puntosDeCajas2.size()==0&&puntosDeCajas3.size()==0)
	            break;

	        Imgproc.drawContours(cajasSeguras,puntosDeCajas,-1,new Scalar(255));
	        Imgproc.drawContours(cajasSeguras,puntosDeCajas2,-1,new Scalar(255));
	        Imgproc.drawContours(cajasSeguras,puntosDeCajas3,-1,new Scalar(255));

	        binariaTemp=binariaTemp1;

	    }

	    return cajasSeguras;
	}



	private MatOfPoint getCaja(Mat objetoAislado) {
		Mat objetoCerrado = new Mat();
	    Imgproc.morphologyEx(objetoAislado,objetoCerrado,Imgproc.MORPH_CLOSE,Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,new org.opencv.core.Size(TAM_CONEX,TAM_CONEX)));
	    List<MatOfPoint> contours_nuevo = new ArrayList<MatOfPoint>();
	    Mat hierarchy_nuevo=new Mat();

	    Imgproc.findContours(objetoCerrado.clone(),contours_nuevo,hierarchy_nuevo,Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_NONE);

	    Rect mbr= Imgproc.boundingRect(contours_nuevo.get(0));
	    if (Imgproc.contourArea(contours_nuevo.get(0)) / (double)((mbr.width-1)*(mbr.height-1)) > RECTANGULARIDAD_MIN)
	        return contours_nuevo.get(0);

	    return new MatOfPoint();
	}
	private boolean tocaEsquina(MatOfPoint matOfPoint) {
		for (int t=0;t<matOfPoint.cols();t++)
	        if (matOfPoint.get(0,t)[0]==1)
	            return true;
	    return false;
	}
	
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
	public static void ESPERA(){
		
	    int inChar;
        System.out.println("Enter a Character:");
        try {
            inChar = System.in.read();
            System.out.print("You entered ");
            System.out.println(inChar);
        }
        catch (IOException e){
            System.out.println("Error reading from user");
        }
	}
	
	private List<MatOfPoint> encontrarCajas(Mat input) {
		//Contours para detectar cajas
	    List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

	    Mat inputclone=input.clone();

	    Mat hierarchy=new Mat();
		Imgproc.findContours(inputclone,contours,hierarchy,Imgproc.RETR_CCOMP,Imgproc.CHAIN_APPROX_NONE);

	    List<MatOfPoint> puntosDeCajas=new ArrayList<MatOfPoint>();

		for (int idx=0;idx>=0;idx=(int) hierarchy.get(0,idx)[0]){

	        Mat objetoAislado = Mat.zeros(input.rows(),input.cols(),CvType.CV_8UC1);

	        //Filtra letras chicas
	        if (Imgproc.contourArea(contours.get(idx)) < MIN_AREA){
	            continue;
	        }


	        //Filtrar fondo que no es una caja
	        if (tocaEsquina(contours.get(idx))){
	            continue;
	        }

	        //Dibujar objeto aislado y ver si es una caja
	        Imgproc.drawContours(objetoAislado,contours,idx,new Scalar(255),-1);

	        MatOfPoint puntosPosibleCaja=getCaja(objetoAislado);
	        if (!puntosPosibleCaja.empty())

	        	puntosDeCajas.add(puntosPosibleCaja);
	        
	        objetoAislado.release();
	        
	        
	    }
	    return puntosDeCajas;
	}

};
