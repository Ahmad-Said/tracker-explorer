package said.ahmad.javafx.tracker.controller.connection.ftp;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPFile;
import org.jetbrains.annotations.Nullable;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import said.ahmad.javafx.tracker.app.ResourcesHelper;
import said.ahmad.javafx.tracker.controller.connection.ConnectionController.ConnectionType;
import said.ahmad.javafx.tracker.controller.connection.uri.URIConnection;
import said.ahmad.javafx.tracker.datatype.ConnectionAccount;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.tracker.system.file.ftp.FTPClientExt;
import said.ahmad.javafx.tracker.system.file.ftp.FTPPathLayer;

public class FTPConnectionController extends URIConnection {

	private FTPClientExt connection;

	private FTPClientExt newFTPClientConnection() throws Exception {
		String username = usernameField.getText();
		String password = passwordField.getText();
		if (anonymousCheckBox.isSelected() || username.isEmpty()) {
			username = null;
		}
		FTPClientExt ftp = new FTPClientExt(getIPAddress(), portField.getValue(), username, password);
		return ftp;
	}

	@Override
	public boolean testConnection() throws Exception {
		connection = newFTPClientConnection();
		boolean isConnected = connection.isConnected();
		connection.disconnect();
		return isConnected;
	}

	@Override
	public PathLayer connect() throws Exception {
		connection = newFTPClientConnection();
		FTPFile ftpFile = connection.getFileOrCached("/");
		FTPPathLayer ftpPathFile = null;
		if (ftpFile != null) {
			ftpPathFile = new FTPPathLayer(connection, ftpFile, "/");
		}
		if (ftpPathFile != null) {
			ConnectionAccount account = new ConnectionAccount(ftpPathFile.toURI());
			FTPClientExt.getAccountsToFTPClients().put(account, connection);
		}
		return ftpPathFile;
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
	public ConnectionType getConnectionType() {
		return ConnectionType.FTP;
	}

	@Override
	@Nullable
	public Pane getViewPane() {
		return viewPane;
	}
}
