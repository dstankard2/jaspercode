package net.sf.jaspercode.patterns.js.parsing;

import net.sf.jaspercode.api.exception.JasperException;

public class JavascriptParsingException extends JasperException {
	private static final long serialVersionUID = 1L;
	private ParsingInput in = null;
	private int line = 0;
	private int col = 0;
	private String error;
	
	public JavascriptParsingException(ParsingInput in, int line, int col,String error) {
		this.in = in;
		this.line = line;
		this.col = col;
		this.error = error;
	}

	public String getMessage() {
		StringBuilder b = new StringBuilder();
		String l = in.getLine(line);

		b.append("("+line+","+col+") "+error+"\n");
		b.append(l).append('\n');
		for(int i=0;i<col-1;i++) {
			b.append(' ');
		}
		b.append("^\n");
		
		return b.toString();
	}
	public JavascriptParsingException() {
		super();
	}

}

