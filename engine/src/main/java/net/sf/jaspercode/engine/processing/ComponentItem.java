package net.sf.jaspercode.engine.processing;

import java.util.Map;

import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.engine.ComponentPattern;
import net.sf.jaspercode.engine.RegisteredProcessor;
import net.sf.jaspercode.engine.application.JasperResources;
import net.sf.jaspercode.engine.application.ProcessingContext;
import net.sf.jaspercode.engine.definitions.ComponentFile;

public class ComponentItem extends ProcessableBase implements Item {
	private Component component = null;
	private ComponentPattern pattern = null;
	
	public ComponentItem(int id, Component component, ProcessingContext processingContext, 
			ComponentFile componentFile, Map<String,String> configs,int originatorId,
			ComponentPattern pattern,JasperResources jasperResources) {
		super(id, processingContext, componentFile, configs, originatorId, jasperResources);
		this.component = component;
		this.pattern = pattern;
		this.log = new ProcessorLog(getName());
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public int getPriority() {
		return component.getPriority();
	}

	public Component getComponent() {
		return component;
	}
	
	@Override
	public String getName() {
		return component.getComponentName();
	}

	@Override
	public boolean process() {
		boolean ret = true;
		ProcessorContext pctx = new ProcessorContextImpl(this,folder,log, jasperResources);
		
		ret = ProcessingUtilities.populateConfigurations(component, log, configs);
		if (ret) {
			for(RegisteredProcessor proc : pattern.getProcessors()) {
				Class<? extends ComponentProcessor> cl = proc.getProcessorClass();
				try {
					ComponentProcessor processor = cl.newInstance();
					processor.init(component, pctx);
					processor.process();
					this.state = ProcessingState.COMPLETE;
				} catch(Exception e) {
					this.log.error(e.getMessage(), e);
					this.state = ProcessingState.ERROR;
					ret = false;
					//e.printStackTrace();
					break;
				}
			}
		}
		return ret;
	}

}

