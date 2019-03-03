package net.sf.jaspercode.api;

import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.resources.ApplicationResource;
import net.sf.jaspercode.api.resources.ResourceWatcher;
import net.sf.jaspercode.api.types.VariableType;

public interface ProcessorContext {

	public void setLanguageSupport(String language) throws JasperException;

	public boolean addSystemAttribute(String name,String type);
	public void originateSystemAttribute(String name);
	public void dependOnSystemAttribute(String name);
	public String getSystemAttribute(String name);

	public void addVariableType(VariableType variableType) throws JasperException;
	public void originateVariableType(String typeName);
	public void dependOnVariableType(String typeName);
	public VariableType getVariableType(String name) throws JasperException;

	public void setObject(String name,Object obj);
	public void originateObject(String name);
	public void dependOnObject(String name);
	public Object getObject(String name);

	public void addSourceFile(SourceFile file);
	public void originateSourceFile(SourceFile file);
	public SourceFile getSourceFile(String path);

	public String getProperty(String name);

	public BuildContext getBuildContext();
	
	public ApplicationResource getResource(String path);
	public void dependOnResource(String path);

	public void addComponent(Component component);

	public Log getLog();

	public void addResourceWatcher(ResourceWatcher watcher, String path);
	
	public ApplicationContext getApplicationContext();
	
}
