package net.sf.jaspercode.langsupport.javascript;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.types.ServiceOperation;
import net.sf.jaspercode.langsupport.javascript.modules.ModuleSourceFile;

public class JavascriptUtils {

	public static ModuleSourceFile getModuleSource(ProcessorContext ctx) throws JasperException {
		ModuleSourceFile ret = null;
		String base = ctx.getBuildContext().getOutputRootPath("js");
		String path = ctx.getProperty("javascript.module.source");
		
		if (path==null) {
			throw new JasperException("Couldn't find required property 'javascript.module.source'");
		}
		String fullPath = base + '/' + path;

		ctx.getLog().info("Found Javascript module sourcefile as '"+fullPath+"'");
		ret = (ModuleSourceFile)ctx.getSourceFile(fullPath);
		if (ret==null) {
			ret = new ModuleSourceFile();
			ret.setPath(fullPath);
			ctx.addSourceFile(ret);
			ctx.getLog().info("Creating module source file.");
		}

		return ret;
	}
	
	public static JavascriptCode callJavascriptOperation(String resultName,String objName,ServiceOperation op,CodeExecutionContext execCtx,Map<String,String> explicitParams,boolean addSemicolon, boolean allParamsRequired) throws JasperException {
		JavascriptCode ret = new JavascriptCode();
		JavascriptCode invoke = new JavascriptCode();

		invoke.appendCodeText(objName+'.');
		if (explicitParams==null) {
			explicitParams = new HashMap<String,String>();
		}

		invoke.appendCodeText(op.getName()+"(");
		List<String> paramNames = op.getParamNames();
		boolean first = true;
		for(String p : paramNames) {
			if (first) first = false;
			else invoke.appendCodeText(",");
			if (explicitParams.get(p)!=null) {
				invoke.appendCodeText(explicitParams.get(p));
			} else if (execCtx.getTypeForVariable(p)!=null) {
				invoke.appendCodeText(p);
			} else {
				// Parameter was not found
				if (allParamsRequired) {
					throw new JasperException("Couldn't find parameter '"+p+"' in current code execution context");
				} else {
					invoke.appendCodeText("undefined");
				}
			}

		}
		invoke.appendCodeText(")");
		if ((resultName!=null) && (resultName.trim().length()>0)) {
			//JavascriptCode fullInvoke = new JavascriptCode();
			//fullInvoke.appendCodeText(s);
			//invoke = JavaUtils.set(resultName, invoke.getCodeText(), execCtx);
			invoke.appendCodeText(";");
			ret.appendCodeText(resultName + " = ");
		} else if (addSemicolon) {
			invoke.appendCodeText(";");
		}
		invoke.appendCodeText("\n");
		ret.append(invoke);
		
		return ret;
	}

}
