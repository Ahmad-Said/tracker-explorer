package application;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javafx.scene.control.Alert.AlertType;

public class Setting {
	// create later a controller for setting and fxml also
	// this things are temporary solution later will use :
	// lightbend refrence.conf see testimagewthatable project
	// these initial definition are even if not initialized
	private static Boolean BackSync = false;
	private static Boolean AutoExpand = true;
	private static Boolean LoadAllIcon = true;
	private static Path LeftLastKnowLocation = null;
	private static Path RightLastKnowLocation = null;
	private static Boolean ShowLeftNotesColumn = false;
	private static Boolean ShowRightNotesColumn = false;
	private static String ActiveUser = "default";
	private static ArrayList<String> UserNames = new ArrayList<String>();
	/**
	 * backsync > BackSync loadallicon > LoadAllIcon LeftLastKnowLocation >
	 * LeftLastKnowLocation > RightLastKnowLocation
	 * 
	 */
	private static Map<String, String> mapOptions = new HashMap<String, String>();
	private static List<String> extIconLoad;
	private static List<String> Recentlocation;
	private static File mSettingFile = new File(System.getenv("APPDATA") + "\\FileTrackerSetting.txt");

	public static void initializeSetting() {
		BackSync = false;
		AutoExpand = true;
		LoadAllIcon = true;
		LeftLastKnowLocation = null;
		RightLastKnowLocation = null;
		ShowLeftNotesColumn = false;
		ShowRightNotesColumn = false;
		ActiveUser = "default";
		// UserNames = new ArrayList<String>() {
		// {
		// add("default");
		// }
		// };
		UserNames.add("default");
	}

	public static void saveSetting() {
		PrintStream p = null;
		try {
			if (mSettingFile.exists())
				mSettingFile.delete();
			p = new PrintStream(getmSettingFile().toString());
			Files.setAttribute(getmSettingFile().toPath(), "dos:hidden", true);
			p.println("/this is a generated folder by application to Save Setting");
			p.println("BackSync=" + BackSync);
			p.println("AutoExpand=" + AutoExpand);
			p.println("LoadAllIcon=" + LoadAllIcon);
			p.println("ActiveUser=" + ActiveUser);
			p.println("ShowLeftNotesColumn=" + ShowLeftNotesColumn);
			p.println("ShowRightNotesColumn=" + ShowRightNotesColumn);
			
			if (LeftLastKnowLocation != null)
				p.println("LeftLastKnowLocation=" + LeftLastKnowLocation.toUri().toString());
			else
				p.println("LeftLastKnowLocation=null");
			if (RightLastKnowLocation != null)
				p.println("RightLastKnowLocation=" + RightLastKnowLocation.toUri().toString());
			else
				p.println("RightLastKnowLocation=null");
			p.println("UserNames=" + String.join(";", UserNames));
			p.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
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
				mapOptions.put(options.get(0), options.get(1));
			}
			scan.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		// those assuming they are in order
		// very bad as same as quicly on the go just for now
		for (String key : mapOptions.keySet()) {
			String value = mapOptions.get(key);
			if (!value.equals("null")) {
				if (key.equals("BackSync"))
					BackSync = Boolean.parseBoolean(value);
				if (key.equals("AutoExpand"))
					AutoExpand = Boolean.parseBoolean(value);
				if (key.equalsIgnoreCase("LoadAllIcon"))
					LoadAllIcon = Boolean.parseBoolean(value);
				if (key.equalsIgnoreCase("ShowLeftNotesColumn"))
					ShowLeftNotesColumn = Boolean.parseBoolean(value);
				if (key.equalsIgnoreCase("ShowRightNotesColumn"))
					ShowRightNotesColumn = Boolean.parseBoolean(value);
				if (key.equals("LeftLastKnowLocation"))
					LeftLastKnowLocation = Paths.get(URI.create(value));
				if (key.equals("RightLastKnowLocation"))
					RightLastKnowLocation = Paths.get(URI.create(value));
				if (key.equals("ActiveUser"))
					ActiveUser = value;
				if (key.equals("UserNames")) {
					UserNames.clear();
					UserNames.addAll(Arrays.asList(value.split(";")));
				}
			}
		}
		scan.close();
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
	 * @return the mSettingFile
	 */
	public static File getmSettingFile() {
		return mSettingFile;
	}

	/**
	 * @param mSettingFile
	 *            the mSettingFile to set
	 */
	public void setmSettingFile(File msettingFile) {
		mSettingFile = msettingFile;
	}

	/**
	 * @return the extIconLoad
	 */
	public static List<String> getExtIconLoad() {
		return extIconLoad;
	}

	/**
	 * @param extIconLoad
	 *            the extIconLoad to set
	 */
	public static void setExtIconLoad(List<String> extIconLoad) {
		Setting.extIconLoad = extIconLoad;
	}

	/**
	 * @return the recentlocation
	 */
	public static List<String> getRecentlocation() {
		return Recentlocation;
	}

	/**
	 * @param recentlocation
	 *            the recentlocation to set
	 */
	public static void setRecentlocation(List<String> recentlocation) {
		Recentlocation = recentlocation;
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
			// TODO Auto-generated catch block
			// e.printStackTrace();
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
			// TODO Auto-generated catch block
			// e.printStackTrace();
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
	// // TODO Auto-generated catch block
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
	// // TODO Auto-generated catch block
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

	public static Boolean getAutoExpand() {
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

}
