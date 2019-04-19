package net.sf.jaspercode.engine.exception;

public class ProcessingException extends Exception {

	public ProcessingException(String msg) {
		super(msg);
	}
	
	public ProcessingException(String msg,Throwable cause) {
		super(msg, cause);
	}
	
}
