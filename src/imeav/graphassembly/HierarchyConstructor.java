package imeav.graphassembly;

import imeav.utilities.Element;

import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Esta clase identifica y asigna las relaciones jerarquicas entre los
 * modulos(uno contiene a otros)
 * 
 * @author clomagno
 *
 */
public class HierarchyConstructor {
	private Size size;

	public HierarchyConstructor(Size size) {
		super();
		this.size = size;
	}

	public void applyHierarchy(List<Element> boxes) {
		for (int i = 0; i < boxes.size(); i++) {
			for (int j = i + 1; j < boxes.size(); j++) {
				apply(boxes.get(i), boxes.get(j));
			}
		}
	}

	/**
	 * Este método se encarga de detectar si el elemento i esta dentro de j o
	 * viceversa. En el caso de que ninguno este dentro del otro, no realiza
	 * ninguna modificación.
	 * 
	 * @param i
	 * @param j
	 */
	private void apply(Element i, Element j) {
		/* Obtengo el Mat con el elemento i pintada en ella */
		Mat iMat = Mat.zeros(size, CvType.CV_8UC1);
		Rect iR = Imgproc.boundingRect(i.getPoints());
		Core.rectangle(iMat, iR.tl(), iR.br(), new Scalar(255), -1);

		/* Obtengo el Mat con el elemento j pintada en ella */
		Mat jMat = Mat.zeros(size, CvType.CV_8UC1);
		Rect jR = Imgproc.boundingRect(j.getPoints());
		Core.rectangle(jMat, jR.tl(), jR.br(), new Scalar(255), -1);

		/* Calculo el AND entre ambos Mat */
		Mat And = Mat.zeros(size, CvType.CV_8UC1);
		Core.bitwise_and(iMat, jMat, And);

		/*
		 * Verifico que ambas cajas se esten tocando en algun punto, en caso
		 * contrario, el AND retornaria un Mat con todos ceros
		 */
		if (Core.countNonZero(And) == 0)
			return;

		/* El elemento que tenga el ancho más grande es el que contiene al otro */
		if (iR.width > jR.width) {
			j.setPadre(i.getId());
		} else {
			i.setPadre(j.getId());
		}
	}
}
