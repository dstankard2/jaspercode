package net.sf.jaspercode.eng.processing;

import java.util.Map;

import net.sf.jaspercode.api.ApplicationContext;
import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.Log;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.ApplicationResource;
import net.sf.jaspercode.api.resources.FileProcessor;
import net.sf.jaspercode.api.resources.FolderWatcher;
import net.sf.jaspercode.api.types.VariableType;
import net.sf.jaspercode.eng.files.ApplicationFolderImpl;
import net.sf.jaspercode.eng.files.ComponentFile;

public class ProcessorContextImpl implements ProcessorContext {

	private ProcessableContext ctx = null;
	private ApplicationContext appCtx = null;
	private ProcessorLog log = null;
	private ApplicationFolderImpl folder = null;
	private String lang = null;
	private Map<String,String> configs = null;
	private ComponentFile componentFile;
	
	public ProcessorContextImpl(ProcessableContext ctx, ApplicationContext appCtx, ProcessorLog log, 
			ApplicationFolderImpl folder, Map<String,String> configs,ComponentFile componentFile) {
		this.ctx = ctx;
		this.appCtx = appCtx;
		this.log = log;
		this.folder = folder;
		this.configs = configs;
		this.componentFile = componentFile;
	}

	@Override
	public void setLanguageSupport(String language) throws JasperException {
		lang = language;
	}

	@Override
	public void addSystemAttribute(String name, String type) throws JasperException {
		String t = ctx.getSystemAttribute(name);
		if ((t!=null) && (!t.equals(type))) {
			throw new JasperException("Tried to add system attribute "+name+" but it already exists as type '"+t+"'");
		}
		ctx.addSystemAttribute(name, type);
	}

	@Override
	public String getSystemAttribute(String name) {
		return ctx.getSystemAttribute(name);
	}

	@Override
	public void addVariableType(VariableType variableType) throws JasperException {
		if (variableType==null || variableType.getName()==null) {
			throw new JasperException("Tried to add a variable type that was null or had no name");
		}
		if (lang==null) {
			throw new JasperException("Couldn't get variable type because there is no selected language");
		}
		if (ctx.getType(lang, variableType.getName())!=null) {
			throw new JasperException("Added a type '"+variableType.getName()+"' but it already exists for language '"+lang+"'");
		}
		ctx.addVariableType(lang, variableType);
	}

	@Override
	public VariableType getVariableType(String name) throws JasperException {
		if (name.startsWith("list/")) name = "list";
		
		VariableType ret = ctx.getType(lang, name);

		return ret;
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
	public void addSourceFile(SourceFile file) {
		ctx.addSourceFile(file);
	}

	@Override
	public SourceFile getSourceFile(String path) {
		return ctx.getSourceFile(path);
	}

	@Override
	public String getProperty(String name) {
		return configs.get(name);
	}

	@Override
	public BuildContext getBuildContext() {
		return folder.getBuildContext();
	}

	@Override
	public ApplicationResource getResource(String path) {
		return folder.getResource(path);
	}

	@Override
	public void addComponent(Component component) {
		ctx.addComponent(componentFile, component);
	}

	@Override
	public Log getLog() {
		return log;
	}

	@Override
	public void addFolderWatcher(String folderPath, FolderWatcher folderWatcher) {
		ctx.addFolderWatcher(componentFile, folderPath, folderWatcher);
	}

	@Override
	public void addFileProcessor(String filePath, FileProcessor fileProcessor) {
		ctx.addFileProcessor(componentFile, filePath, fileProcessor);
	}

	@Override
	public ApplicationContext getApplicationContext() {
		return appCtx;
	}
	
}
