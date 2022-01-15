package net.sf.jaspercode.eng.processing;

import java.util.Map;

import net.sf.jaspercode.api.ApplicationContext;
import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.BuildProcessorContext;
import net.sf.jaspercode.api.Log;
import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.resources.ApplicationFolder;
import net.sf.jaspercode.eng.files.ApplicationFolderImpl;

public class BuildProcessorContextImpl implements BuildProcessorContext {

	private ApplicationFolderImpl folder = null;
	private ProcessorLog log = null;
	private ApplicationContext applicationContext = null;
	private Map<String,String> configs = null;
	private ProcessableContext ctx = null;
	
	public BuildProcessorContextImpl(ApplicationFolderImpl folder, ApplicationContext applicationContext, 
			ProcessorLog log, Map<String,String> configs, ProcessableContext ctx) {
		super();
		this.folder = folder;
		this.log = log;
		this.applicationContext = applicationContext;
		this.configs = configs;
		this.ctx = ctx;
	}

	@Override
	public String getProperty(String name) {
		return configs.get(name);
		//return folder.getProperties().get(name);
	}
	@Override
	public void addSourceFile(SourceFile file) {
		ctx.addSourceFile(file);
	}
	@Override
	public SourceFile getSourceFile(String path) {
		return ctx.getSourceFile(path);
	}
	@Override
	public void setObject(String name, Object obj) {
		ctx.setObject(name, obj);
	}
	@Override
	public Object getObject(String name) {
		return ctx.getObject(name);
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
		ctx.addComponent(configs, component, folder);
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

