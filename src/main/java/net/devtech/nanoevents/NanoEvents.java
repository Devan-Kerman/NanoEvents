package net.devtech.nanoevents;

import net.devtech.nanoevents.evtparser.Evt;
import net.devtech.nanoevents.util.Id;
import net.devtech.nanoevents.util.MixinPath;
import net.devtech.nanoevents.plugin.NanoEventMixinPlugin;
import java.util.*;

// todo check bytecode signatures of listeners
// todo check access flags of listeners | force public
// todo remap method signatures?
public class NanoEvents {
	// ========== do not touch ==========
	@Deprecated
	public static final List<MixinPath> ENABLED = new ArrayList<>();
	@Deprecated
	public static final Map<Id, Collection<String>> LISTENERS = Finder.parseListeners();
	@Deprecated
	public static final Map<Id, Evt> EVENTS = Finder.getAllEvts();
	static {
		for (Id id : LISTENERS.keySet()) {
			if (LISTENERS.containsKey(id)) ENABLED.addAll(EVENTS.get(id).getMixins());
		}
	}
	// ========== do not touch ==========
}
