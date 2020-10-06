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
import net.sf.jaspercode.engine.processing.ProcessingManager;
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
	//private boolean jasperPropertiesChanged = false;
	private boolean otherChangeDetected = false;

	private SystemAttributesFile systemAttributesFile = null;

	Map<String,ApplicationPlugin> plugins = new HashMap<>();
	
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
		this.processingManager = new ProcessingManager(jasperResources, patterns, languages, this, appLog);
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
				appLog.outputToSystem();
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
		otherChangeDetected = false;
		for(Entry<String,ApplicationPlugin> entry : this.plugins.entrySet()) {
			entry.getValue().scanStart();
		}
		
		buildManager.scanStarted();
		
		this.appLog.getMessages(true);
		
		if (firstScan) {
			System.out.println("*** Begin scan of application '"+this.getApplicationName()+"'");
		}

		List<ResourceChange> changes = new ArrayList<>();
		if (!firstScan) {
			scanForRemovedFiles(changes);
			scanForModifiedFiles(changes);
		}
		scanForAddedFiles(changes);

		boolean changeDetected = (changes.size()>0) || (otherChangeDetected) ;

		if (changeDetected) {
			if (!firstScan) {
				System.out.println("*** Begin scan of application '"+this.getApplicationName()+"'");
			}
			
		} else if (firstScan) {
			System.out.println("*** Initial scan found no files - Ending ***");
			//firstScan = false;
			//return;
		}
		
		if (changeDetected) {
			//jasperPropertiesChanged = false;
			for(ResourceChange change : changes) {
				String changePath = change.getPath();
				buildManager.changeDetected(changePath);
				boolean newFile = (change.getOldFile()==null) && (change.getNewFile()!=null);
				
				if (change.getOldFile()!=null) {
					if (change.wasUserFile()) {
						processingManager.removeUserFile(change.getPath());
						outputManager.removeUserFile((UserFile)change.getOldFile());
					}
					else if (change.wasComponentFile()) {
						processingManager.removeComponentFile((ComponentFile)change.getOldFile());
					} else {
						jasperResources.engineDebug("Found old file that wasn't accounted for in scan - "+change.getPath());
					}
				}

				if (change.getNewFile()!=null) {
					if (change.isUserFile()) {
						if (newFile)
							processingManager.addUserFile(change.getPath());
						else 
							processingManager.changeUserFile(change.getPath());

						outputManager.writeUserFile((UserFile)change.getNewFile());
					}
					else if (change.isComponentFile()) {
						processingManager.addComponentFile((ComponentFile)change.getNewFile());
					} else {
						jasperResources.engineDebug("Found new file that wasn't accounted for in scan - "+change.getPath());
					}
				}
			}
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
			buildManager.checkCommands();
		}

		// First scan is complete
		firstScan = false;
	}
	
	// TOD: Implement
	protected ApplicationSnapshot createSnapshot() {
		/*
		List<SourceFileSnapshot> sourceFiles = outputManager.getSourceFileSnapshots();
		List<SystemAttributeSnapshot> attrs = processingManager.getSystemAttributeSnapshots();
		List<ItemSnapshot> comps = processingManager.getItemSnapshots();

		processingManager.populateSourceFileOriginators(sourceFiles);
		return new ApplicationSnapshot(attrs,sourceFiles, comps);
		*/
		return null;
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
		otherChangeDetected = true;
	}

	// TODO: Determine if this should be handled differently.
	public void handleSystemAttributesFileChange(SystemAttributesFile f) {
		Map<String,String> newAttributes = new HashMap<>();
		otherChangeDetected = true;

		if (f!=null) {
			newAttributes = f.getSystemAttributes();
		}
		/*
		if (this.systemAttributesFile!=null) {
			oldAttributes = systemAttributesFile.getSystemAttributes();
		}
		*/
		this.systemAttributesFile = f;

		// Unload all components
		processingManager.unloadComponentFiles("/");
		
		// Reset global attributes in the processing manager
		processingManager.setGlobalSystemAttributes(newAttributes);
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

