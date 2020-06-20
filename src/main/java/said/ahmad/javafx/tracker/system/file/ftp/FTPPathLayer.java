package said.ahmad.javafx.tracker.system.file.ftp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTPFile;
import org.jetbrains.annotations.Nullable;

import said.ahmad.javafx.tracker.datatype.ConnectionAccount;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.tracker.system.file.ProviderType;
import said.ahmad.javafx.tracker.system.file.local.FilePathLayer;
import said.ahmad.javafx.tracker.system.file.util.PathString;

public class FTPPathLayer extends PathLayer {

	/** @see File#separator */
	public final static String FILE_SEPARATOR = "/";

	/** @see FTPFile#FILE_TYPE */
	public static FileType getFTPFileType(FTPFile file) {
		if (file.isFile()) {
			return FileType.FILE;
		} else if (file.isDirectory()) {
			return FileType.DIRECTORY;
		}
		return FileType.NONE;
	}

	private FTPClientExt ftpClient;
	// Caution There is no Local System Provider for this path
	private PathString path;

	/** Create Empty NONE type of {@link FTPPathLayer} with no details */
	public FTPPathLayer(FTPClientExt ftpClient, String name, String path) {
		super(ftpClient.getAbsolutePathPrefix() + path, name, ProviderType.FTP, FileType.NONE, 0, 0);
		this.ftpClient = ftpClient;
		this.path = new PathString(path);
	}

	public FTPPathLayer(FTPClientExt ftpClient, String name, String path, FileType fileType, long lastModified,
			long size) {
		super(ftpClient.getAbsolutePathPrefix() + path, name, ProviderType.FTP, fileType, lastModified, size);
		this.ftpClient = ftpClient;
		this.path = new PathString(path);
		setAbsolutePath(ftpClient.getAbsolutePathPrefix() + path);
		setName(name);
	}

	/**
	 *
	 * @param ftpClient
	 * @param file      at least name is required
	 * @param path      from root example /pathToDir/subDir/filename.mp4
	 *
	 */
	public FTPPathLayer(FTPClientExt ftpClient, FTPFile file, String path) {
		super(ftpClient.getAbsolutePathPrefix() + path, file.getName(), ProviderType.FTP, getFTPFileType(file),
				file.getTimestamp() != null ? file.getTimestamp().getTimeInMillis() : 0, file.getSize());
		this.ftpClient = ftpClient;
		this.path = new PathString(path);
	}

	/**
	 * There is no System Provider associated with this path
	 *
	 * @return null
	 */
	@Override
	@Nullable
	public Path toPath() {
		return null;
	}

	@Override
	public int getNameCount() {
		return path.getNameCount();
	}

	/**
	 * @return null
	 */
	@Override
	@Nullable
	public File toFileIfLocal() {
		return null;
	}

	/**
	 * Format example
	 * ftp://username:password@hostIPAddress:PortNumber/pathToDir/subdir/filename.mp4
	 *
	 * @see https://en.wikipedia.org/wiki/Uniform_Resource_Identifier
	 */
	@Override
	public URI toURI() {
		try {
			return new URI("ftp", ftpClient.getUsername() + ":" + ftpClient.getPassword(), ftpClient.getServer(),
					ftpClient.getServerPort(), path.get(), null, null);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Nullable
	public static FTPPathLayer parseURI(URI uri) {
		FTPPathLayer file = null;
		ConnectionAccount account = new ConnectionAccount(uri);
		FTPClientExt ftpClient = FTPClientExt.getClientFromAccount(account);
		String path = uri.getPath() == null ? "/" : uri.getPath();
		try {
			if (ftpClient == null) {
				ftpClient = new FTPClientExt(uri.getHost(), uri.getPort() == -1 ? 21 : uri.getPort(),
						account.getUsername(), account.getPassword());
			}
			FTPFile ftpFile = ftpClient.getFileOrCached(path);
			if (ftpFile != null) {
				file = new FTPPathLayer(ftpClient, ftpFile, path);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return file;
	}

	@Override
	public PathLayer getParentPath() {
		String parentPath = path.getParent();
		try {
			return parentPath != null ? new FTPPathLayer(ftpClient, ftpClient.getFileOrCached(parentPath), parentPath)
					: null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getParent() {
		String parent = path.getParent();
		return parent != null ? ftpClient.getAbsolutePathPrefix() + parent : null;
	}

	@Override
	public void mkdirs() throws IOException {
		ftpClient.makeDirectory(path.get());
	}

	@Override
	public List<PathLayer> listPathLayers() throws IOException {
		List<FTPFile> listFTPFile = ftpClient.listFiles(path.get());
		List<PathLayer> listPaths = new ArrayList<>();
		for (FTPFile file : listFTPFile) {
			String otherPath = path.resolve(file.getName());
			listPaths.add(new FTPPathLayer(ftpClient, file, otherPath));
		}
		return listPaths;
	}

	@Override
	public List<PathLayer> listNoHiddenPathLayers() throws IOException {
		List<FTPFile> listFTPFile = ftpClient.listFiles(path.get());
		List<PathLayer> listPaths = new ArrayList<>();
		for (FTPFile file : listFTPFile) {
			if (!file.getName().startsWith(".")) {
				String otherPath = path.resolve(file.getName());
				listPaths.add(new FTPPathLayer(ftpClient, file, otherPath));
			}
		}
		return listPaths;
	}

	@Override
	public boolean exists() {
		return ftpClient.checkFileExistance(path.get(), isDirectory());
	}

	@Override
	public PathLayer createNewAsFile() throws IOException {
		FilePathLayer file = new FilePathLayer(File.createTempFile("tempFile", ".tmp"));
		System.out.println(file);
		System.out.println(this);
		file.move(this);
		return this;
	}

	@Override
	public PathLayer createNewAsDirectory() throws IOException {
		ftpClient.makeDirectory(path.get());
		return this;
	}

	@Override
	public boolean isHidden() {
		return getName().startsWith(".");
	}

	@Override
	public boolean setHidden(boolean isHidden) throws IOException {
		return false;
	}

	@Override
	public boolean delete() throws IOException {
		return ftpClient.deleteFile(path.get());
	}

	@Override
	public void deleteForcefully() throws IOException {
		delete();
	}

	@Override
	public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public FTPFile getEmptyFTPFile(String name) {
		FTPFile file = new FTPFile();
		file.setName(name);
		return new FTPFile();
	}

	@Override
	public PathLayer resolve(String other) {
		FTPFile otherFile = null;
		String otherPath = path.resolve(other);
		try {
			otherFile = ftpClient.getFileOrCached(otherPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (otherFile != null) {
			return new FTPPathLayer(ftpClient, otherFile, otherPath);
		} else {
			return new FTPPathLayer(ftpClient, other, otherPath);
		}
	}

	@Override
	public String resolveAsString(String other) {
		return ftpClient.getAbsolutePathPrefix() + path.resolve(other);
	}

	@Override
	public PathLayer resolveSibling(String other) {
		return getParentPath() != null ? getParentPath().resolve(other) : resolve(other);
	}

	@Override
	public String getFILE_SEPARATOR() {
		return FILE_SEPARATOR;
	}

	@Override
	public InputStream getInputFileStream() throws IOException {
		return ftpClient.retrieveFileStream(path.get());
	}

	@Override
	public OutputStream getOutputAppendFileStream() throws IOException {
		return ftpClient.appendFileStream(path.get());
	}

	@Override
	public OutputStream getOutputFileStream() throws IOException {
		return ftpClient.storeFileStream(path.get());
	}

	public void completePendingCommand() throws IOException {
		ftpClient.completePendingCommand();
	}

}
