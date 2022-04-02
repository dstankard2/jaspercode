package net.sf.jaspercode.engine;

import java.util.Set;

import net.sf.jaspercode.api.logging.Log;
import net.sf.jaspercode.api.plugin.PluginContext;

public class PluginContextImpl implements PluginContext {
	private EngineProperties props = null;
	private JasperResources jasperResources;

	public PluginContextImpl(JasperResources jasperResources, EngineProperties props) {
		super();
		this.jasperResources = jasperResources;
		this.props = props;
	}

	@Override
	public String getEngineProperty(String name, String defaultValue) {
		return props.getProperty(name, defaultValue);
	}

	@Override
	public boolean getBooleanEngineProperty(String name, boolean defaultValue) {
		return props.getBoolean(name, defaultValue);
	}

	@Override
	public Log getLog() {
		return null;
	}

	@Override
	public <T> Set<Class<T>> getPlugins(Class<T> superClass) {
		return jasperResources.getPlugins(superClass);
	}

}