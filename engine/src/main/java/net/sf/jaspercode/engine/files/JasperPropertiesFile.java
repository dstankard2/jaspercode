package net.sf.jaspercode.engine.files;

import java.util.Map;

public class JasperPropertiesFile implements WatchedResource {

	public static final String JASPER_PROPERTIES_FILE = "jasper.properties";
	
	private Map<String,String> properties = null;
	private long lastModified = 0L;
	private ApplicationFolderImpl folder = null;
	
	public JasperPropertiesFile(Map<String,String> properties,long lastModified,ApplicationFolderImpl folder) {
		this.properties = properties;
		this.lastModified = lastModified;
		this.folder = folder;
	}

	@Override
	public String getName() {
		return JASPER_PROPERTIES_FILE;
	}

	@Override
	public String getPath() {
		return getFolder().getPath()+JASPER_PROPERTIES_FILE;
	}

	@Override
	public long getLastModified() {
		return lastModified;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	@Override
	public ApplicationFolderImpl getFolder() {
		return folder;
	}

}
