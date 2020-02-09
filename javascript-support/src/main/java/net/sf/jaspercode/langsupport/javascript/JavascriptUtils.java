package net.sf.jaspercode.langsupport.javascript;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.types.ServiceOperation;
import net.sf.jaspercode.langsupport.javascript.modules.ModuleFunction;
import net.sf.jaspercode.langsupport.javascript.modules.ModuleSourceFile;

public class JavascriptUtils {

	public static String getModuleRelativePath(String path,String modulePath) {
		if (!path.startsWith("/")) path = "/" + path;
		if (!modulePath.startsWith("/")) modulePath = "/" + modulePath;
		StringBuilder build = new StringBuilder();
		
		build.append(".");
		int i = modulePath.lastIndexOf('/');
		String moduleFileName = modulePath.substring(i+1);
		String moduleFileDir = modulePath.substring(1,i);
		String pathDir = path.substring(1, path.lastIndexOf('/'));
		
		// TODO: Implement real solution
		if (moduleFileDir.startsWith(pathDir+'/')) {
			build.append('/').append(moduleFileDir.substring(pathDir.length()+1));
		} else if (moduleFileDir.equals(pathDir)) {
			// no-op
			build.append("");
		} else {
			int i2 = pathDir.indexOf('/');
			while(i2>=0) {
				build.append("/..");
				i2 = pathDir.indexOf('/',i2+1);
				//if (i2>=0) build.append('/');
			}
			//build.append("..");
			build.append("/../"+moduleFileDir);
		}
		
		build.append("/").append(moduleFileName);
		
		return build.toString();
	}
	public static String getModulePath(ProcessorContext ctx) throws JasperException {
		String srcPath = getModuleSourceFilePath(ctx);
		String ret = null;
		String base = ctx.getProperty("javascript.modules.srcRoot");
		
		if (base==null) {
			throw new JasperException("Javascript modules require config property 'javascript.modules.srcRoot'");
		}
		if (srcPath.startsWith(base)) {
			ret = srcPath.substring(base.length());
		}
		
		return ret;
	}
	
	protected static String getModuleSourceFilePath(ProcessorContext ctx) throws JasperException {
		String base = ctx.getBuildContext().getOutputRootPath("js");
		String path = ctx.getProperty("javascript.module.source");
		String fullPath = null;
		
		if (path==null) {
			throw new JasperException("Couldn't find required property 'javascript.module.source'");
		}
		if ((base.endsWith("/")) || (path.startsWith("/"))) {
			fullPath = base+path;
		} else {
			fullPath = base+'/'+path;
		}
		return fullPath;
	}

	public static ModuleSourceFile getModuleSource(ProcessorContext ctx) throws JasperException {
		ModuleSourceFile ret = null;

		String fullPath = getModuleSourceFilePath(ctx);

		ctx.getLog().info("Found Javascript module sourcefile as '"+fullPath+"'");
		ret = (ModuleSourceFile)ctx.getSourceFile(fullPath);
		if (ret==null) {
			ret = new ModuleSourceFile(getModulePath(ctx));
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

	public static boolean isJavascriptDebug(ProcessorContext ctx) {
		boolean ret = false;
		String val = ctx.getProperty("javascript.debug");
		if ((val.equalsIgnoreCase("true")) || (val.equalsIgnoreCase("T")) || (val.equalsIgnoreCase("Y"))) {
			ret = true;
		}
		return ret;
	}

	// If localName is true, a _ is prepended to the name
	public static String fnSource(ModuleFunction fn, String namePrepend) {
		StringBuilder b = new StringBuilder();
		String name = fn.getName();

		b.append("function ");
		if (namePrepend!=null) b.append(namePrepend);
		b.append(name+"(");
		boolean firstParam = true;
		for(String p : fn.getParamNames()) {
			if (firstParam) firstParam = false;
			else b.append(',');
			b.append(p);
		}
		b.append(") {\n");
		b.append(fn.getCode().getCodeText());
		b.append("\n}\n");

		return b.toString();
	}

}

