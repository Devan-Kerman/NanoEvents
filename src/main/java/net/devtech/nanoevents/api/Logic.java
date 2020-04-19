package net.devtech.nanoevents.api;

/**
 * This is the class you use to create invoker methods, it has 2 methods, start, and end
 */
public class Logic {
	private Logic() {
		/*some examples of invokers*/
		{
			// this is a cancellable event implemented in the invoker system
			// intellij and other good IDEs (basically just intellij) will
			// warn you about random stuff, just ignore it unless it's an actual
			// problem
			boolean cancelled = false;
			start();
			if (!cancelled) ;
			//invoker();
			end();
			return /*cancelled*/;
		}
		// more soon:tm:

	}

	/**
	 * when defining an invoker, you need to surround a portion of your code in a {@link #start()} and {@link #end()}
	 * method invocation, when your invoker is evaluated, the transformer cuts the enclosed slice of code, and pastes
	 * it, replacing all the shallow recursive calls with direct calls to the listeners. <b>I'm not here to hold your hand
	 * this is an unsafe way of doing this, there's obvious issues with this system, for example</b>
	 * the following code will be cut and paste by the transformer, but will lead to undefined behavior
	 * <pre>
	 * Invoker.start();
	 * if(...) {
	 *    Invoker.end();
	 * }
	 * </pre>
	 * you may only have 1 start call, and 1 end call per method, so no funky ifs or any nonsense like that.
	 */
	public static void start() {}

	/**
	 * this is the terminator for an invoker
	 */
	public static void end() {}
}
