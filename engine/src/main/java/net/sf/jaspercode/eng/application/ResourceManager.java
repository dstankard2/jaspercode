package net.sf.jaspercode.eng.application;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;

import net.sf.jaspercode.api.config.ComponentSet;
import net.sf.jaspercode.eng.ComponentFileReader;
import net.sf.jaspercode.eng.EngineInitException;
import net.sf.jaspercode.eng.JasperResources;
import net.sf.jaspercode.eng.exception.EngineException;
import net.sf.jaspercode.eng.files.ApplicationFolderImpl;
import net.sf.jaspercode.eng.files.ComponentFile;
import net.sf.jaspercode.eng.files.JasperPropertiesFile;
import net.sf.jaspercode.eng.files.SystemAttributesFile;
import net.sf.jaspercode.eng.files.UserFile;
import net.sf.jaspercode.eng.files.WatchedResource;

//Manages reading of files from the input directory
public class ResourceManager {
	private ApplicationFolderImpl applicationFolder;
	//private JasperResources jasperResources = null;
	private ResourceContext ctx = null;
	private ComponentFileReader componentFileReader = null;

	public ResourceManager(File rootFolder, JasperResources jasperResources, ResourceContext ctx) throws EngineInitException {
		this.applicationFolder = new ApplicationFolderImpl(rootFolder, null);
		//this.jasperResources = jasperResources;
		this.ctx = ctx;
		this.componentFileReader = new ComponentFileReader(jasperResources.getXmlConfigClasses());
	}
	
	public void scanForConfigChanges(SystemAttributesFile attributesFile) throws EngineException {
		scanForConfigChanges(applicationFolder, attributesFile);
	}
	
	private void scanForConfigChanges(ApplicationFolderImpl folder, SystemAttributesFile attributesFile) throws EngineException {
		File folderFile = folder.getFolderFile();
		File[] contents = folderFile.listFiles();
		
		boolean isRoot = folder == applicationFolder;
		
		// Handle jasper properties file
		JasperPropertiesFile props = folder.getJasperPropertiesFile();
		File propFile = getFile(contents, "jasper.properties");
		if ((propFile==null) && (props!=null)) {
			folder.setJasperProperties(null);
			folder.setIgnoreFiles(new ArrayList<>());
			folder.markForReadAgain();
		} else if ((propFile!=null) && (props==null)) {
			folder.setJasperProperties(readJasperProperties(propFile, folder));
			folder.markForReadAgain();
		} else if ((props==null) && (propFile==null)) {
			// no-op
		} else if (props.getLastModified() < propFile.lastModified()) {
			folder.setJasperProperties(readJasperProperties(propFile, folder));
			folder.markForReadAgain();
		}

		// Check system attributes file
		File attrFile = getFile(contents, "systemAttributes.properties");
		if (isRoot) {
			if ((attrFile==null) && (attributesFile==null)) {
				// no-op
			} else if ((attrFile==null) && (attributesFile!=null)) {
				ctx.updateSystemAttributesFile(null);
				folder.markForReadAgain();
			} else if ((attrFile!=null) && (attributesFile==null)) {
				SystemAttributesFile f = createSystemAttributesFile(attrFile, folder);
				ctx.updateSystemAttributesFile(f);
				folder.markForReadAgain();
			} else if ((attributesFile.getLastModified() < attrFile.lastModified())) {
				SystemAttributesFile f = createSystemAttributesFile(attrFile, folder);
				ctx.updateSystemAttributesFile(f);
				folder.markForReadAgain();
			}
		}
		
		// Look at subdirectories
		for(File f : contents) {
			if (f.isDirectory()) {
				String name = f.getName();
				ApplicationFolderImpl sub = folder.getSubFolders().get(name);
				if (sub==null) {
					sub = new ApplicationFolderImpl(f, folder);
					folder.getSubFolders().put(name, sub);
				}
				scanForConfigChanges(sub, attributesFile);
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
	
	protected JasperPropertiesFile readJasperProperties(File file,ApplicationFolderImpl folder) throws EngineException {
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
		
		for(Entry<String,WatchedResource> entry : folder.getFiles().entrySet()) {
			String name = entry.getKey();
			WatchedResource res = entry.getValue();
			if (getFile(files, name)==null) {
				String path = res.getPath();
				
				if (res instanceof ApplicationFolderImpl) {
					ApplicationFolderImpl f = (ApplicationFolderImpl)res;
					scanForRemovedFiles(f, changes);
				}
				
				changes.add(new ResourceChange(path, res, null));
			}
		}
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
		for(Entry<String,WatchedResource> entry : folder.getFiles().entrySet()) {
			String name = entry.getKey();
			WatchedResource oldFile = entry.getValue();
			File file = getFile(files, name);
			if (file==null) continue;
			if (oldFile instanceof ApplicationFolderImpl) {
				scanForModifiedFiles((ApplicationFolderImpl)oldFile, results);
			}
			else if (oldFile.getLastModified() < file.lastModified()) {
				String path = folder.getPath()+oldFile.getName();
				WatchedResource newFile = createWatchedResource(folder, file);
				if (newFile!=null) {
					results.add(new ResourceChange(path, oldFile, newFile));
				}
			}
		}
	}
	
	protected WatchedResource createWatchedResource(ApplicationFolderImpl folder, File file) {
		WatchedResource ret = null;
		if (file.getName().equals("jasper.properties")) return null;
		else if (file.getName().contentEquals("systemAttributes.properties")) return null;

		if (!file.getName().endsWith(".xml")) {
			ret = new UserFile(file,folder);
			folder.getUserFiles().put(file.getName(), (UserFile)ret);
		} else {
			ComponentSet comps = componentFileReader.readFile(file);
			if (comps!=null) {
				ret = new ComponentFile(comps,file,folder);
				folder.getComponentFiles().put(file.getName(), (ComponentFile)ret);
			} else {
				ret = new UserFile(file,folder);
				folder.getUserFiles().put(file.getName(), (UserFile)ret);
			}
		}
		
		return ret;
	}
	
	public void scanForAddedFiles(List<ResourceChange> changes) throws EngineException {
		scanForAddedFiles(applicationFolder, changes);
	}

	private void scanForAddedFiles(ApplicationFolderImpl folder,List<ResourceChange> results) throws EngineException {
		File folderFile = folder.getFolderFile();;
		File[] files = null;
		
		if ((!folderFile.exists()) || (!folderFile.isDirectory())) {
			files = new File[0];
		} else {
			files = folderFile.listFiles();
		}
		
		// We'll handle folders after files are handled
		List<File> directories = new ArrayList<>();
		
		for(File file : files) {
			String name = file.getName();
			if (name.equals("jasper.properties")) continue;
			if (name.equals("systemAttributes.properties")) continue;
			if (folder.getIgnoreFiles().contains(name)) continue;
			if (file.isDirectory()) {
				directories.add(file);
			}
			else if (folder.getFiles().get(name)==null) {
				ComponentSet set = componentFileReader.readFile(file);
				if (set==null) {
					UserFile f = new UserFile(file, folder);
					folder.getUserFiles().put(name, f);
					results.add(new ResourceChange(f.getPath(), null, f));
				} else {
					ComponentFile f = new ComponentFile(set, file, folder);
					folder.getComponentFiles().put(name, f);
					results.add(new ResourceChange(f.getPath(), null, f));
				}
			}
		}
		for (File dir : directories) {
			String name = dir.getName();
		//directories.forEach(dir -> {
			ApplicationFolderImpl sub = folder.getSubFolders().get(name);
			if (sub==null) {
				sub = new ApplicationFolderImpl(dir, folder);
				folder.getSubFolders().put(name, sub);
			}
			scanForAddedFiles(sub, results);
		}
	}
	
	protected File getFile(File[] files, String name) {
		for(File f : files) {
			if (f.getName().equals(name)) {
				return f;
			}
		}
		return null;
	}

}
