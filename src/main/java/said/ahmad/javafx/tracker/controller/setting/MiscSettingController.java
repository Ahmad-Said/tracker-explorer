package said.ahmad.javafx.tracker.controller.setting;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.Nullable;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import said.ahmad.javafx.fxGraphics.IntField;
import said.ahmad.javafx.tracker.app.DialogHelper;
import said.ahmad.javafx.tracker.app.ResourcesHelper;
import said.ahmad.javafx.tracker.app.look.IconLoader;
import said.ahmad.javafx.tracker.app.look.IconLoader.ICON_TYPE;
import said.ahmad.javafx.tracker.app.pref.Setting;
import said.ahmad.javafx.tracker.controller.setting.base.GenericSettingController;
import said.ahmad.javafx.tracker.system.call.TeraCopy;
import said.ahmad.javafx.tracker.system.operation.FileHelper;

public class MiscSettingController extends GenericSettingController {

	@FXML
	private ScrollPane pane;

	@FXML
	private TitledPane notificationPane;
	@FXML
	private TitledPane teraCopyPane;
	@FXML
	private TitledPane autoRenameUTFPane;
	@FXML
	private TitledPane autoBackSyncPane;
	@FXML
	private TitledPane limitFileRecursivePane;
	@FXML
	private TitledPane autoClearOperationPane;
	@FXML
	private TitledPane openLastFavoritesStartup;
	@FXML
	private TitledPane dateFormatPane;

	private List<TitledPane> allTitledPanes = new ArrayList<>();

	@FXML
	private IntField limitFilesRercursive;

	@FXML
	private CheckBox notifyFilesChanges;
	@FXML
	private CheckBox showWindowOnTopWhenNotify;

	@FXML
	private TextField inputDateFormatPattern;
	@FXML
	private Label outputDateFormatExample;

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

	@Override
	public String getTitle() {
		return "General";
	}

	@Override
	public @Nullable Image getIconImage() {
		return IconLoader.getIconImage(ICON_TYPE.SETTING);
	}

	@Override
	public FXMLLoader loadFXML() throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(ResourcesHelper.getResourceAsURL("/fxml/setting/MiscSetting.fxml"));
		loader.setController(this);
		loader.load();
		return loader;
	}

	@Override
	public void initializeNodes() {
		// allTitledPanes uses titles in search operations
		allTitledPanes.add(notificationPane);
		allTitledPanes.add(teraCopyPane);
		allTitledPanes.add(autoRenameUTFPane);
		allTitledPanes.add(autoBackSyncPane);
		allTitledPanes.add(limitFileRecursivePane);
		allTitledPanes.add(autoClearOperationPane);
		allTitledPanes.add(openLastFavoritesStartup);
		allTitledPanes.add(dateFormatPane);
		inputDateFormatPattern.textProperty().addListener((observable, oldValue, newValue) -> {
			validateDateFormat();
		});
	}

	@Override
	public void initializeDataViewHolders() {

	}

	@Override
	public Parent getViewPane() {
		return pane;
	}

	@Override
	public boolean searchKeyWord(String keyword) {
		boolean didFoundAny = false;
		for (TitledPane titledPane : allTitledPanes) {
			if (titledPane.getText().toUpperCase().contains(keyword.toUpperCase())) {
				titledPane.setExpanded(true);
				titledPane.setVisible(true);
				didFoundAny = true;
			} else {
				titledPane.setExpanded(false);
				titledPane.setVisible(false);
			}
		}
		return didFoundAny;
	}

	@Override
	public void clearSearch() {
		for (TitledPane titledPane : allTitledPanes) {
			titledPane.setExpanded(false);
			titledPane.setVisible(true);
		}
	}

	@Override
	public void pullDataFromSetting() {
		autoBackSyncCheckBox.setSelected(Setting.isBackSync());
		autoRenameCheckBox.setSelected(Setting.isAutoRenameUTFFile());
		autoClearOperationFIle.setSelected(Setting.isAutoCloseClearDoneFileOperation());

		showWindowOnTopWhenNotify.setSelected(Setting.isShowWindowOnTopWhenNotify());
		notifyFilesChanges.setSelected(Setting.isNotifyFilesChanges());
		inputDateFormatPattern.setText(Setting.getDateFormatPattern());

		useTeraCopy.setSelected(Setting.isUseTeraCopyByDefault());
		limitFilesRercursive.setValue(Setting.getMaxLimitFilesRecursive());
		openRecentFavorites.setSelected(Setting.isRestoreLastOpenedFavorite());
		if (TeraCopy.getPath_Setup() != null) {
			teraCopyPath.setText(TeraCopy.getPath_Setup().toString());
		}

	}

	@Override
	public boolean isValidNewSetting(boolean showDialogAlert) {
		return true;
	}

	@Override
	public boolean pushDataToSetting() {
		Setting.setAutoCloseClearDoneFileOperation(autoClearOperationFIle.isSelected());
		Setting.setAutoRenameUTFFile(autoRenameCheckBox.isSelected());

		Setting.setNotifyFilesChanges(notifyFilesChanges.isSelected());
		Setting.setShowWindowOnTopWhenNotify(showWindowOnTopWhenNotify.isSelected());
		if (validateDateFormat())
			Setting.setDateFormatPattern(inputDateFormatPattern.getText());

		Setting.setUseTeraCopyByDefault(useTeraCopy.isSelected());
		Setting.setBackSync(autoBackSyncCheckBox.isSelected());
		Setting.setMaxLimitFilesRecursive(limitFilesRercursive.getValue());
		Setting.setRestoreLastOpenedFavorite(openRecentFavorites.isSelected());

		return false;
	}

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

		File teraCopyfile = fileChooser.showOpenDialog(getViewPane().getScene().getWindow());
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

	private boolean validateDateFormat() {
		try {
			DateFormat dateFormat = new SimpleDateFormat(inputDateFormatPattern.getText());
			outputDateFormatExample.setText(dateFormat.format(new Date(1646515917000L)));
			outputDateFormatExample.setTextFill(Color.BLACK);
		} catch (Exception e) {
			outputDateFormatExample.setText(e.getMessage());
			outputDateFormatExample.setTextFill(Color.RED);
			return false;
		}
		return true;
	}

	@FXML
	public void openPatternJavaHelpLink() {
		try {
			Desktop.getDesktop().browse(new URL("https://help.gooddata.com/cloudconnect/manual/date-and-time-format.html").toURI());
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
	}

}
