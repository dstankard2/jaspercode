package net.sf.jaspercode.engine.application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.xml.bind.JAXBException;

import net.sf.jaspercode.api.config.ComponentSet;
import net.sf.jaspercode.engine.ComponentFileReader;
import net.sf.jaspercode.engine.EngineInitException;
import net.sf.jaspercode.engine.definitions.ApplicationFolderImpl;
import net.sf.jaspercode.engine.definitions.ComponentFile;
import net.sf.jaspercode.engine.definitions.JasperPropertiesFile;
import net.sf.jaspercode.engine.definitions.SystemAttributesFile;
import net.sf.jaspercode.engine.definitions.UserFile;
import net.sf.jaspercode.engine.definitions.WatchedResource;
import net.sf.jaspercode.engine.processing.EngineException;

public class ResourceManager {
	private ApplicationFolderImpl applicationFolder;
	private ComponentFileReader componentFileReader = null;
	private ApplicationManager applicationManager = null;
	private JasperResources applicationContext = null;
	private Map<String,UserFile> userFiles = new HashMap<>();

	public ResourceManager(File rootFolder,ApplicationManager applicationManager,JasperResources applicationContext) throws EngineInitException {
		this.applicationFolder = new ApplicationFolderImpl(rootFolder, null, applicationContext);
		this.applicationManager = applicationManager;
		this.componentFileReader = new ComponentFileReader(applicationContext.getXmlConfigClasses());
		this.applicationContext = applicationContext;
	}

	public WatchedResource getResource(String path) {
		return (WatchedResource)this.applicationFolder.getResource(path);
	}
	
	public Map<String,UserFile> getUserFiles() {
		//return new HashMap<>();
		return userFiles;
	}

	protected File getFile(File[] files, String name) {
		for(File f : files) {
			if (f.getName().equals(name)) {
				return f;
			}
		}
		return null;
	}

	public void scanForModifiedFiles(List<ResourceChange> changes) throws EngineException {
		scanForModifiedFiles(applicationFolder, changes);
	}

	private void scanForModifiedFiles(ApplicationFolderImpl folder,List<ResourceChange> results) throws EngineException {
		File folderFile = folder.getFolderFile();;
		File[] files = null;
		
		if ((!folderFile.exists()) || (!folderFile.isDirectory())) {
			files = new File[0];
		} else {
			files = folderFile.listFiles();
		}

		boolean isRoot = folder==applicationFolder;

		for(Entry<String,WatchedResource> entry : folder.getFiles().entrySet()) {
			String name = entry.getKey();
			WatchedResource oldFile = entry.getValue();
			File file = getFile(files, name);
			if (file!=null) {
				if (oldFile instanceof ApplicationFolderImpl) {
					ApplicationFolderImpl f = (ApplicationFolderImpl)oldFile;
					scanForModifiedFiles(f, results);
				}
				else if (file.lastModified() > oldFile.getLastModified()) {
					if (oldFile instanceof JasperPropertiesFile) {
						this.manageJasperPropertiesFile(file, folder);
					//} else if (res instanceof ApplicationFolderImpl) {
					} else {
						WatchedResource r = createFile(file, folder);
						String path = r.getPath();
						results.add(new ResourceChange(path,oldFile, r));
					}
				}
			}
		}
		
		// Check systemAttributes.properties
		if ((isRoot) && (applicationManager.getSystemAttributesFile()!=null)) {
			File file = getFile(files, "systemAttributes.properties");
			SystemAttributesFile systemAttributesFile = applicationManager.getSystemAttributesFile();
			if ((file!=null) && (file.lastModified() > systemAttributesFile.getLastModified())) {
				SystemAttributesFile n = createSystemAttributesFile(file, folder);
				applicationManager.handleSystemAttributesFileChange(n);
			}
		}
	}
	
	public void scanForRemovedFiles(List<ResourceChange> changes) throws EngineException {
		scanForRemovedFiles(applicationFolder, changes);
	}

	private void scanForRemovedFiles(ApplicationFolderImpl folder,List<ResourceChange> changes) throws EngineException {
		File folderFile = folder.getFolderFile();;
		File[] files = null;
		
		if ((!folderFile.exists()) || (!folderFile.isDirectory())) {
			files = new File[0];
		} else {
			files = folderFile.listFiles();
		}
		
		// If there is no commands.txt and if the application has commands for this folder, then set them null
		if (applicationManager.getCommands(folder.getPath())!=null) {
			boolean found = false;
			for(File f : files) {
				if (f.getName().equals("commands.txt")) {
					found = true;
					break;
				}
			}
			if (!found) applicationManager.setCommands(folder.getPath(), null);
		}

		for(Entry<String,WatchedResource> entry : folder.getFiles().entrySet()) {
			String name = entry.getKey();
			WatchedResource res = entry.getValue();
			if (getFile(files, name)==null) {
				String path = res.getPath();
				if (res instanceof ApplicationFolderImpl) {
					ApplicationFolderImpl f = (ApplicationFolderImpl)res;
					scanForRemovedFiles(f, changes);
				} else if (res instanceof UserFile) {
					userFiles.remove(res.getPath());
					changes.add(new ResourceChange(path, res, null));
				} else if (res instanceof ComponentFile) {
					folder.getComponentFiles().remove(res.getName());
					changes.add(new ResourceChange(path, res, null));
				} else if (res instanceof JasperPropertiesFile) {
					this.manageJasperPropertiesFile(null, folder);
				} else if (res instanceof SystemAttributesFile) {
					applicationManager.handleSystemAttributesFileChange(null);
				}
			}
		}
	}

	protected SystemAttributesFile createSystemAttributesFile(File file,ApplicationFolderImpl folder) throws EngineException {
		SystemAttributesFile ret = null;
		
		if (file.isDirectory()) {
			throw new EngineException("File '"+file.getAbsolutePath()+"' is not a valid properties file");
		}
		Properties props = new Properties();
		FileReader reader = null;
		ret = new SystemAttributesFile(new HashMap<>(), file.lastModified(), folder);
		
		try {
			reader = new FileReader(file);
			props.load(reader);

			for(Object key : props.keySet()) {
				String value = props.getProperty(key.toString());
				ret.getSystemAttributes().put(key.toString(), value);
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

		return ret;
	}
	
	protected void manageJasperPropertiesFile(File file,ApplicationFolderImpl folder) throws EngineException {
		JasperPropertiesFile newFile = null;
		
		if (file==null) {
			folder.setJasperProperties(null);
			applicationManager.handleJasperPropertiesChange(folder.getPath());
			return;
		}

		if (file.isDirectory()) {
			throw new EngineException("File '"+file.getAbsolutePath()+"' is not a valid properties file");
		}

		Properties props = new Properties();
		FileReader reader = null;
		
		try {
			newFile = new JasperPropertiesFile(new HashMap<>(), file.lastModified(), folder);

			reader = new FileReader(file);
			props.load(reader);
			for(Object key : props.keySet()) {
				String value = props.getProperty(key.toString());
				newFile.getProperties().put(key.toString(), value);
			}

			// Evaluate ignore files in jasper.properties
			String ignoreStr = newFile.getProperties().get("ignore");
			folder.getIgnoreFiles().clear();
			if (ignoreStr!=null) {
				String ignores[] = ignoreStr.split(",");
				for(String ig : ignores) {
					folder.getIgnoreFiles().add(ig);
				}
			}
			// TODO: If jasper.properties is found, then all component files in this directory need to be unloaded
			folder.setJasperProperties(newFile);
			applicationManager.handleJasperPropertiesChange(folder.getPath());
		} catch(IOException e) {
			throw new EngineException("Couldn't read properties '"+file.getAbsolutePath()+"'",e);
		} finally {
			if (reader!=null) {
				try {
					reader.close();
				} catch(Exception e) { }
			}
		}
	}
	
	protected List<String> getCommands(File file) throws EngineException {
		List<String> ret = new ArrayList<>();
		FileReader reader = null;
		BufferedReader r = null;
		
		if (file.isDirectory()) {
			throw new EngineException("File '"+file.getPath()+"' is a directory");
		}

		try {
			reader = new FileReader(file);
			r = new BufferedReader(reader);
			String line = null;
			while((line = r.readLine())!=null) {
				line = line.trim();
				if (line.length()>0) {
					if (line.equals("build")) {
						ret.add("build");
					} else if (line.equals("buildAlways")) {
						ret.add("buildAlways");
					} else if (line.equals("clean")) {
						ret.add("clean");
					} else if (line.equals("cleanAlways")) {
						ret.add("cleanAlways");
					} else if (line.equals("deploy")) {
						ret.add("deploy");
					} else if (line.equals("deployAlways")) {
						ret.add("deployAlways");
					}
				}
			}
		} catch(IOException e) {
			throw new EngineException("Couldn't read commands.txt file",  e);
		} finally {
			if (r!=null) {
				try {
					r.close();
				} catch(Exception e) { }
			}
		}
		
		return ret;
	}

	// Creates a UserFile or a ComponentFile.  jasper.properties and systemAttributes.properties and folders are handled elsewhere.
	protected WatchedResource createFile(File file, ApplicationFolderImpl folder) throws EngineException {
		WatchedResource ret = null;
		
		if (file.isDirectory()) return null;
		if (!file.getName().endsWith(".xml")) {
			ret = new UserFile(file,folder);
			folder.getUserFiles().put(file.getName(), (UserFile)ret);
			userFiles.put(ret.getPath(), (UserFile)ret);
			return ret;
		}

		try {
			ComponentSet comps = componentFileReader.readFile(file);
			ret = new ComponentFile(comps,file,folder);
			folder.getComponentFiles().put(file.getName(), (ComponentFile)ret);
		} catch(JAXBException e) {
			ret = new UserFile(file,folder);
			folder.getUserFiles().put(file.getName(), (UserFile)ret);
			userFiles.put(file.getPath(), (UserFile)ret);
		} catch(IOException e) {
			throw new EngineException("Couldn't read file '"+file.getAbsolutePath()+"'",e);
		}
		
		return ret;
	}
	
	public void scanForNewFiles(List<ResourceChange> changes) throws EngineException {
		scanForNewFiles(applicationFolder,changes);
	}
	
	protected void scanForNewFiles(ApplicationFolderImpl folder,List<ResourceChange> changes) throws EngineException {
		File folderFile = folder.getFolderFile();
		File[] files = folderFile.listFiles();
		
		// First, check jasper properties and systemAttributes.properties file in this directory
		File jasperPropsFile = null;
		File attributesFile = null;
		for(int i=0;i<files.length;i++) {
			File f = files[i];
			if (f.getName().equals(JasperPropertiesFile.JASPER_PROPERTIES_FILE)) {
				jasperPropsFile = f;
				files[i] = null;
			}
			else if (f.getName().equals(SystemAttributesFile.SYSTEM_ATTRIBUTES_FILENAME)) {
				if (folder.getParent()==null) {
					attributesFile = f;
					files[i] = null;
				}
			}
			else if ((jasperPropsFile!=null) && (attributesFile!=null)) {
				// Stop if we've found both files
				break;
			}
		}
		
		if ((applicationManager.getSystemAttributesFile()==null) 
				&& (folder.getParent()==null) && (attributesFile!=null)) {
			SystemAttributesFile systemAttributesFile = createSystemAttributesFile(attributesFile, folder);
			applicationManager.handleSystemAttributesFileChange(systemAttributesFile);
		}
		
		if (jasperPropsFile!=null) {
			if (jasperPropsFile.isDirectory()) {
				throw new EngineException("File '"+jasperPropsFile.getAbsolutePath()+"' is not a valid properties file");
			}
			if (folder.getJasperProperties()==null) {
				manageJasperPropertiesFile(jasperPropsFile, folder);
			}
		}

		// Scan files in current directory (not folders)
		for(File f : files) {
			if (f==null) continue;
			if (folder.getIgnoreFiles().contains(f.getName())) {
				continue;
			}
			if (f.isDirectory()) {
				// skip since we are only processing files
				continue;
			}
			if (folder.getFiles().get(f.getName())!=null) {
				// This is not an added file.
				continue;
			}

			WatchedResource res = createFile(f, folder);
			changes.add(new ResourceChange(res.getPath(), null, res));
		}

		// Scan subdirectories
		for(File f : files) {
			if (f==null) continue;
			if (folder.getIgnoreFiles().contains(f.getName())) {
				continue;
			}
			if (!f.isDirectory()) {
				continue;
			}

			ApplicationFolderImpl sub = folder.getSubFolders().get(f.getName());
			if (sub==null) {
				// This is a new directory
				sub = new ApplicationFolderImpl(f,folder, applicationContext);
				//results.add(sub);
				folder.getSubFolders().put(f.getName(), sub);
			}
			scanForNewFiles(sub, changes);
		}
	}

}

