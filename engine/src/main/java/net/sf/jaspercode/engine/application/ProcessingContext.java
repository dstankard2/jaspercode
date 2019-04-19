package net.sf.jaspercode.engine.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.config.BuildComponent;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.ResourceWatcher;
import net.sf.jaspercode.api.types.VariableType;
import net.sf.jaspercode.engine.definitions.ComponentFile;
import net.sf.jaspercode.engine.exception.PreprocessingException;

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
		List<Integer> ids = processingManager.getObjectDependencies().get(key);
		if (ids==null) {
			ids = new ArrayList<>();
			processingManager.getObjectDependencies().put(key, ids);
		}
		ids.add(id);
	}
	public Object getObject(int id,String key) {
		return processingManager.getObjects().get(key);
	}
	public void setObject(int id,String key,Object value) {
		List<Integer> ids = processingManager.getObjectOriginators().get(key);
		if (ids==null) {
			ids = new ArrayList<>();
			processingManager.getObjectDependencies().put(key, ids);
		}
		ids.add(id);
		processingManager.getObjects().put(key, value);
	}

	// Originate items: system attributes, objects, variable types
	// Originate means that the item is saved in the processing manager
	public void originateSystemAttribute(int id,String name,String type) {
		processingManager.getAttributes().put(name, type);
		List<Integer> ids = processingManager.getAttributeOriginators().get(name);
		if (ids==null) {
			ids = new ArrayList<>();
			processingManager.getAttributeOriginators().put(name, ids);
		}
		ids.add(id);
	}
	public void originateObject(int id,String name,Object value) {
		processingManager.getObjects().put(name, value);
		List<Integer> ids = processingManager.getObjectOriginators().get(name);
		if (ids==null) {
			ids = new ArrayList<>();
			processingManager.getObjectOriginators().put(name, ids);
		}
		if (!ids.contains(id)) ids.add(id);
	}
	public void originateType(int id,String lang,VariableType variableType) {
		String name = variableType.getName();
		Map<String,List<Integer>> origs = processingManager.getTypeOriginators(lang);
		List<Integer> ids = origs.get(name);
		if (ids==null) {
			ids = new ArrayList<>();
			origs.put(name, ids);
		}
		if (!ids.contains(id)) ids.add(id);
		Map<String,VariableType> types = processingManager.getVariableTypes(lang);
		types.put(name, variableType);
	}

	public String getSystemAttribute(int id,String name) {
		return processingManager.getAttributes().get(name);
	}

	// Depend on items: system attributes, objects, variable types
	// Dependency means that the item is referenced
	public void dependOnSystemAttribute(int id,String name) {
		Map<String,List<Integer>> deps = processingManager.getAttributeDependencies();
		List<Integer> ids = deps.get(name);
		if (ids==null) {
			ids = new ArrayList<>();
			deps.put(name, ids);
		}
		if (!ids.contains(id)) ids.add(id);
	}
	public void dependOnType(int id,String lang,String typeName) {
		Map<String,List<Integer>> deps = processingManager.getTypeDependencies(lang);
		List<Integer> ids = deps.get(typeName);
		if (ids==null) {
			ids = new ArrayList<>();
			deps.put(typeName, ids);
		}
		if (!ids.contains(id)) ids.add(id);
	}
	public void dependOnObject(int id,String objectName) {
		Map<String,List<Integer>> deps = processingManager.getObjectDependencies();
		List<Integer> ids = deps.get(objectName);
		if (ids==null) {
			ids = new ArrayList<>();
			deps.put(objectName, ids);
		}
		if (!ids.contains(id)) ids.add(id);
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

	public void addComponent(int id, Component component, ComponentFile componentFile) throws JasperException,PreprocessingException {
		if (component instanceof BuildComponent) {
			throw new JasperException("JasperCode does not support dynamically added build components");
			//throw new IllegalArgumentException("JasperCode does not support dynamically added build components");
		}
		processingManager.addComponent(id, component, componentFile);
	}
	
	public void addResourceWatcher(int originatorId, ComponentFile componentFile,ResourceWatcher watcher) {
		processingManager.addResourceWatcher(originatorId, componentFile, watcher);
	}

}

