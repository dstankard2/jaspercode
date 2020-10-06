package net.sf.jaspercode.engine.processing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.langsupport.LanguageSupport;
import net.sf.jaspercode.api.types.VariableType;
import net.sf.jaspercode.engine.EngineLanguages;
import net.sf.jaspercode.engine.application.ApplicationManager;
import net.sf.jaspercode.engine.application.DependencyManager;
import net.sf.jaspercode.engine.application.JasperResources;
import net.sf.jaspercode.engine.definitions.UserFile;

public class ProcessingDataManager {

	protected ApplicationManager applicationManager = null;
	protected DependencyManager dependencyManager = null;
	protected EngineLanguages languages = null;
	protected JasperResources jasperResources = null;

	// Processing Data
	protected Map<String,Object> objects = new HashMap<>();
	protected Map<String,String> systemAttributes = new HashMap<>();
	protected Map<String,Map<String,VariableType>> variableTypes = new HashMap<>();
	protected Map<String,String> globalSystemAttributes = null;
	
	public ProcessingDataManager(ApplicationManager applicationManager, DependencyManager dependencyManager, EngineLanguages languages, JasperResources jasperResources,Map<String,String> globalAttributes) {
		super();
		this.applicationManager = applicationManager;
		this.dependencyManager = dependencyManager;
		this.languages = languages;
		this.jasperResources = jasperResources;
		this.globalSystemAttributes = globalAttributes;
	}

	// Returns list of items that should also be unloaded due to common dependencies
	public Set<Integer> removeTrackingEntries(int id) {
		jasperResources.engineDebug("Remove tracking entries for id "+id);

		// System attributes
		dependencyManager.removeSystemAttributeDependencies(id);
		Set<Integer> toUnload = dependencyManager.removeSystemAttributeOriginators(id);
		Set<String> attributesToRemove = dependencyManager.purgeOrphanAttributeOriginators();
		for(String attr : attributesToRemove) {
			jasperResources.engineDebug("Remove system attribute "+attr);
			this.systemAttributes.remove(attr);
		}

		// Objects
		Set<String> objs = dependencyManager.getObjectDependencies(id);
		Set<Integer> alsoUnload = dependencyManager.removeObjectDependencies(id);
		toUnload.addAll(alsoUnload);
		for(String obj : objs) {
			this.objects.remove(obj);
		}

		// Types
		dependencyManager.removeVariableTypeDependencies(id);
		alsoUnload = dependencyManager.removeVariableTypeOriginators(id);
		toUnload.addAll(alsoUnload);
		Set<Pair<String,String>> orphanTypes = dependencyManager.purgeOrphanTypes();
		for(Pair<String,String> orphan : orphanTypes) {
			String lang = orphan.getLeft();
			String typeName = orphan.getRight();
			this.variableTypes.get(lang).remove(typeName);
			jasperResources.engineDebug("Remove orphan type '"+typeName+"' for lang '"+lang+"'");
		}
		
		return toUnload;
	}

	// Returns a set of IDs that should be unloaded and re-processed
	public Set<Integer> setGlobalSystemAttributes(Map<String,String> attrs) {
		Set<Integer> ret = new HashSet<>();
		// Add and update current global system attributes
		for(Entry<String,String> entry : attrs.entrySet()) {
			String name = entry.getKey();
			String type = entry.getValue();
			String currentType = globalSystemAttributes.get(name);
			if (currentType==null) {
				// Add system attribute
				globalSystemAttributes.put(name, type);
				this.systemAttributes.put(name, type);
			} else if (!currentType.equals(type)) {
				// Update an existing global system attribute
				Set<Integer> ids = dependencyManager.getSystemAttributeDependencies().get(name);
				if (ids!=null)
					ret.addAll(ids);
				ids = dependencyManager.getSystemAttributeOriginators().get(name);
				if (ids!=null)
					ret.addAll(ids);
				globalSystemAttributes.put(name, type);
				this.systemAttributes.put(name, type);
			}
		}
		Set<String> toRemove = new HashSet<>();
		for(Entry<String,String> entry : globalSystemAttributes.entrySet()) {
			String name = entry.getKey();
			if (attrs.get(name)==null) {
				// Remove an existing global system attribute
				Set<Integer> ids = dependencyManager.getSystemAttributeDependencies().get(name);
				ids = dependencyManager.getSystemAttributeOriginators().get(name);
				if (ids!=null)
					ret.addAll(ids);
				toRemove.add(name);
				this.systemAttributes.remove(name);
			}
		}
		for(String s : toRemove) {
			globalSystemAttributes.remove(s);
		}

		Set<String> attributesToRemove = dependencyManager.purgeOrphanAttributeOriginators();
		for(String a : attributesToRemove) {
			this.systemAttributes.remove(a);
		}

		return ret;
	}
	public UserFile getUserFile(String path) {
		return applicationManager.getUserFiles().get(path);
	}

	public Map<String,Set<Integer>> getObjectOriginators() {
		return dependencyManager.getObjectOriginators();
	}
	
	public Map<String,Object> getObjects() {
		return objects;
	}

	public Map<String,String> getSystemAttributes() {
		return systemAttributes;
	}

	public Map<String,Set<Integer>> getSystemAttributeOriginators() {
		return dependencyManager.getSystemAttributeOriginators();
	}
	
	public Map<String,Set<Integer>> getVariableTypeOriginators(String lang) {
		Map<String,Set<Integer>> ret = dependencyManager.getVariableTypeOriginators().get(lang);

		if (ret==null) {
			ret = new HashMap<String,Set<Integer>>();
			dependencyManager.getVariableTypeOriginators().put(lang, ret);
		}
		
		return ret;
	}
	
	public Map<String,VariableType> getVariableTypes(String lang) {
		Map<String,VariableType> ret = variableTypes.get(lang);
		
		if (ret==null) {
			ret = new HashMap<>();
			LanguageSupport supp = languages.getLanguageSupport(lang);
			if (supp!=null) {
				for(VariableType type : supp.getBaseVariableTypes()) {
					ret.put(type.getName(), type);
				}
			}
			variableTypes.put(lang, ret);
		}
		
		return ret;
	}
	
	public Map<String,Set<Integer>> getSystemAttributeDependencies() {
		return dependencyManager.getSystemAttributeDependencies();
	}
	
	public Map<String,Set<Integer>> getVariableTypeDependencies(String lang) {
		Map<String,Set<Integer>> ret = dependencyManager.getVariableTypeDependencies().get(lang);
		
		if (ret==null) {
			ret = new HashMap<>();
			dependencyManager.getVariableTypeDependencies().put(lang,ret);
		}
		
		return ret;
	}

	public SourceFile getSourceFile(String path) {
		return applicationManager.getSourceFile(path);
	}

	public void addSourceFile(int id,SourceFile src) {
		Map<String,Set<Integer>> deps = dependencyManager.getSourceFileOriginators();
		String path = src.getPath();
		
		if (deps.get(path)==null) {
			deps.put(path, new HashSet<>());
		}
		deps.get(path).add(id);
		applicationManager.addSourceFile(src);
	}
	
}

