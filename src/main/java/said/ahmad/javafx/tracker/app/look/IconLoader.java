package said.ahmad.javafx.tracker.app.look;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import said.ahmad.javafx.tracker.app.ResourcesHelper;
import said.ahmad.javafx.tracker.fxGraphics.ImageGridItem;
import said.ahmad.javafx.tracker.system.SystemIconsHelper;
import said.ahmad.javafx.tracker.system.file.PathLayer;

public class IconLoader {
	/**
	 * Default requested Witdh, Height
	 */
	private static int defaultRequestedWH = 30;

	public enum ICON_TYPE {
		// Context Menu Stuff
		OPEN, OPEN_WITH, COPY, MOVE, CLIPBOARD,

		RENAME, BULK_RENAME, BULK_RENAME_UTILITY,

		COPY_BASE_NAME, PASTE_BASE_NAME,

		NEW, FILE, FOLDER, DELETE, RESOURCE_NOT_FOUND,

		HIDDEN, SORT,

		SYSTEM, CONTEXT_MENU,

		TRACKER, TRACKER_DATA, NOTE,

		CORTANA, VLC, PLAY_MEDIA,

		// Connection icons
		LINK_SYMBOL,

		// Image operation
		CROP, ROTATE_RIGHT, CENTRALIZE, FIT, PHOTO_FRAME,

		// Misc Stuff
		UP, DOWN, PLUS, ADD, MINUS, MERGE_ARROW,

		APPLY, UNDO, REFRESH,

		REMOVE, CANCEL, SELECT_ALL,

		SETTING, TOOL_BOX, INFORMATION, SAVE,

		DRAG, ZOOM, GRID,

		STAR, BLUE_CIRCLE,  USER,
	}

	public static final Map<ICON_TYPE, String> ENUM_TO_NAME = Collections.unmodifiableMap(new HashMap<IconLoader.ICON_TYPE, String>() {
		/**
		 *
		 */
		private static final long serialVersionUID = 8445384121964501348L;
		{
			// Context Menu Stuff
			put(ICON_TYPE.OPEN, "/img/context_menu/open.png");
			put(ICON_TYPE.OPEN_WITH, "/img/context_menu/open_with.png");
			put(ICON_TYPE.COPY, "/img/context_menu/copy.png");
			put(ICON_TYPE.MOVE, "/img/context_menu/move.png");
			put(ICON_TYPE.CLIPBOARD, "/img/context_menu/clipboard.png");
			put(ICON_TYPE.RENAME, "/img/context_menu/rename.png");
			put(ICON_TYPE.BULK_RENAME, "/img/context_menu/bulk_rename.png");
			put(ICON_TYPE.BULK_RENAME_UTILITY, "/img/context_menu/bulk_rename_utility.png");
			put(ICON_TYPE.COPY_BASE_NAME, "/img/context_menu/copy_base_name.png");
			put(ICON_TYPE.PASTE_BASE_NAME, "/img/context_menu/paste_base_name.png");

			put(ICON_TYPE.NEW, "/img/context_menu/new.png");
			put(ICON_TYPE.FILE, "/img/context_menu/file.png");
			put(ICON_TYPE.FOLDER, "/img/context_menu/folder.png");
			put(ICON_TYPE.DELETE, "/img/context_menu/delete.png");
			put(ICON_TYPE.RESOURCE_NOT_FOUND, "/img/file_not_found.png");
			put(ICON_TYPE.HIDDEN, "/img/context_menu/hidden.png");
			put(ICON_TYPE.SORT, "/img/context_menu/sort.png");
			put(ICON_TYPE.SYSTEM, "/img/context_menu/system.png");
			put(ICON_TYPE.CONTEXT_MENU, "/img/context_menu/context_menu.png");
			put(ICON_TYPE.TRACKER, "/img/context_menu/tracker.png");
			put(ICON_TYPE.TRACKER_DATA, "/img/context_menu/tracker_data.png");
			put(ICON_TYPE.NOTE, "/img/context_menu/note.png");
			put(ICON_TYPE.CORTANA, "/img/context_menu/cortana.png");
			put(ICON_TYPE.VLC, "/img/context_menu/vlc.png");
			put(ICON_TYPE.PLAY_MEDIA, "/img/filter_vlc.png");

			// Connections icons
			put(ICON_TYPE.LINK_SYMBOL, "/img/connection/link_symbol.png");

			// Image Stuff
			put(ICON_TYPE.CROP, "/img/img_operation/crop.png");
			put(ICON_TYPE.ROTATE_RIGHT, "/img/img_operation/rotate_right.png");
			put(ICON_TYPE.CENTRALIZE, "/img/img_operation/centralize.png");
			put(ICON_TYPE.FIT, "/img/img_operation/fit.png");
			put(ICON_TYPE.PHOTO_FRAME, "/img/photo_Icon.png");

			// others
			put(ICON_TYPE.REMOVE, "/img/connection/remove_symbol.png");
			put(ICON_TYPE.CANCEL, "/img/context_menu/cancel.png");
			put(ICON_TYPE.SELECT_ALL, "/img/misc/select_all.png");

			// Misc stuff
			put(ICON_TYPE.UP, "/img/misc/up.png");
			put(ICON_TYPE.DOWN, "/img/misc/down.png");

			put(ICON_TYPE.PLUS, "/img/misc/add.png");
			put(ICON_TYPE.ADD, "/img/misc/add.png");
			put(ICON_TYPE.MINUS, "/img/misc/minus.png");
			put(ICON_TYPE.MERGE_ARROW, "/img/misc/merge_arrow.png");

			put(ICON_TYPE.APPLY, "/img/misc/apply.png");
			put(ICON_TYPE.UNDO, "/img/misc/undo.png");
			put(ICON_TYPE.REFRESH, "/img/misc/refresh.png");

			put(ICON_TYPE.SETTING, "/img/setting-512.png");
			put(ICON_TYPE.TOOL_BOX, "/img/setting-512.png");
			put(ICON_TYPE.INFORMATION, "/img/misc/information.png");
			put(ICON_TYPE.SAVE, "/img/misc/save.png");

			put(ICON_TYPE.DRAG, "/img/misc/drag.png");
			put(ICON_TYPE.ZOOM, "/img/zoom_icon.png");
			put(ICON_TYPE.GRID, "/img/grid_button.png");

			put(ICON_TYPE.STAR, "/img/misc/star.png");
			put(ICON_TYPE.BLUE_CIRCLE, "/img/misc/blue_circle.png");
			put(ICON_TYPE.USER, "/img/misc/user.png");

		}
	});
	private static final HashMap<String, Image> enumToImage = new HashMap<String, Image>();

	public static Image getIconImage(ICON_TYPE ICON_TYPE, boolean preserveRatio, int requestedWidth,
									 int requestedHeight) {
		return getIconImage(ICON_TYPE, preserveRatio, requestedWidth, requestedHeight, true);
	}

	public static Image getIconImage(ICON_TYPE ICON_TYPE, boolean preserveRatio, int requestedWidth,
			int requestedHeight, boolean backgroundLoading) {
		String key = "" + ICON_TYPE + requestedWidth + "_" + requestedHeight + "_" + preserveRatio;
		if (enumToImage.containsKey(key)) {
			return enumToImage.get(key);
		}
		Image image = new Image(ResourcesHelper.getResourceAsString(ENUM_TO_NAME.get(ICON_TYPE)), requestedWidth,
				requestedHeight, preserveRatio, true, backgroundLoading);
		enumToImage.put(key, image);
		return image;
	}

	public static ImageView getIconImageView(ICON_TYPE ICON_TYPE, boolean preserveRatio, int requestedWidth,
			int requestedHeight) {
		return new ImageView(getIconImage(ICON_TYPE, preserveRatio, requestedWidth, requestedHeight));
	}

	/**
	 * Load image with default width height
	 *
	 * @param ICON_TYPE
	 * @param preserveRatio
	 * @return
	 * @see #getIconImage(ICON_TYPE, boolean, int, int)
	 */
	public static Image getIconImage(ICON_TYPE ICON_TYPE, boolean preserveRatio) {
		return getIconImage(ICON_TYPE, preserveRatio, defaultRequestedWH, defaultRequestedWH);
	}

	/**
	 * Load image with default width height to use for stage.
	 * This special call load image in foreground in able to display image from first stage show.
	 *
	 * @param ICON_TYPE
	 * @return
	 * @see #getIconImage(ICON_TYPE, boolean, int, int, boolean)
	 */
	public static Image getIconImageForStage(ICON_TYPE ICON_TYPE) {
		return getIconImage(ICON_TYPE, true, defaultRequestedWH, defaultRequestedWH, false);
	}

	/**
	 * Load Icon Image With Default requested Width/Height
	 *
	 * @param ICON_TYPE
	 * @param preserveRatio
	 * @return the loaded image in image view
	 *
	 * @see #getIconImageView(ICON_TYPE, boolean, int, int)
	 * @see #setDefaultRequestedWH(int)
	 */
	public static ImageView getIconImageView(ICON_TYPE ICON_TYPE, boolean preserveRatio) {
		return new ImageView(getIconImage(ICON_TYPE, preserveRatio, defaultRequestedWH, defaultRequestedWH));
	}

	/**
	 * Load Icon image while preserving ratio, using {@link #getDefaultRequestedWH()} for width and height and in background
	 *
	 * @see #getIconImage(ICON_TYPE, boolean, int, int, boolean)
	 */
	public static Image getIconImage(ICON_TYPE ICON_TYPE) {
		return getIconImage(ICON_TYPE, true, defaultRequestedWH, defaultRequestedWH);
	}

	/**
	 * Load Icon image while preserving ratio
	 *
	 * @see #getIconImageView(ICON_TYPE, boolean)
	 */
	public static ImageView getIconImageView(ICON_TYPE ICON_TYPE) {
		return new ImageView(getIconImage(ICON_TYPE, true, defaultRequestedWH, defaultRequestedWH));
	}

	/**
	 * @return the Default Requested Width Height
	 */
	public static int getDefaultRequestedWH() {
		return defaultRequestedWH;
	}

	/**
	 * @param requestedWH requested width/height to be used
	 */
	public static void setDefaultRequestedWH(int requestedWH) {
		IconLoader.defaultRequestedWH = requestedWH;
	}

	/**
	 * Load image from specified path which can be an exe file, so it extract system image.<br>
	 * Or image can be a normal image.<br>
	 * @param path path to exe/image file
	 * @return image in specified path if supported type. null otherwise
	 */
	public static Image getIconImage(PathLayer path) {
		Image img;
		// allow using EXE file as icon like another executable, useful when running
		// subprogram and to easy use parent program icon
		if (path.getExtensionUPPERCASE().equals("EXE")) {
			img = SystemIconsHelper.getFileIcon(path);
		} else {
			// use normal image for icon
			if(ImageGridItem.ArrayIMGExt.contains(path.getExtensionUPPERCASE())) {
				img = new Image(path.toURI().toString(), IconLoader.getDefaultRequestedWH(),
						IconLoader.getDefaultRequestedWH(), true, true, true);
			} else {
				// extension not supported
				img = null;
			}
		}
		return img;
	}
}
