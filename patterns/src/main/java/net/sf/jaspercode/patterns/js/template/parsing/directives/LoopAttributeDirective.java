package net.sf.jaspercode.patterns.js.template.parsing.directives;

import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.annotation.Plugin;
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

		if (execCtx.getVariableType(eltVar)!=null) {
			throw new JasperException("Couldn't create loop variable '"+eltVar+"' as there is already a variable in the current execution context with that name");
		}
		/*
		//String type = DirectiveUtils.getReferenceType(list, execCtx);
		if (type==null) {
			throw new JasperException("Couldn't evaluate type for js-loop list variable '"+list+"'");
		}
		String listStr = DirectiveUtils.getValidReference(list, execCtx);
		if (type.startsWith("list/")) type = type.substring(5);
		//String parentNodes = ctx.newVarName("_n", "object", execCtx);
		*/
		//execCtx = new CodeExecutionContext(execCtx);
		String in = ctx.newVarName("_i","object",execCtx);
		
		CodeExecutionContext newCtx = new CodeExecutionContext(execCtx);
		String func = ctx.newVarName("_lf", "function", execCtx);
		b.append("var "+func+" = function("+eltVar+"){\n");
		newCtx.addVariable(eltVar, "object");
		ctx.continueRenderElement();
		b.append("}\n");
		b.append("try {\n");
		b.append("for(var "+in+"=0;"+in+"<"+list+".length;"+in+"++) {\n");
		b.append("var "+eltVar+" = "+list+"["+in+"];\n");
		b.append(func+"("+eltVar+");\n}\n");
		b.append("}catch(_err){}\n");
	}

}

