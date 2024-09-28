package said.ahmad.javafx.tracker.datatype;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import said.ahmad.javafx.tracker.app.pref.Setting;
import said.ahmad.javafx.tracker.controller.splitview.SplitViewController;
import said.ahmad.javafx.tracker.system.file.PathLayer;

/**
 * When adding a new field into SplitViewState to do: <br>
 * 0- define the field in this class <br>
 * 1- add how the field get its value in
 * {@link SplitViewController#saveStateToSplitState} <br>
 * 2- add the action on applying it in
 * {@link SplitViewController#restoreSplitViewStateWithoutQueue} <br>
 * Then we have 2 cases:<br>
 * a. Omitted field i.e. just a variable used at runtime and don't need to be
 * saved <br>
 * a.1 add @JsonIgnore on top of the attribute to ignore attribute from
 * being saved <br>
 * a.2 define its default value in
 * {@link SplitViewState#initializeOmittedFields()} (called after any instance
 * creation) <br>
 *
 * b. Savable field: <br>
 * b.1 add affectation in all constructor except empty constructor <br>
 * b.2 make sure to check null status of objects charged by XStream in
 * {@link SplitViewState#SplitViewState(SplitViewState)}<br>
 * otherwise, for primitive types it takes for the first time the default value
 * defined in java. (int->0, boolean->false...)
 * b.2 was note on old way of saving setting with xstream, TODO see json handling way
 *
 */
@Data
public class SplitViewState {
	private PathLayer mDirectory;
	private DirectoryViewOptions directoryViewOptions;
	private boolean autoExpandRight;


	@JsonIgnore
	private LinkedList<PathLayer> backQueue;
	@JsonIgnore
	private LinkedList<PathLayer> nextQueue;

	@JsonIgnore
	private int[] selectedIndices;
	@JsonIgnore
	private int scrollTo;

	private String searchKeyword;

	/** Empty constructor */
	public SplitViewState() {
		initializeOmittedFields();
	}

	// initial state conditions
	public SplitViewState(PathLayer mDirectory) {
		this.mDirectory = mDirectory;
		autoExpandRight = Setting.isAutoExpand();
		searchKeyword = "";
		directoryViewOptions = new DirectoryViewOptions();
		initializeOmittedFields();
	}

	public SplitViewState(SplitViewState omittedSplitState) {
		mDirectory = omittedSplitState.mDirectory;
		autoExpandRight = omittedSplitState.autoExpandRight;
		searchKeyword = omittedSplitState.searchKeyword;
		if (omittedSplitState.directoryViewOptions == null) {
			directoryViewOptions = new DirectoryViewOptions();
		} else {
			directoryViewOptions = omittedSplitState.directoryViewOptions;
		}
		initializeOmittedFields();
	}

	private void initializeOmittedFields() {
		backQueue = new LinkedList<>();
		nextQueue = new LinkedList<>();
		selectedIndices = new int[]{};
		scrollTo = 0;
	}

	/**
	 * @param selections the selectedIndices to set
	 */
	public void setSelectedIndices(List<Integer> selections) {
		selectedIndices = new int[selections.size()];
		for (int i = 0; i < selections.size(); i++) {
			selectedIndices[i] = selections.get(i);
		}
	}

	@Override
	public String toString() {
		return "SplitViewState [mDirectory=" + mDirectory + "]";
	}

}
