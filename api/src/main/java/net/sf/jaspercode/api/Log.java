package net.sf.jaspercode.api;

public interface Log {

	public boolean isVerbose();
	public void info(String message);
	public void debug(String message);
	public void warn(String message);
	public void error(String message);
	
}
