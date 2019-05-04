package application;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javafx.scene.control.Alert.AlertType;

public class FileTracker {

	// name of file to options of files separated by >
	// (string) name > (int)1or0 watched or not > other later for vlc use resume
	// start > [ remove start > remove end ] mean as alot
	// to options contain the the above line splitted at < and name as key
	// list conventions index:
	// 0 >> name same as key
	// 1 >> boolean isSeen status
	// 2 >> String ToolTip details
	// if exist:
	// 3 >> start time
	// 4 >> end time
	// 5 >> Description of removal
	private Map<String, List<String>> mapDetails;
	private SplitViewController mSplitViewController;
	private static final String BaseName = ".tracker_explorer";
	// do not conflict UserFileName here is mean't by it's file name
	// do change later
	private static String UserFileName = BaseName + ".txt"; // by default
	// this variable was created just to minimize converting dir file to
	// path in general it always = mSplitViewController.getDirectoryPath();
	// the question is does it worth to do ??
	private Path mWorkingDirPath;
	private static String ConflictLog = "";

	public FileTracker(SplitViewController splitViewController) {
		mSplitViewController = splitViewController;
		mapDetails = new HashMap<String, List<String>>();
	}

	public void loadMap() {
		mWorkingDirPath = mSplitViewController.getDirectoryPath();
		String line = "";
		Scanner scan = null;
		try {
			scan = new Scanner(new FileReader(mWorkingDirPath.resolve(UserFileName).toString()));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			// untracked folder
			// System.out.println("this is untracked folder");
			return;
		}
		mapDetails.clear();
		if (scan.hasNextLine())
			scan.nextLine(); // to ignore first comment line
		while (scan.hasNextLine()) {
			line = scan.nextLine();
			List<String> options;
			// we can also use arraylist if indeed:
			// allWords.addAll(Arrays.asList(strTemp.toLowerCase().split("\\s+")));
			// @addinhere
			options = Arrays.asList(line.split(">"));
			if (options.size() >= 3) // additional check of file
				mapDetails.put(options.get(0), options);
			else // trying to quick fix else user must wipe data
				mapDetails.put(options.get(0), Arrays.asList(options.get(0), "0", " "));
		}
		// System.out.println(mapDetails);
		scan.close();
	}

	// if some changes happened to directory this function
	// will clear all useless data in file
	// so files are tracked as long as there is files in the directory
	// this is called in refresh function after new directory is loaded
	// and before button get theirs properties
	// this resolve is bidirectional that mean for each in directroy list
	// if not exist add to file
	// and for each in map not in directory list remove from the file
	public void resolveConflict() {
		if (!this.isTracked())
			return;
		boolean changed = false;
		ArrayList<String> toremove = new ArrayList<>();
		List<String> dirList = Arrays.asList(mSplitViewController.getCurrentFilesList());
		for (String s : dirList) {
			if (!mapDetails.containsKey(s)) {
				// @addinhere
				ConflictLog = "  - New \t" + s + "\n" + ConflictLog;
				changed = true;
				mapDetails.put(s, Arrays.asList(s, "0", " "));
			}
		}

		for (String key : mapDetails.keySet()) {
			if (!dirList.contains(key)) {
				ConflictLog = "  - Del \t" + key + "\n" + ConflictLog;
				changed = true;
				toremove.add(key);
			}
		}
		for (String string : toremove) {
			mapDetails.remove(string);
		}
		// System.out.println("i;m in conflict \n " + mapDetails +"\n out");
		if (changed) {
			ConflictLog = "\n\n* " + Setting.getActiveUser() + " <<>> " + mWorkingDirPath.toString() + "\n"
					+ ConflictLog;
			writeMapDir(mWorkingDirPath, true);
		}

		// this.writeMap();
		// this writeMap enter in infinite loop since it gonna refresh after deleting
		// file in write map so since we have missing name in map it isn't dangerous to
		// wait till user commit a refresh by doing something :)
		// System.out.println("i resolved conflict");
	}

	// this initialize all files in the view folder to 0 watched
	// and save it on map and file
	public boolean trackNewFolder() {
		if (isTracked())
			return true; // prevent wipe old data
		for (String string : mSplitViewController.getCurrentFilesList()) {
			// name, notSeen, empty ToolTip String
			mapDetails.put(string, Arrays.asList(string, "0", " "));
		}
		return writeMap();
		// this is use less because watchable service will detect creating a new file
		// and will refresh automatically
		// mSplitViewController.refresh();
	}

	// this similar to trackNewFolder but the folder isn't opened in the view
	public boolean NewOutFolder(Path DirtoTrack) {
		// it is uncessary to put file in the file
		// because they will be resolved in conflict check later
		File tracker = new File(DirtoTrack.resolve(UserFileName).toString());
		if (tracker.exists())
			return true;// prevent wipe old data
		return writeMapDir(DirtoTrack, false);
	}

	// easy access write map
	// this method is just to easy call write map for the current view
	public boolean writeMap() {
		return writeMapDir(mWorkingDirPath, false);
	}

	// important calling this function is dangerous since it delete a file in
	// current directroy
	// at same time there is a thread watching change in it so be aware to enter in
	// loop
	// @return boolean to confirm that the path is now tracked sometimes there is
	// write permission access
	public boolean writeMapDir(Path DirtoTrack, boolean fromconflict) {
		try {
			// this is not the returned value it just to separate it this
			// function is called using writeMap() easy access
			boolean isEasy = DirtoTrack.equals(mWorkingDirPath);
			WatchServiceHelper.setRuning(false);
			// System.out.println("i'm at satae " + WatchServiceHelper.isRuning());
			File file = new File(DirtoTrack.resolve(UserFileName).toString());
			// https://howtodoinjava.com/java/io/java-write-to-file/
			if (file.exists())
				file.delete();

			BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
			// FileWriter writer=new FileWriter(file,false);
			// check https://www.baeldung.com/java-string-newline
			String content = "/this is a generated folder by application to track file\r\n";
			if (isEasy)
				for (String key : mapDetails.keySet()) {
					content += String.join(">", String.join(">", mapDetails.get(key))) + "\r\n";
				}
			else {
				File[] listFiles = DirtoTrack.toFile().listFiles(outfile -> !outfile.isHidden());
				for (int i = 0; i < listFiles.length; ++i) {
					if (!listFiles[i].getName().equals(UserFileName))
						content += String.join(">", Arrays.asList(listFiles[i].getName(), "0", " ")) + "\r\n";
				}
			}
			Files.setAttribute(file.toPath(), "dos:hidden", true);
			writer.write(content);
			writer.close();

			// ensure refresh for both pages case where they are in the same folder
			if (isEasy && !fromconflict) // this to handle easy access
				mSplitViewController.getParentWelcome().refreshBothViews(mSplitViewController);
			// from conflict parameter is to prevent call function that called this function
			// and get loop
			// refresh -> resolve conflict -> write map ..<<??
			WatchServiceHelper.setRuning(true);

			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// DialogHelper.showException(e);
			// e.printStackTrace();
			DialogHelper.showAlert(AlertType.ERROR, "New Tracker", "SomeThing Went Wrong",
					"Possible reason: - This may be a system protected Folder, Running this application with admin right may fix the problem(depreciated)");
			return false;
		}
	}

	public void ToogleSingleSeenItem(TableViewModel t) {
		Integer intSeen = Integer.parseInt(getSeen(t));
		Integer invertSeen = (intSeen == 0) ? 1 : 0;
		setSeen(t, invertSeen.toString());
	}

	public boolean getAns() {
		return DialogHelper.showConfirmationDialog("Track new Folder", "Ready to Be Stunned ?",
				"Tracking a new Folder will create a hidden file .tracker_explorer.txt in the folder :) !"
						+ "so nothing dangerous just a file !");
	}

	public void toogleSelectionSeen(List<TableViewModel> list, TableViewModel clicked) {
		if (!isTracked()) {
			boolean ans = getAns();
			if (ans) {
				if (!trackNewFolder()) {
					// is something went wrong like access denied
					clicked.getMarkSeen().setSelected(false);
					return;
				}
			} else {
				clicked.getMarkSeen().setSelected(false);
				return;
			}
		}
		ToogleSingleSeenItem(clicked);
		if (list.size() > 1) // to make it easier for user to not mistake
			for (TableViewModel t : list) {
				if (t != clicked)
					ToogleSingleSeenItem(t);
			}
		// System.out.println("i enter here once ");
		writeMap();
	}

	public boolean isTracked() {
		File tracker = new File(mWorkingDirPath.resolve(UserFileName).toString());
		return tracker.exists();
	}

	String getSeen(TableViewModel t) {
		String keyName = t.getName().toString();
		return mapDetails.get(keyName).get(1);
	}

	void setSeen(TableViewModel t, String status) {
		String keyName = t.getName().toString();
		mapDetails.get(keyName).set(1, status);
	}

	String getTooltipText(TableViewModel t) {
		if (isTracked()) {
			String ans = mapDetails.get(t.getName()).get(2);
			return (ans.equals(" ") ? null : ans);
		}
		return null;
	}

	void setTooltipText(TableViewModel t, String note) {
		if (isTracked())
			mapDetails.get(t.getName()).set(2, note);
		this.writeMap();
	}

	public Map<String, List<String>> getMapDetails() {
		return mapDetails;
	}

	public void setMapDetails(Map<String, List<String>> mapDetails) {
		this.mapDetails = mapDetails;
	}

	public static void resetUserFileName() {
		UserFileName = BaseName + ".txt";
	}

	public static String getUserFileName() {
		return UserFileName;
	}

	public static String getFileName(String userName) {
		String ans = "";
		if (userName.equals("default"))
			ans = BaseName + ".txt";
		else
			ans = BaseName + "_" + userName + ".txt";
		return ans;
	}

	public static void updateUserFileName(String userName) {
		Setting.setActiveUser(userName);
		UserFileName = getFileName(userName);
	}

	public void deleteFile() {
		// TODO Auto-generated method stub
		File file = new File(mWorkingDirPath.resolve(UserFileName).toString());
		if (file.exists())
			file.delete();
	}

	public static void deleteOutFile(Path Dirto, String user) {
		// TODO Auto-generated method stub
		File tracker = new File(Dirto.resolve(getFileName(user)).toString());
		if (tracker.exists())
			tracker.delete();
	}

	public static String getConflictLog() {
		return ConflictLog;
	}

	public static void setConflictLog(String conflictLog) {
		ConflictLog = conflictLog;
	}
}
