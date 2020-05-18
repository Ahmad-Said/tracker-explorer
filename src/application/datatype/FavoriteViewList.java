package application.datatype;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import application.system.file.PathLayer;
import application.system.file.PathLayerHelper;
import javafx.util.Pair;

@XmlRootElement
public class FavoriteViewList {
	// left location and titles are the key of favorites locations
	private ArrayList<PathLayer> leftLocs, rightLocs;
	private ArrayList<String> titles;
	private Pair<String, PathLayer> lastRemovedKey;

	public FavoriteViewList() {
		setLeftLoc(new ArrayList<>());
		setRightLoc(new ArrayList<>());
		setTitle(new ArrayList<String>());
	}

	/**
	 * Take 3 list of string data in order, and skip bad URI format
	 *
	 * @param favoritesTitles    List of String title
	 * @param favoritesLeftLocs  List of URI String to be parsed
	 * @param favoritesRightLocs List of URI String to be parsed
	 */
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
			addNewFovorite(title, left, right);
		}
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

	public void clear() {
		leftLocs.clear();
		rightLocs.clear();
		titles.clear();
	}

	public void addNewFovorite(String Title, PathLayer left, PathLayer right) {
		titles.add(Title);
		leftLocs.add(left);
		rightLocs.add(right);
	}

	public void add(int i, String title, PathLayer leftFile, PathLayer rightFile) {
		titles.add(i, title);
		leftLocs.add(i, leftFile);
		rightLocs.add(i, rightFile);
	}

	public int size() {
		return titles.size();
	}

	public boolean contains(String title) {
		return titles.contains(title);
	}

	public boolean contains(PathLayer leftFile) {
		return leftLocs.contains(leftFile);
	}

	public void remove(PathLayer leftFile) {
		removeIndexI(leftLocs.indexOf(leftFile));
	}

	public void remove(String title) {
		removeIndexI(titles.indexOf(title));
	}

	private void removeIndexI(int i) {
		if (i < 0) {
			return;
		}
		lastRemovedKey = new Pair<String, PathLayer>(titles.get(i), leftLocs.get(i));
		titles.remove(i);
		leftLocs.remove(i);
		rightLocs.remove(i);
	}

	public Pair<String, PathLayer> getLastRemoved() {
		return lastRemovedKey;
	}

	public String getTitleByLeft(PathLayer favoLeftFile) {
		return titles.get(leftLocs.indexOf(favoLeftFile));
	}

	public PathLayer getLeftLocByTitle(String title) {
		return leftLocs.get(titles.indexOf(title));
	}

	public PathLayer getRightLocByTitle(String title) {
		return leftLocs.get(titles.indexOf(title));
	}

	public Integer getIndexByTitle(String title) {
		return titles.indexOf(title);
	}

	/**
	 * @return the title
	 */
	public ArrayList<String> getTitle() {
		return titles;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(ArrayList<String> title) {
		titles = title;
	}

	/**
	 * @return the leftLoc
	 */
	public ArrayList<PathLayer> getLeftLoc() {
		return leftLocs;
	}

	/**
	 * @param leftLoc the leftLoc to set
	 */
	public void setLeftLoc(ArrayList<PathLayer> leftLoc) {
		leftLocs = leftLoc;
	}

	/**
	 * @return the rightLoc
	 */
	public ArrayList<PathLayer> getRightLoc() {
		return rightLocs;
	}

	/**
	 * @param rightLoc the rightLoc to set
	 */
	public void setRightLoc(ArrayList<PathLayer> rightLoc) {
		rightLocs = rightLoc;
	}

	public void updateTitlesAndIndexs(HashMap<String, Pair<String, Integer>> oldToNewTitleAndIndex) {
		ArrayList<PathLayer> nleftLocs = new ArrayList<>(leftLocs);
		ArrayList<PathLayer> nrightLocs = new ArrayList<>(rightLocs);
		ArrayList<String> ntitles = new ArrayList<>(titles);
		for (int i = 0; i < titles.size(); i++) {
			if (!oldToNewTitleAndIndex.containsKey(titles.get(i))) {
				continue;
			}
			String newTitle = oldToNewTitleAndIndex.get(titles.get(i)).getKey();
			int newIndex = oldToNewTitleAndIndex.get(titles.get(i)).getValue();
			ntitles.set(newIndex, newTitle);
			nleftLocs.set(newIndex, leftLocs.get(i));
			nrightLocs.set(newIndex, rightLocs.get(i));
		}
		titles = ntitles;
		leftLocs = nleftLocs;
		rightLocs = nrightLocs;
	}
}
