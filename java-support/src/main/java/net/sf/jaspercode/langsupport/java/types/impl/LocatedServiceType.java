package net.sf.jaspercode.langsupport.java.types.impl;

import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.langsupport.java.JavaCode;

public class LocatedServiceType extends JavaServiceType {
	private String locatorImport = null;
	private String locatorClass = null;
	
	public LocatedServiceType(String pkg,String className,BuildContext buildCtx,String locatorImport,String locatorClass) {
		super(className, pkg+'.'+className, buildCtx);
		this.locatorImport = locatorImport;
		this.locatorClass = locatorClass;
	}
	
	public String getLocatorClass() {
		return locatorClass;
	}
	
	protected JavaCode locateService(String varName,boolean completeCodeLine) {
		JavaCode ret = new JavaCode();

		ret.addImport(locatorImport);
		ret.addImport(getImport());
		if (varName!=null) {
			ret.appendCodeText(varName+" = ");
		}
		ret.appendCodeText("new "+locatorClass+"().get"+getName()+"()");
		if (completeCodeLine) {
			ret.appendCodeText(";\n");
		}

		return ret;
	}
	
	public String getAnonymousInstance() {
		JavaCode code = locateService(null,false);
		return code.getCodeText();
	}

	@Override
	public JavaCode instantiate(String ref) {
		return locateService(ref,true);
	}

}
