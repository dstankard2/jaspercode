package net.sf.jaspercode.engine.exception;

public class PreprocessingException extends EngineException {
	private static final long serialVersionUID = 1L;

	public PreprocessingException() {
		super();
	}

	public PreprocessingException(String msg) {
		super(msg);
	}
	
	public PreprocessingException(String msg,Throwable cause) {
		super(msg, cause);
	}
	
}
