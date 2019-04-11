package net.sf.jaspercode.langsupport.java.types;

import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.types.VariableType;
import net.sf.jaspercode.langsupport.java.JavaCode;

public interface JavaVariableType extends VariableType {

	/**
	 * Fully qualified class name, or null if the package is "java.lang".
	 * @return
	 */
	public String getImport();

	/**
	 * The class name without the package.
	 * @return
	 */
	public String getClassName();

	public JavaCode declare(String name, CodeExecutionContext execCtx) throws JasperException;
	
}
