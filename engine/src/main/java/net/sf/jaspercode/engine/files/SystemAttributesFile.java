package net.sf.jaspercode.engine.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SystemAttributesFile implements WatchedResource {

	public static final String SYSTEM_ATTRIBUTES_FILENAME = "systemAttributes.properties";

	private Map<String,String> systemAttributes = null;
	private long lastModified = 0L;
	private ApplicationFolderImpl folder = null;

	public SystemAttributesFile(File systemAttributesFile, ApplicationFolderImpl folder) {
		super();
		systemAttributes = readAttributes(systemAttributesFile);
		//this.lastModified = lastModified;
		this.folder = folder;
	}

	private Map<String,String> readAttributes(File f) {
		Properties props = new Properties();
		Map<String,String> values = new HashMap<>();

		try {
			try (FileInputStream fin = new FileInputStream(f)) {
				props.load(fin);
				props.entrySet().forEach(e -> {
					values.put(
							e.getKey() != null ? e.getKey().toString() : "", 
							e.getValue() != null ? e.getValue().toString() : "");
				});
			}
		} catch(FileNotFoundException e) {
			// logically can't happen
		} catch(IOException e) {
			// TODO: ???
		}

		return values;
	}

	@Override
	public String getName() {
		return SYSTEM_ATTRIBUTES_FILENAME;
	}

	@Override
	public String getPath() {
		return folder.getPath()+SYSTEM_ATTRIBUTES_FILENAME;
	}

	@Override
	public long getLastModified() {
		return lastModified;
	}

	public Map<String, String> getSystemAttributes() {
		return systemAttributes;
	}

	@Override
	public ApplicationFolderImpl getFolder() {
		return folder;
	}

}
