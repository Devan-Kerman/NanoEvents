package net.devtech.nanoevents;

import net.devtech.nanoevents.evtparser.Evt;
import net.devtech.nanoevents.evtparser.Id;
import net.devtech.nanoevents.evtparser.MixinPath;
import net.devtech.nanoevents.plugin.NanoEventMixinPlugin;
import java.util.*;

// todo remap method signatures?
public class NanoEvents {
	public static final List<MixinPath> ENABLED = new ArrayList<>();
	public static final Map<Id, Collection<String>> LISTENERS = Finder.parseListeners();
	public static final Map<Id, Evt> EVENTS = Finder.getAllEvts();
	static {
		for (Id id : LISTENERS.keySet()) {
			if (LISTENERS.containsKey(id)) ENABLED.addAll(EVENTS.get(id).getMixins());
		}
	}

	/**
	 * if, for whatever reason you need to call 2 events from the same mixin class,
	 * you can call this method to check whether or not a specific Id was activated or not
	 */
	public static boolean isEnabled(Id val) {
		return NanoEventMixinPlugin.contains(val);
	}

	/**
	 * checks if the mixin was applied
	 */
	public static boolean wasApplied(String mixinClassName) {
		return NanoEventMixinPlugin.contains(mixinClassName);
	}
}
