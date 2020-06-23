package said.ahmad.javafx.util;

import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TableView;

public class ControlListHelper {

	/**
	 * Move up selection.
	 *
	 * @param <T>
	 * @param selectionModel can be {@link TableView#getSelectionModel()} or
	 *                       {@link ListView#getSelectionModel()}
	 * @param listData       The data in the view
	 * @return preferred scrollTo index after moving
	 */
	public static <T> int moveUpSelection(MultipleSelectionModel<T> selectionModel, ObservableList<T> listData) {
		int selectSize = selectionModel.getSelectedIndices().size();
		int whichOne = 0;
		int[] toSelect = new int[selectSize];
		int[] selected = selectionModel.getSelectedIndices().stream().mapToInt(m -> m).toArray();
		for (Integer i : selected) {
			int j = i - 1;
			T data = listData.get(i);
			if (j >= whichOne) {
				listData.remove(data);
				listData.add(j, data);
				toSelect[whichOne] = j;
			} else {
				toSelect[whichOne] = i;
			}
			whichOne++;
		}
		selectionModel.clearSelection();
		selectionModel.selectIndices(-1, toSelect);
		return toSelect.length != 0 ? toSelect[0] : 0;
	}

	/**
	 * Move down selection
	 *
	 * @param <T>
	 * @param selectionModel can be {@link TableView#getSelectionModel()} or
	 *                       {@link ListView#getSelectionModel()}
	 * @param listData       The data in the view
	 * @return preferred scrollTo index after moving
	 */
	public static <T> int moveDownSelection(MultipleSelectionModel<T> selectionModel, ObservableList<T> listData) {
		int size = listData.size();
		int selectSize = selectionModel.getSelectedIndices().size();
		int whichOne = 0;
		int[] toSelect = new int[selectSize];
		int[] selected = selectionModel.getSelectedIndices().stream().mapToInt(m -> m).toArray();
		for (Integer index = selectSize - 1; index >= 0; index--) {
			int i = selected[index];
			int j = i + 1;
			T data = listData.get(i);
			if (j <= size - 1 - whichOne) {
				listData.remove(data);
				listData.add(j, data);
				toSelect[whichOne] = j;
			} else {
				toSelect[whichOne] = i;
			}
			whichOne++;
		}
		selectionModel.clearSelection();
		selectionModel.selectIndices(-1, toSelect);
		return toSelect.length != 0 ? toSelect[0] : 0;
	}

	/**
	 * Remove selection
	 *
	 * @param <T>
	 * @param selectionModel can be {@link TableView#getSelectionModel()} or
	 *                       {@link ListView#getSelectionModel()}
	 * @param listData       The data in the view
	 */
	public static <T> void removeSelection(MultipleSelectionModel<T> selectionModel, ObservableList<T> listData) {
		listData.removeAll(selectionModel.getSelectedItems());
	}

}
