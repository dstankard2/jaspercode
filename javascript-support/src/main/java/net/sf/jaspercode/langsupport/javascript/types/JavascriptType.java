package net.sf.jaspercode.langsupport.javascript.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.Code;
import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.types.DataObjectType;
import net.sf.jaspercode.api.types.VariableType;
import net.sf.jaspercode.langsupport.javascript.JavascriptCode;

public abstract class JavascriptType implements VariableType,DataObjectType {

	protected String name = null;
	protected BuildContext bctx = null;
	protected Map<String,String> properties = new HashMap<>();
	protected ProcessorContext ctx;
	//private List<String> superTypes = new ArrayList<>();

	public void addAttribute(String name,String type) {
		properties.put(name, type);
	}

	public JavascriptType(String name) {
		this.name = name;
		if (ctx!=null) {
			this.bctx = ctx.getBuildContext();
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Code declare(String name, CodeExecutionContext execCtx) throws JasperException {
		return new JavascriptCode("var "+name+";\n");
	}

	@Override
	public BuildContext getBuildContext() {
		return bctx;
	}

	@Override
	public String getCodeToRetrieveAttribute(String varName, String attribName, String targetType,
			CodeExecutionContext execCtx) throws IllegalArgumentException, JasperException {
		return varName+'.'+attribName;
	}

	@Override
	public String getCodeToSetAttribute(String varName, String attribName, String valueString,
			CodeExecutionContext execCtx) throws JasperException {
		return varName+"."+attribName+" = "+valueString;
	}

	@Override
	public String getAttributeType(String attrib) throws JasperException {
		return properties.get(attrib);
	}

	@Override
	public List<String> getAttributeNames() {
		List<String> ret = new ArrayList<>();
		
		for(String p : properties.keySet()) {
			ret.add(p);
		}
		return ret;
	}

	@Override
	public Code instantiate(String varName) {
		return new JavascriptCode(varName+" = {};\n");
	}

	@Override
	public List<String> getSuperTypes() {
		return new ArrayList<>();
	}

}

