import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Scanner;

public class VMTranslator {

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {

		if (args.length > 0) {

			
			String input = args[0];

			File file = new File(input);

			if (file.isFile()) {

				int dot = input.indexOf('.');

				String fileName = input.substring(0, dot);

				String outFileName = input.substring(0, dot) + ".asm";

				Scanner scanner = new Scanner(new File(input));

				PrintWriter writer = new PrintWriter(outFileName, "UTF-8");

				int labelCount = 0;
				
				
				String currentFunction = "";
				boolean isFunctionLabel = false;
				
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

					if (p.getFunctionName() != null) {

						currentFunction = p.getFunctionName();
						isFunctionLabel = true;
						
					}

					CodeTranslate c = new CodeTranslate(value, type, seg, aType, labelCount, fileName, label,
							currentFunction, isFunctionLabel);

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

			} else if (file.isDirectory()) {

				File dir = new File(input);

				String outFileName = input + ".asm";

				int labelCount = 0;
				

				PrintWriter writer = new PrintWriter(outFileName, "UTF-8");

				
				
				
				// SP=256
				// call Sys.init 0
				ArrayList<String> code0 = CodeTranslate.bootStrap();

				for (String str : code0) {

					writer.println(str);
				}

				for (File f : dir.listFiles()) {

					if (f.isFile() && f.getName().endsWith(".vm")) {

						
						int d = f.getName().indexOf('.');

						String fileName = f.getName().substring(0, d);

						String currentFunction = "";
						boolean isFunctionLabel = false;
						
						
						Scanner scanner = new Scanner(new File(dir + "/" + f.getName()));

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

							if (p.getFunctionName() != null) {

								currentFunction = p.getFunctionName();
								isFunctionLabel = true;
								
							}

							CodeTranslate c = new CodeTranslate(value, type, seg, aType, labelCount, fileName, label,
									currentFunction, isFunctionLabel);

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

					}

				}
				writer.close();
			}

		}

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
