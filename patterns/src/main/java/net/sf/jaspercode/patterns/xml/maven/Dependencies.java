package net.sf.jaspercode.patterns.xml.maven;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="dependencies",propOrder={ "dependency" })
public class Dependencies {

	@XmlElement
	private List<String> dependency = new ArrayList<>();

	public List<String> getDependency() {
		return dependency;
	}

	public void setDependency(List<String> dependency) {
		this.dependency = dependency;
	}
	
}
