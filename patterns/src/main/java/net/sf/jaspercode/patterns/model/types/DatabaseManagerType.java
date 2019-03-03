package net.sf.jaspercode.patterns.model.types;

import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.langsupport.java.JavaCode;
import net.sf.jaspercode.langsupport.java.types.JavaVariableType;

public interface DatabaseManagerType extends JavaVariableType {

	public JavaCode rollback(String dbVarRef,CodeExecutionContext execCtx);

	public JavaCode commit(String dbVarRef,CodeExecutionContext execCtx);

	public JavaCode close(String dbVarRef,CodeExecutionContext execCtx);

	public JavaCode setReadOnly(String dbVarRef,CodeExecutionContext execCtx);

}
