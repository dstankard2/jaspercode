package net.sf.jaspercode.engine.application;

import java.util.Map.Entry;
import java.util.Set;

import net.sf.jaspercode.api.ApplicationContext;
import net.sf.jaspercode.engine.EngineProperties;
import net.sf.jaspercode.engine.PluginManager;
import net.sf.jaspercode.engine.definitions.ApplicationFolderImpl;
import net.sf.jaspercode.engine.definitions.ComponentFile;
import net.sf.jaspercode.engine.definitions.SystemAttributesFile;

/**
 * Engine-level resources that an applicationManager and its delegates may use.
 * @author DCS
 *
 */
public class JasperResources implements ApplicationContext {

	private PluginManager pluginManager = null;
	private EngineProperties engineProperties = null;

	// TODO: Move this someplace else
	private ApplicationManager applicationManager = null;
	
	public EngineProperties getEngineProperties() {
		return engineProperties;
	}

	public SystemAttributesFile getSystemAttributesFile() {
		return applicationManager.getSystemAttributesFile();
	}
	
	public void setSystemAttributesFile(SystemAttributesFile systemAttributesFile) {
		this.applicationManager.handleSystemAttributesFileChange(systemAttributesFile);
	}
	
	public JasperResources(EngineProperties engineProperties, PluginManager pluginManager, ApplicationManager applicationManager) {
		this.pluginManager = pluginManager;
		this.engineProperties = engineProperties;
		
		this.applicationManager = applicationManager;
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
	
	public void clearExistingProcessables(ApplicationFolderImpl folder) {
		for(Entry<String,ComponentFile> f : folder.getComponentFiles().entrySet()) {
			applicationManager.unloadComponentFile(f.getValue(), false);
		}
	}

	public void engineDebug(String message) {
		if (engineProperties.getDebug()) {
			System.out.println("[Engine-Debug] "+message);
		}
	}
}

