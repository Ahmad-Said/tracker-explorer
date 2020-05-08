package application.system.call;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import application.StringHelper;

/**
 * https://codesector.kayako.com/article/2-command-line
 *
 * @author user
 *
 */
public class TeraCopy {
	private static Path Path_Setup = null;
	private static String FILE_NAME = "TeraCopy.exe";

	public static boolean isWellSetup() {
		return Path_Setup != null && Path_Setup.toFile().exists()
				&& Path_Setup.getFileName().toString().equals(FILE_NAME);
	}

	public static boolean initializeDefaultVLCPath() {
		File file = new File(System.getenv("PROGRAMFILES") + "\\TeraCopy\\TeraCopy.exe");
		if (!file.exists()) {
			file = new File(System.getenv("ProgramFiles(x86)") + "\\TeraCopy\\TeraCopy.exe");
		}
		if (file.exists()) {
			Path_Setup = file.toPath();
			return true;
		}
		return false;
	}

	public static void copy(List<Path> source, Path targetDirectory) throws IOException {
		File tempFileList = getTempFileList(source);
		StringHelper.RunRuntimeProcess(new String[] { Path_Setup.toString(), "Copy", "*" + tempFileList.toString(),
				targetDirectory.toString() });
	}

	public static void move(List<Path> source, Path targetDirectory) throws IOException {
		File tempFileList = getTempFileList(source);
		StringHelper.RunRuntimeProcess(new String[] { Path_Setup.toString(), "Move", "*" + tempFileList.toString(),
				targetDirectory.toString() });
	}

	private static File getTempFileList(List<Path> source) throws IOException {
		File tempFileList = File.createTempFile("teraList", ".txt");
		// was using UTF 8 here but give problem with arabic letter since windows do not
		// use it as default!
		OutputStreamWriter p = new OutputStreamWriter(new FileOutputStream(tempFileList), Charset.defaultCharset());
		for (Path path : source) {
			p.write(path.toString() + "\n");
		}
		p.close();
		tempFileList.deleteOnExit();
		return tempFileList;
	}

	public static void openTeraCopyURL() {
		try {
			Desktop.getDesktop().browse(new URL("https://www.codesector.com/teracopy").toURI());
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
	}

	@Nullable
	public static Path getPath_Setup() {
		return Path_Setup;
	}

	public static void setPath_Setup(Path path_Setup) {
		Path_Setup = path_Setup;
	}
}
