package imeav.textseparation;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.*;
import org.opencv.imgproc.*;

public class CCTextSeparator implements ITextSeparator {
	private static final int CHAR_MAX_AREA = 250;
	private static final int CHAR_MAX_HEIGHT = 90;
	private static final int CHAR_MAX_WIDTH = 20;
	private static final int TEXT_MIN_HEIGHT = 5;
	private static final int TEXT_MAX_HEIGHT = 90;
	private static final int TEXT_MIN_WIDTH = 10;
	private static final int TEXT_MAX_WIDTH = 500;

	@Override
	public Mat getText(Mat imagen) {
		Mat bin = new Mat(imagen.size(), imagen.type());
		Imgproc.threshold(imagen, bin, 80, 255, Imgproc.THRESH_BINARY);
		Mat neg = new Mat(imagen.size(), imagen.type()); 
		Core.bitwise_not(bin, neg);
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(neg.clone(), contours, hierarchy,
				Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_NONE);
		Mat candidatos = Mat.zeros(bin.rows(), bin.cols(), CvType.CV_8U);
		for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]) {
			Rect bound = Imgproc.boundingRect(contours.get(idx));
			if (bound.area() < CHAR_MAX_AREA && bound.width < CHAR_MAX_WIDTH
					&& bound.height < CHAR_MAX_HEIGHT)

				Imgproc.drawContours(candidatos, contours, idx,
						new Scalar(255), -1, 8, hierarchy, 1, new Point(0, 0));
		}
		Mat mbrs = Mat.zeros(bin.rows(), bin.cols(), CvType.CV_8U);
		// dibujo mbr de cada posible car�cter
		for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]) {
			Rect bound = Imgproc.boundingRect(contours.get(idx));
			if (bound.area() < CHAR_MAX_AREA && bound.width < CHAR_MAX_WIDTH
					&& bound.height < CHAR_MAX_HEIGHT) {
				Core.rectangle(mbrs, bound.tl(), bound.br(), new Scalar(255),
						-1);
			}
		}
		Mat unidos = new Mat();
		Imgproc.morphologyEx(mbrs, unidos, Imgproc.MORPH_CLOSE, Imgproc
				.getStructuringElement(Imgproc.MORPH_RECT,
						new org.opencv.core.Size(7, 1)));
		Imgproc.morphologyEx(unidos, unidos, Imgproc.MORPH_CLOSE, Imgproc
				.getStructuringElement(Imgproc.MORPH_RECT,
						new org.opencv.core.Size(1, 3)));

		List<MatOfPoint> contours2 = new ArrayList<MatOfPoint>();
		Mat hierarchy2 = new Mat();
		// contornos sobre la imagen nueva:
		Imgproc.findContours(unidos.clone(), contours2, hierarchy2,
				Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_NONE);

		Mat salida = Mat.zeros(bin.rows(), bin.cols(), CvType.CV_8U);

		for (int idx = 0; idx >= 0; idx = (int) hierarchy2.get(0, idx)[0]) {
			Rect bound = Imgproc.boundingRect(contours2.get(idx));
			if (bound.height > TEXT_MIN_HEIGHT && bound.width > TEXT_MIN_WIDTH
					&& bound.width > bound.height
					&& bound.width < TEXT_MAX_WIDTH
					&& bound.height < TEXT_MAX_HEIGHT) {
				Core.rectangle(salida, bound.tl(), bound.br(), new Scalar(255),-1);
			}
		}
		return salida;
	}

	@Override
	public Mat eraseText(Mat input, Mat texto) {
		// busca el m�ximo de cada caja de texto y lo pinto con ese valor
		Mat salida = input.clone();
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(texto.clone(), contours, hierarchy,
				Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_NONE);
		for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]) {
			Mat objetoAislado = Mat.zeros(input.rows(), input.cols(),CvType.CV_8UC1);
			Imgproc.drawContours(objetoAislado, contours, idx, new Scalar(255),-1);
			Mat sector = new Mat();
			Core.bitwise_and(objetoAislado, input, sector);
			Core.MinMaxLocResult mmlr = Core.minMaxLoc(sector);
			Imgproc.drawContours(salida, contours, idx,	new Scalar(mmlr.maxVal), -1);
			Imgproc.drawContours(salida, contours, idx, new Scalar(mmlr.maxVal));
		}
		return salida;
	}
};
