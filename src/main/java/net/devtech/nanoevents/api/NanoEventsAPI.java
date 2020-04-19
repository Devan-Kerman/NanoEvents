package net.devtech.nanoevents.api;

import net.devtech.nanoevents.NanoEvents;
import net.devtech.nanoevents.plugin.NanoEventMixinPlugin;
import net.devtech.nanoevents.util.Id;
import net.devtech.nanoevents.util.MixinPath;

public class NanoEventsAPI {
	/**
	 * if, for whatever reason you need to call 2 events from the same mixin class,
	 * you can call this method to check whether or not a specific Id was activated or not
	 */
	public static boolean isEnabled(Id val) {
		return NanoEvents.LISTENERS.containsKey(val);
	}

	/**
	 * checks if the mixin was applied
	 *
	 * or, for internal use: should this mixin be applied
	 */
	public static boolean wasApplied(String mixinClassName) {
		for (MixinPath path : NanoEvents.ENABLED) {
			if (path.matches(mixinClassName)) {
				return true;
			}
		}
		return false;
	}
}
