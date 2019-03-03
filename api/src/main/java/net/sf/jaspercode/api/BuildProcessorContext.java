package net.sf.jaspercode.api;

import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.resources.ApplicationFolder;

public interface BuildProcessorContext {

	void setBuildCommand(String cmd);
	void setDeployCommand(String cmd);
	String getFolderPath();
	String getProperty(String name);
	void addSourceFile(SourceFile file);
	SourceFile getSourceFile(String path);
	void setObject(String name,Object obj);
	Object getObject(String name);
	ApplicationFolder getFolder();
	Log getLog();
	void addComponent(Component component);
	BuildContext getParentBuildContext();

}
