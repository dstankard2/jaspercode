package net.sf.jaspercode.eng.application;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.eng.EngineInitException;
import net.sf.jaspercode.eng.EngineLanguages;
import net.sf.jaspercode.eng.EnginePatterns;
import net.sf.jaspercode.eng.EngineProperties;
import net.sf.jaspercode.eng.JasperResources;
import net.sf.jaspercode.eng.PluginManager;
import net.sf.jaspercode.eng.exception.EngineException;
import net.sf.jaspercode.eng.files.ApplicationFolderImpl;
import net.sf.jaspercode.eng.files.ComponentFile;
import net.sf.jaspercode.eng.files.UserFile;
import net.sf.jaspercode.eng.processing.ProcessorLog;


public class ApplicationManager implements ProcessingContext {
	private String applicationName;
	private ProcessorLog appLog = null;
	private JasperResources jasperResources = null;
	private ResourceManager resourceManager = null;
	//private SystemAttributesFile systemAttributesFile = null;
	private OutputManager outputManager = null;
	private ProcessingManager processingManager = null;

	boolean firstScan = true;

	public ApplicationManager(String applicationName, File applicationDir,File outputDir, EngineProperties engineProperties,
			EnginePatterns patterns,EngineLanguages languages,PluginManager pluginManager) throws EngineInitException {
		this.applicationName = applicationName;
		this.appLog = new ProcessorLog("Application:"+applicationName);
		this.jasperResources = new JasperResources(engineProperties, pluginManager);
		this.resourceManager = new ResourceManager(applicationDir, jasperResources);
		this.outputManager = new OutputManager(outputDir, appLog);
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
		boolean changeDetected = false;

		if (firstScan) {
			changeDetected = true;
		} else if (resourceManager.hasChanges()) {
			changeDetected = true;
		}

		if (changeDetected) {
			try {
				outputManager.clearOutput();
				Map<String,String> systemAttributes = resourceManager.readSystemAttributesFile();
				ApplicationFolderImpl folder = resourceManager.readApplicationDirectory();

				List<UserFile> userFiles = getUserFiles(folder);
				List<ComponentFile> componentFiles = getComponentFiles(folder);
				
				processingManager.process(systemAttributes, userFiles, componentFiles);
				appLog.info("Scan completed");
			} catch(EngineException e) {
				appLog.error("Couldn't scan application folder", e);
			}
		}

		firstScan = false;
		appLog.outputToSystem();
	}
	
	private List<UserFile> getUserFiles(ApplicationFolderImpl folder) {
		List<UserFile> ret = new ArrayList<>();
		
		folder.getUserFiles().values().stream().forEach(uf -> ret.add(uf));
		folder.getSubFolders().values().stream().forEach(subfolder -> {
			ret.addAll(getUserFiles(subfolder));
		});
		
		return ret;
	}

	private List<ComponentFile> getComponentFiles(ApplicationFolderImpl folder) {
		List<ComponentFile> ret = new ArrayList<>();
		
		folder.getComponentFiles().values().stream().forEach(uf -> ret.add(uf));
		folder.getSubFolders().values().stream().forEach(subfolder -> {
			ret.addAll(getComponentFiles(subfolder));
		});
		
		return ret;
	}

	@Override
	public void writeUserFile(UserFile userFile) {
		outputManager.writeUserFile(userFile);
	}

	@Override
	public void writeSourceFile(SourceFile srcFile) {
		outputManager.writeSourceFile(srcFile);
	}

}

