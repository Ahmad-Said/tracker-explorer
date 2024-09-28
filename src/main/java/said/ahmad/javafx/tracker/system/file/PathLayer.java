package said.ahmad.javafx.tracker.system.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.io.Util;

import said.ahmad.javafx.tracker.app.StringHelper;
import said.ahmad.javafx.tracker.app.pref.Setting;
import said.ahmad.javafx.tracker.system.file.ftp.FTPPathLayer;
import said.ahmad.javafx.tracker.system.file.local.FilePathLayer;

/**
 * A Layer isolation class to manage files from different source providers<br>
 * Any change done by method only affect object variable not real value
 * attributes
 *
 * @author user
 *
 */
@JsonSerialize(using = PathLayerSerializer.class)
@JsonDeserialize(using = PathLayerDeserializer.class)
public abstract class PathLayer {
	public static enum FileType {
		NONE, DIRECTORY, FILE;

		public boolean isDirectory() {
			return equals(DIRECTORY);
		}

		public boolean isAFile() {
			return equals(FILE);
		}
	}

	private static DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a");
	private static long allowedCopySizeInBytes = 10 * 1024 * 1024; // 10 MB by default

	private static HashMap<PathLayer, FilePathLayer> cachedCopies = new HashMap<>();
	private String absolutePath, name;
	private ProviderType providerType;
	private FileType pathType;
	private long size;
	/**
	 * A <code>long</code> value representing the time the file was last modified,
	 * measured in milliseconds since the epoch (00:00:00 GMT, January 1, 1970), or
	 * <code>0L</code>
	 */
	private long dateModified;

	/*** Creates an empty MyPath. ***/
	public PathLayer() {
		name = "";
		absolutePath = "";
		pathType = FileType.NONE;
		size = 0; // 0 is valid, so use -1
		dateModified = 0;
		providerType = ProviderType.NONE;
	}

	/**
	 *
	 * @param absolutePath  must be unique in same or different provider
	 * @param name          of file
	 * @param providerType  of file
	 * @param fileType      of file
	 * @param dateModified    in milliseconds
	 * @param size 		in bytes
	 */
	public PathLayer(String absolutePath, String name, ProviderType providerType, FileType fileType, long dateModified,
			long size) {
		this.absolutePath = absolutePath;
		this.name = name.isEmpty() ? absolutePath : name;
		this.providerType = providerType;
		pathType = fileType;
		this.dateModified = dateModified;
		this.size = size; // 0 is valid, so use -1
	}

	// ------------------------ Attributes Getters And Setter -------------------
	/***
	 * Determine if the file is a directory.
	 *
	 * @return True if the file is of type <code>DIRECTORY_TYPE</code>, false if
	 *         not.
	 ***/
	public boolean isDirectory() {
		return pathType.isDirectory();
	}

	/***
	 * Determine if the file is a regular file.
	 *
	 * @return True if the file is of type <code>FILE_TYPE</code>, false if not.
	 ***/
	public boolean isFile() {
		return pathType.isAFile();
	}

	/**
	 * @return the path
	 */
	public String getAbsolutePath() {
		return absolutePath;
	}

	/**
	 * @param absolutePath the absolutePath to set
	 */
	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}

	/**
	 * Set the file size in bytes.
	 *
	 * @param size The file size in bytes.
	 */
	public void setSize(long size) {
		this.size = size;
	}

	/***
	 * Return the file size in bytes.
	 *
	 * @return The file size in bytes.
	 ***/
	public long getSize() {
		return size;
	}

	/**
	 * @return Name of denoted Path
	 */
	public String getName() {
		return name;
	};

	public String getExtensionUPPERCASE() {
		return StringHelper.getExtention(name);
	};

	/**
	 *
	 * @return
	 * @see FilenameUtils#getExtension
	 */
	public String getExtension() {
		return FilenameUtils.getExtension(name);
	};

	public String getBaseName() {
		return StringHelper.getBaseName(name);
	};

	/**
	 * Set name
	 *
	 * @param name of file
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 *
	 * @return A <code>long</code> value representing the time the file was last
	 *         modified, measured in milliseconds since the epoch (00:00:00 GMT,
	 *         January 1, 1970), or <code>0L</code>
	 */
	public long getDateModified() {
		return dateModified;
	}

	/**
	 * Change last modified variable
	 *
	 * @param dateModified
	 */
	public void setDateModified(long dateModified) {
		this.dateModified = dateModified;
	}

	/**
	 * @return the providerType
	 */
	public ProviderType getProviderType() {
		return providerType;
	}

	/**
	 *
	 * @return True for file in local system
	 */
	public boolean isLocal() {
		return providerType.isLocal();
	}

	public boolean isWebDav() {
		return providerType.isWebDav();
	}

	public boolean isFTP() {
		return providerType.isFTP();
	}

	public boolean isOnNetwork() {
		return providerType.isOnNetwork();
	}

	/**
	 * @return the dateFormat
	 */
	public static DateFormat getDateFormat() {
		return dateFormat;
	}

	/**
	 * @param dateFormat the dateFormat to set
	 */
	public static void setDateFormat(DateFormat dateFormat) {
		PathLayer.dateFormat = dateFormat;
	}

	public String getDateModifiedFormatted() {
		return dateFormat.format(new Date(dateModified));
	}

	/**
	 * @return the allowedCopySizeInBytes
	 */
	public static long getAllowedCopySizeInBytes() {
		return allowedCopySizeInBytes;
	}

	/**
	 * @param allowedCopySizeInBytes the allowedCopySizeInBytes to set
	 */
	public static void setAllowedCopySizeInBytes(long allowedCopySizeInBytes) {
		PathLayer.allowedCopySizeInBytes = allowedCopySizeInBytes;
	}

	// --------------- Operation implementation -----------------
	/**
	 * @return null if this {@code PathLayer} is not associated with {@code Path}
	 */
	public abstract Path toPath();

	/**
	 * Returns the number of name elements in the path.Returns:the number of
	 * elements in the path, or 0 if this path only represents a root component
	 *
	 */
	public abstract int getNameCount();

	/**
	 * @return File: Use with caution File only exist if {@code PathLayer} is
	 *         {@link #isLocal()}, {@code null} otherwise
	 * @see #toFileIfLocalOrAsCopy(boolean)
	 */
	public abstract File toFileIfLocal();

	/**
	 * @param useCachedCopy Use recently copied file, otherwise will copy file to
	 *                      temporary directory and cache it
	 * @return a copy of file in temporary location <br>
	 *         Used to open network location files
	 * @throws IOException
	 */
	public FilePathLayer ToFileTemporaryAsCopy(boolean useCachedCopy) throws IOException {
		FilePathLayer cachedCopy = cachedCopies.get(this);
		if (useCachedCopy && cachedCopy != null && cachedCopy.exists()) {
			return cachedCopy;
		} else {
			FilePathLayer copyFile = new FilePathLayer(File.createTempFile(getName(), getExtensionUPPERCASE()));
			copy(copyFile);
			cachedCopies.put(this, copyFile);
			copyFile.getFile().deleteOnExit();
			return copyFile;
		}
	};

	/**
	 *
	 * @param useCachedCopy if file is local will use same file, otherwise will copy
	 *                      file to temporary directory
	 * @return
	 * @throws IOException
	 */
	public FilePathLayer toFileIfLocalOrAsCopy(boolean useCachedCopy) throws IOException {
		if (isLocal()) {
			return (FilePathLayer) this;
		} else {
			return ToFileTemporaryAsCopy(useCachedCopy);
		}
	}

	/**
	 * @return null if this {@code PathLayer} cannot be converted {@code URI}
	 */
	public abstract URI toURI();

	/** @return parent as new PathLayer or null if it's a root */
	public abstract PathLayer getParentPath();

	/**
	 * @return parent as absolute path String or empty string if it's a root <br>
	 *         Will not create a new PathLayer while returning parent, it just split
	 *         absolute path string using file separator that depend on each system
	 *         provider
	 */
	public abstract String getParent();

	/**
	 * @throws IOException
	 * @see File#mkdirs()
	 */
	public abstract void mkdirs() throws IOException;

	/** @return empty list if denoted path wasn't a directory */
	public abstract List<PathLayer> listPathLayers() throws IOException;

	/** @return empty list if denoted path wasn't a directory */
	public abstract List<PathLayer> listNoHiddenPathLayers() throws IOException;

	public abstract boolean exists();

	/** @return this (same path) */
	public abstract PathLayer createNewAsFile() throws IOException;

	/** @return this (same path) */
	public abstract PathLayer createNewAsDirectory() throws IOException;

	public abstract boolean isHidden();

	/**
	 * @return {@code true} if pathLayer support setting hidden, other wise return
	 *         {@code false} and does nothing
	 */
	public abstract boolean setHidden(boolean isHidden) throws IOException;

	/**
	 * Delete file or directory if it was empty
	 *
	 * @return if delete was successful
	 *
	 * @see File#delete()
	 */
	public abstract boolean delete() throws IOException;

	/**
	 * Delete file or directory recursively
	 *
	 * @see FileUtils#deleteDirectory(File)
	 * @see FileUtils#forceDelete(File)
	 */
	public abstract void deleteForcefully() throws IOException;

	public boolean copyTo(PathLayer targetDirectory) throws IOException {
		PathLayer targetFile = targetDirectory.resolve(getName());
		return copy(targetFile);
	}

	public boolean moveTo(PathLayer targetDirectory) throws IOException {
		boolean didCopy = copyTo(targetDirectory);
		if (didCopy) {
			delete();
		}
		return didCopy;
	}

	public boolean move(PathLayer targetPathLayer) throws IOException {
		boolean didCopy = copy(targetPathLayer);
		if (didCopy) {
			delete();
		}
		return didCopy;
	}

	public boolean copy(PathLayer targetPathLayer) throws IOException {
		InputStream input = getInputFileStream();
		OutputStream output = targetPathLayer.getOutputFileStream();
		if (input == null || output == null) {
			return false;
		}
		Util.copyStream(input, output);
		input.close();
		output.close(); // Must call
		FTPPathLayer sourceFTP = null;
		if (this instanceof FTPPathLayer) {
			sourceFTP = (FTPPathLayer) this;
			sourceFTP.completePendingCommand();
		}
		return true;
	}

	public abstract WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) throws IOException;

	public abstract PathLayer resolve(String other);

	/**
	 * return same absolute path of {@link #resolve(String)} but without creating a
	 * new PathLayer
	 */
	public abstract String resolveAsString(String other);

	/**
	 * example: <br>
	 * "drive/Spring".resolveSibling("program/workspace") <br>
	 * give = "drive/program/workspace"
	 *
	 * @param other
	 * @return
	 */
	public abstract PathLayer resolveSibling(String other);

	/**
	 * @return file separator used in resolving paths, example <code>"\"for
	 *         windows and <code> "/"</code> for Unix...
	 */
	public abstract String getFILE_SEPARATOR();

	// File Stream stuff
	/** @return {@code null} if unsupported */
	public abstract InputStream getInputFileStream() throws IOException;

	/** @return {@code null} if unsupported */
	public abstract OutputStream getOutputAppendFileStream() throws IOException;

	/** @return {@code null} if unsupported */
	public abstract OutputStream getOutputFileStream() throws IOException;

	// --------------- @Override Section -----------------

	@Override
	public String toString() {
		return absolutePath;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PathLayer) {
			return compareTo((PathLayer) obj) == 0;
		}
		return false;
	}

	public int compareTo(PathLayer other) {
		return other.getAbsolutePath().compareTo(getAbsolutePath());
	}

	@Override
	public int hashCode() {
		return getAbsolutePath().hashCode() ^ 1234321;
	}
}
