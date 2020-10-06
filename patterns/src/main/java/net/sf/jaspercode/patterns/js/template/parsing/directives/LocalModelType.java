package net.sf.jaspercode.patterns.js.template.parsing.directives;

import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.langsupport.javascript.types.JavascriptType;

public class LocalModelType extends JavascriptType {
	String templateName;

	public LocalModelType(String templateName, ProcessorContext ctx) {
		super(templateName+"_Model");
		this.templateName = templateName;
	}

	@Override
	public String getCodeToRetrieveAttribute(String varName, String attribName,
			String targetType, CodeExecutionContext execCtx)
			throws IllegalArgumentException, JasperException {
		StringBuilder b = new StringBuilder();
		
		b.append(varName+"."+attribName);
		return b.toString();
	}

	@Override
	public String getCodeToSetAttribute(String varName, String attribName,
			String evaluatedValue, CodeExecutionContext execCtx)
			throws JasperException {
		String ret = null;
		ret = varName+"."+attribName+" = "+evaluatedValue+";\n";
		return ret;
	}

}
