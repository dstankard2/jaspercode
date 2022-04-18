package net.sf.jaspercode.engine.application;

import java.io.File;
import java.util.List;
import java.util.Map;

import net.sf.jaspercode.engine.ComponentFileReader;
import net.sf.jaspercode.engine.EngineInitException;
import net.sf.jaspercode.engine.JasperResources;
import net.sf.jaspercode.engine.files.ApplicationFolderImpl;
import net.sf.jaspercode.engine.files.SystemAttributesFile;
import net.sf.jaspercode.engine.processing.FileChange;

public class ResourceManager {
	
	File directory;
	//ComponentFileReader componentFileReader;
	ApplicationFolderImpl folder = null;
	SystemAttributesFile attsFile = null;

	public ResourceManager(File applicationFolder, JasperResources jasperResources) throws EngineInitException {
		this.directory = applicationFolder;
		ComponentFileReader componentFileReader = new ComponentFileReader(jasperResources.getXmlConfigClasses());
		//try {
			this.folder = new ApplicationFolderImpl(applicationFolder, null, componentFileReader);
		//} catch(IOException e) {
		//	throw new EngineInitException("Couldn't read application folder", e);
		//}
	}

	public Map<String,String> getSystemAttributes() {
		return folder.getGlobalSystemAttributes();
/*
		if (attsFile==null) {
			return new HashMap<>();
		} else {
			return attsFile.getSystemAttributes();
		}
		*/
	}

	public List<FileChange> getFileChanges() {
		return folder.findChanges(true);
	}
	
	public void close() {
		folder.remove(false);
	}

	/*
	// The contents of this folder should be removed, and removed files added to changes
	private void resetFolder(ApplicationFolderImpl folder, List<FileChange> changes) {
		
		folder.getSubFolders().entrySet().forEach(e -> {
			resetFolder(e.getValue(), changes);
		});
		folder.getSubFolders().clear();
		
		folder.getUserFiles().entrySet().forEach(e -> {
			changes.add(new RemovedFile(e.getValue()));
		});
		folder.getUserFiles().clear();

		folder.getComponentFiles().entrySet().forEach(e -> {
			changes.add(new RemovedFile(e.getValue()));
		});
		folder.getComponentFiles().clear();
		folder.setJasperProperties(null);
	}

	private Map<String,String> readProperties(File f) {
		Properties props = new Properties();
		Map<String,String> values = new HashMap<>();

		try {
			try (FileInputStream fin = new FileInputStream(f)) {
				props.load(fin);
				props.entrySet().forEach(e -> {
					values.put(
							e.getKey() != null ? e.getKey().toString() : "", 
							e.getValue() != null ? e.getValue().toString() : "");
				});
			}
		} catch(FileNotFoundException e) {
			// logically can't happen
		} catch(IOException e) {
			// TODO: ???
		}

		return values;
	}
	*/

	/*
	private void readSystemAttributesFile(File attrFile, ApplicationFolderImpl rootFolder) {
		Map<String,String> values = readProperties(attrFile);
		attsFile = new SystemAttributesFile(values, attrFile.lastModified(), rootFolder);
	}
	
	private JasperPropertiesFile readJasperPropertiesFile(File configFile, ApplicationFolderImpl folder) {
		JasperPropertiesFile ret = null;
		Map<String,String> values = readProperties(configFile);
		
		ret = new JasperPropertiesFile(values, configFile.lastModified(), folder);
		
		return ret;
	}
	*/

	/*
	private File findFile(String name, File dir) {
		return Arrays.asList(dir.listFiles()).stream().filter(f -> f.getName().equals(name)).findFirst().orElse(null);
	}

	private void checkFolder(File dir, ApplicationFolderImpl folder, List<FileChange> changes) {

		// Check systemAttributes.properties in the root folder
		if (folder.getParent()==null) {

			File attrFile = Arrays.asList(dir.listFiles()).stream().filter(f -> f.getName().equals(SystemAttributesFile.SYSTEM_ATTRIBUTES_FILENAME)).findFirst().orElse(null);
			if (attrFile!=null) {
				// File currently exists from previous scan
				if (attsFile==null) {
					resetFolder(folder, changes);
					readSystemAttributesFile(attrFile, folder);
				} else {
					if (attsFile.getLastModified() < attrFile.lastModified()) {
						// FIle has been updated
						resetFolder(folder, changes);
						readSystemAttributesFile(attrFile, folder);
					}
				}
			} else {
				// File doesn't exist from previous scan
				if (attsFile==null) {
					// no-op - it's still not there
				} else {
					resetFolder(folder, changes);
					attsFile = null;
				}
			}
		}
		
		File configFile = Arrays.asList(dir.listFiles()).stream().filter(f -> f.getName().equals(JasperPropertiesFile.JASPER_PROPERTIES_FILE)).findFirst().orElse(null);
		if (configFile!=null) {
			// File is found
			if (folder.getJasperPropertiesFile() == null) {
				// jasper.properties was added
				resetFolder(folder, changes);
				folder.setJasperProperties(readJasperPropertiesFile(configFile, folder));
			} else {
				if (folder.getJasperPropertiesFile().getLastModified() < configFile.lastModified()) {
					resetFolder(folder, changes);
					folder.setJasperProperties(readJasperPropertiesFile(configFile, folder));
				}
			}
		} else {
			if (folder.getJasperPropertiesFile() == null) {
				// no-op - it's still not there
			} else {
				resetFolder(folder, changes);
				folder.setJasperProperties(null);
			}
		}

		// Check for files and folders that have been removed.
		List<String> toRemove = new ArrayList<>();
		folder.getSubFolders().entrySet().forEach(e -> {
			String name = e.getKey();
			File f = findFile(name, dir);
			if ((f==null) || (f.isDirectory()==false)) {
				toRemove.add(name);
			}
			else if (folder.isIgnore(name)) {
				toRemove.add(name);
			}
		});
		toRemove.forEach(name -> {
			resetFolder(folder.getSubFolders().get(name), changes);
			folder.getSubFolders().remove(name);
		});
		toRemove.clear();
		folder.getComponentFiles().entrySet().forEach(e -> {
			String name = e.getKey();
			File f = findFile(name, dir);
			if ((f==null) || f.isDirectory()) {
				toRemove.add(name);
			} else if (folder.isIgnore(name)) {
				toRemove.add(name);
			}
		});
		toRemove.forEach(name -> {
			changes.add(new RemovedFile(folder.getComponentFiles().get(name)));
			folder.getComponentFiles().remove(name);
		});
		toRemove.clear();

		folder.getUserFiles().entrySet().forEach(e -> {
			String name = e.getKey();
			File f = findFile(name, dir);
			if ((f==null) || (f.isDirectory())) {
				toRemove.add(name);
			} else if (folder.isIgnore(name)) {
				toRemove.add(name);
			}
		});
		toRemove.forEach(name -> {
			changes.add(new RemovedFile(folder.getUserFiles().get(name)));
			folder.getUserFiles().remove(name);
		});

		// Handle pre-existing and added subfolders
		Arrays.asList(dir.listFiles()).forEach(f -> {
			long start = System.currentTimeMillis();
			if ((f.isDirectory()) && (!folder.isIgnore(f.getName()))) {
				ApplicationFolderImpl subFolder = folder.getSubFolders().get(f.getName());
				if (subFolder==null) {
					subFolder = new ApplicationFolderImpl(f, folder);
					folder.getSubFolders().put(f.getName(), subFolder);
				}
				checkFolder(f, subFolder, changes);
			}
			long end = System.currentTimeMillis();
			long dur = end - start;
		});
		
		// Read files in the directory.  Check for files that have been changed.  Remove them.
		Arrays.asList(dir.listFiles()).forEach(f -> {
			if (f.isDirectory()) return;
			long lastModified = f.lastModified();
			String name = f.getName();
			
			if (name.equals(SystemAttributesFile.SYSTEM_ATTRIBUTES_FILENAME)) return;
			if (name.equals(JasperPropertiesFile.JASPER_PROPERTIES_FILE)) return;

			if (folder.getComponentFiles().get(name)!=null) {
				ComponentFile cf = folder.getComponentFiles().get(name);
				if (cf.getLastModified() < lastModified) {
					changes.add(new RemovedFile(cf));
					folder.getComponentFiles().remove(name);
				}
			}
			if (folder.getUserFiles().get(name)!=null) {
				UserFile uf = folder.getUserFiles().get(name);
				if (uf.getLastModified() < lastModified) {
					changes.add(new RemovedFile(uf));
					folder.getUserFiles().remove(name);
				}
			}
		});
		
		// Read files in the directory.  Check for files that have been added (this includes changed).  Add them.
		// Check them against ignore files in this folder
		Arrays.asList(dir.listFiles()).forEach(f -> {
			
			// First check ignore list.
			if (folder.isIgnore(f.getName())) return;
			
			if (f.isDirectory()) {
				ApplicationFolderImpl subFolder = folder.getSubFolders().get(f.getName());
				if (subFolder==null) {
					subFolder = new ApplicationFolderImpl(f, folder);
					folder.getSubFolders().put(f.getName(), subFolder);
				}
				checkFolder(f, subFolder, changes);
			} else {
				// A file, not a folder
				if (f.getName().equals(SystemAttributesFile.SYSTEM_ATTRIBUTES_FILENAME)) {
					return;
				} else if (f.getName().equals(JasperPropertiesFile.JASPER_PROPERTIES_FILE)) {
					return;
				}
				
				if (folder.getComponentFiles().get(f.getName())!=null) {
					return;
				}
				if (folder.getUserFiles().get(f.getName())!=null) {
					return;
				}
				ComponentFile compFile = null;
				if (f.getName().endsWith(".xml")) {
					ComponentSet set = componentFileReader.readFile(f);
					if (set!=null) {
						compFile = new ComponentFile(set, f, folder);
						folder.getComponentFiles().put(f.getName(), compFile);
						changes.add(new AddedFile(compFile));
						return;
					}
				}
				UserFile uf = new UserFile(f, folder);
				folder.getUserFiles().put(f.getName(), uf);
				changes.add(new AddedFile(uf));
			}
		});
	}
*/
	
}

