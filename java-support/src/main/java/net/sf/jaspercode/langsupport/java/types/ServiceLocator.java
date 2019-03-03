package net.sf.jaspercode.langsupport.java.types;

import java.util.List;

import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.langsupport.java.JavaCode;

public interface ServiceLocator extends JavaVariableType {

	public String instantiate();
	
	/**
	 * Returns a list of variable type names of services available.
	 * @return Variable type names
	 */
	public List<String> getAvailableServices();
	/**
	 * 
	 * @param factoryInstanceName
	 * @param serviceName
	 * @param serviceInstanceName
	 * @param execCtx
	 * @return
	 * @throws JavascribeException
	 */
	public String getService(String factoryInstanceRef,String serviceName,CodeExecutionContext execCtx) throws JasperException;

	public JavaCode instantiate(String varName);

}

