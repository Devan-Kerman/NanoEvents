package net.devtech.nanoevents.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * In some cases, when there is only one listener for an event, there is a more effecient way of invoking the event
 * A method annotated with this will take priority over the original invoker if there is only one listener,
 * it does not need a Logic.start and Logic.end, and all shallow recursive calls are converted. The single
 * invoker's bytecode replaces the invoker, so you should still call the original invoker.
 * @see Invoker
 */
@Target (ElementType.METHOD)
@Retention (RetentionPolicy.CLASS)
public @interface SingleInvoker {
	/**
	 * the namespace this invoker represents
	 */
	String value();
}
