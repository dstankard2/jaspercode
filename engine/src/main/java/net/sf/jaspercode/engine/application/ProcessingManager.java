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
import net.sf.jaspercode.api.config.ComponentSet;
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
import net.sf.jaspercode.engine.ComponentPattern;
import net.sf.jaspercode.engine.EngineLanguages;
import net.sf.jaspercode.engine.EnginePatterns;
import net.sf.jaspercode.engine.definitions.ApplicationFolderImpl;
import net.sf.jaspercode.engine.definitions.ComponentFile;
import net.sf.jaspercode.engine.definitions.UserFile;
import net.sf.jaspercode.engine.definitions.WatchedResource;
import net.sf.jaspercode.engine.exception.PreprocessingException;
import net.sf.jaspercode.engine.exception.ProcessingException;
import net.sf.jaspercode.engine.processing.AddedFile;
import net.sf.jaspercode.engine.processing.BuildComponentEntry;
import net.sf.jaspercode.engine.processing.ComponentEntry;
import net.sf.jaspercode.engine.processing.FileToProcess;
import net.sf.jaspercode.engine.processing.ModifiedFile;
import net.sf.jaspercode.engine.processing.Processable;
import net.sf.jaspercode.engine.processing.ProcessableBase;
import net.sf.jaspercode.engine.processing.ProcessingState;
import net.sf.jaspercode.engine.processing.ResourceWatcherRecord;
import net.sf.jaspercode.engine.processing.Tracked;

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
	private Map<String,String> attributes = new HashMap<>();

	// Variable Types
	private Map<String,Map<String,VariableType>> variableTypes = new HashMap<>();

	// Originators
	// Variable Types
	private Map<String,Map<String,List<Integer>>> variableTypeOriginators = new HashMap<>();
	// System attributes
	private Map<String,List<Integer>> attributeOriginators = new HashMap<>();
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
	private Map<String,List<Integer>> attributeDependencies = new HashMap<>();

	// System attributes that come from the system attributes file
	private List<String> systemAttributesFromFile = new ArrayList<>();

	// Files to process, found while scanning
	List<FileToProcess> filesToProcess = new ArrayList<>();
	
	// Build components, components and resource watcher records
	private List<Tracked> items = new ArrayList<>();

	private List<Processable> toProcess = new ArrayList<>();
	//List<WatchedResource> toRemove = null;
	
	List<BuildComponentEntry> buildComponentsToProcess = new ArrayList<>();
	//List<BuildComponentEntry> buildComponentsToGenerate = new ArrayList<>();

	public ProcessingState getState() {
		return state;
	}

	public ProcessingManager(ApplicationManager applicationManager, EnginePatterns patterns,EngineLanguages languages, JasperResources resourceContext) {
		this.applicationManager = applicationManager;
		this.patterns = patterns;
		this.languages = languages;
		this.jasperResources = resourceContext;
	}
	
	protected List<ResourceWatcherRecord> getResourceWatcherRecords() {
		List<ResourceWatcherRecord> ret = new ArrayList<>();
		
		for(Tracked item : items) {
			if (item instanceof ResourceWatcherRecord) {
				ret.add((ResourceWatcherRecord)item);
			}
		}
		
		return ret;
	}

	// All resource watcher records that watch this file's path must be removed.
	// Watchers that watch this path exactly must be removed.
	// Watchers that watch a substring of the path should be unloaded.
	public void userFileRemoved(UserFile userFile) {
		String path = userFile.getPath();
		
		for(ResourceWatcherRecord rec : getResourceWatcherRecords()) {
			if (rec.getPath().equals(path)) {
				// This watcher must be removed since its file has been removed
				this.removeItem(rec.getId());
			} else if (path.startsWith(rec.getPath())) {
				// This watcher must be unloaded 
				this.unloadItem(rec.getId());
			}
		}
	}

	// Adds the componentFile to the system.
	public void loadComponentFile(ComponentFile componentFile) {
		this.componentFiles.put(componentFile.getPath(), componentFile);
	}

	// NOTE: Do not track the component file.  It can be tracked elsewhere.
	// When a component file is deleted its components must be removed (remove = true)
	// When a component file needs to be reprocessed its components must be unloaded (remove = false)
	// The latter can happen when jasper.properties is changed
	public void unloadComponentFile(ComponentFile componentFile, boolean remove) {
		ComponentSet set = componentFile.getComponentSet();
		for(Component comp : set.getComponent()) {
			int id = -1;
			if (comp instanceof BuildComponent) {
				componentFile.getFolder().getBuildComponent();
				if (comp==componentFile.getFolder().getBuildComponent().getBuildComponent()) {
					id = componentFile.getFolder().getBuildComponent().getId();
				} else {
					System.err.println("Tried to unload/remove a build component but it was not assigned to this folder!");
				}
			} else {
				for(Tracked i : items) {
					if (i instanceof ComponentEntry) {
						ComponentEntry e = (ComponentEntry)i;
						if (e.getComponent()==comp) {
							id = e.getId();
							break;
						}
					}
				}
			}
			if (id<0) {
				System.err.println("Tried to unload/remove component '"+comp.getComponentName()+"' from file '"+componentFile.getPath()+"' but couldn't find it in engine state");
			} else {
				if (remove) {
					removeItem(id);
				} else {
					this.unloadItem(id);
				}
			}
		}

		// Resource Manager will manage component file in the folder.
		// Remove records of this component file in the processingManager
		componentFiles.remove(componentFile.getPath());
	}

	protected Tracked getItem(int id) {
		for(Tracked item : items) {
			if (item.getId()==id) {
				return item;
			}
		}
		return null;
	}

	// Parameter causeUnload will cause other items dependent on the same key to be unloaded as well.
	// causeUnload is false for system attribute dependency
	// causeUnload is true otherwise.
	protected void removeItemEntries(Map<String,List<Integer>> map, int id, boolean causeUnload) {
		List<Integer> toUnload = new ArrayList<>();
		
		for(Entry<String,List<Integer>> entry : map.entrySet()) {
			List<Integer> ids = entry.getValue();
			int index = ids.indexOf(id);
			if (index>=0) {
				ids.remove(index);
				if (causeUnload) {
					toUnload.addAll(ids);
				}
			}
		}
		for(Integer i : toUnload) {
			this.unloadItem(i);
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
			this.unloadItem(i);
		}
	}
	
	protected void unloadDependencies(Tracked item) {
		int id = item.getId();
		// System attributes
		removeItemEntries(this.attributeOriginators, id, true);
		removeItemEntries(this.attributeDependencies, id, false);

		// Objects
		removeItemEntries(this.objectOriginators, id, true);

		// Types
		unloadItemFromTypes(this.variableTypeDependencies, id, false);
		unloadItemFromTypes(this.variableTypeOriginators, id, true);
		
		// Unload this item's originator?
		if (item.getOriginatorId()>0) {
			this.unloadItem(item.getOriginatorId());
		}

		// Items that originate from this item
	}

	// Remove the resource watchers that originate from the given ID
	// TODO: Implement proper handling of resource watchers for remove = true/false.
	protected void removeResourceWatchersFromOriginator(int id,boolean remove) {
		
	}

	protected void removeItem(int id) {
		Tracked tracked = getItem(id);

		if (jasperResources.getEngineProperties().getDebug()) {
			System.out.println("Removing item id "+id+" with name '"+tracked.getName()+"'");
		}

		if (tracked!=null) {
			// Remove tracked item first so it won't be re-added to toProcess
			removeId(id, false);
			//items.remove(tracked);

			// Remove references to ID
			unloadDependencies(tracked);
			removeResourceWatchersFromOriginator(id,true);
			removeSourceFilesFromOriginator(id);
		}
	}
	
	protected void unloadItem(int id) {
		Tracked tracked = getItem(id);

		if (tracked!=null) {
			if (jasperResources.getEngineProperties().getDebug()) {
				System.out.println("Unloading item id "+id+" with name '"+tracked.getName()+"'");
			}
			removeId(id, true);

			// Remove references to ID
			unloadDependencies(tracked);
			removeResourceWatchersFromOriginator(id,false);
			removeSourceFilesFromOriginator(id);
		}
	}

	protected void removeId(int id, boolean reAdd) {
		Tracked item = null;
		
		for(Tracked t : items) {
			if (t.getId()==id) {
				item = t;
				break;
			}
		}
		
		if (item!=null) {
			System.out.println("Unloading "+item.getName());
			items.remove(item);
			if (item instanceof BuildComponentEntry) {
				BuildComponentEntry e = (BuildComponentEntry)item;
				ApplicationFolderImpl folder = e.getFolder();
				folder.setBuildComponentEntry(null);
				System.err.println("TODO: When a build component is removed, we must unload all components contained in the folder");
			}
			if ((reAdd) && (item instanceof Processable)) {
				if (jasperResources.getEngineProperties().getDebug()) {
					System.out.println("Re-adding item '"+item.getName()+"' which was likely unloaded");
				}
				this.toProcess.add((Processable)item);
			}
		} else {
			System.err.println("Couldn't remove ID "+id+" because it was not loaded");
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
			this.unloadItem(i);
		}
		applicationManager.removeSourceFile(path);
		this.sourceFileOriginators.remove(path);
	}

	// When a file is modified, we want to remove the old version and add the new version
	// This is only applicable for component files.
	// 
	public void fileModified(WatchedResource newFile) {
		String path = newFile.getPath();
		WatchedResource oldFile = null;

		if (componentFiles.get(path)!=null) {
			oldFile = componentFiles.get(path);
			//componentFiles.put(path, (ComponentFile)newFile);
			ModifiedFile f = new ModifiedFile(newFile,oldFile,this,applicationManager,this.jasperResources);
			filesToProcess.add(f);
		} else {
			// This is a userFile - no-op
		}
	}

	public void addFiles(List<ComponentFile> componentFiles) {
		for(ComponentFile c : componentFiles) {
			AddedFile added = new AddedFile(c, this, jasperResources);
			filesToProcess.add(added);
			this.componentFiles.put(c.getPath(), c);
		}
	}

	// These are added via a processable's ProcessorContext
	public void addResourceWatcher(int originatorId, ComponentFile componentFile,ResourceWatcher watcher) {
		ResourceWatcherRecord rec = new ResourceWatcherRecord(jasperResources, new ProcessingContext(this), watcher,componentFile,newId(),originatorId);
		this.items.add(rec);
		this.checkResourceWatchers();
		//toProcessChanged = true;
	}
	public void addComponent(int originatorId, Component component, ComponentFile componentFile) throws PreprocessingException {
		ComponentPattern pattern = patterns.getPattern(component.getClass());
		ComponentEntry e = new ComponentEntry(jasperResources, componentFile, new ProcessingContext(this), component, pattern, newId(), originatorId);
		e.preprocess();
		toProcess.add(e);
		//toProcessChanged = true;
	}
	// End of add via ProcessorContext
	
	public void processChanges() throws PreprocessingException,ProcessingException {
		this.state = ProcessingState.PROCESSING;
		List<FileToProcess> preprocessed = new ArrayList<>();
		try {
			for(FileToProcess f : filesToProcess) {
				List<Processable> entries = f.preprocess();
				preprocessed.add(f);
				// Remove build component if there is one (there will be one or zero)
				if (entries!=null) {
					BuildComponentEntry b = null;
					for(Processable e : entries) {
						if (e instanceof BuildComponentEntry) {
							b = (BuildComponentEntry)e;
							break;
						}
					}
					if (b!=null) {
						entries.remove(b);
						buildComponentsToProcess.add(b);
						//toProcessChanged = true;
					}
					if (entries.size()>0) {
						toProcess.addAll(entries);
						//toProcessChanged = true;
					}
				} else {
					// Do something...
					//throw new JasperException("");
				}
			}
		} finally {
			for(FileToProcess f : preprocessed) {
				filesToProcess.remove(f);
			}
		}

		filesToProcess.clear();
		runProcessing();

		if (state==ProcessingState.PROCESSING) {
			//userFilesToWrite.clear();
			this.state = ProcessingState.COMPLETE;
		}
	}

	List<ResourceWatcherRecord> activeResourceWatcherRecords = new ArrayList<>();
	public void checkResourceWatchers() {
		Map<String,UserFile> userFiles = applicationManager.getUserFiles();
		for(Tracked tracked : items) {
			if (tracked instanceof ResourceWatcherRecord) {
				ResourceWatcherRecord rec = (ResourceWatcherRecord)tracked;
				long last = rec.getLastRun();
				String path = rec.getPath();

				for(Entry<String,UserFile> entry : userFiles.entrySet()) {
					//String path = entry.getKey();
					UserFile userFile = entry.getValue();
					if (last < userFile.getLastModified()) {
						if (entry.getKey().startsWith(path)) {
							if (!activeResourceWatcherRecords.contains(rec)) {
								activeResourceWatcherRecords.add(rec);
								toProcess.add(rec.entry());
							}
						}
					}
				}
				/* processingManager should not concern itself with writing user files.
				for(UserFile file : userFilesToWrite) {
					if (file.getPath().startsWith(path)) {
						if (!activeResourceWatcherRecords.contains(rec)) {
							activeResourceWatcherRecords.add(rec);
							toProcess.add(rec.entry());
						}
					}
				}
				*/
			}
		}
	}
	
	protected void logErrorState(List<ProcessorLogMessage> msgs) {
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
	
	protected void runProcessing() {
		System.out.println("Starting processing, found "+toProcess.size()+" items to process");

		while(toProcess.size()>0) {
			Collections.sort(toProcess);
			Processable p = toProcess.get(0);
			if (p.getPriority()<0) {
				jasperResources.engineDebug("Skipping component '"+p.getName()+"' with invalid priority");
				toProcess.remove(0);
			} else {
				String name = p.getName();
				if (name!=null) {
					System.out.println("Processing component "+name);
				}
				if (p.process()) {
					p.commitChanges();
					toProcess.remove(p);
					if (p instanceof Tracked) {
						items.add((Tracked)p);
					}
				} else {
					logErrorState(p.getMessages());
					p.rollbackChanges();
					this.state = ProcessingState.ERROR;
					break;
				}
			}
		}

		// Finish processing of build components
		while(buildComponentsToProcess.size()>0) {
			BuildComponentEntry e = buildComponentsToProcess.get(0);
			System.out.println("Processing component "+e.getName());
			boolean result = e.process();
			if (result) {
				e.commitChanges();
				e.getFolder().setBuildComponentEntry(e);
				buildComponentsToProcess.remove(0);
				this.items.add(e);
			} else {
				logErrorState(e.getMessages());
				this.state = ProcessingState.ERROR;
				break;
				//throw new JasperException("Processing failed");
			}
		}
		if (state==ProcessingState.ERROR) {
			return;
		}

		activeResourceWatcherRecords.clear();
	}

	// Create a system attribute from system attributes file
	public void originateFromSystemAttributesFile(String name,String type) {
		attributes.put(name,type);
		if (!systemAttributesFromFile.contains(name)) {
			systemAttributesFromFile.add(name);
		}
	}
	
	// Create Processable IDs
	private int nextId = 1;
	public int newId() {
		return nextId++;
	}
	
	public EnginePatterns getPatterns() {
		return patterns;
	}

	public EngineLanguages getLanguages() {
		return languages;
	}

	/*
	public List<ResourceWatcherRecord> getResourceWatcherRecords() {
		return resourceWatcherRecords;
	}
	*/

	// Access source files (add and get - both operations produce a dependency)
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
	public void originateSourceFile(int id,String path) {
		List<Integer> ids = sourceFileOriginators.get(path);
		if (ids==null) {
			ids = new ArrayList<>();
			sourceFileOriginators.put(path, ids);
		}
		if (!ids.contains(id)) {
			ids.add(id);
		}
	}
	public SourceFile getSourceFile(String path) {
		return applicationManager.getSourceFile(path);
	}

	// Access attributes, variable types, objects
	public Map<String,String> getAttributes() {
		return attributes;
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
	public Map<String,Object> getObjects() {
		return objects;
	}
	public Map<String, List<Integer>> getObjectOriginators() {
		return objectOriginators;
	}

	// Get originators: attributes, variable types, objects
	public Map<String, List<Integer>> getAttributeOriginators() {
		return attributeOriginators;
	}
	public Map<String,List<Integer>> getTypeOriginators(String lang) {
		Map<String,List<Integer>> ret = null;
		ret = variableTypeOriginators.get(lang);
		if (ret==null) {
			ret = new HashMap<>();
			variableTypeOriginators.put(lang, ret);
		}
		return ret;
	}

	// Get dependencies: attributes, variable types, objects
	public Map<String, List<Integer>> getAttributeDependencies() {
		return attributeDependencies;
	}
	public Map<String,List<Integer>> getTypeDependencies(String lang) {
		Map<String,List<Integer>> ret = null;
		ret = variableTypeDependencies.get(lang);
		if (ret==null) {
			ret = new HashMap<>();
			variableTypeDependencies.put(lang, ret);
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

	public List<SystemAttributeSnapshot> getSystemAttributeSnapshots() {
		List<SystemAttributeSnapshot> ret = new ArrayList<>();

		for(Entry<String,String> entry : attributes.entrySet()) {
			String name = entry.getKey();
			String type = entry.getValue();
			List<Integer> origs = new ArrayList<>();
			List<Integer> deps = new ArrayList<>();
			if (this.attributeOriginators.containsKey(name)) {
				origs.addAll(this.attributeOriginators.get(name));
			}
			if (this.attributeDependencies.containsKey(name)) {
				deps.addAll(this.attributeDependencies.get(name));
			}
			ret.add(new SystemAttributeSnapshot(name, type, "Placeholder", origs, deps));
		}

		return ret;
	}

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
				sn.setSystemAttributesOriginated(searchMapForId(this.attributeOriginators, e.getId()));
				sn.setSystemAttributeDependencies(searchMapForId(this.attributeDependencies, e.getId()));
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
	
}

