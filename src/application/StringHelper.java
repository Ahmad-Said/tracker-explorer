package application;

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

import org.apache.commons.io.FilenameUtils;

import application.controller.SplitViewController;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

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

	public static String getExtention(String fileName) {
		return FilenameUtils.getExtension(fileName).toUpperCase();
	}

	public static String getBaseName(String fileName) {
		return FilenameUtils.getBaseName(fileName);
	}

	public static String[] getWords(String text) {
		return text.toLowerCase().split("\\W+");
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

	// https://stackoverflow.com/questions/21686352/non-static-method-gethostservices-cannot-be-referenced-from-a-static-context
	public static void open(String resources) {
		Application a = new Application() {
			@Override
			public void start(Stage primaryStage) throws Exception {
			}
		};
		a.getHostServices().showDocument(resources);
	}

	public static void openFile(File resources) {
		open(resources.toURI().toString());
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
				Application a = new Application() {
					@Override
					public void start(Stage primaryStage) throws Exception {
					}
				};
				resources.stream().map(r -> r.toURI().toString()).forEach(r -> {
					a.getHostServices().showDocument(r);
					try {
						// wait 500 MILLISECONDS between each file to not over the computer
						TimeUnit.MILLISECONDS.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
				open(resources.get(0).toURI().toString());
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

	public static Path StringUriToPath(String uri) {
		return Paths.get(URI.create(uri));
	}

	// public boolean isLowerCase(String test) {
	//
	// }
}
