package net.sf.jaspercode.engine.exception;

public class StaleVariableTypeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String lang;
	private String name;
	
	public StaleVariableTypeException(String lang, String name) {
		this.lang = lang;
		this.name = name;
	}
	
	public String getLang() {
		return lang;
	}

	public String getName() {
		return name;
	}

}

