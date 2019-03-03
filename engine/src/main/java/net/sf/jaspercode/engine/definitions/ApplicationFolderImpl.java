package net.sf.jaspercode.engine.definitions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.config.ComponentSet;
import net.sf.jaspercode.api.resources.ApplicationFolder;
import net.sf.jaspercode.api.resources.ApplicationResource;
import net.sf.jaspercode.engine.impl.ApplicationContextImpl;
import net.sf.jaspercode.engine.impl.DefaultBuildComponent;
import net.sf.jaspercode.engine.processing.BuildComponentEntry;
import net.sf.jaspercode.engine.processing.ComponentContainer;

public class ApplicationFolderImpl implements WatchedResource,ApplicationFolder {

	public ApplicationFolderImpl(File file,ApplicationFolderImpl parent,ApplicationContextImpl applicationContext) {
		this.folder = file;
		this.name = file.getName();
		this.parent = parent;
		this.applicationContext = applicationContext;
	}
	
	private long lastModified = Long.MIN_VALUE;
	
	private List<String> ignoreFiles = new ArrayList<>();
	
	private String name;
	
	private String logLevel = null;
	
	private File folder = null;
	
	private ApplicationFolderImpl parent = null;
	private ApplicationContextImpl applicationContext = null;
	
	private BuildComponentEntry buildComponent = null;
	
	private HashMap<String,ApplicationFolderImpl> subFolders = new HashMap<>();
	private Map<String,ComponentFile> componentFiles = new HashMap<>();
	private Map<String,UserFile> userFiles = new HashMap<>();

	// system attributes in systemAttributes.properties
	private long systemAttributesModified = Long.MIN_VALUE;
	
	// configuration in jasper.properties
	private Map<String,String> jasperProperties = new HashMap<String,String>();
	private long jasperPropertiesLastModified = Long.MIN_VALUE;

	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}

	public String getLogLevel() {
		return logLevel;
	}

	public void setBuildComponentEntry(BuildComponentEntry e) {
		this.buildComponent = e;
	}
	
	public List<String> getIgnoreFiles() {
		return ignoreFiles;
	}
	public void setIgnoreFiles(List<String> ignoreFiles) {
		this.ignoreFiles = ignoreFiles;
	}
	public long getSystemAttributesModified() {
		return systemAttributesModified;
	}
	public void setSystemAttributesModified(long l) {
		this.systemAttributesModified = l;
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
	public ApplicationResource getResource(String path) {
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
		if (path.startsWith("/")) {
			return getRootFolder().getResource(path.substring(1));
		}
		else if (path.startsWith("../")) {
			return getParent().getResource(path.substring(3));
		} else if (path.startsWith("./")) {
			return getResource(path.substring(2));
		} else if (path.indexOf('/')>0) {
			int i = path.indexOf('/');
			String sub = path.substring(0, i);
			if (subFolders.get(sub)!=null) {
				return subFolders.get(sub).getResource(path.substring(i+1));
			} else {
				return null;
			}
		} else {
			// path is a name of a folder or UserFileResource
			ApplicationResource ret = subFolders.get(path);
			if (ret==null) {
				ret = userFiles.get(path);
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
		for(String key : jasperProperties.keySet()) {
			String value = jasperProperties.get(key);
			ret.put(key, value);
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
		return jasperProperties;
	}

	public void setJasperProperties(Map<String,String> jasperProperties) {
		this.jasperProperties = jasperProperties;
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
		return jasperPropertiesLastModified;
	}

	public void setJasperPropertiesLastModified(long jasperPropertiesLastModified) {
		this.jasperPropertiesLastModified = jasperPropertiesLastModified;
	}
	
	public BuildComponentEntry getBuildComponent() {
		return buildComponent;
	}
	
	public BuildComponentEntry getCurrentBuildComponent(ComponentContainer mgr) {
		BuildComponentEntry ret = null;
		
		if (buildComponent!=null) {
			ret = buildComponent;
		} else {
			if (parent!=null) {
				ret = parent.getCurrentBuildComponent(mgr);
			} else {
				ComponentFile f = new ComponentFile(new ComponentSet(),null,this);
				ret = new BuildComponentEntry(f, new DefaultBuildComponent(), mgr, null, applicationContext);
				try {
					ret.initialize();
				} catch(Exception e) {
				}
			}
		}
		
		return ret;
	}
	
	public BuildContext getBuildContext(ComponentContainer mgr) {
		return getCurrentBuildComponent(mgr).getBuildContext();
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

}
