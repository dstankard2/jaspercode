package net.sf.jaspercode.api.snapshot;

import java.util.ArrayList;
import java.util.List;

public class ApplicationSnapshot {

	private List<SystemAttributeSnapshot> systemAttributes = new ArrayList<>();
	private List<SourceFileSnapshot> sourceFiles = new ArrayList<>();
	private List<ItemSnapshot> items = new ArrayList<>();

	public ApplicationSnapshot() {
	}

	public ApplicationSnapshot(List<SystemAttributeSnapshot> systemAttributes, List<SourceFileSnapshot> sourceFiles,
			List<ItemSnapshot> items) {
		super();
		this.systemAttributes = systemAttributes;
		this.sourceFiles = sourceFiles;
		this.items = items;
	}

	public List<SystemAttributeSnapshot> getSystemAttributes() {
		return systemAttributes;
	}

	public void setSystemAttributes(List<SystemAttributeSnapshot> systemAttributes) {
		this.systemAttributes = systemAttributes;
	}

	public List<SourceFileSnapshot> getSourceFiles() {
		return sourceFiles;
	}

	public void setSourceFiles(List<SourceFileSnapshot> sourceFiles) {
		this.sourceFiles = sourceFiles;
	}

	public List<ItemSnapshot> getItems() {
		return items;
	}

	public void setItems(List<ItemSnapshot> items) {
		this.items = items;
	}

}

