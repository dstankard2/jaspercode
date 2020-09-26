package net.sf.jaspercode.engine.processing;

public class FileProcessorRecord {

	private int id;
	
	private int originatorId;
	
	private String path;
	
	public FileProcessorRecord(int id, int originatorId, String path) {
		super();
		this.id = id;
		this.originatorId = originatorId;
		this.path = path;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getOriginatorId() {
		return originatorId;
	}

	public void setOriginatorId(int originatorId) {
		this.originatorId = originatorId;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

}

