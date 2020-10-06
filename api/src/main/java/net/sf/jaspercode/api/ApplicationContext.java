package net.sf.jaspercode.api;

import java.util.Set;

/**
 * Gives access to engine-level resources such as engine properties and plugins
 * @author DCS
 */
public interface ApplicationContext {

	public String getEngineProperty(String name);

	public <T> Set<Class<T>> getPlugins(Class<T> superClass);

}

