package net.sf.jaspercode.engine.impl;

import java.util.Set;

import net.sf.jaspercode.api.ApplicationContext;
import net.sf.jaspercode.engine.EngineProperties;
import net.sf.jaspercode.engine.PluginManager;

public class ApplicationContextImpl implements ApplicationContext {

	private PluginManager pluginManager = null;
	private EngineProperties engineProperties = null;
	
	public ApplicationContextImpl(EngineProperties engineProperties, PluginManager pluginManager) {
		this.pluginManager = pluginManager;
		this.engineProperties = engineProperties;
	}

	@Override
	public String getEngineProperty(String name) {
		return engineProperties.getUserOptions().get(name);
	}

	@Override
	public <T> Set<Class<T>> getPlugins(Class<T> superClass) {
		return pluginManager.getPluginSubclasses(superClass);
	}
	
	public Set<Class<?>> getXmlConfigClasses() {
		return pluginManager.getXmlConfigClasses();
	}

}
