package said.ahmad.javafx.tracker.app;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import said.ahmad.javafx.tracker.fxGraphics.IntField;

public class DialogHelper {

	private static Stage defaultStage = Main.getPrimaryStage();

	public static boolean showAlert(AlertType alertType, String title, String header, String content) {
		return showAlert(alertType, title, header, content, defaultStage);
	}

	public static boolean showAlert(AlertType alertType, String title, String header, String content, Stage owner) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(header);
		// WebView webView = new WebView();
		// webView.getEngine().loadContent(content);
		// webView.setPrefSize(150, 60);
		alert.setContentText(content);
		// alert.getDialogPane().setContent(webView);

		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(ThemeManager.DEFAULT_ICON_IMAGE);
		ThemeManager.applyTheme(stage.getScene());
		stage.initOwner(owner);
		stage.initModality(Modality.WINDOW_MODAL);

		Optional<ButtonType> result = alert.showAndWait();
		return result.isPresent() ? result.get() == ButtonType.OK : false;
	}

	public static void showImage(ImageView imageView) {
		final Stage croppedImageStage = new Stage();
		final BorderPane borderPane = new BorderPane();
		final ScrollPane rootPane = new ScrollPane();
		final Scene scene = new Scene(borderPane, 800, 800);
		rootPane.setContent(imageView);
		borderPane.setCenter(rootPane);
		croppedImageStage.setScene(scene);
		croppedImageStage.show();
		croppedImageStage.setScene(scene);
		croppedImageStage.show();
	}

	public static void showExpandableAlert(AlertType alertType, String title, String header, String content,
			String expandableContent) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);

		TextArea textArea = new TextArea(expandableContent);
		textArea.setEditable(false);
		textArea.setWrapText(true);
		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);

		VBox.setVgrow(textArea, Priority.ALWAYS);
		alert.getDialogPane().setExpandableContent(new VBox(textArea));
		alert.getDialogPane().setExpanded(true);

		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(ThemeManager.DEFAULT_ICON_IMAGE);
		ThemeManager.applyTheme(stage.getScene());

		alert.showAndWait();
	}

	public static boolean showConfirmationDialog(String title, String header, String content) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);

		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(ThemeManager.DEFAULT_ICON_IMAGE);
		ThemeManager.applyTheme(stage.getScene());

		Optional<ButtonType> result = alert.showAndWait();
		return result.get() == ButtonType.OK;
	}

	public static boolean showExpandableConfirmationDialog(String title, String header, String content,
			String expandableContent) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);

		TextArea textArea = new TextArea(expandableContent);
		textArea.setEditable(false);
		textArea.setWrapText(true);
		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);

		VBox.setVgrow(textArea, Priority.ALWAYS);
		alert.getDialogPane().setExpandableContent(new VBox(textArea));
		alert.getDialogPane().setExpanded(true);

		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(ThemeManager.DEFAULT_ICON_IMAGE);
		ThemeManager.applyTheme(stage.getScene());

		Optional<ButtonType> result = alert.showAndWait();
		return result.get() == ButtonType.OK;
	}

	@Nullable
	public static String showTextInputDialog(String title, String header, String content, String hint) {
		TextInputDialog dialog = new TextInputDialog(hint);
		dialog.setTitle(title);
		dialog.setHeaderText(header);
		dialog.setContentText(content);

		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(ThemeManager.DEFAULT_ICON_IMAGE);
		ThemeManager.applyTheme(stage.getScene());
		stage.initOwner(defaultStage);
		stage.initModality(Modality.WINDOW_MODAL);
		Optional<String> result = dialog.showAndWait();
		return result.isPresent() ? result.get() : null;
	}

	@Nullable
	public static Pair<String, String> showTextInputDoubledDialog(String title, String header, String content,
			String hint1, String hint2) {
		// https://stackoverflow.com/questions/31556373/javafx-dialog-with-2-input-fields
		// Create the custom dialog.
		Dialog<Pair<String, String>> dialog = new Dialog<>();
		dialog.setTitle(title);

		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(ThemeManager.DEFAULT_ICON_IMAGE);
		ThemeManager.applyTheme(stage.getScene());

		// Set the button types.
		ButtonType loginButtonType = new ButtonType("OK", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

		GridPane gridPane = new GridPane();
		gridPane.setHgap(10);
		gridPane.setVgap(10);
		gridPane.setPadding(new Insets(20, 150, 10, 10));

		TextField text1 = new TextField();
		text1.setPromptText(hint1);
		text1.setText(hint1);
		TextField text2 = new TextField();
		text2.setPromptText(hint2);
		text2.setText(hint2);

		gridPane.add(new Label("New Name:"), 0, 0);
		gridPane.add(text1, 1, 0);
		gridPane.add(new Label("."), 2, 0);
		gridPane.add(text2, 3, 0);

		dialog.getDialogPane().setContent(gridPane);

		Platform.runLater(() -> text1.requestFocus());

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == loginButtonType) {
				return new Pair<>(text1.getText(), text2.getText());
			}
			return null;
		});
		Optional<Pair<String, String>> result = dialog.showAndWait();
		return result.isPresent() ? result.get() : null;
	}

	@Nullable
	public static Integer showIntegerInputDialog(String title, String header, String askedValueName, int minValue,
			int maxValue, int initialValue) {
		// https://stackoverflow.com/questions/31556373/javafx-dialog-with-2-input-fields
		// Create the custom dialog.
		Dialog<Integer> dialog = new Dialog<>();
		dialog.setTitle(title);

		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(ThemeManager.DEFAULT_ICON_IMAGE);
		ThemeManager.applyTheme(stage.getScene());

		// Set the button types.
		ButtonType loginButtonType = new ButtonType("OK", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

		GridPane gridPane = new GridPane();
		gridPane.setHgap(10);
		gridPane.setVgap(10);
		gridPane.setPadding(new Insets(20, 150, 10, 10));

		IntField text1 = new IntField(minValue, maxValue, initialValue);

		gridPane.add(new Label(askedValueName), 0, 0);
		gridPane.add(text1, 1, 0);
		gridPane.add(new Label("Chose a number: "), 0, 1);
		gridPane.add(new Label(minValue + " --- To ---> " + maxValue), 1, 1);

		dialog.getDialogPane().setContent(gridPane);

		Platform.runLater(() -> {
			text1.requestFocus();
			text1.selectAll();
		});

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == loginButtonType) {
				return text1.getValue();
			}
			return null;
		});
		Optional<Integer> result = dialog.showAndWait();
		return result.isPresent() ? result.get() : null;
	}

	/**
	 *
	 * @param title
	 * @param header
	 * @param content
	 * @param labels
	 * @param hints
	 * @return a hash map from with key as label to response from the user hint is
	 *         used in prompt text
	 */
	@Nullable
	public static HashMap<String, String> showMultiTextInputDialog(String title, String header, String content,
			String[] labels, String[] hints, int focusIndex) {
		// https://stackoverflow.com/questions/31556373/javafx-dialog-with-2-input-fields
		// Create the custom dialog.
		Dialog<HashMap<String, String>> dialog = new Dialog<>();
		dialog.setTitle(title);

		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(ThemeManager.DEFAULT_ICON_IMAGE);
		ThemeManager.applyTheme(stage.getScene());

		// Set the button types.
		ButtonType submitButtonOk = new ButtonType("OK", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(submitButtonOk, ButtonType.CANCEL);

		// initializing view
		VBox root = new VBox(5);
		ScrollPane scrollPane = new ScrollPane(root);
		Scene scene = new Scene(scrollPane);
		ThemeManager.applyTheme(scene);

		// initializing Header and content
		Text headerText = new Text();
		if (header != null) {
			headerText = new Text(header + "\n-----------------\n");
			headerText.setFont(Font.font("Verdana", 20));
		}
		Text contentText = new Text(content + "\n");
		TextFlow headerFlow = new TextFlow(headerText, contentText);
		HBox headerHbox = new HBox(headerFlow);
		HBox.setHgrow(headerHbox, Priority.ALWAYS);
		root.getChildren().add(headerHbox);

		ArrayList<TextField> allInputs = new ArrayList<>();
		for (int i = 0; i < labels.length; i++) {
			String string = labels[i];
			Label tempLabel = new Label(string + ": ");
			TextField temp = new TextField();
			if (i < hints.length && !hints[i].isEmpty()) {
				temp.setPromptText(hints[i]);
				temp.setText(hints[i]);
			}
			allInputs.add(temp);
			HBox tempHbox = new HBox(tempLabel, allInputs.get(i));
			HBox.setHgrow(temp, Priority.ALWAYS);
			root.getChildren().add(tempHbox);
		}
		dialog.getDialogPane().setContent(scrollPane);

		// Request focus on asked parameter
		Platform.runLater(() -> allInputs.get(focusIndex).requestFocus());

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == submitButtonOk) {
				HashMap<String, String> responseMap = new HashMap<String, String>();
				for (int i = 0; i < labels.length; i++) {
					String string = labels[i];
					responseMap.put(string, allInputs.get(i).getText());
				}
				return responseMap;
			}
			return null;
		});
		Optional<HashMap<String, String>> result = dialog.showAndWait();
		return result.isPresent() ? result.get() : null;
	}

	public static void showException(Exception e) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		e.printStackTrace(printWriter);
		String exceptionText = stringWriter.toString();
		showExpandableAlert(AlertType.ERROR, "Tracker Explorer", "Something went wrong", e.toString(), exceptionText);
	}

	public static void showPingDialog(String msg) {
		showAlert(AlertType.INFORMATION, "Ping", msg, msg);
	}

	public static void showFileOperationBar() {

	}

	static private Dialog<String> blockDialog;

	public static void showWaitingScreen(String title, String WaitingText) {

		// https://stackoverflow.com/questions/31556373/javafx-dialog-with-2-input-fields
		// Create the custom dialog.
		blockDialog = new Dialog<String>();
		blockDialog.setTitle(title);

		Stage stage = (Stage) blockDialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(ThemeManager.DEFAULT_ICON_IMAGE);
		ThemeManager.applyTheme(stage.getScene());

		blockDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
		final Button btOk = (Button) blockDialog.getDialogPane().lookupButton(ButtonType.OK);
		btOk.setDisable(true);
		blockDialog.setOnCloseRequest(e -> showWaitingScreen(title, WaitingText));
//		dialog.getDialogPane().getButtonTypes().addAll(submitButtonOk, ButtonType.CANCEL);
		// Set the button types.
		GridPane gridPane = new GridPane();
		gridPane.setHgap(10);
		gridPane.setVgap(10);
		gridPane.setPadding(new Insets(20, 150, 10, 10));
		Label showLabel = new Label(WaitingText);
		gridPane.add(showLabel, 1, 0);
		gridPane.add(new ProgressIndicator(), 0, 0);
		blockDialog.getDialogPane().setContent(gridPane);
		blockDialog.show();
//		dialog.close();
	}

	public static void closeWaitingScreen() {
		blockDialog.setOnCloseRequest(e -> {
		});
		blockDialog.close();
	}

}
