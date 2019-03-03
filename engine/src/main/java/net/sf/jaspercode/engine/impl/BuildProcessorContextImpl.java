package net.sf.jaspercode.engine.impl;

import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.BuildProcessorContext;
import net.sf.jaspercode.api.Log;
import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.resources.ApplicationFolder;
import net.sf.jaspercode.engine.definitions.ApplicationFolderImpl;
import net.sf.jaspercode.engine.definitions.ComponentFile;
import net.sf.jaspercode.engine.processing.ComponentContainer;
import net.sf.jaspercode.engine.processing.ProcessorContainerBase;

public class BuildProcessorContextImpl implements BuildProcessorContext {
	ComponentContainer mgr = null;
	ApplicationFolderImpl folder = null;
	ComponentFile componentFile;
	ProcessorContainerBase entry = null;

	public BuildProcessorContextImpl(ComponentContainer cont, ApplicationFolderImpl folder,ComponentFile componentFile, ProcessorContainerBase entry) {
		this.mgr = cont;
		this.folder = folder;
		this.componentFile = componentFile;
		this.entry = entry;
	}
	
	@Override
	public ApplicationFolder getFolder() {
		return folder;
	}

	@Override
	public void addSourceFile(SourceFile file) {
		mgr.addSourceFile(file);
	}

	@Override
	public SourceFile getSourceFile(String path) {
		return mgr.getSourceFile(path);
	}

	@Override
	public String getProperty(String name) {
		return folder.getProperties().get(name);
	}

	@Override
	public void setObject(String name, Object obj) {
		mgr.getObjects().put(name, obj);
	}

	@Override
	public Object getObject(String name) {
		return mgr.getObjects().get(name);
	}

	@Override
	public void setBuildCommand(String cmd) {
	}

	@Override
	public void setDeployCommand(String cmd) {
	}

	@Override
	public String getFolderPath() {
		return folder.getPath();
	}

	@Override
	public Log getLog() {
		return entry.getLog();
	}

	@Override
	public void addComponent(Component component) {
		this.mgr.addComponent(component, componentFile);
	}

	@Override
	public BuildContext getParentBuildContext() {
		ApplicationFolderImpl parentFolder = this.folder.getParent();
		while(parentFolder!=null) {
			BuildContext ret = parentFolder.getBuildContext(mgr);
			if (ret!=null) {
				return ret;
			}
			parentFolder = parentFolder.getParent();
		}
		return null;
	}

}
