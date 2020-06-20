package said.ahmad.javafx.tracker.system.call;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.SystemUtils;

public class SystemExplorer {

	public static Process select(File filePath) {
		if (filePath == null) {
			return null;
		}
		// Microsoft windows
		if (SystemUtils.IS_OS_WINDOWS) {
			// String cmd = "explorer.exe /select," + DataTable.get(0).getFilePath();
			// Runtime.getRuntime().exec(cmd);
			return runRuntimeProcess(new String[] { "explorer.exe", "/select,", filePath.toString() });
		}
		return null;
	}

	public static Process runRuntimeProcess(String[] cmd) {
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return p;
	}

	public static Process startCMDInDir(File dir) {
		if (SystemUtils.IS_OS_WINDOWS) {
			return runRuntimeProcess(new String[] { "cmd.exe", " /c start cd /d", dir.toString() });
		}
		return null;
	}
}
