package net.sf.jaspercode.patterns.maven;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;

public class PropertySet {

	private String eltName;
	
	public PropertySet(String elementName) {
		this.eltName = elementName;
	}

	private List<PluginProperty> properties = new ArrayList<>();
	
	public PropertySet addProperty(String name,String value) {
		properties.add(new PropertyWithSingleValue(name,value));
		return this;
	}

	public PropertyWithNestedProperties addNestingProperty(String name) {
		PropertyWithNestedProperties prop = new PropertyWithNestedProperties(name);
		properties.add(prop);
		return prop;
	}
	
	public PropertyWithValueList addPropertyValueList(String name,String singleValueName) {
		PropertyWithValueList prop = new PropertyWithValueList(name,singleValueName);
		properties.add(prop);
		return prop;
	}
	
	public Element getElement() {
		DocumentFactory factory = new DocumentFactory();
		Element elt = null;
		if (properties.size()>0) {
			elt = factory.createElement(eltName);
			for(PluginProperty prop : properties) {
				elt.add(prop.getPropertyElement());
			}
		}
		return elt;
	}

}
