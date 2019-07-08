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
import net.sf.jaspercode.engine.definitions.ComponentFile;
import net.sf.jaspercode.engine.definitions.SystemAttributesFile;
import net.sf.jaspercode.engine.definitions.UserFile;
import net.sf.jaspercode.engine.exception.EngineException;
import net.sf.jaspercode.engine.processing.ProcessingState;
import net.sf.jaspercode.engine.processing.ProcessorLog;

public class ApplicationManager {

	private boolean firstScan = true;

	// Application resources
	private OutputManager outputManager = null;
	private ResourceManager resourceManager = null;
	private ProcessingManager processingManager = null;
	private BuildManager buildManager = null;
	private ProcessorLog appLog = null;

	// Engine resources and applicationContext
	private String applicationName;
	private JasperResources jasperResources = null;
	private boolean jasperPropertiesChanged = false;

	private SystemAttributesFile systemAttributesFile = null;

	Map<String,ApplicationPlugin> plugins = new HashMap<>();
	
	//Map<String,List<String>> folderCommands = new HashMap<>();

	public ProcessorLog getApplicationLog() {
		return appLog;
	}

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
		this.jasperResources = new JasperResources(engineProperties, pluginManager);
		this.resourceManager = new ResourceManager(applicationDir,this, jasperResources);
		this.processingManager = new ProcessingManager(this, patterns, languages, jasperResources, appLog);
		this.outputManager = new OutputManager(outputDir, jasperResources);
		this.buildManager = new BuildManager(resourceManager.getRootFolder());
		startApplicationPlugins();
	}
	
	public File getOutputDirectory() {
		return outputManager.getOutputDirectory();
	}

	public ProcessingState getProcessingState() {
		return processingManager.getState();
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

	public void setCommands(String folderPath,List<String> commands) {
		if (commands==null) {
			buildManager.clearStandingCommands(folderPath);
		} else {
			buildManager.updateCommands(folderPath, commands);
		}
	}
	
	public List<String> getStandingCommands(String path) {
		return buildManager.getStandingCommands(path);
	}
	
	public void scan() throws Exception {
		boolean userFileChangeDetected = false;
		for(Entry<String,ApplicationPlugin> entry : this.plugins.entrySet()) {
			entry.getValue().scanStart();
		}
		
		buildManager.scanStarted();
		
		if (firstScan) {
			System.out.println("*** Begin scan of application '"+this.getApplicationName()+"'");
		}

		List<ResourceChange> changes = new ArrayList<>();
		if (!firstScan) {
			scanForRemovedFiles(changes);
			scanForModifiedFiles(changes);
		}
		scanForAddedFiles(changes);
		if (changes.size()>0) {
			if (!firstScan) {
				System.out.println("*** Begin scan of application '"+this.getApplicationName()+"'");
			}
			
		} else if (firstScan) {
			System.out.println("*** Initial scan found no files - Ending ***");
			return;
		}
		
		boolean changeDetected = (changes.size()>0) || (userFileChangeDetected) || (jasperPropertiesChanged);

		if (changeDetected) {
			jasperPropertiesChanged = false;
			for(ResourceChange change : changes) {
				buildManager.changeDetected(change.getPath());
				boolean wasComponentFile = change.wasComponentFile();
				boolean wasUserFile = change.wasUserFile();
				boolean isComponentFile = change.isComponentFile();
				boolean isUserFile = change.isUserFile();

				if (change.getOldFile()==null) {
					// Added file
					if (change.isUserFile()) {
						userFileChangeDetected = true;
						outputManager.writeUserFile((UserFile)change.getNewFile());
					} else if (change.isComponentFile()) {
						processingManager.componentFileAdded((ComponentFile)change.getNewFile());
					} else {
						jasperResources.engineDebug("Detected a new file is neither a user file or component file: "+change.getPath());
					}
				} else if (change.getNewFile()==null) {
					// Removed file
					if (change.wasUserFile()) {
						userFileChangeDetected = true;
						outputManager.removeUserFile((UserFile)change.getOldFile());
					} else if (change.wasComponentFile()) {
						processingManager.componentFileRemoved((ComponentFile)change.getOldFile());
					} else {
						jasperResources.engineDebug("Detected a removed file is neither a user file or component file: "+change.getPath());
					}
				}
				else if (wasUserFile) {
					userFileChangeDetected = true; // A user file has changed
					if (isUserFile) {
						outputManager.writeUserFile((UserFile)change.getNewFile());
					} else if (isComponentFile) {
						outputManager.removeUserFile((UserFile)change.getOldFile());
						processingManager.componentFileAdded((ComponentFile)change.getNewFile());
					} else {
						jasperResources.engineDebug("Making a file update that is neither a user file or component file: "+change.getPath());
					}
				} else if (wasComponentFile) {
					// In both cases we remove the old component file
					processingManager.componentFileRemoved((ComponentFile)change.getOldFile());
					if (isUserFile) {
						userFileChangeDetected = true;
						outputManager.writeUserFile((UserFile)change.getNewFile());
					} else if (isComponentFile) {
						processingManager.componentFileAdded((ComponentFile)change.getNewFile());
					} else {
						jasperResources.engineDebug("Making a file update that is neither a user file or component file: "+change.getPath());
					}
				} else {
					jasperResources.engineDebug("Making a file update that was neither a user file or component file: "+change.getPath());
				}
			}

			processingManager.processChanges(userFileChangeDetected);

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
			buildManager.checkCommands();
		}

		// First scan is complete
		firstScan = false;
	}
	
	protected ApplicationSnapshot createSnapshot() {
		List<SourceFileSnapshot> sourceFiles = outputManager.getSourceFileSnapshots();
		List<SystemAttributeSnapshot> attrs = processingManager.getSystemAttributeSnapshots();
		List<ItemSnapshot> comps = processingManager.getItemSnapshots();

		processingManager.populateSourceFileOriginators(sourceFiles);
		return new ApplicationSnapshot(attrs,sourceFiles, comps);
	}

	protected void scanForModifiedFiles(List<ResourceChange> changes) throws EngineException {
		int origSize = changes.size();
		resourceManager.scanForModifiedFiles(changes);
		
		if (changes.size()>origSize) {
			jasperResources.engineDebug("Scanned for modified files and found "+(changes.size() - origSize));
		}
	}
	
	protected void scanForRemovedFiles(List<ResourceChange> changes) throws EngineException {
		int origSize = changes.size();
		resourceManager.scanForRemovedFiles(changes);

		if (changes.size()>origSize) {
			jasperResources.engineDebug("Scanned for modified files and found "+(changes.size() - origSize));
		}
	}

	protected void scanForAddedFiles(List<ResourceChange> changes) throws EngineException {
		int origSize = changes.size();
		resourceManager.scanForNewFiles(changes);
		
		if (changes.size()>origSize) {
			jasperResources.engineDebug("Scanned for modified files and found "+(changes.size() - origSize));
		}
	}

	public void handleJasperPropertiesChange(String folderPath) {
		processingManager.unloadComponentFiles(folderPath);
		jasperPropertiesChanged = true;
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
		
		// Iterate through the new attributes and look for ones that have been added or changed
		for(Entry<String,String> entry : newAttributes.entrySet()) {
			String name = entry.getKey();
			String type = entry.getValue();
			if (processingManager.getSystemAttributes().get(name)!=null) {
				String existingType = processingManager.getSystemAttributes().get(name);
				if (!type.equals(existingType)) {
					processingManager.removeGlobalSystemAttribute(name);
					//processingManager.unloadItemsForAttribute(name);
				}
				processingManager.addGlobalSystemAttribute(name, type);
			} else {
				processingManager.addGlobalSystemAttribute(name, type);
			}
		}
		// Iterate through old attributes and look for ones that have been removed
		for(Entry<String,String> entry : oldAttributes.entrySet()) {
			String name = entry.getKey();
			//String type = entry.getValue();
			if (newAttributes.get(name)==null) {
				processingManager.removeGlobalSystemAttribute(name);
				//processingManager.unloadItemsForAttribute(name);
			}
		}
		this.systemAttributesFile = f;
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

