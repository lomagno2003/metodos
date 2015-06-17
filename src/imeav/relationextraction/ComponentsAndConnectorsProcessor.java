package imeav.relationextraction;

import imeav.graphassembly.GraphContext;
import imeav.utilities.Filter;
import imeav.utilities.PointToPointDistanceFilter;
import imeav.utilities.Relation;

import java.util.LinkedList;
import java.util.List;

import org.opencv.core.Point;

public class ComponentsAndConnectorsProcessor implements IRelationPostProcessor {
	private static Integer LOLYPOP_CLASS_INDEX = 0;

	private static Integer USES_CLASS_INDEX = 0;

	private Double threshold = 20.0;
	
	private GraphContext graphContext;

	public ComponentsAndConnectorsProcessor(GraphContext graphContext) {
		super();
		this.graphContext = graphContext;
	}

	private static class ComponentsAndConnectorRelation {
		private Relation relationA;
		private Relation relationB;
		private Relation newRelation;

		public ComponentsAndConnectorRelation(Relation relationA,
				Relation relationB,
				Relation newRelation) {
			super();
			this.relationA = relationA;
			this.relationB = relationB;
			this.newRelation = newRelation;
		}

		public Relation getRelationA() {
			return relationA;
		}

		public Relation getRelationB() {
			return relationB;
		}
		
		public Relation getNewRelation(){
			return newRelation;
		}
	}

	@Override
	public void process(List<Relation> relations) {
		List<ComponentsAndConnectorRelation> detectedRelations = new LinkedList<ComponentsAndConnectorsProcessor.ComponentsAndConnectorRelation>();

		for (Integer indexA = 0; indexA < relations.size(); indexA++) {
			Relation relationA = relations.get(indexA);
			for (Integer indexB = indexA + 1; indexB < relations.size(); indexB++) {
				Relation relationB = relations.get(indexB);

				processPoint(detectedRelations, relationA, relationB, true);
				processPoint(detectedRelations, relationA, relationB, false);
			}
		}
		
		/* Remove the detected relations and add the new ones */
		for(ComponentsAndConnectorRelation detectedRelation : detectedRelations){			
			/* If i detect at least one C&C relation, the diagram should be a C&C */
			this.graphContext.setBoxType("Component");
			
			/* Remove the relations from the list */
			relations.remove(detectedRelation.getRelationA());
			relations.remove(detectedRelation.getRelationB());
			
			relations.add(detectedRelation.getNewRelation());
		}
	}

	private void processPoint(
			List<ComponentsAndConnectorRelation> detectedRelations,
			Relation relationA, Relation relationB, Boolean extreme1) {
		/* Check with extreme1 from relationA */
		Filter<Point> pointFilter = new PointToPointDistanceFilter(
				(extreme1?relationA.getExtreme1():relationA.getExtreme2())
				, threshold);

		/* Check eXA with e1B */
		if (pointFilter.evaluate(relationB.getExtreme1())) {
			/* Check if the extremes are C&C */
			if ((extreme1 ? relationA.getTipoExt1():relationA.getTipoExt2()) == LOLYPOP_CLASS_INDEX
					&& relationB.getTipoExt1() == USES_CLASS_INDEX) {				
				/* Create the new relation */
				Relation newRelation = new Relation();
				
				/* Put all segments of the detected relations into the new relation */
				newRelation.getSegments().addAll(relationA.getSegments());
				newRelation.getSegments().addAll(relationB.getSegments());
				
				/* Set the extremes */
				newRelation.setExtreme1((extreme1 ? relationA.getExtreme2():relationA.getExtreme1()));
				newRelation.setExtreme2(relationB.getExtreme2());
				
				/* Set the extremes tipes */
				newRelation.setTipoExt1((extreme1 ? relationA.getTipoExt1():relationA.getTipoExt2()));
				newRelation.setTipoExt2(relationB.getTipoExt1());
				
				ComponentsAndConnectorRelation detectedRelation = new ComponentsAndConnectorRelation(
						relationA, relationB, newRelation);
				
				detectedRelations.add(detectedRelation);
			}
		}

		/* Check eXA with e2B */
		if (pointFilter.evaluate(relationB.getExtreme2())) {
			/* Check if the extremes are C&C */
			if ((extreme1 ? relationA.getTipoExt1():relationA.getTipoExt2()) == LOLYPOP_CLASS_INDEX
					&& relationB.getTipoExt2() == USES_CLASS_INDEX) {
				/* Create the new relation */
				Relation newRelation = new Relation();
				
				/* Put all segments of the detected relations into the new relation */
				newRelation.getSegments().addAll(relationA.getSegments());
				newRelation.getSegments().addAll(relationB.getSegments());
				
				/* Set the extremes */
				newRelation.setExtreme1((extreme1 ? relationA.getExtreme2():relationA.getExtreme1()));
				newRelation.setExtreme2(relationB.getExtreme1());
				
				/* Set the extremes tipes */
				newRelation.setTipoExt1((extreme1 ? relationA.getTipoExt1():relationA.getTipoExt2()));
				newRelation.setTipoExt2(relationB.getTipoExt2());
				
				ComponentsAndConnectorRelation detectedRelation = new ComponentsAndConnectorRelation(
						relationA, relationB, newRelation);
				
				detectedRelations.add(detectedRelation);
			}
		}
	}
}
