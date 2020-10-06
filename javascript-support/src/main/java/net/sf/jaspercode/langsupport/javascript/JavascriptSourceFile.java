package net.sf.jaspercode.langsupport.javascript;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.langsupport.javascript.types.ModuleType;

public class JavascriptSourceFile implements SourceFile {

	private StringBuilder source = new StringBuilder();
	protected String path = null;
	protected List<Pair<String,String>> importedModules = new ArrayList<>();
	
	public JavascriptSourceFile() {
	}
	
	@Override
	public StringBuilder getSource() {
		StringBuilder ret = new StringBuilder();
		
		//ret.append(getImportSource());
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

	public void importModule(Pair<String,String> module) {
		this.importedModules.add(module);
	}
	public void importModule(ModuleType type) {
		importedModules.add(Pair.of(type.getName(), type.getWebPath()));
	}

	@Override
	public SourceFile copy() {
		JavascriptSourceFile ret = new JavascriptSourceFile();
		ret.importedModules.addAll(importedModules);
		ret.setPath(getPath());
		ret.source = new StringBuilder(getSource().toString());
		return ret;
	}

}

