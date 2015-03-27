package imeav.relationextraction.pointclassifier;

import org.opencv.core.Mat;

public class BayessianClassifier implements IPointClassifier{
	private org.opencv.ml.CvNormalBayesClassifier classif;
	
	public BayessianClassifier(Mat train, Mat labels){
		classif = new org.opencv.ml.CvNormalBayesClassifier();

		classif.train(train, labels);
	}
	
	@Override
	public int classify(Mat mat) {
		Mat results = new Mat();

		classif.predict(mat, results);

		return (int) results.get(0, 0)[0];
	}

}
