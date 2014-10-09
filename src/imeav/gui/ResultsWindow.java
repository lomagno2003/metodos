package imeav.gui;

import imeav.IMEAVDiagramRecognizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;
import javax.swing.BoxLayout;
import java.awt.Dimension;

public class ResultsWindow extends JDialog implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private IMEAVDiagramRecognizer imeav;
	private JLabel lblResult;
	private ImageIcon imageIconTemp;
	
	private void showResult(Mat img, String titulo) {
		//Imgproc.resize(img, img, new Size(640, 480));
		this.setTitle("Resultados - "+titulo);
		MatOfByte matOfByte = new MatOfByte();
		Highgui.imencode(".jpg", img, matOfByte);
		byte[] byteArray = matOfByte.toArray();
		BufferedImage bufImage = null;
		try {
			InputStream in = new ByteArrayInputStream(byteArray);
			bufImage = ImageIO.read(in);
			imageIconTemp = new ImageIcon(bufImage);
			lblResult.setIcon(new ImageIcon(imageIconTemp.getImage().getScaledInstance(lblResult.getWidth(), lblResult.getHeight(), Image.SCALE_FAST)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Create the dialog.
	 */
	public ResultsWindow(JFrame parent, IMEAVDiagramRecognizer imeav) {
		super(parent);
		this.imeav = imeav;
		initialize();	
	}
	
	private void initialize() {
		setTitle("Resultados");
		setBounds(100, 100, 921, 540);
		getContentPane().setLayout(new BorderLayout());
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setBorder(new LineBorder(new Color(0, 0, 0)));
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("Cerrar");
				okButton.setActionCommand("Cerrar");
				okButton.addActionListener(this); 
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
		
		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		getContentPane().add(panel, BorderLayout.WEST);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		Component verticalStrut_3_1 = Box.createVerticalStrut(20);
		panel.add(verticalStrut_3_1);
		JButton btnNewButton_1 = new JButton("Original");
		btnNewButton_1.setMaximumSize(new Dimension(100, 23));
		btnNewButton_1.setMinimumSize(new Dimension(100, 23));
		panel.add(btnNewButton_1);
		
		Component verticalStrut_1 = Box.createVerticalStrut(20);
		panel.add(verticalStrut_1);
		JButton btnNewButton_2 = new JButton("Estructura");
		btnNewButton_2.setMaximumSize(new Dimension(100, 23));
		panel.add(btnNewButton_2);
		
		Component verticalStrut = Box.createVerticalStrut(20);
		panel.add(verticalStrut);
		
		JButton btnNewButton_3 = new JButton("Texto");
		btnNewButton_3.setMaximumSize(new Dimension(100, 23));
		panel.add(btnNewButton_3);
		
		Component verticalStrut_2 = Box.createVerticalStrut(20);
		panel.add(verticalStrut_2);
		
		JButton btnNewButton_4 = new JButton("Binaria");
		btnNewButton_4.setMaximumSize(new Dimension(100, 23));
		panel.add(btnNewButton_4);
		{
			
			Component verticalStrut_3 = Box.createVerticalStrut(20);
			panel.add(verticalStrut_3);
		}			
		
		JButton btnNewButton_5 = new JButton("M\u00F3dulos");
		btnNewButton_5.setMaximumSize(new Dimension(100, 23));
		panel.add(btnNewButton_5);

		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showResult(imeav.getTextoBorrado(), "Estructura");
			}
		});
		btnNewButton_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showResult(imeav.getAreasTexto(), "Texto");					
			}
		});
		btnNewButton_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showResult(imeav.getBinaria(), "Binaria");
			}
		});
		btnNewButton_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showResult(imeav.getRefinedBoxes(), "Módulos");
			}
		});
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showResult(imeav.getOriginalColor(), "Original");
			}
		});
		
		lblResult = new JLabel("");
		getContentPane().add(lblResult, BorderLayout.CENTER);
	}
	@Override
	public void actionPerformed(ActionEvent ae) {
		String action = ae.getActionCommand();
		if (action.equals("Cerrar")) {
			this.setVisible(false);
		}		
	}
	@Override
	public void repaint(long arg0, int arg1, int arg2, int arg3, int arg4) {
		// TODO Auto-generated method stub
		super.repaint(arg0, arg1, arg2, arg3, arg4);
		lblResult.setIcon(new ImageIcon(imageIconTemp.getImage().getScaledInstance(lblResult.getWidth(), lblResult.getHeight(), Image.SCALE_FAST)));
	}
	
	

}
