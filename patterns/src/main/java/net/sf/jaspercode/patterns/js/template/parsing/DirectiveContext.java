package net.sf.jaspercode.patterns.js.template.parsing;

import java.util.List;
import java.util.Map;

import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.types.ServiceOperation;

public interface DirectiveContext {
	
	public ProcessorContext getProcessorContext();
	public CodeExecutionContext getExecCtx();
	public String getElementName();

	public Map<String,String> getDomAttributes();
	public Map<String,String> getTemplateAttributes();
	
	public StringBuilder getCode();
	public String getElementVarName();
	public String getContainerVarName();
	public String newVarName(String baseName,String type,CodeExecutionContext execCtx);
	public void continueRenderElement(CodeExecutionContext execCtx) throws JasperException;
	public void continueRenderElement() throws JasperException;
	public String getInnerHtml();
	public String getTemplateObj();
	public ServiceOperation getFunction();
	public List<String> getPreviousEltVars();
	
	void addModule(String location, String...moduleNames);

}

