package net.sf.jaspercode.engine.impl;

import java.util.Map;

import net.sf.jaspercode.api.ApplicationContext;
import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.Log;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.resources.ApplicationResource;
import net.sf.jaspercode.api.resources.ResourceWatcher;
import net.sf.jaspercode.api.types.VariableType;
import net.sf.jaspercode.engine.processing.ProcessorContainerBase;
import net.sf.jaspercode.engine.definitions.ComponentFile;
import net.sf.jaspercode.engine.processing.ComponentContainer;

public class ProcessorContextImpl implements ProcessorContext {

	ProcessorContainerBase entry = null;
	ComponentContainer mgr = null;
	String lang = null;
	ApplicationContextImpl applicationContext = null;

	public ProcessorContextImpl(ProcessorContainerBase entry,ComponentContainer mgr, ApplicationContextImpl applicationContext) {
		this.entry = entry;
		this.mgr = mgr;
		this.applicationContext = applicationContext;
	}

	@Override
	public void originateSystemAttribute(String name) {
		entry.originateAttribute(name);
	}

	@Override
	public void dependOnSystemAttribute(String name) {
		entry.dependOnSystemAttribute(name);
	}

	@Override
	public String getSystemAttribute(String name) {
		return mgr.getSystemAttributes().get(name);
	}

	@Override
	public void originateVariableType(String typeName) {
		entry.originateVariableType(typeName);
	}

	@Override
	public void dependOnVariableType(String typeName) {
		try {
			VariableType type = getVariableType(typeName);
			if (type!=null) {
				entry.dependOnVariableType(typeName, type);
			}
		} catch(JasperException e) { }
	} 

	@Override
	public void originateObject(String name) {
		entry.originateObject(name);
	}

	@Override
	public void dependOnObject(String name) {
		entry.dependOnObject(name);
	}

	@Override
	public void originateSourceFile(SourceFile file) {
		entry.originateSourceFile(file.getPath());
	}

	@Override
	public void addSourceFile(SourceFile file) {
		entry.originateSourceFile(file.getPath());
		mgr.addSourceFile(file);
	}

	@Override
	public VariableType getVariableType(String name) throws JasperException {
		if (lang==null) {
			throw new JasperException("Cannot get variable type unless a language is specified");
		}
		if (name==null) throw new JasperException("Cannot get variable type null");
		if (name.indexOf("list/")==0) {
			return getVariableType("list");
		}
		VariableType ret = mgr.getVariableTypes(lang).get(name);
		return ret;
	}

	@Override
	public ApplicationResource getResource(String path) {
		entry.dependOnResource(path);
		return entry.getFolder().getResource(path);
	}

	@Override
	public boolean addSystemAttribute(String name, String type) {
		Map<String,String> attributes = mgr.getSystemAttributes();
		
		if (attributes.get(name)==null) {
			attributes.put(name, type);
		} else {
			if (!attributes.get(name).equals(type)) {
				return false;
			}
		}
		
		entry.originateAttribute(name);
		entry.dependOnSystemAttribute(name);
		return true;
	}

	@Override
	public String getProperty(String name) {
		return this.entry.getConfiguration().get(name);
	}

	@Override
	public void setObject(String name, Object obj) {
		mgr.getObjects().put(name, obj);
		this.originateObject(name);
	}

	@Override
	public Object getObject(String name) {
		entry.dependOnObject(name);
		return mgr.getObjects().get(name);
	}

	@Override
	public SourceFile getSourceFile(String path) {
		return mgr.getSourceFile(path);
	}

	@Override
	public void addVariableType(VariableType variableType) throws JasperException {
		if (lang==null) {
			throw new JasperException("Cannot add a variable type when a language has not been selected");
		}
		entry.originateVariableType(variableType.getName());
		mgr.getVariableTypes(lang).put(variableType.getName(), variableType);
	}

	@Override
	public BuildContext getBuildContext() {
		return entry.getFolder().getBuildContext(mgr);
	}

	@Override
	public void addComponent(Component component) {
		ComponentFile f = this.entry.getComponentFile();
		if (f!=null) {
			mgr.addComponent(component, f);
		}
	}
	
	@Override
	public Log getLog() {
		return entry.getLog();
	}
	
	@Override
	public void setLanguageSupport(String lang) throws JasperException {
		if (mgr.getVariableTypes(lang)==null) {
			throw new JasperException("Found unrecognized language '"+lang+"'");
		}
		this.lang = lang;
	}

	@Override
	public void addResourceWatcher(ResourceWatcher watcher, String path) {
		entry.addResourceWatcher(watcher,path);
	}

	@Override
	public void dependOnResource(String path) {
		entry.dependOnResource(path);
	}
	
	@Override
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

}

