package application.controller;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import application.DialogHelper;
import application.FileHelper;
import application.FileTracker;
import application.Main;
import application.RunMenu;
import application.StringHelper;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class PhotoViewerController {

	@FXML
	private CheckBox fitWidthCheckBox;

	@FXML
	private CheckBox continousCheckBox;

	@FXML
	private BorderPane borderPane;

	@FXML
	private Label indexOfImage;

	@FXML
	private Label sizeMb;

	@FXML
	private Label widthPx;

	@FXML
	private Label heightPx;

	@FXML
	private Label zoomLevelLabel;

	@FXML
	private Slider zoomSlider;

	@FXML
	private ToggleButton markSeen;

	@FXML
	private Label labelDescription;

	@FXML
	private TextField nameImage;

	@FXML
	private Button applyName;

	@FXML
	private Label labelDescription1;

	@FXML
	private TextField noteInput;

	@FXML
	private Button applyNote;

	@FXML
	private ImageView imageView;

	@FXML
	private Slider ySlider;

	@FXML
	private Button nextButton;

	@FXML
	private TextField locationField;

	@FXML
	private Button revealLocation;

	@FXML
	private Slider xSlider;

	@FXML
	private Button previousButton;

	@FXML
	private Pane imagePane;

	// zoom idea source : https://gist.github.com/james-d/ce5ec1fd44ce6c64e81a
	private int MIN_PIXELS = 100;

	public static HashSet<String> ArrayIMGExt = new HashSet<String>(
			Arrays.asList("PNG", "GIF", "JPG", "JPS", "MPO", "BMP", "WEBMP", "JPEG"));
	private FileTracker mFileTracker;
	private File mDirectory;
	private Stage photoStage;
	private List<File> ImgResources;

	public PhotoViewerController(List<File> imgResources, File selectedImg, WelcomeController welcomeToRefreshCanNull) {
		try {
			photoStage = new Stage();
			Parent root;
			Scene scene;
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PhotoViewer.fxml"));
			loader.setController(this);
			root = loader.load();
			scene = new Scene(root);
			scene.getStylesheets().add("/css/bootstrap3.css");
			photoStage.getIcons().add(new Image(Main.class.getResourceAsStream("/img/photo_Icon.png")));

			photoStage.setTitle("Photo Viewer " + selectedImg.getName());
			photoStage.setScene(scene);
		} catch (IOException e) {
			e.printStackTrace();
		}
		mDirectory = selectedImg.getParentFile();
		if (imgResources == null || imgResources.size() <= 1) {
			imgResources = getImgFilesInDir(mDirectory);
		}
		ImgResources = imgResources;
		initializeImageView();
		initializeButtons();
		initializeSliders();
		mFileTracker = new FileTracker(selectedImg.toPath().getParent());
		if (mFileTracker.isTracked()) {
			mFileTracker.loadMap(selectedImg.toPath().getParent(), true, false);
			mFileTracker.resolveConflict();
		}
		rollerPhoto = imgResources.indexOf(selectedImg);
		photoStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent t) {
				if (welcomeToRefreshCanNull != null) {
					welcomeToRefreshCanNull.refreshBothViews(null);
				}
			}
		});

		photoStage.show();
		photoStage.setMaximized(true);
//		photoStage.setFullScreen(true);
		changeImage(selectedImg);

		ChangeListener<Number> stageSizeListener = (observable, oldValue, newValue) -> {
			Platform.runLater(() -> {
				deltaW = getDeltaPerfectWidth(width, height);
				deltaH = getDeltaPerfectHeight(width, height);
				resetCenter(imageView, deltaW, deltaH, width, height);
				if (fitWidthCheckBox.isSelected()) {
					fitWidth();
				}

			});
		};
		photoStage.widthProperty().addListener(stageSizeListener);
		photoStage.heightProperty().addListener(stageSizeListener);

	}

	private void initializeButtons() {
		fitWidthCheckBox.setOnAction(p -> {
			if (fitWidthCheckBox.isSelected()) {
				fitWidth();
			} else {
				resetCenter(imageView, deltaW, deltaH, width, height);
			}
		});
		zoomLevelLabel.setOnMouseClicked(e -> {
			String ans = DialogHelper.showTextInputDialog("TaTa !", "You found secret button :)",
					"Enter new maximum zoom level", "10");
			zoomSlider.setMax(Double.parseDouble(ans));
		});
		nameImage.setOnAction(e -> renameImage());
		noteInput.setOnAction(e -> addNoteImage());
		if (rollerPhoto == ImgResources.size() - 1) {
			nextButton.setDisable(true);
		} else if (rollerPhoto == 0) {
			previousButton.setDisable(true);
		}
		nameImage.setOnKeyPressed(key -> {
			switch (key.getCode()) {
			case ESCAPE:
				nameImage.setText(ImgResources.get(rollerPhoto).getName());
				break;
			default:
				break;
			}
		});
	}

	private void initializeSliders() {

		zoomSlider.setMin(1);
		zoomSlider.setMax(8);
		zoomSlider.valueProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				zoomLevelLabel.setText(String.format("X %.2f", cumulativeZoom));
				if (cumulativeZoom == newValue) {
					// that mean other have changed me as well as CumlativeScale
					return;
				}
				cumulativeZoom = (double) newValue;
				double scale = (double) oldValue / (double) newValue;
				Rectangle2D viewport = imageView.getViewport();
				double newWidth = viewport.getWidth() * scale;
				double newHeight = viewport.getHeight() * scale;

				// keep center of image view constant see above explanation zoom on scroll
				double xCenter = (viewport.getMinX() + viewport.getMaxX()) / 2;
				double yCenter = (viewport.getMinY() + viewport.getMaxY()) / 2;
//				Point2D mouse = imageViewToImage(imageView, new Point2D(xCenter, yCenter));
				double newMinX = clamp(xCenter - (xCenter - viewport.getMinX()) * scale, -deltaW / 2,
						width + deltaW - newWidth);
				double newMinY = clamp(yCenter - (yCenter - viewport.getMinY()) * scale, -deltaH / 2,
						height + deltaH - newHeight);

				imageView.setViewport(new Rectangle2D(newMinX, newMinY, newWidth, newHeight));
				calculateAndCenter(imageView);
				updateWHSlider();
			}

		});
		xSlider.setMin(0.0);
		xSlider.setMax(1);
		xSlider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
				Rectangle2D viewPort = imageView.getViewport();
				double maximumShift = width - viewPort.getWidth();
				if (maximumShift > 0) {
					imageView.setViewport(new Rectangle2D(maximumShift * (double) newValue, viewPort.getMinY(),
							viewPort.getWidth(), viewPort.getHeight()));
				} else {
					xSlider.setValue(0.5);
				}
			}
		});
		ySlider.setMin(0);
		ySlider.setMax(1);
		ySlider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
				Rectangle2D viewPort = imageView.getViewport();
				double maximumShift = height - viewPort.getHeight();
				if (maximumShift > 0) {
					imageView.setViewport(new Rectangle2D(viewPort.getMinX(), maximumShift * (double) newValue,
							viewPort.getWidth(), viewPort.getHeight()));
				} else {
					ySlider.setValue(0.5);
				}
			}
		});
	}

	private void updateWHSlider() {
		xSlider.setValue(imageView.getViewport().getMinX() / (width - imageView.getViewport().getWidth()));
		ySlider.setValue(imageView.getViewport().getMinY() / (height - imageView.getViewport().getHeight()));
	}

	private Double cumulativeZoom = 1.0;

	private void initializeImageView() {
//		imageView.fitWidthProperty().bind(borderPane.widthProperty());
//		imageView.fitHeightProperty().bind(borderPane.heightProperty());
		imageView.fitWidthProperty().bind(imagePane.widthProperty());
		imageView.fitHeightProperty().bind(imagePane.heightProperty());
		imageView.setPreserveRatio(false); // done manually due to zoom constraints

		ObjectProperty<Point2D> mouseDown = new SimpleObjectProperty<>();
		imageView.setFocusTraversable(true);
		imageView.requestFocus();
		imageView.setOnKeyPressed(key -> {
			key.consume();
			switch (key.getCode()) {
			case RIGHT:
			case F:
			case D:

				nextImage();
				break;
			case LEFT:
			case A:
				previousImage();
				break;
			case UP:
			case W:
				shift(imageView, new Point2D(0, height / 10));
				break;
			case DOWN:
			case S:
				shift(imageView, new Point2D(0, -height / 10));
				break;
			case Q:
				fitWidth();
				break;
			case SPACE:
				toggleSeen();
				break;
			case N:
				Platform.runLater(() -> noteInput.requestFocus());
				break;
			case F2:
			case M:
				Platform.runLater(() -> nameImage.requestFocus());
				break;
			case E:
			case R:
				StringHelper.RunRuntimeProcess(
						new String[] { "explorer.exe", "/select,", ImgResources.get(rollerPhoto).toString() });
				break;
			case DELETE:
				deleteIMG();
				break;
			default:
				break;
			}
		});
		imageView.setOnMousePressed(e -> {
			imageView.requestFocus();
			Point2D mousePress = imageViewToImage(imageView, new Point2D(e.getX(), e.getY()));
			mouseDown.set(mousePress);
			imageView.setCursor(Cursor.CLOSED_HAND);
		});

		imageView.setOnMouseDragged(e -> {
			Point2D dragPoint = imageViewToImage(imageView, new Point2D(e.getX(), e.getY()));
			shift(imageView, dragPoint.subtract(mouseDown.get()));
			mouseDown.set(imageViewToImage(imageView, new Point2D(e.getX(), e.getY())));
		});
		imageView.setCursor(Cursor.OPEN_HAND);
		imageView.setOnMouseReleased(e -> {
			imageView.setCursor(Cursor.OPEN_HAND);
		});

		imageView.setOnScroll(e -> {
			if (!e.isControlDown()) {
				shift(imageView, new Point2D(0, e.getDeltaY() * height / 120 / 5));
				return;
			}
			double delta = -e.getDeltaY();
			Rectangle2D viewport = imageView.getViewport();

			double scale = clamp(Math.pow(1.005, delta),

					// don't scale so we're zoomed in to fewer than MIN_PIXELS in any direction:
					Math.min(MIN_PIXELS / viewport.getWidth(), MIN_PIXELS / viewport.getHeight()),

					// don't scale so that we're bigger than image dimensions:
					Math.max((width + deltaW) / viewport.getWidth(), (height + deltaH) / viewport.getHeight())

			);
			cumulativeZoom /= scale;
			zoomSlider.setValue(cumulativeZoom);
			Point2D mouse = imageViewToImage(imageView, new Point2D(e.getX(), e.getY()));

			double newWidth = viewport.getWidth() * scale;
			double newHeight = viewport.getHeight() * scale;

			// To keep the visual point under the mouse from moving, we need
			// (x - newViewportMinX) / (x - currentViewportMinX) = scale
			// where x is the mouse X coordinate in the image

			// solving this for newViewportMinX gives

			// newViewportMinX = x - (x - currentViewportMinX) * scale

			// we then clamp this value so the image never scrolls out
			// of the imageview:

			double newMinX = clamp(mouse.getX() - (mouse.getX() - viewport.getMinX()) * scale, -deltaW / 2,
					width + deltaW - newWidth);
			double newMinY = clamp(mouse.getY() - (mouse.getY() - viewport.getMinY()) * scale, -deltaH / 2,
					height + deltaH - newHeight);

			imageView.setViewport(new Rectangle2D(newMinX, newMinY, newWidth, newHeight));
			calculateAndCenter(imageView);
		});

		imageView.setOnMouseClicked(e -> {
			if (e.getButton().equals(MouseButton.SECONDARY)) {
				RunMenu.showMenu(Arrays.asList(ImgResources.get(rollerPhoto).toPath()));
			} else {

				if (e.getClickCount() == 2) {
					Rectangle2D viewPort = imageView.getViewport();
					double maxX = width + deltaW - viewPort.getWidth();
					double maxY = height + deltaH - viewPort.getHeight();
					if ((int) maxX != 0 || (int) maxY != 0) {
						// there is a scale factor => reset view
						resetCenter(imageView, deltaW, deltaH, width, height);
						return;
					}
					double perfectRatio = imagePane.getWidth() / imagePane.getHeight();
					if (viewPort.getWidth() > width) {
						// perfect fit width zoom
						double maxHeight = width / perfectRatio;
						double newMinY = e.getY() / imagePane.getHeight() * height - maxHeight / 2;
						if (newMinY < 0) {
							newMinY = 0;
						}
						cumulativeZoom = (width + deltaW) / width;
						zoomSlider.setValue(cumulativeZoom);
						imageView.setViewport(new Rectangle2D(0, newMinY, width, maxHeight));
					}
					if (viewPort.getHeight() > height) {
						// perfect fit height zoom
						double maxWidth = height * perfectRatio;
						double newMinX = e.getX() / imagePane.getWidth() * width - maxWidth / 2;
						if (newMinX < 0) {
							newMinX = 0;
						}
						cumulativeZoom = (height + deltaH) / height;
						zoomSlider.setValue(cumulativeZoom);
						imageView.setViewport(new Rectangle2D(newMinX, 0, maxWidth, height));
					}
				}
			}
		});

	}

	@FXML
	private void fitWidth() {
		double perfectRatio = imagePane.getWidth() / imagePane.getHeight();
		// perfect fit width zoom
		double maxHeight = width / perfectRatio;
		cumulativeZoom = (width + deltaW) / width;
		zoomSlider.setValue(cumulativeZoom);
		imageView.setViewport(new Rectangle2D(0, 0, width, maxHeight));
		updateWHSlider();
	}

	private List<File> getImgFilesInDir(File parent) {
		// https://stackoverflow.com/questions/2965747/why-do-i-get-an-unsupportedoperationexception-when-trying-to-remove-an-element-f
		return new LinkedList<File>(
				Arrays.asList(parent.listFiles(p -> ArrayIMGExt.contains(StringHelper.getExtention(p.getName())))));
	}

	// for a faster switch we don't need to define method each time
	// so we define private variable that change on image change
	private double width;
	private double height;
	private double deltaW;
	private double deltaH;

	private void changeImage(File selectedImg) {

		locationField.setText(selectedImg.getAbsolutePath());
		nameImage.setText(selectedImg.getName());
//		Image image = generateImage(selectedImg);
		Image image = new Image(selectedImg.toURI().toString());

		double width = image.getWidth();
		double height = image.getHeight();
		this.width = width;
		this.height = height;

		double deltaW = getDeltaPerfectWidth(width, height);
		double deltaH = getDeltaPerfectHeight(width, height);
		this.deltaW = deltaW;
		this.deltaH = deltaH;

		imageView.setImage(image);
		photoStage.setTitle("Photo Viewer " + selectedImg.getName());
		resetCenter(imageView, deltaW, deltaH, width, height);
		if (fitWidthCheckBox.isSelected()) {
			fitWidth();
		}
		widthPx.setText(width + " Px");
		heightPx.setText(height + " Px");
		sizeMb.setText(String.format("%.2f", selectedImg.length() / 1024.0 / 1024.0) + " MB");
		indexOfImage.setText(rollerPhoto + 1 + " / " + ImgResources.size());

		if (!mFileTracker.getWorkingDir().equals(selectedImg.toPath().getParent())) {
			mFileTracker.setWorkingDir(selectedImg.toPath().getParent());
			if (mFileTracker.isTracked()) {
				mFileTracker.loadMap(selectedImg.toPath().getParent(), true, false);
				mFileTracker.resolveConflict();
			}
		}
		if (mFileTracker.isTracked()) {
			updateMarkSeen(mFileTracker.isSeen(selectedImg.getName()));
			noteInput.setText(mFileTracker.getNoteTooltipText(selectedImg.getName()));
		} else {
			markSeen.getStyleClass().removeAll("info", "success");
			markSeen.setText("-");
		}
	}

	private double getDeltaPerfectHeight(double width, double height) {
		double perfectRatio = imagePane.getWidth() / imagePane.getHeight();
		double expectedRatio = width / height;
		if (perfectRatio < expectedRatio) {
			return width / perfectRatio - height;
		}
		return 0;
	}

	private double getDeltaPerfectWidth(double width, double height) {
		double perfectRatio = imagePane.getWidth() / imagePane.getHeight();
		double expectedRatio = width / height;
		if (perfectRatio > expectedRatio) {
			return height * perfectRatio - width;
		}
		return 0;
	}

	@Deprecated
	private Image generatePerfectRatioImage(File originalImgFile) {
		double perfectRatio = imagePane.getWidth() / imagePane.getHeight();
		Image image = new Image(originalImgFile.toURI().toString());
		double expectedWidth = image.getHeight() * perfectRatio;
		BufferedImage imgb1;
		int width = (int) image.getWidth();
		int height = (int) image.getHeight();
		Image finalImage = null;
		BufferedImage imgbCenter = null;
		try {
			imgbCenter = ImageIO.read(originalImgFile);
		} catch (IOException e) {
		}
		if (expectedWidth > image.getWidth()) {
			// need to add width
			int deltaW = (int) (expectedWidth - image.getWidth());
			imgb1 = new BufferedImage(deltaW + width + 5, height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics = imgb1.createGraphics();
			// https://stackoverflow.com/questions/20826216/copy-two-bufferedimages-into-one-image-side-by-side
			Color oldColor = graphics.getColor();
			// fill background
			graphics.setPaint(new Color(244, 244, 244));
			graphics.fillRect(0, 0, imgb1.getWidth(), imgb1.getHeight());

			// draw image
			graphics.setColor(oldColor);
			graphics.drawImage(imgbCenter, null, deltaW / 2, 0);
			graphics.dispose();
			// https://stackoverflow.com/questions/30970005/bufferedimage-to-javafx-image
			finalImage = SwingFXUtils.toFXImage(imgb1, null);
		} else {
			// need to add height
			double expectedHeight = image.getWidth() / perfectRatio;
			int deltaH = (int) (expectedHeight - image.getHeight());
			imgb1 = new BufferedImage(width, deltaH + height + 5, BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics = imgb1.createGraphics();
			// https://stackoverflow.com/questions/20826216/copy-two-bufferedimages-into-one-image-side-by-side
			Color oldColor = graphics.getColor();
			// fill background
			graphics.setPaint(new Color(244, 244, 244));
			graphics.fillRect(0, 0, imgb1.getWidth(), imgb1.getHeight());

			// draw image
			graphics.setColor(oldColor);
			graphics.drawImage(imgbCenter, null, 0, deltaH / 2);
			graphics.dispose();
			// https://stackoverflow.com/questions/30970005/bufferedimage-to-javafx-image
			finalImage = SwingFXUtils.toFXImage(imgb1, null);
		}
		return finalImage;
	}

	// reset to the top left:
	private void reset(ImageView imageView, double width, double height) {
		imageView.setViewport(new Rectangle2D(0, 0, width, height));
	}

	// reset to center position
	private void resetCenter(ImageView imageView, double deltaW, double deltaH, double width, double height) {
		// x y width height
		cumulativeZoom = 1.0;
		zoomSlider.setValue(cumulativeZoom);
		imageView.setViewport(new Rectangle2D(-deltaW / 2, -deltaH / 2, width + deltaW, height + deltaH));
	}

	// shift the viewport of the imageView by the specified delta, clamping so
	// the viewport does not move off the actual image:
	private void shift(ImageView imageView, Point2D delta) {
		Rectangle2D viewport = imageView.getViewport();
		// if delta.getY() > 0 then it's scroll up
		if (continousCheckBox.isSelected()) {
			if (delta.getY() > 0 && (ySlider.getValue() == 0 || ySlider.getValue() == 0.5)) {
				if (rollerPhoto == 0) {
					return;
				}
				previousImage();
				ySlider.setValue(1);
				return;
			} else if (delta.getY() < 0 && (ySlider.getValue() == 1 || ySlider.getValue() == 0.5)) {
				nextImage();
				return;
			}
		}

		double maxX = width + deltaW - viewport.getWidth();
		double maxY = height + deltaH - viewport.getHeight();
		if ((int) maxX == 0 && (int) maxY == 0) {
			// no scale factor done => no drag allowed
			return;
		}
		double minX = clamp(viewport.getMinX() - delta.getX(), -deltaW / 2, maxX);
		double minY = clamp(viewport.getMinY() - delta.getY(), -deltaH / 2, maxY);
		imageView.setViewport(new Rectangle2D(minX, minY, viewport.getWidth(), viewport.getHeight()));
		calculateAndCenter(imageView);
		updateWHSlider();
	}

	private void calculateAndCenter(ImageView imageView) {
		Rectangle2D viewport = imageView.getViewport();
		double totalWidth = viewport.getWidth();
		double totalHeight = viewport.getHeight();
		double difW = totalWidth - width;
		double difH = totalHeight - height;
		if (totalWidth > width) {
			// distribute width difference
			// let height as it is
			imageView.setViewport(new Rectangle2D(-difW / 2, viewport.getMinY(), totalWidth, totalHeight));
			xSlider.setValue(0.5);
		} else {
			// waste white space is not allowed
			// it is better to move port to 00 if x is negative left limit
			// or if maxX > width + deltaW shift rectangle by difference
			if (viewport.getMinX() < 0) {
				imageView.setViewport(new Rectangle2D(0, viewport.getMinY(), totalWidth, totalHeight));
//				xSlider.setValue(0);
			}
			if (viewport.getMaxX() > width) {
				double shiftDif = viewport.getMaxX() - width;
				imageView.setViewport(
						new Rectangle2D(viewport.getMinX() - shiftDif, viewport.getMinY(), totalWidth, totalHeight));
//				xSlider.setValue(1);
			}
		}
		// similar approach for height
		if (totalHeight > height) {
			imageView.setViewport(new Rectangle2D(viewport.getMinX(), -difH / 2, totalWidth, totalHeight));
			ySlider.setValue(0.5);
		} else {
			if (viewport.getMinY() < 0) {
				imageView.setViewport(new Rectangle2D(viewport.getMinX(), 0, totalWidth, totalHeight));
//				ySlider.setValue(0);
			}
			if (viewport.getMaxY() > height) {
				double shiftDif = viewport.getMaxY() - height;
				imageView.setViewport(
						new Rectangle2D(viewport.getMinX(), viewport.getMinY() - shiftDif, totalWidth, totalHeight));
//				ySlider.setValue(1);
			}
		}
	}

	private double clamp(double value, double min, double max) {

		if (value < min) {
			return min;
		}
		if (value > max) {
			return max;
		}
		return value;
	}

	// convert mouse coordinates in the imageView to coordinates in the actual
	// image:
	private Point2D imageViewToImage(ImageView imageView, Point2D imageViewCoordinates) {
		double xProportion = imageViewCoordinates.getX() / imageView.getBoundsInLocal().getWidth();
		double yProportion = imageViewCoordinates.getY() / imageView.getBoundsInLocal().getHeight();

		Rectangle2D viewport = imageView.getViewport();
		return new Point2D(viewport.getMinX() + xProportion * viewport.getWidth(),
				viewport.getMinY() + yProportion * viewport.getHeight());
	}

	private Integer rollerPhoto = 0;

	@FXML
	private void nextImage() {
		if (rollerPhoto < ImgResources.size() - 1) {
			rollerPhoto = rollerPhoto + 1;
		} else {
			return;
		}
		previousButton.setDisable(false);
		if (rollerPhoto == ImgResources.size() - 1) {
			nextButton.setDisable(true);
		}
		changeImage(ImgResources.get(rollerPhoto));

	}

	@FXML
	private void previousImage() {
		if (rollerPhoto > 0) {
			rollerPhoto = rollerPhoto - 1;
		} else {
			return;
		}
		nextButton.setDisable(false);
		if (rollerPhoto == 0) {
			previousButton.setDisable(true);
		}
		changeImage(ImgResources.get(rollerPhoto));
	}

	@FXML
	private void renameImage() {
		final File curImage = ImgResources.get(rollerPhoto);
		if (nameImage.getText().isEmpty()) {
			DialogHelper.showAlert(AlertType.ERROR, "Rename", "Name cannot be empty", "");
			nameImage.setText(curImage.getName());
			return;
		}
		Path oldPath = curImage.toPath();
		Path newPath = null;
		Path oldconflictPath = null;
		Path newconflictPath = null;

		if (nameImage.getText() != curImage.getName()) {
			try {
				newPath = FileHelper.RenameHelper(oldPath, nameImage.getText());
			} catch (FileAlreadyExistsException e) {
				boolean ans = DialogHelper.showConfirmationDialog("Rename Conflict Error",
						"File Already exist with such name!!",
						"Do you want to rename the other file? \n So we can retry renaming this File..");
				if (ans) {
					oldconflictPath = curImage.toPath().resolveSibling(nameImage.getText());
					newconflictPath = FileHelper.rename(oldconflictPath, false);
					try {
						newPath = FileHelper.RenameHelper(oldPath, nameImage.getText());
					} catch (IOException e1) {
						nameImage.setText(curImage.getName());
						DialogHelper.showException(e);
					}
				} else {
					nameImage.setText(curImage.getName());
				}

			} catch (IOException e) {
				nameImage.setText(curImage.getName());
				DialogHelper.showException(e);
			}
		}
		boolean isOtherRename = oldconflictPath != null && newconflictPath != null;
		boolean isCurrentRename = newPath != null && oldPath != null;
		boolean doWriteMapDetails = false;
		List<String> tempMapDetails;
		// remember that options 0 in map details is name to be changed
		if (isOtherRename) {
			// updating list of images sources in photo explorer
			ImgResources.set(ImgResources.indexOf(oldconflictPath.toFile()), newconflictPath.toFile());

			// conserving tracker data
			if (mFileTracker.isTracked()) {
				tempMapDetails = mFileTracker.getMapDetails().get(oldconflictPath.toFile().getName());
				tempMapDetails.set(0, newconflictPath.toFile().getName());
				mFileTracker.getMapDetails().put(newconflictPath.toFile().getName(), tempMapDetails);
				doWriteMapDetails = true;
			}
		}
		if (isCurrentRename) {
			ImgResources.set(ImgResources.indexOf(oldPath.toFile()), newPath.toFile());
			if (mFileTracker.isTracked()) {
				tempMapDetails = mFileTracker.getMapDetails().get(oldPath.toFile().getName());
				tempMapDetails.set(0, newPath.toFile().getName());
				mFileTracker.getMapDetails().put(newPath.toFile().getName(), tempMapDetails);
				// not not clearing conflict logs useless programming
				if (!isOtherRename) {
					mFileTracker.getMapDetails().remove(oldPath.toFile().getName());
				}
			}
			doWriteMapDetails = true;
		}

		final Path path = newPath;
		if (isCurrentRename) {
			changeImage(path.toFile());
		}
		if (doWriteMapDetails) {
			mFileTracker.writeMapDir(mFileTracker.getWorkingDir(), false);
		}
	}

	private boolean untrackedBehavior() {
		boolean ans;
		ans = mFileTracker.getAns();
		if (ans) {
			mFileTracker.NewOutFolder(mFileTracker.getWorkingDir());
			mFileTracker.resolveConflict();
		}
		return ans;
	}

	@FXML
	private void addNoteImage() {
		if (!mFileTracker.isTracked() && !untrackedBehavior()) {
			return;
		}
		String key = ImgResources.get(rollerPhoto).getName();
		mFileTracker.setTooltipText(key, noteInput.getText());
		imageView.requestFocus();
	}

	public void updateMarkSeen(boolean seen) {
		markSeen.getStyleClass().removeAll("info", "success");
		if (seen) {
			markSeen.getStyleClass().add("success");
			markSeen.setText("S");
			markSeen.setSelected(true);
		} else {
			markSeen.setSelected(false);
			markSeen.getStyleClass().add("info");
			markSeen.setText("U");
		}
		imageView.requestFocus();
	}

	@FXML
	private void toggleSeen() {

		if (!mFileTracker.isTracked() && !untrackedBehavior()) {
			return;
		}
		String key = ImgResources.get(rollerPhoto).getName();
		updateMarkSeen(!mFileTracker.isSeen(key));
		mFileTracker.toggleSingleSeenItem(key, null);
		mFileTracker.writeMapDir(mFileTracker.getWorkingDir(), false, false);
	}

	@FXML
	private void revealInExplorer() {
		StringHelper.RunRuntimeProcess(
				new String[] { "explorer.exe", "/select,", ImgResources.get(rollerPhoto).toString() });
	}

	@FXML
	private void deleteIMG() {
		File img = ImgResources.get(rollerPhoto);
		boolean ans = DialogHelper.showConfirmationDialog("Delete " + img.getName(),
				"Are you Sure you want to delete" + img.getName() + " ?", "");
		if (ans) {
			img.delete();
			imageView.setImage(null);
			locationField.clear();
			nameImage.clear();
			noteInput.clear();
			ImgResources.remove(img);
			if (ImgResources.size() != 0) {
				if (rollerPhoto < ImgResources.size() - 1) {
					nextImage();
				} else if (rollerPhoto > 0) {
					previousImage();
				}
			}
		}
	}

}
