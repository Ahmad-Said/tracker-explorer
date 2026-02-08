package said.ahmad.javafx.tracker.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import said.ahmad.javafx.tracker.controller.setting.base.SettingController;
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
import said.ahmad.javafx.tracker.system.services.VLCException;
import said.ahmad.javafx.tracker.system.tracker.FileTracker;
import said.ahmad.javafx.util.IpAddress;

public class WelcomeController implements Initializable {

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

	@FXML
	private MenuItem reloadXMLSetting;

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

	// Manager instances
	private TabManager tabManager;
	private SplitViewManager splitViewManager;
	private MenuBarManager menuBarManager;
	private KeyboardShortcutManager keyboardShortcutManager;
	private ExternalToolsManager externalToolsManager;

	private Stack<SplitViewController> allSplitViewController;
	private Stack<SplitViewController> allSplitViewControllerRemoved;
	private static final String STAGE_TITLE = "Tracker Explorer";
	private Stage stage;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		// for a faster show stage implementation was moved to initializeViewStage
	}

	public void initializePart2AddSplitView(Stage stage) {
		this.stage = stage;
		try {
			allSplitViewController = new Stack<>();
			allSplitViewControllerRemoved = new Stack<>();
			
			// Initialize managers
			splitViewManager = new SplitViewManager(allSplitViewPane, allSplitViewController, 
					allSplitViewControllerRemoved, stage, this);
			tabManager = new TabManager(tabPane, allSplitViewController, splitViewManager);
			keyboardShortcutManager = new KeyboardShortcutManager(tabManager, splitViewManager);
			externalToolsManager = new ExternalToolsManager();
			
			// Assign column to which property in model
			splitViewManager.addSplitView(Setting.getLeftLastKnowLocation(), true);
			splitViewManager.addSplitView(Setting.getRightLastKnowLocation(), false);
		} catch (Exception e1) {
			e1.printStackTrace();
			DialogHelper.showException(e1);
		}
	}

	/** To be called after setting a scene to the stage */
	public void initializePart3AddTabs(Stage stage, boolean doRefresh) {
		initializeSettingXmlRelated();
		if (doRefresh)
			splitViewManager.refreshAllSplitViews();
		// the rest don't need to refresh since addSplitView have its own refresh
		Setting.getFavoritesViews().addListener(change -> {
			allSplitViewController
					.forEach(sp -> sp.onFavoriteChanges(change, Setting.getFavoritesViews().isReloadingMapOperation()));
		});

		// Initialize tabs
		tabManager.initializeTabs(
			java.util.Arrays.asList(new SplitViewState(Setting.getLeftLastKnowLocation())),
			java.util.Arrays.asList(new SplitViewState(Setting.getRightLastKnowLocation()))
		);

		if (Setting.isRestoreLastOpenedFavorite()) {
			java.util.Map<String, java.util.List<SplitViewState>> favoritesMap = new java.util.HashMap<>();
			for (FavoriteView fav : Setting.getFavoritesViews()) {
				favoritesMap.put(fav.getTitle(), fav.getSplitStatessInitializedCopy());
			}
			tabManager.addTabsFromSettings(Setting.getLastOpenedFavoriteTitle(), favoritesMap);
		}
		
		keyboardShortcutManager.initializeStageKeyboardShortcuts(stage);

		// Restoring last known view as it was
		FavoriteView lastView = Setting.getLastOpenedView();
		if (lastView != null && !Main.isPathArgumentPassed()) {
			while (allSplitViewController.size() < lastView.getSplitStates().size()) {
				splitViewManager.addSplitView();
			}
			for (int i = 0; i < lastView.getSplitStates().size(); i++) {
				allSplitViewController.get(i)
						.restoreSplitViewState(new SplitViewState(lastView.getSplitStates().get(i)));
			}
		}
	}

	/**
	 * Method can be used to initialize UI setting related for xml, or for normal
	 * setting with delay in part 3 initialization
	 */
	private void initializeSettingXmlRelated() {
		// Initialize menu bar manager
		menuBarManager = new MenuBarManager(newEmbedWindow, newWindow, openConnectionMenu,
				reloadXMLSetting, TrackerMenu, themeSelection, showOperationStage, renameItem,
				cortanaMenu, aboutMenuItem, this, splitViewManager);
		menuBarManager.initializeMenuBar();
		PathLayer.setDateFormat(new SimpleDateFormat(Setting.getDateFormatPattern()));
	}

	/**
	 * To be called at initialization and after setting changes via UI.
	 */
	public void changeInSetting() {
		initializeSettingXmlRelated();
		splitViewManager.refreshAllSplitViews();
	}

	@FXML
	public void openSettingTrackerPlayer() {
		TrackerPlayer.openTrackerSettingGUI();
	}

	// Method called by MenuBarManager
	public void createNewWindow() {
		try {
			WelcomeController anotherWelcome = newWelcomeControllerStage(new Stage());
			for (int i = 0; i < allSplitViewController.size(); i++) {
				if (anotherWelcome.allSplitViewController.size() < i + 1) {
					anotherWelcome.splitViewManager.addSplitView();
				}
				anotherWelcome.allSplitViewController.get(i)
						.navigate(allSplitViewController.get(i).getmDirectoryPath());
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public void openFavoriteLocation(FavoriteView favoriteView, SplitViewController splitViewController) {
		String error = "";
		for (SplitViewState path : favoriteView.getSplitStates()) {
			if (!path.getMDirectory().exists()) {
				error += path + "\n\n";
			}
		}
		if (!error.isEmpty()) {
			DialogHelper.showExpandableAlert(AlertType.INFORMATION, "Open Favorites", "Resources cannot be found",
					"Directory gets moved/deleted, or system provider is offline", error);
		}
		List<SplitViewState> splitStates = favoriteView.getSplitStatessInitializedCopy();
		if (splitViewController == allSplitViewController.get(0)) {
			DraggableTab newTab = new DraggableTab(favoriteView.getTitle(), splitStates);
			tabManager.activeActionTab(newTab);
			tabManager.getTabPane().getTabs().add(newTab);
			tabManager.getTabPane().getSelectionModel().select(newTab);
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
		mn.setOnAction(event -> {
			FileTracker.updateUserFileName(user);
			refreshAllSplitViews();
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
								+ "\n - Other wise you can use menu 'Clean Recursivly' with this name:'" + mn.getText()
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

	@FXML
	public void RemoveFromContextMenu() {
		Setting.RemoveFromContextMenu();
	}

	public void ClearSearchField() {
		SplitViewController focusedPane = splitViewManager.getFocusedPane();
		if (focusedPane != null) {
			focusedPane.clearSearchField();
		} else {
			allSplitViewController.get(0).clearSearchField();
		}
	}

	// External tools delegations
	@FXML
	public void ConfigureVLCPath() {
		externalToolsManager.ConfigureVLCPath();
	}

	@FXML
	public void ControlVLC() {
		externalToolsManager.ControlVLC();
	}

	@FXML
	public void ControlVLCAndroid() {
		externalToolsManager.ControlVLCAndroid();
	}

	@FXML
	public void ControlVLCIOS() {
		externalToolsManager.ControlVLCIOS();
	}

	@FXML
	void GetBulkRenameUtility(ActionEvent event) {
		externalToolsManager.GetBulkRenameUtility(event);
	}

	@FXML
	void GetMp3Tag(ActionEvent event) {
		externalToolsManager.GetMp3Tag(event);
	}

	@FXML
	void GetVLC(ActionEvent event) {
		externalToolsManager.GetVLC(event);
	}

	@FXML
	void CheckForUpdate(ActionEvent event) {
		externalToolsManager.CheckForUpdate(event);
	}

	@FXML
	void Tutorial(ActionEvent event) {
		externalToolsManager.Tutorial(event);
	}

	@FXML
	void KeyBoardShortcut(ActionEvent event) {
		DialogHelper.showAlert(AlertType.INFORMATION, "KeyBoard Shortcuts", "KeyBoard Shortcuts",
				KeyboardShortcutManager.getShortcutsHelpText());
	}

	@FXML
	public void plusTab() {
		tabManager.plusTab();
	}

	public PathLayer getLeftLastKnowLocation() {
		return splitViewManager.getMostLeftView().getmDirectoryPath();
	}

	public SplitViewController getMostLeftView() {
		return splitViewManager.getMostLeftView();
	}

	public PathLayer getRightLastKnowLocation() {
		return splitViewManager.getMostRightView().getmDirectoryPath();
	}

	public SplitViewController getMostRightView() {
		return splitViewManager.getMostRightView();
	}

	public void RevealINExplorer() {
		SplitViewController focusedPane = splitViewManager.getFocusedPane();
		if (focusedPane != null) {
			focusedPane.RevealINExplorer();
		} else {
			getMostLeftView().RevealINExplorer();
		}
	}

	public void saveSetting() {
		Setting.setLeftLastKnowLocation(getLeftLastKnowLocation());
		Setting.setRightLastKnowLocation(getRightLastKnowLocation());
		Setting.setAutoExpand(getMostLeftView().isAutoExpand());
		if (Setting.isRestoreLastOpenedFavorite()) {
			Setting.setLastOpenedFavoriteTitle(new ArrayList<>(tabManager.getOpenedTabTitles()));
		}
		Setting.setMaximized(stage.isMaximized());

		Setting.setLastOpenedView(new FavoriteView("Default", allSplitViewController.stream().map(sp -> {
			SplitViewState state = new SplitViewState();
			sp.saveStateToSplitState(state);
			return state;
		}).collect(Collectors.toList())));
	}

	public void switchRecursive() {
		SplitViewController focusedPane = splitViewManager.getFocusedPane();
		if (focusedPane != null) {
			focusedPane.switchRecursive();
		} else {
			getMostLeftView().switchRecursive();
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

	public Stage getStage() {
		return stage;
	}

	public void refreshAllSplitViews() {
		splitViewManager.refreshAllSplitViews();
	}

	public void refreshAllSplitViewsIfMatch(PathLayer directoryView, SplitViewController exception) {
		splitViewManager.refreshAllSplitViewsIfMatch(directoryView, exception);
	}

	public boolean isDirOpenedInOtherView(PathLayer directoryView, SplitViewController exception) {
		return splitViewManager.isDirOpenedInOtherView(directoryView, exception);
	}

	public void refreshUnExistingViewsDir() {
		splitViewManager.refreshUnExistingViewsDir();
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
		anotherWelcome.initializePart2AddSplitView(stage);
		anotherWelcome.initializePart3AddTabs(stage, false);
		return anotherWelcome;
	}
}
