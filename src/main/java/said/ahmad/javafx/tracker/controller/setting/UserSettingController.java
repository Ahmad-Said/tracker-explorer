package said.ahmad.javafx.tracker.controller.setting;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import said.ahmad.javafx.tracker.app.DialogHelper;
import said.ahmad.javafx.tracker.app.ResourcesHelper;
import said.ahmad.javafx.tracker.app.look.IconLoader;
import said.ahmad.javafx.tracker.app.look.IconLoader.ICON_TYPE;
import said.ahmad.javafx.tracker.app.pref.Setting;
import said.ahmad.javafx.tracker.controller.setting.base.GenericSettingController;
import said.ahmad.javafx.tracker.system.tracker.FileTracker;

public class UserSettingController extends GenericSettingController {
	@FXML
	private ScrollPane pane;

	@FXML
	private ListView<String> userListView;

	@FXML
	private Button removeButton;

	@FXML
	private TextField inputNewUser;
	@FXML
	private Button addUserButton;

	@FXML
	private Label inputNewUserError;

	@FXML
	private Label currentActiveUser;

	private ObservableList<String> userData = FXCollections.observableArrayList();

	@Override
	public String getTitle() {
		return "User Manager";
	}

	@Override
	public @Nullable Image getIconImage() {
		return IconLoader.getIconImage(ICON_TYPE.USER);
	}

	@Override
	public FXMLLoader loadFXML() throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(ResourcesHelper.getResourceAsURL("/fxml/setting/UsersSetting.fxml"));
		loader.setController(this);
		loader.load();
		return loader;
	}

	@Override
	public void initializeNodes() {
		inputNewUserError.setText("");
		inputNewUser.setOnKeyPressed(e -> {
			if (e.getCode().equals(KeyCode.ENTER)) {
				e.consume();
				addNewUser(null);
			}
		});
		removeButton.setGraphic(IconLoader.getIconImageView(ICON_TYPE.DELETE));
		addUserButton.setGraphic(IconLoader.getIconImageView(ICON_TYPE.ADD, true, 20, 20));
	}

	@Override
	public void initializeDataViewHolders() {
		userListView.setItems(userData);
		userListView.setOnMouseClicked(e -> {
			if (e.getClickCount() >= 2) {
				String user = userListView.getSelectionModel().getSelectedItem();
				currentActiveUser.setText(user);
			}
		});
	}

	@Override
	public Parent getViewPane() {
		return pane;
	}

	@Override
	public boolean searchKeyWord(String keyword) {
		List<String> searchList = Arrays.asList("user", "active user", "account");
		return searchList.stream().anyMatch(s -> s.toUpperCase().contains(keyword.toUpperCase()));
	}

	@Override
	public void clearSearch() {

	}

	@Override
	public void pullDataFromSetting() {
		userData.clear();
		userData.addAll(Setting.getUserNames());
		currentActiveUser.setText(Setting.getActiveUser());

	}

	@Override
	public boolean pushDataToSetting() {
		boolean isUsersChanged = !Setting.getUserNames()
				.equals(userData.stream().map(e -> e).collect(Collectors.toList()));
		if (isUsersChanged) {
			Setting.getUserNames().clear();
			Setting.getUserNames().addAll(userData);
		}
		isUsersChanged = isUsersChanged || !currentActiveUser.getText().equals(Setting.getActiveUser());
		if (!currentActiveUser.getText().equals(Setting.getActiveUser())) {
			Setting.setActiveUser(currentActiveUser.getText());
			FileTracker.updateUserFileName(currentActiveUser.getText());
		}
		return isUsersChanged;
	}

	@FXML
	private void removeSelectedUser(ActionEvent event) {
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
	private void addNewUser(ActionEvent event) {
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

}
