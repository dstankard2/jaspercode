package net.sf.jaspercode.engine.application;

import java.io.File;
import java.util.List;
import java.util.Map;

import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.engine.EngineInitException;
import net.sf.jaspercode.engine.EngineLanguages;
import net.sf.jaspercode.engine.EnginePatterns;
import net.sf.jaspercode.engine.EngineProperties;
import net.sf.jaspercode.engine.JasperResources;
import net.sf.jaspercode.engine.PluginManager;
import net.sf.jaspercode.engine.files.UserFile;
import net.sf.jaspercode.engine.processing.FileChange;
import net.sf.jaspercode.engine.processing.ProcessorLog;

public class ApplicationManager implements ProcessingContext {
	private String applicationName;
	private ProcessorLog appLog = null;
	private JasperResources jasperResources = null;
	private ResourceManager resourceManager = null;
	private OutputManager outputManager = null;
	private ProcessingManager processingManager = null;

	long lastScan = 0L;

	public ApplicationManager(String applicationName, File applicationDir,File outputDir, EngineProperties engineProperties,
			EnginePatterns patterns,EngineLanguages languages,PluginManager pluginManager, JasperResources jasperResources) throws EngineInitException {
		this.applicationName = applicationName;
		this.appLog = new ProcessorLog("Application:"+applicationName);
		this.jasperResources = jasperResources;
		this.resourceManager = new ResourceManager(applicationDir, jasperResources);
		this.outputManager = new OutputManager(outputDir, jasperResources, appLog);
		this.processingManager = new ProcessingManager(this, jasperResources, patterns, languages, appLog);
		/*
		this.buildManager = new BuildManager(resourceManager.getRootFolder());
		*/
		//startApplicationPlugins();
	}
	
	public String getApplicationName() {
		return applicationName;
	}
	
	public void scan() {
		this.appLog.getMessages(true);
		long start = System.currentTimeMillis();
		boolean firstScan = lastScan == 0L;

		List<FileChange> changes = resourceManager.getFileChanges();
		lastScan = System.currentTimeMillis();
		
		if (firstScan) {
			appLog.info("*** Begin scan of application "+this.applicationName+" ***");
			outputManager.clearOutput();
		}
		
		if (changes.size()>0) {
			// Change detected
			if (!firstScan) {
				appLog.info("*** Begin scan of application "+this.applicationName+" ***");
			}
			appLog.info("Scan found "+changes.size()+" updated files");
			Map<String,String> globalSystemAttributes = resourceManager.getSystemAttributes();
			// Process changes, output source files
			processingManager.processChanges(globalSystemAttributes, changes);
			outputManager.flushSourceFiles();

			long duration = System.currentTimeMillis() - start;
			appLog.info("Scan completed in "+duration+" millis");
		} else {
			if (firstScan) {
				this.appLog.info("Initial scan found no file changes");
			} else {
				// no-op
			}
		}
		
		appLog.outputToSystem();
	}

	@Override
	public Map<String, UserFile> getUserFiles() {
		return outputManager.getUserFiles();
	}

	@Override
	public void removeUserFile(UserFile userFile) {
		outputManager.removeUserFile(userFile);
	}

	@Override
	public SourceFile getSourceFile(String path) {
		return outputManager.getSourceFile(path);
	}

	@Override
	public void addSourceFile(SourceFile srcFile) {
		outputManager.addSourceFile(srcFile);
	}

	@Override
	public void removeSourceFile(String path) {
		outputManager.removeSourceFile(path);
	}

	@Override
	public void writeUserFile(UserFile userFile) {
		outputManager.writeUserFile(userFile);
	}

}

