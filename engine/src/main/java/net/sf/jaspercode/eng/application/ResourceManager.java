package net.sf.jaspercode.eng.application;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import net.sf.jaspercode.api.config.ComponentSet;
import net.sf.jaspercode.eng.ComponentFileReader;
import net.sf.jaspercode.eng.EngineInitException;
import net.sf.jaspercode.eng.JasperResources;
import net.sf.jaspercode.eng.exception.EngineException;
import net.sf.jaspercode.eng.files.ApplicationFolderImpl;
import net.sf.jaspercode.eng.files.ComponentFile;
import net.sf.jaspercode.eng.files.JasperPropertiesFile;
import net.sf.jaspercode.eng.files.UserFile;

public class ResourceManager {
	File rootFolder = null;
	ComponentFileReader componentFileReader;
	JasperResources jasperResources;
	long lastScan = 0L;
	
	public ResourceManager(File rootFolder, JasperResources jasperResources) throws EngineInitException {
		this.rootFolder = rootFolder;
		this.componentFileReader = new ComponentFileReader(jasperResources.getXmlConfigClasses());
		this.jasperResources = jasperResources;
	}

	// After the initial scan, are there file changes?
	public boolean hasChanges() {
		return folderHasChanges(rootFolder);
	}
	private boolean folderHasChanges(File folder) {
		for(File file : folder.listFiles()) {
			if (file.isDirectory()) {
				if (folderHasChanges(file)) {
					return true;
				}
			}
			if (file.lastModified() > lastScan) {
				return true;
			}
		}
		return false;
	}

	public Map<String,String> readSystemAttributesFile() throws EngineException {
		Map<String,String> ret = new HashMap<>();
		File file = new File(rootFolder, "systemAttributes.properties");

		if (file.exists()) {
			if (file.isDirectory()) {
				throw new EngineException("File '"+file.getAbsolutePath()+"' is not a valid properties file");
			}
			
			Properties props = new Properties();
			FileReader reader = null;
			
			try {
				reader = new FileReader(file);
				props.load(reader);

				for(Object key : props.keySet()) {
					String value = props.getProperty(key.toString());
					ret.put(key.toString(), value);
				}
			} catch(IOException e) {
				throw new EngineException("Couldn't read attributes file '"+file.getAbsolutePath()+"'",e);
			} finally {
				if (reader!=null) {
					try {
						reader.close();
					} catch(Exception e) { }
				}
			}
		}
		

		return ret;
	}

	
	public ApplicationFolderImpl readApplicationDirectory() throws EngineException {
		ApplicationFolderImpl ret = null;
		lastScan = System.currentTimeMillis();
		
		ret = scanDirectory(rootFolder, null);
		
		return ret;
	}
	
	private JasperPropertiesFile readJasperProperties(File file,ApplicationFolderImpl folder) throws EngineException {
		Properties props = new Properties();
		JasperPropertiesFile ret = null;
		FileReader reader = null;
		
		try {
			ret = new JasperPropertiesFile(new HashMap<>(), file.lastModified(), folder);

			reader = new FileReader(file);
			props.load(reader);
			for(Object key : props.keySet()) {
				String value = props.getProperty(key.toString());
				ret.getProperties().put(key.toString(), value);
			}

			// Evaluate ignore files in jasper.properties
			String ignoreStr = ret.getProperties().get("ignore");
			folder.getIgnoreFiles().clear();
			if (ignoreStr!=null) {
				String ignores[] = ignoreStr.split(",");
				for(String ig : ignores) {
					folder.getIgnoreFiles().add(ig);
				}
			}
		} catch(IOException e) {
			throw new EngineException("Couldn't read properties '"+file.getAbsolutePath()+"'",e);
		} finally {
			if (reader!=null) {
				try {
					reader.close();
				} catch(Exception e) { }
			}
		}
		return ret;
	}
	
	private JasperPropertiesFile readJasperProperties(File[] files, ApplicationFolderImpl folder) throws EngineException {
		JasperPropertiesFile ret = null;
		
		File file = Arrays.asList(files).stream().filter(f -> f.isDirectory()==false && f.getName().equals("jasper.properties")).findFirst().orElse(null);
		if (file!=null) {
			ret = readJasperProperties(file, folder);
		}
		return ret;
	}

	private boolean isIgnoreFile(String filename, ApplicationFolderImpl folder) {
		if (folder.getIgnoreFiles().contains(filename)) {
			return true;
		}
		return false;
	}

	private ApplicationFolderImpl scanDirectory(File folder,ApplicationFolderImpl parent) throws EngineException {
		ApplicationFolderImpl ret = new ApplicationFolderImpl(folder, parent);
		File[] contents = folder.listFiles();

		JasperPropertiesFile props = readJasperProperties(contents, ret);
		ret.setJasperProperties(props);
		
		for(File f : contents) {
			if (isIgnoreFile(f.getName(), ret)) {
				continue;
			}

			if (f.isDirectory()) {
				// Read directory recursively
				ApplicationFolderImpl sub = scanDirectory(f, ret);
				ret.getSubFolders().put(f.getName(), sub);
			} else if (f.getName().equals("jasper.properties")) {
				continue;
			} else if ((f.getName().equals("systemAttributes.properties")) && (parent==null)) {
				continue;
			} else {
				ComponentFile compFile = null;
				if (f.getName().endsWith(".xml")) {
					ComponentSet set = componentFileReader.readFile(f);
					if (set!=null) {
						compFile = new ComponentFile(set, f, ret);
					}
				}
				if (compFile==null) {
					ret.getUserFiles().put(f.getName(), new UserFile(f, ret));
				} else {
					ret.getComponentFiles().put(f.getName(), compFile);
				}
			}
		}
		
		return ret;
	}

}
