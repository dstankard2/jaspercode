package net.sf.jaspercode.api.snapshot;

import java.util.ArrayList;
import java.util.List;

public class SourceFileSnapshot {

	private String path = null;
	private List<Integer> originators = new ArrayList<>();
	private String content = null;


	public SourceFileSnapshot(String path, List<Integer> originators, String content) {
		super();
		this.path = path;
		this.originators = originators;
		this.content = content;
	}

	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public List<Integer> getOriginators() {
		return originators;
	}
	public void setOriginators(List<Integer> originators) {
		this.originators = originators;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
}
