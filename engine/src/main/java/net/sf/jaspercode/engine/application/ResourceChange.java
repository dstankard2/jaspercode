package net.sf.jaspercode.engine.application;

import net.sf.jaspercode.engine.definitions.ComponentFile;
import net.sf.jaspercode.engine.definitions.JasperPropertiesFile;
import net.sf.jaspercode.engine.definitions.UserFile;
import net.sf.jaspercode.engine.definitions.WatchedResource;

public class ResourceChange {

	private String path = null;
	private WatchedResource oldFile = null;
	private WatchedResource newFile = null;



	// Determine what the file(s) are
	public boolean wasUserFile() {
		return oldFile instanceof UserFile;
	}
	public boolean isUserFile() {
		return newFile instanceof UserFile;
	}
	public boolean wasComponentFile() {
		return oldFile instanceof ComponentFile;
	}
	public boolean isComponentFile() {
		return newFile instanceof ComponentFile;
	}
	public boolean isJasperPropertiesFile() {
		return newFile instanceof JasperPropertiesFile;
	}



	public ResourceChange(String path,WatchedResource oldFile, WatchedResource newFile) {
		super();
		this.path = path;
		this.oldFile = oldFile;
		this.newFile = newFile;
	}

	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public WatchedResource getOldFile() {
		return oldFile;
	}

	public void setOldFile(WatchedResource oldFile) {
		this.oldFile = oldFile;
	}

	public WatchedResource getNewFile() {
		return newFile;
	}

	public void setNewFile(WatchedResource newFile) {
		this.newFile = newFile;
	}

}

