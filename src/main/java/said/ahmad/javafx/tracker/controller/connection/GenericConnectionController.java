package said.ahmad.javafx.tracker.controller.connection;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import said.ahmad.javafx.tracker.controller.connection.ConnectionController.ConnectionType;
import said.ahmad.javafx.tracker.system.file.PathLayer;

public interface GenericConnectionController {

	/**
	 * Check if resources can be reached
	 *
	 * @return <code>true</code>on successful connection, Owner will show Dialog<br>
	 *         <code>false</code>on fail connection, Owner will show Dialog<br>
	 *         <code>null</code>Owner will Do nothing<br>
	 *
	 * @throws Exception if something went wrong
	 */
	public boolean testConnection() throws Exception;

	/**
	 * get resources as PathLayer
	 *
	 * @throws Exception if something went wrong
	 */
	public PathLayer connect() throws Exception;

	/**
	 * clear all input fields
	 */
	public void clearAllFields();

	/**
	 * initialize input fields with default inputs to help
	 */
	public void initializeDefaultFields();

	/**
	 * Load FXML corresponding FXML and return main pane
	 *
	 * @return the pane where all input are
	 * @throws IOException
	 */
	public FXMLLoader loadFXML() throws IOException;

	/**
	 * Return main View Pane
	 *
	 * @return the pane where all input are
	 */
	public Pane getViewPane();

	/**
	 *
	 * @return a connection type that can be served by this controller
	 */
	public ConnectionType getConnectionType();

	/**
	 * Will be called before {@link #testConnection()} or {@link #connect()}
	 *
	 * @return <code>true</code> if all input are corrects and proceed to
	 *         connect<br>
	 *         <code>false</code> will do nothing
	 */
	public boolean checkInputValidation();
}
