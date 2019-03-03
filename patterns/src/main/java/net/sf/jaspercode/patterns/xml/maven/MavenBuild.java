package net.sf.jaspercode.patterns.xml.maven;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import net.sf.jaspercode.api.annotation.ConfigProperty;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.XmlConfig;
import net.sf.jaspercode.api.config.BuildComponent;

@Plugin
@XmlConfig
@XmlRootElement(name="project")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="project",propOrder={ "description", "modules", "dependencies" })
public class MavenBuild extends BuildComponent {

	@XmlElement
	private Dependencies dependencies = new Dependencies();
	
	@XmlElement
	private Modules modules = new Modules();

	@XmlElement
	private String description = "";
	
	@XmlAttribute
	private String artifact = "";

	@XmlAttribute
	private String parent = "";

	@XmlAttribute
	private String packaging = "";
	
	@XmlTransient
	private String javaVersion = null;
	
	@XmlTransient
	private String buildPhases = "";

	@XmlTransient
	private String deployPhases = "";

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getArtifact() {
		return artifact;
	}

	public void setArtifact(String artifact) {
		this.artifact = artifact;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getPackaging() {
		return packaging;
	}

	public void setPackaging(String packaging) {
		this.packaging = packaging;
	}

	public Modules getModules() {
		return modules;
	}

	public void setModules(Modules modules) {
		this.modules = modules;
	}

	public Dependencies getDependencies() {
		return dependencies;
	}

	public void setDependencies(Dependencies dependencies) {
		this.dependencies = dependencies;
	}

	@Override
	public String getComponentName() {
		return "Mavenbuild["+getArtifact()+"]";
	}

	@ConfigProperty(required = true, name = "maven.java.version",
			description = "Compiler source and target versions for Maven", example = "1.8")
	public void setJavaVersion(String javaVersion) {
		this.javaVersion = javaVersion;
	}
	
	public String getJavaVersion() {
		return this.javaVersion;
	}

	@ConfigProperty(required = true, name = "maven.build.phases",
			description = "Maven build phase(s) to execute when JasperCode builds the application", example = "compile,package")
	public void setBuildPhases(String buildPhases) {
		this.buildPhases = buildPhases;
	}
	
	public String getBuildPhases() {
		return this.buildPhases;
	}

	@ConfigProperty(required = true, name = "maven.deploy.phases",
			description = "Maven build phase(s) to execute when JasperCode deploys the application", example = "install")
	public void setDeployPhases(String deployPhases) {
		this.deployPhases = deployPhases;
	}
	
	public String getDeployPhases() {
		return this.deployPhases;
	}

}
