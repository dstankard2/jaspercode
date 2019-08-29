package net.sf.jaspercode.engine.application;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.config.BuildComponent;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.FileWatcher;
import net.sf.jaspercode.api.resources.FolderWatcher;
import net.sf.jaspercode.api.types.VariableType;
import net.sf.jaspercode.engine.definitions.ComponentFile;

/**
 * Manipulates properties of the processingManager in a way that makes sense for the processable.<br/>
 * This is called by a processable to manipulate sourceFiles, attributes, types and other engine 
 * resources.
 * @author DCS
 *
 */
public class ProcessingContext {

	private ProcessingManager processingManager = null;

	public ProcessingContext(ProcessingManager processingManager) {
		super();
		this.processingManager = processingManager;
	}

	public void objectDependency(int id,String key) {
		Set<Integer> ids = processingManager.getObjectOriginators().get(key);
		if (ids==null) {
			ids = new HashSet<>();
			processingManager.getObjectOriginators().put(key, ids);
		}
		if (!ids.contains(id))
			ids.add(id);
	}
	public Object getObject(int id,String key) {
		return processingManager.getObjects().get(key);
	}
	public void setObject(int id,String key,Object value) {
		Set<Integer> ids = processingManager.getObjectOriginators().get(key);
		if (ids==null) {
			ids = new HashSet<>();
			processingManager.getObjectOriginators().put(key, ids);
		}
		if (!ids.contains(id))
			ids.add(id);
		processingManager.getObjects().put(key, value);
	}

	// Originate items: system attributes, objects, variable types
	// Originate means that the item is saved in the processing manager
	public void originateSystemAttribute(int id,String name,String type) {
		processingManager.getSystemAttributes().put(name, type);
		Set<Integer> ids = processingManager.getSystemAttributeOriginators().get(name);
		if (ids==null) {
			ids = new HashSet<>();
			processingManager.getSystemAttributeOriginators().put(name, ids);
		}
		if (!ids.contains(id))
			ids.add(id);
	}
	public void originateObject(int id,String name,Object value) {
		processingManager.getObjects().put(name, value);
		Set<Integer> ids = processingManager.getObjectOriginators().get(name);
		if (ids==null) {
			ids = new HashSet<>();
			processingManager.getObjectOriginators().put(name, ids);
		}
		if (!ids.contains(id))
			ids.add(id);
	}
	public void originateObject(int id,String name) {
		Set<Integer> ids = processingManager.getObjectOriginators().get(name);
		if (ids==null) {
			ids = new HashSet<>();
			processingManager.getObjectOriginators().put(name, ids);
		}
		if (!ids.contains(id))
			ids.add(id);
	}
	public void originateType(int id,String lang,VariableType variableType) {
		String name = variableType.getName();
		Map<String,Set<Integer>> origs = processingManager.getVariableTypeOriginators(lang);
		Set<Integer> ids = origs.get(name);
		if (ids==null) {
			ids = new HashSet<>();
			origs.put(name, ids);
		}
		if (!ids.contains(id))
			ids.add(id);
		Map<String,VariableType> types = processingManager.getVariableTypes(lang);
		types.put(name, variableType);
	}

	public String getSystemAttribute(int id,String name) {
		return processingManager.getSystemAttributes().get(name);
	}

	// Depend on items: system attributes, objects, variable types
	// Dependency means that the item is referenced
	public void dependOnSystemAttribute(int id,String name) {
		Map<String,Set<Integer>> deps = processingManager.getSystemAttributeDependencies();
		Set<Integer> ids = deps.get(name);
		if (ids==null) {
			ids = new HashSet<>();
			deps.put(name, ids);
		}
		if (!ids.contains(id))
			ids.add(id);
	}
	public void dependOnType(int id,String lang,String typeName) {
		Map<String,Set<Integer>> deps = processingManager.getVariableTypeDependencies(lang);
		Set<Integer> ids = deps.get(typeName);
		if (ids==null) {
			ids = new HashSet<>();
			deps.put(typeName, ids);
		}
		if (!ids.contains(id))
			ids.add(id);
	}

	public SourceFile getSourceFile(String path) {
		return processingManager.getSourceFile(path);
	}
	
	public void saveSourceFile(int id,SourceFile src) {
		processingManager.addSourceFile(id,src);
	}

	public VariableType getVariableType(String lang,String name) {
		return processingManager.getVariableTypes(lang).get(name);
	}

	public void addComponent(int id, Component component, ComponentFile componentFile) throws JasperException {
		if (component instanceof BuildComponent) {
			throw new JasperException("JasperCode does not support dynamically added build components");
			//throw new IllegalArgumentException("JasperCode does not support dynamically added build components");
		}
		processingManager.addComponent(id, component, componentFile);
	}
	
	public void addFolderWatcher(int originatorId, ComponentFile componentFile,String path,FolderWatcher watcher) {
		processingManager.addFolderWatcher(originatorId, componentFile, path, watcher);
	}

	public void addFileWatcher(int originatorId, ComponentFile componentFile,String path,FileWatcher watcher) {
		processingManager.addFileWatcher(originatorId, componentFile, path, watcher);
	}

}

