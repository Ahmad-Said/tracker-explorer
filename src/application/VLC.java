package application;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javafx.scene.control.Alert.AlertType;

public class VLC {

	public static Map<Path, Integer> RecentTracker = new HashMap<Path, Integer>();
	// private static String Path_Config= "%appdata%/vlc/vlc-qt-interface.ini";
	private static String Path_Config = System.getenv("APPDATA") + "\\vlc\\vlc-qt-interface.ini";
	private static String Path_Setup;

	private static ArrayList<String> ArrayVideoExt = new ArrayList<String>(
			Arrays.asList("3GP", "ASF", "AVI", "DVR-MS", "FLV", "MKV", "MIDI", "MP4", "Ogg", "OGM", "WAV", "MPEG-2",
					"MXF", "VOB", "RM", "Blu-ray", "DVD-Video", "VCD", "SVCD", "DVB", "HEIF", "AVIF"));

	private static ArrayList<String> ArrayAudioExt = new ArrayList<String>(
			Arrays.asList("AAC", "AC3", "ALAC", "AMR", "DTS", "DVAudio", "XM", "FLAC", "It", "MACE", "MOD", "MP3",
					"Opus", "PLS", "QCP", "QDM2", "QDMC", "S3M", "TTA", "WMA"));

	public static void ReloadRecentMRL() {
		try {

			// Thread.sleep(1000); // to let vlc write it's data not necessary because i
			// waited the process...
			String list = "";
			String times = "";
			Scanner scan = null;
			try {
				// replace space with %20 to resolve spaces in path
				scan = new Scanner(new File(new URI("file:///" + Path_Config.replace('\\', '/').replace(" ", "%20"))));
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				DialogHelper.showAlert(AlertType.ERROR, "Auto Detect", "Something went wrong",
						"Could not Get Data From VLC.\nPlease Choose manually");

			}
			// line=scan.next();
			while (scan.hasNextLine()) {
				list = scan.nextLine();
				String temp = list.split("=")[0];

				if (temp.equals("list"))
					break;
			}
			times = scan.nextLine();
			list = list.substring(5);
			times = times.substring(6);
			if (!list.equals("@Invalid()")) // this how vlc show if recent was not set
			{
				// System.out.println(list);
				// System.out.println(times);
				String List_Parsed[] = list.split(",");
				String Times_Parsed[] = times.split(",");
				// iterate in reverse way so if key exist twice get the most recent one !
				for (int i = Times_Parsed.length - 1; i >= 0; i--) {
					String tim = Times_Parsed[i];
					String lis = List_Parsed[i].trim();
					// System.out.println("tim is " + tim + " and lis is " + lis);
					lis = lis.replace("\"", "");
					try {
						RecentTracker.put(Paths.get(URI.create(lis)), Integer.parseInt(tim.trim()));
					} catch (Exception e) {
						// TODO: handle exception
						// DialogHelper.showAlert(AlertType.ERROR, "Error", "Failed To parse VLC Config
						// URI", lis);
					}
				}
			}
			scan.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
	}

	// private static boolean is86 = false; // use later when generating the batch
	// file

	public static boolean initializeDefaultVLCPath() {
		File file = new File(System.getenv("PROGRAMFILES") + "\\VideoLAN\\VLC\\vlc.exe");
		if (!file.exists()) {
			file = new File(System.getenv("ProgramFiles(x86)") + "\\VideoLAN\\VLC\\vlc.exe");
			// if (file.exists())
			// is86 = true;
		}

		// check existence of vlc in system
		if (file.exists()) {
			Path_Setup = file.getAbsolutePath();
			return true;
		}
		// System.out.println(Path_Setup);
		return false;
	}

	public static int pickTime(Path path) {
		Runtime runtime = Runtime.getRuntime();
		try {
			ReloadRecentMRL();
			String Resume = "";
			if (RecentTracker.containsKey(path))
				Resume = " --start-time " + RecentTracker.get(path) / 1000;
			Process p = runtime.exec(Path_Setup + Resume + " " + path.toUri());
			p.waitFor();
		} catch (InterruptedException | IOException e) {
		}
		// System.out.println("i;m closed");
		ReloadRecentMRL();
		if (RecentTracker.containsKey(path))
			return RecentTracker.get(path);
		else {
			DialogHelper.showAlert(AlertType.ERROR, "VLC Picker", "Something went wrong",
					"Try Again.\nPossible Reason: \n Recent mrl is turned Off.");
			return 0;
		}
	}

	/**
	 * triplet data are to exclude so here we get inverse example: 2:00 to 6:00
	 * title: waste time dance we start the movie from the beginning to 2:00 skip 2
	 * to 6 then continue to end called from {@link FilterVLCController} search:
	 * t.getRunVLC().setOnAction}
	 * 
	 * @param path
	 *            the Path fo media to run
	 * @param tripletData
	 *            1 >> start; 2 >> end; 3 >> title
	 */
	public static File RunMovieasBatch(Path path, ArrayList<MediaCutData> list, boolean isFullPath, boolean isfirst) {

		File tempFile = null;
		try {
			if (isFullPath) // this run when testing from preview table
			{
				// System.out.println("i'm hree");
				tempFile = File.createTempFile("Watch", ".bat");
			} else // this to generate the file next to media
			{
				WatchServiceHelper.setRuning(false); // prevent overload
				String mediaName = path.getFileName().toString();
				String name = mediaName.replace(SystemIconsHelper.getFileExt(path.toString()), ".bat");
				// System.out.println(name);
				tempFile = path.getParent().resolve(name).toFile();
			}
			if (tempFile.exists())
				tempFile.delete();
			PrintStream p = new PrintStream(tempFile);
			// initialize things: template
			// set a="Jurassic.World.Fallen.Kingdom.2018.BluRay.1080p.x264.mp4"
			// set path="%ProgramFiles%\VideoLAN\VLC\vlc.exe"
			// %path% --one-instance --meta-title="some one line" --video-title-timeout
			// 50000 --start-time 50 --stop-time 55 %a%
			// vlc://quit
			// %path% -f --one-instance --no-video-title-show --start-time 60 %a%
			if (isFullPath)
				p.println("set media=\"" + path.toAbsolutePath() + "\"");
			else
				p.println("set media=\"" + path.getFileName() + "\"");

			p.println("set path=\"%PROGRAMFILES%\\VideoLAN\\VLC\\vlc.exe\"");
			if (list.get(0).getStart() != 0 && isfirst)
				// the first condition to prevent opening windows twice and
				// closing it if user want to skip some intro
				// the second condition if user want to test an interval other than first then
				// start from it's end and do not join with the begining of video
				p.println(getVLCcmd("", 0, list.get(0).getStart()));
			for (int i = 0; i < list.size(); i++) {
				if (i + 1 < list.size()) // mean there exist another cut
					p.println(getVLCcmd(list.get(i).getTitle(), list.get(i).getEnd(), list.get(i + 1).getStart()));
				else // this is that last cut
					p.println(getVLCcmd(list.get(i).getTitle(), list.get(i).getEnd(), 0));

			}
			p.close();
			WatchServiceHelper.setRuning(true); // prevent overload
			if (isFullPath) {
				Desktop.getDesktop().open(tempFile);
				// Runtime runtime = Runtime.getRuntime();
				// System.out.println(tempFile.getAbsolutePath());
				// Process p1 = runtime.exec("cmd \""+ tempFile.getAbsolutePath() +"\"");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		return tempFile;
	}

	private static String getVLCcmd(String title, int start, int end) {
		String cmd = "";
		cmd += "%path% -f --one-instance";
		if (start != 0) {
			if (title != null && !title.trim().isEmpty()) {
				int countSpace = title.length() - title.replace(" ", "").length(); // count spaces in title
				// http://www.execuread.com/facts/
				int time = (int) Math.floor(countSpace * 0.6) * 1000 + 10000; // to millisecond 2000 due to open
				cmd += " --meta-title=\"" + title + "\"" + " --video-title-timeout " + time
						+ " --video-title-position=4";
			} else
				cmd += " --no-video-title-show";
			cmd += " --start-time " + start;
		}
		if (end != 0)
			cmd += " --stop-time " + end;
		if (end != 0)
			cmd += " --play-and-exit";
		cmd += " %media%";
		// if (end != 0)
		// cmd += " vlc://quit"; // found alternative --play-and-exit
		// System.out.println(cmd);
		return cmd;
	}

	public static void StartVlc(String arg) {
		try {
			Runtime.getRuntime().exec(Path_Setup + " " + arg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static boolean isVideo(String name) {
		int index = name.lastIndexOf('.') + 1;
		if (index < name.length())
			return VLC.ArrayVideoExt.contains(name.substring(index).toUpperCase());
		return false;
	}

	public static boolean isAudio(String name) {
		int index = name.lastIndexOf('.') + 1;
		if (index < name.length())
			return VLC.ArrayAudioExt.contains(name.substring(index).toUpperCase());
		return false;
	}

	public static boolean isVLCExt(String name) {
		return isAudio(name) || isVideo(name);
	}

	public static String getPath_Setup() {
		return Path_Setup;
	}

	public static void setPath_Setup(String path_Setup) {
		Path_Setup = path_Setup;
	}

}
