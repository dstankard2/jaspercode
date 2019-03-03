package net.sf.jaspercode.langsupport.java.types.impl;

import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.langsupport.java.JavaCode;
import net.sf.jaspercode.langsupport.java.types.JavaVariableTypeBase;

public class StringJavaType extends JavaVariableTypeBase {

	public StringJavaType() {
		super("String",null,null);
	}

	@Override
	public String getName() {
		return "string";
	}

	@Override
	public String getImport() {
		return null;
	}

	@Override
	public String getClassName() {
		return "String";
	}

	@Override
	public JavaCode declare(String name, CodeExecutionContext execCtx) {
		return new JavaCode("String "+name+" = null;\n");
	}

}
