package net.sf.jaspercode.engine;

import java.util.Set;

import net.sf.jaspercode.api.ApplicationContext;
import net.sf.jaspercode.engine.processing.ProcessorLog;

/**
 * Engine-level resources that an applicationManager and its delegates may use.
 * @author DCS
 *
 */
public class JasperResources implements ApplicationContext {

	private PluginManager pluginManager = null;
	private EngineProperties engineProperties = null;
	private ProcessorLog engineLogger = null;

	public EngineProperties getEngineProperties() {
		return engineProperties;
	}

	public JasperResources(ProcessorLog engineLogger, EngineProperties engineProperties, PluginManager pluginManager) {
		this.engineLogger = engineLogger;
		this.pluginManager = pluginManager;
		this.engineProperties = engineProperties;
	}

	@Override
	public String getEngineProperty(String name) {
		return engineProperties.getUserOptions().get(name);
	}
	public boolean getBoolean(String name, boolean def) {
		return engineProperties.getBoolean(name, def);
	}

	@Override
	public <T> Set<Class<T>> getPlugins(Class<T> superClass) {
		return pluginManager.getPluginSubclasses(superClass);
	}

	// For the resourceManager to parse component files
	public Set<Class<?>> getXmlConfigClasses() {
		return pluginManager.getXmlConfigClasses();
	}

	public ProcessorLog getEngineLogger() {
		return engineLogger;
	}

	@Deprecated
	public void engineDebug(String message) {
		if (debug()) {
			engineLogger.debug(message);
		}
	}
	
	public boolean debug() {
		return engineProperties.getDebug();
	}
}

