package net.devtech.nanoevents.util;

/**
 * a path to a class
 * so like
 *
 * net.devtech.Class
 * or
 * net.devtech.nanoevents.*
 * etc.
 */
public class MixinPath {
	private final String path;

	public MixinPath(String path) {this.path = path;}

	/**
	 * checks if the class name fits in the path
	 */
	public boolean matches(String className) {
		if (this.path.length() > className.length()) // star matches will always be smaller
			return false;
		for (int i = 0; i < this.path.length(); i++) {
			char at = this.path.charAt(i);
			if (at == '*') return true;
			if (at != className.charAt(i)) {
				return false;
			}
		}
		return true;
	}
}
