package application;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;

import application.controller.SplitViewController;
import javafx.scene.Node;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;

public class StringHelper {

	public static Path InitialLeftPath;
	public static Path InitialRightPath;
	public static Integer temp;
	private static Map<String, String> KeyAsShiftDown = new HashMap<String, String>() {
		/**
		 *
		 */
		private static final long serialVersionUID = 4486947120104662470L;

		{
			put("-", "_");
			put("0", ")");
			put("1", "!");
			put("2", "@");
			put("3", "#");
			put("4", "$");
			put("5", "%");
			put("6", "^");
			put("7", "&");
			put("9", "(");
			put("0", ")");
			put("-", "_");
			put("=", "+");
			put("`", "~");
			put("]", "}");
			put("[", "{");
			put(",", "<");
			put(".", ">");
			// to make values of space
			put("z", " ");
		}
	};

	public static boolean containsWord(String text, String word) {
		String[] words = getWords(text);
		word = word.toLowerCase();
		for (String item : words) {
			if (item.equals(word)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Parse Arguments in form --name=value return null if not found
	 *
	 * @param args_cmd the argument string
	 * @param name_key the name
	 * @return the value if found, null otherwise
	 */
	public static String getValueFromCMDArgs(String args_cmd, String name_key) {
		int valueStart = args_cmd.indexOf(name_key);
		if (valueStart == -1) {
			return null;
		}
		valueStart += name_key.length() + 1;
		int valueEnd = -1;
		if (args_cmd.charAt(valueStart) == '"') {
			valueStart++;
			valueEnd = args_cmd.indexOf('"', valueStart);
		} else {
			valueEnd = args_cmd.indexOf(' ', valueStart);
		}
		if (valueEnd == -1) {
			valueEnd = args_cmd.length();
		}
		return args_cmd.substring(valueStart, valueEnd);
	}

	public static FileChooser.ExtensionFilter getExtensionFilter(String description, List<String> extensions) {
		return new FileChooser.ExtensionFilter(description,
				extensions.stream().map(p -> "*" + p).collect(Collectors.toList()));
	}

	public static String getExtention(String fileName) {
		return FilenameUtils.getExtension(fileName).toUpperCase();
	}

	public static String getBaseName(String fileName) {
		return FilenameUtils.getBaseName(fileName);
	}

	public static String[] getWords(String text) {
		return text.toLowerCase().split("\\W+");
	}

	/**
	 * Replace any occurrence of these characters /\\:*\"<>| with specified
	 * character
	 *
	 * @param OriginalName
	 * @param toReplaceWith
	 * @return
	 */
	public static String getValidName(String OriginalName, String toReplaceWith) {
		return OriginalName.replaceAll("[/\\\\:*\\\"<>|]", toReplaceWith);
	}

	public static String FixNameEncoding(String original) {
		// //
		// https://stackoverflow.com/questions/216894/get-an-outputstream-into-a-string
		String fixedName = original;
		String last = original;
		ByteArrayOutputStream baos = new ByteArrayOutputStream(fixedName.length());
		try {
			baos.write(fixedName.getBytes());
			baos.close();
			last = new String(baos.toByteArray());
			last = last.replace("?", "");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return last;
	}

	public static void PrintProcessOutput(Process proc) {
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

		BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

		try {
			// read the output from the command
			System.out.println("Here is the standard output of the command:\n");
			String s = null;
			while ((s = stdInput.readLine()) != null) {
				System.out.println(s);
			}
			// read any errors from the attempted command
			System.out.println("Here is the standard error of the command (if any):\n");
			while ((s = stdError.readLine()) != null) {
				System.out.println(s);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Process RunRuntimeProcess(String[] cmd) {
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(cmd);
			// PrintProcessOutput(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return p;
	}

	public static class FileComparator implements Comparator<File> {

		@Override
		public int compare(File f1, File f2) {
			if (f1.isDirectory() && f2.isDirectory() || f1.isFile() && f2.isFile()) {
				return f1.compareTo(f2);
			}
			return f1.isDirectory() ? -1 : 1;
		}
	}

	public static class NaturalFileComparator implements Comparator<File> {
		private final Comparator<String> NATURAL_SORT = new WindowsExplorerComparator();

		@Override
		public int compare(File f1, File f2) {

			if (f1.isDirectory() && f2.isDirectory() || f1.isFile() && f2.isFile()) {
				return NATURAL_SORT.compare(f1.getName(), f2.getName());
			}
			return f1.isDirectory() ? -1 : 1;
		}
	}

	public static void SortArrayFiles(List<File> listFiles) {
		Collections.sort(listFiles, new FileComparator());
	}

	public static void SortNaturalArrayFiles(List<File> listFiles) {
		Collections.sort(listFiles, new NaturalFileComparator());
	}

	public static Map<String, String> getKeyAsShiftDown() {
		return KeyAsShiftDown;
	}

	public static Integer getTemp() {
		return temp;
	}

	public static void setTemp(Integer temp) {
		StringHelper.temp = temp;
	}

	private static Instant Start;
	private static Instant Finish;

	public static void startTimer() {
		Start = Instant.now();
	}

	public static long endTimerAndDisplay() {
		Finish = Instant.now();
		long timeElapsed = Duration.between(Start, Finish).toMillis(); // in millis
		System.out.println("--- Took about :  " + timeElapsed + "  -----");
		return timeElapsed;
	}

	public static void openFile(File resources) {
		try {
			Desktop.getDesktop().open(resources);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * TODO in {@link SplitViewController#navigate(Path)} open multiple files in
	 * same program this is done tweak with vlc by process builder not on any
	 * program like windows explorer do
	 *
	 * @param resources
	 */
	public static void openFiles(List<File> resources) {
		// TODO get multiple files from table and
		Thread multiFileStarter = new Thread() {

			@Override
			public void run() {
				resources.stream().forEach(r -> {
					try {
						Desktop.getDesktop().open(r);
						// wait 500 MILLISECONDS between each file to not over the computer
						TimeUnit.MILLISECONDS.sleep(500);
					} catch (InterruptedException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
				openFile(resources.get(0));
			}
		};
		multiFileStarter.start();

	}

	// https://stackoverflow.com/questions/43572022/javafx-convert-textflow-to-string
	public static String textFlowToString(TextFlow textFlow) {
		StringBuilder sb = new StringBuilder();
		for (Node node : textFlow.getChildren()) {
			if (node instanceof Text) {
				sb.append(((Text) node).getText());
			}
		}
		String fullText = sb.toString();
		return fullText;
	}

	@Nullable
	public static Path parseUriToPath(String uri) {
		try {
			return Paths.get(URI.create(uri));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getFormattedSizeFromMB(double sizeInMB) {
		String format = "";
		if (sizeInMB > 1024) {
			sizeInMB = sizeInMB / 1024;
			format = String.format("%.1f GB", sizeInMB);
		} else {
			format = String.format("%.1f MB", sizeInMB);
		}
		return format;
	}
}
