package said.ahmad.javafx.tracker.app.pref;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import javafx.scene.control.Alert.AlertType;
import lombok.Getter;
import lombok.Setter;
import said.ahmad.javafx.tracker.app.DialogHelper;
import said.ahmad.javafx.tracker.app.StringHelper;
import said.ahmad.javafx.tracker.app.look.THEME;
import said.ahmad.javafx.tracker.app.look.THEME_COLOR;
import said.ahmad.javafx.tracker.datatype.FavoriteView;
import said.ahmad.javafx.tracker.datatype.FavoriteViewList;
import said.ahmad.javafx.tracker.datatype.UserContextMenu;
import said.ahmad.javafx.tracker.system.call.CommandVariable;
import said.ahmad.javafx.tracker.system.call.RunMenu;
import said.ahmad.javafx.tracker.system.call.TeraCopy;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.tracker.system.file.PathLayerHelper;
import said.ahmad.javafx.tracker.system.services.VLC;
import said.ahmad.javafx.util.CallBackToDo;

public class Setting {
	// --------- Definitions ---------
	private static final String PATH_SPLITTER = ";";
	private static final File SETTING_FILE = new File(
			System.getenv("APPDATA") + "\\Tracker Explorer\\TrackerExplorerSetting.txt");
	public static final File SETTING_DIRECTORY = new File(System.getenv("APPDATA") + "\\Tracker Explorer");

	// ---------------- Setting To be loaded as Part One TXT File ----------------
	private static final String Version = "6.2";
	/** @since v5.1 */
	private static long ApplicationTimesLunched = 1;
	/** @since v5.1 */
	private static boolean isMaximized = false;
	private static Boolean BackSync = false;
	private static Boolean AutoExpand = true;
	private static Boolean LoadAllIcon = true;
	private static PathLayer LeftLastKnowLocation = null;
	private static PathLayer RightLastKnowLocation = null;
	private static String ActiveUser = "default";
	private static String VLCHttpPass = "1234";
	private static int MaxLimitFilesRecursive = 10000;
	private static int MaxDepthFilesRecursive = 5;
	private static boolean isDebugMode = false;
	private static boolean autoRenameUTFFile = false;
	private static boolean useTeraCopyByDefault = false;
	private static boolean autoCloseClearDoneFileOperation = true;
	private static ArrayList<String> userNames = new ArrayList<String>(Arrays.asList("default"));
	private static THEME lastTheme = THEME.BOOTSTRAPV3;
	private static THEME_COLOR lastThemeColor = THEME_COLOR.NONE;
	private static final FavoriteViewList favoritesViews = new FavoriteViewList();
	// ---------------- Setting To be loaded as Part Two XML ----------------
	private static boolean restoreLastOpenedFavorite = true;
	private static ArrayList<String> lastOpenedFavoriteTitle = new ArrayList<>();
	private static FavoriteView lastOpenedView;
	private static boolean notifyFilesChanges = true;
	private static boolean showWindowOnTopWhenNotify = false;
	/**@since v5.3*/
	private static String dateFormatPattern = "dd-MM-yyyy HH:mm:ss";

	/** Initial set of groups */
	@Getter
	@Setter
	private static HashMap<String, ArrayList<String>> extensionGroups = new HashMap<String, ArrayList<String>>() {
		{
			putAll(UserContextMenuDefaultSetting.getInitializedExtensionGroupsMap());
		}
	};

	@Getter
	@Setter
	private static List<UserContextMenu> userContextMenus = new ArrayList<UserContextMenu>() {
		{
			addAll(UserContextMenuDefaultSetting.getInitializedMenuList());
		}
	};

	// ---------------- On finish Loading Functional services ----------------
	private static ArrayList<CallBackToDo> onFinishLoadingAllPartToDo = new ArrayList<>();
	private static boolean didLoadedAllPart = false;
	@Getter
	private static boolean didLoadedAllPartAndExecuteRegistredTask = false;

	private static void callOnceLater() {
		VLC.initializeDefaultVLCPath();
		TeraCopy.initializeDefaultVLCPath();
		try {
			RunMenu.initialize();
			migrateOldSetting();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void saveSetting() {
		PrintStream p = null;
		try {
			File dirsetting = new File(System.getenv("APPDATA") + "\\Tracker Explorer");
			if (!dirsetting.exists()) {
				Files.createDirectory(dirsetting.toPath());
			}
			if (SETTING_FILE.exists()) {
				SETTING_FILE.delete();
			}
			p = new PrintStream(SETTING_FILE.toString());
			p.println("/this is a generated folder by application to Save Setting");
			p.println("Version=" + Version);
			p.println("ApplicationTimesLunched=" + ++ApplicationTimesLunched);
			p.println("isMaximized=" + isMaximized);
			p.println("VLCHttpPass=" + getVLCHttpPass());
			// saved as URI
			p.println("VLCPath=" + VLC.getPath_Setup().toUri().toString());
			if (TeraCopy.getPath_Setup() != null) {
				p.println("TeraCopyPath=" + TeraCopy.getPath_Setup().toUri().toString());
			}
			p.println("BackSync=" + BackSync);
			p.println("AutoExpand=" + AutoExpand);
			p.println("autoRenameUTFFile=" + autoRenameUTFFile);
			p.println("useTeraCopyByDefault=" + useTeraCopyByDefault);
			p.println("restoreLastOpenedFavorite=" + restoreLastOpenedFavorite);
			p.println("LoadAllIcon=" + LoadAllIcon);
			p.println("ActiveUser=" + ActiveUser);
			p.println("MaxLimitFilesRecursive=" + MaxLimitFilesRecursive);

			// In General was using Path.toUri() and was all good
			// but after on when creating network location file like:
			// webdav \\192.168.0.104@8080\DavWWWRoot
			// Path.toUri() gives a bad converting (one way):
			// file:////192.168.0.104@8080/DavWWWRoot/
			// File.toURI() gives a 2 ways uri (create and parse):
			// file:////192.168.0.104@8080/DavWWWRoot/
			//
			// As result use file.toFile().toURI()) for network location
			if (LeftLastKnowLocation != null) {
				if (LeftLastKnowLocation.isLocal()) {
					p.println("LeftLastKnowLocation=" + LeftLastKnowLocation.toFileIfLocal().toURI());
				} else {
					p.println("LeftLastKnowLocation=" + LeftLastKnowLocation.toURI());
				}
			} else {
				p.println("LeftLastKnowLocation=null");
			}
			if (RightLastKnowLocation != null) {
				if (RightLastKnowLocation.isLocal()) {
					p.println("RightLastKnowLocation=" + RightLastKnowLocation.toFileIfLocal().toURI());
				} else {
					p.println("RightLastKnowLocation=" + RightLastKnowLocation.toURI());
				}
			} else {
				p.println("RightLastKnowLocation=null");
			}
			p.println("UserNames=" + String.join(PATH_SPLITTER, userNames));
			p.println("lastTheme=" + lastTheme);
			p.println("lastThemeColor=" + lastThemeColor);
			p.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		SettingSaver.saveSetting();
	}

	@SuppressWarnings("deprecation")
	public static void loadSettingPartOne() {
		if (!SETTING_FILE.exists()) {
			saveSetting(); // save initialized setting
			return;
		}
		Scanner scan = null;
		Map<String, String> mapOptions = new HashMap<String, String>();
		try {
			scan = new Scanner(SETTING_FILE);
			scan.nextLine(); // to ignore first comment line
			String line;
			List<String> options;
			while (scan.hasNextLine()) {
				line = scan.nextLine();
				options = Arrays.asList(line.split("="));
				if (options.size() > 1) {
					mapOptions.put(options.get(0), options.get(1));
				} else {
					mapOptions.put(options.get(0), null);
				}
			}
			scan.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		// To read:
		List<String> favoritesTitles = null;
		List<String> favoritesLeftLocs = null;
		List<String> favoritesRightLocs = null;

		for (String key : mapOptions.keySet()) {
			String value = mapOptions.get(key);
			if (value != null && !value.equals("null")) {
				// always overwrite version
				// if (key.equals("Version"))
				// Version = value;
				try {
					if (key.equals("ApplicationTimesLunched")) {
						ApplicationTimesLunched = Long.parseLong(value);
					} else if (key.equals("isMaximized")) {
						isMaximized = Boolean.parseBoolean(value);
					} else if (key.equals("VLCHttpPass")) {
						setVLCHttpPass(value);
					} else if (key.equals("VLCPath")) {
						VLC.setPath_Setup(StringHelper.parseUriToPath(value));
					} else if (key.equals("TeraCopyPath")) {
						TeraCopy.setPath_Setup(StringHelper.parseUriToPath(value));
					} else if (key.equals("BackSync")) {
						BackSync = Boolean.parseBoolean(value);
					} else if (key.equals("autoRenameUTFFile")) {
						autoRenameUTFFile = Boolean.parseBoolean(value);
					} else if (key.equals("useTeraCopyByDefault")) {
						useTeraCopyByDefault = Boolean.parseBoolean(value);
					} else if (key.equals("restoreLastOpenedFavorite")) {
						restoreLastOpenedFavorite = Boolean.parseBoolean(value);
					} else if (key.equals("AutoExpand")) {
						AutoExpand = Boolean.parseBoolean(value);
					} else if (key.equalsIgnoreCase("LoadAllIcon")) {
						LoadAllIcon = Boolean.parseBoolean(value);
					} else if (key.equals("MaxLimitFilesRecursive")) {
						MaxLimitFilesRecursive = Integer.parseInt(value);
					} else if (key.equals("LeftLastKnowLocation")) {
						LeftLastKnowLocation = PathLayerHelper.parseURI(value);
					} else if (key.equals("RightLastKnowLocation")) {
						RightLastKnowLocation = PathLayerHelper.parseURI(value);
					} else if (key.equals("ActiveUser")) {
						ActiveUser = value;
					} else if (key.equals("UserNames")) {
						userNames.clear();
						userNames.addAll(Arrays.asList(value.split(PATH_SPLITTER)));
					} else if (key.equals("lastTheme")) {
						lastTheme = THEME.valueOf(value);
					} else if (key.equals("lastThemeColor")) {
						lastThemeColor = THEME_COLOR.valueOf(value);
					} else if (key.equals("FavoritesTitlesLocations")) {

						// Favorites stuff to save for later and synchronize them
						favoritesTitles = Arrays.asList(value.split(PATH_SPLITTER));

					} else if (key.equals("FavoritesLeftLocations")) {

						favoritesLeftLocs = Arrays.asList(value.split(PATH_SPLITTER));

					} else if (key.equals("FavoritesRightLocations")) {
						favoritesRightLocs = Arrays.asList(value.split(PATH_SPLITTER));
					}
				} catch (Exception e) {
					System.out.println("Something went wrong loading setting");
					e.printStackTrace();
				}
			}
		}
		scan.close();
		if (favoritesTitles != null) {
			favoritesViews.initializeFavoriteViewList(favoritesTitles, favoritesLeftLocs, favoritesRightLocs);
		}
	}

	public static void loadSettingPartTwo(CallBackToDo... onFinishLoadingLateSetting) {
		onFinishLoadingAllPartToDo.add(() -> {
			didLoadedAllPart = true;
			callOnceLater();
		});
		if (onFinishLoadingLateSetting != null) {
			onFinishLoadingAllPartToDo.addAll(Arrays.asList(onFinishLoadingLateSetting));
		}
		onFinishLoadingAllPartToDo.add(() -> didLoadedAllPartAndExecuteRegistredTask = true);
		SettingSaver.loadSetting(onFinishLoadingAllPartToDo);
	}

	/**
	 * If already finish loading setting, it just call action. <br>
	 * other wise it add action to be called later by JavaFx Platform
	 * {@link SettingSaver#loadSetting(List)}<br>
	 * use {@link #isDidLoadedAllPart()} to distinguish between different states
	 *
	 * @param action
	 */
	public static void registerOnFinishLoadingAction(CallBackToDo action) {
		if (didLoadedAllPart) {
			action.call();
		} else {
			onFinishLoadingAllPartToDo.add(action);
		}
	}

	private static void migrateOldSetting() throws IOException {
		File oldSettingfile = new File(System.getenv("APPDATA") + "\\FileTrackerSetting.txt");
		if (oldSettingfile.exists()) {
			oldSettingfile.renameTo(SETTING_FILE);
			oldSettingfile.delete(); // if cannot move it delete it
		}

	}

	public static Boolean isBackSync() {
		return BackSync;
	}

	public static void setBackSync(Boolean backSync) {
		BackSync = backSync;
	}

	public static Boolean getLoadAllIcon() {
		return LoadAllIcon;
	}

	public static void setLoadAllIcon(Boolean loadAllIcon) {
		LoadAllIcon = loadAllIcon;
	}

	public static Boolean getBackSync() {
		return BackSync;
	}

	/**
	 * check for more <a href=
	 * "https://stackoverflow.com/questions/62289/read-write-to-windows-registry-using-java">In
	 * java</a> and <a href=
	 * "https://www.windowscentral.com/how-edit-registry-using-command-prompt-windows-10">In
	 * windows Registry Option</a>
	 */
	public static void AddToContextMenu() {
		DialogHelper.showAlert(AlertType.INFORMATION, "Context Menu Adder", "Context Menu addition",
				"You need admin right to perform this operation\n" + "Note this will add the program to right click so"
						+ " it can directly open the directory from within windows explorer without navigating.");
		File tempFile = null;
		try {
			// this exported after insert via command line
			String add = "Windows Registry Editor Version 5.00\r\n" + "\r\n"
					+ "[HKEY_CLASSES_ROOT\\Directory\\Background\\shell\\Tracker Explorer]\r\n"
					+ "@=\"Tracker Explorer\"\r\n"
					+ "\"Icon\"=hex(2):25,00,6c,00,6f,00,63,00,61,00,6c,00,61,00,70,00,70,00,64,00,61,\\\r\n"
					+ "  00,74,00,61,00,25,00,5c,00,54,00,72,00,61,00,63,00,6b,00,65,00,72,00,20,00,\\\r\n"
					+ "  45,00,78,00,70,00,6c,00,6f,00,72,00,65,00,72,00,5c,00,54,00,72,00,61,00,63,\\\r\n"
					+ "  00,6b,00,65,00,72,00,20,00,45,00,78,00,70,00,6c,00,6f,00,72,00,65,00,72,00,\\\r\n"
					+ "  2e,00,65,00,78,00,65,00,00,00\r\n" + "\r\n"
					+ "[HKEY_CLASSES_ROOT\\Directory\\Background\\shell\\Tracker Explorer\\command]\r\n"
					+ "@=hex(2):22,00,25,00,6c,00,6f,00,63,00,61,00,6c,00,61,00,70,00,70,00,64,00,61,\\\r\n"
					+ "  00,74,00,61,00,25,00,5c,00,54,00,72,00,61,00,63,00,6b,00,65,00,72,00,20,00,\\\r\n"
					+ "  45,00,78,00,70,00,6c,00,6f,00,72,00,65,00,72,00,5c,00,54,00,72,00,61,00,63,\\\r\n"
					+ "  00,6b,00,65,00,72,00,20,00,45,00,78,00,70,00,6c,00,6f,00,72,00,65,00,72,00,\\\r\n"
					+ "  2e,00,65,00,78,00,65,00,22,00,20,00,22,00,25,00,76,00,22,00,00,00\r\n" + "\r\n";
			tempFile = File.createTempFile("TrackerExplorer_Context_Adder", ".reg");
			PrintStream p = new PrintStream(tempFile);
			p.println(add);
			p.close();
			Desktop.getDesktop().open(tempFile);
			tempFile.deleteOnExit();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void RemoveFromContextMenu() {
		DialogHelper.showAlert(AlertType.INFORMATION, "Context Menu Remover", "Context Menu Removal",
				"You need admin right to perform this operation\n"
						+ "Note this will remove the context recently added, Sorry For inconveniance.");
		File tempFile = null;
		try {
			// this exported after insert via command line
			String add = "Windows Registry Editor Version 5.00\r\n" + "\r\n"
					+ "[-HKEY_CLASSES_ROOT\\Directory\\Background\\shell\\Tracker Explorer]\r\n" + "\r\n";
			tempFile = File.createTempFile("TrackerExplorer_Context_Remover", ".reg");
			PrintStream p = new PrintStream(tempFile);
			p.println(add);
			p.close();
			Desktop.getDesktop().open(tempFile);
			tempFile.deleteOnExit();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static PathLayer getLeftLastKnowLocation() {
		return LeftLastKnowLocation;
	}

	public static void setLeftLastKnowLocation(PathLayer LeftLastKnowLocation) {
		Setting.LeftLastKnowLocation = LeftLastKnowLocation;
	}

	public static PathLayer getRightLastKnowLocation() {
		return RightLastKnowLocation;
	}

	public static void setRightLastKnowLocation(PathLayer RightLastKnowLocation) {
		Setting.RightLastKnowLocation = RightLastKnowLocation;
	}

	public static Boolean isAutoExpand() {
		return AutoExpand;
	}

	public static void setAutoExpand(Boolean autoExpand) {
		AutoExpand = autoExpand;
	}

	public static ArrayList<String> getUserNames() {
		return userNames;
	}

	public static void setUserNames(ArrayList<String> userNames) {
		Setting.userNames = userNames;
	}

	public static String getActiveUser() {
		return ActiveUser;
	}

	public static void setActiveUser(String activeUser) {
		ActiveUser = activeUser;
	}

	public static String getVersion() {
		return Version;
	}

	/**
	 * @return the vLCHttpPass
	 */
	public static String getVLCHttpPass() {
		return VLCHttpPass;
	}

	/**
	 * @param vLCHttpPass the vLCHttpPass to set
	 */
	public static void setVLCHttpPass(String vLCHttpPass) {
		VLCHttpPass = vLCHttpPass;
	}

	public static Path getVLCPath() {
		return VLC.getPath_Setup();
	}

	public static FavoriteViewList getFavoritesViews() {
		return favoritesViews;
	}

	/**
	 * @return the autoRenameUTFFile
	 */
	public static boolean isAutoRenameUTFFile() {
		return autoRenameUTFFile;
	}

	/**
	 * @param autoRenameUTFFile the autoRenameUTFFile to set
	 */
	public static void setAutoRenameUTFFile(boolean autoRenameUTFFile) {
		Setting.autoRenameUTFFile = autoRenameUTFFile;
	}

	public static File getmSettingFile() {
		return SETTING_FILE;
	}

	public static void printStackTrace(Exception e) {
		// if (isDebugMode)
		e.printStackTrace();
	}

	public static boolean isDebugMode() {
		return isDebugMode;
	}

	public static void setDebugMode(boolean isDebugMode) {
		Setting.isDebugMode = isDebugMode;
	}

	public static int getMaxLimitFilesRecursive() {
		return MaxLimitFilesRecursive;
	}

	public static void setMaxLimitFilesRecursive(int maxLimitFilesRecursive) {
		MaxLimitFilesRecursive = maxLimitFilesRecursive;
	}

	public static int getMaxDepthFilesRecursive() {
		return MaxDepthFilesRecursive;
	}

	public static void setMaxDepthFilesRecursive(int maxDepthFilesRecursive) {
		MaxDepthFilesRecursive = maxDepthFilesRecursive;
	}

	public static boolean isRestoreLastOpenedFavorite() {
		return restoreLastOpenedFavorite;
	}

	public static void setRestoreLastOpenedFavorite(boolean restoreLastOpenedFavorite) {
		Setting.restoreLastOpenedFavorite = restoreLastOpenedFavorite;
	}

	public static void setAutoCloseClearDoneFileOperation(boolean value) {
		autoCloseClearDoneFileOperation = value;
	}

	public static boolean isAutoCloseClearDoneFileOperation() {
		return autoCloseClearDoneFileOperation;
	}

	/**
	 * @return the useTeraCopyByDefault
	 */
	public static boolean isUseTeraCopyByDefault() {
		return useTeraCopyByDefault && TeraCopy.isWellSetup();
	}

	/**
	 * @param useTeraCopyByDefault the useTeraCopyByDefault to set
	 */
	public static void setUseTeraCopyByDefault(boolean useTeraCopyByDefault) {
		Setting.useTeraCopyByDefault = useTeraCopyByDefault;
	}

	/**
	 * @return the didLoadedAllPart
	 */
	public static boolean isDidLoadedAllPart() {
		return didLoadedAllPart;
	}

	/**
	 * @return the lastOpenedFavoriteTitle
	 */
	public static ArrayList<String> getLastOpenedFavoriteTitle() {
		return lastOpenedFavoriteTitle;
	}

	/**
	 * @param lastOpenedFavoriteTitle the lastOpenedFavoriteTitle to set
	 */
	public static void setLastOpenedFavoriteTitle(ArrayList<String> lastOpenedFavoriteTitle) {
		Setting.lastOpenedFavoriteTitle = lastOpenedFavoriteTitle;
	}

	/**
	 * @return the lastTheme
	 */
	public static THEME getLastTheme() {
		return lastTheme;
	}

	/**
	 * @param lastTheme the lastTheme to set
	 */
	public static void setLastTheme(THEME lastTheme) {
		Setting.lastTheme = lastTheme;
	}

	/**
	 * @return the lastThemeColor
	 */
	public static THEME_COLOR getLastThemeColor() {
		return lastThemeColor;
	}

	/**
	 * @param lastThemeColor the lastThemeColor to set
	 */
	public static void setLastThemeColor(THEME_COLOR lastThemeColor) {
		Setting.lastThemeColor = lastThemeColor;
	}

	/**
	 * @return the isMaximized
	 */
	public static boolean isMaximized() {
		return isMaximized;
	}

	/**
	 * @param isMaximized the isMaximized to set
	 */
	public static void setMaximized(boolean isMaximized) {
		Setting.isMaximized = isMaximized;
	}

	/**
	 * @return the lastOpenedView
	 */
	@Nullable
	public static FavoriteView getLastOpenedView() {
		return lastOpenedView;
	}

	/**
	 * @param lastOpenedView the lastOpenedView to set
	 */
	public static void setLastOpenedView(FavoriteView lastOpenedView) {
		Setting.lastOpenedView = lastOpenedView;
	}

	/**
	 * @return the notifyFilesChanges
	 */
	public static boolean isNotifyFilesChanges() {
		return notifyFilesChanges;
	}

	/**
	 * @param notifyFilesChanges the notifyFilesChanges to set
	 */
	public static void setNotifyFilesChanges(boolean notifyFilesChanges) {
		Setting.notifyFilesChanges = notifyFilesChanges;
	}

	/**
	 * @return the showWindowOnTopWhenNotify
	 */
	public static boolean isShowWindowOnTopWhenNotify() {
		return showWindowOnTopWhenNotify;
	}

	/**
	 * @param showWindowOnTopWhenNotify the showWindowOnTopWhenNotify to set
	 */
	public static void setShowWindowOnTopWhenNotify(boolean showWindowOnTopWhenNotify) {
		Setting.showWindowOnTopWhenNotify = showWindowOnTopWhenNotify;
	}

	public static String getDateFormatPattern() {
		return dateFormatPattern;
	}

	public static void setDateFormatPattern(String dateFormatPattern) {
		Setting.dateFormatPattern = dateFormatPattern;
	}
}
