package net.sf.jaspercode.patterns.js.parsing;

import java.util.Arrays;
import java.util.List;

public class ParsingInput {

	private String input = null;
	private int inputPos = 0;
	private int line = 1;
	private int pos = 1;
	List<String> lines = null;

	public ParsingInput(String code) {
		this.input = code;
		this.inputPos = 0;
		lines = Arrays.asList(code.split("\n"));
	}

	public boolean hasNextChar() {
		return inputPos < (input.length()-1);
	}

	public char nextChar() {
		char c = input.charAt(inputPos);
		inputPos++;
		
		return c;
	}
	
	public char peekNextChar() {
		return input.charAt(inputPos);
	}
	
	public void skipWs() {
		if (Character.isWhitespace(peekNextChar())) {
			nextChar();
		}
	}

	public String getLine(int i) {
		if (i>=lines.size()) return null;
		return lines.get(i-1);
	}

}
