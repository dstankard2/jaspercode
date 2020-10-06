package net.sf.jaspercode.patterns.java.domain;

import java.util.List;

import net.sf.jaspercode.api.ComponentProcessor;
import net.sf.jaspercode.api.JasperUtils;
import net.sf.jaspercode.api.ProcessorContext;
import net.sf.jaspercode.api.annotation.Plugin;
import net.sf.jaspercode.api.annotation.Processor;
import net.sf.jaspercode.api.config.Component;
import net.sf.jaspercode.api.exception.JasperException;
import net.sf.jaspercode.api.types.ServiceOperation;
import net.sf.jaspercode.langsupport.java.types.impl.JavaServiceType;
import net.sf.jaspercode.patterns.xml.java.domain.DomainDataRules;

@Plugin
@Processor(componentClass = DomainDataRules.class)
public class DomainDataRulesProcessor implements ComponentProcessor {
	DomainDataRules comp = null;
	ProcessorContext ctx = null;
	
	@Override
	public void init(Component component, ProcessorContext ctx) {
		this.comp = (DomainDataRules)component;
		this.ctx = ctx;
	}

	@Override
	public void process() throws JasperException {
		ctx.setLanguageSupport("Java8");
		String content = comp.getContent();
		
		DomainDataRuleSet rules = DomainRuleUtils.getDomainDataRules(ctx);
		
		String[] str = content.split("\n");
		for(String s : str) {
			s = s.trim();
			if (s.length()==0) continue;
			DomainDataRule newRule = new DomainDataRule();
			int eq = s.indexOf('=');
			if (eq<1) {
				throw new JasperException("Couldn't parse rule '"+s+"' - = was not in expected position");
			}
			String attr = s.substring(0, eq).trim();
			String ref = s.substring(eq+1).trim();
			newRule.setAttribute(attr);
			List<ServiceOperation> ops = JasperUtils.findRuleFromRef(ref, ctx);
			newRule.setOperations(ops);
			String serviceAttr = ref.substring(0, ref.indexOf('.'));
			JavaServiceType srv = JasperUtils.getTypeForSystemAttribute(JavaServiceType.class, serviceAttr, ctx);
			newRule.setServiceType(srv);
			newRule.setServiceRef(serviceAttr);
			rules.addRule(newRule);
		}
	}

}

