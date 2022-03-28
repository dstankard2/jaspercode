package net.sf.jaspercode.engine.processing;

import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import net.sf.jaspercode.api.ApplicationContext;
import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.logging.Log;
import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.ApplicationResource;
import net.sf.jaspercode.api.resources.FileProcessor;
import net.sf.jaspercode.api.resources.FolderWatcher;
import net.sf.jaspercode.api.types.VariableType;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.engine.JasperResources;
import net.sf.jaspercode.engine.files.ApplicationFolderImpl;

public class ProcessorContextImpl implements ProcessorContext {

	private ProcessableContext ctx = null;
	private JasperResources jasperResources = null;
	private ProcessorLog log = null;
	private ApplicationFolderImpl folder = null;
	private String lang = null;
	private Map<String,String> configs = null;
	private ProcessableChanges changes = null;
	
	public ProcessorContextImpl(ProcessableContext ctx, JasperResources jasperResources, ProcessorLog log, 
			Map<String,String> configs, ApplicationFolderImpl folder,ProcessableChanges changes) {
		this.ctx = ctx;
		this.jasperResources = jasperResources;
		this.log = log;
		this.configs = configs;
		this.folder = folder;
		this.changes = changes;
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
		changes.getAttributesOriginated().add(name);
		if (t==null) {
			changes.getAttributesAdded().put(name, type);
		}
	}

	@Override
	public String getSystemAttribute(String name) {
		changes.getAttributeDependencies().add(name);
		return ctx.getSystemAttribute(name);
	}

	@Override
	public void addVariableType(VariableType variableType) {
		if (variableType==null || variableType.getName()==null) {
			throw new RuntimeException("Tried to add a variable type that was null or had no name");
		}
		if (lang==null) {
			throw new RuntimeException("Couldn't get variable type because there is no selected language");
		}
		if (ctx.getType(lang, variableType.getName())!=null) {
			throw new RuntimeException("Added a type '"+variableType.getName()+"' but it already exists for language '"+lang+"'");
		}
		changes.getTypesModified().add(Pair.of(lang, variableType));
	}

	@Override
	public VariableType getVariableType(String name) {
		if (name.startsWith("list/")) return getVariableType("list");
		VariableType ret = null;
		
		ret = changes.getTypesModified().stream().filter(p -> p.getKey().equals(lang) && p.getValue().getName().equals(name)).findAny().map(Pair::getRight).orElse(null);

		if (ret==null) {
			ret = ctx.getType(lang, name);
			if (ret!=null) {
				changes.getTypeDependencies().add(Pair.of(lang, ret));
			}
		}

		return ret;
	}
	
	@Override
	public void modifyVariableType(VariableType type) {
		VariableType existing = changes.getTypesModified().stream().filter(p -> p.getLeft().equals(lang) && p.getRight()==type).findAny().map(Pair::getRight).orElse(null);
		if (existing==null) {
			ctx.modifyType(lang, type);
			changes.getTypesModified().add(Pair.of(lang, type));
		} 
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
	public void addSourceFile(SourceFile file) {
		changes.getSourceFiles().add(file);
	}

	@Override
	public SourceFile getSourceFile(String path) {
		SourceFile ret = null;
		
		ret = changes.getSourceFiles().stream().filter(src -> path.equals(src.getPath())).findAny().orElse(null);
		if (ret==null) {
			ret = ctx.getSourceFile(path);
			if (ret!=null) {
				changes.getSourceFilesChanged().add(ret);
			}
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

	// The added component will have the same configuration properties
	@Override
	public void addComponent(Component component) {
		changes.getComponentsAdded().add(component);
	}

	@Override
	public Log getLog() {
		return log;
	}

	// The added folder watcher will have the same configuration properties
	@Override
	public void addFolderWatcher(String folderPath, FolderWatcher folderWatcher) {
		changes.getFolderWatchersAdded().add(Pair.of(folderPath, folderWatcher));
	}

	// The added file watcher will have the same configuration properties
	@Override
	public void addFileProcessor(String filePath, FileProcessor fileProcessor) {
		changes.getFileProcessorsAdded().add(Pair.of(filePath, fileProcessor));
	}

	@Override
	public ApplicationContext getApplicationContext() {
		return jasperResources;
	}
	
	@Override
	public void originateSystemAttribute(String name) {
		changes.getAttributesOriginated().add(name);
	}

}
