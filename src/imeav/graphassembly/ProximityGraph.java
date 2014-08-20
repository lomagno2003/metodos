package imeav.graphassembly;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.*;
import org.opencv.highgui.*;
import org.opencv.imgproc.*;

import imeav.utilities.Element;
import imeav.utilities.Relation;
import imeav.utilities.TextBox;
import imeav.utilities.Vec4i;


public class ProximityGraph extends GraphAssembler
{
	private Vector<String> Modules,Relations,Connections;
	
	public static void showResult(Mat img,String titulo) {
		
	    //Imgproc.resize(img, img, new Size(640, 480));
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
	public ProximityGraph(Size size,int MAXIMUM_DIST,String writeDir) {
		s=size;
		MAXIMUM_DISTANCE=MAXIMUM_DIST;
		this.writeDir=writeDir;
	}

	private String writeDir;
	public void buildGraph(Vector<Element> boxes,  Vector<TextBox> textos, Vector<Relation> caminos){
		
		Modules=new Vector<String>();
		Relations=new Vector<String>();
		Connections=new Vector<String>();
		
		
		graphBuild=Mat.zeros(s,CvType.CV_8UC1);////////


		establecerJerarquia(boxes);

		Vector<Relation> caminosValidos = new Vector<Relation>();


		for (int i=0;i<caminos.size();i++){
			Relation p=caminos.get(i);

			//Ver camino_i de quien a quien va
			Point ext1=p.getExtreme1();
			Point ext2=p.getExtreme2();
			//Integer b1=new Integer(-1);Integer b2=new Integer(-1);

			graphBuild.setTo(new Scalar(0));
			drawPath(graphBuild,p.getSegments());////////


			int[] b1andb2 = {-1 , -1};
			findClosestBox(ext1,ext2,b1andb2,boxes);


			//System.out.println("Boxes: "+Integer.toString(b1andb2[0])+" "+Integer.toString(b1andb2[1]));
			//waitKey(0);

			if (validConnection(b1andb2[0],b1andb2[1],boxes)){
				p.connects(b1andb2[0],b1andb2[1]);//El path guarda que box une, es informacion util despues!
				caminosValidos.add(p);
				
				
				Rect r1=Imgproc.boundingRect(boxes.get(b1andb2[0]).getPoints());
				Core.rectangle(graphBuild,r1.tl(),r1.br(),new Scalar(255));////////
				Rect r2 = Imgproc.boundingRect(boxes.get(b1andb2[1]).getPoints());
				Core.rectangle(graphBuild,r2.br(),r2.tl(),new Scalar(255));////////
				//showResult(graphBuild,"Graph build");

				//System.out.println("Valid connect");
				
				

			}

		}
		
		
		for (int i=0;i<textos.size();i++){
			//Unir cada texto a la caja que lo contiene o a la flecha mas cercana
			text_findClosestBoxPathBuild(textos.get(i),boxes,caminosValidos);
		}
		
		for (int i=0;i<caminosValidos.size();i++){
			Relation p = caminosValidos.get(i);
			pathToBoxBuild(p, p.getConnection1(), p.getConnection2(), boxes);
		}
		
		
		setToGraph(boxes);
		
		
		
	}

	private Size s;
	private void setToGraph(Vector<Element> boxes){
		for (int i=0;i<boxes.size();i++){

			String separatedByCommaText= new String();
			Vector<TextBox> tb=boxes.get(i).getTextos();
			//Me quedo con los textos que tiene separado por comas
			for (int j=0;j<tb.size();j++){
				separatedByCommaText=separatedByCommaText.concat(tb.get(j).getText());
					if (j != (tb.size() -1))
						separatedByCommaText=separatedByCommaText.concat(",");
			}
			//Formato graphviz
			String str=new String(
					Integer.toString(boxes.get(i).getId())
					+" [label="+
					"\"" +
					Integer.toString(boxes.get(i).getId())
					+ ", Type='Module', Name='" +
					separatedByCommaText +
					"'\""
					+"]"
					+ ";"
					);
			Modules.add(str);
				
			if (boxes.get(i).getPadre()!= -1){
				//boxes.get(i).getId() tiene padre boxes.get(i).getPadre()
				String str1= new String(
						Integer.toString(boxes.get(i).getId())
						+ " -- "
						+ Integer.toString(boxes.get(i).getPadre())
						+" [label=\" Type='is-part-of', Directionality='"
						+ Integer.toString(boxes.get(i).getId()) +
						"-"
						+ Integer.toString(boxes.get(i).getPadre())
						+ "'\"];"
						);
				Relations.add(str1);
			}
			
		}
		
		
		
		
		
		//Escribir en archivo dot
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(writeDir, "UTF-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writer.println("graph G{\n");
		for (int k=0;k<Modules.size();k++)
			writer.println(Modules.get(k));
		writer.println("");
		for (int k=0;k<Relations.size();k++)
			writer.println(Relations.get(k));
		writer.println("");
		for (int k=0;k<Connections.size();k++)
			writer.println(Connections.get(k));
		writer.println("");
		writer.println("}");
		writer.close();
		
	}
	private void establecerJerarquia(Vector<Element> boxes) {
		for (int i=0;i<boxes.size();i++)
			for (int j=i+1;j<boxes.size();j++){
				jerarquizar(boxes.get(i),boxes.get(j));
			}
	}
	private void jerarquizar(Element i,Element j) {
		Mat iMat=Mat.zeros(s,CvType.CV_8UC1);Mat jMat=Mat.zeros(s,CvType.CV_8UC1);
		Rect iR=Imgproc.boundingRect(i.getPoints());Rect jR=Imgproc.boundingRect(j.getPoints());
		Core.rectangle(iMat,iR.tl(),iR.br(),new Scalar(255),-1);Core.rectangle(jMat,jR.tl(),jR.br(),new Scalar(255),-1);

		Mat And=Mat.zeros(s,CvType.CV_8UC1);
		Core.bitwise_and(iMat,jMat,And);

		if (Core.countNonZero(And)==0)
			return;



		if (iR.width > jR.width){
			//cout<<"I contiene a J"<<to_string(i.getId())<<endl;
			j.setPadre(i.getId());
		}
		else{
			//cout<<"J contiene a I "<<to_string(j.getId())<<endl;
			i.setPadre(j.getId());
		}
		//imshow("I",iMat);imshow("J",jMat);imshow("AND",And);waitKey(0);
		return;
	}
	private void findClosestBox(Point ext1,Point ext2,int[] b1andb2,Vector<Element> boxes) {
		int d1=Integer.MAX_VALUE,d2=Integer.MAX_VALUE;

		//Mat graphBuild=Mat.zeros(s,CvType.CV_8UC1);////////

		for (int l=0;l<boxes.size();l++){
			Element b=boxes.get(l);

			//Minima distancia que existe entre un punto extremo y un set de puntos de la caja
			int mD1=minimumDistance(ext1,b.getPoints());
			int mD2=minimumDistance(ext2,b.getPoints());

			//System.out.println("Distancia {md1,md2} "+Integer.toString(mD1)+Integer.toString(mD2));
			//Si hay un box mas cerca de lo que teniamos hasta ahora
			if ((mD1!=-1)&&(d1>mD1)){
				//System.out.println("Actualiz b1");
				d1=mD1;b1andb2[0]=b.getId();
			}
			if ((mD2!=-1)&&(d2>mD2)){
				//System.out.println("Actualiz b2");
				d2=mD2;b1andb2[1]=b.getId();
			}

			
		}
		
		
		
		
	}
	private int minimumDistance(Point ext,MatOfPoint puntos) {
		long res=Long.MAX_VALUE; int devolver=-1;
		
		List<Point> pts= puntos.toList();
		
		for (int i=0;i<pts.size();i++){
		//for (int i=0;i<puntos.cols();i++){
			Point p=pts.get(i);//new Point(puntos.get(0,i)[0],puntos.get(0,i)[1]);
			long actual= (long)(p.x-ext.x)*(long)(p.x-ext.x) + (long)(p.y-ext.y)*(long)(p.y-ext.y);
			//sqrt(actual)//Para hacerlo mas rapido no calculo sqrt
			if (actual<res){
				res=actual;
				devolver=i;
			}
		}

		//System.out.println(Double.toString(Math.sqrt(res)) + " Vs " + Integer.toString(MAXIMUM_DISTANCE));
		
		if ((int)Math.sqrt(res) < MAXIMUM_DISTANCE){
			//System.out.println("< Maximum Dist!");
			return (int) Math.sqrt(res);//res
		}
		else
			return -1;
	}
	
	
	
	
	private int minimumDistanceS(Point p, Vector<Vec4i> segments) {
		long res=Long.MAX_VALUE; int devolver=-1;
		for (int i=0;i<segments.size();i++){

			Vec4i l = segments.get(i);

			Point p1=new Point(l.v0,l.v1);
			Point p2=new Point(l.v2,l.v3);
			int xMayor,xMenor,yMayor,yMenor;
			if (p1.x > p2.x){
				xMayor=(int) p1.x;
				xMenor=(int) p2.x;
			}
			else {
				xMayor=(int) p2.x;
				xMenor=(int) p1.x;
			}
			if (p1.y>p2.y){
				yMayor=(int) p1.y;
				yMenor=(int) p2.y;
			}
			else {
				yMayor=(int) p2.y;
				yMenor=(int) p1.y;
			}
			//Punto intermedio
			Point p3=new Point(xMenor+(xMayor-xMenor),yMenor+(yMayor-yMenor));


			long actual=((long)(p.x-p1.x))*((long)(p.x-p1.x)) + ((long)(p.y-p1.y))*((long)(p.y-p1.y));
			//sqrt(actual)//Para hacerlo mas rapido no calculo sqrt
			if (actual<res){
				res=actual;
				devolver=i;
			}
			actual=(long)(p.x-p2.x)*(long)(p.x-p2.x) + (long)(p.y-p2.y)*(long)(p.y-p2.y);
			//sqrt(actual)//Para hacerlo mas rapido no calculo sqrt
			if (actual<res){
				res=actual;
				devolver=i;
			}
			actual=(long)(p.x-p3.x)*(long)(p.x-p3.x) + (long)(p.y-p3.y)*(long)(p.y-p3.y);
			//sqrt(actual)//Para hacerlo mas rapido no calculo sqrt
			if (actual<res){
				res=actual;
				devolver=i;
			}


		}


		/*if (sqrt(res) < MAXIMUM_DISTANCE)
			return sqrt(res);//res
		else
			return -1;*/
		return (int) Math.sqrt(res);
	}
	private boolean validConnection(int b1,int b2, Vector<Element> boxes) {
		if ((b1==-1)||(b2==-1))//Alguna es -1, uno de los extremos no llega a ninguna caja cerca
			return false;

		if (b1==b2)//Misma caja
			return false;

		if ((boxes.get(b1).getPadre() == b2)||(boxes.get(b2).getPadre() == b1))//Una contiene a la otra?
			return false;

		return true;
	}
	private void pathToBoxBuild(Relation p,int b1,int b2,Vector<Element> boxes) {
		//Union valida de path p, ext1 a b1 y ext2 a b2
		//Ver el tipo que tiene path!
		
		String separatedByCommaText= new String();
		//Vector<TextBox> tb=boxes.get(i).getTextos();
		//Me quedo con los textos que tiene separado por comas
		
		//String tbs=new String();
		Vector<TextBox> tb=p.getText();
		
		for (int i=0;i<tb.size();i++){
			
			separatedByCommaText=separatedByCommaText.concat(tb.get(i).getText());
			if ((i != (tb.size() -1)))
				separatedByCommaText=separatedByCommaText.concat(",");
		}
		String str1= new String(
				Integer.toString(boxes.get(b1).getId())
				+ " -- "
				+ Integer.toString(boxes.get(b2).getId())
				+" [label=\" Type= 'usage', Directionality='" );
		
		// {0,0} {1,1} se considera Bi-directional
		if (p.getTipoExt1()==p.getTipoExt2())
			str1=str1.concat("bi");
		
		// {0,1} 1 -> 2
		if (p.getTipoExt1() < p.getTipoExt2())
			str1=str1.concat(
				Integer.toString(boxes.get(b1).getId()) +
				"-"
				+ Integer.toString(boxes.get(b2).getId())
				);
		
		// {1,0} 2 -> 1
		if (p.getTipoExt1() > p.getTipoExt2())
			str1=str1.concat(
		Integer.toString(boxes.get(b2).getId()) +
		"-"
		+ Integer.toString(boxes.get(b1).getId())
		);
		
		
		str1=str1.concat("', Name='");
		str1=str1.concat(separatedByCommaText);
		str1=str1.concat("'\"];");
		Connections.add(str1);

		//System.out.println(str1);

	}
	private void establecerTextoB(TextBox t,int box,Vector<Element> boxes){
		//EL TEXTO ESTA CONTENIDO EN EL BOX NUMERO BOX

		if (box==-1){
			//muestra=Mat.zeros(s,CvType.CV_8UC1);
			//Core.rectangle(muestra,t.getArea().tl(),t.getArea().br(),new Scalar(255));
			return;
		}
		muestra=Mat.zeros(s,CvType.CV_8UC1);
		Core.rectangle(muestra,t.getArea().tl(),t.getArea().br(),new Scalar(255));
		

		//MatOfPoint 
		Rect r = Imgproc.boundingRect(boxes.get(box).getPoints());
		Core.rectangle(muestra,r.tl(),r.br(),new Scalar(255));
		
		//showResult(muestra, "Texto a Box");
		
		boxes.get(box).addText(t);		
		
	}
	private void establecerTextoP(TextBox t,int path,Vector<Relation> caminosValidos){
		//EL TEXTO PERTENECE A UN PATH NUMERO PATH DE LOS CAMINOS VALIDOS
		if (path==-1){
			//muestra=Mat.zeros(s,CvType.CV_8UC1);
			//Core.rectangle(muestra,t.getArea().br(),t.getArea().tl(),new Scalar(255));
			return;
		}

		muestra=Mat.zeros(s,CvType.CV_8UC1);
		Core.rectangle(muestra,t.getArea().tl(),t.getArea().br(),new Scalar(255));
		drawPath(muestra,caminosValidos.get(path).getSegments());

		//showResult(muestra, "Texto a path");
		//System.out.println("A"+t.getText());
		caminosValidos.get(path).addText(t);
	}
	private int MAXIMUM_DISTANCE;

	private void drawPath(Mat m, Vector<Vec4i> path) {
		for (int i=0;i<path.size();i++){
			Vec4i l = path.get(i);
			Core.line( m, new Point(l.v0, l.v1), new Point(l.v2, l.v3), new Scalar(255), 1, Core.LINE_AA,0);
		}
		
	}
	private void text_findClosestBoxPathBuild(TextBox texto,Vector<Element> boxes,Vector<Relation> caminosValidos) {
		int d1=Integer.MAX_VALUE,d2=Integer.MAX_VALUE;
		int b1=-1,p1=-1;
		Rect area=texto.getArea();
		Point centro=new Point(area.x+area.width/2,area.y+area.height/2);

		//Me quedo con el box mas cercano, en d1 la distancia
		for (int l=0;l<boxes.size();l++){
			Element b=boxes.get(l);

			//Minima distancia que existe entre un punto central y un set de puntos de la caja
			int mD1=minimumDistance(centro,b.getPoints());

			//Si hay un box mas cerca de lo que teniamos hasta ahora
			if ((mD1!=-1)&&(d1>mD1)&&(estaContenido(area,b.getPoints()))){
				d1=mD1;b1=b.getId();
			}
		}


		
		//Me quedo con el path mas cercano
		for (int l=0;l<caminosValidos.size();l++){
			Relation p=caminosValidos.get(l);

			PATHS=Mat.zeros(s,CvType.CV_8UC1);
			drawPath(PATHS,p.getSegments());
			//Core.rectangle(PATHS,area.br(),area.tl(),new Scalar(255));
			//showResult(PATHS,"Paths");
			//EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE
			

			//Minima distancia que existe entre un punto central y un set de puntos de la caja
			int mD1=minimumDistanceS(centro,p.getSegments());
			//cout<<"Distancia computada: "<<to_string(mD1)<<endl;

			//Si hay un box mas cerca de lo que teniamos hasta ahora
			if ((mD1!=-1)&&(d2>mD1)){
				d2=mD1;p1=l;
			}
		}

		//waitKey(0);
		//cout<<"Distancia box "<<to_string(d1)<<"   Path "<<to_string(d2)<<endl;


		if ((b1!=-1)&&(p1!=-1)){
			//Encontro un box y un path

			//Hay un box mas cerca
			if (d1<=d2){
				establecerTextoB(texto,b1,boxes);
				return;
			}
			//Hay un path mas cerca
			else {
				//Cuando el path se extendio un poco adentro de la caja, y el texto esta contenido en alguno de los path que conecta
				/*if ( estaContenido(texto.getArea(),boxes[caminosValidos[p1].getConnection1()].getPoints()) )
					establecerTexto(texto,caminosValidos[p1].getConnection1(),caminosValidos);
				if ( estaContenido(texto.getArea(),boxes[caminosValidos[p1].getConnection2()].getPoints()) )
					establecerTexto(texto,caminosValidos[p1].getConnection2(),caminosValidos);*/

				establecerTextoP(texto,p1,caminosValidos);
				return;
			}

			
		}
		//else
		if (b1==-1){
			//cout<<"PATH DISTANCE!"<<to_string(d2)<<endl;
			
			if (d2<MAXIMUM_DISTANCE+MAXIMUM_DISTANCE)
				establecerTextoP(texto,p1,caminosValidos);
			return;
		}
		if (p1==-1){
			establecerTextoB(texto,b1,boxes);
			return;
		}
		return;
	}
	private boolean estaContenido(Rect area,MatOfPoint bPoints) {
		return Imgproc.boundingRect(bPoints).contains(new Point(area.x+area.width/2,area.y+area.height/2));
	}
	

	private Mat graphBuild,muestra,PATHS;
};
