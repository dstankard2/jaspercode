package net.sf.jaspercode.engine.processing;

import java.util.ArrayList;
import java.util.List;

import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.engine.RegisteredProcessor;

public class PatternInstance {
	Component component = null;
	List<ProcessorInstance> processors = new ArrayList<>();
	
	public PatternInstance(ProcessorContext ctx,Component component,List<RegisteredProcessor> processors) {
		this.component = component;
		for(RegisteredProcessor proc : processors) {
			ProcessorInstance inst = new ProcessorInstance(component, ctx, proc);
			this.processors.add(inst);
		}
	}
	
	public void process() throws JasperException {
		for(ProcessorInstance inst : processors) {
			inst.invoke();
		}
	}

}
