package imeav;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.*;
import org.opencv.highgui.*;
import org.opencv.imgproc.*;

import imeav.binarization.IBinarizer;
import imeav.binarization.FloodFillerBinarizer;
import imeav.elementextraction.IElementExtractor;
import imeav.elementextraction.CCElementExtractor;
import imeav.graphassembly.IGraphAssembler;
import imeav.graphassembly.ProximityGraph;
import imeav.preprocessing.BorderExtender;
import imeav.preprocessing.IPreprocessor;
import imeav.relationextraction.IRelationExtractor;
import imeav.relationextraction.RelationExtractor;
import imeav.textrecognition.ITextRecognizer;
import imeav.textrecognition.OCR;
import imeav.textseparation.CCTextSeparator;
import imeav.textseparation.ITextSeparator;
import imeav.utilities.Element;
import imeav.utilities.Relation;
import imeav.utilities.TextBox;

public class Main {

	public static void showResult(Mat img, String titulo) {

		// Imgproc.resize(img, img, new Size(640, 480));
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

	public static void main(String[] args) {

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

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		String dirBase;

		// load
		Mat originalColor = Highgui.imread(f.getAbsolutePath());

		if (originalColor.size().height == 0) {
			System.out.println("No se pudo leer la im�gen");
			System.exit(0);
		}

		showResult(originalColor, "Original");

		// Preprocesamiento
		IPreprocessor prepro = new BorderExtender();
		Mat original = prepro.preprocess(originalColor);

		// separar texto
		ITextSeparator separator = new CCTextSeparator();
		Mat areasTexto = separator.getText(original);
		Mat textoBorrado = separator.eraseText(original, areasTexto);
		showResult(textoBorrado, "Estructura");
		showResult(areasTexto, "Texto");

		// reconocer texto
		ITextRecognizer reconocedor = new OCR();
		Vector<TextBox> textos = reconocedor.getText(original, areasTexto);

		// binarizaci�n
		IBinarizer binariz = new FloodFillerBinarizer(135, 0.05, 150);
		Mat binaria = binariz.binarize(textoBorrado);
		showResult(binaria, "Binaria");

		// detecci�n de cajas
		IElementExtractor boxExtractor = new CCElementExtractor(binaria, 30, 7,
				0.97);
		// imshow("Binaria con cajas refinadas",boxExtractor.paintBoxes());

		Vector<Element> boxes = boxExtractor.getBoxes();
		Mat refinedBoxes = Mat.zeros(binaria.rows(), binaria.cols(),
				CvType.CV_8UC1);
		for (int i1 = 0; i1 < boxes.size(); i1++) {
			List<MatOfPoint> tmp = new ArrayList<MatOfPoint>();
			tmp.add(boxes.get(i1).getPoints());
			Imgproc.drawContours(refinedBoxes, tmp, -1, new Scalar(255));
		}
		showResult(refinedBoxes, "M�dulos");

		// detecci�n de conexiones
		IRelationExtractor hough = new RelationExtractor();
		Vector<Relation> caminos = hough.extract(binaria,
				boxExtractor.paintBoxes());
		// ADENTRO DE HOUGH.EXTRACT HAY UN SHOWRESULTS

		// construir grafo
		IGraphAssembler constructorGrafo = new ProximityGraph(original.size(),
				50, Wf.getAbsolutePath());
		constructorGrafo.buildGraph(boxes, textos, caminos);
		// ADENTRO DE GRAPHBUILD HAY SHOWRESULTS

	}

}
