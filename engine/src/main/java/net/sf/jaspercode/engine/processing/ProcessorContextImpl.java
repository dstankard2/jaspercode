package net.sf.jaspercode.engine.processing;

import net.sf.jaspercode.api.ApplicationContext;
import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.Log;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.ApplicationResource;
import net.sf.jaspercode.api.resources.ResourceWatcher;
import net.sf.jaspercode.api.types.ListType;
import net.sf.jaspercode.api.types.VariableType;
import net.sf.jaspercode.engine.definitions.ApplicationFolderImpl;

public class ProcessorContextImpl implements ProcessorContext {
	ProcessableContext ctx =  null;

	// Language currently selected
	String lang = null;

	ApplicationFolderImpl folder = null;
	ProcessorLog log = null;

	public ProcessorContextImpl(ApplicationFolderImpl folder,ProcessableContext ctx,ProcessorLog log) {
		this.folder = folder;
		this.ctx = ctx;
		this.log = log;
	}
	
	@Override
	public void setLanguageSupport(String language) throws JasperException {
		this.lang = language;
	}

	@Override
	public boolean addSystemAttribute(String name, String type) {
		ctx.addSystemAttribute(name, type);
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
		ctx.originateType(lang, variableType);
	}

	@Override
	public void originateVariableType(VariableType variableType) {
		ctx.originateType(lang,  variableType);
	}

	@Override
	public void dependOnVariableType(VariableType variableType) {
		if (getBuildContext()==null) {
			System.out.println("Found processor ctx with no build context");
		}
		ctx.dependOnType(lang, variableType.getName(),variableType.getBuildContext());
	}

	@Override
	public VariableType getVariableType(String name) throws JasperException {
		VariableType ret = null;
		
		if (name.startsWith("list/")) {
			ListType listType = (ListType)ctx.getVariableType(lang, "list");
			String elementTypeName = name.substring(5);
			VariableType eltType = ctx.getVariableType(lang, elementTypeName);
			ret = listType.getListTypeWithElementTypName(eltType);
		} else {
			ret = ctx.getVariableType(lang, name);
		}
		
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
	public void dependOnResource(String path) {
		//  TODO: Implement
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
	public void addResourceWatcher(ResourceWatcher watcher) {
		ctx.addResourceWatcher(watcher);
	}

	@Override
	public ApplicationContext getApplicationContext() {
		return ctx.getApplicationContext();
	}

}
