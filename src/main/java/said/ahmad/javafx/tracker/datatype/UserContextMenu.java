package said.ahmad.javafx.tracker.datatype;

import java.util.*;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import javafx.scene.image.Image;
import lombok.Getter;
import lombok.Setter;
import said.ahmad.javafx.tracker.app.StringHelper;
import said.ahmad.javafx.tracker.app.look.IconLoader;
import said.ahmad.javafx.tracker.app.pref.Setting;
import said.ahmad.javafx.tracker.fxGraphics.ImageGridItem;
import said.ahmad.javafx.tracker.system.SystemIconsHelper;
import said.ahmad.javafx.tracker.system.call.CallMethod;
import said.ahmad.javafx.tracker.system.call.CommandVariable;
import said.ahmad.javafx.tracker.system.call.CommandVariableAffector;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.tracker.system.file.PathLayerHelper;
import said.ahmad.javafx.util.ArrayListHelper;

@Getter
@Setter
public class UserContextMenu implements Cloneable {

	public static IconLoader.ICON_TYPE DEFAULT_ICON = IconLoader.ICON_TYPE.BLUE_CIRCLE;
	/**
	 * Used to activate the context menu
	 */
	private boolean isActive = true;

	/**
	 * Parent menu name to be used to add the options. <br>
	 * Examples: <br>
	 * <ul>
	 * <li><code>null</code> -> add as new menu</li>
	 * <li><code>"Parent"</code> -> added under Parent menu</li>
	 * <li><code>"Parent/Submenu/child"</code> added under parent in another menu
	 * </li>
	 * </ul>
	 */
	private String parentMenuNames;

	/**
	 * Specify the menu order to be inserted in the existing menu 1 to be first.
	 * 
	 * @see ArrayListHelper#getCyclicIndex
	 */
	private int menuOrder;

	/**
	 * Alias to be used in context menu. Can be combination of constant literal
	 * string and command variable to be affected that depend on each file <br>
	 * * Example: "Add to archive %BASENAME%.rar"
	 * 
	 * @see CommandVariable
	 */
	private String alias;

	/**
	 * Alias to be used in context menu when multiple files is selected. Can be
	 * combination of constant literal string and command variable to be affected
	 * that depend on each file <br>
	 * If alias is not set, the alias for single selection is used.<br>
	 * Example: "Add to archive %1PARENT_NAME%.rar"
	 *
	 * @see CommandVariable
	 */
	private String aliasMultiple;
	
	/**
	 * Path to executable file, do accept env variable
	 *
	 * @see #getPathToExecutableAsPath()
	 */
	private String pathToExecutable;


	/**
	 * In order to use icon image from saved within application the convention is to
	 * use "INNER_ICON-ENUM_ICON_TYPE" <br>
	 * Example: "INNER_ICON-BLUE_CIRCLE" will use
	 * {@link IconLoader.ICON_TYPE#BLUE_CIRCLE} as icon image
	 * 
	 * @see IconLoader.ICON_TYPE
	 * @see #evaluateAndGetImage
	 */
	public static String INNER_ICON_CONVENTION = "INNER_ICON-";

	/**
	 * Optional path used for icon
	 * and with idea that pathToExecutable can be inner function and not converted into pathlayer
	 */
	private String iconPath;

	/**
	 * Optional path used for icon
	 * // we can expand definition to support multiple icon path separated by semi colon
	 * // saved image will be an array list of image of all parents, such implementation
	 * // depend on its use case
	 */
	private String parentIconPath;

	/**
	 * Options for call to be added before the file <br>
	 * Note: there is no space added between prefix evaluated command and selection
	 * files, so add manually if needed. because sometime argument need to be
	 * concatenated with file path example @file. <br>
	 * <ul>
	 * Template Format:
	 * <li>executable.exe <b> prefixCommandOptions</b>SelectionFiles</li>
	 * </ul>
	 * 
	 * @see #getPostfixCommandOptions()
	 */
	private String prefixCommandOptions;

	/**
	 * Options for call to be added after the file. <br>
	 * Note: there is automatic addition of space between prefixCommandOptions and
	 * selection files.<br>
	 * Example executable.exe selectionFiles <b> postfixCommandOptions </b>
	 */
	private String postfixCommandOptions;

	/**
	 * Call behavior on multiple selection
	 */
	private CallMethod callMethod;

	/**
	 * Call will use relatif path of files when files are in same directory. When
	 * calling a file using executable file command will be like in case of: <br>
	 * <ul>
	 * <li>true: relative path, will use relative path when possible and
	 * working directory of the process will be in same file location ex.: <br>
	 * C:\Program Files\Program.exe myFile.txt</li>
	 * <li>false: full path, ex.: <br>
	 * C:\Program Files\Program.exe D:\somewhere\myFile.txt</li>
	 * </ul>
	 */
	private boolean CallUsingRelatifPath = true;

	/**
	 * Add context menu to the list of extensions.<br>
	 * - Use "*" to add context to all files. <br>
	 * - add dash before extension to make exclusion example: "-txt" to exclude
	 * extension "txt"
	 * 
	 * @see #getCompatibleExtensions() getCompatibleExtensions() for business usage
	 */
	@Getter
	private List<String> extensions = new ArrayList<>();

	/**
	 * Use predefined groups of extension. Format of the extensions list mapping is
	 * the same as {@link #getExtensions()}
	 * 
	 * @see Setting#getExtensionGroups() Setting#getExtensionGroups() to parse
	 *      values
	 * @see #getCompatibleExtensions() getCompatibleExtensions() for business usage
	 */
	@Getter
	private List<String> extensionsGroupNames = new ArrayList<>();

	/**
	 * Add context menu to directory file type
	 */
	private boolean isDirectoryContext;

	/**
	 * Add context menu if a single file is selected
	 */
	private boolean isOnSingleSelection;

	/**
	 * Add context menu if multiple files are selected
	 */
	private boolean isOnMultipleSelection;

	/**
	 * Display in a UI the process output
	 */
	private boolean displayUIProcessOutput = false;

	/**
	 * cached image of {@link #iconPath}
	 */
	@XStreamOmitField
	private Image iconImage;

	/**
	 * cached image of {@link #pathToExecutable}
	 */
	@XStreamOmitField
	private Image iconExec;

	/**
	 * cached image of {@link #parentIconPath}
	 */
	@XStreamOmitField
	private Image parentIconImage;

	/**
	 * Fx image based on {@link #getIconPath()} file if it exists. if not, return
	 * the executable icon of file used in File System. <br>
	 *
	 * Once this image loaded, it is cached in memory. <br>
	 *
	 * This image is used for parent menu if {@link #getParentIconPath()} not
	 * defined and for menu item by default.
	 *
	 * @return Icon image to be used in context menu graphics
	 */
	public Image getIconImage() {
		if (iconImage == null) {
			iconImage = getImageWithTryOf(iconPath);
		}
		return iconImage;
	}

	/**
	 * Image based on {@link #getParentIconPath()} if it is defined. if not,
	 * return {@link #getIconImage()}. <br>
	 *
	 * Once this image loaded, it is cached in memory. <br>
	 *
	 * @return Icon image to be used in context menu graphics
	 */
	public Image getParentIconImage() {
		if (parentIconImage == null) {
			parentIconImage = getImageWithTryOf(parentIconPath);
		}
		return parentIconImage;
	}

	/**
	 * Will try to get image from path parameter. if failed will try to get image from executable path.
	 * if failed will return default icon image<br>
	 * Call in order:
	 * <ul>
	 *     <li>{@link #evaluateAndGetImage}</li>
	 *     <li>{@link #getIconExec()}</li>
	 *     <li>{@link #DEFAULT_ICON}</li>
	 * </ul>
	 * @param iconPathImage
	 * @return not null image from path parameter, will use alternative if not found.
	 */
	private Image getImageWithTryOf(String iconPathImage) {
		// try to evaluate path
		Image img = evaluateAndGetImage(iconPathImage);

		// if failed use executable path icon
		if (img == null) {
			img = getIconExec();
		}

		// if failed use default icon image
		if (img == null) {
			img = IconLoader.getIconImage(DEFAULT_ICON);
		}
		return img;
	}

	/**
	 * Translate icon path image into an image, it can be:
	 * <ul>
	 *     <li>EXE file: extract system icon file</li>
	 *     <li>Image file: load image extension must be supported, see {@link ImageGridItem#ArrayIMGExt}</li>
	 *     <li>Inner icon: must follow the convention {@link #INNER_ICON_CONVENTION} : {@link IconLoader.ICON_TYPE}</li>
	 * </ul>
	 * Note: path can contain environment variable in the form of %env%
	 * @param iconPathImage
	 * @return loaded image if possible, null otherwise.
	 */
	public static Image evaluateAndGetImage(String iconPathImage) {
		if (iconPathImage == null) {
			return null;
		}
		// String use inner icon convention
		int indexOfInner = iconPathImage.toUpperCase().indexOf(INNER_ICON_CONVENTION);
		if (indexOfInner != -1) {
			try {

				IconLoader.ICON_TYPE iconType = IconLoader.ICON_TYPE
						.valueOf(iconPathImage.substring(indexOfInner + INNER_ICON_CONVENTION.length()).toUpperCase());
				return IconLoader.getIconImage(iconType);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}

		// String is a path to a file
		return IconLoader.getIconImage(PathLayerHelper.evaluateEnvVariableInPath(iconPathImage));
	}

	/**
	 * Fx image based on the executable file icon used in System. <br>
	 * Once this image loaded, it is cached in memory.
	 *
	 * @return
	 */
	public Image getIconExec() {
		if (iconExec == null) {
			PathLayer executableAsPath = getPathToExecutableAsPath();
			if (executableAsPath != null && executableAsPath.exists()) {
				iconExec = SystemIconsHelper.getFileIcon(executableAsPath);
			} else {
				iconExec = IconLoader.getIconImage(DEFAULT_ICON);
			}
		}
		return iconExec;
	}

	/**
	 * Clear extensions and extensions groups lists
	 *
	 * @see #getExtensions()
	 * @see #getExtensionsGroupNames()
	 */
	public void clearDefinedExtensions() {
		getExtensions().clear();
		getExtensionsGroupNames().clear();
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		UserContextMenu ct = (UserContextMenu) super.clone();
		ct.setExtensions(new ArrayList<>(getExtensions()));
		ct.setExtensionsGroupNames(new ArrayList<>(getExtensionsGroupNames()));
		return ct;
	}

	/**
	 * Evaluate environment variable if found
	 *
	 * @return path to executable file, null in case of
	 *         {@link CallMethod#INNER_FUNCTION}, the path otherwise
	 * @see PathLayerHelper#evaluateEnvVariableInPath(String)
	 */
	public PathLayer getPathToExecutableAsPath() {
		if (callMethod.equals(CallMethod.INNER_FUNCTION)) {
			return null;
		}
		return PathLayerHelper.evaluateEnvVariableInPath(getPathToExecutable());
	}

	/**
	 * A title describing menu using its {@link #getAlias()} if present, <br>
	 * otherwise {@link #getAliasMultiple()} if present,<br>
	 * otherwise an empty String "";<br>
	 * 
	 * @return title representing the menu
	 */
	public String getTitle() {
		String title;
		if (!StringHelper.isEmpty(getAlias())) {
			title = getAlias();
		} else if (!StringHelper.isEmpty(getAliasMultiple())) {
			title = getAliasMultiple();
		} else {
			title = "";
		}
		if(getParentMenuNames() != null) {
			title = getParentMenuNames() + "/" + title;
		}
		return  title;
	}

	/**
	 * Evaluate String using
	 * {@link CommandVariableAffector#getEvaluatedCommand}
	 *
	 * @param selections
	 *            files to work with
	 * @return evaluated alias
	 */
	public String getAliasEvaluated(List<PathLayer> selections) {
		if (selections.size() > 1 && isOnMultipleSelection() && aliasMultiple != null && !aliasMultiple.isEmpty()) {
			return CommandVariableAffector.getEvaluatedCommand(selections, null, getAliasMultiple());
		}
		return CommandVariableAffector.getEvaluatedCommand(selections, null, getAlias());
	}

	/**
	 * Evaluate String using
	 * {@link CommandVariableAffector#getEvaluatedCommand}
	 *
	 * @param selections
	 *            files to work with
	 * @return evaluated prefix options
	 */
	public String getPrefixCommandOptionsEvaluated(List<PathLayer> selections, @Nullable PathLayer priorityPath) {
		return CommandVariableAffector.getEvaluatedCommand(selections, priorityPath, getPrefixCommandOptions());
	}

	/**
	 * Evaluate String using
	 * {@link CommandVariableAffector#getEvaluatedCommand}
	 *
	 * @param selections
	 *            files to work with
	 * @return evaluated postfix options
	 */
	public String getPostfixCommandOptionsEvaluated(List<PathLayer> selections, @Nullable PathLayer priorityPath) {
		return CommandVariableAffector.getEvaluatedCommand(selections, priorityPath, getPostfixCommandOptions());
	}

	/**
	 * This function do not eliminate "-ext" from the list.<br>
	 * Use getExcludedExtensions() to get those.
	 * 
	 * @return set of all compatible extension in UPPERCASE joining
	 *         {@link #getExtensions()} and {@link #getExtensionsGroupNames()}
	 *         values
	 */
	public Set<String> getCompatibleExtensions() {
		Set<String> allExt = new HashSet<>(StringHelper.getUpperCaseList(extensions));
		extensionsGroupNames.stream().map(extGrp -> Setting.getExtensionGroups().get(extGrp))
				.filter(Objects::nonNull)
				.forEach(extGrp -> allExt.addAll(StringHelper.getUpperCaseList(extGrp)));
		return allExt;
	}

	/**
	 * Excluded extension is useful when using "*" and want to exclude certain
	 * extension.
	 *
	 * @return UPPERCASE list of compatible extension that are excluded i.e.
	 *         starting with dash. Example for "-txt", it returns "txt".
	 * @see #getExtensions()
	 */
	public Set<String> getExcludedExtensions() {
		Set<String> allExt = getCompatibleExtensions();
		Set<String> allExcludedExt = allExt.stream().filter(ext -> ext.startsWith("-")).map(ext -> ext.substring(1))
				.collect(Collectors.toSet());
		extensionsGroupNames.stream().filter(extGrp -> extGrp.startsWith("-"))
				.map(extGrp -> Setting.getExtensionGroups().get(extGrp.substring(1))).filter(Objects::nonNull)
				.forEach(extGrp -> allExcludedExt.addAll(StringHelper.getUpperCaseList(extGrp)));
		return allExcludedExt;
	}

	/**
	 * Check whenever this context menu is compatible with list of files provided in
	 * parameter. i.e. all files must have extension supported by this menu, or it
	 * is a directory compatible.
	 *
	 * @param selections
	 * @return
	 */
	public boolean isCompatibleWithList(List<PathLayer> selections) {
		if (!isActive)
			return false;
		if (selections.size() == 0)
			return false;
		if (!isOnSingleSelection() && selections.size() == 1)
			return false;
		if (!isOnMultipleSelection() && selections.size() > 1)
			return false;
		if (!isDirectoryContext() && selections.stream().anyMatch(PathLayer::isDirectory))
			return false;

		Set<String> allExt = getCompatibleExtensions();
		Set<String> allExcludedExt = getExcludedExtensions();

		if (selections.stream().anyMatch(s -> allExcludedExt.contains(s.getExtensionUPPERCASE())))
			return false;
		if (allExt.contains("*"))
			return true;
		return selections.stream().filter(path -> !path.isDirectory())
				.allMatch(s -> allExt.contains(s.getExtensionUPPERCASE()));
	}

	public void clearIconCache() {
		setIconExec(null);
		setIconImage(null);
		setParentIconImage(null);
	}
}
