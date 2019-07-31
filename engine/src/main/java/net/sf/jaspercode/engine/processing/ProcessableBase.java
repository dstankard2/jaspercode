package net.sf.jaspercode.engine.processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;

import net.sf.jaspercode.api.ApplicationContext;
import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.plugin.ProcessorLogMessage;
import net.sf.jaspercode.api.resources.FileWatcher;
import net.sf.jaspercode.api.resources.FolderWatcher;
import net.sf.jaspercode.api.types.VariableType;
import net.sf.jaspercode.engine.application.ProcessingContext;
import net.sf.jaspercode.engine.definitions.ApplicationFolderImpl;
import net.sf.jaspercode.engine.definitions.ComponentFile;

/**
 * This Processable is the processableContext for the underlying processor
 * @author DCS
 *
 */
public abstract class ProcessableBase extends ConfigurableProcessable implements ProcessableContext {

	protected ApplicationContext applicationContext = null;
	//protected ApplicationFolderImpl folder = null;
	protected ProcessorContextImpl processorContext = null;
	protected ProcessingContext processingContext = null;
	private ComponentFile componentFile = null;
	protected int id = 0;

	protected Map<String,Object> objects = new HashMap<>();
	protected List<String> objectDependencies = new ArrayList<>();
	
	protected Map<String,String> attributesOriginated = new HashMap<>();
	protected List<String> attributeDependencies = new ArrayList<>();

	protected List<Pair<String,String>> typeDependencies = new ArrayList<>();
	protected List<Pair<String,VariableType>> typesOriginated = new ArrayList<>();
	
	protected List<SourceFile> sourceFiles = new ArrayList<>();

	protected Map<String,String> configOverride = null;
	
	protected List<Pair<String,FolderWatcher>> folderWatchersAdded = new ArrayList<>();
	protected List<Pair<String,FileWatcher>> fileWatchersAdded = new ArrayList<>();
	protected List<Component> componentsAdded = new ArrayList<>();

	protected String name = null;
	
	protected ProcessingState state = null;
	
	public ProcessableBase(ApplicationContext applicationContext,ComponentFile componentFile,ProcessingContext processingContext,int id,String name,Map<String,String> configOverride) {
		this.applicationContext = applicationContext;
		this.componentFile = componentFile;
		this.name = name;
		this.processingContext = processingContext;
		this.id = id;
		this.configOverride = configOverride;
	}
	
	protected String getProperty(String name) {
		if (configOverride.get(name)!=null) return configOverride.get(name);
		return componentFile.getFolder().getProperties().get(name);
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public List<ProcessorLogMessage> getMessages() {
		return log.getMessages(false);
	}

	// Compares priority of components, for sorting
	@Override
	public int compareTo(Processable o) {
		if (o==null) return -1;

		int p = this.getPriority();
		int op = o.getPriority();
		if (p>op) return 1;
		else if (p==op) return 0;
		else return -1;
	}
	
	@Override
	public ApplicationFolderImpl getFolder() {
		return componentFile.getFolder();
	}

	@Override
	public abstract int getPriority();
	
	@Override
	public abstract boolean process();

	@Override
	public void rollbackChanges() {
		this.typesOriginated.clear();
	}

	@Override
	public boolean commitChanges() {
		// Added components
		for(Component comp : this.componentsAdded) {
			try {
				processingContext.addComponent(id, comp, componentFile);
			} catch(JasperException e) {
				log.error(e.getMessage(), e.getCause());
				return false;
			}
		}
		componentsAdded.clear();
		
		// Objects
		// Commit dependencies to processingContext
		for(String obj : objectDependencies) {
			processingContext.originateObject(id, obj);
		}
		// Don't clear these
		objectDependencies.clear();

		// Commit object updates to processingContext
		for(Entry<String,Object> ob : objects.entrySet()) {
			processingContext.setObject(id, ob.getKey(), ob.getValue());
		}
		objects.clear();
		
		// System attributes
		// Commit attributes to processingContext
		for(Entry<String,String> attr : attributesOriginated.entrySet()) {
			processingContext.originateSystemAttribute(id, attr.getKey(), attr.getValue());
		}
		attributesOriginated.clear();
		
		// Commit dependencies to processingContext
		for(String attr : attributeDependencies) {
			processingContext.dependOnSystemAttribute(id, attr);
		}
		attributeDependencies.clear();

		// Variable Types
		// Commit Dependencies to processingContext
		for(Pair<String,String> dep : typeDependencies) {
 			String lang = dep.getLeft();
			String typeName = dep.getRight();
			processingContext.dependOnType(id, lang, typeName);
		}
		typeDependencies.clear();
		
		// Commit variable type changes to processingContext
		for(Pair<String,VariableType> p : this.typesOriginated) {
			processingContext.originateType(id, p.getLeft(), p.getRight());
		}
		this.typesOriginated.clear();
		
		// Source Files
		for(SourceFile src : sourceFiles) {
			processingContext.saveSourceFile(id, src);
		}
		sourceFiles.clear();
		
		// Added Folder Watchers
		for(Pair<String,FolderWatcher> w : this.folderWatchersAdded) {
			processingContext.addFolderWatcher(id, componentFile, w.getKey(), w.getRight());
		}
		folderWatchersAdded.clear();
		
		// Added File Watchers
		for(Pair<String,FileWatcher> w : this.fileWatchersAdded) {
			processingContext.addFileWatcher(id, componentFile, w.getKey(), w.getRight());
		}
		fileWatchersAdded.clear();
		
		return true;
	}

	/* Implementation of ProcessableContext */
	
	@Override
	public void addSourceFile(SourceFile sourceFile) {
		sourceFiles.add(sourceFile);
	}

	@Override
	public SourceFile getSourceFile(String path) {
		SourceFile ret = null;
		for(SourceFile src : sourceFiles) {
			if (src.getPath().equals(path)) {
				ret = src;
				break;
			}
		}
		if (ret==null) {
			ret = processingContext.getSourceFile(path);
			if (ret!=null) {
				sourceFiles.add(ret);
			}
		}
		return ret;
	}

	@Override
	public void setObject(String name, Object value) {
		objects.put(name, value);
		objectDependencies.add(name);
	}

	@Override
	public Object getObject(String objectName) {
		Object ret = objects.get(objectName);
		if (ret==null) {
			ret = processingContext.getObject(id, objectName);
		}
		objectDependencies.add(objectName);
		return ret;
	}

	@Override
	public void addSystemAttribute(String name, String type) {
		this.attributesOriginated.put(name, type);
	}

	@Override
	public void originateSystemAttribute(String name) {
		this.attributesOriginated.put(name, this.getSystemAttribute(name));
	}

	@Override
	public void dependOnSystemAttribute(String name) {
		this.attributeDependencies.add(name);
	}

	@Override
	public String getSystemAttribute(String name) {
		String ret = this.attributesOriginated.get(name);
		if (ret==null) {
			ret = this.processingContext.getSystemAttribute(id, name);
		}
		this.attributeDependencies.add(name);
		return ret;
	}

	@Override
	public void originateType(String lang, VariableType variableType) {
		this.typesOriginated.add(Pair.of(lang, variableType));
	}

	@Override
	public void dependOnType(String lang, String name,BuildContext buildCtx) {
		this.typeDependencies.add(Pair.of(lang, name));
		if ((buildCtx!=null) && (this.componentFile.getFolder().getBuildContext() != buildCtx)) {
			this.componentFile.getFolder().getBuildContext().addDependency(buildCtx);
		}
	}

	@Override
	public VariableType getVariableType(String language, String typeName) {
		for(Pair<String,VariableType> type : this.typesOriginated) {
			if ((type.getLeft().equals(language)) && (type.getRight().getName().equals(typeName))) {
				return type.getRight();
			}
		}
		return processingContext.getVariableType(language, typeName);
	}

	@Override
	public String getConfigurationProperty(String name) {
		String ret = this.configOverride.get(name);
		if (ret==null) {
			ret = componentFile.getFolder().getProperties().get(name);
		}
		return ret;
	}

	@Override
	public void addFolderWatcher(String path,FolderWatcher folderWatcher) {
		this.folderWatchersAdded.add(Pair.of(path, folderWatcher));
	}

	@Override
	public void addFileWatcher(String path,FileWatcher fileWatcher) {
		this.fileWatchersAdded.add(Pair.of(path, fileWatcher));
	}

	@Override
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	@Override
	public ProcessingState getState() {
		return state;
	}

	@Override
	public void addComponent(Component component) {
		this.componentsAdded.add(component);
	}

}

