package net.sf.jaspercode.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for properties required by a component.  A field on a ComponentBase implementation 
 * may use this annotation to have a configuration property injected by the engine.  This annotation 
 * is also used to 
 * @author DCS
 */
@Target(value=ElementType.METHOD)
@Retention(value=RetentionPolicy.RUNTIME)
public @interface ConfigProperty {

	boolean required() default true;
	String name();
	String description() default "";
	String example() default "";

}
