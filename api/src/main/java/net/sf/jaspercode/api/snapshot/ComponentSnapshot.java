package net.sf.jaspercode.api.snapshot;

import java.util.ArrayList;
import java.util.List;

public class ComponentSnapshot extends ItemSnapshot {

	// System Attributes
	private List<String> systemAttributesOriginated = new ArrayList<>();
	private List<String> systemAttributeDependencies = new ArrayList<>();

	// Source files (paths)
	private List<String> sourceFilePaths = new ArrayList<>();

	// Types
	private List<TypeInfo> typesOriginated = new ArrayList<>();
	private List<TypeInfo> typeDependencies = new ArrayList<>();
	//private List<String> typesOriginated = new ArrayList<>();

	// Objects
	private List<String> objectDependencies = new ArrayList<>();
	
	// Components
	private List<String> componentsOriginated = new ArrayList<>();

	public ComponentSnapshot() {
	}

	/*
	public ComponentSnapshot(int id, String name, List<String> systemAttributesOriginated,
			List<String> systemAttributeDependencies, List<String> sourceFilePaths, List<TypeInfo> typesOriginated,
			List<TypeInfo> typeDependencies, List<String> objectDependencies, List<String> componentsOriginated) {
		super();
		this.id = id;
		this.name = name;
		this.systemAttributesOriginated = systemAttributesOriginated;
		this.systemAttributeDependencies = systemAttributeDependencies;
		this.sourceFilePaths = sourceFilePaths;
		this.typesOriginated = typesOriginated;
		this.typeDependencies = typeDependencies;
		this.objectDependencies = objectDependencies;
		this.componentsOriginated = componentsOriginated;
	}
	*/

	public List<String> getSystemAttributesOriginated() {
		return systemAttributesOriginated;
	}

	public void setSystemAttributesOriginated(List<String> systemAttributesOriginated) {
		this.systemAttributesOriginated = systemAttributesOriginated;
	}

	public List<String> getSystemAttributeDependencies() {
		return systemAttributeDependencies;
	}

	public void setSystemAttributeDependencies(List<String> systemAttributeDependencies) {
		this.systemAttributeDependencies = systemAttributeDependencies;
	}

	public List<String> getSourceFilePaths() {
		return sourceFilePaths;
	}

	public void setSourceFilePaths(List<String> sourceFilePaths) {
		this.sourceFilePaths = sourceFilePaths;
	}

	public List<TypeInfo> getTypesOriginated() {
		return typesOriginated;
	}

	public void setTypesOriginated(List<TypeInfo> typesOriginated) {
		this.typesOriginated = typesOriginated;
	}

	public List<TypeInfo> getTypeDependencies() {
		return typeDependencies;
	}

	public void setTypeDependencies(List<TypeInfo> typeDependencies) {
		this.typeDependencies = typeDependencies;
	}

	public List<String> getObjectDependencies() {
		return objectDependencies;
	}

	public void setObjectDependencies(List<String> objectDependencies) {
		this.objectDependencies = objectDependencies;
	}

	public List<String> getComponentsOriginated() {
		return componentsOriginated;
	}

	public void setComponentsOriginated(List<String> componentsOriginated) {
		this.componentsOriginated = componentsOriginated;
	}

}

