package net.sf.jaspercode.patterns.js.template.parsing.directives;

import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.patterns.js.template.parsing.AttributeDirectiveBase;
import net.sf.jaspercode.patterns.js.template.parsing.DirectiveContext;

@Plugin
public class LoopAttributeDirective extends AttributeDirectiveBase {

	@Override
	public int getPriority() { return 2; }
	
	@Override
	public String getAttributeName() {
		return "js-loop";
	}

	@Override
	public void generateCode(DirectiveContext ctx) throws JasperException {
		StringBuilder b = ctx.getCode();
		CodeExecutionContext execCtx = ctx.getExecCtx();
		
		String a = ctx.getTemplateAttributes().get("js-loop");
		int i = a.indexOf(" in ");
		String eltVar = a.substring(0, i).trim();
		String list = a.substring(i+4).trim();
		
		String indexVar = null;
		
		final String indexIndicator = " index ";
		if (list.indexOf(indexIndicator)>0) {
			int indicatorIndex = list.indexOf(indexIndicator);
			indexVar = list.substring(indicatorIndex+indexIndicator.length());
			list = list.substring(0, list.indexOf(indexIndicator));
			ctx.getProcessorContext().getLog().warn("Found indexVar as "+indexVar);
			if (execCtx.getVariableType(indexVar)!=null) {
				throw new JasperException("Tried to define index variable '"+indexVar+"' but it already exists in the code execution context");
			}
		}

		if (execCtx.getVariableType(eltVar)!=null) {
			throw new JasperException("Couldn't create loop variable '"+eltVar+"' as there is already a variable in the current execution context with that name");
		}
		String in = ctx.newVarName("_i","object",execCtx);
		
		String func = ctx.newVarName("_lf", "function", execCtx);
		execCtx.addVariable(func, "function");
		CodeExecutionContext newCtx = new CodeExecutionContext(execCtx);
		String args = eltVar;
		if (indexVar!=null) {
			args = args + ',' + indexVar;
			newCtx.addVariable(indexVar, "integer");
		}
		b.append("var "+func+" = function("+args+"){\n");
		newCtx.addVariable(eltVar, "object");
		// TODO: Pass newCtx or existing one?  Should be new one...
		ctx.continueRenderElement(newCtx);
		b.append("}\n");
		b.append("try {\n");
		b.append("for(var "+in+"=0;"+in+"<"+list+".length;"+in+"++) {\n");
		args = eltVar;
		if (indexVar!=null) {
			args = args + ','+in;
		}
		b.append("var "+eltVar+" = "+list+"["+in+"];\n");
		b.append(func+"("+args+");\n}\n");
		b.append("}catch(_err){}\n");
	}

}

