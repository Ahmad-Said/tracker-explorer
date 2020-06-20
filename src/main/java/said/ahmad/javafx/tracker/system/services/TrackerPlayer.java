package said.ahmad.javafx.tracker.system.services;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import mslinks.ShellLink;
import mslinks.ShellLinkException;
import net.sf.image4j.codec.ico.ICOEncoder;
import said.ahmad.javafx.tracker.app.DialogHelper;
import said.ahmad.javafx.tracker.app.Main;
import said.ahmad.javafx.tracker.app.StringHelper;
import said.ahmad.javafx.tracker.controller.SettingController;
import said.ahmad.javafx.tracker.system.SystemIconsHelper;
import said.ahmad.javafx.tracker.system.WindowsShortcut;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.tracker.system.file.local.FilePathLayer;

/**
 * The use of Playlist in this class mean the file that shortcut point to (that
 * is the real file inside shortcut)
 *
 * @author user
 *
 */
public class TrackerPlayer {

	/**
	 * Note that this path can be -not- exist() so always do check for existence and
	 * create folder if needed
	 */
	public static Path SHORTCUT_DIRECTORY = Paths
			.get(System.getenv("APPDATA") + "\\Microsoft\\Windows\\Start Menu\\Programs\\Ahmad Said\\Playlist");
	public static Path ICON_DIRECTORY = Paths
			.get(System.getenv("APPDATA") + "\\Microsoft\\Windows\\Start Menu\\Programs\\Ahmad Said\\Playlist\\Icons");
	private static Path TRACKER_EXE = Paths.get(System.getenv("APPDATA")).getParent()
			.resolve("Local\\Tracker Explorer\\Tracker Explorer.exe");

	/**
	 * Create a shortcut to Tracker Explorer with argument to run a specific
	 * Playlist and put it in Start Menu for windows
	 *
	 * @param shortcutName
	 * @param playlist
	 */
	public static void createNewShortcutPlaylist(String shortcutName, Path playlist) {
		File workingDir = SHORTCUT_DIRECTORY.toFile();
		workingDir.mkdirs();
		File trackerExplorer = TRACKER_EXE.toFile();

		ShellLink sl = ShellLink.createLink(trackerExplorer.toString()).setWorkingDir(workingDir.toString());

		if (VLC.isVLCMediaExt(playlist.toString())) {
			sl.setIconLocation("%SystemRoot%\\system32\\SHELL32.dll");
			sl.getHeader().setIconIndex(116);
		} else {
			try {
				// creating icon
				PathLayer pathLayer = new FilePathLayer(playlist.toFile());
				String ext = pathLayer.getExtension();
				File iconFile = ICON_DIRECTORY.resolve(ext + ".ico").toFile();
				if (!iconFile.exists()) {
					if (!ICON_DIRECTORY.toFile().exists()) {
						ICON_DIRECTORY.toFile().mkdirs();
					}
					Image iconImage = SystemIconsHelper.getFileIcon(pathLayer);
					BufferedImage bImage = SwingFXUtils.fromFXImage(iconImage, null);
					ICOEncoder.write(bImage, iconFile);
				}
				sl.setIconLocation(iconFile.toString());
			} catch (IOException e1) {
				sl.setIconLocation("%SystemRoot%\\system32\\SHELL32.dll");
				sl.getHeader().setIconIndex(116);
				e1.printStackTrace();
			}
		}

		sl.setCMDArgs("--silent=1 --player=\"" + playlist + "\"");

		try {
			sl.saveTo(workingDir.toPath().resolve(shortcutName + ".lnk").toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void createNewShortcutPlaylist(String shortcutName, List<Path> mediaFiles) {
		Path playlist = SHORTCUT_DIRECTORY.resolve(shortcutName + ".m3u8");
		VLC.createPlaylistM3U(mediaFiles, playlist);
		createNewShortcutPlaylist(shortcutName, playlist);
	}

	public static File openPlaylistInLnk(File playlistFile) throws IOException, ParseException {
		if (VLC.isWellSetup() && VLC.isVLCMediaExt(playlistFile.getName())) {
			VLC.watchWithRemote(playlistFile.toPath().toUri(), "");
			System.gc();
		} else {
			if (WindowsShortcut.isPotentialValidLink(playlistFile)) {
				Desktop.getDesktop().open(new File(new WindowsShortcut(playlistFile).getRealFilename()));
			} else {
				Desktop.getDesktop().open(playlistFile);
			}
		}
		return playlistFile;
	}

	public static File openShortcutLnk(File shortcutFile) throws IOException, ShellLinkException, ParseException {
		File playlist = new File(StringHelper.getValueFromCMDArgs(new ShellLink(shortcutFile).getCMDArgs(),
				Main.ArgsType.player.toString()));
		return openPlaylistInLnk(playlist);
	}

	public static void openTrackerSettingGUI() {
		SettingController miniSetting = new SettingController(null);
		miniSetting.switchToTrackerPlayerTab();
	}

	public static String getPlaylistName() {
		return DialogHelper.showTextInputDialog("Add New Playlist", "Enter Playlist Name",
				"It is Advised to use a name that cortana does recognize it with your voice,"
						+ "\n\tso you can say: hey cortana open 'playlist name'."
						+ "\nHint: -a good practice to test name with cortana then come here"
						+ "\n\t -If Cortana do stupid bing search instead of opening your shortcut"
						+ "\n\t  do search in start menu and open it few times so it get into cortana knowledges"
						+ "\n\t - And last thing stay away of well known cortana command or similar application",
				"Tracker Player");
	}

	/**
	 *
	 * @param initialDirectory optional Can be null
	 * @return
	 */
	@Nullable
	public static File getPlaylistLocation(File initialDirectory, Stage requestingStage) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Navigate to where Your Playlist is located");
		if (initialDirectory != null && initialDirectory.exists()) {
			fileChooser.setInitialDirectory(initialDirectory);
		} else {
			fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		}
		fileChooser.getExtensionFilters().addAll(StringHelper.getExtensionFilter("Playlist", VLC.ArrayPlayListExt));
		fileChooser.getExtensionFilters().addAll(StringHelper.getExtensionFilter("Simple Audio", VLC.ArrayAudioExt));
		fileChooser.getExtensionFilters().addAll(StringHelper.getExtensionFilter("Simple Video", VLC.ArrayVideoExt));
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Anything else", "*"));
		File playlistFile = fileChooser.showOpenDialog(requestingStage);
		return playlistFile;
	}

	/**
	 *
	 * @param playlistName is the base name without extension
	 */
	public static void deleteShortcutAndPlaylist(String playlistName) {
		File shortcutFile = SHORTCUT_DIRECTORY.resolve(playlistName + ".lnk").toFile();
		File playlistFile = SHORTCUT_DIRECTORY.resolve(playlistName + ".m3u8").toFile();
		if (shortcutFile.exists()) {
			shortcutFile.delete();
		}
		if (playlistFile.exists()) {
			playlistFile.delete();
		}
	}

	/**
	 *
	 * @param originalName old name of playlist without extension
	 * @param newName      new name of playlist without extension
	 *
	 * @return pair Shortcut File ->> Playlist File
	 * @throws ShellLinkException
	 * @throws IOException
	 */
	public static Pair<File, File> renameShorcutAndPlaylist(String originalName, String newName)
			throws IOException, ShellLinkException {

		File shortcutFile = SHORTCUT_DIRECTORY.resolve(originalName + ".lnk").toFile();
		File newShortcutFile = SHORTCUT_DIRECTORY.resolve(newName + ".lnk").toFile();

		File playlistFile = new File(StringHelper.getValueFromCMDArgs(new ShellLink(shortcutFile).getCMDArgs(),
				Main.ArgsType.player.toString()));

		boolean doCreateNewShorcut = false;
		File newPlaylistFile = playlistFile;

		if (playlistFile.getParentFile().toPath().equals(SHORTCUT_DIRECTORY)) {
			doCreateNewShorcut = true;
			newPlaylistFile = SHORTCUT_DIRECTORY.resolve(newName + ".m3u8").toFile();
		}

		Pair<File, File> pair = new Pair<File, File>(newShortcutFile, newPlaylistFile);
		if (shortcutFile.exists()) {
			if (doCreateNewShorcut) {
				shortcutFile.delete();
				if (playlistFile.exists()) {
					FileUtils.moveFile(playlistFile, newPlaylistFile);
				}
				createNewShortcutPlaylist(newName, newPlaylistFile.toPath());
			} else {
				FileUtils.moveFile(shortcutFile, newShortcutFile);
			}
		}
		return pair;
	}

	public static HashMap<File, File> getAllShortcutTracker() {
		HashMap<File, File> shortcutToRealFile = new HashMap<File, File>();
		if (TrackerPlayer.SHORTCUT_DIRECTORY.toFile().exists()) {
			Arrays.asList(TrackerPlayer.SHORTCUT_DIRECTORY.toFile().listFiles()).stream()
					.filter(p -> StringHelper.getExtention(p.toString()).equals("LNK")).forEach(f -> {
						try {
							shortcutToRealFile.put(f,
									new File(StringHelper.getValueFromCMDArgs(new ShellLink(f).getCMDArgs(),
											Main.ArgsType.player.toString())));
						} catch (IOException | ShellLinkException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					});
		}
		return shortcutToRealFile;
	}
}
