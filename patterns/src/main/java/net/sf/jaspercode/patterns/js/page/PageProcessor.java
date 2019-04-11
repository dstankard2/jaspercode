package net.sf.jaspercode.patterns.js.page;

import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.Processor;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.patterns.xml.js.page.Page;

@Plugin
@Processor(componentClass = Page.class)
public class PageProcessor implements ComponentProcessor {

	private Page page = null;
	private ProcessorContext ctx = null;
	
	@Override
	public void init(Component component, ProcessorContext ctx) {
		this.page = (Page)component;
		this.ctx = ctx;
	}

	@Override
	public void process() throws JasperException {
		ctx.setLanguageSupport("Javascript");
		String name = page.getName();

		if (name.trim().length()==0) {
			throw new JasperException("Page has no name");
		}
		
		PageInfo pageInfo = PageUtils.getPageInfo(page.getName(), ctx);

		if (pageInfo!=null) {
			throw new JasperException("Page '"+page.getName()+"' is already defined");
		}
		pageInfo = new PageInfo();
		pageInfo.setName(name);
		PageUtils.addPageInfo(pageInfo, ctx);
	}

}
