package net.sf.jaspercode.example1.service;

import net.sf.jaspercode.patterns.java.handwritten.BusinessRule;
import net.sf.jaspercode.patterns.java.handwritten.BusinessService;

@BusinessService(group = "Service", ref = "testService", priority = 17500)
public class TestService {

	@BusinessRule
	public void runThing() {
		System.out.println("Hello there.");
	}

}
