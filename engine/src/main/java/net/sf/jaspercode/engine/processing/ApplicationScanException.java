package net.sf.jaspercode.engine.processing;

public class ApplicationScanException extends Exception {

	private static final long serialVersionUID = 1L;

	public ApplicationScanException() {
		super();
	}
	
	public ApplicationScanException(String msg) {
		super(msg);
	}
	
	public ApplicationScanException(String msg,Throwable cause) {
		super(msg,cause);
	}
	
}
