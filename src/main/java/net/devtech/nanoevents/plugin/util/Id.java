package net.devtech.nanoevents.plugin.util;

public final class Id {
	public final String mod;
	public final String name;
	private final int hashcode;
	public Id(String mod, String name) {
		this.mod = mod;
		this.name = name;

		int result = this.mod.hashCode();
		result = 31 * result + this.name.hashCode();
		this.hashcode = result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Id)) return false;

		Id id = (Id) o;

		if (!this.mod.equals(id.mod)) return false;
		return this.name.equals(id.name);
	}

	@Override
	public int hashCode() {
		return this.hashcode;
	}
}
