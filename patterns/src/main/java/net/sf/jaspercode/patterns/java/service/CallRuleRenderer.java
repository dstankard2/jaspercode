package net.sf.jaspercode.patterns.java.service;

import java.util.List;
import java.util.Map;

import net.sf.jaspercode.api.AttribEntry;
import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.JasperUtils;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.types.ServiceOperation;
import net.sf.jaspercode.langsupport.java.JavaCode;
import net.sf.jaspercode.langsupport.java.JavaUtils;
import net.sf.jaspercode.langsupport.java.types.JavaVariableType;
import net.sf.jaspercode.patterns.xml.java.service.CallRuleOperation;

public class CallRuleRenderer extends OperationRenderer {

	private CallRuleOperation op = null;
	
	public CallRuleRenderer(ProcessorContext ctx,CallRuleOperation op) {
		super(ctx);
		this.op = op;
	}
	
	@Override
	public JavaCode getCode(CodeExecutionContext execCtx) throws JasperException {
		JavaCode code = new JavaCode();
		String result = op.getResult();
		String rule = op.getRule();
		String paramString = op.getParams();
		String serviceRef = null;
		ServiceOperation o = null;
		List<AttribEntry> params = null;
		Map<String,String> paramMap = JasperUtils.readParametersAsMap(paramString, ctx);
		
		if ((result.trim().length()>0) && (result.indexOf('.') < 0 ) && (!execCtx.getVariableNames().contains(result))) {
			String resultTypeName = ctx.getSystemAttribute(result);
			if (resultTypeName==null) {
				throw new JasperException("Couldn't find a system attribute called '"+result+"' - it must be defined already");
			}
			ctx.dependOnSystemAttribute(result);
			JavaVariableType resultType = execCtx.getType(JavaVariableType.class, resultTypeName);
			ctx.dependOnVariableType(resultTypeName);
			JavaUtils.append(code, resultType.declare(result, execCtx));
			execCtx.addVariable(result, resultTypeName);
		}
		
		int i = rule.indexOf('.');
		if (i<0) {
			throw new JasperException("Couldn't invoke a rule called '"+rule+"' - it must be of format '<serviceRef>.<rule>'");
		}
		serviceRef = rule.substring(0, i);
		//serviceRule = rule.substring(i+1);
		//type = JasperUtils.getTypeForSystemAttribute(JavaServiceType.class, serviceRef, ctx);

		params = JasperUtils.readParametersAsList(paramString, ctx);
		o = JavaUtils.findRule(rule, params, ctx, execCtx);
		/*
		List<ServiceOperation> serviceOps = type.getOperations(serviceRule);
		if (serviceOps.size()==1) {
			o = serviceOps.get(0);
		}
		else if (serviceOps.size()>1) {
			if (params.size()==0) throw new JasperException("Service '"+serviceRef+"' had multiple rules called '"+serviceRule+"' and couldn't determine which to use");
			for(ServiceOperation curr : serviceOps) {
				boolean fit = true;
				for(Entry<String,String> entry : params.entrySet()) {
					if (!curr.getParamNames().contains(entry.getKey())) {
						fit = false;
						break;
					}
				}
				if (fit) {
					if (o != null) {
						throw new JasperException("Service '"+serviceRef+"' had multiple rules called '"+serviceRule+"' and couldn't determine which to use");
					}
					o = curr;
				}
			}
		}
		*/

		JavaUtils.append(code, JavaUtils.addServiceToExecutionContext(serviceRef, execCtx, ctx));
		JavaUtils.append(code, JavaUtils.callJavaOperation(result, serviceRef, o, execCtx, paramMap));

		return code;
	}

}
