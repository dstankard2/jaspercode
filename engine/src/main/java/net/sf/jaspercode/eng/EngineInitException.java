package net.sf.jaspercode.eng;

public class EngineInitException extends Exception {
	private static final long serialVersionUID = 1L;

	public EngineInitException() {
		super();
	}

	public EngineInitException(String msg) {
		super(msg);
	}

	public EngineInitException(String msg,Throwable cause) {
		super(msg, cause);
	}

}
