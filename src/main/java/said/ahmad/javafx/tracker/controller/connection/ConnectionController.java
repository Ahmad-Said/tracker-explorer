package said.ahmad.javafx.tracker.controller.connection;

import java.io.IOException;
import java.util.HashMap;

import javax.xml.ws.Holder;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import said.ahmad.javafx.tracker.app.DialogHelper;
import said.ahmad.javafx.tracker.app.ResourcesHelper;
import said.ahmad.javafx.tracker.app.ThreadExecutors;
import said.ahmad.javafx.tracker.app.look.ThemeManager;
import said.ahmad.javafx.tracker.controller.connection.ftp.FTPConnectionController;
import said.ahmad.javafx.tracker.controller.connection.local.LocalConnectionController;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.util.CallBackVoid;

public class ConnectionController {

	public static enum ConnectionType {
		FTP, LOCAL
	}

	@FXML
	private ProgressIndicator connectingIndicator;

	@FXML
	private ComboBox<ConnectionType> providerTypeCombo;

	@FXML
	private Button clearAllFieldsButton;
	@FXML
	private Button testConnectionButton;
	@FXML
	private Button connectButton;

	@FXML
	private BorderPane viewPane;

	public static final Image CONNECTION_ICON_IMAGE = new Image(
			ResourcesHelper.getResourceAsStream("/img/connection/link-symbol.png"));

	private HashMap<ConnectionType, Pane> cachedConnectionPane = new HashMap<>();
	private GenericConnectionController currentConnection;
	private CallBackVoid<PathLayer> onSuccessFullConnectionDo;
	private Stage stage;

	public ConnectionController(ConnectionType connectionType, CallBackVoid<PathLayer> onSuccessFullConnectionDo) {
		this.onSuccessFullConnectionDo = onSuccessFullConnectionDo;
		try {
			Parent root;
			Scene scene;
			stage = new Stage();
			stage.sizeToScene();
			FXMLLoader loader = new FXMLLoader(ResourcesHelper.getResourceAsURL("/fxml/connection/Connection.fxml"));
			loader.setController(this);
			root = loader.load();
			scene = new Scene(root);
			ThemeManager.applyTheme(scene);
			stage.setTitle("Open Connection");
			stage.setScene(scene);
			stage.getIcons().add(CONNECTION_ICON_IMAGE);

			providerTypeCombo.setItems(FXCollections.observableArrayList(ConnectionType.values()));
			providerTypeCombo
					.setOnAction(e -> changeConnection(providerTypeCombo.getSelectionModel().getSelectedItem()));
			providerTypeCombo.getSelectionModel().select(connectionType);
			changeConnection(connectionType);
			currentConnection.initializeDefaultFields();
			exitConnectingBehavior();
			stage.show();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void showConnectingBehavior() {
		currentConnection.getViewPane().setDisable(true);
		providerTypeCombo.setDisable(true);
		clearAllFieldsButton.setDisable(true);
		testConnectionButton.setDisable(true);
		connectButton.setDisable(true);
		connectingIndicator.setVisible(true);
	}

	private void exitConnectingBehavior() {
		currentConnection.getViewPane().setDisable(false);
		providerTypeCombo.setDisable(false);
		clearAllFieldsButton.setDisable(false);
		testConnectionButton.setDisable(false);
		connectButton.setDisable(false);
		connectingIndicator.setVisible(false);
	}

	private void changeConnection(ConnectionType selectedConnection) {
		viewPane.setCenter(null);
		currentConnection = null;
		if (cachedConnectionPane.containsKey(selectedConnection)) {
			viewPane.setCenter(cachedConnectionPane.get(selectedConnection));
			return;
		}
		switch (selectedConnection) {
		case FTP:
			currentConnection = new FTPConnectionController();
			break;
		case LOCAL:
			currentConnection = new LocalConnectionController();
			break;
		default:
			break;
		}
		if (currentConnection != null)

		{
			// load corresponding connection and put it's pane in map
			try {
				currentConnection.loadFXML();
				cachedConnectionPane.put(currentConnection.getConnectionType(), currentConnection.getViewPane());
				viewPane.setCenter(currentConnection.getViewPane());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@FXML
	void clearAllFields(ActionEvent event) {
		currentConnection.clearAllFields();
	}

	@FXML
	void connect(ActionEvent event) {
		if (!currentConnection.checkInputValidation()) {
			return;
		}
		showConnectingBehavior();
		ThreadExecutors.recursiveExecutor.execute(() -> {
			Holder<PathLayer> pathLayer = new Holder<>();
			Holder<Boolean> doCloseView = new Holder<>(false);
			try {
				pathLayer.value = currentConnection.connect();
				Platform.runLater(() -> {
					if (pathLayer.value != null) {
						if (onSuccessFullConnectionDo != null) {
							onSuccessFullConnectionDo.call(pathLayer.value);
							doCloseView.value = true;
						} else {
							DialogHelper.showAlert(AlertType.INFORMATION, "Success", "Connection successfull",
									"Connection has been successfully established\n" + pathLayer.value, stage);
						}
					} else {
						DialogHelper.showAlert(AlertType.WARNING, "Fail", "Connection fail", "Connection unsuccessful",
								stage);
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
				Platform.runLater(() -> DialogHelper.showException(e));
			}
			Platform.runLater(() -> {
				exitConnectingBehavior();
				if (doCloseView.value) {
					stage.close();
				}
			});
		});

	}

	@FXML
	void testConnection(ActionEvent event) {
		if (!currentConnection.checkInputValidation()) {
			return;
		}
		showConnectingBehavior();
		ThreadExecutors.recursiveExecutor.execute(() -> {
			try {
				boolean reply = currentConnection.testConnection();
				Platform.runLater(() -> {
					if (reply) {
						DialogHelper.showAlert(AlertType.INFORMATION, "Success", "Test connection success",
								"Connection has been successfully established", stage);
					} else {
						DialogHelper.showAlert(AlertType.WARNING, "Fail", "Connection fail", "Connection unsuccessful",
								stage);
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
				Platform.runLater(() -> DialogHelper.showException(e));
			}
			Platform.runLater(() -> exitConnectingBehavior());
		});
	}

	/**
	 * @return the currentConnection
	 */
	public GenericConnectionController getCurrentConnection() {
		return currentConnection;
	}

}
