package net.sf.jaspercode.engine.definitions;

import java.io.File;

import net.sf.jaspercode.api.config.ComponentSet;

public class ComponentFile implements WatchedResource {

	private long lastModified = 0;
	private File file = null;
	private ComponentSet componentSet = null;
	private ApplicationFolderImpl folder = null;

	public ComponentFile(ComponentSet componentSet,File file,ApplicationFolderImpl folder) {
		this.file = file;
		this.componentSet = componentSet;
		this.folder = folder;
		this.lastModified = file.lastModified();
	}

	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public String getPath() {
		return folder.getPath()+file.getName();
	}

	@Override
	public long getLastModified() {
		return lastModified;
	}

	public ComponentSet getComponentSet() {
		return componentSet;
	}

	public void setComponentSet(ComponentSet componentSet) {
		this.componentSet = componentSet;
	}

	@Override
	public ApplicationFolderImpl getFolder() {
		return folder;
	}

}

