package application;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import application.controller.SplitViewController;
import application.controller.WelcomeController;
import application.datatype.Setting;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FileHelper {

	static class Operation {
		// operations stuff
		String Action; // 'Copy' 'Move'
		String ActionInPast; // 'Copied' 'Moved'
		String ActionInContinuous; // 'Copying' 'Moving'
		Path TargetDirectory;
		List<Path> SourceList;
		List<Path> TargetDirLocList;
		Path lastMovedDirTobeDeleted;
		HashMap<Path, List<Path>> ParentTochildren;
		// oldfile to newly created file
		File LastCreatedFile;
		int FilesCounts;
		boolean isRuning;
		Thread currentThread;
		// mean either they didn't get copy or moved
		List<String> unModifiable = new ArrayList<>();
		Operation thisOperation;

		// View Stuff
		Button btnControl; // start -> pause -> clear
		Button btnCancel; // clear list
		Text txtState;
		Text Msg;
		ProgressBar pbar;
		ProgressIndicator pind;
		Group gr;
		HBox hbox;

		public Operation(String action, Path targetDir, List<Path> src) {
			Action = action;
			if (action.equals("Copy")) {
				ActionInPast = "Copied";
				ActionInContinuous = "Copying";
			} else {
				ActionInPast = "Moved";
				ActionInContinuous = "Moving";
			}
			TargetDirectory = targetDir;
			// for more control if src was a directory we split all files in it
			// and in action we check again if the src was a directory we simply create it
			// then move file to this dir
			SourceList = new ArrayList<>();
			TargetDirLocList = new ArrayList<>();
			ParentTochildren = new HashMap<>();
			FilesCounts = 0;
			for (Path p : src) {
				if (p.toFile().isDirectory()) {
					splitDirectory(TargetDirectory, p);
				} else {
					SourceList.add(p);
					TargetDirLocList.add(TargetDirectory);
					FilesCounts++;
				}
				thisOperation = this;
			}

			isRuning = true;
			// initialize view
			DoOperationView();
		}

		private void splitDirectory(Path treedirTargetPath, Path newDirPath) {
			SourceList.add(newDirPath);
			// null i.e. in case of dir we will create a directory not copy or move
			// unused definition of this list just to not be null... can set null
			// if not null.. in task
			TargetDirLocList.add(treedirTargetPath);

			ParentTochildren.put(newDirPath, new ArrayList<>());
			FilesCounts++;
			treedirTargetPath = treedirTargetPath.resolve(newDirPath.getFileName());
			RecursiveFileWalker r = new RecursiveFileWalker(true);
			try {
				// Files.walkFileTree(p, EnumSet.of(FileVisitOption.FOLLOW_LINKS), 1, r);
				final Path treedirTargetPathTemp = treedirTargetPath;
				Files.walkFileTree(newDirPath, EnumSet.of(FileVisitOption.FOLLOW_LINKS), 1, r);
				r.getAllFiles().stream().filter(path -> !path.equals(newDirPath)).forEach(path -> {
					if (path.toFile().isDirectory()) {
						splitDirectory(treedirTargetPathTemp, path);
					} else {
						SourceList.add(path);
						ParentTochildren.get(newDirPath).add(path);
						TargetDirLocList.add(treedirTargetPathTemp);
						FilesCounts++;

					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		private Thread getThread() {
			// Create new Task and Thread - Bind Progress Property to Task Progress
			Task<Object> task = getTask();
			Platform.runLater(() -> {
				pbar.progressProperty().unbind();
				pbar.progressProperty().bind(task.progressProperty());
				pind.progressProperty().unbind();
				pind.progressProperty().bind(task.progressProperty());
			});
			return currentThread = new Thread(task);
		}

		private Stack<Path> toDeleteLater = new Stack<>();

		private Task<Object> getTask() {
			return new Task<Object>() {

				@Override
				protected Object call() throws Exception {

					while (isRuning && SourceList.size() != 0) {
						Path srcPath = SourceList.get(0);
						Path srcPathParent = srcPath.getParent();
						File sourceFile = srcPath.toFile();

						File targetDirFile = TargetDirLocList.get(0).toFile();
						Path targetPathResolved = targetDirFile.toPath().resolve(srcPath.getFileName());
						LastCreatedFile = targetPathResolved.toFile();
						updateProgress(updateMsgText(true), FilesCounts);

						// target String resolve printing if targetdir was a root folder like D:\
						String targetDisplayText = TargetDirectory.getRoot().equals(TargetDirectory)
								? TargetDirectory.toString()
								: TargetDirectory.getFileName().toString();
						// String targetDisplayText = (targetDirFile.toPath().getNameCount() == 0)
						// ? targetDirFile.toString()
						// : targetDirFile.getName();

						if (Action.equals("Copy")) {

							Platform.runLater(() -> {
								btnControl.setText("Pause");
								Main.ProcessTitle(
										"Copying.. " + srcPath.getFileName().toString() + " To " + targetDisplayText);
							});
							try {
								if (sourceFile.isDirectory()) {

									Files.createDirectories(targetPathResolved);
									// Files.createDirectories(TargetDirectory.resolve(sourceFile.getName()));
									// FileUtils.copyDirectoryToDirectory(sourceFile, TargetDirectory.toFile());
								} else {
									// this is access public restricted so added a new rule for it
									// https://stackoverflow.com/questions/17083896/how-to-cancel-files-copy-in-java
									// https://stackoverflow.com/questions/25222811/access-restriction-the-type-application-is-not-api-restriction-on-required-l
									targetDirFile.mkdirs();
									Files.copy(srcPath, targetPathResolved,
											com.sun.nio.file.ExtendedCopyOption.INTERRUPTIBLE);
									// Files channel are giving access denied
									// https://stackoverflow.com/questions/35875142/how-to-cancel-files-copy-in-java-while-not-using-a-non-api-class?noredirect=1&lq=1
									// FileUtils.copyFileToDirectory(sourceFile, targetDirFile);
									// try (FileChannel targetted = FileChannel.open(targetDirFile.toPath(),
									// StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
									//
									// targetted.transferFrom(Channels.newChannel(new FileInputStream(sourceFile)),
									// 0,
									// Long.MAX_VALUE);
									// }
								}
							} catch (Exception e) {
								Platform.runLater(() -> Setting.printStackTrace(e));
								unModifiable.add(ExceptionUtils.getMessage(e));
								// unModifiable.add(e.getLocalizedMessage());
							}

						} else if (Action.equals("Move")) {

							Platform.runLater(() -> {
								btnControl.setText("Pause");
								Main.ProcessTitle(
										"Moving.." + srcPath.getFileName().toString() + " To " + targetDisplayText);

							});
							try {
								if (sourceFile.isDirectory()) {
									Files.createDirectories(targetPathResolved);
								} else {
									FileUtils.moveToDirectory(srcPath.toFile(), targetDirFile, true);
									// ensure after moving all files from a directory delete this directory
									if (ParentTochildren.containsKey(srcPathParent)) {
										List<Path> myFamily = ParentTochildren.get(srcPathParent);
										myFamily.remove(srcPath);
										if (myFamily.size() == 0) {
											ParentTochildren.remove(srcPathParent);
											// to ensure no that src path field don't get deleted we stack them
											// in reverse order
											try {
												Files.delete(srcPathParent);
											} catch (Exception e) {
												Setting.printStackTrace(e);
												toDeleteLater.push(srcPathParent);
											}
										}

									}
								}
							} catch (Exception e) {
								// https://stackoverflow.com/questions/1149703/how-can-i-convert-a-stack-trace-to-a-string
								unModifiable.add(ExceptionUtils.getMessage(e));
								Platform.runLater(() -> Setting.printStackTrace(e));

							}
						}
						// System.out.println(
						// "i still here and my item is " + srcPath + " and size is " +
						// SourceList.size());
						// System.out.println("remaining " + SourceList);
						// we done this element move to next
						// note in case of interrupt the item can still exist with LastCreatedFile
						// in case outside change to list like in cancel to check again
						setDisableControl(false);
						if (SourceList.size() > 0) {
							// Successful copy without interruption

							// Transferring Data File Tracker if the file directly putted in target dir
							// because on new files created a refresh view is triggered by
							// watchServiceHelper so it may resolve conflict if all mapdetails are moved at
							// once warning writing files a number of times equals to files sources size
							// in initial sources directory
							if (targetDirFile.toPath().equals(TargetDirectory)) {
								FileTracker miniFileTracker = new FileTracker(TargetDirectory);
								miniFileTracker.loadMap(TargetDirectory, true, false);
								miniFileTracker.operationUpdate(srcPath, "copy_move");
							}

							// remove from queue
							SourceList.remove(0);
							TargetDirLocList.remove(0);
						} else {
							// in here button cancel get his action as no remove in this function
							// so trust the other function i'm getting out here
							return true;
						}
						if (SourceList.size() == 0) {
							// all operation have done
							if (unModifiable.size() > 0) {
								String content = "";
								for (String st : unModifiable) {
									content += st + System.lineSeparator();
								}
								String message = "Some files were not " + ActionInPast + " properly";
								String cont = content;
								Platform.runLater(() -> DialogHelper.showAlert(Alert.AlertType.INFORMATION,
										Action + " Exception", message, cont));
							}
							updateProgress(1, 1);
							Platform.runLater(() -> Msg.setText(FilesCounts - unModifiable.size() + "/" + FilesCounts
									+ " Files were " + ActionInPast + " Successfully"));
							finalizeThis(false);
						}
					}
					return true;
				}
			};
		};

		private void finalizeThis(boolean doDeleteLastElement) {

			Platform.runLater(() -> {
				if (doDeleteLastElement) {
					if (LastCreatedFile != null && LastCreatedFile.exists()) {
						LastCreatedFile.deleteOnExit();
					}
				} else {
					if (!btnCancel.getStyleClass().contains("danger")) {
						btnCancel.setText("Done");
						btnCancel.getStyleClass().remove("warning");
						btnCancel.getStyleClass().add("success");
					}
				}
				while (!toDeleteLater.empty()) {
					try {
						Files.deleteIfExists(toDeleteLater.pop());
					} catch (IOException e1) {
						Setting.printStackTrace(e1);
					}
				}
				btnControl.setText("Clear");
				btnControl.getStyleClass().add("info");
				btnCancel.setOnAction(e -> {
				});
				setDisableControl(false);
				updateViews();
				if (Setting.isAutoCloseClearDoneFileOperation()) {
					btnControl.fire();
				}
			});
		}

		private void updateViews() {
			// sending paths related to refresh them
			// we do create multiple paths for src as in recursive mode they have differents
			// parents
			Main.ResetTitle();
			// in normal case watch service do detect refresh
		}

		private double updateMsgText(boolean asStarted) {
			double remaining = FilesCounts - SourceList.size();

			if (asStarted && remaining != FilesCounts) {
				remaining += 0.5;
			}
			final double remaindisplayHelper = remaining;
			// defining this tempName here as when run later source may be empty!
			String tempName = SourceList.get(0).toFile().getName();
			Platform.runLater(() -> Msg.setText(" (" + remaindisplayHelper + "/" + FilesCounts + ") "
					+ ActionInContinuous + " " + tempName + "\nTo " + TargetDirectory.toFile().getName()));
			return remaining;
		}

		private void DoOperationView() {
			// Nodes
			txtState = new Text();
			txtState.setFont(Font.font(18));
			txtState.setFill(Color.BLUE);
			txtState.setText(Action + " Pending");
			Msg = new Text();
			if (Action.equals("Copy")) {
				Msg.setFill(Color.BLUE);
			} else {
				Msg.setFill(Color.GREEN);
			}
			updateMsgText(false);
			// ProgressBar
			pbar = new ProgressBar(0);
			pbar.indeterminateProperty().addListener(new ChangeListener<Boolean>() {

				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
					if (newValue) {
						txtState.setText("Initializing");
					} else {
						txtState.setText(Action + " In Progress");
					}

				}
			});

			pbar.progressProperty().addListener(new ChangeListener<Number>() {

				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					if (t1.doubleValue() == 1) {
						txtState.setText(Action + " Done !");
						txtState.setFill(Color.GREEN);
						btnControl.setText("Clear");
						btnControl.getStyleClass().add("success");
					}
				}

			});

			// PrograssIndicator
			pind = new ProgressIndicator(0);
			pind.indeterminateProperty().addListener(new ChangeListener<Boolean>() {

				@Override
				public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
					if (t1) {
						txtState.setText("Initializing");
						txtState.setFill(Color.BLUE);
					} else {
						txtState.setText(Action + " In Progress");

					}
				}
			});

			// Control Button
			btnControl = new Button();
			btnControl.setText("Start");
			btnControl.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent arg0) {
					if (OperationsList.contains(thisOperation)) {
						OperationsList.remove(thisOperation);
					}
					if (btnControl.getText().equals("Clear")) {
						root.getChildren().remove(gr);
						if (root.getChildren().size() == 0) {
							OperationStage.hide();
						}
					} else if (btnControl.getText().equals("Pause")) {
						btnControl.setText("Start");
						setDisableControl(true);
						isRuning = false;
						updateViews();
					} else if (btnControl.getText().equals("Start")) {
						btnControl.setText("Pause");
						isRuning = true;
						getThread().start();
					}
				}
			});
			btnCancel = new Button();
			btnCancel.setText("Cancel");
			btnCancel.getStyleClass().add("warning");
			btnCancel.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent arg0) {

					// Note this is platform thread
					// we cannot join another thread here
					SourceList.clear();
					TargetDirLocList.clear();
					btnCancel.setText("Canceled");
					btnCancel.getStyleClass().remove("warning");
					btnCancel.getStyleClass().add("danger");
					txtState.setText(Action + " Canceled");
					txtState.setFill(Color.RED);
					if (!isRuning || Action.equals("Move")) {
						// we do not interrupt the thread we just guarantee no more loop since list is
						// clear
						String oldTxt = Msg.getText();
						Platform.runLater(() -> Msg.setText("Will Cancel After " + ActionInContinuous
								+ " below Item to prevent lose:\n" + Msg.getText()));
						setDisableControl(true);
						// building another thread on top of current then on finish do call finalize
						new Thread() {
							@Override
							public void run() {
								try {
									currentThread.join();
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								finalizeThis(false);
								Platform.runLater(() -> Msg.setText(oldTxt));
							}
						}.start();
					} else {
						currentThread.interrupt();
						finalizeThis(true);
					}

					// else
					// currentThread.interrupt();
					// setDisableControl(true);
				}
			});

			// Layout
			hbox = new HBox(15);
			HBox h2 = new HBox();
			h2.getChildren().add(Msg);
			hbox.getChildren().addAll(pbar, pind, txtState, btnControl, btnCancel);
			hbox.setPadding(new Insets(5));
			h2.setPadding(new Insets(5));
			VBox Ver = new VBox();
			Ver.setPadding(new Insets(50));
			Ver.getChildren().addAll(hbox, h2);
			gr = new Group();
			gr.getChildren().addAll(Ver);
			root.getChildren().addAll(gr);
			// root.getChildren().remove(0);
			if (root.getChildren().size() <= 3) {
				OperationStage.sizeToScene();
			}
			OperationStage.show();
		}

		private void setDisableControl(boolean state) {
			btnControl.setDisable(state);
			btnCancel.setDisable(state);
		}
	}

	private static LinkedList<Operation> OperationsList = new LinkedList<>();
	// private static ArrayList<Operation> OperationsList = new ArrayList<>();
	private static Stage OperationStage;
	private static VBox root;
	private static ScrollPane scrollPane;
	private static Scene scene;
	private static Thread operationsThread = null;
	// private static ScrollBar scrollVertical;

	public static void initializeView() {
		OperationStage = new Stage();
		OperationStage.initModality(Modality.WINDOW_MODAL);
		root = new VBox();
		scrollPane = new ScrollPane(root);
		scene = new Scene(scrollPane);
		scene.getStylesheets().add("/css/bootstrap3.css");
		OperationStage.getIcons().add(new Image(Main.class.getResourceAsStream("/img/files_operation.png")));
		OperationStage.setTitle("File Operations");
		OperationStage.setScene(scene);
	}

	public static int count = 0;

	private static void DoThreadOperationsAllList() {
		if (operationsThread != null && operationsThread.isAlive()) {
			return;
		}
		// System.out.println("i enter as new running machine queue ");
		operationsThread = new Thread() {
			@Override
			public void run() {
				while (OperationsList.size() != 0) {
					Operation opItem = OperationsList.peek();
					Thread thItem = opItem.getThread();
					// https://stackoverflow.com/questions/6546193/how-to-catch-an-exception-from-a-thread
					thItem.start();
					try {
						thItem.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					// element may be removed by pause button
					// OperationsList.pop();
					if (OperationsList.contains(opItem)) {
						OperationsList.remove(opItem);
					}
				}
			}
		};
		operationsThread.start();
	}

	public static void copyWithTeraCopy(List<Path> source, Path targetDirectory) {
		try {
			TeraCopy.copy(source, targetDirectory);
		} catch (IOException e) {
			e.printStackTrace();
			DialogHelper.showException(e);
		}
	}

	public static void copy(List<Path> source, Path targetDirectory) {
		OperationsList.add(new Operation("Copy", targetDirectory, source));
		DoThreadOperationsAllList();
	}

	public static void moveWithTeraCopy(List<Path> source, Path targetDirectory) {
		try {
			TeraCopy.move(source, targetDirectory);
		} catch (IOException e) {
			e.printStackTrace();
			DialogHelper.showException(e);
		}
	}

	public static void move(List<Path> source, Path targetDirectory) {
		OperationsList.add(new Operation("Move", targetDirectory, source));
		DoThreadOperationsAllList();
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
				focusedPane.getMfileTracker().NewOutFolder(path);
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
		return RenameHelper(source, newPath);
	}

	public static Set<Path> getParentsPaths(List<File> sonFiles) {
		return sonFiles.stream().map(f -> f.getParentFile().toPath()).collect(Collectors.toSet());
	}

	public static Set<Path> getParentsPathsFromPath(List<Path> sonPaths) {
		return sonPaths.stream().map(p -> p.getParent()).collect(Collectors.toSet());
	}

	/**
	 *
	 * Version 2 Rename
	 *
	 * Rename while conserving status in File Tracker
	 *
	 * @param source  Original file source
	 * @param newPath Final new Path
	 * @return
	 * @throws IOException
	 */
	public static Path RenameHelper(Path oldSource, Path NewRenamed) throws IOException {

		// do later at each rename conserve status in file tracker
		// even at beginning if it cost a file write in each operation
		// other wise must get a list of rename
		// then group them by parent directory and make a file write at once..
		// a trigger of watch service is lunched here

		// danger of concurrent modification
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
				RenameHelper(source, target);
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
