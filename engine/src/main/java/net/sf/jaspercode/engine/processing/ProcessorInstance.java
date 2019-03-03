package net.sf.jaspercode.engine.processing;

import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.engine.RegisteredProcessor;

public class ProcessorInstance {
	ComponentProcessor processor = null;
	private ProcessorContext ctx = null;
	private Component component = null;
	
	public ProcessorInstance(Component component,ProcessorContext ctx,RegisteredProcessor proc) {
		this.component = component;
		this.ctx = ctx;
		Class<? extends ComponentProcessor> procClass = proc.getProcessorClass();
		try {
			processor = procClass.newInstance();
		} catch(Exception e) {
			throw new RuntimeException("Couldn't create instance pf component processor",e);
		}
	}

	public void invoke() throws JasperException {
		processor.init(component, ctx);
		processor.process();
	}

}
