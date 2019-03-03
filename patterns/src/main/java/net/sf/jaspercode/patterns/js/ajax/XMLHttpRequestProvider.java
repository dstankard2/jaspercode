package net.sf.jaspercode.patterns.js.ajax;

import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.langsupport.javascript.JavascriptCode;
import net.sf.jaspercode.patterns.js.AjaxClientProvider;

@Plugin
public class XMLHttpRequestProvider implements AjaxClientProvider {

	@Override
	public String getName() {
		return "XMLHttpRequest";
	}

	@Override
	public JavascriptCode getAjaxCode(String optionsVar, String resultVar) {
		StringBuilder b = new StringBuilder();

		b.append(resultVar+" = new Promise(function(_resolve, _reject) {\n");
		
		b.append("var _rr = new XMLHttpRequest();\n");
		b.append("_rr.onload=() => {");
		//b.append("_rr.onreadystatechanged=() => {");
		
		// If request is done
		b.append("if (_rr.readyState==4) {\n");
		
		// Parse response into variable _response
		b.append("var _response;\n");
		b.append("if (_rr.responseText) {\n");
		b.append("try { _response = JSON.parse(_rr.responseText);} catch(err) {_response = _rr.responseText;}\n");
		b.append("}\n");
		
		// If success
		b.append("if ((_rr.status >= 200) && (_rr.status < 300)) {");
		b.append("// success\n");
		b.append("_resolve(_response);\n");
		b.append("}\n");

		// Else fail
		b.append(" else {\n");
		b.append("// fail\n");
		b.append("_reject(_response);\n");
		b.append("}\n");

		// End of request being done
		b.append("}\n");

		// End of event listener
		b.append("}\n");

		b.append("_rr.open("+optionsVar+".method, "+optionsVar+".path, true);\n");
		// Set headers if they are there
		b.append("if ("+optionsVar+".headers) {\n");
		b.append("for (var _key in "+optionsVar+".headers) {\n");
		b.append("var _value = "+optionsVar+".headers[_key];\n");
		b.append("_rr.setRequestHeader(_key,_value);\n");
		b.append("}\n}\n");
		// Finish with setting headers

		b.append("if (!"+optionsVar+".requestBody) _rr.send();\n");
		b.append("else  _rr.send("+optionsVar+".requestBody);\n");

		b.append("});\n");

		return new JavascriptCode(b.toString());
	}

}
