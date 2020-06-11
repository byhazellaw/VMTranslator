import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Scanner;

public class VMTranslator {

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		
		
		String inFile = "StackTest.vm";
		int dot = inFile.indexOf('.');
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
			
			Parser p = new Parser(line);
			
			int type = p.getType();
			int value = p.getValue();
			String seg = p.getSegment();
			String aType = p.getaType();
			
			CodeTranslate c = new CodeTranslate(value, type, seg, aType, labelCount);
			
			ArrayList<String> code = c.getCode();
			
			if (code.size()!=0) {
				
				
				for (int i=0; i<code.size(); i++) {
					
					String aCode = code.get(i);
					
					writer.println(aCode);
					
				}
			}
		
			labelCount++;
			
		}
		
		
		scanner.close();
		writer.close();
	}
	
	
	
	
	

	
}
