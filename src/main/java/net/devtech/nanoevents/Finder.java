package net.devtech.nanoevents;

import net.devtech.nanoevents.evtparser.Evt;
import net.devtech.nanoevents.util.Id;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.util.Pair;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

/**
 * the util class for parsing listener and event files from mods
 */
class Finder {
	public static final Logger LOGGER = Logger.getLogger("EvtParser");

	/**
	 * find all the listeners declared in each mod
	 */
	public static Map<Id, Collection<String>> parseListeners() {
		Map<Id, Collection<String>> listeners = new HashMap<>();
		forVal("nano:lst", (m, c) -> {
			Path path = m.getPath(c);
			try {
				Properties properties = new Properties();
				properties.load(Files.newBufferedReader(path));
				for (Map.Entry<Object, Object> entry : properties.entrySet()) {
					String listener = (String) entry.getKey();
					String eventId = (String) entry.getValue();
					int colonIndex = eventId.indexOf(':');
					if (colonIndex == -1) {
						LOGGER.severe("Invalid identifier: " + eventId + " in " + c + " in " + m.getMetadata().getId());
					} else {
						listeners.computeIfAbsent(new Id(eventId.substring(0, colonIndex), eventId.substring(colonIndex + 1)), i -> new ArrayList<>()).add(listener);
					}
				}
			} catch (IOException e) {
				LOGGER.severe("error in reading listneers in " + path + " in mod " + m.getMetadata().getId());
				e.printStackTrace();
			}
		});
		return listeners;
	}

	/**
	 * util method for files in custom mod jsons
	 */
	private static void forVal(String val, BiConsumer<ModContainer, String> consumer) {
		for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
			ModMetadata metadata = mod.getMetadata();
			CustomValue value = metadata.getCustomValue(val);
			if(value != null) {
				if (value.getType() == CustomValue.CvType.STRING) consumer.accept(mod, value.getAsString());
				else if (value.getType() == CustomValue.CvType.ARRAY) for (CustomValue customValue : value.getAsArray()) {
					if (customValue.getType() == CustomValue.CvType.STRING) consumer.accept(mod, customValue.getAsString());
					else LOGGER.severe("Invalid type in array: " + value + " mod: " + metadata.getId());
				}
			}
		}
	}

	/**
	 * find all the events declared in each mod
	 */
	public static Map<Id, Evt> getAllEvts() {
		Map<Id, Evt> events = new HashMap<>();
		forVal("nano:evt", (m, c) -> {
			Path path = m.getPath(c);
			try {
				boolean cont = true;
				while (cont) {
					Pair<Evt, Boolean> pair = Evt.parse(Files.newBufferedReader(path));
					cont = pair.getRight();
					Evt evt = pair.getLeft();
					events.put(evt.getId(), evt);
				}
			} catch (IOException e) {
				LOGGER.severe("error in parsing events in " + path + " in mod " + m.getMetadata().getId());
				e.printStackTrace();
			}
		});
		return events;
	}
}
