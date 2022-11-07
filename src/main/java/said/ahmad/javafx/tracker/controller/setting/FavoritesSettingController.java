package said.ahmad.javafx.tracker.controller.setting;

import java.io.IOException;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import said.ahmad.javafx.tracker.app.DialogHelper;
import said.ahmad.javafx.tracker.app.ResourcesHelper;
import said.ahmad.javafx.tracker.app.look.IconLoader;
import said.ahmad.javafx.tracker.app.look.IconLoader.ICON_TYPE;
import said.ahmad.javafx.tracker.app.pref.Setting;
import said.ahmad.javafx.tracker.controller.setting.base.GenericSettingController;
import said.ahmad.javafx.tracker.datatype.FavoriteView;
import said.ahmad.javafx.tracker.datatype.FavoriteViewList;
import said.ahmad.javafx.util.ControlListHelper;

public class FavoritesSettingController extends GenericSettingController {

	@FXML
	private ScrollPane pane;

	@FXML
	private ListView<String> favoritesListView;

	@FXML
	private Button upButton;
	@FXML
	private Button downButton;
	@FXML
	private Button removeButton;

	@FXML
	private TextField inputFavoriteName;

	@FXML
	private Label targetFavoriteName;

	@FXML
	private Label inputFavoriteNameError;

	private ObservableList<String> favoritesData = FXCollections.observableArrayList();
	private HashMap<String, FavoriteView> favoritesViewByNewTitle = new HashMap<>();

	@Override
	public String getTitle() {
		return "Favorites";
	}

	@Override
	public @Nullable Image getIconImage() {
		return IconLoader.getIconImage(ICON_TYPE.STAR);
	}

	@Override
	public FXMLLoader loadFXML() throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(ResourcesHelper.getResourceAsURL("/fxml/setting/FavoritesSetting.fxml"));
		loader.setController(this);
		loader.load();
		return loader;
	}

	@Override
	public void initializeNodes() {
		inputFavoriteNameError.setText("");
		inputFavoriteName.setOnKeyPressed(e -> {
			if (e.getCode().equals(KeyCode.ENTER)) {
				e.consume();
				renameFavorite();
			}
		});
		upButton.setGraphic(IconLoader.getIconImageView(ICON_TYPE.UP));
		downButton.setGraphic(IconLoader.getIconImageView(ICON_TYPE.DOWN));
		removeButton.setGraphic(IconLoader.getIconImageView(ICON_TYPE.DELETE));
	}

	@Override
	public void initializeDataViewHolders() {
		favoritesListView.setItems(favoritesData);
		favoritesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		favoritesListView.setOnMouseClicked(e -> {
			targetFavoriteName.setText(favoritesListView.getSelectionModel().getSelectedItem());
			inputFavoriteName.setText(favoritesListView.getSelectionModel().getSelectedItem());
		});
	}

	@Override
	public Parent getViewPane() {
		return pane;
	}

	@Override
	public boolean searchKeyWord(String keyword) {
		List<String> searchList = Arrays.asList("favorites", "locations");
		return searchList.stream().anyMatch(s -> s.toUpperCase().contains(keyword.toUpperCase()));
	}

	@Override
	public void clearSearch() {

	}

	@Override
	public void pullDataFromSetting() {
		favoritesData.clear();
		favoritesViewByNewTitle.clear();
		for (FavoriteView favorite : Setting.getFavoritesViews()) {
			favoritesData.add(favorite.getTitle());
			favoritesViewByNewTitle.put(favorite.getTitle(), favorite);
		}
		resetFavoriteRename();
	}

	@Override
	public boolean isValidNewSetting(boolean showDialogAlert) {
		return true;
	}

	@Override
	public boolean pushDataToSetting() {
		FavoriteViewList toBeUpdatedFavoritesList = Setting.getFavoritesViews();
		// list of all favorites so we can add them all at once
		// so listeners got less notified
		List<FavoriteView> toBeAdded = new ArrayList<>();
		for (String title : favoritesData) {
			toBeAdded.add(favoritesViewByNewTitle.get(title));
		}
		toBeUpdatedFavoritesList.clearThenAddAll(toBeAdded);
		return false;
	}

	void resetFavoriteRename() {
		targetFavoriteName.setText("oldName");
		inputFavoriteName.setText("");
	}

	@FXML
	public void upSelectedFavorites() {
		int scrollTo = ControlListHelper.moveUpSelection(favoritesListView.getSelectionModel(), favoritesData);
		favoritesListView.scrollTo(scrollTo);
	}

	@FXML
	public void downSelectedFavorites() {
		int scrollTo = ControlListHelper.moveDownSelection(favoritesListView.getSelectionModel(), favoritesData);
		favoritesListView.scrollTo(scrollTo);
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

}
