package said.ahmad.javafx.tracker.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import said.ahmad.javafx.tracker.app.DialogHelper;
import said.ahmad.javafx.tracker.app.look.THEME;
import said.ahmad.javafx.tracker.app.look.THEME_COLOR;
import said.ahmad.javafx.tracker.app.look.ThemeManager;
import said.ahmad.javafx.tracker.app.pref.Setting;
import said.ahmad.javafx.tracker.controller.connection.ConnectionController;
import said.ahmad.javafx.tracker.controller.connection.ConnectionController.ConnectionType;
import said.ahmad.javafx.tracker.controller.setting.base.SettingController;
import said.ahmad.javafx.tracker.fxGraphics.MenuItemFactory;
import said.ahmad.javafx.tracker.system.services.TrackerPlayer;
import said.ahmad.javafx.tracker.system.tracker.FileTracker;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles menu bar initialization and configuration
 */
public class MenuBarManager {
    private final Menu newEmbedWindow;
    private final MenuItem newWindow;
    private final Menu openConnectionMenu;
    private final MenuItem reloadXMLSetting;
    private final Menu TrackerMenu;
    private final Menu themeSelection;
    private final MenuItem showOperationStage;
    private final MenuItem renameItem;
    private final Menu cortanaMenu;
    private final MenuItem aboutMenuItem;

    private Menu subMenuActiveUser;
    private Menu subMenuRemoveUser;
    private ToggleGroup toogleActiveUserGroup;
    private ArrayList<RadioMenuItem> allActiveUser = new ArrayList<>();
    private ArrayList<MenuItem> allRemoveUser = new ArrayList<>();

    private final WelcomeController welcomeController;
    private final SplitViewManager splitViewManager;

    public MenuBarManager(Menu newEmbedWindow, MenuItem newWindow, Menu openConnectionMenu,
                         MenuItem reloadXMLSetting, Menu TrackerMenu, Menu themeSelection,
                         MenuItem showOperationStage, MenuItem renameItem, Menu cortanaMenu,
                         MenuItem aboutMenuItem, WelcomeController welcomeController,
                         SplitViewManager splitViewManager) {
        this.newEmbedWindow = newEmbedWindow;
        this.newWindow = newWindow;
        this.openConnectionMenu = openConnectionMenu;
        this.reloadXMLSetting = reloadXMLSetting;
        this.TrackerMenu = TrackerMenu;
        this.themeSelection = themeSelection;
        this.showOperationStage = showOperationStage;
        this.renameItem = renameItem;
        this.cortanaMenu = cortanaMenu;
        this.aboutMenuItem = aboutMenuItem;
        this.welcomeController = welcomeController;
        this.splitViewManager = splitViewManager;
    }

    public void initializeMenuBar() {
        initializeFileMenu();
        initializeConnectionMenu();
        initializeReloadXMLSetting();
        initializeThemeMenu();
        initializeTrackerMenu();
        initializeCortanaMenu();
        initializeAboutMenu();
    }

    private void initializeFileMenu() {
        newEmbedWindow.getItems().clear();

        MenuItem newSplitLeftTemplate = new MenuItem("Left Template");
        newSplitLeftTemplate.setOnAction(e -> {
            try {
                splitViewManager.addSplitView(
                    splitViewManager.getAllSplitViewController().peek().getmDirectoryPath(), true);
            } catch (IOException e3) {
                e3.printStackTrace();
            }
        });

        MenuItem newSplitRightTemplate = new MenuItem("Right Template");
        newSplitRightTemplate.setOnAction(e -> {
            try {
                splitViewManager.addSplitView(
                    splitViewManager.getAllSplitViewController().peek().getmDirectoryPath(), false);
            } catch (IOException e3) {
                e3.printStackTrace();
            }
        });

        newEmbedWindow.getItems().addAll(newSplitLeftTemplate, newSplitRightTemplate);

        newWindow.setOnAction(e -> welcomeController.createNewWindow());
    }

    private void initializeConnectionMenu() {
        openConnectionMenu.getItems().clear();
        for (ConnectionType connectionType : ConnectionType.values()) {
            MenuItem mn = new MenuItem(connectionType.toString());
            mn.setOnAction(e -> new ConnectionController(connectionType,
                    path -> splitViewManager.getMostLeftView().setmDirectoryThenRefresh(path)));
            openConnectionMenu.getItems().add(mn);
        }
    }

    private void initializeReloadXMLSetting() {
        reloadXMLSetting.setOnAction(e -> {
            Setting.loadSettingPartTwo();
            welcomeController.changeInSetting();
        });
    }

    private void initializeThemeMenu() {
        themeSelection.getItems().clear();

        MenuItem oldFashionedNoThemStyle = new MenuItem("old fashioned");
        MenuItem bootStrapThem = new MenuItem("Bootstrap V3");
        MenuItem micosoftWindowsLight = new MenuItem("Windows 10 Theme light");
        MenuItem micosoftWindowsDark = new MenuItem("Windows 10 Theme Dark");

        oldFashionedNoThemStyle.setOnAction(e -> {
            ThemeManager.changeThemeAndApply(welcomeController.getStage().getScene(), THEME.MODENAFX, THEME_COLOR.NONE);
            splitViewManager.refreshCSSSplitViews();
        });

        bootStrapThem.setOnAction(e -> {
            ThemeManager.changeThemeAndApply(welcomeController.getStage().getScene(), THEME.BOOTSTRAPV3, THEME_COLOR.NONE);
            splitViewManager.refreshCSSSplitViews();
        });

        micosoftWindowsLight.setOnAction(e -> {
            ThemeManager.changeThemeAndApply(welcomeController.getStage().getScene(), THEME.WINDOWS, THEME_COLOR.LIGHT);
            splitViewManager.refreshCSSSplitViews();
        });

        micosoftWindowsDark.setOnAction(e -> {
            ThemeManager.changeThemeAndApply(welcomeController.getStage().getScene(), THEME.WINDOWS, THEME_COLOR.DARK);
            splitViewManager.refreshCSSSplitViews();
        });

        themeSelection.getItems().addAll(oldFashionedNoThemStyle, bootStrapThem,
                                         micosoftWindowsLight, micosoftWindowsDark);

        showOperationStage.setOnAction(e -> said.ahmad.javafx.tracker.system.operation.FileHelperGUIOperation.showOperationStage());
        renameItem.setOnAction(e -> new RenameUtilityController(new ArrayList<>()));
    }

    private void initializeTrackerMenu() {
        TrackerMenu.getItems().clear();

        MenuItem settingController = new MenuItem("Setting preference");
        TrackerMenu.getItems().add(settingController);

        MenuItem clearFavorite = new MenuItem("Clear Favorites	(!-!)");
        TrackerMenu.getItems().add(clearFavorite);

        MenuItem addTocontextMenu = new MenuItem("Add Tracker To Context Menu");
        addTocontextMenu.setOnAction(e -> Setting.AddToContextMenu());
        TrackerMenu.getItems().add(addTocontextMenu);

        MenuItem removeTocontextMenu = new MenuItem("Remove Tracker From Context Menu");
        removeTocontextMenu.setOnAction(e -> Setting.RemoveFromContextMenu());
        TrackerMenu.getItems().add(removeTocontextMenu);

        MenuItem NewUser = new MenuItem("Add A new User");
        subMenuActiveUser = new Menu("Set Active User");
        toogleActiveUserGroup = new ToggleGroup();
        subMenuRemoveUser = new Menu("Remove User");
        TrackerMenu.getItems().addAll(NewUser, subMenuActiveUser, subMenuRemoveUser);

        MenuItem experimentalFeatures = new MenuItem("---------** Experimental Features **---------");
        TrackerMenu.getItems().add(experimentalFeatures);

        settingController.setOnAction(e -> {
            try {
                new SettingController(welcomeController);
            } catch (Exception e2) {
                e2.printStackTrace();
                DialogHelper.showException(e2);
            }
        });

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

        NewUser.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                handleNewUser(NewUser);
            }
        });

        for (String user : Setting.getUserNames()) {
            AddActiveUser(user);
        }

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
    }

    private void handleNewUser(MenuItem NewUser) {
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
        splitViewManager.refreshAllSplitViews();
    }

    private void AddActiveUser(String user) {
        RadioMenuItem mn = new RadioMenuItem(user);
        mn.setOnAction(event -> {
            FileTracker.updateUserFileName(user);
            splitViewManager.refreshAllSplitViews();
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
                if (mn.getText().equals(Setting.getActiveUser())) {
                    DialogHelper.showAlert(AlertType.ERROR, "Remove User", "Warning: Removal of an Active User",
                            "For a safety Check Please set active ANOTHER User, then remove this user from here");
                    return;
                }

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

    private void checkConflictLog() {
        boolean ans = DialogHelper.showExpandableConfirmationDialog("Changes Log", "Changes Log",
                "This Windows show the difference of files between the last saved tracker data and the current directory state.\nAbbreviations:\n - {$userName} <<>> {$directory}\n - Del = Moved or Deleted\n - New = new Added File. \n Press OK to clear log.",
                FileTracker.getConflictLog());
        if (ans) {
            FileTracker.setConflictLog("");
        }
    }

    private void initializeCortanaMenu() {
        MenuItemFactory.registerMenu(cortanaMenu);
        cortanaMenu.setOnShowing(e -> {
            cortanaMenu.getItems().remove(1, cortanaMenu.getItems().size());
            TrackerPlayer.getAllShortcutTracker().forEach((shortcut, realFile) -> {
                MenuItemFactory.getMenuItem(cortanaMenu,
                        said.ahmad.javafx.tracker.app.StringHelper.getBaseName(shortcut.getName()), true)
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
    }

    private void initializeAboutMenu() {
        aboutMenuItem.setOnAction(e -> DialogHelper.showAlert(Alert.AlertType.INFORMATION, "About", null,
                "Tracker Explorer v" + Setting.getVersion() + "\n\n" + "Copyright C 2024 by Ahmad Said"));
    }
}

