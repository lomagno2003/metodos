package imeav.gui;

import imeav.IMEAVDiagramRecognizer;
import imeav.exceptions.InputFileException;
import imeav.exceptions.OutputFileException;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SpringLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.JList;
import javax.swing.border.LineBorder;

import java.awt.Color;

import javax.swing.ListSelectionModel;

public class MainWindow {

	private JFrame frmImeav;
	private JTextField OutPutFileTextField;
	private Vector<IMEAVDiagramRecognizer> imeavs;
	private JList<String> listInputImages;
	private JScrollPane scrollPane;
	private JComboBox<String> comboBox;
	private Vector<String> imagePaths;
	private ResultsWindow resultsWindow;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
					MainWindow window = new MainWindow();
					window.frmImeav.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		imeavs = new Vector<IMEAVDiagramRecognizer>();
		imagePaths = new Vector<String>();
		resultsWindow = new ResultsWindow(this.frmImeav, null);
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		// creacion de componentes
		frmImeav = new JFrame();
		frmImeav.setResizable(false);
		frmImeav.setTitle("IMEAV ");
		frmImeav.setBounds(100, 100, 635, 329);
		frmImeav.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SpringLayout springLayout = new SpringLayout();
		frmImeav.getContentPane().setLayout(springLayout);

		JLabel lblNewLabel = new JLabel("Directorio de salida");
		frmImeav.getContentPane().add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("Im\u00E1genes de entrada");
		springLayout.putConstraint(SpringLayout.NORTH, lblNewLabel_1, -118, SpringLayout.NORTH, lblNewLabel);
		springLayout.putConstraint(SpringLayout.WEST, lblNewLabel_1, 29, SpringLayout.WEST, frmImeav.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, lblNewLabel_1, -104, SpringLayout.NORTH, lblNewLabel);
		springLayout.putConstraint(SpringLayout.EAST, lblNewLabel_1, -455, SpringLayout.EAST, frmImeav.getContentPane());
		frmImeav.getContentPane().add(lblNewLabel_1);

		OutPutFileTextField = new JTextField();
		springLayout.putConstraint(SpringLayout.EAST, OutPutFileTextField, -10, SpringLayout.EAST, frmImeav.getContentPane());
		springLayout.putConstraint(SpringLayout.NORTH, lblNewLabel, 3, SpringLayout.NORTH, OutPutFileTextField);
		frmImeav.getContentPane().add(OutPutFileTextField);
		OutPutFileTextField.setColumns(10);

		JButton btnInputImageSelection = new JButton("Seleccionar");
		springLayout.putConstraint(SpringLayout.NORTH, btnInputImageSelection, 29, SpringLayout.NORTH, frmImeav.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, btnInputImageSelection, 6, SpringLayout.EAST, lblNewLabel_1);
		frmImeav.getContentPane().add(btnInputImageSelection);

		JButton btnOutputFileSelection = new JButton("Seleccionar");
		springLayout.putConstraint(SpringLayout.WEST, OutPutFileTextField, 27, SpringLayout.EAST, btnOutputFileSelection);
		springLayout.putConstraint(SpringLayout.EAST, lblNewLabel, -43, SpringLayout.WEST, btnOutputFileSelection);
		springLayout.putConstraint(SpringLayout.NORTH, OutPutFileTextField, 1, SpringLayout.NORTH, btnOutputFileSelection);
		springLayout.putConstraint(SpringLayout.NORTH, btnOutputFileSelection, 95, SpringLayout.SOUTH, btnInputImageSelection);
		springLayout.putConstraint(SpringLayout.WEST, btnOutputFileSelection, 0, SpringLayout.WEST, btnInputImageSelection);
		springLayout.putConstraint(SpringLayout.EAST, btnOutputFileSelection, 103, SpringLayout.WEST, btnInputImageSelection);
		frmImeav.getContentPane().add(btnOutputFileSelection);

		JButton btnConvertImage = new JButton("Convertir imágenes");
		springLayout.putConstraint(SpringLayout.SOUTH, btnConvertImage, -25, SpringLayout.SOUTH, frmImeav.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, btnConvertImage, -209, SpringLayout.EAST, frmImeav.getContentPane());
		frmImeav.getContentPane().add(btnConvertImage);
		
		listInputImages = new JList<String>();
		listInputImages.setEnabled(false);
		listInputImages.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		springLayout.putConstraint(SpringLayout.NORTH, listInputImages, 0, SpringLayout.NORTH, frmImeav.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, listInputImages, 28, SpringLayout.EAST, btnInputImageSelection);
		springLayout.putConstraint(SpringLayout.EAST, listInputImages, -173, SpringLayout.EAST, frmImeav.getContentPane());
		listInputImages.setVisibleRowCount(5);
		listInputImages.setBorder(new LineBorder(Color.LIGHT_GRAY));
		
		scrollPane = new JScrollPane(listInputImages);
		scrollPane.setEnabled(false);
		springLayout.putConstraint(SpringLayout.EAST, btnInputImageSelection, -27, SpringLayout.WEST, scrollPane);
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, -17, SpringLayout.NORTH, OutPutFileTextField);
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, frmImeav.getContentPane());
		springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 30, SpringLayout.NORTH, frmImeav.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 310, SpringLayout.WEST, frmImeav.getContentPane());
		frmImeav.getContentPane().add(scrollPane);
		
		
		comboBox = new JComboBox<String>();
		comboBox.setEnabled(false);
		springLayout.putConstraint(SpringLayout.NORTH, comboBox, 1, SpringLayout.NORTH, btnConvertImage);
		springLayout.putConstraint(SpringLayout.WEST, comboBox, 37, SpringLayout.EAST, btnConvertImage);
		frmImeav.getContentPane().add(comboBox, BorderLayout.CENTER);
		comboBox.addItem("Ver resultados");
		comboBox.addActionListener(new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	        	if (comboBox.getSelectedIndex()!= 0){
	        	//	if (imeavs.elementAt(comboBox.getSelectedIndex()-1).getOriginalColor() != null){
		        		resultsWindow.setVisible(true);
		        		resultsWindow.setImeav(imeavs.elementAt(comboBox.getSelectedIndex()-1));
	        		}
	        	//}
	        }
	    });
		// agrego acciones a botones
		btnInputImageSelection.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setMultiSelectionEnabled(true);
				int option = chooser.showOpenDialog(null);
				if (option == JFileChooser.APPROVE_OPTION){			
					File[] images = chooser.getSelectedFiles();
					DefaultListModel<String> model = new DefaultListModel<String>();
					for(int i = 0; i< images.length; i++){
						model.addElement(images[i].getName());
						imagePaths.add(images[i].getAbsolutePath());
						comboBox.addItem(images[i].getName());
					}					
					listInputImages.setModel(model);
				}
			}
		});
		
		btnOutputFileSelection.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser Wchooser = new JFileChooser();
				//Wchooser.setSelectedFile(new File("outputGraph.dot"));
				Wchooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int Woption = Wchooser.showSaveDialog(null);
				if (Woption == JFileChooser.APPROVE_OPTION){
					File Wf = Wchooser.getSelectedFile();
					OutPutFileTextField.setText(Wf.getAbsolutePath());
				}
			}
		});

		btnConvertImage.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					for (int i=0; i< imagePaths.size(); i++){
						IMEAVDiagramRecognizer currentImeav = new IMEAVDiagramRecognizer();
						imeavs.add(currentImeav);
						currentImeav.convert(imagePaths.elementAt(i),
							OutPutFileTextField.getText()+"\\Output"+i+".dot");						
					}
					if (imagePaths.size() > 1){
						comboBox.setEnabled(true);
					}
				} catch (InputFileException e) {
					JOptionPane.showMessageDialog(frmImeav,
							"Imposible continuar",
							"Error al cargar archivo de entrada",
							JOptionPane.ERROR_MESSAGE);
				} catch (OutputFileException e) {
					JOptionPane.showMessageDialog(frmImeav,
							"Imposible continuar",
							"Error al cargar archivo de salida",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

	}
}
