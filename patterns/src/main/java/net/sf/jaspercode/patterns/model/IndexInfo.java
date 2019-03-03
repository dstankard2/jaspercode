package net.sf.jaspercode.patterns.model;

import java.util.ArrayList;
import java.util.List;

public class IndexInfo {

	private String name;
	private List<String> columns = new ArrayList<>();
	private boolean unique = false;
	
	public IndexInfo() {
	}
	
	public IndexInfo(String name, List<String> columns, boolean unique) {
		super();
		this.name = name;
		this.columns = columns;
		this.unique = unique;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getColumns() {
		return columns;
	}
	public void setColumns(List<String> columns) {
		this.columns = columns;
	}
	public boolean isUnique() {
		return unique;
	}
	public void setUnique(boolean unique) {
		this.unique = unique;
	}
	
}
