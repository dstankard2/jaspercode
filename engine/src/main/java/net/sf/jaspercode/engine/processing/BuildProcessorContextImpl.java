package net.sf.jaspercode.engine.processing;

import net.sf.jaspercode.api.ApplicationContext;
import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.BuildProcessorContext;
import net.sf.jaspercode.api.Log;
import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.config.BuildComponent;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.resources.ApplicationFolder;
import net.sf.jaspercode.engine.definitions.ApplicationFolderImpl;

public class BuildProcessorContextImpl implements BuildProcessorContext {

	private ApplicationFolderImpl folder = null;
	private BuildComponentEntry entry = null;
	private ProcessorLog log = null;
	private ApplicationContext applicationContext = null;
	
	public BuildProcessorContextImpl(ApplicationFolderImpl folder, BuildComponentEntry entry, 
			BuildComponent component,ApplicationContext applicationContext, ProcessorLog log) {
		super();
		this.folder = folder;
		this.entry = entry;
		this.log = log;
		this.applicationContext = applicationContext;
	}

	@Override
	public String getProperty(String name) {
		return folder.getProperties().get(name);
	}
	@Override
	public void addSourceFile(SourceFile file) {
		entry.saveSourceFile(file);
	}
	@Override
	public SourceFile getSourceFile(String path) {
		return entry.getSourceFile(path);
	}
	@Override
	public void setObject(String name, Object obj) {
		entry.setObject(name, obj);
	}
	@Override
	public Object getObject(String name) {
		return entry.getObject(name);
	}
	@Override
	public ApplicationFolder getFolder() {
		return folder;
	}
	@Override
	public Log getLog() {
		return log;
	}
	@Override
	public void addComponent(Component component) {
		entry.addComponent(component);
	}

	@Override
	public BuildContext getParentBuildContext() {
		if (folder.getParent()==null) {
			return null;
		} else {
			return folder.getParent().getBuildContext();
		}
	}

	@Override
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

}

