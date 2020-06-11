import java.util.ArrayList;

/*
 * Generates assembly code from parsed VM command
 */

public class CodeTranslate {

	private int value;
	private int type;
	private String seg;
	private String aType;
	
	private int labelCount;

	private ArrayList<String> code;

	public CodeTranslate(int value, int type, String seg, String aType, int labelCount) {

		this.value = value;
		this.type = type;
		this.seg = seg;
		this.aType = aType;
		this.labelCount = labelCount;

		code = new ArrayList<>();

		if (type == 1) {

			push(value);

		}

		if (type == 2) {

			pop(seg, value);
		}

		// 9 arithmetic
		if (type == 0 && !aType.equals(null)) {

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
				or();
				break;
			}

		}

	}

	private void pop(String seg, int value) {

		// addr = segmentPointer + i; SP--; *addr = *SP

		// TODO address seg

	}

	
	private void currentSpMValue() {

		// D = *SP
		code.add("");
//		code.add("//SP-- and D = *SP");
		code.add("@SP");
		code.add("M=M-1");
		code.add("A=M");
		code.add("D=M");

		return;
	}

	

	private void not() {
		
		
		code.add("");
		code.add("//not");
		code.add("@SP");
		code.add("M=M-1");
		code.add("A=M");
		code.add("D=!M");
		code.add("M=D");
		code.add("");
		
		updateSP();


	}

	private void or() {
		
		//D|M
		code.add("//or");
		currentSpMValue();
		
		code.add("");
//		code.add("//SP-- and D = *SP");
		code.add("@SP");
		code.add("M=M-1");
		code.add("A=M");//M becomes RAM[A]
	
		code.add("D=D|M");
		code.add("M=D");
		
		updateSP();
		

	}

	private void and() {
		
		code.add("");
		code.add("//and");
		currentSpMValue();
		
		

		code.add("@SP");
		code.add("M=M-1");
		code.add("A=M");//M becomes RAM[A]
		
		
		code.add("D=D&M");
		code.add("M=D");
		
		updateSP();
		
		
	}

	private void neg() {
		
		code.add("");
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

	
	
	// push constant i == *SP =i, SP++

	private void push(int value) {

		//constant
		code.add("");
		code.add("//push");
		code.add("@" + value);
		code.add("D=A");

		// *SP=D
		code.add("@SP");
		code.add("A=M");
		code.add("M=D");

		// SP++
		code.add("@SP");
		code.add("M=M+1");

		return;

	}

	// pop constant i == SP--, *SP=i


	
	
	private void updateSP() {
		
		//SP++ 
		code.add("");
		code.add("@SP");
		code.add("M=M+1");
		
		return;

	}

	private void sub() {

		
		code.add("");
		code.add("//sub");
		currentSpMValue();

	
		
		code.add("@SP");
		code.add("M=M-1");

		code.add("");
//		code.add("//go to SP");
		code.add("A=M");

		code.add("D=M-D");
		code.add("M=D");

		updateSP();

		return;

	}

	private void add() {

		code.add("");
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
		
		code.add("");
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
		code.add("@"+end);
		code.add("M;JEQ");

		// update value
		code.add("(" + label + ")");
		code.add("@SP");
		code.add("A=M");
		code.add("M=-1");
		
		code.add("("+end+")");
		updateSP();
		
		return;
		
	}
	
	private void lt() {
		
		String label = "IFJLT_" + Integer.toString(labelCount);
		String end = "END_" + Integer.toString(labelCount);
		
		currentSpMValue();
		
		code.add("");
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
		code.add("@"+end);
		code.add("M;JEQ");

		// update value
		code.add("(" + label+")");
		code.add("@SP");
		code.add("A=M");
		code.add("M=-1");
		
		code.add("("+end+")");
		updateSP();
		
		return;
	}
	
	private void eq() {

		String label = "IFEQ_" + Integer.toString(labelCount);
		String end = "END_" + Integer.toString(labelCount);
		
		code.add("");
		code.add("//eq");
		currentSpMValue();
		
		code.add("@SP");
		code.add("M=M-1");
		code.add("A=M");
		code.add("D=D-M");

		
		code.add("@"+label);
		code.add("D;JEQ");
		
		code.add("@SP");
		code.add("A=M");
		code.add("M=0");
		code.add("@"+end);
		code.add("M;JEQ");

		// update value
		code.add("(" + label + ")");
		code.add("@SP");
		code.add("A=M");
		code.add("M=-1");
		
		
		code.add("("+end+")");
		updateSP();
		
		
		return;

	}

}
