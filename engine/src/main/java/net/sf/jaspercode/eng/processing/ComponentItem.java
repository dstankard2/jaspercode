package net.sf.jaspercode.eng.processing;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.eng.ComponentPattern;
import net.sf.jaspercode.eng.JasperResources;
import net.sf.jaspercode.eng.RegisteredProcessor;
import net.sf.jaspercode.eng.files.ComponentFile;

public class ComponentItem extends ProcessableBase {
	Component component = null;
	ComponentPattern pattern = null;
	
	public ComponentItem(Component component, ProcessableContext ctx, ComponentFile originatorFile,
			JasperResources jasperResources, ComponentPattern pattern) {
		super(ctx, originatorFile, jasperResources);
		this.component = component;
		this.pattern = pattern;
	}
	@Override
	public int getPriority() {
		return component.getPriority();
	}

	@Override
	public String getName() {
		return component.getComponentName();
	}

	@Override
	public boolean process() {
		AtomicBoolean ret = new AtomicBoolean(true);
		List<RegisteredProcessor> procs = pattern.getProcessors();

		Map<String,String> configs = ProcessingUtilities.getConfigs(originatorFile,  component);
		ProcessingUtilities.populateConfigurations(component, log, configs);

		procs.stream().forEach(proc -> {
			ProcessorContextImpl processorCtx = new ProcessorContextImpl(ctx, jasperResources, log, folder, configs, originatorFile);

			try {
				Class<? extends ComponentProcessor> procClass = proc.getProcessorClass();
				ComponentProcessor processor = procClass.getDeclaredConstructor().newInstance();
				processor.init(component, processorCtx);
				processor.process();
			} catch(Exception e) {
				ret.set(false);
				log.error("Couldn't process component", e);
			}
		});

		return ret.get();
	}

}
