package net.sf.jaspercode.engine.processing;

import java.util.Map;

import net.sf.jaspercode.api.ApplicationContext;
import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.BuildProcessorContext;
import net.sf.jaspercode.api.Log;
import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.config.BuildComponent;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.resources.ApplicationFolder;
import net.sf.jaspercode.engine.definitions.ApplicationFolderImpl;
import net.sf.jaspercode.engine.processing.ProcessorLog;

public class BuildProcessorContextImpl implements BuildProcessorContext {

	private ApplicationFolderImpl folder = null;
	private BuildComponentItem item = null;
	private ProcessorLog log = null;
	private ApplicationContext applicationContext = null;
	private Map<String,String> configs = null;
	
	public BuildProcessorContextImpl(ApplicationFolderImpl folder, BuildComponentItem item, 
			BuildComponent component,ApplicationContext applicationContext, ProcessorLog log,
			Map<String,String> configs) {
		super();
		this.folder = folder;
		this.item = item;
		this.log = log;
		this.applicationContext = applicationContext;
		this.configs = configs;
	}

	@Override
	public String getProperty(String name) {
		return configs.get(name);
		//return folder.getProperties().get(name);
	}
	@Override
	public void addSourceFile(SourceFile file) {
		item.saveSourceFile(file);
	}
	@Override
	public SourceFile getSourceFile(String path) {
		return item.getSourceFile(path);
	}
	@Override
	public void setObject(String name, Object obj) {
		item.setObject(name, obj);
	}
	@Override
	public Object getObject(String name) {
		return item.getObject(name);
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
		item.addComponent(component);
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

