package net.sf.jaspercode.patterns.js.template;

import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.Processor;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.resources.ApplicationFolder;
import net.sf.jaspercode.api.resources.ApplicationResource;
import net.sf.jaspercode.patterns.PatternPriority;
import net.sf.jaspercode.patterns.xml.js.template.TemplateSet;

@Plugin
@Processor(componentClass=TemplateSet.class)
public class TemplateSetProcessor implements ComponentProcessor {

	private TemplateSet comp = null;
	private ProcessorContext ctx = null;

	@Override
	public void init(Component component, ProcessorContext ctx) {
		this.comp = (TemplateSet)component;
		this.ctx = ctx;
	}

	@Override
	public void process() throws JasperException {
		ctx.setLanguageSupport("Javascript");

		String ref = comp.getRef();
		
		if (ctx.getSystemAttribute(ref)!=null) {
			throw new JasperException("Couldn't use ref '"+ref+"' for template set as that system attribute already exists");
		}
		String serviceName = "TemplateSet_"+ref;

		int priority = PatternPriority.HTML_TEMPLATE+comp.getOrder();

		ApplicationResource res = ctx.getResource(comp.getPath());
		if (res==null) {
			throw new JasperException("Couldn't find folder directory at path '"+comp.getPath()+"'");
		}
		if (!(res instanceof ApplicationFolder)) {
			throw new JasperException("Resource '"+comp.getPath()+"' is not an application resource");
		}
		
		TemplateSingleFolderWatcher watcher = new TemplateSingleFolderWatcher(ref,serviceName,priority,(ApplicationFolder)res);
		ctx.addFolderWatcher(res.getPath(), watcher);
	}

}

