package said.ahmad.javafx.tracker.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
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

import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
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
import said.ahmad.javafx.tracker.app.DialogHelper;
import said.ahmad.javafx.tracker.app.Main;
import said.ahmad.javafx.tracker.app.ResourcesHelper;
import said.ahmad.javafx.tracker.app.StringHelper;
import said.ahmad.javafx.tracker.app.look.THEME;
import said.ahmad.javafx.tracker.app.look.THEME_COLOR;
import said.ahmad.javafx.tracker.app.look.ThemeManager;
import said.ahmad.javafx.tracker.app.pref.Setting;
import said.ahmad.javafx.tracker.controller.connection.ConnectionController;
import said.ahmad.javafx.tracker.controller.connection.ConnectionController.ConnectionType;
import said.ahmad.javafx.tracker.controller.splitview.SplitViewController;
import said.ahmad.javafx.tracker.datatype.FavoriteView;
import said.ahmad.javafx.tracker.datatype.SplitViewState;
import said.ahmad.javafx.tracker.fxGraphics.DraggableTab;
import said.ahmad.javafx.tracker.fxGraphics.MenuItemFactory;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.tracker.system.operation.FileHelper;
import said.ahmad.javafx.tracker.system.operation.FileHelperGUIOperation;
import said.ahmad.javafx.tracker.system.services.TrackerPlayer;
import said.ahmad.javafx.tracker.system.services.VLC;
import said.ahmad.javafx.tracker.system.tracker.FileTracker;
import said.ahmad.javafx.util.IpAddress;

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
	private Menu openConnectionMenu;

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
	private MenuButton rootsMenu;
	private Menu subMenuActiveUser;
	private Menu subMenuRemoveUser;

	ToggleGroup toogleActiveUserGroup;

	@FXML
	private Menu cortanaMenu;

	private Stack<SplitViewController> allSplitViewController;
	private Stack<SplitViewController> allSplitViewControllerRemoved;
	private static final String STAGE_TITLE = "Tracker Explorer";
	private Stage stage;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		// for a faster show stage implementation was moved to initializeViewStage
	}

	public void initializePart2AddSplitView(Stage stage, boolean doRefresh) {
		this.stage = stage;
		try {
			allSplitViewController = new Stack<>();
			allSplitViewControllerRemoved = new Stack<>();
			// Assign column to which property in model
			addSplitView(Setting.getLeftLastKnowLocation(), true);
			addSplitView(Setting.getRightLastKnowLocation(), false);
		} catch (Exception e1) {
			e1.printStackTrace();
			DialogHelper.showException(e1);
		}
		if (doRefresh) {
			refreshAllSplitViews();
		}
	}

	/** To be called after setting a scene to the stage */
	public void initializePart3AddTabs(Stage stage, boolean doRefresh) {
		initializeMenuBar();
		Setting.getFavoritesViews().addListener((MapChangeListener<String, FavoriteView>) change -> {
			allSplitViewController
					.forEach(sp -> sp.onFavoriteChanges(change, Setting.getFavoritesViews().isReloadingMapOperation()));
		});
		initializeTabs();
		initializeStage();

		// Restoring last known view as it was
		FavoriteView lastView = Setting.getLastOpenedView();
		if (lastView != null && !Main.isPathArgumentPassed()) {
			while (allSplitViewController.size() < lastView.getSplitStates().size()) {
				// some auto configuration done is performed when adding splitView so done it
				// outside below loop
				addSplitView();
			}
			for (int i = 0; i < lastView.getSplitStates().size(); i++) {
				allSplitViewController.get(i)
						.restoreSplitViewState(new SplitViewState(lastView.getSplitStates().get(i)));
			}
		}
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
			// if template was left hide Desktop button and show leftDominate
			if (isLeftTemplate) {
				newSplit.getLeftDominate().setVisible(true);
				newSplit.getDesktopButton().setText("Desk");
				Insets oldDeskInset = GridPane.getMargin(newSplit.getDesktopButton());
				GridPane.setMargin(newSplit.getDesktopButton(),
						new Insets(oldDeskInset.getTop(), oldDeskInset.getRight(), oldDeskInset.getBottom(), 70));
				Insets oldFavInset = GridPane.getMargin(newSplit.getFavoritesLocations());
				GridPane.setMargin(newSplit.getFavoritesLocations(),
						new Insets(oldFavInset.getTop(), oldFavInset.getRight(), oldFavInset.getBottom(), 125));
			}
			// mean there exist left
			SplitViewController leftNeighbor = allSplitViewController.get(allSplitViewController.size() - 2);
			newSplit.setLeftViewNeighbor(leftNeighbor);
			leftNeighbor.setRightViewNeighbor(newSplit);
			leftNeighbor.getAutoExpand().setOnAction(null);
			leftNeighbor.getAutoExpand().setText("<>");
			leftNeighbor.setAutoExpand(Setting.isAutoExpand());
			leftNeighbor.getExitSplitButton().setVisible(false);
			setSplitAsLastOne(newSplit);
		}
		return newSplit;
	}

	private void setSplitAsLastOne(SplitViewController splitViewController) {
		splitViewController.getAutoExpand().setText("+");
		splitViewController.setAutoExpand(false);
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

	/**
	 * Does remove all split view and reAdd them <br>
	 * Used to reload view CSS stuff after loading a new theme Search for JMetro
	 */
	private void refreshCSSSplitViews() {
		int size = allSplitViewController.size();
		while (size-- != 0) {
			try {
				addSplitView(allSplitViewController.get(0).getmDirectoryPath(), allSplitViewController.get(0).isLeft());
				removeSplitView(0);
				allSplitViewControllerRemoved.clear();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		allSplitViewController.forEach(sp -> sp.refresh(null));
	}

	private void autoFitWidthSplitPane() {
		double dividerEach = 1.0 / allSplitViewController.size();
		double start = dividerEach;
		for (Divider d : allSplitViewPane.getDividers()) {
			d.setPosition(start);
			start += dividerEach;
		}
	}

	private static String DEFAULT_TAB_TITLE = "Default";

	private void initializeTabs() {
		// initialize tabs
		tabPane.getTabs().clear();
		DraggableTab defaultTab = new DraggableTab(DEFAULT_TAB_TITLE,
				Arrays.asList(new SplitViewState(Setting.getLeftLastKnowLocation()),
						new SplitViewState(Setting.getRightLastKnowLocation())));
		defaultTab.setClosable(false);
		defaultTab.flipisEnteringAction();
		tabPane.getTabs().add(defaultTab);
		activeActionTab(defaultTab);

		if (Setting.isRestoreLastOpenedFavorite()) {
			for (String title : Setting.getLastOpenedFavoriteTitle()) {
				if (Setting.getFavoritesViews().containsByTitle(title)) {
					FavoriteView favorite = Setting.getFavoritesViews().getByTitle(title);
					addTabOnly(favorite.getTitle(), favorite.getSplitStatessInitializedCopy());
				}
			}
		}
	}

	/**
	 * Used when closing a tab to restore to previously selected one
	 *
	 * @see #closeCurrentTab()
	 */
	private DraggableTab lastSelectedTab;

	private void activeActionTab(DraggableTab dragTab) {
		dragTab.setOnSelectionChanged(e -> {
			// not in every switch from tab to another
			// 2 tabs will trigger this action the entering tab which we will restore from
			// it's state
			// and the leaving tab which will save it's state
			ArrayList<SplitViewState> splitStates = dragTab.getSplitViewStates();

			if (dragTab.isEnteringAction()) {
				// the new tab switched to
				// change queue for the corresponding tab
				// Sync current shownSplit size to drag ShownSplitSize
				// by adding/removing splitView
				int toBeShownSplit = dragTab.getShownSplitViewSize();
				int shownSplit = allSplitViewController.size();
				// add or remove splitView as needed
				while (toBeShownSplit != shownSplit) {
					// add missing splitView
					for (int i = allSplitViewController.size(); i < dragTab.getShownSplitViewSize(); i++) {
						addSplitView();
						shownSplit++;
					}
					// remove unnecessary splitView
					while (allSplitViewController.size() > dragTab.getShownSplitViewSize()) {
						removeSplitView(allSplitViewController.size() - 1);
						shownSplit--;
					}
				}

				for (int i = 0; i < shownSplit; i++) {
					allSplitViewController.get(i).restoreSplitViewState(splitStates.get(i));
				}
			} else {
				// the tab that is switched from
				// will trigger code first
				dragTab.setShownSplitViewSize(allSplitViewController.size());
				for (int i = 0; i < allSplitViewController.size(); i++) {
					if (i >= splitStates.size()) {
						splitStates.add(new SplitViewState());
					}
					allSplitViewController.get(i).saveStateToSplitState(splitStates.get(i));
				}
				lastSelectedTab = dragTab;
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
		addTabAndSwitch("New Tab", allSplitViewController.stream().map(sp -> new SplitViewState(sp.getmDirectoryPath()))
				.collect(Collectors.toList()));
	}

	public void closeCurrentTab() {
		if (tabPane.getSelectionModel().getSelectedItem().isClosable()) {
			Tab toBeRemoved = tabPane.getSelectionModel().getSelectedItem();
			if (lastSelectedTab != null) {
				tabPane.getSelectionModel().select(lastSelectedTab);
			}
			tabPane.getTabs().remove(toBeRemoved);
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

	private DraggableTab addTabAndSwitch(String title, List<SplitViewState> splitStates) {
		DraggableTab tempTab = addTabOnly(title, splitStates);
		tabPane.getSelectionModel().select(tempTab);
		return tempTab;
	}

	private DraggableTab addTabOnly(String title, List<SplitViewState> splitStates) {
		DraggableTab tempTab = new DraggableTab(title, splitStates);
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

	public void changeInSetting() {
		initializeMenuBar();
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
			try {
				WelcomeController anotherWelcome = newWelcomeControllerStage(new Stage());
				for (int i = 0; i < allSplitViewController.size(); i++) {
					if (anotherWelcome.allSplitViewController.size() < i + 1) {
						anotherWelcome.addSplitView();
					}
					anotherWelcome.allSplitViewController.get(i)
							.navigate(allSplitViewController.get(i).getmDirectoryPath());
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});

		/**
		 * Set up connection menu
		 */
		for (ConnectionType connectionType : ConnectionType.values()) {
			MenuItem mn = new MenuItem(connectionType.toString());
			mn.setOnAction(e -> new ConnectionController(connectionType,
					path -> allSplitViewController.get(0).setmDirectoryThenRefresh(path)));
			openConnectionMenu.getItems().add(mn);
		}

		/**
		 * set up Operation Stage
		 */
		MenuItem oldFashionedNoThemStyle = new MenuItem("old fashioned");
		MenuItem bootStrapThem = new MenuItem("Bootstrap V3");
		MenuItem micosoftWindowsLight = new MenuItem("Windows 10 Theme light");
		MenuItem micosoftWindowsDark = new MenuItem("Windows 10 Theme Dark");
		oldFashionedNoThemStyle.setOnAction(e -> {
			ThemeManager.changeThemeAndApply(stage.getScene(), THEME.MODENAFX, THEME_COLOR.NONE);
			refreshCSSSplitViews();
		});
		bootStrapThem.setOnAction(e -> {
			ThemeManager.changeThemeAndApply(stage.getScene(), THEME.BOOTSTRAPV3, THEME_COLOR.NONE);
			refreshCSSSplitViews();
		});
		micosoftWindowsLight.setOnAction(e -> {
			ThemeManager.changeThemeAndApply(stage.getScene(), THEME.WINDOWS, THEME_COLOR.LIGHT);
			refreshCSSSplitViews();
		});
		micosoftWindowsDark.setOnAction(e -> {
			ThemeManager.changeThemeAndApply(stage.getScene(), THEME.WINDOWS, THEME_COLOR.DARK);
			refreshCSSSplitViews();
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

		MenuItem clearFavorite = new MenuItem("Clear Favorites	(!-!)");
		TrackerMenu.getItems().add(clearFavorite);

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
					Setting.getFavoritesViews().clear();
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

		MenuItem showConflict = new MenuItem("Show Changes Log");
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
				"Tracker Explorer v" + Setting.getVersion() + "\n\n" + "Copyright C 2020 by Ahmad Said"));
	}

	@FXML
	public void openSettingTrackerPlayer() {
		TrackerPlayer.openTrackerSettingGUI();
	}

	public void openFavoriteLocation(FavoriteView favoriteView, SplitViewController splitViewController) {
		String error = "";
		for (SplitViewState path : favoriteView.getSplitStates()) {
			if (!path.getmDirectory().exists()) {
				error += path.toString() + "\n\n";
			}
		}
		if (!error.isEmpty()) {
			DialogHelper.showExpandableAlert(AlertType.INFORMATION, "Open Favorites", "Resources cannot be found",
					"Directory gets moved/deleted, or system provider is offline", error);
		}
		List<SplitViewState> splitStates = favoriteView.getSplitStatessInitializedCopy();
		if (splitViewController == allSplitViewController.get(0)) {
			DraggableTab newTab = new DraggableTab(favoriteView.getTitle(), splitStates);
			activeActionTab(newTab);
			tabPane.getTabs().add(newTab);
			tabPane.getSelectionModel().select(newTab);
		} else {
			int start = allSplitViewController.indexOf(splitViewController);
			int end = Math.min(allSplitViewController.size(), start + splitStates.size());
			int j = 0;
			for (int i = start; i < end; i++) {
				allSplitViewController.get(i).restoreSplitViewStateWithoutQueue(splitStates.get(j++));
			}
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
		boolean ans = DialogHelper.showExpandableConfirmationDialog("Changes Log", "Changes Log",
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
			allSplitViewController.get(0).clearSearchField();
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
		try {
			String ip = IpAddress.getLocalAddress();
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
		return getMostLeftView().getmDirectoryPath();
	}

	public SplitViewController getMostLeftView() {
		return allSplitViewController.get(0);
	}

	public PathLayer getRightLastKnowLocation() {
		return getMostRightView().getmDirectoryPath();
	}

	public SplitViewController getMostRightView() {
		return allSplitViewController.get(allSplitViewController.size() - 1);
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
				+ "\n - Alt + Shift + R       = Reveal in System Explorer"
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
	public void RemoveFromContextMenu() {
		Setting.RemoveFromContextMenu();
	}

	public void RevealINExplorer() {
		SplitViewController focusedPane = getFocusedPane();
		if (focusedPane != null) {
			focusedPane.RevealINExplorer();
		} else {
			getMostLeftView().RevealINExplorer();
		}
	}

	/** @see #initializePart3AddTabs(Stage, boolean) */
	public void saveSetting() {
		Setting.setLeftLastKnowLocation(getLeftLastKnowLocation());
		Setting.setRightLastKnowLocation(getRightLastKnowLocation());
		Setting.setShowLeftNotesColumn(getMostLeftView().isNoteColumnVisible());
		Setting.setShowRightNotesColumn(getMostRightView().isNoteColumnVisible());
		Setting.setAutoExpand(getMostLeftView().isAutoExpand());
		if (Setting.isRestoreLastOpenedFavorite()) {
			ArrayList<String> lastOpenedFavoritesIndex = new ArrayList<>();
			for (Tab tab : tabPane.getTabs()) {
				String title = tab.getTooltip() == null ? null : tab.getTooltip().getText();
				if (title != null && !title.isEmpty()) {
					lastOpenedFavoritesIndex.add(title);
				}
			}
			Setting.setLastOpenedFavoriteTitle(lastOpenedFavoritesIndex);
		}
		Setting.setMaximized(stage.isMaximized());

		Setting.setLastOpenedView(new FavoriteView(DEFAULT_TAB_TITLE, allSplitViewController.stream().map(sp -> {
			SplitViewState state = new SplitViewState();
			sp.saveStateToSplitState(state);
			return state;
		}).collect(Collectors.toList())));
	}

	public void switchRecursive() {
		SplitViewController focusedPane = getFocusedPane();
		if (focusedPane != null) {
			focusedPane.switchRecursive();
		} else {
			getMostLeftView().switchRecursive();
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
		stage.setTitle(toAdd + " - " + STAGE_TITLE);
	}

	public void ResetTitle() {
		stage.setTitle(STAGE_TITLE);
	}

	public void ProcessTitle(String toAppend) {
		char flip = '/';
		if (stage.getTitle() != null && !stage.getTitle().isEmpty() && stage.getTitle().charAt(0) == '\\') {
			flip = '/';
		} else {
			flip = '\\';
		}
		stage.setTitle(" " + flip + toAppend);
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

	public static WelcomeController newWelcomeControllerStage(Stage stage) throws IOException {
		FXMLLoader loader = new FXMLLoader();
		WelcomeController anotherWelcome = new WelcomeController();
		loader.setController(anotherWelcome);
		loader.setLocation(ResourcesHelper.getResourceAsURL("/fxml/Welcome.fxml"));
		loader.load();
		Parent root = loader.getRoot();
		Scene scene = new Scene(root);
		ThemeManager.applyTheme(scene);
		stage.setScene(scene);
		ThemeManager.applyTheme(scene);
		stage.getIcons().add(ThemeManager.DEFAULT_ICON_IMAGE);
		stage.show();
		anotherWelcome.initializePart2AddSplitView(stage, true);
		anotherWelcome.initializePart3AddTabs(stage, false);
		return anotherWelcome;
	}
}
