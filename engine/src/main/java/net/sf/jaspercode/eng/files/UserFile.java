package net.sf.jaspercode.eng.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.sf.jaspercode.api.resources.ApplicationFile;

/**
 * Implements watched resource because it is watched by the engine.
 * Implements ApplicationFile for the sake of client API.
 * @author DCS
 */
public class UserFile implements WatchedResource,ApplicationFile {
	private File file = null;
	private ApplicationFolderImpl folder = null;
	private long lastModified = 0;

	public UserFile(File file,ApplicationFolderImpl folder) {
		this.file = file;
		this.folder = folder;
		this.lastModified = file.lastModified();
	}

	@Override
	public ApplicationFolderImpl getFolder() {
		return folder;
	}

	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public String getPath() {
		return folder.getPath()+getName();
	}

	@Override
	public long getLastModified() {
		return lastModified;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		FileInputStream fin = new FileInputStream(file);
		return fin;
	}


}

