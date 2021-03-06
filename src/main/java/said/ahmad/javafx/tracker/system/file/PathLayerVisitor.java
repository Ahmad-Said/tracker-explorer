package said.ahmad.javafx.tracker.system.file;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;

/**
 * A visitor of files. An implementation of this interface is provided to the
 * {@link PathLayerHelper#walkFileTree PathLayerHelper.walkFileTree} methods to
 * visit each file in a file tree. <br>
 *
 * @see {@link FileVisitor} for similar functionality
 */

public interface PathLayerVisitor<T extends PathLayer> {

	/**
	 * Invoked for a directory before entries in the directory are visited.
	 *
	 * <p>
	 * If this method returns {@link FileVisitResult#CONTINUE CONTINUE}, then
	 * entries in the directory are visited. If this method returns
	 * {@link FileVisitResult#SKIP_SUBTREE SKIP_SUBTREE} or
	 * {@link FileVisitResult#SKIP_SIBLINGS SKIP_SIBLINGS} then entries in the
	 * directory (and any descendants) will not be visited.
	 *
	 * @param dir a reference to the directory
	 *
	 * @return the visit result
	 *
	 * @throws IOException if an I/O error occurs
	 */
	FileVisitResult preVisitDirectory(T dir) throws IOException;

	/**
	 * Invoked for a file in a directory.
	 *
	 * @param file a reference to the file
	 *
	 * @return the visit result
	 *
	 * @throws IOException if an I/O error occurs
	 */
	FileVisitResult visitFile(T file) throws IOException;

	/**
	 * Invoked for a file that could not be visited. This method is invoked if the
	 * file's attributes could not be read, the file is a directory that could not
	 * be opened, and other reasons.
	 *
	 * @param file a reference to the file
	 * @param exc  the I/O exception that prevented the file from being visited
	 *
	 * @return the visit result
	 *
	 * @throws IOException if an I/O error occurs
	 */
	FileVisitResult visitFileFailed(T file, IOException exc) throws IOException;

	/**
	 * Invoked for a directory after entries in the directory, and all of their
	 * descendants, have been visited. This method is also invoked when iteration of
	 * the directory completes prematurely (by a {@link #visitFile visitFile} method
	 * returning {@link FileVisitResult#SKIP_SIBLINGS SKIP_SIBLINGS}, or an I/O
	 * error when iterating over the directory).
	 *
	 * @param dir a reference to the directory
	 * @param exc {@code null} if the iteration of the directory completes without
	 *            an error; otherwise the I/O exception that caused the iteration of
	 *            the directory to complete prematurely
	 *
	 * @return the visit result
	 *
	 * @throws IOException if an I/O error occurs
	 */
	FileVisitResult postVisitDirectory(T dir, IOException exc) throws IOException;
}
