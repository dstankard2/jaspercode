package net.sf.jaspercode.eng.exception;

public class EngineException extends Exception {
	private static final long serialVersionUID = 1L;

	public EngineException() {
		super();
	}
	
	public EngineException(String msg) {
		super(msg);
	}
	
	public EngineException(String msg,Throwable cause) {
		super(msg,cause);
	}
	
}
