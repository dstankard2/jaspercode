package net.sf.jaspercode.patterns.maven;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

public class PropertyWithNestedProperties extends PluginProperty {
	List<PluginProperty> nestedProperties = new ArrayList<>();
	
	public PropertyWithNestedProperties(String name) {
		super(name);
	}
	
	public PropertyWithNestedProperties addPropertySingleValue(String name,String value) {
		nestedProperties.add(new PropertyWithSingleValue(name,value));
		return this;
	}
	public PropertyWithValueList addPropertyWithValueList(String name,String entryName) {
		PropertyWithValueList ret = new PropertyWithValueList(name,entryName);
		nestedProperties.add(ret);
		return ret;
	}
	public PropertyWithNestedProperties addPropertyWithNestedProperties(String name) {
		PropertyWithNestedProperties ret = new PropertyWithNestedProperties(name);
		nestedProperties.add(ret);
		return ret;
	}

	@Override
	public Element getPropertyElement() {
		Element ret = factory.createElement(name);
		for(PluginProperty prop : nestedProperties) {
			ret.add(prop.getPropertyElement());
		}
		return ret;
	}
	
}
