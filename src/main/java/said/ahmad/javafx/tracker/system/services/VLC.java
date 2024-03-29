package said.ahmad.javafx.tracker.system.services;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.Nullable;

import javafx.scene.control.Alert.AlertType;
import javafx.util.Duration;
import said.ahmad.javafx.tracker.app.DialogHelper;
import said.ahmad.javafx.tracker.app.StringHelper;
import said.ahmad.javafx.tracker.app.pref.Setting;
import said.ahmad.javafx.tracker.controller.FilterVLCController;
import said.ahmad.javafx.tracker.datatype.MediaCutData;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.tracker.system.file.local.FilePathLayer;
import said.ahmad.javafx.tracker.system.tracker.FileTracker;
import said.ahmad.javafx.tracker.system.tracker.FileTrackerHolder;

public class VLC {

	public final static ArrayList<String> ArrayAudioExt = new ArrayList<String>(
			Arrays.asList("AAC", "AC3", "ALAC", "AMR", "DTS", "DVAudio", "XM", "FLAC", "It", "MACE", "MOD", "MP3",
					"Opus", "PLS", "QCP", "QDM2", "QDMC", "S3M", "TTA", "WMA"));
	public final static ArrayList<String> ArrayPlayListExt = new ArrayList<String>(
			Arrays.asList("XSPF", "M3U", "M3U8"));
	public final static ArrayList<String> ArrayVideoExt = new ArrayList<String>(Arrays.asList("3GP", "ASF", "AVI",
			"DVR-MS", "FLV", "MKV", "MIDI+", "MP4", "OGG", "OGM", "WAV", "MPEG-2", "MXF", "VOB", "RM", "Blu-ray",
			"DVD-Video", "VCD", "SVCD", "DVB", "HEIF", "AVIF", "WMV", "TS", "MPEG", "M4V"));

	// private static String Path_Config= "%appdata%/vlc/vlc-qt-interface.ini";
	private static Path Path_Config = new File(System.getenv("APPDATA") + "\\vlc\\vlc-qt-interface.ini").toPath();

	private static Path Path_Setup = null;
	private static final String FILE_NAME = "vlc.exe";

	public static Map<URI, Long> recentTracker = new HashMap<>();

	/**
	 * @return the path_Setup
	 */
	@Nullable
	public static Path getPath_Setup() {
		return Path_Setup;
	}

	// private static boolean is86 = false; // use later when generating the batch
	// file

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
		if (end != 0) {
			track += "                <vlc:option>stop-time=" + end + "</vlc:option>\r\n";
		}
		track += "            </extension>\r\n" + "        </track>";
		return track;
	}

	public static boolean initializeDefaultVLCPath() {
		File file = new File(System.getenv("PROGRAMFILES") + "\\VideoLAN\\VLC\\vlc.exe");
		if (!file.exists()) {
			file = new File(System.getenv("ProgramFiles(x86)") + "\\VideoLAN\\VLC\\vlc.exe");
		}

		// check existence of VLC in system
		if (file.exists()) {
			Path_Setup = file.toPath();
			return true;
		}
		return false;
	}

	private static boolean isInExt(String name, ArrayList<String> ext) {
		return ext.contains(StringHelper.getExtention(name));
	}

	public static boolean isWellSetup() {
		return Path_Setup != null && Path_Setup.toFile().exists()
				&& Path_Setup.getFileName().toString().equals(FILE_NAME);
	}

	public static boolean isPlaylist(String name) {
		return isInExt(name, ArrayPlayListExt);
	}

	public static boolean isAudio(String name) {
		return isInExt(name, ArrayAudioExt);
	}

	public static boolean isVideo(String name) {
		return isInExt(name, ArrayVideoExt);
	}

	public static boolean isAudioOrVideo(String name) {
		return isVideo(name) || isAudio(name);
	}

	public static boolean isVLCMediaExt(String name) {
		return isAudio(name) || isVideo(name) || isPlaylist(name);
	}

	/**
	 * Start VLC to run specified path at specific moment, if moment is null
	 * will start at last saved time in VLC history.
	 *
	 * @param pathURI
	 * @param moment can be null
	 * @return <b>millisSecond</b> time picked after closing vlc at specific moment
	 *         in millisSecond beginning from start of video
	 */
	public static void startVLCAtSpecificMoment(URI pathURI, Duration moment) throws VLCException, IOException {
		reloadRecentMRL();
		String Resume = "";
		if (moment != null) {
			Resume = " --start-time " + moment.toSeconds();
		} else if (recentTracker.containsKey(pathURI)) {
			Resume = " --start-time " + recentTracker.get(pathURI) / 1000;
		}
		StartVlc(Resume + " " + pathURI);
	}

	/**
	 *
	 * @param pathURI
	 * @return last saved moment of media in millisecond
	 * @throws VLCException @see {@link #reloadRecentMRL()}
	 */
	public static long getLastSavedMoment(URI pathURI) throws VLCException, IOException {
		reloadRecentMRL();
		if (recentTracker.containsKey(pathURI)) {
			return recentTracker.get(pathURI);
		} else {
			return 0;
		}
	}

	/**
	 * Load configuration file {@link #Path_Config} of vlc and parse its recent MRL
	 * field. After succesfull read it create map from file to last time the user has left of
	 * @throws VLCException
	 */
	public static void reloadRecentMRL() throws VLCException, IOException {
		// Thread.sleep(1000); // to let vlc write it's data not necessary because i
		// waited the process...
		String list = "";
		String times = "";
		BufferedReader scan = null;
		try {
			scan = new BufferedReader(
					new InputStreamReader(new FilePathLayer(Path_Config.toFile()).getInputFileStream()
							, StandardCharsets.US_ASCII));
		} catch (Exception e) {
			// e.printStackTrace();
			throw new VLCException("Auto Detect", "Something went wrong",
					"Could not Get Data From VLC.\nPlease Choose manually");

		}
		// line=scan.next();
		while ((list = scan.readLine()) != null) {
			String temp = list.split("=")[0];

			if (temp.equals("list")) {
				break;
			}
		}
		times = scan.readLine();
		list = list.substring(5);
		times = times.substring(6);
		if (!list.equals("@Invalid()")) // this how vlc show if recent was not set
		{
			String List_Parsed[] = list.split(",");
			String Times_Parsed[] = times.split(",");
			// iterate in reverse way so if key exist twice get the most recent one !
			for (int i = Times_Parsed.length - 1; i >= 0; i--) {
				String tim = Times_Parsed[i];
				String lis = List_Parsed[i].trim();
				lis = lis.replace("\"", "");
				if (list.contains("\\x")) {
					// VLC use \xFFF for escaping unicode character but it is not translated
					// in java
					lis	= lis.replaceAll("\\\\x", "\\\\u0");
					lis	= StringEscapeUtils.unescapeJava(lis);
				}
				try {
					recentTracker.put(URI.create(lis), Long.parseLong(tim.trim()));
				} catch (Exception e) {
					 e.printStackTrace();
				}
			}
		}
		scan.close();
	}

	/**
	 * triplet data are to exclude so here we get inverse example: 2:00 to 6:00
	 * title: waste time dance we start the movie from the beginning to 2:00 skip 2
	 * to 6 then continue to end called from {@link FilterVLCController} search:
	 * t.getRunVLC().setOnAction}
	 *
	 * @param path         the Path fo media to run
	 * @param list         Media cut data 1 >> start; 2 >> end; 3 >> title
	 */
	public static PathLayer SavePlayListFile(PathLayer path, ArrayList<MediaCutData> list, boolean isFullPath,
			boolean isfirst, boolean notifyEnd) throws VLCException {

		PathLayer tempFile = null;
		String mediaLocation = null;
		String mediaName = path.getName();

		try {
			if (isFullPath) // this run when testing from preview table
			{
				tempFile = new FilePathLayer(File.createTempFile("Watch", ".xspf"));
				mediaLocation = path.getAbsolutePath();
			} else // this to generate the file next to media
			{
				String name = StringHelper.getBaseName(mediaName) + " [Filtered].xspf";
				tempFile = path.getParentPath().resolve(name);
				mediaLocation = path.getName();
			}
			if (tempFile.exists()) {
				tempFile.delete();
			}
			if (!isFullPath) {
				FileTracker.appendTrackerData(tempFile, new FileTrackerHolder(tempFile.getName())
						.setTimeToLiveDefaultMax().setSeen(false).setNoteText(list.size() + " Filtered Scene"), false);
			}
			OutputStream outputPathLayer = tempFile.getOutputFileStream();
			if (outputPathLayer == null) {
				DialogHelper.showAlert(AlertType.ERROR, "VLC", "Unsupported operation in current path",
						tempFile.toString());
				return null;
			}
			OutputStreamWriter p = new OutputStreamWriter(outputPathLayer, StandardCharsets.UTF_8);
			// initialize things: template
			String initi = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + "<playlist version=\"1\" \r\n"
					+ "    xmlns=\"http://xspf.org/ns/0/\" \r\n"
					+ "    xmlns:vlc=\"http://www.videolan.org/vlc/playlist/ns/0/\">\r\n" + "    <title>" + mediaName
					+ " Filtred" + " </title>\r\n" + "    <trackList>";
			int id = 1;
			p.write(initi + "\n\r");
			if (list.get(0).getStart() != 0 && isfirst) {
				// the first condition to prevent opening windows twice and
				// closing it if user want to skip some intro
				// the second condition if user want to test an interval other than first then
				// start from it's end and do not join with the beginning of video
				int gasp = 0;
				if (notifyEnd && list.get(0).getStart() > 20) {
					gasp = 10;
				}
				p.write(getVLCxspf("Scene 00: " + mediaName + " [Filtered]", mediaLocation, id++, 0,
						list.get(0).getStart() - gasp) + "\n\r");
				if (gasp != 0) {
					p.write(getVLCxspf("End Scene 00", mediaLocation, id++, list.get(0).getStart() - gasp,
							list.get(0).getStart()) + "\n\r");
				}
			}

			for (int i = 0; i < list.size(); i++) {
				if (i + 1 < list.size()) // mean there exist another cut
				{
					// gasp is the time to split filter in half
					// this is meant to show playList entry at the end of the scene
					// with time back of 10 second before switching to next scene
					int gasp = 0;
					if (notifyEnd && list.get(i + 1).getStart() - list.get(i).getEnd() > 20) {
						gasp = 10;
					}
					p.write(getVLCxspf(list.get(i).getTitle(), mediaLocation, id++, list.get(i).getEnd(),
							list.get(i + 1).getStart() - gasp) + "\n\r");
					if (gasp != 0) {
						p.write(getVLCxspf("End " + list.get(i).getTitle().substring(0, 8), mediaLocation, id++,
								list.get(i + 1).getStart() - gasp, list.get(i + 1).getStart()) + "\n\r");
					}
				} else // this is that last cut
				{

					p.write(getVLCxspf(list.get(i).getTitle().replace("Scene", "Last Scene"), mediaLocation, id++,
							list.get(i).getEnd(), 0) + "\n\r");
				}

			}
			p.write("    </trackList>\r\n" + "</playlist>" + "\n\r");
			p.close();
			if (isFullPath) {
				startXSPFInOrder(tempFile);
				FilePathLayer tempFileReal = (FilePathLayer) tempFile;
				tempFileReal.getFile().deleteOnExit();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return tempFile;
	}

	/**
	 * @param path_Setup the path_Setup to set
	 */
	public static void setPath_Setup(Path path_Setup) {
		Path_Setup = path_Setup;
	}

	/**
	 * Start VLC with given arguments
	 *
	 * @param arg general format "--option value URIfilePath"
	 */
	public static void StartVlc(String arg) throws VLCException {
		try {
			int startSplit = 0, endSplit = 0, inc = 0;
			while (endSplit != arg.length()) {
				endSplit = arg.lastIndexOf("file:", inc += 15000);
				endSplit = arg.indexOf(" ", endSplit);
				if (startSplit == endSplit || endSplit == -1) {
					endSplit = arg.length();
				}
				String command = Path_Setup.toAbsolutePath()
						+ " --one-instance --video-title-timeout 12000 --video-title-position=4"
						+ arg.substring(startSplit, endSplit);
				Runtime.getRuntime()
						.exec(command);

				startSplit = endSplit < arg.length() ? endSplit : arg.length();
			}
			/**
			 * Important!! Call JVM for running Garbage Collector after calling this
			 * function so VLC won't get stuck.
			 *
			 * requesting JVM for running Garbage Collector
			 * in order to release process from memory from being expanded as vlc will
			 * take large resources and may get freeze after certain ammount of time
			 * read more at : https://www.geeksforgeeks.org/garbage-collection-java/
			 */
			System.gc();
		} catch (IOException e) {
			e.printStackTrace();
			throw new VLCException("Run VLC",
					"Could Not Run VLC",
					"VLC is not installed on the system or misconfigured." +
							"\n\n Please Get/Configure VLC using VLC menu");
		}
	}

	/**
	 * Start Playlist in order with remote feature
	 *
	 * @param path path of playlist to run
	 */
	public static void startXSPFInOrder(PathLayer path) throws VLCException {
		watchWithRemote(path.toURI(), "--no-random");
	}

	/**
	 * Start VLC and open http Lua remote with the saved password(1234)
	 *
	 * @param uri         can be null
	 * @param addArgument can be anything add at the last of command
	 */
	public static void watchWithRemote(URI uri, String addArgument) throws VLCException {
		if (uri != null) {
			addArgument += " " + uri;
		}
		// we do start watching in remote mode only in case
		StartVlc(" --extraintf=http --http-password=" + Setting.getVLCHttpPass() + " --qt-recentplay-filter=watch*"
				+ addArgument);
	}

	/**
	 *
	 *
	 * @param mediaFiles
	 * @param playlist   Can be absolute path or just a name to be automatically
	 *                   saved to {@linkplain TrackerPlayer#SHORTCUT_DIRECTORY}
	 */
	public static void createPlaylistM3U(List<Path> mediaFiles, Path playlist) {

		// Forcing M3U8 Extension
		String name = playlist.getFileName().toString();
		if (!StringHelper.getExtention(name).equals("m3u8")) {
			name = StringHelper.getBaseName(name) + ".m3u8";
			playlist = playlist.getParent().resolve(name);
		}
		// Set to start menu path by default
		if (!playlist.isAbsolute()) {
			playlist = TrackerPlayer.SHORTCUT_DIRECTORY.resolve(name);
		}

		try {
			// creating Playlist file
			File playlistFile = playlist.toFile();
			playlistFile.getParentFile().mkdirs();
			playlistFile.createNewFile();

			OutputStreamWriter p = new OutputStreamWriter(new FileOutputStream(playlistFile), StandardCharsets.UTF_8);

			// initialize things: template
			String initi = "#EXTM3U\r\n" + "#PLAYLIST: " + name + "\r\n";

			p.write(initi + "\n\r");
			for (Path path : mediaFiles) {
				p.write("#EXTINF:0," + path.getFileName() + "\n\r");
				p.write(path.toUri().toString() + "\n\r");
			}
			p.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
