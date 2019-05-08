package net.sf.jaspercode.api;

import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.ApplicationResource;
import net.sf.jaspercode.api.resources.FileWatcher;
import net.sf.jaspercode.api.resources.FolderWatcher;
import net.sf.jaspercode.api.types.VariableType;

public interface ProcessorContext {

	public void setLanguageSupport(String language) throws JasperException;

	public boolean addSystemAttribute(String name,String type) throws JasperException;
	public void originateSystemAttribute(String name);
	public void dependOnSystemAttribute(String name);
	public String getSystemAttribute(String name);

	public void addVariableType(VariableType variableType) throws JasperException;
	public void originateVariableType(VariableType variableType);
	public void dependOnVariableType(VariableType variableType);
	public VariableType getVariableType(String name) throws JasperException;

	public void setObject(String name,Object obj);
	public Object getObject(String name);

	public void addSourceFile(SourceFile file);
	public SourceFile getSourceFile(String path);

	public String getProperty(String name);

	public BuildContext getBuildContext();
	
	public ApplicationResource getResource(String path);

	public void addComponent(Component component);

	Log getLog();

	void addFileWatcher(String path,FileWatcher fileWatcher);
	
	void addFolderWatcher(String folderPath,FolderWatcher folderWatcher);
	
	ApplicationContext getApplicationContext();
	
}

