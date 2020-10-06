package net.sf.jaspercode.patterns.js.page;

import java.util.List;

import net.sf.jaspercode.api.AttribEntry;
import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.JasperUtils;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.Processor;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.patterns.xml.js.page.PageModel;

@Plugin
@Processor(componentClass = PageModel.class)
public class PageModelProcessor implements ComponentProcessor {

	private ProcessorContext ctx = null;
	private PageModel comp = null;

	@Override
	public void init(Component component, ProcessorContext ctx) {
		this.ctx = ctx;
		this.comp = (PageModel)component;
	}

	@Override
	public void process() throws JasperException {
		ctx.setLanguageSupport("Javascript");
		String pageName = comp.getPageName();
		
		if (pageName.trim().equals("")) {
			throw new JasperException("PageModel requires a 'pageName' attribute");
		}
		PageInfo info = PageUtils.getPageInfo(pageName, ctx);
		if (info==null) {
			throw new JasperException("Couldn't find page '"+pageName+"' for pageModel component");
		}

		PageModelType modelType = info.getModelType();
		ctx.originateVariableType(modelType);

		String attrs = comp.getAttributes();
		List<AttribEntry> entries = JasperUtils.readParametersAsList(attrs, ctx);
		for(AttribEntry entry : entries) {
			String name = entry.getName();
			if (modelType.getAttributeType(name)!=null) {
				if (!modelType.getAttributeType(name).equals(entry.getType().getName())) {
					throw new JasperException("Found inconsistent types for model attribute ");
				}
			} else {
				PageUtils.addModelAttribute(pageName, name, entry.getType().getName(), ctx);
			}
			if (entry.isOriginator()) {
				ctx.originateSystemAttribute(entry.getName());
			} else {
				ctx.dependOnSystemAttribute(entry.getName());
			}
		}
	}

}

