package net.sf.jaspercode.patterns.js.parsing;

import java.util.ArrayList;
import java.util.List;

import net.sf.jaspercode.api.CodeExecutionContext;

public class JavascriptParser {

	private ParsingInput input = null;
	private String code = null;
	private CodeExecutionContext execCtx;
	private List<String> impliedVariables = new ArrayList<>();
	
	public JavascriptParser(String code,CodeExecutionContext execCtx) {
		this.input = new ParsingInput(code);
		this.code = code;
		this.execCtx = execCtx;
	}
	
	public JavascriptParser addImpliedVariable(String name) {
		impliedVariables.add(name);
		return this;
	}
	
	public JavascriptParsingResult evalExpression() throws JavascriptParsingException {
		JavascriptParsingResult ret = new JavascriptParsingResult();
		
		ret.setCode(code);
		
		return ret;
	}
	
	public JavascriptParsingResult evalCodeBlock() throws JavascriptParsingException {
		JavascriptParsingResult ret = new JavascriptParsingResult();
		// Insert \n after ;
		String c = code.replaceAll(";", ";\n");
		// Also put \n after {
		c = c.replaceAll("\\{", "\\{\n");
		ret.setCode(c);
		
		return ret;
	}
	
}
