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
import net.sf.jaspercode.langsupport.javascript.JavascriptUtils;

public abstract class JavascriptType implements VariableType,DataObjectType {

	protected boolean module = false;
	protected String sourcePath = null;
	protected String name = null;
	protected BuildContext bctx = null;
	protected Map<String,String> properties = new HashMap<>();
	protected ProcessorContext ctx;
	//private List<String> superTypes = new ArrayList<>();

	public void addAttribute(String name,String type) {
		properties.put(name, type);
	}

	public JavascriptType(String name,boolean module,ProcessorContext ctx) {
		this.ctx = ctx;
		if (module) {
			try {
				this.sourcePath = JavascriptUtils.getModuleSource(ctx).getPath();
			} catch(JasperException e) { }
		}
		this.module = module;
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

	public boolean isModule() {
		return module;
	}

	public void setModule(boolean module) {
		this.module = module;
	}

	public String getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
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

	/* TODO: Support inheritance in Javascript objects
	public List<String> getSuperTypes() {
		return superTypes;
	}
	*/

}

