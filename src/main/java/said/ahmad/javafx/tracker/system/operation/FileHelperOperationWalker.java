package said.ahmad.javafx.tracker.system.operation;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.tracker.system.file.PathLayerVisitor;
import said.ahmad.javafx.tracker.system.operation.FileHelper.ActionOperation;

@Getter
class FileHelperOperationWalker implements PathLayerVisitor<PathLayer> {

	private Queue<PathLayer> sourceList;
	private Queue<PathLayer> targetList;
	private Stack<PathLayer> toDeleteLater; // only on move operation
	private ActionOperation actionOperation;


	/**
	 * Map from source files to their new names. This will create target file in its
	 * default location in walk tree but will use the new name instead of original
	 * name. It is useful when a file name has changed during a copy/move operation.
	 */
	@Nullable
	private Map<PathLayer, String> sourceToRename;

	/** Temporary data */
	private Stack<PathLayer> rootDirListingTarget;
	
	public FileHelperOperationWalker(PathLayer targetDirectory, Queue<PathLayer> sourceList,
			Queue<PathLayer> targetList, Stack<PathLayer> toDeleteLater, ActionOperation action) {
		this(targetDirectory, sourceList, targetList, toDeleteLater, action, null);
	}
	public FileHelperOperationWalker(PathLayer targetDirectory, Queue<PathLayer> sourceList,
			Queue<PathLayer> targetList, Stack<PathLayer> toDeleteLater, ActionOperation action,
			Map<PathLayer, String> sourceToRename) {
		this.sourceList = sourceList;
		this.targetList = targetList;
		this.toDeleteLater = toDeleteLater;
		actionOperation = action;
		this.sourceToRename = sourceToRename;

		/** Temporary data */
		rootDirListingTarget = new Stack<>();
		rootDirListingTarget.push(targetDirectory);
	}

	@Override
	public FileVisitResult preVisitDirectory(PathLayer dir) throws IOException {
		sourceList.add(dir);

		PathLayer targetDir = rootDirListingTarget.peek().resolve(getName(dir));
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
		targetList.add(rootDirListingTarget.peek().resolve(getName(file)));

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


	private String getName(PathLayer path) {
		if(this.sourceToRename != null && this.sourceToRename.containsKey(path)){
			return this.sourceToRename.get(path);
		} else {
			return path.getName();
		}
	}

}
