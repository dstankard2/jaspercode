package net.sf.jaspercode.engine.application;

import java.io.File;
import java.util.List;
import java.util.Map;

import net.sf.jaspercode.engine.ComponentFileReader;
import net.sf.jaspercode.engine.EngineInitException;
import net.sf.jaspercode.engine.JasperResources;
import net.sf.jaspercode.engine.files.ApplicationFolderImpl;
import net.sf.jaspercode.engine.files.SystemAttributesFile;
import net.sf.jaspercode.engine.processing.FileChange;

public class ResourceManager {
	
	File directory;
	ApplicationFolderImpl folder = null;
	SystemAttributesFile attsFile = null;

	public ResourceManager(File applicationFolder, JasperResources jasperResources) throws EngineInitException {
		this.directory = applicationFolder;
		ComponentFileReader componentFileReader = new ComponentFileReader(jasperResources.getXmlConfigClasses());
		this.folder = new ApplicationFolderImpl(applicationFolder, null, componentFileReader);
	}

	public Map<String,String> getSystemAttributes() {
		return folder.getGlobalSystemAttributes();
	}

	public List<FileChange> getFileChanges() {
		return folder.findChanges(true);
	}
	
	public void close() {
		folder.remove(false);
	}

}

