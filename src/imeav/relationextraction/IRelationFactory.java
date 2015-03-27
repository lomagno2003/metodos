package imeav.relationextraction;

import imeav.utilities.Relation;
import imeav.utilities.Vec4i;

import java.util.List;
import java.util.Set;

public interface IRelationFactory {
	public Set<Relation> getRelations(List<Vec4i> segments);
}
