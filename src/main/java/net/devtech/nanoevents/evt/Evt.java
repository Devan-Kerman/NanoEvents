package net.devtech.nanoevents.evt;

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
 * // normal properties file inside but all whitespaces are removed (except newlines), including the ones in strings
 * }
 */
public class Evt {
	/**
	 * the id of the event
	 */
	private final Id id;
	/**
	 * the class the invoker is in
	 */
	private final String invokerClass;
	/**
	 * the mixin predicates, this determines whether or not a mixin should be applied
	 */
	private final Collection<MixinPath> mixins;

	/**
	 * true if the event has no side effects when there are no listeners
	 */
	private final boolean noOp;


	public Evt(Id id, String invokerClass, Collection<MixinPath> mixins, boolean op) {
		this.id = id;
		this.invokerClass = invokerClass;
		this.mixins = mixins;
		this.noOp = op;
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
				idBuilder.append((char) chr);
			}
		}

		if (chr == -1) throw new EOFException();

		// read the inner scope thing
		StringBuilder inner = new StringBuilder();
		while ((chr = reader.read()) != -1) {
			if (!Character.isWhitespace(chr) || chr == '\n') {
				if (chr == '}') break;
				inner.append((char) chr);
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
		String path = properties.getProperty("classes");
		// isn't this just "true".equals? not for nulls it isn't
		// it's basically true-by-default
		boolean noop = !"false".equals(properties.getProperty("noOp"));

		Collection<MixinPath> paths = new ArrayList<>();
		if (path != null) {
			String[] classes = path.split(",");
			for (String s : classes) {
				paths.add(new MixinPath(s));
			}
		}

		return new Pair<>(new Evt(id, invoker, paths, noop), end);
	}

	public Id getId() {
		return this.id;
	}

	public String getInvokerClass() {
		return this.invokerClass;
	}

	public Collection<MixinPath> getMixins() {
		return this.mixins;
	}

	public boolean isNoOp() {
		return this.noOp;
	}
}
