package said.ahmad.javafx.tracker.controller.setting.base;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import said.ahmad.javafx.tracker.app.pref.Setting;

public abstract class GenericSettingController {

	private TreeItem<GenericSettingController> treeItem = new TreeItem<GenericSettingController>(this);

	/**
	 *
	 * @return Title to be displayed in tree view
	 */
	public abstract String getTitle();

	/**
	 *
	 * @return The root Tree Item of
	 */
	public TreeItem<GenericSettingController> getTreeItem() {
		Image graphics = getIconImage();
		if (graphics != null) {
			treeItem.setGraphic(new ImageView(graphics));
		}
		return treeItem;
	};

	/** Image to be used as graphic in TreeItem */
	@Nullable
	public abstract Image getIconImage();

	/**
	 * Load FXML corresponding FXML and return main pane
	 *
	 * @return the pane where all input are
	 * @throws IOException
	 */
	public abstract FXMLLoader loadFXML() throws IOException;

	/**
	 * To be Called once after {@link #loadFXML()}<br>
	 * Example: used to initialize Error label to empty <br>
	 * or programmatically bind action to buttons...
	 */
	public abstract void initializeNodes();

	/**
	 * To be Called once after {@link #initializeNodes()} <br>
	 * Used to initialize DataView like binding Observable list to a list View
	 */
	public abstract void initializeDataViewHolders();

	/**
	 * Return main View Pane
	 *
	 * @return the pane where all input are<br>
	 *         <code>null</code> when pane is not loaded
	 */
	@Nullable
	public abstract Parent getViewPane();

	public boolean isLoadedView() {
		return getViewPane() != null;
	}

	/**
	 * Search for setting and Hide unrelated stuff corresponding to parameter
	 * keyword
	 *
	 * @param keyword
	 * @return <code>true</code> if any match<br>
	 *         <code>false</code> otherwise
	 */
	public abstract boolean searchKeyWord(String keyword);

	/**
	 * show all stuff if any get hidden by {@link #searchKeyWord(String)}
	 */
	public abstract void clearSearch();

	/**
	 * Used to reset form data to user saved setting from {@link Setting}
	 */
	public abstract void pullDataFromSetting();

	/**
	 * Check if current setting are valid and ready to be saved<br>
	 * This function will be called before {@link #pushDataToSetting()} and before
	 * moving to other generic controller.<br>
	 * Showing detail of possible error and dialog alert is up to the generic
	 * controller implementation
	 *
	 * @param showDialogAlert show dialog alert if set to true, silent otherwise
	 * @return true if setting are valid, false otherwise
	 */
	public abstract boolean isValidNewSetting(boolean showDialogAlert);

	/**
	 * Used to save form data setting to {@link Setting} <br>
	 * if {@link #isValidNewSetting()} returned false, nothing is saved
	 * @return <code>true</code>If changes occurs and require refresh of view<br>
	 *         <code>false</code> otherwise.<br>
	 *         Example when changing active user in setting this requires loading
	 *         other tracker data in view thus require a refresh
	 */
	public abstract boolean pushDataToSetting();

	@Override
	public String toString() {
		return getTitle();
	};
}
