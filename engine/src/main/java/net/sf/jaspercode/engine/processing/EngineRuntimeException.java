package net.sf.jaspercode.engine.processing;

public class EngineRuntimeException extends Exception {
	private static final long serialVersionUID = 1L;

	public EngineRuntimeException() {
		super();
	}
	
	public EngineRuntimeException(String msg) {
		super(msg);
	}
	
	public EngineRuntimeException(String msg,Throwable cause) {
		super(msg,cause);
	}
	
}
