package net.sf.jaspercode.api;

import net.sf.jaspercode.api.resources.ApplicationResource;

/**
 * The compile-time context of a component in Jasper.
 * Can be used to add dependencies
 * @author DCS
 *
 */
public interface BuildContext {

	void addDependency(String name);
	
	void addDependency(BuildContext buildCtx) throws JasperException;

	String getOutputRootPath(String fileExt);

	String getOutputRootPath();

	String getApplicationFolderPath();

	ApplicationResource getApplicationResource(String path);

	RuntimePlatform getRuntimePlatform();

	void setRuntimePlatform(RuntimePlatform platform) throws JasperException;

	String getName();
	
	void addBuildCommand(String cmd);
	
}

