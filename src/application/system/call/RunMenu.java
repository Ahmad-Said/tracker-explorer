package application.system.call;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import application.Main;
import application.StringHelper;

public class RunMenu {
	private static File runmenuFile = new File(System.getenv("APPDATA") + "\\Tracker Explorer\\runmenu.exe");

	public static void initialize() throws IOException {
		if (!runmenuFile.exists()) {

			// ensure directory is present
			File dirsetting = new File(System.getenv("APPDATA") + "\\Tracker Explorer");
			if (!dirsetting.exists()) {
				Files.createDirectory(dirsetting.toPath());
			}

			// copying resources somewhere on the system
			InputStream is = Main.class.getResourceAsStream("/resources/runmenu.exe");
			FileOutputStream fos = new FileOutputStream(runmenuFile);

			byte bytes[] = new byte[1000];
			int k = 0;
			while ((k = is.read(bytes)) != -1) {
				fos.write(bytes, 0, k);
			}
			fos.close(); // Do not forget to close the outputstream, otherwise your code will be holding
							// the file and it won't be possible to execute it
		}
	}

	public static void showMenu(List<File> toShow) {
		String[] cmdArray = new String[toShow.size() + 2];
		int j = 0;
		cmdArray[j++] = runmenuFile.toString();
		cmdArray[j++] = "/show";
		for (File path : toShow) {
			cmdArray[j++] = path.getAbsolutePath();
		}
		// https://stackoverflow.com/questions/1105085/runtime-exec-with-absolute-directory
		StringHelper.RunRuntimeProcess(cmdArray);
	}

}
