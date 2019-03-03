package net.sf.jaspercode.langsupport.javascript.modules;

import java.util.ArrayList;
import java.util.List;

import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.langsupport.javascript.JavascriptSourceFile;

public class ModuleSourceFile extends JavascriptSourceFile {

	private List<ModuleSource> modules = new ArrayList<>();
	
	@Override
	public StringBuilder getSource() throws JasperException {
		StringBuilder build = new StringBuilder();

		build.append(super.getImportSource());
		
		for(ModuleSource src : modules) {
			build.append('\n');
			build.append("export function "+src.getName()+"() {\n");
			build.append(src.getCode().toString());
			build.append("}\n");
		}
		return build;
	}

	public void addModule(ModuleSource src) {
		modules.add(src);
	}

	public ModuleSource getModule(String name) {
		for(ModuleSource s : modules) {
			if (s.getName().equals(name)) {
				return s;
			}
		}
		return null;
	}

}

