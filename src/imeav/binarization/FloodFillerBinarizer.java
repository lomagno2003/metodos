package imeav.binarization;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.*;
import org.opencv.imgproc.*;

public class FloodFillerBinarizer implements IBinarizer {
	/* Constantes */
	private double ELONGATEDNESS_FACTOR;
	private int PIXELS_MIN;
	
	/**
	 * Variable que representa el limite que usara para binarizar. Los pixeles por encima de este limite seran 1s y de otro modo 0s
	 */
	private int thresh;

	public FloodFillerBinarizer(int threshold, double elongatednessFactor,
			int pixels_min) {
		thresh = threshold;
		ELONGATEDNESS_FACTOR = elongatednessFactor;
		PIXELS_MIN = pixels_min;
	}

	/**
	 * @link {imeav.binarization.Binarizer#binarize(Mat)}
	 */
	@Override
	public Mat binarize(Mat input) {
		Mat floodFilled = input.clone();
		Mat seedMask = Mat.zeros(input.rows() + 2, input.cols() + 2,
				CvType.CV_8UC1);

		Point seed = new Point(0, 0);

		Mat bordes = Mat.ones(input.rows(), input.cols(), CvType.CV_8UC1);// *255
		bordes.setTo(new Scalar(255));

		boolean primerCC = true;

		while (true) {
			if (seed.x == -1)// Ya hizo floodfill de toda la imagen
				break;

			Scalar newVal;
			if (input.get((int) seed.y, (int) seed.x)[0] <= thresh)
				newVal = new Scalar(0);
			else
				newVal = new Scalar(255);

			Mat previousSeedMask = seedMask.clone();
			
			/* Matriz usada para calcular el XOR entre el seedMask actual y el anterior */
			Mat xorr = new Mat();

			Imgproc.floodFill(floodFilled, seedMask, seed, newVal, null,
					new Scalar(8), new Scalar(8), 8 | (255 << 8));

			Core.bitwise_xor(seedMask, previousSeedMask, xorr);

			if (newVal.val[0] == 255 && !primerCC) {
				buscarBordes(xorr, bordes);
			} else {
				primerCC = false;
			}

			// Buscar proxima semilla
			seed = nextSeed(seedMask, seed);
		}

		Mat salida = new Mat();
		Core.bitwise_and(floodFilled, bordes, salida);
		
		return salida;
	}

	/**
	 * Devuelve el siguiente seed de la matriz recorriendo por filas en busca de un pixel == 0
	 * 
	 * @param seedMask
	 * @param seed
	 * @return
	 */
	private Point nextSeed(Mat seedMask, Point seed) {
		Point next = new Point(-1, -1);

		for (int y = (int) (seed.y + 1); y < seedMask.rows() - 1; y++)
			for (int x = 1; x < seedMask.cols() - 1; x++)
				if (seedMask.get(y, x)[0] == 0)
					return new Point(x - 1, y - 1);
		
		return next;
	}

	
	/**
	 * Esta funcion, dado la matriz de puntos, devuelve si el rectangulo minimo que contenga a todos los 
	 * puntos es alargado o no(width/height > ELONGATEDNESS_FACTOR ó height/width > ELONGATEDNESS_FACTOR)
	 * 
	 * @param matrix
	 * @return
	 */
	private boolean isElongatedObject(Mat matrix) {
		Mat puntos = new Mat();
		Core.findNonZero(matrix, puntos);

		MatOfPoint p = new MatOfPoint(puntos);
		Rect minBoundingRect = Imgproc.boundingRect(p);

		double w = minBoundingRect.width;
		double h = minBoundingRect.height;
		if (w < h) {
			if (w / h > ELONGATEDNESS_FACTOR)
				return false;
		} else {
			if (h / w > ELONGATEDNESS_FACTOR)
				return false;
		}

		return true;
	}

	/**
	 * Esta funcion escribe en la matriz output los bordes que se detectan en la matriz input 
	 * 
	 * @param input
	 * @param pixels
	 * @param ourput
	 */
	private void buscarBordes(Mat input, Mat ourput) {
		/* Puntos de nonZeroValues estan desplazados */
		if (Core.countNonZero(input) < PIXELS_MIN)
			return;

		if (isElongatedObject(input)) {
			return;
		}

		Mat xorDilate = input.clone();
		Imgproc.morphologyEx(xorDilate, xorDilate, Imgproc.MORPH_DILATE,
				Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,
						new org.opencv.core.Size(3, 3)));

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(xorDilate, contours, hierarchy,
				Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_NONE,
				new Point(-1, -1));

		if (!contours.get(0).empty())
			Imgproc.drawContours(ourput, contours, -1, new Scalar(0));

		return;
	}
};
