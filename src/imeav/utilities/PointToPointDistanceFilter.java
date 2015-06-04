package imeav.utilities;

import org.opencv.core.Point;

public class PointToPointDistanceFilter implements Filter<Point>{
	private Point point;
	
	private Double threshold;
		
	public PointToPointDistanceFilter(Point point, Double threshold) {
		super();
		this.point = point;
		this.threshold = threshold;
	}

	@Override
	public Boolean evaluate(Point elem) {
		Double dist = Math.sqrt(
				Math.pow(point.x - elem.x , 2) + 
				Math.pow(point.y - elem.y , 2));
		return dist < threshold;
	}
}
