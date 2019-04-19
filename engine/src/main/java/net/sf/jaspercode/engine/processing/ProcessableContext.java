package net.sf.jaspercode.engine.processing;

import net.sf.jaspercode.api.ApplicationContext;
import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.resources.ResourceWatcher;
import net.sf.jaspercode.api.types.VariableType;

/**
 * Internal engine interface for managing a processable.
 * @author DCS
 *
 */
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
	//void addVariableType(String lang,VariableType variableType);
	void originateType(String lang,VariableType type);
	void dependOnType(String lang,String name, BuildContext buildCtx);
	VariableType getVariableType(String language,String typeName);

	String getConfigurationProperty(String name);
	void addResourceWatcher(ResourceWatcher resourceWatcher);
	ApplicationContext getApplicationContext();
	void addComponent(Component component);

}

