package said.ahmad.javafx.tracker.system.file.local;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import org.apache.commons.lang3.SystemUtils;
import said.ahmad.javafx.tracker.app.pref.Setting;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.tracker.system.file.ProviderType;

public class FilePathLayer extends PathLayer {

	/** @see File#separator */
	public final static String FILE_SEPARATOR = File.separator;

	private File file;
	/** Empty constructor */
	public FilePathLayer() {}

	/**
	 * Empty constructor used to create virtual FilePathLayer usually used for
	 * options to be saved in tracker data
	 */
	public FilePathLayer(String OptionName) {
		super();
		setName(OptionName);
		setAbsolutePath("Virtual/TrackerExplorer/" + OptionName);
	}

	public FilePathLayer(File file) {
		super(file.getAbsolutePath(), file.getName(), ProviderType.LOCAL,
				file.isDirectory() ? FileType.DIRECTORY : FileType.FILE, file.lastModified(), file.length());
		this.file = file;
	}

	@Override
	public Path toPath() {
		return file.toPath();
	}

	@Override
	public int getNameCount() {
		return toPath().getNameCount();
	}

	/** return same file as {@link #getFile()} */
	@Override
	public File toFileIfLocal() {
		return getFile();
	}

	@Override
	public URI toURI() {
		return file.toPath().toUri();
	}

	@Override
	public PathLayer getParentPath() {
		File parent = file.getParentFile();
		if (parent != null) {
			return new FilePathLayer(file.getParentFile());
		} else {
			return null;
		}
	}

	@Override
	public String getParent() {
		return file.getParent();
	}

	@Override
	public void mkdirs() {
		file.mkdirs();
	};

	@Override
	public List<PathLayer> listPathLayers() throws IOException {
		List<PathLayer> listPathLayer = new ArrayList<>();
		if (file.isFile()) {
			return listPathLayer;
		}
		for (File element : file.listFiles()) {
			listPathLayer.add(new FilePathLayer(element));
		}
		return listPathLayer;
	}

	@Override
	public List<PathLayer> listNoHiddenPathLayers() throws IOException {
		List<PathLayer> listPathLayer = new ArrayList<>();
		if (file.isFile()) {
			return listPathLayer;
		}
		for (File element : file.listFiles()) {
			if (!element.isHidden()) {
				listPathLayer.add(new FilePathLayer(element));
			}
		}
		return listPathLayer;
	}

	@Override
	public boolean exists() {
		return file.exists();
	}

	@Override
	public PathLayer createNewAsFile() throws IOException {
		Files.createFile(file.toPath());
		return this;
	};

	@Override
	public PathLayer createNewAsDirectory() throws IOException {
		Files.createDirectories(file.toPath());
		return this;
	};

	@Override
	public boolean isHidden() {
		return file.isHidden();
	}

	@Override
	public boolean setHidden(boolean isHidden) throws IOException {
		if (SystemUtils.IS_OS_WINDOWS) {
			Files.setAttribute(file.toPath(), "dos:hidden", true);
		}
		return true;
	}

	@Override
	public boolean delete() throws IOException {
		return file.delete();
	}

	@Override
	public void deleteForcefully() throws IOException {
		if (isDirectory()) {
			FileUtils.deleteDirectory(file);
		} else {
			FileUtils.forceDelete(file);
		}
	}

	@Override
	public boolean copyTo(PathLayer targetDirectory) throws IOException {
		if (targetDirectory.isLocal()) {
			if (isFile()) {
				Files.copy(file.toPath(), targetDirectory.toPath().resolve(getName()),
						com.sun.nio.file.ExtendedCopyOption.INTERRUPTIBLE);
			} else {
				targetDirectory.toPath().resolve(getName()).toFile().mkdirs();
			}
			return true;
		} else {
			return super.copyTo(targetDirectory);
		}
	}

	@Override
	public boolean moveTo(PathLayer targetDirectory) throws IOException {
		if (targetDirectory.isLocal()) {
			return moveTo((FilePathLayer) targetDirectory);
		} else {
			return super.moveTo(targetDirectory);
		}
	}

	public boolean moveTo(FilePathLayer targetDirectory) throws IOException {
		if (isFile()) {
			FileUtils.moveToDirectory(file, targetDirectory.file, true);
		} else {
			Files.createDirectories(targetDirectory.toPath());
		}
		return false;
	}

	@Override
	public boolean move(PathLayer targetPathLayer) throws IOException {
		if (compareTo(targetPathLayer) == 0) {
			// same file require nothing
			return true;
		}
		// target is another file
		if (targetPathLayer.isLocal()) {
			Files.move(toPath(), targetPathLayer.toPath());
			return true;
		} else {
			return super.move(targetPathLayer);
		}
	}

	@Override
	public boolean copy(PathLayer targetPathLayer) throws IOException {
		if (targetPathLayer.isLocal()) {
			Files.copy(file.toPath(), targetPathLayer.toPath(), com.sun.nio.file.ExtendedCopyOption.INTERRUPTIBLE);
			return true;
		} else {
			return super.copy(targetPathLayer);
		}
	}

	@Override
	public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException {
		return toPath().register(watcher, events);
	}

	@Override
	public PathLayer resolve(String other) {
		return new FilePathLayer(toPath().resolve(other).toFile());
	}

	@Override
	public String resolveAsString(String other) {
		return toPath().resolve(other).toString();
	}

	@Override
	public PathLayer resolveSibling(String other) {
		return new FilePathLayer(toPath().resolveSibling(other).toFile());
	}

	/** @see #FILE_SEPARATOR */
	@Override
	public String getFILE_SEPARATOR() {
		return FILE_SEPARATOR;
	}

	@Override
	public InputStream getInputFileStream() throws IOException {
		return new FileInputStream(file);
	}

	@Override
	public OutputStream getOutputAppendFileStream() throws IOException {
		return new FileOutputStream(file, true);
	}

	@Override
	public OutputStream getOutputFileStream() throws IOException {
		return new FileOutputStream(file);
	}

	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @param file the file to set
	 */
	public void setFile(File file) {
		this.file = file;
	}
}
