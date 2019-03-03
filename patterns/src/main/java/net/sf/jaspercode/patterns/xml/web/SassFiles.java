package net.sf.jaspercode.patterns.xml.web;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.XmlConfig;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.patterns.PatternPriority;

@Plugin
@XmlConfig
@XmlRootElement(name="sassFiles")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="sassFiles",propOrder={ })
public class SassFiles extends Component {

	@XmlAttribute
	private String sourcePath = "";
	
	@XmlAttribute
	private String outputPath = "";

	public int getPriority() {
		return PatternPriority.SASS_FILES;
	}

	public String getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	public String getOutputPath() {
		return outputPath;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}
	
	public String getComponentName() {
		return "SASS Files at '"+getSourcePath()+"'";
	}

}

