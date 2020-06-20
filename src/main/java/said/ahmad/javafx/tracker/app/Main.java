package said.ahmad.javafx.tracker.app;

// if system have icon for a folder

// all folder will be the same icon because i saved in a map for faster rendering

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import said.ahmad.javafx.tracker.app.look.ThemeManager;
import said.ahmad.javafx.tracker.app.pref.Setting;
import said.ahmad.javafx.tracker.controller.PhotoViewerController;
import said.ahmad.javafx.tracker.controller.WelcomeController;
import said.ahmad.javafx.tracker.system.SystemIconsHelper;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.tracker.system.file.local.FilePathLayer;
import said.ahmad.javafx.tracker.system.operation.FileHelperGUIOperation;
import said.ahmad.javafx.tracker.system.services.TrackerPlayer;
import said.ahmad.javafx.tracker.system.tracker.FileTracker;

public class Main extends Application {
	private static WelcomeController mWelcomeController;
	private static Stage primaryStage;
	private static boolean isPathArgumentPassed = false;

	public static enum ArgsType {
		player, silent
	}

	@Override
	public void start(Stage primStage) throws IOException {
		primaryStage = primStage;
		Setting.loadSettingPartOne();
		FileTracker.updateUserFileName(Setting.getActiveUser());

		Parameters parameters = getParameters();

		// argument in the form argument
		List<String> unnamed = parameters.getUnnamed();

		// argument in the form --name=value
		Map<String, String> named = parameters.getNamed();

		// ---------- Tracker Player action --player=playlistPath -----------------
		boolean isSilentWithShutdown = true;
		primStage.setScene(new Scene(new Label()));
		primStage.centerOnScreen();
		if (named.containsKey(ArgsType.player.toString())) {
			File playlist = new File(named.get(ArgsType.player.toString()));
			// initialize simple Primary stage so Dialog helper can be shown
			if (playlist.exists()) {
				try {
					TrackerPlayer.openPlaylistInLnk(playlist);
				} catch (ParseException e1) {
					e1.printStackTrace();
					DialogHelper.showException(e1);
				}
			} else {
				DialogHelper.showAlert(AlertType.ERROR, "Tracker Player", "Playlist File is Missing!\n",
						"\t\tNOT FOUND !\n  ----  " + playlist + "  ----  \n\nPlease change it's location in setting.");
				TrackerPlayer.openTrackerSettingGUI();
				isSilentWithShutdown = false;
			}
		}

		// ---------- Silent Action --silent=1 -----------------
		if (named.containsKey(ArgsType.silent.toString())) {
			if (isSilentWithShutdown) {
				Platform.exit();
				System.exit(0);
				return;
			}
			if (named.get(ArgsType.silent.toString()).equals("1")) {
				return;
			}
		}

		// ---------- Argument Path Action 'initialPath' -----------------
		if (unnamed.size() == 0) {
			initializePath();
		} else {
			// I get windows argument here
			// to resolve opening root dir
			String path = unnamed.get(0);
			path = path.replace("\"", "");
			FilePathLayer temp = new FilePathLayer(new File(path));
			if (temp.exists()) {
				String extension = temp.getExtensionUPPERCASE();
				if (PhotoViewerController.ArrayIMGExt.contains(extension)) {
					new PhotoViewerController(null, temp, null);
					return;
				}
				isPathArgumentPassed = true;
				Setting.setLeftLastKnowLocation(temp);
				Setting.setRightLastKnowLocation(temp);
			} else {
				initializePath();
			}
		}
		FXMLLoader loader = new FXMLLoader();
//		 loader.setLocation(ResourcesHelper.getResourceAsURL("/fxml/bootstrap3.fxml"));
		loader.setLocation(ResourcesHelper.getResourceAsURL("/fxml/Welcome.fxml"));
		mWelcomeController = new WelcomeController();
		loader.setController(mWelcomeController);
		loader.load();
		Parent root = loader.getRoot();

		Scene scene = new Scene(root);
		ThemeManager.changeDefaultTheme(Setting.getLastTheme(), Setting.getLastThemeColor());
		ThemeManager.applyTheme(scene);

		primaryStage.setScene(scene);
		primaryStage.getIcons().add(ThemeManager.DEFAULT_ICON_IMAGE);

		// TODO later resolve system icon helper to work with uppercase extension
		PhotoViewerController.ArrayIMGExt
				.forEach(ext -> SystemIconsHelper.addFileIcon(ext, PhotoViewerController.PHOTO_ICON_IMAGE));

		primaryStage.setMaximized(Setting.isMaximized());
		primaryStage.show();
		mWelcomeController.initializePart2AddSplitView(primaryStage, true);
		ThreadExecutors.recursiveExecutor.execute(
				() -> Setting.loadSettingPartTwo(() -> mWelcomeController.initializePart3AddTabs(primaryStage, false)));
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent t) {
				primStage.hide();
				mWelcomeController.saveSetting();
				Platform.exit();
				Setting.saveSetting();
				System.exit(0);
			}
		});

		FileHelperGUIOperation.initializeView();
	}

	public static void main(String[] args) {
		launch(args);
	}

	private static void initializePath() {
		boolean doneLeft = false, doneRight = false;
		// priority to argument then last know location then root 0 (C)
		PathLayer temp;
		if (Setting.getLeftLastKnowLocation() != null) {
			// check if file still exist then distribute task and switch the missing one to
			// the other
			temp = Setting.getLeftLastKnowLocation();
			if (temp.exists()) {
				doneLeft = true;
			}
		}
		if (Setting.getRightLastKnowLocation() != null) {
			temp = Setting.getRightLastKnowLocation();
			if (temp.exists()) {
				doneRight = true;
				if (!doneLeft) {
					Setting.setLeftLastKnowLocation(Setting.getRightLastKnowLocation());
					doneLeft = true;
				}
			}
		}
		if (doneLeft && !doneRight) {
			Setting.setRightLastKnowLocation(Setting.getLeftLastKnowLocation());
			doneRight = true;
		}

		if (!doneLeft) {
			File[] roots = File.listRoots();
			Setting.setLeftLastKnowLocation(new FilePathLayer(roots[0]));
			Setting.setRightLastKnowLocation(new FilePathLayer(roots[0]));
		}
	}

	public static void showStage() {
		primaryStage.show();
	}

	public static void hideStage() {
		primaryStage.hide();
	}

	public static Stage getPrimaryStage() {
		return primaryStage;
	}

	private static String baseName;

	public static void UpdateTitle(String toAdd) {
		baseName = toAdd + " - Tracker Explorer";
		primaryStage.setTitle(baseName);
	}

	public static void ResetTitle() {
		primaryStage.setTitle(baseName);
	}

	private static char pr = '\\';

	public static void ProcessTitle(String toAppend) {
		pr = pr == '\\' ? '/' : '\\';
		primaryStage.setTitle(" " + pr + toAppend);
	}

	public static String GetTitle() {
		return primaryStage.getTitle();
	}

	/**
	 * @return the isPathArgumentPassed
	 */
	public static boolean isPathArgumentPassed() {
		return isPathArgumentPassed;
	}

	// about deploying as package independent check this :
	// create an environment variable like this
	// JAVA_HOME = C:\Program Files\Java\jdk1.8.0_211
	// https://stackoverflow.com/questions/24840414/javafx-build-failed
	// https://code.makery.ch/library/javafx-tutorial/part7/
	// if error encountered get older version of Inno Setup
	// about icon
	// https://github.com/BilledTrain380/javafx-gradle-plugin/blob/648acafa7198e9bd7cf1a2ef933456ce5e0b65f9/README.md#customize-icons
}
