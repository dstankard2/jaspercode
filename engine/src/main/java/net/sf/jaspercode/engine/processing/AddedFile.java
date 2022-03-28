package net.sf.jaspercode.engine.processing;

import net.sf.jaspercode.engine.files.WatchedResource;

public class AddedFile implements FileChange {

	private WatchedResource file;
	
	public AddedFile(WatchedResource file) {
		this.file = file;
	}
	
	public WatchedResource getFile() {
		return file;
	}

}
