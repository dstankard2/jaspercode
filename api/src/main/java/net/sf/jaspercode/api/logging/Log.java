package net.sf.jaspercode.api.logging;

public interface Log {

	public boolean isVerbose();
	public void info(String message);
	public void debug(String message);
	public void warn(String message);
	public void warn(String message, Throwable e);
	public void error(String message);
	public void error(String message,Throwable e);
	
}
