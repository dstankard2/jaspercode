package net.sf.jaspercode.langsupport.javascript.types;

import net.sf.jaspercode.api.Code;
import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.langsupport.javascript.JavascriptCode;

public class ModuleType extends JavascriptType {

	private String moduleName = null;
	private String sourceFile = null;
	
	public ModuleType(String name) {
		super(name,true,null);
		this.moduleName = name;
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
