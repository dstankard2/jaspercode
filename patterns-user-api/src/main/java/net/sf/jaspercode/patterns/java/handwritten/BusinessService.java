package net.sf.jaspercode.patterns.java.handwritten;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value=ElementType.TYPE)
@Retention(value=RetentionPolicy.SOURCE)
public @interface BusinessService {

	int priority() default 30000;
	String ref() default "";
	String group();

}
