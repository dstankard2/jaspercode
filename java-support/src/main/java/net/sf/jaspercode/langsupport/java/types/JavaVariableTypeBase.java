package net.sf.jaspercode.langsupport.java.types;

import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.langsupport.java.JavaCode;

public abstract class JavaVariableTypeBase implements JavaVariableType {
	
	private String classname = null;
	private String im = null;
	private BuildContext buildCtx;
	
	public JavaVariableTypeBase(String classname,String im,BuildContext buildCtx) {
		this.classname = classname;
		this.im = im;
		this.buildCtx = buildCtx;
	}
	
	@Override
	public String getName() {
		return classname;
	}

	@Override
	public String getImport() {
		return im;
	}

	@Override
	public String getClassName() {
		return classname;
	}

	@Override
	public JavaCode declare(String name, CodeExecutionContext execCtx) throws JasperException {
		return new JavaCode(getClassName()+" "+name+" = null;\n",getImport());
	}

	@Override
	public BuildContext getBuildContext() {
		return buildCtx;
	}

}
