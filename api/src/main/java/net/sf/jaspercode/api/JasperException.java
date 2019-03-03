package net.sf.jaspercode.api;

public class JasperException extends Exception {
	private static final long serialVersionUID = 1L;

	public JasperException() {
		super();
	}
	
	public JasperException(String msg) {
		super(msg);
	}
	
	public JasperException(String msg,Throwable cause) {
		super(msg,cause);
	}
	
}
