package net.sf.jaspercode.engine.processing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.sf.jaspercode.api.langsupport.LanguageSupport;
import net.sf.jaspercode.api.types.VariableType;
import net.sf.jaspercode.engine.EngineLanguages;
import net.sf.jaspercode.engine.JasperResources;

public class ProcessingDataManager {

	private EngineLanguages languages = null;
	private JasperResources jasperResources = null;

	private Map<String,Map<String,VariableType>> types = new HashMap<>();
	// The base types of a language are read-only
	private Map<String,Map<String,VariableType>> baseTypes = new HashMap<>();

	private Map<String,String> attributes = new HashMap<>();
	private Map<String,Object> objects = new HashMap<>();
	protected Map<String,String> globalSystemAttributes = new HashMap<>();

	private Map<String,List<Integer>> srcDependencies = new HashMap<>();
	private Map<String,List<Integer>> attributeDependencies = new HashMap<>();
	private Map<String,List<Integer>> attributeOriginators = new HashMap<>();
	private Map<String,List<Integer>> objectDependencies = new HashMap<>();
	private Map<String, Map<String,List<Integer>>> typeDependencies = new HashMap<>();
	private Map<String, Map<String,List<Integer>>> typeOriginators = new HashMap<>();

	public ProcessingDataManager(EngineLanguages languages, JasperResources jasperResources) {
		this.languages = languages;
		this.jasperResources = jasperResources;
	}

	public void setGlobalSystemAttributes(Map<String,String> globalSystemAttributes) {
		this.globalSystemAttributes = globalSystemAttributes;
	}

	public Map<String,Object> getObjects() {
		return objects;
	}

	public List<String> getSourceFilesFromId(int itemId) {
		List<String> ret = new ArrayList<>();
		ret = srcDependencies.entrySet().stream().filter(e -> e.getValue().contains(itemId)).map(Map.Entry::getKey).collect(Collectors.toList());
		return ret;
	}

	public boolean originatesProcessingData(int itemId) {
		if (srcDependencies.values().stream().anyMatch(deps -> deps.contains(itemId))) {
			return true;
		}
		if (attributeDependencies.values().stream().anyMatch(deps -> deps.contains(itemId))) {
			return true;
		}
		if (objectDependencies.values().stream().anyMatch(deps -> deps.contains(itemId))) {
			return true;
		}
		for(Map<String,List<Integer>> typesForLang : typeDependencies.values()) {
			if (typesForLang.values().stream().anyMatch(deps -> deps.contains(itemId))) {
				return true;
			}
		}

		return false;
	}

	// Removes the itemId and everything that that itemId originates.  
	// The itemId is removed from dependencies
	// Returns a list of items to be removed and re-added
	public Set<Integer> removeItem(int itemId) {
		Set<Integer> ret = new HashSet<>();

		srcDependencies.entrySet().stream().filter(d -> d.getValue().contains(itemId)).collect(Collectors.toList()).forEach(e -> {
			srcDependencies.remove(e.getKey());
			ret.addAll(e.getValue());
		});

		attributeOriginators.entrySet().stream().filter(d -> d.getValue().contains(itemId)).collect(Collectors.toList()).forEach(e -> {
			String name = e.getKey();
			List<Integer> ids = e.getValue();
			// remove the originators
			attributeOriginators.remove(name);
			// remove the definition of the attribute
			this.attributes.remove(e.getKey());
			// Remove and re-add other originators of this attribute
			ret.addAll(ids);
			// Remove items that depend on this attribute as well (and re-add them)
			List<Integer> depIds = attributeDependencies.get(name);
			if (depIds!=null) {
				ret.addAll(depIds);
			}
			attributeDependencies.remove(name);
		});

		// Remove this item from object dependencies and re-evaluate everything that depends on it
		objectDependencies.entrySet().stream().filter(e -> e.getValue().contains(itemId)).collect(Collectors.toList()).forEach(e -> {
			objectDependencies.remove(e.getKey());
			ret.addAll(e.getValue());
			objects.remove(e.getKey());
		});

		// Remove this item from type dependencies
		for(Map<String,List<Integer>> langDeps : typeDependencies.values()) {
			langDeps.entrySet().stream().filter(d -> d.getValue().contains(itemId)).collect(Collectors.toList()).forEach(e -> {
				e.getValue().removeAll(Arrays.asList(itemId));
			});
		}
		
		// Remove this item from type originators
		for(HashMap.Entry<String,Map<String,List<Integer>>> langOrigs : typeOriginators.entrySet()) {
			String lang = langOrigs.getKey();
			langOrigs.getValue().entrySet().stream().filter(d -> d.getValue().contains(itemId)).collect(Collectors.toList()).forEach(e -> {
				String typeName = e.getKey();
				// Remove originators of this type
				langOrigs.getValue().remove(e.getKey());
				ret.addAll(e.getValue());
				// Remove this type
				types.get(lang).remove(e.getKey());
				// Remove items that depend on this type as well.
				Map<String,List<Integer>> langDeps = typeDependencies.get(lang);
				if (langDeps!=null) {
					List<Integer> ids = langDeps.get(typeName);
					if (ids!=null) {
						ret.addAll(ids);
					}
					langDeps.remove(typeName);
				}
			});
		}
		
		// Remove this item from attribute dependencies
		attributeDependencies.entrySet().stream().filter(e -> e.getValue().contains(itemId)).collect(Collectors.toList()).forEach(e -> {
			e.getValue().removeAll(Arrays.asList(itemId));
		});

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

	public void commitChanges(ProcessableChanges changes) {
		int itemId = changes.getItemId();

		changes.getAttributesAdded().entrySet().forEach(e -> {
			attributes.put(e.getKey(), e.getValue());
		});
		// Attributes originated
		changes.getAttributesOriginated().forEach(attr -> {
			List<Integer> deps = attributeOriginators.get(attr);
			if (deps==null) {
				deps = new ArrayList<>();
				attributeOriginators.put(attr, deps);
			}
			deps.add(itemId);
		});

		// Attribute dependencies
		changes.getAttributeDependencies().forEach(attr -> {
			List<Integer> deps = attributeDependencies.get(attr);
			if (deps==null) {
				deps = new ArrayList<>();
				attributeDependencies.put(attr, deps);
			}
			deps.add(itemId);
		});

		// Object dependencies and additions
		changes.getObjects().entrySet().forEach(e -> {
			String name = e.getKey();
			this.objects.put(name, e.getValue());
			List<Integer> deps = objectDependencies.get(name);
			if (deps==null) {
				deps = new ArrayList<>();
				objectDependencies.put(name, deps);
			}
			deps.add(itemId);
		});

		// Types modified
		changes.getTypesModified().forEach(pair -> {
			String lang = pair.getKey();
			VariableType type = pair.getRight();
			String name = type.getName();
			
			Map<String,VariableType> typesForLang = this.getTypes(lang);
			typesForLang.put(name, type);
			Map<String,List<Integer>> depsForLang = typeOriginators.get(lang);
			if (depsForLang==null) {
				depsForLang = new HashMap<>();
				typeOriginators.put(lang, depsForLang);
			}
			List<Integer> deps = depsForLang.get(name);
			if (deps==null) {
				deps = new ArrayList<>();
				depsForLang.put(name, deps);
			}
			deps.add(itemId);
		});

		// Type dependencies
		changes.getTypeDependencies().forEach(pair -> {
			String lang = pair.getKey();
			VariableType type = pair.getRight();
			String name = type.getName();

			Map<String,List<Integer>> depsForLang = typeDependencies.get(lang);
			if (depsForLang==null) {
				depsForLang = new HashMap<>();
				typeDependencies.put(lang, depsForLang);
			}
			List<Integer> deps = depsForLang.get(name);
			if (deps==null) {
				deps = new ArrayList<>();
				depsForLang.put(name, deps);
			}
			deps.add(itemId);
		});

		changes.getSourceFiles().forEach(src -> {
			List<Integer> deps = srcDependencies.get(src.getPath());
			
			if (deps==null) {
				deps = new ArrayList<>();
				srcDependencies.put(src.getPath(), deps);
			}
			deps.add(itemId);
		});
		return;
	}
	
	public List<Integer> getItemsForObjectName(String objectName) {
		return this.objectDependencies.get(objectName);
	}
	
	public List<Integer> getItemsForSourceFile(String path) {
		return this.srcDependencies.get(path);
	}

	public List<Integer> getItemsForType(String lang, String name) {
		List<Integer> ret = null;
		Map<String,List<Integer>> typesForLang = this.typeOriginators.get(lang);
		if (typesForLang != null) {
			ret = typesForLang.get(name);
		}
		return ret;
	}

}
