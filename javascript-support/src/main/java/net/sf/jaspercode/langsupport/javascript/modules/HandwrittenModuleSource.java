package net.sf.jaspercode.langsupport.javascript.modules;

public class HandwrittenModuleSource implements ModuleSource {
	private StringBuilder code = new StringBuilder();
	private String name = null;

	public HandwrittenModuleSource(String name) {
		this.name = name;
	}

	public StringBuilder getCode() {
		return code;
	}
	public void setCode(StringBuilder code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public ModuleSource copy() {
		HandwrittenModuleSource ret = new HandwrittenModuleSource(name);
		ret.code = new StringBuilder(code.toString());
		return ret;
	}

}

