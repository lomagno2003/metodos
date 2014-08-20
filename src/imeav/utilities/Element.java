package imeav.utilities;

import java.util.List;
import java.util.Vector;

import org.opencv.core.*;
import org.opencv.highgui.*;
import org.opencv.imgproc.*;

public class Element {

	public Element(){padre=-1;id=-1;textos=new Vector<TextBox>();}
	public void setPoints(MatOfPoint list) {this.puntos=list;}
	public MatOfPoint getPoints() {return puntos;}
	public void setPadre(int p){this.padre=p;}
	public int getPadre(){return padre;}
	public void setId(int i){this.id=i;}
	public int getId(){return id;}
	public void addText(TextBox t){textos.add(t);}
	public Vector<TextBox> getTextos(){return textos;}


	//private List<Point> puntos;
	private MatOfPoint puntos;
	private int id,padre;
	private Vector<TextBox> textos;

};
