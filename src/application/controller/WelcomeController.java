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
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
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
import application.RecursiveFileWalker;
import application.StringHelper;
import application.VLC;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
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
	private MenuItem aboutMenuItem;

	@FXML
	private ToggleButton autoExpand;

	@FXML
	private MenuItem bulkRemoveMenuItem;

	@FXML
	private MenuItem deleteItem;

	@FXML
	private Button DeleteTracker;

	@FXML
	private CheckBox FavoriteCheckBox;

	@FXML
	private MenuButton FavoritesLocations;

	@FXML
	private Menu fileMenu;

	@FXML
	private Button GoDesktop;

	@FXML
	private Menu helpMenu;

	@FXML
	private Button leftBack;

	@FXML
	private Button leftDominate;

	@FXML
	private Button leftExplorer;

	@FXML
	private TableColumn<TableViewModel, HBox> lefthboxActions;

	@FXML
	private TableColumn<TableViewModel, ImageView> leftIcon;

	// show favorites location navigation
	@FXML
	private Label leftLabelItemsNumber;

	@FXML
	private TableColumn<TableViewModel, String> leftName;

	@FXML
	private Button leftNavigateRecursive;

	@FXML
	private Button leftNext;

	@FXML
	private TableColumn<TableViewModel, String> leftNote;

	@FXML
	private TextField leftPathInput;

	@FXML
	private TextField leftPredictNavigation;

	@FXML
	private CheckBox leftRecusiveSearch;

	@FXML
	private Button leftSearchButton;

	@FXML
	private TextField leftSearchField;

	@FXML
	private TableColumn<TableViewModel, Double> leftSize;

	@FXML
	private TableView<TableViewModel> leftTable;

	@FXML
	private MenuButton leftToolsMenu;

	@FXML
	private Button leftUp;

	@FXML
	private MenuItem newFile;

	@FXML
	private MenuItem newFolder;

	@FXML
	private MenuItem renameItem;

	@FXML
	private Button rightBack;

	@FXML
	private Button rightDominate;

	@FXML
	private Button rightExplorer;

	@FXML
	private TableColumn<TableViewModel, HBox> righthboxActions;

	@FXML
	private TableColumn<TableViewModel, ImageView> rightIcon;

	@FXML
	private Label rightLabelItemsNumber;

	@FXML
	private TableColumn<TableViewModel, String> rightName;

	@FXML
	private Button rightNavigateRecursive;

	@FXML
	private Button rightNext;

	@FXML
	private TableColumn<TableViewModel, String> rightNote;

	@FXML
	private TextField rightPathInput;

	@FXML
	private TextField rightPredictNavigation;

	@FXML
	private CheckBox rightRecusiveSearch;

	@FXML
	private Button rightSearchButton;

	@FXML
	private TextField rightSearchField;

	@FXML
	private TableColumn<TableViewModel, Double> rightSize;

	@FXML
	private TableView<TableViewModel> rightTable;

	@FXML
	private MenuButton rightToolsMenu;

	@FXML
	private Button rightUp;

	@FXML
	private MenuButton rootsMenu;
	Menu subMenuActiveUser;
	Menu subMenuRemoveUser;
	@FXML
	private Button SwapButton;

	@FXML
	private MenuItem toogleAutoBackSync;
	ToggleGroup toogleActiveUserGroup;
	@FXML
	private Menu TrackerMenu;

	@FXML
	private MenuItem trackLeftRecusivlyMenuItem;

	ObservableList<TableViewModel> leftDataTable = FXCollections.observableArrayList();
	ObservableList<TableViewModel> rightDataTable = FXCollections.observableArrayList();
	private SplitViewController leftView;
	private SplitViewController rightView;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		try {

			// Assign column to which property in model

			VLC.initializeDefaultVLCPath();
			initializeMenuBar();

			// later do thing if return false;

			leftNote.setCellValueFactory(new PropertyValueFactory<TableViewModel, String>("NoteText"));
			leftName.setCellValueFactory(new PropertyValueFactory<TableViewModel, String>("Name"));
			lefthboxActions.setCellValueFactory(new PropertyValueFactory<TableViewModel, HBox>("hboxActions"));
			leftIcon.setCellValueFactory(new PropertyValueFactory<TableViewModel, ImageView>("imgIcon"));
			leftSize.setCellValueFactory(new PropertyValueFactory<TableViewModel, Double>("FileSize"));

			rightNote.setCellValueFactory(new PropertyValueFactory<TableViewModel, String>("NoteText"));
			rightName.setCellValueFactory(new PropertyValueFactory<TableViewModel, String>("Name"));
			righthboxActions.setCellValueFactory(new PropertyValueFactory<TableViewModel, HBox>("hboxActions"));
			rightIcon.setCellValueFactory(new PropertyValueFactory<TableViewModel, ImageView>("imgIcon"));
			rightSize.setCellValueFactory(new PropertyValueFactory<TableViewModel, Double>("FileSize"));

			leftView = new SplitViewController(StringHelper.InitialLeftPath, true, this, leftDataTable, leftPathInput,
					leftUp, leftSearchField, leftSearchButton, leftTable, leftExplorer, lefthboxActions, leftBack,
					leftNext, leftPredictNavigation, leftRecusiveSearch, leftLabelItemsNumber, leftNavigateRecursive,
					leftToolsMenu, leftNote, leftName);
			rightView = new SplitViewController(StringHelper.InitialRightPath, false, this, rightDataTable,
					rightPathInput, rightUp, rightSearchField, rightSearchButton, rightTable, rightExplorer,
					righthboxActions, rightBack, rightNext, rightPredictNavigation, rightRecusiveSearch,
					rightLabelItemsNumber, rightNavigateRecursive, rightToolsMenu, rightNote, rightName);

			refreshBothViews(null);
			initializeButtons();

			// initialize coloumn preference
			rightNote.setVisible(Setting.getShowRightNotesColumn());
			leftNote.setVisible(Setting.getShowLeftNotesColumn());
		} catch (Exception e1) {
			DialogHelper.showException(e1);
		}
	}

	private void initializeButtons() {
		autoExpand.setSelected(Setting.isAutoExpand());
		GoDesktop.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				changeDirInLastestView(new File(System.getProperty("user.home") + File.separator + "Desktop"));
			}
		});
		initializeRootsMenu();
		rootsMenu.setOnMouseReleased(m -> {
			initializeRootsMenu();
		});
		initializeFavorites();
	}

	private void initializeFavorites() {
		for (int i = Setting.getFavoritesLocations().size() - 1; i >= 0; i--) {
			AddandPriorizethisMenu(Setting.getFavoritesLocations().get(i));
		}
		FavoriteCheckBox.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (FavoriteCheckBox.isSelected()) {
					AddandPriorizethisMenu(leftView.getDirectoryPath());
				} else {
					removeFavorite(leftView.getDirectoryPath());
				}
			}
		});
	}

	private void initializeMenuBar() {
		/**
		 * Set up file menu
		 */
		newFile.setOnAction(e -> createFile());
		newFile.setAccelerator(Main.SHORTCUT_NEW_FILE);

		newFolder.setOnAction(e -> createDirectory());
		newFolder.setAccelerator(Main.SHORTCUT_NEW_DIRECTORY);

		renameItem.setOnAction(e -> rename());
		renameItem.setAccelerator(Main.SHORTCUT_RENAME);

		deleteItem.setOnAction(e -> delete());
		deleteItem.setAccelerator(Main.SHORTCUT_DELETE);

		/**
		 * set up FileTracker Menu TODO
		 */
		// check http://tutorials.jenkov.com/javafx/menubar.html
		// Menus :
		MenuItem clearFavorite = new MenuItem("Clear Favorites	(!-!)");
		MenuItem NewUser = new MenuItem("Add A new User");
		subMenuActiveUser = new Menu("Set Active User");
		toogleActiveUserGroup = new ToggleGroup();
		subMenuRemoveUser = new Menu("Remove User");
		MenuItem ChangeLimitFilesRecursive = new MenuItem("Change Limit Files Count In recursive");

		// -!Clear Favorites!- Menu Item
		clearFavorite.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				boolean ans = DialogHelper.showConfirmationDialog("Clear Favorite",
						"Are You sure you want to clear favorites items??",
						"This Cannot be undone!!\nIn case you get bothered of specific item,"
								+ "\nOpen in the left view Then uncheck box \"Favorite Folder\"\nPress Ok to clear list. OtherWise cancel operation");
				if (ans) {
					Setting.getFavoritesLocations().clear();
					FavoritesLocations.getItems().clear();
					allMenuFavoriteLocation.clear();
				}
			}
		});
		TrackerMenu.getItems().add(0, clearFavorite);

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
				if (user == null || user.isEmpty()) {
					return;
				}
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
				// this file tracker also change setting
				// so it automatically when adding the new user
				// will be selected when comparing in add
				FileTracker.updateUserFileName(user);
				AddActiveUser(user);
				AddRemoveUser(user);
				Setting.getUserNames().add(user);
				refreshBothViews(null);
			}
		});
		int where = 4;
		TrackerMenu.getItems().add(where++, NewUser);
		// Select Active User Menu
		for (String user : Setting.getUserNames()) {
			AddActiveUser(user);
		}
		TrackerMenu.getItems().add(where++, subMenuActiveUser);

		// Remove User
		for (String user : Setting.getUserNames()) {
			AddRemoveUser(user);
		}
		TrackerMenu.getItems().add(where++, subMenuRemoveUser);

		TrackerMenu.getItems().add(where++, ChangeLimitFilesRecursive);
		ChangeLimitFilesRecursive.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				ChangeLimitFilesRecursiveAction();
			}
		});

		// Clean Recursively
		MenuItem CleanRecursively = new MenuItem("Clean Recursively");
		CleanRecursively.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				String depthANS = DialogHelper.showTextInputDialog("Recursive Cleaner", "From Left Depth to consider",
						"Enter depht value to consider begining from left view folder and track all sub directory in it\n"
								+ "Input must be a number format if anything goes wrong '0' is the default value",
						"0");
				if (depthANS == null) {
					return;
				}
				Integer depth;
				try {
					depth = Integer.parseInt(depthANS);
				} catch (NumberFormatException e1) {
					depth = 0;
					// e1.printStackTrace();
				}
				String user = DialogHelper.showTextInputDialog("Recursive Cleaner", "User Name To clean for",
						"Enter User name (it Work even if The user wasn't in the list)", Setting.getActiveUser());
				if (user == null) {
					return;
				}

				Path dir = leftView.getDirectoryPath();
				StringHelper.setTemp(depth);
				Thread cleanerThread = new Thread() {
					@Override
					public void run() {
						try {
							RecursiveFileWalker r = new RecursiveFileWalker();
							Files.walkFileTree(dir, EnumSet.noneOf(FileVisitOption.class), StringHelper.getTemp(), r);

							r.getParent().forEach(p -> {
								Platform.runLater(() -> Main.ProcessTitle(p.toString()));
								FileTracker.deleteOutFile(p, user);
							});

							// Main.ResetTitle();
							Platform.runLater(() -> refreshBothViews(null));
						} catch (IOException e) {
							// e.printStackTrace();
						}
					}
				};
				cleanerThread.start();
			}
		});
		TrackerMenu.getItems().add(2, CleanRecursively);
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
				"Tracker Explorer v2.1\n\n" + "Copyright © 2019 by Ahmad Said"));
	}

	ArrayList<RadioMenuItem> allActiveUser = new ArrayList<>();
	private Map<Path, MenuItem> allMenuFavoriteLocation = new HashMap<Path, MenuItem>();
	ArrayList<MenuItem> allRemoveUser = new ArrayList<>();

	public void initializeRootsMenu() {
		rootsMenu.getItems().clear();
		File[] roots = File.listRoots();
		// check https://www.geeksforgeeks.org/javafx-menubutton/
		for (File temp : roots) {
			MenuItem mx = new MenuItem(temp.toString());
			mx.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					changeDirInLastestView(temp);
				}
			});
			rootsMenu.getItems().add(mx);
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
		if (Setting.getActiveUser().equals(user)) {
			mn.setSelected(true);
		}
		subMenuActiveUser.getItems().add(mn);
	}

	private void AddandPriorizethisMenu(Path path) {
		if (allMenuFavoriteLocation.containsKey(path)) {
			removeFavorite(path);
		}
		String text;
		try {
			// this try catch resolve if folder was a root
			text = path.getFileName().toString();
		} catch (Exception e) {
			text = path.toString();
		}
		MenuItem mx = new MenuItem(text);
		File temp = path.toFile();
		mx.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				changeDirInLastestView(temp);
			}
		});
		allMenuFavoriteLocation.put(path, mx);
		if (!Setting.getFavoritesLocations().contains(path)) {
			Setting.getFavoritesLocations().add(0, path);
		}
		FavoritesLocations.getItems().add(0, mx);
		// auto clean menu if more than 10
		// if (allMenuFavoriteLocation.size() > 10)
		// removeLastFavorite();
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

					if (item.getText().equals(mn.getText())) {
						subMenuActiveUser.getItems().remove(item);
					}
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
		if (mn.getText().equals("default")) {
			mn.setDisable(true);
		}
		allRemoveUser.add(mn);
		subMenuRemoveUser.getItems().add(mn);
	}

	@FXML
	public void AddToContextMenu() {
		Setting.AddToContextMenu();
	}

	private SplitViewController changeDirInLastestView(File temp) {
		// get the lastest view changed by detecting title folder name
		// and giving priority the left view
		SplitViewController view = null;
		if (SplitViewController.isLastChangedLeft) {
			view = leftView;
		} else {
			view = rightView;
		}

		// best approach it let the use to chooze on interface which one
		view.setmDirectoryThenRefresh(temp);
		view.requestFocus();
		return view;
	}

	protected void ChangeLimitFilesRecursiveAction() {
		String answer = DialogHelper.showTextInputDialog("Change Limit Files Count In recursive",
				"Max files count To consider",
				"Enter max files count \r\n" + "Note: The more You high this number\n\t ---> The more You wait",
				"" + Setting.getMaxLimitFilesRecursive());
		if (answer == null) {
			return;
		}
		Integer Maxi;
		try {
			Maxi = Integer.parseInt(answer);
		} catch (NumberFormatException e1) {
			Maxi = Setting.getMaxLimitFilesRecursive();
			// e1.printStackTrace();
		}
		Setting.setMaxLimitFilesRecursive(Maxi);
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
		if (ans) {
			FileTracker.setConflictLog("");
		}
	}

	@FXML
	void CheckForUpdate(ActionEvent event) {
		try {
			Desktop.getDesktop().browse(new URL("https://github.com/Ahmad-Said/tracker-explorer/releases").toURI());
		} catch (IOException | URISyntaxException e) {
			// e.printStackTrace();
		}
	}

	public void ClearSearchField() {
		SplitViewController focusedPane = getFocusedPane();
		if (focusedPane != null) {
			focusedPane.clearSearchField();
		} else {
			leftView.clearSearchField();
		}
	}

	@FXML
	public void ConfigureVLCPath() {
		// https://docs.oracle.com/javafx/2/ui_controls/file-chooser.htm
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Navigate to where VLC is installed");
		File initfile = VLC.getPath_Setup().toFile().getParentFile();
		if (initfile.exists()) {
			fileChooser.setInitialDirectory(initfile);
		} else {
			// try another time to auto detect path:
			// that's in case vlc is installed while already opening the program
			VLC.initializeDefaultVLCPath();
			initfile = VLC.getPath_Setup().toFile().getParentFile();
			if (initfile.exists()) {
				fileChooser.setInitialDirectory(initfile);
			} else {
				fileChooser.setInitialDirectory(new File(System.getenv("ProgramFiles")));
			}
		}
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Path To ", "vlc.exe"));
		File vlcfile = fileChooser.showOpenDialog(Main.getPrimaryStage());
		if (vlcfile == null) {
			return;
		}
		if (vlcfile.getName().equals("vlc.exe")) {
			Setting.setVLCPath(vlcfile.toURI().toString());
			DialogHelper.showAlert(AlertType.INFORMATION, "Configure VLC Path", "VLC well configured",
					"Path: " + VLC.getPath_Setup());
		} else {
			DialogHelper.showAlert(AlertType.ERROR, "Configure VLC Path", "VLC misconfigured",
					"Please chose the right file 'vlc.exe'\n\nCurrent Path:\n " + VLC.getPath_Setup());
		}

	}

	@FXML
	public void ControlVLC() {
		String pass = DialogHelper.showTextInputDialog("VLC Over The Web", "Enter Password Access",
				"Enter Password authorisation to use when accessing vlc.\n\n" + "Note: "
						+ "\n\t- VLC will run into system tray."
						+ "\n\t- If you are using chrome set save password to never"
						+ "\n\tas it may cause problem when changing password."
						+ "\n\t- Changing password require that VLC is not running" + "\n\t to Take effect",
				Setting.getVLCHttpPass());
		if (pass == null) {
			return;
		}
		if (pass.trim().isEmpty()) {
			pass = "1234";
		}
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
		if (!test) {
			return;
		}

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

	@FXML
	public void copy() {
		// WatchServiceHelper.setRuning(false);
		if (leftView.isFocusedTable()) {
			List<Path> source = leftView.getSelection();
			Path target = rightView.getDirectoryPath();
			FileHelper.copy(source, target);
			// rightView.getMfileTracker().OperationUpdate(source,
			// leftView.getMfileTracker(), "copy");
		} else if (rightView.isFocusedTable()) {
			List<Path> source = rightView.getSelection();
			Path target = leftView.getDirectoryPath();
			FileHelper.copy(source, target);
			// leftView.getMfileTracker().OperationUpdate(source,
			// rightView.getMfileTracker(), "copy");
		}
		// WatchServiceHelper.setRuning(true);
	}

	// private void removeLastFavorite() {
	// removeFavorite(Setting.getFavoritesLocations().get(Setting.getFavoritesLocations().size()
	// - 1));
	// }

	public void countWords() {
		Path path = getSelectedPath();
		if (path != null && path.toString().endsWith(".txt")) {
			Path resultPath = path.getParent().resolve("[Word Count] " + path.getFileName());
			try (PrintWriter printWriter = new PrintWriter(resultPath.toFile())) {
				Arrays.stream(new String(Files.readAllBytes(path), StandardCharsets.UTF_8).toLowerCase().split("\\W+"))
						.collect(Collectors.groupingBy(Function.identity(), TreeMap::new, Collectors.counting()))
						.entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed())
						.forEach(printWriter::println);
				// Desktop.getDesktop().open(resultPath.toFile());
				StringHelper.open(resultPath.toUri().toString());
			} catch (IOException e) {
				DialogHelper.showException(e);
			}
		}
	}

	public void createDirectory() {
		SplitViewController focusedPane = getFocusedPane();
		if (focusedPane != null) {
			FileHelper.createDirectory(focusedPane.getDirectoryPath(), focusedPane);
		}
	}

	public void createFile() {
		SplitViewController focusedPane = getFocusedPane();
		if (focusedPane != null) {
			FileHelper.createFile(focusedPane.getDirectoryPath());
		}
	}

	public void delete() {
		// WatchServiceHelper.setRuning(false);
		SplitViewController focusedPane = getFocusedPane();
		if (focusedPane != null) {
			List<Path> source = focusedPane.getSelection();
			if (!FileHelper.delete(source)) {
				return;
			}
			// WatchServiceHelper.setRuning(false);
			// focusedPane.getMfileTracker().OperationUpdate(source, null, "delete");
			// focusedPane.refreshAsPathField();
			refreshWhenDetected(source.get(0).getParent());
			// refresh and change to parent directory if deleted folder was the other view
			SplitViewController unfocused = getunFocusedPane();
			if (source.stream().anyMatch(p -> p.equals(unfocused.getDirectoryPath()))) {
				unfocused.setmDirectoryThenRefresh(unfocused.getmDirectory().getParentFile());
			}
		}
		// WatchServiceHelper.setRuning(true);
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
			rightView.refreshAsPathField();
		}
	}

	@FXML
	public void DominateLeft() {
		leftView.setPathFieldThenRefresh(rightView.getPathField().getText());
	}

	@FXML
	public void DominateRight() {
		rightView.setPathFieldThenRefresh(leftView.getPathField().getText());
	}

	public void focus_Switch_VIEW() {
		SplitViewController focusedPane = getunFocusedPane();
		if (focusedPane != null) {
			focusedPane.focusTable();
		} else {
			leftView.focusTable();
		}
	}

	public void focus_VIEW() {
		SplitViewController focusedPane = getFocusedPane();
		if (focusedPane != null) {
			focusedPane.focusTable();
		} else {
			leftView.focusTable();
		}
	}

	public void focusSearchField() {
		SplitViewController focusedPane = getFocusedPane();
		if (focusedPane != null) {
			focusedPane.focusSearchField();
		} else {
			leftView.focusSearchField();
		}
	}

	public void focusTextField() {
		SplitViewController focusedPane = getFocusedPane();
		if (focusedPane != null) {
			focusedPane.getPathField().requestFocus();
		}
	}

	@FXML
	void GetBulkRenameUtility(ActionEvent event) {
		boolean openIt = DialogHelper.showAlert(AlertType.INFORMATION, "Get Bulk Rename Utility",
				"Bulk Rename Utility: Rename Like A Pro",
				"Bulk Rename Utility is a tool to rename multiple files"
						+ " together like inserting a prefix to 50 files in one"
						+ " click and much more!\nFor a proper use select Files"
						+ " from view, Drag and drop them in Bulk Rename Utility view." + " See more at their website.."
						+ "\nA link to the tool will open now in browser.");
		if (openIt) {
			try {
				Desktop.getDesktop().browse(new URL("https://www.bulkrenameutility.co.uk").toURI());
			} catch (IOException | URISyntaxException e) {
				// e.printStackTrace();
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

	public Path getLeftLastKnowLocation() {
		return leftView.getDirectoryPath();
	}

	public SplitViewController getLeftView() {
		return leftView;
	}

	@FXML
	void GetMp3Tag(ActionEvent event) {
		boolean openIt = DialogHelper.showAlert(AlertType.INFORMATION, "Get Mp3 Tag", "Mp3 Tag: Tag Like A Pro",
				"Mp3tag is a powerful and easy-to-use tool to edit metadata of audio files:"
						+ " batch tag-editing, Export to HTML, RTF, CSV.. and much more!"
						+ "\nFor a proper use you can select Files"
						+ " from view, Drag and drop them in Mp3 Tag, or just right click media files!."
						+ "\nA link to the tool will open now in browser.");
		if (openIt) {

			try {
				Desktop.getDesktop().browse(new URL("https://www.mp3tag.de/en/").toURI());
			} catch (IOException | URISyntaxException e) {
				// e.printStackTrace();
			}
		}
	}

	public Path getRightLastKnowLocation() {
		return rightView.getDirectoryPath();
	}

	public SplitViewController getRightView() {
		return rightView;
	}

	@Nullable
	private Path getSelectedPath() {
		SplitViewController focusedPane = getFocusedPane();
		if (focusedPane == null) {
			return null;
		}
		List<Path> selection = focusedPane.getSelection();
		if (selection.size() != 1) {
			return null;
		}
		return selection.get(0);
	}

	public MenuButton getToggleFavorite() {
		return FavoritesLocations;
	}

	// private SplitViewController getFocusedPane(TextField textField) {
	// if (textField == leftView.getPathField()) {
	// return leftView;
	// } else {
	// return rightView;
	// }
	// }

	private SplitViewController getunFocusedPane() {
		if (leftView.isFocused()) {
			return rightView;
		} else if (rightView.isFocused()) {
			return leftView;
		} else {
			return null;
		}
	}

	@FXML
	void GetVLC(ActionEvent event) {
		boolean openIt = DialogHelper.showAlert(AlertType.INFORMATION, "Get VLC", "VLC Media Player: Watch Like A Pro",
				"Simple, fast and powerful!\n" + "That's what make VLC most famous media player:"
						+ "\n - Plays everything," + "\n - Completely Free with no ads!" + "\n ... and much more!"
						+ "\nFor a proper use just select Media Files" + " and hit Enter!."
						+ "\nA link to the tool will open now in browser.");
		if (openIt) {
			try {
				Desktop.getDesktop().browse(new URL("https://www.videolan.org/vlc").toURI());
			} catch (IOException | URISyntaxException e) {
				// e.printStackTrace();
			}
		}
	}

	public boolean isAutoExpandToRight() {
		return autoExpand.isSelected();
	}

	@FXML
	void KeyBoardShortcut(ActionEvent event) {
		String ky = "Navigation:" + "\n - Tab                   = Focus Table View"
				+ "\n - Ctrl + F              = Focus on search Field"
				+ "\n - Escape                = Clear Search Field"
				+ "\n - Ctrl + Tab || F3      = Switch Focus between Tables"
				+ "\n - Alt + Up || BackSpace = Go To parent Directory"
				+ "\n - Alt + Left Arrow      = Go Back To Previous Folder" + "\n - Alt + Right Arrow     = Go Next"
				+ "\n - Alt + Shift + R      = Reveal in Windows Explorer"
				+ "\n - Shift + D             = Focus On Path Field"
				+ "\n - Shift + F             = Mark Folder As Favorite"
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
	public void leftTrackRecusivly() {
		String answer = DialogHelper.showTextInputDialog("Recursive Tracker", "Depth to consider",
				"Enter depht value to consider begining from left view folder and track all sub directory in it\n"
						+ "Input must be a number format if anything goes wrong '1' is the default value",
				"1");
		if (answer == null) {
			return;
		}
		Integer depth;
		try {
			depth = Integer.parseInt(answer);
		} catch (NumberFormatException e1) {
			depth = 1;
			// e1.printStackTrace();
		}
		StringHelper.setTemp(depth);
		Path dir = leftView.getDirectoryPath();
		Thread trackerThread = new Thread() {
			@Override
			public void run() {
				try {
					RecursiveFileWalker r = new RecursiveFileWalker();
					Files.walkFileTree(dir, EnumSet.noneOf(FileVisitOption.class), StringHelper.getTemp(), r);
					r.getParent().forEach(p -> {
						Platform.runLater(() -> Main.ProcessTitle(p.toString()));
						leftView.getMfileTracker().NewOutFolder(p);
					});
					Platform.runLater(() -> refreshBothViews(null));
				} catch (IOException e) {
					// e.printStackTrace();
				}
			}
		};
		trackerThread.start();
	}

	@FXML
	public void move() {
		// WatchServiceHelper.setRuning(false);
		if (leftView.isFocusedTable()) {
			List<Path> source = leftView.getSelection();
			Path target = rightView.getDirectoryPath();
			FileHelper.move(source, target);
			// rightView.getMfileTracker().OperationUpdate(source,
			// leftView.getMfileTracker(), "move");
		} else if (rightView.isFocusedTable()) {
			List<Path> source = rightView.getSelection();
			Path target = leftView.getDirectoryPath();
			FileHelper.move(source, target);
			// leftView.getMfileTracker().OperationUpdate(source,
			// rightView.getMfileTracker(), "move");
		}
		// refresh is committed from within FileTracker#insert function
		// WatchServiceHelper.setRuning(true);
	}

	public void RecursiveHelpersetBlocked(boolean state) {
		if (state == true) {
			autoExpand.setSelected(false);
		} else {
			autoExpand.setSelected(Setting.isAutoExpand());
		}
		rightDominate.setDisable(state);
		leftDominate.setDisable(state);
		autoExpand.setDisable(state);
		FavoritesLocations.setDisable(state);
		TrackerMenu.setDisable(state);
		SwapButton.setDisable(state);
		rootsMenu.setDisable(state);
		GoDesktop.setDisable(state);
	}

	// i can just refresh both but trying to optimize and call refresh only when
	// Necessary
	public void refreshBothViews(SplitViewController mSplitViewController) {
		// could send null also to refresh both views
		if (mSplitViewController == null || leftView.getmDirectory().equals(rightView.getmDirectory())) {
			rightView.refresh(null);
			leftView.refresh(null);
			return;
		}
		mSplitViewController.refresh(null);
	}

	public void refreshBothViewsAsPathField(SplitViewController mSplitViewController) {
		if (mSplitViewController == null || leftView.getmDirectory().equals(rightView.getmDirectory())) {
			leftView.refreshAsPathField();
			rightView.refreshAsPathField();
			return;
		}
		mSplitViewController.refreshAsPathField();
	}

	// this is used in case of change on a view without refreshing it
	// so to force change on the other view do this
	public void refreshTheOtherView(SplitViewController mSplitViewController) {
		if (leftView.getmDirectory().equals(rightView.getmDirectory())) {
			// do search and refresh the other view
			// problem see mfile tracker where is used
			if (mSplitViewController.equals(leftView)) {
				rightView.refreshAsPathField();
			} else {
				leftView.refreshAsPathField();
			}
		}
	}

	// the lastest version of refresh: send paths and refresh the corresponding
	// views
	public void refreshWhenDetected(Path... paths) {
		if (paths.length == 0) {
			refreshBothViewsAsPathField(null);
		}
		for (Path path : paths) {
			if (leftView.getDirectoryPath().equals(path)) {
				leftView.refreshAsPathField();
			}
			if (rightView.getDirectoryPath().equals(path)) {
				rightView.refreshAsPathField();
			}
		}
	}

	private void removeFavorite(Path path) {
		if (Setting.getFavoritesLocations().contains(path)) {
			Setting.getFavoritesLocations().remove(path);
			FavoritesLocations.getItems().remove(allMenuFavoriteLocation.get(path));
			allMenuFavoriteLocation.remove(path);
		}
	}

	@FXML
	public void RemoveFromContextMenu() {
		Setting.RemoveFromContextMenu();
	}

	public void rename() {
		SplitViewController focusedPane = getFocusedPane();
		if (focusedPane != null) {
			List<Path> selection = focusedPane.getSelection();
			if (selection.size() == 1) {
				Path src = selection.get(0);
				if (src.getNameCount() == 0) {
					return;
				}
				Path target = FileHelper.rename(src, false);
				if (target == null) {
					return;
				}
				// file tracker operation update
				focusedPane.getMfileTracker().operationUpdate(target, src.toFile().getName(),
						target.toFile().getName());
				// refresh directory is satisfied by watch service

				// scroll to renamed item in any view
				Thread tempScroll = new Thread() {

					@Override
					public void run() {
						try {
							TimeUnit.MILLISECONDS.sleep(100);
							Platform.runLater(() -> focusedPane.ScrollToName(target.getFileName().toString()));
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				};
				tempScroll.start();
			} else {
				new RenameUtilityController(selection);
			}
		}
	}

	public void RevealINExplorer() {
		SplitViewController focusedPane = getFocusedPane();
		if (focusedPane != null) {
			focusedPane.RevealINExplorer();
		} else {
			leftView.RevealINExplorer();
		}
	}

	@FXML
	void rightBulkRemoveIntro(ActionEvent event) {
		if (rightView.isOutOfTheBoxHelper()) {
			DialogHelper.showAlert(AlertType.INFORMATION, "Bulk Intro Remover", "Recursive Mode Restriction",
					"this feature is unavailable in recursive mode,\r\n"
							+ "Please turn it off then try again.\nIf you like it to be contact developpers!");
			return;
		}
		String answer = DialogHelper.showTextInputDialog("Bulk Intro Remover", "Time To exclude from start?",
				"Enter the time to exclude From the begining, this will apply on all Media file in Right View\n"
						+ "Input must be in duration format: ss or mm:ss or hh:mm:ss (example: 234 or 3:54)",
				"00     :00     :00");
		if (answer == null) {
			return;
		}
		Duration ans = FilterVLCController.studyFormat(answer, " Feild", true);
		if (ans == null || ans.toSeconds() <= 0) {
			return;
		}

		if (!rightView.getMfileTracker().isTracked()) {
			rightView.getMfileTracker().trackNewFolder();
		}

		for (String name : rightView.getCurrentFilesListName()) {
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

	public void saveSetting() {
		Setting.setLeftLastKnowLocation(getLeftLastKnowLocation());
		Setting.setRightLastKnowLocation(getRightLastKnowLocation());
		Setting.setShowLeftNotesColumn(leftNote.isVisible());
		Setting.setShowRightNotesColumn(rightNote.isVisible());
	}

	@FXML
	public void SwapView() {
		String temp = rightView.getPathField().getText();
		DominateRight();
		leftView.setPathFieldThenRefresh(temp);
	}

	public void switchRecursive() {
		SplitViewController focusedPane = getFocusedPane();
		if (focusedPane != null) {
			focusedPane.switchRecursive();
		} else {
			leftView.switchRecursive();
		}
	}

	public void SynctoLeft(String pathField) {
		leftView.setPathFieldThenRefresh(pathField);
		// leftView.setmDirectoryThenRefresh(rightView.NametoFile(path));
	}

	public void SynctoLeftParent() {
		File parent = rightView.getmDirectory().getParentFile();
		if (parent.exists()) {
			leftView.setmDirectoryThenRefresh(parent);
			leftView.refresh(null);
		}
	}

	public void SynctoRight(String pathField) {
		rightView.setPathFieldThenRefresh(pathField);
		// rightView.setmDirectoryThenRefresh(leftView.NametoFile(path));
	}

	@FXML
	void toggleAutoExpand(ActionEvent event) {
		Setting.setAutoExpand(!Setting.isAutoExpand());
	}

	@FXML
	void toogleAutoBackSync(ActionEvent event) {
		Setting.setBackSync(!Setting.getBackSync());
		DialogHelper.showAlert(AlertType.INFORMATION, "Auto Back Sync",
				"Auto Back Sync Set To " + Setting.getBackSync(),
				"This will make left view move automatically to parent directory of right view."
						+ "\nSo Both views become always Synced together");
	}

	public void ToogleFavorite(Path path) {
		if (Setting.getFavoritesLocations().contains(path)) {
			removeFavorite(path);
		} else {
			AddandPriorizethisMenu(path);
		}
		updateFavoriteCheckBox(false);
	}

	@FXML
	void Tutorial(ActionEvent event) {
		try {
			Desktop.getDesktop().browse(new URL("https://github.com/Ahmad-Said/tracker-explorer").toURI());
		} catch (IOException | URISyntaxException e) {
			// e.printStackTrace();
		}
	}

	public void updateFavoriteCheckBox(boolean isOutofTheBoxHelper) {
		if (isOutofTheBoxHelper) {
			FavoriteCheckBox.setVisible(false);
		} else {
			FavoriteCheckBox.setVisible(true);
			FavoriteCheckBox.setSelected(Setting.getFavoritesLocations().contains(leftView.getDirectoryPath()));
		}
	}

}
