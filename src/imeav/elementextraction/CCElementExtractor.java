package imeav.elementextraction;

import java.util.Vector;

import org.opencv.core.*;
import imeav.utilities.Element;

public class CCElementExtractor extends IElementExtractor
{

	@Override
	public Vector<Element> getBoxes() {
		return refinador.getBoxes();
	}
	@Override
	public Mat paintBoxes() {
		return refinador.paintBoxes();
	}

	public CCElementExtractor(Mat input, int min_area,int tam_conex,double rectang_min) {
		detector= new CCBoxDetector(min_area,tam_conex,rectang_min);

	    Mat cajas = detector.paintBoxes(input);
	    refinador= new CCBoxRefiner();

	    refinador.connectBoxes(cajas);
	}


	private CCBoxDetector detector;
	private CCBoxRefiner refinador;
};
