package application.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import application.DialogHelper;
import application.FileHelper;
import application.FileTracker;
import application.Main;
import application.StringHelper;
import application.VLC;
import application.WatchServiceHelper;
import application.model.Setting;
import application.model.TableViewModel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;

public class WelcomeController implements Initializable {

	@FXML
	private TextField leftPredictNavigation;

	@FXML
	private TextField rightPredictNavigation;

	@FXML
	private ToggleButton autoExpand;

	@FXML
	private MenuItem toogleAutoBackSync;

	@FXML
	private Menu TrackerMenu;

	@FXML
	private MenuItem aboutMenuItem;

	@FXML
	private MenuItem bulkRemoveMenuItem;

	@FXML
	private MenuItem newFile;

	@FXML
	private MenuItem renameItem;

	@FXML
	private MenuItem trackLeftRecusivlyMenuItem;

	@FXML
	private Menu fileMenu;

	@FXML
	private Menu helpMenu;

	@FXML
	private MenuItem newFolder;

	@FXML
	private MenuItem deleteItem;

	@FXML
	private Button GoDesktop;

	@FXML
	private Button DeleteTracker;

	@FXML
	private TextField leftPathInput;

	@FXML
	private Button leftBack;

	@FXML
	private Button leftNext;

	@FXML
	private MenuButton rootsMenu;

	@FXML
	private TextField leftSearchField;

	@FXML
	private Button leftUp;

	@FXML
	private Button leftExplorer;

	@FXML
	private Button leftDominate;

	@FXML
	private Button leftSearchButton;

	@FXML
	private TableColumn<TableViewModel, String> leftStatus;

	@FXML
	private TableColumn<TableViewModel, ImageView> leftIcon;

	@FXML
	private TableColumn<TableViewModel, String> leftName;

	@FXML
	private TableColumn<TableViewModel, HBox> lefthboxActions;

	@FXML
	private TableView<TableViewModel> leftTable;

	ObservableList<TableViewModel> leftDataTable = FXCollections.observableArrayList();

	@FXML
	private TextField rightPathInput;

	@FXML
	private TextField rightSearchField;

	@FXML
	private Button rightUp;

	@FXML
	private Button rightNext;

	@FXML
	private Button rightBack;

	@FXML
	private Button rightExplorer;

	@FXML
	private Button rightDominate;

	@FXML
	private Button rightSearchButton;

	@FXML
	private TableColumn<TableViewModel, String> rightStatus;

	@FXML
	private TableColumn<TableViewModel, ImageView> rightIcon;

	@FXML
	private TableColumn<TableViewModel, String> rightName;

	@FXML
	private TableColumn<TableViewModel, HBox> righthboxActions;

	@FXML
	private TableView<TableViewModel> rightTable;

	Menu subMenuActiveUser;
	ToggleGroup toogleActiveUserGroup;
	ArrayList<RadioMenuItem> allActiveUser = new ArrayList<>();
	Menu subMenuRemoveUser;
	ArrayList<MenuItem> allRemoveUser = new ArrayList<>();

	ObservableList<TableViewModel> rightDataTable = FXCollections.observableArrayList();

	private static final String ACTION_SELECT = "select";
	private static final String ACTION_COPY = "copy";
	private static final String ACTION_MOVE = "move";
	private static final String ACTION_DELETE = "delete";
	private static final String ACTION_OPEN = "open";
	private SplitViewController leftView;
	private SplitViewController rightView;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		// leftDataTable = FXCollections.observableArrayList(new TableViewModel("Jacob",
		// "Smith",
		// "jacob.smith@example.com"),
		// new TableViewModel("Isabella", "Johnson", "isabella.johnson@example.com"),
		// new TableViewModel("Ethan", "Williams", "ethan.williams@example.com"),
		// new TableViewModel("Emma", "Jones", "emma.jones@example.com"),
		// new TableViewModel("Michael", "Brown", "michael.brown@example.com"),
		// new TableViewModel("Michael", "Brown", "michael.brown@example.com"),
		// new TableViewModel("Michael", "Brown", "michael.brown@example.com"),
		// new TableViewModel("Michael", "Brown", "michael.brown@example.com"));
		// fill leftDataTable from system path name and status from saved file
		try {

			// Assign column to which property in model

			VLC.initializeDefaultVLCPath();
			this.initializeMenuBar();

			// later do thing if return false;

			leftStatus.setCellValueFactory(new PropertyValueFactory<TableViewModel, String>("Status"));
			leftName.setCellValueFactory(new PropertyValueFactory<TableViewModel, String>("Name"));
			lefthboxActions.setCellValueFactory(new PropertyValueFactory<TableViewModel, HBox>("hboxActions"));
			leftIcon.setCellValueFactory(new PropertyValueFactory<TableViewModel, ImageView>("imgIcon"));

			rightStatus.setCellValueFactory(new PropertyValueFactory<TableViewModel, String>("Status"));
			rightName.setCellValueFactory(new PropertyValueFactory<TableViewModel, String>("Name"));
			righthboxActions.setCellValueFactory(new PropertyValueFactory<TableViewModel, HBox>("hboxActions"));
			rightIcon.setCellValueFactory(new PropertyValueFactory<TableViewModel, ImageView>("imgIcon"));
			leftView = new SplitViewController(StringHelper.InitialLeftPath, true, this, leftDataTable, leftPathInput,
					leftUp, leftSearchField, leftSearchButton, leftTable, leftExplorer, lefthboxActions, leftBack,
					leftNext, leftPredictNavigation);
			rightView = new SplitViewController(StringHelper.InitialRightPath, false, this, rightDataTable,
					rightPathInput, rightUp, rightSearchField, rightSearchButton, rightTable, rightExplorer,
					righthboxActions, rightBack, rightNext, rightPredictNavigation);
			leftView.getPathField().setOnAction(e -> onTextEntered(leftView.getPathField()));
			rightView.getPathField().setOnAction(e -> onTextEntered(rightView.getPathField()));
			refreshBothViews(null);
			initializeButtons();
			// testing

			// initialize coloumn preference
			rightStatus.setVisible(Setting.getShowRightNotesColumn());
			leftStatus.setVisible(Setting.getShowLeftNotesColumn());
		} catch (Exception e1) {
			DialogHelper.showException(e1);
		}
	}

	private void AddActiveUser(String user) {
		RadioMenuItem mn = new RadioMenuItem(user);
		mn.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				FileTracker.updateUserFileName(user);
				refreshBothViews(null);
			}
		});
		toogleActiveUserGroup.getToggles().add(mn);
		allActiveUser.add(mn);
		if (Setting.getActiveUser().equals(user))
			mn.setSelected(true);
		subMenuActiveUser.getItems().add(mn);
	}

	private void AddRemoveUser(String user) {
		MenuItem mn = new MenuItem(user);
		mn.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				// Check for conflict disallow if user is selected as active user
				if (mn.getText().equals(Setting.getActiveUser())) {
					DialogHelper.showAlert(AlertType.ERROR, "Remove User", "Warning: Removal of an Active User",
							"For a safety Check Please set active ANOTHER User, then remove this user from here");
					return;
				}
				// removing from active user menu
				for (RadioMenuItem item : allActiveUser) {

					if (item.getText().equals(mn.getText()))
						subMenuActiveUser.getItems().remove(item);
				}
				subMenuRemoveUser.getItems().remove(mn);
				Setting.getUserNames().remove(user);
				DialogHelper.showAlert(AlertType.CONFIRMATION, "Remove User", "Account User Removed Successfully",
						"User with name " + mn.getText()
								+ " was removed.\nNotes: - it's data will remain for specified purpose :)"
								+ "\n - Other wise you can use menu 'Clean Recursivly' with this name:\'" + mn.getText()
								+ "' for a full clean.");
			}
		});
		if (mn.getText().equals("default"))
			mn.setDisable(true);
		allRemoveUser.add(mn);
		subMenuRemoveUser.getItems().add(mn);
	}

	private void initializeMenuBar() {
		/**
		 * Set up file menu
		 */
		newFile.setOnAction(e -> this.createFile());
		newFile.setAccelerator(Main.SHORTCUT_NEW_FILE);

		newFolder.setOnAction(e -> this.createDirectory());
		newFolder.setAccelerator(Main.SHORTCUT_NEW_DIRECTORY);

		renameItem.setOnAction(e -> this.rename());
		renameItem.setAccelerator(Main.SHORTCUT_RENAME);

		deleteItem.setOnAction(e -> this.delete());
		deleteItem.setAccelerator(Main.SHORTCUT_DELETE);

		/**
		 * set up FileTracker Menu
		 */
		// check http://tutorials.jenkov.com/javafx/menubar.html
		// Menus :
		MenuItem NewUser = new MenuItem("Add A new User");
		subMenuActiveUser = new Menu("Set Active User");
		toogleActiveUserGroup = new ToggleGroup();
		subMenuRemoveUser = new Menu("Remove User");

		// Add A new User Menu Item
		NewUser.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				String noChar = "/\\:*\"<>|";
				int limit = 10;
				String user = DialogHelper.showTextInputDialog("Add A new User", "Enter The User Name",
						"Notes:\n - Creating a new user will allow to use program in multiuser Mode so each User have it's own tracker Data!\n - The name Should: \n\t- Not contain any of the following character: "
								+ noChar + "\n\t-Not exceed of " + limit + " character",
						"user");
				if (user == null || user.isEmpty())
					return;
				if (user.length() > limit) {
					DialogHelper.showAlert(AlertType.ERROR, "Add A new User", "Character Limit Excceded",
							"Max Limit:" + limit + " Characters");
					NewUser.fire();
					return;
				}
				// https://stackoverflow.com/questions/14392270/how-do-i-check-if-a-string-contains-a-list-of-characters
				Set<Character> charsToTestFor = noChar.chars().mapToObj(ch -> Character.valueOf((char) ch))
						.collect(Collectors.toSet());
				boolean anyCharInString = user.chars()
						.anyMatch(ch -> charsToTestFor.contains(Character.valueOf((char) ch)));
				if (anyCharInString) {
					DialogHelper.showAlert(AlertType.ERROR, "Add A new User", "Invalid Character",
							"Your name should not contain any of the following Character:" + noChar);
					NewUser.fire();
					return;
				}
				FileTracker.updateUserFileName(user);
				AddActiveUser(user);
				AddRemoveUser(user);
				Setting.getUserNames().add(user);
				refreshBothViews(null);
			}
		});
		TrackerMenu.getItems().add(NewUser);
		// Select Active User Menu
		for (String user : Setting.getUserNames())
			AddActiveUser(user);
		TrackerMenu.getItems().add(subMenuActiveUser);

		// Remove User
		for (String user : Setting.getUserNames())
			AddRemoveUser(user);
		TrackerMenu.getItems().add(subMenuRemoveUser);

		// Clean Recursively
		MenuItem CleanRecursively = new MenuItem("Clean Recursively");
		CleanRecursively.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				String depthANS = DialogHelper.showTextInputDialog("Recursive Cleaner", "From Left Depth to consider",
						"Enter depht value to consider begining from left view folder and track all sub directory in it\n"
								+ "Input must be a number format if anything goes wrong '0' is the default value",
						"0");
				if (depthANS == null)
					return;
				Integer depth;
				try {
					depth = Integer.parseInt(depthANS);
				} catch (NumberFormatException e1) {
					depth = 0;
					// e1.printStackTrace();
				}
				String user = DialogHelper.showTextInputDialog("Recursive Cleaner", "User Name To clean for",
						"Enter User name (it Work even if The user wasn't in the list)", Setting.getActiveUser());
				if (user == null)
					return;

				WatchServiceHelper.setRuning(false);
				Path dir = leftView.getDirectoryPath();
				try {
					FileTracker.deleteOutFile(dir, user);
					Files.walk(dir, depth).filter(p -> Files.isDirectory(p) && !p.equals(dir)).forEach(p -> {
						Main.ProcessTitle(p.getFileName().toString());
						FileTracker.deleteOutFile(p, user);
					});
					Main.ResetTitle();
					refreshBothViews(null);
					WatchServiceHelper.setRuning(true);
				} catch (IOException e) {
					// e.printStackTrace();
				}

			}
		});
		TrackerMenu.getItems().add(CleanRecursively);
		MenuItem showConflict = new MenuItem("Show Conflict Log");
		showConflict.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				checkConflictLog();
			}
		});
		TrackerMenu.getItems().add(showConflict);
		/**
		 * Set up helpMenu
		 */
		aboutMenuItem.setOnAction(e -> DialogHelper.showAlert(Alert.AlertType.INFORMATION, "About", null,
				"Tracker Explorer v2.0\n\n" + "Copyright © 2019 by Ahmad Said"));
	}

	private void initializeButtons() {
		autoExpand.setSelected(Setting.getAutoExpand());
		GoDesktop.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				leftView.setmDirectoryThenRefresh(
						new File(System.getProperty("user.home") + File.separator + "Desktop"));
			}
		});

		File[] roots = File.listRoots();

		// check https://www.geeksforgeeks.org/javafx-menubutton/
		for (int i = 0; i < roots.length; i++) {
			MenuItem mx = new MenuItem(roots[i].toString());
			File temp = roots[i];
			mx.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					SplitViewController view = getFocusedPane();
					if (view != null) {
						view.setmDirectoryThenRefresh(temp);
					} else {
						leftView.setmDirectoryThenRefresh(temp);
					}

				}
			});
			rootsMenu.getItems().add(mx);
		}
	}

	public void SynctoRight(String path) {
		rightView.setmDirectoryThenRefresh(leftView.NametoFile(path));
	}

	public void SynctoLeft(String path) {
		leftView.setmDirectoryThenRefresh(rightView.NametoFile(path));
	}

	public void SynctoLeftParent() {
		File parent = rightView.getmDirectory().getParentFile();
		if (parent.exists()) {
			leftView.setmDirectoryThenRefresh(parent);
			leftView.refresh();
		}
	}

	@FXML
	public void copy() {
		WatchServiceHelper.setRuning(false);
		if (leftView.isFocused()) {
			List<Path> source = leftView.getSelection();
			Path target = rightView.getDirectoryPath();
			FileHelper.copy(source, target);
			rightView.getMfileTracker().OperationUpdate(source, leftView.getMfileTracker(), "copy");
		} else if (rightView.isFocused()) {
			List<Path> source = rightView.getSelection();
			Path target = leftView.getDirectoryPath();
			FileHelper.copy(source, target);
			leftView.getMfileTracker().OperationUpdate(source, rightView.getMfileTracker(), "copy");
		}
		WatchServiceHelper.setRuning(true);
	}

	@FXML
	public void move() {
		WatchServiceHelper.setRuning(false);
		if (leftView.isFocused()) {
			List<Path> source = leftView.getSelection();
			Path target = rightView.getDirectoryPath();
			FileHelper.move(source, target);
			rightView.getMfileTracker().OperationUpdate(source, leftView.getMfileTracker(), "move");
		} else if (rightView.isFocused()) {
			List<Path> source = rightView.getSelection();
			Path target = leftView.getDirectoryPath();
			FileHelper.move(source, target);
			leftView.getMfileTracker().OperationUpdate(source, rightView.getMfileTracker(), "move");
		}
		// refresh is committed from within FileTracker#insert function
		WatchServiceHelper.setRuning(true);
	}

	public void delete() {
		SplitViewController focusedPane = getFocusedPane();
		if (focusedPane != null) {
			List<Path> source = focusedPane.getSelection();
			if (!FileHelper.delete(source)) // if not confirmed do nothing
				return;
			WatchServiceHelper.setRuning(false);
			focusedPane.getMfileTracker().OperationUpdate(source, null, "delete");
			focusedPane.refresh();
			// refresh and change to parent directory if deleted folder was the other view
			SplitViewController unfocused = getunFocusedPane();
			if (source.contains(unfocused.getDirectoryPath())) {
				unfocused.setmDirectory(unfocused.getmDirectory().getParentFile());
				unfocused.refresh();
			}
		}
		WatchServiceHelper.setRuning(true);
	}

	public void rename() {
		WatchServiceHelper.setRuning(false);
		SplitViewController focusedPane = getFocusedPane();
		if (focusedPane != null) {
			List<Path> selection = focusedPane.getSelection();
			if (selection.size() == 1) {
				Path target = FileHelper.rename(selection.get(0), focusedPane);
				focusedPane.refresh();
				// refresh and change directory if rename was committed on same directory as the
				// other view
				SplitViewController unfocused = getunFocusedPane();
				if (selection.get(0).equals(unfocused.getDirectoryPath())) {
					unfocused.setmDirectory(target.toFile());
					unfocused.refresh();
				}
			}
			// tracker info is resolved in rename function
		}
		WatchServiceHelper.setRuning(true);
	}

	public void createDirectory() {
		SplitViewController focusedPane = getFocusedPane();
		if (focusedPane != null)
			FileHelper.createDirectory(focusedPane.getDirectoryPath(), focusedPane);
	}

	public void createFile() {
		SplitViewController focusedPane = getFocusedPane();
		if (focusedPane != null)
			FileHelper.createFile(focusedPane.getDirectoryPath());
	}

	public void focusTextField() {
		SplitViewController focusedPane = getFocusedPane();
		if (focusedPane != null)
			focusedPane.getPathField().requestFocus();
	}

	public void focusSearchField() {
		SplitViewController focusedPane = getFocusedPane();
		if (focusedPane != null)
			focusedPane.focusSearchFeild();
		else
			leftView.focusSearchFeild();
	}

	public void RevealINExplorer() {
		SplitViewController focusedPane = getFocusedPane();
		if (focusedPane != null)
			focusedPane.RevealINExplorer();
		else
			leftView.RevealINExplorer();
	}

	public void ClearSearchField() {
		SplitViewController focusedPane = getFocusedPane();
		if (focusedPane != null)
			focusedPane.clearSearchFeild();
		else
			leftView.clearSearchFeild();
	}

	public void focus_VIEW() {
		SplitViewController focusedPane = getFocusedPane();
		if (focusedPane != null)
			focusedPane.focusTable();
		else
			leftView.focusTable();
	}

	public void focus_Switch_VIEW() {
		SplitViewController focusedPane = getunFocusedPane();
		if (focusedPane != null)
			focusedPane.focusTable();
		else
			leftView.focusTable();
	}

	public void countWords() {
		Path path = getSelectedPath();
		if (path != null && path.toString().endsWith(".txt")) {
			Path resultPath = path.getParent().resolve("[Word Count] " + path.getFileName());
			try (PrintWriter printWriter = new PrintWriter(resultPath.toFile())) {
				Arrays.stream(new String(Files.readAllBytes(path), StandardCharsets.UTF_8).toLowerCase().split("\\W+"))
						.collect(Collectors.groupingBy(Function.identity(), TreeMap::new, Collectors.counting()))
						.entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed())
						.forEach(printWriter::println);
				Desktop.getDesktop().open(resultPath.toFile());
			} catch (IOException e) {
				DialogHelper.showException(e);
			}
		}
	}

	private SplitViewController getFocusedPane() {
		if (leftView.isFocused()) {
			return leftView;
		} else if (rightView.isFocused()) {
			return rightView;
		} else {
			return null;
		}
	}

	private SplitViewController getunFocusedPane() {
		if (leftView.isFocused()) {
			return rightView;
		} else if (rightView.isFocused()) {
			return leftView;
		} else {
			return null;
		}
	}

	private SplitViewController getFocusedPane(TextField textField) {
		if (textField == leftView.getPathField()) {
			return leftView;
		} else {
			return rightView;
		}
	}

	@Nullable
	private Path getSelectedPath() {
		SplitViewController focusedPane = getFocusedPane();
		if (focusedPane == null)
			return null;
		List<Path> selection = focusedPane.getSelection();
		if (selection.size() != 1)
			return null;
		return selection.get(0);
	}

	private void onTextEntered(TextField textField) {
		SplitViewController focusedPane = getFocusedPane(textField);
		String command = textField.getText().trim();
		File file = new File(command);
		if (file.exists()) {
			focusedPane.openFile(file);
			focusedPane.requestFocus();
		} else if (command.startsWith(ACTION_SELECT)) {
			String regex = command.substring(ACTION_SELECT.length()).trim();
			focusedPane.select(regex);
			focusedPane.requestFocus();
		} else if (command.startsWith(ACTION_COPY)) {
			String regex = command.substring(ACTION_COPY.length()).trim();
			focusedPane.select(regex);
			focusedPane.requestFocus();
			copy();
		} else if (command.startsWith(ACTION_MOVE)) {
			String regex = command.substring(ACTION_MOVE.length()).trim();
			focusedPane.select(regex);
			focusedPane.requestFocus();
			move();
		} else if (command.startsWith(ACTION_DELETE)) {
			String regex = command.substring(ACTION_DELETE.length()).trim();
			focusedPane.select(regex);
			focusedPane.requestFocus();
			delete();
		} else if (command.startsWith(ACTION_OPEN)) {
			String regex = command.substring(ACTION_OPEN.length()).trim();
			focusedPane.select(regex);
			focusedPane.requestFocus();
			for (Path path : focusedPane.getSelection()) {
				try {
					Desktop.getDesktop().open(path.toFile());
				} catch (Exception e) {
					DialogHelper.showException(e);
				}
			}
		}
		textField.setText(focusedPane.getDirectoryPath().toString());
	}

	@FXML
	public void DominateRight() {
		rightView.setmDirectoryThenRefresh(leftView.getmDirectory());
	}

	@FXML
	public void DominateLeft() {
		leftView.setmDirectoryThenRefresh(rightView.getmDirectory());
	}

	@FXML
	public void Deleteright() {
		if (!rightView.getMfileTracker().isTracked()) {
			DialogHelper.showAlert(AlertType.INFORMATION, "Delete Tracker Data", "This is already Untracked folder",
					"Are you kidding me.");
			return;
		}
		boolean ans = DialogHelper.showConfirmationDialog("Delete Tracker Data",
				"Are you Sure You want to wipe tracker data?",
				"Note: this have nothing to do with your files, it just delete .tracker_explorer.txt"
						+ " >>And so set all item to untracked.\nThis cannot be undone!");

		if (ans) {
			rightView.getMfileTracker().deleteFile();
		}
	}

	// i can just refresh both but trying to optimize and call refresh only when
	// Necessary
	public void refreshBothViews(SplitViewController mSplitViewController) {
		// could send null also to refresh both views
		if (mSplitViewController == null || leftView.getmDirectory().equals(rightView.getmDirectory())) {
			leftView.refresh();
			rightView.refresh();
			return;
		}
		mSplitViewController.refresh();
	}

	@FXML
	public void leftTrackRecusivly() {
		String answer = DialogHelper.showTextInputDialog("Recursive Tracker", "Depth to consider",
				"Enter depht value to consider begining from left view folder and track all sub directory in it\n"
						+ "Input must be a number format if anything goes wrong '1' is the default value",
				"1");
		if (answer == null)
			return;
		Integer depth;
		try {
			depth = Integer.parseInt(answer);
		} catch (NumberFormatException e1) {
			depth = 1;
			// e1.printStackTrace();
		}

		Path dir = leftView.getDirectoryPath();
		try {
			leftView.getMfileTracker().trackNewFolder();
			Files.walk(dir, depth).filter(p -> Files.isDirectory(p) && !p.equals(dir)).forEach(p -> {
				Main.ProcessTitle(p.getFileName().toString());
				leftView.getMfileTracker().NewOutFolder(p);
			});
			Main.ResetTitle();
		} catch (IOException e) {
			// e.printStackTrace();
		}
		// refreshBothViews(null);
	}

	@FXML
	void rightBulkRemoveIntro(ActionEvent event) {
		String answer = DialogHelper.showTextInputDialog("Bulk Intro Remover", "Time To exclude from start?",
				"Enter the time to exclude From the begining, this will apply on all Media file in Right View\n"
						+ "Input must be in duration format: ss or mm:ss or hh:mm:ss (example: 234 or 3:54)",
				"00     :00     :00");
		if (answer == null)
			return;
		Duration ans = FilterVLCController.studyFormat(answer, " Feild", true);
		if (ans == null || ans.toSeconds() <= 0)
			return;

		if (!rightView.getMfileTracker().isTracked())
			rightView.getMfileTracker().trackNewFolder();

		for (String name : rightView.getCurrentFilesList()) {
			if (VLC.isVLCMediaExt(name)) {
				Main.ProcessTitle(name);
				String key = name;
				List<String> options = rightView.getMfileTracker().getMapDetails().get(key);
				ArrayList<String> newCopy = new ArrayList<String>();
				for (int i = 0; i < 3; i++) {
					newCopy.add(options.get(i));
				}
				String desc = name + " [Skipped Intro]";
				newCopy.add("0");
				newCopy.add("" + (int) ans.toSeconds());
				newCopy.add(desc);

				rightView.getMfileTracker().getMapDetails().put(key, newCopy);
			}
		}
		Main.ResetTitle();
		rightView.getMfileTracker().writeMap();
	}

	@FXML
	public void AddToContextMenu() {
		Setting.AddToContextMenu();
	}

	@FXML
	public void RemoveFromContextMenu() {
		Setting.RemoveFromContextMenu();
	}

	public Path getLeftLastKnowLocation() {
		return leftView.getDirectoryPath();
	}

	public Path getRightLastKnowLocation() {
		return rightView.getDirectoryPath();
	}

	@FXML
	void toogleAutoBackSync(ActionEvent event) {
		Setting.setBackSync(!Setting.getBackSync());
		DialogHelper.showAlert(AlertType.INFORMATION, "Auto Back Sync",
				"Auto Back Sync Set To " + Setting.getBackSync(),
				"This will make left view move automatically to parent directory of right view.\nSo Both views become always Synced together");
	}

	@FXML
	void toggleAutoExpand(ActionEvent event) {
		Setting.setAutoExpand(!Setting.getAutoExpand());
	}

	// the current problem in conflict log is this case:
	// when user track a new folder without changing any parameter in it
	// i didn't write the map for the issue that in it i also change isRuning
	// of watch service to false but this function is called by watch service it
	// self
	// it give alot of errors
	private void checkConflictLog() {
		boolean ans = DialogHelper.showExpandableConfirmationDialog("Conflict Log", "Conflict Log",
				"This Windows show the difference of files between the last saved tracker data and the current directory state.\nAbbreviations:\n - {$userName} <<>> {$directory}\n - Del = Moved or Deleted\n - New = new Added File. \n Press OK to clear log.",
				FileTracker.getConflictLog());
		if (ans)
			FileTracker.setConflictLog("");
	}

	@FXML
	void CheckForUpdate(ActionEvent event) {
		try {
			Desktop.getDesktop().browse(new URL("https://github.com/Ahmad-Said/tracker-explorer/releases").toURI());
		} catch (IOException | URISyntaxException e) {
			// e.printStackTrace();
		}
	}

	@FXML
	void GetVLC(ActionEvent event) {
		try {
			Desktop.getDesktop().browse(new URL("https://www.videolan.org/vlc").toURI());
		} catch (IOException | URISyntaxException e) {
			// e.printStackTrace();
		}
	}

	@FXML
	void Tutorial(ActionEvent event) {
		try {
			Desktop.getDesktop().browse(new URL("https://github.com/Ahmad-Said/tracker-explorer").toURI());
		} catch (IOException | URISyntaxException e) {
			// e.printStackTrace();
		}
	}

	@FXML
	void KeyBoardShortcut(ActionEvent event) {
		String ky = "Navigation:" + "\n - Tab                   = Focus Table View"
				+ "\n - Ctrl + F              = Focus on search Field"
				+ "\n - Escape                = Clear Search Field"
				+ "\n - Ctrl + Tab            = Switch Focus between Tables"
				+ "\n - Alt + Up || BackSpace = Go To parent Directory"
				+ "\n - Alt + Left Arrow      = Go Back To Previous Folder" + "\n - Alt + Right Arrow     = Go Next"
				+ "\n - Ctrl + Shift + R      = Reveal in Windows Explorer"
				+ "\n - Shift + D             = Focus On Path Field"
				+ "\n\nFile Operations: (Applied on the focused Table)" + "\n - Space            = Toogle MarkSeen"
				+ "\n - Ctrl + N         = New File" + "\n - Ctrl + Shift + N = New Directory"
				+ "\n - Ctrl + C         = Copy to the other Table" + "\n - Ctrl + X         = Move to the other Table"
				+ "\n - Ctrl + X         = Delete Selected Files" + "\n - F2               = Rename Seleted File"
				+ "\n\n - Within Table View:" + "\n - F            = Focus Search Field"
				+ "\n - S            = Clear Search Field" + "\n - Up / Left    = Navigate Selected with Shift support"
				+ "\n - Left / Right = Dominate Other Table View"
				+ "\n - Trick        = Scroll up/down with mouse on clear button to toggle seen/unseen";
		DialogHelper.showAlert(AlertType.INFORMATION, "KeyBoard Shortcuts", "KeyBoard Shortcuts", ky);
	}

	@FXML
	public void ConfigureVLCPath() {
		// https://docs.oracle.com/javafx/2/ui_controls/file-chooser.htm
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Navigate to where VLC is installed");
		File initfile = VLC.getPath_Setup().toFile().getParentFile();
		if (initfile.exists())
			fileChooser.setInitialDirectory(initfile);
		else {
			// try another time to auto detect path:
			// that's in case vlc is installed while already opening the program
			VLC.initializeDefaultVLCPath();
			initfile = VLC.getPath_Setup().toFile().getParentFile();
			if (initfile.exists())
				fileChooser.setInitialDirectory(initfile);
			else
				fileChooser.setInitialDirectory(new File(System.getenv("ProgramFiles")));
		}
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Path To ", "vlc.exe"));
		File vlcfile = fileChooser.showOpenDialog(Main.getPrimaryStage());
		if (vlcfile == null)
			return;
		if (vlcfile.getName().equals("vlc.exe")) {
			Setting.setVLCPath(vlcfile.toURI().toString());
			DialogHelper.showAlert(AlertType.INFORMATION, "Configure VLC Path", "VLC well configured",
					"Path: " + VLC.getPath_Setup());
		} else
			DialogHelper.showAlert(AlertType.ERROR, "Configure VLC Path", "VLC misconfigured",
					"Please chose the right file 'vlc.exe'\n\nCurrent Path:\n " + VLC.getPath_Setup());

	}

	@FXML
	public void ControlVLC() {
		String pass = DialogHelper.showTextInputDialog("VLC Over The Web", "Enter Password Access",
				"Enter Password authorisation to use when accessing vlc.\n\n" + "Note: "
						+ "\n\t- VLC will run into system tray."
						+ "\n\t- If you are using chrome set save password to never"
						+ "\n\tas it may cause problem when changing password.\n",
				Setting.getVLCHttpPass());
		if (pass == null)
			return;
		if (pass.trim().isEmpty())
			pass = "1234";
		Setting.setVLCHttpPass(pass);
		// --extraintf=http to enable http interface
		// --one-instance to make it possible when lunching any video
		// do open in this instance of vlc
		// pass is to set login access
		// .. refer to https://wiki.videolan.org/VLC_command-line_help for more details
		VLC.watchWithRemote(null, " --qt-start-minimized");
		// do open in the web
		boolean test = DialogHelper.showConfirmationDialog("VLC Over The Web", "Do you Want to test Connection ?",
				"This will start url on current system browser");
		if (!test)
			return;

		// ask user if he want to test it
		// https://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java?rq=1
		try (final DatagramSocket socket = new DatagramSocket()) {
			socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
			String ip = socket.getLocalAddress().getHostAddress();
			Desktop.getDesktop().browse(new URL("http://" + ip + ":8080").toURI());

			// this run later to request focus after starting empty web
			Platform.runLater(() -> {
				try {
					TimeUnit.SECONDS.sleep(2);
				} catch (InterruptedException e) {
					// e.printStackTrace();
				}
				Main.getPrimaryStage().requestFocus();
			});

		} catch (URISyntaxException | IOException e) {
			// e.printStackTrace();
		}
	}

	@FXML
	public void ControlVLCAndroid() {
		try {
			Desktop.getDesktop().browse(
					new URL("https://play.google.com/store/apps/details?id=adarshurs.android.vlcmobileremote").toURI());
		} catch (IOException | URISyntaxException e) {
			// e.printStackTrace();
		}

	}

	@FXML
	public void ControlVLCIOS() {
		try {
			Desktop.getDesktop().browse(
					new URL("https://itunes.apple.com/us/app/vlc-mobile-remote/id1140931401?ls=1&mt=8").toURI());
		} catch (IOException | URISyntaxException e) {
			// e.printStackTrace();
		}

	}

	public void saveSetting() {
		Setting.setLeftLastKnowLocation(getLeftLastKnowLocation());
		Setting.setRightLastKnowLocation(getRightLastKnowLocation());
		Setting.setShowLeftNotesColumn(leftStatus.isVisible());
		Setting.setShowRightNotesColumn(rightStatus.isVisible());
	}

}
