package net.sf.jaspercode.langsupport.javascript;

import java.util.ArrayList;
import java.util.List;

import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.SourceFile;

public class JavascriptSourceFile implements SourceFile {

	private String path = null;
	private StringBuilder source = new StringBuilder();

	private List<ModuleImport> importedModules = new ArrayList<>();
	
	protected String getImportSource() {
		StringBuilder ret = new StringBuilder();
		
		for(ModuleImport im : importedModules) {
			String b = "import {";
			boolean first = true;
			for(String name : im.getModuleNames()) {
				if (first) first = false;
				else b += ',';
				b += name;
			}
			b += "} from '"+im.getLocation()+"';\n";
			ret.append(b);
		}
		
		return ret.toString();
	}
	
	@Override
	public StringBuilder getSource() throws JasperException {
		StringBuilder ret = new StringBuilder();
		
		ret.append(getImportSource());
		ret.append(source.toString());
		
		return ret;
	}

	@Override
	public String getPath() {
		return path;
	}
	public void setPath(String s) {
		this.path = s;
	}
	
	public void addModule(String location, String...moduleNames) {
		ModuleImport module = new ModuleImport();
		module.setLocation(location);
		for(String moduleName : moduleNames) {
			module.getModuleNames().add(moduleName);
		}
		importedModules.add(module);
	}
	
	public void addModule(ModuleImport module) {
		importedModules.add(module);
	}

}

