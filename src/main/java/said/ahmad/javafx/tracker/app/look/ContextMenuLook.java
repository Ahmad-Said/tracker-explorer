package said.ahmad.javafx.tracker.app.look;

import javafx.scene.image.Image;
import said.ahmad.javafx.tracker.app.look.IconLoader.ICON_TYPE;

public class ContextMenuLook {

	/**
	 * Default requested width/height
	 */
	private static int defaultWH = 30;

	public static final Image openIcon = IconLoader.getIconImage(ICON_TYPE.OPEN, false, defaultWH, defaultWH);

	public static final Image copyIcon = IconLoader.getIconImage(ICON_TYPE.COPY, false, defaultWH, defaultWH);;

	public static final Image moveIcon = IconLoader.getIconImage(ICON_TYPE.MOVE, false, defaultWH, defaultWH);;

	public static final Image clipboardIcon = IconLoader.getIconImage(ICON_TYPE.CLIPBOARD, true, defaultWH, defaultWH);;

	public static final Image renameIcon = IconLoader.getIconImage(ICON_TYPE.RENAME, false, defaultWH, defaultWH);;

	public static final Image bulkRenameIcon = IconLoader.getIconImage(ICON_TYPE.BULK_RENAME, false, defaultWH,
			defaultWH);

	public static final Image bulkRenameUtilityIcon = IconLoader.getIconImage(ICON_TYPE.BULK_RENAME_UTILITY, false,
			defaultWH, defaultWH);
	public static final Image copyBaseNameIcon = IconLoader.getIconImage(ICON_TYPE.COPY_BASE_NAME, false, defaultWH,
			defaultWH);
	public static final Image pasteBaseNameIcon = IconLoader.getIconImage(ICON_TYPE.PASTE_BASE_NAME, false, defaultWH,
			defaultWH);
	public static final Image undoIcon = IconLoader.getIconImage(ICON_TYPE.UNDO, false, defaultWH, defaultWH);

	public static final Image newIcon = IconLoader.getIconImage(ICON_TYPE.NEW, false, defaultWH, defaultWH);
	public static final Image fileIcon = IconLoader.getIconImage(ICON_TYPE.FILE, false, defaultWH, defaultWH);
	public static final Image folderIcon = IconLoader.getIconImage(ICON_TYPE.FOLDER, false, defaultWH, defaultWH);

	public static final Image deleteIcon = IconLoader.getIconImage(ICON_TYPE.DELETE, false, defaultWH, defaultWH);

	public static final Image hiddenIcon = IconLoader.getIconImage(ICON_TYPE.HIDDEN, false, defaultWH, defaultWH);

	public static final Image sortIcon = IconLoader.getIconImage(ICON_TYPE.SORT, false, defaultWH, defaultWH);

	public static final Image systemIcon = IconLoader.getIconImage(ICON_TYPE.SYSTEM, false, defaultWH, defaultWH);

	public static final Image cancelIcon = IconLoader.getIconImage(ICON_TYPE.CANCEL, false, defaultWH, defaultWH);

	public static final Image trackerIcon = IconLoader.getIconImage(ICON_TYPE.TRACKER, false, defaultWH, defaultWH);
	public static final Image trackerDataIcon = IconLoader.getIconImage(ICON_TYPE.TRACKER_DATA, false, defaultWH,
			defaultWH);
	public static final Image noteIcon = IconLoader.getIconImage(ICON_TYPE.NOTE, false, defaultWH, defaultWH);

	public static final Image cortanaIcon = IconLoader.getIconImage(ICON_TYPE.CORTANA, false, defaultWH, defaultWH);
	public static final Image vlcIcon = IconLoader.getIconImage(ICON_TYPE.VLC, false, defaultWH, defaultWH);

	/**
	 * @return the defaultWH
	 */
	public static int getDefaultWidthHeight() {
		return defaultWH;
	}

	/**
	 * @param defaultWH the defaultWH to set
	 */
	public static void setDefaultWidthHeight(int defaultWH) {
		ContextMenuLook.defaultWH = defaultWH;
	}
}
