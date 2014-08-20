package imeav.relationextraction;

import java.util.Comparator;

import imeav.utilities.Vec4i;


public class HoughComparator implements Comparator{
private boolean menor(Vec4i s1, Vec4i s2){
    return Math.pow(s1.v0-s1.v2,2)+Math.pow(s1.v1-s1.v3,2) <
    	Math.pow(s2.v0-s2.v2,2)+Math.pow(s2.v1-s2.v3,2);
}

@Override
public int compare(Object o1, Object o2) {
	// TODO Auto-generated method stub
	
	Vec4i s1 = (Vec4i) o1;
	Vec4i s2 = (Vec4i) o2;
	
	if (menor(s1,s2))
		return -1;
	if (menor(s2,s1))
		return 1;
	return 0;
}

}