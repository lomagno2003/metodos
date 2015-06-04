package imeav.relationextraction;

import imeav.utilities.Relation;

import java.util.List;

public interface IRelationPostProcessor {
	public void process(List<Relation> relations);
}
