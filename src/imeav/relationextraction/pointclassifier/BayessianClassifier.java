package imeav.relationextraction.pointclassifier;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class BayessianClassifier implements IPointClassifier{
	private org.opencv.ml.CvNormalBayesClassifier classif;
	final String matrizYML = "matriz.yml";
	final String labelsYML = "labels.yml";
	
	public BayessianClassifier() throws IOException{
		String archivo;
		
		/* Loads the file with the examples */
		archivo = fileToString(matrizYML);

		String[] splitted = archivo.split(",");
		Mat train = new Mat(splitted.length / 7, 7, CvType.CV_32F);

		for (int k = 0, contador = 0; k < splitted.length; k++) {
			float f = Float.parseFloat(splitted[k]);
			int kM = k % 7;
			train.put(contador, kM, f);
			if (kM == 0)
				contador++;
		}

		/* Loads the file with the labels */
		archivo = fileToString(labelsYML);

		String[] splitted2 = archivo.split(",");
		Mat labels = new Mat(1, splitted2.length, CvType.CV_32F);

		for (int k = 0; k < splitted2.length; k++) {
			float f = Float.parseFloat(splitted2[k]);
			labels.put(0, k, f);
		}
		
		classif = new org.opencv.ml.CvNormalBayesClassifier();

		classif.train(train, labels);
	}
	
	@Override
	public int classify(Mat mat) {
		Mat results = new Mat();

		classif.predict(mat, results);

		return (int) results.get(0, 0)[0];
	}
	
	//TODO Usar Apache IOUtils
	/**
	 * Este metodo lee un archivo ubicado en filePath y lo retorna en forma de
	 * String
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	private String fileToString(String filePath) throws IOException {
		String archivo;
		FileReader fr = new FileReader(filePath);
		StringBuilder sb = new StringBuilder();
		String line = new String();
		BufferedReader br = new BufferedReader(fr);

		while (line != null) {
			sb.append(line);
			sb.append("\n");
			line = br.readLine();
		}
		archivo = sb.toString();

		br.close();

		return archivo;
	}

}
