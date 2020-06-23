package said.ahmad.javafx.tracker.system.tracker;

import java.util.ArrayList;

import said.ahmad.javafx.tracker.system.file.PathLayer;

/**
 * When opening a folder that is already tracked, change may occur outside
 * control of this program<br>
 * So what to do is track those change again.
 *
 * @see FileTracker#resolveConflict(java.util.Set)
 */
public class FileTrackerConflictLog {
	public ArrayList<PathLayer> addedItems;
	public ArrayList<PathLayer> removedItems;
	public StringBuilder summary;

	/**
	 * Initialize Objects with empty data to be used
	 */
	public FileTrackerConflictLog() {
		addedItems = new ArrayList<>();
		removedItems = new ArrayList<>();
		summary = new StringBuilder();
	}

	public int getChangedCounts() {
		return addedItems.size() + removedItems.size();
	}

	public boolean didChangeOccurs() {
		return getChangedCounts() != 0;
	}

	/**
	 * Generate a quick Summary in {@link #summary} in the following format: <br>
	 * - New added_file_name <br>
	 * - Del removed_file_name
	 */
	public void generateSummary() {
		summary = new StringBuilder(getChangedCounts() * 10);
		for (PathLayer p : addedItems) {
			summary.append("  - New \t" + p.getName() + "\n");
		}
		for (PathLayer p : removedItems) {
			summary.append("  - Del \t" + p.getName() + "\n");
		}
	}

	@Override
	public String toString() {
		return "FileTrackerConflictLog [addedItems=" + addedItems + ", removedItems=" + removedItems + ", summary="
				+ summary + "]";
	}
}