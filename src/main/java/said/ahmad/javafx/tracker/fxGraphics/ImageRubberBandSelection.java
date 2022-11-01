package said.ahmad.javafx.tracker.fxGraphics;

import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

/**
 * Drag rectangle with mouse cursor in order to get selection bounds
 */
public class ImageRubberBandSelection extends RubberBandSelection {

	/**
	 * Target node
	 */
	private final ImageView imageView;

	/**
	 * Parent node that contain image and resizable rectangle
	 */
	private final Group parentGroup;

	public Bounds getBounds() {
		return getRectangle().getBoundsInParent();
	}

	/**
	 *
	 * ImageView is used to restrict rubberBandSelection to be limited by the
	 * boundary of the image
	 *
	 * @param parentGroup
	 *            the layer where imageView is in
	 * @param imageView
	 *            can be null
	 */
	public ImageRubberBandSelection(Group parentGroup, ImageView imageView) {
		super();
		this.parentGroup = parentGroup;
		this.imageView = imageView;
		setOnMousePressedEventHandler(getDefaultOnMousePressedEventHandler(), parentGroup);
		setOnMouseDraggedEventHandler(getDefaultOnMouseDraggedEventHandler(), parentGroup);
		setOnMouseReleasedEventHandler(getDefaultOnMouseReleasedEventHandler(), parentGroup);
		selectFullImage();
	}

	public void selectFullImage() {
		if (imageView != null) {
			if (getParentNode().getChildren().contains(getRectangle())) {
				getRectangle().removeFromGroup(getParentNode());
			}
			getRectangle().setX(imageView.getBoundsInLocal().getMinX());
			getRectangle().setY(imageView.getBoundsInLocal().getMinY());
			getRectangle().setWidth(imageView.getBoundsInLocal().getWidth());
			getRectangle().setHeight(imageView.getBoundsInLocal().getHeight());
			getRectangle().addToGroup(getParentNode());
		}
		limitRectToImageView();
	}

	@Override
	public ImageView getTargetNode() {
		return imageView;
	}

	@Override
	public double getRectanglePositionX() {
		return getRectangle().getX();
	}

	@Override
	public void setRectanglePositionX(double x) {
		getRectangle().setX(x);
	}

	@Override
	public double getRectanglePositionY() {
		return getRectangle().getY();
	}

	@Override
	public void setRectanglePositionY(double y) {
		getRectangle().setY(y);
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
		rectangle.addToGroup(parentGroup);
	}

	@Override
	public void removeRectangleFromParent(ResizableRectangle rectangle) {
		rectangle.removeFromGroup(parentGroup);
	}

	@Override
	public void limitRectToTargetNode() {
		limitRectToImageView();
	}

	@Override
	public Group getParentNode() {
		return parentGroup;
	}

	public void limitRectToImageView() {

		if (imageView != null) {
			super.limitRectToTargetNode();
			double width = getRectangle().getWidth();
			double height = getRectangle().getHeight();
			double minX = getRectangle().getX();
			double minY = getRectangle().getY();
			double maxX = getRectangle().getX() + width;
			double maxY = getRectangle().getY() + height;
			Double finalWidth = width;
			Double finalHeight = height;

			//
			double widthScale = imageView.getBoundsInLocal().getWidth() / imageView.getViewport().getWidth();
			double heightScale = imageView.getBoundsInLocal().getHeight() / imageView.getViewport().getHeight();

			// resolving left and top sides
			if (imageView.getViewport().getMinX() < 0) {
				double imageX = -imageView.getViewport().getMinX() * widthScale;
				if (getRectangle().getX() < imageX) {
					getRectangle().setX(imageX);
					finalWidth -= imageX - minX;
				}
			}
			if (imageView.getViewport().getMinY() < 0) {
				double imageY = -imageView.getViewport().getMinY() * heightScale;
				if (getRectangle().getY() < imageY) {
					getRectangle().setY(imageY);
					finalHeight -= imageY - minY;
				}
			}
			if (minX < 0) {
				getRectangle().setX(0);
				finalWidth -= -minX;
			}
			if (minY < 0) {
				getRectangle().setY(0);
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

			getRectangle().setWidth(finalWidth);
			getRectangle().setHeight(finalHeight);
		}
	}
}
