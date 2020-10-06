package net.sf.jaspercode.engine.processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import net.sf.jaspercode.api.ApplicationContext;
import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.resources.FileProcessor;
import net.sf.jaspercode.api.resources.FolderWatcher;
import net.sf.jaspercode.api.types.VariableType;
import net.sf.jaspercode.engine.application.JasperResources;

public abstract class ProcessableContextBase implements ProcessableContext {

	protected List<SourceFile> sourceFiles = new ArrayList<>();
	protected Map<String,Object> objects = new HashMap<>();
	protected List<Pair<String,VariableType>> typesOriginated = new ArrayList<>();
	protected List<Pair<String,VariableType>> typeDependencies = new ArrayList<>();

	protected Map<String,String> attributesAdded = new HashMap<>();
	protected List<String> attributesOriginated = new ArrayList<>();
	protected List<String> attributeDependencies = new ArrayList<>();

	protected List<Pair<String,FolderWatcher>> folderWatchersAdded = new ArrayList<>();
	protected List<Pair<String,FileProcessor>> fileProcessorsAdded = new ArrayList<>();
	
	protected List<Component> componentsAdded = new ArrayList<>();

	protected JasperResources jasperResources = null;
	
	public ProcessableContextBase(JasperResources jasperResources) {
		this.jasperResources = jasperResources;
	}
	
	@Override
	public void addSourceFile(SourceFile sourceFile) {
		sourceFiles.add(sourceFile);
	}

	@Override
	public abstract SourceFile getSourceFile(String path);

	@Override
	public void setObject(String name, Object value) {
		objects.put(name, value);
	}

	@Override
	public abstract Object getObject(String objectName);

	@Override
	public void addSystemAttribute(String name, String type) {
		attributesAdded.put(name, type);
	}

	@Override
	public void originateSystemAttribute(String name) {
		attributesOriginated.add(name);
	}

	@Override
	public void dependOnSystemAttribute(String name) {
		attributeDependencies.add(name);
	}

	@Override
	public abstract String getSystemAttribute(String name);

	@Override
	public void originateType(String lang, VariableType type) {
		typesOriginated.add(Pair.of(lang, type));
	}

	@Override
	public abstract void dependOnType(String lang, String name, BuildContext buildCtx);

	@Override
	public abstract VariableType getVariableType(String language, String typeName);

	@Override
	public abstract String getConfigurationProperty(String name);

	@Override
	public void addFolderWatcher(String path, FolderWatcher folderWatcher) {
		folderWatchersAdded.add(Pair.of(path, folderWatcher));
	}

	@Override
	public void addFileProcessor(String path, FileProcessor fileProcessor) {
		fileProcessorsAdded.add(Pair.of(path, fileProcessor));
	}

	@Override
	public ApplicationContext getApplicationContext() {
		return jasperResources;
	}

	@Override
	public void addComponent(Component component) {
		componentsAdded.add(component);
	}

}

