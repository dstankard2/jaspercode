package net.sf.jaspercode.engine.application;

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
import java.util.Map.Entry;

import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.snapshot.SourceFileSnapshot;
import net.sf.jaspercode.engine.definitions.UserFile;

public class OutputManager {

	File outputDir = null;

	// Source files that have been persisted
	Map<String,SourceFile> sourceFiles = new HashMap<>();

	// Source files that have been copied for the current component
	List<SourceFile> sourceFilesCreated = new ArrayList<>();
	
	// Source files that have not been persisted yet.
	List<SourceFile> sourceFilesAdded = new ArrayList<>();
	
	public OutputManager(File outputDir) {
		this.outputDir = outputDir;
	}

	// Returns snapshots of source files that have been persisted
	public List<SourceFileSnapshot> getSourceFileSnapshots() {
		List<SourceFileSnapshot> ret = new ArrayList<>();
		for(Entry<String,SourceFile> entry : sourceFiles.entrySet()) {
			SourceFile src = entry.getValue();
			SourceFileSnapshot f = new SourceFileSnapshot(src.getPath(),new ArrayList<>(),src.getSource().toString());
			ret.add(f);
		}
		return ret;
	}

	public void removeUserFile(UserFile userFile) {
		File outputFile = null;
		
		outputFile = new File(outputDir, userFile.getPath());
		outputFile.delete();
	}

	public void writeUserFile(UserFile userFile) {
		File outputFile = null;
		
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
	}

	// If the file has been accessed by the current component it will be in created files.
	// If the file has been access in the current scan it will be in added files.
	// Otherwise the file will be in sourceFiles
	// If the file isn't in createdFiles we have to make a copy of it.
	public SourceFile getSourceFile(String path) {
		SourceFile ret = null;
		
		for(SourceFile src : sourceFilesCreated) {
			if (src.getPath().equals(path)) {
				ret = src;
				break;
			}
		}
		if (ret==null) {
			for(SourceFile src : sourceFilesAdded) {
				if (src.getPath().equals(path)) {
					ret = src;
					break;
				}
			}
			if (ret!=null) {
				ret = ret.copy();
			}
		}
		if (ret==null) {
			ret = sourceFiles.get(path);
			if (ret!=null) {
				ret = ret.copy();
			}
		}

		return ret;
	}
	
	// If the source file has already been added, then there's no need to save
	public void addSourceFile(SourceFile sourceFile) {
		SourceFile current = null;
		for(SourceFile src : sourceFilesAdded) {
			if (src.getPath().equals(sourceFile.getPath())) {
				current = src;
				break;
			}
		}
		if (current!=null) {
			sourceFilesAdded.remove(current);
		}
		sourceFilesAdded.add(sourceFile);
	}

	// The files created by the current component should be added to addedFiles 
	// so that they can be persisted at the end of the scan
	public void commitCreatedFiles() {
		for(SourceFile src : sourceFilesCreated) {
			String path = src.getPath();
			SourceFile addedFile = null;
			for(SourceFile added : sourceFilesAdded) {
				if (added.getPath().equals(path)) {
					addedFile = added;
					break;
				}
			}
			if (addedFile!=null) {
				sourceFilesAdded.remove(addedFile);
			}
			sourceFilesAdded.add(src);
		}
		sourceFilesCreated.clear();
	}

	protected File getFile(String path) {
		return new File(outputDir, path);
	}
	
	// This is never called by a component so there is not a need to check created files.
	public void removeSourceFile(String path) {
		SourceFile added = null;

		sourceFiles.remove(path);
		
		for(SourceFile src : sourceFilesAdded) {
			if (src.getPath().equals(path)) {
				added = src;
				break;
			}
		}
		if (added!=null) {
			sourceFilesAdded.remove(added);
			added = null;
		}
		
		File actualFile = getFile(path);
		if (actualFile.exists()) {
			actualFile.delete();
			System.out.println("Removing source file at path "+actualFile.getAbsolutePath());
		}
	}
	
	public void writeAddedSourceFiles() {
		for(SourceFile src : sourceFilesAdded) {
			writeAddedSourceFile(src);
		}
		sourceFilesAdded.clear();
	}

	protected void writeAddedSourceFile(SourceFile src) {
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
	}
	
}

