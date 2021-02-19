package net.sf.jaspercode.engine.processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.jaspercode.api.langsupport.LanguageSupport;
import net.sf.jaspercode.api.types.VariableType;
import net.sf.jaspercode.engine.EngineLanguages;
import net.sf.jaspercode.engine.JasperResources;

public class ProcessingDataManager {

	private Map<String,Map<String,VariableType>> types = new HashMap<>();
	private Map<String,String> attributes = new HashMap<>();
	private Map<String,Object> objects = new HashMap<>();
	protected Map<String,String> globalSystemAttributes = new HashMap<>();

	private Map<String,List<Integer>> attributeDependencies = new HashMap<>();
	private Map<String, Map<String,List<Integer>>> typeDependencies = new HashMap<>();
	private Map<String,List<Integer>> objectDependencies = new HashMap<>();
	private EngineLanguages languages = null;
	private JasperResources jasperResources = null;
	private Map<String,List<Integer>> srcDependencies = new HashMap<>();
	private Map<String,Map<String,VariableType>> baseTypes = new HashMap<>();

	public ProcessingDataManager(EngineLanguages languages, JasperResources jasperResources) {
		this.languages = languages;
		this.jasperResources = jasperResources;
	}

	public Map<String,Object> getObjects() {
		return objects;
	}
	
	public List<String> getSourceFilesFromId(int itemId) {
		List<String> ret = new ArrayList<>();
		
		srcDependencies.entrySet().forEach(e -> {
			String path = e.getKey();
			if (e.getValue().contains(itemId)) {
				ret.add(path);
			}
		});
		return ret;
	}

	public boolean originates(int itemId) {

		// attributeDependencies
		for(Entry<String,List<Integer>> e : attributeDependencies.entrySet()) {
			if (e.getValue().contains(itemId)) return true;
		}
		
		// objectDependencies
		for(Entry<String,List<Integer>> e : objectDependencies.entrySet()) {
			if (e.getValue().contains(itemId)) return true;
		}

		// typeDependencies
		
		for(Entry<String,Map<String,List<Integer>>> lang : typeDependencies.entrySet()) {
			for(Entry<String,List<Integer>> e : lang.getValue().entrySet()) {
				if (e.getValue().contains(itemId)) return true;
			}
		}

		// srcDependencies
		for(Entry<String,List<Integer>> e : srcDependencies.entrySet()) {
			if (e.getValue().contains(itemId)) return true;
		}

		return false;
	}

	// Removes the itemId and everything that that itemId depends on
	// Returns a list of items to be removed and re-added
	public Set<Integer> removeItem(int itemId) {
		Set<Integer> ret = new HashSet<>();
		List<String> toRemove = new ArrayList<>();
		
		// attributeDependencies
		attributeDependencies.entrySet().forEach(e -> {
			if (e.getValue().contains(itemId)) {
				toRemove.add(e.getKey());
				ret.addAll(e.getValue());
			}
		});
		toRemove.stream().forEach(r -> {
			attributeDependencies.remove(r);
			attributes.remove(r);
		});
		toRemove.clear();
		
		// objectDependencies
		objectDependencies.entrySet().forEach(e -> {
			if (e.getValue().contains(itemId)) {
				toRemove.add(e.getKey());
				ret.addAll(e.getValue());
			}
		});
		toRemove.stream().forEach(r -> {
			objectDependencies.remove(r);
			objects.remove(r);
		});
		toRemove.clear();
		
		// typeDependencies
		typeDependencies.entrySet().forEach(e -> {
			String lang = e.getKey();
			e.getValue().entrySet().forEach(type -> {
				String name = type.getKey();
				if (type.getValue().contains(itemId)) {
					// Do not remove a base type!
					if (baseTypes.get(lang).get(name)==null) {
						toRemove.add(name);
						ret.addAll(type.getValue());
					}
				}
			});
			toRemove.stream().forEach(r -> {
				e.getValue().remove(r);
				types.get(lang).remove(r);
			});
			toRemove.clear();
		});
		
		// srcDependencies
		srcDependencies.entrySet().forEach(e -> {
			if (e.getValue().contains(itemId)) {
				toRemove.add(e.getKey());
				ret.addAll(e.getValue());
			}
		});
		toRemove.parallelStream().forEach(r -> {
			srcDependencies.remove(r);
		});
		toRemove.clear();
		
		if (ret.contains(itemId)) {
			ret.remove(itemId);
		}

		return ret;
	}

	public Map<String,VariableType> getTypes(String lang) {
		Map<String,VariableType> ret = types.get(lang);
		if (ret==null) {
			ret = new HashMap<>();
			Map<String,VariableType> langBaseTypes = new HashMap<>();
			this.baseTypes.put(lang, langBaseTypes);
			LanguageSupport supp = languages.getLanguageSupport(lang);
			if (supp!=null) {
				for(VariableType type : supp.getBaseVariableTypes()) {
					ret.put(type.getName(), type);
					langBaseTypes.put(type.getName(), type);
				}
			} else {
				jasperResources.engineDebug("Tried to access types for language '"+lang+"' but there is no language support for it");
			}
			types.put(lang, ret);
		}
		return ret;
	}
	public String getSystemAttribute(String name) {
		String ret = globalSystemAttributes.get(name);
		if (ret==null) {
			ret = attributes.get(name);
		}
		
		return ret;
	}
	
	public void setGlobalSystemAttributes(Map<String,String> attributes) {
		globalSystemAttributes = attributes;
	}
	
	public void commitChanges(ProcessableChanges changes) {
		int id = changes.getItemId();
		
		// New attributes
		changes.getAttributesAdded().entrySet().forEach(e -> {
			attributes.put(e.getKey(), e.getValue());
		});
		
		// Attribute dependences
		changes.getAttributeDependencies().forEach(attr -> {
			List<Integer> deps = attributeDependencies.get(attr);
			if (deps==null) {
				deps = new ArrayList<>();
				attributeDependencies.put(attr, deps);
			}
			deps.add(id);
		});
		
		// Object dependencies
		changes.getObjectDeps().forEach(obj -> {
			List<Integer> deps = objectDependencies.get(obj);
			if (deps==null) {
				deps = new ArrayList<>();
				objectDependencies.put(obj, deps);
			}
			deps.add(id);
		});
		
		// Objects added
		changes.getObjects().entrySet().forEach(e -> {
			String name = e.getKey();
			objects.put(name, e.getValue());
		});
		
		// Type dependencies
		changes.getTypeDependencies().forEach(pair -> {
			String lang = pair.getLeft();
			VariableType type = pair.getRight();
			
			Map<String,List<Integer>> l = typeDependencies.get(lang);
			if (l==null) {
				l = new HashMap<>();
				typeDependencies.put(lang, l);
			}
			List<Integer> deps = l.get(type.getName());
			if (deps==null) {
				deps = new ArrayList<>();
				l.put(type.getName(), deps);
			}
			deps.add(id);
		});
		
		// Types added
		changes.getTypesAdded().forEach(pair -> {
			String lang = pair.getKey();
			VariableType type = pair.getValue();

			Map<String,VariableType> t = getTypes(lang);
			//Map<String,VariableType> t = types.get(lang);
			if (t==null) {
				t = new HashMap<>();
				types.put(lang, t);
			}
			t.put(type.getName(), type);
		});
		
		// Source files modified
		changes.getSourceFiles().forEach(src -> {
			List<Integer> deps = srcDependencies.get(src.getPath());
			
			if (deps==null) {
				deps = new ArrayList<>();
				srcDependencies.put(src.getPath(), deps);
			}
			deps.add(id);
		});

	}

}
