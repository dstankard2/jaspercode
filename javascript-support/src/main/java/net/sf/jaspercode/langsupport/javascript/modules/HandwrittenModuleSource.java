package net.sf.jaspercode.langsupport.javascript.modules;

import net.sf.jaspercode.langsupport.javascript.types.ExportedModuleType;

public class HandwrittenModuleSource implements ModuleSource {
	private StringBuilder codeBuild = new StringBuilder();
	private String name = null;
	private ExportedModuleType exportType = null;

	public HandwrittenModuleSource(String name) {
		this.name = name;
	}

	public ExportedModuleType getExportType() {
		return exportType;
	}

	public void setExportType(ExportedModuleType exportType) {
		this.exportType = exportType;
	}

	public StringBuilder getCodeBuild() {
		return codeBuild;
	}
	public String getSource() {
		StringBuilder b = new StringBuilder();
		if (this.getExportType()==ExportedModuleType.CONST) {
			b.append("export const "+getName()+" {\n");
			b.append(codeBuild.toString());
			b.append("\n};\n");
		} else {
			b.append("export function "+getName()+"() {\n");
			b.append(codeBuild.toString());
			b.append("\n};\n");
		}
		return b.toString();
		//return codeBuild.toString();
	}
 	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public ModuleSource copy() {
		HandwrittenModuleSource ret = new HandwrittenModuleSource(name);
		ret.exportType = exportType;
		ret.codeBuild = new StringBuilder(codeBuild.toString());
		return ret;
	}

}

