package imeav.extractor;

import imeav.utilities.Relation;
import imeav.utilities.Vec4i;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.opencv.core.Point;

public class Path {
	
	private int endpointMatrixCols,endpointMatrixRows;
	private int tolerance;
	private Vector<Vec4i> segmentList;
	private List< Vector<Integer> > endpointMatrix;
	private Set< Integer > usados;
	
	public Path(Set<Integer> _usados){
		tolerance = 10;
	    usados = _usados;
	}
	
	public Relation getPath(int index,Vector<Vec4i> _segmentList){

	    Vector<Vec4i> caminoUnido = new Vector<Vec4i>();
	    segmentList = _segmentList;
	    Relation pathIzq=
	    		avanzar(index,new Point(segmentList.get(index).v0,segmentList.get(index).v1));
	    Relation pathDer=
	    		avanzar(index,new Point(segmentList.get(index).v2,segmentList.get(index).v3));

	    Vector<Vec4i> izq= pathIzq.getSegments();
	    Vector<Vec4i> der= pathDer.getSegments();

	    //sandwich
	    for (int j=0;j<izq.size();j++)
	        caminoUnido.add(izq.get(j));
	    caminoUnido.add(segmentList.get(index));
	    for (int j=der.size()-1;j>=0;j--)
	        caminoUnido.add(der.get(j));

	    Relation salida = new Relation();
	    salida.setSegments(caminoUnido);
	    salida.setExtreme1(pathIzq.getExtreme2());
	    salida.setExtreme2(pathDer.getExtreme2());

	    return salida;
	}
	
	private void CurrentExtreme(Vec4i segmentoActual,Point otroExtremoActual,Point ptoSalidaActual,Point otroExtremoActual_t){
        /*
		*calculo otroExtremoActual, y el trasladado
		*/
        if (segmentoActual.v0==ptoSalidaActual.x && segmentoActual.v1==ptoSalidaActual.y){
            otroExtremoActual=new Point(segmentoActual.v2,segmentoActual.v3);
            otroExtremoActual_t=new Point(segmentoActual.v2-ptoSalidaActual.x,segmentoActual.v3-ptoSalidaActual.y);
        } else {
            otroExtremoActual=new Point(segmentoActual.v0,segmentoActual.v1);
            otroExtremoActual_t=new Point(segmentoActual.v0-ptoSalidaActual.x,segmentoActual.v1-ptoSalidaActual.y);
        }
	}
	
	private void findOtherExtreme(Vector<Integer> vecinos,Point ptoSalidaActual,Point otroExtremoActual_t
									,Point extremoMasAlejado,Vec4i segmentoMasAlejado){
		/*
		 * Busca dado un punto el otro extremo.
		 */
		double mayorDist2 = 0;
        for (int i=0;i<vecinos.size();i++){
            //agrego al vecino a la lista de usados,esto incluye tambi�n al segmento actual ya
            //que es vecino de s� mismo
            usados.add(vecinos.get(i));

            Vec4i v = segmentList.get(vecinos.get(i)); //vecino actual

            //el otro extremo del segmento de entrada
            Point otroExtremoVecino;
            Point otroExtremoVecino_t;

            //busco el otro extremo del punto vecino, y el trasladado
            if (((v.v0-ptoSalidaActual.x)*(v.v0-ptoSalidaActual.x) + (v.v1-ptoSalidaActual.y)*(v.v1-ptoSalidaActual.y))<
                ((v.v2-ptoSalidaActual.x)*(v.v2-ptoSalidaActual.x) + (v.v3-ptoSalidaActual.y)*(v.v3-ptoSalidaActual.y))){
                otroExtremoVecino=new Point(v.v2,v.v3);
                otroExtremoVecino_t=new Point(v.v2-v.v0,v.v3-v.v1);
            } else {
                otroExtremoVecino=new Point(v.v0,v.v1);
                otroExtremoVecino_t=new Point(v.v0-v.v2,v.v1-v.v3);
            }

            double coseno = otroExtremoActual_t.dot(otroExtremoVecino_t)/(Math.sqrt(otroExtremoActual_t.dot(otroExtremoActual_t))*Math.sqrt(otroExtremoVecino_t.dot(otroExtremoVecino_t)));

            //�nicamente si el �ngulo demuestra que este
            //vecino es una continuaci�n probable...
            if (coseno<0.4){
                //veo si es m�s alejado que el m�s alejado hasta ahora, y de ser
                //as� lo reemplazo
                double distancia2 = Math.pow(ptoSalidaActual.x-otroExtremoVecino.x,2)+
                                   Math.pow(ptoSalidaActual.y-otroExtremoVecino.y,2);

                if (distancia2>mayorDist2){
                    mayorDist2=distancia2;
                    extremoMasAlejado=otroExtremoVecino;
                    segmentoMasAlejado=v;
                }
            }
        }
	}
	
	private Relation avanzar(int indiceSegmento, Point salida){
	/*
	 * devuelve un path que contiene el camino recorrido y como "extreme2" el punto m�s alejado al que llega,
	 * como "extreme1" el ptoinicial del recorrido
	 */
	    Vector<Vec4i> salida1=new Vector<Vec4i>();
	    //el segmento actual, incialmente el que viene como par�metro
	    Vec4i segmentoActual = segmentList.get(indiceSegmento);
	    //es el otro extremo del segmento actual
	    Point otroExtremoActual= new Point();
	    //Point2f otroExtremoActual;
	    //punto de salida, incialmente el que viene como par�metro
	    Point ptoSalidaActual=salida;
	    Point otroExtremoActual_t;
	    int nit=0;
	    while (nit<5){
	        nit++;

	        otroExtremoActual_t=null;
	        //Se calcula el extremo actual y ademas de cuanto es el traslado
	        CurrentExtreme(segmentoActual,otroExtremoActual,ptoSalidaActual,otroExtremoActual_t);

	        //busco los vecinos
	        Vector<Integer> vecinos = getNeighbors((int)ptoSalidaActual.x,(int)ptoSalidaActual.y,tolerance);

	        //el extremo m�s alejado, inicialmente es el actual (no avanc� todav�a)
	        Point extremoMasAlejado=otroExtremoActual;
	        Vec4i segmentoMasAlejado=segmentoActual;
	        
	        findOtherExtreme(vecinos, ptoSalidaActual, otroExtremoActual_t, extremoMasAlejado, segmentoMasAlejado);
	        //si no encontr� un segmento por donde avanzar, termina
	        if (segmentoMasAlejado==segmentoActual)
	            break;

	        //ya que encontr� un segmento actualiza los datos
	        //y agrega al vecino a la lista
	        segmentoActual=segmentoMasAlejado;
	        ptoSalidaActual=extremoMasAlejado;
	        salida1.add(segmentoMasAlejado);
	    }
	    Relation pathSalida = new Relation();
	    pathSalida.setSegments(salida1);
	    pathSalida.setExtreme1(salida);
	    pathSalida.setExtreme2(otroExtremoActual);

	    return pathSalida;
		
	}
	
	private Vector< Integer > getNeighbors(int x, int y, int k ){
		

	    //busco los l�mites
	    int xmin=x-k/2;
	    int xmax=x+k/2;
	    int ymin= y-k/2;
	    int ymax=y+k/2;
	    if (xmin<0) xmin=0;
	    if (xmax>=endpointMatrixCols) xmax=endpointMatrixCols-1;
	    if (ymin<0) ymin=0;
	    if (ymax>=endpointMatrixRows) ymax=endpointMatrixRows-1;

	    Vector<Integer> salida = new Vector<Integer>();

	    for (int i=ymin;i<=ymax;i++)
	        for (int j=xmin;j<xmax;j++)
	        	if (!(endpointMatrix.get(i*endpointMatrixCols + j).isEmpty())){
	                Vector<Integer> subVector = endpointMatrix.get(i*endpointMatrixCols + j);

	                
	                for (int w=0;w<subVector.size();w++)
	                    salida.add(subVector.get(new Integer(w)));
	            }


	    return salida;
	}
	
}
