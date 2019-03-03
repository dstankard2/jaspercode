package net.sf.jaspercode.engine.definitions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import net.sf.jaspercode.api.resources.ApplicationFile;
import net.sf.jaspercode.api.resources.ApplicationFolder;

/**
 * 
 * @author DCS
 */
public class UserFile implements WatchedResource,ApplicationFile {
	private File file = null;
	private ApplicationFolderImpl folder = null;
	private Date lastModified = null;

	public UserFile(File file,ApplicationFolderImpl folder) {
		this.file = file;
		lastModified = new Date();
		this.folder = folder;
	}

	@Override
	public ApplicationFolder getFolder() {
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
		return lastModified.getTime();
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		FileInputStream fin = new FileInputStream(file);
		return fin;
	}

}
