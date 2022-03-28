package net.sf.jaspercode.engine.exception;


public class StaleSourceFileException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String path;
	
	public StaleSourceFileException(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

}

