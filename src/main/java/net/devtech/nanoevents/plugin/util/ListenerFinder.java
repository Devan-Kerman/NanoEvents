package net.devtech.nanoevents.plugin.util;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.Logger;

public class ListenerFinder {
	private static final Logger LOGGER = Logger.getLogger("NanoEvents");

	public static Map<Id, String> findEventHandlers() {
		Map<Id, String> listeners = new HashMap<>();
		findProperties("nano_handlers", (c, m) -> parseEventHandlers(listeners, m, c));
		return listeners;
	}

	public static Map<String, Collection<String>> findListeners(Map<Id, String> registered) {
		Map<String, Collection<String>> listeners = new HashMap<>();
		findProperties("nano_listeners", (c, m) -> parseEventListeners(registered, listeners, m, c));
		return listeners;
	}

	public static Map<String, String> findInvokers(Map<Id, String> registered) {
		Map<String, String> listeners = new HashMap<>();
		findProperties("nano_listeners", (c, m) -> parseInvokers(registered, listeners, m, c));
		return listeners;
	}

	private static void parseInvokers(Map<Id, String> registered, Map<String, String> invokers, ModContainer mod, CustomValue value) {
		parseNamespacedProperties("invoker", value, mod, (i, v) -> {
			// basic first check before invokers are found
			String val = registered.get(i);
			if (val != null) {
				invokers.put(val, v);
			} else LOGGER.severe("No event handler for id " + i);
		}, null);
	}

	private static void parseEventHandlers(Map<Id, String> eventHandlers, ModContainer mod, CustomValue value) {
		parseNamespacedProperties("handler", value, mod, eventHandlers::put, p -> Optional.ofNullable(p.remove("package")).map(v -> v + ".").orElse(""));
	}

	private static void parseEventListeners(Map<Id, String> registered, Map<String, Collection<String>> used, ModContainer container, CustomValue value) {
		parseNamespacedProperties("listener", value, container, (i, v) -> {
			String reg = registered.get(i);
			if (reg != null) { // ensure there is a registered event provider for the id
				String[] parsed = v.split("\\|");
				for (String s : parsed) { // add the mod event listener class to the used map
					if (v.indexOf('#') != -1) {
						used.computeIfAbsent(reg, i2 -> new ArrayList<>()).add(s);
					} else LOGGER.severe("Invalid method pointer '" + v + "' must be path.to.Class#myMethod");
				}
			} else LOGGER.severe("No Event Provider Found For Id: " + i + " in '" + value + "'");
		}, p -> Optional.ofNullable(p.remove("package")).map(v -> v + ".").orElse(""));
	}

	private static void parseNamespacedProperties(String thing, CustomValue value, ModContainer mod, BiConsumer<Id, String> propertyConsumer, Function<Properties, String> prefixProcessor) {
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
				// prefix all values with this
				String prefix = prefixProcessor == null ? "" : prefixProcessor.apply(properties);
				// iterate through the rest of the entries
				properties.forEach((k, v) -> {
					String key = (String) k;
					int colonIndex = key.indexOf(':');
					if (colonIndex != -1) { // validate actual namespace mod:event
						Id id = new Id(key.substring(0, colonIndex), key.substring(colonIndex + 1));
						propertyConsumer.accept(id, prefix + v);
					} else LOGGER.severe("Invalid namespace " + key + " in " + path);
				});
			} catch (IOException e) {
				LOGGER.severe("Error when reading nano " + thing + " properties: " + path);
				e.printStackTrace();
			}
		} else {
			LOGGER.severe("Invalid type in 'nano_" + thing + "' can only be a string, or array of strings");
			LOGGER.severe(mod.getMetadata().getName() + " has an invalid NanoEvent " + thing + " declaration!");
			LOGGER.severe("Erroring jar: " + mod.getRootPath().toString());
		}
	}

	private static void findProperties(String key, BiConsumer<CustomValue, ModContainer> consumer) {
		for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
			CustomValue metadata = mod.getMetadata().getCustomValue(key);
			if (metadata != null) {
				// can be an array of property files or just one
				if (metadata.getType() == CustomValue.CvType.ARRAY) {
					for (CustomValue listener : metadata.getAsArray()) {
						consumer.accept(listener, mod);
					}
				} else {
					consumer.accept(metadata, mod);
				}
			}
		}
	}


}
