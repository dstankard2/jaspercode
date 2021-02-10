package net.sf.jaspercode.engine.application;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.engine.EngineInitException;
import net.sf.jaspercode.engine.EngineLanguages;
import net.sf.jaspercode.engine.EnginePatterns;
import net.sf.jaspercode.engine.EngineProperties;
import net.sf.jaspercode.engine.JasperResources;
import net.sf.jaspercode.engine.PluginManager;
import net.sf.jaspercode.engine.application.ResourceChange;
import net.sf.jaspercode.engine.exception.EngineException;
import net.sf.jaspercode.engine.files.ComponentFile;
import net.sf.jaspercode.engine.files.SystemAttributesFile;
import net.sf.jaspercode.engine.files.UserFile;
import net.sf.jaspercode.engine.processing.ProcessorLog;

public class ApplicationManager implements ResourceContext,ProcessingContext {

	private String applicationName;
	private ProcessorLog appLog = null;
	private JasperResources jasperResources = null;
	private ResourceManager resourceManager = null;
	private SystemAttributesFile systemAttributesFile = null;
	private OutputManager outputManager = null;
	private ProcessingManager processingManager = null;
	/*
	private BuildManager buildManager = null;
	*/

	boolean firstScan = true;
	boolean otherChangeDetected = false;
	
	public ApplicationManager(String applicationName, File applicationDir,File outputDir, EngineProperties engineProperties,
			EnginePatterns patterns,EngineLanguages languages,PluginManager pluginManager) throws EngineInitException {
		this.applicationName = applicationName;
		this.appLog = new ProcessorLog("Application:"+applicationName);
		this.jasperResources = new JasperResources(engineProperties, pluginManager);
		this.resourceManager = new ResourceManager(applicationDir, jasperResources, this);
		this.outputManager = new OutputManager(outputDir, jasperResources, appLog);
		this.processingManager = new ProcessingManager(jasperResources, patterns, languages, this, appLog);
		/*
		this.buildManager = new BuildManager(resourceManager.getRootFolder());
		*/
		//startApplicationPlugins();
	}
	
	public String getApplicationName() {
		return applicationName;
	}

	public void scan() throws EngineException {
		otherChangeDetected = false;
		
		this.appLog.getMessages(true);

		List<ResourceChange> changes = new ArrayList<>();
		
		// TODO: Before the first scan, we need to clean the output directory
		if (firstScan) {
			System.out.println("*** Begin scan of application '"+this.getApplicationName()+"'");
		}

		resourceManager.scanForConfigChanges(systemAttributesFile);
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
		}

		if (changeDetected) {
			//jasperPropertiesChanged = false;
			for(ResourceChange change : changes) {
				//String changePath = change.getPath();
				//buildManager.changeDetected(changePath);
				//boolean newFile = (change.getOldFile()==null) && (change.getNewFile()!=null);
				
				// Out with the old
				if (change.getOldFile()!=null) {
					if (change.wasUserFile()) {
						processingManager.removeUserFile((UserFile)change.getOldFile());
						//outputManager.removeUserFile((UserFile)change.getOldFile());
					}
					else if (change.wasComponentFile()) {
						processingManager.removeComponentFile((ComponentFile)change.getOldFile());
					} else {
						jasperResources.engineDebug("Found old file that wasn't accounted for in scan - "+change.getPath());
					}
				}

				// In with the new
				if (change.getNewFile()!=null) {
					if (change.isUserFile()) {
						processingManager.addUserFile((UserFile)change.getNewFile());
					}
					else if (change.isComponentFile()) {
						processingManager.addComponentFile((ComponentFile)change.getNewFile());
					} else {
						jasperResources.engineDebug("Found new file that wasn't accounted for in scan - "+change.getPath());
					}
				}
			}
			processingManager.processChanges();
			outputManager.flushSourceFiles();
			firstScan = false;
			appLog.info("*** Application scan Complete ***");
			appLog.outputToSystem();
		}
		
	}

	protected void scanForRemovedFiles(List<ResourceChange> changes) throws EngineException {
		int origSize = changes.size();
		resourceManager.scanForRemovedFiles(changes);

		if (changes.size()>origSize) {
			jasperResources.engineDebug("Scanned for modified files and found "+(changes.size() - origSize));
		}
	}

	protected void scanForModifiedFiles(List<ResourceChange> changes) throws EngineException {
		int origSize = changes.size();
		resourceManager.scanForModifiedFiles(changes);

		if (changes.size()>origSize) {
			jasperResources.engineDebug("Scanned for modified files and found "+(changes.size() - origSize));
		}
	}

	protected void scanForAddedFiles(List<ResourceChange> changes) throws EngineException {
		int origSize = changes.size();
		resourceManager.scanForAddedFiles(changes);

		if (changes.size()>origSize) {
			jasperResources.engineDebug("Scanned for modified files and found "+(changes.size() - origSize));
		}
	}

	@Override
	public void updateSystemAttributesFile(SystemAttributesFile file) {
		Map<String,String> attribs = new HashMap<>();
		if (file!=null) attribs = file.getSystemAttributes();
		processingManager.updateGlobalSystemAttributes(attribs);
		this.systemAttributesFile = file;
	}

	@Override
	public Map<String, UserFile> getUserFiles() {
		return outputManager.getUserFiles();
	}
	
	@Override
	public SourceFile getSourceFile(String path) {
		return outputManager.getSourceFile(path);
	}
	
	@Override
	public void writeSourceFile(SourceFile srcFile) {
		outputManager.updateSourceFile(srcFile);
	}
	
	@Override
	public void removeSourceFile(String path) {
		outputManager.removeSourceFile(path);
	}

	@Override
	public void writeUserFile(UserFile userFile) {
		outputManager.writeUserFile(userFile);
	}

	@Override
	public void removeUserFile(UserFile userFile) {
		outputManager.removeUserFile(userFile);
	}
	
}
