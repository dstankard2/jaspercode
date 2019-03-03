package net.sf.jaspercode.launcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import static java.lang.System.getenv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class AgentLoader {
	
	String home = null;
	Properties daemonProperties;
	File libDir = null;
	private boolean runOnce = false;
	private boolean daemonMode = true;
	private HashMap<String,String> userOptions = new HashMap<>();
	
	private static final String HELP = "Usage: jasper.bat [-h] [-D<definitions_dir>] [-d<single_definition_dir>] [-s]";
	
	public AgentLoader(String args[]) throws Throwable {

		if ((args.length==1) && (args[0].equals("-h"))) {
			System.out.println(HELP);
			daemonMode = false;
			runOnce = false;
			return;
		}
		for(String arg : args ) {
			if (arg.startsWith("-D")) {
				
			}
		}
	}

	public void start() throws FileNotFoundException, IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		if ((!runOnce) && (!daemonMode)) {
			return;
		}
		
		String home = getenv("JASPER_HOME");
		if (home==null) {
			throw new Error("Environment variable JASPER_HOME not set");
		}
		File homeDir = new File(home);
		if ((!homeDir.exists()) || (!homeDir.isDirectory())) {
			throw new Error("JASPER_HOME is not a valid location");
		}
		File binDir = new File(homeDir,"bin");
		if ((!binDir.exists()) || (!binDir.isDirectory())) {
			throw new Error("Could not find JASPER_HOME bin directory");
		}
		File propFile = new File(binDir,"daemon.properties");
		if ((!propFile.exists()) && (!propFile.isFile())) {
			throw new Error("Could not find daemon.properties in Jasper distribution");
		}
		daemonProperties = new Properties();
		daemonProperties.load(new FileReader(propFile));
		
		libDir = new File(homeDir,"lib");
		if ((!libDir.exists()) && (!libDir.isDirectory())) {
			throw new Error("Could not find lib directory in Jasper distribution");
		}

		File[] libFiles = libDir.listFiles();
		ArrayList<File> l = new ArrayList<File>();
		
		for(File file : libFiles) {
			if ((file.getName().endsWith(".jar")) && (file.isFile()) && (file.exists())) {
				l.add(file);
			}
		}

		URL[] libUrls = new URL[l.size()];
		for(int i=0;i<l.size();i++) {
			libUrls[i] = l.get(i).toURI().toURL();
		}
		ClassLoader original = Thread.currentThread().getContextClassLoader();
		URLClassLoader loader = new URLClassLoader(libUrls, original);

		try {
			Class<?> cl = loader.loadClass("net.sf.jaspercode.engine.runner.JasperAgent");
			Constructor<?> cons = cl.getConstructor(File[].class,File.class,HashMap.class);
			Object obj = cons.newInstance(libFiles, homeDir, userOptions);
			Method start = cl.getMethod("start");
			start.invoke(obj);
		} finally {
			if (original!=null) {
				Thread.currentThread().setContextClassLoader(original);
			}
		}
	}

}
