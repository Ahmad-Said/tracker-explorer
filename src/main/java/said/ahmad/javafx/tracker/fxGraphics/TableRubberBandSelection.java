package said.ahmad.javafx.tracker.fxGraphics;

import java.util.*;
import java.util.stream.Collectors;

import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import said.ahmad.javafx.tracker.model.TableViewModel;

/**
 * area selection in a table located in a grid pane
 */
public class TableRubberBandSelection<S> extends RubberBandSelection {

	/**
	 * Target node
	 */
	private final TableView<S> tableView;

	/**
	 * Parent node that contain image and resizable rectangle
	 */
	private final GridPane parentPane;
	/**
	 * Row from row to model
	 */
	private final Map<TableRow<S>, S> rowMap;

	private Map<Integer, Boolean> rowMapIsSelectedCopy;

	private Set<Integer> previouslySelectedIndex;
	private boolean isEventsHandlerActive;

	/**
	 * Backup of table events
	 */
	private EventHandler<? super MouseEvent> backUpOnMousePressedEvent;
	private EventHandler<? super MouseEvent> backUpOnMouseDraggedEvent;
	private EventHandler<? super MouseEvent> backUpOnMouseReleasedEvent;

	public TableRubberBandSelection(TableView<S> tableView, GridPane parentPane, Map<TableRow<S>, S> rowMap) {
		this.tableView = tableView;
		this.parentPane = parentPane;
		this.rowMap = rowMap;
		isEventsHandlerActive = false;

		// event filter are accumulative.
		// event filter happen before selection model select a row
		// event handler happen after selection model select a row
		tableView.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
			previouslySelectedIndex = new HashSet<>(tableView.getSelectionModel().getSelectedIndices());
		});
	}

	/**
	 * Replace event handler of table to work with rubber selection drag instead of
	 * normal drag behavior.
	 *
	 * @param rowUnderEvent
	 *            the row selected on event
	 * @return <code>true</code> if rubber selection mode is activated <br>
	 *         <code>false</code> otherwise when trying to really drag already
	 *         selected items
	 */
	public boolean activateEventHandlerTable(MouseEvent event, TableRow<TableViewModel> rowUnderEvent) {
		// compare if selected index already exist in recent row map, if yes then action
		// is meant to be a drag and drop operation
		// the first selected item is done by first mouse press
		if (rowUnderEvent != null) {
			Integer rowIndex = rowUnderEvent.getIndex();

			if(previouslySelectedIndex.contains(rowIndex))
				return false;
		}

		// activate rubber selection mode if not activated
		if (!isEventsHandlerActive) {
			// backup events handler table
			this.backUpOnMousePressedEvent = tableView.getOnMousePressed();
			this.backUpOnMouseDraggedEvent = tableView.getOnMouseDragged();
			this.backUpOnMouseReleasedEvent = tableView.getOnMouseReleased();
			// Set new events
			setOnMousePressedEventHandler(getDefaultOnMousePressedEventHandler(), tableView);
			setOnMouseDraggedEventHandler(getDefaultOnMouseDraggedEventHandler(), tableView);
			setOnMouseReleasedEventHandler(getDefaultOnMouseReleasedEventHandler(), tableView);
			isEventsHandlerActive = true;

			// back up the initial status of rows, so we can go back to it if selection
			// no longer intersect with it
			// with this behavior we can expand and shrink the selection and get expected
			// behavior of inverting selected status
			rowMapIsSelectedCopy = getSelectedRowIndexes();
			// ignore selection effect done automatically by table with first press
			Integer selectedIndex = tableView.getSelectionModel().getSelectedIndex();
			rowMapIsSelectedCopy.put(selectedIndex, !tableView.getSelectionModel().isSelected(selectedIndex));

			getOnMousePressedEventHandler().handle(event);
		}

		return isEventsHandlerActive;
	}

	/**
	 * create a map from row index to its selected status
	 *
	 * @return map : row index -> isSelected
	 */
	private Map<Integer, Boolean> getSelectedRowIndexes() {
		return rowMap.keySet().stream().map(IndexedCell::getIndex).distinct()
				.collect(Collectors.toMap(index -> index, index -> tableView.getSelectionModel().isSelected(index)));
	}

	/**
	 * Deactivate rubber selection mode by restoring previous table handlers
	 */
	public void deactivateEventHandlerTable() {
		if (isEventsHandlerActive) {
			setOnMousePressedEventHandler(this.backUpOnMousePressedEvent, tableView);
			setOnMouseDraggedEventHandler(this.backUpOnMouseDraggedEvent, tableView);
			setOnMouseReleasedEventHandler(this.backUpOnMouseReleasedEvent, tableView);
			isEventsHandlerActive = false;
		}
	}

	@Override
	public EventHandler<MouseEvent> getDefaultOnMouseDraggedEventHandler() {
		EventHandler<MouseEvent> onMouseDragged = super.getDefaultOnMouseDraggedEventHandler();
		return event -> {
			onMouseDragged.handle(event);
			if (!event.isControlDown() && !event.isShiftDown())
				tableView.getSelectionModel().clearSelection();
			if (rowMap.size() == 0)
				return;
			Rectangle bounds = new Rectangle(getRectanglePositionX(), getRectanglePositionY(),
					getRectangle().getWidth(), getRectangle().getHeight());
			double headerHeight = TableRowUtil.getParentMinY(rowMap.keySet());
			ArrayList<Integer> indexToSelect = new ArrayList<>();
			for (TableRow<S> row : rowMap.keySet().stream().sorted(Comparator.comparingInt(IndexedCell::getIndex))
					.collect(Collectors.toList())) {
				Rectangle rowRect = new Rectangle(row.getLayoutX(), row.getLayoutY() + headerHeight + 2, row.getWidth(),
						row.getHeight() - 2);
				boolean wasSelected = rowMapIsSelectedCopy.get(row.getIndex());
				if (bounds.intersects(rowRect.getBoundsInLocal())) {
					if (wasSelected)
						tableView.getSelectionModel().clearSelection(row.getIndex());
					else
						indexToSelect.add(row.getIndex());

				} else {
					if (wasSelected)
						indexToSelect.add(row.getIndex());
					else
						tableView.getSelectionModel().clearSelection(row.getIndex());

				}
			}
			int[] toSelect = indexToSelect.stream().mapToInt(i -> i).toArray();
			tableView.getSelectionModel().selectIndices(-1, toSelect);
		};
	}

	@Override
	public EventHandler<MouseEvent> getDefaultOnMouseReleasedEventHandler() {
		return mouseEvent -> {
			if (mouseEvent.isSecondaryButtonDown() || getRectangle().isResizing()) {
				return;
			}
			removeRectangleFromParent(getRectangle());
			deactivateEventHandlerTable();
		};
	}

	@Override
	public GridPane getParentNode() {
		return parentPane;
	}

	@Override
	public TableView<S> getTargetNode() {
		return tableView;
	}

	@Override
	public double getRectanglePositionX() {
		if (GridPane.getMargin(getRectangle()) == null) {
			return 0;
		} else {
			return GridPane.getMargin(getRectangle()).getLeft();
		}
	}

	@Override
	public void setRectanglePositionX(double x) {
		Insets insets = GridPane.getMargin(getRectangle());
		if (insets == null) {
			GridPane.setMargin(getRectangle(), new Insets(0, 0, 0, x));
		} else {
			GridPane.setMargin(getRectangle(), new Insets(insets.getTop(), insets.getRight(), insets.getBottom(), x));
		}
	}

	@Override
	public double getRectanglePositionY() {
		if (GridPane.getMargin(getRectangle()) == null) {
			return 0;
		} else {
			return GridPane.getMargin(getRectangle()).getTop();
		}
	}

	@Override
	public void setRectanglePositionY(double y) {
		Insets insets = GridPane.getMargin(getRectangle());
		if (insets == null) {
			GridPane.setMargin(getRectangle(), new Insets(y, 0, 0, 0));
		} else {
			GridPane.setMargin(getRectangle(), new Insets(y, insets.getRight(), insets.getBottom(), insets.getLeft()));
		}
	}

	@Override
	public double getEventPositionX(MouseEvent mouseEvent) {
		return mouseEvent.getX();
	}

	@Override
	public double getEventPositionY(MouseEvent mouseEvent) {
		return mouseEvent.getY();
	}

	@Override
	public void addRectangleToParent(ResizableRectangle rectangle) {
		if (!parentPane.getChildren().contains(rectangle)) {
			int rowIndex = GridPane.getRowIndex(tableView) == null ? 0 : GridPane.getRowIndex(tableView);
			int colIndex = GridPane.getColumnIndex(tableView) == null ? 0 : GridPane.getColumnIndex(tableView);
			GridPane.setValignment(rectangle, VPos.TOP);
			GridPane.setHalignment(rectangle, HPos.LEFT);
			parentPane.add(rectangle, colIndex, rowIndex);
		}
	}

	@Override
	public void removeRectangleFromParent(ResizableRectangle rectangle) {
		parentPane.getChildren().remove(rectangle);
	}
}
