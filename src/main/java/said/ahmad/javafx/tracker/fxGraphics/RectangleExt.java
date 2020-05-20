package said.ahmad.javafx.tracker.fxGraphics;

import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

public class RectangleExt extends Rectangle {

	public RectangleExt() {
		super();
	}

	public RectangleExt(double x, double y, double width, double height) {
		super(x, y, width, height);
	}

	public RectangleExt(double width, double height, Paint fill) {
		super(width, height, fill);
	}

	public RectangleExt(double width, double height) {
		super(width, height);
	}

	public double getMinX() {
		return getX();
	}

	public double getMaxX() {
		return getX() + getWidth();
	}

	public double getMinY() {
		return getY();
	}

	public double getMaxY() {
		return getY() + getHeight();
	}

}
