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
}
