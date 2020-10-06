package net.sf.jaspercode.patterns.xml.js;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.XmlConfig;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.patterns.PatternPriority;

@XmlConfig
@Plugin
@XmlRootElement(name="module")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="module",propOrder={ })
public class HandwrittenModule extends Component {

	@XmlAttribute
	private String ref = "";
	
	@XmlAttribute
	private String name = "";
	
	@XmlAttribute
	private String webPath = "";
	
	@XmlAttribute
	private String exportType = "";

	public int getPriority() {
		return PatternPriority.HANDWRITTEN_JS_MODULE;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getWebPath() {
		return webPath;
	}

	public void setWebPath(String webPath) {
		this.webPath = webPath;
	}

	public String getExportType() {
		return exportType;
	}

	public void setExportType(String exportType) {
		this.exportType = exportType;
	}

}

