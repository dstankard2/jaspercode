package net.sf.jaspercode.eng.processing;

import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

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

public class ProcessorContextImpl implements ProcessorContext {

	private ProcessableChanges changes = null;
	private ProcessableContext ctx = null;
	private ApplicationContext appCtx = null;
	private ProcessorLog log = null;
	private ApplicationFolderImpl folder = null;
	private String lang = null;
	private Map<String,String> configs = null;
	
	public ProcessorContextImpl(int itemId, ProcessableContext ctx, ApplicationContext appCtx, ProcessorLog log, 
			ApplicationFolderImpl folder, Map<String,String> configs, ProcessableChanges changes) {
		this.changes = changes;
		this.ctx = ctx;
		this.appCtx = appCtx;
		this.log = log;
		this.folder = folder;
		this.configs = configs;
	}

	public ProcessableChanges getChanges() {
		return changes;
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
		changes.attributesAdded.put(name, type);
	}

	@Override
	public String getSystemAttribute(String name) {
		String ret = changes.attributesAdded.get(name);
		if (ret==null) ret = ctx.getSystemAttribute(name);
		return ret;
	}

	@Override
	public void addVariableType(VariableType variableType) throws JasperException {
		changes.getTypesAdded().add(Pair.of(lang, variableType));
	}

	protected VariableType findAddedType(String name) {
		for(Pair<String,VariableType> p : changes.getTypesAdded()) {
			if (!p.getKey().equals(lang)) continue;
			if (p.getRight().getName().equals(name)) return p.getRight();
		}
		return null;
	}

	@Override
	public VariableType getVariableType(String name) throws JasperException {
		
		if (name.startsWith("list/")) name = "list";
		
		VariableType ret = findAddedType(name);
		if (ret==null) {
			ret = ctx.getType(lang, name);
		}
		if (ret!=null) {
			changes.getTypeDependencies().add(Pair.of(lang, ret));
		}

		return ret;
	}

	/*
	@Override
	public VariableType getVariableType(String name) throws JasperException {
		if (language==null) throw new JasperException("Cannot get variable type unless a language is specified");
		if (name==null) throw new JasperException("Cannot get variable type null");
		
		if (name.indexOf("list/")==0) {
			return getVariableType("list");
		}
		return ctx.getVariableType(language, name);
	}
	*/
	
	@Override
	public void setObject(String name, Object obj) {
		changes.getObjects().put(name, obj);
	}

	@Override
	public Object getObject(String name) {
		Object value = changes.getObjects().get(name);
		if (value==null) value = ctx.getObject(name);
		changes.getObjectDeps().add(name);
		return value;
	}

	@Override
	public void addSourceFile(SourceFile file) {
		changes.getSourceFiles().add(file);
	}

	@Override
	public SourceFile getSourceFile(String path) {
		SourceFile ret = null;
		
		for(SourceFile src : changes.getSourceFiles()) {
			if (src.getPath().equals(path)) {
				ret = src;
				break;
			}
		}
		if (ret==null) {
			ret = ctx.getSourceFile(path);
		}
		
		return ret;
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
		changes.componentsAdded.add(component);
	}

	@Override
	public Log getLog() {
		return log;
	}

	@Override
	public void addFolderWatcher(String folderPath, FolderWatcher folderWatcher) {
		changes.folderWatchersAdded.add(Pair.of(folderPath, folderWatcher));
	}

	@Override
	public void addFileProcessor(String filePath, FileProcessor fileProcessor) {
		changes.fileProcessorsAdded.add(Pair.of(filePath, fileProcessor));
	}

	@Override
	public ApplicationContext getApplicationContext() {
		return appCtx;
	}
	
}
