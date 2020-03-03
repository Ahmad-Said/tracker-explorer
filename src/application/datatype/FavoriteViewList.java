package application.datatype;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javafx.util.Pair;

public class FavoriteViewList implements Cloneable {
	// left location and titles are the key of favorites locations
	private ArrayList<Path> leftLocs, rightLocs;
	private ArrayList<String> titles;
	private Pair<String, Path> lastRemovedKey;

	public FavoriteViewList() {
		setLeftLoc(new ArrayList<>());
		setRightLoc(new ArrayList<Path>());
		setTitle(new ArrayList<String>());
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

	public void addNewFovorite(String Title, Path left, Path right) {
		titles.add(Title);
		leftLocs.add(left);
		rightLocs.add(right);
	}

	public void add(int i, String title, Path leftPath, Path rightPath) {
		titles.add(i, title);
		leftLocs.add(i, leftPath);
		rightLocs.add(i, rightPath);
	}

	public int size() {
		return titles.size();
	}

	public boolean contains(String title) {
		return titles.contains(title);
	}

	public boolean contains(Path leftPath) {
		return leftLocs.contains(leftPath);
	}

	public void remove(Path leftPath) {
		removeIndexI(leftLocs.indexOf(leftPath));
	}

	public void remove(String title) {
		removeIndexI(titles.indexOf(title));
	}

	private void removeIndexI(int i) {
		if (i < 0) {
			return;
		}
		lastRemovedKey = new Pair<String, Path>(titles.get(i), leftLocs.get(i));
		titles.remove(i);
		leftLocs.remove(i);
		rightLocs.remove(i);
	}

	public Pair<String, Path> getLastRemoved() {
		return lastRemovedKey;
	}

	public String getTitleByLeft(Path favoLeftPath) {
		return titles.get(leftLocs.indexOf(favoLeftPath));
	}

	public Path getLeftLocByTitle(String title) {
		return leftLocs.get(titles.indexOf(title));
	}

	public Path getRightLocByTitle(String title) {
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
	public ArrayList<Path> getLeftLoc() {
		return leftLocs;
	}

	/**
	 * @param leftLoc the leftLoc to set
	 */
	public void setLeftLoc(ArrayList<Path> leftLoc) {
		leftLocs = leftLoc;
	}

	/**
	 * @return the rightLoc
	 */
	public ArrayList<Path> getRightLoc() {
		return rightLocs;
	}

	/**
	 * @param rightLoc the rightLoc to set
	 */
	public void setRightLoc(ArrayList<Path> rightLoc) {
		rightLocs = rightLoc;
	}

	public void updateTitlesAndIndexs(HashMap<String, Pair<String, Integer>> oldToNewTitleAndIndex) {
		ArrayList<Path> nleftLocs = new ArrayList<>(Arrays.asList(new Path[oldToNewTitleAndIndex.size()])),
				nrightLocs = new ArrayList<>(Arrays.asList(new Path[oldToNewTitleAndIndex.size()]));
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
