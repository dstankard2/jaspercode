package net.sf.jaspercode.langsupport.javascript.types;

import java.util.HashMap;
import java.util.Map;

public class DOMElementType extends JavascriptType {
	protected Map<String,String> attributes = new HashMap<String,String>(); // Accessed via getAttribute
	//protected Map<String,String> properties = new HashMap<String,String>(); // Accessed via dot notation

	public DOMElementType() {
		super("DOMElement");
	}

	{
		properties.put("border", "integer");
		properties.put("colSpan", "integer");
		properties.put("tagvalue", "object");
		properties.put("tagName", "string");
		properties.put("tabIndex", "integer");
		properties.put("parentNode", "DOMElement");
		properties.put("className", "string");
		properties.put("contentEditable", "boolean");
		properties.put("dir", "string");
		properties.put("id", "string");
		properties.put("innerHTML", "string");
		properties.put("nodeName", "string");
		properties.put("offsetHeight", "integer");
		properties.put("offsetLeft", "integer");
		properties.put("offsetTop", "integer");
		properties.put("offsetWidth", "integer");
		properties.put("style", "DOMStyle");
		properties.put("classList", "DOMClassList");
		properties.put("min", "integer");
		properties.put("max", "integer");
		properties.put("value", "string");
		properties.put("id", "string");
		properties.put("disabled", "string");
		properties.put("title", "string");
		properties.put("type", "string");
		properties.put("cols", "integer");
		properties.put("rows", "integer");
	}

}

