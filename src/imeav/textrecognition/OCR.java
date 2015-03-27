package imeav.textrecognition;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.imageio.ImageIO;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import org.opencv.core.*;
import org.opencv.highgui.*;
import org.opencv.imgproc.*;

import imeav.utilities.TextBox;

public class OCR implements ITextRecognizer {
	/**
	 * @see{imeav.relationextraction.ITextRecognizer#getText(org.opencv.core.Mat,org.opencv.core.Mat)
	 */
	@Override
	public Vector<TextBox> getText(Mat original, Mat areas) {
		Vector<TextBox> salida = new Vector<TextBox>();

		Mat hierarchy = new Mat();

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(areas.clone(), contours, hierarchy,
				Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_NONE);

		for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]) {
			Rect r = Imgproc.boundingRect(contours.get(idx));

			Mat textRect = original.submat(r);
			Size dsize = textRect.size();
			dsize.height *= 10;
			dsize.width *= 10;
			Imgproc.resize(textRect, textRect, dsize);
			Imgproc.threshold(textRect, textRect, -1, 255, Imgproc.THRESH_OTSU);

			String ruta = "text";
			Highgui.imwrite(ruta + ".jpg", textRect);


			File imageFile = new File(ruta + ".jpg");
//			Tesseract instance = Tesseract.getInstance(); // JNA Interface
//
//			try {
//				BufferedImage image = ImageIO.read(imageFile);
//				String result = instance.doOCR(image);
//				// Reconocer texto usando original y r
//				TextBox tb = new TextBox(result, r);
//				salida.add(tb);
//				System.out.println(result);
//			} catch (TesseractException | IOException e) {
//				System.err.println(e.getMessage());
//			}		
		}

		return salida;
	}
};