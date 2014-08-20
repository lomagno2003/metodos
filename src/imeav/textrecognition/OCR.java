package imeav.textrecognition;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.TesseractException;

import org.opencv.core.*;
import org.opencv.highgui.*;
import org.opencv.imgproc.*;

import imeav.utilities.TextBox;



public class OCR extends TextRecognizer
{

	public OCR(){


	}
	
	
	public static void showResult(Mat img,String titulo) {
		
	    //Imgproc.resize(img, img, new Size(640, 480));
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
	
	public String readFile(String filename)
	{
	   String content = null;
	   File file = new File(filename); //for ex foo.txt
	   try {
	       FileReader reader = new FileReader(file);
	       char[] chars = new char[(int) file.length()];
	       reader.read(chars);
	       content = new String(chars);
	       reader.close();
	   } catch (IOException e) {
	       e.printStackTrace();
	   }
	   String separator = System.getProperty("line.separator");
	   String str = content.replaceAll("\n", "");
	   
	   return str;
	}
	
public Vector<TextBox> getText(Mat original, Mat areas){
	
    Vector<TextBox> salida = new Vector<TextBox>();


    //Vector<Vector<Point> > contours;
    //Vector<Vec4i> hierarchy;
    Mat hierarchy=new Mat();

    List<MatOfPoint> contours=new ArrayList<MatOfPoint>();
	Imgproc.findContours(areas.clone(),contours,hierarchy,Imgproc.RETR_CCOMP,Imgproc.CHAIN_APPROX_NONE);


	int contador=1;
	
	/*File dir= new File("/TEXT");
	if (!dir.exists()){
		boolean hecho = dir.mkdir();
		if (!hecho){
			System.out.println("Unable to crate dir");
			System.exit(-1);
		}
			
	}*/
    for (int idx=0;idx>=0;idx=(int) hierarchy.get(0,idx)[0]){
    	
        Rect r = Imgproc.boundingRect(contours.get(idx));
        
        Mat textRect = original.submat(r);
        Size dsize = textRect.size();
        dsize.height*=10;
        dsize.width*=10;
        Imgproc.resize(textRect, textRect, dsize);
        Imgproc.threshold(textRect, textRect, -1, 255, Imgproc.THRESH_OTSU);


        String ruta="text";
        Highgui.imwrite(ruta + ".png", textRect);

        //Reconocer texto usando original y r
        TextBox tb = new TextBox("",r);
        
        
//        try {
//            Runtime rt = Runtime.getRuntime();
//            Process pr = rt.exec("tesseract "+ruta+".png "+ ruta+ " -l spa");
//
//            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
//
//            String line=null;
//
//            while((line=input.readLine()) != null) {
//                System.out.println(line);
//            }
//
//            int exitVal = pr.waitFor();
//            System.out.println("Tesseract: Exited with error code "+exitVal);
//            
//            //Leer el archivo recien escrito y asignarlo en el constructor
//            String str=readFile(ruta+".txt");
//            tb.setText(str);
//            System.out.println(str);
//
//        } catch(Exception e) {
//            //System.out.println(e.toString());
//            e.printStackTrace();
//            System.out.println("No se encontro tesseracte");
//        }
        
//        System.setProperty("jna.library.path", "win32-x86-64");
        
        File imageFile = new File(ruta+".png ");
        Tesseract instance = Tesseract.getInstance();  // JNA Interface Mapping
//        Tesseract1 instance = new Tesseract1(); // JNA Direct Mapping

        try {
        	BufferedImage image = ImageIO.read(imageFile);
            String result = instance.doOCR(image);
            System.out.println(result);
        } catch (TesseractException | IOException e) {
            System.err.println(e.getMessage());
        }

        
        salida.add(tb);
        
        contador++;
    }


    return salida;
}


};
