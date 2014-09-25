package imeav.textrecognition;

import java.util.Vector;

import org.opencv.core.*;

import imeav.utilities.TextBox;

public interface ITextRecognizer {
	/**
	 * Dada una imagen original, y areas donde se detecto texto, se busca en
	 * esas areas de texto y se digitaliza el texto usando un OCR
	 * 
	 * @param original
	 *            Imagen original completa
	 * @param areas
	 *            Imagen que contiene las areas en donde se busca el texto
	 * 
	 * @return TextBoxsVector Vector de todos los textos encontrados en el
	 *         archivo original
	 */
	public Vector<TextBox> getText(Mat original, Mat areas);
};
