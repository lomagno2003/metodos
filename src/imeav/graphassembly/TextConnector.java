package imeav.graphassembly;

import imeav.utilities.Element;
import imeav.utilities.Relation;
import imeav.utilities.TextBox;
import imeav.utilities.Vec4i;

import java.util.List;
import java.util.Vector;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

/**
 * Esta clase es la encargada de unir los textos reconocidos con las diferentes
 * cajas o paths reconocidos
 * 
 * @author clomagno
 *
 */
public class TextConnector {
	private int maxDistance;

	public TextConnector(int maxDistance) {
		super();
		this.maxDistance = maxDistance;
	}

	/**
	 * Vincula los textos a los respectivos paths o boxes segun cual este más
	 * cerca
	 * 
	 * @param textos
	 * @param boxes
	 * @param caminosValidos
	 */
	public void connect(Vector<TextBox> textos, Vector<Element> boxes,
			Vector<Relation> caminosValidos) {
		for (int i = 0; i < textos.size(); i++) {
			linkText(textos.get(i), boxes, caminosValidos);
		}
	}

	/**
	 * Vincula un texto en particular a un box o path segun cual este más cerca
	 * 
	 * @param texto
	 * @param boxes
	 * @param caminosValidos
	 */
	private void linkText(TextBox texto, Vector<Element> boxes,
			Vector<Relation> caminosValidos) {
		int distanceToBox = Integer.MAX_VALUE;
		int distanceToPath = Integer.MAX_VALUE;
		int closestBox = -1, closestPath = -1;
		Rect area = texto.getArea();
		Point centro = new Point(area.x + area.width / 2, area.y + area.height
				/ 2);

		/* Busco el box más cercano */
		for (int l = 0; l < boxes.size(); l++) {
			Element box = boxes.get(l);

			/*
			 * Calculo la distancia minima que hay entre el centro del texto y
			 * el box
			 */
			int minDistance = minimumDistance(centro, box.getPoints());

			/* Me fijo si este box es el más cercano al texto hasta ahora */
			if ((minDistance != -1) && (distanceToBox > minDistance)
					&& (estaContenido(area, box.getPoints()))) {
				distanceToBox = minDistance;
				closestBox = box.getId();
			}
		}

		/* Busco el path más cercano */
		for (int l = 0; l < caminosValidos.size(); l++) {
			Relation path = caminosValidos.get(l);

			/*
			 * Calculo la minima distancia que hay entre el centro del texto y
			 * algun punto del path
			 */
			int minDistance = minimumDistances(centro, path.getSegments());

			/* Me fijo si este path es el más cercano al texto hasta ahora */
			if ((minDistance != -1) && (distanceToPath > minDistance)) {
				distanceToPath = minDistance;
				closestPath = l;
			}
		}

		/*
		 * Si se encontro un path y un box cercanos al texto, lo asocio al que
		 * se encuentre más cerca al texto.
		 */
		if ((closestBox != -1) && (closestPath != -1)) {
			if (distanceToBox <= distanceToPath) {
				/* El box esta más cerca */
				establecerTextoB(texto, closestBox, boxes);
				return;
			} else {
				/* El path esta más cerca */
				establecerTextoP(texto, closestPath, caminosValidos);
				return;
			}
		}

		/* Si no se encontro un box cercano al texto, lo asocio al path */
		if (closestBox == -1) {
			if (distanceToPath < maxDistance + maxDistance) {
				establecerTextoP(texto, closestPath, caminosValidos);
			}

			return;
		}

		/* Si no se encontro un path cercano al texto, lo asocio al box */
		if (closestPath == -1) {
			establecerTextoB(texto, closestBox, boxes);
			return;
		}
	}

	/**
	 * Retorna true si el area esta contenida en la matriz de puntos bPoints
	 * 
	 * @param area
	 * @param bPoints
	 * @return
	 */
	private boolean estaContenido(Rect area, MatOfPoint bPoints) {
		return Imgproc.boundingRect(bPoints).contains(
				new Point(area.x + area.width / 2, area.y + area.height / 2));
	}

	/**
	 * Asigna el texto al path en el caso que path != -1
	 * 
	 * @param t
	 * @param path
	 * @param caminosValidos
	 */
	private void establecerTextoP(TextBox t, int path,
			Vector<Relation> caminosValidos) {
		if (path == -1) {
			return;
		}

		caminosValidos.get(path).addText(t);
	}

	/**
	 * Asigna el texto al box en el caso que box != -1
	 * 
	 * @param t
	 * @param path
	 * @param caminosValidos
	 */
	private void establecerTextoB(TextBox t, int box, Vector<Element> boxes) {
		if (box == -1) {
			return;
		}

		boxes.get(box).addText(t);
	}

	/* FIXME Metodo similar al miniumDistance */
	private int minimumDistances(Point p, Vector<Vec4i> segments) {
		long res = Long.MAX_VALUE;
		for (int i = 0; i < segments.size(); i++) {
			Vec4i l = segments.get(i);

			Point p1 = new Point(l.v0, l.v1);
			Point p2 = new Point(l.v2, l.v3);
			int xMayor, xMenor, yMayor, yMenor;
			if (p1.x > p2.x) {
				xMayor = (int) p1.x;
				xMenor = (int) p2.x;
			} else {
				xMayor = (int) p2.x;
				xMenor = (int) p1.x;
			}
			if (p1.y > p2.y) {
				yMayor = (int) p1.y;
				yMenor = (int) p2.y;
			} else {
				yMayor = (int) p2.y;
				yMenor = (int) p1.y;
			}

			// Punto intermedio
			Point p3 = new Point(xMenor + (xMayor - xMenor), yMenor
					+ (yMayor - yMenor));

			long actual = (long) (Math.pow(p.x - p1.x, 2) + Math.pow(
					p.y - p1.y, 2));

			if (actual < res) {
				res = actual;
			}

			actual = (long) (Math.pow(p.x - p2.x, 2) + Math.pow(p.y - p2.y, 2));

			if (actual < res) {
				res = actual;
			}

			actual = (long) (Math.pow(p.x - p3.x, 2) + Math.pow(p.y - p3.y, 2));

			if (actual < res) {
				res = actual;
			}
		}

		return (int) Math.sqrt(res);
	}

	/* FIXME Metodo repetido en PathConnector */
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
}
