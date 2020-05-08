package application.datatype;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import application.DialogHelper;
import application.StringHelper;
import application.system.call.RunMenu;
import application.system.call.TeraCopy;
import application.system.services.VLC;
import javafx.scene.control.Alert.AlertType;

public class Setting {
	// create later a controller for setting and fxml also
	// this things are temporary solution later will use :
	// lightbend refrence.conf see testimagewthatable project
	// these initial definition are even if not initialized
	private static String Version = "5.0";
	private static Boolean BackSync = false;
	private static Boolean AutoExpand = true;
	private static Boolean LoadAllIcon = true;
	private static Path LeftLastKnowLocation = null;
	private static Path RightLastKnowLocation = null;
	private static Boolean ShowLeftNotesColumn = false;
	private static Boolean ShowRightNotesColumn = false;
	private static String ActiveUser = "default";
	private static String VLCHttpPass = "1234";
	private static int MaxLimitFilesRecursive = 10000;
	private static int MaxDepthFilesRecursive = 5;
	private static boolean isDebugMode = false;
	private static boolean autoRenameUTFFile = false;
	private static boolean useTeraCopyByDefault = false;
	private static boolean autoCloseClearDoneFileOperation = true;

	private static boolean restoreLastOpenedFavorite = true;
	private static ArrayList<Integer> lastOpenedFavoriteIndex = new ArrayList<Integer>();

	private static ArrayList<String> UserNames = new ArrayList<String>();
	private static FavoriteViewList FavoritesLocations = new FavoriteViewList();
	/**
	 * backsync > BackSync loadallicon > LoadAllIcon LeftLastKnowLocation >
	 * LeftLastKnowLocation > RightLastKnowLocation
	 *
	 */
	private static Map<String, String> mapOptions = new HashMap<String, String>();
	private static File mSettingFile = new File(
			System.getenv("APPDATA") + "\\Tracker Explorer\\TrackerExplorerSetting.txt");
	public static File SETTING_DIRECTORY = new File(System.getenv("APPDATA") + "\\Tracker Explorer");

	public static void initializeSetting() {
		Version = "5.0";
		BackSync = false;
		AutoExpand = true;
		LoadAllIcon = true;

		LeftLastKnowLocation = null;
		RightLastKnowLocation = null;
		ShowLeftNotesColumn = false;
		ShowRightNotesColumn = false;
		ActiveUser = "default";
		MaxLimitFilesRecursive = 10000;
		MaxDepthFilesRecursive = 5;
		setVLCHttpPass("1234");

		VLC.initializeDefaultVLCPath();
		TeraCopy.initializeDefaultVLCPath();

		UserNames.add("default");

		isDebugMode = false;
		autoRenameUTFFile = false;
		useTeraCopyByDefault = false;
		autoCloseClearDoneFileOperation = true;
		restoreLastOpenedFavorite = true;

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
			if (mSettingFile.exists()) {
				mSettingFile.delete();
			}
			p = new PrintStream(mSettingFile.toString());
			Files.setAttribute(mSettingFile.toPath(), "dos:hidden", true);
			p.println("/this is a generated folder by application to Save Setting");
			p.println("Version=" + Version);
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
			p.println("ShowLeftNotesColumn=" + ShowLeftNotesColumn);
			p.println("ShowRightNotesColumn=" + ShowRightNotesColumn);
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
				p.println("LeftLastKnowLocation=" + LeftLastKnowLocation.toFile().toURI());
			} else {
				p.println("LeftLastKnowLocation=null");
			}
			if (RightLastKnowLocation != null) {
				p.println("RightLastKnowLocation=" + RightLastKnowLocation.toFile().toURI());
			} else {
				p.println("RightLastKnowLocation=null");
			}
			p.println("UserNames=" + String.join(";", UserNames));
			p.println("lastOpenedFavoriteIndex=" + String.join(";",
					lastOpenedFavoriteIndex.stream().map(m -> m.toString()).collect(Collectors.toList())));

			// check https://winterbe.com/posts/2014/07/31/java8-stream-tutorial-examples/
			p.println("FavoritesTitlesLocations="
					+ FavoritesLocations.getTitle().stream().map(s -> s).collect(Collectors.joining(";")));

			p.println("FavoritesLeftLocations=" + FavoritesLocations.getLeftLoc().stream()
					.map(s -> s.toURI().toString()).collect(Collectors.joining(";")));

			p.println("FavoritesRightLocations=" + FavoritesLocations.getRightLoc().stream()
					.map(s -> s.toURI().toString()).collect(Collectors.joining(";")));
			p.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void loadSetting() {
		initializeSetting();
		if (!mSettingFile.exists()) {
			saveSetting(); // save initialized setting
			return;
		}
		Scanner scan = null;
		try {
			scan = new Scanner(mSettingFile);
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
					if (key.equals("VLCHttpPass")) {
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
					} else if (key.equalsIgnoreCase("ShowLeftNotesColumn")) {
						ShowLeftNotesColumn = Boolean.parseBoolean(value);
					} else if (key.equalsIgnoreCase("ShowRightNotesColumn")) {
						ShowRightNotesColumn = Boolean.parseBoolean(value);
					} else if (key.equals("MaxLimitFilesRecursive")) {
						MaxLimitFilesRecursive = Integer.parseInt(value);
					} else if (key.equals("LeftLastKnowLocation")) {
						LeftLastKnowLocation = StringHelper.parseUriToPath(value);
					} else if (key.equals("RightLastKnowLocation")) {
						RightLastKnowLocation = StringHelper.parseUriToPath(value);
					} else if (key.equals("ActiveUser")) {
						ActiveUser = value;
					} else if (key.equals("UserNames")) {
						UserNames.clear();
						UserNames.addAll(Arrays.asList(value.split(";")));
					} else if (key.equals("lastOpenedFavoriteIndex")) {
						lastOpenedFavoriteIndex.clear();
						Arrays.asList(value.split(";")).stream().mapToInt(m -> Integer.parseInt(m))
								.forEach(e -> lastOpenedFavoriteIndex.add(e));

					} else if (key.equals("FavoritesTitlesLocations")) {

						// Favorites stuff to save for later and synchronize them
						favoritesTitles = Arrays.asList(value.split(";"));

					} else if (key.equals("FavoritesLeftLocations")) {

						favoritesLeftLocs = Arrays.asList(value.split(";"));

					} else if (key.equals("FavoritesRightLocations")) {
						favoritesRightLocs = Arrays.asList(value.split(";"));
					}
				} catch (Exception e) {
					System.out.println("Something went wrong loading setting");
					e.printStackTrace();
				}
			}
		}
		scan.close();
		FavoritesLocations.initializeFavoriteViewList(favoritesTitles, favoritesLeftLocs, favoritesRightLocs);
	}

	public static void migrateOldSetting() throws IOException {
		File oldSettingfile = new File(System.getenv("APPDATA") + "\\FileTrackerSetting.txt");
		if (oldSettingfile.exists()) {
			oldSettingfile.renameTo(mSettingFile);
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

	// https://stackoverflow.com/questions/4350356/detect-if-java-application-was-run-as-a-windows-admin
	// used for old way
	// public static boolean isAdmin() {
	// try {
	// ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe");
	// Process process = processBuilder.start();
	// PrintStream printStream = new PrintStream(process.getOutputStream(), true);
	// Scanner scanner = new Scanner(process.getInputStream());
	// printStream.println("@echo off");
	// printStream.println(
	// ">nul 2>&1 \"%SYSTEMROOT%\\system32\\cacls.exe\"
	// \"%SYSTEMROOT%\\system32\\config\\system\"");
	// printStream.println("echo %errorlevel%");
	//
	// boolean printedErrorlevel = false;
	// while (true) {
	// String nextLine = scanner.nextLine();
	// if (printedErrorlevel) {
	// int errorlevel = Integer.parseInt(nextLine);
	// return errorlevel == 0;
	// } else if (nextLine.equals("echo %errorlevel%")) {
	// printedErrorlevel = true;
	// }
	// }
	// } catch (IOException e) {
	// return false;
	// }
	// }

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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// old way require starting the application as admin instead do make a reg file
	// and start it
	// public static void AddToContextMenu() {
	// if (isAdmin())
	// try {
	// // this need admin in process canceled will create a temporary reg file
	// String add = "reg add
	// \"HKEY_CLASSES_ROOT\\Directory\\Background\\shell\\Tracker Explorer\" /ve /d
	// \"Tracker Explorer\"";
	// Process p = Runtime.getRuntime().exec(add);
	// p.waitFor();
	// p.destroy();
	// add = "reg add \"HKEY_CLASSES_ROOT\\Directory\\Background\\shell\\Tracker
	// Explorer\" /v \"Icon\" /t REG_EXPAND_SZ /d \"%localappdata%\\Tracker
	// Explorer\\Tracker Explorer.exe\"";
	// p = Runtime.getRuntime().exec(add);
	// p.waitFor();
	// p.destroy();
	// add = "reg add \"HKEY_CLASSES_ROOT\\Directory\\Background\\shell\\Tracker
	// Explorer\\command\" /ve /t REG_EXPAND_SZ /d \"\\\"%localappdata%\\Tracker
	// Explorer\\Tracker Explorer.exe\\\" \\\"%v\\\"\"";
	// p = Runtime.getRuntime().exec(add);
	// p.waitFor();
	// p.destroy();
	//
	// DialogHelper.showAlert(AlertType.INFORMATION, "Add Context Menu", "Context
	// Menu Added Successfully",
	// "You can now right click anywhere and open me,Thanks you.");
	// } catch (IOException | InterruptedException e) {
	// // e.printStackTrace();
	// }
	// else
	// DialogHelper.showAlert(AlertType.ERROR, "Add To context Menu", "Access
	// Denied",
	// "You need admin right to perform operation\nClose this application and
	// restart"
	// + " it as admin then try again.\nNote this will add the program to right
	// click so"
	// + " it can directly open the directory from within windows explorer without
	// navigating.");
	// }
	//
	// public static void RemoveFromContextMenu() {
	//
	// if (isAdmin())
	// try {
	// // this need admin in process canceled will create a temporary reg file
	// String add = "reg delete
	// \"HKEY_CLASSES_ROOT\\Directory\\Background\\shell\\Tracker Explorer\" /f";
	// Process p = Runtime.getRuntime().exec(add);
	// p.waitFor();
	// p.destroy();
	// DialogHelper.showAlert(AlertType.INFORMATION, "Remove Context Menu", "Context
	// Removed Successfully",
	// "Action rolled Back,Sorry For inconveniance\nThanks you.");
	// } catch (IOException | InterruptedException e) {
	// // e.printStackTrace();
	// }
	// else
	// DialogHelper.showAlert(AlertType.ERROR, "Add To context Menu", "Access
	// Denied",
	// "You need admin right to perform operation\nClose this application and
	// restart"
	// + " it as admin then try again.\nNote this will add the program to right
	// click so"
	// + " it can directly open the directory from within windows explorer without
	// navigating.");
	// }

	public static Path getLeftLastKnowLocation() {
		return LeftLastKnowLocation;
	}

	public static void setLeftLastKnowLocation(Path LeftLastKnowLocation) {
		Setting.LeftLastKnowLocation = LeftLastKnowLocation;
	}

	public Map<String, String> getMapOptions() {
		return mapOptions;
	}

	public void setMapOptions(Map<String, String> mapoptions) {
		mapOptions = mapoptions;
	}

	public static Path getRightLastKnowLocation() {
		return RightLastKnowLocation;
	}

	public static void setRightLastKnowLocation(Path RightLastKnowLocation) {
		Setting.RightLastKnowLocation = RightLastKnowLocation;
	}

	public static Boolean isAutoExpand() {
		return AutoExpand;
	}

	public static void setAutoExpand(Boolean autoExpand) {
		AutoExpand = autoExpand;
	}

	public static ArrayList<String> getUserNames() {
		return UserNames;
	}

	public static void setUserNames(ArrayList<String> userNames) {
		UserNames = userNames;
	}

	public static String getActiveUser() {
		return ActiveUser;
	}

	public static void setActiveUser(String activeUser) {
		ActiveUser = activeUser;
	}

	public static Boolean getShowLeftNotesColumn() {
		return ShowLeftNotesColumn;
	}

	public static void setShowLeftNotesColumn(Boolean showLeftNotesColumn) {
		ShowLeftNotesColumn = showLeftNotesColumn;
	}

	public static Boolean getShowRightNotesColumn() {
		return ShowRightNotesColumn;
	}

	public static void setShowRightNotesColumn(Boolean showRightNotesColumn) {
		ShowRightNotesColumn = showRightNotesColumn;
	}

	public static String getVersion() {
		return Version;
	}

	public static void setVersion(String version) {
		Version = version;
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

	public static FavoriteViewList getFavoritesLocations() {
		return FavoritesLocations;
	}

	public static void setFavoritesLocations(FavoriteViewList favoritesLocations) {
		FavoritesLocations = favoritesLocations;
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
	public static void setAutoRenameUTFFile(boolean autorenameUTFFile) {
		autoRenameUTFFile = autorenameUTFFile;
	}

	public static File getmSettingFile() {
		return mSettingFile;
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

	public static ArrayList<Integer> getLastOpenedFavoriteIndex() {
		return lastOpenedFavoriteIndex;
	}

	public static void setLastOpenedFavoriteIndex(ArrayList<Integer> lastOpenedFavoriteIndex) {
		Setting.lastOpenedFavoriteIndex = lastOpenedFavoriteIndex;
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
}
