package imeav.gui;

import imeav.IMEAVDiagramRecognizer;
import imeav.exceptions.InputFileException;
import imeav.exceptions.OutputFileException;

import java.awt.EventQueue;

import javax.swing.JFrame;

import javax.swing.JLabel;
import javax.swing.SpringLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;

public class MainWindow {

	private JFrame frmImeav;
	private JTextField InputImageTextField;
	private JTextField OutPutFileTextField;
	private IMEAVDiagramRecognizer imeav;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
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
		imeav = new IMEAVDiagramRecognizer();
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

		JLabel lblNewLabel = new JLabel("Archivo de salida");
		springLayout.putConstraint(SpringLayout.WEST, lblNewLabel, 40,
				SpringLayout.WEST, frmImeav.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, lblNewLabel, -486,
				SpringLayout.EAST, frmImeav.getContentPane());
		frmImeav.getContentPane().add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("Imagen de entrada");
		springLayout.putConstraint(SpringLayout.SOUTH, lblNewLabel_1, -214,
				SpringLayout.SOUTH, frmImeav.getContentPane());
		springLayout.putConstraint(SpringLayout.NORTH, lblNewLabel, 26,
				SpringLayout.SOUTH, lblNewLabel_1);
		springLayout.putConstraint(SpringLayout.WEST, lblNewLabel_1, 29,
				SpringLayout.WEST, frmImeav.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, lblNewLabel_1, 0,
				SpringLayout.EAST, lblNewLabel);
		frmImeav.getContentPane().add(lblNewLabel_1);

		InputImageTextField = new JTextField();
		springLayout.putConstraint(SpringLayout.WEST, InputImageTextField, 6,
				SpringLayout.EAST, lblNewLabel_1);
		springLayout.putConstraint(SpringLayout.EAST, InputImageTextField,
				-171, SpringLayout.EAST, frmImeav.getContentPane());
		frmImeav.getContentPane().add(InputImageTextField);
		InputImageTextField.setColumns(10);

		OutPutFileTextField = new JTextField();
		springLayout.putConstraint(SpringLayout.WEST, OutPutFileTextField, 6,
				SpringLayout.EAST, lblNewLabel);
		springLayout.putConstraint(SpringLayout.EAST, OutPutFileTextField,
				-171, SpringLayout.EAST, frmImeav.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, InputImageTextField,
				-20, SpringLayout.NORTH, OutPutFileTextField);
		springLayout.putConstraint(SpringLayout.NORTH, OutPutFileTextField,
				110, SpringLayout.NORTH, frmImeav.getContentPane());
		frmImeav.getContentPane().add(OutPutFileTextField);
		OutPutFileTextField.setColumns(10);

		JButton btnInputImageSelection = new JButton("Seleccionar");
		springLayout.putConstraint(SpringLayout.NORTH, btnInputImageSelection,
				-4, SpringLayout.NORTH, lblNewLabel_1);
		springLayout.putConstraint(SpringLayout.WEST, btnInputImageSelection,
				481, SpringLayout.WEST, frmImeav.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, btnInputImageSelection,
				-45, SpringLayout.EAST, frmImeav.getContentPane());
		frmImeav.getContentPane().add(btnInputImageSelection);

		JButton btnOutputFileSelection = new JButton("Seleccionar");
		springLayout.putConstraint(SpringLayout.WEST, btnOutputFileSelection,
				23, SpringLayout.EAST, OutPutFileTextField);
		springLayout.putConstraint(SpringLayout.EAST, btnOutputFileSelection,
				0, SpringLayout.EAST, btnInputImageSelection);
		springLayout.putConstraint(SpringLayout.NORTH, btnOutputFileSelection,
				-4, SpringLayout.NORTH, lblNewLabel);
		frmImeav.getContentPane().add(btnOutputFileSelection);

		JButton btnConvertImage = new JButton("Convertir imagen");
		springLayout.putConstraint(SpringLayout.NORTH, btnConvertImage, 71,
				SpringLayout.SOUTH, btnOutputFileSelection);
		springLayout.putConstraint(SpringLayout.EAST, btnConvertImage, -92,
				SpringLayout.EAST, frmImeav.getContentPane());
		frmImeav.getContentPane().add(btnConvertImage);

		// agrego acciones a botones
		btnInputImageSelection.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				int option = chooser.showOpenDialog(null);
				if (option != JFileChooser.APPROVE_OPTION)
					System.exit(0);
				File f = chooser.getSelectedFile();
				InputImageTextField.setText(f.getAbsolutePath());
			}
		});

		btnOutputFileSelection.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser Wchooser = new JFileChooser();
				Wchooser.setSelectedFile(new File("outputGraph.dot"));
				int Woption = Wchooser.showSaveDialog(null);
				if (Woption != JFileChooser.APPROVE_OPTION)
					System.exit(0);
				File Wf = Wchooser.getSelectedFile();
				OutPutFileTextField.setText(Wf.getAbsolutePath());
			}
		});

		btnConvertImage.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					imeav.convert(InputImageTextField.getText(),
							OutPutFileTextField.getText());
					ResultsWindow resultsWindow = new ResultsWindow(frmImeav,
							imeav);
					resultsWindow.setVisible(true);
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
