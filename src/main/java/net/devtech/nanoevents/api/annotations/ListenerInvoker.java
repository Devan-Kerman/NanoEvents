package net.devtech.nanoevents.api.annotations;

/**
 * sometimes you may want to have your invoker have a different method signature than your listener
 * so by annotating a method in the same class and namespace as your invoker, NanoEvents will replace
 * invocations of the annotated method with the listener method. This method should have
 * no implementation, u can use the native keyword or anything as the method body, it wont be copied
 */
public @interface ListenerInvoker {
	/**
	 * the namespace this invoker represents
	 */
	String value();
}
