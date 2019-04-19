package net.sf.jaspercode.engine;

import net.sf.jaspercode.api.Log;
import net.sf.jaspercode.api.plugin.PluginContext;

public class PluginContextImpl implements PluginContext {
	Log log = null;
	EngineProperties props = null;

	public PluginContextImpl(Log log, EngineProperties props) {
		super();
		this.log = log;
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
		return log;
	}

}
