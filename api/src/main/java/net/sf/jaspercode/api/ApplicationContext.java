package net.sf.jaspercode.api;

import java.util.Set;

public interface ApplicationContext {

	public String getEngineProperty(String name);

	public <T> Set<Class<T>> getPlugins(Class<T> superClass);

}

