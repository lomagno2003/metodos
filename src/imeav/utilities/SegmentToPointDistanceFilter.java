package imeav.utilities;

import org.opencv.core.Point;

/**
 * 
 * @author clomagno
 *
 */
public class SegmentToPointDistanceFilter implements Filter<Vec4i> {
	private Point point;
	private Double threshold;

	public SegmentToPointDistanceFilter(Point point, Double threshold) {
		this.point = point;
		this.threshold = threshold;
	}

	@Override
	public Boolean evaluate(Vec4i elem) {
		Double distance1 = getDistance(point.x, point.y,
				Integer.valueOf(elem.v0).doubleValue(), Integer
						.valueOf(elem.v1).doubleValue());
		
		Double distance2 = getDistance(point.x, point.y,
				Integer.valueOf(elem.v2).doubleValue(), Integer
						.valueOf(elem.v3).doubleValue());
		
		return distance1 <= threshold || distance2 <= threshold;
	}

	private Double getDistance(Double x1, Double y1, Double x2, Double y2) {
		return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
	}

}
