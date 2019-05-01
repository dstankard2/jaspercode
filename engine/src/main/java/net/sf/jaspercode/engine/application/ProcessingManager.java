package net.sf.jaspercode.engine.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.config.BuildComponent;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.langsupport.LanguageSupport;
import net.sf.jaspercode.api.logging.ProcessorLogLevel;
import net.sf.jaspercode.api.plugin.ProcessorLogMessage;
import net.sf.jaspercode.api.resources.ResourceWatcher;
import net.sf.jaspercode.api.snapshot.ComponentSnapshot;
import net.sf.jaspercode.api.snapshot.ItemSnapshot;
import net.sf.jaspercode.api.snapshot.SourceFileSnapshot;
import net.sf.jaspercode.api.snapshot.SystemAttributeSnapshot;
import net.sf.jaspercode.api.snapshot.TypeInfo;
import net.sf.jaspercode.api.types.VariableType;
import net.sf.jaspercode.engine.BuildComponentPattern;
import net.sf.jaspercode.engine.ComponentPattern;
import net.sf.jaspercode.engine.EngineLanguages;
import net.sf.jaspercode.engine.EnginePatterns;
import net.sf.jaspercode.engine.definitions.ApplicationFolderImpl;
import net.sf.jaspercode.engine.definitions.ComponentFile;
import net.sf.jaspercode.engine.definitions.UserFile;
import net.sf.jaspercode.engine.exception.PreprocessingException;
import net.sf.jaspercode.engine.processing.AddComponentFileEntry;
import net.sf.jaspercode.engine.processing.BuildComponentEntry;
import net.sf.jaspercode.engine.processing.ComponentEntry;
import net.sf.jaspercode.engine.processing.Processable;
import net.sf.jaspercode.engine.processing.ProcessableBase;
import net.sf.jaspercode.engine.processing.ProcessingState;
import net.sf.jaspercode.engine.processing.RemoveComponentFileEntry;
import net.sf.jaspercode.engine.processing.ResourceWatcherRecord;
import net.sf.jaspercode.engine.processing.Tracked;
import net.sf.jaspercode.engine.processing.UnloadComponentFileEntry;

public class ProcessingManager {
	
	private ApplicationManager applicationManager = null;
	private EnginePatterns patterns = null;
	private EngineLanguages languages = null;
	JasperResources jasperResources = null;
	private ProcessingState state = ProcessingState.TO_PROCESS;

	// Component Files
	protected Map<String,ComponentFile> componentFiles = new HashMap<>();
	
	// Shared objects
	private Map<String,Object> objects = new HashMap<>();

	// System attributes
	private Map<String,String> systemAttributes = new HashMap<>();

	// Variable Types
	private Map<String,Map<String,VariableType>> variableTypes = new HashMap<>();

	// Originators
	// Variable Types
	private Map<String,Map<String,List<Integer>>> variableTypeOriginators = new HashMap<>();
	// System attributes
	private Map<String,List<Integer>> systemAttributeOriginators = new HashMap<>();
	//  Source Files
	private Map<String,List<Integer>> sourceFileOriginators = new HashMap<>();
	// Objects
	private Map<String,List<Integer>> objectOriginators = new HashMap<>();

	// Dependencies.  
	// Source Files don't have dependents, only originators
	// Objects don't have dependents, only originators
	// Variable Types
	private Map<String,Map<String,List<Integer>>> variableTypeDependencies = new HashMap<>();
	// System attributes
	private Map<String,List<Integer>> systemAttributeDependencies = new HashMap<>();

	// Build components, components and resource watcher records
	private List<Tracked> items = new ArrayList<>();

	private List<Processable> toProcess = new ArrayList<>();
	
	List<BuildComponentEntry> buildComponentsToProcess = new ArrayList<>();

	public ProcessingState getState() {
		return state;
	}

	public ProcessingManager(ApplicationManager applicationManager, EnginePatterns patterns,EngineLanguages languages, JasperResources resourceContext) {
		this.applicationManager = applicationManager;
		this.patterns = patterns;
		this.languages = languages;
		this.jasperResources = resourceContext;
	}

	/**
	 * Miscellaneous internal APIs 
	 */
	private List<ResourceWatcherRecord> getResourceWatcherRecords() {
		List<ResourceWatcherRecord> ret = new ArrayList<>();
		
		for(Tracked item : items) {
			if (item instanceof ResourceWatcherRecord) {
				ret.add((ResourceWatcherRecord)item);
			}
		}
		
		return ret;
	}

	protected Tracked getItem(int id) {
		for(Tracked item : items) {
			if (item.getId()==id) {
				return item;
			}
		}
		return null;
	}

	/**
	 * End of miscellaneous internal APIs 
	 */

	

	/**
	 * API required by applicationManager
	 */
	
	public void componentFileRemoved(ComponentFile file) {
		this.toProcess.add(new RemoveComponentFileEntry(file, this));
	}
	
	public void componentFileAdded(ComponentFile file) {
		this.toProcess.add(new AddComponentFileEntry(file, this));
	}
	
	// Unloads component files whose path starts with the given string, and marks the items to be reprocessed.
	// This happens because of a change in systemAttributes.properties or jasper.properties
	public void unloadComponentFiles(String pathPrefix) {
		for(Entry<String,ComponentFile> entry : componentFiles.entrySet()) {
			String path = entry.getKey();
			if (path.startsWith(pathPrefix)) {
				toProcess.add(new UnloadComponentFileEntry(entry.getValue(), this));
			}
		}
	}
	
	public void processChanges(boolean userFileChangesDetected) {
		if (userFileChangesDetected) {
			this.checkResourceWatchers();
		}
		runProcessing();
	}

	/**
	 * End of methods used by applicationManager
	 */



	/**
	 * Start of methods for processing
	 */

	public void addComponentFile(ComponentFile componentFile) {
		for(Component comp : componentFile.getComponentSet().getComponent()) {
			if (comp instanceof BuildComponent) {
				BuildComponent b = (BuildComponent)comp;
				ProcessingContext processingContext = new ProcessingContext(this);
				BuildComponentPattern pattern = patterns.getBuildPattern(b.getClass());
				BuildComponentEntry e = new BuildComponentEntry(componentFile, processingContext, jasperResources, b, pattern, newId(), 0);
				toProcess.add(e);
			} else {
				ProcessingContext processingContext = new ProcessingContext(this);
				ComponentPattern pattern = this.patterns.getPattern(comp.getClass());
				ComponentEntry e = new ComponentEntry(jasperResources, componentFile, processingContext, comp, pattern, newId(), 0);
				toProcess.add(e);
			}
		}
	}

	public void removeComponentFile(ComponentFile componentFile, boolean remove) {
		if (componentFiles.get(componentFile.getPath())!=componentFile) {
			System.err.println("Attempted to remove component file '"+componentFile.getPath()+"' but is not the same as the file registered with the processingManager");
		}
		for(Component comp : componentFile.getComponentSet().getComponent()) {
			int id = 0;
			// TODO: Unload/remove the component
			if (comp instanceof BuildComponent) {
				if (comp==componentFile.getFolder().getBuildComponent().getBuildComponent()) {
					id = componentFile.getFolder().getBuildComponent().getId();
				}
			} else {
				for(Tracked item : items) {
					if (item instanceof ComponentEntry) {
						ComponentEntry e = (ComponentEntry)item;
						if (e.getComponent()==comp) {
							id = e.getId();
							break;
						}
					}
				}
			}
			unloadItem(id, remove);
		}
		if (remove) {
			componentFiles.remove(componentFile.getPath());
		}
	}

	/**
	 * End of methods for processing
	 */



	/**
	 * Methods required to unload/remove an item
	 */
	
	// Removes the item from processing completely.
	// The item is never null
	protected void removeItem(Tracked item, boolean reAdd) {
		jasperResources.engineDebug("Unloading item "+item.getId());
		
		items.remove(item);
		if (item instanceof BuildComponentEntry) {
			BuildComponentEntry e = (BuildComponentEntry)item;
			ApplicationFolderImpl folder = e.getFolder();
			folder.setBuildComponentEntry(null);
			if (this.buildComponentsToProcess.contains(e)) {
				buildComponentsToProcess.remove(e);
			}
			System.err.println("TODO: When a build component is removed, we must unload all components contained in the folder");
		}
		
		if (reAdd) {
			if (jasperResources.getEngineProperties().getDebug()) {
				System.out.println("Re-adding item '"+item.getName()+"' which was likely unloaded");
			}
			this.toProcess.add((Processable)item);
		}
	}

	protected void removeSourceFilesFromOriginator(int id) {
		List<String> toRemove = new ArrayList<>();
		
		for(Entry<String,List<Integer>> entry : sourceFileOriginators.entrySet()) {
			if (entry.getValue().contains(id)) {
				toRemove.add(entry.getKey());
			}
		}
		for(String s : toRemove) {
			removeSourceFile(s);
		}
	}
	protected void removeSourceFile(String path) {
		List<Integer> origs = this.sourceFileOriginators.get(path);
		List<Integer> todo = new ArrayList<>(origs);
		for(Integer i : todo) {
			this.unloadItem(i, false);
		}
		applicationManager.removeSourceFile(path);
		this.sourceFileOriginators.remove(path);
	}

	// Remove the resource watchers that originate from the given ID
	protected void removeResourceWatchersFromOriginator(int id, boolean remove) {
		for(ResourceWatcherRecord record : this.getResourceWatcherRecords()) {
			if (record.getOriginatorId()==id) {
				this.unloadItem(record.getId(), remove);
			}
		}
	}

	// Parameter causeUnload will cause other items dependent on the same key to be
	// unloaded as well.
	// causeUnload is false for system attribute dependency
	// causeUnload is true otherwise.
	protected void removeItemEntries(Map<String, List<Integer>> map, int id, boolean causeUnload) {
		List<Integer> toUnload = new ArrayList<>();

		for (Entry<String, List<Integer>> entry : map.entrySet()) {
			List<Integer> ids = entry.getValue();
			int index = ids.indexOf(id);
			if (index >= 0) {
				ids.remove(index);
				if (causeUnload) {
					toUnload.addAll(ids);
				}
			}
		}
		for (Integer i : toUnload) {
			this.unloadItem(i, false);
		}
	}
	protected void unloadItemFromTypes(Map<String,Map<String,List<Integer>>> typeMap, int id, boolean causeUnload) {
		List<Integer> toUnload = new ArrayList<>();
		
		for(Entry<String,Map<String,List<Integer>>> langEntry : typeMap.entrySet()) {
			for(Entry<String,List<Integer>> typeEntry : langEntry.getValue().entrySet()) {
				List<Integer> ids = typeEntry.getValue();
				int index = ids.indexOf(id);
				if (index>=0) {
					ids.remove(index);
					if (causeUnload) {
						toUnload.addAll(ids);
						if (variableTypes.get(langEntry.getKey()).containsKey(typeEntry.getKey())) {
							this.variableTypes.get(langEntry.getKey()).remove(typeEntry.getKey());
							if (this.jasperResources.getEngineProperties().getDebug()) {
								System.out.println("Removed type '"+typeEntry.getKey()+"' from lang '"+langEntry.getKey()+"'");
							}
						}
					}
				}
			}
		}
		for(Integer i : toUnload) {
			this.unloadItem(i, false);
		}
	}
	protected void unloadDependencies(Tracked item) {
		int id = item.getId();
		// System attributes
		removeItemEntries(this.systemAttributeOriginators, id, true);
		removeItemEntries(this.systemAttributeDependencies, id, false);

		// Objects
		removeItemEntries(this.objectOriginators, id, true);

		// Types
		unloadItemFromTypes(this.variableTypeDependencies, id, false);
		unloadItemFromTypes(this.variableTypeOriginators, id, true);

		// Unload this item's originator?
		if (item.getOriginatorId() > 0) {
			this.unloadItem(item.getOriginatorId(), false);
		}

		// Items that originate from this item
	}

	protected void unloadItem(int id, boolean remove) {
		Tracked tracked = getItem(id);
		
		if (tracked==null) return;
		
		if (jasperResources.getEngineProperties().getDebug()) {
			System.out.println("Removing item id "+id+" with name '"+tracked.getName()+"'");
		}
		removeItem(tracked, !remove);
		unloadDependencies(tracked);
		removeResourceWatchersFromOriginator(id,true);
		removeSourceFilesFromOriginator(id);
	}
	
	/**
	 * End of methods required to unload/remove an item
	 */

	

	protected void logMessages(List<ProcessorLogMessage> msgs) {
		for(ProcessorLogMessage msg : msgs) {
			String m = String.format("[%s] %s", msg.getLevel().name(), msg.getMessage());
			if (msg.getLevel()==ProcessorLogLevel.ERROR) {
				System.err.println(m);
				if (msg.getThrowable()!=null) {
					msg.getThrowable().printStackTrace();
				}
			} else {
				System.out.println(m);
				if (msg.getThrowable()!=null) {
					msg.getThrowable().printStackTrace(System.out);
				}
			}
		}
	}
	
	// Create Processable IDs
	private int nextId = 1;
	public int newId() {
		return nextId++;
	}
	
	/*
	public EngineLanguages getLanguages() {
		return languages;
	}
	*/
	
	private void checkResourceWatchers() {
		Map<String,UserFile> userFiles = applicationManager.getUserFiles();
		for(ResourceWatcherRecord rec : this.getResourceWatcherRecords()) {
			// If the resource watcher is already active then a userFile must be a perfect path match
			boolean alreadyActive = rec.isActive(); 
			String path = rec.getPath();
			for(Entry<String,UserFile> entry : userFiles.entrySet()) {
				String filePath = entry.getKey();
				UserFile file = entry.getValue();
				if (rec.getLastRun() < file.getLastModified()) {
					if ((alreadyActive) && (filePath.equals(path))) {
						this.toProcess.add(rec.entry());
					} else if ((!alreadyActive) && (filePath.startsWith(path))) {
						this.toProcess.add(rec.entry());
					}
				}
			}
		}
	}
	
	private void runProcessing() {
		System.out.println("Starting processing, found "+toProcess.size()+" items to process");

		while(toProcess.size()>0) {
			Collections.sort(toProcess);
			Processable p = toProcess.get(0);
			if (p.getPriority()<0) {
				jasperResources.engineDebug("Skipping component '"+p.getName()+"' with invalid priority");
				toProcess.remove(0);
			} else {
				
			}
		}
	}



	/**
	 * Required by processingContext
	 */
	
	public void addResourceWatcher(int originatorId, ComponentFile componentFile,ResourceWatcher watcher) {
		ResourceWatcherRecord rec = new ResourceWatcherRecord(jasperResources, new ProcessingContext(this), watcher,componentFile,newId(),originatorId);
		this.items.add(rec);
		this.checkResourceWatchers();
	}
	public void addComponent(int originatorId, Component component, ComponentFile componentFile) throws PreprocessingException {
		ComponentPattern pattern = patterns.getPattern(component.getClass());
		ComponentEntry e = new ComponentEntry(jasperResources, componentFile, new ProcessingContext(this), component, pattern, newId(), originatorId);
		e.preprocess();
		toProcess.add(e);
	}
	
	public Map<String,List<Integer>> getObjectOriginators() {
		return objectOriginators;
	}
	public Map<String,Object> getObjects() {
		return objects;
	}
	public Map<String,String> getSystemAttributes() {
		return systemAttributes;
	}
	public Map<String,List<Integer>> getSystemAttributeOriginators() {
		return systemAttributeOriginators;
	}
	public Map<String,List<Integer>> getVariableTypeOriginators(String lang) {
		return this.variableTypeOriginators.get(lang);
	}
	public Map<String,List<Integer>> getVariableTypeDependencies(String lang) {
		return this.variableTypeDependencies.get(lang);
	}
	public Map<String,VariableType> getVariableTypes(String lang) {
		Map<String,VariableType> ret = variableTypes.get(lang);
		if (ret==null) {
			LanguageSupport supp = this.languages.getLanguageSupport(lang);
			ret = new HashMap<>();
			for(VariableType type : supp.getBaseVariableTypes()) {
				ret.put(type.getName(), type);
			}
			variableTypes.put(lang, ret);
		}
		return ret;
	}
	public Map<String,List<Integer>> getSystemAttributeDependencies() {
		return systemAttributeDependencies;
	}
	public SourceFile getSourceFile(String path) {
		return applicationManager.getSourceFile(path);
	}
	public void addSourceFile(int id,SourceFile sourceFile) {
		applicationManager.addSourceFile(sourceFile);
		List<Integer> ids = sourceFileOriginators.get(sourceFile.getPath());
		if (ids==null) {
			ids = new ArrayList<>();
			sourceFileOriginators.put(sourceFile.getPath(), ids);
		}
		if (!ids.contains(id)) {
			ids.add(id);
		}
	}
	
	/**
	 * End of methods required by processingContext
	 */



	/**
	 * Create application snapshot
	 */

	protected List<String> searchMapForId(Map<String,List<Integer>> map, int id) {
		List<String> ret = new ArrayList<>();
		
		for(Entry<String,List<Integer>> entry : map.entrySet()) {
			if (entry.getValue().contains(id)) {
				ret.add(entry.getKey());
			}
		}
		
		return ret;
	}
	protected List<TypeInfo> searchTypes(Map<String,Map<String,List<Integer>>> map,int id) {
		List<TypeInfo> ret = new ArrayList<>();
		
		for(Entry<String,Map<String,List<Integer>>> entry : map.entrySet()) {
			String lang = entry.getKey();
			for(Entry<String,List<Integer>> e : entry.getValue().entrySet()) {
				if (e.getValue().contains(id)) {
					ret.add(new TypeInfo(lang,e.getKey()));
				}
			}
		}

		return ret;
	}
	public List<ItemSnapshot> getItemSnapshots() {
		List<ItemSnapshot> ret = new ArrayList<>();
		
		for(Tracked item : items) {
			ProcessableBase e = null;
			
			if (item instanceof ComponentEntry) {
				e = (ProcessableBase)item;
			} else if (item instanceof ResourceWatcherRecord) {
				ResourceWatcherRecord rec = (ResourceWatcherRecord)item;
				e = rec.entry();
			} else {
				System.err.println("Could not create snapshot for Tracked class "+item.getClass().getCanonicalName());
			}
			
			if (e!=null) {
				ComponentSnapshot sn = new ComponentSnapshot();
				ret.add(sn);
				sn.setId(e.getId());
				sn.setName(e.getName());
				sn.setSystemAttributesOriginated(searchMapForId(this.systemAttributeOriginators, e.getId()));
				sn.setSystemAttributeDependencies(searchMapForId(this.systemAttributeDependencies, e.getId()));
				sn.setTypesOriginated(searchTypes(this.variableTypeOriginators, e.getId()));
				sn.setTypeDependencies(searchTypes(this.variableTypeDependencies, e.getId()));
				sn.setSourceFilePaths(searchMapForId(this.sourceFileOriginators, e.getId()));
			}
			
			/*
			if (item instanceof ComponentEntry) {
				ComponentEntry e = (ComponentEntry)item;
			} else if (item instanceof ResourceWatcherRecord) {
				ResourceWatcherRecord rec = (ResourceWatcherRecord)item;
				ResourceWatcherEntry e = rec.entry();
			}
			*/
		}
		
		return ret;
	}
	public List<SystemAttributeSnapshot> getSystemAttributeSnapshots() {
		List<SystemAttributeSnapshot> ret = new ArrayList<>();

		for(Entry<String,String> entry : systemAttributes.entrySet()) {
			String name = entry.getKey();
			String type = entry.getValue();
			List<Integer> origs = new ArrayList<>();
			List<Integer> deps = new ArrayList<>();
			if (this.systemAttributeOriginators.containsKey(name)) {
				origs.addAll(this.systemAttributeOriginators.get(name));
			}
			if (this.systemAttributeDependencies.containsKey(name)) {
				deps.addAll(this.systemAttributeDependencies.get(name));
			}
			ret.add(new SystemAttributeSnapshot(name, type, "Placeholder", origs, deps));
		}

		return ret;
	}

	public void populateSourceFileOriginators(List<SourceFileSnapshot> srcs) {
		for(SourceFileSnapshot src : srcs) {
			List<Integer> origs = new ArrayList<>();
			origs.addAll(this.sourceFileOriginators.get(src.getPath()));
			src.setOriginators(origs);
		}
	}
	
	/**
	 * End of methods to create application snapshots
	 */

}

