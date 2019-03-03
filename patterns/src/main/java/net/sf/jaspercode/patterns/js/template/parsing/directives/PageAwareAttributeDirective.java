package net.sf.jaspercode.patterns.js.template.parsing.directives;

import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.langsupport.javascript.types.JavascriptServiceType;
import net.sf.jaspercode.patterns.js.page.PageModelType;
import net.sf.jaspercode.patterns.js.template.parsing.AttributeDirectiveBase;
import net.sf.jaspercode.patterns.js.template.parsing.DirectiveContext;
import net.sf.jaspercode.patterns.js.template.parsing.DirectiveUtils;

@Plugin
public class PageAwareAttributeDirective extends AttributeDirectiveBase {

	@Override
	public void generateCode(DirectiveContext ctx) throws JasperException {
		String ref = ctx.getTemplateAttributes().get("js-page-ref");
		String modelRef = ctx.getTemplateAttributes().get("js-model-ref");
		handlePageAware(ctx, ref, modelRef);
	}

	@Override
	public String getAttributeName() {
		return "js-page-aware";
	}
	
	public static boolean isPageTemplatePageType(JavascriptServiceType pageType) throws JasperException {
		if (pageType.getAttributeType("_isTemplate")!=null) return true;
		return false;
	}
	
	public static void handlePageAware(DirectiveContext ctx,String pageRef,String modelRef) throws JasperException {
		String pageName = ctx.getFunction().getName();
		String pageTypeName = pageName+"TemplatePage";
		String pageVar = DirectiveUtils.PAGE_VAR;
		
		if (ctx.getExecCtx().getVariableType(pageVar)!=null) {
			throw new JasperException("Directive js-page-aware cannot be used on a template that is either a page template or is already page aware");
		}

		// Create types for this pseudo-page and its model
		JavascriptServiceType pageType = new JavascriptServiceType(pageName, false, ctx.getProcessorContext());
		PageModelType modelType = new PageModelType(pageName, false, ctx.getProcessorContext());
		ctx.getProcessorContext().addVariableType(pageType);
		ctx.getProcessorContext().addVariableType(modelType);
		pageType.addAttribute("model", modelType.getName());

		// Modify function to take page parameter
		ctx.getFunction().addParam(pageVar, pageTypeName);
		ctx.getExecCtx().addVariable(pageVar, pageTypeName);
		if ((pageRef!=null) && (pageRef.trim().length()>0)) {
			pageRef = pageRef.trim();
			if (ctx.getExecCtx().getTypeForVariable(pageRef)!=null) {
				throw new JasperException("Couldn't create a page reference called '"+pageRef+"' as there is another variable with that name in the template");
			}
			ctx.getCode().append("var "+pageRef+" = "+pageVar+";\n");
			ctx.getExecCtx().addVariable(pageRef, pageTypeName);
		}
		
		// Create variable for event dispatcher
		String var = DirectiveUtils.EVENT_DISPATCHER_FN_VAR;
		ctx.getExecCtx().addVariable(var, "function");
		ctx.getCode().append("var "+var+" = "+pageVar+".event;\n");

		if ((modelRef!=null) && (modelRef.trim().length()>0)) {
			modelRef = modelRef.trim();
			if (ctx.getExecCtx().getTypeForVariable(modelRef)!=null) {
				throw new JasperException("Couldn't create a model reference called '"+modelRef+"' as there is another variable with that name in the template");
			}
			ctx.getCode().append("var "+modelRef+" = "+pageVar+".model;\n");
			ctx.getExecCtx().addVariable(modelRef, modelType.getName());
		}
		
		ctx.continueRenderElement(ctx.getExecCtx());
	}

}
