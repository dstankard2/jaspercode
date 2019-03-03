package net.sf.jaspercode.patterns.model;

import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.langsupport.java.JavaCode;
import net.sf.jaspercode.langsupport.java.types.JavaVariableType;
import net.sf.jaspercode.patterns.model.types.impl.EntityManagerType;

public interface EntityManagerLocator extends JavaVariableType {

	public JavaCode getEntityManager(String var,CodeExecutionContext execCtx);

	public JavaCode releaseEntityManager(String var,CodeExecutionContext execCtx);

	public EntityManagerType getEntityManagerType();
	
}

