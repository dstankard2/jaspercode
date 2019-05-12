package net.sf.jaspercode.engine;

import net.sf.jaspercode.api.BuildComponentProcessor;
import net.sf.jaspercode.api.config.BuildComponent;
import net.sf.jaspercode.engine.exception.PreprocessingException;

public class BuildComponentPattern {

	private Class<? extends BuildComponent> componentClass;
	
	private Class<? extends BuildComponentProcessor> processorClass;

	/**
	 * Neither parameter is ever null.
	 * @param componentClass
	 * @param processorClass
	 */
	public BuildComponentPattern(Class<? extends BuildComponent> componentClass,Class<? extends BuildComponentProcessor> processorClass) {
		this.componentClass = componentClass;
		this.processorClass = processorClass;
	}

	public BuildComponentProcessor getProcessor(BuildComponent comp) throws PreprocessingException {
		BuildComponentProcessor ret = null;
		
		try {
			ret = processorClass.newInstance();
		} catch(Exception e) {
			throw new PreprocessingException("Exception while getting processor", e);
		}
		
		return ret;
	}

	public Class<? extends BuildComponent> getComponentClass() {
		return componentClass;
	}

	public Class<? extends BuildComponentProcessor> getProcessorClass() {
		return processorClass;
	}

}
