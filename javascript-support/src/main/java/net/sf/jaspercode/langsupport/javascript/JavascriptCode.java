package net.sf.jaspercode.langsupport.javascript;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import net.sf.jaspercode.api.Code;

public class JavascriptCode implements Code {

	private StringBuilder code = new StringBuilder();
	
	private List<Pair<String,String>> importedModules = new ArrayList<>();
	
	public JavascriptCode() {
	}

	public JavascriptCode(String code) {
		this.code.append(code);
	}

	@Override
	public String getCodeText() {
		return code.toString();
	}

	@Override
	public void appendCodeText(String s) {
		code.append(s);
	}

	@Override
	public void append(Code append) {
		if (append instanceof JavascriptCode) {
			JavascriptCode c = (JavascriptCode)append;
			appendCodeText(c.getCodeText());
		}
	}

	public StringBuilder getCode() {
		return code;
	}

	public void setCode(StringBuilder code) {
		this.code = code;
	}

	public List<Pair<String,String>> getImportedModules() {
		return importedModules;
	}

	public void setImportedModules(List<Pair<String,String>> importedModules) {
		this.importedModules = importedModules;
	}

}
