package said.ahmad.javafx.tracker.system.operation;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.util.Queue;
import java.util.Stack;

import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.tracker.system.file.PathLayerVisitor;
import said.ahmad.javafx.tracker.system.operation.FileHelper.ActionOperation;

class FileHelperOperationWalker implements PathLayerVisitor<PathLayer> {

	private Queue<PathLayer> sourceList;
	private Queue<PathLayer> targetList;
	private Stack<PathLayer> toDeleteLater; // only on move operation
	private ActionOperation actionOperation;

	/** Temporary data */
	private Stack<PathLayer> rootDirListingTarget;

	public FileHelperOperationWalker(PathLayer targetDirectory, Queue<PathLayer> sourceList,
			Queue<PathLayer> targetList, Stack<PathLayer> toDeleteLater, ActionOperation action) {
		this.sourceList = sourceList;
		this.targetList = targetList;
		this.toDeleteLater = toDeleteLater;
		actionOperation = action;

		/** Temporary data */
		rootDirListingTarget = new Stack<>();
		rootDirListingTarget.push(targetDirectory);
	}

	@Override
	public FileVisitResult preVisitDirectory(PathLayer dir) throws IOException {
		sourceList.add(dir);

		PathLayer targetDir = rootDirListingTarget.peek().resolve(dir.getName());
		targetList.add(targetDir);

		rootDirListingTarget.push(targetDir);

		if (actionOperation.equals(ActionOperation.MOVE)) {
			toDeleteLater.add(dir);
		}

		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(PathLayer file) throws IOException {
		sourceList.add(file);
		targetList.add(rootDirListingTarget.peek().resolve(file.getName()));

		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(PathLayer file, IOException exc) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory(PathLayer dir, IOException exc) throws IOException {
		rootDirListingTarget.pop();

		return FileVisitResult.CONTINUE;
	}

}
