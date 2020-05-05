package application.model;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import application.DialogHelper;
import javafx.scene.control.Alert.AlertType;

public class SplitViewState {
	private File mDirectory;
	private LinkedList<File> BackQueue = new LinkedList<File>();
	private LinkedList<File> NextQueue = new LinkedList<File>();
	private int SelectedIndices[] = {};
	private int ScrollTo = 0;
	private String searchKeyword = "";

	public SplitViewState(File mDirectory) {
		this.mDirectory = mDirectory;
	}

	private File getExistingParent(File curFile) {
		File temp = curFile;
		while (!temp.exists()) {
			temp = temp.getParentFile();
			if (temp == null) {
				temp = File.listRoots()[0];
				DialogHelper.showAlert(AlertType.ERROR, "Open Directory", "Location could not be reached!",
						"For network location: Make sure server is running.");

			}
		}
		return temp;
	}

	public File getmDirectoryExisting() {
		// change Directory to last know location of tabs if not exist go to parent
		mDirectory = getExistingParent(mDirectory);
		return mDirectory;
	}

	public void setmDirectory(File mDirectory) {
		this.mDirectory = mDirectory;
	}

	public LinkedList<File> getBackQueue() {
		return BackQueue;
	}

	public void setBackQueue(LinkedList<File> backQueue) {
		BackQueue = backQueue;
	}

	public LinkedList<File> getNextQueue() {
		return NextQueue;
	}

	public void setNextQueue(LinkedList<File> nextQueue) {
		NextQueue = nextQueue;
	}

	/**
	 * @return the selectedIndices
	 */
	public int[] getSelectedIndices() {
		return SelectedIndices;
	}

	/**
	 * @param selectedIndices the selectedIndices to set
	 */
	public void setSelectedIndices(List<Integer> selections) {
		SelectedIndices = new int[selections.size()];
		for (int i = 0; i < selections.size(); i++) {
			SelectedIndices[i] = selections.get(i);
		}
	}

	/**
	 * @return the scrollTo
	 */
	public int getScrollTo() {
		return ScrollTo;
	}

	/**
	 * @param scrollTo the scrollTo to set
	 */
	public void setScrollTo(int scrollTo) {
		ScrollTo = scrollTo;
	}

	/**
	 * @return the searchKeyword
	 */
	public String getSearchKeyword() {
		return searchKeyword;
	}

	/**
	 * @param searchKeyword the searchKeyword to set
	 */
	public void setSearchKeyword(String searchKeyword) {
		this.searchKeyword = searchKeyword;
	}

}
