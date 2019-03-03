package net.sf.jaspercode.patterns.java.service;

import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.langsupport.java.JavaCode;

public abstract class NestingOperationRenderer extends OperationRenderer {

	public NestingOperationRenderer(ProcessorContext ctx) {
		super(ctx);
	}

	public abstract JavaCode endingCode(CodeExecutionContext execCtx) throws JasperException;

}
