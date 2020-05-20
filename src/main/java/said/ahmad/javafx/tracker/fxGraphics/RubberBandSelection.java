package said.ahmad.javafx.tracker.fxGraphics;

import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

/**
 * Drag rectangle with mouse cursor in order to get selection bounds
 */
public class RubberBandSelection {

	final DragContext dragContext = new DragContext();
	ResizableRectangle rect;

	Group group;
	ImageView imageView = null;

	public Bounds getBounds() {
		return rect.getBoundsInParent();
	}

	/**
	 *
	 * ImageView is used to restrict rubberBandSelection to be limited by the
	 * boundary of the image
	 *
	 * @param group     the layer where imageView is in
	 * @param imageView can be null
	 */
	public RubberBandSelection(Group group, ImageView imageView) {

		this.group = group;
		this.imageView = imageView;

		rect = new ResizableRectangle(0, 0, 0, 0);
		rect.setStroke(Color.BLUE);
		rect.setStrokeWidth(1);
		rect.setStrokeLineCap(StrokeLineCap.ROUND);
		rect.setFill(Color.LIGHTBLUE.deriveColor(0, 1.2, 1, 0.6));
		selectFullImage();

		group.addEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
		group.addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);
		group.addEventHandler(MouseEvent.MOUSE_RELEASED, onMouseReleasedEventHandler);

	}

	public void selectFullImage() {
		if (imageView != null) {
			if (group.getChildren().contains(rect)) {
				rect.removeFromGroup(group);
			}
			rect.setX(imageView.getBoundsInLocal().getMinX());
			rect.setY(imageView.getBoundsInLocal().getMinY());
			rect.setWidth(imageView.getBoundsInLocal().getWidth());
			rect.setHeight(imageView.getBoundsInLocal().getHeight());
			rect.addToGroup(group);
		}
		limitRectToImageView();
	}

	EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {

		@Override
		public void handle(MouseEvent event) {

			if (event.isSecondaryButtonDown() || rect.isResizing()) {
				return;
			}

			// remove old rect
			rect.setX(0);
			rect.setY(0);
			rect.setWidth(0);
			rect.setHeight(0);

			rect.removeFromGroup(group);

			// prepare new drag operation
			dragContext.mouseAnchorX = event.getX();
			dragContext.mouseAnchorY = event.getY();

			rect.setX(dragContext.mouseAnchorX);
			rect.setY(dragContext.mouseAnchorY);
			rect.setWidth(0);
			rect.setHeight(0);

			rect.addToGroup(group);

		}
	};

	EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {

		@Override
		public void handle(MouseEvent event) {

			if (event.isSecondaryButtonDown() || rect.isResizing()) {
				return;
			}

			double offsetX = event.getX() - dragContext.mouseAnchorX;
			double offsetY = event.getY() - dragContext.mouseAnchorY;

			if (offsetX > 0) {
				rect.setWidth(offsetX);
			} else {
				rect.setX(event.getX());
				rect.setWidth(dragContext.mouseAnchorX - rect.getX());
			}

			if (offsetY > 0) {
				rect.setHeight(offsetY);
			} else {
				rect.setY(event.getY());
				rect.setHeight(dragContext.mouseAnchorY - rect.getY());
			}
		}
	};

	EventHandler<MouseEvent> onMouseReleasedEventHandler = new EventHandler<MouseEvent>() {

		@Override
		public void handle(MouseEvent event) {

			if (event.isSecondaryButtonDown() || rect.isResizing()) {
				return;
			}
			limitRectToImageView();

		}

	};

	public void limitRectToImageView() {

		if (imageView != null) {

			double width = rect.getWidth();
			double height = rect.getHeight();
			double minX = rect.getX();
			double minY = rect.getY();
			double maxX = rect.getX() + width;
			double maxY = rect.getY() + height;
			Double finalWidth = width;
			Double finalHeight = height;

			// limiting to image pane
			if (minX < imageView.getBoundsInLocal().getMinX()) {
				rect.setX(imageView.getBoundsInLocal().getMinX());
				finalWidth -= rect.getX() - minX;
			}
			if (minY < imageView.getBoundsInLocal().getMinY()) {
				rect.setY(imageView.getBoundsInLocal().getMinY());
				finalHeight -= rect.getY() - minY;
			}

			if (maxX > imageView.getBoundsInLocal().getMaxX()) {
				finalWidth -= maxX - imageView.getBoundsInLocal().getMaxX();
			}
			if (maxY > imageView.getBoundsInLocal().getMaxY()) {
				finalHeight -= maxY - imageView.getBoundsInLocal().getMaxY();
			}
			minX = rect.getX();
			minY = rect.getY();
			maxX = rect.getX() + finalWidth;
			maxY = rect.getY() + finalHeight;

			//
			double widthScale = imageView.getBoundsInLocal().getWidth() / imageView.getViewport().getWidth();
			double heightScale = imageView.getBoundsInLocal().getHeight() / imageView.getViewport().getHeight();

			// resolving left and top sides
			if (imageView.getViewport().getMinX() < 0) {
				double imageX = -imageView.getViewport().getMinX() * widthScale;
				if (rect.getX() < imageX) {
					rect.setX(imageX);
					finalWidth -= imageX - minX;
				}
			}
			if (imageView.getViewport().getMinY() < 0) {
				double imageY = -imageView.getViewport().getMinY() * heightScale;
				if (rect.getY() < imageY) {
					rect.setY(imageY);
					finalHeight -= imageY - minY;
				}
			}
			if (minX < 0) {
				rect.setX(0);
				finalWidth -= -minX;
			}
			if (minY < 0) {
				rect.setY(0);
				finalHeight -= -minY;
			}

			// resolving right and bottom side
			// if width of viewPort > width of image, the imageX = (imagewidth)*widthscale
			double imageMaxX = imageView.getImage().getWidth() * widthScale;
			double imageMaxY = imageView.getImage().getHeight() * heightScale;
			if (imageView.getViewport().getMinX() < 0) {
				imageMaxX += -imageView.getViewport().getMinX() * widthScale;
			}
			if (imageView.getViewport().getMinY() < 0) {
				imageMaxY += -imageView.getViewport().getMinY() * heightScale;
			}
			if (imageView.getViewport().getWidth() > imageView.getImage().getWidth()) {
				if (maxX > imageMaxX) {
					finalWidth -= maxX - imageMaxX;
				}
			}
			if (imageView.getViewport().getHeight() > imageView.getImage().getHeight()) {
				if (maxY > imageMaxY) {
					finalHeight -= maxY - imageMaxY;
				}
			}

			if (maxX > imageView.getBoundsInLocal().getMaxX()) {
				finalWidth -= maxX - imageView.getBoundsInLocal().getMaxX();
			}
			if (maxY > imageView.getBoundsInLocal().getMaxY()) {
				finalHeight -= maxY - imageView.getBoundsInLocal().getMaxY();
			}

			rect.setWidth(finalWidth);
			rect.setHeight(finalHeight);
		}
	}

	private static final class DragContext {

		public double mouseAnchorX;
		public double mouseAnchorY;

	}

	public void setImageView(ImageView imageView2) {
		imageView = imageView2;
	}
}
