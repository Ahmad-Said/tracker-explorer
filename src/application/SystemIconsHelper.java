package application;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.filechooser.FileSystemView;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class SystemIconsHelper {

	static HashMap<String, Image> mapOfFileExtToSmallIcon = new HashMap<String, Image>();

	// filePath here is a full path to file i.e path.toString()
	public static Image getFileIcon(String filePath) {
		final String ext = getFileExt(filePath);

		Image fileIcon = mapOfFileExtToSmallIcon.get(ext);
		if (fileIcon == null) {
			javax.swing.Icon jswingIcon = null;

			File file = new File(filePath);
			if (file.exists()) {
				jswingIcon = getJSwingIconFromFileSystem(file);
			} else {
				File tempFile = null;
				try {
					tempFile = File.createTempFile("icon", ext);
					jswingIcon = getJSwingIconFromFileSystem(tempFile);
				} catch (IOException ignored) {
					// Cannot create temporary file.
				} finally {
					if (tempFile != null)
						tempFile.delete();
				}
			}

			if (jswingIcon != null) {
				fileIcon = jswingIconToImage(jswingIcon);
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

		// resolving if a file doesn't have an extention:
		File file = new File(filePath);
		if (file.isFile() && ext == ".")
			ext = ".SomethingNotAnextention";

		// resolving same name extetion with different icon
		if (Setting.getLoadAllIcon()) {
			if (ext.equals(".exe") || ext.equals(".lnk"))
				ext = file.getName();

			if (file.isDirectory()) {
				// check if directory have ini file for icon
				File iniCheck = new File(filePath + ("\\desktop.ini"));
				if (iniCheck.exists())
					ext = file.getName();
			}
		}
		return ext.toLowerCase();
	}

	private static javax.swing.Icon getJSwingIconFromFileSystem(File file) {

		FileSystemView view = FileSystemView.getFileSystemView();
		javax.swing.Icon icon = view.getSystemIcon(file);

		return icon;
	}

	private static Image jswingIconToImage(javax.swing.Icon jswingIcon) {
		BufferedImage bufferedImage = new BufferedImage(jswingIcon.getIconWidth(), jswingIcon.getIconHeight(),
				BufferedImage.TYPE_INT_ARGB);
		jswingIcon.paintIcon(null, bufferedImage.getGraphics(), 0, 0);
		return SwingFXUtils.toFXImage(bufferedImage, null);
	}
}
