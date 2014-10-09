package imeav.preprocessing;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class BorderExtender implements IPreprocessor {
	private static final int FACTOR = 2;
	private Scalar value;

	public BorderExtender() {
		value = new Scalar(255);
	}

	@Override
	public Mat preprocess(Mat input) {
		Mat inputGray = new Mat(input.size(), CvType.CV_8UC1);
		// convertir a escala de grises
		Imgproc.cvtColor(input, inputGray, Imgproc.COLOR_BGR2GRAY);
		// agrandar bordes
		Mat agrandada = new Mat(input.rows() + 4, input.cols() + 4,
				CvType.CV_8UC1, new Scalar(255));
		Imgproc.copyMakeBorder(inputGray, agrandada, FACTOR, FACTOR, FACTOR,
				FACTOR, Imgproc.BORDER_CONSTANT, value);
		return agrandada;
	}
};
