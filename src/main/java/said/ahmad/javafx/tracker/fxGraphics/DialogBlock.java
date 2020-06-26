package said.ahmad.javafx.tracker.fxGraphics;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import said.ahmad.javafx.tracker.app.look.ThemeManager;

public class DialogBlock extends Dialog<String> {
	private Label blockWaitingText;

	public DialogBlock(String title, String WaitingText) {
		super();
		blockWaitingText = new Label();
		setTitle(title);
		blockWaitingText.setText(WaitingText);

		Stage stage = (Stage) getDialogPane().getScene().getWindow();
		stage.getIcons().add(ThemeManager.DEFAULT_ICON_IMAGE);
		ThemeManager.applyTheme(stage.getScene());

		getDialogPane().getButtonTypes().addAll(ButtonType.OK);
		final Button btOk = (Button) getDialogPane().lookupButton(ButtonType.OK);
		btOk.setDisable(true);
//		dialog.getDialogPane().getButtonTypes().addAll(submitButtonOk, ButtonType.CANCEL);
		// Set the button types.
		GridPane gridPane = new GridPane();
		gridPane.setHgap(10);
		gridPane.setVgap(10);
		gridPane.setPadding(new Insets(20, 150, 10, 10));
		gridPane.add(blockWaitingText, 1, 0);
		gridPane.add(new ProgressIndicator(), 0, 0);
		getDialogPane().setContent(gridPane);
	}

	public void showWaitingScreenBlock(String title, String waitingText) {
		setOnCloseRequest(e -> showWaitingScreenBlock(title, waitingText));
		setTitle(title);
		blockWaitingText.setText(waitingText);
		show();
	}

	public void hideWaitingScreen() {
		closeWaitingScreen();
	}

	public void closeWaitingScreen() {
		setOnCloseRequest(e -> {
		});
		close();
	}

}
