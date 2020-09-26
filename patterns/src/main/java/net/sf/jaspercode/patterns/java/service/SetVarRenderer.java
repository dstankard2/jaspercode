package net.sf.jaspercode.patterns.java.service;

import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.JasperUtils;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.langsupport.java.JavaCode;
import net.sf.jaspercode.langsupport.java.JavaUtils;
import net.sf.jaspercode.langsupport.java.types.JavaVariableType;
import net.sf.jaspercode.patterns.xml.java.service.SetVarOperation;

public class SetVarRenderer extends OperationRenderer {

	private SetVarOperation op = null;
	
	public SetVarRenderer(ProcessorContext ctx,SetVarOperation op) {
		super(ctx);
		this.op = op;
	}
	
	public JavaCode getCode(CodeExecutionContext execCtx) throws JasperException {
		JavaCode ret = new JavaCode();
		
		String name = op.getName();
		String type = op.getType();
		String value = op.getValue();
		
		if ((name==null) || (name.trim().length()==0)) {
			throw new JasperException("Service operation setVar requires an attribute 'name'");
		}
		if (name.indexOf('.')>=0) {
			throw new JasperException("Service operation setVar requires a variable name to declare");
		}
		
		if (type==null) {
			type = ctx.getSystemAttribute(name);
			if (type==null) {
				throw new JasperException("Could not determine type for variable '"+name+"'");
			}
		} else {
			if ((ctx.getSystemAttribute(name)!=null) && (!type.equals(ctx.getSystemAttribute(name)))) {
				throw new JasperException("Could not create variable '"+name+"' because its declared type is different from a pre-existing system attribute");
			}
		}
		
		if (execCtx.getTypeForVariable(name)!=null) {
			throw new JasperException("Cannot declare a variable named '"+name+"' because there is already one with that name in this rule");
		}

		JavaVariableType varType = JasperUtils.getType(JavaVariableType.class, type, ctx);
		if (varType==null) {
			throw new JasperException("Did not recognize type '"+type+"' as a valid Java type");
		}
		String val = null;
		if (value!=null) {
			val = JasperUtils.evaluateReference(value, execCtx);
		}
		execCtx.addVariable(name, type);
		JavaUtils.append(ret, varType.declare(name, execCtx));
		if (val!=null) {
			ret.appendCodeText(name+" = "+val+";\n");
		}

		return ret;
	}
	
}
