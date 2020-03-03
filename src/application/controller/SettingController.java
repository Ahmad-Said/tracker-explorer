package application.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

import application.DialogHelper;
import application.FileTracker;
import application.Main;
import application.datatype.Setting;
import application.fxGraphics.IntField;
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
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

public class SettingController {

	@FXML
	private ListView<String> favoritesListView;

	@FXML
	private ListView<String> userListView;

	@FXML
	private TextField inputFavoriteName;

	@FXML
	private TextField inputNewUser;

	@FXML
	private Label targetFavoriteName;

	@FXML
	private Label inputFavoriteNameError;

	@FXML
	private Label inputNewUserError;

	@FXML
	private Label currentActiveUser;

	@FXML
	private IntField limitFilesRercursive;

	@FXML
	private CheckBox autoRenameCheckBox;

	@FXML
	private CheckBox openRecentFavorites;

	@FXML
	private CheckBox autoBackSyncCheckBox;

	private Stage settingStage; // defined to close it later
	private ObservableList<String> userData = FXCollections.observableArrayList();
	private ObservableList<String> favoritesData = FXCollections.observableArrayList();
	private HashMap<String, String> favoritesCurrentToOldNames = new HashMap<>();
	private WelcomeController welcomeController;

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
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SettingView.fxml"));
			loader.setController(this);
			root = loader.load();
			scene = new Scene(root);

			scene.getStylesheets().add("/css/bootstrap3.css");

			settingStage.setTitle("Setting Controller");
			settingStage.setScene(scene);

			settingStage.getIcons().add(new Image(Main.class.getResourceAsStream("/img/setting-512.png")));

			initializeButtons();

			initializeListView();
			resetFormToDefault(null);

			settingStage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initializeButtons() {
		inputNewUserError.setText("");
		inputFavoriteNameError.setText("");
		inputNewUser.setOnKeyPressed(e -> {
			if (e.getCode().equals(KeyCode.ENTER)) {
				e.consume();
				addNewUser(null);
			}
		});
		inputFavoriteName.setOnKeyPressed(e -> {
			if (e.getCode().equals(KeyCode.ENTER)) {
				e.consume();
				renameFavorite();
			}
		});
	}

	private void initializeListView() {
		userListView.setItems(userData);
		userListView.setOnMouseClicked(e -> {
			if (e.getClickCount() >= 2) {
				String user = userListView.getSelectionModel().getSelectedItem();
				currentActiveUser.setText(user);
			}
		});

		favoritesListView.setItems(favoritesData);
		favoritesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		favoritesListView.setOnMouseClicked(e -> {
			targetFavoriteName.setText(favoritesListView.getSelectionModel().getSelectedItem());
			inputFavoriteName.setText(favoritesListView.getSelectionModel().getSelectedItem());
		});
	}

	@FXML
	private void resetFormToDefault(ActionEvent event) {
		autoBackSyncCheckBox.setSelected(Setting.isBackSync());
		autoRenameCheckBox.setSelected(Setting.isAutoRenameUTFFile());
		limitFilesRercursive.setValue(Setting.getMaxLimitFilesRecursive());
		openRecentFavorites.setSelected(Setting.isRestoreLastOpenedFavorite());

		userData.clear();
		userData.addAll(Setting.getUserNames());

		favoritesData.clear();
		favoritesData.addAll(Setting.getFavoritesLocations().getTitle());

		favoritesCurrentToOldNames.clear();
		favoritesData.forEach(e -> favoritesCurrentToOldNames.put(e, e));
		resetFavoriteRename();

		currentActiveUser.setText(Setting.getActiveUser());
	}

	void resetFavoriteRename() {
		targetFavoriteName.setText("oldName");
		inputFavoriteName.setText("");
	}

	@FXML
	void cancel(ActionEvent event) {
		settingStage.close();
	}

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
				favoritesCurrentToOldNames.remove(string);
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

		String oldValue = favoritesCurrentToOldNames.get(targetString);
		favoritesCurrentToOldNames.remove(targetString);
		favoritesCurrentToOldNames.put(inputString, oldValue);
	}

	public Stage getSettingStage() {
		return settingStage;
	}

	@FXML
	void saveSetting(ActionEvent event) {
		Setting.setAutoRenameUTFFile(autoRenameCheckBox.isSelected());
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
		HashMap<String, Pair<String, Integer>> oldToNewTitleAndIndex = new HashMap<String, Pair<String, Integer>>();
		for (String string : favoritesCurrentToOldNames.keySet()) {
			oldToNewTitleAndIndex.put(favoritesCurrentToOldNames.get(string),
					new Pair<String, Integer>(string, favoritesData.indexOf(string)));
		}
		Setting.getFavoritesLocations().updateTitlesAndIndexs(oldToNewTitleAndIndex);

		if (welcomeController != null) {
			welcomeController.changeInSetting();
		} else {
			DialogHelper.showAlert(AlertType.INFORMATION, "User Changed", hint,
					"A refresh May be required to take effect");
		}

		Setting.saveSetting();
		settingStage.close();
	}
}
