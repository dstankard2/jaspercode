package net.sf.jaspercode.engine.processing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.engine.definitions.UserFile;

public class OutputManager {
	File outputDirectory = null;
	Map<String,SourceFile> sourceFiles = new HashMap<>();
	Map<String,UserFile> userFiles = new HashMap<>();
	Set<SourceFile> addedSourceFiles = new HashSet<>();

	public File getOutputDirectory() {
		return outputDirectory;
	}

	public OutputManager(String outputPath) throws JasperException {
		File dir = new File(outputPath);
		if (dir.exists()) {
			if (!dir.isDirectory()) {
				throw new JasperException("Output location '"+outputPath+"' is not a directory");
			} else {
				File contents[] = dir.listFiles();
				for(File f : contents) {
					String path = "/"+f.getName();
					if (f.isDirectory()) {
						removeFolder(path);
					} else {
						removeFile(path);
					}
				}
			}
		} else {
			dir.mkdirs();
		}
		this.outputDirectory = dir;
	}
	
	public void removeFolder(String folderPath) {
		
	}
	
	public void removeFile(String path) {
		
	}
	
	/*
	public void writeFile(String path,byte[] contents) throws IOException {
		File f = outputFiles.get(path);
		if (f!=null) {
			throw new IOException("Internal error - attempted to write file '"+path+"' but it was already there");
		}
		String p = path.substring(1);
		f = new File(outputDirectory,p);

		FileOutputStream fout = null;
		
		try {
			fout = new FileOutputStream(f);
			fout.write(contents);
		} finally {
			if (fout!=null) {
				try {
					fout.close();
				} catch(Exception e) { }
			}
		}
	}
	*/
	
	public void addSourceFile(SourceFile src) {
		addedSourceFiles.add(src);
	}
	
	public SourceFile getSourceFile(String path) {
		SourceFile ret = sourceFiles.get(path);
		if (ret==null) {
			for(SourceFile src : addedSourceFiles) {
				if (src.getPath().equals(path)) {
					ret = src;
					break;
				}
			}
		}
		return ret;
	}
	
	public void writeSourceFiles() throws EngineRuntimeException {
		for(SourceFile src : addedSourceFiles) {
			writeSourceFile(src);
			String path = src.getPath();
			sourceFiles.put(path, src);
		}
		addedSourceFiles.clear();
	}

	/*
	public void writeFile(String path,String contents) throws IOException {
		File f = outputFiles.get(path);
		if (f!=null) {
			throw new IOException("Internal error - attempted to write file '"+path+"' but it was already there");
		}
		String p = path.substring(1);
		f = new File(outputDirectory,p);
		FileWriter writer = null;

		try {
			writer = new FileWriter(f);
			writer.write(contents);
			writer.flush();
		} finally {
			if (writer!=null) {
				try {
					writer.close();
				} catch(Exception e) { }
			}
		}
	}
	*/
	
	public void forceFolder(String path) {
		File f = new File(outputDirectory,path);
		
		f.mkdirs();
	}
	
	protected void writeSourceFile(SourceFile src) throws EngineRuntimeException {
		File file = new File(outputDirectory,src.getPath());
		if (file.exists()) file.delete();
		FileWriter writer = null;
		
		try {
			String str = src.getSource().toString();
			file.getParentFile().mkdirs();
			writer = new FileWriter(file);
			writer.write(str);
		} catch(Exception e) {
			throw new EngineRuntimeException("Couldn't write file '"+file.getAbsolutePath()+"'",e);
		} finally {
			if (writer!=null) {
				try {
					writer.close();
				} catch(Exception e) { }
			}
		}
	}
	
	public void writeUserFile(UserFile userFile) {
		File file = new File(outputDirectory,userFile.getPath());
		if (file.exists()) file.delete();
		InputStream in = null;
		FileOutputStream fout = null;
		
		try {
			in = userFile.getInputStream();
			fout = new FileOutputStream(file);
			while(in.available()>0) {
				int b = in.read();
				if (b>=0) fout.write(b);
			}
		} catch(Exception e) {
			System.err.println("Couldn't write file '"+file.getAbsolutePath()+"'");
			e.printStackTrace();
		} finally {
			if (in!=null) {
				try {
					in.close();
				} catch(Exception e) { }
			}
			if (fout!=null) {
				try {
					fout.close();
				} catch(Exception e) { }
			}
		}

	}
	
}
