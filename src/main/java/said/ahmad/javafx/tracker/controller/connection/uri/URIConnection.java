package said.ahmad.javafx.tracker.controller.connection.uri;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import said.ahmad.javafx.fxGraphics.IntField;
import said.ahmad.javafx.tracker.app.ResourcesHelper;
import said.ahmad.javafx.tracker.app.ThreadExecutors;
import said.ahmad.javafx.tracker.controller.connection.ConnectionController.ConnectionType;
import said.ahmad.javafx.tracker.controller.connection.GenericConnectionController;
import said.ahmad.javafx.tracker.datatype.ConnectionAccount;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.util.IpAddress;

public class URIConnection implements Initializable, GenericConnectionController {

	/**
	 * @return the portField
	 */
	public IntField getPortField() {
		return portField;
	}

	@FXML
	protected GridPane viewPane;

	@FXML
	protected IntField ipPart1Field;

	@FXML
	protected IntField ipPart2Field;

	@FXML
	protected IntField ipPart3Field;

	@FXML
	protected IntField ipPart4Field;

	@FXML
	protected IntField portField;

	@FXML
	protected TextField usernameField;

	@FXML
	protected PasswordField passwordField;

	@FXML
	protected CheckBox anonymousCheckBox;

	@FXML
	protected Text errorText;

	// just for easy access iteration
	protected ArrayList<TextField> allTextField = new ArrayList<>();

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		// IPs field
		allTextField.add(ipPart1Field);
		allTextField.add(ipPart2Field);
		allTextField.add(ipPart3Field);
		allTextField.add(ipPart4Field);

		allTextField.add(portField);

		allTextField.add(usernameField);

		errorText.setText("");

		allTextField.forEach(txt -> txt.setText(""));
		for (int i = 0; i < 4; i++) {
			int j = i;
			allTextField.get(i).setOnKeyPressed(keyEvent -> {
				if (keyEvent.getCode().equals(KeyCode.DECIMAL)) {
					allTextField.get(j + 1).requestFocus();
				}
			});
		}

		anonymousCheckBox.selectedProperty().addListener((obser, old, isSelected) -> {
			if (isSelected) {
				usernameField.setDisable(true);
				passwordField.setDisable(true);
			} else {
				usernameField.setDisable(false);
				passwordField.setDisable(false);
			}
		});
	}

	@Override
	public boolean checkInputValidation() {
		for (TextField textField : allTextField) {
			if (!textField.isDisabled() && (textField.getText() == null || textField.getText().isEmpty())) {
				textField.requestFocus();
				errorText.setText("Please fill all field. This Field is required!\nHint:" + textField.getPromptText());
				return false;
			}
		}
		errorText.setText("");
		return true;
	}

	public String getIPAddress() {
		return ipPart1Field.getValue() + "." + ipPart2Field.getValue() + "." + ipPart3Field.getValue() + "."
				+ ipPart4Field.getValue();
	}

	public URI getURIConnection(String scheme) throws URISyntaxException {
		URI uri = null;
		String username = usernameField.getText();
		String password = passwordField.getText();
		if (usernameField.isDisabled()) {
			username = null;
			password = null;
		}
		String userInfo = username == null || username.isEmpty() ? null
				: username + (password == null || password.isEmpty() ? "" : ":" + password);
		uri = new URI(scheme, userInfo, getIPAddress(), portField.getValue(), null, null, null);
		return uri;
	}

	public void initializeInputFieldsWithLocalHost() {
		ThreadExecutors.recursiveExecutor.execute(() -> {
			String ip = IpAddress.getLocalAddress();
			if (ip != null) {
				ConnectionAccount account = new ConnectionAccount();
				account.setHost(ip);
				Platform.runLater(() -> setInputFields(account));
			}
		});
	}

	public void setInputFields(ConnectionAccount account) {
		ArrayList<Integer> ipsAsInt = IpAddress.splitIpAddress(account.getHost());
		setInputFields(ipsAsInt.get(0), ipsAsInt.get(1), ipsAsInt.get(2), ipsAsInt.get(3), account.getPort(),
				account.getUsername(), account.getPassword());
	}

	public void setInputFields(Integer ipPart1, Integer ipPart2, Integer ipPart3, Integer ipPart4, Integer port,
			String user, String password) {
		if (ipPart1 != null) {
			ipPart1Field.setText(ipPart1.toString());
		}
		if (ipPart2 != null) {
			ipPart2Field.setText(ipPart2.toString());
		}
		if (ipPart3 != null) {
			ipPart3Field.setText(ipPart3.toString());
		}
		if (ipPart4 != null) {
			ipPart4Field.setText(ipPart4.toString());
		}
		if (port != null) {
			portField.setText(port.toString());
		}
		if (user != null) {
			usernameField.setText(user);
		}
		if (password != null) {
			passwordField.setText(password);
		}
	}

	@Override
	public boolean testConnection() throws Exception {
		return false;
	}

	@Override
	public PathLayer connect() throws Exception {
		return null;
	}

	@Override
	public void clearAllFields() {
		allTextField.forEach(txt -> txt.setText(""));
	}

	@Override
	public FXMLLoader loadFXML() throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(ResourcesHelper.getResourceAsURL("/fxml/connection/uri/URIConnection.fxml"));
		loader.setController(this);
		loader.load();
		return loader;
	}

	@Override
	public Pane getViewPane() {
		return viewPane;
	}

	@Override
	public ConnectionType getConnectionType() {
		return null;
	}

	@Override
	public void initializeDefaultFields() {
		initializeInputFieldsWithLocalHost();
	}
}
