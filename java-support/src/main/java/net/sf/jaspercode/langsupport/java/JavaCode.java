package net.sf.jaspercode.langsupport.java;

import java.util.ArrayList;
import java.util.List;

import net.sf.jaspercode.api.Code;

public class JavaCode implements Code {
	List<String> imports = new ArrayList<>();
	StringBuilder code = new StringBuilder();
	
	public JavaCode() {
	}

	public JavaCode(String code,String... imports) {
		this.code.append(code);
		for(String i : imports) {
			addImport(i);
		}
	}

	public List<String> getImports() {
		return imports;
	}
	public void addImport(String s) {
		if (!imports.contains(s)) {
			imports.add(s);
		}
	}

	@Override
	public String getCodeText() {
		return code.toString();
	}

	@Override
	public void appendCodeText(String s) {
		this.code.append(s);
	}

	@Override
	public void append(Code append) {
		if (!(append instanceof JavaCode)) {
			return;
		}
		JavaCode other = (JavaCode)append;
		this.appendCodeText(other.getCodeText());
		for(String im : other.getImports()) {
			addImport(im);
		}
	}
	
}
