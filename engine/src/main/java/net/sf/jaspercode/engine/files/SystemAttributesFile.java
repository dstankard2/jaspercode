package net.sf.jaspercode.engine.files;

import java.util.Map;

public class SystemAttributesFile implements WatchedResource {

	public static final String SYSTEM_ATTRIBUTES_FILENAME = "systemAttributes.properties";

	private Map<String,String> systemAttributes = null;
	private long lastModified = 0L;
	private ApplicationFolderImpl folder = null;

	public SystemAttributesFile(Map<String, String> systemAttributes, long lastModified, ApplicationFolderImpl folder) {
		super();
		this.systemAttributes = systemAttributes;
		this.lastModified = lastModified;
		this.folder = folder;
	}

	@Override
	public String getName() {
		return SYSTEM_ATTRIBUTES_FILENAME;
	}

	@Override
	public String getPath() {
		return folder.getPath()+SYSTEM_ATTRIBUTES_FILENAME;
	}

	@Override
	public long getLastModified() {
		return lastModified;
	}

	public Map<String, String> getSystemAttributes() {
		return systemAttributes;
	}

	@Override
	public ApplicationFolderImpl getFolder() {
		return folder;
	}

}
