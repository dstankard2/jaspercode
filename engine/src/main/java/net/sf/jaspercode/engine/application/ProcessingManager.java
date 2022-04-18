package net.sf.jaspercode.engine.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.stream.Collectors;

import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.config.BuildComponent;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.config.ComponentSet;
import net.sf.jaspercode.api.logging.ProcessorLogLevel;
import net.sf.jaspercode.api.plugin.ProcessorLogMessage;
import net.sf.jaspercode.api.resources.FileProcessor;
import net.sf.jaspercode.api.resources.FolderWatcher;
import net.sf.jaspercode.api.types.VariableType;
import net.sf.jaspercode.engine.BuildComponentPattern;
import net.sf.jaspercode.engine.ComponentPattern;
import net.sf.jaspercode.engine.EngineLanguages;
import net.sf.jaspercode.engine.EnginePatterns;
import net.sf.jaspercode.engine.JasperResources;
import net.sf.jaspercode.engine.exception.StaleObjectException;
import net.sf.jaspercode.engine.exception.StaleSourceFileException;
import net.sf.jaspercode.engine.exception.StaleVariableTypeException;
import net.sf.jaspercode.engine.files.ApplicationFolderImpl;
import net.sf.jaspercode.engine.files.ComponentFile;
import net.sf.jaspercode.engine.files.UserFile;
import net.sf.jaspercode.engine.files.WatchedResource;
import net.sf.jaspercode.engine.processing.AddedFile;
import net.sf.jaspercode.engine.processing.BuildComponentItem;
import net.sf.jaspercode.engine.processing.ComponentItem;
import net.sf.jaspercode.engine.processing.FileChange;
import net.sf.jaspercode.engine.processing.FileProcessorRecord;
import net.sf.jaspercode.engine.processing.FolderWatcherItem;
import net.sf.jaspercode.engine.processing.FolderWatcherProcessable;
import net.sf.jaspercode.engine.processing.Item;
import net.sf.jaspercode.engine.processing.Processable;
import net.sf.jaspercode.engine.processing.ProcessableChanges;
import net.sf.jaspercode.engine.processing.ProcessableContext;
import net.sf.jaspercode.engine.processing.ProcessingDataManager;
import net.sf.jaspercode.engine.processing.ProcessingUtilities;
import net.sf.jaspercode.engine.processing.ProcessorLog;
import net.sf.jaspercode.engine.processing.RemovedFile;

public class ProcessingManager implements ProcessableContext {
	ProcessingContext ctx;
	JasperResources jasperResources;
	EnginePatterns patterns;
	EngineLanguages languages;
	ProcessorLog appLog;
	ProcessingDataManager processingDataManager;

	List<Item> items = new ArrayList<>();
	List<Processable> toProcess = new ArrayList<>();
	
	List<BuildComponentItem> buildsToInit = new ArrayList<>();
	List<BuildComponentItem> buildsToProcess = new ArrayList<>();
	
	// Modifiable data added in this run - If a second item tries to modify it, then the item must be re-evaluated
	private List<String> objectsAddedThisRun = new ArrayList<>();
	private List<String> sourceFilesAddedThisRun = new ArrayList<>();
	private List<VariableType> typesAddedThisRun = new ArrayList<>();
	
	public ProcessingManager(ProcessingContext ctx, JasperResources jasperResources, EnginePatterns patterns,
			EngineLanguages languages, ProcessorLog appLog) {
		this.ctx = ctx;
		this.jasperResources = jasperResources;
		this.patterns = patterns;
		this.languages = languages;
		this.appLog = appLog;
		this.processingDataManager = new ProcessingDataManager(languages, jasperResources);
	}
	
	private int nextItemId = 1;
	public int newItemId() {
		return nextItemId++;
	}
	
	// Since items are being both added and removed, we first determine all changes to be made (add/remove) and then 
	// at the end remove all changes and add all new additions
	// This avoids a problem of trying to add something that is intended to be removed but hasn't been yet
	public void processChanges(Map<String,String> globalSystemAttributes, List<FileChange> changes) {
		Set<Item> itemsToAdd = new HashSet<>();
		Set<UserFile> userFilesAdded = new HashSet<>();
		Set<UserFile> userFilesRemoved = new HashSet<>();
		
		processingDataManager.setGlobalSystemAttributes(globalSystemAttributes);
		for(FileChange change : changes) {
			if (change instanceof AddedFile) {
				AddedFile f = (AddedFile)change;
				WatchedResource res = f.getFile();
				if (res instanceof UserFile) {
					UserFile userFile = (UserFile)res;
					userFilesAdded.add(userFile);
				} else if (res instanceof ComponentFile) {
					ComponentFile file = (ComponentFile)res;
					ApplicationFolderImpl folder = file.getFolder();
					ComponentSet set = file.getComponentSet();
					if (set!=null) {
						set.getComponent().forEach(comp -> {
							int itemId = newItemId();
							Map<String,String> configs = ProcessingUtilities.getConfigs(file, comp);
							
							if (comp instanceof BuildComponent) {
								BuildComponent build = (BuildComponent)comp;
								BuildComponentPattern pattern = patterns.getBuildPattern(build.getClass());
								BuildComponentItem item = new BuildComponentItem(itemId, build, pattern,
										jasperResources, this, file, configs);
								itemsToAdd.add(item);
							} else {
								ComponentPattern pattern = this.patterns.getPattern((Class<? extends Component>)comp.getClass());
								ComponentItem item = new ComponentItem(itemId, comp, configs, this, 
										pattern, jasperResources, -1, folder);
								itemsToAdd.add(item);
								//items.add(item);
								//toProcess.add(item);
							}
						});
					}
				}
			} else if (change instanceof RemovedFile) {
				RemovedFile f = (RemovedFile)change;
				WatchedResource res = f.getFile();
				if (res instanceof UserFile) {
					UserFile userFile = (UserFile)res;
					userFilesRemoved.add(userFile);
				} else if (res instanceof ComponentFile) {
					ComponentFile compFile = (ComponentFile)res;
					if (compFile.getComponentSet() != null) {
						compFile.getComponentSet().getComponent().forEach(comp -> {
							ComponentItem item = getComponentItem(comp);
							if (item!=null) {
								this.removeItem(item.getItemId(), true);
							}
							BuildComponentItem buildItem = getBuildComponentItem(comp);
							if (buildItem!=null) {
								this.removeItem(buildItem.getItemId(), true);
							}
						});
					}
				}
			}
		}

		userFilesRemoved.forEach(userFile -> {
			userFileRemoved(userFile);
		});
		itemsToAdd.forEach(item -> {
			this.addItem(item);
		});
		
		userFilesAdded.forEach(uf -> {
			ctx.writeUserFile(uf);
		});

		// For every folder watcher, check for user files that apply to it.
		checkFolderWatchers();

		runProcessing();

		toProcess.stream().filter(proc -> proc instanceof FolderWatcherProcessable).collect(Collectors.toList()).stream().forEach(proc -> {
			FolderWatcherProcessable p = (FolderWatcherProcessable)proc;
			p.remove();
			toProcess.remove(proc);
		});
	}
	
	private void checkFolderWatchers() {
		this.getFolderWatchers().forEach(item -> {
			checkFolderWatcherAgainstUserFiles(item);
		});
	}
	
	private void checkFolderWatcherAgainstUserFiles(FolderWatcherItem item) {
		List<String> userFilePaths = ctx.getUserFiles().keySet().stream().collect(Collectors.toList());

		userFilePaths.forEach(path -> {
			Processable proc = item.getProc(path);
			if (proc!=null) {
				toProcess.add(proc);
			}
		});
	}

	protected void errorState(ProcessorLog log) {
		List<ProcessorLogMessage> msgs = log.getMessages(true);
		
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
	
	protected void runProcessing() {
		jasperResources.getEngineLogger().debug("runProcessables - There are "+buildsToInit.size()+" builds to init");
		jasperResources.getEngineLogger().debug("runProcessables - There are "+buildsToProcess.size()+" builds to process");
		jasperResources.getEngineLogger().debug("runProcessables - There are "+toProcess.size()+" processables to process");

		typesAddedThisRun.clear();
		sourceFilesAddedThisRun.clear();
		objectsAddedThisRun.clear();

		while(buildsToInit.size()>0) {
			BuildComponentItem b = buildsToInit.get(0);

			appLog.info("Initializing build '"+b.getName()+"'");
			appLog.outputToSystem();
			ProcessableChanges changes = b.init();
			if (changes!=null) {
				buildsToProcess.add(b);
				buildsToInit.remove(b);
				commitChanges(changes, b.getConfigs(), b.getFolder());
			} else {
				b.getLog().outputToSystem();
				//errorState(b.getLog());
				return;
			}
		}
		
		while(toProcess.size() > 0) {
			if (toProcess.contains(null)) {
				appLog.warn("Found a null entry in toProcess");
			}
			Collections.sort(toProcess);
			Processable proc = toProcess.get(0);
			appLog.info("*** Processing "+proc.getName()+"("+proc.getItemId()+") ***");
			appLog.outputToSystem();
			boolean success = true;

			try {
				success = proc.process();
				if (success) {
					ProcessableChanges changes = proc.getChanges();
					commitChanges(changes, proc.getConfigs(), proc.getFolder());
					toProcess.remove(proc);
					proc.getLog().outputToSystem();
				} else {
					// todo?
					//errorState(proc.getLog());
					proc.getLog().outputToSystem();
				}
				proc.getLog().outputToSystem();
			} catch(StaleObjectException e) {
				String objectName = e.getName();
				List<Integer> toReAdd = processingDataManager.getItemsForObjectName(objectName);
				toReAdd.stream().forEach(id -> this.removeItem(id, false));

				// Clear the processable's log
				proc.getLog().getMessages(true);
				
				// Remove this object
				processingDataManager.getObjects().remove(objectName);
				this.objectsAddedThisRun.remove(objectName);
				this.checkFolderWatchers();
			} catch(StaleVariableTypeException e) {
				String lang = e.getLang();
				String name = e.getName();
				List<Integer> toReAdd = processingDataManager.getItemsForType(lang, name);
				toReAdd.stream().forEach(id -> this.removeItem(id, false));
				// Clear the processable's log
				proc.getLog().getMessages(true);

				// Remove this type so it can be added again
				VariableType type = processingDataManager.getTypes(lang).get(name);
				// TODO: This should never be null?
				if (type!=null) {
					processingDataManager.getTypes(lang).remove(name);
					this.typesAddedThisRun.remove(type);
				}
				this.checkFolderWatchers();
			} catch(StaleSourceFileException e) {
				String path = e.getPath();
				List<Integer> toReAdd = processingDataManager.getItemsForSourceFile(path);
				if (toReAdd!=null) {
					toReAdd.stream().forEach(id -> this.removeItem(id, false));
				} else {
					// this shouldn't happen?
					jasperResources.getEngineLogger().debug("Source file "+e.getPath()+" was stale but I found no dependent items");
				}
				// Clear the processable's log
				proc.getLog().getMessages(true);
				// Remove the source file and remove it from source files added this run
				ctx.removeSourceFile(path);
				// Not sure this is required
				this.sourceFilesAddedThisRun.remove(path);
				this.checkFolderWatchers();
			}
			if (!success) {
				return;
			}
		}
		
		while(buildsToProcess.size()>0) {
			BuildComponentItem b = buildsToProcess.get(0);

			appLog.info("Processing build '"+b.getName()+"'");
			appLog.outputToSystem();
			ProcessableChanges changes = b.process();
			if (changes!=null) {
				buildsToProcess.remove(b);
				commitChanges(changes, b.getConfigs(), b.getFolder());
			} else {
				errorState(b.getLog());
				return;
			}
		}
	}

	// Returns a list of items to be re-added.
	protected List<Item> userFileRemoved(UserFile userFile) {
		List<Item> ret = new ArrayList<>();
		String path = userFile.getPath();
		List<FileProcessorRecord> procs = getFileProcessors();
		List<FolderWatcherItem> watchers = getFolderWatchers();

		// Find file processors for this file path
		List<FileProcessorRecord> procsToRemove = procs.stream().filter(proc -> proc.getFilePath().equals(path)).collect(Collectors.toList());
		// Remove the items for these processors
		procsToRemove.forEach(proc -> removeItem(proc.getItemId(), true));
		
		// Find folder watchers for this path, remove/readd them.
		List<FolderWatcherItem> watchersToRemove = watchers.stream().filter(watcher -> path.startsWith(watcher.getPath())).collect(Collectors.toList());
		// Remove items for these folder watchers
		watchersToRemove.forEach(watcher -> removeItem(watcher.getItemId(), false));
		
		ctx.removeUserFile(userFile);
		
		return ret;
	}
	
	// Removes the given itemId.
	// If remove is not permanent, re-add any items again if they are components or build components
	// Returns a list of items to be re-added
	protected void removeItem(int id, boolean permanent) {

		// Get the item, see if it has already been removed
		Item item = this.getItem(id);
		if (item==null) return;

		FolderWatcherItem folderWatcher = (item instanceof FolderWatcherItem) ? (FolderWatcherItem)item : null;

		jasperResources.getEngineLogger().debug("Remove item "+item.getName()+"("+id+") with perm as "+permanent);

		// Remove this item from items
		this.items.remove(item);

		// If this is a folder watcher, clear its file processors
		if (folderWatcher!=null) {
			folderWatcher.clearProcs();
		}
		
		// Remove source files from this item
		List<String> srcPaths = processingDataManager.getSourceFilesFromId(id);
		srcPaths.forEach(path -> {
			ctx.removeSourceFile(path);
		});

		// Items to remove and re-add
		Set<Integer> toRemoveAndAdd = new HashSet<>();

		// Other Items to remove permanently
		Set<Integer> toRemove = new HashSet<>();

		// If the originator originates any application data, it should be removed and re-added.  This item will be removed permanently.
		if (item.getOriginatorId()>0) {

			if (processingDataManager.originatesProcessingData(item.getOriginatorId())) {
				toRemoveAndAdd.add(item.getOriginatorId());
				permanent = true;
			}
		}

		// Permanently remove any item that originates from this one
		items.forEach(i -> {
			if (i.getOriginatorId()==id) {
				toRemove.add(i.getItemId());
			}
		});
		
		// Remove and re-add all items that depend on the same application data
		toRemoveAndAdd.addAll(processingDataManager.removeItem(id));

		// Remove processables with this itemId
		this.toProcess.stream().filter(proc -> proc.getItemId()==id).collect(Collectors.toList()).forEach(proc -> {
			toProcess.remove(proc);
		});

		BuildComponentItem buildCompItem = (item instanceof BuildComponentItem) ? (BuildComponentItem)item : null;

		if (buildCompItem != null) {
			buildsToInit.remove(buildCompItem);
			buildsToProcess.remove(buildCompItem);
			// Unset the folder's build component.
			buildCompItem.getFolder().setBuildComponentItem(null);
			// TODO: The folder will have to be reloaded.
		}

		toRemove.forEach(i -> {
			removeItem(i, true);
		});

		// The itemId can wind up in the -add list.  make sure it's not there
		toRemoveAndAdd.stream().filter(removeId -> removeId != id).forEach(i -> {
			removeItem(i, false);
		});

		if (!permanent) {
			this.addItem(item);
		}

	}
	
	private void addItem(Item item) {
		if (items.contains(item)) {
			return;
		}
		//item.assignItemId(newItemId());
		items.add(item);
		jasperResources.getEngineLogger().debug("Added item #"+item.getItemId()+" as "+item.getName());
		if (item instanceof ComponentItem) {
			ComponentItem c = (ComponentItem)item;
			toProcess.add(c);
			//items.add(c);
		} else if (item instanceof BuildComponentItem) {
			BuildComponentItem b = (BuildComponentItem)item;
			buildsToInit.add(b);
			//items.add(b);
		//} else if (item instanceof FolderWatcherItem) {
		//	FolderWatcherItem f = (FolderWatcherItem)item;
		//	checkFilesForFolderWatcher(f);
		}
	}

	// When changes are commited, items are only added (not removed) so we can add them right away instead of after 
	// everything's done
	protected void commitChanges(ProcessableChanges changes, Map<String,String> configs, ApplicationFolderImpl folder) {
		int originatorId = changes.getItemId();
		//int originatorId = changes.getItemId();
		changes.getComponentsAdded().forEach(comp -> {
			int itemId = newItemId();
			ComponentPattern pattern = patterns.getPattern(comp.getClass());
			ComponentItem item = new ComponentItem(itemId, comp, configs, this, pattern, jasperResources, originatorId, folder);
			items.add(item);
			toProcess.add(item);
		});

		// Application data that has been manipulated this run
		changes.getTypesModified().stream().forEach(type -> typesAddedThisRun.add(type.getRight()));

		changes.getObjects().entrySet().forEach(e -> {
			objectsAddedThisRun.add(e.getKey());
		});

		changes.getSourceFilesAdded().forEach(sourceFile -> {
			ctx.addSourceFile(sourceFile);
			sourceFilesAddedThisRun.add(sourceFile.getPath());
		});

		// Add folder watchers
		changes.getFolderWatchersAdded().forEach(pair -> {
			String path = pair.getKey();
			FolderWatcher w = pair.getValue();
			
			int id = newItemId();
			
			FolderWatcherItem item = new FolderWatcherItem(id, path, w, this, jasperResources, configs, originatorId, folder);
			items.add(item);
			checkFolderWatcherAgainstUserFiles(item);
 		});

		// Add file processors
		changes.getFileProcessorsAdded().forEach(proc -> {
			String path = proc.getKey();
			FileProcessor p = proc.getRight();
			UserFile userFile = ctx.getUserFiles().get(path);
			if (userFile!=null) {
				FileProcessorRecord rec = new FileProcessorRecord(originatorId, path, this, p, configs, jasperResources, 
						folder);
				rec.init(userFile);
				this.toProcess.add(rec);
			}
		});
		processingDataManager.commitChanges(changes);
	}

	/*
	protected void checkFilesForFolderWatcher(FolderWatcherItem item) {
		String path = item.getPath();
		// Check all user files for the given folder watcher
		Map<String,UserFile> files = ctx.getUserFiles();
		for(Entry<String,UserFile> entry : files.entrySet()) {
			String filePath = entry.getKey();
			if (filePath.startsWith(path)) {
				FolderWatcherProcessable proc = item.getProc(filePath);
				if (proc!=null)
					toProcess.add(proc);
			}
		}
	}
*/
	
	// Get item with the given ID
	protected Item getItem(int itemId) {
		return items.stream().filter(item -> item.getItemId() == itemId).findAny().orElse(null);
	}
	
	protected ComponentItem getComponentItem(Component component) {
		for(Item item : items) {
			if (item instanceof ComponentItem) {
				ComponentItem i = (ComponentItem)item;
				if (((ComponentItem) item).getComponent()==component) {
					return i;
				}
			}
		}
		return null;
	}

	protected BuildComponentItem getBuildComponentItem(Component component) {
		
		for(Item i : items) {
			if (i instanceof BuildComponentItem) {
				BuildComponentItem b = (BuildComponentItem)i;
				if (b.getBuildComponent()==component) {
					return b;
				}
			}
		}
		return null;
	}

	List<FolderWatcherItem> getFolderWatchers() {
		List<FolderWatcherItem> ret = new ArrayList<>();
		
		items.stream().forEach(item -> {
			if (item instanceof FolderWatcherItem) ret.add((FolderWatcherItem)item);
		});
		return ret;
	}
	
	List<FileProcessorRecord> getFileProcessors() {
		List<FileProcessorRecord> ret = new ArrayList<>();
		
		toProcess.stream().forEach(item -> {
			if (item instanceof FileProcessorRecord) ret.add((FileProcessorRecord)item);
		});
		return ret;
	}
	
	// ProcessableContext API
	
	@Override
	public String getSystemAttribute(String name) {
		return processingDataManager.getSystemAttribute(name);
	}

	@Override
	public VariableType getType(String lang, String name) {
		VariableType ret = null;
		
		Map<String,VariableType> types = processingDataManager.getTypes(lang);
		if (types!=null) {
			ret = types.get(name);
		}

		return ret;
	}
	
	@Override
	public void modifyType(String lang, VariableType type) {

		if (!typesAddedThisRun.contains(type)) {
			throw new StaleVariableTypeException(lang, type.getName());
		}
	}

	@Override
	public UserFile getUserFile(String path) {
		return this.ctx.getUserFiles().get(path);
	}

	@Override
	public SourceFile getSourceFile(String path) {
		SourceFile ret = ctx.getSourceFile(path);
		if (ret!=null) {
			if (!sourceFilesAddedThisRun.contains(path)) {
				throw new StaleSourceFileException(path);
			}
		}
		return ret;
	}

	@Override
	public Object getObject(String name) {
		Object obj = processingDataManager.getObjects().get(name);

		if (obj!=null) {
			if (!objectsAddedThisRun.contains(name)) {
				throw new StaleObjectException(name);
			}
		}
		return obj;
	}

}

