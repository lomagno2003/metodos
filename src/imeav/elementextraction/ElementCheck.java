package imeav.elementextraction;

import java.util.Vector;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;


public class ElementCheck {
	private int i,j;
	private MatOfPoint punt;
	
	public void setI(int _i){
		this.i = _i;
	}
	public void setJ(int _j){
		this.j = _j;
	}
	public int getI(){
		return this.i;
	}
	public int getJ(){
		return this.j;
	}
	public void setPunt(MatOfPoint _punt){
		this.punt =_punt;
	}
	public MatOfPoint getPunt(){
		return this.punt;
	}
	
} ;