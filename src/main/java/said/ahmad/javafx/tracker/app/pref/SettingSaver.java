package said.ahmad.javafx.tracker.app.pref;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

import javafx.application.Platform;
import said.ahmad.javafx.tracker.app.DialogHelper;
import said.ahmad.javafx.tracker.datatype.FavoriteView;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.tracker.system.file.PathLayerConverter;
import said.ahmad.javafx.tracker.system.file.local.FilePathLayer;
import said.ahmad.javafx.util.CallBackToDo;

class SettingSaver {
	private static File mSettingFileXML = new File(
			System.getenv("APPDATA") + "\\Tracker Explorer\\TrackerExplorerSetting.xml");
	// only write if successfully read
	private static boolean successfullyRead = true;
	private static SettingSaver toBeSaved = new SettingSaver();
	// ------------- Setting -----------------
	/// when adding a new setting To do:
	// 0- Setting declaration in Setting + getters and setters
	// 1- same declaration here with no modifiers (no static)
	// 2- add Setting.getVar() in pushToSetting
	// 3- add Setting.setVar() in pullFromSetting

	// favorite stuff
	private ArrayList<FavoriteView> favoritesLocations = new ArrayList<>();
	private boolean restoreLastOpenedFavorite = true;
	private ArrayList<String> lastOpenedFavoriteTitle = new ArrayList<>();

	public static void pushToSetting() {
		// favorite stuff
		Setting.setRestoreLastOpenedFavorite(toBeSaved.restoreLastOpenedFavorite);
		Setting.getFavoritesLocations().addAll(toBeSaved.favoritesLocations);
		Setting.setLastOpenedFavoriteTitle(toBeSaved.lastOpenedFavoriteTitle);

	}

	public static void pullFromSetting() {
		// favorite stuff
		toBeSaved.restoreLastOpenedFavorite = Setting.isRestoreLastOpenedFavorite();
		toBeSaved.favoritesLocations = new ArrayList<>(Setting.getFavoritesLocations().getList().values());
		toBeSaved.lastOpenedFavoriteTitle = Setting.getLastOpenedFavoriteTitle();
	}

	private static XStream getXStream() {
		XStream xstream = new XStream();
		xstream.alias("Tracker.Explorer.preferences", SettingSaver.class);

		xstream.alias("PathLayer", PathLayer.class);
		xstream.alias("LocalFile", FilePathLayer.class);

		xstream.alias("FavoriteView", FavoriteView.class);
		xstream.addImplicitCollection(FavoriteView.class, "locations");

		xstream.allowTypesByWildcard(new String[] { "said.ahmad.javafx.**" });
		xstream.ignoreUnknownElements();

		xstream.registerConverter(new PathLayerConverter());
		return xstream;
	}

	public static void saveSetting() {
		// Saving data to file
		if (!successfullyRead) {
			return;
		}
		pullFromSetting();
		try {
			XStream xstream = getXStream();
			OutputStream outputStream = new FileOutputStream(mSettingFileXML);
			xstream.toXML(toBeSaved, outputStream);

		} catch (Exception e) { // catches ANY exception
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param onFinishLoadingPlatformRun list of action to do on finish<br>
	 *                                   Can be null
	 */
	public static void loadSetting(List<CallBackToDo> onFinishLoadingPlatformRun) {
		// reading data from file
		if (!mSettingFileXML.exists()) {
			callOnFinishLoadingPlatformRun(onFinishLoadingPlatformRun);
			return;
		}
		XStream xstream = getXStream();
		try {
			toBeSaved = (SettingSaver) xstream.fromXML(mSettingFileXML);
			pushToSetting();
		} catch (XStreamException e) {
			successfullyRead = false;
			Platform.runLater(() -> {
				successfullyRead = DialogHelper.showExpandableConfirmationDialog("Tracker Explorer",
						"Something went Wrong loading XML Setting...",
						"\nDo you want To Overwrite Setting Next Time?" + "\nWarning you will lose your setting!",
						"File located at\n\t" + mSettingFileXML + "\n" + ExceptionUtils.getStackTrace(e));
				System.out.println(successfullyRead);
			});
			e.printStackTrace();
		}
		callOnFinishLoadingPlatformRun(onFinishLoadingPlatformRun);
	}

	private static void callOnFinishLoadingPlatformRun(List<CallBackToDo> onFinishLoadingPlatformRun) {
		if (onFinishLoadingPlatformRun != null) {
			Platform.runLater(() -> {
				onFinishLoadingPlatformRun.forEach(action -> {
					try {
						// call registered action independently
						action.call();
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
				onFinishLoadingPlatformRun.clear();
			});
		}
	}

}
