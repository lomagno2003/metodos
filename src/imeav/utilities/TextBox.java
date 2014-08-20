package imeav.utilities;

import org.opencv.core.*;
import org.opencv.highgui.*;
import org.opencv.imgproc.*;

public class TextBox{
	private String str;
	private Rect area;
	
public TextBox(){;}
public TextBox(String str, Rect area) {this.str=str;this.area=area;}

public void setText(String str) {this.str=str;}
public String getText() {return str;}

public void setArea(Rect area) {this.area=area;}
public Rect getArea() {return area;}




};
