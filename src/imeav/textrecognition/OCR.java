package imeav.textrecognition;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
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
	 * Lee un archivo y concatena todas sus lineas en una sola linea
	 * 
	 * @param filename
	 * @return
	 */
	public String readFile(String filename) {
		String content = null;
		File file = new File(filename); // for ex foo.txt
		try {
			FileReader reader = new FileReader(file);
			char[] chars = new char[(int) file.length()];
			reader.read(chars);
			content = new String(chars);
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String separator = System.lineSeparator();

		// TODO Verificar que anda el caracter "separator
		String str = content.replaceAll(separator, "");

		return str;
	}

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

			// Reconocer texto usando original y r
			TextBox tb = new TextBox("", r);

			File imageFile = new File(ruta + ".png ");
			Tesseract instance = Tesseract.getInstance(); // JNA Interface

			try {
				BufferedImage image = ImageIO.read(imageFile);
				String result = instance.doOCR(image);
				System.out.println(result);
			} catch (TesseractException | IOException e) {
				System.err.println(e.getMessage());
			}

			salida.add(tb);
		}

		return salida;
	}
};