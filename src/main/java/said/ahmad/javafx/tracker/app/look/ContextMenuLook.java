package said.ahmad.javafx.tracker.app.look;

import javafx.scene.image.Image;
import said.ahmad.javafx.tracker.app.ResourcesHelper;

public class ContextMenuLook {

	public static int requestedWH = 30;

	public static final Image openIcon = new Image(ResourcesHelper.getResourceAsStream("/img/context_menu/open.png"),
			requestedWH, requestedWH, false, true);

	public static final Image copyIcon = new Image(ResourcesHelper.getResourceAsStream("/img/context_menu/copy.png"),
			requestedWH, requestedWH, false, true);

	public static final Image moveIcon = new Image(ResourcesHelper.getResourceAsStream("/img/context_menu/move.png"),
			requestedWH, requestedWH, false, true);

	public static final Image clipboardIcon = new Image(
			ResourcesHelper.getResourceAsStream("/img/context_menu/clipboard.png"), requestedWH, requestedWH, true,
			true);

	public static final Image renameIcon = new Image(
			ResourcesHelper.getResourceAsStream("/img/context_menu/rename.png"), requestedWH, requestedWH, false, true);

	public static final Image bulkRenameIcon = new Image(
			ResourcesHelper.getResourceAsStream("/img/context_menu/bulk_rename.png"), requestedWH, requestedWH, false,
			true);
	public static final Image copyBaseNameIcon = new Image(
			ResourcesHelper.getResourceAsStream("/img/context_menu/copy_base_name.png"), requestedWH, requestedWH,
			false, true);
	public static final Image pasteBaseNameIcon = new Image(
			ResourcesHelper.getResourceAsStream("/img/context_menu/paste_base_name.png"), requestedWH, requestedWH,
			false, true);
	public static final Image undoIcon = new Image(ResourcesHelper.getResourceAsStream("/img/context_menu/undo.png"),
			requestedWH, requestedWH, false, true);

	public static final Image newIcon = new Image(ResourcesHelper.getResourceAsStream("/img/context_menu/new.png"),
			requestedWH, requestedWH, false, true);
	public static final Image fileIcon = new Image(ResourcesHelper.getResourceAsStream("/img/context_menu/file.png"),
			requestedWH, requestedWH, false, true);
	public static final Image folderIcon = new Image(
			ResourcesHelper.getResourceAsStream("/img/context_menu/folder.png"), requestedWH, requestedWH, false, true);

	public static final Image deleteIcon = new Image(
			ResourcesHelper.getResourceAsStream("/img/context_menu/delete.png"), requestedWH, requestedWH, false, true);

	public static final Image systemIcon = new Image(
			ResourcesHelper.getResourceAsStream("/img/context_menu/system.png"), requestedWH, requestedWH, false, true);

	public static final Image cancelIcon = new Image(
			ResourcesHelper.getResourceAsStream("/img/context_menu/cancel.png"), requestedWH, requestedWH, false, true);

	public static final Image trackerIcon = new Image(
			ResourcesHelper.getResourceAsStream("/img/context_menu/tracker.png"), requestedWH, requestedWH, false,
			true);
	public static final Image trackerDataIcon = new Image(
			ResourcesHelper.getResourceAsStream("/img/context_menu/tracker_data.png"), requestedWH, requestedWH, false,
			true);
	public static final Image noteIcon = new Image(ResourcesHelper.getResourceAsStream("/img/context_menu/note.png"),
			requestedWH, requestedWH, false, true);
	public static final Image cortanaIcon = new Image(
			ResourcesHelper.getResourceAsStream("/img/context_menu/cortana.png"), requestedWH, requestedWH, false,
			true);
	public static final Image vlcIcon = new Image(ResourcesHelper.getResourceAsStream("/img/context_menu/vlc.png"),
			requestedWH, requestedWH, false, true);
}
