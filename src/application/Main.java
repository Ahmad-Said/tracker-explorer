package application;
// if system have icon for a folder

// all folder will be the same icon because i saved in a map for faster rendering

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import application.controller.WelcomeController;
import application.datatype.Setting;
import application.system.operation.FileHelperGUIOperation;
import application.system.services.TrackerPlayer;
import application.system.tracker.FileTracker;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {
	private static WelcomeController mWelcomeController;
	private static Stage primaryStage;

	public static enum ArgsType {
		player, silent
	}

	@Override
	public void start(Stage primStage) throws IOException {
		primaryStage = primStage;
		Setting.loadSetting();
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

		Scene scene = new Scene(root);

//		new JMetro(JMetro.Style.LIGHT).applyTheme(root);
		scene.getStylesheets().add("/css/bootstrap3.css");

		primaryStage.setScene(scene);
		primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/img/icon.png")));

		primaryStage.show();

		mWelcomeController.initializeViewStage(primaryStage, true);

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

	// about deploying as package independent check this :
	// create an environment variable like this
	// JAVA_HOME = C:\Program Files\Java\jdk1.8.0_211
	// https://stackoverflow.com/questions/24840414/javafx-build-failed
	// https://code.makery.ch/library/javafx-tutorial/part7/
	// if error encountered get older version of Inno Setup
	// about icon
	// https://github.com/BilledTrain380/javafx-gradle-plugin/blob/648acafa7198e9bd7cf1a2ef933456ce5e0b65f9/README.md#customize-icons
}
