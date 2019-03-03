package net.sf.jaspercode.patterns.java.http;

import java.util.ArrayList;
import java.util.List;

public class ServletFilterChainDef {

	private String name;
	private List<String> filterNames = new ArrayList<>();

	public ServletFilterChainDef() {
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getFilterNames() {
		return filterNames;
	}
	public void setFilterNames(List<String> filterNames) {
		this.filterNames = filterNames;
	}

}
