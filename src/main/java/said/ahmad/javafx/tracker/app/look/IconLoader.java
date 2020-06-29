package said.ahmad.javafx.tracker.app.look;

import java.util.HashMap;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import said.ahmad.javafx.tracker.app.ResourcesHelper;

public class IconLoader {
	/**
	 * Default requested Witdh, Height
	 */
	private static int defaultRequestedWH = 30;

	public static enum ICON_TYPE {
		// Context Menu Stuff
		OPEN, COPY, MOVE, CLIPBOARD,

		RENAME, BULK_RENAME, BULK_RENAME_UTILITY,

		COPY_BASE_NAME, PASTE_BASE_NAME,

		NEW, FILE, FOLDER, DELETE, RESOURCE_NOT_FOUND,

		HIDDEN, SORT,

		SYSTEM,

		TRACKER, TRACKER_DATA, NOTE,

		CORTANA, VLC,

		// Connection icons
		LINK_SYMBOL,

		// Image operation
		CROP, ROTATE_RIGHT, CENTRALIZE, FIT,

		// Misc Stuff
		UP, DOWN, PLUS, ADD, MINUS,

		APPLY, UNDO, REFRESH,

		REMOVE, CANCEL, SELECT_ALL,

		SETIING, TOOL_BOX, INFORMATION, SAVE,

		DRAG, ZOOM, GRID,

		STAR, USER,
	}

	private static final HashMap<ICON_TYPE, String> enumToName = new HashMap<IconLoader.ICON_TYPE, String>() {
		/**
		 *
		 */
		private static final long serialVersionUID = 8445384121964501348L;
		{
			// Context Menu Stuff
			put(ICON_TYPE.OPEN, "/img/context_menu/open.png");
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
			put(ICON_TYPE.TRACKER, "/img/context_menu/tracker.png");
			put(ICON_TYPE.TRACKER_DATA, "/img/context_menu/tracker_data.png");
			put(ICON_TYPE.NOTE, "/img/context_menu/note.png");
			put(ICON_TYPE.CORTANA, "/img/context_menu/cortana.png");
			put(ICON_TYPE.VLC, "/img/context_menu/vlc.png");

			// Connections icons
			put(ICON_TYPE.LINK_SYMBOL, "/img/connection/link_symbol.png");

			// Image Stuff
			put(ICON_TYPE.CROP, "/img/img_operation/crop.png");
			put(ICON_TYPE.ROTATE_RIGHT, "/img/img_operation/rotate_right.png");
			put(ICON_TYPE.CENTRALIZE, "/img/img_operation/centralize.png");
			put(ICON_TYPE.FIT, "/img/img_operation/fit.png");

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

			put(ICON_TYPE.APPLY, "/img/misc/apply.png");
			put(ICON_TYPE.UNDO, "/img/misc/undo.png");
			put(ICON_TYPE.REFRESH, "/img/misc/refresh.png");

			put(ICON_TYPE.SETIING, "/img/setting-512.png");
			put(ICON_TYPE.TOOL_BOX, "/img/setting-512.png");
			put(ICON_TYPE.INFORMATION, "/img/misc/information.png");
			put(ICON_TYPE.SAVE, "/img/misc/save.png");

			put(ICON_TYPE.DRAG, "/img/misc/drag.png");
			put(ICON_TYPE.ZOOM, "/img/zoom_icon.png");
			put(ICON_TYPE.GRID, "/img/grid_button.png");

			put(ICON_TYPE.STAR, "/img/misc/star.png");
			put(ICON_TYPE.USER, "/img/misc/user.png");

		}
	};
	private static final HashMap<String, Image> enumToImage = new HashMap<String, Image>();

	public static Image getIconImage(ICON_TYPE ICON_TYPE, boolean preserveRatio, int requestedWidth,
			int requestedHeight) {
		String key = "" + ICON_TYPE + requestedWidth + "_" + requestedHeight + "_" + preserveRatio;
		if (enumToImage.containsKey(key)) {
			return enumToImage.get(key);
		}
		Image image = new Image(ResourcesHelper.getResourceAsString(enumToName.get(ICON_TYPE)), requestedWidth,
				requestedHeight, preserveRatio, true, true);
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
	 * Load Icon image while preserving ratio
	 *
	 * @see #getIconImage(ICON_TYPE, boolean)
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
	 * @param default requested width/height to be used
	 */
	public static void setDefaultRequestedWH(int requestedWH) {
		IconLoader.defaultRequestedWH = requestedWH;
	}

}
