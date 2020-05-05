package application.datatype;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import application.StringHelper;
import javafx.util.Pair;

@XmlRootElement(name = "FavoritesList")
public class FavoriteViewList {
	public String Version = "5.0";
	public Boolean BackSync = false;
	public Boolean AutoExpand = true;
	public Boolean LoadAllIcon = true;
	public File LeftLastKnowLocation = null;
	public File RightLastKnowLocation = null;
	public Boolean ShowLeftNotesColumn = false;
	public Boolean ShowRightNotesColumn = false;
	public String ActiveUser = "default";
	public String VLCHttpPass = "1234";
	public int MaxLimitFilesRecursive = 10000;
	public int MaxDepthFilesRecursive = 5;
	public boolean isDebugMode = false;
	public boolean autoRenameUTFFile = false;
	public boolean useTeraCopyByDefault = false;
	public boolean autoCloseClearDoneFileOperation = true;

	// left location and titles are the key of favorites locations
	private ArrayList<File> leftLocs, rightLocs;
	private ArrayList<String> titles;
	private Pair<String, File> lastRemovedKey;

	public FavoriteViewList() {
		setLeftLoc(new ArrayList<>());
		setRightLoc(new ArrayList<File>());
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
		File left = null, right = null;

		for (int i = 0; i < size; i++) {
			title = favoritesTitles.get(i);
			if (title == null || title.isEmpty()) {
				continue;
			}
			left = StringHelper.parseUriToPath(favoritesLeftLocs.get(i)).toFile();
			right = StringHelper.parseUriToPath(favoritesRightLocs.get(i)).toFile();
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

	public void addNewFovorite(String Title, File left, File right) {
		titles.add(Title);
		leftLocs.add(left);
		rightLocs.add(right);
	}

	public void add(int i, String title, File leftFile, File rightFile) {
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

	public boolean contains(File leftFile) {
		return leftLocs.contains(leftFile);
	}

	public void remove(File leftFile) {
		removeIndexI(leftLocs.indexOf(leftFile));
	}

	public void remove(String title) {
		removeIndexI(titles.indexOf(title));
	}

	private void removeIndexI(int i) {
		if (i < 0) {
			return;
		}
		lastRemovedKey = new Pair<String, File>(titles.get(i), leftLocs.get(i));
		titles.remove(i);
		leftLocs.remove(i);
		rightLocs.remove(i);
	}

	public Pair<String, File> getLastRemoved() {
		return lastRemovedKey;
	}

	public String getTitleByLeft(File favoLeftFile) {
		return titles.get(leftLocs.indexOf(favoLeftFile));
	}

	public File getLeftLocByTitle(String title) {
		return leftLocs.get(titles.indexOf(title));
	}

	public File getRightLocByTitle(String title) {
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
	public ArrayList<File> getLeftLoc() {
		return leftLocs;
	}

	/**
	 * @param leftLoc the leftLoc to set
	 */
	public void setLeftLoc(ArrayList<File> leftLoc) {
		leftLocs = leftLoc;
	}

	/**
	 * @return the rightLoc
	 */
	public ArrayList<File> getRightLoc() {
		return rightLocs;
	}

	/**
	 * @param rightLoc the rightLoc to set
	 */
	public void setRightLoc(ArrayList<File> rightLoc) {
		rightLocs = rightLoc;
	}

	public void updateTitlesAndIndexs(HashMap<String, Pair<String, Integer>> oldToNewTitleAndIndex) {
		ArrayList<File> nleftLocs = new ArrayList<>(Arrays.asList(new File[oldToNewTitleAndIndex.size()])),
				nrightLocs = new ArrayList<>(Arrays.asList(new File[oldToNewTitleAndIndex.size()]));
		ArrayList<String> ntitles = new ArrayList<>(Arrays.asList(new String[oldToNewTitleAndIndex.size()]));

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
