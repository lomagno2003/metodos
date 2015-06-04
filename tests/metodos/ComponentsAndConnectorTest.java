package metodos;

import imeav.IMEAVDiagramRecognizer;
import imeav.exceptions.InputFileException;
import imeav.exceptions.OutputFileException;

import java.io.IOException;
import java.text.MessageFormat;

import org.junit.Test;

public class ComponentsAndConnectorTest {
	private static final String filePathFormat = "/home/clomagno/Projects/Metodos/workspace/metodos/tests/metodos/{0}";
	
	@Test
	public void testCandC() throws InputFileException, OutputFileException, IOException{
		String inputFilePath = MessageFormat.format(filePathFormat, "CandCDiagram.jpg");
		String outputFilePath = MessageFormat.format(filePathFormat, "CandCDiagramOut.dot");
		
		IMEAVDiagramRecognizer recognizer = new IMEAVDiagramRecognizer();

		recognizer.convert(inputFilePath, outputFilePath);
	}
	
	@Test
	public void testModules() throws InputFileException, OutputFileException, IOException{
		String inputFilePath = MessageFormat.format(filePathFormat, "ModulesDiagram.jpg");
		String outputFilePath = MessageFormat.format(filePathFormat, "ModulesDiagramOut.dot");
		
		IMEAVDiagramRecognizer recognizer = new IMEAVDiagramRecognizer();

		recognizer.convert(inputFilePath, outputFilePath);
	}
}
