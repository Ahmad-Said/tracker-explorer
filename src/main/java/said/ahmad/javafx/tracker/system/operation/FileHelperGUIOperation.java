package said.ahmad.javafx.tracker.system.operation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import org.apache.commons.lang3.exception.ExceptionUtils;

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
import said.ahmad.javafx.tracker.app.DialogHelper;
import said.ahmad.javafx.tracker.app.Main;
import said.ahmad.javafx.tracker.app.ResourcesHelper;
import said.ahmad.javafx.tracker.app.ThemeManager;
import said.ahmad.javafx.tracker.app.ThreadExecutors;
import said.ahmad.javafx.tracker.datatype.Setting;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.tracker.system.file.PathLayerHelper;
import said.ahmad.javafx.tracker.system.operation.FileHelper.ActionOperation;

public class FileHelperGUIOperation {
	private static LinkedList<FileHelperGUIOperation> OperationsList = new LinkedList<>();
	// private static ArrayList<Operation> OperationsList = new ArrayList<>();
	private static Stage operationStage;
	private static VBox root;
	private static ScrollPane scrollPane;
	private static Scene scene;
	public static Image OPERATION_ICON_IMAGE = new Image(
			ResourcesHelper.getResourceAsStream("/img/files_operation.png"));
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
		ThemeManager.applyTheme(scene);
		operationStage.getIcons().add(OPERATION_ICON_IMAGE);
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
				ThreadExecutors.operationsThread.execute(operation.getThread());
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
	private PathLayer TargetDirectory;
	private Queue<PathLayer> SourceList;
	private Queue<PathLayer> TargetList;
	// old file to newly created file
	private PathLayer LastCreatedFile;
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
	public FileHelperGUIOperation(ActionOperation action, PathLayer targetDir, List<? extends PathLayer> src) {
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
		SourceList = new LinkedList<>();
		TargetList = new LinkedList<>();
		FilesCounts = 0;
		// for more control if source was a directory we split all files in it.
		FileHelperOperationWalker directorySplitter = new FileHelperOperationWalker(TargetDirectory, SourceList,
				TargetList, toDeleteLater, action);
		for (PathLayer p : src) {
			if (p.isDirectory()) {
				try {
					PathLayerHelper.walkFileTree(p, Integer.MAX_VALUE, true, directorySplitter);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				SourceList.add(p);
				TargetList.add(TargetDirectory.resolve(p.getName()));
			}
		}
		FilesCounts = SourceList.size();

		isRuning = false;
		isOperationComplete = false;
		// initialize view
		initializeOperationView();
	}

	private Thread getThread() {
		// Create new Task and Thread - Bind Progress Property to Task Progress
		return currentThread = new Thread(getTask());
	}

	private Stack<PathLayer> toDeleteLater = new Stack<>();

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
					PathLayer srcPath = SourceList.peek();
					PathLayer targetPathResolved = TargetList.peek();

					LastCreatedFile = targetPathResolved;
					updateProgress(updateMsgText(true), FilesCounts);

					String targetDisplayText = targetPathResolved.getAbsolutePath();

					if (Action.equals("Copy")) {

						Platform.runLater(() -> {
							btnControl.setText("Pause");
							Main.ProcessTitle("Copying.. " + srcPath.getName() + " To " + targetDisplayText);
						});
						try {
							if (srcPath.isDirectory()) {
								targetPathResolved.mkdirs();
							} else {
								// this is access public restricted so added a new rule for it
								// https://stackoverflow.com/questions/17083896/how-to-cancel-files-copy-in-java
								// https://stackoverflow.com/questions/25222811/access-restriction-the-type-application-is-not-api-restriction-on-required-l
								srcPath.copy(targetPathResolved);
								// Files.copy(srcPath, targetPathResolved,
								// com.sun.nio.file.ExtendedCopyOption.INTERRUPTIBLE);
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
							Main.ProcessTitle("Moving.." + srcPath.getName().toString() + " To " + targetDisplayText);

						});
						try {
							if (srcPath.isDirectory()) {
								targetPathResolved.mkdirs();
							} else {
								srcPath.move(targetPathResolved);
							}
						} catch (Exception e) {
							// https://stackoverflow.com/questions/1149703/how-can-i-convert-a-stack-trace-to-a-string
							unModifiable.add(ExceptionUtils.getMessage(e));
							Platform.runLater(() -> Setting.printStackTrace(e));

						}
					}

					// we done this element move to next
					// note in case of interrupt the item can still exist with LastCreatedFile
					// in case outside change to list like in cancel to check again
					setDisableControl(false);
					if (SourceList.size() > 0) {
						// Successful copy without interruption
						// remove from queue
						SourceList.poll();
						TargetList.poll();
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
				if (LastCreatedFile != null && LastCreatedFile.isLocal() && LastCreatedFile.exists()) {
					LastCreatedFile.toFileIfLocal().deleteOnExit();
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
					toDeleteLater.pop().delete();
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
		String tempName = SourceList.peek().getName();
		Platform.runLater(() -> Msg.setText(" (" + remaindisplayHelper + "/" + FilesCounts + ") " + ActionInContinuous
				+ " " + tempName + "\nTo " + TargetDirectory.getName()));
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
