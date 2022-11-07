package said.ahmad.javafx.tracker.app.pref;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import said.ahmad.javafx.tracker.datatype.UserContextMenu;
import said.ahmad.javafx.tracker.system.call.CallMethod;
import said.ahmad.javafx.tracker.system.call.CommandVariable;
import said.ahmad.javafx.tracker.system.call.inner.InnerFunctionCall;
import said.ahmad.javafx.tracker.system.call.inner.InnerFunctionName;

public class UserContextMenuDefaultSetting {

	public static Map<String, ArrayList<String>> getInitializedExtensionGroupsMap() {
		Map<String, ArrayList<String>> extensionGroupMap = new HashMap<>();

		extensionGroupMap.put("Text", new ArrayList<String>(Arrays.asList("TXT", "LOG", "HTML", "CSS", "JSP", "XML",
				"JSON", "JAVA", "C", "CPP", "PY", "SQL", "JS", "BAT", "CMD", "SH", "CFG", "")));
		extensionGroupMap.put("Video",
				new ArrayList<String>(Arrays.asList("3GP", "ASF", "AVI", "DVR-MS", "FLV", "MKV", "MIDI+", "MP4", "OGG",
						"OGM", "WAV", "MPEG-2", "MXF", "VOB", "RM", "BLU-RAY", "DVD-VIDEO", "VCD", "SVCD", "DVB",
						"HEIF", "AVIF", "WMV", "TS", "MPEG", "M4V")));
		extensionGroupMap.put("Audio",
				new ArrayList<String>(Arrays.asList("AAC", "AC3", "ALAC", "AMR", "DTS", "DVAudio", "XM", "FLAC", "It",
						"MACE", "MOD", "MP3", "Opus", "PLS", "QCP", "QDM2", "QDMC", "S3M", "TTA", "WMA")));
		extensionGroupMap.put("Playlist", new ArrayList<String>(Arrays.asList("XSPF", "M3U", "M3U8")));
		extensionGroupMap.put("Image",
				new ArrayList<>(Arrays.asList("PNG", "GIF", "JPG", "JPS", "MPO", "BMP", "WEBMP", "JPEG")));
		extensionGroupMap.put("Archive",
				new ArrayList<>(Arrays.asList("RAR", "ZIP", "CAB", "ARJ", "LZ", "TLZ", "LZH", "LHA", "ACE", "7Z", "TAR",
						"GZ", "TGZ", "UUE", "XXE", "UU", "BZ2", "TBZ2", "BZ", "TBZ", "JAR", "ISO", "Z", "TAZ", "XZ",
						"TXZ", "ZIPX", "001")));
		extensionGroupMap.put("Executable", new ArrayList<>(Arrays.asList("EXE", "EAR", "WAR", "JAR", "APK", "MSI")));
		return extensionGroupMap;
	}

	/**
	 * Generate following menus:<br>
	 * <ul>
	 * <li>Open with code</li>
	 * <li>Open with adobe</li>
	 * <li>WinRar: compress and extract (7 entries)</li>
	 * </ul>
	 * 
	 * @return all menus listed
	 * @see #getOpenWithCodeMenu()
	 * @see #getOpenWithAdobeMenu()
	 * @see #getAddSingleToWinRARMenu()
	 * @see #getAddMultipleToWinRARMenu()
	 * @see #getMoveSingleToWinRARMenu()
	 * @see #getMoveMultipleToWinRARMenu()
	 * @see #getExtractRARMenu()
	 * @see #getExtractMultipleRARMenu()
	 * @see #getExtractRARHereMenu()
	 */
	public static List<UserContextMenu> getInitializedMenuList() {
		List<UserContextMenu> menuList = new ArrayList<>();

		UserContextMenu editMenu = getOpenWithCodeMenu();
		menuList.add(editMenu);

		UserContextMenu openWithAdobe = getOpenWithAdobeMenu();
		menuList.add(openWithAdobe);

		/***** WinRAR section *****/
		UserContextMenu addToWinRAR = getAddMultipleToWinRARMenu();
		menuList.add(addToWinRAR);

		UserContextMenu winRarSingleAddToRar = getAddSingleToWinRARMenu();
		menuList.add(winRarSingleAddToRar);

		UserContextMenu moveToWinRAR = getMoveMultipleToWinRARMenu();
		menuList.add(moveToWinRAR);

		UserContextMenu winRarSingleMoveTo = getMoveSingleToWinRARMenu();
		menuList.add(winRarSingleMoveTo);

		UserContextMenu winRarExtractTo = getExtractRARMenu();
		menuList.add(winRarExtractTo);

		// extract all zip to
		UserContextMenu winRarMultipleExtractTo = getExtractMultipleRARMenu();
		menuList.add(winRarMultipleExtractTo);

		UserContextMenu winRarExtractHere = getExtractRARHereMenu();
		menuList.add(winRarExtractHere);

		UserContextMenu mergePdf = getMergePdfs();
		menuList.add(mergePdf);

		return menuList;
	}

	/**
	 * Context menu to open files with microsoft code.
	 *
	 * Parent: Open With
	 *
	 * Apply to: single, multiple and directory <br>
	 * files Extensions: "Text" group
	 *
	 * @return context menu
	 * @see #getInitializedExtensionGroupsMap()
	 */
	public static UserContextMenu getOpenWithCodeMenu() {
		UserContextMenu openWithCode = new UserContextMenu();
		openWithCode.setParentMenuNames("Open With");
		openWithCode.setAlias("Code");
		openWithCode.setAliasMultiple("Open all with code");
		openWithCode.setPathToExecutable("%LocalAppData%\\Programs\\Microsoft VS Code\\code.exe");
		openWithCode.getExtensionsGroupNames().add("Text");
		// * to match any file extension
		openWithCode.setCallMethod(CallMethod.COMBINED_CALL);
		openWithCode.setDirectoryContext(true);
		openWithCode.setOnSingleSelection(true);
		openWithCode.setOnMultipleSelection(true);
		openWithCode.setMenuOrder(2);
		return openWithCode;
	}

	/**
	 * Context menu to open pdf files with adobe acrobat DC.
	 *
	 * Parent: Open With
	 *
	 * Apply to: single, multiple including directory <br>
	 * files Extensions: pdf
	 *
	 * @return context menu
	 */
	public static UserContextMenu getOpenWithAdobeMenu() {
		UserContextMenu openWithAdobe = new UserContextMenu();

		openWithAdobe.setMenuOrder(2);
		openWithAdobe.setPathToExecutable("%ProgramFiles(x86)%\\Adobe\\Acrobat DC\\Acrobat\\Acrobat.exe");

		openWithAdobe.getExtensions().add("pdf");

		openWithAdobe.setOnSingleSelection(true);
		openWithAdobe.setAlias("Adobe Acrobat DC");
		openWithAdobe.setOnMultipleSelection(true);
		openWithAdobe.setAliasMultiple("Open all %" + CommandVariable.FILES_COUNT + "% with Adobe" );
		openWithAdobe.setParentMenuNames("Open With");

		openWithAdobe.setCallMethod(CallMethod.SEPARATE_CALL);
		return openWithAdobe;
	}


	/**
	 * Merge selected files into pdf
	 * @return
	 */
	public static UserContextMenu getMergePdfs() {
		return InnerFunctionCall.FUNCTION_CALLS.get(InnerFunctionName.MERGE_PDF).createDefaultUserContextMenu();
	}


	/**
	 * Basic context menu for WinRAR using default location
	 *
	 * @return context menu
	 */
	private static UserContextMenu getWinRARBaseMenu() {
		UserContextMenu winRAR = new UserContextMenu();
		winRAR.setPathToExecutable("%ProgramFiles%\\WinRAR\\WinRar.exe");
		winRAR.setParentMenuNames("WinRAR");
		return winRAR;
	}

	/**
	 * Context menu to compress multiple files in one RAR file having parent
	 * directory name.
	 *
	 * Parent: WinRAR
	 *
	 * Apply to: multiple including directory <br>
	 *
	 * files Extensions: * = all files
	 *
	 * @return context menu
	 */
	public static UserContextMenu getAddMultipleToWinRARMenu() {
		UserContextMenu addMultipleToWinRAR = getWinRARBaseMenu();
		addMultipleToWinRAR.setMenuOrder(-2);

		addMultipleToWinRAR.getExtensions().add("*");
		addMultipleToWinRAR.setDirectoryContext(true);

		addMultipleToWinRAR.setOnMultipleSelection(true);
		addMultipleToWinRAR.setAliasMultiple("Add to \"%" + CommandVariable.PARENT_NAME + "%.rar\"");

		addMultipleToWinRAR.setPrefixCommandOptions("a \"%" + CommandVariable.PARENT_NAME + "%.rar\" @");
		addMultipleToWinRAR.setCallMethod(CallMethod.TXT_FILE_CALL);
		addMultipleToWinRAR.setDisplayUIProcessOutput(true);

		return addMultipleToWinRAR;
	}

	/**
	 * Context menu to compress single files in one RAR file having same name
	 *
	 * Parent: WinRAR
	 *
	 * Apply to: single including directory <br>
	 *
	 * files Extensions: * = all files except archive group
	 *
	 * @return context menu
	 */
	public static UserContextMenu getAddSingleToWinRARMenu() {
		UserContextMenu addSingleToWinRAR = getAddMultipleToWinRARMenu();
		addSingleToWinRAR.getExtensionsGroupNames().add("-Archive");

		addSingleToWinRAR.setOnSingleSelection(true);
		addSingleToWinRAR.setAlias("Add to %" + CommandVariable.BASENAME + "%.rar");
		addSingleToWinRAR.setOnMultipleSelection(false);
		addSingleToWinRAR.setAliasMultiple(null);

		addSingleToWinRAR.setPrefixCommandOptions(
				"a \"%" + CommandVariable.PARENT_PATH + "%\\%" + CommandVariable.BASENAME + "%.rar\" ");
		addSingleToWinRAR.setCallMethod(CallMethod.SEPARATE_CALL);

		return addSingleToWinRAR;
	}

	/**
	 * Context menu to compress multiple files in one RAR file having parent
	 * directory name.<br>
	 * Those files are then deleted.
	 *
	 * Parent: WinRAR
	 *
	 * Apply to: multiple including directory <br>
	 *
	 * files Extensions: * = all files
	 *
	 * @return context menu
	 */
	public static UserContextMenu getMoveMultipleToWinRARMenu() {
		UserContextMenu moveMultipleToWinRAR = getAddMultipleToWinRARMenu();
		moveMultipleToWinRAR.setAliasMultiple("Move to %" + CommandVariable.PARENT_NAME + "%.rar ");
		moveMultipleToWinRAR.setPrefixCommandOptions("m \"%" + CommandVariable.PARENT_NAME + "%.rar\" @");
		return moveMultipleToWinRAR;
	}

	/**
	 * Context menu to compress single file in one RAR file having same name<br>
	 * this file is then deleted.
	 *
	 * Parent: WinRAR
	 *
	 * Apply to: single including directory <br>
	 *
	 * files Extensions: * = all files except archive group
	 *
	 * @return context menu
	 */
	public static UserContextMenu getMoveSingleToWinRARMenu() {
		UserContextMenu winRarSingleMoveTo = getAddSingleToWinRARMenu();
		winRarSingleMoveTo.setAlias("Move to %" + CommandVariable.BASENAME + "%.rar ");
		winRarSingleMoveTo.setPrefixCommandOptions("m \"%" + CommandVariable.BASENAME + "%.rar\" ");
		return winRarSingleMoveTo;
	}

	/**
	 * Context menu to extract archive into a new directory having same name.<br>
	 * for multiple archive extract each to separate directory. <br>
	 *
	 * Parent: WinRAR
	 *
	 * Apply to: single, multiple <br>
	 *
	 * files Extensions: Archive group
	 *
	 * @return context menu
	 */
	public static UserContextMenu getExtractRARMenu() {
		UserContextMenu winRarExtractTo = getAddSingleToWinRARMenu();

		winRarExtractTo.setOnSingleSelection(true);
		winRarExtractTo.setAlias("Extract To %" + CommandVariable.BASENAME + "%");
		winRarExtractTo.setOnMultipleSelection(true);
		winRarExtractTo.setAliasMultiple("Extract each to separate directory");

		winRarExtractTo.clearDefinedExtensions();
		winRarExtractTo.setDirectoryContext(false);
		winRarExtractTo.getExtensionsGroupNames().add("Archive");

		winRarExtractTo.setPrefixCommandOptions("x ");
		winRarExtractTo.setPostfixCommandOptions("\"%" + CommandVariable.BASENAME + "%\"\\");
		return winRarExtractTo;
	}

	/**
	 * Context menu to extract multiple archive file into one new directory having same name as parent.<br>
	 *
	 * Parent: WinRAR
	 *
	 * Apply to: single, multiple <br>
	 *
	 * files Extensions: Archive group
	 *
	 * @return context menu
	 */
	public static UserContextMenu getExtractMultipleRARMenu() {
		UserContextMenu winRarMultipleExtractTo = getExtractRARMenu();

		winRarMultipleExtractTo.setOnSingleSelection(false);
		winRarMultipleExtractTo.setAlias(null);
		winRarMultipleExtractTo.setAliasMultiple("Extract to %1" + CommandVariable.BASENAME + "%");

		winRarMultipleExtractTo.setPostfixCommandOptions("\"%1" + CommandVariable.BASENAME + "%\"\\");
		return winRarMultipleExtractTo;
	}

	/**
	 * Context menu to extract multiple archive file into one new directory having same name as parent.<br>
	 *
	 * Parent: WinRAR
	 *
	 * Apply to: single, multiple <br>
	 *
	 * files Extensions: Archive group
	 *
	 * @return context menu
	 */
	public static UserContextMenu getExtractRARHereMenu() {
		UserContextMenu winRarExtractHere = getExtractRARMenu();

		winRarExtractHere.setOnSingleSelection(true);
		winRarExtractHere.setAlias("Extract Here");
		winRarExtractHere.setOnMultipleSelection(true);
		winRarExtractHere.setAliasMultiple("Extract All Here");

		winRarExtractHere.setPostfixCommandOptions("");
		return winRarExtractHere;
	}

}
