package net.sf.jaspercode.langsupport.java.types.impl;

import java.util.ArrayList;
import java.util.List;

import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.JasperUtils;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.langsupport.java.JavaCode;
import net.sf.jaspercode.langsupport.java.types.ServiceLocator;

public class ServiceLocatorImpl implements ServiceLocator {
	private List<String> services = new ArrayList<>();
	String className = null;
	String im = null;
	BuildContext buildCtx = null;
	String name = null;

	public ServiceLocatorImpl(String name,String className,String pkg,BuildContext buildCtx) {
		this.buildCtx = buildCtx;
		this.name = name;
		this.className = className;
		this.im = pkg + '.' + className;
	}

	@Override
	public String getImport() {
		return im;
	}

	@Override
	public String getClassName() {
		return className;
	}

	@Override
	public JavaCode declare(String name, CodeExecutionContext execCtx) throws JasperException {
		return new JavaCode(getClassName()+" "+name+";\n",getImport());
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public JavaCode instantiate(String name) {
		return new JavaCode(name + " = new "+getClassName()+"();\n");
	}

	@Override
	public BuildContext getBuildContext() {
		return buildCtx;
	}

	@Override
	public String instantiate() {
		return "new "+getImport()+"()";
	}

	@Override
	public List<String> getAvailableServices() {
		return services;
	}
	
	public void addService(String ref) {
		services.add(ref);
	}

	@Override
	public String getService(String locatorRef, String serviceName, CodeExecutionContext execCtx)
			throws JasperException {
		String typeName = JasperUtils.getUpperCamelName(serviceName);
		return locatorRef+".get"+typeName+"()";
	}

}
