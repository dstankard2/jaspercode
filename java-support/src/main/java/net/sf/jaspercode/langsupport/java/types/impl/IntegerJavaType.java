package net.sf.jaspercode.langsupport.java.types.impl;

import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.langsupport.java.JavaCode;
import net.sf.jaspercode.langsupport.java.types.JavaVariableTypeBase;

public class IntegerJavaType extends JavaVariableTypeBase {

	public IntegerJavaType() {
		super("integer",null,null);
	}

	@Override
	public String getName() {
		return "integer";
	}

	@Override
	public String getImport() {
		return null;
	}

	@Override
	public String getClassName() {
		return "Integer";
	}

	@Override
	public JavaCode declare(String name, CodeExecutionContext execCtx) {
		return new JavaCode("Integer "+name+" = null;\n");
	}

}
