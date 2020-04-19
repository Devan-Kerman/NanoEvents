package net.devtech.nanoevents.testing;

import net.devtech.nanoevents.api.Invoker;
import net.devtech.nanoevents.api.Logic;

public class Test {
	@Invoker("test:event")
	public static boolean invoker() {
		boolean cancelled = false;
		Logic.start();
		if(!cancelled)
			cancelled = invoker();
		Logic.end();
		return cancelled;
	}
}
