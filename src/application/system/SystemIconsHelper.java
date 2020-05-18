package application.system;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.filechooser.FileSystemView;

import org.jetbrains.annotations.Nullable;

import application.datatype.Setting;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class SystemIconsHelper {

	static HashMap<String, Image> mapOfFileExtToSmallIcon = new HashMap<String, Image>();
	static int id = 1;

	@Nullable
	public static Image getFileIconIfCached(String filePath) {
		return mapOfFileExtToSmallIcon.get(getFileExt(filePath));
	}

	// filePath here is a full path to file i.e path.toString()
	public static Image getFileIcon(String filePath) {
		final String ext = getFileExt(filePath);

		Image fileIcon = mapOfFileExtToSmallIcon.get(ext);
		if (fileIcon == null) {
			javax.swing.Icon jSwingIcon = null;

			File file = new File(filePath);
			if (file.exists()) {
				jSwingIcon = getJSwingIconFromFileSystem(file);
			} else {
				File tempFile = null;
				try {
					tempFile = File.createTempFile("icon", ext);
					jSwingIcon = getJSwingIconFromFileSystem(tempFile);
				} catch (IOException ignored) {
					// Cannot create temporary file.
				} finally {
					if (tempFile != null) {
						tempFile.delete();
					}
				}
			}

			if (jSwingIcon != null) {
				fileIcon = jSwingIconToImage(jSwingIcon);
				mapOfFileExtToSmallIcon.put(ext, fileIcon);
			}
		}

		return fileIcon;
	}

	public static String getFileExt(String filePath) {
		String ext = ".";
		int p = filePath.lastIndexOf('.');
		if (p >= 0) {
			ext = filePath.substring(p);
		}

		// resolving if a file doesn't have an extension:
		File file = new File(filePath);
		if (file.isFile() && ext == ".") {
			ext = ".SomethingNotAnextention";
		} else if (file.isDirectory()) {
			ext = ".SomethingADirectory";
		}

		// resolving local files same name extension with different icon
		if (Setting.getLoadAllIcon() && !filePath.startsWith("\\\\")) {
			if (ext.equals(".exe") || ext.equals(".lnk")) {
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
						ext = "root" + id++;
					}
				}
			}
		}
		return ext.toLowerCase();
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
