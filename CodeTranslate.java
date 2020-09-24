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
	private boolean isFunctionLabel;

	public CodeTranslate(int value, int type, String seg, String aType, int labelCount, String fileName, String label,
			String functionName, boolean isFunctionLabel) {

		this.seg = seg;
		this.labelCount = labelCount;
		this.fileName = fileName;
		this.functionName = functionName;
		code = new ArrayList<>();
		this.isFunctionLabel = isFunctionLabel;

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

			// pop
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

		} else if (type == 8) {

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
				// nothing happens
				break;
			}

		} else {

			// nothing happens if the command is not one of the above types
		}

	}

	// calling a function after arguments have been pushed on the stack
	private void writeCall(int value) {

		String reAdd = functionName + "$ret." + labelCount;

		code.add("//writeCall");

		// push return address
		code.add("@" + reAdd);
		code.add("D=A");

		code.add("@SP");
		code.add("A=M");
		code.add("M=D");
		updateSP();

		// push LCL
		code.add("@LCL");
		code.add("D=M");
		code.add("@SP");
		code.add("A=M");
		code.add("M=D");
		updateSP();

		// push ARG
		code.add("@ARG");
		code.add("D=M");
		code.add("@SP");
		code.add("A=M");
		code.add("M=D");
		updateSP();

		// push THIS
		code.add("@THIS");
		code.add("D=M");
		code.add("@SP");
		code.add("A=M");
		code.add("M=D");
		updateSP();

		// push THAT
		code.add("@THAT");
		code.add("D=M");
		code.add("@SP");
		code.add("A=M");
		code.add("M=D");
		updateSP();

		// ARG = SP - value - 5
		code.add("@SP");
		code.add("A=M");
		code.add("D=A");
		int j = value + 5;
		code.add("@" + j);
		code.add("D=D-A");
		code.add("@ARG");
		code.add("M=D");

		// LCL = SP
		code.add("@SP");
		code.add("A=M");
		code.add("D=A");

		code.add("@LCL");
		code.add("M=D");

		// goto function
		code.add("@" + functionName);
		code.add("0;JMP");

		code.add("(" + reAdd + ")");

	}

	// labels inside return only live in return
	private void writeReturn(int value) {

		code.add("//return");

		String f = "@RET" + labelCount;

		// FRAME=LCL
		code.add("@LCL");
		code.add("A=M");
		code.add("D=A");
		code.add(f);
		code.add("M=D");

		// RET=*(FRAME-5)

		String retu = "reAdd" + labelCount;
		code.add("@5");
		code.add("A=D-A");
		code.add("D=M");
		code.add("@" + retu);
		code.add("M=D");

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

		code.add("@ARG");
		code.add("M=M-1");

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

		// goto Return address
		code.add("@" + retu);
		code.add("A=M");
		code.add("0;JMP");

		return;

	}

	// only call bootStrap when translating a folder of .vm files
	protected static ArrayList<String> bootStrap() {

		ArrayList<String> codeBoot = new ArrayList<>();

		codeBoot.add("//SP=256");
		codeBoot.add("@256");
		codeBoot.add("D=A");
		codeBoot.add("@SP");
		codeBoot.add("M=D");

		codeBoot.add("//call sys.init 0");

		// push return address
		codeBoot.add("@Sys.init$ret$0");
		codeBoot.add("D=M");
		codeBoot.add("@SP");
		codeBoot.add("A=M");
		codeBoot.add("M=D");

		codeBoot.add("@SP");
		codeBoot.add("M=M+1");

		// push LCL
		codeBoot.add("@LCL");
		codeBoot.add("D=M");
		codeBoot.add("@SP");
		codeBoot.add("A=M");
		codeBoot.add("M=D");

		codeBoot.add("@SP");
		codeBoot.add("M=M+1");

		// push ARG
		codeBoot.add("@ARG");
		codeBoot.add("D=M");
		codeBoot.add("@SP");
		codeBoot.add("A=M");
		codeBoot.add("M=D");

		codeBoot.add("@SP");
		codeBoot.add("M=M+1");

		// push THIS
		codeBoot.add("@THIS");
		codeBoot.add("D=M");
		codeBoot.add("@SP");
		codeBoot.add("A=M");
		codeBoot.add("M=D");

		codeBoot.add("@SP");
		codeBoot.add("M=M+1");

		// push THAT
		codeBoot.add("@THAT");
		codeBoot.add("D=M");
		codeBoot.add("@SP");
		codeBoot.add("A=M");
		codeBoot.add("M=D");

		codeBoot.add("@SP");
		codeBoot.add("M=M+1");

		// goto function
		codeBoot.add("@Sys.init");
		codeBoot.add("0;JMP");

		codeBoot.add("(Sys.init$ret$0)");

		return codeBoot;

	}

	private void writeFunction(int value) {

		code.add("//writeFunction");
		// declare label for function entry
		code.add("(" + functionName + ")");

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

	// if goto lives in functions
	private void writeIfLabel(String label) {

		currentSpMValue();
		// if current *SP !=0 then go

		if (isFunctionLabel) {

			code.add("@" + functionName + "$" + label);

		} else {

			code.add("@" + fileName + "$" + label);

		}
		code.add("D;JNE");

		return;

	}

	// goto lives in functions
	private void writeGoToLabel(String label) {

		if (isFunctionLabel) {

			code.add("@" + functionName + "$" + label);

		} else {

			code.add("@" + fileName + "$" + label);

		}

		code.add("0;JMP");

		return;
	}

	// label for arithmetic commands, outside functions
	private void label(String label) {

		if (isFunctionLabel) {

			code.add("(" + functionName + "$" + label + ")");

		} else {

			code.add("(" + fileName + "$" + label + ")");

		}

		return;

	}

	// global variables - predefined from 5-256
	private void popStatic(int value) {

		code.add("//popStatic");

		String f = "@" + fileName + "." + value;

		currentSpMValue();

		code.add(f);
		code.add("M=D");

		return;
	}

	private void pushStatic(int value) {

		code.add("//push Static");

		code.add("@" + fileName + "." + value);
		code.add("D=M");

		code.add("@SP");
		code.add("A=M");
		code.add("M=D");

		updateSP();

		return;
	}

	private void pushPointer(int value) {

		code.add("//push pointer");
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

		code.add("//pop pointer");
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

		int val = value + 5;

		code.add("@" + val);
//		code.add("A=M");
		code.add("M=D");

		return;
	}

	// addr = 5+i; *SP=*addr, SP++
	private void pushTemp(int value) {

		code.add("//pushTemp");
		int val = value + 5;

		code.add("@" + val);
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

	// addr = segmentPointer + i; *SP = *addr, SP++
	private void push(String seg, int value) {

		code.add("//pushSeg");

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

		code.add("//pop");
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

		code.add("//lt");

		currentSpMValue();

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
//		code.add("D=M");

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
