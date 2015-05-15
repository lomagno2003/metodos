package imeav.relationextraction.pointclassifier;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class WekaClassifier implements IPointClassifier {
	private Classifier classifier;

	private Integer size = null;
	private Instances instances = null;
	
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

	public void buildClassifier(Map<Integer, Set<String>> trainingImages) throws Exception {
		for(Integer key:trainingImages.keySet()){
			for(String imageName:trainingImages.get(key)){
				Mat mat = getMatFromImage(imageName);
				
				Mat huValue = executeHuMoment(mat);
				
				/* Lazy initialization */
				if(size == null){
					FastVector fastVector = new FastVector();
					
					size = huValue.cols() * huValue.rows() + 1;
					
					for(int i=0;i<size;i++){
						fastVector.addElement(new Attribute("Col" + Integer.valueOf(i).toString()));
					}
					
					
					FastVector posibleValues = new FastVector();
					for(Integer clazz:trainingImages.keySet()){
						posibleValues.addElement(clazz.toString());
					}
					fastVector.addElement(new Attribute("Class", posibleValues));
					
					instances = new Instances("Instances", fastVector,trainingImages.size());
					instances.setClassIndex(size);
				}
				
				Instance instance = new Instance(size + 1);
				instance.setDataset(instances);
				
				for(int y = 0; y < huValue.rows() ; y++){
					for(int x = 0; x < huValue.cols() ; x++){
						instance.setValue((huValue.cols() * y) + x, huValue.get(y, x)[0]);
					}
				}
				
				instance.setValue(size, key.toString());
				instances.add(instance);
			}
		}
		
		this.classifier = new NaiveBayes();
		this.classifier.buildClassifier(instances);
	}

	@Override
	public int classify(Mat mat) {		
		try {
			Mat huValue = executeHuMoment(mat);

			Instance instance = new Instance(size + 1);
			instance.setDataset(instances);
			for(int x = 0; x < huValue.cols() ; x++){
				for(int y = 0; y < huValue.rows() ; y++){
					instance.setValue((huValue.cols() * y) + x, huValue.get(y, x)[0]);
				}
			}
		
			return Double.valueOf(this.classifier.classifyInstance(instance)).intValue();
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		
	}

	/**
	 * Este metodo retorna el momento Hu al rededor del punto p contenido en la
	 * matriz binary
	 * 
	 * @param p
	 * @param mat
	 * @param classif
	 * @param index
	 * @return
	 */
	private Mat executeHuMoment(Mat mat) {		
		/* Center of the image */
		Point p = new Point();
		p.x = mat.cols() / 2;
		p.y = mat.rows() / 2;
		
		int rangeX = 50;
		int rangeY = 50;
		
		// TODO Parametrizar el rango del punto
		Mat ext = mat.colRange((int) p.x - rangeX, (int) p.x + rangeX).rowRange(
				(int) p.y - rangeY, (int) p.y + rangeY);
		
		Mat hu = new Mat();

		Imgproc.HuMoments(Imgproc.moments(ext), hu);

		Mat huf = new Mat();// de float
		hu.convertTo(huf, CvType.CV_32FC1);
		huf = huf.t();
				
		return huf;
	}
}
