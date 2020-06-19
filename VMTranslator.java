import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Scanner;

public class VMTranslator {

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {

//		if (args.length > 0) {

			//TODO scan directory for .vm files
			//translate all the .vm files into one .asm file
		
			String inFile = "SimpleFunction.vm";
			int dot = inFile.indexOf('.');

			String fileName = inFile.substring(0, dot);
			String outFileName = inFile.substring(0, dot) + ".asm";

			Scanner scanner = new Scanner(new File(inFile));

			PrintWriter writer = new PrintWriter(outFileName, "UTF-8");

			int labelCount = 0;

			while (scanner.hasNextLine()) {

				String line = scanner.nextLine();

				// ignore empty and documentation
				if (line.indexOf("/") == 0 || line.isEmpty()) {
					continue;
				}
				
				String cleaned = cleanLines(line);

				
				
				Parser p = new Parser(cleaned);

				int type = p.getType();
				int value = p.getValue();
				String seg = p.getSegment();
				String aType = p.getaType();
				String label = p.getLabel();
				String functionName = p.getFunctionName();

				CodeTranslate c = new CodeTranslate(value, type, seg, aType, labelCount, fileName, label, functionName);

				
				
				ArrayList<String> code = c.getCode();

				if (code.size() != 0) {

					for (int i = 0; i < code.size(); i++) {

						String aCode = code.get(i);

						writer.println(aCode);

						
					}
				}

				labelCount++;

			}

			scanner.close();
			writer.close();
//		}

	}
	
	// String processor
	protected static String cleanLines(String line) {

		String justCode;

		if (line.contains("/")) { // ignore comments

			justCode = line.split("/")[0];

		} else {

			justCode = line;
		}

		
		return justCode;
	}

}
