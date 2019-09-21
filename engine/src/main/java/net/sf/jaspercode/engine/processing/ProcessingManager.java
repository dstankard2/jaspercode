package net.sf.jaspercode.engine.processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.util.Set;

import net.sf.jaspercode.api.config.BuildComponent;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.logging.ProcessorLogLevel;
import net.sf.jaspercode.api.plugin.ProcessorLogMessage;
import net.sf.jaspercode.api.resources.FileProcessor;
import net.sf.jaspercode.api.resources.FolderWatcher;
import net.sf.jaspercode.engine.BuildComponentPattern;
import net.sf.jaspercode.engine.ComponentPattern;
import net.sf.jaspercode.engine.EngineLanguages;
import net.sf.jaspercode.engine.EnginePatterns;
import net.sf.jaspercode.engine.application.ApplicationManager;
import net.sf.jaspercode.engine.application.DependencyManager;
import net.sf.jaspercode.engine.application.JasperResources;
import net.sf.jaspercode.engine.application.ProcessingContext;
import net.sf.jaspercode.engine.definitions.ApplicationFolderImpl;
import net.sf.jaspercode.engine.definitions.ComponentFile;
import net.sf.jaspercode.engine.definitions.UserFile;

public class ProcessingManager {

	private ProcessorLog appLog = null;
	private ApplicationManager applicationManager = null;
	private EnginePatterns patterns = null;
	private ProcessingState state = ProcessingState.TO_PROCESS;
	private JasperResources jasperResources = null;
	private ProcessingDataManager processingDataManager = null;
	private DependencyManager dependencyManager = null;

	// Files that have been updated in a scan - Handle these first
	private List<ComponentFile> componentFilesAdded = new ArrayList<>();
	private List<ComponentFile> componentFilesRemoved = new ArrayList<>();
	private List<String> userFilesAdded = new ArrayList<>();
	private List<String> userFilesRemoved = new ArrayList<>();
	private List<String> userFilesChanged = new ArrayList<>();

	// processing Tracking
	
	//  Items
	private List<Item> items = new ArrayList<>();
	
	// Build components
	private List<BuildComponentItem> buildItemsToInit = new ArrayList<>();
	private List<BuildComponentItem> buildItemsToGenerate = new ArrayList<>();
	private List<BuildComponentItem> buildItems = new ArrayList<>();

	// Processables to process
	private List<Processable> toProcess = new ArrayList<>();

	// Component files loaded
	private Map<String,ComponentFile> componentFiles = new HashMap<>();

	// The key is an id, the value is a file path
	private Map<Integer,String> fileProcessorItems = new HashMap<>();

	public ProcessingManager(JasperResources jasperResources,EnginePatterns patterns, EngineLanguages languages,ApplicationManager applicationManager,ProcessorLog appLog) {
		this.jasperResources = jasperResources;
		this.patterns = patterns;
		this.applicationManager = applicationManager;
		this.appLog = appLog;
		Map<String,String> globalAttributes = new HashMap<>();
		this.dependencyManager = new DependencyManager(globalAttributes);
		this.processingDataManager = new ProcessingDataManager(applicationManager, dependencyManager, languages, jasperResources,globalAttributes);
	}

	protected List<FolderWatcherItem> getFolderWatchers() {
		List<FolderWatcherItem> ret = new ArrayList<>();
		
		for(Item item : items) {
			if (item instanceof FolderWatcherItem) {
				ret.add((FolderWatcherItem)item);
			}
		}
		
		return ret;
	}
	
	protected Item getItem(int id) {
		for(Item item : items) {
			if (item.getId()==id) return item;
		}
		return null;
	}
	
	protected boolean isFileProcessor(int id) {
		return fileProcessorItems.get(id) != null;
	}

	protected Set<Integer> getFileProcessorIds(String path) {
		Set<Integer> ret = new HashSet<>();

		for(Entry<Integer,String> entry : fileProcessorItems.entrySet()) {
			if (entry.getValue().equals(path)) {
				ret.add(entry.getKey());
			}
		}

		return ret;
	}

	protected ComponentItem getComponentItem(Component component) {
		for(Item item : items) {
			if (item instanceof ComponentItem) {
				ComponentItem i = (ComponentItem)item;
				if (i.getComponent()==component) {
					return i;
				}
			}
		}
		return null;
	}

	public void setGlobalSystemAttributes(Map<String,String> attributes) {
		Set<Integer> toUnload = processingDataManager.setGlobalSystemAttributes(attributes);
		for(Integer i : toUnload) {
			removeItem(i, false);
		}
	}

	// Unloads the component files and re-adds them for processing.
	public void unloadComponentFiles(String folderPath) {
		for(Entry<String,ComponentFile> entry : componentFiles.entrySet()) {
			if (entry.getKey().startsWith(folderPath)) {
				ComponentFile f = entry.getValue();
				if (!componentFilesRemoved.contains(f)) {
					this.componentFilesRemoved.add(entry.getValue());
				}
				if (!componentFilesAdded.contains(f)) {
					this.componentFilesAdded.add(entry.getValue());
				}
			}
		}
	}

	public void addUserFile(String path) {
		userFilesAdded.add(path);
	}
	public void removeUserFile(String path) {
		userFilesRemoved.add(path);
	}
	public void changeUserFile(String path) {
		userFilesChanged.add(path);
	}
	public void removeComponentFile(ComponentFile componentFile) {
		componentFilesRemoved.add(componentFile);
	}
	public void addComponentFile(ComponentFile componentFile) {
		componentFilesAdded.add(componentFile);
	}

	public ProcessingState getState() {
		return state;
	}
	
	protected int _currentId = 0;
	protected int newId() {
		_currentId++;
		return _currentId;
	}

	public void processChanges() {
		// Look for userFiles removed
		for(String f : userFilesRemoved) {
			userFileRemoved(f);
		}
		userFilesRemoved.clear();
		// Look for component Files removed
		for(ComponentFile file : componentFilesRemoved) {
			for(Component comp : file.getComponentSet().getComponent()) {
				ComponentItem item = getComponentItem(comp);
				if (item!=null) {
					removeItem(item.getId(), true);
				}
			}
		}
		componentFilesRemoved.clear();
		
		// Look for component Files Added - add component entries
		for(ComponentFile f : componentFilesAdded) {
			for(Component comp : f.getComponentSet().getComponent()) {
				Map<String,String> configs = ProcessingUtilities.getConfigs(f);
				if (comp instanceof BuildComponent) {
					int id = newId();
					BuildComponent buildComp = (BuildComponent)comp;
					BuildComponentPattern pattern =  patterns.getBuildPattern(buildComp.getClass());
					// TODO: Real processing context from processing manager
					ProcessingContext pctx = new ProcessingContext(this, processingDataManager);
					BuildComponentItem i = new BuildComponentItem(id, buildComp, pattern, configs, jasperResources, pctx, f);
					buildItemsToInit.add(i);
				} else {
					ComponentPattern pattern = patterns.getPattern(comp.getClass());
					ProcessingContext ctx = new ProcessingContext(this, processingDataManager);
					int id = newId();
					ComponentItem item = new ComponentItem(id, comp, ctx, f, configs, 0, pattern, jasperResources);
					toProcess.add(item);
					items.add(item);
				}
			}
		}
		componentFilesAdded.clear();
		
		// Look for userFiles added - check folder watchers
		if (userFilesAdded.size()>0) {
			List<FolderWatcherItem> l = this.getFolderWatchers();
			if (l.size()>0) {
				for(String f : userFilesAdded) {
					applicationManager.getUserFiles().get(f);
					for(FolderWatcherItem item : l) {
						if (f.startsWith(item.getPath())) {
							// If the file was added then add toProcess
							FolderWatcherProcessable proc = item.getProc(f);
							toProcess.add(proc);
						}
					}
				}
			}
			userFilesAdded.clear();
		}

		if (userFilesChanged.size()>0) {
			userFilesChanged.clear();
		}
		
		runProcessables();
	}
	
	protected void errorState(ProcessorLog log) {
		this.state = ProcessingState.ERROR;
		List<ProcessorLogMessage> msgs = log.getMessages(false);
		
		for(ProcessorLogMessage msg : msgs) {
			String m = String.format("[%s] %s", msg.getLevel().name(), msg.getMessage());
			if (msg.getLevel()==ProcessorLogLevel.ERROR) {
				System.err.println(m);
				if ((msg.getThrowable()!=null) && (jasperResources.debug())) {
					msg.getThrowable().printStackTrace();
				}
			} else {
				System.out.println(m);
				if ((msg.getThrowable()!=null) && (jasperResources.debug())) {
					msg.getThrowable().printStackTrace(System.out);
				}
			}
		}
	}
	
	protected void runProcessables() {
		this.state = ProcessingState.PROCESSING;

		jasperResources.engineDebug("runProcessables - There are "+buildItemsToInit.size()+" builds to init");
		jasperResources.engineDebug("runProcessables - There are "+buildItemsToGenerate.size()+" builds to generate");
		jasperResources.engineDebug("runProcessables - There are "+toProcess.size()+" processables to process");

		// Initialize build components to process
		while(buildItemsToInit.size()>0) {
			BuildComponentItem b = buildItemsToInit.get(0);

			appLog.info("Initializing build '"+b.getName()+"'");
			appLog.outputToSystem();
			boolean result = b.init();
			if (result) {
				if (b.commitChanges()) {
					buildItemsToGenerate.add(b);
					buildItemsToInit.remove(b);
				} else {
					errorState(b.getLog());
					return;
				}
			} else {
				errorState(b.getLog());
				return;
			}
		}
		
		// Update file watcher processables with the most up to date files
		// If the file isn't there, the processable will have to be removed.
		List<Integer> idsToRemove = new ArrayList<>();
		for(Processable p : toProcess) {
			if (p instanceof FileProcessorItem) {
				FileProcessorItem i = (FileProcessorItem)p;
				String path = i.getFilePath();
				UserFile file = applicationManager.getUserFiles().get(path);
				if (file!=null) {
					try {
						i.fileUpdated(file);
					} catch(Exception e) {
						errorState(i.getLog());
						return;
					}
				} else {
					idsToRemove.add(i.getId());
				}
			}
		}
		for(Integer id : idsToRemove) {
			this.removeItem(id, true);
		}
		
		// Run processables
		while(toProcess.size()>0) {
			Collections.sort(toProcess);
			Processable p = toProcess.get(0);

			appLog.info("Processing '"+p.getName()+"'");
			appLog.outputToSystem();
			boolean result = p.process();
			if (result) {
				result = p.commitChanges();
				if (result) {
					toProcess.remove(p);
				} else {
					errorState(p.getLog());
					return;
				}
			} else {
				errorState(p.getLog());
				return;
			}
		}
		
		// generate builds
		while(buildItemsToGenerate.size()>0) {
			BuildComponentItem b = buildItemsToGenerate.get(0);
			appLog.info("Generating build '"+b.getName()+"'");
			boolean result = b.process();
			if (result) {
				b.commitChanges();
				buildItemsToGenerate.remove(b);
				buildItems.add(b);
			} else {
				errorState(b.getLog());
				return;
			}
		}

		this.state = ProcessingState.COMPLETE;
	}

	protected void userFileRemoved(String path) {
		boolean fileChanged = this.userFilesChanged.contains(path);
		// If the file changed, we still want to run file processors
		if (!fileChanged) {
			// Remove file processors permanently
			Set<Integer> fileProcessorIds = getFileProcessorIds(path);
			for(Integer id : fileProcessorIds) {
				removeItem(id, true);
			}
		}

		// Remove folder watchers for this path
		List<FolderWatcherItem> folderWatchers = getFolderWatchers();
		for(FolderWatcherItem item : folderWatchers) {
			if (path.indexOf(item.getPath())==0) {
				if (fileChanged) {
					// If the file is changed then not permanent remove
					removeItem(item.getId(), false);
				} else {
					// If the file is removed (not changed) then permanent remove
					removeItem(item.getId(), true);
				}
			}
		}
	}

	// When re-adding a folderWatcher, evaluate userFiles to process
	protected void removeItem(int id,boolean permanent) {
		Item item = getItem(id);
		boolean isFileProcessor = this.isFileProcessor(id);
		
		if ((item==null) && (!isFileProcessor)) return;

		// TODO: Determine if we should unload the item's originator

		if (item!=null) {
			jasperResources.engineDebug("Removing item "+item.getName()+"("+id+") with perm = "+permanent);
		} else {
			jasperResources.engineDebug("Removing item ("+id+") with permr = "+permanent);
		}
		unloadItem(id);
		
		jasperResources.engineDebug("Finished removing item "+id);

		if ((permanent) || (isFileProcessor)) return;

		// Re-add item
		if (item instanceof ComponentItem) {
			ComponentItem c = (ComponentItem)item;
			if (!toProcess.contains(c))
				toProcess.add(c);
			if (!items.contains(c))
				items.add(c);
		}
		else if (item instanceof FolderWatcherItem) {
			FolderWatcherItem f = (FolderWatcherItem)item;
			if (!items.contains(f))
				items.add(f);
			checkFilesForFolderWatcher(f);
		}
		else if (item instanceof BuildComponentItem) {
			BuildComponentItem b = (BuildComponentItem)item;
			if (!buildItemsToInit.contains(b))
				buildItemsToInit.add(b);
		}
	}
	
	protected void unloadItem(int id) {
		Item item = getItem(id);
		boolean isFileProcessor = this.isFileProcessor(id);
		Set<Integer> toRemove = null;

		//boolean isFolderWatcher = item instanceof FolderWatcherRecord;

		// remove this item from records
		if (item!=null) {
			this.items.remove(item);
		} else if (isFileProcessor) {
			this.fileProcessorItems.remove(id);
		} else {
			System.err.println("Couldn't determine how to remove item "+id);
		}
		
		toRemove = processingDataManager.removeTrackingEntries(id);
		
		// Unload items that originate from this one
		for(Item i : items) {
			if (i.getOriginatorId()==id) {
				toRemove.add(i.getId());
			}
		}

		toRemove.addAll(removeSourceFilesFromOriginator(id));

		if (isFileProcessor) {
			// Remove fileProcessor from toProcess
			for(int i=0;i<toProcess.size();i++) {
				Processable p = toProcess.get(i);
				if (p instanceof FileProcessorItem) {
					FileProcessorItem x = (FileProcessorItem)p;
					if (x.getId()==id) {
						toProcess.remove(i);
						i--;
					}
				}
			}
		} else if (item!=null) {
			if (item instanceof BuildComponentItem) {
				BuildComponentItem b = (BuildComponentItem)item;
				this.buildItems.remove(b);
				this.buildItemsToInit.remove(b);
				this.buildItemsToGenerate.remove(b);
				b.getFolder().setBuildComponentItem(null);
				this.unloadComponentFiles(b.getFolder().getPath());
			} else if (item instanceof FolderWatcherItem) {
				//FolderWatcherItem f = (FolderWatcherItem)item;
				for(int i=0;i<toProcess.size();i++) {
					Processable p = toProcess.get(i);
					if (p instanceof FolderWatcherProcessable) {
						FolderWatcherProcessable proc = (FolderWatcherProcessable)p;
						if (proc.getOriginatorId()==id) {
							toProcess.remove(i);
							i--;
						}
					}
				}
			} else if (item instanceof ComponentItem) {
				ComponentItem c = (ComponentItem)item;
				toProcess.remove(c);
			}
		} else {
			System.err.println("Item with id "+id+" had no item entry and was not a file watcher");
		}

		// Remove other items that are marked to be removed
		for(Integer i : toRemove) {
			this.removeItem(i, false);
		}
	}

	protected Set<Integer> removeSourceFilesFromOriginator(int id) {
		Set<String> toRemove = dependencyManager.getSourceFilesFromOriginator(id);
		Set<Integer> ret = new HashSet<>();
		
		for(String s : toRemove) {
			Set<Integer> addition = removeSourceFile(s);
			ret.addAll(addition);
		}
		return ret;
	}
	protected Set<Integer> removeSourceFile(String path) {
		Set<Integer> toUnload = dependencyManager.removeSourceFile(path);

		applicationManager.removeSourceFile(path);

		return toUnload;
		/*
		for(Integer i : toUnload) {
			//this.removeItem(i, true);
			this.removeItem(i, false);
		}
		*/
	}

	// Required for ProcessingContext
	// component is not a BuildComponent
	public void addComponent(int originatorId, Component component, ComponentFile componentFile) throws JasperException {
		ComponentPattern pattern = null;
		int id = newId();
		ProcessingContext processingContext = new ProcessingContext(this, processingDataManager);
		pattern = patterns.getPattern(component.getClass());
		
		if (pattern==null) {
			throw new JasperException("Couldn't find pattern for component class "+component.getClass().getCanonicalName());
		}
		
		Map<String,String> configs = ProcessingUtilities.getConfigs(componentFile);
		ComponentItem item = new ComponentItem(id, component, processingContext, componentFile, configs, originatorId, pattern, jasperResources);
		toProcess.add(item);
		items.add(item);
		jasperResources.engineDebug("Added component "+component.getComponentName());
	}

	protected void checkFilesForFolderWatcher(FolderWatcherItem item) {
		String path = item.getPath();
		// Check all user files for the given folder watcher
		Map<String,UserFile> files = applicationManager.getUserFiles();
		//String path = item.getPath();
		for(Entry<String,UserFile> entry : files.entrySet()) {
			String filePath = entry.getKey();
			if (filePath.startsWith(path)) {
				FolderWatcherProcessable proc = item.getProc(filePath);
				toProcess.add(proc);
			}
		}
	}
	public void addFolderWatcher(int originatorId, ComponentFile componentFile,String path,FolderWatcher folderWatcher) {
		int id = newId();
		Map<String,String> configs = ProcessingUtilities.getConfigs(componentFile);
		ProcessingContext processingContext = new ProcessingContext(this, processingDataManager);
		FolderWatcherItem item = new FolderWatcherItem(id, folderWatcher, originatorId, path, processingContext, componentFile, configs, jasperResources);
		items.add(item);
		checkFilesForFolderWatcher(item);
	}

	// If the file doesn't exist then this is a no-op
	// A file processor runs one time.
	public void addFileProcessor(int originatorId, ComponentFile componentFile,String path,FileProcessor fileProcessor) {
		int id = newId();
		ApplicationFolderImpl folder = componentFile.getFolder();
		ProcessingContext processingContext = new ProcessingContext(this,processingDataManager);
		Map<String,String> configs = ProcessingUtilities.getConfigs(componentFile);
		FileProcessorItem proc = new FileProcessorItem(id, path, jasperResources, processingContext, fileProcessor, componentFile, folder, configs, originatorId, jasperResources);
		UserFile userFile = applicationManager.getUserFiles().get(path);
		if (userFile!=null) {
			try {
				proc.fileUpdated(userFile);
			} catch(Exception e) {
				//  Swallow this this time.
				// TODO: Figure out what to do!
				e.printStackTrace();
			}
			this.toProcess.add(proc);
			// Track this file processor ID
			this.fileProcessorItems.put(id, path);
		}
	}

}

