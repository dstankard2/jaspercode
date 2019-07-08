package net.sf.jaspercode.patterns.js.template.parsing.directives;

import net.sf.jaspercode.api.CodeExecutionContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.types.ServiceOperation;
import net.sf.jaspercode.patterns.js.page.PageInfo;
import net.sf.jaspercode.patterns.js.page.PageUtils;
import net.sf.jaspercode.patterns.js.template.parsing.AttributeDirectiveBase;
import net.sf.jaspercode.patterns.js.template.parsing.DirectiveContext;
import net.sf.jaspercode.patterns.js.template.parsing.DirectiveUtils;

@Plugin
public class PageAttributeDirective extends AttributeDirectiveBase {

	@Override
	public String getAttributeName() {
		return "js-page";
	}

	@Override
	public void generateCode(DirectiveContext ctx) throws JasperException {
		String pageName = ctx.getTemplateAttributes().get("js-page");
		String pageRef = ctx.getTemplateAttributes().get("js-page-ref");
		String modelRef = ctx.getTemplateAttributes().get("js-model-ref");
		CodeExecutionContext execCtx = ctx.getExecCtx();

		PageInfo pageInfo = PageUtils.getPageInfo(pageName, ctx.getProcessorContext());

		if (pageInfo==null) {
			throw new JasperException("Found no page called '"+pageName+"'");
		}

		ServiceOperation op = ctx.getFunction();
		StringBuilder fnCode = ctx.getCode();
		execCtx.addVariable("_page", pageName);
		op.addParam("_page", pageName);

		execCtx.addVariable("_model", pageInfo.getModelType().getName());
		fnCode.append("var _model = _page.model;\n");
		
		String modelTypeName = pageInfo.getModelType().getName();
		execCtx.addVariable("_model", modelTypeName);
		if (pageRef!=null) {
			if (execCtx.getVariableType(pageRef)!=null) {
				throw new JasperException("Couldn't create variable '"+pageRef+"' for page ref because that variable already exists");
			}
			fnCode.append("var "+pageRef+" = _page;\n");
			execCtx.addVariable(pageRef, pageName);
		}
		
		if (modelRef!=null) {
			if (execCtx.getVariableType(modelRef)!=null) {
				throw new JasperException("Couldn't create variable '"+pageRef+"' for page model ref because that variable already exists");
			}
			fnCode.append("var "+modelRef+" = _page.model;\n");
			execCtx.addVariable(modelRef, pageInfo.getModelType().getName());
		}
		// Get the page renderer
		String ref = ctx.getProcessorContext().getProperty("page.template.objRef");
		
		if (ref==null) {
			throw new JasperException("Page Attribute directive requires config property 'page.template.objRef' which should be a reference to an object on the window that will render the page");
		}
		
		//String ref = ctx.getTemplateObj();
		pageInfo.setPageRendererObj(ref);
		pageInfo.setPageRendererRule(op);
		
		// Add event function to execCtx
		execCtx.addVariable(DirectiveUtils.EVENT_DISPATCHER_FN_VAR, "function");
		fnCode.append("var "+DirectiveUtils.EVENT_DISPATCHER_FN_VAR+" = _page.event;\n");
		
		ctx.continueRenderElement(execCtx);

		/* TODO: Move appropriate code to page builder

		String typeName = ctx.getProcessorContext().getSystemAttribute(pageName);
		if (typeName==null) {
			throw new JasperException("Could not find a page called '"+pageName+"'");
		}
		JavascriptServiceType pageType = JasperUtils.getType(JavascriptServiceType.class, typeName, ctx.getProcessorContext());
		if (DirectiveUtils.getPageName(ctx)!=null) {
			throw new JasperException("Only one page may be defined for any element - pages must be in separate locations");
		}
		String pageVar = DirectiveUtils.PAGE_VAR;
		b.append(pageType.declare(pageVar, execCtx).getCodeText());
		b.append(pageType.instantiate(pageVar).getCodeText());
		execCtx.addVariable(pageVar, pageType.getName());
		
		if (pageRef!=null) {
			if (execCtx.getVariableType(pageRef)!=null) {
				throw new JasperException("Couldn't make a page ref called '"+pageRef+"' - that variable already exists");
			}
			b.append("var "+pageRef+" = "+DirectiveUtils.PAGE_VAR+";\n");
			execCtx.addVariable(pageRef, pageType.getName());
		}

		ctx.continueRenderElement(execCtx);
		String var = ctx.getElementVarName();
		b.append(var+".id = '"+pageName+"';\n");
		StringBuilder init = PageUtils.getInitFunction(ctx.getProcessorContext(), pageName);
		init.append("var _page;\n");
		execCtx.addVariable("_page", pageName);
		init.append("var "+ctx.getTemplateObj()+" = window."+ctx.getTemplateObj()+";\n");
		init.append(JavascriptUtils.callJavascriptOperation(resultName, objName, op, execCtx, explicitParams, addSemicolon))
		init.append(JavascriptUtils.invokeFunction("_page", ctx.getTemplateObj(), ctx.getFunction(), execCtx).getCodeText());
		init.append("this.view.page.parentNode.replaceChild(_page,this.view.page);\n");
		init.append("this.view.page = _page;\n");
*/
	}

	/*
	public static String getPageName(DirectiveContext ctx) {
		String ret = null;
		
		if (ctx.getExecCtx().getVariableType("_page")!=null) {
			ret = ctx.getExecCtx().getVariableType("_page");
		}
		
		return ret;
	}
	
	public static PageModelType getPageModelType(DirectiveContext ctx) throws JavascribeException {
		PageModelType ret = null;
		
		PageType page = (PageType)ctx.getExecCtx().getTypeForVariable("_page");
		String modelTypeName = page.getAttributeType("model");
		ret = (PageModelType)ctx.getProcessorContext().getType(modelTypeName);
		
		return ret;
	}
	*/

}

