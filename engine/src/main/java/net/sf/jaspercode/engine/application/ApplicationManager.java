package net.sf.jaspercode.engine.application;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.plugin.ApplicationPlugin;
import net.sf.jaspercode.api.plugin.PluginContext;
import net.sf.jaspercode.api.snapshot.ApplicationSnapshot;
import net.sf.jaspercode.api.snapshot.ItemSnapshot;
import net.sf.jaspercode.api.snapshot.SourceFileSnapshot;
import net.sf.jaspercode.api.snapshot.SystemAttributeSnapshot;
import net.sf.jaspercode.engine.EngineInitException;
import net.sf.jaspercode.engine.EngineLanguages;
import net.sf.jaspercode.engine.EnginePatterns;
import net.sf.jaspercode.engine.EngineProperties;
import net.sf.jaspercode.engine.PluginContextImpl;
import net.sf.jaspercode.engine.PluginManager;
import net.sf.jaspercode.engine.definitions.ApplicationFolderImpl;
import net.sf.jaspercode.engine.definitions.ComponentFile;
import net.sf.jaspercode.engine.definitions.JasperPropertiesFile;
import net.sf.jaspercode.engine.definitions.SystemAttributesFile;
import net.sf.jaspercode.engine.definitions.UserFile;
import net.sf.jaspercode.engine.definitions.WatchedResource;
import net.sf.jaspercode.engine.processing.EngineException;
import net.sf.jaspercode.engine.processing.ProcessingState;
import net.sf.jaspercode.engine.processing.ProcessorLog;

public class ApplicationManager {

	private boolean firstScan = true;
	private boolean changeDetected = false;

	// Application resources
	private OutputManager outputManager = null;
	private ResourceManager resourceManager = null;
	private ProcessingManager processingManager = null;
	private ProcessorLog appLog = null;

	// Engine resources and applicationContext
	private String applicationName;
	private JasperResources jasperResources = null;

	private SystemAttributesFile systemAttributesFile = null;

	Map<String,ApplicationPlugin> plugins = new HashMap<>();

	public String getApplicationName() {
		return applicationName;
	}
	public SystemAttributesFile getSystemAttributesFile() {
		return systemAttributesFile;
	}

	public ApplicationManager(String applicationName, File applicationDir,File outputDir, EngineProperties engineProperties,
			EnginePatterns patterns,EngineLanguages languages,PluginManager pluginManager) throws EngineInitException {
		this.applicationName = applicationName;
		this.appLog = new ProcessorLog("Application:"+applicationName);
		jasperResources = new JasperResources(engineProperties, pluginManager, this);
		this.resourceManager = new ResourceManager(applicationDir,this, jasperResources);
		this.processingManager = new ProcessingManager(this, patterns, languages, jasperResources);
		this.outputManager = new OutputManager(outputDir,new OutputContext(this));
		startApplicationPlugins();
	}

	protected void startApplicationPlugins() throws EngineInitException {
		this.appLog.info("Scanning for application plugins");
		Set<Class<ApplicationPlugin>> pluginClasses = jasperResources.getPlugins(ApplicationPlugin.class);
		for(Class<ApplicationPlugin> pluginClass : pluginClasses) {
			String className = pluginClass.getCanonicalName();
			try {
				ApplicationPlugin plugin = pluginClass.newInstance();
				String name = plugin.getPluginName();
				if ((name==null) || (name.trim().length()==0)) {
					throw new EngineInitException("Found plugin '"+className+"' with no name");
				}
				if (plugins.get(name)!=null) {
					throw new EngineInitException("Found multiple application plugins with name '"+name+"'");
				}
				String prop = "applicationPlugin."+plugin.getPluginName() + ".enabled";
				boolean enabled = this.jasperResources.getBoolean(prop, false);
				if (enabled) {
					appLog.info("Starting application plugin '"+name+"'");
					PluginContext ctx = new PluginContextImpl(this.appLog, jasperResources.getEngineProperties());
					plugin.setApplicationName(applicationName);
					plugin.setPluginContext(ctx);
					plugins.put(name, plugin);
				} else {
					appLog.info("Found plugin '"+plugin.getPluginName()+"' of class class '"+className+"' but it is not enabled");
				}
			} catch(IllegalAccessException | InstantiationException e) {
				throw new EngineInitException("Couldn't initialize engine plugin class "+className, e);
			} finally {
				appLog.flushToSystem();
			}
		}
		
	}

	public void scan() throws Exception {
		for(Entry<String,ApplicationPlugin> entry : this.plugins.entrySet()) {
			entry.getValue().scanStart();
		}
		
		changeDetected = false;
		if (!firstScan) {
			scanForRemovedFiles();
			scanForModifiedFiles();
		}
		scanForAddedFiles();
		if (changeDetected) {
			// This means that some file was changed, including userFile or ComponentFile
			processingManager.checkResourceWatchers();
			processingManager.processChanges();
			if (processingManager.getState()==ProcessingState.COMPLETE) {
				outputManager.writeAddedSourceFiles();
			}
			System.out.println("*** Scan completed ***");
			if (plugins.size()>0) {
				ApplicationSnapshot snapshot = createSnapshot();
				for(Entry<String,ApplicationPlugin> entry : this.plugins.entrySet()) {
					entry.getValue().scanComplete(snapshot);
				}
			}
		}
		firstScan = false;

	}
	
	protected ApplicationSnapshot createSnapshot() {
		List<SourceFileSnapshot> sourceFiles = outputManager.getSourceFileSnapshots();
		List<SystemAttributeSnapshot> attrs = processingManager.getSystemAttributeSnapshots();
		List<ItemSnapshot> comps = processingManager.getItemSnapshots();

		processingManager.populateSourceFileOriginators(sourceFiles);
		return new ApplicationSnapshot(attrs,sourceFiles, comps);
	}

	protected void scanForModifiedFiles() throws EngineException {
		List<WatchedResource> results = resourceManager.scanForModifiedFiles();
		
		if (results.size()>0) {
			jasperResources.engineDebug("Scanned for modified files and found "+results.size());
			changeDetected = true;
			for(WatchedResource res : results) {
				modifyResource(res);
			}
		}
	}
	
	protected void modifyResource(WatchedResource resource) {
		processingManager.fileModified(resource);
	}
	
	protected void scanForRemovedFiles() throws EngineException {
		List<WatchedResource> results = resourceManager.scanForRemovedFiles();

		if (results.size()>0) {
			jasperResources.engineDebug("Scanned for removed files and found "+results.size());
			changeDetected = true;
			for(WatchedResource res : results) {
				removeResource(res);
			}
		}
	}

	// The resource has been removed (deleted) from the application folder
	// User files need to be removed from output
	// Component Files need to be unloaded by processingManager.
	// UserFiles need to be removed from output
	// Files need to be removed from parent folders
	public void removeResource(WatchedResource res) {
		ApplicationFolderImpl folder = res.getFolder();
		String name = res.getName();
		
		if (res instanceof ComponentFile) {
			processingManager.unloadComponentFile((ComponentFile)res, true);
			folder.getComponentFiles().remove(name);
		} else if (res instanceof UserFile) {
			UserFile userFile = (UserFile)res;
			folder.getUserFiles().remove(userFile.getName());
			outputManager.removeUserFile(userFile);
			processingManager.userFileRemoved(userFile);
		} else if (res instanceof JasperPropertiesFile) {
			// Everything in this folder and subfolders needs to be unloaded.
			// This includes User Files and Component Files
			recursiveRemoveContents(folder);
			folder.setJasperProperties(null);
		} else if (res instanceof SystemAttributesFile) {
			//SystemAttributesFile f = (SystemAttributesFile)res;
			// Everything in this folder and subfolders needs to be removed and re-evaluated.
			// This includes User Files and Component Files.
			this.handleSystemAttributesFileChange(null);
		}
	}
	
	protected void recursiveRemoveContents(ApplicationFolderImpl folder) {
		List<WatchedResource> resources = new ArrayList<>();
		for(Entry<String,ComponentFile> entry : folder.getComponentFiles().entrySet()) {
			resources.add(entry.getValue());
		}
		for(Entry<String,UserFile> entry : folder.getUserFiles().entrySet()) {
			resources.add(entry.getValue());
		}
		for(WatchedResource res : resources) {
			removeResource(res);
		}
		List<ApplicationFolderImpl> folders = new ArrayList<>();
		for(Entry<String,ApplicationFolderImpl> entry : folder.getSubFolders().entrySet()) {
			folders.add(entry.getValue());
		}
		for(ApplicationFolderImpl subfolder : folders) {
			recursiveRemoveContents(subfolder);
		}
	}

	protected void scanForAddedFiles() throws EngineException {
		List<WatchedResource> added = resourceManager.scanForNewFiles();
		
		if (added.size()>0) {
			jasperResources.engineDebug("Scanned for added files and found "+added.size());
			List<ComponentFile> componentFiles = new ArrayList<>();
			for(WatchedResource res : added) {
				if (res instanceof ComponentFile) {
					componentFiles.add((ComponentFile)res);
				} else if (res instanceof UserFile) {
					outputManager.writeUserFile((UserFile)res);
				} else if (res instanceof ApplicationFolderImpl) {
					// no-op
				} else {
					System.out.println("Hi");
				}
			}
			changeDetected = true;
			processingManager.addFiles(componentFiles);
		}
	}

	public void handleSystemAttributesFileChange(SystemAttributesFile f) {
		Map<String,String> newAttributes = new HashMap<>();
		Map<String,String> oldAttributes = new HashMap<>();
		
		if (f!=null) {
			newAttributes = f.getSystemAttributes();
		}
		if (this.systemAttributesFile!=null) {
			oldAttributes = systemAttributesFile.getSystemAttributes();
		}

		this.systemAttributesFile = f;
		
		// Iterate through the new attributes
		for(Entry<String,String> entry : newAttributes.entrySet()) {
			String name = entry.getKey();
			String type = entry.getValue();
			if ((processingManager.getAttributes().get(name)!=null) && (!processingManager.getAttributes().get(name).equals(type))) {
				System.err.println("processingManager should remove components that use system attribute '"+name+"'");
			}
			processingManager.originateFromSystemAttributesFile(name, type);
		}
		
		// TODO: We need to unload and re-process everything in the application
		if (!oldAttributes.isEmpty()) {
			System.err.println("Cannot properly handle changes in systemAttributes.properties");
		}
	}
	
	// The component file needs to be unloaded from the processing manager
	public void unloadComponentFile(ComponentFile componentFile, boolean remove) {
		processingManager.unloadComponentFile(componentFile, remove);
	}

	public Map<String,UserFile> getUserFiles() {
		return resourceManager.getUserFiles();
	}

	public SourceFile getSourceFile(String path) {
		return outputManager.getSourceFile(path);
	}
	
	public void addSourceFile(SourceFile sourceFile) {
		outputManager.addSourceFile(sourceFile);
	}
	
	public void removeSourceFile(String path) {
		outputManager.removeSourceFile(path);
	}
	
}
