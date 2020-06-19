import java.util.ArrayList;

/*
 * Generates assembly code from parsed VM command
 */

public class CodeTranslate {

	private String seg;
	private String fileName;
	private int labelCount;
	private String functionName;
	private ArrayList<String> code;

	public CodeTranslate(int value, int type, String seg, String aType, int labelCount, String fileName, String label, String functionName) {

		this.seg = seg;
		this.labelCount = labelCount;
		this.fileName = fileName;
		this.functionName = functionName;
		code = new ArrayList<>();

		// push
		if (type == 1) {

			if (seg != null) {

				if (seg.equals("LCL") || seg.equals("ARG") || seg.equals("THIS") || seg.equals("THAT")) {

					push(seg, value);

				} else if (seg.equals("temp")) {

					pushTemp(value);

				} else if (seg.equals("pointer")) {

					pushPointer(value);

				} else if (seg.equals("static")) {

					pushStatic(value);
				}

			} else {

				pushConstant(value);

			}

		} else if (type == 2 && seg != null) {

			if (seg.equals("LCL") || seg.equals("ARG") || seg.equals("THIS") || seg.equals("THAT")) {

				pop(seg, value);

			} else if (seg.equals("temp")) {

				popTemp(value);
				
			} else if (seg.equals("pointer")) {

				popPointer(value);
				
			} else if (seg.equals("static")) {

				popStatic(value);
			}

		} else if (type == 3) {

			label(label);

		} else if (type == 4) {

			writeGoToLabel(label);

		} else if (type == 5) {

			writeIfLabel(label);

		} else if (type == 6) {

			writeFunction(value);

		} else if (type == 7) {


			writeReturn(value);
			
		} else if(type==8) {
			
			writeCall(value);
			
		} else if (type == 0 && !aType.equals(null)) {

			switch (aType) {
			case "add":
				add();
				break;
			case "eq":
				eq();
				break;
			case "sub":
				sub();
				break;
			case "gt":
				gt();
				break;
			case "lt":
				lt();
				break;
			case "neg":
				neg();
				break;
			case "and":
				and();
				break;
			case "or":
				or();
				break;
			case "not":
				not();
				break;
			default:
				//nothing happens
				break;
			}

		} else {
			
			//nothing happens if the command is not one of the above types
		}

	}
	
	
	// calling a function after value arguments have been pushed
	private void writeCall(int value) {
		
		String reAdd = "@RETURN_" + labelCount;
	
		//declare a label for return address - current SP after arguments	
		code.add("@SP"); 
		code.add("A=M");
		code.add("D=A");
		//SP - nArgs
		for (int i=0; i<value; i++) {
			
			code.add("D=D-1");
		}
		code.add(reAdd);
		code.add("M=D");

		
		//push return address
		code.add(reAdd);
		code.add("D=M");
		code.add("@SP");
		code.add("A=M");
		code.add("M=D");
		updateSP();
		
		
		//push LCL
		code.add("@LCL");
		code.add("D=M");
		code.add("@SP");
		code.add("A=M");
		code.add("M=D");
		updateSP();
		
		
		//push ARG
		code.add("@ARG");
		code.add("D=M");
		code.add("@SP");
		code.add("A=M");
		code.add("M=D");
		updateSP();
		
		
		//push THIS
		code.add("@THIS");
		code.add("D=M");
		code.add("@SP");
		code.add("A=M");
		code.add("M=D");
		updateSP();
		
		
		//push THAT
		code.add("@THAT");
		code.add("D=M");
		code.add("@SP");
		code.add("A=M");
		code.add("M=D");
		updateSP();
		
		//ARG = SP - value - 5
		code.add("@SP");
		code.add("D=M");
		
		for(int i=0; i<value+5; i++) {
			
			code.add("D=D-1");
		}
		code.add("@ARG");
		code.add("M=D");
		
		
		//LCL = SP
		code.add("@SP");
		code.add("D=M");
		
		code.add("@LCL");
		code.add("M=D");
		
		//goto function
		code.add("@"+functionName);
		

		
		
	}


	//sys.init to be placed at the beginning of the asm
	private void writeInit() {
		
		//TODO finish writeInit
		//SP=256 - initialize stack pointed to 0x0100
		code.add("@256");
		code.add("D=A");
		code.add("@SP");
		code.add("M=D");

		//call Sys.init
		
		
	}
	
	private void writeReturn(int value) {

		code.add("//return");

		String f = "@FRAME" + Integer.toString(labelCount);

		// FRAME=LCL
		code.add("@LCL");
		code.add("A=M");
		code.add("D=A");

		code.add(f);
		code.add("M=D");
		
		// RET=*(FRAME-5)

		// *ARG = pop()
		currentSpMValue();
		code.add("@ARG");
		code.add("A=M");
		code.add("M=D");

		// SP=ARG+1
		code.add("@ARG");
		code.add("M=M+1");
		code.add("D=M");
		code.add("@SP");
		code.add("M=D");

		
		// THAT=*(FRAME-1)
		code.add(f);
		code.add("A=M-1");
		code.add("A=M");
		code.add("D=A");
		code.add("@THAT");
		code.add("M=D");

		
		
		// THIS=*(FRAME-2)
		code.add(f);
		code.add("M=M-1");
		code.add("A=M-1");
		code.add("D=M");
		code.add("@THIS");
		code.add("M=D");
		
		
		
		// ARG=*(FRAME-3)
		code.add(f);
		code.add("M=M-1");
		code.add("A=M-1");
		code.add("D=M");
		code.add("@ARG");
		code.add("M=D");
		
		
		
		// LCL=*(FRAME-4)
		code.add(f);
		code.add("M=M-1");
		code.add("A=M-1");
		code.add("D=M");
		code.add("@LCL");
		code.add("M=D");
		
		
		
		// goto RET

		return;

	}

	private void writeFunction(int value) {

		// declare label for function entry
		code.add("@"+functionName);

		// repeat value times PUSH 0
		for (int i = 0; i < value; i++) {

			code.add("@0");
			code.add("D=A");

			// *SP=D
			code.add("@SP");
			code.add("A=M");
			code.add("M=D");

			updateSP();

		}

		return;

	}

	private void writeIfLabel(String label) {

		currentSpMValue();

		code.add("@" + label);

		code.add("D;JGT");

		return;

	}

	private void writeGoToLabel(String label) {

		if (label != null) {

			code.add("@" + label);
			code.add("0;JMP");

		}
		return;
	}

	private void label(String label) {

		if (label != null) {

			code.add("(" + label + ")");

		}

		return;

	}

	private void popStatic(int value) {

		code.add("//popStatic");
		currentSpMValue();

		code.add("@" + fileName + "." + value);

		if (value == 0) {

			code.add("A=M");

		} else {

			for (int i = 0; i < value; i++) {

				code.add("A=A+1");
			}

		}

		code.add("M=D");

		// reset base address
		if (value != 0) {

			code.add("@" + seg);

			for (int i = 0; i < value; i++) {
				code.add("A=A-1");

			}
		}

		return;
	}

	private void pushStatic(int value) {

		code.add("@" + fileName + "." + value);

		if (value == 0) {

			code.add("A=M");

		} else {

			for (int i = 0; i < value; i++) {

				code.add("A=A+1");
			}

		}

		code.add("D=M");

		code.add("@SP");
		code.add("A=M");
		code.add("M=D");

		updateSP();

		// reset base address
		if (value != 0) {

			code.add("@" + seg);

			for (int i = 0; i < value; i++) {
				code.add("A=A-1");

			}
		}

		return;
	}

	private void pushPointer(int value) {

		if (value == 0) {
			// *SP=THIS, SP++

			code.add("@THIS");

			code.add("D=M");

			code.add("@SP");
			code.add("A=M");
			code.add("M=D");

			updateSP();

		} else {

			// *SP=THAT, SP++
			code.add("@THAT");

			code.add("D=M");

			code.add("@SP");
			code.add("A=M");
			code.add("M=D");

			updateSP();

		}

		return;
	}

	private void popPointer(int value) {

		if (value == 0) {

			// SP--, THIS=*SP

			currentSpMValue();

			code.add("@THIS");

			code.add("M=D");

		} else {

			// SP--, THAT=*SP

			currentSpMValue();

			code.add("@THAT");

			code.add("M=D");
		}

		return;
	}

	// addr = 5+i; SP--; *SP=*addr
	private void popTemp(int value) {

		// D = *sp
		code.add("//pop");
		currentSpMValue();

		String addr = Integer.toString(value + 5);

		code.add("@" + addr);
		code.add("M=D");

		return;
	}

	// addr = 5+i; *SP=*addr, SP++
	private void pushTemp(int value) {

		String addr = Integer.toString(value + 5);

		code.add("@" + addr);
		code.add("D=M");

		code.add("@SP");
		code.add("A=M");
		code.add("M=D");

		updateSP();

		return;
	}

	// push constant i == *SP =i, SP++

	private void pushConstant(int value) {

		// constant

		code.add("//pushConstant");
		code.add("@" + value);
		code.add("D=A");

		// *SP=D
		code.add("@SP");
		code.add("A=M");
		code.add("M=D");

		updateSP();

		return;

	}

	// M+1 is the only allowed expression
	// addr = segmentPointer + i; *SP = *addr, SP++
	private void push(String seg, int value) {

		code.add("//push");
		code.add("@" + seg);

		if (value == 0) {

			code.add("A=M");

		} else {

			for (int i = 0; i < value; i++) {

				code.add("M=M+1");
			}
			code.add("A=M");
		}

		code.add("D=M");

		code.add("@SP");
		code.add("A=M");
		code.add("M=D");

		updateSP();

		// reset base address
		if (value != 0) {

			code.add("@" + seg);

			for (int i = 0; i < value; i++) {
				code.add("M=M-1");

			}
		}

		return;
	}

	// addr = segmentPointer + i; SP--; *addr = *SP
	private void pop(String seg, int value) {

		// D = *sp
		currentSpMValue();

		code.add("@" + seg);

		if (value == 0) {

			code.add("A=M");

		} else {

			for (int i = 0; i < value; i++) {

				code.add("M=M+1");
			}
			code.add("A=M");
		}
		code.add("M=D");

		// reset base address
		if (value != 0) {

			code.add("@" + seg);

			for (int i = 0; i < value; i++) {
				code.add("M=M-1");

			}
		}

		return;
	}

	// SP--
	private void currentSpMValue() {

		code.add("// D = *SP");

		code.add("@SP");
		code.add("M=M-1");
		code.add("A=M");
		code.add("D=M");

		return;
	}

	private void not() {

		code.add("//not");
		code.add("@SP");
		code.add("M=M-1");
		code.add("A=M");
		code.add("D=!M");
		code.add("M=D");
		code.add("");

		updateSP();

		return;

	}

	private void or() {

		// D|M
		code.add("//or");
		currentSpMValue();

//		code.add("//SP-- and D = *SP");
		code.add("@SP");
		code.add("M=M-1");
		code.add("A=M");// M becomes RAM[A]

		code.add("D=D|M");
		code.add("M=D");

		updateSP();

		return;
	}

	private void and() {

		code.add("//and");
		currentSpMValue();

		code.add("@SP");
		code.add("M=M-1");
		code.add("A=M");// M becomes RAM[A]

		code.add("D=D&M");
		code.add("M=D");

		updateSP();

		return;

	}

	private void neg() {

		code.add("//neg");

		code.add("@SP");
		code.add("M=M-1");
		code.add("A=M");
		code.add("D=-M");
		code.add("M=D");
		code.add("");

		updateSP();

		return;
	}

	public ArrayList<String> getCode() {

		return code;
	}

	private void updateSP() {

		// SP++

		code.add("@SP");
		code.add("M=M+1");

		return;

	}

	private void sub() {

		code.add("//sub");
		currentSpMValue();

		code.add("@SP");
		code.add("M=M-1");

//		code.add("//go to SP");
		code.add("A=M");

		code.add("D=M-D");
		code.add("M=D");

		updateSP();

		return;

	}

	private void add() {

		code.add("//add");
		currentSpMValue();

		code.add("@SP");
		code.add("M=M-1");
		code.add("A=M");
		code.add("D=D+M");
		code.add("M=D");

		updateSP();

		return;

	}

	private void gt() {

		String label = "IFJGT_" + Integer.toString(labelCount);
		String end = "END_" + Integer.toString(labelCount);

		code.add("//gt");

		currentSpMValue();

		code.add("@SP");
		code.add("M=M-1");
		code.add("A=M");
		code.add("D=M-D");

		code.add("@" + label);
		code.add("D;JGT");
		code.add("@SP");
		code.add("A=M");
		code.add("M=0");
		code.add("@" + end);
		code.add("M;JEQ");

		// update value
		code.add("(" + label + ")");
		code.add("@SP");
		code.add("A=M");
		code.add("M=-1");

		code.add("(" + end + ")");
		updateSP();

		return;

	}

	private void lt() {

		String label = "IFJLT_" + Integer.toString(labelCount);
		String end = "END_" + Integer.toString(labelCount);

		currentSpMValue();

		code.add("//lt");
		code.add("@SP");
		code.add("M=M-1");
		code.add("A=M");
		code.add("D=M-D");

		code.add("@" + label);
		code.add("D;JLT");
		code.add("@SP");
		code.add("A=M");
		code.add("M=0");
		code.add("@" + end);
		code.add("M;JEQ");

		// update value
		code.add("(" + label + ")");
		code.add("@SP");
		code.add("A=M");
		code.add("M=-1");

		code.add("(" + end + ")");
		updateSP();

		return;
	}

	private void eq() {

		String label = "IFEQ_" + Integer.toString(labelCount);
		String end = "END_" + Integer.toString(labelCount);

		code.add("//eq");
		currentSpMValue();

		code.add("@SP");
		code.add("M=M-1");
		code.add("A=M");
		code.add("D=D-M");

		code.add("@" + label);
		code.add("D;JEQ");

		code.add("@SP");
		code.add("A=M");
		code.add("M=0");
		code.add("@" + end);
		code.add("M;JEQ");

		// update value
		code.add("(" + label + ")");
		code.add("@SP");
		code.add("A=M");
		code.add("M=-1");

		code.add("(" + end + ")");
		updateSP();

		return;

	}

}
