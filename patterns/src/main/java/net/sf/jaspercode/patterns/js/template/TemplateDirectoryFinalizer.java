package net.sf.jaspercode.patterns.js.template;

import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.Processor;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;

@Plugin
@Processor(componentClass=TemplateDirectoryFinish.class)
public class TemplateDirectoryFinalizer implements ComponentProcessor {

	TemplateDirectoryFinish comp = null;
	ProcessorContext ctx = null;
	
	@Override
	public void init(Component component, ProcessorContext ctx) {
		this.comp = (TemplateDirectoryFinish)component;
		this.ctx = ctx;
	}

	@Override
	public void process() throws JasperException {
		
	}

}

