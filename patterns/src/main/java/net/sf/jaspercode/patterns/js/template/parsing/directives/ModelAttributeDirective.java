package net.sf.jaspercode.patterns.js.template.parsing.directives;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.jaspercode.api.JasperException;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.patterns.js.template.parsing.AttributeDirectiveBase;
import net.sf.jaspercode.patterns.js.template.parsing.DirectiveContext;

@Plugin
public class ModelAttributeDirective extends AttributeDirectiveBase {

	@Override
	public String getAttributeName() {
		return "js-model";
	}

	@Override
	public void generateCode(DirectiveContext ctx) throws JasperException {
		String model = ctx.getTemplateAttributes().get("js-model");
		StringBuilder b = ctx.getCode();
		String eltName = ctx.getElementName();

		//ctx.continueRenderElement(ctx.getExecCtx());
		String var = ctx.getElementVarName();
		String template = null;
		Map<String,String> templateParams = new HashMap<>();
		templateParams.put("ELTVAR", var);
		String remFn = ctx.newVarName("_rm", "function", ctx.getExecCtx());
		templateParams.put("REM_FN", remFn);
		templateParams.put("MODEL_REF", "_model."+model);
		
		String attrName = model;
		if (model.indexOf('.')>=0) {
			attrName = model.substring(0, model.indexOf('.'));
		}
		templateParams.put("CHANGE_EVENT", attrName+"Changed");

		if (eltName.equals("select")) {
			template = SELECT_MODEL;
			String fn = ctx.newVarName("_f", "function", ctx.getExecCtx());
			templateParams.put("FN", fn);
		} else if (eltName.equals("input")) {
			String type = ctx.getDomAttributes().get("type");
			String changeEvent;
			if (type==null) changeEvent = "keyup";
			else if (type.equals("text")) changeEvent = "keyup";
			else if (type.equals("password")) changeEvent = "keyup";
			else changeEvent = "change";
			template = INPUT_MODEL_TEXT;
			templateParams.put("DOMEVENT", changeEvent);
		} else if (eltName.equals("textarea")) {
			
		}
		
		String code = template;
		for(Entry<String,String> entry : templateParams.entrySet()) {
			code = code.replaceAll("aaa"+entry.getKey(), entry.getValue());
		}
		b.append(code);
		ctx.continueRenderElement();
	}
	
	private static final String INPUT_MODEL_TEXT = "aaaELTVAR.addEventListener('aaaDOMEVENT', function() {\n"
			+ "var val = aaaELTVAR.value;\n"
			+ "try {\n"
			+ "aaaMODEL_REF = val;\n"
			+ "} catch(err) { }\n"
			+ "});\n"
			+ "var aaaREM_FN = _page.event('aaaCHANGE_EVENT', function() {\n"
			+ "var val = aaaMODEL_REF;\n"
			+ "try {\n"
			+ "if ((val===null) || (val===undefined)) val = '';\n"
			+ "aaaELTVAR.value = val;\n"
			+ "} catch(err) {}\n"
			+ "});\n"
			+ "aaaELTVAR.$$remove.push(aaaREM_FN);\n";

	private static final String SELECT_MODEL = "aaaELTVAR.onchange = function() {\r\n" + 
			"var val = aaaELTVAR.value;\r\n" + 
			"try {\r\n" + 
			"aaaMODEL_REF = val;\r\n" + 
			"} catch(_err) { }\r\n" + 
			"}\r\n" + 
			"var aaaFN = function() {\r\n" + 
			"	var val = null;\r\n" + 
			"	try {\r\n" + 
			"		val = aaaMODEL_REF;\r\n" + 
			"	} catch(_err) { }\r\n" + 
			"	for(var _i=0;_i<aaaELTVAR.options.length;_i++) {\r\n" + 
			"		if (aaaELTVAR.options[_i].value == val) {\r\n" + 
			"			aaaELTVAR.selectedIndex = _i;\r\n" + 
			"			break;\r\n" + 
			"		}\r\n" + 
			"	}\r\n" + 
			"}.bind(_page);\r\n" + 
			"var aaaREM_FN = _page.event('aaaCHANGE_EVENT',aaaFN);\r\n" + 
			"aaaFN();\r\n" + 
			"aaaELTVAR.$$remove.push(aaaREM_FN);\r\n" + 
			"";
}

