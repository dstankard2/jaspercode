package net.sf.jaspercode.engine.exception;

public class EngineException extends Exception {

	public EngineException() {
		super();
	}

	public EngineException(String msg) {
		super(msg);
	}
	
	public EngineException(String msg,Throwable cause) {
		super(msg, cause);
	}
	
}
