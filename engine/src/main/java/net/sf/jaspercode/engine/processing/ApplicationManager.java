package net.sf.jaspercode.engine.processing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;

import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.langsupport.LanguageSupport;
import net.sf.jaspercode.api.resources.ApplicationResource;
import net.sf.jaspercode.api.types.VariableType;
import net.sf.jaspercode.engine.EngineLanguages;
import net.sf.jaspercode.engine.EnginePatterns;
import net.sf.jaspercode.engine.definitions.WatchedResource;
import net.sf.jaspercode.engine.impl.ApplicationContextImpl;
import net.sf.jaspercode.engine.definitions.ApplicationFolderImpl;
import net.sf.jaspercode.engine.definitions.ComponentFile;
import net.sf.jaspercode.engine.definitions.UserFile;

public class ApplicationManager implements ComponentContainer {
	private String name = null;

	private ResourceManager resourceManager = null;
	private ComponentManager componentManager = null;
	private OutputManager outputManager = null;
	private EngineLanguages languages = null;
	//private ApplicationContextImpl applicationContext = null;

	private Map<String,Object> objects = new HashMap<>();
	private Map<String,String> systemAttributes = new HashMap<>();

	private Map<String,Map<String,VariableType>> variableTypes = new HashMap<>();

	public ApplicationManager(File applicationDir,String engineOutputPath,EnginePatterns patterns,EngineLanguages languages,ApplicationContextImpl applicationContext) throws JAXBException,JasperException {
		this.name = applicationDir.getName();
		//this.applicationContext = applicationContext;
		resourceManager = new ResourceManager(systemAttributes,applicationDir,patterns,applicationContext);
		componentManager = new ComponentManager(this,patterns,applicationContext);
		this.languages = languages;
		outputManager = new OutputManager(engineOutputPath+File.separatorChar+applicationDir.getName());
	}
	
	public String getApplicationName() {
		return name;
	}

	@Override
	public File getOutputDirectory() {
		return outputManager.getOutputDirectory();
	}



	public void checkDeploy() {
		ApplicationFolderImpl root = resourceManager.getRootFolder();
		checkDeploy(root);
	}
	
	protected void checkDeploy(ApplicationFolderImpl folder) {
		BuildComponentEntry buildComp = folder.getBuildComponent();
		
		if (buildComp!=null) {
			buildComp.checkDeploy();
		}
		for(Entry<String,ApplicationFolderImpl> entry : folder.getSubFolders().entrySet()) {
			checkDeploy(entry.getValue());
		}
	}

	public void checkBuild() {
		ApplicationFolderImpl root = resourceManager.getRootFolder();
		BuildComponentEntry buildComp = root.getCurrentBuildComponent(this);
		
		if (buildComp!=null) {
			buildComp.checkBuild();
		}
	}

	public void scan() throws IOException, FileNotFoundException, JasperException,EngineRuntimeException {
		checkForRemovedFiles();
		checkForAddedFiles();
		//checkBuild();
		//checkDeploy();
	}
	
	protected void checkForAddedFiles() throws EngineRuntimeException,JasperException {
		List<WatchedResource> added = resourceManager.scanForNewFiles();
		
		// First, write directories and user files.
		for(WatchedResource file : added) {
			if (file instanceof ApplicationFolderImpl) {
				ApplicationFolderImpl folder = (ApplicationFolderImpl)file;
				outputManager.forceFolder(folder.getPath());
			} else if (file instanceof UserFile) {
				UserFile userFile = (UserFile)file;
				outputManager.writeUserFile(userFile);
			} else {
				componentManager.addComponentFile((ComponentFile)file);
			}
		}
		componentManager.processComponentFilesAdded();
		outputManager.writeSourceFiles();
	}
	
	protected void checkForRemovedFiles() {
		List<WatchedResource> removed = resourceManager.scanForRemovedFiles();
		
		for(WatchedResource res : removed) {
			if (res instanceof ApplicationFolderImpl) {
				ApplicationFolderImpl f = (ApplicationFolderImpl)res;
				removeFolder(f);
			} else if (res instanceof UserFile) {
				removeUserFile((UserFile)res);
			} else if (res instanceof ComponentFile) {
				removeComponentFile((ComponentFile)res);
			}
		}
	}
	
	protected void removeComponentFile(ComponentFile f) {
		System.out.println("Remove component file "+f.getPath());
	}
	
	protected void removeUserFile(UserFile f) {
		System.out.println("Remove user file "+f.getPath());
	}
	
	protected void removeFolder(ApplicationFolderImpl f) {
		System.out.println("Remove folder "+f.getPath());
	}

	@Override
	public Map<String, VariableType> getVariableTypes(String lang) throws JasperException {
		if (!variableTypes.containsKey(lang)) {
			LanguageSupport supp = languages.getLanguageSupport(lang);
			if (supp==null) {
				return null;
			}
			List<VariableType> baseTypes = supp.getBaseVariableTypes();
			Map<String,VariableType> langTypes = new HashMap<>();
			for(VariableType type : baseTypes) {
				langTypes.put(type.getName(), type);
			}
			variableTypes.put(lang, langTypes);
		}
		
		return variableTypes.get(lang);
	}

	@Override
	public Map<String, String> getSystemAttributes() {
		return systemAttributes;
	}

	@Override
	public Map<String, Object> getObjects() {
		return objects;
	}

	@Override
	public SourceFile getSourceFile(String path) {
		return outputManager.getSourceFile(path);
	}

	@Override
	public BuildContext getBuildContext(String folderPath) {
		ApplicationFolderImpl folder = (ApplicationFolderImpl)resourceManager.getRootFolder().getResource(folderPath);
		return folder.getBuildContext(this);
	}

	@Override
	public void addComponent(Component component, ComponentFile originator) {
		componentManager.addComponent(component, originator);
	}

	public void addSourceFile(SourceFile src) {
		outputManager.addSourceFile(src);
	}

	@Override
	public Map<String, String> getConfiguration(String folderPath) {
		ApplicationFolderImpl folder = (ApplicationFolderImpl)resourceManager.getRootFolder().getResource(folderPath);
		return folder.getJasperProperties();
	}

	@Override
	public ApplicationResource getApplicationResource(String path) {
		return resourceManager.getRootFolder().getResource(path);
	}

}
