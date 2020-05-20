package said.ahmad.javafx.tracker.fxGraphics;

import java.util.ArrayList;

import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

/**
 * https://github.com/imgeself/JavaFX-ImageCropper
 *
 * @author imgeself
 */
class ResizableRectangle extends Rectangle {

	private double rectangleStartX;
	private double rectangleStartY;
	private double mouseClickPozX;
	private double mouseClickPozY;
	private static final double RESIZER_SQUARE_SIDE = 12;
	private Paint resizerSquareColor = Color.WHITE;
	private Paint rectangleStrokeColor = Color.BLACK;
	// used as isResizing or moving
	private boolean isResizing = false;

	ArrayList<Rectangle> allMiniRec = new ArrayList<>();

	ResizableRectangle(double x, double y, double width, double height) {
		super(x, y, width, height);
		super.setStroke(rectangleStrokeColor);
		super.setStrokeWidth(1);
		super.setFill(Color.color(1, 1, 1, 0));

		/**
		 * north east south west
		 */
		// clockwise turn
		// north west
		makeNWResizerSquare();
		// west
		makeCWResizerSquare();
		// south west
		makeSWResizerSquare();
		// south
		makeSCResizerSquare();
		// south east
		makeSEResizerSquare();
		// east
		makeCEResizerSquare();
		// north east
		makeNEResizerSquare();
		// north
		makeNCResizerSquare();

		makeNCMoverSquare();
	}

	private void makeNCMoverSquare() {

		Rectangle moveRect = new Rectangle(RESIZER_SQUARE_SIDE + 2, RESIZER_SQUARE_SIDE + 2);
		moveRect.xProperty().bind(super.xProperty()
				.add(super.widthProperty().divide(2.0).subtract(moveRect.widthProperty().divide(2.0))));
		moveRect.yProperty().bind(super.yProperty().subtract(moveRect.heightProperty().multiply(2.0)));

		prepareResizerSquare(moveRect);
//		group.getChildren().add(moveRect);

		moveRect.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> moveRect.getParent().setCursor(Cursor.MOVE));

		moveRect.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
			moveRect.getParent().setCursor(Cursor.MOVE);
			isResizing = true;
			mouseClickPozX = event.getX();
			mouseClickPozY = event.getY();

		});

		moveRect.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
			moveRect.getParent().setCursor(Cursor.HAND);
			isResizing = false;
		});

		moveRect.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {

			double offsetX = event.getX() - mouseClickPozX;
			double offsetY = event.getY() - mouseClickPozY;
			double newX = super.getX() + offsetX;
			double newY = super.getY() + offsetY;

			if (newX >= 0 && newX + super.getWidth() <= super.getParent().getBoundsInLocal().getWidth()) {
				super.setX(newX);
			}

			if (newY >= 0 && newY + super.getHeight() <= super.getParent().getBoundsInLocal().getHeight()) {
				super.setY(newY);
			}
			mouseClickPozX = event.getX();
			mouseClickPozY = event.getY();

		});

	}

	private void makeNWResizerSquare() {
		Rectangle squareNW = new Rectangle(RESIZER_SQUARE_SIDE, RESIZER_SQUARE_SIDE);

		squareNW.xProperty().bind(super.xProperty().subtract(squareNW.widthProperty().divide(2.0)));
		squareNW.yProperty().bind(super.yProperty().subtract(squareNW.heightProperty().divide(2.0)));

		squareNW.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> squareNW.getParent().setCursor(Cursor.NW_RESIZE));

		prepareResizerSquare(squareNW);

		squareNW.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
			rectangleStartX = super.getX();
			rectangleStartY = super.getY();
			double offsetX = event.getX() - rectangleStartX;
			double offsetY = event.getY() - rectangleStartY;
			double newX = super.getX() + offsetX;
			double newY = super.getY() + offsetY;

			// commented like this is to keep selection area within group otherwise
			// i will allow any selection area and in rubber band selection on release will
			// re limit the rectangle
//			if (newX >= 0 && newX <= super.getX() + super.getWidth()) {
			if (newX <= super.getX() + super.getWidth()) {
				super.setX(newX);
				super.setWidth(super.getWidth() - offsetX);
			}

//			if (newY >= 0 && newY <= super.getY() + super.getHeight()) {
			if (newY <= super.getY() + super.getHeight()) {
				super.setY(newY);
				super.setHeight(super.getHeight() - offsetY);
			}

		});
		squareNW.setOnMousePressed(e -> {
			isResizing = true;
		});
		squareNW.setOnMouseReleased(e -> {
			isResizing = false;
		});
	}

	private void makeCWResizerSquare() {
		Rectangle squareCW = new Rectangle(RESIZER_SQUARE_SIDE, RESIZER_SQUARE_SIDE);
		squareCW.xProperty().bind(super.xProperty().subtract(squareCW.widthProperty().divide(2.0)));
		squareCW.yProperty().bind(super.yProperty()
				.add(super.heightProperty().divide(2.0).subtract(squareCW.heightProperty().divide(2.0))));

		squareCW.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> squareCW.getParent().setCursor(Cursor.W_RESIZE));

		prepareResizerSquare(squareCW);

		squareCW.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
			rectangleStartX = super.getX();
			double offsetX = event.getX() - rectangleStartX;
			double newX = super.getX() + offsetX;

//			if (newX >= 0 && newX <= super.getX() + super.getWidth() - 5) {
			if (newX <= super.getX() + super.getWidth() - 5) {
				super.setX(newX);
				super.setWidth(super.getWidth() - offsetX);
			}

		});
		squareCW.setOnMousePressed(e -> {
			isResizing = true;
		});
		squareCW.setOnMouseReleased(e -> {
			isResizing = false;
		});

	}

	private void makeSWResizerSquare() {
		Rectangle squareSW = new Rectangle(RESIZER_SQUARE_SIDE, RESIZER_SQUARE_SIDE);
		squareSW.xProperty().bind(super.xProperty().subtract(squareSW.widthProperty().divide(2.0)));
		squareSW.yProperty()
				.bind(super.yProperty().add(super.heightProperty().subtract(squareSW.heightProperty().divide(2.0))));

		squareSW.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> squareSW.getParent().setCursor(Cursor.SW_RESIZE));

		prepareResizerSquare(squareSW);

		squareSW.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
			rectangleStartX = super.getX();
			rectangleStartY = super.getY();
			double offsetX = event.getX() - rectangleStartX;
			double offsetY = event.getY() - rectangleStartY;
			double newX = super.getX() + offsetX;

//			if (newX >= 0 && newX <= super.getX() + super.getWidth() - 5) {
			if (newX <= super.getX() + super.getWidth() - 5) {
				super.setX(newX);
				super.setWidth(super.getWidth() - offsetX);
			}

//			if (offsetY >= 0 && offsetY <= super.getY() + super.getHeight() - 5) {
			if (offsetY >= 0) {
				super.setHeight(offsetY);
			}
		});
		squareSW.setOnMousePressed(e -> {
			isResizing = true;
		});
		squareSW.setOnMouseReleased(e -> {
			isResizing = false;
		});
	}

	private void makeSCResizerSquare() {
		Rectangle squareSC = new Rectangle(RESIZER_SQUARE_SIDE, RESIZER_SQUARE_SIDE);

		squareSC.xProperty().bind(super.xProperty()
				.add(super.widthProperty().divide(2.0).subtract(squareSC.widthProperty().divide(2.0))));
		squareSC.yProperty()
				.bind(super.yProperty().add(super.heightProperty().subtract(squareSC.heightProperty().divide(2.0))));

		squareSC.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> squareSC.getParent().setCursor(Cursor.S_RESIZE));

		prepareResizerSquare(squareSC);

		squareSC.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
			rectangleStartY = super.getY();
			double offsetY = event.getY() - rectangleStartY;
			// like this are commented to make it possible to depass limit of pane
//			if (offsetY >= 0 && offsetY <= super.getY() + super.getHeight() - 5) {
			if (offsetY >= 0) {
				super.setHeight(offsetY);
			}

		});
		squareSC.setOnMousePressed(e -> {
			isResizing = true;
		});
		squareSC.setOnMouseReleased(e -> {
			isResizing = false;
		});
	}

	private void makeSEResizerSquare() {
		Rectangle squareSE = new Rectangle(RESIZER_SQUARE_SIDE, RESIZER_SQUARE_SIDE);
		squareSE.xProperty()
				.bind(super.xProperty().add(super.widthProperty()).subtract(squareSE.widthProperty().divide(2.0)));
		squareSE.yProperty()
				.bind(super.yProperty().add(super.heightProperty().subtract(squareSE.heightProperty().divide(2.0))));

		squareSE.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> squareSE.getParent().setCursor(Cursor.SE_RESIZE));

		prepareResizerSquare(squareSE);

		squareSE.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
			rectangleStartX = super.getX();
			rectangleStartY = super.getY();
			double offsetX = event.getX() - rectangleStartX;
			double offsetY = event.getY() - rectangleStartY;

//			if (offsetX >= 0 && offsetX <= super.getX() + super.getWidth() - 5) {
			if (offsetX >= 0) {
				super.setWidth(offsetX);
			}

//			if (offsetY >= 0 && offsetY <= super.getY() + super.getHeight() - 5) {
			if (offsetY >= 0) {
				super.setHeight(offsetY);
			}
		});
		squareSE.setOnMousePressed(e -> {
			isResizing = true;
		});
		squareSE.setOnMouseReleased(e -> {
			isResizing = false;
		});
	}

	private void makeCEResizerSquare() {
		Rectangle squareCE = new Rectangle(RESIZER_SQUARE_SIDE, RESIZER_SQUARE_SIDE);
		squareCE.xProperty()
				.bind(super.xProperty().add(super.widthProperty()).subtract(squareCE.widthProperty().divide(2.0)));
		squareCE.yProperty().bind(super.yProperty()
				.add(super.heightProperty().divide(2.0).subtract(squareCE.heightProperty().divide(2.0))));

		squareCE.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> squareCE.getParent().setCursor(Cursor.E_RESIZE));

		prepareResizerSquare(squareCE);

		squareCE.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
			rectangleStartX = super.getX();
			double offsetX = event.getX() - rectangleStartX;
//			if (offsetX >= 0 && offsetX <= super.getX() + super.getWidth() - 5) {
			if (offsetX >= 0) {
				super.setWidth(offsetX);
			}

		});
		squareCE.setOnMousePressed(e -> {
			isResizing = true;
		});
		squareCE.setOnMouseReleased(e -> {
			isResizing = false;
		});
	}

	private void makeNEResizerSquare() {
		Rectangle squareNE = new Rectangle(RESIZER_SQUARE_SIDE, RESIZER_SQUARE_SIDE);

		squareNE.xProperty()
				.bind(super.xProperty().add(super.widthProperty()).subtract(squareNE.widthProperty().divide(2.0)));
		squareNE.yProperty().bind(super.yProperty().subtract(squareNE.heightProperty().divide(2.0)));

		squareNE.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> squareNE.getParent().setCursor(Cursor.NE_RESIZE));

		prepareResizerSquare(squareNE);

		squareNE.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
			rectangleStartX = super.getX();
			rectangleStartY = super.getY();
			double offsetX = event.getX() - rectangleStartX;
			double offsetY = event.getY() - rectangleStartY;
			double newY = super.getY() + offsetY;

//			if (offsetX >= 0 && offsetX <= super.getX() + super.getWidth() - 5) {
			if (offsetX >= 0) {
				super.setWidth(offsetX);
			}

//			if (newY >= 0 && newY <= super.getY() + super.getHeight() - 5) {
			if (newY <= super.getY() + super.getHeight() - 5) {
				super.setY(newY);
				super.setHeight(super.getHeight() - offsetY);
			}

		});
		squareNE.setOnMousePressed(e -> {
			isResizing = true;
		});
		squareNE.setOnMouseReleased(e -> {
			isResizing = false;
		});
	}

	private void makeNCResizerSquare() {
		Rectangle squareNC = new Rectangle(RESIZER_SQUARE_SIDE, RESIZER_SQUARE_SIDE);

		squareNC.xProperty().bind(super.xProperty()
				.add(super.widthProperty().divide(2.0).subtract(squareNC.widthProperty().divide(2.0))));
		squareNC.yProperty().bind(super.yProperty().subtract(squareNC.heightProperty().divide(2.0)));

		squareNC.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> squareNC.getParent().setCursor(Cursor.N_RESIZE));

		prepareResizerSquare(squareNC);

		squareNC.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
			rectangleStartY = super.getY();
			double offsetY = event.getY() - rectangleStartY;
			double newY = super.getY() + offsetY;

//			if (newY >= 0 && newY <= super.getY() + super.getHeight()) {
			if (newY <= super.getY() + super.getHeight()) {
				super.setY(newY);
				super.setHeight(super.getHeight() - offsetY);
			}
		});
		squareNC.setOnMousePressed(e -> {
			isResizing = true;
		});
		squareNC.setOnMouseReleased(e -> {
			isResizing = false;
		});
	}

	public boolean isResizing() {
		return isResizing;
	}

	private void prepareResizerSquare(Rectangle rect) {
		rect.setFill(resizerSquareColor);

		rect.addEventHandler(MouseEvent.MOUSE_EXITED, event -> rect.getParent().setCursor(Cursor.DEFAULT));
		allMiniRec.add(rect);
	}

	public void addToGroup(Group group) {
		group.getChildren().add(this);
		allMiniRec.forEach(e -> group.getChildren().add(e));
	}

	public void removeFromGroup(Group group) {
		group.getChildren().remove(this);
		allMiniRec.forEach(e -> group.getChildren().remove(e));
	}

}