package said.ahmad.javafx.tracker.fxGraphics;

import javafx.scene.Node;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import java.util.Set;

public class TableRowUtil {

	/**
	 * Get the height of header
	 * 
	 * @param row
	 * @return min y of parent containing all rows without header.
	 */
	public static <S> double getParentMinY(TableRow<S> row) {
		Node parent = getTableViewAsParent(row);
		if (parent == null) {
			return 0;
		}
		return parent.getBoundsInParent().getMinY();
	}

	private static Node getTableViewAsParent(Node node) {
		if (node == null || node.getParent() == null)
			return null;
		return node.getParent() instanceof TableView ? node : getTableViewAsParent(node.getParent());
	}

	/**
	 * Iterate over collection and find first row that have a parent giving other than 0 min y
	 * @param tableRowSet
	 * @return
	 * @param <S> table model
	 */
	public static <S> double getParentMinY(Set<TableRow<S>> tableRowSet) {
		double parentMinY = 0;
		for (TableRow<S> row : tableRowSet) {
			parentMinY = getParentMinY(row);
			if (parentMinY != 0) {
				return parentMinY;
			}
		}
		return parentMinY;
	}
}
