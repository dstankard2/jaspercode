package net.sf.jaspercode.engine.exception;

public class RequiredConfigurationException extends PreprocessingException {
	private String name = null;
	
	public RequiredConfigurationException(String name) {
		super(null);
		this.name = name;
	}
	
	@Override
	public String getMessage() {
		return String.format("Missing required configuration '%s'", name);
	}

}

