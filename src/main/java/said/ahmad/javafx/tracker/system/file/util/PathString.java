package said.ahmad.javafx.tracker.system.file.util;

/**
 * Does return parent/resolved path from a string based on a file separator if
 * you want to change {@link #DEFAULT_FILE_SEPARATOR} just extend this class and
 * override {@link #getFILE_SEPARATOR()}
 */
public class PathString {
	private static final String DEFAULT_FILE_SEPARATOR = "/";

	protected String path;

	public String getFILE_SEPARATOR() {
		return DEFAULT_FILE_SEPARATOR;
	};

	public PathString(String path) {
		this.path = path;
	}

	public String get() {
		return path;
	}

	public String getName() {
		String name = null;
		int lastSeparator = path.lastIndexOf(getFILE_SEPARATOR());
		if (lastSeparator + 1 < path.length()) {
			name = path.substring(lastSeparator + 1);
		}
		return name;
	}

	public int getNameCount() {
		return path.split(getFILE_SEPARATOR()).length;
	}

	public String getParent() {
		String parent = path;
		int lastSeparator = parent.lastIndexOf(getFILE_SEPARATOR());
		// just a separator
		if (path.length() == 1 || lastSeparator < 0) {
			return null;
		}
		if (lastSeparator == 0) {
			// the root folder
			return getFILE_SEPARATOR();
		}
		return path.substring(0, lastSeparator);
	}

	public static String getParent(String path) {
		PathString pathString = new PathString(path);
		return pathString.getParent();
	}

	public String resolve(String other) {
		return normalize(path + getFILE_SEPARATOR() + other);
	}

	public String resolveSibling(String other) {
		return getParent() == null ? getFILE_SEPARATOR() + other : normalize(getParent() + getFILE_SEPARATOR() + other);
	}

	/**
	 * clean any consecutive file separator
	 *
	 *
	 * @param path
	 * @return
	 */
	private String normalize(String path) {
		return path.replaceAll(getFILE_SEPARATOR() + "+", getFILE_SEPARATOR());
	}

	@Override
	public String toString() {
		return path;
	}
}
