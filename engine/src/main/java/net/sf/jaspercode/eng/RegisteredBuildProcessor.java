package net.sf.jaspercode.eng;

import net.sf.jaspercode.api.BuildComponentProcessor;
import net.sf.jaspercode.api.config.BuildComponent;

public class RegisteredBuildProcessor {

	private Class<? extends BuildComponent> buildComponentClass;
	private Class<? extends BuildComponentProcessor> processorClass = null;

	public Class<? extends BuildComponentProcessor> getProcessorClass() {
		return processorClass;
	}
	public void setProcessorClass(Class<? extends BuildComponentProcessor> processorClass) {
		this.processorClass = processorClass;
	}
	public Class<? extends BuildComponent> getBuildComponentClass() {
		return buildComponentClass;
	}
	public void setBuildComponentClass(Class<? extends BuildComponent> buildComponentClass) {
		this.buildComponentClass = buildComponentClass;
	}
	
}
