package net.sf.jaspercode.langsupport.java.types.impl;

import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.langsupport.java.JavaCode;
import net.sf.jaspercode.langsupport.java.types.JavaVariableTypeBase;

public class BooleanJavaType extends JavaVariableTypeBase {

	public BooleanJavaType() {
		super("Boolean",null,null);
	}

	@Override
	public String getName() {
		return "boolean";
	}

	@Override
	public String getImport() {
		return null;
	}

	@Override
	public String getClassName() {
		return "Boolean";
	}

	@Override
	public JavaCode declare(String name, CodeExecutionContext execCtx) {
		return new JavaCode("Boolean "+name+" = null;\n");
	}

}
