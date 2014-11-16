package imeav.graphassembly;

import imeav.utilities.Element;
import imeav.utilities.Relation;
import imeav.utilities.TextBox;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

/**
 * Esta clase es la encargada de exportar un grafo a su respectivo archivo
 * ".dot"
 * 
 * @author clomagno
 *
 */
public class GraphExporter {
	public void export(Vector<Relation> caminosValidos, Vector<Element> boxes,
			String writeDir) {
		Vector<String> Modules = new Vector<String>();
		Vector<String> Relations = new Vector<String>();
		Vector<String> Connections = new Vector<String>();

		for (int i = 0; i < caminosValidos.size(); i++) {
			Relation p = caminosValidos.get(i);
			pathToBoxBuild(p, p.getConnection1(), p.getConnection2(), boxes,
					Connections);
		}

		setToGraph(boxes, Modules, Relations, Connections, writeDir);
	}

	private void pathToBoxBuild(Relation p, int b1, int b2,
			Vector<Element> boxes, Vector<String> Connections) {
		// Union valida de path p, ext1 a b1 y ext2 a b2
		// Ver el tipo que tiene path!

		String separatedByCommaText = new String();

		// Me quedo con los textos que tiene separado por comas
		Vector<TextBox> tb = p.getText();

		for (int i = 0; i < tb.size(); i++) {

			separatedByCommaText = separatedByCommaText.concat(tb.get(i)
					.getText());
			if ((i != (tb.size() - 1)))
				separatedByCommaText = separatedByCommaText.concat(",");
		}
		String str1 = new String(Integer.toString(boxes.get(b1).getId())
				+ " -- " + Integer.toString(boxes.get(b2).getId())
				+ " [label=\" Type= 'usage', Directionality='");

		// {0,0} {1,1} se considera Bi-directional
		if (p.getTipoExt1() == p.getTipoExt2())
			str1 = str1.concat("bi");

		// {0,1} 1 -> 2
		if (p.getTipoExt1() < p.getTipoExt2())
			str1 = str1.concat(Integer.toString(boxes.get(b1).getId()) + "-"
					+ Integer.toString(boxes.get(b2).getId()));

		// {1,0} 2 -> 1
		if (p.getTipoExt1() > p.getTipoExt2())
			str1 = str1.concat(Integer.toString(boxes.get(b2).getId()) + "-"
					+ Integer.toString(boxes.get(b1).getId()));

		str1 = str1.concat("', Name='");
		str1 = str1.concat(separatedByCommaText);
		str1 = str1.concat("'\"];");
		Connections.add(str1);
	}

	private void setToGraph(Vector<Element> boxes, Vector<String> Modules,
			Vector<String> Relations, Vector<String> Connections,
			String writeDir) {
		for (int i = 0; i < boxes.size(); i++) {

			String separatedByCommaText = new String();
			Vector<TextBox> tb = boxes.get(i).getTextos();
			// Me quedo con los textos que tiene separado por comas
			for (int j = 0; j < tb.size(); j++) {
				separatedByCommaText = separatedByCommaText.concat(tb.get(j)
						.getText());
				if (j != (tb.size() - 1))
					separatedByCommaText = separatedByCommaText.concat(",");
			}
			// Formato graphviz
			String str = new String(Integer.toString(boxes.get(i).getId())
					+ " [label=" + "\""
					+ Integer.toString(boxes.get(i).getId())
					+ ", Type='Module', Name='" + separatedByCommaText + "'\""
					+ "]" + ";");
			Modules.add(str);

			if (boxes.get(i).getPadre() != -1) {
				// boxes.get(i).getId() tiene padre boxes.get(i).getPadre()
				String str1 = new String(Integer.toString(boxes.get(i).getId())
						+ " -- " + Integer.toString(boxes.get(i).getPadre())
						+ " [label=\" Type='is-part-of', Directionality='"
						+ Integer.toString(boxes.get(i).getId()) + "-"
						+ Integer.toString(boxes.get(i).getPadre()) + "'\"];");
				Relations.add(str1);
			}

		}

		// Escribir en archivo dot
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(writeDir, "UTF-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writer.println("graph G{\n");
		for (int k = 0; k < Modules.size(); k++)
			writer.println(Modules.get(k));
		writer.println("");
		for (int k = 0; k < Relations.size(); k++)
			writer.println(Relations.get(k));
		writer.println("");
		for (int k = 0; k < Connections.size(); k++)
			writer.println(Connections.get(k));
		writer.println("");
		writer.println("}");
		writer.close();
	}
}
