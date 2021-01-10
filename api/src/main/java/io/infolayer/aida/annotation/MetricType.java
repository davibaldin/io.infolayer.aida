package io.infolayer.aida.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MetricType {
	
	/**
	 * Metric space name.
	 * @return
	 */
	String spaceName();
	
	/**
	 * Metric name.
	 * @return
	 */
	String name();
	
	/**
	 * Description
	 * @return
	 */
	String description();
	
	/**
	 * Type.
	 * @return
	 */
	int type();
	
	/**
	 * Known value for metric error.
	 * @return
	 */
	String errorValue() default "";
	
	/**
	 * Known pattern for string values.
	 * @return
	 */
	String acceptPattern() default "";
	
	/**
	 * Minimum value.
	 * @return
	 */
	float minValue() default 0;
	
	/**
	 * Maximum value.
	 * @return
	 */
	float maxValue() default 0;
	
	/**
	 * Field used for correlation id.
	 * @return
	 */
	String fieldIdName() default "";

}
