package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import application.controller.SplitViewController;
import application.datatype.Setting;
import application.model.TableViewModel;

public class FileTracker {

	private static final String BaseName = ".tracker_explorer";
	// conflict log will discard creating new File/Folder operations
	private static String ConflictLog = "";
	// do not conflict UserFileName here is mean't by it's file name
	// do change later
	private static String UserFileName = BaseName + ".txt"; // by default

	public static void deleteOutFile(Path Dirto, String user) {
		File tracker = new File(Dirto.resolve(getFileName(user)).toString());
		if (tracker.exists()) {
			tracker.delete();
		}
	}

	public static String getConflictLog() {
		return ConflictLog;
	}

	public static String getFileName(String userName) {
		String ans = "";
		if (userName.equals("default")) {
			ans = BaseName + ".txt";
		} else {
			ans = BaseName + "_" + userName + ".txt";
		}
		return ans;
	}

	public static String getUserFileName() {
		return UserFileName;
	}

	public static void resetUserFileName() {
		UserFileName = BaseName + ".txt";
	}

	public static void setConflictLog(String conflictLog) {
		ConflictLog = conflictLog;
	}

	public static void updateUserFileName(String userName) {
		Setting.setActiveUser(userName);
		UserFileName = getFileName(userName);
	}

	// name of file to options of files separated by >
	// (string) name > (int)1or0 watched or not > other later for vlc use resume
	// start > [ remove start > remove end ] mean as alot
	// to options contain the the above line splitted at < and name as key
	// list conventions index:
	// 0 >> name same as key (path.getFilename)
	// 1 >> boolean isSeen status
	// 2 >> String ToolTip details
	// if exist:
	// 3 >> start time
	// 4 >> end time
	// 5 >> Description of removal
	private Map<String, List<String>> mapDetails;

	private SplitViewController mSplitViewController;

	// this variable was created just to minimize converting dir file to
	// path in general it always = mSplitViewController.getDirectoryPath();
	// the question is does it worth to do ??
	private Path mWorkingDirPath;

	public Path getWorkingDir() {
		return mWorkingDirPath;
	}

	public void setWorkingDir(Path p) {
		mWorkingDirPath = p;
	}

	/**
	 * this is used to define a virtual miniFileTracker if you wish to use Full URI
	 * path for loadMap do change use {@link #setVirtualModeToFullURIPathKey()} to
	 * true
	 *
	 * @param workingPath
	 */
	public FileTracker(Path workingPath) {
		mapDetails = new HashMap<String, List<String>>();
		mSplitViewController = new SplitViewController();
		mWorkingDirPath = workingPath;
		mSplitViewController.setmDirectory(workingPath.toFile());
	}

	public void setVirtualModeToFullURIPathKey() {
		mSplitViewController.setIsOutOfTheBoxHelper(true);
	}

	public FileTracker(SplitViewController splitViewController) {
		mSplitViewController = splitViewController;
		mapDetails = new HashMap<String, List<String>>();
		// in case of definition of virtual minifiletracker we do use
		// just the same as initial value as calling resolve conflict do need it
		mWorkingDirPath = mSplitViewController.getDirectoryPath();
	}

	public void deleteFile() {
		File file = new File(mWorkingDirPath.resolve(UserFileName).toString());
		if (file.exists()) {
			file.delete();
		}
	}

	public boolean getAns() {
		return DialogHelper.showConfirmationDialog("Track new Folder", "Ready to Be Stunned ?",
				"Tracking a new Folder will create a hidden file .tracker_explorer.txt in the folder :) !"
						+ "so nothing dangerous just a file !");
	}

	public Map<String, List<String>> getMapDetails() {
		return mapDetails;
	}

	/**
	 * in normal case key is {@link TableViewModel#getName()} Other wise is out of
	 * the box it is {@link TableViewModel#getmFilePath()}
	 */
	public String getNoteTooltipText(String key) {
		if (!mapDetails.containsKey(key)) {
			return "";
		}
		String ans = mapDetails.get(key).get(2);
		return ans.equals(" ") ? "" : ans;
	}

	public String getSeen(String key) {
		return mapDetails.get(key).get(1);
	}

	public String getSeen(TableViewModel t) {
		return getSeen(t.getName());
	}

	public boolean isSeen(String key) {
		return getSeen(key).equals("0") ? false : true;
	}

	public boolean isTracked() {
		File tracker = new File(mWorkingDirPath.resolve(UserFileName).toString());
		return tracker.exists();
	}

	public boolean isTrackedOutFolder(Path Dirto) {
		File tracker = new File(Dirto.resolve(UserFileName).toString());
		return tracker.exists();
	}

	private boolean isVirtual() {
		boolean virtualSplitView = false;
		if (mSplitViewController.getmWatchServiceHelper() == null) {
			virtualSplitView = true;
		}
		return virtualSplitView;
	}

	public void loadMap(Path Dirpath, boolean doclear, boolean doFullpath) {
		String line = "";
		// every time get the new path before loading in normal case
		mWorkingDirPath = mSplitViewController.getDirectoryPath();
		File file = Dirpath.resolve(UserFileName).toFile();
		if (!file.exists()) {
			return;
		}
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
		} catch (UnsupportedEncodingException | FileNotFoundException e1) {
			// TODO Auto-generated catch block
			Setting.printStackTrace(e1);
		}

		if (doclear) {
			mapDetails.clear();
		}

		try {
			in.readLine(); // to ignore first comment line
			while ((line = in.readLine()) != null) {
				List<String> options;
				// we can also use arraylist if indeed:
				// allWords.addAll(Arrays.asList(strTemp.toLowerCase().split("\\s+")));
				// @addinhere
				options = Arrays.asList(line.split(">"));
				// by default the key of the map is the name which is saved to map
				// other wise if full path key is the URI path of the file
				String keyInMap = options.get(0);
				try {
					if (doFullpath) {
						// Add here unsupported character
						if (options.get(0).contains("?")) {
							continue;
						}
						try {
							Path test = Dirpath.resolve(options.get(0));
							keyInMap = test.toFile().toURI().toString();
						} catch (java.nio.file.InvalidPathException e) {
							// if path is valid
							Setting.printStackTrace(e);
							continue;
						}
					}
					if (options.size() >= 3) {
						mapDetails.put(keyInMap, options);
					} else {
						mapDetails.put(keyInMap, Arrays.asList(options.get(0), "0", " "));
					}
				} catch (Exception e) {
					// if anything goes wrong in parsing do nothing about it
					Setting.printStackTrace(e);
				}
			}
			in.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	// this similar to trackNewFolder but the folder isn't opened in the view
	// return false if nothing is changed
	public boolean NewOutFolder(Path DirtoTrack) {
		// it is uncessary to put file in the file
		// because they will be resolved in conflict check later
		File tracker = new File(DirtoTrack.resolve(UserFileName).toString());
		if (tracker.exists()) {
			return false;// prevent wipe old data
		}
		return writeMapDir(DirtoTrack, false, false);
	}

	public void updateMapDetailsUponRename(String oldKey, String newKey, String newFileName) {
		List<String> options = mapDetails.get(oldKey);
		mapDetails.remove(oldKey);
		options.set(0, newFileName);
		mapDetails.put(newKey, options);
	}

	/**
	 * when doing a copy or move operations: The purpose is to preserve the tracker
	 * data when copying or moving and in case of delete do not track change in
	 * conflict log
	 *
	 * "this" fileTracker is the one who receiving the files noting also the
	 * Receiver always do receive in the box
	 *
	 *
	 * @param keyChangedSender is the final path considered as the target file his
	 *                         parent can stand for the sender file tracker who
	 *                         sending the files in case of delete or move we need
	 *                         to remove the key can be null in case of rename in
	 *                         case of rename it just the new file in same directory
	 *
	 * @param args             Arrays of string arguments in case of:<br>
	 *                         rename:<br>
	 *                         * 0 ->> is The original name <br>
	 *                         *1 ->> is to be the new key <br>
	 *                         <br>
	 *
	 *                         * 0 ->> otherwise "action" -> "copy","move","delete"
	 *                         // note get the original name from path keyChanged
	 */
	public void operationUpdate(Path keyChangedSender, String... args) {
		if (!isTrackedOutFolder(keyChangedSender.getParent())) {
			return;
		}
		if (args.length > 1) {
			// rename action
			if (!mSplitViewController.isOutOfTheBoxHelper()) {
				updateMapDetailsUponRename(args[0], args[1], args[1]);
				writeMapDir(mWorkingDirPath, false);
			} else {
				updateMapDetailsUponRename(keyChangedSender.toFile().toURI().toString(),
						keyChangedSender.getParent().resolve(args[1]).toFile().toURI().toString(), args[1]);
				OutofTheBoxWriteMap(keyChangedSender.getParent(), null, args);
				// additional refresh in case of change isn't the same as root
				// directory in case
				if (!keyChangedSender.getParent().equals(mWorkingDirPath) && !isVirtual()) {
					mSplitViewController.refreshAsPathField();
				}
			}
			// will auto detect by watch service
			// mSplitViewController.refreshAsPathField();
			return;
		}
		// case of delete
		// TODO at the moment operations aren't removed from conflict log
		if (args[0].equals("delete")) {
			return;
		}
		// case of copy and move:
		if (!isTrackedOutFolder(keyChangedSender.getParent())) {
			return;
		}
		// System.out.println(mapDetails);
		String keyName = keyChangedSender.toFile().getName();
		// getting the sender data
		FileTracker senderfileTracker = new FileTracker(keyChangedSender.getParent());
		senderfileTracker.loadMap(keyChangedSender.getParent(), true, false);

		// overwriting current data
		mapDetails.put(keyName, senderfileTracker.getMapDetails().get(keyName));
		writeMapDir(mWorkingDirPath, false, false);
	}

	/**
	 * Copy_Move_Delete_Operations only
	 *
	 * When doing a copy or move operations: It is necessary to preserve the tracker
	 * data, and in case of delete do nothing about it. But clearing from conflict
	 * log is optional
	 *
	 * "this" fileTracker is the one who receiving the files
	 *
	 * @param source
	 * @param otherfileTracker who sending the files
	 */
	public void OperationUpdate(List<Path> source, FileTracker otherfileTracker, String operation) {
		if (otherfileTracker.mSplitViewController.isOutOfTheBoxHelper()) {
			// System.out.println("i;m out of the box");
			// TODO update item after finishing copy or move of every file in file helper

			return;
		}
		mSplitViewController.setPredictNavigation("");
		if (!isTracked() || otherfileTracker != null && !otherfileTracker.isTracked()) {
			// care of your own busniss
			// there is 2 refresh now one from here and other from file helper
			// mSplitViewController.getParentWelcome().refreshBothViewsAsPathField(null);
			return;
		}
		if (operation.equals("delete")) {
			for (Path src : source) {
				String key = src.getFileName().toString();
				mapDetails.remove(key);
			}
		} else {
			for (Path src : source) {
				String key = src.getFileName().toString();
				mapDetails.put(key, otherfileTracker.getMapDetails().get(key));
				if (operation.equals("move")) {
					otherfileTracker.mapDetails.remove(key);
				}
			}
		}

		// will do refresh after moving all files
		// these write will make them undetectable by resolve conflict
		writeMapDir(mWorkingDirPath, false, false);
		if (otherfileTracker != null) {
			otherfileTracker.writeMapDir(otherfileTracker.mWorkingDirPath, false, false);
		}
	}

	public void OutofTheBoxAddToMapRecusive(Path Dirto) {
		File tracker = new File(Dirto.resolve(UserFileName).toString());
		if (tracker.exists()) {
			// SplitViewController minisplit = new SplitViewController();
			// minisplit.setmDirectory(Dirto.toFile());
			FileTracker miniFileTracker = new FileTracker(Dirto);
			// loading the original map to resolve conflict -> detecting new files..
			// this will use easy call in write map
			miniFileTracker.loadMap(Dirto, true, false);
			miniFileTracker.resolveConflict();
			loadMap(Dirto, false, true);
			// TODO clean recursively and track recursively do refresh folders alot
		} else {
			// add untracked data folder here require listing dir again
			File allFiles[] = Dirto.toFile().listFiles(file -> !file.isHidden());
			// System.out.println("tracking for " + Dirto);
			if (allFiles == null || allFiles.length == 0) {
				return;
			}
			List<File> dirList = Arrays.asList(allFiles);
			StringHelper.SortArrayFiles(dirList);
			for (File f : dirList) {
				try {
					if (f.getName().contains("?")) {
						continue;
					}

					mapDetails.put(f.toURI().toString(), Arrays.asList(f.getName(), "0", ""));
				} catch (Exception e) {
					// checking that path contain legal character other wise skip them
					Setting.printStackTrace(e);
					continue;
				}
			}
		}
	}

	private void OutofTheBoxChangeHelper(List<TableViewModel> list, TableViewModel clicked, String Operation,
			String optionalArg) {
		// map from each directory path to it's corresponding element
		// the list of table view model contain name
		Map<Path, List<TableViewModel>> toUpdate = new HashMap<>();
		// including clicked into the list
		Set<TableViewModel> Alllist = new HashSet<>(list);
		if (Alllist.size() == 1) {
			Alllist.clear();
		}
		if (!Alllist.contains(clicked)) {
			Alllist.add(clicked);
		}

		Boolean ans = null;
		for (TableViewModel t : Alllist) {
			Path parent = t.getmFilePath().getParent();
			if (parent == null) {
				continue;
			}
			if (!isTrackedOutFolder(parent)) {
				if (ans == null) {
					ans = DialogHelper.showConfirmationDialog("Recursive Operation", "Untracked Files Selection",
							"We have detetected some Untracked Files in selection,\nDo you want To Track Them and include In The operation ?");
				}
				if (ans) {
					NewOutFolder(parent);
				}
			}
			String key = t.generateKeyURI();
			if (Operation.equals("toogle_seen")) {
				toggleSingleSeenItem(key, t);
			} else if (Operation.equals("set_tooltip")) {
				OutofTheBoxsetsetTooltipTextHelper(t, key, optionalArg);
			}

			List<TableViewModel> TList = toUpdate.get(parent);

			// if list does not exist create it
			if (TList == null) {
				TList = new ArrayList<TableViewModel>();
				TList.add(t);
				toUpdate.put(parent, TList);
			} else {
				// optional additional check
				// add if item is not already in list
				if (!TList.contains(t)) {
					TList.add(t);
				}
			}
		}
		for (Path path : toUpdate.keySet()) {
			OutofTheBoxWriteMap(path, toUpdate.get(path));
		}
		// mSplitViewController.setPathFieldThenRefresh(mSplitViewController.getPathField().getText());
		// restore later when exit the recursive search
		// mSplitViewController.getmWatchServiceHelper().RestoreObservableDirectory();

	}

	private void OutofTheBoxsetsetTooltipTextHelper(TableViewModel t, String key, String note) {
		t.setNoteText(note);
		mapDetails.get(key).set(2, note);
	}

	public void OutofTheBoxsetTooltipsTexts(List<TableViewModel> list, TableViewModel clicked, String note) {
		OutofTheBoxChangeHelper(list, clicked, "set_tooltip", note);
	}

	public void OutofTheBoxtoggleSelectionSeen(List<TableViewModel> list, TableViewModel clicked) {
		OutofTheBoxChangeHelper(list, clicked, "toogle_seen", null);
	}

	public void OutofTheBoxTrackFolder(Set<Path> paths) {
		for (Path path : paths) {
			if (NewOutFolder(path)) {
				loadMap(path, false, true);
			}
		}
	}

	// this is called if any change have committed to the dirto
	// toUpdate is used if action on any Visual Button like toggleseen or note..
	// NameChanger is used in operation update in case of rename to perserve data
	/**
	 *
	 *
	 * @param Dirto    The working directory containing the list of TableViewModel
	 *                 files
	 * @param toUpdate List to take in consideration that their status
	 *                 seen/note..<br>
	 *                 can be null if only use of "args" parameter
	 * @param args     Used for rename/copy/move operation check more at
	 *                 {@link #operationUpdate(Path, String...)}
	 */
	public void OutofTheBoxWriteMap(Path Dirto, List<TableViewModel> toUpdate, String... args) {
		// the idea is to create another filetracker with another
		// splitviewcontroller defining only mdirectory
		// with some function in restricted mode
		// do collect all corresponding parent directory of items in map
		// then write map like above with the new generated map
		// refresh is forbidden

		// initializing temporary corresponding file Tracker
		FileTracker miniFileTracker = new FileTracker(Dirto);
		if (!miniFileTracker.isTracked()) {
			return;
		}
		// loading the original map
		// this will use easy call in write map
		miniFileTracker.loadMap(Dirto, true, false);
		// Do update name before resloving conflict
		if (args.length > 0) {
			// this miniFileTracker must be virtual using names as key otherwise
			// it will enter in endless loop so if a virtual with fullURI key (outofthebox)
			// call this function, then it create a miniOne this variable to help him
			// writing maps with ease
			miniFileTracker.operationUpdate(Dirto.resolve(args[0]), args);
		} else {
			// in case of Concurrent rename it may delete the old key
			/**
			 * Check PhotoViewerController#renameImage()
			 */
			miniFileTracker.resolveConflict();
		}
		if (toUpdate != null) {
			toUpdate.forEach(p -> {
				String keyName = p.getName();
				String keyURI = p.generateKeyURI();
				List<String> OptionsCopy = new ArrayList<>(mapDetails.get(keyURI));
				miniFileTracker.getMapDetails().put(keyName, OptionsCopy);
			});
		}

		miniFileTracker.writeMapDir(Dirto, false);
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
		if (!isTracked()) {
			return;
		}
		String currentConflict = "";
		ArrayList<String> toremove = new ArrayList<>();
		List<String> dirList = mSplitViewController.getCurrentFilesListName();
		for (String s : dirList) {
			if (!mapDetails.containsKey(s)) {
				// @addinhere
				currentConflict = "  - New \t" + s + "\n" + currentConflict;
				mapDetails.put(s, Arrays.asList(s, "0", " "));
			}
		}

		for (String key : mapDetails.keySet()) {
			if (!dirList.contains(key)) {
				currentConflict = "  - Del \t" + key + "\n" + currentConflict;
				toremove.add(key);
			}
		}
		for (String string : toremove) {
			mapDetails.remove(string);
		}
		if (!currentConflict.isEmpty()) {
			ConflictLog = "\n\n* " + Setting.getActiveUser() + " <<>> " + mWorkingDirPath.toString() + "\n"
					+ currentConflict + ConflictLog;
		}
		if (!currentConflict.isEmpty()) {
			writeMapDir(mWorkingDirPath, false, false);
			// writeMapDir(mWorkingDirPath, false);
		}

		// this.writeMap();
		// this writeMap enter in infinite loop since it gonna refresh after deleting
		// file in write map so since we have missing name in map it isn't dangerous to
		// wait till user commit a refresh by doing something :)
	}

	public void saveIndexFile() {
		// TODO Auto-generated method stub

	}

	public void setMapDetails(Map<String, List<String>> mapDetails) {
		this.mapDetails = mapDetails;
	}

	public void setSeen(String key, String status, TableViewModel t) {
		mapDetails.get(key).set(1, status);
		if (t != null) {
			mSplitViewController.updateVisualSeenButton(key, t);
		}
	}

	public void setTooltipsTexts(List<TableViewModel> list, String note) {
		if (isTracked()) {
			for (TableViewModel t : list) {
				mapDetails.get(t.getName()).set(2, note);
			}
		}
	}

	public void setTooltipText(String key, String note) {
		if (isTracked()) {
			mapDetails.get(key).set(2, note);
		}
		writeMapDir(mWorkingDirPath, false);
	}

	public void toggleSelectionSeen(List<TableViewModel> list, List<TableViewModel> xspfList, TableViewModel clicked) {
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
		toggleSingleSeenItem(clicked.getName(), clicked);
		if (list.size() > 1) {
			for (TableViewModel t : list) {
				if (t != clicked) {
					toggleSingleSeenItem(t.getName(), t);
				}
			}
		}
		if (xspfList != null) {
			for (TableViewModel t : xspfList) {
				toggleSingleSeenItem(t.getName(), t);
			}
		}
		writeMapDir(mWorkingDirPath, false);
	}

	public void toggleSingleSeenItem(String key, TableViewModel t) {
		Integer intSeen = Integer.parseInt(getSeen(key));
		Integer invertSeen = intSeen == 0 ? 1 : 0;
		setSeen(key, invertSeen.toString(), t);
	}

	// this initialize all files in the view folder to 0 watched
	// and save it on map and file
	public boolean trackNewFolder() {
		if (isTracked()) {
			return true; // prevent wipe old data
		}
		for (String string : mSplitViewController.getCurrentFilesListName()) {
			// name, notSeen, empty ToolTip String
			mapDetails.put(string, Arrays.asList(string, "0", " "));
		}
		return writeMap();
		// this is use less because watchable service will detect creating a new file
		// and will refresh automatically
		// mSplitViewController.refresh();
	}

	// easy access write map
	// this method is just to easy call write map for the current view
	public boolean writeMap() {
		return writeMapDir(mWorkingDirPath, true);
	}

	// important calling this function is dangerous since it delete a file in
	// current directroy
	// at same time there is a thread watching change in it so be aware to enter in
	// loop
	// @return boolean to confirm that the path is now tracked sometimes there is
	// write permission access
	// doRefresh[] :
	// 0 ---> do refresh this view that call the function
	// 1 ---> if exist the status of the other view, if not (default do refresh)
	public boolean writeMapDir(Path DirtoTrack, boolean... doRefresh) {
		try {
			// this is not the returned value it just to separate it this
			// function is called using writeMap() easy access
			boolean isEasy = DirtoTrack.equals(mWorkingDirPath);
			// WatchServiceHelper.setRuning(false);

			// to prevent forbidden access if split was send from recursive view writemap
			boolean virtualSplitView = isVirtual();

			File file = new File(DirtoTrack.resolve(UserFileName).toString());
			// https://howtodoinjava.com/java/io/java-write-to-file/
			if (file.exists()) {
				file.delete();
			}
			// resolve all character
			// https://stackoverflow.com/questions/1001540/how-to-write-a-utf-8-file-with-java
			// Always use UTF_8 for writing reading tracker files so no confilict occur when
			// running under different environment
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
			// check https://www.baeldung.com/java-string-newline
			String content = "/This is a generated file by Tracker Explorer to store track data of files\r\n";
			if (isEasy) {
				for (String key : mapDetails.keySet()) {
					content += String.join(">", String.join(">", mapDetails.get(key))) + "\r\n";
				}
			} else {
				File[] listFiles = DirtoTrack.toFile().listFiles(outfile -> !outfile.isHidden());
				for (File listFile : listFiles) {
					if (!listFile.getName().equals(UserFileName)) {
						content += String.join(">", Arrays.asList(listFile.getName(), "0", " ")) + "\r\n";
					}
				}
			}
			Files.setAttribute(file.toPath(), "dos:hidden", true);
			writer.write(content);
			writer.close();

			// ensure refresh for both pages case where they are in the same folder
			if (!virtualSplitView) {
				if (isEasy && doRefresh[0]) {
					mSplitViewController.getParentWelcome()
							.refreshAllSplitViewsIfMatch(mSplitViewController.getmDirectory(), mSplitViewController);
				} else {
					// was always entering here: in case on one view there is mark seen yes
					// and the other no
					// when updating markseen action (toggleseenhelper) seen without writing the map
					// with refresh
					// the other view doesn't detect since watchable service is turned off
					// so we do refresh the other view if the same directory in both view :)
					if (doRefresh.length == 1 || doRefresh[1] == true) {
						mSplitViewController.getParentWelcome().refreshAllSplitViewsIfMatch(
								mSplitViewController.getmDirectory(), mSplitViewController);
					}
				}
			}
			// TODO old approach ... it is safe now since no refresh will trigger on write
			// from conflict parameter is to prevent call function that called this function
			// and get loop
			// refresh -> resolve conflict -> write map ..<<??
			// WatchServiceHelper.setRuning(true);
			return true;
		} catch (IOException e) {
			// DialogHelper.showException(e);
			if (Setting.isDebugMode()) {
				e.printStackTrace();
			}
			// DialogHelper.showAlert(AlertType.ERROR, "New Tracker", "SomeThing Went
			// Wrong",
			// "Possible reason: - This may be a system protected Folder, Running this
			// application with admin right may fix the problem(depreciated)");
			return false;
		}
	}
}
