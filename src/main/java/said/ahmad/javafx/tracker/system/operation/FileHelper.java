package said.ahmad.javafx.tracker.system.operation;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
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

	public static void move(List<? extends PathLayer> source, PathLayer targetDirectory) {
		FileTracker.operationUpdate(source, targetDirectory, ActionOperation.MOVE);
		FileHelperGUIOperation.getOperationsList()
				.add(new FileHelperGUIOperation(ActionOperation.MOVE, targetDirectory, source));
		FileHelperGUIOperation.doThreadOperationsAllList();
	}

	/**
	 * --> Ask to confirm deletion<br>
	 * --> Resolve FileTracker Data by using
	 * {@link FileTracker#operationUpdate(List, PathLayer, ActionOperation)}
	 * {@value ActionOperation#DELETE}<br>
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
	public static HashSet<PathLayer> delete(List<PathLayer> source, CallBackVoid<HashSet<PathLayer>> onFinishTask) {
		HashSet<PathLayer> deletedPaths = new HashSet<>();
		if (source.size() == 0) {
			return deletedPaths;
		}
		String sourceDirectory = source.get(0).getParentPath().toString();

		String filesToDelete = "";
		for (PathLayer path : source) {
			filesToDelete += path.toString() + System.lineSeparator();
		}
		boolean isConfirmed = DialogHelper.showExpandableConfirmationDialog(sourceDirectory, "Delete",
				"Do you really want to delete selected files?", filesToDelete);

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

	public static File getCopyFileName(File sourceFile) {
		File copiedFile = sourceFile;
		Path parentFile = sourceFile.getParentFile().toPath();
		String ext = StringHelper.getExtention(copiedFile.getName()).toLowerCase();
		String baseName = StringHelper.getBaseName(copiedFile.getName());
		if (!ext.isEmpty()) {
			ext = "." + ext;
		}
		copiedFile = parentFile.resolve(baseName + " - Copy" + ext).toFile();
		int i = 2;
		while (copiedFile.exists()) {
			copiedFile = parentFile.resolve(baseName + " - Copy (" + i++ + ")" + ext).toFile();
		}
		return copiedFile;
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
	 * @param source  Original file source
	 * @param newPath Final new Path
	 * @return
	 * @throws IOException
	 */
	public static PathLayer renameHelper(PathLayer oldSource, PathLayer NewRenamed) throws IOException {
		oldSource.move(NewRenamed);
		return NewRenamed;
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
	 * @param onFinishCreateAction do a call with the new path just before renaming
	 *                             <br>
	 *                             can be null
	 * @return null for unsuccessful rename or the new path of renamed item
	 */
	public static PathLayer renameGUI(PathLayer source, CallBackVoid<PathLayer> onFinishCreateAction) {
		String newName;
		String OriginalName = source.getName().toString();
		if (source.isDirectory()) {
			newName = DialogHelper.showTextInputDialog("Rename", null, "Enter New Name", source.getName().toString());
		} else {
			HashMap<String, String> PreNameExt = DialogHelper
					.showMultiTextInputDialog("Rename", null, "Enter New Name",
							new String[] { "Prefix", "Name", "Extention" }, new String[] { "",
									FilenameUtils.getBaseName(OriginalName), FilenameUtils.getExtension(OriginalName) },
							1);
			if (PreNameExt == null) {
				return null;
			} else {
				if (PreNameExt.get("Name").isEmpty() && PreNameExt.get("Prefix").isEmpty()) {
					DialogHelper.showAlert(AlertType.ERROR, "Rename", "Name Cannot be empty!",
							"Please Enter a valid name");
					return renameGUI(source, onFinishCreateAction);
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
			} catch (Exception e) {
				e.printStackTrace();
				DialogHelper.showException(e);
			}
		}
		return null;
	}

	// TODO PathLayer
	public static void copyFiles(List<File> sourceFiles, List<File> targetFiles) {
		Runnable runnable = () -> {
			try {
				Platform.runLater(() -> DialogHelper.showWaitingScreen("Please Wait.. Copying files",
						"Copying your files...\nIn case of bunch files use your system explorer.This just on the go copy."));
				for (int i = 0; i < sourceFiles.size(); i++) {
					File srcFile = sourceFiles.get(i);
					File destFile = targetFiles.get(i);
					if (srcFile.isFile()) {
						FileUtils.copyFile(srcFile, destFile);
					} else {
						FileUtils.copyDirectory(srcFile, destFile);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			Platform.runLater(() -> DialogHelper.closeWaitingScreen());
		};
		Thread th = new Thread(runnable);
		th.start();
	}
}
