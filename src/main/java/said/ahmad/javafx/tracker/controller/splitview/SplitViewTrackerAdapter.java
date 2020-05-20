package said.ahmad.javafx.tracker.controller.splitview;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import javafx.util.Pair;
import said.ahmad.javafx.tracker.model.TableViewModel;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.tracker.system.tracker.FileTracker;
import said.ahmad.javafx.tracker.system.tracker.FileTrackerMultipleReturn;

class SplitViewTrackerAdapter {
	private FileTracker fileTracker;
	private SplitViewController splitView;

	public SplitViewTrackerAdapter(FileTracker fileTracker, SplitViewController splitView) {
		this.fileTracker = fileTracker;
		this.splitView = splitView;
	}

	/**
	 * Track any untracked folder from selection <br>
	 * Check Details at : {@link FileTracker#trackNewMultipleAndAsk(Set, boolean)}
	 *
	 * @param selectedItems
	 * @param clicked
	 * @return <code>true<code> -> already or newly tracked <br>
	 *         <code>false<code> -> not tracked
	 */
	public FileTrackerMultipleReturn untrackedBehaviorAndAskTrack(List<TableViewModel> selectedItems,
			TableViewModel clicked) {
		// returned false
		Set<PathLayer> parentPaths = selectedItems.stream().map(selection -> selection.getFilePath().getParentPath())
				.collect(Collectors.toSet());
		parentPaths.add(clicked.getFilePath().getParentPath());
		FileTrackerMultipleReturn fileMultipleReturn = fileTracker.trackNewMultipleAndAsk(parentPaths, true);
		if (fileMultipleReturn.didTrackNewFolder) {
			splitView.refreshTableWithSameData();
		}
		return fileMultipleReturn;
	}

	/**
	 * Will toggle seen on selection, ask if any entry wasn't tracked
	 *
	 * @param selectedItems
	 * @param clicked
	 */
	public void toggleSeen(List<TableViewModel> selectedItems, TableViewModel clicked) {
		Pair<List<PathLayer>, List<TableViewModel>> toChange = getToChangeListAndAskIfUntracked(selectedItems, clicked);
		if (toChange == null) {
			clicked.getMarkSeen().setSelected(false);
			return;
		}
		toChange.getValue().forEach(t -> t.setSeen(!fileTracker.isSeen(t.getFilePath())));
		fileTracker.toggleSeen(toChange.getKey());
		fileTracker.commitTrackerDataChange(toChange.getKey());
		splitView.reloadSearchField();
	}

	public void setNoteTextAfterAsk(List<TableViewModel> selectedItems, TableViewModel clicked) {
		Pair<List<PathLayer>, List<TableViewModel>> toChange = getToChangeListAndAskIfUntracked(selectedItems, clicked);
		if (toChange == null) {
			return;
		}
		String note = FileTracker.askForNoteText(clicked.getNoteText());
		if (note == null) {
			return;
		}
		toChange.getValue().forEach(t -> t.setNoteText(note));
		fileTracker.setNoteText(toChange.getKey(), note);
		fileTracker.commitTrackerDataChange(toChange.getKey());
		splitView.refreshTableWithSameData();
	}

	/**
	 *
	 * @param selectedItems
	 * @param clicked
	 * @return list of path to change as key<br>
	 *         list of tableViewModel to change as value
	 */
	@Nullable
	private Pair<List<PathLayer>, List<TableViewModel>> getToChangeListAndAskIfUntracked(
			List<TableViewModel> selectedItems, TableViewModel clicked) {
		FileTrackerMultipleReturn fileMultipleReturn = untrackedBehaviorAndAskTrack(selectedItems, clicked);
		if (fileMultipleReturn.trackedList.size() == 0) {
			return null;
		}

		List<PathLayer> pathsToUpdate = new ArrayList<>();
		List<TableViewModel> viewsToUpdate = new ArrayList<>();
		Stream<TableViewModel> clickedStream;
		Stream<TableViewModel> selectedStream;
		if (selectedItems.size() == 1) {
			clickedStream = Stream.of(clicked);
			selectedStream = Stream.of();
		} else {
			if (!selectedItems.contains(clicked)) {
				clickedStream = Stream.of(clicked);
			} else {
				clickedStream = Stream.of();
			}
			selectedStream = selectedItems.stream();
		}
		Stream.concat(clickedStream, selectedStream)
				.filter(t -> fileMultipleReturn.trackedList.contains(t.getFilePath().getParentPath())).forEach(t -> {
					viewsToUpdate.add(t);
					pathsToUpdate.add(t.getFilePath());
				});
		return new Pair<List<PathLayer>, List<TableViewModel>>(pathsToUpdate, viewsToUpdate);
	}
}
