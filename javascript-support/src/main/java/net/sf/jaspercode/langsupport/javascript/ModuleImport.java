package net.sf.jaspercode.langsupport.javascript;

import java.util.ArrayList;
import java.util.List;

public class ModuleImport {

	private List<String> moduleNames = new ArrayList<>();
	
	private String location = null;

	public List<String> getModuleNames() {
		return moduleNames;
	}

	public void setModuleNames(List<String> moduleNames) {
		this.moduleNames = moduleNames;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	
}
