package net.sf.jaspercode.example1.util;

import net.sf.jaspercode.patterns.java.handwritten.BusinessRule;
import net.sf.jaspercode.patterns.java.handwritten.BusinessService;

import net.sf.jaspercode.example1.obj.User;
import net.sf.jaspercode.example1.obj.UserData;

@BusinessService(group = "Service", ref = "userUtil", priority = 17500)
public class UserUtil {

	@BusinessRule
	public void runThing() {
		// This is a test. //
		System.out.println("Hello there.");
	}

	@BusinessRule
	public User getUser(Integer userId) {
		User ret = null;
		
		ret = new User();
		
		return ret;
	}
	
	public void updateUserData(UserData userData) {
		// no-op
	}
	
}
