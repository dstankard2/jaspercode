package net.sf.jaspercode.engine.application;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.config.BuildComponent;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.FileProcessor;
import net.sf.jaspercode.api.resources.FileWatcher;
import net.sf.jaspercode.api.resources.FolderWatcher;
import net.sf.jaspercode.api.types.VariableType;
import net.sf.jaspercode.engine.definitions.ComponentFile;
import net.sf.jaspercode.engine.definitions.UserFile;
import net.sf.jaspercode.engine.processing.ProcessingDataManager;
import net.sf.jaspercode.engine.processing.ProcessingManager;

/**
 * Manipulates properties of the processingManager in a way that makes sense for the processable.<br/>
 * This is called by a processable to manipulate sourceFiles, attributes, types and other engine 
 * resources.
 * @author DCS
 *
 */
public class ProcessingContext {

	protected ProcessingManager processingManager = null;
	protected ProcessingDataManager processingDataManager = null;

	public ProcessingContext(ProcessingManager processingManager, ProcessingDataManager processingDataManager) {
		super();
		this.processingDataManager = processingDataManager;
		this.processingManager = processingManager;
	}
	
	public UserFile getUserFile(String path) {
		return processingDataManager.getUserFile(path);
	}

	public void objectDependency(int id,String key) {
		Set<Integer> ids = processingDataManager.getObjectOriginators().get(key);
		if (ids==null) {
			ids = new HashSet<>();
			processingDataManager.getObjectOriginators().put(key, ids);
		}
		if (!ids.contains(id))
			ids.add(id);
	}
	public Object getObject(int id,String key) {
		return processingDataManager.getObjects().get(key);
	}
	public void setObject(int id,String key,Object value) {
		Set<Integer> ids = processingDataManager.getObjectOriginators().get(key);
		if (ids==null) {
			ids = new HashSet<>();
			processingDataManager.getObjectOriginators().put(key, ids);
		}
		if (!ids.contains(id))
			ids.add(id);
		processingDataManager.getObjects().put(key, value);
	}

	// Originate items: system attributes, objects, variable types
	// Originate means that the item is saved in the processing manager
	public void originateSystemAttribute(int id,String name,String type) {
		processingDataManager.getSystemAttributes().put(name, type);
		Set<Integer> ids = processingDataManager.getSystemAttributeOriginators().get(name);
		if (ids==null) {
			ids = new HashSet<>();
			processingDataManager.getSystemAttributeOriginators().put(name, ids);
		}
		if (!ids.contains(id))
			ids.add(id);
	}
	public void originateObject(int id,String name,Object value) {
		processingDataManager.getObjects().put(name, value);
		Set<Integer> ids = processingDataManager.getObjectOriginators().get(name);
		if (ids==null) {
			ids = new HashSet<>();
			processingDataManager.getObjectOriginators().put(name, ids);
		}
		if (!ids.contains(id))
			ids.add(id);
	}
	public void originateObject(int id,String name) {
		Set<Integer> ids = processingDataManager.getObjectOriginators().get(name);
		if (ids==null) {
			ids = new HashSet<>();
			processingDataManager.getObjectOriginators().put(name, ids);
		}
		if (!ids.contains(id))
			ids.add(id);
	}
	public void originateType(int id,String lang,VariableType variableType) {
		String name = variableType.getName();
		Map<String,Set<Integer>> origs = processingDataManager.getVariableTypeOriginators(lang);
		Set<Integer> ids = origs.get(name);
		if (ids==null) {
			ids = new HashSet<>();
			origs.put(name, ids);
		}
		if (!ids.contains(id))
			ids.add(id);
		Map<String,VariableType> types = processingDataManager.getVariableTypes(lang);
		if (types.get(name)!=null) {
			if (types.get(name)!=variableType) {
				System.err.println("Invalid variable type for lang "+lang+" and type "+variableType.getName()+" originated from id "+id);
			}
		} else {
			types.put(name, variableType);
		}
	}

	public String getSystemAttribute(String name) {
		return processingDataManager.getSystemAttributes().get(name);
	}

	// Depend on items: system attributes, objects, variable types
	// Dependency means that the item is referenced
	public void dependOnSystemAttribute(int id,String name) {
		Map<String,Set<Integer>> deps = processingDataManager.getSystemAttributeDependencies();
		Set<Integer> ids = deps.get(name);
		if (ids==null) {
			ids = new HashSet<>();
			deps.put(name, ids);
		}
		if (!ids.contains(id))
			ids.add(id);
	}
	public void dependOnType(int id,String lang,String typeName) {
		Map<String,Set<Integer>> deps = processingDataManager.getVariableTypeDependencies(lang);
		Set<Integer> ids = deps.get(typeName);
		if (ids==null) {
			ids = new HashSet<>();
			deps.put(typeName, ids);
		}
		if (!ids.contains(id))
			ids.add(id);
	}

	public SourceFile getSourceFile(String path) {
		return processingDataManager.getSourceFile(path);
	}
	
	public void saveSourceFile(int id,SourceFile src) {
		processingDataManager.addSourceFile(id,src);
	}

	public VariableType getVariableType(String lang,String name) {
		return processingDataManager.getVariableTypes(lang).get(name);
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

	public void addFileProcessor(int originatorId, ComponentFile componentFile,String path,FileProcessor watcher) {
		processingManager.addFileProcessor(originatorId, componentFile, path, watcher);
	}

	public void addFileWatcher(int originatorId, ComponentFile componentFile,String path,FileWatcher watcher) {
		//processingDataManager.addFileWatcher(originatorId, componentFile, path, watcher);
	}

}

