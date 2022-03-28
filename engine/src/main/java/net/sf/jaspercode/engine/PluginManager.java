package net.sf.jaspercode.engine;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.XmlConfig;
import net.sf.jaspercode.api.config.Property;
import net.sf.jaspercode.api.config.BuildComponent;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.config.ComponentSet;
import net.sf.jaspercode.api.plugin.ApplicationPlugin;
import net.sf.jaspercode.api.plugin.EnginePlugin;

public class PluginManager {
	private Set<Class<?>> plugins = null;
	private File[] pluginLocations = null;
	private Set<Class<?>> xmlConfigClasses = null;

	public Set<Class<EnginePlugin>> getEnginePlugins() {
		return getPluginSubclasses(EnginePlugin.class);
	}

	public Set<Class<ApplicationPlugin>> getApplicationPlugins() {
		return getPluginSubclasses(ApplicationPlugin.class);
	}

	public PluginManager(File[] libs) throws EngineInitException {
		pluginLocations = libs;

		plugins = new HashSet<>();
		for(File loc : pluginLocations) {
			if (loc.isDirectory()) {
				scanDirectory("",loc,Plugin.class);
			} else {
				scanJar(loc,Plugin.class);
			}
		}
		xmlConfigClasses = getPluginsWithAnnotation(XmlConfig.class);
		xmlConfigClasses.add(Component.class);
		xmlConfigClasses.add(ComponentSet.class);
		xmlConfigClasses.add(BuildComponent.class);
		xmlConfigClasses.add(Property.class);
	}
	
	public Set<Class<?>> getXmlConfigClasses() {
		return xmlConfigClasses;
	}
	
	public Set<Class<?>> getPluginsWithAnnotation(Class<? extends Annotation> cl) {
		Set<Class<?>> ret = new HashSet<>();
		
		for(Class<?> plugin : plugins) {
			if (plugin.isAnnotationPresent(cl)) {
				ret.add(plugin);
			}
		}
		
		return ret;
	}

	@SuppressWarnings("unchecked")
	public <T> Set<Class<T>> getPluginSubclasses(Class<T> superClass) {
		HashSet<Class<T>> ret = new HashSet<>();
		
		for(Class<?> plugin : plugins) {
			if (superClass.isAssignableFrom(plugin)) {
				ret.add((Class<T>)plugin);
			}
		}
		
		return ret;
	}
	
	private void scanJar(File file,Class<? extends Annotation> an) throws EngineInitException {
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

	private void scanDirectory(String pkg,File dir,Class<? extends Annotation> an) throws EngineInitException {
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

}
