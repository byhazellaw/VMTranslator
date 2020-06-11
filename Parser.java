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
	
	private static String aType;
	
	
	private static final int S_LOCAL = 10;
	private static final int S_ARG = 11;
	private static final int S_THIS = 12;
	private static final int S_THAT = 13;
	private static final int S_CONT = 14;
	private static final int S_TMP = 15;
	
	private static String seg;
	private static int type;
	private static int value;
	
	public Parser(String line) {
		
		this.line = line;
		
		//initialize variables
		seg = null;
		type = -1;
		value = -1;
		aType= null;
		
		
		commandParser(line);
		
		
		//non arithmetic commands have more than one word
		if (type != C_ARITHMETIC) {
			
			segmentParser(line);
			valueParser(line);
		
		} 
		
	}
	
	// return commandType
		private int commandParser(String line) {

			//command has more than one arguments
			if (line.indexOf(" ") != -1) {

				String code = line.split(" ")[0];

				if (code != null) {

					if (code.equals("push")) {

						type = C_PUSH;

					} else {

						type = C_POP;
					}

				}

			} else {

				type = C_ARITHMETIC;
				
				aType = line;
			}
			return type;
		}

		// return segments
		private String segmentParser(String line) {
			
			
			if (line.indexOf(" ")!= -1) {
				
				String code = line.split(" ")[1];
				
				if (code!=null) {
					
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
					
					default:
						seg = null;
					}
					 
					
				}
			}
			return seg;
			
		}
		
		
		private int valueParser(String line) {
			
			if (line.split(" ")[2]!=null) {
				
				value= Integer.parseInt(line.split(" ")[2]);
				
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
		
	
}
