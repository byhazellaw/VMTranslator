import java.util.regex.Pattern;

import org.hamcrest.Matcher;

/*
 * Handles the parsing of a single .vm file
 * Reads a VM command, parses the command into its lexical components
 * and provides convenient access to these components
 * Ignores all white space and comments
 */

public class Parser {

	private String line;
	private static final int C_ARITHMETIC = 0;
	private static final int C_PUSH = 1;
	private static final int C_POP = 2;
	private static final int C_LABEL = 3;
	private static final int C_GOTO = 4;
	private static final int C_IF = 5;
	private static final int C_FUNCTION = 6;
	private static final int C_RETURN = 7;
	private static final int C_CALL = 8;
	private String label;
	
	private static String aType;
	private static String functionName;
	private static String seg;
	private static int type;
	private static int value;

	
	public Parser(String line) {

		this.line = line;

		// initialize variables
		seg = null;
		type = -1;
		value = -1;
		aType = null;
		label = null;
		functionName = null;
		

		commandParser(line);

		// non arithmetic commands have more than one word
		if (type != C_ARITHMETIC) {

			segmentParser(line);
			valueParser(line);

		}

	}

	// return commandType
	private int commandParser(String line) {

		// command has more than one arguments
		if (line.indexOf(" ") != -1) {

			String code = line.split(" ")[0];

			int space = line.indexOf(" ");

			if (code != null) {

				switch (code) {

				case "push":
					type = C_PUSH;
					break;

				case "pop":
					
					type = C_POP;
					break;
				case "label":
					
					type = C_LABEL;
					label = line.substring(space + 1);
					break;
				case "if-goto":
					
					type = C_IF;
					label = line.substring(space + 1);
					break;
				case "goto":
					
					type = C_GOTO;
					label = line.substring(space + 1);
					break;
					
				case "function":
					
					type = C_FUNCTION;
					functionName = line.split(" ")[1];
					
					break;
	
				case "call":
					type = C_CALL;
					break;
				default:
					type = -1;
				}

			}

		} else {

			if (line.equals("return")) {
				
		
				type = C_RETURN;
				
			} else {
				
				type = C_ARITHMETIC;

				aType = line;
			}
			
			
		}

		return type;
	}

	// return segments
	private String segmentParser(String line) {

		if (line.indexOf(" ") != -1) {

			String code = line.split(" ")[1];

			if (code != null) {

				switch (code) {

				case "local":
					seg = "LCL";
					break;

				case "argument":
					seg = "ARG";
					break;

				case "this":
					seg = "THIS";
					break;

				case "that":
					seg = "THAT";
					break;
				case "temp":
					seg = "temp";
					break;
				case "pointer":
					seg = "pointer";
					break;
				case "static":
					seg = "static";
					break;

				default:
					seg = null;
				}

			}
		}
		return seg;

	}

	private int valueParser(String line) {

		String l = cleanLines(line);

		Pattern p = Pattern.compile("-?\\d+");
		java.util.regex.Matcher m = p.matcher(l);

		while (m.find()) {

			value = Integer.parseInt(m.group());

		}

		return value;

	}

	public String getSegment() {

		return seg;
	}

	public int getValue() {

		return value;
	}

	public int getType() {

		return type;
	}

	public String getaType() {

		return aType;
	}

	public String getLabel() {

		return label;

	}
	
	public String getFunctionName() {
		
		return functionName;
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
