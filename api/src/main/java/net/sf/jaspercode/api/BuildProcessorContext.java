package net.sf.jaspercode.api;

import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.logging.Log;
import net.sf.jaspercode.api.resources.ApplicationFolder;

public interface BuildProcessorContext {

	// Get configuration property
	String getProperty(String name);

	// Source Files
	void addSourceFile(SourceFile file);
	SourceFile getSourceFile(String path);

	// Objects
	void setObject(String name,Object obj);
	Object getObject(String name);

	// Get folder.  Never returns null
	ApplicationFolder getFolder();
	
	// Get the logger
	Log getLog();
	
	// Add a component
	void addComponent(Component component);

	// Get parent build context, or null if this is the root folder of the application
	BuildContext getParentBuildContext();

	ApplicationContext getApplicationContext();

}

