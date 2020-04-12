package net.devtech.nanoevents.plugin.util;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.minecraft.util.Identifier;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

public class ListenerFinder {
	private static final Logger LOGGER = Logger.getLogger("NanoEvents");

	public static Map<Identifier, String> findEventHandlers() {
		Map<Identifier, String> listeners = new HashMap<>();
		for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
			CustomValue metadata = mod.getMetadata().getCustomValue("nano_events");
			if (metadata != null) {
				// can be an array of property files or just one
				if (metadata.getType() == CustomValue.CvType.ARRAY) {
					for (CustomValue listener : metadata.getAsArray()) {
						parse(listeners, mod, listener);
					}
				} else {
					parse(listeners, mod, metadata);
				}
			}
		}
		return listeners;
	}

	private static void parse(Map<Identifier, String> eventHandlers, ModContainer mod, CustomValue value) {
		if (value.getType() == CustomValue.CvType.STRING) {
			String listenerProperties = value.getAsString();
			// get the event properties
			if (!listenerProperties.endsWith(".properties")) listenerProperties += ".properties";

			Path path = mod.getPath(listenerProperties);
			try {
				// load event properties
				Properties properties = new Properties();
				BufferedInputStream input = new BufferedInputStream(Files.newInputStream(path));
				properties.load(input);
				input.close();
				// go through properties
				properties.forEach((k, v) -> {
					String key = (String) k;
					int colonIndex = key.indexOf(':');
					if (colonIndex != -1) { // validate actual namespace mod:event
						Identifier id = new Identifier(key.substring(0, colonIndex), key.substring(colonIndex + 1));
						eventHandlers.put(id, (String) v);
					} else LOGGER.severe("Invalid namespace " + key + " in " + path);
				});
			} catch (IOException e) {
				LOGGER.severe("Error when reading nano event properties: " + path);
				e.printStackTrace();
			}
		} else {
			LOGGER.severe("Invalid type in 'nano_events' can only be a string, or array of strings");
			LOGGER.severe(mod.getMetadata().getName() + " has an invalid NanoEvent event declaration!");
			LOGGER.severe("Erroring jar: " + mod.getRootPath().toString());
		}
	}

	public static Map<String, Collection<String>> findListeners(Map<Identifier, String> registered) {
		Map<String, Collection<String>> listeners = new HashMap<>();
		for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
			// put path to event properties in mod json
			CustomValue metadata = mod.getMetadata().getCustomValue("nano_listeners");
			if (metadata != null) {
				// can be an array of property files or just one
				if (metadata.getType() == CustomValue.CvType.ARRAY) {
					for (CustomValue listener : metadata.getAsArray()) {
						set(registered, listeners, mod, listener);
					}
				} else {
					set(registered, listeners, mod, metadata);
				}
			}
		}
		return listeners;
	}

	private static void set(Map<Identifier, String> registered, Map<String, Collection<String>> used, ModContainer container, CustomValue value) {
		if (value.getType() == CustomValue.CvType.STRING) {
			String listenerProperties = value.getAsString();
			// get the event properties
			if (!listenerProperties.endsWith(".properties")) listenerProperties += ".properties";

			Path path = container.getPath(listenerProperties);
			try {
				// load event properties
				Properties properties = new Properties();
				BufferedInputStream input = new BufferedInputStream(Files.newInputStream(path));
				properties.load(input);
				input.close();
				// optional cus lazy and lambda complained about final
				String pack = (String) Optional.ofNullable(properties.remove("package")).orElse("");
				// iterate through the rest of the entries
				properties.forEach((k, v) -> {
					String key = (String) k;
					int colonIndex = key.indexOf(':');
					if (colonIndex != -1) { // validate actual namespace mod:event
						Identifier id = new Identifier(key.substring(0, colonIndex), key.substring(colonIndex + 1));
						String registeredPackage = registered.get(id);
						if (registeredPackage != null) { // ensure there is a registered event provider for the id
							String[] parsed = ((String) v).split("\\|");
							for (String s : parsed) { // add the mod event listener class to the used map
								used.computeIfAbsent(registeredPackage, i -> new ArrayList<>()).add(pack + s);
							}
						} else LOGGER.severe("No Event Provider Found For Id: " + id + " in " + path);
					} else LOGGER.severe("Invalid namespace " + key + " in " + path);
				});
			} catch (IOException e) {
				LOGGER.severe("Error when reading nano listener properties: " + path);
				e.printStackTrace();
			}
		} else {
			LOGGER.severe("Invalid type in 'nano_listeners' can only be a string, or array of strings");
			LOGGER.severe(container.getMetadata().getName() + " has an invalid NanoEvent listener declaration!");
			LOGGER.severe("Erroring jar: " + container.getRootPath().toString());
		}
	}
}
