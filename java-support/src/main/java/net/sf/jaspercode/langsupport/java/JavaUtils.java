package net.sf.jaspercode.langsupport.java;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.forge.roaster.model.Type;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import net.sf.jaspercode.api.AttribEntry;
import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.JasperUtils;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.types.ServiceOperation;
import net.sf.jaspercode.langsupport.java.types.JavaVariableType;
import net.sf.jaspercode.langsupport.java.types.ServiceLocator;
import net.sf.jaspercode.langsupport.java.types.impl.JavaDataObjectType;
import net.sf.jaspercode.langsupport.java.types.impl.JavaServiceType;

public class JavaUtils {

	public static final String CONFIG_PROPERTY_JAVA_ROOT_PACKAGE = "java.rootPackage";
	
	public static String getRootPackage(ProcessorContext ctx) throws JasperException {
		String val = ctx.getProperty(CONFIG_PROPERTY_JAVA_ROOT_PACKAGE);
		if (val==null) {
			throw new JasperException("Couldn't find root package configuration property '"+CONFIG_PROPERTY_JAVA_ROOT_PACKAGE+"'");
		}
		return val;
	}
	
	public static String getJavaPackage(JavaComponent component,ProcessorContext ctx) throws JasperException {
		return getRootPackage(ctx)+'.'+component.getPkg();
	}

	private static String getJavaSourcePath(String cannonicalName,ProcessorContext ctx) throws JasperException {
		String ret = null;
		
		ret = ctx.getBuildContext().getOutputRootPath("java") + '/';
		ret = ret + cannonicalName.replace('.', '/')+".java";
		
		return ret;
	}
	/**
	 * Gets the specified source file, creating it if it's not there.
	 * @param cannonicalName
	 * @param ctx
	 * @return
	 */
	public static JavaClassSourceFile getClassSourceFile(String cannonicalName,ProcessorContext ctx) throws JasperException {
		return getClassSourceFile(cannonicalName,ctx,true);
	}
	
	/**
	 * Gets the specified source file, creating it if it's not there.
	 * @param cannonicalName
	 * @param ctx
	 * @return
	 */
	public static JavaClassSourceFile getClassSourceFile(String cannonicalName,ProcessorContext ctx, boolean create) throws JasperException {
		JavaClassSourceFile ret = null;

		ret = (JavaClassSourceFile)ctx.getSourceFile(getJavaSourcePath(cannonicalName,ctx));
		if ((ret==null) && (create)) {
			ret = new JavaClassSourceFile(ctx);
			int i = cannonicalName.lastIndexOf('.');
			String pkg = cannonicalName.substring(0, i);
			String className = cannonicalName.substring(i+1);
			ret.getSrc().setPackage(pkg);
			ret.getSrc().setName(className);
			ctx.addSourceFile(ret);
		}
		
		return ret;
	}
	
	public static JavaCode append(JavaCode code,JavaCode append) {
		code.appendCodeText(append.getCodeText());
		for(String im : append.getImports()) {
			if (!code.getImports().contains(im)) {
				code.addImport(im);
			}
		}
		return code;
	}
	
	public static void addServiceOperation(ServiceOperation op,JavaCode code,JavaClassSource cl,ProcessorContext ctx) throws JasperException {
		MethodSource<JavaClassSource> method = cl.addMethod().setName(op.getName()).setBody(code.getCodeText()).setPublic();

		if (code!=null) {
			for(String im : code.getImports()) {
				cl.addImport(im);
			}
		}

		if (op.getReturnType()!=null) {
			String t = op.getReturnType();
			JavaVariableType type = JasperUtils.getType(JavaVariableType.class, t, ctx);
			if (type.getImport()!=null) cl.addImport(type.getImport());
			if (t.indexOf("list/")==0) {
				JavaVariableType eltType = JasperUtils.getType(JavaVariableType.class, t.substring(5), ctx);
				method.setReturnType("List<"+eltType.getClassName()+">");
			} else {
				method.setReturnType(type.getClassName());
			}
		}
		for(String p : op.getParamNames()) {
			String t = op.getParamType(p);
			JavaVariableType type = JasperUtils.getType(JavaVariableType.class, t, ctx);
			if (type.getImport()!=null) cl.addImport(type.getImport());
			method.addParameter(type.getClassName(), p);
		}

	}
	
	public static void addProperty(JavaClassSourceFile src,String name,String typeName,ProcessorContext ctx) throws JasperException {
		JavaVariableType type = JasperUtils.getType(JavaVariableType.class, typeName, ctx);
		src.addImport(type);
		src.getSrc().addProperty(type.getClassName(), name);
	}
	
	public static JavaCode addServiceToExecutionContext(String ref,CodeExecutionContext execCtx,ProcessorContext ctx) throws JasperException {
		JavaCode ret = null;
		String typeName = ctx.getSystemAttribute(ref);

		if (execCtx.getVariableType(ref)!=null) {
			return new JavaCode();
		}
		
		if (typeName==null) {
			throw new JasperException("Couldn't recognize service reference '"+ref+"'");
		}
		JavaServiceType type = JasperUtils.getType(JavaServiceType.class, typeName, ctx);
		ret = type.declare(ref, execCtx);
		execCtx.addVariable(ref, typeName);

		append(ret,type.instantiate(ref));
		
		return ret;
	}

	public static JavaCode callJavaOperation(String resultName,String objName,ServiceOperation op,CodeExecutionContext execCtx,Map<String,String> explicitParams) throws JasperException {
		return callJavaOperation(resultName,objName,op,execCtx,explicitParams,true);
	}

	public static JavaCode callJavaOperation(String resultName,String objName,ServiceOperation op,CodeExecutionContext execCtx,Map<String,String> explicitParams,boolean addSemicolon) throws JasperException {
		JavaCode ret = new JavaCode();
		JavaCode invoke = new JavaCode();
		
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
				throw new JasperException("Couldn't find parameter '"+p+"' in current code execution context");
			}

		}
		invoke.appendCodeText(")");
		if ((resultName!=null) && (resultName.trim().length()>0)) {
			invoke = JavaUtils.set(resultName, invoke.getCodeText(), execCtx);
		} else if (addSemicolon) {
			invoke.appendCodeText(";");
		}
		invoke.appendCodeText("\n");
		JavaUtils.append(ret, invoke);

		return ret;
	}
	
	/**
	 * Ensure that the given service ref exists in the current code execution context by creating it if necessary
	 * @param ref Reference to the service.
	 * @param type A subclass of JavaServiceType or ServiceLocator
	 * @param execCtx Current code execution context
	 * @param ctx Processor Conntext
	 * @return Code to ensure the service exists.
	 * @throws JasperException
	 */
	public static JavaCode serviceInstance(String ref,JavaVariableType type,CodeExecutionContext execCtx,ProcessorContext ctx) throws JasperException {
		JavaCode ret = new JavaCode();

		// If the variable is already there, then it is already initialized.
		if (execCtx.getTypeForVariable(ref)==null) {
			ret = type.declare(ref, execCtx);
			execCtx.addVariable(ref, type.getName());
			if (type instanceof ServiceLocator) {
				ServiceLocator loc = (ServiceLocator)type;
				JavaUtils.append(ret, loc.instantiate(ref));
			} else if (type instanceof JavaServiceType) {
				JavaServiceType s = (JavaServiceType)type;
				JavaUtils.append(ret, s.instantiate(ref));
			} else {
				throw new JasperException("Couldn't create an instance of service type '"+type.getName()+"' because it isn't a service or service locator");
			}
		}
		
		return ret;
	}

	/**
	 * In case a code executionContext needs to be stored because it will be accessed by multiple processors.
	 * @param className
	 * @param methodName
	 * @param ctx
	 * @return
	 */
	public static CodeExecutionContext getCodeExecutionContext(String className,String methodName, ProcessorContext ctx,boolean createIfNull) {
		CodeExecutionContext ret = null;
		String name = "CodeExecutionContext_"+className+'_'+methodName;
		Object obj = ctx.getObject(name);
		
		if (obj==null) {
			if (createIfNull) {
				ret = new CodeExecutionContext(ctx);
				ctx.setObject(name, ret);
			}
		} else {
			ret = (CodeExecutionContext)obj;
		}
		
		return ret;
	}

	public static String getTypeName(Type<JavaClassSource> cl) {
		String ret = null;
		
		if ((cl.getQualifiedName().equals("java.util.List")) && (cl.isParameterized())) {
			String name = cl.getTypeArguments().get(0).getQualifiedName();
			ret = "list/"+getTypeName(name);
		}
		else ret = getTypeName(cl.getQualifiedName());
		
		return ret;
	}

	public static String getTypeName(String className) {
		String ret = null;
		
		if (className.equals("java.lang.String")) ret = "string";
		else if (className.equals("java.lang.Boolean")) ret = "boolean";
		else if (className.equals("java.lang.Integer")) ret = "integer";
		else if (className.equals("java.lang.Double")) ret = "double";
		else if (className.startsWith("java.lang.Object")) ret = "object";
		else if (className.startsWith("java.lang.Long")) ret = "longint";
		else if (className.startsWith("java.util.Date")) ret = "date";
		else if (className.startsWith("java.sql.Date")) ret = "date";
		else if (className.startsWith("java.sql.Timestamp")) ret = "datetime";
		else if (className.startsWith("java.util.List<")) {
			String subtype = className.substring(15);
			ret = "list/"+getTypeName(subtype.substring(0, subtype.length()-1));
		}
		else {
			int i = className.lastIndexOf('.');
			ret = className.substring(i+1);
		}
		
		return ret;
	}

	public static ServiceOperation findRule(String rule,List<AttribEntry> params, ProcessorContext ctx, CodeExecutionContext execCtx) throws JasperException {
		ServiceOperation op = null;
		String obj = JasperUtils.getObjectName(rule);
		String ruleName = JasperUtils.getRuleName(rule);
		JavaServiceType type = JasperUtils.getTypeForSystemAttribute(JavaServiceType.class, obj, ctx);
		List<ServiceOperation> ops = type.getOperations(ruleName);

		if (ops.size()==0) {
			throw new JasperException("Couldn't find rule '"+rule+"'");
		} else if (ops.size()==1) {
			op = ops.get(0);
		} else {
			/* 
			 * For the service operations that match, find one that can be executed in the 
			 * current code execution context with the given explicit parameters.
			 */
			for(ServiceOperation o : ops) {
				boolean correctOp = true;
				for(String param : o.getParamNames()) {
					if (execCtx.getVariableType(param)!=null) continue;
					boolean found = false;
					for(AttribEntry ex : params) {
						if (ex.getName().equals(param)) {
							found = true;
							break;
						}
					}
					if (!found) {
						correctOp = false;
						break;
					}
				}
				if (correctOp) {
					op = o;
					break;
				}
			}
		}
		if(op==null) {
			throw new JasperException("Couldn't find rule "+rule+" with params = "+params);
		}
		return op;
	}

	public static JavaCode set(String ref, String valueString, CodeExecutionContext execCtx) throws JasperException {
		JavaCode ret = new JavaCode();
		boolean first = true;
		String b = "";
		//StringBuilder b = new StringBuilder();
		boolean nested = false;
		JavaDataObjectType t = null;

		String refs[] = ref.split("\\.");
		if (refs.length>1) nested = true;
		for(String r : refs) {
			boolean last = (r==refs[refs.length-1]);
			if ((first) && (nested)) {
				b = r;
				first = false;
				if (nested) {
					String typeName = execCtx.getVariableType(r);
					t = execCtx.getType(JavaDataObjectType.class, typeName);
				}
			} else if (!nested) {
				ret.appendCodeText(ref+" = "+valueString+";\n");
			} else {
				if (last) {
					ret.appendCodeText(t.getCodeToSetAttribute(b, r, valueString, execCtx));
					ret.appendCodeText(";");
				} else {
					if (t==null) {
						throw new JasperException("Couldn't set value of reference '"+ref+"'");
					}
					b = t.getCodeToRetrieveAttribute(b, r, null, execCtx);
					String attrTypeName = t.getAttributeType(r);
					JavaVariableType x = execCtx.getType(JavaVariableType.class, attrTypeName);
					if (x instanceof JavaDataObjectType) t = (JavaDataObjectType)x;
					else t = null;
				}
			}
		}
		
		return ret;
	}
	
	public static String getClassDisplayForList(String listType, ProcessorContext ctx) throws JasperException {
		String ret = "java.util.List";
		
		int i = listType.indexOf('/');
		if (i>0) {
			String eltTypeName = listType.substring(i+1);
			JavaVariableType eltType = JasperUtils.getType(JavaVariableType.class, eltTypeName, ctx);
			ret = ret + "<";
			if (eltType.getImport()!=null) {
				ret = ret + eltType.getImport();
			} else {
				ret = ret + eltType.getClassName();
			}
			ret = ret + ">";
		}
		
		return ret;
	}
	
	/*
	public static void addImports(JavaClassSourceFile src, JavaCode code) {
		for(String im : code.getImports()) {
			src.getJavaClassSource().addImport(im);
		}
	}
	*/
	
}

