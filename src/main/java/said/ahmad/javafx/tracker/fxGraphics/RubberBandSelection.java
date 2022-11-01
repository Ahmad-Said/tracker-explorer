package said.ahmad.javafx.tracker.fxGraphics;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
abstract class RubberBandSelection {
	/**
	 * Class that hold first mouse pressed location. It act as anchor point where
	 * from it rectangle will resize.
	 */
	public static final class DragContext {

		public double mouseAnchorX;
		public double mouseAnchorY;

	}

	private DragContext dragContext;

	/**
	 * rectangle used to UI show the selected area by user
	 */
	private ResizableRectangle rectangle;

	/**
	 * Event to add {@link #rectangle} to {@link #getParentNode()}
	 */
	private EventHandler<? super MouseEvent> onMousePressedEventHandler;

	/**
	 * Event to expand rectangle following mouse movement
	 */
	private EventHandler<? super MouseEvent> onMouseDraggedEventHandler;

	/**
	 * Event to remove {@link #rectangle} from {@link #getParentNode()}
	 */
	private EventHandler<? super MouseEvent> onMouseReleasedEventHandler;

	/**
	 * General usage to make a selection in node.
	 * <ul>
	 * <li>Create a group</li>
	 * <li>Adding targetNode to the group is up to user before calling this
	 * constructor</li>
	 * <li>Replace targetNode in your application with the created group</li>
	 * <li>Replace targetNode in your application with the created group</li>
	 * </ul>
	 */
	public RubberBandSelection() {
		this.dragContext = new DragContext();
		setRectangle(getDefaultRectangle());
	}

	public void setOnMousePressedEventHandler(EventHandler<? super MouseEvent> onMousePressedEventHandler, Node node) {
		replaceEventHandler(MouseEvent.MOUSE_PRESSED, this.onMousePressedEventHandler, onMousePressedEventHandler,
				node);
		this.onMousePressedEventHandler = onMousePressedEventHandler;
	}

	public void setOnMouseDraggedEventHandler(EventHandler<? super MouseEvent> onMouseDraggedEventHandler, Node node) {
		replaceEventHandler(MouseEvent.MOUSE_DRAGGED, this.onMouseDraggedEventHandler, onMouseDraggedEventHandler,
				node);
		this.onMouseDraggedEventHandler = onMouseDraggedEventHandler;

	}

	public void setOnMouseReleasedEventHandler(EventHandler<? super MouseEvent> onMouseReleasedEventHandler,
			Node node) {
		replaceEventHandler(MouseEvent.MOUSE_RELEASED, this.onMouseReleasedEventHandler, onMouseReleasedEventHandler,
				node);
		this.onMouseReleasedEventHandler = onMouseReleasedEventHandler;
	}

	private void replaceEventHandler(EventType<MouseEvent> eventType, EventHandler<? super MouseEvent> oldHandler,
			EventHandler<? super MouseEvent> newHandler, Node node) {
		if (oldHandler != null) {
			node.removeEventHandler(eventType, oldHandler);
		}
		if (newHandler != null) {
			node.addEventHandler(eventType, newHandler);
		}
	}

	public ResizableRectangle getDefaultRectangle() {
		ResizableRectangle rectangle = new ResizableRectangle(0, 0, 0, 0);
		rectangle.setStroke(Color.BLUE);
		rectangle.setStrokeWidth(1);
		rectangle.setStrokeLineCap(StrokeLineCap.ROUND);
		rectangle.setFill(Color.LIGHTBLUE.deriveColor(0, 1.2, 1, 0.6));
		return rectangle;
	}


	/**
	 * Event to add rectangle in parent node. And to initialize position of
	 * rectangle top left corner acting as anchor point
	 * 
	 * @return default on mouse press handler
	 */
	public EventHandler<MouseEvent> getDefaultOnMousePressedEventHandler() {
		return mouseEvent -> {
			if (mouseEvent.isSecondaryButtonDown() || rectangle.isResizing()) {
				return;
			}
			// prepare new drag operation
			getDragContext().mouseAnchorX = getEventPositionX(mouseEvent);
			getDragContext().mouseAnchorY = getEventPositionY(mouseEvent);

			addRectangleToParent(rectangle);
			setRectanglePositionX(getDragContext().mouseAnchorX);
			setRectanglePositionY(getDragContext().mouseAnchorY);
			rectangle.setWidth(0);
			rectangle.setHeight(0);
		};
	}

	public EventHandler<MouseEvent> getDefaultOnMouseDraggedEventHandler() {
		return mouseEvent -> {
			if (mouseEvent.isSecondaryButtonDown() || rectangle.isResizing()) {
				return;
			}

			double offsetX = getEventPositionX(mouseEvent) - getDragContext().mouseAnchorX;
			double offsetY = getEventPositionY(mouseEvent) - getDragContext().mouseAnchorY;

			if (offsetX > 0) {
				rectangle.setWidth(offsetX);
			} else {
				setRectanglePositionX(getEventPositionX(mouseEvent));
				rectangle.setWidth(getDragContext().mouseAnchorX - getRectanglePositionX());
			}

			if (offsetY > 0) {
				rectangle.setHeight(offsetY);
			} else {
				setRectanglePositionY(getEventPositionY(mouseEvent));
				rectangle.setHeight(getDragContext().mouseAnchorY - getRectanglePositionY());
			}
		};
	}


	public EventHandler<MouseEvent> getDefaultOnMouseReleasedEventHandler() {
		return mouseEvent -> {
			if (mouseEvent.isSecondaryButtonDown() || rectangle.isResizing()) {
				return;
			}
			limitRectToTargetNode();
		};
	}

	public void limitRectToTargetNode() {

		if (getTargetNode() != null) {
			Node targetNode = getTargetNode();
			double width = getRectangle().getWidth();
			double height = getRectangle().getHeight();
			double minX = getRectanglePositionX();
			double minY = getRectanglePositionY();
			double maxX = minX + width;
			double maxY = minY + height;
			Double finalWidth = width;
			Double finalHeight = height;

			// limiting to target pane
			if (minX < targetNode.getBoundsInLocal().getMinX()) {
				setRectanglePositionX(targetNode.getBoundsInLocal().getMinX());
				finalWidth -= getRectangle().getX() - minX;
			}
			if (minY < targetNode.getBoundsInLocal().getMinY()) {
				setRectanglePositionY(targetNode.getBoundsInLocal().getMinY());
				finalHeight -= getRectangle().getY() - minY;
			}

			if (maxX > targetNode.getBoundsInLocal().getMaxX()) {
				finalWidth -= maxX - targetNode.getBoundsInLocal().getMaxX();
			}
			if (maxY > targetNode.getBoundsInLocal().getMaxY()) {
				finalHeight -= maxY - targetNode.getBoundsInLocal().getMaxY();
			}

			getRectangle().setWidth(finalWidth);
			getRectangle().setHeight(finalHeight);
		}
	}

	public abstract Node getParentNode();

	public abstract Node getTargetNode();
	public abstract double getRectanglePositionX();
	public abstract void setRectanglePositionX(double x);
	public abstract double getRectanglePositionY();
	public abstract void setRectanglePositionY(double y);
	public abstract double getEventPositionX(MouseEvent mouseEvent);
	public abstract double getEventPositionY(MouseEvent mouseEvent);

	public abstract void addRectangleToParent(ResizableRectangle rectangle);
	public abstract void removeRectangleFromParent(ResizableRectangle rectangle);


}
