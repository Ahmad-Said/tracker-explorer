package application;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import application.FileHelper.ActionOperation;
import application.datatype.Setting;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
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

public class FileHelperGUIOperation {
	private static LinkedList<FileHelperGUIOperation> OperationsList = new LinkedList<>();
	// private static ArrayList<Operation> OperationsList = new ArrayList<>();
	private static Stage operationStage;
	private static VBox root;
	private static ScrollPane scrollPane;
	private static Scene scene;
	private static ThreadPoolExecutor operationsThread = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
	// private static ScrollBar scrollVertical;

	/**
	 * Initialize Stage where all operations are shown
	 */
	public static void initializeView() {
		operationStage = new Stage();
		operationStage.initModality(Modality.WINDOW_MODAL);
		root = new VBox();
		scrollPane = new ScrollPane(root);
		scene = new Scene(scrollPane);
		scene.getStylesheets().add("/css/bootstrap3.css");
		operationStage.getIcons().add(new Image(Main.class.getResourceAsStream("/img/files_operation.png")));
		operationStage.setTitle("File Operations");
		operationStage.setScene(scene);
	}

	public static void showOperationStage() {
		if (root.getChildren().size() == 0) {
			DialogHelper.showAlert(AlertType.INFORMATION, "File Operations", "Empty Queue",
					"There are no operations running a the moment!\nHave a nice day.....");
		} else {
			operationStage.show();
		}
	}

	public static LinkedList<FileHelperGUIOperation> getOperationsList() {
		return OperationsList;
	}

	public static void doThreadOperationsAllList() {
		boolean requireFocus = false;
		for (FileHelperGUIOperation operation : OperationsList) {
			if (!operation.isOperationComplete && !operation.isRuning) {
				operationsThread.execute(operation.getThread());
				requireFocus = true;
			}
		}
		if (requireFocus) {
			operationStage.requestFocus();
		}
	}

	// operations stuff
	private String Action; // 'Copy' 'Move'
	private String ActionInPast; // 'Copied' 'Moved'
	private String ActionInContinuous; // 'Copying' 'Moving'
	private Path TargetDirectory;
	private List<Path> SourceList;
	private List<Path> TargetDirLocList;
	private HashMap<Path, List<Path>> ParentTochildren;
	// old file to newly created file
	private File LastCreatedFile;
	private int FilesCounts;
	private boolean isRuning;
	private boolean isOperationComplete;
	private Thread currentThread;
	// mean either they didn't get copy or moved
	private List<String> unModifiable = new ArrayList<>();

	// View Stuff
	private Button btnControl; // start -> pause -> clear
	private Button btnCancel; // clear list
	private Text txtState;
	private Text Msg;
	private ProgressBar pbar;
	private ProgressIndicator pind;
	private Group gr;
	private HBox hbox;

	/**
	 * static method {@link #initializeView()} must be called once before creating
	 * any operation
	 *
	 * @param action    check {@linkplain ActionOperation}
	 * @param targetDir
	 * @param src
	 */
	public FileHelperGUIOperation(ActionOperation action, Path targetDir, List<Path> src) {
		switch (action) {
		case COPY:
			Action = "Copy";
			ActionInPast = "Copied";
			ActionInContinuous = "Copying";
			break;
		case MOVE:
			Action = "Move";
			ActionInPast = "Moved";
			ActionInContinuous = "Moving";
			break;
		default:
			DialogHelper.showAlert(AlertType.ERROR, "File Operation", "Unsuported File Operation: " + action.toString(),
					"");
			return;
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
		}
		isRuning = false;
		isOperationComplete = false;
		// initialize view
		initializeOperationView();
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
		return currentThread = new Thread(getTask());
	}

	private Stack<Path> toDeleteLater = new Stack<>();

	private Task<Void> getTask() {
		return new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				isRuning = true;
				Platform.runLater(() -> {
					pbar.progressProperty().unbind();
					pbar.progressProperty().bind(progressProperty());
					pind.progressProperty().unbind();
					pind.progressProperty().bind(progressProperty());
				});
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
											Platform.runLater(() -> Setting.printStackTrace(e));
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
						// remove from queue
						SourceList.remove(0);
						TargetDirLocList.remove(0);
					} else {
						// in here button cancel get his action as no remove in this function
						// so trust the other function i'm getting out here
						isRuning = false;
						return null;
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
							Platform.runLater(() -> DialogHelper.showExpandableAlert(Alert.AlertType.INFORMATION,
									Action + " Exception", message,
									"Total of " + unModifiable.size() + " files were not " + ActionInPast, cont));
						}
						updateProgress(1, 1);
						Platform.runLater(() -> Msg.setText(FilesCounts - unModifiable.size() + "/" + FilesCounts
								+ " Files were " + ActionInPast + " Successfully"));
						finalizeThis(false);
					}
				}
				isRuning = false;
				return null;
			}
		};
	};

	private void finalizeThis(boolean doDeleteLastElement) {

		isOperationComplete = true;
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
		Platform.runLater(() -> Msg.setText(" (" + remaindisplayHelper + "/" + FilesCounts + ") " + ActionInContinuous
				+ " " + tempName + "\nTo " + TargetDirectory.toFile().getName()));
		return remaining;
	}

	private void initializeOperationView() {
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
				if (OperationsList.contains(FileHelperGUIOperation.this)) {
					OperationsList.remove(FileHelperGUIOperation.this);
				}
				if (btnControl.getText().equals("Clear")) {
					root.getChildren().remove(gr);
					if (root.getChildren().size() == 0) {
						operationStage.hide();
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
			operationStage.sizeToScene();
		}
		operationStage.show();
	}

	private void setDisableControl(boolean state) {
		btnControl.setDisable(state);
		btnCancel.setDisable(state);
	}

}
