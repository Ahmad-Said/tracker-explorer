package said.ahmad.javafx.tracker.controller.setting.base;

import java.io.IOException;
import java.util.LinkedHashMap;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import said.ahmad.javafx.tracker.app.DialogHelper;
import said.ahmad.javafx.tracker.app.Main;
import said.ahmad.javafx.tracker.app.ResourcesHelper;
import said.ahmad.javafx.tracker.app.look.IconLoader;
import said.ahmad.javafx.tracker.app.look.IconLoader.ICON_TYPE;
import said.ahmad.javafx.tracker.app.look.ThemeManager;
import said.ahmad.javafx.tracker.app.pref.Setting;
import said.ahmad.javafx.tracker.controller.WelcomeController;
import said.ahmad.javafx.tracker.controller.setting.FavoritesSettingController;
import said.ahmad.javafx.tracker.controller.setting.MiscSettingController;
import said.ahmad.javafx.tracker.controller.setting.TrackerPlayerSettingController;
import said.ahmad.javafx.tracker.controller.setting.UserSettingController;

public class SettingController {

	public static enum Setting_TYPE {
		MISC, USERS, FAVORITE, TRACKER_PLAYER,
	}

	private LinkedHashMap<Setting_TYPE, GenericSettingController> allSettingControllers = new LinkedHashMap<Setting_TYPE, GenericSettingController>() {
		/**
		 *
		 */
		private static final long serialVersionUID = 7284932390488743829L;

		{
			put(Setting_TYPE.MISC, new MiscSettingController());
			put(Setting_TYPE.USERS, new UserSettingController());
			put(Setting_TYPE.FAVORITE, new FavoritesSettingController());
			put(Setting_TYPE.TRACKER_PLAYER, new TrackerPlayerSettingController());
		}
	};

	// ----------------------------- Common UI -----------------------------
	@FXML
	private SplitPane splitPane;

	@FXML
	private Button restoreDefaultButton;
	@FXML
	private Button cancelButton;
	@FXML
	private Button saveButton;

	@FXML
	private TextField searchField;

	@FXML
	private VBox noContentPane;

	@FXML
	private TreeView<GenericSettingController> treeViewSetting;
	private TreeItem<GenericSettingController> rootTreeItem;

	private GenericSettingController currentShownSetting;
	private Stage settingStage; // defined to close it later
	private WelcomeController welcomeController;
	public static final Image SETTING_ICON_IMAGE = IconLoader.getIconImage(ICON_TYPE.SETIING);

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
			FXMLLoader loader = new FXMLLoader(ResourcesHelper.getResourceAsURL("/fxml/setting/base/SettingView.fxml"));
			loader.setController(this);
			root = loader.load();
			scene = new Scene(root);
			ThemeManager.applyTheme(scene);

			settingStage.setTitle("Setting Tracker Explorer");
			settingStage.setScene(scene);

			settingStage.getIcons().add(SETTING_ICON_IMAGE);

			initializeNodes();
			initializeTreeView();

			splitPane.getItems().remove(noContentPane);
			showSettingView(Setting_TYPE.MISC);
			pullDataFromSetting();

			settingStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initializeNodes() {
		// TODO Auto-generated method stub
		restoreDefaultButton.setGraphic(IconLoader.getIconImageView(ICON_TYPE.UNDO));
		cancelButton.setGraphic(IconLoader.getIconImageView(ICON_TYPE.CANCEL));
		saveButton.setGraphic(IconLoader.getIconImageView(ICON_TYPE.APPLY));

		noContentPane.getChildren().add(IconLoader.getIconImageView(ICON_TYPE.RESOURCE_NOT_FOUND, true, 250, 200));

		searchField.textProperty().addListener((observable, oldValue, newValue) -> {
			reloadTreeView();
			if (newValue == null || newValue.isEmpty()) {
				if (isShowingNoContentPane()) {
					hideNoContentPane();
					showSettingView(currentShownSetting);
				}
				allSettingControllers.values().forEach(st -> {
					st.clearSearch();
				});
			} else {
				GenericSettingController lastfoundController = null;
				for (GenericSettingController controller : allSettingControllers.values()) {
					if (controller.searchKeyWord(newValue)) {
						lastfoundController = controller;
					} else {
						rootTreeItem.getChildren().remove(controller.getTreeItem());
					}
				}
				if (lastfoundController != null) {
					treeViewSetting.getSelectionModel().select(lastfoundController.getTreeItem());
				} else {
					showNoContentPane();
				}
			}
		});
	}

	private void initializeTreeView() {
		TreeItem<GenericSettingController> root = new TreeItem<>(new MiscSettingController());
		rootTreeItem = root;
		treeViewSetting.setRoot(root);
		treeViewSetting.setShowRoot(false);
		treeViewSetting.getSelectionModel().selectedItemProperty()
				.addListener((observable, oldController, newController) -> {
					if (newController != null && newController != oldController) {
						showSettingView(newController.getValue());
					}
				});
		reloadTreeView();
	}

	private void reloadTreeView() {
		TreeItem<GenericSettingController> oldSelection = treeViewSetting.getSelectionModel().getSelectedItem();
		rootTreeItem.getChildren().clear();
		for (GenericSettingController controller : allSettingControllers.values()) {
			rootTreeItem.getChildren().add(controller.getTreeItem());
		}
		treeViewSetting.getSelectionModel().select(oldSelection);
	}

	public void showSettingView(Setting_TYPE SETTING_TYPE) {
		showSettingView(allSettingControllers.get(SETTING_TYPE));
	}

	private void showSettingView(GenericSettingController genericSettingController) {
		if (currentShownSetting != null) {
			splitPane.getItems().remove(currentShownSetting.getViewPane());
		}
		currentShownSetting = genericSettingController;
		if (currentShownSetting.getViewPane() == null) {
			try {
				currentShownSetting.loadFXML();
				currentShownSetting.initializeNodes();
				currentShownSetting.initializeDataViewHolders();
				currentShownSetting.pullDataFromSetting();
			} catch (IOException e) {
				e.printStackTrace();
				DialogHelper.showException(e);
			}
		}
		if (splitPane.getItems().contains(currentShownSetting.getViewPane())) {
			return;
		}
		hideNoContentPane();
		splitPane.getItems().add(currentShownSetting.getViewPane());
		splitPane.getDividers().get(0).setPosition(0.2);
		treeViewSetting.getSelectionModel().select(currentShownSetting.getTreeItem());
	}

	public void showNoContentPane() {
		splitPane.getItems().remove(currentShownSetting.getViewPane());
		if (!isShowingNoContentPane()) {
			splitPane.getItems().add(noContentPane);
			splitPane.getDividers().get(0).setPosition(0.2);
		}
	}

	public void hideNoContentPane() {
		if (isShowingNoContentPane()) {
			splitPane.getItems().remove(noContentPane);
		}
	}

	public boolean isShowingNoContentPane() {
		return splitPane.getItems().contains(noContentPane);
	}

	@FXML
	private void pullDataFromSetting() {
		for (GenericSettingController controller : allSettingControllers.values()) {
			if (controller.isLoadedView()) {
				controller.pullDataFromSetting();
			}
		}
	}

	@FXML
	private void saveSetting(ActionEvent event) {
		boolean isChanged = false;
		for (GenericSettingController controller : allSettingControllers.values()) {
			if (controller.isLoadedView()) {
				isChanged |= controller.pushDataToSetting();
			}
		}
		if (welcomeController != null) {
			welcomeController.changeInSetting();
		} else if (isChanged) {
			DialogHelper.showAlert(AlertType.INFORMATION, "Setting Changed", "Setting applied require view changes",
					"A refresh May be required to take effect.");
		}
		Setting.saveSetting();
		settingStage.close();
	}

	@FXML
	private void cancel(ActionEvent event) {
		settingStage.close();
	}

	public Stage getSettingStage() {
		return settingStage;
	}
}
