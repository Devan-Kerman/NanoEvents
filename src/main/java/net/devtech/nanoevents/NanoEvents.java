package net.devtech.nanoevents;

import net.devtech.nanoevents.evt.Evt;
import net.devtech.nanoevents.util.Id;
import net.devtech.nanoevents.util.MixinPath;
import net.fabricmc.api.ModInitializer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// todo add a throw exception at the top of event handler methods that suck ass 
// todo check bytecode signatures of listeners
// todo check access flags of listeners | force public
// todo remap method signatures?
public class NanoEvents implements ModInitializer {
	// ========== do not touch ==========
	@Deprecated public static final List<MixinPath> ENABLED = new ArrayList<>();
	@Deprecated public static final Map<Id, List<String>> LISTENERS = Finder.parseListeners();
	@Deprecated public static final Map<Id, Evt> EVENTS = Finder.getAllEvts();

	static {
		for (Id id : LISTENERS.keySet()) {
			if (LISTENERS.containsKey(id)) {
				ENABLED.addAll(EVENTS.get(id).getMixins());
			}
		}
	}

	@Override
	public void onInitialize() {
		ENABLED.clear();
		EVENTS.clear();
	}
	// ========== do not touch ==========
}
