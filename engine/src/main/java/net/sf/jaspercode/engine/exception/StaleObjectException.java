package net.sf.jaspercode.engine.exception;

public class StaleObjectException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String name;
	
	public StaleObjectException(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

}

