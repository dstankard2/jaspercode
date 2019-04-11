package net.sf.jaspercode.patterns.maven;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import net.sf.jaspercode.api.BuildComponentProcessor;
import net.sf.jaspercode.api.BuildContext;
import net.sf.jaspercode.api.BuildProcessorContext;
import net.sf.jaspercode.api.Command;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.config.BuildComponent;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.ApplicationFile;
import net.sf.jaspercode.api.resources.ApplicationResource;
import net.sf.jaspercode.patterns.CommandImpl;
import net.sf.jaspercode.patterns.files.XmlFile;
import net.sf.jaspercode.patterns.xml.java.handwritten.HandwrittenCode;
import net.sf.jaspercode.patterns.xml.maven.Dependencies;
import net.sf.jaspercode.patterns.xml.maven.MavenBuild;
import net.sf.jaspercode.patterns.xml.maven.Modules;

@Plugin
public class MavenBuildComponentProcessor implements BuildComponentProcessor {
	private MavenBuild component;
	private BuildProcessorContext ctx = null;
	private List<String> dependencies = new ArrayList<>();
	private String packaging = null;
	private List<PluginConfig> plugins = new ArrayList<>();
	private String finalName = null;
	private String artifact = null;

	private List<Command> build = new ArrayList<>();

	@Override
	public Class<? extends BuildComponent> getComponentClass() {
		return MavenBuild.class;
	}
	
	public String getArtifact() {
		return artifact;
	}
	
	public void addBuildCommand(String cmd) {
		Command command = new CommandImpl(cmd,false);
		build.add(command);
	}
	
	public void addBuildPhase(String phase) {
		BuildContext parentBuildContext = ctx.getParentBuildContext();
		MavenBuildContext mctx = (MavenBuildContext)parentBuildContext;
		
		if (mctx!=null) {
			mctx.addBuildPhase(phase);
		}
		MavenCommand cmd = (MavenCommand)build.get(0);
		cmd.addPhase(phase);
	}

	@Override
	public void initialize(BuildComponent component, BuildProcessorContext ctx) throws JasperException {
		this.component = (MavenBuild)component;
		this.ctx = ctx;
		ctx.getLog().info("Initialize Maven Build Context for path "+ctx.getFolder().getPath());
		this.packaging = this.component.getPackaging();
		Dependencies deps = this.component.getDependencies();
		for(String s : deps.getDependency()) {
			this.dependencies.add(s);
		}
		
		build.add(new MavenCommand());
		
		ApplicationResource src = ctx.getFolder().getResource("src/main/java");
		if (src!=null) {
			if (src instanceof ApplicationFile) {
				throw new JasperException("Maven build found src/main/java but it was not a folder");
			}
			HandwrittenCode code = new HandwrittenCode();
			String path = ctx.getFolder().getPath()+"src/main/java";
			code.setPath(path);
			ctx.addComponent(code);
		}

		// Artifact
		artifact = this.component.getArtifact();
		if ((artifact==null) || (artifact.trim().length()==0)) {
			throw new JasperException("Found a maven build with no artifact");
		}
		if (MavenUtils.getVersion(artifact)==null) {
			BuildContext parent = ctx.getParentBuildContext();
			if (!(parent instanceof MavenBuildContext)) {
				throw new JasperException("Cannot find artifact version for Maven Component '"+this.component.getComponentName()+"'");
			}
			MavenBuildContext mavenParent = (MavenBuildContext)parent;
			String v = MavenUtils.getVersion(mavenParent.getArtifact());
			if (v==null) {
				throw new JasperException("Cannot find artifact version for Maven Component '"+this.component.getComponentName()+"'");
			}
			artifact = artifact + ':'+v;
		}
		
		if (ctx.getParentBuildContext()==null) {
			// TODO: To clean, perform the clean phase
			// addCleanPhase("clean");
			
			// To build, perform the compile goal
			addBuildPhase("compile");

			// To deploy a JAR, run the install goal
			if (this.getPackaging().equals("jar")) {
				//addDeployGoal("install");
			}
			// To deploy, run the package and install goals.  Only for the root build context

			// For a WAR, deploy includes the "package" phase
			if (this.getPackaging().equals("war")) {
				//addDeployPhase("package");
			}
			//addDeployGoal("install");
		}

	}

	@Override
	public BuildContext createBuildContext() {
		ctx.getLog().debug("Create new Maven build context at path '"+ctx.getFolder().getPath()+"'");
		return new MavenBuildContext(ctx,this);
	}

	@Override
	public void generateBuild() throws JasperException {
		String path = ctx.getFolder().getPath();
		XmlFile pom = new XmlFile(path+"pom.xml");
		Document doc = pom.getDocument();
		Element root = doc.addElement("project");
		Element modelVersion = root.addElement("modelVersion");

		modelVersion.setText("4.0.0");
		ctx.addSourceFile(pom);

		String parentStr = component.getParent();
		if ((parentStr!=null) && (parentStr.trim().length()>0)) {
			String pa[] = parentStr.split(":");
			Element parent = root.addElement("parent");
			if ((pa.length<2) || (pa.length>3)) {
				throw new JasperException("Found bad parent reference '"+parentStr+"'");
			}
			parent.addElement("groupId").addText(pa[0]);
			parent.addElement("artifactId").addText(pa[1]);
			if (pa.length==3) {
				parent.addElement("version").addText(pa[2]);
			}
		}

		String[] elts = artifact.split(":");
		if ((elts.length<2) || (elts.length>3)) {
			throw new JasperException("Maven artifact identifier value '"+artifact+"' is not valid");
		}
		root.addElement("groupId").addText(elts[0]);
		root.addElement("artifactId").addText(elts[1]);
		if (elts.length==3) {
			root.addElement("version").addText(elts[2]);
		}
		
		if (packaging.trim().length()==0) {
			this.ctx.getLog().warn("Maven Build '"+component.getComponentName()+"' has no packaging structure defined.  Defaulting to 'pom'."); 
			packaging = "pom";
		}
		root.addElement("packaging").setText(packaging);
		
		Element propsElt = root.addElement("properties");
		String version = ctx.getProperty("maven.java.version");
		if (version!=null) {
			propsElt.addElement("maven.compiler.source").setText(version);
			propsElt.addElement("maven.compiler.target").setText(version);
		}
		
		Modules modules = component.getModules();
		if ((modules!=null) && (modules.getModule().size()>0)) {
			Element modulesElt = root.addElement("modules");
			for(String module : modules.getModule()) {
				modulesElt.addElement("module").setText(module);
			}
		}
		
		if (dependencies.size()>0) {
			Element dependenciesElt = root.addElement("dependencies");

			for(String depName : dependencies) {
				String artifact = depName;
				if (artifact.indexOf(':')<0) {
					String config = "maven.dependency."+depName;
					artifact = ctx.getProperty(config);
					if (artifact==null) {
						throw new JasperException("Maven Processor needs configuration property '"+config+"' to resolve dependency '"+depName+"'");
					}
				}

				Element elt = dependenciesElt.addElement("dependency");
				elt.addElement("groupId").addText(MavenUtils.getGroupId(artifact));
				elt.addElement("artifactId").addText(MavenUtils.getArtifactId(artifact));
				String v = MavenUtils.getVersion(artifact);
				if (v!=null) {
					elt.addElement("version").addText(v);
				}
			}
		}
		if ((plugins.size()>0) || (finalName!=null)) {
			Element buildElt = root.addElement("build");
			if (finalName!=null) {
				Element finalNameElt = buildElt.addElement("finalName");
				finalNameElt.setText(finalName);
			}
			Element pluginsElt = buildElt.addElement("plugins");
			for(PluginConfig plugin : plugins) {
				Element pluginElt = pluginsElt.addElement("plugin");
				String a = plugin.getArtifact();
				String[] parts = a.split(":");
				if ((parts.length<2) || (parts.length>3)) {
					throw new JasperException("Couldn't understand artifact '"+a+"' for maven plugin");
				}
				pluginElt.addElement("groupId").setText(parts[0]);
				pluginElt.addElement("artifactId").setText(parts[1]);
				if (parts.length==3) {
					pluginElt.addElement("version").setText(parts[2]);
				}
				if (plugin.getDependencies().size()>0) {
					Element dependenciesElt = pluginElt.addElement("dependencies");
					for(String dep : plugin.getDependencies()) {
						Element depElt = dependenciesElt.addElement("dependency");
						parts = dep.split(":");
						if ((parts.length<2) || (parts.length>3)) {
							throw new JasperException("Couldn't understand dependency '"+a+"' for maven plugin");
						}
						depElt.addElement("groupId").setText(parts[0]);
						depElt.addElement("artifactId").setText(parts[1]);
						if (parts.length==3) {
							depElt.addElement("version").setText(parts[2]);
						}
					}
				}
				Element configElt = plugin.getConfiguration().getElement();
				if (configElt!=null) {
					pluginElt.add(configElt);
				}
				if (plugin.getExecutions().size()>0) {
					Element execsElt = pluginElt.addElement("executions");
					for(ExecutionConfig exec : plugin.getExecutions()) {
						Element execElt = execsElt.addElement("execution");
						if (exec.getId().trim().length()>0) {
							execElt.addElement("id").setText(exec.getId());
						}
						if (exec.getPhase().trim().length()>0) {
							execElt.addElement("phase").setText(exec.getPhase());
						}
						Element execConfigElt = exec.getConfiguration().getElement();
						if (execConfigElt!=null) {
							execElt.add(execConfigElt);
						}
						if (exec.getGoals().size()>0) {
							Element goalsElt = execElt.addElement("goals");
							for(String goal : exec.getGoals()) {
								goalsElt.addElement("goal").setText(goal);
							}
						}
					}
				}
			}
		}
	}
	
	public String getPackaging() {
		return packaging;
	}
	
	public void addPlugin(PluginConfig plugin) {
		plugins.add(plugin);
	}
	
	public void setFinalName(String finalName) {
		this.finalName = finalName;
	}
	
	public PluginConfig getPlugin(String artifact) {
		for(PluginConfig c : plugins) {
			if (c.getArtifact().indexOf(artifact)==0) {
				return c;
			}
		}
		return null;
	}

	public void addDependency(String dependency) {
		if (!dependencies.contains(dependency)) {
			dependencies.add(dependency);
		}
	}

	@Override
	public List<Command> build() {
		return build;
	}

}
