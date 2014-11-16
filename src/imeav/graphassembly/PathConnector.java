package imeav.graphassembly;

import imeav.utilities.Element;
import imeav.utilities.Relation;

import java.util.List;
import java.util.Vector;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

public class PathConnector {
	private int maxDistance;

	public PathConnector(int maxDistance) {
		super();
		this.maxDistance = maxDistance;
	}

	/**
	 * Conecta los paths a sus respectivas cajas y retorna los paths validos(es
	 * decir los que se pudieron conectar en ambos extremos correctamente)
	 * 
	 * @param caminos
	 * @param boxes
	 * @return
	 */
	public Vector<Relation> connectPaths(Vector<Relation> caminos,
			Vector<Element> boxes) {
		Vector<Relation> caminosValidos = new Vector<Relation>();

		for (int i = 0; i < caminos.size(); i++) {
			Relation p = caminos.get(i);

			// Ver camino_i de quien a quien va
			Point ext1 = p.getExtreme1();
			Point ext2 = p.getExtreme2();

			int[] b1andb2 = { -1, -1 };
			findClosestBox(ext1, ext2, b1andb2, boxes);

			if (validConnection(b1andb2[0], b1andb2[1], boxes)) {
				p.connects(b1andb2[0], b1andb2[1]);

				caminosValidos.add(p);
			}
		}

		return caminosValidos;
	}

	/**
	 * Dados los extremos de un path, retorna las dos cajas más cercanas a cada
	 * extremo
	 * 
	 * @param ext1
	 * @param ext2
	 * @param b1andb2
	 * @param boxes
	 */
	private void findClosestBox(Point ext1, Point ext2, int[] b1andb2,
			Vector<Element> boxes) {
		int distance1 = Integer.MAX_VALUE;
		int distance2 = Integer.MAX_VALUE;

		for (int l = 0; l < boxes.size(); l++) {
			Element box = boxes.get(l);

			/* Obtengo las distancias entre cada extremo y el box */
			int minDistance1 = minimumDistance(ext1, box.getPoints());
			int minDistance2 = minimumDistance(ext2, box.getPoints());

			// Si hay un box mas cerca de lo que teniamos hasta ahora
			if ((minDistance1 != -1) && (distance1 > minDistance1)) {
				distance1 = minDistance1;
				b1andb2[0] = box.getId();
			}
			if ((minDistance2 != -1) && (distance2 > minDistance2)) {
				distance2 = minDistance2;
				b1andb2[1] = box.getId();
			}
		}
	}

	/**
	 * Este metodo retorna la distancia minima entre un punto y una coleccion de
	 * puntos. En el caso que la distancia sea mayor a maxDistance, retorna -1.
	 * 
	 * @param ext
	 * @param puntos
	 * @return
	 */
	private int minimumDistance(Point ext, MatOfPoint puntos) {
		long res = Long.MAX_VALUE;

		List<Point> pts = puntos.toList();

		for (int i = 0; i < pts.size(); i++) {
			Point p = pts.get(i);

			long actual = (long) (Math.pow(p.x - ext.x, 2) + Math.pow(p.y
					- ext.y, 2));

			if (actual < res) {
				res = actual;
			}
		}

		if ((int) res < maxDistance) {
			return (int) Math.sqrt(res);
		} else {
			return -1;
		}
	}

	/**
	 * Este método verifica que la conexion entre b1 y b2 sea valida, siguiendo
	 * ciertas reglas
	 * 
	 * @param b1
	 * @param b2
	 * @param boxes
	 * @return
	 */
	private boolean validConnection(int b1, int b2, Vector<Element> boxes) {
		/*
		 * Si b1 o b2 son -1, entonces los extremos no estan conectados a
		 * ninguna caja, lo cual no es permitido
		 */
		if ((b1 == -1) || (b2 == -1)) {
			return false;
		}

		/*
		 * Si b1 == b2, entonces esta conectado a la misma caja, lo cual no es
		 * posible
		 */
		if (b1 == b2) {
			return false;
		}

		/*
		 * Si alguna contiene a la otra, entonces no son admitidos paths entre
		 * ellas
		 */
		if ((boxes.get(b1).getPadre() == b2)
				|| (boxes.get(b2).getPadre() == b1)) {
			return false;
		}

		return true;
	}
}
