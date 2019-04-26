package net.sf.jaspercode.engine;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sf.jaspercode.api.plugin.EnginePlugin;
import net.sf.jaspercode.api.plugin.PluginContext;
import net.sf.jaspercode.engine.application.ApplicationManager;
import net.sf.jaspercode.engine.processing.ProcessorLog;

public class JasperAgent {
	private File[] libs = null;
	private String homeDir = null;
	private EnginePatterns patterns = null;
	private EngineLanguages languages = null;
	private EngineProperties engineProperties = null;
	private PluginManager pluginManager = null;
	private HashMap<String,ApplicationManager> apps = new HashMap<>();
	private boolean done = false;
	Map<String,EnginePlugin> enginePlugins = new HashMap<>();
	private ProcessorLog engineLogger = null;
	
	public JasperAgent(File[] libFiles,HashMap<String,String> userOptions) {
		String homeDir = userOptions.get("home.dir");
		libs = libFiles;
		this.homeDir = homeDir;
		this.engineProperties = new EngineProperties(userOptions);
		this.engineLogger = new ProcessorLog("SYSTEM");
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
		if (!once) {
			startEnginePlugins();
		}
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
	
	protected void startEnginePlugins() throws EngineInitException {
		// When we start the engine, enable all engine plugins
		this.engineLogger.info("Scanning for engine plugins");
		Set<Class<EnginePlugin>> pluginClasses = pluginManager.getEnginePlugins();
		for(Class<EnginePlugin> pluginClass : pluginClasses) {
			String className = pluginClass.getCanonicalName();
			try {
				EnginePlugin plugin = pluginClass.newInstance();
				String name = plugin.getPluginName();
				if ((name==null) || (name.trim().length()==0)) {
					throw new EngineInitException("Found plugin '"+className+"' with no name");
				}
				if (enginePlugins.get(name)!=null) {
					throw new EngineInitException("Found multiple engine plugins with name '"+name+"'");
				}
				String prop = "enginePlugin."+plugin.getPluginName() + ".enabled";
				boolean enabled = this.engineProperties.getBoolean(prop, false);
				if (enabled) {
					PluginContext ctx = new PluginContextImpl(this.engineLogger, engineProperties);
					plugin.setPluginContext(ctx);
					plugin.engineStart();
					enginePlugins.put(name, plugin);
				} else {
					engineLogger.info("Found plugin '"+plugin.getPluginName()+"' of class class '"+className+"' but it is not enabled");
				}
			} catch(IllegalAccessException | InstantiationException e) {
				throw new EngineInitException("Couldn't initialize engine plugin class "+className, e);
			}
		}
		this.engineLogger.info("Engine plugin scan complete");
		this.engineLogger.flushToSystem();
	}
	
	protected void scanSingleApp() throws EngineInitException {
		if (apps.size()==0) {
			String appDir = engineProperties.getApplicationDir();
			File appDirFile = new File(appDir);
			String name = appDirFile.getName();
			ApplicationManager proc = null;
			String outputDir = engineProperties.getOutputDir();
			
			File outputDirFile = new File(outputDir);
			
			proc = new ApplicationManager(name,appDirFile,outputDirFile,engineProperties,patterns,languages,pluginManager/*, engineLogger*/);
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

	private void initializeEngineProperties() {
		// Find engine.properties in home/bin/engine.properties
		File binDir = new File(homeDir,"bin");
		if ((!binDir.exists()) || (!binDir.isDirectory())) return;
		//File engineProps = new File(binDir,"engine.properties");
		
	}

}
