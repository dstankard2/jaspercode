package net.sf.jaspercode.engine.definitions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.config.BuildComponent;
import net.sf.jaspercode.api.resources.ApplicationFolder;
import net.sf.jaspercode.engine.processing.BuildComponentItem;

public class ApplicationFolderImpl implements WatchedResource,ApplicationFolder {

	// lastModified is used to determine if the folder needs to be processed again.  i.e. if jasper.properties is changed
	private long lastModified = Long.MIN_VALUE;
	
	private List<String> ignoreFiles = new ArrayList<>();
	
	private String name;
	
	private String logLevel = null;
	
	private File folder = null;

	//private ApplicationContext applicationContext = null;
	
	private ApplicationFolderImpl parent = null;
	
	private BuildComponentItem buildComponent = null;
	
	private HashMap<String,ApplicationFolderImpl> subFolders = new HashMap<>();
	private Map<String,ComponentFile> componentFiles = new HashMap<>();
	private Map<String,UserFile> userFiles = new HashMap<>();
	
	// configuration in jasper.properties
	private JasperPropertiesFile jasperProperties = null;

	public ApplicationFolderImpl(File file,ApplicationFolderImpl parent) {
		this.folder = file;
		this.name = file.getName();
		this.parent = parent;
		this.lastModified = file.lastModified();
	}

	// TODO: Should this unload component files in the folder?
	public void setJasperProperties(JasperPropertiesFile jasperProperties) {
		this.jasperProperties = jasperProperties;
		// This folder needs to be processed again.  Mark it as modified
		lastModified = System.currentTimeMillis() - 1;
		String logLevel = this.getJasperProperties().get("logLevel");
		if (logLevel==null) this.setLogLevel("WARN");
		else if (logLevel.equalsIgnoreCase("ERROR")) this.setLogLevel("ERROR");
		else if (logLevel.equalsIgnoreCase("INFO")) this.setLogLevel("INFO");
		else if (logLevel.equalsIgnoreCase("DEBUG")) this.setLogLevel("DEBUG");
		else if (logLevel.equalsIgnoreCase("WARN")) this.setLogLevel("WARN");
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

	public Map<String,String> getJasperProperties() {
		if (jasperProperties==null) return null;
		return jasperProperties.getProperties();
	}

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

	public long getJasperPropertiesLastModified() {
		if (jasperProperties==null) return 0L;
		return jasperProperties.getLastModified();
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
				ret = new BuildComponentItem(-1, buildComp, null, this.getProperties(), null, null, null);
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
