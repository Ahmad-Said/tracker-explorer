package said.ahmad.javafx.tracker.system.operation;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import said.ahmad.javafx.tracker.app.DialogHelper;
import said.ahmad.javafx.tracker.app.Main;
import said.ahmad.javafx.tracker.app.StringHelper;
import said.ahmad.javafx.tracker.app.pref.Setting;
import said.ahmad.javafx.tracker.system.WatchServiceHelper;
import said.ahmad.javafx.tracker.system.call.TeraCopy;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.tracker.system.file.local.FilePathLayer;
import said.ahmad.javafx.tracker.system.tracker.FileTracker;
import said.ahmad.javafx.util.CallBackVoid;

public class FileHelper {

	/**
	 * <---COPY---> Copy operation <br>
	 * <---MOVE---> Move operation <br>
	 * <---DELETE---> Delete operation <br>
	 * <---RENAME---> Rename operation
	 */
	public static enum ActionOperation {
		COPY, MOVE, DELETE, RENAME
	}

	public static void copyWithTeraCopy(List<FilePathLayer> source, FilePathLayer targetDirectory) {
		try {
			FileTracker.operationUpdate(source, targetDirectory, ActionOperation.COPY);
			TeraCopy.copy(source, targetDirectory);
		} catch (IOException e) {
			e.printStackTrace();
			DialogHelper.showException(e);
		}
	}

	public static void copyFiles(List<? extends PathLayer> sourceFiles,  List<? extends PathLayer> targetFiles) {
		FileTracker.operationUpdateAsList(sourceFiles, targetFiles, ActionOperation.COPY);
		FileTracker.operationUpdateAsList(sourceFiles, targetFiles, ActionOperation.RENAME);
		FileHelperGUIOperation.getOperationsList()
				.add(new FileHelperGUIOperation(ActionOperation.COPY, sourceFiles, targetFiles));
		FileHelperGUIOperation.doThreadOperationsAllList();
	}

	public static void copy(List<? extends PathLayer> source, PathLayer targetDirectory) {
		FileTracker.operationUpdate(source, targetDirectory, ActionOperation.COPY);
		FileHelperGUIOperation.getOperationsList()
				.add(new FileHelperGUIOperation(ActionOperation.COPY, targetDirectory, source));
		FileHelperGUIOperation.doThreadOperationsAllList();
	}

	public static void moveWithTeraCopy(List<FilePathLayer> toOperatePath, FilePathLayer pathLayer) {
		try {
			FileTracker.operationUpdate(toOperatePath, pathLayer, ActionOperation.MOVE);
			TeraCopy.move(toOperatePath, pathLayer);
		} catch (IOException e) {
			e.printStackTrace();
			DialogHelper.showException(e);
		}
	}

	public static void moveFiles(List<? extends PathLayer> sourceFiles,  List<? extends PathLayer> targetFiles) {
		FileTracker.operationUpdateAsList(sourceFiles, targetFiles, ActionOperation.MOVE);
		FileTracker.operationUpdateAsList(sourceFiles, targetFiles, ActionOperation.RENAME);
		FileHelperGUIOperation.getOperationsList()
				.add(new FileHelperGUIOperation(ActionOperation.MOVE, sourceFiles, targetFiles));
		FileHelperGUIOperation.doThreadOperationsAllList();
	}

	public static void move(List<? extends PathLayer> source, PathLayer targetDirectory) {
		FileTracker.operationUpdate(source, targetDirectory, ActionOperation.MOVE);
		FileHelperGUIOperation.getOperationsList()
				.add(new FileHelperGUIOperation(ActionOperation.MOVE, targetDirectory, source));
		FileHelperGUIOperation.doThreadOperationsAllList();
	}

	/**
	 * 
	 */
	public static HashSet<PathLayer> delete(List<PathLayer> source, CallBackVoid<HashSet<PathLayer>> onFinishTask) {
		return delete(source, onFinishTask, "Delete",
				"Do you really want to delete selected files?");
	}

		/**
         * --> Ask to confirm deletion<br>
         * --> Resolve FileTracker Data by using
         * {@link FileTracker#operationUpdate(List, PathLayer, ActionOperation)}
         * {@link ActionOperation#DELETE} <br>
         * --> Delete files <br>
         * --> call on onFinishTask only if the deletedPaths list is not empty (delete
         * occurs)
         *
         * @param source
         * @param onFinishTask call with list of deleted paths (same as returned list)
         *                     only if the list is not empty
         * @return successfully deleted paths list,<br>
         *         Empty list if was not confirmed
         */
		public static HashSet<PathLayer> delete(List<PathLayer> source, CallBackVoid<HashSet<PathLayer>> onFinishTask,
				String headerConfirmation, String contentConfirmation) {
			HashSet<PathLayer> deletedPaths = new HashSet<>();
			if (source.size() == 0) {
				return deletedPaths;
			}
		String sourceDirectory = source.get(0).getParentPath().toString();

		String filesToDelete = "";
		for (PathLayer path : source) {
			filesToDelete += path.toString() + System.lineSeparator();
		}
		boolean isConfirmed = DialogHelper.showExpandableConfirmationDialog(sourceDirectory, headerConfirmation,
				contentConfirmation, filesToDelete);

		if (isConfirmed) {
			try {
				FileTracker.operationUpdate(source, null, ActionOperation.DELETE);
			} catch (Exception e) {
				e.printStackTrace();
			}
			new Thread() {
				@Override
				public void run() {
					List<PathLayer> undeleted = new ArrayList<>();
					for (PathLayer path : source) {
						Platform.runLater(() -> Main.ProcessTitle("Please Wait..Deleting " + path.getName()));
						try {
							path.deleteForcefully();
							deletedPaths.add(path);
						} catch (Exception e) {
							e.printStackTrace();
							undeleted.add(path);
						}
					}
					if (undeleted.size() > 0) {
						StringBuilder content = new StringBuilder();
						for (PathLayer path : undeleted) {
							content.append(path.toString() + System.lineSeparator());
						}
						String message = "Some files were not deleted";
						Platform.runLater(
								() -> DialogHelper.showExpandableAlert(Alert.AlertType.INFORMATION, sourceDirectory,
										message, message + "  " + undeleted.size() + " files.", content.toString()));
					}
					Platform.runLater(() -> {
						Main.ResetTitle();
						if (!deletedPaths.isEmpty()) {
							onFinishTask.call(deletedPaths);
						}
					});
				}
			}.start();
		}
		return deletedPaths;
	}

	/**
	 * @param parent
	 * @param onFinishCreateAction do a call with new name of directory created<br>
	 *                             can be null
	 */
	public static void createDirectory(PathLayer parent, CallBackVoid<String> onFinishCreateAction) {
		String title = parent.toString();
		String hint = "New Folder";
		PathLayer temp = parent.resolve(hint);
		int i = 2;
		while (temp.exists()) {
			temp = parent.resolve("New Folder (" + i++ + ")");
		}
		hint = temp.getName();
		String name = DialogHelper.showTextInputDialog(title, null, "New Directory", hint);
		if (name != null) {
			PathLayer path = parent.resolve(name);
			try {
				path.createNewAsDirectory();
				if (onFinishCreateAction != null) {
					onFinishCreateAction.call(name);
				}
			} catch (FileAlreadyExistsException e) {
				DialogHelper.showAlert(Alert.AlertType.INFORMATION, title, "Directory already exists", path.toString());
			} catch (Exception e) {
				DialogHelper.showAlert(Alert.AlertType.INFORMATION, title, "Directory was not created",
						path.toString());
			}
		}
	}

	/**
	 * @param parent
	 * @param onFinishCreateAction do a call with new name of directory created<br>
	 *                             can be null
	 */
	public static void createFile(PathLayer parent, CallBackVoid<String> onFinishCreateAction) {
		String title = parent.toString();
		String hint = "New File.txt";
		PathLayer temp = parent.resolve(hint);
		int i = 2;
		while (temp.exists()) {
			temp = parent.resolve("New File (" + i++ + ").txt");
		}
		hint = temp.getName();
		String name = DialogHelper.showTextInputDialog(title, null, "New File", hint);
		if (name != null) {
			PathLayer path = parent.resolve(name);
			try {
				path.createNewAsFile();
				if (onFinishCreateAction != null) {
					onFinishCreateAction.call(path.getName());
				}
			} catch (FileAlreadyExistsException e) {
				DialogHelper.showAlert(Alert.AlertType.INFORMATION, title, "File already exists", path.toString());
			} catch (Exception e) {
				e.printStackTrace();
				DialogHelper.showException(e);
			}
		}
	}

	public static ArrayList<String> getBaseNames(List<String> fileNames) {
		ArrayList<String> baseNames = new ArrayList<>(fileNames.size());

		return baseNames;
	}

	/**
	 * return a non-existing file having name same as source file by using pattern 'fileName - Copy (i++).ext'
	 * @param sourceFile
	 * @return
	 * @see #getAvailablePath 
	 */
	public static PathLayer getCopyFileName(PathLayer sourceFile) {
		PathLayer copiedFile;
		PathLayer parentFile = sourceFile.getParentPath();
		String ext = StringHelper.getExtention(sourceFile.getName()).toLowerCase();
		String baseName = StringHelper.getBaseName(sourceFile.getName());
		if (!ext.isEmpty()) {
			ext = "." + ext;
		}
		copiedFile = parentFile.resolve(baseName + " - Copy" + ext);
		int i = 2;
		while (copiedFile.exists()) {
			copiedFile = parentFile.resolve(baseName + " - Copy (" + i++ + ")" + ext);
		}
		return copiedFile;
	}



	/**
	 * Return a PathLayer that doesn't exist in target directory using name(i++) pattern.<br>
	 *
	 * Example: if file.txt already exist in target directory will return 'file (1).txt',
	 * if also 'file (1).txt' exist will return 'file (2).txt' and so on until file doesn't exist...
	 * @param name the original name
	 * @param targetDirectory the target directory
	 * @return
	 */
	public static PathLayer getAvailablePath(String name, PathLayer targetDirectory) {
		PathLayer targetFile = targetDirectory.resolve(name);
		String baseName = targetFile.getBaseName();
		String ext = targetFile.getExtension();
		int i = 2;
		while (targetFile.exists()) {
			targetFile = targetDirectory.resolve(baseName + "-(" + i + ")" + (ext.isEmpty() ? "" : "." + ext));
			i++;
		}
		return targetFile;
	}

	/**
	 * Made to deal with look for another application path
	 * <p>
	 * Example if VLC was in <br>
	 * ----------> C:\Program Files (x86)\VideoLAN\VLC\vlc.exe
	 * <p>
	 * it will try to return:<br>
	 * ----------> C:\Program Files (x86)\VideoLAN\VLC\
	 * <p>
	 * if any of them not found it return defaultLoc parameter if it wasn't null or
	 * <br>
	 * ----------> System.getenv("ProgramFiles")
	 *
	 * @param originalFile
	 * @param defaultLoc   can be null
	 * @return no null Existing File
	 */
	public static File getParentExeFile(Path originalFile, File defaultLoc) {
		File finRescue = new File(System.getenv("ProgramFiles"));
		if (defaultLoc != null && defaultLoc.exists()) {
			finRescue = defaultLoc;
		}
		if (!finRescue.exists()) {
			finRescue = File.listRoots()[0];
		}
		if (originalFile == null) {
			return finRescue;
		}
		File parentTry = originalFile.toFile().getParentFile();
		if (parentTry == null || !parentTry.exists()) {
			return finRescue;
		}
		return parentTry;
	}

	public static PathLayer RenameHelper(PathLayer source, String newName) throws IOException {
		return renameHelper(source, source.resolveSibling(newName));
	}

	/**
	 *
	 * Version 2 of Rename<br>
	 * Just Simple Rename
	 * <p>
	 * if you wish to rename too many files in shown view
	 * {@link WatchServiceHelper#setRuning(boolean)} to false before calling this
	 * function
	 * <p>
	 * If you wish to conserve tracker data of renamed file use
	 * {@link FileTracker#operationUpdateAsList(List, List, ActionOperation)} with
	 * RENAME parameter as {@link ActionOperation}
	 *
	 * @param oldSource  Original file source
	 * @param NewRenamed Final new Path
	 * @return
	 * @throws IOException
	 */
	public static PathLayer renameHelper(PathLayer oldSource, PathLayer NewRenamed) throws IOException {
		oldSource.move(NewRenamed);
		return NewRenamed;
	}

	/**
	 * Call {@link #renameGUI(PathLayer, CallBackVoid, String, String)}
	 * @param source
	 * @param onFinishCreateAction
	 * @return the new target file
	 */
	public static PathLayer renameGUI(PathLayer source, CallBackVoid<PathLayer> onFinishCreateAction) {
		return renameGUI(source, onFinishCreateAction, null, null);
	}


	/**
	 *
	 * Version 1 Rename with GUI Note that this Function does not update tracker
	 * data, use
	 * {@link FileTracker#operationUpdateAsList(List, List, ActionOperation)} with
	 * following parameters:<br>
	 * source path <br>
	 * new Path <br>
	 * {@link ActionOperation#RENAME}
	 *
	 * @param source
	 * @param onFinishCreateAction
	 *            do a call with the new path just before renaming <br>
	 *            can be null
	 * @param hintNewName
	 *            the new name used as hint in input, if null original name
	 *            will be used <br>
	 *            can be null
	 * @param headerMessage
	 *            message to notify user about an error or to give a hint message <br>
	 *            can be null
	 * @return null for unsuccessful rename or the new path of renamed item
	 */
	public static PathLayer renameGUI(PathLayer source, CallBackVoid<PathLayer> onFinishCreateAction,
			String hintNewName, String headerMessage) {
		String newName;
		String suggestedName;
		if (hintNewName != null && !hintNewName.isEmpty()) {
			suggestedName = hintNewName;
		} else {
			suggestedName = source.getName().toString();
		}
		if (source.isDirectory()) {
			newName = DialogHelper.showTextInputDialog("Rename", headerMessage, "Enter New Name", suggestedName);
		} else {
			HashMap<String, String> PreNameExt = DialogHelper
					.showMultiTextInputDialog("Rename", headerMessage, "Enter New Name",
							new String[] { "Prefix", "Name", "Extention" }, new String[] { "",
									FilenameUtils.getBaseName(suggestedName), FilenameUtils.getExtension(suggestedName) },
							1);
			if (PreNameExt == null) {
				return null;
			} else {
				if (PreNameExt.get("Name").isEmpty() && PreNameExt.get("Prefix").isEmpty()) {
					return renameGUI(source, onFinishCreateAction, suggestedName, "Error : name cannot be empty");
				}
			}
			newName = PreNameExt.get("Prefix") + PreNameExt.get("Name")
					+ (!PreNameExt.get("Extention").isEmpty() ? "." + PreNameExt.get("Extention") : "");
		}
		if (newName != null && !newName.isEmpty()) {
			PathLayer target = source.getParentPath().resolve(newName);
			try {
				// https://stackoverflow.com/questions/1000183/reliable-file-renameto-alternative-on-windows
				if (Setting.isAutoRenameUTFFile()) {
					String fixedName = StringHelper.FixNameEncoding(target.getName());
					target = source.getParentPath().resolve(fixedName);
				}
				if (target.equals(source)) {
					return null;
				}
				if (onFinishCreateAction != null) {
					onFinishCreateAction.call(target);
				}
				renameHelper(source, target);
				return target;
			} catch (FileAlreadyExistsException e) {
				e.printStackTrace();
				return renameGUI(source, onFinishCreateAction, target.getName(),
						"File already exist!\nChoose a different name.");
			} catch (FileSystemException e) {
				e.printStackTrace();
				return renameGUI(source, onFinishCreateAction, target.getName(),
						"File is open in another program!\nClose file and try again.");
			} catch (Exception e) {
				e.printStackTrace();
				DialogHelper.showException(e);
				return renameGUI(source, onFinishCreateAction, target.getName(),
						"Something went wrong:\n" + e.getMessage());
			}
		}
		return null;
	}
}
