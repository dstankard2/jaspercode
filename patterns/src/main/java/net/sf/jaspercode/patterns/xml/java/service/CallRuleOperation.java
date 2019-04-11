package net.sf.jaspercode.patterns.xml.java.service;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.sf.jaspercode.api.AttribEntry;
import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.JasperUtils;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.XmlConfig;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.types.ServiceOperation;
import net.sf.jaspercode.langsupport.java.JavaUtils;
import net.sf.jaspercode.patterns.java.service.CallRuleRenderer;
import net.sf.jaspercode.patterns.java.service.OperationRenderer;

@Plugin
@XmlConfig
@XmlRootElement(name="callRule")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="callRule",propOrder={ })
public class CallRuleOperation extends Operation implements ResultOperation {

	@Override 
	public OperationRenderer getRenderer(ProcessorContext ctx) { return new CallRuleRenderer(ctx,this); }

	@XmlAttribute
	private String result = "";
	
	@XmlAttribute
	private String params = "";

	@XmlAttribute
	private String rule = "";
	
	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}
	
	@Override
	public String getResultType(ProcessorContext ctx, CodeExecutionContext execCtx) throws JasperException {
		if (result==null) return null;
		List<AttribEntry> paramEntries = JasperUtils.readParametersAsList(params, ctx);
		ServiceOperation op = JavaUtils.findRule(rule,paramEntries,ctx, execCtx);

		return op.getReturnType();
	}

	@Override
	public String getResultName(ProcessorContext ctx, CodeExecutionContext execCtx) {
		if ((result==null) || (result.trim().length()==0)) return null;
		return result;
	}

}

