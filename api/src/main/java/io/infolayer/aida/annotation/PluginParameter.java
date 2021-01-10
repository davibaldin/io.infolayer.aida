package io.infolayer.aida.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginParameter {
	
	public static final String NAME = "_same";
	
	/**
	 * Parameter name.
	 * @return
	 */
	String name() default NAME;
	
	/**
	 * Inventory type description.
	 * @return
	 */
	boolean required() default false;
	
	/**
	 * How to distinct each other. This is an expression.
	 * @return
	 */
	String defaultValue() default "";
	
	/**
	 * Description
	 * @return
	 */
	String description() default "";

}
