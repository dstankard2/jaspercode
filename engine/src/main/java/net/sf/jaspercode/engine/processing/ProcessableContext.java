package net.sf.jaspercode.engine.processing;

import net.sf.jaspercode.api.ApplicationContext;
import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.resources.FileProcessor;
import net.sf.jaspercode.api.resources.FolderWatcher;
import net.sf.jaspercode.api.types.VariableType;

public interface ProcessableContext {


	// Source Files
	void addSourceFile(SourceFile sourceFile);
	SourceFile getSourceFile(String path);
	
	// Objects
	void setObject(String name, Object value);
	Object getObject(String objectName);

	// System Attributes
	void addSystemAttribute(String name,String type);
	void originateSystemAttribute(String name);
	void dependOnSystemAttribute(String name);
	String getSystemAttribute(String name);

	// Variable types
	void addVariableType(String lang,VariableType variableType);
	void originateType(String lang,VariableType type);
	void dependOnType(String lang,String name, BuildContext buildCtx);
	VariableType getVariableType(String language,String typeName);

	String getConfigurationProperty(String name);
	void addFolderWatcher(String path,FolderWatcher folderWatcher);
	void addFileProcessor(String path,FileProcessor fileProcessor);
	ApplicationContext getApplicationContext();
	void addComponent(Component component);

}
