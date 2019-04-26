package net.sf.jaspercode.api.snapshot;

public class ResourceWatcherSnapshot {

	private String name = null;
	private String path = null;
	private long lastRun = 0;
	private int matchedFiles = 0;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public long getLastRun() {
		return lastRun;
	}
	public void setLastRun(long lastRun) {
		this.lastRun = lastRun;
	}
	public int getMatchedFiles() {
		return matchedFiles;
	}
	public void setMatchedFiles(int matchedFiles) {
		this.matchedFiles = matchedFiles;
	}
	
}
