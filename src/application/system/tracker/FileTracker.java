package application.system.tracker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.ws.Holder;

import org.jetbrains.annotations.Nullable;

import application.DialogHelper;
import application.StringHelper;
import application.controller.splitview.SplitViewController;
import application.datatype.Setting;
import application.system.operation.FileHelper;
import application.system.operation.FileHelper.ActionOperation;
import javafx.scene.control.Alert.AlertType;

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

	/**
	 * New Options structure From Version 5+, value should not contain any '>'
	 *
	 * <pre>
	 * character Format In Order: <br>
	 * 		'|' (CommandOption) '>' (value)
	 * Example:<br>
	 * 		|TimeToLive>43>Udemy>0>
	 * </pre>
	 */
	public enum CommandOption {
		TimeToLive
	}

	// Used when doing a file operation and need to clear map immediately
	// so when TimeToLive reach 0 it git ignored
	public final static int TIME_TO_LIVE_MAX = 64;

	// ---------------------- Initializing Section ----------------------

	private HashMap<Path, FileTrackerHolder> mapDetailsRevolved;

	public Map<Path, FileTrackerHolder> getMapDetailsRevolved() {
		return mapDetailsRevolved;
	}

	private Path workingDirPath;
	/**
	 * Changed to true when loading files that are not brothers,<br>
	 * so to be aware how to write map and {@link #commitTrackerDataChange(Path)}
	 * <br>
	 * value updated in:
	 *
	 * {@link #loadMap(Path, boolean)}<br>
	 * {@link #loadResolvedMapOrEmpty(Path)}
	 */
	private boolean isLoadedOtherThanWorkingDir;

	/**
	 * Main working directory, see {@link #isLoadedOtherThanWorkingDir}
	 *
	 * @return
	 */
	public Path getWorkingDir() {
		return workingDirPath;
	}

	/**
	 *
	 * @param <P>
	 */
	public interface OnWriteMapFinishCallBack<P extends Path> {
		void handle(P path);
	}

	private OnWriteMapFinishCallBack<Path> onWriteMapAction;

	public OnWriteMapFinishCallBack<Path> getOnWriteMapAction() {
		return onWriteMapAction;
	}

	public void setOnWriteMapAction(OnWriteMapFinishCallBack<Path> onWriteMapAction) {
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
	public FileTracker(Path workingPath, OnWriteMapFinishCallBack<Path> onWriteMapAction) {
		mapDetailsRevolved = new HashMap<Path, FileTrackerHolder>();
		// ensure no null workingDir for later comparison
		workingDirPath = workingPath == null ? Paths.get("/") : workingPath;
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
	public static Path getTrackerFileIn(Path pathToDir) {
		if (pathToDir == null) {
			return null;
		}
		return pathToDir.resolve(getUserFileName());
	}

	@Nullable
	public Path getTrackerFileInWorkingDir() {
		return getTrackerFileIn(workingDirPath);
	}

	public void deleteTrackerFile() {
		File file = getTrackerFileInWorkingDir().toFile();
		if (file.exists()) {
			file.delete();
		}
	}

	public static boolean getAns() {
		return DialogHelper.showConfirmationDialog("Track new Folder", "Ready to Be Stunned ?",
				"Tracking a new Folder will create a hidden file .tracker_explorer.txt in the folder :) !"
						+ "so nothing dangerous just a file !");
	}

	public static boolean getAnsForMultipleFiles(HashSet<Path> unTrackedList) {
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
		File tracker = new File(workingDirPath.resolve(UserFileName).toString());
		return tracker.exists();
	}

	/**
	 * Just Check if tracker data exist in specified directory<br>
	 * In case for multiple folder do use {@link #filterTrackedFolder(Set)}
	 *
	 * @param dirToCheck
	 * @return
	 */
	public static boolean isTrackedOutFolder(Path dirToCheck) {
		File tracker = new File(dirToCheck.resolve(UserFileName).toString());
		return tracker.exists();
	}

	public void loadMap(Path dirPath, boolean doclear) {
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
		File file = dirPath.resolve(UserFileName).toFile();
		if (!file.exists()) {
			return;
		}
		BufferedReader in = null;
		String line = "";
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
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
				Path keyInMap = dirPath.resolve(optionsItem.getName());
				// Force biggest time to live to be overridden
				// in case of 2 concurrent file operation the last one
				// will remain
				// this allow file operation enough time to exist
				if (!mapDetailsRevolved.containsKey(keyInMap)
						|| optionsItem.getTimeToLive() >= mapDetailsRevolved.get(keyInMap).getTimeToLive()) {
					mapDetailsRevolved.put(keyInMap, optionsItem);
				}
			}
			in.close();
		} catch (IOException e1) {
			e1.printStackTrace();
			Setting.printStackTrace(e1);
		}
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
	public void loadResolvedMapOrEmpty(Path Dirto) {
		File tracker = new File(Dirto.resolve(UserFileName).toString());
		if (!Dirto.equals(workingDirPath)) {
			isLoadedOtherThanWorkingDir = true;
		}
		if (tracker.exists()) {
			FileTracker miniFileTracker = new FileTracker(Dirto, null);
			// loading the original map to resolve conflict -> detecting new files..
			miniFileTracker.loadMap(Dirto, true);
			try {
				miniFileTracker.resolveConflict();
				mapDetailsRevolved.putAll(miniFileTracker.mapDetailsRevolved);
			} catch (IOException e) {
				e.printStackTrace();
			}
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
					mapDetailsRevolved.put(f.toPath(), new FileTrackerHolder(f.getName()));
				} catch (Exception e) {
					// checking that path contain legal character other wise skip them
					Setting.printStackTrace(e);
					continue;
				}
			}
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

	/**
	 *
	 *
	 * @param DirtoTrack         target directory to write tracker data to
	 * @param callOnFinishAction if set to true, on finish writing map
	 *                           {@link #onWriteMapAction} will be called
	 * @return
	 */
	public boolean writeMapDir(Path DirtoTrack, boolean callOnFinishAction) {
		try {
			File file = new File(DirtoTrack.resolve(UserFileName).toString());
			// https://howtodoinjava.com/java/io/java-write-to-file/
			if (file.exists()) {
				file.delete();
			}
			// resolve all character
			// https://stackoverflow.com/questions/1001540/how-to-write-a-utf-8-file-with-java
			// Always use UTF_8 for writing reading tracker files so no conflict occur when
			// running under different environment
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);

			// check https://www.baeldung.com/java-string-newline
			String content = FIRST_LINE_TRACKER;

			for (Path path : mapDetailsRevolved.keySet()) {
				content += mapDetailsRevolved.get(path).toString();
			}
			Files.setAttribute(file.toPath(), "dos:hidden", true);
			writer.write(content);
			writer.close();
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
	 * @return <code>true</code> if write was successful<br>
	 *         <code>false</code> if tracker data already exist
	 * @throws IOException
	 */
	private static boolean writeNewDefaultMap(Path DirtoTrack, OnWriteMapFinishCallBack<Path> onFinishAction)
			throws IOException {
		File file = new File(DirtoTrack.resolve(UserFileName).toString());
		if (file.exists()) {
			return false;
		}
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
		String content = FIRST_LINE_TRACKER;
		File[] listFiles = DirtoTrack.toFile().listFiles(outfile -> !outfile.isHidden());
		for (File listFile : listFiles) {
			if (!listFile.getName().equals(UserFileName)) {
				content += String.join(">", Arrays.asList(listFile.getName(), "0", " ")) + "\r\n";
			}
		}
		Files.setAttribute(file.toPath(), "dos:hidden", true);
		writer.write(content);
		writer.close();
		if (onFinishAction != null) {
			onFinishAction.handle(DirtoTrack);
		}
		return true;
	}

	/**
	 * -> Track a new Folder with default option <br>
	 * -> Load tracker data into current map<br>
	 * <br>
	 * More Details about initial data saved at<br>
	 * {@link #writeNewDefaultMap(Path, OnWriteMapFinishCallBack)}
	 *
	 * @return true if successfully write data
	 * @throws IOException
	 */
	public boolean trackNewFolder() throws IOException {
		// prevent wipe is checked in write
		boolean isSuccess = writeNewDefaultMap(workingDirPath, onWriteMapAction);
		if (isSuccess) {
			loadMap(workingDirPath, true);
		}
		return isSuccess;
	}

	/**
	 * If directory already tracked do nothing <br>
	 * if you wish to load map after folder being tracked call
	 * {@link #loadMap(Path, boolean)} <br>
	 *
	 * More Details about initial data saved at<br>
	 * {@link #writeNewDefaultMap(Path, OnWriteMapFinishCallBack)}
	 *
	 * @param DirtoTrack
	 * @return <code>true</code> if write was successful<br>
	 *         <code>false</code> if tracker data already exist
	 * @throws IOException
	 */
	public boolean trackNewOutFolder(Path DirtoTrack) throws IOException {
		// write prevent wipe old data is done in the call
		return writeNewDefaultMap(DirtoTrack, null);
	}

	/**
	 * -> Track every untracked Folder with default option <br>
	 * -> Load tracker data into current map<br>
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
	public FileTrackerMultipleReturn trackNewMultipleFolder(Set<Path> DirstoTrack, boolean showDialogError) {
		FileTrackerMultipleReturn toReturn = new FileTrackerMultipleReturn();
		String error = "";
		for (Path path : DirstoTrack) {
			try {
				if (trackNewOutFolder(path)) {
					loadMap(path, false);
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
	public static FileTrackerMultipleReturn filterTrackedFolder(Set<Path> dirsToCheck) {
		FileTrackerMultipleReturn toReturn = new FileTrackerMultipleReturn();
		for (Path d : dirsToCheck) {
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
	public FileTrackerMultipleReturn trackNewMultipleAndAsk(Set<Path> paths, boolean showDialogError) {
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
	 * if some changes happened to directory this function will clear all useless
	 * data (moved/deleted files) also add new created files.<br>
	 * Just a note {@link SplitViewController#refresh(String)} do call this function
	 * after loading map {@link #loadMap(Path, boolean)} and before loading views
	 * stuff
	 *
	 * @throws IOException
	 */
	public void resolveConflict() throws IOException {
		if (!isTracked()) {
			return;
		}
		String currentConflict = "";
		ArrayList<Path> toremove = new ArrayList<>();
		Set<Path> dirList = FileHelper.getListPathsNoHidden(workingDirPath);
		for (Path p : dirList) {
			if (!mapDetailsRevolved.containsKey(p)) {
				// @addinhere
				currentConflict = "  - New \t" + p.getFileName() + "\n" + currentConflict;
				mapDetailsRevolved.put(p, new FileTrackerHolder(p.getFileName().toString()));
			}
		}

		for (Path key : mapDetailsRevolved.keySet()) {

			if (mapDetailsRevolved.get(key).getTimeToLive() > 0) {
				// File can stay maybe it's coming in some operation
				// but if it exist in directory do clear TimeToLive
				if (dirList.contains(key)) {
					mapDetailsRevolved.get(key).setTimeToLive(-1);
				}
				continue;
			} else if (!dirList.contains(key)) {
				if (mapDetailsRevolved.get(key).getTimeToLive() != 0) {
					currentConflict = "  - Del \t" + key + "\n" + currentConflict;
				}
				toremove.add(key);
			}
		}
		for (Path key : toremove) {
			mapDetailsRevolved.remove(key);
		}
		if (!currentConflict.isEmpty()) {
			ConflictLog = "\n\n* " + Setting.getActiveUser() + " <<>> " + workingDirPath.toString() + "\n"
					+ currentConflict + ConflictLog;
		}
		if (!currentConflict.isEmpty()) {
			writeMapDir(workingDirPath, false);
		}
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
	 *                  Note: MOVE operation cannot be used for rename
	 * @throws IndexOutOfBoundsException in case both list weren't the same size
	 * @return MapDetails containing all new updated sources, empty in case of
	 *         delete
	 */
	public static Map<Path, FileTrackerHolder> operationUpdateAsList(List<Path> sources, List<Path> targets,
			ActionOperation operation) throws IndexOutOfBoundsException {
		if (sources.size() != targets.size()) {
			throw new IndexOutOfBoundsException("Sources list and Target list parameter must be same size");
		}
		// All key changed map
		Map<Path, FileTrackerHolder> allUpdatedSources = new HashMap<Path, FileTrackerHolder>();
		if (sources.size() == 0) {
			return allUpdatedSources;
		}
		// a map from a (target) directory to sources that have common target
		// directory
		HashMap<Path, List<Path>> targetDirToSources = new HashMap<>();
		for (int i = 0; i < sources.size(); i++) {
			if (sources.get(i) == null || targets.get(i) == null) {
				continue;
			}
			Path targetParent = targets.get(i).getParent();
			if (!targetDirToSources.containsKey(targets.get(i).getParent())) {
				targetDirToSources.put(targetParent, new ArrayList<Path>());
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
			Set<Path> allSrcParent = FileHelper.getParentsPathsFromPath(sources);
			FileTracker miniFileTracker = new FileTracker(null, null);
			allSrcParent.forEach(parentSrc -> {
				miniFileTracker.loadMap(parentSrc, false);
			});
			// append MAXIMUM time to live option in target directory files of targets list
			// Path
			HashMap<Path, Path> srcToTarget = new HashMap<>();
			for (int i = 0; i < sources.size(); i++) {
				if (sources.get(i) == null || targets.get(i) == null) {
					continue;
				}
				srcToTarget.put(sources.get(i), targets.get(i));
			}
			targetDirToSources.forEach((target, miniSources) -> {
				Holder<OutputStreamWriter> writer = new Holder<>();
				try {
					File trackerFile = FileTracker.getTrackerFileIn(target).toFile();
					// appending data to end with time to live
					writer.value = new OutputStreamWriter(new FileOutputStream(trackerFile, true),
							StandardCharsets.UTF_8);
					for (Path src : miniSources) {
						if (miniFileTracker.getMapDetailsRevolved().containsKey(src)) {
							writer.value.write(miniFileTracker.getTrackerData(src)
									.setName(srcToTarget.get(src).getFileName().toString())
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
	 * For rename use
	 *
	 * When doing a copy or move operations: It is necessary to preserve the tracker
	 * data, and in case of delete do nothing about it. But clearing from conflict
	 * log is optional
	 *
	 * @param source    The list of path to copy/move/delete
	 * @param targetDir The target directory where these path are copied/moved<br>
	 *                  Can be null in case of delete, will auto get parent from
	 *                  source
	 * @param operation check {@link ActionOperation}
	 * @return MapDetails containing all new updated sources, empty in case of
	 *         delete
	 */
	public static Map<Path, FileTrackerHolder> operationUpdate(List<Path> source, Path targetDir,
			ActionOperation operation) {
		// get parent to son map to deal with each list of son brother together
		HashMap<Path, List<Path>> parentToFiles = FileHelper.getParentTochildren(source);
		// All key changed map
		Map<Path, FileTrackerHolder> allUpdatedSources = new HashMap<Path, FileTrackerHolder>();
		if (source.size() == 0) {
			return allUpdatedSources;
		}
		// instance of file tracker to read/write tracker data
		FileTracker senderFileTracker = new FileTracker(null, null);

		try {
			File trackerFile = null;
			Holder<OutputStreamWriter> writer = new Holder<OutputStreamWriter>(null);
			if (operation.equals(ActionOperation.DELETE)) {
				// will open stream file for each list of son
				targetDir = null;// to prevent further access
			} else {
				trackerFile = new File(targetDir.resolve(UserFileName).toString());
				if (!trackerFile.exists()) {
					writeNewDefaultMap(targetDir, null);
				}
				writer.value = new OutputStreamWriter(new FileOutputStream(trackerFile, true), StandardCharsets.UTF_8);
			}
			parentToFiles.forEach((parent, brotherList) -> {
				senderFileTracker.workingDirPath = parent;
				if (senderFileTracker.isTracked()) {
					// parent Tracker file exist -> Data sources exist
					senderFileTracker.loadMap(parent, true);
					try {
						// Append Data to parent Tracker file with some time to live
						if (operation.equals(ActionOperation.DELETE)) {
							if (writer.value != null) {
								writer.value.close();
							}
							writer.value = new OutputStreamWriter(
									new FileOutputStream(senderFileTracker.getTrackerFileInWorkingDir().toFile(), true),
									StandardCharsets.UTF_8);
						}
						brotherList.forEach(son -> {
							try {
								if (operation.equals(ActionOperation.DELETE)) {
									writer.value
											.write(senderFileTracker.getTrackerData(son).setTimeToLive(5).toString());
								} else {
									writer.value.write(senderFileTracker.getTrackerData(son)
											.setTimeToLive(TIME_TO_LIVE_MAX).toString());
									allUpdatedSources.put(son, senderFileTracker.getTrackerData(son));
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
				}
			});
			writer.value.close();
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
	public FileTrackerHolder getTrackerData(Path key) {
		return mapDetailsRevolved.get(key);
	}

	/**
	 *
	 * @param paths
	 * @return stream of paths existing in map details
	 */
	private Stream<FileTrackerHolder> getExistingPathInMap(List<Path> paths) {
		return paths.stream().filter(p -> mapDetailsRevolved.containsKey(p)).map(p -> mapDetailsRevolved.get(p));
	}

	/**
	 *
	 * @param key
	 * @return null if not in map, Seen Status otherwise
	 */
	@Nullable
	public Boolean isSeen(Path key) {
		return getTrackerData(key) == null ? null : getTrackerData(key).isSeen();
	}

	/**
	 * Does not write map, to do so use {@link #commitTrackerDataChange(Path)}
	 *
	 * @param path
	 * @param isSeen
	 */
	public void setSeen(Path path, Boolean isSeen) {
		if (mapDetailsRevolved.containsKey(path)) {
			mapDetailsRevolved.get(path).setSeen(isSeen);
		}
	}

	/**
	 * Does not write map, to do so use {@link #commitTrackerDataChange(Path)}
	 *
	 * @param paths
	 * @param isSeen
	 */
	public void setSeen(List<Path> paths, Boolean isSeen) {
		getExistingPathInMap(paths).forEach(tData -> tData.setSeen(isSeen));
	}

	/**
	 * Does not write map, to do so use {@link #commitTrackerDataChange(Path)}
	 *
	 * @param path
	 */
	public void toggleSeen(Path path) {
		if (mapDetailsRevolved.containsKey(path)) {
			mapDetailsRevolved.get(path).toggleSeen();
		}
	}

	/**
	 * Does not write map, to do so use {@link #commitTrackerDataChange(Path)}
	 *
	 * @param keyPath
	 */
	public void toggleSeen(List<Path> paths) {
		getExistingPathInMap(paths).forEach(tData -> tData.toggleSeen());
	}

	/**
	 *
	 * @param path
	 * @return note text if exist, empty String otherwise
	 */
	public String getNoteText(Path path) {
		if (!mapDetailsRevolved.containsKey(path)) {
			return "";
		}
		String ans = mapDetailsRevolved.get(path).getNoteText();
		return ans.equals(" ") ? "" : ans;
	}

	/**
	 * Does not write map, to do so use {@link #commitTrackerDataChange(Path)}
	 *
	 * @param path
	 * @param note
	 */
	public void setNoteText(Path path, String note) {
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
	 * Does not write map, to do so use {@link #commitTrackerDataChange(Path)}
	 *
	 * @param paths
	 * @param note
	 */
	public void setNoteText(List<Path> paths, String note) {
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
	public void commitTrackerDataChange(Path path) {
		if (!isLoadedOtherThanWorkingDir) {
			writeMapDir(workingDirPath, true);
		} else {
			// creating a new file tracker is indeed
			FileTracker miniFileTracker = new FileTracker(null, onWriteMapAction);
			miniFileTracker.loadMap(path.getParent(), true);
			miniFileTracker.getMapDetailsRevolved().put(path, getTrackerData(path));
			miniFileTracker.commitTrackerDataChange(path);
		}
	}

	/**
	 * Do write map for list of Paths
	 */
	public void commitTrackerDataChange(List<Path> paths) {
		if (!isLoadedOtherThanWorkingDir) {
			writeMapDir(workingDirPath, true);
		} else {
			// creating a new file tracker is indeed
			FileTracker miniFileTracker = new FileTracker(null, onWriteMapAction);
			FileHelper.getParentTochildren(paths).forEach((parent, sons) -> {
				miniFileTracker.loadMap(parent, true);
				sons.forEach(son -> miniFileTracker.getMapDetailsRevolved().put(son, getTrackerData(son)));
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
	private void appendCommitTrackerData(List<Path> paths, Path parent) throws IOException {
		File trackerFile = getTrackerFileIn(parent).toFile();
		// appending data to end with time to live
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(trackerFile, true),
				StandardCharsets.UTF_8);
		for (Path changed : paths) {
			if (getMapDetailsRevolved().containsKey(changed)) {
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
	public static void appendTrackerData(Path path, FileTrackerHolder trackerData, boolean writeANewMapIfNeeded)
			throws IOException {
		Path parent = path.getParent();
		if (!isTrackedOutFolder(parent)) {
			if (writeANewMapIfNeeded) {
				writeNewDefaultMap(parent, null);
			} else {
				return;
			}
		}
		File trackerFile = getTrackerFileIn(parent).toFile();
		// appending data to end with time to live
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(trackerFile, true),
				StandardCharsets.UTF_8);
		writer.write(trackerData.toString());
		writer.close();
	}
}
