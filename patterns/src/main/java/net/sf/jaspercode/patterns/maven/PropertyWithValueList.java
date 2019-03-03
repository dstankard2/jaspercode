package net.sf.jaspercode.patterns.maven;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

public class PropertyWithValueList extends PluginProperty {

	private String singleValueName = null;
	private List<String> values = new ArrayList<>();
	
	public PropertyWithValueList(String listName,String singleValueName) {
		super(listName);
		this.singleValueName = singleValueName;
	}

	public PropertyWithValueList addValue(String value) {
		this.values.add(value);
		return this;
	}

	@Override
	public Element getPropertyElement() {
		Element ret = factory.createElement(name);
		for(String v : values) {
			Element elt = ret.addElement(singleValueName);
			elt.setText(v);
		}

		return ret;
	}
	
}
