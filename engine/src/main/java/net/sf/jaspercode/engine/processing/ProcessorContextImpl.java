package net.sf.jaspercode.engine.processing;

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
import net.sf.jaspercode.engine.application.JasperResources;
import net.sf.jaspercode.engine.definitions.ApplicationFolderImpl;

public class ProcessorContextImpl implements ProcessorContext {

	private ProcessableContext ctx = null;
	private String language = null;
	private ApplicationFolderImpl folder = null;
	private ProcessorLog log = null;
	private JasperResources jasperResources = null;
	
	public ProcessorContextImpl(ProcessableContext ctx,ApplicationFolderImpl folder,ProcessorLog log,JasperResources jasperResources) {
		this.ctx = ctx;
		this.folder = folder;
		this.log = log;
		this.jasperResources = jasperResources;
	}
	
	@Override
	public void setLanguageSupport(String language) throws JasperException {
		this.language = language;
	}

	@Override
	public boolean addSystemAttribute(String name, String type) throws JasperException {
		ctx.addSystemAttribute(name, type);
		ctx.originateSystemAttribute(name);
		return true;
	}

	@Override
	public void originateSystemAttribute(String name) {
		ctx.originateSystemAttribute(name);
	}

	@Override
	public void dependOnSystemAttribute(String name) {
		ctx.dependOnSystemAttribute(name);
	}

	@Override
	public String getSystemAttribute(String name) {
		return ctx.getSystemAttribute(name);
	}

	@Override
	public void addVariableType(VariableType variableType) throws JasperException {
		if (language==null) {
			throw new JasperException("Unable to add variable type "+variableType.getName()+" as there is no currently selected language");
		}
		ctx.addVariableType(language, variableType);
	}

	@Override
	public void originateVariableType(VariableType variableType) throws JasperException {
		ctx.originateType(language, variableType);
	}

	@Override
	public void dependOnVariableType(VariableType variableType) {
		ctx.dependOnType(language, variableType.getName(), folder.getBuildContext());
	}

	@Override
	public VariableType getVariableType(String name) throws JasperException {
		if (language==null) throw new JasperException("Cannot get variable type unless a language is specified");
		if (name==null) throw new JasperException("Cannot get variable type null");
		
		if (name.indexOf("list/")==0) {
			return getVariableType("list");
		}
		return ctx.getVariableType(language, name);
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
		return ctx.getConfigurationProperty(name);
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
		ctx.addComponent(component);
	}

	@Override
	public Log getLog() {
		return log;
	}

	@Override
	public void addFolderWatcher(String folderPath, FolderWatcher folderWatcher) {
		ctx.addFolderWatcher(folderPath, folderWatcher);
	}

	public void addFileProcessor(String filePath,FileProcessor fileProcessor) {
		ctx.addFileProcessor(filePath, fileProcessor);
	}

	@Override
	public ApplicationContext getApplicationContext() {
		return this.jasperResources;
	}

}
