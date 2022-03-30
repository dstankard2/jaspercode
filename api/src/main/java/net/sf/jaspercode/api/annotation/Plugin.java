package net.sf.jaspercode.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a Jasper plugin that will be found by the classpath scanner.
 * Any Component subclass, component processor, application plugin or engine plugin must have 
 * this annotation in order to be found by the classpath scanner.
 * @author DCS
 */
@Target(value=ElementType.TYPE)
@Retention(value=RetentionPolicy.RUNTIME)
public @interface Plugin {

}
