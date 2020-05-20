package said.ahmad.javafx.tracker.datatype;

import java.util.LinkedList;
import java.util.List;

import said.ahmad.javafx.tracker.system.file.PathLayer;

public class SplitViewState {
	private PathLayer mDirectory;
	private LinkedList<PathLayer> BackQueue = new LinkedList<PathLayer>();
	private LinkedList<PathLayer> NextQueue = new LinkedList<PathLayer>();
	private int SelectedIndices[] = {};
	private int ScrollTo = 0;
	private String searchKeyword = "";

	public SplitViewState(PathLayer mDirectory) {
		this.mDirectory = mDirectory;
	}

	public PathLayer getmDirectory() {
		return mDirectory;
	}

	public void setmDirectory(PathLayer mDirectory) {
		this.mDirectory = mDirectory;
	}

	public LinkedList<PathLayer> getBackQueue() {
		return BackQueue;
	}

	public void setBackQueue(LinkedList<PathLayer> backQueue) {
		BackQueue = backQueue;
	}

	public LinkedList<PathLayer> getNextQueue() {
		return NextQueue;
	}

	public void setNextQueue(LinkedList<PathLayer> nextQueue) {
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
