package application.system.file;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * @see PathLayerHelper#walkFileTree(PathLayer, int, boolean, PathLayerVisitor)
 * @see Files#walkFileTree(java.nio.file.Path, java.nio.file.FileVisitor)
 */
class PathLayerTreeWalker implements Closeable {
	private boolean includeHiddenFiles;
	private final int maxDepth;
	private final ArrayDeque<DirectoryNode> stack = new ArrayDeque<>();
	private boolean closed;

	/**
	 * The element on the walking stack corresponding to a directory node.
	 */
	private static class DirectoryNode {
		private final PathLayer dir;
		private final Stream<PathLayer> stream;
		private final Iterator<PathLayer> iterator;
		private boolean skipped;

		DirectoryNode(PathLayer dir, Stream<PathLayer> stream) {
			this.dir = dir;
			this.stream = stream;
			iterator = stream.iterator();
		}

		PathLayer directory() {
			return dir;
		}

		Stream<PathLayer> stream() {
			return stream;
		}

		Iterator<PathLayer> iterator() {
			return iterator;
		}

		void skip() {
			skipped = true;
		}

		boolean skipped() {
			return skipped;
		}
	}

	/**
	 * The event types.
	 */
	static enum EventType {
		/**
		 * Start of a directory
		 */
		START_DIRECTORY,
		/**
		 * End of a directory
		 */
		END_DIRECTORY,
		/**
		 * An entry in a directory
		 */
		ENTRY;
	}

	/**
	 * Events returned by the {@link #walk} and {@link #next} methods.
	 */
	static class Event {
		private final EventType type;
		private final PathLayer file;
		private final IOException ioe;

		Event(EventType type, PathLayer file, IOException ioe) {
			this.type = type;
			this.file = file;
			this.ioe = ioe;
		}

		public Event(EventType entry, PathLayer file) {
			this(entry, file, null);
		}

		EventType type() {
			return type;
		}

		PathLayer file() {
			return file;
		}

		IOException ioeException() {
			return ioe;
		}
	}

	/**
	 * Creates a {@code FileTreeWalker}.
	 *
	 * @param includeHiddenFiles
	 *
	 * @throws IllegalArgumentException if {@code maxDepth} is negative
	 */
	PathLayerTreeWalker(int maxDepth, boolean includeHiddenFiles) {
		if (maxDepth < 0) {
			throw new IllegalArgumentException("'maxDepth' is negative");
		}

		this.maxDepth = maxDepth;
		this.includeHiddenFiles = includeHiddenFiles;
	}

	/**
	 * Visits the given file, returning the {@code Event} corresponding to that
	 * visit.
	 *
	 * The {@code ignoreSecurityException} parameter determines whether any
	 * SecurityException should be ignored or not. If a SecurityException is thrown,
	 * and is ignored, then this method returns {@code null} to mean that there is
	 * no event corresponding to a visit to the file.
	 *
	 */
	private Event visit(PathLayer entry, boolean ignoreSecurityException) {
		// at maximum depth or file is not a directory
		int depth = stack.size();
		if (depth >= maxDepth || !entry.isDirectory()) {
			return new Event(EventType.ENTRY, entry);
		}

		// file is a directory, attempt to open it
		Stream<PathLayer> stream = null;
		try {
			if (includeHiddenFiles) {
				stream = entry.listPathLayers().stream();
			} else {
				stream = entry.listNoHiddenPathLayers().stream();
			}
		} catch (IOException ioe) {
			return new Event(EventType.ENTRY, entry, ioe);
		} catch (SecurityException se) {
			if (ignoreSecurityException) {
				return null;
			}
			throw se;
		}

		// push a directory node to the stack and return an event
		stack.push(new DirectoryNode(entry, stream));
		return new Event(EventType.START_DIRECTORY, entry);
	}

	/**
	 * Start walking from the given file.
	 */
	Event walk(PathLayer file) {
		if (closed) {
			throw new IllegalStateException("Closed");
		}

		Event ev = visit(file, false // ignoreSecurityException
		);
		assert ev != null;
		return ev;
	}

	/**
	 * Returns the next Event or {@code null} if there are no more events or the
	 * walker is closed.
	 */
	Event next() {
		DirectoryNode top = stack.peek();
		if (top == null) {
			return null; // stack is empty, we are done
		}

		// continue iteration of the directory at the top of the stack
		Event ev;
		do {
			PathLayer entry = null;
			IOException ioe = null;

			// get next entry in the directory
			if (!top.skipped()) {
				Iterator<PathLayer> iterator = top.iterator();
				try {
					if (iterator.hasNext()) {
						entry = iterator.next();
					}
				} catch (DirectoryIteratorException x) {
					ioe = x.getCause();
				}
			}

			// no next entry so close and pop directory, creating corresponding event
			if (entry == null) {
//				try {
				top.stream().close();
//				} catch (IOException e) {
//					if (ioe != null) {
//						ioe = e;
//					} else {
//						ioe.addSuppressed(e);
//					}
//				}
				stack.pop();
				return new Event(EventType.END_DIRECTORY, top.directory(), ioe);
			}

			// visit the entry
			ev = visit(entry, true // ignoreSecurityException
			);

		} while (ev == null);

		return ev;
	}

	/**
	 * Pops the directory node that is the current top of the stack so that there
	 * are no more events for the directory (including no END_DIRECTORY) event. This
	 * method is a no-op if the stack is empty or the walker is closed.
	 */
	void pop() {
		if (!stack.isEmpty()) {
			DirectoryNode node = stack.pop();
//			try {
			node.stream().close();
//			} catch (IOException ignore) {
//			}
		}
	}

	/**
	 * Skips the remaining entries in the directory at the top of the stack. This
	 * method is a no-op if the stack is empty or the walker is closed.
	 */
	void skipRemainingSiblings() {
		if (!stack.isEmpty()) {
			stack.peek().skip();
		}
	}

	/**
	 * Returns {@code true} if the walker is open.
	 */
	boolean isOpen() {
		return !closed;
	}

	/**
	 * Closes/pops all directories on the stack.
	 */
	@Override
	public void close() {
		if (!closed) {
			while (!stack.isEmpty()) {
				pop();
			}
			closed = true;
		}
	}

}
