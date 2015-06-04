package imeav.relationextraction;

import imeav.graphassembly.GraphContext;
import imeav.relationextraction.pointclassifier.BayessianClassifier;
import imeav.relationextraction.pointclassifier.IPointClassifier;
import imeav.utilities.Relation;
import imeav.utilities.SegmentLengthComparator;
import imeav.utilities.Vec4i;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

public class RelationExtractor implements IRelationExtractor {
	private ISegmentExtractor linesExtractor = new HoughSegmentExtractor();
	private IRelationFactory relationFactory = new RelationFactory();
	private IRelationPostProcessor relationPostProcessor;

	public RelationExtractor(GraphContext graphContext) {
		super();
		this.relationPostProcessor = new ComponentsAndConnectorsProcessor(graphContext);
	}

	@Override
	public Vector<Relation> extract(Mat binary, Mat boxes) {
		try {
			/*Get all the segments */
			List<Vec4i> segmentList = linesExtractor.extractLines(binary, boxes);

			//TODO Por que los ordena?
			Collections.sort(segmentList, new SegmentLengthComparator());
			Collections.reverse(segmentList);
			
			// Crea la lista de caminos.
			Vector<Relation> listaCaminos= new Vector<Relation>(relationFactory.getRelations(segmentList));
			
			IPointClassifier pointClassifier = new BayessianClassifier();
			
			int pointType;
			Mat huf;
			for (int i = 0; i < listaCaminos.size(); i++) {
				Relation c = listaCaminos.get(i);
				Point p1 = c.getExtreme1();
				Point p2 = c.getExtreme2();
				if (isValid(p1, binary)) {
					huf = executeHuMoment(p1, binary);
					pointType = pointClassifier.classify(huf);
					listaCaminos.get(i).setTipoExt1(pointType);
				}
				if (isValid(p2, binary)) {
					huf = executeHuMoment(p2, binary);
					pointType = pointClassifier.classify(huf);
					listaCaminos.get(i).setTipoExt2(pointType);
				}
			}
			
			this.relationPostProcessor.process(listaCaminos);

			return listaCaminos;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Este metodo retorna true en el caso que el punto p se encuentre dentro de
	 * los limites de la matris binary con un margen de 10 hacia adentro.
	 * 
	 * @param p
	 * @param binary
	 * @return
	 */
	private boolean isValid(Point p, Mat binary) {
		// TODO Parametrizar el rango del punto
		if (p.x - 10 >= 0 && p.x + 10 < binary.cols() && p.y - 10 >= 0
				&& p.y + 10 < binary.rows()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Este metodo retorna el momento Hu al rededor del punto p contenido en la
	 * matriz binary
	 * 
	 * @param p
	 * @param binary
	 * @param classif
	 * @param index
	 * @return
	 */
	private Mat executeHuMoment(Point p, Mat binary) {
		// TODO Parametrizar el rango del punto
		Mat ext = binary.colRange((int) p.x - 10, (int) p.x + 10).rowRange(
				(int) p.y - 10, (int) p.y + 10);

		Mat hu = new Mat();

		Imgproc.HuMoments(Imgproc.moments(ext), hu);

		Mat huf = new Mat();// de float
		hu.convertTo(huf, CvType.CV_32FC1);
		huf = huf.t();

		return huf;
	}


}
