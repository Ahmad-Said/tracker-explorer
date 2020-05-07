package application;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import application.controller.WelcomeController;
import application.controller.splitview.SplitViewController;
import application.datatype.Setting;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

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

	public static void copyWithTeraCopy(List<Path> source, Path targetDirectory) {
		try {
			FileTracker.operationUpdate(source, targetDirectory, ActionOperation.COPY);
			TeraCopy.copy(source, targetDirectory);
		} catch (IOException e) {
			e.printStackTrace();
			DialogHelper.showException(e);
		}
	}

	public static void copy(List<Path> source, Path targetDirectory) {
		FileTracker.operationUpdate(source, targetDirectory, ActionOperation.COPY);
		FileHelperGUIOperation.getOperationsList()
				.add(new FileHelperGUIOperation(ActionOperation.COPY, targetDirectory, source));
		FileHelperGUIOperation.doThreadOperationsAllList();
	}

	public static void moveWithTeraCopy(List<Path> source, Path targetDirectory) {
		try {
			FileTracker.operationUpdate(source, targetDirectory, ActionOperation.MOVE);
			TeraCopy.move(source, targetDirectory);
		} catch (IOException e) {
			e.printStackTrace();
			DialogHelper.showException(e);
		}
	}

	public static void move(List<Path> source, Path targetDirectory) {
		FileTracker.operationUpdate(source, targetDirectory, ActionOperation.MOVE);
		FileHelperGUIOperation.getOperationsList()
				.add(new FileHelperGUIOperation(ActionOperation.MOVE, targetDirectory, source));
		FileHelperGUIOperation.doThreadOperationsAllList();
	}

	public static boolean delete(List<Path> source, EventHandler<Event> onFinishTask) {
		if (source.size() == 0) {
			return false;
		}
		String sourceDirectory = source.get(0).getParent().toString();

		String filesToDelete = "";
		for (Path path : source) {
			filesToDelete += path.toString() + System.lineSeparator();
		}
		boolean isConfirmed = DialogHelper.showExpandableConfirmationDialog(sourceDirectory, "Delete",
				"Do you really want to delete selected files?", filesToDelete);

		if (isConfirmed) {
			FileTracker.operationUpdate(source, null, ActionOperation.DELETE);
			new Thread() {
				@Override
				public void run() {
					List<Path> undeleted = new ArrayList<>();
					for (Path path : source) {
						Platform.runLater(
								() -> Main.ProcessTitle("Please Wait..Deleting " + path.getFileName().toString()));
						try {
							if (path.toFile().isDirectory()) {
								FileUtils.deleteDirectory(path.toFile());
							} else {
								FileUtils.forceDelete(path.toFile());
							}
						} catch (Exception e) {
							undeleted.add(path);
						}
					}
					if (undeleted.size() > 0) {
						String content = "";
						for (Path path : undeleted) {
							content += path.toString() + System.lineSeparator();
						}
						String message = "Some files were not deleted";
						String contectHelper = content;
						Platform.runLater(() -> DialogHelper.showAlert(Alert.AlertType.INFORMATION, sourceDirectory,
								message, contectHelper));
					}
					Platform.runLater(() -> {
						Main.ResetTitle();
						onFinishTask.handle(null);
					});
				}
			}.start();
		}
		return isConfirmed;

	}

	public static void createDirectory(Path parent, SplitViewController focusedPane) {
		String title = parent.toString();
		String hint = "New Folder";
		File temp = parent.resolve(hint).toFile();
		int i = 2;
		while (temp.exists()) {
			temp = parent.resolve("New Folder (" + i++ + ")").toFile();
		}
		hint = temp.getName();
		String name = DialogHelper.showTextInputDialog(title, null, "New Directory", hint);
		if (name != null) {
			Path path = parent.resolve(name);
			try {
				Files.createDirectory(path);
				focusedPane.getFileTracker().trackNewOutFolder(path);
				Platform.runLater(() -> focusedPane.ScrollToName(path.toFile().getName()));
			} catch (FileAlreadyExistsException e) {
				DialogHelper.showAlert(Alert.AlertType.INFORMATION, title, "Directory already exists", path.toString());
			} catch (Exception e) {
				DialogHelper.showAlert(Alert.AlertType.INFORMATION, title, "Directory was not created",
						path.toString());
			}
		}
	}

	public static void createFile(Path parent) {
		String title = parent.toString();
		String hint = "New File.txt";
		File temp = parent.resolve(hint).toFile();
		int i = 2;
		while (temp.exists()) {
			temp = parent.resolve("New File (" + i++ + ").txt").toFile();
		}
		hint = temp.getName();
		String name = DialogHelper.showTextInputDialog(title, null, "New File", hint);
		if (name != null) {
			Path path = parent.resolve(name);
			try {
				Files.createFile(path);
			} catch (FileAlreadyExistsException e) {
				DialogHelper.showAlert(Alert.AlertType.INFORMATION, title, "File already exists", path.toString());
			} catch (Exception e) {
				DialogHelper.showAlert(Alert.AlertType.ERROR, title, "File was not created", path.toString());
			}
		}
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

	public static Path RenameHelper(Path source, String newName) throws IOException {
		Path newPath = source.resolveSibling(newName);
		return renameHelper(source, newPath);
	}

	public static Set<Path> getParentsPaths(List<File> sonFiles) {
		return sonFiles.stream().filter(f -> f != null).map(f -> f.getParentFile().toPath())
				.collect(Collectors.toSet());
	}

	public static Set<Path> getParentsPathsFromPath(List<Path> sonPaths) {
		return sonPaths.stream().filter(p -> p != null).map(p -> p.getParent()).collect(Collectors.toSet());
	}

	public static HashMap<Path, List<Path>> getParentTochildren(List<Path> sonsPaths) {
		HashMap<Path, List<Path>> parentToSons = new HashMap<>();
		sonsPaths.forEach(s -> {
			Path parent = s.getParent();
			if (!parentToSons.containsKey(parent)) {
				parentToSons.put(parent, new ArrayList<Path>());
			}
			parentToSons.get(parent).add(s);
		});
		return parentToSons;
	};

	public static Set<Path> getListPathsNoHidden(Path parentDirectory) throws IOException {
		return Files.list(parentDirectory).filter(p -> !p.toFile().isHidden()).collect(Collectors.toSet());
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
	public static Path renameHelper(Path oldSource, Path NewRenamed) throws IOException {
		Files.move(oldSource, NewRenamed);
		return NewRenamed;
	}

	/**
	 *
	 * Version 1 of Rename
	 *
	 * Rename used on every shown file in view it used to fix name encoding
	 * Especially for Arabic letter. {@link SplitViewController#showList}
	 *
	 * Also used for single rename. Its tracker data are performed in
	 * {@link WelcomeController#rename()}
	 *
	 * @param source
	 * @param silentFix <br>
	 *                  true: to disable prompt user. <br>
	 *                  false: display simple input form
	 *
	 * @return null for unsuccessful rename or the new path of renamed item
	 */
	public static Path rename(Path source, boolean silentFix) {
		String newName;
		if (!silentFix) {
			String OriginalName = source.getFileName().toString();
			if (source.toFile().isDirectory()) {
				newName = DialogHelper.showTextInputDialog("Rename", null, "Enter New Name",
						source.getFileName().toString());
			} else {
				HashMap<String, String> PreNameExt = DialogHelper.showMultiTextInputDialog(
						"Rename", null, "Enter New Name", new String[] { "Prefix", "Name", "Extention" }, new String[] {
								"", FilenameUtils.getBaseName(OriginalName), FilenameUtils.getExtension(OriginalName) },
						1);
				if (PreNameExt == null) {
					return null;
				} else {
					if (PreNameExt.get("Name").isEmpty() && PreNameExt.get("Prefix").isEmpty()) {
						DialogHelper.showAlert(AlertType.ERROR, "Rename", "Name Cannot be empty!",
								"Please Enter a valid name");
						return rename(source, silentFix);
					}
				}
				newName = PreNameExt.get("Prefix") + PreNameExt.get("Name")
						+ (!PreNameExt.get("Extention").isEmpty() ? "." + PreNameExt.get("Extention") : "");
			}
		} else {
			newName = StringHelper.FixNameEncoding(source.toFile().getName());
		}
		if (newName != null && !newName.isEmpty()) {
			Path target = source.getParent().resolve(newName);
			try {
				// System.out.println("name is " + source.toString());
				// System.out.println("target is "+ target);
				// https://stackoverflow.com/questions/1000183/reliable-file-renameto-alternative-on-windows
				if (Setting.isAutoRenameUTFFile()) {
					String fixedName = StringHelper.FixNameEncoding(target.toFile().getName());
					target = source.getParent().resolve(fixedName);
				}
				if (target.equals(source)) {
					return null;
				}
				renameHelper(source, target);
				// Files.move(source, target);
				// if (source.toFile().isDirectory())
				// FileUtils.moveDirectory(source.toFile(), target.toFile());
				// else
				// FileUtils.moveFile(source.toFile(), target.toFile());
				return target;
			} catch (Exception e) {
				e.printStackTrace();
				DialogHelper.showException(e);
				// if (!silentFix)
//				DialogHelper.showAlert(Alert.AlertType.INFORMATION, source.getParent().toString(),
//						"File was not renamed", source.toString());
			}
		}
		return null;
	}

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
