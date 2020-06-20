package said.ahmad.javafx.tracker.controller.connection.local;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.jetbrains.annotations.Nullable;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import said.ahmad.javafx.tracker.app.ResourcesHelper;
import said.ahmad.javafx.tracker.controller.connection.ConnectionController.ConnectionType;
import said.ahmad.javafx.tracker.controller.connection.GenericConnectionController;
import said.ahmad.javafx.tracker.controller.connection.uri.URIConnection;
import said.ahmad.javafx.tracker.system.call.SystemExplorer;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.tracker.system.file.local.FilePathLayer;

public class LocalConnectionController implements GenericConnectionController, Initializable {

	URIConnection uriConnection;

	private static enum SUPPORTED_SYSTEM_CONNECTION {
		FILE_DIRECTORY, HTTP, OTHER
	}

	@FXML
	private GridPane viewAllPane;

	@FXML
	private ComboBox<SUPPORTED_SYSTEM_CONNECTION> schemaComboBox;

	@FXML
	private VBox vboxPane;

	@FXML
	private HBox schemeOtherHbox;
	@FXML
	private TextField schemeOtherNameTextField;

	@FXML
	private HBox browseLocalFileHbox;

	@FXML
	private Label targetFile;

	List<Pane> allUnderPane = new ArrayList<>();

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		assert uriConnection != null;
		allUnderPane.add(uriConnection.getViewPane());
		allUnderPane.add(browseLocalFileHbox);
		allUnderPane.add(schemeOtherHbox);

		schemaComboBox.setItems(FXCollections.observableArrayList(SUPPORTED_SYSTEM_CONNECTION.values()));
		schemaComboBox.setOnAction(e -> changeSchemeType(schemaComboBox.getSelectionModel().getSelectedItem()));
		schemaComboBox.getSelectionModel().select(SUPPORTED_SYSTEM_CONNECTION.HTTP);
		changeSchemeType(SUPPORTED_SYSTEM_CONNECTION.HTTP);
	}

	private void changeSchemeType(SUPPORTED_SYSTEM_CONNECTION scheme) {
		allUnderPane.forEach(p -> p.setVisible(false));
		vboxPane.getChildren().clear();
		switch (scheme) {
		case FILE_DIRECTORY:
			vboxPane.getChildren().add(browseLocalFileHbox);
			browseLocalFileHbox.setVisible(true);
			return;
		case OTHER:
			vboxPane.getChildren().add(schemeOtherHbox);
			schemeOtherHbox.setVisible(true);
		case HTTP:
			uriConnection.getPortField().setPromptText("8080");
			uriConnection.getPortField().setValue(8080);
			break;
		default:
			break;
		}
		vboxPane.getChildren().add(uriConnection.getViewPane());
		uriConnection.getViewPane().setVisible(true);
	}

	@FXML
	private void browseTargetDirectory() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Navigate where where you want to browse");
		final Node source = schemaComboBox;
//		final Stage stage = (Stage) source.getScene().getWindow();
		File targetDir = directoryChooser.showDialog(source.getScene().getWindow());
		if (targetDir == null) {
			return;
		}
		updateTargetFile(targetDir.toString(), e -> SystemExplorer.select(targetDir));
	}

	private void updateTargetFile(String message, EventHandler<Event> actionOnClick) {
		targetFile.setText(message);
		targetFile.setOnMouseClicked(e -> actionOnClick.handle(e));
		targetFile.setOnTouchPressed(e -> actionOnClick.handle(e));
	}

	private String getSelectedScheme() {
		if (schemeOtherHbox.isVisible()) {
			return schemeOtherNameTextField.getText();
		}
		return schemaComboBox.getSelectionModel().getSelectedItem().toString();
	}

	private File getFileConnection() {
		File file = null;
		if (uriConnection.getViewPane().isVisible()) {
			try {
				URI uri = uriConnection.getURIConnection(getSelectedScheme());
				// currently mounting webDav as Local File, Windows System handle it by mounting
				// WebDav connection
				if (getSelectedScheme().equals("HTTP")) {
					String connection = "\\\\" + uri.getHost() + "@" + uri.getPort() + "\\DavWWWRoot";
					file = new File(connection);
				}
				if (file == null) {
					return null;
				}
				File messageFile = file;
				Platform.runLater(() -> {
					updateTargetFile(messageFile.toString(), e -> SystemExplorer.select(messageFile));
				});
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		try {
			file = new File(targetFile.getText());
		} catch (Exception e) {
		}
		return file;
	}

	@Override
	public boolean testConnection() throws Exception {
		return getFileConnection().exists();
	}

	@Override
	public PathLayer connect() throws Exception {
		File file = getFileConnection();
		if (file == null) {
			return null;
		}
		return new FilePathLayer(file);
	}

	@Override
	public boolean checkInputValidation() {
		if (uriConnection.getViewPane().isVisible()) {
			return uriConnection.checkInputValidation();
		}
		return true;
	}

	@Override
	public void clearAllFields() {
		uriConnection.clearAllFields();
	}

	@Override
	public FXMLLoader loadFXML() throws IOException {
		uriConnection = new URIConnection();
		FXMLLoader loaderURI = new FXMLLoader();
		loaderURI.setController(uriConnection);
		loaderURI.setLocation(ResourcesHelper.getResourceAsURL("/fxml/connection/uri/URIConnection.fxml"));
		loaderURI.load();
		FXMLLoader loader = new FXMLLoader();
		loader.setController(this);
		loader.setLocation(ResourcesHelper.getResourceAsURL("/fxml/connection/local/LocalConnection.fxml"));
		loader.load();
		return loader;
	}

	@Override
	public ConnectionType getConnectionType() {
		return ConnectionType.LOCAL;
	}

	@Override
	@Nullable
	public Pane getViewPane() {
		return viewAllPane;
	}

	@Override
	public void initializeDefaultFields() {
		// do nothing
	}

}
