package said.ahmad.javafx.tracker.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import mslinks.ShellLinkException;
import said.ahmad.javafx.tracker.app.DialogHelper;
import said.ahmad.javafx.tracker.app.Main;
import said.ahmad.javafx.tracker.app.ResourcesHelper;
import said.ahmad.javafx.tracker.app.StringHelper;
import said.ahmad.javafx.tracker.app.ThemeManager;
import said.ahmad.javafx.tracker.app.pref.Setting;
import said.ahmad.javafx.tracker.datatype.FavoriteView;
import said.ahmad.javafx.tracker.datatype.FavoriteViewList;
import said.ahmad.javafx.tracker.fxGraphics.IntField;
import said.ahmad.javafx.tracker.system.call.TeraCopy;
import said.ahmad.javafx.tracker.system.operation.FileHelper;
import said.ahmad.javafx.tracker.system.services.TrackerPlayer;
import said.ahmad.javafx.tracker.system.tracker.FileTracker;

public class SettingController {

	// ----------------------------- General Setting -----------------------------

	@FXML
	private Tab tabGeneralSetting;

	@FXML
	private IntField limitFilesRercursive;

	@FXML
	private Label teraCopyPath;
	@FXML
	private CheckBox useTeraCopy;
	@FXML
	private CheckBox autoClearOperationFIle;

	@FXML
	private CheckBox autoRenameCheckBox;

	@FXML
	private CheckBox openRecentFavorites;

	@FXML
	private CheckBox autoBackSyncCheckBox;

	// ----------------------------- User Manager -----------------------------
	@FXML
	private Tab tabUserManager;

	@FXML
	private ListView<String> userListView;

	@FXML
	private TextField inputNewUser;

	@FXML
	private Label inputNewUserError;

	@FXML
	private Label currentActiveUser;

	private ObservableList<String> userData = FXCollections.observableArrayList();

	// ----------------------------- Favorite Manager -----------------------------
	@FXML
	private Tab tabFavoriteManger;

	@FXML
	private ListView<String> favoritesListView;

	@FXML
	private TextField inputFavoriteName;

	@FXML
	private Label targetFavoriteName;

	@FXML
	private Label inputFavoriteNameError;

	private ObservableList<String> favoritesData = FXCollections.observableArrayList();
	private HashMap<String, FavoriteView> favoritesViewByNewTitle = new HashMap<>();

	// ----------------------------- Tracker Player -----------------------------
	@FXML
	private Tab tabTrackerPlayer;

	@FXML
	private ListView<String> playlistListView;

	@FXML
	private TextField inputPlaylistName;

	@FXML
	private Label targetPlaylistName;

	@FXML
	private Label targetPlaylistLocation;

	@FXML
	private Label inputPlaylistNameError;
	@FXML
	private Label targetPlaylistLocationError;

	private ObservableList<String> playlistData = FXCollections.observableArrayList();
	// From playlist name to it's real playlist(m3u8..) location
	private HashMap<String, Path> playlistShorcutLocation = new HashMap<>();

	// ----------------------------- Common UI -----------------------------
	@FXML
	private TabPane tabPaneSetting;

	private Stage settingStage; // defined to close it later
	private WelcomeController welcomeController;
	public static final Image SETTING_ICON_IMAGE = new Image(
			ResourcesHelper.getResourceAsStream("/img/setting-512.png"));

	/**
	 *
	 * @param mWelcomeController can be null, is indeed to auto refresh views in
	 *                           case of user changes
	 */
	public SettingController(WelcomeController mWelcomeController) {

		welcomeController = mWelcomeController;
		settingStage = new Stage();
		settingStage.sizeToScene();
		settingStage.initOwner(Main.getPrimaryStage());
		settingStage.initModality(Modality.WINDOW_MODAL);
		Parent root;
		Scene scene;
		try {
			FXMLLoader loader = new FXMLLoader(ResourcesHelper.getResourceAsURL("/fxml/SettingView.fxml"));
			loader.setController(this);
			root = loader.load();
			scene = new Scene(root);
			ThemeManager.applyTheme(scene);

			settingStage.setTitle("Setting Tracker Explorer");
			settingStage.setScene(scene);

			settingStage.getIcons().add(SETTING_ICON_IMAGE);

			initializeButtons();

			initializeListView();
			resetFormToDefault(null);

			settingStage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initializeButtons() {
		// -----GGGGGGGGGGGGGGGGGGG----- General Setting -----GGGGGGGGGGGGGGGGGGG-----

		// -----UUUUUUUUUUUUUUUUUUU----- User Manager -----UUUUUUUUUUUUUUUUUUU-----
		inputNewUserError.setText("");
		inputNewUser.setOnKeyPressed(e -> {
			if (e.getCode().equals(KeyCode.ENTER)) {
				e.consume();
				addNewUser(null);
			}
		});

		// -----FFFFFFFFFFFFFFFFFFF----- Favorite Manager -----FFFFFFFFFFFFFFFFFFF-----
		inputFavoriteNameError.setText("");
		inputFavoriteName.setOnKeyPressed(e -> {
			if (e.getCode().equals(KeyCode.ENTER)) {
				e.consume();
				renameFavorite();
			}
		});

		// -----PPPPPPPPPPPPPPPPPPP----- Tracker Player -----PPPPPPPPPPPPPPPPPPP-----
		inputPlaylistNameError.setText("");
		targetPlaylistLocationError.setText("");

		inputPlaylistName.setOnKeyPressed(e -> {
			if (e.getCode().equals(KeyCode.ENTER)) {
				e.consume();
				renamePlaylist();
			}
		});
	}

	private void initializeListView() {
		// -----GGGGGGGGGGGGGGGGGGG----- General Setting -----GGGGGGGGGGGGGGGGGGG-----

		// -----UUUUUUUUUUUUUUUUUUU----- User Manager -----UUUUUUUUUUUUUUUUUUU-----
		userListView.setItems(userData);
		userListView.setOnMouseClicked(e -> {
			if (e.getClickCount() >= 2) {
				String user = userListView.getSelectionModel().getSelectedItem();
				currentActiveUser.setText(user);
			}
		});

		// -----FFFFFFFFFFFFFFFFFFF----- Favorite Manager -----FFFFFFFFFFFFFFFFFFF-----
		favoritesListView.setItems(favoritesData);
		favoritesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		favoritesListView.setOnMouseClicked(e -> {
			targetFavoriteName.setText(favoritesListView.getSelectionModel().getSelectedItem());
			inputFavoriteName.setText(favoritesListView.getSelectionModel().getSelectedItem());
		});

		// -----PPPPPPPPPPPPPPPPPPP----- Tracker Player -----PPPPPPPPPPPPPPPPPPP-----
		playlistListView.setItems(playlistData);
		playlistListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		playlistListView.setOnMouseClicked(e -> updateTrackerPlayerTarget());
		targetPlaylistLocation.setTooltip(new Tooltip());
		targetPlaylistLocation.textProperty().addListener((observable, oldValue, newValue) -> {
			targetPlaylistLocation.getTooltip().setText(newValue);
		});
	}

	@FXML
	private void resetFormToDefault(ActionEvent event) {

		// -----GGGGGGGGGGGGGGGGGGG----- General Setting -----GGGGGGGGGGGGGGGGGGG-----
		autoBackSyncCheckBox.setSelected(Setting.isBackSync());
		autoRenameCheckBox.setSelected(Setting.isAutoRenameUTFFile());
		autoClearOperationFIle.setSelected(Setting.isAutoCloseClearDoneFileOperation());
		useTeraCopy.setSelected(Setting.isUseTeraCopyByDefault());
		limitFilesRercursive.setValue(Setting.getMaxLimitFilesRecursive());
		openRecentFavorites.setSelected(Setting.isRestoreLastOpenedFavorite());
		if (TeraCopy.getPath_Setup() != null) {
			teraCopyPath.setText(TeraCopy.getPath_Setup().toString());
		}

		// -----UUUUUUUUUUUUUUUUUUU----- User Manager -----UUUUUUUUUUUUUUUUUUU-----
		userData.clear();
		userData.addAll(Setting.getUserNames());
		currentActiveUser.setText(Setting.getActiveUser());

		// -----FFFFFFFFFFFFFFFFFFF----- Favorite Manager -----FFFFFFFFFFFFFFFFFFF-----
		favoritesData.clear();
		favoritesViewByNewTitle.clear();
		for (FavoriteView favorite : Setting.getFavoritesLocations()) {
			favoritesData.add(favorite.getTitle());
			favoritesViewByNewTitle.put(favorite.getTitle(), favorite);
		}
		resetFavoriteRename();

		// -----PPPPPPPPPPPPPPPPPPP----- Tracker Player -----PPPPPPPPPPPPPPPPPPP-----
		playlistData.clear();
		TrackerPlayer.getAllShortcutTracker().forEach((shortcut, realFile) -> {
			playlistShorcutLocation.put(StringHelper.getBaseName(shortcut.getName()), realFile.toPath());
		});

		playlistData.addAll(playlistShorcutLocation.keySet());
		for (String key : playlistShorcutLocation.keySet()) {
			if (!playlistShorcutLocation.get(key).toFile().exists()) {
				playlistListView.getSelectionModel().select(key);
				updateTrackerPlayerTarget();
				break;
			}
		}
	}

	// ----------------------------- General Setting -----------------------------
	@FXML
	public void openTeraCopyLink() {
		TeraCopy.openTeraCopyURL();
	}

	@FXML
	public void ConfigureTeraCopyPath() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Navigate to where TeraCopy is installed");
		File initfile = FileHelper.getParentExeFile(TeraCopy.getPath_Setup(), null);
		fileChooser.setInitialDirectory(initfile);
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Path To ", "TeraCopy.exe"));

		File teraCopyfile = fileChooser.showOpenDialog(settingStage);
		if (teraCopyfile == null) {
			return;
		}
		if (teraCopyfile.getName().equals("TeraCopy.exe")) {
			TeraCopy.setPath_Setup(teraCopyfile.toPath());
			DialogHelper.showAlert(AlertType.INFORMATION, "Configure TeraCopy Path", "TeraCopy well configured",
					"Path: " + TeraCopy.getPath_Setup());
		} else {
			DialogHelper.showAlert(AlertType.ERROR, "Configure TeraCopy Path", "TeraCopy misconfigured",
					"Please chose the right file 'TeraCopy.exe'\n\nCurrent Path:\n " + TeraCopy.getPath_Setup());
		}
	}

	// ----------------------------- User Manager -----------------------------
	@FXML
	void removeSelectedUser(ActionEvent event) {
		if (userListView.getSelectionModel().getSelectedIndex() == 0) {
			DialogHelper.showAlert(AlertType.INFORMATION, "Remove User", "Default User cannot be removed!", "");
			return;
		}
		boolean ans = DialogHelper.showConfirmationDialog("Remove User", "Are you sure you want To Remove This User?",
				"User: " + userListView.getSelectionModel().getSelectedItem());
		if (ans) {
			String user = userListView.getSelectionModel().getSelectedItem();

			userData.remove(user);
			if (user.equals(currentActiveUser.getText())) {
				currentActiveUser.setText(userData.get(0));
			}
		}
	}

	@FXML
	void addNewUser(ActionEvent event) {
		// code copied from welcome controller
		String user = inputNewUser.getText();
		String noChar = "/\\:*\"<>|";
		Set<Character> charsToTestFor = noChar.chars().mapToObj(ch -> Character.valueOf((char) ch))
				.collect(Collectors.toSet());
		boolean anyCharInString = user.chars().anyMatch(ch -> charsToTestFor.contains(Character.valueOf((char) ch)));
		if (user.length() > 0 && user.length() < 10 && !userData.contains(user) && !anyCharInString) {
			userData.add(user);
			inputNewUserError.setText("");
			inputNewUser.setText("");
		} else {
			inputNewUserError.setText("Name does not respect rules!");
		}
	}

	// ----------------------------- Favorite Manager -----------------------------
	void resetFavoriteRename() {
		targetFavoriteName.setText("oldName");
		inputFavoriteName.setText("");
	}

	@FXML
	public void upSelectedFavorites() {
		int selectSize = favoritesListView.getSelectionModel().getSelectedIndices().size();
		int whichOne = 0;
		int[] toSelect = new int[selectSize];
		int[] selected = favoritesListView.getSelectionModel().getSelectedIndices().stream().mapToInt(m -> m).toArray();
		for (Integer i : selected) {
			int j = i - 1;
			String title = favoritesData.get(i);
			if (j >= whichOne) {
				favoritesData.remove(title);
				favoritesData.add(j, title);
				toSelect[whichOne] = j;
			} else {
				toSelect[whichOne] = i;
			}
			whichOne++;
		}
		favoritesListView.getSelectionModel().clearSelection();
		favoritesListView.getSelectionModel().selectIndices(-1, toSelect);
	}

	@FXML
	public void downSelectedFavorites() {
		int size = favoritesData.size();
		int selectSize = favoritesListView.getSelectionModel().getSelectedIndices().size();
		int whichOne = 0;
		int[] toSelect = new int[selectSize];
		int[] selected = favoritesListView.getSelectionModel().getSelectedIndices().stream().mapToInt(m -> m).toArray();
		for (Integer index = selectSize - 1; index >= 0; index--) {
			int i = selected[index];
			int j = i + 1;
			String title = favoritesData.get(i);
			if (j <= size - 1 - whichOne) {
				favoritesData.remove(title);
				favoritesData.add(j, title);
				toSelect[whichOne] = j;
			} else {
				toSelect[whichOne] = i;
			}
			whichOne++;
		}
		favoritesListView.getSelectionModel().clearSelection();
		favoritesListView.getSelectionModel().selectIndices(-1, toSelect);

	}

	@FXML
	public void removeSelectedFavorites() {
		if (favoritesListView.getSelectionModel().getSelectedItems().size() == 0) {
			return;
		}
		String AIO = favoritesListView.getSelectionModel().getSelectedItems().toString();
		boolean ans = DialogHelper.showConfirmationDialog("Remove Favorites",
				"Are you sure you want to remove these favorites?", AIO);
		if (ans) {
			for (String string : favoritesListView.getSelectionModel().getSelectedItems()) {
				favoritesData.remove(string);
				favoritesViewByNewTitle.remove(string);
			}
			resetFavoriteRename();
		}
	}

	@FXML
	public void renameFavorite() {
		String inputString = inputFavoriteName.getText();
		String targetString = targetFavoriteName.getText();

		if (targetString.equals("oldName")) {
			inputFavoriteNameError.setText("Select target from table first!");
			return;
		} else if (favoritesData.contains(inputString)) {
			inputFavoriteNameError.setText("Name already exist!");
			favoritesListView.getSelectionModel().clearAndSelect(favoritesData.indexOf(inputString));
			return;
		} else if (inputString.isEmpty()) {
			inputFavoriteNameError.setText("Name Could Not be empty!");
			return;
		}

		inputFavoriteNameError.setText("");

		inputString = inputString.replaceAll(";", "_");
		int index = favoritesData.indexOf(targetString);
		favoritesData.remove(targetString);
		favoritesData.add(index, inputString);
		targetFavoriteName.setText(inputString);
		favoritesListView.getSelectionModel().clearAndSelect(index);
		inputFavoriteName.setText("");

		FavoriteView oldValue = favoritesViewByNewTitle.get(targetString);
		favoritesViewByNewTitle.remove(targetString);
		oldValue.setTitle(inputString);
		favoritesViewByNewTitle.put(inputString, oldValue);
	}

	// ----------------------------- Tracker Player -----------------------------

	public void switchToTrackerPlayerTab() {
		tabPaneSetting.getSelectionModel().select(tabTrackerPlayer);
	}

	private void updateTrackerPlayerTarget() {
		String target = playlistListView.getSelectionModel().getSelectedItem();
		if (target == null) {
			return;
		}
		targetPlaylistName.setText(target);
		targetPlaylistLocation.setText(playlistShorcutLocation.get(target).toString());
		if (!playlistShorcutLocation.get(target).toFile().exists()) {
			targetPlaylistLocationError.setText("Playlist Not Found!->");
		} else {
			targetPlaylistLocationError.setText("");
		}
		inputPlaylistName.setText(target);
	}

	@FXML
	private void addNewPlaylist() {
		String name = TrackerPlayer.getPlaylistName();
		if (name == null) {
			return;
		}
		File location = TrackerPlayer.getPlaylistLocation(null, getSettingStage());
		if (location == null) {
			return;
		}
		TrackerPlayer.createNewShortcutPlaylist(name, location.toPath());
		playlistData.add(name);
		playlistShorcutLocation.put(name, location.toPath());
	}

	@FXML
	private void removeSelectedPlaylist() {
		if (playlistListView.getSelectionModel().getSelectedItems().size() == 0) {
			targetSelectFirstErrorDialog();
			return;
		}
		List<String> AIO = new ArrayList<>(playlistListView.getSelectionModel().getSelectedItems());
		boolean ans = DialogHelper.showConfirmationDialog("Remove Tracker Player Shortcut",
				"Are you sure you want to remove these Shortcut?", AIO.toString());
		if (ans) {
			for (String string : AIO) {
				TrackerPlayer.deleteShortcutAndPlaylist(string);
				playlistData.remove(string);
				playlistShorcutLocation.remove(string);
			}
		}
		resetPlaylistTargetField();
	}

	private void resetPlaylistTargetField() {
		inputPlaylistName.setText("");
		targetPlaylistLocation.setText("");
		targetPlaylistName.setText("");
	}

	@FXML
	private void renamePlaylist() {
		String newName = inputPlaylistName.getText();
		String oldName = targetPlaylistName.getText();

		if (newName.equals(oldName)) {
			return;
		} else if (oldName.equals("oldName")) {
			inputFavoriteNameError.setText("Select target from table first!");
			return;
		} else if (playlistData.contains(newName)) {
			inputPlaylistNameError.setText("Name already exist!");
			playlistListView.getSelectionModel().clearAndSelect(favoritesData.indexOf(newName));
			return;
		} else if (newName.isEmpty()) {
			inputPlaylistNameError.setText("Name Could Not be empty!");
			return;
		}
		inputPlaylistNameError.setText("");

		try {
			newName = StringHelper.getValidName(newName, " ");
			Pair<File, File> shortcutToPlaylist = TrackerPlayer.renameShorcutAndPlaylist(oldName, newName);

			int index = playlistData.indexOf(oldName);
			playlistData.remove(oldName);
			playlistData.add(index, newName);
			playlistListView.getSelectionModel().clearAndSelect(index);

			playlistShorcutLocation.remove(oldName);
			playlistShorcutLocation.put(newName, shortcutToPlaylist.getValue().toPath());

			updateTrackerPlayerTarget();
		} catch (IOException | ShellLinkException e) {
			e.printStackTrace();
			DialogHelper.showException(e);
		}
	}

	@FXML
	private void changeLocPlaylist() {
		String targetKey = targetPlaylistName.getText();
		if (!playlistData.contains(targetKey)) {
			targetSelectFirstErrorDialog();
			return;
		}
		File playlistFile = TrackerPlayer
				.getPlaylistLocation(new File(targetPlaylistLocation.getText()).getParentFile(), settingStage);
		if (playlistFile == null) {
			return;
		}
		TrackerPlayer.createNewShortcutPlaylist(targetKey, playlistFile.toPath());
		playlistShorcutLocation.put(targetKey, playlistFile.toPath());
		playlistListView.getSelectionModel().select(targetKey);
		updateTrackerPlayerTarget();
	}

	@FXML
	private void browsePlaylistLocation() {
		File test = new File(targetPlaylistLocation.getText());
		if (test.exists()) {
			StringHelper
					.RunRuntimeProcess(new String[] { "explorer.exe", "/select,", targetPlaylistLocation.getText() });
		}
	}

	@FXML
	private void openSelectedPlaylist() {
		File playlist = new File(targetPlaylistLocation.getText());
		if (!playlistData.contains(targetPlaylistName.getText())) {
			targetSelectFirstErrorDialog();
			return;
		}
		try {
			TrackerPlayer.openPlaylistInLnk(playlist);
		} catch (IOException | ParseException e) {
			e.printStackTrace();
			DialogHelper.showException(e);
		}
	}

	private void targetSelectFirstErrorDialog() {
		DialogHelper.showAlert(AlertType.ERROR, "Change Playlist location", "Select a target first from table list",
				"");
	}

	// ----------------------------- Common UI -----------------------------
	@FXML
	void cancel(ActionEvent event) {
		settingStage.close();
	}

	public Stage getSettingStage() {
		return settingStage;
	}

	@FXML
	void saveSetting(ActionEvent event) {
		Setting.setAutoCloseClearDoneFileOperation(autoClearOperationFIle.isSelected());
		Setting.setAutoRenameUTFFile(autoRenameCheckBox.isSelected());
		Setting.setUseTeraCopyByDefault(useTeraCopy.isSelected());
		Setting.setBackSync(autoBackSyncCheckBox.isSelected());
		Setting.setMaxLimitFilesRecursive(limitFilesRercursive.getValue());
		Setting.setRestoreLastOpenedFavorite(openRecentFavorites.isSelected());

		// Users stuff
		boolean isUsersChanged = !Setting.getUserNames()
				.equals(userData.stream().map(e -> e).collect(Collectors.toList()));
		String hint = "";
		if (isUsersChanged) {
			Setting.getUserNames().clear();
			Setting.getUserNames().addAll(userData);
			hint += "User list changed!";

		}
		isUsersChanged = isUsersChanged || !currentActiveUser.getText().equals(Setting.getActiveUser());
		if (!currentActiveUser.getText().equals(Setting.getActiveUser())) {
			Setting.setActiveUser(currentActiveUser.getText());
			FileTracker.updateUserFileName(currentActiveUser.getText());
			hint += "\nActive User is changed!";
		}

		// Favorites stuff
		FavoriteViewList toBeUpdatedFavoritesList = Setting.getFavoritesLocations();
		// list of all favorites so we can add them all at once
		// so listeners got less notified
		List<FavoriteView> toBeAdded = new ArrayList<>();
		for (String title : favoritesData) {
			toBeAdded.add(favoritesViewByNewTitle.get(title));
		}
		Collections.reverse(toBeAdded);
		toBeUpdatedFavoritesList.addAll(toBeAdded);

		if (welcomeController != null) {
			welcomeController.changeInSetting();
		} else {
			if (isUsersChanged) {
				DialogHelper.showAlert(AlertType.INFORMATION, "User Changed", hint,
						"A refresh May be required to take effect");
			}
		}

		Setting.saveSetting();
		settingStage.close();
	}
}
