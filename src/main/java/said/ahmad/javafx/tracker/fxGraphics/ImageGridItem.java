package said.ahmad.javafx.tracker.fxGraphics;

import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.PixelGrabber;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.xml.ws.Holder;

import org.jetbrains.annotations.Nullable;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Pair;
import said.ahmad.javafx.tracker.app.DialogHelper;
import said.ahmad.javafx.tracker.app.Main;
import said.ahmad.javafx.tracker.app.ResourcesHelper;
import said.ahmad.javafx.tracker.app.StringHelper;
import said.ahmad.javafx.tracker.app.look.ContextMenuLook;
import said.ahmad.javafx.tracker.app.look.IconLoader;
import said.ahmad.javafx.tracker.app.look.IconLoader.ICON_TYPE;
import said.ahmad.javafx.tracker.datatype.ImagePosition;
import said.ahmad.javafx.tracker.system.call.RunMenu;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.tracker.system.file.PathLayerHelper;
import said.ahmad.javafx.tracker.system.file.local.FilePathLayer;
import said.ahmad.javafx.tracker.system.operation.FileHelper;
import said.ahmad.javafx.util.CallBackToDo;
import said.ahmad.javafx.util.CallBackVoid;

/**
 * To Add this imageView to a parent node use {@link #getImageAllPane()}
 *
 * @author Ahmad Said
 *
 */
public class ImageGridItem extends ImageView {
	public static HashSet<String> ArrayIMGExt = new HashSet<String>(
			Arrays.asList("PNG", "GIF", "JPG", "JPS", "MPO", "BMP", "WEBMP", "JPEG"));

	private static boolean MantaingPos = true;
	private static boolean AutoFitWidth = false;
	private static boolean DoubleClickBehavior = true;

	private boolean stretchImage = false;

	private static final Image ERROR_IMAGE = new Image(ResourcesHelper.getResourceAsStream("/img/image_not_Found.png"));
	private static final Image INITIAL_IMAGE = new Image(ResourcesHelper.getResourceAsStream("/img/photo_Icon.png"));

	private Stage parentStage = null;
	private static Stage tempStage = new Stage();

	private VBox allPanesVBox;
	private HBox toolBoxHBox;
	private Pane imagePane;

	private ImageGridItem alreadyShownGridImage;
	private EventHandler<Event> onFinishLoading = null;
	private CallBackToDo onSetUpImage = null;
	private CallBackVoid<PathLayer> onDeleteAction = null;
	/**
	 * @see #setPartnerActionGridItems(List)
	 * @see #getAffectedImageGridItem()
	 */
	@Nullable
	private List<ImageGridItem> partnerActionGridItems;

	/**
	 * Tool box stuff
	 */
	private SplitMenuButton cropMenuButton;
	private MenuItem restoreCropped;
	private MenuItem selectAllImage;
	private MenuItem saveAsCrop;
	private MenuItem saveToClipBoard;
	private MenuItem cancelCrop;
	private Button rotateRightIMG;
	private Button deleteIMG;
	private Button dragThisPhoto;
	private Button revealLocation;
	/**
	 * Name and note stuff
	 */
	private HBox nameHBox;
	private Label nameLabel;
	private HBox noteHBox;
	private Label noteLabel;

	// Slider and position stuff
	private ImagePosition curImgPosition;
	private Slider ySlider;
	private ChangeListener<Number> yChangeListener;
	private HBox ySliderContainer;
	private Slider xSlider;
	private ChangeListener<Number> xChangeListener;
	private Slider zoomSlider;
	private ChangeListener<Number> zoomChangeListener;
	private Label zoomLevelLabel;
	private Double cumulativeZoom = 1.0;
	private HBox zoomSliderContainer;

	// Loading image stuff
	private VBox loadingPane;
	private Label statusLabel;
	private ProgressBar progressBar;
	private ProgressIndicator progressIndicator;

	boolean isInCropMode = false;
	private Image lastBackUpCroppedImage = null;
	private PathLayer lastBackUpCroppedFile = null;
	private RubberBandSelection rubberBandSelection = null;

	// for a faster switch we don't need to define method each time
	// so we define private variable that change on image change
	private Dimension2D originalImageDim;
	// state of image if fully loaded
	private boolean isFullyLoaded;
	/**
	 * if image was automatically fully loaded since it was put this set to true
	 * after call of {@link #doLoadFullImage()}
	 */
	private boolean didImageChanged;

	private double width;
	private double height;
	private double deltaW;
	private double deltaH;
	@Nullable
	private PathLayer imageFile;

	// zoom idea source : https://gist.github.com/james-d/ce5ec1fd44ce6c64e81a
	private static int MIN_PIXELS = 100;

	/**
	 * To change image inside this graphics use
	 * {@link #setImageAndSetup(File, ImagePosition)} <br>
	 * To add this graphic with full feature add to view {@link #getImageAllPane()}
	 *
	 *
	 * @param alreadyShownGridImage Used to calculate and center image in imagePane
	 *                              and getWidth() and getHeight() stuff, as when
	 *                              creating a new imageGrid after showing the stage
	 *                              its width doesn't get calculated.<BR>
	 *                              So at least let one grid item be created in the
	 *                              view before showing the stage and send it as
	 *                              parameter for going on created ImageGridItem
	 *
	 * @param parentStage           can be null
	 */

	public ImageGridItem(Stage parentStage, ImageGridItem alreadyShownGridImage) {
		super();
		if (alreadyShownGridImage == null) {
			alreadyShownGridImage = this;
		}
		this.alreadyShownGridImage = alreadyShownGridImage;

		imagePane = new Pane();
		allPanesVBox = new VBox();

		imagePane.getChildren().add(this);
		initializeImageView();
		initializeListeners();
		initializeSliders();
		initializetoolBox();
		initialzieLoadingPane();

		VBox.setVgrow(imagePane, Priority.ALWAYS);
		HBox.setHgrow(imagePane, Priority.ALWAYS);
		allPanesVBox.setAlignment(Pos.CENTER);
		initalizeVisualNames();

		// The gap at top of Pane is because xSlider is hidden
		allPanesVBox.getChildren().addAll(xSlider, nameHBox, ySliderContainer, noteHBox, zoomSliderContainer,
				toolBoxHBox);

		if (parentStage == null) {
			parentStage = tempStage;
		}
		this.parentStage = parentStage;
		setDisable(true, false);
	}

	private void initalizeVisualNames() {
		nameHBox = new HBox();
		nameHBox.setAlignment(Pos.CENTER);

		nameLabel = new Label();
		nameLabel.setTextAlignment(TextAlignment.CENTER);
		nameLabel.setStyle("-fx-border-radius:5;-fx-border-width:2;-fx-border-color: #0097A7;-fx-font-weight:bold");
		nameHBox.getChildren().add(nameLabel);

		noteHBox = new HBox();
		noteHBox.setAlignment(Pos.CENTER);

		noteLabel = new Label();
		noteLabel.setTextAlignment(TextAlignment.CENTER);
		noteLabel.setStyle("-fx-border-radius:5;-fx-border-width:2;-fx-border-color: #7F00FF;-fx-font-weight:bold;"
				+ "-fx-text-fill:#7F00FF;");
		noteHBox.getChildren().add(noteLabel);
	}

	/**
	 * will disable All pane including imageView with toolBox and sliders
	 *
	 * @param disableStatus
	 * @param withErrorImage only matter if disableStatus is set to true
	 * @return
	 */
	public ImageGridItem setDisable(boolean disableStatus, boolean withErrorImage) {
		if (disableStatus == isDisable()) {
			return this;
		}
		allPanesVBox.setDisable(disableStatus);
		setDisable(disableStatus);
		toolBoxHBox.setDisable(disableStatus);
		if (disableStatus) {
			imageFile = null;
			setNoteLabel("");
			setSeen(false);
			if (withErrorImage) {
				nameLabel.setText("!! Error Loading Image !!");
				setImage(ERROR_IMAGE);
			} else {
				nameLabel.setText("");
				setImage(INITIAL_IMAGE);
			}
		}
		return this;
	}

	private void initialzieLoadingPane() {
		loadingPane = new VBox();
		statusLabel = new Label();

		progressBar = new ProgressBar();
		progressBar.setMaxWidth(Double.MAX_VALUE);

		progressIndicator = new ProgressIndicator();
		progressIndicator.progressProperty().bind(progressBar.progressProperty());

		loadingPane.setAlignment(Pos.CENTER);
		loadingPane.getChildren().addAll(statusLabel, progressIndicator, progressBar);
	}

	private void initializetoolBox() {
		toolBoxHBox = new HBox();
		// Crop stuff
		cropMenuButton = new SplitMenuButton();
		cropMenuButton.setGraphic(IconLoader.getIconImageView(ICON_TYPE.CROP, true, 25, 25));
		cropMenuButton.setText("Crop");
		cropMenuButton.setOnAction(e -> cropPhotoMode());
		cropMenuButton.setStyle("-fx-font-weight:bold");

		restoreCropped = new MenuItem("Restore Previous");
		restoreCropped.setOnAction(e -> restoreCropped());

		selectAllImage = new MenuItem("Select All");
		selectAllImage.setOnAction(e -> selectAllImage());

		saveAsCrop = new MenuItem("Save As");
		saveAsCrop.setOnAction(e -> saveAsCrop());

		saveToClipBoard = new MenuItem("Copy To ClipBoard");
		saveToClipBoard.setOnAction(e -> saveToClipBoard());

		cancelCrop = new MenuItem("Cancel");
		cancelCrop.setOnAction(e -> cancelCrop());

		cropMenuButton.getItems().addAll(restoreCropped, selectAllImage, saveAsCrop, saveToClipBoard, cancelCrop);

		cropMenuButton.getItems().forEach(e -> e.setVisible(false));
		restoreCropped.setVisible(true);
		restoreCropped.setDisable(true);

		// other stuff
		rotateRightIMG = new Button("Rotate");
		rotateRightIMG.setGraphic(IconLoader.getIconImageView(ICON_TYPE.ROTATE_RIGHT));
		rotateRightIMG.setOnAction(e -> rotateRightIMG());
		rotateRightIMG.setStyle("-fx-font-weight:bold");
		rotateRightIMG.getStyleClass().addAll("middle");

		deleteIMG = new Button("Delete - D");
		deleteIMG.setGraphic(IconLoader.getIconImageView(ICON_TYPE.DELETE, true, 25, 25));
		deleteIMG.setOnAction(e -> deleteIMG());
		deleteIMG.setStyle("-fx-font-weight:bold");
		deleteIMG.getStyleClass().addAll("middle");

		dragThisPhoto = new Button("Drag");
		dragThisPhoto.setGraphic(IconLoader.getIconImageView(ICON_TYPE.DRAG, true, 25, 25));
		dragThisPhoto.setOnDragDetected(e -> onDragDetected(e));
		dragThisPhoto.setStyle("-fx-font-weight:bold");
		dragThisPhoto.getStyleClass().addAll("middle");

		revealLocation = new Button("Explorer - E");
		revealLocation.setGraphic(IconLoader.getIconImageView(ICON_TYPE.TRACKER, true, 25, 25));
		revealLocation.setOnAction(e -> revealInExplorer());
		revealLocation.setStyle("-fx-font-weight:bold");
		revealLocation.getStyleClass().addAll("last");

		toolBoxHBox.getChildren().addAll(cropMenuButton, rotateRightIMG, deleteIMG, dragThisPhoto, revealLocation);
		toolBoxHBox.setAlignment(Pos.CENTER);
	}

	private void onDragDetected(MouseEvent e) {
		Dragboard db = dragThisPhoto.startDragAndDrop(TransferMode.ANY);
		ClipboardContent cb = new ClipboardContent();
		List<File> selectedFiles = new ArrayList<>();
		if (imageFile.isLocal()) {
			selectedFiles.add(imageFile.toFileIfLocal());
			cb.putFiles(selectedFiles);
		} else {
			cb.putString(PathLayerHelper.ON_DROP_URI_OPERATION_KEY + "\n" + imageFile.toURI());
		}
		db.setContent(cb);
	}

	public void showContextMenu() {
		if (!isInCropMode) {
			showContextMenuCustom(getContextMenuList());
		} else {
			showContextMenuCustom(getContextMenuListCropMode());
		}
	}

	public void showContextMenuCustom(List<MenuItem> menuItems) {
		Point mouse = MouseInfo.getPointerInfo().getLocation();
		ContextMenu mn = new ContextMenu();
		mn.getItems().addAll(menuItems);
		mn.show(parentStage, mouse.getX(), mouse.getY());
	}

	private ArrayList<MenuItem> getContextMenuList() {
		ArrayList<MenuItem> allMenu = new ArrayList<>();

		MenuItem fitWidth = new MenuItem("Zoom To Fit");
		fitWidth.setGraphic(IconLoader.getIconImageView(ICON_TYPE.FIT));
		fitWidth.setOnAction(e -> fitWidth());
		MenuItem resetCenter = new MenuItem("Reset Center");
		resetCenter.setGraphic(IconLoader.getIconImageView(ICON_TYPE.CENTRALIZE));
		resetCenter.setOnAction(e -> resetCenter());

		MenuItem copyImage = new MenuItem("Copy as Image");
		copyImage.setGraphic(IconLoader.getIconImageView(ICON_TYPE.COPY));
		copyImage.setOnAction(e -> copyImageToClipBoard());

		MenuItem crop = new MenuItem("Crop");
		crop.setGraphic(IconLoader.getIconImageView(ICON_TYPE.CROP));
		crop.setOnAction(e -> cropPhotoMode());

		@Nullable
		MenuItem restoreCrop = null;
		if (!restoreCropped.isDisable()) {
			restoreCrop = new MenuItem("Undo Last Crop");
			restoreCrop.setGraphic(IconLoader.getIconImageView(ICON_TYPE.UNDO));
			restoreCrop.setOnAction(e -> restoreCropped.fire());
		}

		MenuItem rotate = new MenuItem("Rotate");
		rotate.setGraphic(IconLoader.getIconImageView(ICON_TYPE.ROTATE_RIGHT));
		rotate.setOnAction(e -> rotateRightIMG());

		MenuItem delete = new MenuItem("Delete");
		delete.setGraphic(IconLoader.getIconImageView(ICON_TYPE.DELETE));
		delete.setOnAction(e -> deleteIMG());

		MenuItem reveal = new MenuItem("Reveal In Explorer");
		reveal.setGraphic(IconLoader.getIconImageView(ICON_TYPE.TRACKER));
		reveal.setOnAction(e -> revealInExplorer());

		Menu view = new Menu("View");
		view.setGraphic(IconLoader.getIconImageView(ICON_TYPE.HIDDEN));

		MenuItem toolBox = new MenuItem("Tool Box");
		toolBox.setGraphic(IconLoader.getIconImageView(ICON_TYPE.TOOL_BOX));
		toolBox.setOnAction(e -> toggleShowToolBox());

		MenuItem name = new MenuItem("Name");
		name.setGraphic(IconLoader.getIconImageView(ICON_TYPE.FILE));
		name.setOnAction(e -> toggleShowNameLabel());

		MenuItem note = new MenuItem("Note");
		note.setGraphic(IconLoader.getIconImageView(ICON_TYPE.NOTE));
		note.setOnAction(e -> toggleShowNoteLabel());

		MenuItem zoomSlider = new MenuItem("Zoom Slider");
		zoomSlider.setGraphic(IconLoader.getIconImageView(ICON_TYPE.ZOOM));
		zoomSlider.setOnAction(e -> toggleShowZoomSlider());

		view.getItems().addAll(toolBox, name, note, zoomSlider);

		allMenu.add(resetCenter);
		allMenu.add(fitWidth);
		allMenu.add(copyImage);
		allMenu.add(crop);
		if (restoreCrop != null) {
			allMenu.add(restoreCrop);
		}
		allMenu.add(rotate);
		allMenu.add(delete);
		allMenu.add(view);
		allMenu.add(reveal);

		if (imageFile.isLocal()) {
			// System Context Menu
			MenuItem systemContextMenu = new MenuItem("More");
			systemContextMenu.setOnAction(e -> RunMenu.showMenu(Arrays.asList(imageFile.toFileIfLocal())));
			systemContextMenu.setGraphic(new ImageView(ContextMenuLook.systemIcon));
			allMenu.add(systemContextMenu);
		}
		return allMenu;
	}

	private List<MenuItem> getContextMenuListCropMode() {
		MenuItem performCrop = new MenuItem("Crop Selection");
		performCrop.setGraphic(IconLoader.getIconImageView(ICON_TYPE.CROP));
		performCrop.setOnAction(e -> cropMenuButton.fire());

		MenuItem selectAllImage = new MenuItem("Select All");
		selectAllImage.setGraphic(IconLoader.getIconImageView(ICON_TYPE.SELECT_ALL));
		selectAllImage.setOnAction(e -> this.selectAllImage.fire());

		MenuItem saveAsCrop = new MenuItem("Save As");
		saveAsCrop.setGraphic(IconLoader.getIconImageView(ICON_TYPE.SAVE));
		saveAsCrop.setOnAction(e -> this.saveAsCrop.fire());

		MenuItem saveToClipBoard = new MenuItem("Copy To ClipBoard");
		saveToClipBoard.setGraphic(IconLoader.getIconImageView(ICON_TYPE.CLIPBOARD));
		saveToClipBoard.setOnAction(e -> this.saveToClipBoard.fire());

		MenuItem cancelCrop = new MenuItem("Exit Crop Mode");
		cancelCrop.setGraphic(IconLoader.getIconImageView(ICON_TYPE.CANCEL));
		cancelCrop.setOnAction(e -> this.cancelCrop.fire());
		return Arrays.asList(performCrop, selectAllImage, saveAsCrop, saveToClipBoard, cancelCrop);
	}

	/**
	 * check {@link #isFullyLoaded} for load status
	 *
	 * @return if image really need to be loaded
	 */
	private boolean doLoadFullImage() {
		boolean doNeedLoad = !isFullyLoaded;
		if (!isFullyLoaded) {
			setImageAndSetup(imageFile, true, false, curImgPosition.clone());
			didImageChanged = true;
		}
		return doNeedLoad;
	}

	/**
	 * if you desire to cache image using this function also do cache
	 * {@link #isFullyLoaded} value with it so It can distinguish whenever to load
	 * image again for a nice zoom
	 *
	 * @param file
	 * @param positionImg can be null
	 * @return the loaded image as {@link Pair#getKey()} of the pair <br>
	 *         and fully loaded or not as {@link Pair#getValue()}
	 */
	public Pair<Image, Boolean> setImageAndSetup(PathLayer file, ImagePosition positionImg) {
		return setImageAndSetup(file.toURI().toString(), false, true, positionImg, file);
	}

	/**
	 * if you desire to cache image using this function also do cache
	 * {@link #isFullyLoaded} value with it so It can distinguish whenever to load
	 * image again for a nice zoom
	 *
	 * @param file
	 * @param loadFullImage
	 * @param showLoadingBar
	 * @param positionImg
	 * @return the loaded image as {@link Pair#getKey()} of the pair <br>
	 *         and fully loaded or not as {@link Pair#getValue()}
	 */
	public Pair<Image, Boolean> setImageAndSetup(PathLayer file, boolean loadFullImage, boolean showLoadingBar,
			ImagePosition positionImg) {
		return setImageAndSetup(file.toURI().toString(), loadFullImage, showLoadingBar, positionImg, file);
	}

	// if setUpimage is called multiple times consecutively
	// ensure that only last image is shown up and do not add
	// loading bar multiple times
	private long displayInThisCall = Long.MIN_VALUE;

	/**
	 * if you desire to cache image using this function also do cache
	 * {@link #isFullyLoaded} value with it so It can distinguish whenever to load
	 * image again for a nice zoom
	 *
	 * @param url
	 * @param positionImg can be null
	 *
	 * @return the loaded image as {@link Pair#getKey()} of the pair <br>
	 *         and fully loaded or not as {@link Pair#getValue()}
	 */
	public Pair<Image, Boolean> setImageAndSetup(String url, boolean loadFullImage, boolean showLoadingBar,
			ImagePosition positionImg, PathLayer imageFileFromUrlCanBeNull) {
		// https://gist.github.com/jewelsea/2556122
		// load an image in the background.
		isFullyLoaded = false;
		int requestWidth = 0;
		int requestHeight = 0;

		if (imageFileFromUrlCanBeNull == null) {
			try {
				imageFileFromUrlCanBeNull = PathLayerHelper.parseURI(url);
			} catch (URISyntaxException e) {
				e.printStackTrace();
				return new Pair<Image, Boolean>(null, null);
			}
		}
		try {
			try (ImageInputStream in = ImageIO
					.createImageInputStream(imageFileFromUrlCanBeNull.toFileIfLocalOrAsCopy(true).getFile())) {
				final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
				if (readers.hasNext()) {
					ImageReader reader = readers.next();
					try {
						reader.setInput(in);
						requestWidth = reader.getWidth(0);
						requestHeight = reader.getHeight(0);
						originalImageDim = new Dimension2D(requestWidth, requestHeight);
					} finally {
						reader.dispose();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		// do load full in 2 case
		// if manually asked for
		// or if fit width is selected
		if (!loadFullImage && !AutoFitWidth) {
			// TODO resolve later if imagePane have width 0
			requestWidth = Math.min((int) alreadyShownGridImage.imagePane.getWidth() + 500, requestWidth);
			requestHeight = Math.min((int) alreadyShownGridImage.imagePane.getHeight() + 500, requestHeight);
		}
		if (originalImageDim != null && requestHeight != originalImageDim.getHeight()
				|| requestWidth != originalImageDim.getWidth()) {
			isFullyLoaded = false;
		} else {
			isFullyLoaded = true;
		}
		Image newImage = new Image(url, requestWidth, requestHeight, true, true, true);
		setImageAndSetup(newImage, imageFileFromUrlCanBeNull, isFullyLoaded, positionImg, showLoadingBar);
		return new Pair<Image, Boolean>(newImage, isFullyLoaded);
	}

	/**
	 * if you desire to cache image using this function also do cache
	 * {@link #isFullyLoaded} value with it so It can distinguish whenever to load
	 * image again for a nice zoom
	 *
	 * @param url
	 * @param positionImg
	 * @return the loaded image as {@link Pair#getKey()} of the pair <br>
	 *         and fully loaded or not as {@link Pair#getValue()}
	 */
	public Pair<Image, Boolean> setImageAndSetup(String url, ImagePosition positionImg) {
		return setImageAndSetup(url, false, true, positionImg, null);
	}

	/**
	 * EasyAccessor to show image with loading bar
	 *
	 * @param image       normally will be cached image
	 * @param imageFile   image file
	 * @param positionImg
	 */
	public void setImageAndSetup(Image image, PathLayer imageFile, boolean isFullyLoaded, ImagePosition positionImg) {
		setImageAndSetup(image, imageFile, isFullyLoaded, positionImg, true);
	}

	/**
	 * The final method to be called when setting image
	 *
	 * @param image
	 * @param imageFile
	 * @param positionImg
	 * @param showLoadingBar
	 */
	public void setImageAndSetup(Image newImage, PathLayer imageFile, boolean isFullyLoaded, ImagePosition positionImg,
			boolean showLoadingBar) {
		// only changed after do load full image or when rotating/croping image
		didImageChanged = false;
		this.isFullyLoaded = isFullyLoaded;
		if (newImage.progressProperty().get() == 1) {
			finalizeSetUpImage(newImage, imageFile, positionImg);
		} else {
			long currentCall = ++displayInThisCall;
			if (showLoadingBar) {

				if (imagePane.getChildren().contains(loadingPane)) {
					// mean there is another call of this function which is going on in background
					// loading image
					progressBar.progressProperty().unbind();
				} else {
					// Normal behavior
					loadingPane.setLayoutX(alreadyShownGridImage.imagePane.getWidth() / 2);
					loadingPane.setLayoutY(alreadyShownGridImage.imagePane.getHeight() / 2);
					imagePane.getChildren().add(loadingPane);
				}
				// reset the error text.
				statusLabel.setText("Loading image . . .");
				statusLabel.setStyle("-fx-text-fill: silver;");

				// track the image's error property.
				newImage.errorProperty().addListener(new ChangeListener<Boolean>() {
					@Override
					public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
							Boolean imageError) {
						if (imageError && currentCall == displayInThisCall) {
							statusLabel.setText("Oh-oh there was an error loading: \n" + imageFile);
							statusLabel.setStyle("-fx-text-fill: firebrick;");
							imagePane.getChildren().remove(loadingPane);
							setDisable(true, true);
						}
					}
				});
				// track the image's loading progress.
				progressBar.progressProperty().bind(newImage.progressProperty());
				newImage.progressProperty().addListener(new ChangeListener<Number>() {
					@Override
					public void changed(ObservableValue<? extends Number> observable, Number oldValue,
							Number progress) {
						if ((Double) progress == 1.0 && !newImage.isError() && currentCall == displayInThisCall) {
							statusLabel.setText("Loading complete");
							statusLabel.setStyle("-fx-text-fill: forestgreen;");
							imagePane.getChildren().remove(loadingPane);
							finalizeSetUpImage(newImage, imageFile, positionImg);
							if (onFinishLoading != null) {
								onFinishLoading.handle(null);
							}
						}
					}
				});
			} else {
				newImage.progressProperty().addListener(new ChangeListener<Number>() {

					@Override
					public void changed(ObservableValue<? extends Number> observable, Number oldValue,
							Number progress) {
						if ((Double) progress == 1.0 && !newImage.isError()) {
							finalizeSetUpImage(newImage, imageFile, positionImg);
							if (onFinishLoading != null) {
								onFinishLoading.handle(null);
							}
						}
					}

				});
			}
		}
	}

	public void reloadImageLocation() {
		finalizeSetUpImage(getImage(), imageFile, curImgPosition);
	}

	/**
	 *
	 * @param image       A fully loaded image
	 * @param imageFile
	 * @param positionImg
	 */
	private void finalizeSetUpImage(Image image, PathLayer imageFile, ImagePosition positionImg) {
		this.imageFile = imageFile;
		nameLabel.setText(imageFile.getName());
		setImage(image);
		width = image.getWidth();
		height = image.getHeight();
		if (!stretchImage) {
			deltaW = getDeltaPerfectWidth();
			deltaH = getDeltaPerfectHeight();
		} else {
			deltaW = 0;
			deltaH = 0;
		}

		setDisable(false, false);

		if (rubberBandSelection != null) {
			rubberBandSelection.limitRectToImageView();
		}
		resetCenterThisOnly();
		if (MantaingPos && positionImg != null) {
			setPosition(positionImg);
		} else if (AutoFitWidth) {
			fitWidthThisOnly();
		}
		if (onSetUpImage != null) {
			onSetUpImage.call();
		}
	}

	public void setPosition(ImagePosition positionImg) {
		zoomSlider_setValue(positionImg.getZoomLvl());
		xSlider_setValue(positionImg.getxLvl());
		ySlider_setValue(positionImg.getyLvl());
	}

	private void initializeImageView() {
		fitWidthProperty().bind(imagePane.widthProperty());
		fitHeightProperty().bind(imagePane.heightProperty());
		setPreserveRatio(false); // done manually due to zoom constraints
		ObjectProperty<Point2D> mouseDown = new SimpleObjectProperty<>();
		setFocusTraversable(true);
		requestFocus();
		getImageAllPane().setOnKeyPressed(key -> {
			if (!hasValidImageFile()) {
				return;
			}
			switch (key.getCode()) {
			case UP:
			case W:
				shift(new Point2D(0, height / 10));
				break;
			case DOWN:
			case S:
				shift(new Point2D(0, -height / 10));
				break;
			case Q:
				fitWidth();
				break;
			case CONTEXT_MENU:
				showContextMenu();
				break;
			case E:
			case R:
				StringHelper.RunRuntimeProcess(new String[] { "explorer.exe", "/select,", imageFile.toString() });
				break;
			case DELETE:
				deleteIMG();
				break;
			default:
				break;
			}
		});
		imagePane.setOnMousePressed(e -> {
			requestFocus();
			Point2D mousePress = imageViewToImage(new Point2D(e.getX(), e.getY()));
			mouseDown.set(mousePress);
			setCursor(Cursor.CLOSED_HAND);
		});

		imagePane.setOnMouseDragged(e -> {
			if (isInCropMode) {
				return;
			}
			Point2D dragPoint = imageViewToImage(new Point2D(e.getX(), e.getY()));
			shift(dragPoint.subtract(mouseDown.get()));
			mouseDown.set(imageViewToImage(new Point2D(e.getX(), e.getY())));
		});
		setCursor(Cursor.OPEN_HAND);
		imagePane.setOnMouseReleased(e -> {
			setCursor(Cursor.OPEN_HAND);
		});

		imagePane.setOnScroll(e -> {
			if (!hasValidImageFile()) {
				return;
			}
			if (!e.isControlDown()) {
				shift(new Point2D(0, e.getDeltaY() * height / 120 / 5));
				return;
			}
			double delta = -e.getDeltaY();
			Rectangle2D viewport = getViewport();

			double scale = clamp(Math.pow(1.005, delta),

					// don't scale so we're zoomed in to fewer than MIN_PIXELS in any direction:
					Math.min(MIN_PIXELS / viewport.getWidth(), MIN_PIXELS / viewport.getHeight()),

					// don't scale so that we're bigger than image dimensions:
					Math.max((width + deltaW) / viewport.getWidth(), (height + deltaH) / viewport.getHeight())

			);
			cumulativeZoom /= scale;
			zoomSlider_setValue(cumulativeZoom);
			Point2D mouse = imageViewToImage(new Point2D(e.getX(), e.getY()));

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

			setViewport(new Rectangle2D(newMinX, newMinY, newWidth, newHeight));
			calculateAndCenter();
			updateWHSlider();
		});
		imagePane.setOnContextMenuRequested(e1 -> showContextMenu());
		setOnMouseClicked(doubleClickBehavior);
	}

	private EventHandler<MouseEvent> doubleClickBehavior = e -> {
		if (e.getClickCount() % 2 == 0 && isDoubleClickBehavior()) {
			Rectangle2D viewPort = getViewport();
			double maxX = width + deltaW - viewPort.getWidth();
			double maxY = height + deltaH - viewPort.getHeight();
			if ((int) maxX != 0 || (int) maxY != 0) {
				// there is a scale factor => reset view
				resetCenterThisOnly();
				return;
			}
			double perfectRatio = alreadyShownGridImage.imagePane.getWidth()
					/ alreadyShownGridImage.imagePane.getHeight();
			if (viewPort.getWidth() > width) {
				// perfect fit width zoom
				double maxHeight = width / perfectRatio;
				double newMinY = e.getY() / alreadyShownGridImage.imagePane.getHeight() * height - maxHeight / 2;
				if (newMinY < 0) {
					newMinY = 0;
				}
				cumulativeZoom = (width + deltaW) / width;
				setViewport(new Rectangle2D(0, newMinY, width, maxHeight));
				updateWHSlider();
				zoomSlider_setValue(cumulativeZoom);
				return;
			}
			if (viewPort.getHeight() > height) {
				// perfect fit height zoom
				double maxWidth = height * perfectRatio;
				double newMinX = e.getX() / alreadyShownGridImage.imagePane.getWidth() * width - maxWidth / 2;
				if (newMinX < 0) {
					newMinX = 0;
				}
				cumulativeZoom = (height + deltaH) / height;
				setViewport(new Rectangle2D(newMinX, 0, maxWidth, height));
				updateWHSlider();
				zoomSlider_setValue(cumulativeZoom);
				return;
			}
		}
	};

	// convert mouse coordinates in the imageView to coordinates in the actual
	// image:
	private Point2D imageViewToImage(Point2D imageViewCoordinates) {
		double xProportion = imageViewCoordinates.getX() / getBoundsInLocal().getWidth();
		double yProportion = imageViewCoordinates.getY() / getBoundsInLocal().getHeight();

		Rectangle2D viewport = getViewport();
		return new Point2D(viewport.getMinX() + xProportion * viewport.getWidth(),
				viewport.getMinY() + yProportion * viewport.getHeight());
	}

	/** Fit width for all {@link #getAffectedImageWithPartner()} */
	public void fitWidth() {
		boolean oldAutoFitWidth = isAutoFitWidth();
		setAutoFitWidth(true);
		for (ImageGridItem item : getAffectedImageWithPartner()) {
			item.fitWidthThisOnly();
		}
		setAutoFitWidth(oldAutoFitWidth);
	}

	public void fitWidthThisOnly() {
		if (!hasValidImageFile()) {
			return;
		}
		double perfectRatio = alreadyShownGridImage.imagePane.getWidth() / alreadyShownGridImage.imagePane.getHeight();
		// perfect fit width zoom
		double maxHeight = width / perfectRatio;
		cumulativeZoom = (width + deltaW) / width;
		setViewport(new Rectangle2D(0, 0, width, maxHeight));
		updateWHSlider();
		zoomSlider_setValue(cumulativeZoom);
	}

	// shift the viewport of the imageView by the specified delta, clamping so
	// the viewport does not move off the actual image:
	private void shift(Point2D delta) {
		Rectangle2D viewport = getViewport();
		double maxX = width + deltaW - viewport.getWidth();
		double maxY = height + deltaH - viewport.getHeight();
		if ((int) maxX == 0 && (int) maxY == 0) {
			// no scale factor done => no drag allowed
			return;
		}
		double minX = clamp(viewport.getMinX() - delta.getX(), -deltaW / 2, maxX);
		double minY = clamp(viewport.getMinY() - delta.getY(), -deltaH / 2, maxY);
		setViewport(new Rectangle2D(minX, minY, viewport.getWidth(), viewport.getHeight()));
		calculateAndCenter();
		updateWHSlider();
	}

	private void initializeListeners() {

		curImgPosition = new ImagePosition(1, 0.5, 0.5);

		yChangeListener = new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
				Rectangle2D viewPort = getViewport();
				double maximumShift = height - viewPort.getHeight();
				if (maximumShift > 0) {
					ySlider.setVisible(true);
					setViewport(new Rectangle2D(viewPort.getMinX(), maximumShift * (double) newValue,
							viewPort.getWidth(), viewPort.getHeight()));
				} else {
					ySlider.setValue(0.5);
					ySlider.setVisible(false);
				}
				curImgPosition.setyLvl(ySlider.getValue());
			}
		};
		xChangeListener = new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
				Rectangle2D viewPort = getViewport();
				double maximumShift = width - viewPort.getWidth();
				if (maximumShift > 0) {
					xSlider.setVisible(true);
					setViewport(new Rectangle2D(maximumShift * (double) newValue, viewPort.getMinY(),
							viewPort.getWidth(), viewPort.getHeight()));
				} else {
					xSlider.setValue(0.5);
					xSlider.setVisible(false);
				}
				curImgPosition.setxLvl(xSlider.getValue());
			}
		};
		zoomChangeListener = new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				curImgPosition.setZoomLvl(newValue.doubleValue());
				if (zoomLevelLabel != null) {
					zoomLevelLabel.setText(String.format("X %.2f", newValue.doubleValue()));
				}
				if (!isFullyLoaded) {
					doLoadFullImage();
					return;
				}
				if (cumulativeZoom.equals(newValue.doubleValue())) {
					// that mean other have changed me as well as CumlativeScale
					return;
				}
				cumulativeZoom = newValue.doubleValue();
				double scale = oldValue.doubleValue() / newValue.doubleValue();
				Rectangle2D viewport = getViewport();
				double newWidth = viewport.getWidth() * scale;
				double newHeight = viewport.getHeight() * scale;

				// keep center of image view constant see above explanation zoom on scroll
				double xCenter = (viewport.getMinX() + viewport.getMaxX()) / 2;
				double yCenter = (viewport.getMinY() + viewport.getMaxY()) / 2;
				// Point2D mouse = imageViewToImage(imageView, new Point2D(xCenter, yCenter));
				double newMinX = clamp(xCenter - (xCenter - viewport.getMinX()) * scale, -deltaW / 2,
						width + deltaW - newWidth);
				double newMinY = clamp(yCenter - (yCenter - viewport.getMinY()) * scale, -deltaH / 2,
						height + deltaH - newHeight);

				setViewport(new Rectangle2D(newMinX, newMinY, newWidth, newHeight));
				calculateAndCenter();
				updateWHSlider();
			}
		};
	}

	private void initializeSliders() {
		xSlider = new Slider();
		xSlider.setMin(0);
		xSlider.setMax(1);
		xSlider.setValue(0.5);
		xSlider.valueProperty().addListener(xChangeListener);

		ySliderContainer = new HBox();
		ySlider = new Slider();
		ySlider.setMin(0);
		ySlider.setMax(1);
		ySlider.setValue(0.5);
		ySlider.valueProperty().addListener(yChangeListener);
		ySlider.setRotate(180);
		ySliderContainer.getChildren().addAll(imagePane, ySlider);
		ySlider.setOrientation(Orientation.VERTICAL);
		VBox.setVgrow(ySliderContainer, Priority.ALWAYS);

		zoomSliderContainer = new HBox();
		zoomSlider = new Slider();
		zoomSlider.setMin(1);
		zoomSlider.setMax(8);
		zoomSlider.valueProperty().addListener(zoomChangeListener);
		zoomLevelLabel = new Label(String.format("X %.2f", cumulativeZoom));
		zoomSliderContainer.getChildren().addAll(zoomSlider, zoomLevelLabel);
		zoomSliderContainer.setAlignment(Pos.CENTER);

		ySlider.setVisible(false);
		xSlider.setVisible(false);
	}

	/**
	 * while conserving effective viewPort of image, just center the white space
	 * equally to the left and right in imagePane to get image centered
	 */
	private void calculateAndCenter() {
		Rectangle2D viewport = getViewport();
		double totalWidth = viewport.getWidth();
		double totalHeight = viewport.getHeight();
		double difW = totalWidth - width;
		double difH = totalHeight - height;
		if (totalWidth > width) {
			// distribute width difference
			// let height as it is
			setViewport(new Rectangle2D(-difW / 2, viewport.getMinY(), totalWidth, totalHeight));
			xSlider_setValue(0.5);
		} else {
			// waste white space is not allowed
			// it is better to move port to 00 if x is negative left limit
			// or if maxX > width + deltaW shift rectangle by difference
			if (viewport.getMinX() < 0) {
				setViewport(new Rectangle2D(0, viewport.getMinY(), totalWidth, totalHeight));
				// xSlider_setValue(0);
			}
			if (viewport.getMaxX() > width) {
				double shiftDif = viewport.getMaxX() - width;
				setViewport(
						new Rectangle2D(viewport.getMinX() - shiftDif, viewport.getMinY(), totalWidth, totalHeight));
				// xSlider_setValue(1);
			}
		}
		// similar approach for height
		if (totalHeight > height) {
			setViewport(new Rectangle2D(viewport.getMinX(), -difH / 2, totalWidth, totalHeight));
			ySlider_setValue(0.5);
		} else {
			if (viewport.getMinY() < 0) {
				setViewport(new Rectangle2D(viewport.getMinX(), 0, totalWidth, totalHeight));
				// ySlider_setValue(0);
			}
			if (viewport.getMaxY() > height) {
				double shiftDif = viewport.getMaxY() - height;
				setViewport(
						new Rectangle2D(viewport.getMinX(), viewport.getMinY() - shiftDif, totalWidth, totalHeight));
				// ySlider_setValue(1);
			}
		}
	}

	private void updateWHSlider() {
		xSlider_setValue(getViewport().getMinX() / (width - getViewport().getWidth()));
		ySlider_setValue(getViewport().getMinY() / (height - getViewport().getHeight()));
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

	public Pane getImageAllPane() {
		return allPanesVBox;
	}

	public Pane getImagePaneOnly() {
		return imagePane;
	}

	/**
	 * reset to center position using full image width and height
	 */
	public void resetCenter() {
		getAffectedImageWithPartner().forEach(gid -> gid.resetCenterThisOnly());
	}

	public void resetCenterThisOnly() {
		if (!hasValidImageFile()) {
			return;
		}
		// x y width height
		cumulativeZoom = 1.0;
		zoomSlider_setValue(cumulativeZoom);
		setViewport(new Rectangle2D(-deltaW / 2, -deltaH / 2, width + deltaW, height + deltaH));
		updateWHSlider();
	}

	private double getDeltaPerfectHeight() {
		double perfectRatio = alreadyShownGridImage.imagePane.getWidth() / alreadyShownGridImage.imagePane.getHeight();

		double expectedRatio = width / height;
		if (perfectRatio < expectedRatio) {
			return width / perfectRatio - height;
		}
		return 0;
	}

	private double getDeltaPerfectWidth() {
		double perfectRatio = alreadyShownGridImage.imagePane.getWidth() / alreadyShownGridImage.imagePane.getHeight();
		double expectedRatio = width / height;
		if (perfectRatio > expectedRatio) {
			return height * perfectRatio - width;
		}
		return 0;
	}

	private void ySlider_setValue(double value) {
		if (ySlider != null) {
			ySlider.setValue(value);
		}
	}

	private void xSlider_setValue(double value) {
		if (xSlider != null) {
			xSlider.setValue(value);
		}
	}

	private void zoomSlider_setValue(double value) {
		if (zoomSlider != null) {
			zoomSlider.setValue(value);
		}
	}

	public void setSlidersViews(Slider xSlider, Slider ySlider, Slider zoomSlider, Label zoomLvlLabel) {
		setySlider(ySlider);
		setxSlider(xSlider);
		setZoomSlider(zoomSlider);
		setZoomLevelLabel(zoomLvlLabel);
	}

	/**
	 * @return the ySlider
	 */
	public Slider getySlider() {
		return ySlider;
	}

	/**
	 * @param ySlider the ySlider to set in scale 0 to 1
	 */
	public void setySlider(Slider ySlider) {
		this.ySlider = ySlider;
	}

	/**
	 * @return the xSlider
	 */
	public Slider getxSlider() {
		return xSlider;
	}

	/**
	 * @param xSlider the xSlider to set in scale 0 to 1
	 */
	public void setxSlider(Slider xSlider) {
		this.xSlider = xSlider;
	}

	public Slider getZoomSlider() {
		return zoomSlider;
	}

	/**
	 * in scale 1 to 8 number for example meaning on 2 zoom will be allowed to 2
	 *
	 * @param zoomSlider
	 */
	public void setZoomSlider(Slider zoomSlider) {
		this.zoomSlider = zoomSlider;
	}

	/**
	 * @return the zoomLevelLabel
	 */
	public Label getZoomLevelLabel() {
		return zoomLevelLabel;
	}

	/**
	 * @param zoomLevelLabel the zoomLevelLabel to set
	 */
	public void setZoomLevelLabel(Label zoomLevelLabel) {
		this.zoomLevelLabel = zoomLevelLabel;
	}

	public static boolean isMantaingPos() {
		return MantaingPos;
	}

	public static void setMantaingPos(boolean isMantaingPos) {
		MantaingPos = isMantaingPos;
	}

	/**
	 * rotate image and it's partners in background thread blocking any user
	 * controls of javaFx via {@link DialogHelper#showWaitingScreen(String, String)}
	 */
	// @FXML
	private void rotateRightIMG() {
		// imageView.setRotate(imageView.getRotate() + 90);
		Runnable runnable = () -> {
			try {
				HashSet<ImageGridItem> affected = getAffectedImageWithPartner();
				Holder<Integer> i = new Holder<>(1);
				for (ImageGridItem gridItem : affected) {
					String whichOne = i.value++ + " / " + affected.size();
					if (gridItem.getImageFile() != null) {
						Platform.runLater(() -> DialogHelper.showWaitingScreen("Please Wait.. Processing " + whichOne,
								"Rotating Image.." + whichOne + "\n" + gridItem.getImageFile().getName()));
						gridItem.rotateRightThisImageOnly();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			Platform.runLater(() -> DialogHelper.closeWaitingScreen());
		};
		Thread th = new Thread(runnable);
		th.start();
	}

	/**
	 * Rotate this image only, run in current thread
	 *
	 * @throws IOException
	 */
	public void rotateRightThisImageOnly() throws IOException {
		BufferedImage buffImg = null;
		FilePathLayer newImageFile = imageFile.toFileIfLocalOrAsCopy(true);
		buffImg = ImageIO.read(newImageFile.getFile());
		buffImg = getRotatedImage(buffImg, 90); // or other angle if needed be
		imageFile.delete();
		ImageIO.write(buffImg, imageFile.getExtensionUPPERCASE(), newImageFile.getFile());
		newImageFile.move(imageFile);
		Platform.runLater(() -> {
			// imageView.setRotate(imageView.getRotate() - 90);
			setImageAndSetup(imageFile, null);
			didImageChanged = true;
		});
	}

	// @FXML
	/**
	 * FileTracker is already resolved in this function
	 */
	private void deleteIMG() {
		HashSet<ImageGridItem> affectedGrids = getAffectedImageWithPartner();

		FileHelper.delete(
				affectedGrids.stream().filter(p -> p != null).map(p -> p.getImageFile()).collect(Collectors.toList()),
				deletedPaths -> {
					for (ImageGridItem gridItem : affectedGrids) {
						if (deletedPaths.contains(gridItem.imageFile)) {
							setImage(null);
							if (onDeleteAction != null) {
								onDeleteAction.call(imageFile);
							}
						}
					}
				});
	}

	// @FXML
	private void revealInExplorer() {
		try {
			Main.revealInExplorer(imageFile);
		} catch (IOException e) {
			e.printStackTrace();
			DialogHelper.showException(e);
		}
	}

	// @FXML
	private void cropPhotoMode() {
		if (!isInCropMode) {
			isInCropMode = true;
			doLoadFullImage();
			// Open Crop mode
			cropMenuButton.setText("Save");
			cropMenuButton.getItems().forEach(e -> e.setVisible(true));
			if (StringHelper.getExtention(imageFile.getName()).equals("GIF")) {
				cropMenuButton.setOnAction(e -> {
					saveAsCrop();
					cropMenuButton.setOnAction(e2 -> cropPhotoMode());
				});
			}
			// image layer: a group of images
			Group imageLayer = new Group();
			setOnKeyReleased(e -> {
				if (e.getCode().equals(KeyCode.ENTER)) {
					Boolean ans = DialogHelper.showAlert(AlertType.CONFIRMATION, "Crop Image",
							"Are you sure you want to crop this image?", "", parentStage);
					if (ans == null || !ans) {
						return;
					}
					outCropMode();
					crop(rubberBandSelection.getBounds(), imageFile);
				}
			});
			// add image to layer
			imageLayer.getChildren().add(this);
			// Replace layer with imageView
			imagePane.getChildren().remove(this);
			imagePane.getChildren().add(imageLayer);

			// RubberBand selection
			rubberBandSelection = new RubberBandSelection(imageLayer, this);
			EventHandler<Event> recentOnFinishLoading = onFinishLoading;
			onFinishLoading = e -> {
				selectAllImage();
				onFinishLoading = recentOnFinishLoading;
			};
			isInCropMode = true;
			requestFocus();
		} else {
			outCropMode();
			// perform crop based on rubberBandSelectioin
			// crop the image on bounds rubber
			crop(rubberBandSelection.getBounds(), imageFile);
		}
	}

	private void outCropMode() {
		isInCropMode = false;
		cropMenuButton.setText("Crop");
		cropMenuButton.getItems().forEach(e -> e.setVisible(false));
		restoreCropped.setVisible(true);
		setOnKeyReleased(e -> {
		});
		imagePane.getChildren().clear();
		imagePane.getChildren().add(this);
	}

	// @FXML
	private void selectAllImage() {
		rubberBandSelection.selectFullImage();
	}

	// @FXML
	private void saveAsCrop() {
		outCropMode();
		crop(rubberBandSelection.getBounds(), null);
	}

	// @FXML
	private void cancelCrop() {
		outCropMode();
	}

	private void copyImageToClipBoard() {
		putClipBoardImage(getImage());
	}

	private void saveToClipBoard() {
		outCropMode();
		WritableImage croppedImage = cropImage(rubberBandSelection.getBounds(), null);
		putClipBoardImage(croppedImage);
	}

	private void putClipBoardImage(Image croppedImage) {
		Clipboard clipboard = Clipboard.getSystemClipboard();
		ClipboardContent content = new ClipboardContent();
		content.putImage(croppedImage);
		boolean didCopy = false;
		try {
			clipboard.setContent(content);
			didCopy = true;
		} catch (Exception e) {
			e.printStackTrace();
			DialogHelper.showException(e);
		}
		if (didCopy) {
			DialogHelper.showAlert(AlertType.INFORMATION, "Copy To ClipBoard", "Image Succefully Copeid",
					"Use Ctrl+V To pase it anywhere", parentStage);
		}
	}

	// @FXML
	private void restoreCropped() {
		// convert last backup cropped image to last cropped file

		Runnable runnable = () -> {
			Platform.runLater(() -> {
				DialogHelper.showWaitingScreen("Restoring Your Image...", "Please Wait...");

			});
			convertImageToFile(lastBackUpCroppedImage, lastBackUpCroppedFile);
			Platform.runLater(() -> {
				imageFile = lastBackUpCroppedFile;
				setImageAndSetup(imageFile, null);
				didImageChanged = true;
				restoreCropped.setDisable(true);
				DialogHelper.closeWaitingScreen();
			});
		};
		Thread th = new Thread(runnable);
		th.start();
	}

	private void crop(Bounds bounds, PathLayer outFile) {
		int width = (int) bounds.getWidth();
		int height = (int) bounds.getHeight();

		if (width <= 1 || height <= 1) {
			return;
		}

		if (outFile == null) {
			FileChooser fileChooser = new FileChooser();
			PathLayer originalFile = imageFile;
			File curFile = imageFile.toFileIfLocal();
			if (curFile != null) {
				fileChooser.setInitialDirectory(curFile.getParentFile());
			}
			String ext = StringHelper.getExtention(originalFile.getName());
			if (ext.equals("GIF")) {
				ext = "png";
			}
			fileChooser.setInitialFileName(StringHelper.getBaseName(originalFile.getName()) + "_Cropped." + ext);
			fileChooser.setSelectedExtensionFilter(new ExtensionFilter("Output Format", "jpg"));
			fileChooser.setTitle("Save Image");
			File outFileChoser = fileChooser.showSaveDialog(parentStage);
			if (outFileChoser == null) {
				return;
			}
			outFile = new FilePathLayer(outFileChoser);
		}
		cropImage(bounds, outFile);
	}

	private WritableImage cropImage(Bounds bounds, PathLayer outFile) {
		// converting bounds to real photo bounds
		// convert rectangle of rubberBandSelection from imageView to image:
		Point2D ptStartBounds = new Point2D(bounds.getMinX(), bounds.getMinY());
		Point2D ptEndBounds = new Point2D(bounds.getMaxX(), bounds.getMaxY());
		Point2D realStartPoint = imageViewToImage(ptStartBounds);
		Point2D realEndPoint = imageViewToImage(ptEndBounds);
		Rectangle rectOnOriginalImage = new Rectangle(realStartPoint.getX(), realStartPoint.getY(),
				realEndPoint.subtract(realStartPoint).getX(), realEndPoint.subtract(realStartPoint).getY());
		// more check on bound of image
		if (rectOnOriginalImage.getX() < 0) {
			rectOnOriginalImage.setX(0);
		}
		if (rectOnOriginalImage.getY() < 0) {
			rectOnOriginalImage.setY(0);
		}
		if (rectOnOriginalImage.getWidth() > getImage().getWidth()) {
			rectOnOriginalImage.setWidth(getImage().getWidth());
		}
		if (rectOnOriginalImage.getHeight() > getImage().getHeight()) {
			rectOnOriginalImage.setHeight(getImage().getHeight());
		}

		int width = (int) rectOnOriginalImage.getWidth();
		int height = (int) rectOnOriginalImage.getHeight();
		SnapshotParameters parameters = new SnapshotParameters();
		parameters.setFill(Color.TRANSPARENT);
		parameters.setViewport(new Rectangle2D(rectOnOriginalImage.getX(), rectOnOriginalImage.getY(),
				rectOnOriginalImage.getWidth(), rectOnOriginalImage.getHeight()));

		WritableImage wi = new WritableImage(width, height);

		// putting image in a big image view without zooming to take a clear snapshot:
		ImageView mainImageView = new ImageView();
		lastBackUpCroppedFile = imageFile;
		lastBackUpCroppedImage = convertFileToImage(imageFile);
		restoreCropped.setDisable(false);
		mainImageView.setImage(lastBackUpCroppedImage);
		WritableImage croppedImage = mainImageView.snapshot(parameters, wi);
		if (outFile != null) {
			saveCroppedImage(wi, outFile);
		}
		return croppedImage;
	}

	private static final int[] RGB_MASKS = { 0xFF0000, 0xFF00, 0xFF };
	private static final ColorModel RGB_OPAQUE = new DirectColorModel(32, RGB_MASKS[0], RGB_MASKS[1], RGB_MASKS[2]);

	private void saveCroppedImage(WritableImage wi, PathLayer outFile) {

		if (outFile == null) {
			return;
		}
		String ext = outFile.getExtensionUPPERCASE();
		if (!ArrayIMGExt.contains(ext) || ext.equals("GIF")) {
			// renaming no valid extension type to PNG
			outFile = outFile.resolveSibling(outFile.getName() + ".png");
		}
		final PathLayer nOutFile = outFile;
		Runnable runnable = () -> {
			Platform.runLater(() -> DialogHelper.showWaitingScreen("Croping Image.. Processing", "Cropping Image..."));
			Graphics2D graphics = null;
			FilePathLayer tempFile;
			try {
				tempFile = new FilePathLayer(File.createTempFile(nOutFile.getName(), "." + nOutFile.getExtension()));
			} catch (IOException e1) {
				e1.printStackTrace();
				DialogHelper.showException(e1);
				return;
			}
			try {
				String extention = StringHelper.getExtention(nOutFile.getName()).toLowerCase();
				BufferedImage bufImageARGB = SwingFXUtils.fromFXImage(wi, null);
				if (!extention.equals("png")) {
					// https://stackoverflow.com/questions/4386446/issue-using-imageio-write-jpg-file-pink-background
					PixelGrabber pg = new PixelGrabber(bufImageARGB, 0, 0, -1, -1, true);
					pg.grabPixels();
					int width = pg.getWidth(), height = pg.getHeight();

					DataBuffer buffer = new DataBufferInt((int[]) pg.getPixels(), pg.getWidth() * pg.getHeight());
					WritableRaster raster = Raster.createPackedRaster(buffer, width, height, width, RGB_MASKS, null);
					BufferedImage bi = new BufferedImage(RGB_OPAQUE, raster, false, null);

					ImageIO.write(bi, extention, tempFile.getFile());
				} else {
					BufferedImage bufImageRGB = new BufferedImage(bufImageARGB.getWidth(), bufImageARGB.getHeight(),
							BufferedImage.BITMASK);

					graphics = bufImageRGB.createGraphics();
					graphics.drawImage(bufImageARGB, 0, 0, null);
					// writing image and forcing PNG format
					ImageIO.write(bufImageRGB, extention, tempFile.getFile());
				}
				nOutFile.delete();
				tempFile.move(nOutFile);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				Platform.runLater(() -> {
					cumulativeZoom = 1.0;
					zoomSlider_setValue(1);
					setImageAndSetup(nOutFile.toURI().toString(), null);
					didImageChanged = true;
				});
				if (graphics != null) {
					graphics.dispose();
				}
				System.gc();
			}
			Platform.runLater(() -> DialogHelper.closeWaitingScreen());
		};
		Thread thread = new Thread(runnable);
		thread.start();
	}

	/**
	 *
	 * @param image
	 * @param outFile
	 * @return outputFile
	 */
	private PathLayer convertImageToFile(Image image, PathLayer outFile) {
		BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
		try {
			File tempFile = File.createTempFile(outFile.getName(), outFile.getExtension());
			ImageIO.write(bImage, outFile.getExtensionUPPERCASE(), tempFile);
			outFile.delete();
			new FilePathLayer(tempFile).move(outFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return outFile;
	}

	private Image convertFileToImage(PathLayer imageFile) {
		Image image = null;
		File inputFile = null;
		try {
			inputFile = imageFile.toFileIfLocalOrAsCopy(false).getFile();
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
		try (FileInputStream fileInputStream = new FileInputStream(inputFile)) {
			image = new Image(fileInputStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return image;
	}

	public BufferedImage getRotatedImage(BufferedImage image, int angle) {
		// https://blog.idrsolutions.com/2019/05/image-rotation-in-java/
		final double rads = Math.toRadians(angle);
		final double sin = Math.abs(Math.sin(rads));
		final double cos = Math.abs(Math.cos(rads));
		final int w = (int) Math.floor(image.getWidth() * cos + image.getHeight() * sin);
		final int h = (int) Math.floor(image.getHeight() * cos + image.getWidth() * sin);
		final BufferedImage rotatedImage = new BufferedImage(w, h, image.getType());
		final AffineTransform at = new AffineTransform();
		at.translate(w / 2, h / 2);
		at.rotate(rads, 0, 0);
		at.translate(-image.getWidth() / 2, -image.getHeight() / 2);
		final AffineTransformOp rotateOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		rotateOp.filter(image, rotatedImage);
		return rotatedImage;
	}

	@Override
	public String toString() {
		return "ImageGridItem [width=" + width + ", height=" + height + ", deltaW=" + deltaW + ", deltaH=" + deltaH
				+ "]";
	}

	public ImagePosition getCurImgPosition() {
		return curImgPosition;
	}

	public void setCurImgPosition(ImagePosition curImgPosition) {
		this.curImgPosition = curImgPosition;
	}

	public Dimension2D getOriginalImageDim() {
		return originalImageDim;
	}

	public void setOriginalImageDim(Dimension2D originalImageDim) {
		this.originalImageDim = originalImageDim;
	}

	public PathLayer getImageFile() {
		return imageFile;
	}

	public void setImageFile(PathLayer imageFile) {
		this.imageFile = imageFile;
	}

	/**
	 * @return the onFinishLoading
	 */
	public EventHandler<Event> getOnFinishLoading() {
		return onFinishLoading;
	}

	/**
	 * will be called with null event parameter when image is loaded on background
	 * i.e. when it's {@link Image#progressProperty()} is 1
	 *
	 * @param onFinishLoading the onFinishLoading to set
	 */
	public void setOnFinishLoading(EventHandler<Event> onFinishLoading) {
		this.onFinishLoading = onFinishLoading;
	}

	/**
	 * @return the onSetUpImage
	 */
	public CallBackToDo getOnSetUpImage() {
		return onSetUpImage;
	}

	/**
	 * Called after {@link #finalizeSetUpImage(Image, File, ImagePosition)} when a
	 * ready image is on the imageView
	 *
	 * @param onSetUpImage the onSetUpImage to set
	 */
	public void setOnSetUpImage(CallBackToDo onSetUpImage) {
		this.onSetUpImage = onSetUpImage;
	}

	public boolean isFullyLoaded() {
		return isFullyLoaded;
	}

	public void setFullyLoaded(boolean isFullyLoaded) {
		this.isFullyLoaded = isFullyLoaded;
	}

	public boolean hasValidImageFile() {
		return !isDisable() && imageFile != null;
	}

	/**
	 *
	 * if image did a full load since it was put this set to true after call of
	 * {@link #doLoadFullImage()} or it's cropped/rotated<br>
	 * Normally use this when changing to a new Photo and you want to cache
	 * {@link #getImage()} <br>
	 * Other wise you want {@link #isFullyLoaded}
	 *
	 * @return the didImageChanged
	 */
	public boolean isDidImageChanged() {
		return didImageChanged;
	}

	public void showZoomSlider() {
		if (!isZoomSliderShown()) {
			allPanesVBox.getChildren().add(zoomSliderContainer);
		}
	}

	public void hideZoomSlider() {
		allPanesVBox.getChildren().remove(zoomSliderContainer);
	}

	public void toggleShowZoomSlider() {
		if (isZoomSliderShown()) {
			hideZoomSlider();
		} else {
			showZoomSlider();
		}
	}

	public boolean isZoomSliderShown() {
		return allPanesVBox.getChildren().contains(zoomSliderContainer);
	}

	public void showToolBox() {
		if (!isToolBoxShown()) {
			allPanesVBox.getChildren().add(toolBoxHBox);
		}
	}

	public void hideToolBox() {
		allPanesVBox.getChildren().remove(toolBoxHBox);
	}

	public void toggleShowToolBox() {
		if (isToolBoxShown()) {
			hideToolBox();
		} else {
			showToolBox();
		}
	}

	public boolean isToolBoxShown() {
		return allPanesVBox.getChildren().contains(toolBoxHBox);
	}

	public void showNameLabel() {
		if (!isNameLabelShown()) {
			// add direct up to imagePnae
			int where = allPanesVBox.getChildren().indexOf(ySliderContainer);
			allPanesVBox.getChildren().add(where, nameHBox);
		}
	}

	public void hideNameLabel() {
		allPanesVBox.getChildren().remove(nameHBox);
	}

	public void toggleShowNameLabel() {
		if (isNameLabelShown()) {
			hideNameLabel();
		} else {
			showNameLabel();
		}
	}

	public boolean isNameLabelShown() {
		return allPanesVBox.getChildren().contains(nameHBox);
	}

	public void showNoteLabel() {
		if (!isNoteLabelShown()) {
			// add Before zoom Slider
			int where = isZoomSliderShown() ? allPanesVBox.getChildren().indexOf(zoomSliderContainer)
					: allPanesVBox.getChildren().size();
			allPanesVBox.getChildren().add(where, noteHBox);
		}
	}

	public void hideNoteLabel() {
		allPanesVBox.getChildren().remove(noteHBox);
	}

	public void toggleShowNoteLabel() {
		if (isNoteLabelShown()) {
			hideNoteLabel();
		} else {
			showNoteLabel();
		}
	}

	public boolean isNoteLabelShown() {
		return allPanesVBox.getChildren().contains(noteHBox);
	}

	public void setNoteLabel(String note) {
		noteLabel.setText(note);
	}

	public String getNoteLabel() {
		return noteLabel.getText();
	}

	public void setSeen(Boolean isSeen) {
		if (isSeen != null && isSeen) {
			getImageAllPane().setStyle("-fx-background-color: #DCEDC8;");
		} else {
			getImageAllPane().setStyle("");
		}
	}

	public void requestFocusOnImageAllPane() {
		getImageAllPane().requestFocus();
	}

	public static boolean isAutoFitWidth() {
		return AutoFitWidth;
	}

	public static void setAutoFitWidth(boolean isAutoFitWidth) {
		AutoFitWidth = isAutoFitWidth;
	}

	/**
	 * @return the doubleClickBehavior
	 */
	public static boolean isDoubleClickBehavior() {
		return DoubleClickBehavior;
	}

	/**
	 * Double click behavior is that on double click image will fit width,<br>
	 * and again on double click it will reset center.
	 *
	 * @param doubleClickBehavior the doubleClickBehavior to set
	 * @see #reEnableDoubleClickBehavior()
	 */
	public static void setDoubleClickBehavior(boolean doubleClickBehavior) {
		DoubleClickBehavior = doubleClickBehavior;
	}

	public double getMinHeight() {
		return allPanesVBox.minHeight(-1);
	}

	public double getCurrentHeight() {
		return allPanesVBox.getHeight();
	}

	/**
	 * @return the stretchImage
	 */
	public boolean isStretchImage() {
		return stretchImage;
	}

	/**
	 * @param stretchImage the stretchImage to set
	 */
	public void setStretchImage(boolean stretchImage) {
		this.stretchImage = stretchImage;
	}

	/**
	 * @return the onDeleteAction
	 */
	public CallBackVoid<PathLayer> getOnDeleteAction() {
		return onDeleteAction;
	}

	/**
	 * @param onDeleteAction the onDeleteAction to set
	 */
	public void setOnDeleteAction(CallBackVoid<PathLayer> onDeleteAction) {
		this.onDeleteAction = onDeleteAction;
	}

	/**
	 * @return the partnerActionGridItems
	 * @see #getAffectedImageWithPartner()
	 */
	@Nullable
	public List<ImageGridItem> getPartnerActionGridItems() {
		return partnerActionGridItems;
	}

	/**
	 * The map contain at least 'this'.
	 *
	 * @return the map of grid item that action should trigger on with partner and
	 *         'this' each mapping to {@link #getImageFile()}
	 * @see #setPartnerActionGridItems(List)
	 */
	public HashSet<ImageGridItem> getAffectedImageWithPartner() {
		HashSet<ImageGridItem> affectedGrids = new HashSet<>();
		affectedGrids.add(this);
		if (partnerActionGridItems != null) {
			for (ImageGridItem gridItem : partnerActionGridItems) {
				if (!affectedGrids.contains(gridItem)) {
					affectedGrids.add(gridItem);
				}
			}
		}
		return affectedGrids;
	}

	/**
	 * Affect partners also when calling any of these functions:<br>
	 * <ul>
	 * <li>{@link #rotateRightIMG()}</li>
	 * <li>{@link #deleteIMG()}</li>
	 * <li>{@link #fitWidth()}</li>
	 * <li>{@link #resetCenter()}</li>
	 * </ul>
	 *
	 * Will ensure that if other gridItem that have same list of partner that the
	 * action will trigger once on each of them and not recursively
	 *
	 * @param partnerActionGridItems the partnerActionGridItems to set<br>
	 *                               Set to <code>null</code> to clear partners
	 * @see #getAffectedImageWithPartner()
	 */
	public void setPartnerActionGridItems(@Nullable List<ImageGridItem> partnerActionGridItems) {
		this.partnerActionGridItems = partnerActionGridItems;
	}

}
