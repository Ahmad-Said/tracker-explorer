package said.ahmad.javafx.tracker.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Pair;
import lombok.Getter;
import said.ahmad.javafx.tracker.app.DialogHelper;
import said.ahmad.javafx.tracker.app.StringHelper;
import said.ahmad.javafx.tracker.app.look.IconLoader;
import said.ahmad.javafx.tracker.app.pref.Setting;
import said.ahmad.javafx.tracker.datatype.UserContextMenu;
import said.ahmad.javafx.tracker.system.call.GenericCaller;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.util.ArrayListHelper;
import said.ahmad.javafx.util.CallBackVoid;

public class UserContextMenuController {

	@Getter
	public static Map<String, Image> reservedParentNamesImage = new HashMap<String, Image>() {
		{
			put("Open With", IconLoader.getIconImage(IconLoader.ICON_TYPE.OPEN_WITH));
		}
	};
	/**
	 * Generate MenuItem graphics that correspond to list of files based on
	 * UserContextMenu template. <br>
	 * If the menu isn't compatible will return <code>null</code>.
	 *
	 * @param userContextMenu
	 * @param selections
	 * @return MenuItem generated if compatible, <code>null</code> otherwise.
	 */
	public static MenuItem generateUserContextMenu(UserContextMenu userContextMenu, List<PathLayer> selections) {
		MenuItem menuItem = null;
		if (selections.size() != 0 && userContextMenu.isCompatibleWithList(selections)) {
			// example of title would be "Add to archive %basenameOfTheFile%.rar"
			menuItem = new MenuItem(userContextMenu.getAliasEvaluated(selections));
			menuItem.setUserData(userContextMenu);
			menuItem.setOnAction(e -> {
				new Thread(() -> {
					try {
						GenericCaller.call(selections, userContextMenu, true);
					} catch (Exception ex) {
						ex.printStackTrace();
						DialogHelper.showException(ex);
					}
				}).start();
			});
			ImageView imgView = new ImageView(userContextMenu.getIconImage());
			imgView.setFitHeight(IconLoader.getDefaultRequestedWH());
			imgView.setFitWidth(IconLoader.getDefaultRequestedWH());
			menuItem.setGraphic(imgView);
		}
		return menuItem;
	}

	/**
	 * Generate User defined menus using {@link Setting#getUserContextMenus()}
	 * 
	 * @param selections
	 *            List of files to work with
	 * @param onParentAddition
	 *            action to do when a new parent menu is created, the first context
	 *            menu is returned also to make use of menu order.
	 * 
	 * @return Empty list if selections size = 0, all generated menu otherwise.
	 */
	public static List<MenuItem> generateUserContextMenus(List<PathLayer> selections,
			@Nullable CallBackVoid<Pair<UserContextMenu, MenuItem>> onParentAddition) {
		if (selections.size() == 0) {
			return new ArrayList<>();
		}
		ArrayList<MenuItem> allCustomMenu = new ArrayList<>();
		Map<String, Menu> createdParentMenu = new HashMap<>();
		for (UserContextMenu userMenu : Setting.getUserContextMenus()) {
			MenuItem menuItem = UserContextMenuController.generateUserContextMenu(userMenu, selections);
			if (menuItem != null) {
				menuItem = UserContextMenuController.appendAndCreateParentsMenus(menuItem,
						userMenu.getParentMenuNames(), userMenu.getParentIconImage(), createdParentMenu);

				if (!allCustomMenu.contains(menuItem)) {
					allCustomMenu.add(menuItem);
					if (onParentAddition != null) {
						onParentAddition.call(new Pair<>(userMenu, menuItem));
					}
				}
			}
		}
		return allCustomMenu;
	}


	/**
	 * @see #appendAndCreateParentsMenus(MenuItem, String, Image, Map)
	 */
	private static MenuItem appendAndCreateParentsMenus(MenuItem menuItem, String parentMenusNames,
			Image parentIconImage, Map<String, Menu> createdParentsMenus, String parentsFromRoot) {
		if (StringHelper.isEmpty(parentMenusNames)){
			return menuItem;
		}
		Menu parentMenu;
		int nextParent = parentMenusNames.indexOf("/");
		String currentParent = nextParent == -1 ? parentMenusNames : parentMenusNames.substring(0, nextParent);
		String restParent = nextParent == -1 ? "" : parentMenusNames.substring(nextParent + 1);
		String currentParentKey = parentsFromRoot + "/" + currentParent;
		// adding parent root to current parent
		if (createdParentsMenus.containsKey(currentParentKey)) {
			parentMenu = createdParentsMenus.get(currentParentKey);
		} else {
			parentMenu = new Menu(currentParent);
			ImageView imgView;
			// some parent menu name have in program defined image such as Open With
			imgView = new ImageView(reservedParentNamesImage.getOrDefault(currentParent, parentIconImage));
			imgView.setFitHeight(IconLoader.getDefaultRequestedWH());
			imgView.setFitWidth(IconLoader.getDefaultRequestedWH());
			parentMenu.setGraphic(imgView);
			createdParentsMenus.put(currentParentKey, parentMenu);
		}
		MenuItem child = appendAndCreateParentsMenus(menuItem, restParent, parentIconImage, createdParentsMenus, currentParentKey);
		if(!parentMenu.getItems().contains(child))
			parentMenu.getItems().add(child);
		return parentMenu;
	}

	/**
	 * Generate parent menus recursively and save them into a map.
	 *
	 * @param menuItem
	 * @param parentMenusNames
	 *            see {@link UserContextMenu#getParentMenuNames()}
	 * @param createdParentsMenus
	 *            useful when many menus use same parents, so they won't be created
	 *            twice
	 * @return The first created parent menu to be added that contain all other
	 *         submenus.
	 */
	public static MenuItem appendAndCreateParentsMenus(MenuItem menuItem, String parentMenusNames,
													   Image parentIconImage, Map<String, Menu> createdParentsMenus) {
		// helper function to save to add parameter used in recursive function
		return appendAndCreateParentsMenus(menuItem, parentMenusNames, parentIconImage, createdParentsMenus, "");
	}

}
