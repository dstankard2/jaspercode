package net.sf.jaspercode.engine.processing;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.engine.ComponentPattern;
import net.sf.jaspercode.engine.JasperResources;
import net.sf.jaspercode.engine.RegisteredProcessor;
import net.sf.jaspercode.engine.files.ApplicationFolderImpl;

public class ComponentItem extends ProcessableBase implements Item {

	private Component component = null;
	private ComponentPattern pattern = null;
	private int originatorId;
	private ApplicationFolderImpl folder;
	
	public ComponentItem(int itemId, Component component, Map<String,String> configs, ProcessableContext ctx, 
			ComponentPattern pattern,JasperResources jasperResources, int originatorId,
			ApplicationFolderImpl folder) {
		super(itemId, configs, ctx, jasperResources);
		this.component = component;
		this.pattern = pattern;
		this.log = new ProcessorLog(getName());
		this.originatorId = originatorId;
		this.folder = folder;
	}

	@Override
	public void assignItemId(int itemId) {
		this.itemId = itemId;
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
		boolean ret = true;
		changes = new ProcessableChanges(itemId);

		ProcessorContextImpl pctx = new ProcessorContextImpl(ctx, jasperResources, log, configs, folder, changes);

		ret = ProcessingUtilities.populateConfigurations(component, log, configs);
		if (ret) {
			for(RegisteredProcessor proc : pattern.getProcessors()) {
				Class<? extends ComponentProcessor> cl = proc.getProcessorClass();
				try {
					ComponentProcessor processor = cl.getConstructor().newInstance();
					processor.init(component, pctx);
					processor.process();
				} catch(JasperException | NoSuchMethodException | InvocationTargetException 
						| IllegalAccessException | InstantiationException e) {
					this.log.error(e.getMessage(), e);
					ret = false;
					break;
				}
			}
		}
		
		return ret;
	}

	@Override
	public ApplicationFolderImpl getFolder() {
		return folder;
	}

}

