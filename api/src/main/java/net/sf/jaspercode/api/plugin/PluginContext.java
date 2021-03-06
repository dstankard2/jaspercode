package net.sf.jaspercode.api.plugin;

import net.sf.jaspercode.api.Log;

public interface PluginContext {

	String getEngineProperty(String name,String defaultValue);

	boolean getBooleanEngineProperty(String name,boolean defaultValue);

	Log getLog();

}

