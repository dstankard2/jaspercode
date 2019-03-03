package net.sf.jaspercode.engine.impl;

import net.sf.jaspercode.api.Log;

public class ProcessorLogImpl implements Log {
	String componentName = null;
	
	public ProcessorLogImpl(String componentName, String logLevel) {
		this.componentName = componentName;
	}

	private static final int NOTIFY_MESSAGE_LENGTH = 80;
	
	public void notifyStartProcessing() {
		String name = componentName;
		if (name.length()>NOTIFY_MESSAGE_LENGTH) name = name.substring(0, 77)+"...";
		int len = NOTIFY_MESSAGE_LENGTH + 10 - name.length();
		int start = len / 2;
		int end = (len+1) / 2;
		StringBuilder b = new StringBuilder();
		b.append("[SYSTEM] ");
		for(int i=0;i<start;i++) {
			b.append('-');
		}
		b.append(' ').append(name).append(' ');
		for(int i=0;i<end;i++) {
			b.append('-');
		}
		
		System.out.println(b.toString());
	}
	
	public void notifyFinishProcessing() {
		StringBuilder b = new StringBuilder();
		b.append("[SYSTEM] ------------------------------------- End of Component -------------------------------------");
		System.out.println(b.toString());
	}
	
	public void notifyNoProcessor() {
		System.out.println("No processor found for component");
	}
	
	private String logLevel = "INFO";	

	@Override
	public boolean isVerbose() {
		return false;
	}
	
	protected void log(String level,String message) {
		System.out.println("["+level+"] "+message);
	}

	@Override
	public void info(String message) {
		if (logLevel.equals("INFO")) {
			log("INFO",message);
		}
	}

	@Override
	public void debug(String message) {
		if ((logLevel.equals("INFO")) || (logLevel.equals("DEBUG"))) {
			log("DEBUG", message);
		}
	}

	@Override
	public void warn(String message) {
		if (!logLevel.equals("ERROR")) {
			log("WARN",message);
		}
	}

	@Override
	public void error(String message) {
		log("ERROR",message);
	}

}
