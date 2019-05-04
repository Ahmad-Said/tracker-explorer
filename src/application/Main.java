package application;
// if system have icon for a folder 

// all folder will be the same icon because i saved in a map for faster rendering

import java.io.File;
import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
	static final KeyCombination SHORTCUT_DELETE = new KeyCodeCombination(KeyCode.DELETE);
	static final KeyCombination SHORTCUT_NEW_FILE = new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN);
	static final KeyCombination SHORTCUT_NEW_DIRECTORY = new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN,
			KeyCombination.SHIFT_DOWN);
	static final KeyCombination SHORTCUT_FOCUS_VIEW = new KeyCodeCombination(KeyCode.TAB);
	static final KeyCombination SHORTCUT_RENAME = new KeyCodeCombination(KeyCode.F2);
	static final KeyCombination SHORTCUT_FOCUS_SWITCH_VIEW = new KeyCodeCombination(KeyCode.TAB,
			KeyCombination.CONTROL_DOWN);
	static final KeyCombination SHORTCUT_SEARCH = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_ANY);
	static final KeyCombination SHORTCUT_Clear_Search = new KeyCodeCombination(KeyCode.ESCAPE,
			KeyCombination.CONTROL_ANY);
	static final KeyCombination SHORTCUT_REVEAL_IN_EXPLORER = new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN,
			KeyCombination.SHIFT_DOWN);

	private WelcomeController mWelcomeController;
	private static Stage PrimaryStage;

	@Override
	public void start(Stage primaryStage) throws IOException {
		PrimaryStage = primaryStage;
		// the old way but now i need the controller of the scene to call it here and
		// bind some action
		// FXMLLoader.load(getClass().getResource("/fxml/bootstrap3.fxml"));
		// Parent root = FXMLLoader.load(getClass().getResource("/fxml/Welcome.fxml"));
		// so i get my loader in another variable to get controller from it :)
		// if asking what controller does to this scene as it say with it i can controll
		// all the content of the scene
		// Setting.loadSetting();

		// deploy configuration:
		// this.getFrame().setIconImage(Toolkit.getDefaultToolkit().getImage(getClass()
		// .getClassLoader().getResource("MyProject/resources/myIcon.png")));
		FXMLLoader loader = new FXMLLoader();
		// loader.setLocation(getClass().getResource("/fxml/bootstrap3.fxml"));
		loader.setLocation(getClass().getResource("/fxml/Welcome.fxml"));
		loader.load();
		Parent root = loader.getRoot();

		mWelcomeController = loader.getController();

		Scene scene = new Scene(root);
		scene.getStylesheets().add("/css/bootstrap3.css");

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
			} else if (SHORTCUT_FOCUS_VIEW.match(e)) {
				mWelcomeController.focus_VIEW();
			} else if (SHORTCUT_FOCUS_SWITCH_VIEW.match(e)) {
				mWelcomeController.focus_Switch_VIEW();
			} else if (SHORTCUT_SEARCH.match(e)) {
				mWelcomeController.focusSearchField();
			} else if (SHORTCUT_Clear_Search.match(e)) {
				mWelcomeController.ClearSearchField();
			} else if (SHORTCUT_REVEAL_IN_EXPLORER.match(e)) {
				mWelcomeController.RevealINExplorer();
			}
		});
		PrimaryStage.setScene(scene);
		// PrimaryStage.getIcons().add(new
		// Image(Main.class.getResourceAsStream("/img/welcome.png")));
		PrimaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/img/icon.png")));
		PrimaryStage.show();
		PrimaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent t) {
				primaryStage.hide();
				Setting.setLeftLastKnowLocation((mWelcomeController.getLeftLastKnowLocation()));
				Setting.setRightLastKnowLocation((mWelcomeController.getRightLastKnowLocation()));
				Platform.exit();
				Setting.saveSetting();
				System.exit(0);
			}
		});
	}

	public static void main(String[] args) {
		Setting.loadSetting();
		boolean done = false;
		// priority to argument then last know location then root 0 (C)
		if (args.length == 0) {
			if (Setting.getLeftLastKnowLocation() != null) {
				// check if file still exist then distrubute task and switch the missing one to
				// the other
				File temp = new File(Setting.getLeftLastKnowLocation().toString());
				if (temp.exists()) {
					StringHelper.InitialLeftPath = Setting.getLeftLastKnowLocation();
					done = true;
				}
				if (Setting.getRightLastKnowLocation() != null) {
					temp = new File(Setting.getRightLastKnowLocation().toString());
					if (temp.exists()) {
						StringHelper.InitialRightPath = Setting.getRightLastKnowLocation();
						if (!done) {
							StringHelper.InitialLeftPath = StringHelper.InitialRightPath;
							done = true;
						}
					} else if (done)
						StringHelper.InitialRightPath = StringHelper.InitialLeftPath;
				}
			}
			if (!done) {
				File[] roots = File.listRoots();
				StringHelper.InitialLeftPath = roots[0].toPath();
				StringHelper.InitialRightPath = roots[0].toPath();
			}
		} else {
			File temp = new File(args[0]);
			StringHelper.InitialLeftPath = temp.toPath();
			StringHelper.InitialRightPath = temp.toPath();
		}
		VLC.initializeDefaultVLCPath();
		FileTracker.updateUserFileName(Setting.getActiveUser());
		// System.out.println(VLC.RecentTracker);
		launch(args);
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
		pr = (pr == '\\') ? '\\' : '/';
		PrimaryStage.setTitle(" " + pr + toAppend);
	}

	public static String GetTitle() {
		return PrimaryStage.getTitle();
	}

	// about deploying as package independant check this :
	// https://code.makery.ch/library/javafx-tutorial/part7/
	// about icon
	// https://github.com/BilledTrain380/javafx-gradle-plugin/blob/648acafa7198e9bd7cf1a2ef933456ce5e0b65f9/README.md#customize-icons
}
