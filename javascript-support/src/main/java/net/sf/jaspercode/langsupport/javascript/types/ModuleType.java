package net.sf.jaspercode.langsupport.javascript.types;

import net.sf.jaspercode.api.Code;
import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.langsupport.javascript.JavascriptCode;

public class ModuleType extends JavascriptServiceType {

	private String moduleName = null;
	private String sourceFile = null;
	private String webPath = null;
	private ExportedModuleType exportType = null;
	
	public ModuleType(String name,String webPath,ExportedModuleType exportType) {
		super(name);
		this.moduleName = name;
		this.webPath = webPath;
		this.exportType = exportType;
	}

	public ExportedModuleType getExportType() {
		return exportType;
	}

	public void setExportType(ExportedModuleType exportType) {
		this.exportType = exportType;
	}

	public String getWebPath() {
		return webPath;
	}

	public void setWebPath(String webPath) {
		this.webPath = webPath;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getSourceFile() {
		return sourceFile;
	}

	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}

	public Code declare(String name, CodeExecutionContext execCtx) throws JasperException {
		JavascriptCode ret = new JavascriptCode();
		ret.appendCodeText("import {"+moduleName+"} from '"+sourceFile+"';\n");
		ret.appendCodeText("var "+name+";\n");
		return ret;
	}

}
