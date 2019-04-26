package net.sf.jaspercode.example1.util;

import net.sf.jaspercode.patterns.java.handwritten.BusinessRule;
import net.sf.jaspercode.patterns.java.handwritten.BusinessService;

import net.sf.jaspercode.example1.obj.User;

@BusinessService(group = "Service", ref = "userUtil", priority = 17500)
public class UserUtil {

	@BusinessRule
	public void runThing() {
		System.out.println("Hello there.");
	}

	@BusinessRule
	public User getUser(Integer userId) {
		User ret = null;
		
		ret = new User();
		
		return ret;
	}
}
