package net.sf.jaspercode.test;

import java.io.File;
import java.util.HashMap;

import net.sf.jaspercode.engine.JasperAgent;

public class JasperTest {

	private static File[] libs = new File[] {
			new File("C:\\git\\jasperSPE\\patterns\\target\\classes"),
			new File("C:\\git\\jasperSPE\\javascript-support\\target\\classes"),
			new File("C:\\git\\jasperSPE\\java-support\\target\\classes"),
			new File("C:\\git\\jasperSPE\\javascript-support\\target\\classes")
	};

	public static void main(String args[]) {
		try {
			runTest();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/*
	protected static void runTest() throws Exception {
		JasperAgent agent = null;
		HashMap<String,String> options = new HashMap<>();

		options.put("once", "");
		options.put("applicationDir", "/SomeLinuxFolder");
		options.put("applicationDir", "c://SomeWindowsFolder");

		options.put("singleAppMode", "");
		options.put("outputDir","/SomeLinuxFolder");
		options.put("outputDir","c:\\SomeWindowsFolder");

		agent = new JasperAgent(libs,options);
		agent.start();
	}
	*/

	// Realms, single app, daemon mode
	protected static void runTest() throws Exception {
		JasperAgent agent = null;
		HashMap<String,String> options = new HashMap<>();

		//options.put("once", "");
		//options.put("applicationDir", "/SomeLinuxFolder");
		options.put("applicationDir", "C:\\workspaces\\realms");

		options.put("singleAppMode", "");
		//options.put("outputDir","/SomeLinuxFolder");
		options.put("outputDir","C:\\build\\realms");
		
		agent = new JasperAgent(libs,options);
		agent.start();
	}

}

