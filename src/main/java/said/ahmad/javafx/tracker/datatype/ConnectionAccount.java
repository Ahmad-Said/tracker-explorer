package said.ahmad.javafx.tracker.datatype;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * What define an account is scheme, host, port, username, and password<BR>
 * And on that hashCode is generated {@link #hashCode()}
 *
 */
public class ConnectionAccount {
	private String scheme;
	private String username;
	private String password;
	private String host;
	private Integer port;
	private String path;

	/**
	 * null fields
	 */
	public ConnectionAccount() {

	}

	public ConnectionAccount(String scheme, String username, String password, String host, int port, String path) {
		this.scheme = scheme;
		this.username = username;
		this.password = password;
		this.host = host;
		this.port = port;
		this.path = path;
	}

	public ConnectionAccount(URI uri) {
		String username = null;
		String password = null;
		String userInfo = uri.getUserInfo();
		if (userInfo != null) {
			String splitted[] = userInfo.split(":");
			username = splitted[0];
			if (splitted.length > 1) {
				password = splitted[1];
			}
		}
		scheme = uri.getScheme().toLowerCase();
		host = uri.getHost();
		this.username = username;
		this.password = password;
		port = uri.getPort() != -1 ? uri.getPort() : null;
		path = uri.getPath();
	}

	public URI toURI() throws URISyntaxException {
		String userInfo = null;
		if (username != null && !username.isEmpty()) {
			userInfo = username;
			if (password != null && !password.isEmpty()) {
				userInfo += ":" + password;
			}
		}
		return new URI(scheme, userInfo, host, port, path, null, null);
	}

	@Override
	public int hashCode() {
		return getkey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ConnectionAccount) {
			return getkey().equals(((ConnectionAccount) obj).getkey());
		}
		return false;
	}

	private String getkey() {
		return scheme + host + port + username + password;
	}

	/**
	 * @return the scheme
	 */
	public String getScheme() {
		return scheme;
	}

	/**
	 * @param scheme the scheme to set
	 */
	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the port
	 */
	public Integer getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(Integer port) {
		this.port = port;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}
}
