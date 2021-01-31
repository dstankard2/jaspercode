package net.sf.jaspercode.eng.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.config.BuildComponent;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.logging.ProcessorLogLevel;
import net.sf.jaspercode.api.plugin.ProcessorLogMessage;
import net.sf.jaspercode.api.resources.FileProcessor;
import net.sf.jaspercode.api.resources.FolderWatcher;
import net.sf.jaspercode.api.types.VariableType;
import net.sf.jaspercode.eng.BuildComponentPattern;
import net.sf.jaspercode.eng.ComponentPattern;
import net.sf.jaspercode.eng.EngineLanguages;
import net.sf.jaspercode.eng.EnginePatterns;
import net.sf.jaspercode.eng.JasperResources;
import net.sf.jaspercode.eng.files.ComponentFile;
import net.sf.jaspercode.eng.files.UserFile;
import net.sf.jaspercode.eng.processing.BuildComponentItem;
import net.sf.jaspercode.eng.processing.ComponentItem;
import net.sf.jaspercode.eng.processing.FileProcessorRecord;
import net.sf.jaspercode.eng.processing.FolderWatcherItem;
import net.sf.jaspercode.eng.processing.FolderWatcherProcessable;
import net.sf.jaspercode.eng.processing.Item;
import net.sf.jaspercode.eng.processing.Processable;
import net.sf.jaspercode.eng.processing.ProcessableChanges;
import net.sf.jaspercode.eng.processing.ProcessableContext;
import net.sf.jaspercode.eng.processing.ProcessingDataManager;
import net.sf.jaspercode.eng.processing.ProcessorLog;

public class ProcessingManager implements ProcessableContext {
	private JasperResources jasperResources;
	private EnginePatterns patterns;
	private ProcessingContext ctx;
	private ProcessorLog appLog;
	private ProcessingDataManager data = null;
	
	private List<ComponentFile> componentFilesAdded = new ArrayList<>();
	private List<ComponentFile> componentFilesRemoved = new ArrayList<>();
	private List<UserFile> userFilesAdded = new ArrayList<>();
	private List<UserFile> userFilesRemoved = new ArrayList<>();

	// Processables that have committed changes
	private List<Processable> processed = new ArrayList<>();
	// Processables that do not have committed changes
	private List<Processable> toProcess = new ArrayList<>();
	// Items that have been detected by processing manager, derived from component files
	private List<Item> items = new ArrayList<>();

	private List<BuildComponentItem> buildItemsToInit = new ArrayList<>();
	private List<BuildComponentItem> buildItemsToGenerate = new ArrayList<>();

	public ProcessingManager(JasperResources jasperResources,EnginePatterns patterns,EngineLanguages languages,
			ProcessingContext ctx, ProcessorLog appLog) {
		this.jasperResources = jasperResources;
		this.patterns = patterns;
		this.ctx = ctx;
		this.appLog = appLog;
		this.data = new ProcessingDataManager(languages, jasperResources);
	}

	/* Public API for ApplicationManager */
	
	// Remove any FileProcessor for this file path
	public void removeUserFile(UserFile userFile) {
		this.userFilesRemoved.add(userFile);
	}

	// Check folder watchers for this path
	public void addUserFile(UserFile userFile) {
		this.userFilesAdded.add(userFile);
	}

	// Add these components to be processed.
	public void addComponentFile(ComponentFile file) {
		this.componentFilesAdded.add(file);
	}
	
	// Remove items for the components in this file
	public void removeComponentFile(ComponentFile file) {
		this.componentFilesRemoved.add(file);
	}
	
	public void processChanges() {
		Set<Item> itemsToAdd = new HashSet<>();

		// Handle userFiles removed
		for(UserFile f : userFilesRemoved) {
			itemsToAdd.addAll(userFileRemoved(f));
		}
		userFilesRemoved.clear();

		List<Item> itemsToRemove = new ArrayList<>();
		// Manage component files that have been removed
		for(ComponentFile file : componentFilesRemoved) {
			for(Component comp : file.getComponentSet().getComponent()) {
				ComponentItem item = getComponentItem(comp);
				if (item!=null) {
					itemsToRemove.add(item);
					//itemsToAdd.addAll(removeItem(item.getItemId(), true));
					//removeItem(item.getItemId(), true);
				}
				BuildComponentItem buildItem = getBuildComponentItem(comp);
				if (buildItem!=null) {
					itemsToRemove.add(buildItem);
				}
			}
		}
		itemsToRemove.forEach(item -> {
			removeItem(item.getItemId(), true);
		});
		
		if (componentFilesRemoved.size()>0) {
			jasperResources.engineDebug("Cleaned out component files and now there are "+this.items.size()+" items");
		}
		
		componentFilesRemoved.clear();
		
		// Look for component Files Added - add component entries
		for(ComponentFile f : componentFilesAdded) {
			for(Component comp : f.getComponentSet().getComponent()) {
				addComponent(f, comp);
			}
		}
		componentFilesAdded.clear();
		
		// Look for userFiles added - check folder watchers
		if (userFilesAdded.size()>0) {
			List<FolderWatcherItem> l = this.getFolderWatchers();
			for(UserFile f : userFilesAdded) {
				ctx.writeUserFile(f);
				if (l.size()>0) {
					for(FolderWatcherItem item : l) {
						if (f.getPath().startsWith(item.getPath())) {
							// If the file was added then add toProcess
							FolderWatcherProcessable proc = item.getProc(f.getPath());
							toProcess.add(proc);
						}
					}
				}
			}
			userFilesAdded.clear();
		}

		// Run processing of items in queue
		runProcessables();
	}

	private void addComponent(ComponentFile f, Component comp) {
		int itemId = newId();
		if (comp instanceof BuildComponent) {
			BuildComponent buildComp = (BuildComponent)comp;
			BuildComponentPattern pattern =  patterns.getBuildPattern(buildComp.getClass());
			BuildComponentItem i = new BuildComponentItem(itemId, buildComp, pattern, jasperResources, this, f);
			buildItemsToInit.add(i);
			items.add(i);
		} else {
			ComponentPattern pattern = patterns.getPattern(comp.getClass());
			ComponentItem item = new ComponentItem(itemId, comp, this, f, pattern, jasperResources, 0);
			toProcess.add(item);
			items.add(item);
		}
	}
	
	private void addItem(Item item) {
		if (item instanceof ComponentItem) {
			ComponentItem c = (ComponentItem)item;
			toProcess.add(c);
			items.add(c);
		} else if (item instanceof BuildComponentItem) {
			BuildComponentItem b = (BuildComponentItem)item;
			buildItemsToInit.add(b);
			items.add(b);
		}
	}

	/* End of public API for ApplicationManager */

	protected void runProcessables() {
		jasperResources.engineDebug("runProcessables - There are "+buildItemsToInit.size()+" builds to init");
		jasperResources.engineDebug("runProcessables - There are "+buildItemsToGenerate.size()+" builds to generate");
		jasperResources.engineDebug("runProcessables - There are "+toProcess.size()+" processables to process");

		// Initialize build components to process
		while(buildItemsToInit.size()>0) {
			BuildComponentItem b = buildItemsToInit.get(0);

			appLog.info("Initializing build '"+b.getName()+"'");
			appLog.outputToSystem();
			ProcessableChanges changes = b.init();
			if (changes!=null) {
				buildItemsToGenerate.add(b);
				buildItemsToInit.remove(b);
				commitChanges(changes);
			} else {
				errorState(b.getLog());
				return;
			}
		}
		
		// Run as many processables as we can without error
		while(toProcess.size()>0) {
			Collections.sort(toProcess);
			Processable p = toProcess.get(0);
			appLog.info("Processing component "+p.getName());
			appLog.outputToSystem();
			boolean success = p.process();

			if (success) {
				ProcessableChanges changes = p.getChanges();
				toProcess.remove(0);
				processed.add(p);
				commitChanges(changes);
			} else {
				errorState(p.getLog());
				return;
			}
		}

		// generate builds
		while(buildItemsToGenerate.size()>0) {
			BuildComponentItem b = buildItemsToGenerate.get(0);
			appLog.info("Generating build '"+b.getName()+"'");
			appLog.outputToSystem();
			ProcessableChanges changes = b.process();

			if (changes != null) {
				commitChanges(changes);
				//commitChanges(b.getChanges());
				buildItemsToGenerate.remove(b);
			} else {
				errorState(b.getLog());
				return;
			}
		}
	}

	public void commitChanges(ProcessableChanges changes) {
		int itemId = changes.getItemId();
		changes.getSourceFiles().forEach(src -> {
			ctx.writeSourceFile(src);
		});
		data.commitChanges(changes);
		
		ComponentFile componentFile = changes.getOriginalFile();
		Component originalComponent = changes.getOriginalComponent();
		changes.getComponentsAdded().forEach(comp -> {
			int id = newId();
			ComponentPattern pattern = patterns.getPattern(comp.getClass());
			ComponentItem item = new ComponentItem(id, comp, this, componentFile, pattern, jasperResources, itemId);
			toProcess.add(item);
			items.add(item);
		});
		
		changes.getFolderWatchersAdded().forEach(pair -> {
			String path = pair.getKey();
			FolderWatcher w = pair.getValue();
			
			int id = newId();
			
			FolderWatcherItem item = new FolderWatcherItem(id, w, path, this, componentFile,
					jasperResources, originalComponent, itemId);
			items.add(item);
			checkFilesForFolderWatcher(item);
		});
		
		changes.getFileProcessorsAdded().forEach(proc -> {
			String path = proc.getKey();
			FileProcessor p = proc.getRight();
			UserFile userFile = ctx.getUserFiles().get(path);
			if (userFile!=null) {
				FileProcessorRecord rec = new FileProcessorRecord(itemId, path, originalComponent, this, p, componentFile, jasperResources);
				rec.init(userFile);
				this.toProcess.add(rec);
			}
		});
	}

	protected void checkFilesForFolderWatcher(FolderWatcherItem item) {
		String path = item.getPath();
		// Check all user files for the given folder watcher
		Map<String,UserFile> files = ctx.getUserFiles();
		for(Entry<String,UserFile> entry : files.entrySet()) {
			String filePath = entry.getKey();
			if (filePath.startsWith(path)) {
				FolderWatcherProcessable proc = item.getProc(filePath);
				toProcess.add(proc);
			}
		}
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

	int nextId = 0;
	protected int newId() {
		return ++nextId;
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
	
	// Returns a list of items to be re-added.
	protected List<Item> userFileRemoved(UserFile userFile) {
		List<Item> ret = new ArrayList<>();
		String path = userFile.getPath();
		List<FileProcessorRecord> procs = getFileProcessors();
		List<FileProcessorRecord> procsToRemove = new ArrayList<>();
		List<FolderWatcherItem> watchers = getFolderWatchers();
		List<FolderWatcherItem> watchersToRemove = new ArrayList<>();
		
		// Find file processors for this file path
		procs.forEach(proc -> {
			if (proc.getFilePath().equals(path)) {
				procsToRemove.add(proc);
			}
		});
		
		// Remove the items for these processors
		procsToRemove.forEach(proc -> {
			int id = proc.getItemId();
			removeItem(id, true);
		});
		
		// Find folder watchers for this path, remove/readd them.
		watchers.forEach(watcher -> {
			if (path.startsWith(watcher.getPath())) {
				watchersToRemove.add(watcher);
			}
		});
		watchersToRemove.forEach(watcher -> {
			removeItem(watcher.getItemId(), false);
		});
		
		ctx.removeUserFile(userFile);
		
		return ret;
	}
	
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

	// Removes the given itemId.
	// If remove is not permanent, re-add any items again if they are components or build components
	// Returns a list of items to be re-added
	protected void removeItem(int id, boolean permanent) {
		jasperResources.engineDebug("Remove item "+id+" with perm as "+permanent);

		// Get the item, see if it has already been removed
		Item item = this.getItem(id);
		if (item==null) return;

		// Remove this item from items
		this.items.remove(item);
		
		// Remove source files from this item
		List<String> srcPaths = data.getSourceFilesFromId(id);
		srcPaths.forEach(path -> {
			ctx.removeSourceFile(path);
		});
		
		// Items to remove and re-add
		Set<Integer> toRemoveAndAdd = null;

		// Other Items to remove permanently
		Set<Integer> toRemove = new HashSet<>();
		
		// TODO: Remove any item that originates from this one
		items.forEach(i -> {
			if (i.getOriginatorId()==id) {
				toRemove.add(i.getItemId());
			}
		});
		
		// Remove and re-add all items that depend on the same application data
		toRemoveAndAdd = data.removeItem(id);

		// List of processables to remove
		List<Processable> procs = new ArrayList<>();

		// Remove processables with this itemId

		// Remove toProcess and processed processables with this itemId
		this.processed.forEach(p -> {
			if (p.getItemId()==id) {
				procs.add(p);
			}
		});
		procs.forEach(proc -> {
			processed.remove(proc);
		});
		procs.clear();
		this.toProcess.forEach(p -> {
			if (p.getItemId()==id) {
				procs.add(p);
			}
		});
		procs.forEach(p -> {
			toProcess.remove(p);
		});

		BuildComponentItem buildCompItem = (item instanceof BuildComponentItem) ? (BuildComponentItem)item : null;
		//ComponentItem compItem = (item instanceof ComponentItem) ? (ComponentItem)item : null;

		if (buildCompItem != null) {
			buildItemsToInit.remove(buildCompItem);
			buildItemsToGenerate.remove(buildCompItem);
		}

		toRemoveAndAdd.forEach(i -> {
			removeItem(i, false);
		});

		toRemove.forEach(i -> {
			removeItem(i, true);
		});

		// lastly, re-add item if this is not permanent
		if (!permanent) {
			addItem(item);
		}
	}

	public void updateGlobalSystemAttributes(Map<String,String> attributes) {
		data.setGlobalSystemAttributes(attributes);
	}

	@Override
	public String getSystemAttribute(String name) {
		return data.getSystemAttribute(name);
	}

	@Override
	public VariableType getType(String lang, String name) {
		return data.getTypes(lang).get(name);
	}

	@Override
	public UserFile getUserFile(String path) {
		return ctx.getUserFiles().get(path);
	}

	@Override
	public SourceFile getSourceFile(String path) {
		return ctx.getSourceFile(path);
	}

	@Override
	public Object getObject(String name) {
		return data.getObjects().get(name);
	}

}

