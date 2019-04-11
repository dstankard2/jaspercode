package net.sf.jaspercode.langsupport.javascript;

import java.util.ArrayList;
import java.util.List;

import net.sf.jaspercode.api.SourceFile;

public class JavascriptSourceFile implements SourceFile {

	private StringBuilder source = new StringBuilder();
	protected String path = null;
	protected List<ModuleImport> importedModules = new ArrayList<>();
	
	public JavascriptSourceFile() {
	}
	
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
	public StringBuilder getSource() {
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

	@Override
	public SourceFile copy() {
		JavascriptSourceFile ret = new JavascriptSourceFile();
		ret.setPath(getPath());
		ret.source = new StringBuilder(getSource().toString());
		return ret;
	}

}

