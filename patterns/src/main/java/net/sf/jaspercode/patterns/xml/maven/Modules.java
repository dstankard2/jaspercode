package net.sf.jaspercode.patterns.xml.maven;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="modules",propOrder={ "module" })
public class Modules {

	@XmlElement
	private List<String> module = new ArrayList<>();

	public List<String> getModule() {
		return module;
	}

	public void setModule(List<String> module) {
		this.module = module;
	}
	
}
