package application;

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

import application.controller.FilterVLCController;
import application.model.MediaCutData;
import javafx.scene.control.Alert.AlertType;
import javafx.util.Duration;

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

	private static ArrayList<String> ArrayPlayListExt = new ArrayList<String>(
			Arrays.asList("XSPF"));

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
						// DialogHelper.showAlert(AlertType.ERROR, "Error", "Failed To parse VLC Config
						// URI", lis);
					}
				}
			}
			scan.close();

		} catch (FileNotFoundException e) {
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

	public static int pickTime(Path path, Duration resumeTime) {
		Runtime runtime = Runtime.getRuntime();
		try {
			ReloadRecentMRL();
			String Resume = "";
			if (resumeTime != null)
				Resume = " --start-time " + resumeTime.toSeconds();
			else if (RecentTracker.containsKey(path))
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
	 * @param MediaCutData
	 *            1 >> start; 2 >> end; 3 >> title
	 */
	public static File SavePlayListFile(Path path, ArrayList<MediaCutData> list, boolean isFullPath, boolean isfirst,
			boolean notifyEnd) {

		File tempFile = null;
		String mediaLocation = null;
		String mediaName = path.getFileName().toString();

		try {
			if (isFullPath) // this run when testing from preview table
			{
				tempFile = File.createTempFile("Watch", ".xspf");
				mediaLocation = path.toUri().toString();
			} else // this to generate the file next to media
			{
				WatchServiceHelper.setRuning(false); // prevent overload
				String name = mediaName.replace(SystemIconsHelper.getFileExt(path.toString()), "[Filtered].xspf");
				tempFile = path.getParent().resolve(name).toFile();
				mediaLocation = path.toUri().toString().substring((path.toUri().toString().lastIndexOf('/') + 1));
			}
			if (tempFile.exists())
				tempFile.delete();
			PrintStream p = new PrintStream(tempFile);
			// initialize things: template
			String initi = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + "<playlist version=\"1\" \r\n"
					+ "    xmlns=\"http://xspf.org/ns/0/\" \r\n"
					+ "    xmlns:vlc=\"http://www.videolan.org/vlc/playlist/ns/0/\">\r\n" + "    <title>" + mediaName
					+ " Filtred" + " </title>\r\n" + "    <trackList>";
			int id = 1;
			p.println(initi);
			if (list.get(0).getStart() != 0 && isfirst) {
				// the first condition to prevent opening windows twice and
				// closing it if user want to skip some intro
				// the second condition if user want to test an interval other than first then
				// start from it's end and do not join with the beginning of video
				int gasp = 0;
				if (notifyEnd && list.get(0).getStart() > 20)
					gasp = 10;
				p.println(getVLCxspf("Scene 00: " + mediaName + " [Filtered]", mediaLocation, id++, 0,
						list.get(0).getStart() - gasp));
				if (gasp != 0)
					p.println(getVLCxspf("End Scene 00", mediaLocation, id++, list.get(0).getStart() - gasp,
							list.get(0).getStart()));
			}

			for (int i = 0; i < list.size(); i++) {
				if (i + 1 < list.size()) // mean there exist another cut
				{
					// gasp is the time to split filter in half
					// this is meant to show playList entry at the end of the scene
					// with time back of 10 second before switching to next scene
					int gasp = 0;
					if (notifyEnd && list.get(i + 1).getStart() - list.get(i).getEnd() > 20)
						gasp = 10;
					p.println(getVLCxspf(list.get(i).getTitle(), mediaLocation, id++, list.get(i).getEnd(),
							list.get(i + 1).getStart() - gasp));
					if (gasp != 0)
						p.println(getVLCxspf("End " + list.get(i).getTitle().substring(0, 8), mediaLocation, id++,
								list.get(i + 1).getStart() - gasp, list.get(i + 1).getStart()));
				} else // this is that last cut
				{

					p.println(getVLCxspf(list.get(i).getTitle().replace("Scene", "Last Scene"), mediaLocation, id++,
							list.get(i).getEnd(), 0));
				}

			}
			p.println("    </trackList>\r\n" + "</playlist>");
			p.close();
			if (isFullPath) {
				startXSPF(tempFile.toPath());
				tempFile.deleteOnExit();
			} else
				WatchServiceHelper.setRuning(true); // </prevent overload
		} catch (IOException e) {
			// e.printStackTrace();
		}
		return tempFile;
	}

	private static String getVLCxspf(String title, String location, int id, int start, int end) {
		String track = "        <track>\r\n" + "            <location>" + location + "</location>\r\n"
				+ "            <album>https://ahmad-said.github.io/tracker-explorer/</album>\r\n"
				+ "            <image>https://dummyimage.com/25x25/ffffff/000000.png&amp;text=" + id + "</image>\r\n";
		if (title != null && !title.trim().isEmpty()) {
			track += "            <title>" + title + "</title>\r\n" + "            <annotation>Duration: "
					+ (end - start) + title + "</annotation>\r\n";

		} else {
			track += "            <title>" + "." + "</title>\r\n";
		}
		track += "            <extension application=\"http://www.videolan.org/vlc/playlist/0\">\r\n"
				+ "                <vlc:id>" + id + "</vlc:id>\r\n";
		if (start != 0) {
			track += "                <vlc:option>start-time=" + start + "</vlc:option>\r\n";
		}
		if (end != 0)
			track += "                <vlc:option>stop-time=" + end + "</vlc:option>\r\n";
		track += "            </extension>\r\n" + "        </track>";
		return track;
	}

	public static void StartVlc(String arg) {
		try {
			Runtime.getRuntime().exec(Path_Setup + " " + arg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void startXSPF(Path path) {
		try {
			Runtime.getRuntime()
			.exec(Path_Setup + "  --qt-recentplay-filter=watch* --video-title-timeout 12000 --video-title-position=4 "
					+path.toUri());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static boolean isInExt(String name, ArrayList<String> ext) {
//		int index = name.lastIndexOf('.') + 1;
//		if (index < name.length())
//			return ext.contains(name.substring(index).toUpperCase());
			return ext.contains(StringHelper.getExtention(name));
//		return false;
	}
	
	
	public static boolean isVideo(String name) {
		return isInExt(name, ArrayVideoExt);
	}

	public static boolean isAudio(String name) {
		return isInExt(name, ArrayAudioExt);
	}

	public static boolean isPlaylist(String name) {
		return isInExt(name, ArrayPlayListExt);
	}

	public static boolean isVLCExt(String name) {
		return isAudio(name) || isVideo(name) || isPlaylist(name);
	}

	public static String getPath_Setup() {
		return Path_Setup;
	}

	public static void setPath_Setup(String path_Setup) {
		Path_Setup = path_Setup;
	}

}
