package net.sf.jaspercode.patterns.xml.java.dataobject;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.RequiredXml;
import net.sf.jaspercode.api.annotation.XmlConfig;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.patterns.PatternPriority;

@XmlConfig
@Plugin
@XmlRootElement(name="applyClassification")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="applyClassification",propOrder={ })
public class ApplyClassification extends Component {

	@XmlAttribute
	private Boolean autoApply = Boolean.FALSE;

	@RequiredXml
	@XmlAttribute
	private String classificationName = "";
	
	@RequiredXml
	@XmlElement
	private List<String> object = new ArrayList<>();

	public Boolean getAutoApply() {
		return autoApply;
	}

	public void setAutoApply(Boolean autoApply) {
		this.autoApply = autoApply;
	}

	public String getClassificationName() {
		return classificationName;
	}

	public void setClassificationName(String classificationName) {
		this.classificationName = classificationName;
	}

	public List<String> getObject() {
		return object;
	}

	public void setObject(List<String> object) {
		this.object = object;
	}
	
	@Override
	public int getPriority() {
		return PatternPriority.APPLY_CLASSIFICATION;
	}

}

