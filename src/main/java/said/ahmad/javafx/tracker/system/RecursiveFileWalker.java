package said.ahmad.javafx.tracker.system;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import said.ahmad.javafx.tracker.app.pref.Setting;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.tracker.system.file.PathLayerHelper;
import said.ahmad.javafx.tracker.system.file.PathLayerVisitor;

/**
 * @see PathLayerHelper#walkFileTree(PathLayer, int, boolean, PathLayerVisitor)
 */
public class RecursiveFileWalker implements PathLayerVisitor<PathLayer> {

	private long filesCount;
	private HashMap<PathLayer, ArrayList<PathLayer>> directoriesToFiles = new HashMap<>();
	private HashSet<PathLayer> directories = new HashSet<>();
	/** Temporary data */
	private Stack<PathLayer> rootDirListing = new Stack<>();

	public RecursiveFileWalker() {
	}

	@Override
	public FileVisitResult preVisitDirectory(PathLayer dir) throws IOException {
		// this is used because later we gonna sort them as depth
		// getNameCount() return the depth of current file
		// so it is useless to go further if there exist more files than to show
		// but it is important to define all node to make bfs work correctly
		if (rootDirListing.size() != 0) {
			directoriesToFiles.get(rootDirListing.peek()).add(dir);
		}
		rootDirListing.push(dir);
		directoriesToFiles.put(dir, new ArrayList<>());
		if (filesCount > Setting.getMaxLimitFilesRecursive()) {
			if (dir.getNameCount() > Setting.getMaxDepthFilesRecursive()) {
				return FileVisitResult.SKIP_SIBLINGS;
			} else {
				directories.add(dir);
			}
		} else {
			directories.add(dir);
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(PathLayer file) throws IOException {
		if (rootDirListing.size() != 0) {
			directoriesToFiles.get(rootDirListing.peek()).add(file);
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(PathLayer file, IOException exc) throws IOException {
		// This is important to note. Test this behavior
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory(PathLayer dir, IOException exc) throws IOException {
		rootDirListing.pop();
		return FileVisitResult.CONTINUE;
	}

	public long getFilesCount() {
		return filesCount;
	}

	public HashSet<PathLayer> getDirectories() {
		return directories;
	}

	/**
	 * @return the directoriesToFiles
	 */
	public HashMap<PathLayer, ArrayList<PathLayer>> getDirectoriesToFiles() {
		return directoriesToFiles;
	}

	public void clearAllRecords() {
		rootDirListing.clear();
		directoriesToFiles.clear();
		directories.clear();
	}
}
