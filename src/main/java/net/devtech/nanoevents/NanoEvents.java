package net.devtech.nanoevents;

import net.devtech.nanoevents.evt.Evt;
import net.devtech.nanoevents.util.Id;
import net.devtech.nanoevents.util.MixinPath;
import net.fabricmc.api.ModInitializer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// todo check bytecode signatures of listeners
// todo check access flags of listeners | force public
// todo dynamic registry
// todo remove test code, events.evt, listener.properties, fabric.mod.json custom
public class NanoEvents implements ModInitializer {
	// ========== do not touch ==========
	@Deprecated public static final List<MixinPath> ENABLED = new ArrayList<>();
	@Deprecated public static final Map<Id, List<String>> LISTENERS = Finder.parseListeners();
	@Deprecated public static final Map<Id, Evt> EVENTS = Finder.getAllEvts();

	static {
		for (Id id : LISTENERS.keySet()) {
			if (LISTENERS.containsKey(id)) {
				Evt evt = EVENTS.get(id);
				if (evt != null) ENABLED.addAll(evt.getMixins());
			}
		}
	}

	@Override
	public void onInitialize() {
		// remove unessesary stuff from memory
		ENABLED.clear();
		EVENTS.clear();
	}
	// ========== do not touch ==========
}
