package said.ahmad.javafx.tracker.datatype;

import java.util.LinkedList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import said.ahmad.javafx.tracker.app.pref.Setting;
import said.ahmad.javafx.tracker.system.file.PathLayer;

public class SplitViewState {
	private PathLayer mDirectory;
	private boolean autoExpandRight;

	@XStreamOmitField
	private LinkedList<PathLayer> backQueue;
	@XStreamOmitField
	private LinkedList<PathLayer> nextQueue;

	@XStreamOmitField
	private int selectedIndices[];
	@XStreamOmitField
	private int scrollTo;

	private String searchKeyword;

	/** Empty constructor */
	public SplitViewState() {
		initializeOmmittedFields();
	}

	// initial state conditions
	public SplitViewState(PathLayer mDirectory) {
		this.mDirectory = mDirectory;
		autoExpandRight = Setting.isAutoExpand();
		searchKeyword = "";
		initializeOmmittedFields();
	}

	public SplitViewState(SplitViewState ommittedSplitState) {
		mDirectory = ommittedSplitState.mDirectory;
		autoExpandRight = ommittedSplitState.autoExpandRight;
		searchKeyword = ommittedSplitState.searchKeyword;
		initializeOmmittedFields();
	}

	private void initializeOmmittedFields() {
		backQueue = new LinkedList<PathLayer>();
		nextQueue = new LinkedList<PathLayer>();
		int selected[] = {};
		selectedIndices = selected;
		scrollTo = 0;
	}

	public PathLayer getmDirectory() {
		return mDirectory;
	}

	public void setmDirectory(PathLayer mDirectory) {
		this.mDirectory = mDirectory;
	}

	public LinkedList<PathLayer> getBackQueue() {
		return backQueue;
	}

	public void setBackQueue(LinkedList<PathLayer> backQueue) {
		this.backQueue = backQueue;
	}

	public LinkedList<PathLayer> getNextQueue() {
		return nextQueue;
	}

	public void setNextQueue(LinkedList<PathLayer> nextQueue) {
		this.nextQueue = nextQueue;
	}

	/**
	 * @return the selectedIndices
	 */
	public int[] getSelectedIndices() {
		return selectedIndices;
	}

	/**
	 * @param selectedIndices the selectedIndices to set
	 */
	public void setSelectedIndices(List<Integer> selections) {
		selectedIndices = new int[selections.size()];
		for (int i = 0; i < selections.size(); i++) {
			selectedIndices[i] = selections.get(i);
		}
	}

	/**
	 * @return the scrollTo
	 */
	public int getScrollTo() {
		return scrollTo;
	}

	/**
	 * @param scrollTo the scrollTo to set
	 */
	public void setScrollTo(int scrollTo) {
		this.scrollTo = scrollTo;
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

	/**
	 * @return the autoExpandRight
	 */
	public boolean isAutoExpandRight() {
		return autoExpandRight;
	}

	/**
	 * @param autoExpandRight the autoExpandRight to set
	 */
	public SplitViewState setAutoExpandRight(boolean autoExpandRight) {
		this.autoExpandRight = autoExpandRight;
		return this;
	}

	@Override
	public String toString() {
		return "SplitViewState [mDirectory=" + mDirectory + "]";
	}

}
