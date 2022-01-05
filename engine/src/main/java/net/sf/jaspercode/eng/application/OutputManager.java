package net.sf.jaspercode.eng.application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.eng.files.UserFile;
import net.sf.jaspercode.eng.processing.ProcessorLog;

public class OutputManager {
	File outputDir;
	ProcessorLog appLog = null;
	
	public OutputManager(File outputDir, ProcessorLog appLog) {
		this.outputDir = outputDir;
		this.appLog = appLog;
	}
	
	public void clearOutput() {
		removeDirectoryContents(outputDir);
	}
	
	private void removeDirectoryContents(File dir) {
		File[] files = dir.listFiles();
		if (files!=null) {
			for(File f : files) {
				if (f.isDirectory()) {
					removeDirectoryContents(f);
				} else {
					f.delete();
				}
			}
		}
	}

	protected File getFile(String path) {
		return new File(outputDir, path);
	}
	
	public void writeUserFile(UserFile userFile) {
		File outputFile = null;
		
		this.appLog.debug("Write user file "+userFile.getPath());
		outputFile = new File(outputDir, userFile.getPath());
		outputFile.getParentFile().mkdirs();
		try (InputStream in = userFile.getInputStream(); FileOutputStream out = new FileOutputStream(outputFile)) {
			int i = 0;
			while((i = in.read()) >= 0) {
				out.write(i);
			}
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		//userFiles.put(userFile.getPath(), userFile);
	}

	public void writeSourceFile(SourceFile src) {
		appLog.debug("Writing source file "+src.getPath());

		File current = getFile(src.getPath());
		if (current.exists())
			current.delete();
		StringBuilder content = src.getSource();
		
		current = getFile(src.getPath());
		current.getParentFile().mkdirs();
		try (FileWriter writer = new FileWriter(current)) {
			writer.write(content.toString());
			writer.flush();
		} catch(IOException e) {
			System.err.println("Couldn't write file "+src.getPath());
			e.printStackTrace();
		}
	}
	
}

