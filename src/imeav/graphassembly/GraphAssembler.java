package imeav.graphassembly;

import java.util.Vector;

import imeav.utilities.Element;
import imeav.utilities.Relation;
import imeav.utilities.TextBox;


public abstract class GraphAssembler {

	public abstract void buildGraph(Vector<Element> boxes,Vector<TextBox> textos,Vector<Relation> caminos);
}
