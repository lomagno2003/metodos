package imeav.graphassembly;

import java.util.Vector;

import imeav.utilities.Element;
import imeav.utilities.Relation;
import imeav.utilities.TextBox;


public interface IGraphAssembler {

	public void buildGraph(Vector<Element> boxes,Vector<TextBox> textos,Vector<Relation> caminos);
}
