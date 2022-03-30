package net.sf.jaspercode.engine.processing;

import java.util.Map;

import net.sf.jaspercode.api.ApplicationContext;
import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.logging.Log;
import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.BuildProcessorContext;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.resources.ApplicationFolder;
import net.sf.jaspercode.engine.JasperResources;
import net.sf.jaspercode.engine.files.ApplicationFolderImpl;

// TODO: Create superclass for this and ProcessorContextImpl (ProcessorContextBase?)
public class BuildProcessorContextImpl implements BuildProcessorContext {

	private ApplicationFolderImpl folder = null;
	private ProcessorLog log = null;
	private JasperResources jasperResources = null;
	private Map<String,String> configs = null;
	private ProcessableChanges changes = null;
	private ProcessableContext ctx = null;
	
	public BuildProcessorContextImpl(ApplicationFolderImpl folder, JasperResources jasperResources, 
			ProcessorLog log, Map<String,String> configs, ProcessableContext ctx) {
		this.folder = folder;
		this.jasperResources = jasperResources;
		this.log = log;
		this.configs = configs;
		this.ctx = ctx;
	}

	@Override
	public String getProperty(String name) {
		return configs.get(name);
	}

	@Override
	public void addSourceFile(SourceFile file) {
		changes.getSourceFilesAdded().add(file);
	}

	@Override
	public SourceFile getSourceFile(String path) {
		SourceFile ret = null;
		
		ret = changes.getSourceFilesAdded().stream().filter(src -> path.equals(src.getPath())).findAny().orElse(null);
		if (ret==null) {
			ret = changes.getSourceFilesChanged().stream().filter(src -> path.equals(src.getPath())).findAny().orElse(null);
		}
		if (ret==null) {
			ret = ctx.getSourceFile(path);
			if (ret!=null) {
				changes.getSourceFilesChanged().add(ret);
			}
		}
		
		return ret;
	}

	@Override
	public void setObject(String name, Object obj) {
		changes.getObjects().put(name, obj);
	}
	@Override
	public Object getObject(String name) {
		Object ret = this.changes.getObjects().get(name);
		if (ret==null) {
			ret = ctx.getObject(name);
			if (ret!=null) {
				changes.getObjects().put(name, ret);
			}
		}
		return ret;
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
	public BuildContext getParentBuildContext() {
		if (folder.getParent()==null) {
			return null;
		} else {
			return folder.getParent().getBuildContext();
		}
	}

	@Override
	public ApplicationContext getApplicationContext() {
		return jasperResources;
	}
	
	public void setChanges(ProcessableChanges changes) {
		this.changes = changes;
	}

	@Override
	public void addComponent(Component component) {
		this.changes.getComponentsAdded().add(component);
	}

}

