package net.sf.jaspercode.patterns.xml.java.service;

import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.ProcessorContext;

public interface ResultOperation {

	public String getResultType(ProcessorContext ctx, CodeExecutionContext execCtx) throws JasperException;
	public String getResultName(ProcessorContext ctx, CodeExecutionContext execCtx) throws JasperException;

}

