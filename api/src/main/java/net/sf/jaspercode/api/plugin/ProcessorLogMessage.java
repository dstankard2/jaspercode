package net.sf.jaspercode.api.plugin;

import net.sf.jaspercode.api.logging.ProcessorLogLevel;

public class ProcessorLogMessage {

	private ProcessorLogLevel level = null;
	private String message = null;
	private Throwable e = null;
	
	public ProcessorLogMessage() {
	}
	
	public ProcessorLogMessage(ProcessorLogLevel level, String message, Throwable e) {
		super();
		this.level = level;
		this.message = message;
		this.e = e;
	}

	public ProcessorLogLevel getLevel() {
		return level;
	}

	public void setLevel(ProcessorLogLevel level) {
		this.level = level;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Throwable getThrowable() {
		return e;
	}

	public void setE(Throwable e) {
		this.e = e;
	}

}

