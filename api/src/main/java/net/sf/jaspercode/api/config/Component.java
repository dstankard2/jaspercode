package net.sf.jaspercode.api.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.config.Property;

/**
 * Base class for any component pattern.  The implementation class 
 * should be JAXB-annotated.  The implementation should also override 
 * getPriority() so that components of this pattern are processed in 
 * their due place.
 * @author DCS
 */
@Plugin
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="component",propOrder={ "property" })
@XmlRootElement(name="component")
public class Component {

	private List<Property> property = new ArrayList<>();
	
	public List<Property> getProperty() {
		return property;
	}

	public void setProperty(List<Property> property) {
		this.property = property;
	}

	public int getPriority() {
		return Integer.MAX_VALUE;
	}
	
	public String getPatternName() {
		return this.getClass().getSimpleName();
	}
	
	public String getComponentName() {
		return "*** Component["+this.getClass().getCanonicalName()+"] ***";
	}
	
	/**
	 * Is the given component the same component definition as this one?<br/>
	 * This method is used when reading a modified file, to check if the components in the 
	 * updated file match existing components.<br/>
	 * This should only examine the XML configuration.  Changes to jasper.properties are monitored 
	 * by the engine.<br/>
	 * @param other Component to compare against
	 * @return true if these are the same permutation of the same pattern.
	 */
	public boolean isSameComponent(Component other) {
		return false;
	}

}
