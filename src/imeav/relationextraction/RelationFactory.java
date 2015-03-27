package imeav.relationextraction;

import imeav.utilities.AndFilter;
import imeav.utilities.Filter;
import imeav.utilities.OrFilter;
import imeav.utilities.Relation;
import imeav.utilities.SegmentToPointDistanceFilter;
import imeav.utilities.SimilarAngleSegmentFilter;
import imeav.utilities.Vec4i;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.opencv.core.Point;

/**
 * This class returns a list with all Relations contained in a list of segments, linking them accordingly.
 * 
 * @author clomagno
 *
 */
public class RelationFactory implements IRelationFactory {
	@Override
	public Set<Relation> getRelations(List<Vec4i> segments) {
		Set<Relation> result = new HashSet<Relation>();
		Set<Vec4i> usedSegments = new HashSet<Vec4i>();

		for (Vec4i segment : segments) {
			if (!usedSegments.contains(segment)) {
				Relation camino = getRelation(segment, usedSegments, segments);
				result.add(camino);

				usedSegments.addAll(camino.getSegments());
			}
		}

		return result;
	}

	
	public Relation getRelation(Vec4i segment, Set<Vec4i> usedSegments,
			List<Vec4i> segments) {

		Set<Vec4i> resultSegments = new HashSet<Vec4i>();
		resultSegments.add(segment);

		/* Find all segments of the relation */
		Boolean segmentFound = true;
		while (segmentFound) {
			segmentFound = false;

			Set<Vec4i> newNeighbors = new HashSet<Vec4i>();

			for (Vec4i unanalyzedSegment : resultSegments) {
				if (!usedSegments.contains(unanalyzedSegment)) {
					usedSegments.add(unanalyzedSegment);

					Set<Vec4i> neighbors = getNeighbors(segments, usedSegments,
							unanalyzedSegment);

					if (!neighbors.isEmpty()) {
						segmentFound = true;
					}

					newNeighbors.addAll(neighbors);
				}
			}

			resultSegments.addAll(newNeighbors);
		}
		
		/* Find the extremes */
		Extractor xExtractor = new XExtractor();
		Extractor yExtractor = new YExtractor();
		Comparator<Integer> lowerComparator= new LowerComparator();
		Comparator<Integer> higherComparator= new HigherComparator();
		
		/* Find max and min extreme using X */
		Point maxX = getExtreme(segments, xExtractor, higherComparator);
		Point minX = getExtreme(segments, xExtractor, lowerComparator);
		
		/* Find max and min extreme using Y */
		Point maxY = getExtreme(segments, yExtractor, higherComparator);
		Point minY = getExtreme(segments, yExtractor, lowerComparator);
		
		/* Use the extremes of the max distance between the selected dimension */
		Point extreme1;
		Point extreme2;
		if(maxX.x -minX.x > maxY.y - minY.y){
			extreme1 = maxX;
			extreme2 = minX;
		} else {
			extreme1 = maxY;
			extreme2 = minY;
		}
		
		/* Construct the relation */
		Relation result = new Relation();
		result.setSegments(new Vector<Vec4i>(resultSegments));
		result.setExtreme1(extreme1);
		result.setExtreme2(extreme2);

		return result;
	}

	private interface Extractor {
		public Integer extract1(Vec4i segment);

		public Integer extract2(Vec4i segment);
		
		public Integer extractPoint(Point point);
	}

	private class XExtractor implements Extractor {
		@Override
		public Integer extract1(Vec4i segment) {
			return segment.v0;
		}

		@Override
		public Integer extract2(Vec4i segment) {
			return segment.v2;
		}

		@Override
		public Integer extractPoint(Point point) {
			return Double.valueOf(point.x).intValue();
		}
		
		
	}

	private class YExtractor implements Extractor {
		@Override
		public Integer extract1(Vec4i segment) {
			return segment.v1;
		}

		@Override
		public Integer extract2(Vec4i segment) {
			return segment.v3;
		}

		@Override
		public Integer extractPoint(Point point) {
			return Double.valueOf(point.y).intValue();
		}
	}

	private class LowerComparator implements Comparator<Integer> {
		@Override
		public int compare(Integer o1, Integer o2) {
			if (o1 <= o2) {
				return 1;
			} else {
				return -1;
			}
		}
	}

	private class HigherComparator implements Comparator<Integer> {
		@Override
		public int compare(Integer o1, Integer o2) {
			if (o1 > o2) {
				return 1;
			} else {
				return -1;
			}
		}
	}

	private Point getExtreme(List<Vec4i> segments, Extractor extractor,
			Comparator<Integer> comparator) {
		Point extreme = null;
		for (Vec4i segmentIterator : segments) {
			/* Initialize segments */
			if (extreme == null) {
				extreme = new Point(segmentIterator.v0, segmentIterator.v1);
			}

			/* Look for the extreme with higher x */
			if (comparator.compare(extractor.extract1(segmentIterator),
					extractor.extract2(segmentIterator)) > 0) {
				if (comparator.compare(extractor.extract1(segmentIterator),
						extractor.extractPoint(extreme)) > 0) {
					extreme = new Point(segmentIterator.v0, segmentIterator.v1);
				}
			} else {
				if (comparator.compare(extractor.extract2(segmentIterator),
						extractor.extractPoint(extreme)) > 0) {
					extreme = new Point(segmentIterator.v0, segmentIterator.v1);
				}
			}
		}

		return extreme;
	}

	/**
	 * Returns the neighbors segments around the segment and with similar angle.
	 * 
	 * @param usedSegments
	 * 
	 * @param x
	 * @param y
	 * @param k
	 * @return
	 */
	private Set<Vec4i> getNeighbors(Collection<Vec4i> segments,
			Set<Vec4i> usedSegments, Vec4i unanalyzedSegment) {
		Double distanceThreshold = null;
		Double angleThreshold = null;

		Filter<Vec4i> filter = new AndFilter<Vec4i>(new OrFilter<Vec4i>(
				new SegmentToPointDistanceFilter(new Point(
						unanalyzedSegment.v0, unanalyzedSegment.v1),
						distanceThreshold), new SegmentToPointDistanceFilter(
						new Point(unanalyzedSegment.v2, unanalyzedSegment.v3),
						distanceThreshold)), new SimilarAngleSegmentFilter(
				unanalyzedSegment, angleThreshold));

		Set<Vec4i> result = new HashSet<Vec4i>();

		for (Vec4i segment : segments) {
			if (!usedSegments.contains(segment)) {
				if (filter.evaluate(segment)) {
					result.add(segment);
				}
			}
		}

		return result;
	}
}
