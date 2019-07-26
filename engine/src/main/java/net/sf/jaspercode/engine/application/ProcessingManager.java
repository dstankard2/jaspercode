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
import net.sf.jaspercode.api.resources.FolderWatcher;
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
import net.sf.jaspercode.engine.exception.EngineException;
import net.sf.jaspercode.engine.processing.AddComponentFileEntry;
import net.sf.jaspercode.engine.processing.BuildComponentEntry;
import net.sf.jaspercode.engine.processing.ComponentEntry;
import net.sf.jaspercode.engine.processing.FileToProcess;
import net.sf.jaspercode.engine.processing.FolderWatcherEntry;
import net.sf.jaspercode.engine.processing.Processable;
import net.sf.jaspercode.engine.processing.ProcessableBase;
import net.sf.jaspercode.engine.processing.ProcessingState;
import net.sf.jaspercode.engine.processing.ProcessorLog;
import net.sf.jaspercode.engine.processing.RemoveComponentFileEntry;
import net.sf.jaspercode.engine.processing.FolderWatcherRecord;
import net.sf.jaspercode.engine.processing.Tracked;
import net.sf.jaspercode.engine.processing.UnloadComponentFileEntry;

public class ProcessingManager {
	
	private ApplicationManager applicationManager = null;
	private EnginePatterns patterns = null;
	private EngineLanguages languages = null;
	JasperResources jasperResources = null;
	private ProcessingState state = ProcessingState.TO_PROCESS;
	boolean userFileCheckRequired = false;
	private ProcessorLog applicationLog = null;

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

	private List<FileToProcess> filesToProcess = new ArrayList<>();
	private List<Processable> toProcess = new ArrayList<>();
	List<BuildComponentEntry> buildComponentsToProcess = new ArrayList<>();
	List<BuildComponentEntry> buildComponentsInProcess = new ArrayList<>();

	private Map<String,String> globalSystemAttributes = new HashMap<>();

	// Should we check folder watchers at the start of processing?
	private boolean folderWatchersUpdated = false;
	
	public void addGlobalSystemAttribute(String name, String type) {
		if (globalSystemAttributes.get(name)==null) {
			globalSystemAttributes.put(name, type);
		}
		if (systemAttributes.get(name)==null) {
			systemAttributes.put(name, type);
		}
	}
	public void removeGlobalSystemAttribute(String name) {
		// TODO: Might need to do something else here
		globalSystemAttributes.remove(name);
		List<Integer> deps = this.systemAttributeDependencies.get(name);
		List<Integer> originators = systemAttributeOriginators.get(name);

		if (originators.size()==0) {
			this.systemAttributes.remove(name);
		}
		
		for(Integer id : deps) {
			unloadItem(id, false);
		}
		for(Integer id : originators) {
			unloadItem(id, false);
		}
	}

	/*
	protected void unloadItemsForAttribute(String systemAttribute) {
		List<Integer> deps = this.systemAttributeDependencies.get(systemAttribute);
		List<Integer> originators = systemAttributeOriginators.get(systemAttribute);

		for(Integer id : deps) {
			unloadItem(id, false);
		}
		for(Integer id : originators) {
			unloadItem(id, false);
		}
	}
	*/

	public ProcessingState getState() {
		return state;
	}

	public ProcessingManager(ApplicationManager applicationManager, EnginePatterns patterns,
			EngineLanguages languages, JasperResources resourceContext,
			ProcessorLog applicationLog) {
		this.applicationManager = applicationManager;
		this.patterns = patterns;
		this.languages = languages;
		this.jasperResources = resourceContext;
		this.applicationLog = applicationLog;
	}

	/**
	 * Miscellaneous internal APIs 
	 */
	private List<FolderWatcherRecord> getFolderWatcherRecords() {
		List<FolderWatcherRecord> ret = new ArrayList<>();
		
		for(Tracked item : items) {
			if (item instanceof FolderWatcherRecord) {
				ret.add((FolderWatcherRecord)item);
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
		this.filesToProcess.add(new RemoveComponentFileEntry(file, this));
	}

	public void componentFileAdded(ComponentFile file) {
		this.filesToProcess.add(new AddComponentFileEntry(file, this, this.applicationLog));
	}
	
	// Unloads component files whose path starts with the given string, and marks the items to be reprocessed.
	// This happens because of a change in systemAttributes.properties or jasper.properties
	public void unloadComponentFiles(String pathPrefix) {
		if (componentFiles.size()==0) return;
		
		for(Entry<String,ComponentFile> entry : componentFiles.entrySet()) {
			String path = entry.getKey();
			if (path.startsWith(pathPrefix)) {
				filesToProcess.add(0, new UnloadComponentFileEntry(entry.getValue(), this));
			}
		}
	}
	
	public void processChanges(boolean userFileChangesDetected) {
		// Before we run processing, look at filesToProcess and process them.
		if (userFileChangesDetected) {
			this.userFileCheckRequired = true;
		}

		this.state = ProcessingState.PROCESSING;
		
		while(filesToProcess.size()>0) {
			FileToProcess f = filesToProcess.get(0);
			if (f.process()) {
				f.commitChanges();
				filesToProcess.remove(0);
			} else {
				this.state = ProcessingState.ERROR;
				break;
			}
		}

		if (state!=ProcessingState.ERROR) {
			runProcessing();
		}

	}

	/**
	 * End of methods used by applicationManager
	 */



	/**
	 * Start of methods for processing
	 */

	public void addComponentFile(ComponentFile componentFile) throws EngineException {
		BuildComponentEntry buildComp = null;
		List<ComponentEntry> newComps = new ArrayList<>();
		for(Component comp : componentFile.getComponentSet().getComponent()) {
			if (comp instanceof BuildComponent) {
				if (buildComp!=null) {
					throw new EngineException("File contained more than one build component");
				}
				BuildComponent b = (BuildComponent)comp;
				ProcessingContext processingContext = new ProcessingContext(this);
				BuildComponentPattern pattern = patterns.getBuildPattern(b.getClass());
				buildComp = new BuildComponentEntry(componentFile, processingContext, jasperResources, b, pattern, newId(), 0, applicationManager.getOutputDirectory(), applicationManager.getApplicationLog());
				//buildComp.preprocess();
			} else {
				ProcessingContext processingContext = new ProcessingContext(this);
				ComponentPattern pattern = this.patterns.getPattern(comp.getClass());
				ComponentEntry e = new ComponentEntry(jasperResources, componentFile, processingContext, comp, pattern, newId(), 0);
				//e.preprocess();
				newComps.add(e);
			}
		}
		if (buildComp!=null)
			buildComponentsToProcess.add(buildComp);
		for(ComponentEntry e : newComps) {
			toProcess.add(e);
		}
		componentFiles.put(componentFile.getPath(), componentFile);
	}

	public void removeComponentFile(ComponentFile componentFile, boolean remove) {
		if (componentFiles.get(componentFile.getPath())!=componentFile) {
			System.err.println("Attempted to remove component file '"+componentFile.getPath()+"' but is not the same as the file registered with the processingManager");
		}
		
		List<Integer> idsToRemove = new ArrayList<>();
		
		for(Component comp : componentFile.getComponentSet().getComponent()) {
			int id = 0;
			// TODO: Unload/remove the component
			if (comp instanceof BuildComponent) {
				if (componentFile.getFolder().getBuildComponent()!=null) {
					id = componentFile.getFolder().getBuildComponent().getId();
				}
				//if ((componentFile) && ()) {
				//if (comp==componentFile.getFolder().getBuildComponent().getBuildComponent()) {
				//}
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
			if (id>0) {
				idsToRemove.add(id);
			}
		}
		
		for(Integer id : idsToRemove) {
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
	protected void removeItem(Tracked item, boolean permanent) {
		List<Integer> toRemove = new ArrayList<>();
		items.remove(item);

		boolean isFolderWatcher = item instanceof FolderWatcherRecord;

		// Unload this item's originator
		//  TODO: Determine if this makes sense
		if ((item.getOriginatorId() > 0) && (!isFolderWatcher)) {
			System.err.println("To remove or not to remove originator - currently not removing");
			//System.err.println("To remove or not to remove originator - currently removing");
			//toRemove.add(item.getOriginatorId());
		}

		// Unload items that originate from this item
		for(Tracked i : items) {
			if (i.getOriginatorId()==item.getId()) {
				toRemove.add(i.getId());
			}
		}

		for(Integer i : toRemove) {
			this.unloadItem(i, permanent);
		}

		// TODO: Create a separate rule for cleaning inProcess items?
		// May need to check resource watchers
		// Remove this item from pending processing
		this.buildComponentsToProcess.remove(item);
		this.buildComponentsInProcess.remove(item);
		if (item instanceof Processable) {
			this.toProcess.remove((Processable)item);
		}
		
		if (item instanceof FolderWatcherRecord) {
			FolderWatcherRecord e = (FolderWatcherRecord)item;
			e.clearFilesProcessed();

			// Clear folder watcher entries from toProcess.
			List<FolderWatcherEntry> r = new ArrayList<>();
			for(Processable p : toProcess) {
				if (p instanceof FolderWatcherEntry) {
					FolderWatcherEntry en = (FolderWatcherEntry)p;
					if (en.getRecord()==e) {
						r.add(en);
					}
				}
			}
			toProcess.removeAll(r);
			folderWatchersUpdated = true;
		}
		
		if (item instanceof BuildComponentEntry) {
			BuildComponentEntry e = (BuildComponentEntry)item;
			ApplicationFolderImpl folder = e.getFolder();
			folder.setBuildComponentEntry(null);
			buildComponentsToProcess.remove(e);
			buildComponentsInProcess.remove(e);
			this.unloadComponentFiles(folder.getPath());
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
	// They are to be removed permanently
	protected void removeResourceWatchersFromOriginator(int id) {
		List<Integer> toRemove = new ArrayList<>();
		for(FolderWatcherRecord record : this.getFolderWatcherRecords()) {
			if (record.getOriginatorId()==id) {
				toRemove.add(record.getId());
			}
		}
		for(Integer r : toRemove) {
			this.unloadItem(r, true);
		}
	}

	// Parameter causeUnload will cause other items dependent on the same key to be
	// unloaded as well.
	// causeUnload is false for system attribute dependency
	// causeUnload is true otherwise.
	protected void removeItemEntries(Map<String, List<Integer>> map, int id, boolean causeUnload, boolean causeRemoveSystemAttribute, Map<String,?> actualData) {
		List<Integer> toUnload = new ArrayList<>();
		List<String> entriesToRemove = new ArrayList<>();
		List<String> systemAttributesToRemove = new ArrayList<>();

		for (Entry<String, List<Integer>> entry : map.entrySet()) {
			List<Integer> ids = entry.getValue();
			int index = ids.indexOf(id);
			if (index >= 0) {
				ids.remove(index);
				if (causeUnload) {
					toUnload.addAll(ids);
				}
				if (causeRemoveSystemAttribute) {
					systemAttributesToRemove.add(entry.getKey());
				}
				if (ids.size()==0) entriesToRemove.add(entry.getKey());
			}
		}
		for (Integer i : toUnload) {
			this.unloadItem(i, false);
		}
		for(String s : entriesToRemove) {
			map.remove(s);
			if (actualData!=null)
				actualData.remove(s);
		}
		for(String s : systemAttributesToRemove) {
			systemAttributes.remove(s);
		}
	}
	protected void unloadItemFromTypes(Map<String,Map<String,List<Integer>>> typeMap, int id, boolean causeUnload) {
		List<Integer> toUnload = new ArrayList<>();
		
		for(Entry<String,Map<String,List<Integer>>> langEntry : typeMap.entrySet()) {
			List<String> toRemove = new ArrayList<>();
			for(Entry<String,List<Integer>> typeEntry : langEntry.getValue().entrySet()) {
				List<Integer> ids = typeEntry.getValue();
				int index = ids.indexOf(id);
				if (index>=0) {
					ids.remove(index);
					if (causeUnload) {
						toUnload.addAll(ids);
						if (variableTypes.get(langEntry.getKey()).containsKey(typeEntry.getKey())) {
							this.variableTypes.get(langEntry.getKey()).remove(typeEntry.getKey());
						}
					}
					if (ids.size()==0) {
						toRemove.add(typeEntry.getKey());
					}
				}
			}
			for(String r : toRemove) {
				langEntry.getValue().remove(r);
			}
		}
		for(Integer i : toUnload) {
			this.unloadItem(i, false);
		}
	}

	protected void removeTrackingEntries(int id) {
		// System attributes
		removeItemEntries(this.systemAttributeDependencies, id, false, false, null);
		removeItemEntries(this.systemAttributeOriginators, id, false, true, this.systemAttributes);

		// Objects
		removeItemEntries(this.objectOriginators, id, true, false, this.objects);

		// Types
		unloadItemFromTypes(this.variableTypeDependencies, id, false);
		unloadItemFromTypes(this.variableTypeOriginators, id, true);
	}

	// Unloads the item.  Re-adds it if remove is false
	protected void unloadItem(int id, boolean remove) {
		Tracked tracked = getItem(id);

		if ((tracked==null) || (id<1)) return;

		jasperResources.engineDebug("Removing item id "+id+" with name '"+tracked.getName()+"' with remove = "+remove);
		removeItem(tracked, remove);
		removeTrackingEntries(id);
		removeResourceWatchersFromOriginator(id);
		removeSourceFilesFromOriginator(id);

		if (!remove) {
			if (tracked instanceof BuildComponentEntry) {
				buildComponentsToProcess.add((BuildComponentEntry)tracked);
			} else if (tracked instanceof Processable) {
				toProcess.add((Processable)tracked);
			} else if (tracked instanceof FolderWatcherRecord) {
				items.add((FolderWatcherRecord)tracked);
				folderWatchersUpdated = true;
				//this.checkFolderWatchers();
				System.out.println("TODO: Determine what to do on folder watcher re-add");
			} else {
				System.err.println("Couldn't re-add item to processing");
			}
		}
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
	
	private void checkFolderWatchers() {
		if ((!this.userFileCheckRequired) && (!this.folderWatchersUpdated)) {
			return;
		}
		
		Map<String,UserFile> userFiles = applicationManager.getUserFiles();

		for(FolderWatcherRecord rec : this.getFolderWatcherRecords()) {
			String path = rec.getPath();
			Map<String,Long> processed = rec.getFilesProcessed();
			for(Entry<String,UserFile> entry : userFiles.entrySet()) {
				boolean previouslyApplied = false;
				boolean apply = false;
				String filePath = entry.getKey();
				UserFile userFile = entry.getValue();
				if (filePath.startsWith(path)) {
					long fileModified = userFile.getLastModified();
					if (processed.get(filePath)!=null) {
						previouslyApplied = true;
						if (fileModified > processed.get(filePath)) {
							apply = true;
						}
					} else {
						apply = true;
					}
				}
				if (apply) {
					FolderWatcherEntry e = rec.entry(userFile);
					if (previouslyApplied) {
						this.unloadItem(e.getId(), false);
					}
					toProcess.add(e);
					this.jasperResources.engineDebug("Item '"+e.getId()+"' is adding watcher to process for file '"+userFile.getPath()+"'");
					processed.put(filePath, userFile.getLastModified());
				}
			}
			userFileCheckRequired = false;
			folderWatchersUpdated = false;
		}
	}
	
	private void runProcessing() {
		state = ProcessingState.PROCESSING;

		checkFolderWatchers();
		
		while(buildComponentsToProcess.size()>0) {
			//System.out.println("In processing and there are "+buildComponentsToProcess.size()+" build components to process");
			BuildComponentEntry e = buildComponentsToProcess.get(0);
			if (!e.preprocess()) {
				this.logMessages(e.getMessages());
				this.state = ProcessingState.ERROR;
				return;
			}
			if (e.init()) {
				e.commitChanges();
				buildComponentsToProcess.remove(e);
				buildComponentsInProcess.add(e);
				e.getFolder().setBuildComponentEntry(e);
				checkFolderWatchers();
			} else {
				// Halt processing
				this.logMessages(e.getMessages());
				this.state = ProcessingState.ERROR;
				return;
			}
		}

		while(toProcess.size()>0) {
			jasperResources.engineDebug("In processing and there are "+toProcess.size()+" items to process");
			Collections.sort(toProcess);
			Processable p = toProcess.get(0);
			if (p.getPriority()<0) {
				//jasperResources.engineDebug("Skipping component '"+p.getName()+"' with invalid priority");
				toProcess.remove(p);
			} else {
				String name = p.getName();
				if (name!=null) {
					System.out.println("Processing component "+name);					
				}
				if (!p.preprocess()) {
					logMessages(p.getMessages());
					state = ProcessingState.ERROR;
					return;
				}
				if (p.process()) {
					p.commitChanges();
					toProcess.remove(p);
					if (p instanceof Tracked) {
						items.add((Tracked)p);
					}
					checkFolderWatchers();
				} else {
					logMessages(p.getMessages());
					p.rollbackChanges();
					state = ProcessingState.ERROR;
					return;
				}
			}
		}
		
		// Finish build components
		while(buildComponentsInProcess.size()>0) {
			BuildComponentEntry e = buildComponentsInProcess.get(0);
			String name = e.getName();
			if (name!=null) {
				System.out.println("Finishing build component "+name);
			}
			if (e.process()) {
				e.commitChanges();
				buildComponentsInProcess.remove(e);
				items.add(e);
			} else {
				e.rollbackChanges();
				logMessages(e.getMessages());
				state = ProcessingState.ERROR;
				return;
			}
		}
		
		state = ProcessingState.COMPLETE;
	}



	/**
	 * Required by processingContext
	 */
	
	public void addFolderWatcher(int originatorId, ComponentFile componentFile,String path,FolderWatcher watcher) {
		FolderWatcherRecord rec = new FolderWatcherRecord(path,jasperResources, new ProcessingContext(this), watcher,componentFile,newId(),originatorId);
		this.items.add(rec);
		folderWatchersUpdated = true;
	}
	public void addComponent(int originatorId, Component component, ComponentFile componentFile) {
		ComponentPattern pattern = patterns.getPattern(component.getClass());
		ComponentEntry e = new ComponentEntry(jasperResources, componentFile, new ProcessingContext(this), component, pattern, newId(), originatorId);
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
		Map<String,List<Integer>> ret = variableTypeOriginators.get(lang);
		if (ret==null) {
			ret = new HashMap<String,List<Integer>>();
			variableTypeOriginators.put(lang, ret);
		}
		return ret;
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
			
			if (item instanceof FolderWatcherRecord) {
				// TODO: Determine what to do here.
				//FolderWatcherRecord rec = (FolderWatcherRecord)item;
			} else {
				ProcessableBase e = null;
				
				if (item instanceof ComponentEntry) {
					e = (ProcessableBase)item;
				} else if (item instanceof FolderWatcherRecord) {
					System.err.println("Could not create snapshot for Tracked class "+item.getClass().getCanonicalName());
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
			}
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

