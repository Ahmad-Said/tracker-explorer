package said.ahmad.javafx.tracker.system.tracker;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.ws.Holder;

import org.jetbrains.annotations.Nullable;

import javafx.scene.control.Alert.AlertType;
import said.ahmad.javafx.tracker.app.DialogHelper;
import said.ahmad.javafx.tracker.app.pref.Setting;
import said.ahmad.javafx.tracker.controller.splitview.SplitViewController;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.tracker.system.file.PathLayerHelper;
import said.ahmad.javafx.tracker.system.file.local.FilePathLayer;
import said.ahmad.javafx.tracker.system.operation.FileHelper.ActionOperation;

/**
 *
 * Print in file tracker list conventions (entry separated by >):<br>
 * 0 >> name same as key (path.getFilename) when is the box<br>
 * 1 >> boolean isSeen status<br>
 * 2 >> String ToolTip details<br>
 * other tracker data:<br>
 * VLC filter data section as many triplet<br>
 * ----------------<br>
 * 3* >> start time<br>
 * 4* >> end time<br>
 * 5* >> Description of removal<br>
 * ----------------<br>
 * after first found of pipe command options '|'<br>
 * 6* >> optionsCommand (key)<br>
 * 7* >> value<br>
 *
 */
public class FileTracker {

	// ---------------------- Static Section ----------------------
	private static final String FIRST_LINE_TRACKER = "/This is a generated file by "
			+ "Tracker Explorer to store track data of files, --Version=" + Setting.getVersion() + "\r\n";

	private static final String BaseName = ".tracker_explorer";
	// conflict log will discard creating new File/Folder operations
	private static StringBuilder ConflictLog = new StringBuilder();
	// do not conflict UserFileName here is mean't by it's file name
	// do change later
	private static String UserFileName = BaseName + ".txt"; // by default

	public static void deleteOutFile(PathLayer Dirto, String user) {
		File tracker = new File(Dirto.toPath().resolve(getFileName(user)).toString());
		if (tracker.exists()) {
			tracker.delete();
		}
	}

	public static String getConflictLog() {
		return ConflictLog.toString();
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
		ConflictLog = new StringBuilder(conflictLog);
	}

	public static void updateUserFileName(String userName) {
		Setting.setActiveUser(userName);
		UserFileName = getFileName(userName);
	}

	/**
	 * New Options structure From Version 5+, value should not contain any '>'
	 *
	 * <pre>
	 * character Format In Order: <br>
	 * 		'|' (CommandOption) '>' (value)
	 * Example:<br>
	 * 		Udemy>0>note>|TimeToLive>43
	 * </pre>
	 */
	public enum CommandOption {
		TimeToLive
	}

	// Used when doing a file operation and no need to clear map immediately
	// Time to live will decrement by one each time map is written to file
	// so when TimeToLive reach 0 tracker record get discarded
	public final static int TIME_TO_LIVE_MAX = 64;

	// ---------------------- Initializing Section ----------------------

	private HashMap<PathLayer, FileTrackerHolder> mapDetailsRevolved;

	/**
	 * A Map from absolute Path {@link PathLayer#getAbsolutePath()} to it's tracker
	 * data
	 */
	public Map<PathLayer, FileTrackerHolder> getMapDetails() {
		return mapDetailsRevolved;
	}

	private PathLayer workingDirPath;
	/**
	 * Changed to true when loading files that are not brothers,<br>
	 * so to be aware how to write map and
	 * {@link #commitTrackerDataChange(PathLayer)} <br>
	 * value updated in:
	 *
	 * {@link #loadMap(PathLayer, boolean)}<br>
	 * {@link #loadResolvedMapOrEmpty(PathLayer)}
	 */
	private boolean isLoadedOtherThanWorkingDir;

	/**
	 * Main working directory, see {@link #isLoadedOtherThanWorkingDir}
	 *
	 * @return
	 */
	public PathLayer getWorkingDir() {
		return workingDirPath;
	}

	/**
	 * Use with caution
	 *
	 * Used to determine if map {@link #isLoadedOtherThanWorkingDir} that is used to
	 * ensure {@link #commitTrackerDataChange()} is writing map correctly in a
	 * single directory when loading one directory<br>
	 * {@link #isLoadedOtherThanWorkingDir} become true after call of
	 * {@link #loadMap(PathLayer, boolean, Map)} with no clear option and with
	 * PathLayer directory other than the previously loaded
	 *
	 * @param workingDirPath the workingDirPath to set
	 */
	public void setWorkingDirPath(PathLayer workingDirPath) {
		this.workingDirPath = workingDirPath;
	}

	/**
	 *
	 * @param <P>
	 */
	public interface OnWriteMapFinishCallBack<P extends PathLayer> {
		void handle(P path);
	}

	private OnWriteMapFinishCallBack<PathLayer> onWriteMapAction;

	public OnWriteMapFinishCallBack<PathLayer> getOnWriteMapAction() {
		return onWriteMapAction;
	}

	public void setOnWriteMapAction(OnWriteMapFinishCallBack<PathLayer> onWriteMapAction) {
		this.onWriteMapAction = onWriteMapAction;
	}

	/**
	 * General WorkFlow:<br>
	 * -> {@link #trackNewFolder()} <br>
	 * -> {@link #loadMap(Path, boolean)}<br>
	 * -> {@link #resolveConflict()}<br>
	 * -> {@link #getTrackerData(Path)} do some change<br>
	 * -> {@link #commitTrackerDataChange(Path)}<br>
	 *
	 * @param workingPath      can be null
	 * @param onWriteMapAction action to be triggered when writing map at specific
	 *                         Path<br>
	 *                         can be null
	 */
	public FileTracker(PathLayer workingPath, OnWriteMapFinishCallBack<PathLayer> onWriteMapAction) {
		mapDetailsRevolved = new HashMap<PathLayer, FileTrackerHolder>();
		// ensure no null workingDir for later comparison
		workingDirPath = workingPath == null ? new FilePathLayer() : workingPath;
		this.onWriteMapAction = onWriteMapAction;
	}

	// ---------------------- Loading/Writing Section ----------------------

	/**
	 *
	 * @param pathToDir
	 * @return a resolved path for file tracker in directory provided.<br>
	 *         null if null paramter was send
	 */
	@Nullable
	public static PathLayer getTrackerFileIn(PathLayer pathToDir) {
		if (pathToDir == null) {
			return null;
		}
		return pathToDir.resolve(getUserFileName());
	}

	@Nullable
	public PathLayer getTrackerFileInWorkingDir() {
		return getTrackerFileIn(workingDirPath);
	}

	public void deleteTrackerFile() throws IOException {
		PathLayer file = getTrackerFileInWorkingDir();
		if (file.exists()) {
			file.delete();
		}
	}

	public static boolean getAns() {
		return DialogHelper.showConfirmationDialog("Track new Folder", "Ready to Be Stunned ?",
				"Tracking a new Folder will create a hidden file .tracker_explorer.txt in the folder :) !"
						+ "so nothing dangerous just a file !");
	}

	public static boolean getAnsForMultipleFiles(HashSet<PathLayer> unTrackedList) {
		return DialogHelper.showExpandableConfirmationDialog("Track new Folder [Multiple Mode]",
				"Ready to Be Stunned ?",
				"Tracking a new Folder will create a hidden file .tracker_explorer.txt"
						+ " in the folder to save data tracker !"
						+ "\nIn Multiple mode the creation will trigger on all needed Items.",
				"Following Directories will be tracked:\n"
						+ unTrackedList.stream().map(p -> "- " + p.toString()).collect(Collectors.joining("\n")));
	}

	public boolean isTracked() {
		if (workingDirPath == null) {
			return false;
		}
		return workingDirPath.resolve(UserFileName).exists();
	}

	/**
	 * Just Check if tracker data exist in specified directory<br>
	 * In case for multiple folder do use {@link #filterTrackedFolder(Set)}
	 *
	 * @param dirToCheck
	 * @return
	 */
	public static boolean isTrackedOutFolder(PathLayer dirToCheck) {
		return dirToCheck.resolve(UserFileName).exists();
	}

	/**
	 *
	 * @param dirPath                 Directory to read tracker data from
	 * @param doclear                 clear already loaded data in map
	 * @param cachedPathsForKeysInMap a map from absolute path (String) to it's
	 *                                corresponding PathLayer<br>
	 *                                It can be null and so will create a new
	 *                                PathLayer by resolving name from tracker data
	 *                                with dirPath
	 *
	 * @return loaded map in directory provided, (null if directory is not tracked
	 *         or failed to load it due to permission reading file)
	 * @see {@link PathLayerHelper#getAbsolutePathToPaths(List)}
	 */
	@Nullable
	public HashMap<PathLayer, FileTrackerHolder> loadMap(PathLayer dirPath, boolean doclear,
			Map<String, PathLayer> cachedPathsForKeysInMap) {
		HashMap<PathLayer, FileTrackerHolder> loadedMap = new HashMap<>();
		if (doclear) {
			mapDetailsRevolved.clear();
		}
		// every time get the new path before loading in normal case
		if (mapDetailsRevolved.size() != 0 && !dirPath.equals(workingDirPath)) {
			isLoadedOtherThanWorkingDir = true;
		} else {
			isLoadedOtherThanWorkingDir = false;
		}
		workingDirPath = dirPath;
		PathLayer file = dirPath.resolve(UserFileName);
		if (!file.exists()) {
			return null;
		}
		BufferedReader in = null;
		String line = "";
		try {
			InputStream inputPathLayer = file.getInputFileStream();
			if (inputPathLayer == null) {
				return null;
			}

			in = new BufferedReader(new InputStreamReader(inputPathLayer, "UTF8"));
			in.readLine(); // to ignore first comment line
			while ((line = in.readLine()) != null) {
				FileTrackerHolder optionsItem;
				String lineSplit[] = line.split(">");
				// Minimum allowed length of split line is name/isSeen/note
				if (lineSplit.length >= 2) {
					optionsItem = new FileTrackerHolder(lineSplit[0]);
					optionsItem.setSeen(Integer.parseInt(lineSplit[1]) == 1);
					// when note is empty it's not sliced as new element in basic array
					// name/isSeen/note
					if (lineSplit.length >= 3) {
						optionsItem.setNoteText(lineSplit[2].trim());
					}
				} else {
					continue;
				}
				for (int i = 3; i < lineSplit.length; i++) {
					if (!lineSplit[i].isEmpty() && lineSplit[i].charAt(0) == '|') {
						if (i + 1 < lineSplit.length) {
							switch (CommandOption.valueOf(lineSplit[i].substring(1))) {
							case TimeToLive:
								optionsItem.setTimeToLive(Integer.parseInt(lineSplit[++i]) - 1);
								break;

							default:
								break;
							}
						}
					} else {
						// Filter VLC Stuff
						if (i + 2 < lineSplit.length) {
							optionsItem.concatMediaCutDataUnPrased(
									">" + lineSplit[i] + ">" + lineSplit[++i] + ">" + lineSplit[++i]);
						}
					}
				}
				PathLayer keyInMap;
				String keyInCachedMap = workingDirPath.resolveAsString(optionsItem.getName());
				if (cachedPathsForKeysInMap != null && cachedPathsForKeysInMap.containsKey(keyInCachedMap)) {
					keyInMap = cachedPathsForKeysInMap.get(keyInCachedMap);
				} else {
					keyInMap = dirPath.resolve(optionsItem.getName());
				}
				// Force biggest time to live to be overridden
				// in case of 2 concurrent file operation the last one
				// will remain
				// this allow file operation enough time to exist
				if (!mapDetailsRevolved.containsKey(keyInMap)
						|| optionsItem.getTimeToLive() >= mapDetailsRevolved.get(keyInMap).getTimeToLive()) {
					loadedMap.put(keyInMap, optionsItem);
					mapDetailsRevolved.put(keyInMap, optionsItem);
				}
			}
			in.close();
		} catch (IOException e1) {
			e1.printStackTrace();
			Setting.printStackTrace(e1);
			return null;
		}
		return loadedMap;
	}

	/**
	 * If specified directory is tracked, will load its map in specified directory,
	 * resolve conflict and add it to current map <br>
	 * <p>
	 * Other wise will load empty map of directory after listing it
	 *
	 *
	 * @param Dirto specified directory to load even when it's not tracked
	 */
	public void loadResolvedMapOrEmpty(PathLayer Dirto) {
		File tracker = new File(Dirto.resolve(UserFileName).toString());
		if (!Dirto.equals(workingDirPath)) {
			isLoadedOtherThanWorkingDir = true;
		}
		List<PathLayer> listOfNoHiddenPath = null;
		try {
			listOfNoHiddenPath = Dirto.listNoHiddenPathLayers();
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		if (tracker.exists()) {
			FileTracker miniFileTracker = new FileTracker(Dirto, null);
			// loading the original map to resolve conflict -> detecting new files..
			miniFileTracker.loadMap(Dirto, true, PathLayerHelper.getAbsolutePathToPaths(listOfNoHiddenPath));
			miniFileTracker.resolveConflict(new HashSet<>(listOfNoHiddenPath));
			mapDetailsRevolved.putAll(miniFileTracker.mapDetailsRevolved);
		} else {
			loadEmptyMapOfList(listOfNoHiddenPath);
		}
	}

	/**
	 * Just load list of path with default tracker data
	 *
	 * @param listOfPaths
	 */
	public void loadEmptyMapOfList(List<PathLayer> listOfPaths) {
		// add untracked data folder here require listing dir again
		for (PathLayer p : listOfPaths) {
			mapDetailsRevolved.put(p, new FileTrackerHolder(p.getName()));
		}
	}

	/**
	 * Write Current {@link #mapDetailsRevolved} tracker data into file in
	 * {@link #workingDirPath}, <br>
	 *
	 * will call On Finish writing map {@link #onWriteMapAction}
	 *
	 * @return true if writing occur without any problem and file write access is
	 *         Guaranteed
	 */
	public boolean writeMap() {
		return writeMapDir(workingDirPath, true);
	}

	private static int getAverageCountContentCharacters(int numberOfFiles) {
		return FIRST_LINE_TRACKER.length() + numberOfFiles * 20;
	}

	/**
	 *
	 *
	 * @param DirtoTrack         target directory to write tracker data to
	 * @param callOnFinishAction if set to true, on finish writing map
	 *                           {@link #onWriteMapAction} will be called
	 * @return
	 */
	public boolean writeMapDir(PathLayer DirtoTrack, boolean callOnFinishAction) {
		try {
			PathLayer file = DirtoTrack.resolve(UserFileName);
			// https://howtodoinjava.com/java/io/java-write-to-file/
			if (file.exists()) {
				file.delete();
			}
			OutputStream outputPathLayer = file.getOutputFileStream();
			if (outputPathLayer == null) {
				return false;
			}
			// resolve all character
			// https://stackoverflow.com/questions/1001540/how-to-write-a-utf-8-file-with-java
			// Always use UTF_8 for writing reading tracker files so no conflict occur when
			// running under different environment
			// note that OutputStreamWriter writer already has buffer of 8 KB
			OutputStreamWriter writer = new OutputStreamWriter(outputPathLayer, StandardCharsets.UTF_8);
			// check https://www.baeldung.com/java-string-newline \r\n is used inside
			// StringBuilder is much faster than += for string. check google benchmark
			StringBuilder content = new StringBuilder(getAverageCountContentCharacters(mapDetailsRevolved.size()));
			content.append(FIRST_LINE_TRACKER);

			for (PathLayer path : mapDetailsRevolved.keySet()) {
				content.append(mapDetailsRevolved.get(path).toString());
			}
			writer.write(content.toString());
			writer.close();
			file.setHidden(true);
			if (callOnFinishAction && onWriteMapAction != null) {
				onWriteMapAction.handle(DirtoTrack);
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Write Default map In Specified Directory<br>
	 * If directory already tracked do nothing
	 *
	 * <p>
	 * done in following sequence :<br>
	 * -> do list files in directory<br>
	 * -> generate tracker data with default options name/unseen/empty_note <br>
	 * -> call onFinishAction parameter
	 *
	 * @param DirtoTrack     Directory to track
	 * @param onFinishAction to do after finish writing map, can be null
	 *
	 * @return <code>Map</code> The written map if write was successful<br>
	 *         <code>null</code> if tracker data already exist or failed to write
	 * @throws IOException
	 */
	@Nullable
	private static HashMap<PathLayer, FileTrackerHolder> writeNewDefaultMap(PathLayer DirtoTrack,
			OnWriteMapFinishCallBack<PathLayer> onFinishAction) throws IOException {
		PathLayer file = DirtoTrack.resolve(UserFileName);
		if (file.exists()) {
			return null;
		}
		OutputStream outputPathLayer = file.getOutputFileStream();
		if (outputPathLayer == null) {
			return null;
		}
		OutputStreamWriter writer = new OutputStreamWriter(outputPathLayer, StandardCharsets.UTF_8);
		List<PathLayer> listFiles = DirtoTrack.listNoHiddenPathLayers();
		StringBuilder content = new StringBuilder(getAverageCountContentCharacters(listFiles.size()));
		HashMap<PathLayer, FileTrackerHolder> mapWritten = new HashMap<>();
		content.append(FIRST_LINE_TRACKER);
		for (PathLayer singleFile : listFiles) {
			if (!singleFile.getName().equals(UserFileName)) {
				FileTrackerHolder dataHolder = new FileTrackerHolder(singleFile.getName()).setSeen(false);
				mapWritten.put(singleFile, dataHolder);
				content.append(dataHolder.toString());
			}
		}
		writer.write(content.toString());
		writer.close();
		if (onFinishAction != null) {
			onFinishAction.handle(DirtoTrack);
		}
		file.setHidden(true);

		return mapWritten;
	}

	/**
	 * -> Trying to track {@link #workingDirPath}<br>
	 * -> Clear current map <br>
	 * -> Track a new Folder with default option <br>
	 * -> If already tracked do nothing <br>
	 * -> Otherwise load written map into current map<br>
	 * <br>
	 * More Details about initial data saved at<br>
	 * {@link #writeNewDefaultMap(Path, OnWriteMapFinishCallBack)}
	 *
	 * @return <code>Map</code> The written map if successfully write data<br>
	 *         <code>null</code> otherwise
	 * @see #trackNewOutFolder(PathLayer, boolean, boolean)
	 * @throws IOException
	 */
	@Nullable
	public HashMap<PathLayer, FileTrackerHolder> trackNewFolder(boolean loadIfSuccessIntoCurrentMap,
			boolean clearPerviousMap) throws IOException {
		// prevent wipe is checked in write
		@Nullable
		HashMap<PathLayer, FileTrackerHolder> writtenMap = writeNewDefaultMap(workingDirPath, onWriteMapAction);
		if (clearPerviousMap) {
			mapDetailsRevolved.clear();
		}
		if (writtenMap != null && loadIfSuccessIntoCurrentMap) {
			mapDetailsRevolved.putAll(writtenMap);
		}
		return writtenMap;
	}

	/**
	 * -> Clear current map <br>
	 * -> Track a new Folder with default option <br>
	 * -> If already tracked do nothing <br>
	 * -> Otherwise load written map into current map<br>
	 * <br>
	 * More Details about initial data saved at<br>
	 * {@link #writeNewDefaultMap(Path, OnWriteMapFinishCallBack)}
	 *
	 * @param DirtoTrack
	 * @return <code>Map</code> The written map if successfully write data<br>
	 *         <code>null</code> otherwise
	 * @throws IOException
	 */
	@Nullable
	public HashMap<PathLayer, FileTrackerHolder> trackNewOutFolder(PathLayer DirtoTrack,
			boolean loadIfSuccessIntoCurrentMap, boolean clearPerviousMap) throws IOException {
		// write prevent wipe old data is done in the call
		@Nullable
		HashMap<PathLayer, FileTrackerHolder> writtenMap = writeNewDefaultMap(DirtoTrack, null);
		if (clearPerviousMap) {
			mapDetailsRevolved.clear();
		}
		if (writtenMap != null && loadIfSuccessIntoCurrentMap) {
			mapDetailsRevolved.putAll(writtenMap);
		}
		return writtenMap;
	}

	/**
	 * -> Track every untracked Folder with default option <br>
	 * -> Load tracker data into current map (all cumulative tracked data no map
	 * clear is done)<br>
	 * -> ignore any failed track<br>
	 * <br>
	 * More Details about initial data saved at<br>
	 * {@link #writeNewDefaultMap(Path, OnWriteMapFinishCallBack)}
	 *
	 * @param DirstoTrack
	 * @param showDialogError if any fails in tracking folder an error dialog will
	 *                        appear
	 * @return FileTrackerReturn<br>
	 *         {@link FileTrackerMultipleReturn#trackedList} as list of tracked
	 *         paths as Key pair<br>
	 *         {@link FileTrackerMultipleReturn#unTrackedList} as list of failed to
	 *         track paths as value<br>
	 *         {@link FileTrackerMultipleReturn#didTrackNewFolder} as if any newly
	 *         tracked path
	 */
	public FileTrackerMultipleReturn trackNewMultipleFolder(Set<PathLayer> DirstoTrack, boolean showDialogError) {
		FileTrackerMultipleReturn toReturn = new FileTrackerMultipleReturn();
		String error = "";
		for (PathLayer path : DirstoTrack) {
			try {
				@Nullable
				HashMap<PathLayer, FileTrackerHolder> writtenMap = trackNewOutFolder(path, true, false);
				if (writtenMap != null) {
					toReturn.didTrackNewFolder = true;
				}
				toReturn.trackedList.add(path);
			} catch (IOException e) {
				e.printStackTrace();
				toReturn.unTrackedList.add(path);
				if (showDialogError) {
					error += e.toString();
				}
			}
		}
		if (showDialogError && !error.isEmpty()) {
			DialogHelper.showExpandableAlert(AlertType.ERROR, "Tracker New Folder", "Some Files weren't Tracked!",
					"Check logs", error);
		}
		return toReturn;
	}

	/**
	 *
	 * @param dirsToCheck
	 * @return list of tracked paths as Key pair<br>
	 *         list of untracked paths as value
	 */
	public static FileTrackerMultipleReturn filterTrackedFolder(Set<PathLayer> dirsToCheck) {
		FileTrackerMultipleReturn toReturn = new FileTrackerMultipleReturn();
		for (PathLayer d : dirsToCheck) {
			if (isTrackedOutFolder(d)) {
				toReturn.trackedList.add(d);
			} else {
				toReturn.unTrackedList.add(d);
			}
		}
		return toReturn;
	}

	/**
	 * If any of paths is untracked will ask for it, then track all of them<br>
	 * <br>
	 * and call {@link #trackNewMultipleFolder(Set, boolean)}
	 *
	 * @param paths           to work with
	 * @param showDialogError if any fails in tracking folder an error dialog will
	 *                        appear
	 * @return list of tracked paths as Key pair<br>
	 *         list of failed to track paths as value
	 */
	public FileTrackerMultipleReturn trackNewMultipleAndAsk(Set<PathLayer> paths, boolean showDialogError) {
		boolean ans = true;
		FileTrackerMultipleReturn allPaths = filterTrackedFolder(paths);

		if (allPaths.unTrackedList.size() != 0) {
			// there is untracked Path!
			if (allPaths.unTrackedList.size() == 1) {
				ans = FileTracker.getAns();
			} else {
				ans = FileTracker.getAnsForMultipleFiles(allPaths.unTrackedList);
			}
			if (ans) {
				allPaths = trackNewMultipleFolder(paths, showDialogError);
			}
		}
		return allPaths;
	}

	/**
	 * compare data in {@link #mapDetailsRevolved} with given parameter
	 * listToCompareWith:<br>
	 * ---> will clear all useless data in map (moved/deleted files) <br>
	 * ---> add missing(= newly created) files to <b>map tracker data with null
	 * Seen</b><br>
	 * ---> if any conflict occur will rewrite map details in
	 * {@link #getWorkingDir()}<br>
	 * Just a note {@link SplitViewController#refresh(String)} do call this function
	 * after loading map {@link #loadMap(Path, boolean)} and before loading views
	 * stuff
	 *
	 * @return conflict description if conflict occur<br>
	 *         <code>null</code> otherwise
	 * @see #resolveConflict()
	 * @throws IOException
	 */
	@Nullable
	public FileTrackerConflictLog resolveConflict(Set<PathLayer> listToCompareWith) {
		if (!isTracked()) {
			return null;
		}
		FileTrackerConflictLog currentConflict = new FileTrackerConflictLog();
		ArrayList<PathLayer> toremove = new ArrayList<>();
		for (PathLayer p : listToCompareWith) {
			if (!mapDetailsRevolved.containsKey(p)) {
				// @AddInHere
				currentConflict.addedItems.add(p);
				mapDetailsRevolved.put(p, new FileTrackerHolder(p.getName().toString()));
			}
		}

		for (PathLayer key : mapDetailsRevolved.keySet()) {

			if (mapDetailsRevolved.get(key).getTimeToLive() > 0) {
				// File can stay maybe it's coming in some operation
				// but if it exist in directory do clear TimeToLive
				if (listToCompareWith.contains(key)) {
					mapDetailsRevolved.get(key).setTimeToLive(-1);
				}
				continue;
			} else if (!listToCompareWith.contains(key)) {
				// if time to live reach 0 -> silent remove
				// log otherwise
				if (mapDetailsRevolved.get(key).getTimeToLive() != 0) {
					currentConflict.removedItems.add(key);
				}
				toremove.add(key);
			}
		}
		for (PathLayer key : toremove) {
			mapDetailsRevolved.remove(key);
		}
		if (currentConflict.didChangeOccurs()) {
			currentConflict.generateSummary();
			ConflictLog.insert(0, "\n\n* " + Setting.getActiveUser() + " <<>> " + workingDirPath.toString() + "\n"
					+ currentConflict.summary);

			writeMapDir(workingDirPath, false);
			return currentConflict;
		}
		return null;
	}

	/**
	 * Call {@link #resolveConflict(List)} after listing current working directory
	 *
	 * <p>
	 * <b> Note: </b> if you already have list of files of current directory, do
	 * immediately call {@link #resolveConflict(List)} to prevent double list of
	 * same directory
	 *
	 * @see #resolveConflict(List)
	 * @return set of list in working directory
	 */
	public Set<PathLayer> resolveConflictInworkingDir() throws IOException {
		List<PathLayer> dirListAsList = workingDirPath.listNoHiddenPathLayers();
		HashSet<PathLayer> set = new HashSet<>(dirListAsList);
		resolveConflict(set);
		return set;
	}

	// ---------------------- File Operation (Copy..) Section ----------------------

	/**
	 * When doing a copy or move operations: It is necessary to preserve the tracker
	 * data, and in case of delete do nothing about it. But clearing from conflict
	 * log is optional
	 *
	 * @param sources   sources file that get changed, null key will be ignored
	 * @param targets   targets file the new file location path, null are ignored
	 *
	 * @param operation Check {@link ActionOperation} <br>
	 *                  Note: MOVE operation cannot be used for rename use
	 *                  {@link ActionOperation#RENAME} instead
	 * @throws IndexOutOfBoundsException in case both list weren't the same size
	 * @return MapDetails containing all new updated sources, empty in case of
	 *         delete<br>
	 *         Use fileTracker.getMapDetails().putAll(operationUpdateAsList(...)) to
	 *         update instance map without reloading from file
	 */
	public static Map<PathLayer, FileTrackerHolder> operationUpdateAsList(List<PathLayer> sources,
			List<PathLayer> targets, ActionOperation operation) throws IndexOutOfBoundsException {
		if (sources.size() != targets.size()) {
			throw new IndexOutOfBoundsException("Sources list and Target list parameter must be same size");
		}
		// All key changed map
		Map<PathLayer, FileTrackerHolder> allUpdatedSources = new HashMap<>();
		if (sources.size() == 0) {
			return allUpdatedSources;
		}
		// a map from a (target) directory to sources that have common target
		// directory
		HashMap<PathLayer, List<PathLayer>> targetDirToSources = new HashMap<>();
		for (int i = 0; i < sources.size(); i++) {
			if (sources.get(i) == null || targets.get(i) == null) {
				continue;
			}
			PathLayer targetParent = targets.get(i).getParentPath();
			if (!targetDirToSources.containsKey(targets.get(i).getParentPath())) {
				targetDirToSources.put(targetParent, new ArrayList<PathLayer>());
			}
			targetDirToSources.get(targetParent).add(sources.get(i));
		}
		switch (operation) {
		case COPY:
		case MOVE:
		case DELETE:
			// to create function in format of operationUpdate(source,targetDir,operation)
			targetDirToSources.forEach(
					(target, miniSources) -> allUpdatedSources.putAll(operationUpdate(miniSources, target, operation)));
			break;
		case RENAME:
			// to create function in format of operationUpdate(source,targetDir,operation)
			// Get all source Tracker Data Holders
			Set<PathLayer> allSrcParent = PathLayerHelper.getParentsPaths(sources);
			FileTracker miniFileTracker = new FileTracker(null, null);
			allSrcParent.forEach(parentSrc -> {
				miniFileTracker.loadMap(parentSrc, false, null);
			});
			if (miniFileTracker.mapDetailsRevolved.size() == 0) {
				// no data to bring
				break;
			}
			// append MAXIMUM time to live option in target directory files of targets list
			// Path
			HashMap<PathLayer, PathLayer> srcToTarget = new HashMap<>();
			for (int i = 0; i < sources.size(); i++) {
				if (sources.get(i) == null || targets.get(i) == null) {
					continue;
				}
				srcToTarget.put(sources.get(i), targets.get(i));
			}
			targetDirToSources.forEach((target, miniSources) -> {
				Holder<OutputStreamWriter> writer = new Holder<>();
				try {
					PathLayer trackerFile = FileTracker.getTrackerFileIn(target);
					OutputStream outputAppendPathLayer = trackerFile.getOutputAppendFileStream();
					if (outputAppendPathLayer == null) {
						// Using return; will not prevent the full loop from
						// completing. It will only stop executing the current iteration of the forEach
						// loop.
						return;
					}
					// appending data to end with time to live
					writer.value = new OutputStreamWriter(outputAppendPathLayer, StandardCharsets.UTF_8);
					for (PathLayer src : miniSources) {
						if (miniFileTracker.getMapDetails().containsKey(src)) {
							writer.value
									.write(miniFileTracker.getTrackerData(src).setName(srcToTarget.get(src).getName())
											.setTimeToLive(TIME_TO_LIVE_MAX).toString());
							allUpdatedSources.put(srcToTarget.get(src), miniFileTracker.getTrackerData(src));
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (writer.value != null) {
					try {
						writer.value.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});

		default:
			break;
		}
		return allUpdatedSources;
	}

	/**
	 * Copy_Move_Delete_Operations only <br>
	 *
	 * When doing a copy or move operations: It is necessary to preserve the tracker
	 * data, and in case of delete do nothing about it. But clearing from conflict
	 * log is optional
	 * <p>
	 * For <b>rename</b> use
	 * {@link #operationUpdateAsList(List, List, ActionOperation)} <br>
	 *
	 * @param source    The list of path to copy/move/delete
	 * @param targetDir The target directory where these path are copied/moved<br>
	 *                  Can be null in case of delete, will auto get parent from
	 *                  source
	 * @param operation check {@link ActionOperation}
	 * @return MapDetails containing all new updated sources, empty in case of
	 *         delete
	 */
	public static Map<PathLayer, FileTrackerHolder> operationUpdate(List<? extends PathLayer> source,
			PathLayer targetDir, ActionOperation operation) {
		// get parent to son map to deal with each list of son brother together
		HashMap<PathLayer, List<PathLayer>> parentToFiles = PathLayerHelper.getParentTochildren(source);
		// All key changed map
		Map<PathLayer, FileTrackerHolder> allUpdatedSources = new HashMap<PathLayer, FileTrackerHolder>();
		if (source.size() == 0) {
			return allUpdatedSources;
		}
		// collect all tracked parents if none end this function;
		HashSet<PathLayer> trackedParents = new HashSet<>();
		parentToFiles.keySet().forEach(p -> {
			if (isTrackedOutFolder(p)) {
				trackedParents.add(p);
			}
		});

		if (trackedParents.size() == 0) {
			return allUpdatedSources;
		}
		// instance of file tracker to read/write tracker data
		FileTracker senderFileTracker = new FileTracker(null, null);
		tryBlock: try {
			PathLayer trackerFile = null;
			Holder<OutputStreamWriter> writer = new Holder<OutputStreamWriter>(null);
			if (operation.equals(ActionOperation.DELETE)) {
				// will open stream file for each list of son
				targetDir = null;// to prevent further access
			} else {
				trackerFile = targetDir.resolve(UserFileName);
				if (!trackerFile.exists()) {
					writeNewDefaultMap(targetDir, null);
				}
				OutputStream outputAppendPathLayer = trackerFile.getOutputAppendFileStream();
				if (outputAppendPathLayer == null) {
					break tryBlock;
				}
				writer.value = new OutputStreamWriter(outputAppendPathLayer, StandardCharsets.UTF_8);
			}
			trackedParents.forEach(parent -> {
				List<PathLayer> brotherList = parentToFiles.get(parent);

				senderFileTracker.workingDirPath = parent;
				// parent Tracker file exist -> Data sources exist
				senderFileTracker.loadMap(parent, true, null);
				try {
					// Append Data to parent Tracker file with some time to live
					if (operation.equals(ActionOperation.DELETE)) {
						if (writer.value != null) {
							writer.value.close();
						}
						// data source output stream
						OutputStream outputSrcToClearConflict = senderFileTracker.getTrackerFileInWorkingDir()
								.getOutputAppendFileStream();
						if (outputSrcToClearConflict == null) {
							return;
						}
						writer.value = new OutputStreamWriter(outputSrcToClearConflict, StandardCharsets.UTF_8);
					}

					brotherList.forEach(son -> {
						try {
							if (operation.equals(ActionOperation.DELETE)) {
								writer.value.write(senderFileTracker.getTrackerData(son).setTimeToLive(5).toString());
							} else {
								// it is possible that sender tracker hasn't resolved conflict
								if (senderFileTracker.getTrackerData(son) != null) {
									writer.value.write(senderFileTracker.getTrackerData(son)
											.setTimeToLive(TIME_TO_LIVE_MAX).toString());
									allUpdatedSources.put(son, senderFileTracker.getTrackerData(son));
								}
							}
						} catch (IOException e) {
							// sons try catch
							e.printStackTrace();
						}
					});
				} catch (IOException e) {
					// parent try catch
					e.printStackTrace();
				}
			});
			if (writer.value != null) {
				writer.value.close();
			}
		} catch (IOException e) {
			// all function try catch
			e.printStackTrace();
		}
		return allUpdatedSources;
	}

	// ---------------------- File Tracker Holder Section ----------------------

	/**
	 *
	 * @param key Path
	 * @return {@link FileTrackerHolder}
	 */
	@Nullable
	public FileTrackerHolder getTrackerData(PathLayer key) {
		return mapDetailsRevolved.get(key);
	}

	/**
	 *
	 * @param paths
	 * @return stream of paths existing in map details
	 */
	private Stream<FileTrackerHolder> getExistingPathInMap(List<PathLayer> paths) {
		return paths.stream().filter(p -> mapDetailsRevolved.containsKey(p)).map(p -> mapDetailsRevolved.get(p));
	}

	/**
	 *
	 * @param key
	 * @return null if not in map, Seen Status otherwise
	 */
	@Nullable
	public Boolean isSeen(PathLayer key) {
		return getTrackerData(key) == null ? null : getTrackerData(key).isSeen();
	}

	/**
	 * Does not write map, to do so use {@link #commitTrackerDataChange(PathLayer)}
	 *
	 * @param path
	 * @param isSeen
	 */
	public void setSeen(PathLayer path, Boolean isSeen) {
		if (mapDetailsRevolved.containsKey(path)) {
			mapDetailsRevolved.get(path).setSeen(isSeen);
		}
	}

	/**
	 * Does not write map, to do so use {@link #commitTrackerDataChange(PathLayer)}
	 *
	 * @param paths
	 * @param isSeen
	 */
	public void setSeen(List<PathLayer> paths, Boolean isSeen) {
		getExistingPathInMap(paths).forEach(tData -> tData.setSeen(isSeen));
	}

	/**
	 * Does not write map, to do so use {@link #commitTrackerDataChange(PathLayer)}
	 *
	 * @param path
	 */
	public void toggleSeen(PathLayer path) {
		if (mapDetailsRevolved.containsKey(path)) {
			mapDetailsRevolved.get(path).toggleSeen();
		}
	}

	/**
	 * Does not write map, to do so use {@link #commitTrackerDataChange(PathLayer)}
	 *
	 * @param keyPath
	 */
	public void toggleSeen(List<PathLayer> paths) {
		getExistingPathInMap(paths).forEach(tData -> tData.toggleSeen());
	}

	/**
	 *
	 * @param path
	 * @return note text if exist, empty String otherwise
	 */
	public String getNoteText(PathLayer path) {
		if (!mapDetailsRevolved.containsKey(path)) {
			return "";
		}
		String ans = mapDetailsRevolved.get(path).getNoteText();
		return ans.equals(" ") ? "" : ans;
	}

	/**
	 * Does not write map, to do so use {@link #commitTrackerDataChange(PathLayer)}
	 *
	 * @param path
	 * @param note
	 */
	public void setNoteText(PathLayer path, String note) {
		if (mapDetailsRevolved.containsKey(path)) {
			mapDetailsRevolved.get(path).setNoteText(note);
		}
	}

	/**
	 * Ask for note via
	 * {@link DialogHelper#showTextInputDialog(String, String, String, String)} <br>
	 * Not to worry about filtering note.
	 *
	 * @return null if operation is cancelled, the string otherwise
	 */
	@Nullable
	public static String askForNoteText(String hint) {
		String note = DialogHelper.showTextInputDialog("Quick Note Editor", "Add Note To see on hover",
				"Old note Was:\n" + hint, hint);
		// if null set it to space like it was
		if (note == null) {
			return null; // keep note unchanged
		}
		return note.replaceAll(">", "_");
	}

	/**
	 * Does not write map, to do so use {@link #commitTrackerDataChange(PathLayer)}
	 *
	 * @param paths
	 * @param note
	 */
	public void setNoteText(List<PathLayer> paths, String note) {
		getExistingPathInMap(paths).forEach(tData -> tData.setNoteText(note));
	}

	/**
	 * Write map to file into working directory <br>
	 * {@link #onWriteMapAction} will be called
	 */
	public void commitTrackerDataChange() {
		if (!isLoadedOtherThanWorkingDir) {
			writeMapDir(workingDirPath, true);
		}
	}

	/**
	 * Write map for specific Path
	 *
	 * @param path
	 */
	public void commitTrackerDataChange(PathLayer path) {
		if (!isLoadedOtherThanWorkingDir) {
			writeMapDir(workingDirPath, true);
		} else {
			// creating a new file tracker is indeed
			FileTracker miniFileTracker = new FileTracker(null, onWriteMapAction);
			miniFileTracker.loadMap(path.getParentPath(), true, null);
			miniFileTracker.getMapDetails().put(path, getTrackerData(path));
			miniFileTracker.commitTrackerDataChange(path);
		}
	}

	/**
	 * Do write map for list of Paths
	 */
	public void commitTrackerDataChange(List<PathLayer> paths) {
		if (!isLoadedOtherThanWorkingDir) {
			writeMapDir(workingDirPath, true);
		} else {
			// creating a new file tracker is indeed
			FileTracker miniFileTracker = new FileTracker(null, onWriteMapAction);
			PathLayerHelper.getParentTochildren(paths).forEach((parent, sons) -> {
				miniFileTracker.loadMap(parent, true, null);
				sons.forEach(son -> miniFileTracker.getMapDetails().put(son, getTrackerData(son)));
				miniFileTracker.commitTrackerDataChange(sons);
			});
		}
	}

	/**
	 * It append tracker data to end of file without rewriting a new file (with time
	 * to live) It is faster, but causes a big file overtime.
	 *
	 * @param paths
	 * @param parent
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private void appendCommitTrackerData(List<PathLayer> paths, PathLayer parent) throws IOException {
		PathLayer trackerFile = getTrackerFileIn(parent);
		// appending data to end with time to live
		OutputStream outputPathLayer = trackerFile.getOutputAppendFileStream();
		if (outputPathLayer == null) {
			return;
		}
		OutputStreamWriter writer = new OutputStreamWriter(outputPathLayer, StandardCharsets.UTF_8);
		for (PathLayer changed : paths) {
			if (getMapDetails().containsKey(changed)) {
				writer.write(getTrackerData(changed).setTimeToLive(TIME_TO_LIVE_MAX).toString());
			}
		}
		writer.close();
	}

	/**
	 * When creating a new file append tracker data with this option
	 *
	 * @param paths
	 * @param parent
	 * @throws IOException
	 */
	public static void appendTrackerData(PathLayer path, FileTrackerHolder trackerData, boolean writeANewMapIfNeeded)
			throws IOException {
		PathLayer parent = path.getParentPath();
		if (!isTrackedOutFolder(parent)) {
			if (writeANewMapIfNeeded) {
				writeNewDefaultMap(parent, null);
			} else {
				return;
			}
		}
		PathLayer trackerFile = getTrackerFileIn(parent);
		// appending data to end with time to live
		OutputStream outputPathLayer = trackerFile.getOutputAppendFileStream();
		if (outputPathLayer == null) {
			return;
		}
		OutputStreamWriter writer = new OutputStreamWriter(outputPathLayer, StandardCharsets.UTF_8);
		writer.write(trackerData.toString());
		writer.close();
	}
}
