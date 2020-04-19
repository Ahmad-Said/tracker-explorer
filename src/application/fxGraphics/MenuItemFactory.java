package application.fxGraphics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

public class MenuItemFactory {
	private static Map<Menu, Set<MenuItem>> allMenus = new HashMap<>();

	public static void registerMenu(Menu newMenu) {
		allMenus.put(newMenu, new HashSet<>());
	}

	public static MenuItem getMenuItem(Menu parentMenu) {
		for (MenuItem menuItem : allMenus.get(parentMenu)) {
			if (menuItem.getParentMenu() == null) {
				return menuItem;
			}
		}
		MenuItem mn = new MenuItem();
		allMenus.get(parentMenu).add(mn);
		return mn;
	}

	public static MenuItem getMenuItem(Menu parentMenu, String settext) {
		MenuItem mn = getMenuItem(parentMenu);
		mn.setText(settext);
		return mn;
	}

	public static MenuItem getMenuItem(Menu parentMenu, String settext, boolean addToLast) {
		MenuItem mn = getMenuItem(parentMenu);
		mn.setText(settext);
		parentMenu.getItems().add(mn);
		return mn;
	}

}
