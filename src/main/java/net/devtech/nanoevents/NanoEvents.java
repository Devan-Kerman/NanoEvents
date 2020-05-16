package net.devtech.nanoevents;

import net.devtech.nanoevents.evt.Evt;
import net.devtech.nanoevents.util.Id;
import net.devtech.nanoevents.util.MixinPath;
import net.fabricmc.api.ModInitializer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// soon:tm:
// todo dynamic registry

// == requires partial rewrite? ==
// todo mutliple start/end cut and pastes

// == requires listener modification ==
// todo check bytecode signatures of listeners
// todo check access flags of listeners or force public
// todo multi-invokers?

// == before publish ==
// todo remove test code, events.evt, listener.properties, fabric.mod.json custom
public class NanoEvents implements ModInitializer {
	// ========== do not touch ==========
	@Deprecated public static final List<MixinPath> ENABLED = new ArrayList<>();
	@Deprecated public static final Map<Id, List<String>> LISTENERS = Finder.parseListeners();
	@Deprecated public static final Map<Id, Evt> EVENTS = Finder.getAllEvts();

	static {
		for (Evt value : EVENTS.values()) {
			if (LISTENERS.containsKey(value.getId()) || !value.isNoOp()) {
				ENABLED.addAll(value.getMixins());
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
