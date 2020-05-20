package said.ahmad.javafx.tracker.app;

import java.io.InputStream;
import java.net.URL;

public class ResourcesHelper {
	private static final ResourcesHelper RESOURCE_INSTANCE = new ResourcesHelper();
	private static final String PACKAGE_SCOOP = "/said/ahmad/javafx/tracker";

	/** Parameter in the form /fxml/Some.fxml or /img/Some.png */
	public static URL getResourceAsURL(String resourcesName) {
		return RESOURCE_INSTANCE.getClass().getResource(PACKAGE_SCOOP + resourcesName);
	}

	/** Parameter in the form /fxml/Some.fxml or /img/Some.png */
	public static InputStream getResourceAsStream(String resourcesName) {
		return RESOURCE_INSTANCE.getClass().getResourceAsStream(PACKAGE_SCOOP + resourcesName);
	}

	public static String getResourceAsString(String resourcesName) {
		return PACKAGE_SCOOP + resourcesName;
	}
}
