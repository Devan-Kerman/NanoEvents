package net.devtech.nanoevents.evtparser;

import net.devtech.nanoevents.util.Id;
import net.devtech.nanoevents.util.MixinPath;
import net.minecraft.util.Pair;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

/**
 * the parser and container of data for the EVT file format,
 * the evt format is essentially
 * <p>
 * mod:namespace {
 * // normal properties file inside but all whitespaces are removed, including the ones in strings
 * }
 */
public class Evt {
	/**
	 * the id of the event
	 */
	private final Id id;
	/**
	 * the method signature of the invoker
	 */
	private final String invoker;
	/**
	 * the mixin predicates, this determines whether or not a mixin should be applied
	 */
	private final Collection<MixinPath> mixins;


	public Evt(Id id, String invoker, Collection<MixinPath> mixins) {
		this.id = id;
		this.invoker = invoker;
		this.mixins = mixins;
	}

	/**
	 * parse the Evt config
	 */
	public static Pair<Evt, Boolean> parse(Reader reader) throws IOException {
		boolean end = false;
		int chr;
		// find event namespace
		StringBuilder idBuilder = new StringBuilder();
		while ((chr = reader.read()) != -1) {
			if (!Character.isWhitespace(chr)) {
				if (chr == '{') break;
				idBuilder.append((char)chr);
			}
		}

		if (chr == -1) throw new EOFException();

		// read the inner scope thing
		StringBuilder inner = new StringBuilder();
		while ((chr = reader.read()) != -1) {
			if (!Character.isWhitespace(chr) || chr == '\n') {
				if (chr == '}') break;
				inner.append((char)chr);
			}
		}

		if (chr == -1) end = true;

		// load inside as properties
		Properties properties = new Properties();
		properties.load(new StringReader(inner.toString()));
		String[] name = idBuilder.toString().split(":");

		// the id of the event
		Id id = new Id(name[0], name[1]);
		String invoker = properties.getProperty("invoker");
		String[] path = properties.getProperty("classes").split(",");

		Collection<MixinPath> paths = new ArrayList<>();
		for (String s : path) {
			paths.add(new MixinPath(s));
		}

		return new Pair<>(new Evt(id, invoker, paths), end);
	}

	public Id getId() {
		return this.id;
	}

	public String getInvoker() {
		return this.invoker;
	}

	public Collection<MixinPath> getMixins() {
		return this.mixins;
	}
}
