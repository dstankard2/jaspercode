package net.sf.jaspercode.eng.application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.eng.JasperResources;
import net.sf.jaspercode.eng.files.UserFile;
import net.sf.jaspercode.eng.processing.ProcessorLog;

public class OutputManager {
	File outputDir;
	JasperResources jasperResources;
	Map<String,UserFile> userFiles = new HashMap<>();
	Map<String,SourceFile> sourceFiles = new HashMap<>();
	List<SourceFile> updates = new ArrayList<>();
	ProcessorLog appLog = null;
	
	public OutputManager(File outputDir, JasperResources jasperResources, ProcessorLog appLog) {
		this.outputDir = outputDir;
		this.jasperResources = jasperResources;
		this.appLog = appLog;
	}
	
	protected File getFile(String path) {
		return new File(outputDir, path);
	}
	
	public void removeUserFile(UserFile userFile) {
		if (!userFiles.containsKey(userFile.getPath())) {
			return;
		}
		this.appLog.debug("Removing user file "+userFile.getPath());
		File outputFile = null;
		
		outputFile = new File(outputDir, userFile.getPath());
		if (outputFile.exists()) {
			File dir = outputFile.getParentFile();
			outputFile.delete();
			userFiles.remove(userFile.getPath());
			if (dir.isDirectory()) {
				if (dir.listFiles().length==0) {
					dir.delete();
				}
			}
		}
	}
	
	public void writeUserFile(UserFile userFile) {
		File outputFile = null;
		
		if (userFiles.containsKey(userFile.getPath())) {
			return;
		}
		
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
		userFiles.put(userFile.getPath(), userFile);
	}

	// TODO: Returns empty map after update of systemAttributes.properties
	public Map<String,UserFile> getUserFiles() {
		return userFiles;
	}
	
	public SourceFile getSourceFile(String path) {
		SourceFile ret = null;
		
		ret = updates.stream().filter(src -> src.getPath().equals(path)).findFirst().orElse(null);
		if (ret==null) {
			SourceFile existing = sourceFiles.get(path);
			if (existing!=null) {
				ret = existing.copy();
				updates.add(ret);
			}
		}
		
		return ret;
	}
	
	// The file will never be in updates.  updates should be empty when this is invoked
	public void removeSourceFile(String path) {
		SourceFile existing = sourceFiles.get(path);

		if (existing!=null) {
			File actualFile = getFile(path);
			if (actualFile.exists()) {
				actualFile.delete();
				jasperResources.engineDebug("Removing source file at path "+actualFile.getAbsolutePath());
				sourceFiles.remove(path);
			}
		}
	}

	// Update the record for a source file.
	public void updateSourceFile(SourceFile src) {
		SourceFile existing = updates.stream().filter(f -> f.getPath().equals(src.getPath())).findFirst().orElse(null);
		if (existing!=null) {
			updates.remove(existing);
		}
		updates.add(src);
	}
	
	// For all source files that have been updated, write them to the file system. 
	public void flushSourceFiles() {
		updates.forEach(update -> {
			saveSourceFile(update);
			sourceFiles.put(update.getPath(), update);
		});
		updates.clear();
	}

	private void saveSourceFile(SourceFile src) {
		appLog.debug("Writing source file "+src.getPath());
		removeSourceFile(src.getPath());

		File current = getFile(src.getPath());
		if (current.exists())
			current.delete();
		StringBuilder content = src.getSource();
		
		current = getFile(src.getPath());
		current.getParentFile().mkdirs();
		try (FileWriter writer = new FileWriter(current)) {
			writer.write(content.toString());
			writer.flush();
			sourceFiles.put(src.getPath(), src);
		} catch(IOException e) {
			System.err.println("Couldn't write file "+src.getPath());
			e.printStackTrace();
		}
		sourceFiles.put(src.getPath(), src);
	}
	
}

