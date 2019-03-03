package net.sf.jaspercode.engine.processing;

import java.io.File;
import java.util.Map;

import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.resources.ApplicationResource;
import net.sf.jaspercode.api.types.VariableType;
import net.sf.jaspercode.engine.definitions.ComponentFile;

/**
 * Represents a software unit that can contain component entries, such as an application.
 * @author DCS
 *
 */
public interface ComponentContainer {

	Map<String,VariableType> getVariableTypes(String lang) throws JasperException;
	
	Map<String,String> getSystemAttributes();
	
	Map<String,String> getConfiguration(String folderPath);

	Map<String,Object> getObjects();

	SourceFile getSourceFile(String path);

	BuildContext getBuildContext(String path);

	void addComponent(Component component,ComponentFile originator);

	void addSourceFile(SourceFile src);
	
	ApplicationResource getApplicationResource(String path);

	File getOutputDirectory();

}

