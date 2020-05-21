package said.ahmad.javafx.tracker.datatype;

import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.tracker.system.file.PathLayerHelper;

/**
 * {@link FavoriteView#getTitle()} are considered as keys of favorites locations
 */
public class FavoriteViewList implements Iterable<FavoriteView> {

	/**
	 * tried to extend ObservableListWrapper but it is API restricted <br>
	 * A map from title to favorite View
	 */
	private final ObservableMap<String, FavoriteView> map = FXCollections
			.observableMap(new LinkedHashMap<String, FavoriteView>());

	/**
	 * Take 3 list of string data in order, and skip bad URI format
	 *
	 * @param favoritesTitles    List of String title
	 * @param favoritesLeftLocs  List of URI String to be parsed
	 * @param favoritesRightLocs List of URI String to be parsed
	 * @deprecated since 5.1.2
	 */
	@Deprecated
	public void initializeFavoriteViewList(List<String> favoritesTitles, List<String> favoritesLeftLocs,
			List<String> favoritesRightLocs) {
		if (favoritesTitles == null || favoritesLeftLocs == null || favoritesRightLocs == null) {
			return;
		}
		int size = Math.min(Math.min(favoritesTitles.size(), favoritesLeftLocs.size()), favoritesRightLocs.size());
		String title = "";
		PathLayer left = null;
		PathLayer right = null;

		for (int i = 0; i < size; i++) {
			title = "";
			left = null;
			right = null;
			title = favoritesTitles.get(i);
			if (title == null || title.isEmpty()) {
				continue;
			}
			try {
				left = PathLayerHelper.parseURI(favoritesLeftLocs.get(i));
				right = PathLayerHelper.parseURI(favoritesRightLocs.get(i));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			if (left == null || right == null) {
				continue;
			}
			map.put(title, new FavoriteView(title, left, right));
		}
	}

	/**
	 *
	 * @param title
	 * @return
	 */
	@Nullable
	public FavoriteView getByTitle(String title) {
		return map.get(title);
	}

	/**
	 *
	 * @param title
	 * @return
	 */
	@Nullable
	public FavoriteView getByFirstLoc(PathLayer firstLoc) {
		for (FavoriteView favorite : map.values()) {
			if (favorite.getLocations().size() != 0 && favorite.getLocations().get(0).equals(firstLoc)) {
				return favorite;
			}
		}
		return null;
	}

	public boolean containsByTitle(String title) {
		return getByTitle(title) != null;
	}

	public boolean containsByFirstLoc(PathLayer firstLoc) {
		return getByFirstLoc(firstLoc) != null;
	}

	/** @see #remove(Object) */
	public FavoriteView removeByFirstLoc(PathLayer firstLoc) {
		return map.remove(getByFirstLoc(firstLoc).getTitle());
	}

	/** @see #remove(Object) */
	public FavoriteView removeByTitle(String title) {
		return map.remove(title);
	}

	/**
	 * A map from title to favorite View
	 *
	 * @return the list
	 */
	public ObservableMap<String, FavoriteView> getList() {
		return map;
	}

	public void clear() {
		map.clear();
	}

	public void addAll(List<FavoriteView> otherList) {
		LinkedHashMap<String, FavoriteView> map = new LinkedHashMap<>();
		otherList.stream().forEach(f -> map.put(f.getTitle(), f));
		this.map.putAll(map);
	}

	public void addAtEnd(FavoriteView favo) {
		map.remove(favo.getTitle());
		map.put(favo.getTitle(), favo);
	}

	public void addAtFirst(FavoriteView favo) {
		LinkedHashMap<String, FavoriteView> newmap = new LinkedHashMap<>(map);
		map.clear();
		map.put(favo.getTitle(), favo);
		map.putAll(newmap);
	}

	public int size() {
		return map.size();
	}

	public void addListener(MapChangeListener<? super String, ? super FavoriteView> listChangeListener) {
		map.addListener(listChangeListener);
	}

	@Override
	public Iterator<FavoriteView> iterator() {
		return map.values().iterator();
	}

	public boolean contains(FavoriteView favoriteView) {
		return map.containsKey(favoriteView.getTitle());
	}
}
