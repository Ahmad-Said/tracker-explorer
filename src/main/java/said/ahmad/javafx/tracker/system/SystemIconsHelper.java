package said.ahmad.javafx.tracker.system;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.filechooser.FileSystemView;

import org.jetbrains.annotations.Nullable;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import said.ahmad.javafx.tracker.app.pref.Setting;
import said.ahmad.javafx.tracker.system.file.PathLayer;

public class SystemIconsHelper {

	/**
	 * extension are stored in UpperCase <br>
	 *
	 * @see {@link #getFileExt(String)}
	 */
	static HashMap<String, Image> mapOfFileExtToSmallIcon = new HashMap<String, Image>();
	static int id = 1;

	public static enum IconsExtensions {
		DIRECTORY, FILE_UNKOWN;
	};

	/**
	 *
	 * @param extension TXT or JPEG as for example
	 * @param imageIcon
	 */
	public static void addFileIcon(String extension, Image imageIcon) {
		mapOfFileExtToSmallIcon.put(extension.toLowerCase(), imageIcon);
	}

	@Nullable
	public static Image getFileIconIfCached(PathLayer filePath) {
		return mapOfFileExtToSmallIcon.get(getFileExt(filePath));
	}

	// filePath here is a full path to file i.e path.toString()
	public static Image getFileIcon(PathLayer filePath) {
		final String ext = getFileExt(filePath);

		Image fileIcon = mapOfFileExtToSmallIcon.get(ext);
		if (fileIcon == null) {
			if (filePath.isLocal() && filePath.exists()) {
				javax.swing.Icon jSwingIcon = null;
				jSwingIcon = getJSwingIconFromFileSystem(filePath.toFileIfLocal());
				if (jSwingIcon != null) {
					fileIcon = jSwingIconToImage(jSwingIcon);
					mapOfFileExtToSmallIcon.put(ext, fileIcon);
				}
			} else {
				return getFileIcon(ext);
			}

		}
		return fileIcon;
	}

	/**
	 * Use {@link #getFileIcon(PathLayer)} if possible for better processing
	 *
	 * @param extension
	 * @return
	 */
	public static Image getFileIcon(String extension) {
		Image fileIcon = mapOfFileExtToSmallIcon.get(extension.toUpperCase());
		if (fileIcon == null) {
			File tempFile = null;
			javax.swing.Icon jSwingIcon = null;
			try {
				if (extension.equals(IconsExtensions.DIRECTORY.toString())) {
					// get icon of any directory
					tempFile = Setting.SETTING_DIRECTORY;
				} else {
					tempFile = File.createTempFile("icon", "." + extension);
				}
				jSwingIcon = getJSwingIconFromFileSystem(tempFile);
			} catch (IOException ignored) {
				// ignore Cannot create temporary file.
			} finally {
				if (tempFile != null) {
					tempFile.delete();
				}
			}
			if (jSwingIcon != null) {
				fileIcon = jSwingIconToImage(jSwingIcon);
				mapOfFileExtToSmallIcon.put(extension.toUpperCase(), fileIcon);
			}
		}
		// if file icon still null
		if (fileIcon == null) {
			fileIcon = mapOfFileExtToSmallIcon.get(IconsExtensions.FILE_UNKOWN.toString());
		}
		return fileIcon;
	}

	private static String getFileExt(PathLayer filePath) {
		String ext = filePath.getExtensionUPPERCASE();
		if (filePath.isDirectory()) {
			ext = IconsExtensions.DIRECTORY.toString();
		} else if (ext.isEmpty()) {
			ext = IconsExtensions.FILE_UNKOWN.toString();
		}
		if (filePath.isLocal()) {
			File file = filePath.toFileIfLocal();
			// resolving local files same name extension with different icon
			if (Setting.getLoadAllIcon() && !filePath.getAbsolutePath().startsWith("\\\\")) {
				if (ext.equals("EXE") || ext.equals("LNK")) {
					ext = file.getName();
				}

				if (file.isDirectory()) {
					// check if directory have ini file for icon
					File iniCheck = new File(filePath + "\\desktop.ini");
					if (iniCheck.exists()) {
						ext = file.getName();
					} else {
						// check if it was a root
						if (file.toPath().getNameCount() == 0) {
							ext = "ROOT" + id++;
						}
					}
				}
			}
		}
		return ext;
	}

	private static javax.swing.Icon getJSwingIconFromFileSystem(File file) {

		FileSystemView view = FileSystemView.getFileSystemView();
		javax.swing.Icon icon = view.getSystemIcon(file);
		return icon;
	}

	private static Image jSwingIconToImage(javax.swing.Icon jSwingIcon) {
		BufferedImage bufferedImage = new BufferedImage(jSwingIcon.getIconWidth(), jSwingIcon.getIconHeight(),
				BufferedImage.TYPE_INT_ARGB);
		jSwingIcon.paintIcon(null, bufferedImage.getGraphics(), 0, 0);
		return SwingFXUtils.toFXImage(bufferedImage, null);
	}
}
