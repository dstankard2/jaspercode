package net.sf.jaspercode.engine.processing;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBException;

import net.sf.jaspercode.api.config.ComponentSet;
import net.sf.jaspercode.engine.ComponentFileReader;
import net.sf.jaspercode.engine.EnginePatterns;
import net.sf.jaspercode.engine.definitions.WatchedResource;
import net.sf.jaspercode.engine.impl.ApplicationContextImpl;
import net.sf.jaspercode.engine.definitions.ApplicationFolderImpl;
import net.sf.jaspercode.engine.definitions.ComponentFile;
import net.sf.jaspercode.engine.definitions.UserFile;

public class ResourceManager {

	File rootFolder = null;
	Map<String,WatchedResource> resources = new HashMap<>();
	ApplicationFolderImpl applicationFolder;
	ComponentFileReader componentFileReader = null;
	EnginePatterns patterns = null;
	Map<String,String> systemAttributes = null;
	private ApplicationContextImpl applicationContext = null;

	public ResourceManager(Map<String,String> systemAttributes,File rootFolder, EnginePatterns patterns,ApplicationContextImpl applicationContext) throws JAXBException {
		this.rootFolder = rootFolder;
		this.applicationFolder = new ApplicationFolderImpl(rootFolder,null,applicationContext);
		this.patterns = patterns;
		this.componentFileReader = new ComponentFileReader(applicationContext.getXmlConfigClasses());
		this.systemAttributes = systemAttributes;
		this.applicationContext = applicationContext;
	}

	public ApplicationFolderImpl getRootFolder() {
		return applicationFolder;
	}
	
	private void scanForRemovedFiles(ApplicationFolderImpl defFolder,List<WatchedResource> removed) {
		//File[] files = folder.listFiles();
		System.out.println("TODO: scan for removed files");
	}
	
	// Scans for updated files and removes them.
	public List<WatchedResource> scanForRemovedFiles() {
		List<WatchedResource> ret = new ArrayList<>();
		
		scanForRemovedFiles(applicationFolder, ret);
		
		return ret;
	}
	
	public List<WatchedResource> scanForNewFiles() throws EngineRuntimeException {
		List<WatchedResource> ret = new ArrayList<>();
		
		scanForNewFiles(applicationFolder,ret);
		
		return ret;
	}
	
	protected void scanForNewFiles(ApplicationFolderImpl folder,List<WatchedResource> results) throws EngineRuntimeException {
		File folderFile = folder.getFolderFile();
		File[] contents = folderFile.listFiles();
		List<File> directories = new ArrayList<>();
		
		// First, check jasper properties file in this directory
		File jasperPropsFile = null;
		for(File f : contents) {
			if (f.getName().equals("jasper.properties")) {
				jasperPropsFile = f;
				break;
			}
		}
		
		if (jasperPropsFile==null) {
			folder.getJasperProperties().clear();
			if (folder.getJasperPropertiesLastModified()>0) {
				folder.getComponentFiles().clear();
			}
		} else {
			if (jasperPropsFile.isDirectory()) {
				throw new EngineRuntimeException("File '"+jasperPropsFile.getAbsolutePath()+"' is not a valid properties file");
			}
			if (jasperPropsFile.lastModified()>folder.getJasperPropertiesLastModified()) {
				//folder.getComponentFiles().clear();
				Properties props = new Properties();
				FileReader reader = null;
				try {
					reader = new FileReader(jasperPropsFile);
					props.load(reader);
					for(Object key : props.keySet()) {
						String value = props.getProperty(key.toString());
						folder.getJasperProperties().put(key.toString(), value);
					}
					folder.setLastModified(jasperPropsFile.lastModified());
					if (props.getProperty("ignore")!=null) {
						List<String> ignoreList = Arrays.asList(props.getProperty("ignore").split(","));
						folder.setIgnoreFiles(ignoreList);
					}

					String logLevel = folder.getProperties().get("logLevel");
					if (logLevel==null) logLevel = "WARN";
					else if (logLevel.equalsIgnoreCase("warn")) logLevel = "WARN";
					else if (logLevel.equalsIgnoreCase("error")) logLevel = "ERROR";
					else if (logLevel.equalsIgnoreCase("info")) logLevel = "INFO";
					else if (logLevel.equalsIgnoreCase("debug")) logLevel = "DEBUG";
					else throw new EngineRuntimeException("Properties in folder '"+folder.getPath()+"' has invalid log level '"+logLevel+"' specified");
					folder.setLogLevel(logLevel);
				} catch(IOException e) {
					throw new EngineRuntimeException("Couldn't read properties '"+jasperPropsFile.getAbsolutePath()+"'",e);
				} finally {
					if (reader!=null) {
						try {
							reader.close();
						} catch(Exception e) { }
					}
				}
			}
		}

		// Second, check for systemAttributes.properties file in this directory
		File attributesFile = null;
		for(File f : contents) {
			if (f.getName().equals("systemAttributes.properties")) {
				attributesFile = f;
				break;
			}
		}
		if (attributesFile!=null) {
			if (attributesFile.lastModified() > folder.getSystemAttributesModified()) {
				if (attributesFile.isDirectory()) {
					throw new EngineRuntimeException("File '"+attributesFile.getAbsolutePath()+"' must be a file, not a directory");
				}

				Properties props = new Properties();
				FileReader reader = null;
				try {
					folder.setSystemAttributesModified(attributesFile.lastModified());
					reader = new FileReader(attributesFile);
					props.load(reader);
					for(Object key : props.keySet()) {
						String value = props.getProperty(key.toString());
						systemAttributes.put(key.toString(), value);
					}
				} catch(IOException e) {
					throw new EngineRuntimeException("Couldn't read attributes file '"+attributesFile.getAbsolutePath()+"'",e);
				} finally {
					if (reader!=null) {
						try {
							reader.close();
						} catch(Exception e) { }
					}
				}

			}
		}
		
		for(File f : contents) {
			if (folder.getIgnoreFiles().contains(f.getName())) {
				continue;
			}
			if (f.isDirectory()) {
				directories.add(f);
				continue;
			}
			if (f.getName().equals("jasper.properties")) continue;
			if (f.getName().equals("systemAttributes.properties")) continue;

			if (f.getName().endsWith(".xml")) {
				ComponentSet comps;
				try {
					comps = componentFileReader.readFile(f);
					ComponentFile newFile = new ComponentFile(comps,f,folder);
					results.add(newFile);
					folder.getComponentFiles().put(f.getName(), newFile);
				} catch(JAXBException e) {
					UserFile uf = new UserFile(f,folder);
					results.add(uf);
					folder.getUserFiles().put(f.getName(), uf);
				} catch(IOException e) {
					throw new EngineRuntimeException("Couldn't read file '"+f.getAbsolutePath()+"'",e);
				}
			} else {
				UserFile uf = new UserFile(f,folder);
				results.add(uf);
				folder.getUserFiles().put(f.getName(), uf);
			}
		}
		
		for(File f : directories) {
			ApplicationFolderImpl sub = folder.getSubFolders().get(f.getName());
			if (sub==null) {
				sub = new ApplicationFolderImpl(f,folder,applicationContext);
				results.add(sub);
				folder.getSubFolders().put(f.getName(), sub);
			}
			scanForNewFiles(sub, results);
		}
		
	}

	/*
	protected void scanDirectory(String path,File folder,List<ApplicationFile> scanned) {
		File[] contents = folder.listFiles();
		List<File> subfolders = new ArrayList<>();

		for(File f : contents) {
			if (f.isDirectory()) {
				subfolders.add(f);
				continue;
			}
		}

		for(File f : subfolders) {
			String newPath = path + f.getName()+'/';
			if (resources.get(newPath)!=null) {
				
			}
		}

	}
	*/

}
