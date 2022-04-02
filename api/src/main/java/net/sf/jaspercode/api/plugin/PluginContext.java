package net.sf.jaspercode.api.plugin;

import java.util.Set;

import net.sf.jaspercode.api.logging.Log;

public interface PluginContext {

	String getEngineProperty(String name,String defaultValue);

	boolean getBooleanEngineProperty(String name,boolean defaultValue);

	Log getLog();

	public <T> Set<Class<T>> getPlugins(Class<T> superClass);

}

