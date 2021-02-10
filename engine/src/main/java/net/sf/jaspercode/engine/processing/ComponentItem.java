package net.sf.jaspercode.engine.processing;

import java.util.Map;

import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.engine.ComponentPattern;
import net.sf.jaspercode.engine.JasperResources;
import net.sf.jaspercode.engine.RegisteredProcessor;
import net.sf.jaspercode.engine.files.ComponentFile;

public class ComponentItem extends ProcessableBase implements Item {
	private Component component = null;
	private ComponentPattern pattern = null;
	private int originatorId;
	
	public ComponentItem(int itemId, Component component, ProcessableContext processableContext, 
			ComponentFile componentFile, ComponentPattern pattern,JasperResources jasperResources, int originatorId) {
		super(itemId, processableContext, componentFile, jasperResources);
		this.component = component;
		this.pattern = pattern;
		this.log = new ProcessorLog(getName());
		this.originatorId = originatorId;
	}

	@Override
	public int getPriority() {
		return component.getPriority();
	}
	
	@Override
	public int getOriginatorId() {
		return originatorId;
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
		Map<String,String> configs = null;
		boolean ret = true;
		changes = new ProcessableChanges(itemId, originatorFile, component);
		
		configs = ProcessingUtilities.getConfigs(originatorFile, component);
		
		ProcessorContextImpl pctx = new ProcessorContextImpl(itemId, processableCtx, jasperResources, log, folder, configs, changes);

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
					ret = false;
					break;
				}
			}
		}
		
		if (!ret) {
			this.state = ProcessingState.ERROR;
		}
		return ret;
	}

}

