package net.sf.jaspercode.langsupport.javascript.modules;

import java.util.ArrayList;
import java.util.List;

import net.sf.jaspercode.api.SourceFile;
import net.sf.jaspercode.langsupport.javascript.JavascriptSourceFile;
import net.sf.jaspercode.langsupport.javascript.ModuleImport;

public class ModuleSourceFile extends JavascriptSourceFile {

	private List<ModuleSource> modules = new ArrayList<>();
	
	public ModuleSourceFile() {
		super();
	}

	@Override
	public StringBuilder getSource() {
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

	@Override
	public SourceFile copy() {
		// TODO: Finish
		ModuleSourceFile ret = new ModuleSourceFile();
		
		ret.setPath(getPath());
		for(ModuleImport im : this.importedModules) {
			ret.addModule(im);
		}
		for(ModuleSource mod : this.modules) {
			ret.addModule(mod.copy());
		}

		return ret;
	}

}

