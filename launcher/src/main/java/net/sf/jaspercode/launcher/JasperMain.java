package net.sf.jaspercode.launcher;

public class JasperMain {

	public static void main(String args[]) {
		try {
			new AgentLoader(args).start();
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}
	
}
