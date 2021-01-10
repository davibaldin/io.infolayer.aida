package io.infolayer.aida.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Plugin {
	
	String name();
	
	String description();
	
	String platform() default "*";
	
	int timeout() default 0;
	
	Class<?> configurationParser() default Void.class;
	
	Env[] environment() default {};
	
	Class<?>[] inputHandler() default {};
	
	Class<?>[] outputHandler() default {};

}
