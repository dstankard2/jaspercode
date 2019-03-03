package net.sf.jaspercode.patterns.java.service;

import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.langsupport.java.JavaCode;

public abstract class OperationRenderer {
	protected ProcessorContext ctx;
	
	public OperationRenderer(ProcessorContext ctx) {
		this.ctx = ctx;
	}

	public abstract JavaCode getCode(CodeExecutionContext execCtx) throws JasperException;

}

