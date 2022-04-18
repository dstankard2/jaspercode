package net.sf.jaspercode.engine.files;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.SneakyThrows;
import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.config.BuildComponent;
import net.sf.jaspercode.api.config.ComponentSet;
import net.sf.jaspercode.api.resources.ApplicationFolder;
import net.sf.jaspercode.engine.ComponentFileReader;
import net.sf.jaspercode.engine.processing.AddedFile;
import net.sf.jaspercode.engine.processing.BuildComponentItem;
import net.sf.jaspercode.engine.processing.FileChange;
import net.sf.jaspercode.engine.processing.RemovedFile;

public class ApplicationFolderImpl implements WatchedResource,ApplicationFolder {

	// lastModified is used to determine if the folder needs to be processed again.  i.e. if jasper.properties is changed
	private long lastModified = Long.MIN_VALUE;
	
	private List<String> ignoreFiles = new ArrayList<>();
	private String name;
	private String logLevel = null;
	private File folder = null;
	private ApplicationFolderImpl parent = null;
	private BuildComponentItem buildComponent = null;
	
	private HashMap<String,ApplicationFolderImpl> subFolders = new HashMap<>();
	private Map<String,ComponentFile> componentFiles = new HashMap<>();
	private Map<String,UserFile> userFiles = new HashMap<>();
	
	// File handling
	private JasperPropertiesFile jasperProperties = null;
	private WatchKey watchKey = null;
	private List<FileChange> changes = new ArrayList<>();
	private ComponentFileReader componentFileReader;
	private SystemAttributesFile attributes = null;

	// Are we already reloading the folder?
	private boolean reloading = false;
	
	public ApplicationFolderImpl(File file,ApplicationFolderImpl parent, ComponentFileReader componentFileReader) {
		this.folder = file;
		this.name = file.getName();
		this.parent = parent;
		this.lastModified = file.lastModified();
		this.componentFileReader = componentFileReader;
		initFolder();
	}

	// For the resource manager to get global system attributes
	public Map<String,String> getGlobalSystemAttributes() {
		if (attributes!=null) {
			return attributes.getSystemAttributes();
		}
		return new HashMap<>();
	}

	// Called when the folder is created, or when the folder must be reloaded (due to jasper.properties or systemAttributes.properties)
	// userFiles, componentFiles and subFolders will all be empty
	@SneakyThrows
	public void initFolder() {
		File[] files = folder.listFiles();
		List<File> fileList = new ArrayList<>(Arrays.asList(files));

		// Reload the folder
		this.reloading = true;
		this.filesAdded(fileList);
		this.reloading = false;

		// Create the file watcher on the folder
		WatchService watchService = FileSystems.getDefault().newWatchService();
		Path path = Paths.get(folder.getAbsolutePath());
		watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, 
				StandardWatchEventKinds.ENTRY_DELETE);
	}
	
	// Called by resourceManager to get current file changes
	// Also called by parent folders on children
	public List<FileChange> findChanges(boolean required) {
		List<FileChange> ret = new ArrayList<>();

		if (required) {
			List<WatchEvent<?>> events = watchKey.pollEvents();
			if (events.size()>0) {
				List<File> filesAdded = new ArrayList<>();
				List<File> filesRemoved = new ArrayList<>();
				events.forEach(event -> {
					Path p = (Path)event.context();
					File file = new File(folder, p.toString());
					//File file = p.toFile();
					if (event.kind()==null) {
					} else if (event.kind().equals(StandardWatchEventKinds.ENTRY_CREATE)) {
						filesAdded.add(file);
					} else if (event.kind().equals(StandardWatchEventKinds.ENTRY_DELETE)) {
						filesRemoved.add(file);
					} else if (event.kind().equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
						filesAdded.add(file);
						filesRemoved.add(file);
					}
				});
				removeFiles(filesRemoved, required);
				filesAdded(filesAdded);
			
			}
		}
		
		ret.addAll(changes);
		changes.clear();
		
		// Check subfolders for changes, if changes are required
		if (required) {
			this.subFolders.entrySet().forEach(e -> {
				ret.addAll(e.getValue().findChanges(true));
			});
		}
		
		return ret;
	}

	// The only time changes are not required is if we're closing the application
	// If changesRequired is true, the folder's change list will be updated with removed files.
	// if changesRequired is false, that means the resourceManager is shutting down.
	public void remove(boolean changesRequired) {
		// Remove all files
		removeAllFiles(changesRequired);
		
		// Cancel file watcher
		if (watchKey!=null) {
			watchKey.pollEvents();
			watchKey.cancel();
			watchKey = null;
		} else {
			
		}
	}

	protected void removeAllFiles(boolean changesRequired) {
		if (changesRequired) {
			componentFiles.entrySet().forEach(e -> {
				changes.add(new RemovedFile(e.getValue()));
			});
			userFiles.entrySet().forEach(e -> {
				changes.add(new RemovedFile(e.getValue()));
			});
		}
		componentFiles.clear();
		userFiles.clear();
		subFolders.entrySet().forEach(e -> {
			e.getValue().remove(changesRequired);
			changes.addAll(e.getValue().findChanges(changesRequired));
		});
		subFolders.clear();
		
		// Reset jasper.properties and systemAttributes.properties
		this.jasperProperties = null;
		this.attributes = null;
	}

	protected void removeFiles(List<File> files, boolean changesRequired) {
		files.stream().map(File::getName).forEach(fileName -> {
			if (componentFiles.get(fileName) != null) {
				if (changesRequired) {
					this.changes.add(new RemovedFile(componentFiles.get(fileName)));
				}
				componentFiles.remove(fileName);
			}
			
			if (userFiles.get(fileName) != null) {
				if (changesRequired) {
					this.changes.add(new RemovedFile(userFiles.get(fileName)));
				}
				userFiles.remove(fileName);
			}
			if (subFolders.get(fileName) != null) {
				ApplicationFolderImpl folder = subFolders.get(fileName);
				folder.remove(changesRequired);
				changes.addAll(folder.findChanges(changesRequired));
			}
		});
	}

	/*
	@SneakyThrows
	protected void addFile(File file) {
		String name = file.getName();
		if (file.isDirectory()) {
			ApplicationFolderImpl subfolder = new ApplicationFolderImpl(file, this, componentFileReader);
			this.subFolders.put(name, subfolder);
			subfolder.initFolder();
		} else if (name.equals("jasper.properties")) {
			jasperProperties = new JasperPropertiesFile(file, this);
			this.clearFolderContents();
			this.initFolder();
		} else if ((name.equals("systemAttributes.properties")) && (getParent()==null)) {
			
		} else if (name.endsWith(".xml")) {
			ComponentSet set = componentFileReader.readFile(file);
			if (set!=null) {
				ComponentFile compFile = new ComponentFile(set, file, this);
				this.changes.add(new AddedFile(compFile));
			}
		} else {
			UserFile userFile = new UserFile(file, this);
			//this.userFiles.put(name, userFile);
			this.changes.add(new AddedFile(userFile));
		}
	}
	*/

	// Always add changes since we're adding the files
	// If jasper.properties is changed, remove this folder and initialize it again and that's it.
	// If systemAttributes.properties is changed and this is the root folder, remove this folder and initialize it again and that's it
	protected void filesAdded(List<File> fileList) {
		
		// Look for jasper.properties
		File props = fileList.stream().filter(f -> f.getName().equals("jasper.properties")).findFirst().orElse(null);
		if (props!=null) {
			// If we aren't reloading, then reload and that's it
			if (!reloading) {
				this.remove(true);
				this.initFolder();
				return;
			}
			fileList.remove(props);
			this.jasperProperties = new JasperPropertiesFile(props, this);
			
		}
		// If we're in the root directory, look for systemAttributes.properties
		if (parent==null) {
			File attrs = fileList.stream().filter(f -> f.getName().equals("systemAttributes.properties")).findFirst().orElse(null);
			if (attrs!=null) {
				if (!reloading) {
					this.remove(true);
					initFolder();
					return;
				}
				fileList.remove(attrs);
				this.attributes = new SystemAttributesFile(attrs, this);
			}
		}
		
		for(File f : fileList) {
			String name = f.getName();
			
			// Check ignore files
			if (isIgnore(name)) {
				continue;
			}
			if (f.isDirectory()) {
				ApplicationFolderImpl folder = new ApplicationFolderImpl(f, this, componentFileReader);
				this.subFolders.put(name, folder);
				this.changes.addAll(folder.findChanges(true));
			} else {
				ComponentFile compFile = null;
				if (f.getName().endsWith(".xml")) {
					ComponentSet set = componentFileReader.readFile(f);
					if (set!=null) {
						compFile = new ComponentFile(set, f, this);
						this.changes.add(new AddedFile(compFile));
						componentFiles.put(f.getName(), compFile);
					}
				}
				if (compFile==null) {
					UserFile uf = new UserFile(f, this);
					changes.add(new AddedFile(uf));
					this.userFiles.put(f.getName(), uf);
				}
			}
		}
	}

	@SneakyThrows
	protected void removeFile(File file) {
		String name = file.getName();
		if (file.isDirectory()) {
			ApplicationFolderImpl folder = this.subFolders.get(name);
			if (folder!=null) {
				folder.remove(true);
				changes.addAll(folder.findChanges(true));
				subFolders.remove(name);
			}
		} else {
			if (name.equals("jasper.properties")) {
				// Remove jasper.properties, reset the folder.  Then done
				this.jasperProperties = null;
				this.remove(true);
				this.initFolder();
				return;
			}
			else if ((name.equals("systemAttributes.properties")) && (parent==null)) {
				// Remove system attributes properties file, reset the folder.
				this.attributes = null;
				this.remove(true);
				this.initFolder();
			}
			if (componentFiles.get(name)!=null) {
				changes.add(new RemovedFile(componentFiles.get(name)));
				componentFiles.remove(name);
			}
			else if (userFiles.get(name)!=null) {
				changes.add(new RemovedFile(userFiles.get(name)));
				userFiles.remove(name);
			}
		}
	}

	public boolean isIgnore(String filename) {
		if (jasperProperties==null) return false;
		return jasperProperties.isIgnore(filename);
	}

	// TODO: Calculate property map once here when jasper properties is set
	public void setJasperProperties(JasperPropertiesFile jasperProperties) {
		this.jasperProperties = jasperProperties;
		// This folder needs to be processed again.  Mark it as modified
		lastModified = System.currentTimeMillis() - 1;
		String logLevel = this.getProperties().get("logLevel");
		if (logLevel==null) this.setLogLevel("WARN");
		else if (logLevel.equalsIgnoreCase("ERROR")) this.setLogLevel("ERROR");
		else if (logLevel.equalsIgnoreCase("INFO")) this.setLogLevel("INFO");
		else if (logLevel.equalsIgnoreCase("DEBUG")) this.setLogLevel("DEBUG");
		else if (logLevel.equalsIgnoreCase("WARN")) this.setLogLevel("WARN");
		
		if (jasperProperties!=null) {
			String ig = jasperProperties.getProperties().get("ignore");
			if (ig!=null) {
				this.ignoreFiles.addAll(Arrays.asList(ig.split(",")));
			} else {
				this.ignoreFiles.clear();
			}
		} else {
			this.ignoreFiles.clear();
		}
	}

	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}

	public String getLogLevel() {
		return logLevel;
	}

	public void setBuildComponentItem(BuildComponentItem e) {
		this.buildComponent = e;
	}
	
	public List<String> getIgnoreFiles() {
		return ignoreFiles;
	}
	public void setIgnoreFiles(List<String> ignoreFiles) {
		this.ignoreFiles = ignoreFiles;
	}

	public Map<String,WatchedResource> getFiles() {
		Map<String,WatchedResource> ret = new HashMap<>();
		
		for(String key : subFolders.keySet()) {
			ret.put(key, subFolders.get(key));
		}
		for(String key : componentFiles.keySet()) {
			ret.put(key, componentFiles.get(key));
		}
		for(String key : userFiles.keySet()) {
			ret.put(key, userFiles.get(key));
		}
		if (jasperProperties!=null) {
			ret.put("jasper.properties", jasperProperties);
		}
		
		return ret;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPath() {
		if (parent == null) {
			return "/";
		}
		return parent.getPath() + getName() + "/";
	}

	@Override
	public long getLastModified() {
		return lastModified;
	}

	@Override
	public WatchedResource getResource(String path) {
		if (path==null) return null;
		path = path.trim();
		if (path.length()==0) {
			return this;
		}
		if (path.equals(".")) {
			return this;
		}
		if (path.startsWith("./")) {
			return getResource(path.substring(2));
		}
		else if (path.startsWith("/")) {
			return getRootFolder().getResource(path.substring(1));
		}
		else if (path.startsWith("../")) {
			return getParent().getResource(path.substring(3));
		}
		else if (path.indexOf('/')>0) {
			int i = path.indexOf('/');
			String sub = path.substring(0, i);
			if (subFolders.get(sub)!=null) {
				return subFolders.get(sub).getResource(path.substring(i+1));
			} else {
				return null;
			}
		} else {
			// path is a name of a folder or UserFileResource
			WatchedResource ret = subFolders.get(path);
			if (ret==null) {
				ret = userFiles.get(path);
				if (ret==null) {
					ret = this.componentFiles.get(path);
				}
			}
			return ret;
		}
	}
	
	private ApplicationFolderImpl getRootFolder() {
		ApplicationFolderImpl current = this;
		ApplicationFolderImpl parent = current.getParent();
		
		while(parent!=null) {
			ApplicationFolderImpl temp = current;
			current = parent;
			parent = temp.getParent();
		}
		
		return current;
	}

	public File getFolderFile() {
		return folder;
	}

	public void setFolder(File folder) {
		this.folder = folder;
	}

	public Map<String,String> getProperties() {
		Map<String,String> ret = null;
		
		if (parent!=null) {
			ret = parent.getProperties();
		} else {
			ret = new HashMap<String,String>();
		}
		if (jasperProperties!=null) {
			for(String key : jasperProperties.getProperties().keySet()) {
				String value = jasperProperties.getProperties().get(key);
				ret.put(key, value);
			}
		}
		
		return ret;
	}
	
	public Map<String, UserFile> getUserFiles() {
		return userFiles;
	}

	public void setUserFiles(Map<String, UserFile> userFiles) {
		this.userFiles = userFiles;
	}

	public void setLastModified(Long lastModified) {
		this.lastModified = lastModified;
	}

	/*
	public Map<String,String> getJasperProperties() {
		if (jasperProperties==null) return null;
		return jasperProperties.getProperties();
	}
	*/

	public Map<String, ComponentFile> getComponentFiles() {
		return componentFiles;
	}

	public ApplicationFolderImpl getParent() {
		return parent;
	}

	public void setParent(ApplicationFolderImpl parent) {
		this.parent = parent;
	}

	public void setComponentFiles(Map<String, ComponentFile> componentFiles) {
		this.componentFiles = componentFiles;
	}

	public HashMap<String, ApplicationFolderImpl> getSubFolders() {
		return subFolders;
	}

	public void setSubFolders(HashMap<String, ApplicationFolderImpl> subFolders) {
		this.subFolders = subFolders;
	}

	public JasperPropertiesFile getJasperPropertiesFile() {
		return jasperProperties;
	}

	public BuildComponentItem getBuildComponent() {
		return buildComponent;
	}
	
	public BuildComponentItem getCurrentBuildComponent() {
		BuildComponentItem ret = null;
		
		if (buildComponent!=null) {
			ret = buildComponent;
		} else {
			if (parent!=null) {
				ret = parent.getCurrentBuildComponent();
			} else {
				BuildComponent buildComp = new DefaultBuildComponent();
				ret = new BuildComponentItem(buildComp, this);
				ret.init();
			}
		}
		
		return ret;
	}
	
	public BuildContext getBuildContext() {
		return getCurrentBuildComponent().getBuildContext();
	}

	@Override
	public List<String> getContentNames() {
		ArrayList<String> ret = new ArrayList<>();

		for(String k : subFolders.keySet()) {
			ret.add(k);
		}
		for(String k : userFiles.keySet()) {
			ret.add(k);
		}

		return ret;
	}

	@Override
	public ApplicationFolderImpl getFolder() {
		return this.getParent();
	}

}

