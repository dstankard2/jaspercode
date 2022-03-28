package net.sf.jaspercode.engine.processing;

import net.sf.jaspercode.engine.files.WatchedResource;

public class RemovedFile implements FileChange {
	private WatchedResource file;
	
	public RemovedFile(WatchedResource file) {
		this.file = file;
	}
	
	public WatchedResource getFile() {
		return file;
	}


}
