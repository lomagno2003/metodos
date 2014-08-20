package imeav.utilities;

import java.util.Vector;

import org.opencv.core.*;
import org.opencv.highgui.*;
import org.opencv.imgproc.*;

public class Relation {


	public Relation(){b1=-1;b2=-2;textos=new Vector<TextBox>();}
	public	Vector<Vec4i> getSegments(){ return vec;}
	public	void setSegments( Vector<Vec4i> vec){this.vec=vec;}
	public	Point getExtreme1(){return extreme1;}
	public	Point getExtreme2(){return extreme2;}
	public	void setExtreme1(Point extreme){extreme1=extreme;}
	public	void setExtreme2(Point extreme){extreme2=extreme;}
	public	int getTipoExt1(){return tipoExt1;}
	public	int getTipoExt2(){return tipoExt2;}
	public	void setTipoExt1(int tipo){this.tipoExt1=tipo;}
	public	void setTipoExt2(int tipo){this.tipoExt2=tipo;}
	public	void connects(int B1,int B2){b1=B1;b2=B2;}
	public	int getConnection1(){return b1;}
	public	int getConnection2(){return b2;}


	private	Vector<Vec4i> vec;
	private Vector<TextBox> textos;

	private	Point extreme1;
	private	Point extreme2;

	private	int tipoExt1;
	private	int tipoExt2;

	private	int b1,b2;

	public void addText(TextBox t) {textos.add(t);}
	public Vector<TextBox> getText(){return textos;}
};
