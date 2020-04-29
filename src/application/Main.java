package application;
// if system have icon for a folder

// all folder will be the same icon because i saved in a map for faster rendering

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import application.controller.WelcomeController;
import application.datatype.Setting;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {
	static final KeyCombination SHORTCUT_COPY = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);
	static final KeyCombination SHORTCUT_MOVE = new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN);
	static final KeyCombination SHORTCUT_OPEN_FAVORITE = new KeyCodeCombination(KeyCode.F, KeyCombination.SHIFT_DOWN);
	public static final KeyCombination SHORTCUT_DELETE = new KeyCodeCombination(KeyCode.DELETE);
	public static final KeyCombination SHORTCUT_NEW_FILE = new KeyCodeCombination(KeyCode.N,
			KeyCombination.SHORTCUT_DOWN);
	public static final KeyCombination SHORTCUT_NEW_DIRECTORY = new KeyCodeCombination(KeyCode.N,
			KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);
	static final KeyCombination SHORTCUT_FOCUS_VIEW = new KeyCodeCombination(KeyCode.TAB);
	static final KeyCombination SHORTCUT_SWITCH_RECURSIVE = new KeyCodeCombination(KeyCode.R,
			KeyCombination.CONTROL_DOWN);
	public static final KeyCombination SHORTCUT_RENAME = new KeyCodeCombination(KeyCode.F2);
	static final KeyCombination SHORTCUT_SWITCH_NEXT_TABS = new KeyCodeCombination(KeyCode.TAB,
			KeyCombination.CONTROL_DOWN);
	static final KeyCombination SHORTCUT_SWITCH_PREVIOUS_TABS = new KeyCodeCombination(KeyCode.TAB,
			KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
	static final KeyCombination SHORTCUT_CLOSE_CURRENT_TAB = new KeyCodeCombination(KeyCode.W,
			KeyCombination.CONTROL_DOWN);
	static final KeyCombination SHORTCUT_OPEN_NEW_TAB = new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN);
	static final KeyCombination SHORTCUT_EASY_FOCUS_SWITCH_VIEW = new KeyCodeCombination(KeyCode.F3);
	static final KeyCombination SHORTCUT_SEARCH = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN);
	static final KeyCombination SHORTCUT_Clear_Search = new KeyCodeCombination(KeyCode.ESCAPE,
			KeyCombination.CONTROL_ANY);
	static final KeyCombination SHORTCUT_REVEAL_IN_EXPLORER = new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN,
			KeyCombination.SHIFT_DOWN);

	private static WelcomeController mWelcomeController;
	private static Stage PrimaryStage;

	public static enum ArgsType {
		player, silent
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		PrimaryStage = primaryStage;
		Setting.loadSetting();
		Parameters parameters = getParameters();

		// argument in the form argument
		List<String> unnamed = parameters.getUnnamed();

		// argument in the form --name=value
		Map<String, String> named = parameters.getNamed();

		// ---------- Tracker Player action --player=playlistPath -----------------
		boolean isSilentWithShutdown = true;
		primaryStage.setScene(new Scene(new Label()));
		primaryStage.centerOnScreen();
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
			File temp = new File(path);
			if (temp.exists()) {
				StringHelper.InitialLeftPath = temp.toPath();
				StringHelper.InitialRightPath = temp.toPath();
			} else {
				initializePath();
			}
		}

		FileTracker.updateUserFileName(Setting.getActiveUser());

		FXMLLoader loader = new FXMLLoader();
		// loader.setLocation(getClass().getResource("/fxml/bootstrap3.fxml"));
		loader.setLocation(getClass().getResource("/fxml/Welcome.fxml"));
		loader.load();
		Parent root = loader.getRoot();

		mWelcomeController = loader.getController();
		mWelcomeController.setStage(primaryStage);

		Scene scene = new Scene(root);

//		new JMetro(JMetro.Style.LIGHT).applyTheme(root);
		scene.getStylesheets().add("/css/bootstrap3.css");

		PrimaryStage.setScene(scene);
		PrimaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/img/icon.png")));

		PrimaryStage.show();
		scene.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
			if (SHORTCUT_DELETE.match(e)) {
				mWelcomeController.delete();
			} else if (SHORTCUT_NEW_FILE.match(e)) {
				mWelcomeController.createFile();
			} else if (SHORTCUT_NEW_DIRECTORY.match(e)) {
				mWelcomeController.createDirectory();
			} else if (SHORTCUT_RENAME.match(e)) {
				mWelcomeController.rename();
			} else if (SHORTCUT_COPY.match(e)) {
				mWelcomeController.copy();
			} else if (SHORTCUT_MOVE.match(e)) {
				mWelcomeController.move();
			} else if (SHORTCUT_OPEN_FAVORITE.match(e)) {
				mWelcomeController.getToggleFavorite().fire();
				mWelcomeController.getToggleFavorite().requestFocus();
			} else if (SHORTCUT_FOCUS_VIEW.match(e)) {
				mWelcomeController.focus_VIEW();
			} else if (SHORTCUT_EASY_FOCUS_SWITCH_VIEW.match(e)) {
				mWelcomeController.focus_Switch_VIEW();
			} else if (SHORTCUT_SWITCH_NEXT_TABS.match(e)) {
				mWelcomeController.switch_Next_Tabs();
			} else if (SHORTCUT_CLOSE_CURRENT_TAB.match(e)) {
				mWelcomeController.close_Current_Tab();
			} else if (SHORTCUT_OPEN_NEW_TAB.match(e)) {
				mWelcomeController.open_New_Tab();
			} else if (SHORTCUT_SWITCH_PREVIOUS_TABS.match(e)) {
				mWelcomeController.switch_Previous_Tab();
			} else if (SHORTCUT_SEARCH.match(e)) {
				mWelcomeController.focusSearchField();
			} else if (SHORTCUT_SWITCH_RECURSIVE.match(e)) {
				mWelcomeController.switchRecursive();
			} else if (SHORTCUT_Clear_Search.match(e)) {
				mWelcomeController.ClearSearchField();
			} else if (SHORTCUT_REVEAL_IN_EXPLORER.match(e)) {
				mWelcomeController.RevealINExplorer();
			}
		});

		PrimaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent t) {
				primaryStage.hide();
				mWelcomeController.saveSetting();
				Platform.exit();
				Setting.saveSetting();
				System.exit(0);
			}
		});

		FileHelper.initializeView();
	}

	public static void main(String[] args) {
		launch(args);
	}

	private static void initializePath() {
		boolean doneLeft = false, doneRight = false;
		// priority to argument then last know location then root 0 (C)
		File temp;
		if (Setting.getLeftLastKnowLocation() != null) {
			// check if file still exist then distribute task and switch the missing one to
			// the other
			temp = new File(Setting.getLeftLastKnowLocation().toString());
			if (temp.exists()) {
				StringHelper.InitialLeftPath = Setting.getLeftLastKnowLocation();
				doneLeft = true;
			}
		}
		if (Setting.getRightLastKnowLocation() != null) {
			temp = new File(Setting.getRightLastKnowLocation().toString());
			if (temp.exists()) {
				StringHelper.InitialRightPath = Setting.getRightLastKnowLocation();
				doneRight = true;
				if (!doneLeft) {
					StringHelper.InitialLeftPath = StringHelper.InitialRightPath;
					doneLeft = true;
				}
			}
		}
		if (doneLeft && !doneRight) {
			StringHelper.InitialRightPath = StringHelper.InitialLeftPath;
			doneRight = true;
		}

		if (!doneLeft) {
			File[] roots = File.listRoots();
			StringHelper.InitialLeftPath = roots[0].toPath();
			StringHelper.InitialRightPath = roots[0].toPath();
		}
	}

	public static void refreshWelcomeController(List<Path> paths) {
		mWelcomeController.refreshWhenDetected(paths.toArray(new Path[paths.size()]));
	}

	public static void refreshWelcomeController(Path... paths) {
		mWelcomeController.refreshWhenDetected(paths);
	}

	public static void showStage() {
		PrimaryStage.show();
	}

	public static void hideStage() {
		PrimaryStage.hide();
	}

	public static Stage getPrimaryStage() {
		return PrimaryStage;
	}

	private static String baseName;

	public static void UpdateTitle(String toAdd) {
		baseName = toAdd + " - Tracker Explorer";
		PrimaryStage.setTitle(baseName);
	}

	public static void ResetTitle() {
		PrimaryStage.setTitle(baseName);
	}

	private static char pr = '\\';

	public static void ProcessTitle(String toAppend) {
		pr = pr == '\\' ? '/' : '\\';
		PrimaryStage.setTitle(" " + pr + toAppend);
	}

	public static String GetTitle() {
		return PrimaryStage.getTitle();
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
