package net.sf.jaspercode.patterns.java.domain;

import java.util.ArrayList;
import java.util.List;

public class DomainDataRuleSet {
	private List<DomainDataRule> rules = new ArrayList<>();
	
	public void addRule(DomainDataRule rule) {
		rules.add(rule);
	}

	public List<DomainDataRule> findRulesForAttribute(String attrib) {
		List<DomainDataRule> ret = new ArrayList<>();

		for(DomainDataRule rule : rules) {
			if (rule.getAttribute()==null) continue;
			if (rule.getAttribute().equals(attrib)) {
				ret.add(rule);
			}
		}
		
		return ret;
	}

}

