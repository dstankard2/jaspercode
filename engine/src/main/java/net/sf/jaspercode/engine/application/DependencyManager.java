package net.sf.jaspercode.engine.application;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;

public class DependencyManager {

	private Map<String,String> globalAttributes = null;
	
	public DependencyManager(Map<String,String> globalAttributes) {
		this.globalAttributes = globalAttributes;
	}

	// Originators
	// Variable Types
	private Map<String,Map<String,Set<Integer>>> variableTypeOriginators = new HashMap<>();
	// System attributes
	private Map<String,Set<Integer>> systemAttributeOriginators = new HashMap<>();
	//  Source Files
	private Map<String,Set<Integer>> sourceFileOriginators = new HashMap<>();
	// Objects
	private Map<String,Set<Integer>> objectOriginators = new HashMap<>();

	// Dependencies.  
	// Source Files don't have dependents, only originators
	// Objects don't have dependents, only originators
	// Variable Types
	private Map<String,Map<String,Set<Integer>>> variableTypeDependencies = new HashMap<>();
	// System attributes
	private Map<String,Set<Integer>> systemAttributeDependencies = new HashMap<>();

	public Map<String,Set<Integer>> getObjectOriginators() {
		return objectOriginators;
	}
	public Map<String,Map<String,Set<Integer>>> getVariableTypeDependencies() {
		return variableTypeDependencies;
	}
	public Map<String,Map<String,Set<Integer>>> getVariableTypeOriginators() {
		return variableTypeOriginators;
	}
	public Map<String,Set<Integer>> getSystemAttributeOriginators() {
		return systemAttributeOriginators;
	}
	public Map<String,Set<Integer>> getSystemAttributeDependencies() {
		return systemAttributeDependencies;
	}
	public Map<String,Set<Integer>> getSourceFileOriginators() {
		return sourceFileOriginators;
	}

	public boolean systemAttributeHasDependencies(String name) {
		if (systemAttributeDependencies.get(name)!=null) return true;
		if (systemAttributeOriginators.get(name)!=null) return true;
		return false;
	}

	public Set<String> getSourceFilesFromOriginator(Integer id) {
		Set<String> ret = new HashSet<>();
		
		for(Entry<String,Set<Integer>> entry : sourceFileOriginators.entrySet()) {
			if (entry.getValue().contains(id)) ret.add(entry.getKey());
		}

		return ret;
	}
	
	public Set<Integer> removeSourceFile(String path) {
		Set<Integer> ret = new HashSet<>();

		ret = sourceFileOriginators.remove(path);

		return ret;
	}

	public Set<String> purgeOrphanAttributeOriginators() {
		Set<String> ret = new HashSet<>();

		for(Entry<String,Set<Integer>> entry : systemAttributeOriginators.entrySet()) {
			String key = entry.getKey();
			// Don't purge a system attribute that is a global system attribute.
			if (globalAttributes.get(key)==null) {
				if (entry.getValue().size()==0) {
					ret.add(key);
				}
			}
		}
		for(String s : ret) {
			systemAttributeOriginators.remove(s);
			//systemAttributeDependencies.remove(s);
		}

		return ret;
	}

	public void removeSystemAttributeDependencies(int id) {
		for(Entry<String,Set<Integer>> entry : systemAttributeDependencies.entrySet()) {
			if (entry.getValue().contains(id)) {
				entry.getValue().remove(id);
			}
		}
	}

	// Caller must unload all IDs that are returned
	public Set<Integer> removeSystemAttributeOriginators(int id) {
		Set<Integer> ret = new HashSet<>();
		
		for(Entry<String,Set<Integer>> entry : systemAttributeOriginators.entrySet()) {
			// If this attribute is global, don't remove originators
			if (globalAttributes.get(entry.getKey())==null) {
				if (entry.getValue().contains(id)) {
					entry.getValue().remove(id);
					ret.addAll(entry.getValue());
				}
			}
		}
		
		return ret;
	}
	
	public Set<String> getObjectDependencies(int id) {
		Set<String> ret = new HashSet<>();
		
		for(Entry<String,Set<Integer>> entry : objectOriginators.entrySet()) {
			if (entry.getValue().contains(id)) {
				ret.add(entry.getKey());
			}
		}
		
		return ret;
	}

	public Set<Integer> removeObjectDependencies(int id) {
		Set<Integer> ret = new HashSet<>();
		
		for(Entry<String,Set<Integer>> entry : objectOriginators.entrySet()) {
			if (entry.getValue().contains(id)) {
				entry.getValue().remove(id);
				ret.addAll(entry.getValue());
			}
		}
		
		return ret;
	}
	
	// Remove dependencies on the given id
	public void removeVariableTypeDependencies(int id) {
		for(Entry<String,Map<String,Set<Integer>>> lang : this.variableTypeDependencies.entrySet()) {
			for(Entry<String,Set<Integer>> typeEntry : lang.getValue().entrySet()) {
				Set<Integer> deps = typeEntry.getValue();
				if (deps.contains(id)) {
					deps.remove(id);
				}
			}
		}
	}
	
	public Set<Integer> removeVariableTypeOriginators(int id) {
		Set<Integer> ret = new HashSet<>();
		
		for(Entry<String,Map<String,Set<Integer>>> lang : this.variableTypeOriginators.entrySet()) {
			for(Entry<String,Set<Integer>> typeEntry : lang.getValue().entrySet()) {
				Set<Integer> origs = typeEntry.getValue();
				if (origs.contains(id)) {
					origs.remove(id);
					ret.addAll(origs);
					origs.clear();
				}
			}
		}
		
		return ret;
	}
	
	public Set<Pair<String,String>> purgeOrphanTypes() {
		Set<Pair<String,String>> ret = new HashSet<>();
		
		for(Entry<String,Map<String,Set<Integer>>> lang : this.variableTypeOriginators.entrySet()) {
			Set<String> toRemove = new HashSet<>();
			for(Entry<String,Set<Integer>> typeEntry : lang.getValue().entrySet()) {
				if (typeEntry.getValue().isEmpty()) {
					toRemove.add(typeEntry.getKey());
				}
			}
			for(String r : toRemove) {
				ret.add(Pair.of(lang.getKey(), r));
				lang.getValue().remove(r);
			}
		}
		
		return ret;
	}

}
