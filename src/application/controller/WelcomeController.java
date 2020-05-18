package application.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import application.DialogHelper;
import application.Main;
import application.StringHelper;
import application.controller.splitview.SplitViewController;
import application.datatype.Setting;
import application.datatype.SplitViewState;
import application.fxGraphics.DraggableTab;
import application.fxGraphics.MenuItemFactory;
import application.system.RecursiveFileWalker;
import application.system.file.PathLayer;
import application.system.file.PathLayerHelper;
import application.system.operation.FileHelper;
import application.system.operation.FileHelperGUIOperation;
import application.system.services.TrackerPlayer;
import application.system.services.VLC;
import application.system.tracker.FileTracker;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.SplitPane.Divider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import jfxtras.styles.jmetro8.JMetro;

public class WelcomeController implements Initializable {
	static final KeyCombination SHORTCUT_OPEN_NEW_TAB = new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN);
	static final KeyCombination SHORTCUT_CLOSE_CURRENT_TAB = new KeyCodeCombination(KeyCode.W,
			KeyCombination.CONTROL_DOWN);
	static final KeyCombination SHORTCUT_SWITCH_NEXT_TABS = new KeyCodeCombination(KeyCode.TAB,
			KeyCombination.CONTROL_DOWN);
	static final KeyCombination SHORTCUT_SWITCH_PREVIOUS_TABS = new KeyCodeCombination(KeyCode.TAB,
			KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);

	static final KeyCombination SHORTCUT_EASY_FOCUS_SWITCH_VIEW = new KeyCodeCombination(KeyCode.F3);
	static final KeyCombination SHORTCUT_FOCUS_VIEW = new KeyCodeCombination(KeyCode.TAB);
	static final KeyCombination SHORTCUT_FOCUS_PREVIOUS_VIEW = new KeyCodeCombination(KeyCode.TAB,
			KeyCombination.SHIFT_DOWN);

	@FXML
	private BorderPane borderPane;
	@FXML
	private SplitPane allSplitViewPane;

	// TOP Border Pane
	// File SubMenus
	@FXML
	private Menu newEmbedWindow;
	@FXML
	private MenuItem newWindow;
	@FXML
	private MenuItem newFile;
	@FXML
	private MenuItem newFolder;
	@FXML
	private MenuItem deleteItem;

	// Tracker SubMenus
	@FXML
	private Menu TrackerMenu;

	@FXML
	private TabPane tabPane;

	@FXML
	private MenuItem aboutMenuItem;

	// View SubMenus
	@FXML
	private Menu themeSelection;
	@FXML
	private MenuItem showOperationStage;
	@FXML
	private MenuItem renameItem;

	@FXML
	private MenuItem bulkRemoveMenuItem;

	@FXML
	private Menu helpMenu;

	@FXML
	private MenuButton rightToolsMenu;

	@FXML
	private MenuButton rootsMenu;
	private Menu subMenuActiveUser;
	private Menu subMenuRemoveUser;

	ToggleGroup toogleActiveUserGroup;

	@FXML
	private Menu cortanaMenu;

	private Stack<SplitViewController> allSplitViewController;
	private Stack<SplitViewController> allSplitViewControllerRemoved;
	private SplitViewController leftView;
	private SplitViewController rightView;
	private String stageTitle = "";
	private Stage stage;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		try {
			allSplitViewController = new Stack<>();
			allSplitViewControllerRemoved = new Stack<>();
			// Assign column to which property in model

			initializeMenuBar();

			leftView = addSplitView(StringHelper.InitialLeftPath, true);
			rightView = addSplitView(StringHelper.InitialRightPath, false);
			// Split view do refresh later on call of #initializeViewStage.. from main
			// to faster showing stage first

			initializeViewSetting();
			// refresh is done since we switch to default tab
			initializeTabs();
			initializeButtons();
		} catch (Exception e1) {
			e1.printStackTrace();
			DialogHelper.showException(e1);
		}
	}

	/**
	 * @param stage the stage to set having Scene
	 */
	public void initializeViewStage(Stage stage, boolean doRefreshView) {
		this.stage = stage;
		if (doRefreshView) {
			leftView.refresh(null);
			rightView.refresh(null);
		}
		initializeStage();
	}

	private void initializeStage() {
		stage.getScene().addEventFilter(KeyEvent.KEY_RELEASED, e -> {
			if (SHORTCUT_OPEN_NEW_TAB.match(e)) {
				openNewTab();
			} else if (SHORTCUT_CLOSE_CURRENT_TAB.match(e)) {
				closeCurrentTab();
			} else if (SHORTCUT_SWITCH_NEXT_TABS.match(e)) {
				switchNextTabs();
			} else if (SHORTCUT_SWITCH_PREVIOUS_TABS.match(e)) {
				switchPreviousTab();
			} else if (SHORTCUT_EASY_FOCUS_SWITCH_VIEW.match(e) || SHORTCUT_FOCUS_VIEW.match(e)) {
				focusNextSplitView();
			} else if (SHORTCUT_FOCUS_PREVIOUS_VIEW.match(e)) {
				focusPreviousSplitView();
			}
		});
	}

	private SplitViewController addSplitView() {
		boolean doAddLeft = false;
		if (allSplitViewController.size() % 2 == 0) {
			doAddLeft = true;
		}
		SplitViewController newSplitView = null;
		try {
			newSplitView = addSplitView(allSplitViewController.peek().getmDirectoryPath(), doAddLeft);
			newSplitView.refresh(null);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return newSplitView;
	}

	private SplitViewController addSplitView(PathLayer initialePath, boolean isLeftTemplate) throws IOException {
		SplitViewController newSplit = null;
		if (allSplitViewControllerRemoved.size() != 0) {
			newSplit = allSplitViewControllerRemoved.pop();
			newSplit.setmDirectoryThenRefresh(initialePath);
		} else {
			newSplit = new SplitViewController(initialePath, isLeftTemplate, this);
			if (isLeftTemplate) {
				SplitViewController.loadFXMLViewAsLeft(newSplit);
			} else {
				SplitViewController.loadFXMLViewAsRight(newSplit);
			}
		}
		newSplit.getAutoExpand().setText("+");
		MenuItem mn = new MenuItem("Close This View");
		final SplitViewController splitView = newSplit;
		newSplit.getExitSplitButton().setOnAction(e -> removeSplitView(splitView));
		mn.setOnAction(e -> splitView.getExitSplitButton().fire());
		newSplit.getAutoExpand().setContextMenu(new ContextMenu(mn));

		allSplitViewController.add(newSplit);
		allSplitViewPane.getItems().add(newSplit.getViewPane());

		autoFitWidthSplitPane();
		if (allSplitViewController.size() > 1) {
			// mean there exist left
			SplitViewController leftNeighbor = allSplitViewController.get(allSplitViewController.size() - 2);
			newSplit.setLeftViewNeighbor(leftNeighbor);
			leftNeighbor.setRightViewNeighbor(newSplit);
			leftNeighbor.getAutoExpand().setOnAction(null);
			leftNeighbor.getAutoExpand().setText("<>");
			leftNeighbor.getExitSplitButton().setVisible(false);
			setSplitAsLastOne(newSplit);
		}
		return newSplit;
	}

	private void setSplitAsLastOne(SplitViewController splitViewController) {
		splitViewController.getAutoExpand().setText("+");
		splitViewController.getAutoExpand().setSelected(false);
		splitViewController.setRightViewNeighbor(null);
		splitViewController.getExitSplitButton().setVisible(true);
		splitViewController.getAutoExpand().setOnAction(e -> addSplitView());
	}

	private void removeSplitView(SplitViewController toRemoveSplitView) {
		removeSplitView(allSplitViewController.indexOf(toRemoveSplitView));
	}

	private void removeSplitView(int toRemoveIndex) {
		int indexLastOne = allSplitViewController.size() - 1;
		SplitViewController removedSplitView = allSplitViewController.get(toRemoveIndex);
		allSplitViewPane.getItems().remove(toRemoveIndex);
		allSplitViewController.remove(toRemoveIndex);
		// updating neighbors
		if (removedSplitView.getRightViewNeighbor() != null && removedSplitView.getLeftViewNeighbor() != null) {
			removedSplitView.getRightViewNeighbor().setLeftViewNeighbor(removedSplitView.getLeftViewNeighbor());
			removedSplitView.getLeftViewNeighbor().setRightViewNeighbor(removedSplitView.getRightViewNeighbor());
		}
		allSplitViewControllerRemoved.add(removedSplitView);
		if (allSplitViewPane.getItems().size() == 0) {
			stage.close();
			return;
		}
		if (indexLastOne == toRemoveIndex) {
			setSplitAsLastOne(removedSplitView.getLeftViewNeighbor());
		}
		autoFitWidthSplitPane();
	}

	private void autoFitWidthSplitPane() {
		double dividerEach = 1.0 / allSplitViewController.size();
		double start = dividerEach;
		for (Divider d : allSplitViewPane.getDividers()) {
			d.setPosition(start);
			start += dividerEach;
		}
	}

	private void initializeViewSetting() {
		leftView.setAutoExpand(Setting.isAutoExpand());
	}

	private void initializeButtons() {

	}

	private void initializeTabs() {
		// initialize tabs
		tabPane.getTabs().clear();
		DraggableTab defaultTab = new DraggableTab("Default",
				Arrays.asList(StringHelper.InitialLeftPath, StringHelper.InitialRightPath));
		defaultTab.setClosable(false);
		defaultTab.flipisEnteringAction();
		tabPane.getTabs().add(defaultTab);
		activeActionTab(defaultTab);

		if (Setting.isRestoreLastOpenedFavorite()) {
			int sizeFavo = Setting.getFavoritesLocations().getTitle().size();
			for (int i : Setting.getLastOpenedFavoriteIndex()) {
				if (i >= 0 && i < sizeFavo) {
					addTabOnly(Setting.getFavoritesLocations().getTitle().get(i),
							Arrays.asList(Setting.getFavoritesLocations().getLeftLoc().get(i),
									Setting.getFavoritesLocations().getRightLoc().get(i)));
				}
			}
		}
	}

	private void activeActionTab(DraggableTab dragTab) {
		dragTab.setOnSelectionChanged(e -> {
			// not in every switch from tab to another
			// 2 tabs will trigger this action the entering tab which we will restore from
			// it's state
			// and the leaving tab which will save it's state
			ArrayList<SplitViewState> splitStates = dragTab.getSplitViewStates();

			// Sync states size
			int maxStates = splitStates.size();
			int shownSplit = allSplitViewController.size();
			// If showing split view more than saved add new Split States same as last split
			// View
			if (maxStates != shownSplit) {
				PathLayer defaultDir = splitStates.get(splitStates.size() - 1).getmDirectory();
				for (int i = maxStates - 1; i < shownSplit; i++) {
					splitStates.add(new SplitViewState(defaultDir));
				}
			}

			if (dragTab.isEnteringAction()) {
				// the new tab switched to
				// change queue for the corresponding tab
				for (int i = 0; i < shownSplit; i++) {
					allSplitViewController.get(i).restoreSplitViewState(splitStates.get(i));
				}
			} else {
				// the tab that is switched from
				// will trigger code first
				for (int i = 0; i < shownSplit; i++) {
					allSplitViewController.get(i).saveStateToSplitState(splitStates.get(i));
				}
			}
			dragTab.flipisEnteringAction();
		});
		dragTab.getGraphic().setOnDragOver(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				if (event.getDragboard().hasFiles() || event.getDragboard().hasContent(DataFormat.URL)
						|| event.getDragboard().hasString()) {
					event.acceptTransferModes(TransferMode.ANY);
				}
				tabPane.getSelectionModel().select(dragTab);
			}
		});
		dragTab.getGraphic().setOnMouseClicked(e -> {
			if (e.getButton().equals(MouseButton.SECONDARY)) {
				ContextMenu mn = new ContextMenu();
				MenuItem mnRenameTitle = new MenuItem("Rename");
				mn.getItems().addAll(mnRenameTitle);
				mn.show(Main.getPrimaryStage(), e.getScreenX(), e.getScreenY());
				mnRenameTitle.setOnAction(eR -> {
					String title = DialogHelper.showTextInputDialog("Rename Tab", "Enter New Name", "",
							dragTab.getLabelText());
					if (title == null || title.isEmpty()) {
						return;
					}
					title = title.replaceAll(";", "_");
					dragTab.setLabelText(title);
				});
			}
		});

	}

	@FXML
	private void plusTab() {
		addTabAndSwitch("New Tab", getSplitDirectories());
	}

	private List<PathLayer> getSplitDirectories() {
		return allSplitViewController.stream().map(spCon -> spCon.getmDirectoryPath()).collect(Collectors.toList());
	}

	public void closeCurrentTab() {
		if (tabPane.getSelectionModel().getSelectedItem().isClosable()) {
			tabPane.getTabs().remove(tabPane.getSelectionModel().getSelectedItem());
		} else {
			if (tabPane.getTabs().size() == 1) {
				Platform.exit();
			}
		}
	}

	public void openNewTab() {
		plusTab();
	}

	public void switchNextTabs() {
		tabPane.getSelectionModel()
				.select((tabPane.getSelectionModel().getSelectedIndex() + 1) % tabPane.getTabs().size());
	}

	public void switchPreviousTab() {
		tabPane.getSelectionModel()
				.select((tabPane.getSelectionModel().getSelectedIndex() - 1 + tabPane.getTabs().size())
						% tabPane.getTabs().size());

	}

	private DraggableTab addTabAndSwitch(String title, List<PathLayer> splitDirectories) {
		DraggableTab tempTab = addTabOnly(title, splitDirectories);
		tabPane.getSelectionModel().select(tempTab);
		return tempTab;
	}

	private DraggableTab addTabOnly(String title, List<PathLayer> splitDirectories) {
		DraggableTab tempTab = new DraggableTab(title, splitDirectories);
		activeActionTab(tempTab);
		tabPane.getTabs().add(tempTab);
		return tempTab;
	}

	@Nullable
	private SplitViewController getFocusedPane() {
		SplitViewController focusedSplit = null;
		Node focusedNode = stage.getScene().getFocusOwner();
		while (focusedNode != null && !(focusedNode instanceof GridPane)) {
			focusedNode = focusedNode.getParent();
		}
		for (SplitViewController splitView : allSplitViewController) {
			if (focusedNode == splitView.getViewPane() || splitView.isFocused()) {
				focusedSplit = splitView;
				break;
			}
		}
		return focusedSplit;
	}

	private void focusNextSplitView() {
		SplitViewController lastFocus = getFocusedPane();
		if (lastFocus != null && lastFocus.isFocusedSearchField()) {
			lastFocus.requestFocus();
		} else if (lastFocus != null && lastFocus.getRightViewNeighbor() != null) {
			lastFocus.getRightViewNeighbor().requestFocus();
		} else {
			allSplitViewController.get(0).requestFocus();
		}
	}

	private void focusPreviousSplitView() {
		SplitViewController lastFocus = getFocusedPane();
		if (lastFocus != null && lastFocus.getLeftViewNeighbor() != null) {
			lastFocus.getLeftViewNeighbor().requestFocus();
		} else {
			allSplitViewController.peek().requestFocus();
		}
	}

//	public void focus_Switch_VIEW() {
//		SplitViewController focusedPane = getunFocusedPane();
//		if (focusedPane != null) {
//			focusedPane.focusTable();
//		} else {
//			leftView.focusTable();
//		}
//		switcher = !switcher;
//	}
//
//
//	public void focus_VIEW() {
//		SplitViewController focusedPane = getFocusedPane();
//		if (focusedPane != null) {
//			focusedPane.focusTable();
//		} else {
//			if (switcher) {
//				leftView.focusTable();
//			} else {
//				rightView.focusTable();
//			}
//			switcher = !switcher;
//		}
//	}
//
//	public void focusSearchField() {
//		SplitViewController focusedPane = getFocusedPane();
//		if (focusedPane != null) {
//			focusedPane.focusSearchField();
//		} else {
//			leftView.focusSearchField();
//		}
//	}
//
//	public void focusTextField() {
//		SplitViewController focusedPane = getFocusedPane();
//		if (focusedPane != null) {
//			focusedPane.getPathField().requestFocus();
//		}
//	}

	public void changeInSetting() {
		initializeMenuBar();
		allSplitViewController.forEach(s -> s.reloadFavorites());
		refreshAllSplitViews();
	}

	private void initializeMenuBar() {
		/**
		 * Set up file menu
		 */
		// New Embed Window menu
		MenuItem newSplitLeftTemplate = new MenuItem("Left Template");
		newSplitLeftTemplate.setOnAction(e -> {
			try {
				addSplitView(allSplitViewController.peek().getmDirectoryPath(), true).refresh(null);
			} catch (IOException e3) {
				e3.printStackTrace();
			}
		});
		MenuItem newSplitRightTemplate = new MenuItem("Right Template");
		newSplitRightTemplate.setOnAction(e -> {
			try {
				addSplitView(allSplitViewController.peek().getmDirectoryPath(), false).refresh(null);
			} catch (IOException e3) {
				e3.printStackTrace();
			}
		});
		newEmbedWindow.getItems().addAll(newSplitLeftTemplate, newSplitRightTemplate);
		// New Window menu
		newWindow.setOnAction(e -> {
			FXMLLoader loader = new FXMLLoader();
			// loader.setLocation(getClass().getResource("/fxml/bootstrap3.fxml"));
			loader.setLocation(getClass().getResource("/fxml/Welcome.fxml"));
			try {
				loader.load();
				Parent root = loader.getRoot();
				Scene scene = new Scene(root);
				scene.getStylesheets().add("/css/bootstrap3.css");

				Stage anotherStage = new Stage();
				anotherStage.setScene(scene);
				anotherStage.getIcons().add(new Image(Main.class.getResourceAsStream("/img/icon.png")));

				WelcomeController anotherWelcome = loader.getController();
				anotherStage.show();
				anotherWelcome.initializeViewStage(anotherStage, false);
				anotherWelcome.leftView.navigate(leftView.getmDirectoryPath());
				anotherWelcome.rightView.navigate(rightView.getmDirectoryPath());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});

		/**
		 * set up Operation Stage
		 */
		MenuItem oldFashionedNoThemStyle = new MenuItem("old fashioned");
		MenuItem bootStrapThem = new MenuItem("Bootstrap V3");
		MenuItem micosoftWindowsLight = new MenuItem("Windows 10 Theme light");
		MenuItem micosoftWindowsDark = new MenuItem("Windows 10 Theme Dark");
		oldFashionedNoThemStyle.setOnAction(e -> stage.getScene().getStylesheets().clear());
		bootStrapThem.setOnAction(e -> {
			stage.getScene().getStylesheets().clear();
			stage.getScene().getStylesheets().add("/css/bootstrap3.css");
		});
		micosoftWindowsLight.setOnAction(e -> {
			// need restart application under to work
			if (stage.getScene().getStylesheets().contains("JMetroLightTheme.css")) {
				// need restart application under to work
			} else {
				stage.getScene().getStylesheets().clear();
				new JMetro(JMetro.Style.LIGHT).applyTheme(stage.getScene());
			}
		});
		micosoftWindowsDark.setOnAction(e -> {
			if (stage.getScene().getStylesheets().contains("JMetroDarkTheme.css")) {
				// need restart application under to work
			} else {
				stage.getScene().getStylesheets().clear();
				new JMetro(JMetro.Style.DARK).applyTheme(stage.getScene());
			}
		});
		themeSelection.getItems().addAll(oldFashionedNoThemStyle, bootStrapThem, micosoftWindowsLight,
				micosoftWindowsDark);

		showOperationStage.setOnAction(e -> FileHelperGUIOperation.showOperationStage());
		renameItem.setOnAction(e -> new RenameUtilityController(new ArrayList<>()));

		/**
		 * set up FileTracker Menu
		 */
		// check http://tutorials.jenkov.com/javafx/menubar.html
		// Menus :
		TrackerMenu.getItems().clear();

		MenuItem settingController = new MenuItem("Setting preference");
		TrackerMenu.getItems().add(settingController);

		MenuItem trackLeftRecusivlyMenuItem = new MenuItem("Track left view Recursivly");
		trackLeftRecusivlyMenuItem.setOnAction(e -> leftTrackRecusivly());
		TrackerMenu.getItems().add(trackLeftRecusivlyMenuItem);

		MenuItem clearFavorite = new MenuItem("Clear Favorites	(!-!)");
		TrackerMenu.getItems().add(clearFavorite);

		MenuItem CleanRecursively = new MenuItem("Clean Recursively");
		TrackerMenu.getItems().add(CleanRecursively);

		MenuItem addTocontextMenu = new MenuItem("Add Tracker To Context Menu");
		addTocontextMenu.setOnAction(e -> AddToContextMenu());
		TrackerMenu.getItems().add(addTocontextMenu);

		MenuItem removeTocontextMenu = new MenuItem("Remove Tracker From Context Menu");
		removeTocontextMenu.setOnAction(e -> RemoveFromContextMenu());
		TrackerMenu.getItems().add(removeTocontextMenu);

		MenuItem NewUser = new MenuItem("Add A new User");
		subMenuActiveUser = new Menu("Set Active User");
		toogleActiveUserGroup = new ToggleGroup();
		subMenuRemoveUser = new Menu("Remove User");
		TrackerMenu.getItems().addAll(NewUser, subMenuActiveUser, subMenuRemoveUser);

		MenuItem experimentalFeatures = new MenuItem("---------** Experimental Features **---------");
		TrackerMenu.getItems().add(experimentalFeatures);

		// Setting preference Menu Item
		settingController.setOnAction(e -> {
			try {
				new SettingController(this);
			} catch (Exception e2) {
				e2.printStackTrace();
				DialogHelper.showException(e2);
			}
		});

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
					allSplitViewController.forEach(s -> s.clearFavorites());
				}
			}
		});

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
				refreshAllSplitViews();
			}
		});
		// Select Active User Menu
		for (String user : Setting.getUserNames()) {
			AddActiveUser(user);
		}

		// Remove User
		for (String user : Setting.getUserNames()) {
			AddRemoveUser(user);
		}

		// Clean Recursively
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

				PathLayer dir = leftView.getmDirectoryPath();
				StringHelper.setTemp(depth);
				Thread cleanerThread = new Thread() {
					@Override
					public void run() {
						try {
							RecursiveFileWalker r = new RecursiveFileWalker();
							PathLayerHelper.walkFileTree(dir, StringHelper.getTemp(), false, r);

							r.getDirectories().forEach(p -> {
								Platform.runLater(() -> Main.ProcessTitle(p.toString()));
								FileTracker.deleteOutFile(p, user);
							});

							// Main.ResetTitle();
							Platform.runLater(() -> refreshAllSplitViews());
						} catch (IOException e) {
							// e.printStackTrace();
						}
					}
				};
				cleanerThread.start();
			}
		});

		MenuItem showConflict = new MenuItem("Show Conflict Log");
		showConflict.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				checkConflictLog();
			}
		});
		TrackerMenu.getItems().add(showConflict);

		MenuItemFactory.registerMenu(cortanaMenu);
		cortanaMenu.setOnShowing(e -> {
			cortanaMenu.getItems().remove(1, cortanaMenu.getItems().size());
			TrackerPlayer.getAllShortcutTracker().forEach((shortcut, realFile) -> {
				MenuItemFactory.getMenuItem(cortanaMenu, StringHelper.getBaseName(shortcut.getName()), true)
						.setOnAction(e2 -> {
							try {
								TrackerPlayer.openPlaylistInLnk(realFile);
							} catch (IOException | ParseException e1) {
								e1.printStackTrace();
								DialogHelper.showException(e1);
							}
						});
			});
		});
		/**
		 * Set up helpMenu
		 */
		aboutMenuItem.setOnAction(e -> DialogHelper.showAlert(Alert.AlertType.INFORMATION, "About", null,
				"Tracker Explorer v" + Setting.getVersion() + "\n\n" + "Copyright © 2020 by Ahmad Said"));
	}

	@FXML
	public void openSettingTrackerPlayer() {
		TrackerPlayer.openTrackerSettingGUI();
	}

	// TODO later make favorites for all views
	public void openFavoriteLocation(String title, PathLayer leftPath, PathLayer rightPath,
			SplitViewController splitViewController) {
		if (!leftPath.exists()) {
			DialogHelper.showAlert(AlertType.INFORMATION, "Open Favorites", "File Doesn't exist!", leftPath.toString());
		}
		if (!rightPath.exists()) {
			DialogHelper.showAlert(AlertType.INFORMATION, "Open Favorites", "File Doesn't exist!",
					rightPath.toString());
		}
		if (splitViewController == leftView) {
			DraggableTab newTab = new DraggableTab(title, Arrays.asList(leftPath, rightPath));
			activeActionTab(newTab);
			tabPane.getTabs().add(newTab);
			tabPane.getSelectionModel().select(newTab);
		} else {
			splitViewController.setmDirectoryThenRefresh(leftPath);
			splitViewController.synctoRight(rightPath.toString());
		}
	}

	ArrayList<RadioMenuItem> allActiveUser = new ArrayList<>();
	ArrayList<MenuItem> allRemoveUser = new ArrayList<>();

	private void AddActiveUser(String user) {
		RadioMenuItem mn = new RadioMenuItem(user);
		mn.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				FileTracker.updateUserFileName(user);
				refreshAllSplitViews();
			}
		});
		toogleActiveUserGroup.getToggles().add(mn);
		allActiveUser.add(mn);
		if (Setting.getActiveUser().equals(user)) {
			mn.setSelected(true);
		}
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
		File initfile = FileHelper.getParentExeFile(VLC.getPath_Setup(), null);
		fileChooser.setInitialDirectory(initfile);
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Path To ", "vlc.exe"));

		File vlcfile = fileChooser.showOpenDialog(Main.getPrimaryStage());
		if (vlcfile == null) {
			return;
		}
		if (vlcfile.getName().equals("vlc.exe")) {
			VLC.setPath_Setup(vlcfile.toPath());
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
			e.printStackTrace();
		}

	}

	public void refreshAllSplitViews() {
		allSplitViewController.forEach(spCon -> spCon.refreshAsPathField());
	}

	/**
	 * Refresh Split view only current directory match provided File parameter
	 *
	 * @param directoryView
	 * @param exception     Do not refresh given splitView <br>
	 *                      can be null
	 */
	public void refreshAllSplitViewsIfMatch(PathLayer directoryView, SplitViewController exception) {
		allSplitViewController.stream()
				.filter(spCon -> spCon != exception && spCon.getmDirectoryPath().equals(directoryView))
				.forEach(spCon -> spCon.refreshAsPathField());
	}

	public void refreshUnExistingViewsDir() {
		allSplitViewController.forEach(spCon -> {
			boolean doRefresh = false;
			while (!spCon.getmDirectoryPath().exists()) {
				spCon.setmDirectory(spCon.getmDirectoryPath().getParentPath());
				doRefresh = true;
			}
			if (doRefresh) {
				spCon.refresh(null);
			}
		});
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
				e.printStackTrace();
			}
		}
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
				e.printStackTrace();
			}
		}
	}

	public PathLayer getLeftLastKnowLocation() {
		return leftView.getmDirectoryPath();
	}

	public SplitViewController getLeftView() {
		return leftView;
	}

	public PathLayer getRightLastKnowLocation() {
		return rightView.getmDirectoryPath();
	}

	public SplitViewController getRightView() {
		return rightView;
	}

	@Nullable
	private PathLayer getSelectedPath() {
		SplitViewController focusedPane = getFocusedPane();
		if (focusedPane == null) {
			return null;
		}
		List<PathLayer> selection = focusedPane.getSelection();
		if (selection.size() != 1) {
			return null;
		}
		return selection.get(0);
	}

	// private SplitViewController getFocusedPane(TextField textField) {
	// if (textField == leftView.getPathField()) {
	// return leftView;
	// } else {
	// return rightView;
	// }
	// }

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
				e.printStackTrace();
			}
		}
	}

	@FXML
	void KeyBoardShortcut(ActionEvent event) {
		String ky = "Navigation:" + "\n - Tab                   = Focus Table View"
				+ "\n - Ctrl + F              = Focus on search Field"
				+ "\n - Escape                = Clear Search Field" + "\n - F3      = Switch Focus between Tables"
				+ "\n - Ctrl + Tab      = Switch To Next Tab" + "\n - Ctrl + Shift + Tab      = Switch To PreviousTab"
				+ "\n - Ctrl + W      = Close Current Tab" + "\n - Ctrl + T   = Open New Tab"
				+ "\n - F3      = Switch Focus between Tables" + "\n - Alt + Up || BackSpace = Go To parent Directory"
				+ "\n - Alt + Left Arrow      = Go Back To Previous Folder" + "\n - Alt + Right Arrow     = Go Next"
				+ "\n - Alt + Shift + R       = Reveal in Windows Explorer"
				+ "\n - Shift + D             = Focus On Path Field"
				+ "\n - Ctrl + Shift + F      = Mark Folder As Favorite"
				+ "\n - Shift + F             = Open Favorite Menu"
				+ "\n\nFile Operations: (Applied on the focused Table)" + "\n - Space            = Toogle MarkSeen"
				+ "\n - Ctrl + N         = New File" + "\n - Ctrl + Shift + N = New Directory"
				+ "\n - Ctrl + C         = Copy to the other Table" + "\n - Ctrl + X         = Move to the other Table"
				+ "\n - Ctrl + X         = Delete Selected Files" + "\n - F2               = Rename Seleted File"
				+ "\n\n - Within Table View:" + "\n - Up / Left    = Navigate Selected with Shift support"
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
		PathLayer dir = leftView.getmDirectoryPath();
		Thread trackerThread = new Thread() {
			@Override
			public void run() {
				try {
					RecursiveFileWalker r = new RecursiveFileWalker();
					PathLayerHelper.walkFileTree(dir, StringHelper.getTemp(), false, r);
					r.getDirectories().forEach(p -> {
						Platform.runLater(() -> Main.ProcessTitle(p.toString()));
						try {
							leftView.getFileTracker().trackNewOutFolder(p);
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
					Platform.runLater(() -> refreshAllSplitViews());
				} catch (IOException e) {
					// e.printStackTrace();
				}
			}
		};
		trackerThread.start();
	}

//	// i can just refresh both but trying to optimize and call refresh only when
//	// Necessary
//	public void refreshBothViews(SplitViewController mSplitViewController) {
//		// could send null also to refresh both views
//		if (mSplitViewController == null || leftView.getmDirectory().equals(rightView.getmDirectory())) {
//			rightView.refresh(null);
//			leftView.refresh(null);
//			return;
//		}
//		mSplitViewController.refresh(null);
//	}
//
//	public void refreshBothViewsAsPathField(SplitViewController mSplitViewController) {
//		if (mSplitViewController == null || leftView.getmDirectory().equals(rightView.getmDirectory())) {
//			leftView.refreshAsPathField();
//			rightView.refreshAsPathField();
//			return;
//		}
//		mSplitViewController.refreshAsPathField();
//	}
//
//	// this is used in case of change on a view without refreshing it
//	// so to force change on the other view do this
//	public void refreshTheOtherView(SplitViewController mSplitViewController) {
//		if (leftView.getmDirectory().equals(rightView.getmDirectory())) {
//			// do search and refresh the other view
//			// problem see mfile tracker where is used
//			if (mSplitViewController.equals(leftView)) {
//				rightView.refreshAsPathField();
//			} else {
//				leftView.refreshAsPathField();
//			}
//		}
//	}
//
//	// the lastest version of refresh: send paths and refresh the corresponding
//	// views
//	public void refreshWhenDetected(Path... paths) {
//		if (paths.length == 0) {
//			refreshBothViewsAsPathField(null);
//		}
//		for (Path path : paths) {
//			if (leftView.getDirectoryPath().equals(path)) {
//				leftView.refreshAsPathField();
//			}
//			if (rightView.getDirectoryPath().equals(path)) {
//				rightView.refreshAsPathField();
//			}
//		}
//	}

	@FXML
	public void RemoveFromContextMenu() {
		Setting.RemoveFromContextMenu();
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

		if (!rightView.getFileTracker().isTracked()) {
			try {
				rightView.getFileTracker().trackNewFolder();
			} catch (IOException e) {
				e.printStackTrace();
				DialogHelper.showException(e);
				return;
			}
		}

		// TODO
//		for (String name : rightView.getCurrentFilesListName()) {
//			if (VLC.isVLCMediaExt(name)) {
//				Main.ProcessTitle(name);
//				String key = name;
////				List<String> options = rightView.getMfileTracker().getMapDetailsRevolved().get(key);
//				ArrayList<String> newCopy = new ArrayList<String>();
//				for (int i = 0; i < 3; i++) {
//					newCopy.add(options.get(i));
//				}
//				String desc = name + " [Skipped Intro]";
//				newCopy.add("0");
//				newCopy.add("" + (int) ans.toSeconds());
//				newCopy.add(desc);
//
//				rightView.getMfileTracker().getMapDetails().put(key, newCopy);
//			}
//		}
		Main.ResetTitle();
		rightView.getFileTracker().writeMap();
	}

	public void saveSetting() {
		Setting.setLeftLastKnowLocation(getLeftLastKnowLocation());
		Setting.setRightLastKnowLocation(getRightLastKnowLocation());
		Setting.setShowLeftNotesColumn(leftView.isNoteColumnVisible());
		Setting.setShowRightNotesColumn(rightView.isNoteColumnVisible());
		Setting.setAutoExpand(leftView.isAutoExpand());
		if (Setting.isRestoreLastOpenedFavorite()) {
			ArrayList<Integer> lastOpenedFavoritesIndex = new ArrayList<Integer>();
			for (Tab tab : tabPane.getTabs()) {
				int index = Setting.getFavoritesLocations().getIndexByTitle(tab.getTooltip().getText());
				if (index >= 0) {
					lastOpenedFavoritesIndex.add(index);
				}
			}
			Setting.setLastOpenedFavoriteIndex(lastOpenedFavoritesIndex);
		}
	}

	public void switchRecursive() {
		SplitViewController focusedPane = getFocusedPane();
		if (focusedPane != null) {
			focusedPane.switchRecursive();
		} else {
			leftView.switchRecursive();
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

	public void UpdateTitle(String toAdd) {
		stageTitle = toAdd + " - Tracker Explorer";
		stage.setTitle(stageTitle);
	}

	public void ResetTitle() {
		stage.setTitle(stageTitle);
	}

	private char pr = '\\';

	public void ProcessTitle(String toAppend) {
		pr = pr == '\\' ? '/' : '\\';
		stage.setTitle(" " + pr + toAppend);
	}

	public String GetTitle() {
		return stage.getTitle();
	}

	/**
	 * @return the stage
	 */
	public Stage getStage() {
		return stage;
	}
}
