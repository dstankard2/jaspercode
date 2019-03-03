package net.sf.jaspercode.api.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.sf.jaspercode.api.annotation.Plugin;

/**
 * Base class for any component pattern.  The implementation class 
 * should be JAXB-annotated.  The implementation should also override 
 * getPriority() so that components of this pattern are processed in 
 * their due place.
 * @author DCS
 */
@Plugin
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="component",propOrder={ })
@XmlRootElement(name="component")
public class Component {

	public int getPriority() {
		return Integer.MAX_VALUE;
	}
	
	public String getPatternName() {
		return this.getClass().getSimpleName();
	}
	
	public String getComponentName() {
		return "*** Component["+this.getClass().getCanonicalName()+"] ***";
	}

}
