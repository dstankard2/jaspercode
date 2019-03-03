package net.sf.jaspercode.engine.processing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ProcessContainer implements Runnable {
	private Process process = null;
	private String id = null;
	private Thread thread = null;
	private boolean stop = false;
	
	public static ProcessContainer create(String id, Process process) {
		ProcessContainer ret = new ProcessContainer(id, process);
		ret.init();
		return ret;
	}
	
	private void init() {
		thread = new Thread(this);
		thread.start();
	}
	
	@Override
	public void run() {
		InputStream procOut = process.getInputStream();
		InputStream procErr = process.getErrorStream();
		BufferedReader readerOut = new BufferedReader(new InputStreamReader(procOut));
		BufferedReader readerErr = new BufferedReader(new InputStreamReader(procErr));
		
		while(!stop) {
			try {
				if (!process.isAlive()) {
					System.out.println("Process '"+id+"' is no longer alive");
					stop = true;
				} else {
					if (procOut.available()>0) {
						String line = readerOut.readLine();
						System.out.println("[PROCESS:"+id+"] "+line);
					} else if (procErr.available()>0) {
						String line = readerErr.readLine();
						System.err.println("[PROCESS:"+id+"] "+line);
					}
				}
				try {
					Thread.sleep(50);
				} catch(Exception e) {				
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		terminate();
	}

	private ProcessContainer(String id,Process process) {
		this.id = id;
		this.process = process;
	}

	public void terminate() {
		if (process.isAlive()) {
			process.destroyForcibly();
		}
		System.out.println("Terminating process '"+id+"', exit code is "+process.exitValue());
		stop = true;
		thread = null;
		process = null;
	}

}

