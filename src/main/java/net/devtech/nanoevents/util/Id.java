package net.devtech.nanoevents.util;

/**
 * a namespace, basically just {@link net.minecraft.util.Identifier} but not a minecraft class
 * so it's safer to use in mixin plugins and early stuff
 */
public class Id {
	/**
	 * the mod that provides the event
	 */
	public final String mod;

	/**
	 * the name/val of the provided event
	 */
	public final String value;

	public Id(String id) {
		int colonIndex = id.indexOf(':');
		if(colonIndex == -1)
			throw new IllegalArgumentException(id + " is an invalid id");
		this.mod = id.substring(0, colonIndex);
		this.value = id.substring(colonIndex + 1);
	}

	public Id(String mod, String value) {
		this.mod = mod;
		this.value = value;
	}

	@Override
	public int hashCode() {
		int result = this.mod.hashCode();
		result = 31 * result + this.value.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || this.getClass() != o.getClass()) return false;

		Id id = (Id) o;

		if (!this.mod.equals(id.mod)) return false;
		return this.value.equals(id.value);
	}

	@Override
	public String toString() {
		return this.mod + ":" + this.value;
	}
}
