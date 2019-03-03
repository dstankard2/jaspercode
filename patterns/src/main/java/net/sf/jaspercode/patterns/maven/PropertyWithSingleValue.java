package net.sf.jaspercode.patterns.maven;

import org.dom4j.Element;

public class PropertyWithSingleValue extends PluginProperty {
	private String value = null;
	
	public PropertyWithSingleValue(String name,String value) {
		super(name);
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}

	@Override
	public Element getPropertyElement() {
		Element elt = factory.createElement(name);
		elt.setText(value);
		return elt;
	}
	
}
