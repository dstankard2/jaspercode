package net.sf.jaspercode.engine;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.sf.jaspercode.api.annotation.Plugin;

public class AnnotationHelper {

	//private static Set<Class<?>> plugins = null;
	//private static File[] pluginLocations = null;
	
	/*
	public void resetPluginLocations() {
		plugins = null;
		pluginLocations = null;
	}
	*/

	/*
	public static Set<Class<?>> getPluginSubclasses(Class<?> superclass) {
		HashSet<Class<?>> ret = new HashSet<>();
		
		for(Class<?> plugin : plugins) {
			if (superclass.isAssignableFrom(plugin)) {
				ret.add(plugin);
			}
		}
		
		return ret;
	}
	*/

	/*
	public static Set<Class<?>> getPluginsWithAnnotation(Class<? extends Annotation> cl) {
		Set<Class<?>> ret = new HashSet<>();
		
		for(Class<?> plugin : plugins) {
			if (plugin.isAnnotationPresent(cl)) {
				ret.add(plugin);
			}
		}
		
		return ret;
	}
	*/

	/*
	public static void setPluginLocations(File[] locations) throws EngineInitException {
		if (pluginLocations!=null) {
			throw new RuntimeException("Cannot set plugin locations twice in AnnotationHelper");
		}
		pluginLocations = locations;

		plugins = new HashSet<>();
		for(File loc : locations) {
			if (loc.isDirectory()) {
				scanDirectory("",loc,Plugin.class);
			} else {
				scanJar(loc,Plugin.class);
			}
		}
	}
	*/
	
	/*
	@SuppressWarnings("unchecked")
	public static <T> Set<Class<T>> findSubclasses(Class<T> superClass) {
		HashSet<Class<T>> ret = new HashSet<>();
		
		for(Class<?> cl : plugins) {
			if (superClass.isAssignableFrom(cl)) {
				ret.add((Class<T>)cl);
			}
		}
		
		return ret;
	}
	
	public static Set<Class<?>> findAnnotatedClasses(Class<? extends Annotation> an) {
		HashSet<Class<?>> ret = new HashSet<>();
		
		for(Class<?> cl : plugins) {
			if (cl.isAnnotationPresent(an)) {
				ret.add(cl);
			}
		}
		
		return ret;
	}
	*/
	/*
	public static List<Class<?>> findAnnotatedClasses(Class<? extends Annotation> an,File[] locations) throws ClassNotFoundException,IOException {
		List<Class<?>> ret = new ArrayList<Class<?>>();

		for(File loc : locations) {
			if (loc.isDirectory()) {
				scanDirectory("",loc,an,ret);
			} else {
				scanJar(loc,an,ret);
			}
		}


		return ret;
	}
	*/

	/*
	private static void scanJar(File file,Class<? extends Annotation> an) throws EngineInitException {
		JarFile jar = null;

		try {
			jar = new JarFile(file);

			Enumeration<JarEntry> entries = jar.entries();
			while(entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				if (entry.getName().endsWith(".class")) {
					String name = entry.getName().substring(0, entry.getName().length()-6);
					name = name.replace('/', '.');
					try {
						Class<?> cl = Thread.currentThread().getContextClassLoader().loadClass(name);
						if ((cl.isAnnotationPresent(an)) && (!plugins.contains(cl))) {
							plugins.add(cl);
						}
					} catch(NoClassDefFoundError e) { 
						// No-op if couldn't find the class.
					}
				}
			}
		} catch(Exception e) {
			throw new EngineInitException("Couldn't scan jar file '"+file.getAbsolutePath()+"'",e);
		} finally {
			if (jar!=null) {
				try { jar.close(); } catch(Exception e) { }
			}
		}
	}

	private static void scanDirectory(String pkg,File dir,Class<? extends Annotation> an) throws EngineInitException {
		try {
			File contents[] = dir.listFiles();
			for(File f : contents) {
				if (f.isDirectory()) {
					String newPkg = "";
					if (pkg.length()>0) {
						newPkg = pkg+'.'+f.getName();
					} else {
						newPkg = f.getName();
					}
					scanDirectory(newPkg,f,an);
				}
				else {
					if (f.getName().endsWith(".class")) {
						String clName = pkg+'.'+f.getName().substring(0, f.getName().length()-6);
						Class<?> cl = Thread.currentThread().getContextClassLoader().loadClass(clName);
						if ((cl.isAnnotationPresent(an)) && (!plugins.contains(cl))) {
							plugins.add(cl);
						}
					}
				}
			}
		} catch(ClassNotFoundException e) {
			throw new EngineInitException("Exception while scanning for Jasper plugins",e);
		}
	}
	*/

}
