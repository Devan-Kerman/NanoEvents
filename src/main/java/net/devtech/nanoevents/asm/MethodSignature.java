package net.devtech.nanoevents.asm;

public class MethodSignature {
	private final String type;
	private final String name;
	private final String signature;

	public MethodSignature(String type, String name, String signature) {
		this.type = type;
		this.name = name;
		this.signature = signature;
	}

	public String getType() {
		return this.type;
	}

	public String getName() {
		return this.name;
	}

	public String getSignature() {
		return this.signature;
	}
}
