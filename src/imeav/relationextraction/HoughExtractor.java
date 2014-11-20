package imeav.relationextraction;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.*;
import org.opencv.highgui.*;
import org.opencv.imgproc.*;

import imeav.extractor.DrawPath;
import imeav.extractor.EndPointMatrix;
import imeav.extractor.Extractor;
import imeav.utilities.Relation;
import imeav.utilities.Vec4i;

public class HoughExtractor implements IRelationExtractor
{
	
	public static void showResult(Mat img,String titulo) {
		
	    MatOfByte matOfByte = new MatOfByte();
	    Highgui.imencode(".jpg", img, matOfByte);
	    byte[] byteArray = matOfByte.toArray();
	    BufferedImage bufImage = null;
	    try {
	        InputStream in = new ByteArrayInputStream(byteArray);
	        bufImage = ImageIO.read(in);
	        JFrame frame = new JFrame();
	        frame.setTitle(titulo);
	        frame.getContentPane().add(new JLabel(new ImageIcon(bufImage)));
	        frame.pack();
	        frame.setVisible(true);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}




@Override
public Vector<Relation> extract(Mat binary, Mat boxes) {
	Extractor extractor = new Extractor();
	return extractor.extract(binary, boxes);
}

};
