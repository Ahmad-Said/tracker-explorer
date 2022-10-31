package said.ahmad.javafx.tracker.system.file.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.jetbrains.annotations.Nullable;

import said.ahmad.javafx.tracker.datatype.ConnectionAccount;
import said.ahmad.javafx.tracker.system.file.util.PathString;

public class FTPClientExt {
	private String server;
	private int serverPort;
	private String username;
	private String password;
	private FTPClient ftpLister;
	private FTPClient ftpDataHandler;
	boolean isMLSDSupported = false;
	boolean isMDTMSupported = false;
	// a map contain all valid from HostNames to FTPClients
	private static final HashMap<ConnectionAccount, FTPClientExt> accountsToFTPClients = new HashMap<>();

	/**
	 * used to cache FTPFile after {@link #listFiles()}, so later on when asking
	 * again for a specific file in it using {@link #mlistFile(String)} <br>
	 * the server don't have to be asked again<br>
	 * Structure: <br>
	 *
	 * [DirPath -> [filename -> FTPFile] ]
	 */
	private final LinkedHashMap<String, HashMap<String, FTPFile>> cachedFTPFile = new LinkedHashMap<>();
	private final LinkedHashMap<String, List<FTPFile>> cachedFTPList = new LinkedHashMap<>();
	// every new list that beyond this number, the first listing will be removed
	private static final int MAXIMUM_FTP_CACHE_ALLOWED = 1000;

	/**
	 *
	 * @param server
	 * @param serverPort
	 * @param username     may be null
	 * @param password
	 * @throws Exception
	 */
	public FTPClientExt(String server, int serverPort, String username, String password) throws Exception {
		if (username == null || username.isEmpty()) {
			username = "anonymous";
			password = "";
		}
		ftpLister = new FTPClient();
		ftpDataHandler = new FTPClient();
		connect(server, serverPort, username, password);
	}

	public void connect(String server, int serverPort, String username, String password) throws IOException {
		disconnect();
		this.server = server;
		this.serverPort = serverPort;
		this.username = username;
		this.password = password;
		initializeClient(ftpLister, FTP.ASCII_FILE_TYPE);
//		initializeClient(ftpDataHandler, FTP.BINARY_FILE_TYPE); // initialize ftpData on each request
		accountsToFTPClients.put(new ConnectionAccount("ftp", username, password, server, serverPort, null), this);
		for (String command : ftpLister.listHelp().split(" ")) {
			switch (command.trim().toUpperCase()) {
			case "MLSD":
				isMLSDSupported = true;
				break;
			case "MDTM":
				isMDTMSupported = true;
				break;
			default:
				break;
			}
		}
	}

	private void initializeClient(FTPClient ftpClient, int fileType) throws SocketException, IOException {
		int reply;
		ftpClient.setAutodetectUTF8(true);
		ftpClient.connect(server, serverPort);
		reply = ftpClient.getReplyCode();
		if (!FTPReply.isPositiveCompletion(reply)) {
			ftpClient.disconnect();
			throw new IOException(
					"Could not connect to remote host reply:\n " + Arrays.asList(ftpClient.getReplyStrings()));
		}
		ftpClient.login(username, password);
		reply = ftpClient.getReplyCode();
		if (!FTPReply.isPositiveCompletion(reply)) {
			ftpClient.disconnect();
			throw new IOException("Failed to login.\n\t" + Arrays.asList(ftpClient.getReplyStrings()));
		}
		ftpClient.setFileType(fileType);
		ftpClient.enterLocalPassiveMode();
	}

	public void disconnect() throws IOException {
		synchronized (ftpLister) {
			ftpLister.disconnect();
		}
		synchronized (ftpDataHandler) {
			ftpDataHandler.disconnect();
		}
	}

	public boolean isConnected() {
		return ftpLister.isConnected();
	}

	private void reconnectFTPLister() throws IOException {
		if (!ftpLister.isConnected()) {
			initializeClient(ftpLister, FTP.ASCII_FILE_TYPE);
		}
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	public String getServer() {
		return server;
	}

	public String getAbsolutePathPrefix() {
		if (username == null) {
			// anonymous login
			return "ftp://" + getServer() + ":" + getServerPort();
		} else {
			return "ftp://" + username + ":" + password + "@" + getServer() + ":" + getServerPort();
		}
	}

	/**
	 * @return the hostnamesToFTPClients
	 */
	public static HashMap<ConnectionAccount, FTPClientExt> getAccountsToFTPClients() {
		return accountsToFTPClients;
	}

	public static FTPClientExt getClientFromURI(URI uri) {
		return accountsToFTPClients.get(new ConnectionAccount(uri));
	}

	public static FTPClientExt getClientFromAccount(ConnectionAccount account) {
		return accountsToFTPClients.get(account);
	}

	public List<FTPFile> listFiles(String pathname) throws IOException {
		FTPFile[] ftpFileList = null;
		// caching causing problem as refresh is not done after copying/pasting files
//		if (cachedFTPFile.containsKey(pathname)) {
//			return cachedFTPList.get(pathname);
//		}
		synchronized (ftpLister) {
			reconnectFTPLister();
			ftpLister.changeWorkingDirectory(pathname);
			ftpLister.setFileType(FTP.ASCII_FILE_TYPE);
			if (isMLSDSupported) {
				ftpFileList = ftpLister.mlistDir();
			} else {
				ftpFileList = ftpLister.listFiles();
			}
			addCachedList(pathname, ftpFileList);
		}
		return Arrays.asList(ftpFileList);
	}

	private void addCachedList(String parent, FTPFile[] ftpFileList) {
		HashMap<String, FTPFile> fileNameToFTP = new HashMap<>();
		for (FTPFile ftpFile : ftpFileList) {
			fileNameToFTP.put(ftpFile.getName(), ftpFile);
		}
		cachedFTPFile.put(parent, fileNameToFTP);
		cachedFTPList.put(parent, Arrays.asList(ftpFileList));
		if (cachedFTPFile.size() > MAXIMUM_FTP_CACHE_ALLOWED) {
			cachedFTPFile.remove(cachedFTPFile.keySet().iterator().next());
		}
	}

	@Nullable
	public FTPFile getFileOrCached(String pathname) throws IOException {
		FTPFile ftpFile = null;
		PathString pathString = new PathString(pathname);
		String parent = pathString.getParent();
		if (pathname.equals("/")) {
			ftpFile = new FTPFile();
			ftpFile.setName("/");
			ftpFile.setType(FTPFile.DIRECTORY_TYPE);
			return ftpFile;
		}
		if (!cachedFTPFile.containsKey(parent)) {
			listFiles(parent);
		}
		ftpFile = getCachedFTPFile(parent, pathString.getName());
		return ftpFile;
	}

	public boolean checkFileExistance(String path, boolean isDirectory) {
		// we can't use cached version
		boolean exist = false;
		synchronized (ftpLister) {
			try {
				reconnectFTPLister();
				if (path.equals("/")) {
					exist = true;
				} else if (isDirectory) {
					exist = ftpLister.changeWorkingDirectory(path);
				} else
				// target is a file
				if (isMDTMSupported) {
					exist = ftpLister.getModificationTime(path) != null;
				} else {
					PathString pathString = new PathString(path);
					listFiles(pathString.getParent());
					exist = getFileOrCached(path) != null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return exist;
	}

	@Nullable
	private FTPFile getCachedFTPFile(String parent, String fileName) {
		if (cachedFTPFile.containsKey(parent) && cachedFTPFile.get(parent).containsKey(fileName)) {
			return cachedFTPFile.get(parent).get(fileName);
		}
		return null;
	}

	/**
	 * @return the serverPort
	 */
	public int getServerPort() {
		return serverPort;
	}

	private void reconnectFTPDataHandlerAsNew() throws SocketException, IOException {
		ftpDataHandler.disconnect();
		initializeClient(ftpDataHandler, FTP.BINARY_FILE_TYPE);
	}

	private void reconnectFTPDataHandler() throws SocketException, IOException {
		if (!ftpDataHandler.isConnected()) {
			initializeClient(ftpDataHandler, FTP.BINARY_FILE_TYPE);
		}
	}

	private void clearParentPathCache(String pathName) {
		String parent = PathString.getParent(pathName);
		cachedFTPFile.remove(parent);
		cachedFTPList.remove(parent);
	}

	public void makeDirectory(String pathname) throws IOException {
		synchronized (ftpDataHandler) {
			reconnectFTPDataHandler();
			clearParentPathCache(pathname);
			ftpDataHandler.makeDirectory(pathname);
		}
	}

	public boolean deleteFile(String pathname) throws IOException {
		synchronized (ftpDataHandler) {
			reconnectFTPDataHandler();
			clearParentPathCache(pathname);
			return ftpDataHandler.deleteFile(pathname);
		}
	}

	public InputStream retrieveFileStream(String remote) throws IOException {
		synchronized (ftpDataHandler) {
			reconnectFTPDataHandlerAsNew();
			return ftpDataHandler.retrieveFileStream(remote);
		}
	}

	public OutputStream appendFileStream(String remote) throws IOException {
		synchronized (ftpDataHandler) {
			reconnectFTPDataHandlerAsNew();
			return ftpDataHandler.appendFileStream(remote);
		}
	}

	public OutputStream storeFileStream(String remote) throws IOException {
		synchronized (ftpDataHandler) {
			reconnectFTPDataHandlerAsNew();
			return ftpDataHandler.storeFileStream(remote);
		}
	}

	public void completePendingCommand() throws IOException {
		synchronized (ftpDataHandler) {
			ftpDataHandler.completePendingCommand();
		}
	}
}
