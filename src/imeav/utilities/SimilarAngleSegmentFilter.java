package imeav.utilities;


/**
 * This filter returns true if the angle between two vectors is lower than a
 * threshold.
 * 
 * @see http://www.euclideanspace.com/maths/algebra/vectors/angleBetween/
 * 
 * @author clomagno
 *
 */
public class SimilarAngleSegmentFilter implements Filter<Vec4i> {
	private Vec4i testVector;
	private Double threshold;

	public SimilarAngleSegmentFilter(Vec4i testVector, Double threshold) {
		this.testVector = testVector;
		this.threshold = threshold;
	}

	public Vec4i getTestVector() {
		return testVector;
	}

	public void setTestVector(Vec4i testVector) {
		this.testVector = testVector;
	}

	public Double getThreshold() {
		return threshold;
	}

	public void setThreshold(Double threshold) {
		this.threshold = threshold;
	}

	public Boolean evaluate(Vec4i elem) {
		Integer v1x = elem.v2 - elem.v0;
		Integer v1y = elem.v3 - elem.v1;
		Integer v2x = testVector.v2 - testVector.v0;
		Integer v2y = testVector.v3 - testVector.v1;

		Double angle = Math.atan2(v2y, v2x) - Math.atan2(v1y, v1x);
		if (Math.abs(angle) < threshold) {
			return true;
		} else {
			return false;
		}
	}
}
