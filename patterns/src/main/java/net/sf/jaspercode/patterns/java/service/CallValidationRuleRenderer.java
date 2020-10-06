package net.sf.jaspercode.patterns.java.service;

import java.util.HashMap;
import java.util.List;

import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.JasperUtils;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.types.ServiceOperation;
import net.sf.jaspercode.langsupport.java.JavaCode;
import net.sf.jaspercode.langsupport.java.JavaUtils;
import net.sf.jaspercode.langsupport.java.types.impl.JavaServiceType;
import net.sf.jaspercode.patterns.xml.java.service.CallValidationRuleOperation;

public class CallValidationRuleRenderer extends NestingOperationRenderer {
	CallValidationRuleOperation op = null;
	ProcessorContext ctx = null;

	public CallValidationRuleRenderer(ProcessorContext ctx,CallValidationRuleOperation op) {
		super(ctx);
		this.op = op;
		this.ctx = ctx;
	}

	@Override
	public JavaCode getCode(CodeExecutionContext execCtx) throws JasperException {
		JavaCode ret = null;
		JavaServiceType type = null;
		String service = op.getRule();
		
		String objName = JasperUtils.getObjectName(service);
		String ruleName = JasperUtils.getRuleName(service);

		ret = JavaUtils.addServiceToExecutionContext(objName, execCtx, ctx);
		
		String typeName = ctx.getSystemAttribute(objName);
		type = JasperUtils.getType(JavaServiceType.class, typeName, ctx);
		
		List<ServiceOperation> ops = type.getOperations(ruleName);
		if (ops.size()==0) {
			throw new JasperException("Could not find validation rule '"+service+"'");
		} else if (ops.size()>1) {
			throw new JasperException("Validation rule does not support overloaded method '"+service+"' as it cannot determine which method to invoke");
		}
		ServiceOperation operation = ops.get(0);
		HashMap<String,String> explicitParams = new HashMap<String,String>();
		if (op.getParams().trim().length()>0) {
			explicitParams = JasperUtils.readParametersAsMap(op.getParams(), ctx);
		}
		if ((operation.getReturnType()==null) || (!operation.getReturnType().equals("string"))) {
			throw new JasperException("A validation rule must return a String");
		}

		JavaUtils.append(ret, JavaUtils.callJavaOperation("returnValue.validationError"
				+ "", objName, operation, execCtx, explicitParams));

		ret.appendCodeText("if (returnValue.getValidationError()==null) {\n");

		return ret;
	}

	@Override
	public JavaCode endingCode(CodeExecutionContext execCtx) throws JasperException {
		JavaCode ret = new JavaCode("}\n");
		return ret;
	}

}
