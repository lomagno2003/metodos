package metodos;

import static org.junit.Assert.assertEquals;
import imeav.relationextraction.pointclassifier.WekaClassifier;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class WekaClassifierTest {
	public Mat getMatFromImage(String imageName) throws IOException {
		try {
			BufferedImage originalImage = ImageIO.read(new File(imageName));

			int rows = originalImage.getWidth();
			int cols = originalImage.getHeight();
			int type = CvType.CV_64F;
			Mat newMat = Mat.zeros(rows, cols, type);
	
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					newMat.put(r, c, originalImage.getRGB(r, c));
				}
			}
	
			return newMat;
		} catch (IOException e) {
			System.out.println(e.getMessage());
			return null;
		}
	}

	@Test
	public void test() throws Exception {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		String filePathFormat = "/home/clomagno/Projects/Metodos/workspace/metodos/tests/metodos/{0}.png";
		
		Map<Integer, Set<String>> training = new HashMap<Integer, Set<String>>();
		training.put(0, new HashSet<String>());
		training.put(1, new HashSet<String>());
		training.put(2, new HashSet<String>());

		training.get(0).add(MessageFormat.format(filePathFormat, 1));
		training.get(0).add(MessageFormat.format(filePathFormat, 2));
		training.get(1).add(MessageFormat.format(filePathFormat, 3));
		training.get(1).add(MessageFormat.format(filePathFormat, 4));
		training.get(2).add(MessageFormat.format(filePathFormat, 5));
		training.get(2).add(MessageFormat.format(filePathFormat, 6));
		
		WekaClassifier classifier = new WekaClassifier();
		classifier.buildClassifier(training);
		
		Mat testMat0 = getMatFromImage(MessageFormat.format(filePathFormat, 1));
		assertEquals(0, classifier.classify(testMat0));
		
		Mat testMat1 = getMatFromImage(MessageFormat.format(filePathFormat, 3));
		assertEquals(1, classifier.classify(testMat1));
		
		Mat testMat2 = getMatFromImage(MessageFormat.format(filePathFormat, 5));
		assertEquals(2, classifier.classify(testMat2));
	}

}
