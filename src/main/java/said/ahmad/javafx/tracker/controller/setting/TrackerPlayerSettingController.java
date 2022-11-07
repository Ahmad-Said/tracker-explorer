package said.ahmad.javafx.tracker.controller.setting;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.util.Pair;
import mslinks.ShellLinkException;
import said.ahmad.javafx.tracker.app.DialogHelper;
import said.ahmad.javafx.tracker.app.ResourcesHelper;
import said.ahmad.javafx.tracker.app.StringHelper;
import said.ahmad.javafx.tracker.app.look.IconLoader;
import said.ahmad.javafx.tracker.app.look.IconLoader.ICON_TYPE;
import said.ahmad.javafx.tracker.controller.setting.base.GenericSettingController;
import said.ahmad.javafx.tracker.system.services.TrackerPlayer;

public class TrackerPlayerSettingController extends GenericSettingController {
	@FXML
	private ScrollPane pane;

	@FXML
	private ListView<String> playlistListView;

	@FXML
	private Button addButton;
	@FXML
	private Button removeButton;
	@FXML
	private Button openButton;

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

	@Override
	public String getTitle() {
		return "Tracker Player";
	}

	@Override
	public @Nullable Image getIconImage() {
		return IconLoader.getIconImage(ICON_TYPE.CORTANA);
	}

	@Override
	public FXMLLoader loadFXML() throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(ResourcesHelper.getResourceAsURL("/fxml/setting/TrackerPlayerSetting.fxml"));
		loader.setController(this);
		loader.load();
		return loader;
	}

	@Override
	public void initializeNodes() {
		inputPlaylistNameError.setText("");
		targetPlaylistLocationError.setText("");

		inputPlaylistName.setOnKeyPressed(e -> {
			if (e.getCode().equals(KeyCode.ENTER)) {
				e.consume();
				renamePlaylist();
			}
		});

		addButton.setGraphic(IconLoader.getIconImageView(ICON_TYPE.ADD));
		removeButton.setGraphic(IconLoader.getIconImageView(ICON_TYPE.DELETE));
		openButton.setGraphic(IconLoader.getIconImageView(ICON_TYPE.OPEN));
	}

	@Override
	public void initializeDataViewHolders() {
		playlistListView.setItems(playlistData);
		playlistListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		playlistListView.setOnMouseClicked(e -> updateTrackerPlayerTarget());
		targetPlaylistLocation.setTooltip(new Tooltip());
		targetPlaylistLocation.textProperty().addListener((observable, oldValue, newValue) -> {
			targetPlaylistLocation.getTooltip().setText(newValue);
		});
	}

	@Override
	public Parent getViewPane() {
		return pane;
	}

	@Override
	public boolean searchKeyWord(String keyword) {
		List<String> searchList = Arrays.asList("cortana", "tracker player", "playlist", "shortcut");
		return searchList.stream().anyMatch(s -> s.toUpperCase().contains(keyword.toUpperCase()));
	}

	@Override
	public void clearSearch() {

	}

	@Override
	public void pullDataFromSetting() {
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

	@Override
	public boolean isValidNewSetting(boolean showDialogAlert) {
		return true;
	}

	@Override
	public boolean pushDataToSetting() {
		return false;
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
		File location = TrackerPlayer.getPlaylistLocation(null, getViewPane().getScene().getWindow());
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
			inputPlaylistNameError.setText("Select target from table first!");
			return;
		} else if (playlistData.contains(newName)) {
			inputPlaylistNameError.setText("Name already exist!");
			playlistListView.getSelectionModel().clearAndSelect(playlistData.indexOf(newName));
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
		File playlistFile = TrackerPlayer.getPlaylistLocation(
				new File(targetPlaylistLocation.getText()).getParentFile(), getViewPane().getScene().getWindow());
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

}
