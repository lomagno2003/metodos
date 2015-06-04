package imeav.graphassembly;

import java.util.Vector;

import org.opencv.core.*;

import imeav.utilities.Element;
import imeav.utilities.Relation;
import imeav.utilities.TextBox;

/**
 * Esta clase es la encargada de buscar relaciones entre un conjunto de textos,
 * boxes y paths dados. A su vez, también es la encargada de exportar el grafo
 * generado a un archivo ".dot"
 * 
 * @author clomagno
 *
 */
public class ProximityGraph implements IGraphAssembler {
	private HierarchyConstructor hierarchyConstructor;
	private PathConnector pathConnector;
	private TextConnector textConnector;
	private GraphExporter graphExporter;
	
	private String outputPath;

	public ProximityGraph(Size size, int maxDistance, String outputPath, GraphContext graphContext) {
		this.outputPath = outputPath;

		this.hierarchyConstructor = new HierarchyConstructor(size);
		this.pathConnector = new PathConnector(maxDistance);
		this.textConnector = new TextConnector(maxDistance);
		this.graphExporter = new GraphExporter(graphContext);
	}

	@Override
	public void buildGraph(Vector<Element> boxes, Vector<TextBox> textos,
			Vector<Relation> caminos) {
		/* Busco las relaciones de contencion(un modulo contiene a otro) */
		hierarchyConstructor.applyHierarchy(boxes);

		/*
		 * Conecto los paths con los boxes más cercanos y descarto los paths
		 * invalidos
		 */
		Vector<Relation> caminosValidos = pathConnector.connectPaths(caminos,
				boxes);

		/*
		 * Asigno los textos reconocidos a los boxes o paths que les
		 * corresponden
		 */
		textConnector.connect(textos, boxes, caminosValidos);

		/* Exporto el grafo creado al archivo ubicado en outputPath */
		graphExporter.export(caminosValidos, boxes, outputPath);
	}
};
