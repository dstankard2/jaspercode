package net.sf.jaspercode.eng.exception;

public class ProcessingException extends Exception {
	private static final long serialVersionUID = 1L;

	public ProcessingException(String msg) {
		super(msg);
	}
	
	public ProcessingException(String msg,Throwable cause) {
		super(msg, cause);
	}
	
}
