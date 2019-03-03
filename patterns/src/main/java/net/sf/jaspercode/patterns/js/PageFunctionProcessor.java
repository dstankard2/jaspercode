package net.sf.jaspercode.patterns.js;

import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.Processor;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.patterns.xml.js.PageFunction;

@Plugin
@Processor(componentClass = PageFunction.class)
public class PageFunctionProcessor implements ComponentProcessor {

	ProcessorContext ctx = null;
	PageFunction comp = null;
	
	@Override
	public void init(Component component, ProcessorContext ctx) {
		this.ctx = ctx;
		this.comp = (PageFunction)component;
	}

	@Override
	public void process() throws JasperException {
		
	}

}
