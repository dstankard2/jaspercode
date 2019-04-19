package net.sf.jaspercode.api.snapshot;

import java.util.ArrayList;
import java.util.List;

public class SystemAttributeSnapshot {

	private String name = null;
	private String type = null;
	private String description = null;
	private List<Integer> originators = new ArrayList<>();
	private List<Integer> dependants = new ArrayList<>();

	public SystemAttributeSnapshot(String name, String type, String description, List<Integer> originators,
			List<Integer> dependants) {
		super();
		this.name = name;
		this.type = type;
		this.description = description;
		this.originators = originators;
		this.dependants = dependants;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<Integer> getOriginators() {
		return originators;
	}
	public void setOriginators(List<Integer> originators) {
		this.originators = originators;
	}
	public List<Integer> getDependants() {
		return dependants;
	}
	public void setDependants(List<Integer> dependants) {
		this.dependants = dependants;
	}

}
