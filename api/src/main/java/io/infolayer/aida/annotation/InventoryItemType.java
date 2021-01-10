package io.infolayer.aida.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InventoryItemType {
	
	/**
	 * Inventory type description.
	 * @return
	 */
	String description();
	
	/**
	 * How to distinct each other. This is an expression.
	 * @return
	 */
	String distinct();
	
	/**
	 * Known fields for simple search.
	 * @return
	 */
	String searchFields();
	
	/**
	 * Template for show its name.
	 * @return
	 */
	String showName();
	
	/**
	 * Template for show its Description.
	 * @return
	 */
	String showDescription();

}
