package net.sf.jaspercode.engine.processing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import net.sf.jaspercode.api.ApplicationContext;
import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.annotation.ConfigProperty;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.engine.ComponentPattern;
import net.sf.jaspercode.engine.RegisteredProcessor;
import net.sf.jaspercode.engine.application.ProcessingContext;
import net.sf.jaspercode.engine.definitions.ComponentFile;

public class ComponentEntry extends ProcessableBase implements Tracked {

	private Component component = null;
	private ComponentPattern pattern = null;
	private int originatorId = -1;

	public ComponentEntry(ApplicationContext applicationContext,ComponentFile componentFile, ProcessingContext processingContext, Component component, ComponentPattern pattern, int id, int originatorId) {
		super(applicationContext, componentFile, processingContext,id,component.getComponentName(), new HashMap<>());
		this.component = component;
		this.pattern = pattern;
		this.originatorId = originatorId;
		this.log = new ProcessorLog(getName());
		this.processorContext = new ProcessorContextImpl(componentFile.getFolder(), this, log);
	}
	
	public int getOriginatorId() {
		return originatorId;
	}

	@Override
	public int getPriority() {
		return component.getPriority();
	}
	
	public Component getComponent() {
		return component;
	}

	@Override
	public boolean preprocess() {
		this.state = ProcessingState.PREPROCESSING;
		if (!populateConfigurations(component)) {
			return false;
		}
		this.state = ProcessingState.PREPROCESSED;
		return true;
	}
	
	@Override
	public String getName() {
		return this.component.getComponentName();
	}
	
	@Override
	public boolean process() {
		boolean ret = true;
		this.state = ProcessingState.PROCESSING;
		for(RegisteredProcessor p : pattern.getProcessors()) {
			Class<? extends ComponentProcessor> cl = p.getProcessorClass();
			try {
				ComponentProcessor proc = cl.newInstance();
				proc.init(component, processorContext);
				proc.process();
				this.state = ProcessingState.COMPLETE;
			} catch(Exception e) {
				this.state = ProcessingState.ERROR;
				ret = false;
				e.printStackTrace();
			}
		}
		return ret;
	}

}

