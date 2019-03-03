package net.sf.jaspercode.patterns.maven;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;

public abstract class PluginProperty {

	protected DocumentFactory factory = new DocumentFactory();
	protected String name;
	
	public PluginProperty(String name) {
		this.name = name;
	}

	public abstract Element getPropertyElement();
	
}
