package net.sf.jaspercode.engine.processing;

import net.sf.jaspercode.api.JasperException;

public class DefinitionReadException extends JasperException {
	private static final long serialVersionUID = 1L;

	public DefinitionReadException() {
		super();
	}

	public DefinitionReadException(String resource,String msg) {
		super("Error processing '"+resource+"': "+msg);
	}

	public DefinitionReadException(String resource,String msg,Throwable cause) {
		super("Error processing '"+resource+"': "+msg,cause);
	}

	public DefinitionReadException(String msg) {
		super(msg);
	}
	
	public DefinitionReadException(String msg,Throwable cause) {
		super(msg,cause);
	}
	
}
