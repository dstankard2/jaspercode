package net.sf.jaspercode.api.types;

import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.Code;
import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.exception.JasperException;

public interface VariableType {

	public String getName();
	public Code declare(String name,CodeExecutionContext execCtx) throws JasperException;
	public BuildContext getBuildContext();

}
