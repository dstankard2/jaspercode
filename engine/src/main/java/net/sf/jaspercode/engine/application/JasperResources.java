package net.sf.jaspercode.engine.application;

import java.util.Set;

import net.sf.jaspercode.api.ApplicationContext;
import net.sf.jaspercode.engine.EngineProperties;
import net.sf.jaspercode.engine.PluginManager;

/**
 * Engine-level resources that an applicationManager and its delegates may use.
 * @author DCS
 *
 */
public class JasperResources implements ApplicationContext {

	private PluginManager pluginManager = null;
	private EngineProperties engineProperties = null;

	public EngineProperties getEngineProperties() {
		return engineProperties;
	}

	public JasperResources(EngineProperties engineProperties, PluginManager pluginManager) {
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
	
	public void engineDebug(String message) {
		if (engineProperties.getDebug()) {
			System.out.println("[Engine-Debug] "+message);
		}
	}
}

