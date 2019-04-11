package net.sf.jaspercode.engine;

import java.io.File;
import java.util.HashMap;

import javax.xml.bind.JAXBException;

import net.sf.jaspercode.engine.application.ApplicationManager;

public class JasperAgent {
	private File[] libs = null;
	private String homeDir = null;
	private EnginePatterns patterns = null;
	private EngineLanguages languages = null;
	private EngineProperties engineProperties = null;
	private PluginManager pluginManager = null;
	private HashMap<String,ApplicationManager> apps = new HashMap<>();
	private boolean done = false;
	
	public JasperAgent(File[] libFiles,HashMap<String,String> userOptions) {
		String homeDir = userOptions.get("home.dir");
		libs = libFiles;
		this.homeDir = homeDir;
		this.engineProperties = new EngineProperties(userOptions);
	}

	public void start() throws EngineInitException {
		boolean once = engineProperties.getRunOnce();
		if (homeDir!=null) {
			initializeEngineProperties();
		} else {
			
		}
		initializePluginManager(libs);
		initializePatterns();
		initializeLanguages();
		while(!done) {
			if (once) done = true;
			scanApplications();
			try {
				if (!once)
					Thread.sleep(1000);
				else done = true;
			} catch(Exception e) {
				// no-op
			}
		}
	}
	
	protected void scanSingleApp() throws EngineInitException {
		if (apps.size()==0) {
			String appDir = engineProperties.getApplicationDir();
			File appDirFile = new File(appDir);
			String name = appDirFile.getName();
			ApplicationManager proc = null;
			String outputDir = engineProperties.getOutputDir();
			//ApplicationContextImpl applicationContext = new ApplicationContextImpl(this.engineProperties, pluginManager);
			
			File outputDirFile = new File(outputDir);
			
			try {
				proc = new ApplicationManager(name,appDirFile,outputDirFile,engineProperties,patterns,languages,pluginManager);
				//proc = new ApplicationManager(appDirFile,outputDir,patterns,languages,applicationContext);
			} catch(JAXBException e) {
				throw new EngineInitException("Couldn't initialize application manager",e);
			//} catch(JasperException e) {
			//	throw new EngineInitException("Couldn't initialize application manager",e);
			}
			apps.put(name, proc);
		}
		
		for(ApplicationManager proc : apps.values()) {
			try {
				proc.scan();
			} catch(Exception e) {
				System.out.println("Exception while scanning application '"+proc.getApplicationName()+"': ");
				e.printStackTrace();
			}
		}
	}
	
	protected void scanApplications() throws EngineInitException {
		if (engineProperties.getSingleApp()) scanSingleApp();
	}
	
	protected void initializePluginManager(File libs[]) throws EngineInitException {
		this.pluginManager = new PluginManager(libs);
	}

	protected void initializePatterns() throws EngineInitException {
		patterns = new EnginePatterns(pluginManager);
		patterns.findPatterns();
	}
	
	protected void initializeLanguages() throws EngineInitException {
		languages = new EngineLanguages(pluginManager);
		languages.findLanguages();
	}

	// TODO: Implement
	private void initializeEngineProperties() {
		// Find engine.properties in home/bin/engine.properties
		File binDir = new File(homeDir,"bin");
		if ((!binDir.exists()) || (!binDir.isDirectory())) return;
		//File engineProps = new File(binDir,"engine.properties");
		
	}

}
