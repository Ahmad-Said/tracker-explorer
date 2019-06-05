package application;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

import application.model.Setting;

public class RecursiveFileWalker implements FileVisitor<Path> {

	private long filesCount;
	private Set<Path> Parent = new HashSet<>();
	private Set<Path> allFiles = new HashSet<>();
	private boolean GetFiles;

	public RecursiveFileWalker() {
		// TODO Auto-generated constructor stub
	}

	public RecursiveFileWalker(boolean doGetFiles) {
		GetFiles = doGetFiles;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		// this is used because later we gonna sort them as depth
		// getNameCount() return the depth of current file
		// so it is useless to go further if there exist more files than to show
		// but it is important to define all node to make bfs work correctly
		if (filesCount > Setting.getMaxLimitFilesRecursive()) {
			if (dir.getNameCount() > Setting.getMaxDepthFilesRecursive())
				return FileVisitResult.SKIP_SIBLINGS;
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		// This is where I need my logic
		Parent.add(file.getParent());
		// check later if it impact speed
		if (GetFiles)
			allFiles.add(file);
		filesCount++;
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
		// This is important to note. Test this behaviour
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	public long getFilesCount() {
		return filesCount;
	}

	public Set<Path> getParent() {
		return Parent;
	}

	public Set<Path> getAllFiles() {
		return allFiles;
	}
}
