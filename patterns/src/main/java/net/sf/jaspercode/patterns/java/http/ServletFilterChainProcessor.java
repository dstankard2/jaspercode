package net.sf.jaspercode.patterns.java.http;

import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.Processor;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.patterns.xml.java.http.ServletFilterGroup;

@Plugin
@Processor(componentClass = ServletFilterGroup.class)
public class ServletFilterChainProcessor implements ComponentProcessor {

	private ServletFilterGroup comp = null;
	private ProcessorContext ctx = null;

	@Override
	public void init(Component component, ProcessorContext ctx) {
		this.comp = (ServletFilterGroup)component;
		this.ctx = ctx;
	}

	@Override
	public void process() throws JasperException {
		String name = comp.getName();
		String filters = comp.getFilters();

		if (name.trim().length()==0) {
			throw new JasperException("ServletFilterChain has no name defined");
		}
		
		if (filters.trim().length()==0) {
			throw new JasperException("ServletFilterChain has no filters specified");
		}
		
		String[] filterNames = filters.split(",");
		JavaWebUtils.addServletFilterChain(name, filterNames, ctx);
	}

	
}
