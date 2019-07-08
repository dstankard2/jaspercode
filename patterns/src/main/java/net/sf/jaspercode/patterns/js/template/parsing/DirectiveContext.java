package net.sf.jaspercode.patterns.js.template.parsing;

import java.util.List;
import java.util.Map;

import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.types.ServiceOperation;

public interface DirectiveContext {
	
	ProcessorContext getProcessorContext();
	CodeExecutionContext getExecCtx();
	String getElementName();

	Map<String,String> getDomAttributes();
	Map<String,String> getTemplateAttributes();
	
	StringBuilder getCode();
	String getElementVarName();
	String getContainerVarName();
	String newVarName(String baseName,String type,CodeExecutionContext execCtx);
	void continueRenderElement(CodeExecutionContext execCtx) throws JasperException;
	void continueRenderElement() throws JasperException;
	String getInnerHtml();
	String getTemplateObj();
	ServiceOperation getFunction();
	List<String> getPreviousEltVars();
	
	void addModule(String location, String...moduleNames);
	
}

