package imeav;
import imeav.binarization.IBinarizer;
import imeav.binarization.FloodFillerBinarizer;
import imeav.elementextraction.CCElementExtractor;
import imeav.elementextraction.IElementExtractor;
import imeav.exceptions.InputFileException;
import imeav.exceptions.OutputFileException;
import imeav.graphassembly.IGraphAssembler;
import imeav.graphassembly.ProximityGraph;
import imeav.preprocessing.BorderExtender;
import imeav.preprocessing.IPreprocessor;
import imeav.relationextraction.RelationExtractor;
import imeav.relationextraction.IRelationExtractor;
import imeav.textrecognition.OCR;
import imeav.textrecognition.ITextRecognizer;
import imeav.textseparation.CCTextSeparator;
import imeav.textseparation.ITextSeparator;
import imeav.utilities.Element;
import imeav.utilities.Relation;
import imeav.utilities.TextBox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class IMEAVDiagramRecognizer {
	private Mat originalColor;
	private Mat textoBorrado;
	private Mat areasTexto;
	private Mat binaria;
	private Mat refinedBoxes;
	
	public Mat getOriginalColor() {
		return originalColor;
	}

	public Mat getTextoBorrado() {
		return textoBorrado;
	}

	public Mat getAreasTexto() {
		return areasTexto;
	}

	public Mat getBinaria() {
		return binaria;
	}

	public Mat getRefinedBoxes() {
		return refinedBoxes;
	}

	public void convert(String inputFilePath, String outputFilePath) throws InputFileException,OutputFileException {
		File inputFile = new File(inputFilePath);
		File outputFile = new File(outputFilePath);
		if (!inputFile.canRead()){
			throw new InputFileException();
		}
		try {
			outputFile.createNewFile();
		} catch (IOException e) {
			throw new OutputFileException();
		}
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		// load
		originalColor = Highgui.imread(inputFile.getAbsolutePath());

		if (originalColor.size().height == 0) {
			System.out.println("No se pudo leer la im�gen");
			System.exit(0);
		}

		//showResult(originalColor, "Original");

		// Preprocesamiento
		IPreprocessor prepro = new BorderExtender();
		Mat original = prepro.preprocess(originalColor);

		// separar texto
		ITextSeparator separator = new CCTextSeparator();
		areasTexto = separator.getText(original);
		textoBorrado = separator.eraseText(original, areasTexto);
		//showResult(textoBorrado, "Estructura");
		//showResult(areasTexto, "Texto");

		// reconocer texto
		ITextRecognizer reconocedor = new OCR();
		Vector<TextBox> textos = reconocedor.getText(original, areasTexto);

		// binarizaci�n
		IBinarizer binariz = new FloodFillerBinarizer(135, 0.05, 150);
		binaria = binariz.binarize(textoBorrado);
		//showResult(binaria, "Binaria");

		// detecci�n de cajas
		IElementExtractor boxExtractor = new CCElementExtractor(binaria, 30, 7,	0.97);
		// imshow("Binaria con cajas refinadas",boxExtractor.paintBoxes());

		Vector<Element> boxes = boxExtractor.getBoxes();
		refinedBoxes = Mat.zeros(binaria.rows(), binaria.cols(),CvType.CV_8UC1);
		for (int i1 = 0; i1 < boxes.size(); i1++) {
			List<MatOfPoint> tmp = new ArrayList<MatOfPoint>();
			tmp.add(boxes.get(i1).getPoints());
			Imgproc.drawContours(refinedBoxes, tmp, -1, new Scalar(255));
		}
		//showResult(refinedBoxes, "M�dulos");

		// detecci�n de conexiones
		IRelationExtractor hough = new RelationExtractor();
		Vector<Relation> caminos = hough.extract(binaria,boxExtractor.paintBoxes());
		// ADENTRO DE HOUGH.EXTRACT HAY UN SHOWRESULTS

		// construir grafo
		IGraphAssembler constructorGrafo = new ProximityGraph(original.size(),
				200, outputFile.getAbsolutePath());
		constructorGrafo.buildGraph(boxes, textos, caminos);
		// ADENTRO DE GRAPHBUILD HAY SHOWRESULTS
	}
}
