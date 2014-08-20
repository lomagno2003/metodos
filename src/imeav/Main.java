package imeav;

import java.awt.Canvas;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream.GetField;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.omg.CORBA.Environment;
import org.opencv.core.*;
import org.opencv.highgui.*;
import org.opencv.imgproc.*;
import org.opencv.utils.Converters;

import imeav.binarization.Binarizer;
import imeav.binarization.FloodFillerBinarizer;
import imeav.elementextraction.ElementExtractor;
import imeav.elementextraction.CCElementExtractor;
import imeav.graphassembly.GraphAssembler;
import imeav.graphassembly.ProximityGraph;
import imeav.preprocessing.BorderExtender;
import imeav.preprocessing.Preprocessor;
import imeav.relationextraction.RelationExtractor;
import imeav.relationextraction.HoughExtractor;
import imeav.textrecognition.OCR;
import imeav.textrecognition.TextRecognizer;
import imeav.textseparation.CCTextSeparator;
import imeav.textseparation.TextSeparator;
import imeav.utilities.Element;
import imeav.utilities.Relation;
import imeav.utilities.TextBox;


public class Main {
	
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

	
	public static void main(String[] args){
		
		JFileChooser chooser = new JFileChooser();
		int option = chooser.showOpenDialog(null);
		if (option != JFileChooser.APPROVE_OPTION)
			System.exit(0);
		File f = chooser.getSelectedFile();
		System.out.println(f.getAbsolutePath());
		
		JFileChooser Wchooser = new JFileChooser();
		Wchooser.setSelectedFile(new File("outputGraph.dot"));
		int Woption = Wchooser.showSaveDialog(null);
		if (Woption != JFileChooser.APPROVE_OPTION)
			System.exit(0);
		File Wf = Wchooser.getSelectedFile();
		System.out.println(Wf.getAbsolutePath());
		
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		String dirBase;
		
		/*if (args.length>0)
			dirBase = args[0];
		else
			dirBase="C:/users/lgervasoni/Desktop/SUB_CORREGIDO/";
			//      "SUB_CORREGIDO/"
		*/
		
		
				
				//load
				Mat originalColor=Highgui.imread(f.getAbsolutePath());
				
				if (originalColor.size().height==0){
					System.out.println("No se pudo leer la imágen");
					System.exit(0);
				}
				
				showResult(originalColor, "Original");
					
								

				//Preprocesamiento
				Preprocessor prepro = new BorderExtender();
				Mat original = prepro.preprocess(originalColor);			    
			


				//separar texto
				TextSeparator separator = new CCTextSeparator();
		        Mat areasTexto = separator.getText(original);		        
		        Mat textoBorrado = separator.eraseText(original,areasTexto);		        
		        showResult(textoBorrado,"Estructura");
		        showResult(areasTexto,"Texto");

		        

		        //reconocer texto
		        TextRecognizer reconocedor = new OCR();
		        Vector<TextBox> textos= reconocedor.getText(original,areasTexto);
 

		        
				//binarización
				Binarizer binariz = new FloodFillerBinarizer(135,0.05,150);
		        Mat binaria=binariz.binarize(textoBorrado);
		        showResult(binaria,"Binaria");

		        

				//detección de cajas
		        ElementExtractor boxExtractor = new CCElementExtractor(binaria,30,7,0.97);
		        //imshow("Binaria con cajas refinadas",boxExtractor.paintBoxes());

		        Vector<Element> boxes = boxExtractor.getBoxes();
		        Mat refinedBoxes = Mat.zeros(binaria.rows(),binaria.cols(),CvType.CV_8UC1);
		        for (int i1=0;i1<boxes.size();i1++){
		            List<MatOfPoint> tmp = new ArrayList<MatOfPoint>();
		            tmp.add(boxes.get(i1).getPoints());
		            Imgproc.drawContours(refinedBoxes,tmp,-1,new Scalar(255));
		        }
		        showResult(refinedBoxes,"Módulos");


		        
		        //detección de conexiones
		        RelationExtractor hough = new HoughExtractor();
		        Vector<Relation> caminos =  hough.extract(binaria,boxExtractor.paintBoxes());
		        //ADENTRO DE HOUGH.EXTRACT HAY UN SHOWRESULTS
		        

		        //construir grafo
		        GraphAssembler constructorGrafo = new ProximityGraph(original.size(),50,Wf.getAbsolutePath());
				constructorGrafo.buildGraph(boxes,textos,caminos);
				//ADENTRO DE GRAPHBUILD HAY SHOWRESULTS

			




				
				

		         	


			
			
	}

}
