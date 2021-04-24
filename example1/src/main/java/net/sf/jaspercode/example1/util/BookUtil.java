package net.sf.jaspercode.example1.util;

import net.sf.jaspercode.patterns.java.handwritten.BusinessRule;
import net.sf.jaspercode.patterns.java.handwritten.BusinessService;
import net.sf.jaspercode.example1.entity.Book;

@BusinessService(group = "Service", ref = "bookUtil", priority = 17500)
public class BookUtil {

	@BusinessRule
	public void doSomething() {
		System.out.println("Hello there.");
	}

	@BusinessRule
	public Book getBook(Integer bookId) {
		Book ret = null;
		
		//ret = new Book();
		
		return ret;
	}
}
