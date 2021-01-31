package net.sf.jaspercode.eng.processing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.resources.FileProcessor;
import net.sf.jaspercode.api.resources.FolderWatcher;
import net.sf.jaspercode.api.types.VariableType;
import net.sf.jaspercode.eng.files.ComponentFile;

public class ProcessableChanges {

	protected int itemId = 0;
	
	protected List<SourceFile> sourceFiles = new ArrayList<>();
	protected List<Pair<String,VariableType>> typeDependencies = new ArrayList<>();
	protected List<Pair<String,VariableType>> typesAdded = new ArrayList<>();

	protected List<String> objectDeps = new ArrayList<>();
	protected Map<String,Object> objects = new HashMap<>();
	
	protected Map<String,String> attributesAdded = new HashMap<>();
	protected List<String> attributeDependencies = new ArrayList<>();

	protected List<Pair<String,FolderWatcher>> folderWatchersAdded = new ArrayList<>();
	protected List<Pair<String,FileProcessor>> fileProcessorsAdded = new ArrayList<>();
	
	protected List<Component> componentsAdded = new ArrayList<>();

	// Required for configurations
	protected ComponentFile originalFile = null;
	protected Component originalComponent = null;
	
	public ProcessableChanges(int itemId, ComponentFile originalFile, Component originalComponent) {
		this.itemId = itemId;
		this.originalFile = originalFile;
		this.originalComponent = originalComponent;
	}

	public int getItemId() {
		return itemId;
	}

	public List<SourceFile> getSourceFiles() {
		return sourceFiles;
	}

	public void setSourceFiles(List<SourceFile> sourceFiles) {
		this.sourceFiles = sourceFiles;
	}

	public Map<String, Object> getObjects() {
		return objects;
	}

	public void setObjects(Map<String, Object> objects) {
		this.objects = objects;
	}

	public List<String> getObjectDeps() {
		return objectDeps;
	}

	public void setObjectDeps(List<String> objectDeps) {
		this.objectDeps = objectDeps;
	}

	public List<Pair<String, VariableType>> getTypeDependencies() {
		return typeDependencies;
	}

	public void setTypeDependencies(List<Pair<String, VariableType>> typeDependencies) {
		this.typeDependencies = typeDependencies;
	}

	public List<Pair<String, VariableType>> getTypesAdded() {
		return typesAdded;
	}

	public void setTypesAdded(List<Pair<String, VariableType>> typesAdded) {
		this.typesAdded = typesAdded;
	}

	public Map<String, String> getAttributesAdded() {
		return attributesAdded;
	}

	public void setAttributesAdded(Map<String, String> attributesAdded) {
		this.attributesAdded = attributesAdded;
	}

	public List<String> getAttributeDependencies() {
		return attributeDependencies;
	}

	public void setAttributeDependencies(List<String> attributeDependencies) {
		this.attributeDependencies = attributeDependencies;
	}

	public List<Pair<String, FolderWatcher>> getFolderWatchersAdded() {
		return folderWatchersAdded;
	}

	public void setFolderWatchersAdded(List<Pair<String, FolderWatcher>> folderWatchersAdded) {
		this.folderWatchersAdded = folderWatchersAdded;
	}

	public List<Pair<String, FileProcessor>> getFileProcessorsAdded() {
		return fileProcessorsAdded;
	}

	public void setFileProcessorsAdded(List<Pair<String, FileProcessor>> fileProcessorsAdded) {
		this.fileProcessorsAdded = fileProcessorsAdded;
	}

	public List<Component> getComponentsAdded() {
		return componentsAdded;
	}

	public void setComponentsAdded(List<Component> componentsAdded) {
		this.componentsAdded = componentsAdded;
	}

	public List<Pair<String,VariableType>> getTypesReferenced() {
		List<Pair<String,VariableType>> ret = new ArrayList<>();
		ret.addAll(typeDependencies);
		ret.addAll(typesAdded);
		return ret;
	}

	public ComponentFile getOriginalFile() {
		return originalFile;
	}

	public void setOriginalFile(ComponentFile originalFile) {
		this.originalFile = originalFile;
	}

	public Component getOriginalComponent() {
		return originalComponent;
	}

	public void setOriginalComponent(Component originalComponent) {
		this.originalComponent = originalComponent;
	}

}

