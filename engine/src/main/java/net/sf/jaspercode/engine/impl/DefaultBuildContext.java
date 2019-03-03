package net.sf.jaspercode.engine.impl;

import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.RuntimePlatform;
import net.sf.jaspercode.api.resources.ApplicationResource;
import net.sf.jaspercode.engine.definitions.ApplicationFolderImpl;

public class DefaultBuildContext implements BuildContext {
	ApplicationFolderImpl root = null;
	RuntimePlatform platform = null;

	public DefaultBuildContext(ApplicationFolderImpl root) {
		this.root = root;
	}

	@Override
	public String getApplicationFolderPath() {
		return root.getPath();
	}
	
	public String getName() {
		return root.getPath();
	}

	@Override
	public ApplicationResource getApplicationResource(String path) {
		return root.getResource(path);
	}

	@Override
	public void addDependency(String name) {
	}

	@Override
	public String getOutputRootPath(String fileExt) {
		return null;
	}

	@Override
	public String getOutputRootPath() {
		return getOutputRootPath(null);
	}

	@Override
	public RuntimePlatform getRuntimePlatform() {
		return platform;
	}

	@Override
	public void setRuntimePlatform(RuntimePlatform platform) throws JasperException {
		this.platform = platform;
	}

	@Override
	public void addDependency(BuildContext buildCtx) throws JasperException {
	}

	@Override
	public void addBuildCommand(String cmd) {
		System.out.println("Build command required: '"+cmd+"'");
	}

}
