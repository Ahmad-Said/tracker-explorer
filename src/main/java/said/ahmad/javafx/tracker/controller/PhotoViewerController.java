package said.ahmad.javafx.tracker.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair;
import said.ahmad.javafx.tracker.app.DialogHelper;
import said.ahmad.javafx.tracker.app.ResourcesHelper;
import said.ahmad.javafx.tracker.app.StringHelper;
import said.ahmad.javafx.tracker.app.look.ThemeManager;
import said.ahmad.javafx.tracker.datatype.ImagePosition;
import said.ahmad.javafx.tracker.fxGraphics.ImageGridItem;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.tracker.system.file.PathLayerHelper;
import said.ahmad.javafx.tracker.system.file.local.FilePathLayer;
import said.ahmad.javafx.tracker.system.operation.FileHelper;
import said.ahmad.javafx.tracker.system.operation.FileHelper.ActionOperation;
import said.ahmad.javafx.tracker.system.tracker.FileTracker;

public class PhotoViewerController {
	@FXML
	private Button fullScreenBut;

	@FXML
	private Button hideShowButTop;

	@FXML
	private Button hideShowButBot;

	@FXML
	private VBox topVbox;

	@FXML
	private VBox bottomVbox;

	@FXML
	private CheckBox fitWidthCheckBox;

	@FXML
	private CheckBox maintainPosCheckBox;

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
	private ToggleButton markSeen;

	@FXML
	private Label labelDescription;

	@FXML
	private TextField nameImage;

	@FXML
	private Button applyName;

	@FXML
	private TextField noteInput;

	@FXML
	private Button applyNote;

	@FXML
	private TextField locationField;

	@FXML
	private Button nextButton;

	@FXML
	private Button previousButton;

	// @FXML
	// private ScrollPane scrollPane;
	@FXML
	private GridPane gridImagesPane;
	@FXML
	private ScrollPane scrollPane;
	private int gridRowSize = 1;
	private int gridColumnSize = 1;
	/**
	 * Simply it is equal to gridRowSize * gridColumnSize
	 */
	private int gridSize = 1;

	@FXML
	private ToggleButton zoomOnClick;

	@FXML
	private ToggleButton showToolBoxToggle;

	@FXML
	private CheckBox stretchCheckBox;

	@FXML
	private CheckBox showNameCheckBox;

	@FXML
	private CheckBox showZoomCheckBox;

	@FXML
	private CheckBox showNoteCheckBox;

	@FXML
	private Button increaseGridSize;
	@FXML
	private Button decreaseGridSize;

	@FXML
	private SplitMenuButton gridSwitchButton;

	private ImageGridItem initialGridItem;
	private ArrayList<ImageGridItem> allGridItems = new ArrayList<>();

	public static final Image PHOTO_ICON_IMAGE = new Image(ResourcesHelper.getResourceAsStream("/img/photo_Icon.png"));

	public static HashSet<String> ArrayIMGExt = new HashSet<String>(
			Arrays.asList("PNG", "GIF", "JPG", "JPS", "MPO", "BMP", "WEBMP", "JPEG"));
	private FileTracker fileTracker;
	private Stage photoStage;

	private List<PathLayer> ImgResources;
	private Integer rollerPhoto = 0;

	/**
	 * general notes: Remember that viewPort is a rectangle on original photo pixels
	 * size so after setting this viewport rectangle it will be scaled to imageView
	 * where image is being shown.
	 *
	 * @param list                    can be null
	 * @param filePath
	 * @param welcomeToRefreshCanNull can be null
	 */
	public PhotoViewerController(List<PathLayer> list, PathLayer filePath, WelcomeController welcomeToRefreshCanNull) {
		try {
			photoStage = new Stage();
			Parent root;
			Scene scene;
			FXMLLoader loader = new FXMLLoader(ResourcesHelper.getResourceAsURL("/fxml/PhotoViewer.fxml"));
			loader.setController(this);
			root = loader.load();
			scene = new Scene(root);
			ThemeManager.applyTheme(scene);
			photoStage.getIcons().add(PHOTO_ICON_IMAGE);

			photoStage.setTitle("Photo Viewer " + filePath.getName());
			photoStage.setScene(scene);
		} catch (IOException e) {
			e.printStackTrace();
		}
		PathLayer directory = filePath.getParentPath();
		if (list == null || list.size() <= 1) {
			try {
				list = getImgFilesInDir(directory);
			} catch (IOException e) {
				e.printStackTrace();
				if (list == null) {
					list = new ArrayList<>();
					list.add(filePath);
				}
			}
		}
		ImgResources = list;
		rollerPhoto = list.indexOf(filePath);
		initializeButtons();
		initializeFileTracker(welcomeToRefreshCanNull);
		photoStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent t) {
				cachedImage.clear();
				cachedImage = null;
			}
		});

		initialGridItem = new ImageGridItem(photoStage, null);
		initialGridItem.hideToolBox();
		allGridItems.add(initialGridItem);
		gridImagesPane.add(initialGridItem.getImageAllPane(), 0, 0);

		photoStage.show();
		photoStage.setMaximized(true);

		initializeGridImagesPane();

		changeImage(filePath);

		gridImagesPane.requestFocus();

		// Scroll Pane in case of too much grid Item
		scrollPane.setStyle("-fx-background-color:transparent;");
		gridImagesPane.setPrefWidth(scrollPane.getWidth() - 10);
		gridImagesPane.setPrefHeight(scrollPane.getHeight() - 10);
		scrollPane.widthProperty().addListener((obsevable, oldWidth, newWidth) -> {
			if (scrollPane.getContent() != null && scrollPane.getContent().equals(gridImagesPane)) {
				gridImagesPane.setPrefWidth(newWidth.doubleValue() - 10);
			}
		});
		scrollPane.heightProperty().addListener((obsevable, oldHeight, newHeight) -> {
			if (scrollPane.getContent() != null && scrollPane.getContent().equals(gridImagesPane)) {
				gridImagesPane.setPrefHeight(newHeight.doubleValue() - 10);
			}
		});
	}

	private void initializeFileTracker(WelcomeController welcomeCon) {
		fileTracker = new FileTracker(null,
				welcomeCon == null ? null : writtenPath -> welcomeCon.refreshAllSplitViewsIfMatch(writtenPath, null));
		PathLayerHelper.getParentsPaths(ImgResources).forEach(parent -> {
			fileTracker.loadResolvedMapOrEmpty(parent);
		});
	}

	public void fireOnClickEvent(boolean doSwitchGridZoom, ImageGridItem gridItem) {
		selectImageGrid(gridItem);
		if (!gridItem.isDisabled()) {
			if (zoomOnClick.isSelected() && doSwitchGridZoom) {
				gridSwitchButton.fire();
			}
		}
	}

	public void selectImageGrid(ImageGridItem gridItem) {
		if (!gridItem.isDisabled()) {
			rollerPhoto = ImgResources.indexOf(gridItem.getImageFile());
			indexOfImage.setText(rollerPhoto + 1 + " / " + ImgResources.size());
			photoStage.setTitle("Photo Viewer " + gridItem.getImageFile().getName());
			locationField.setText(gridItem.getImageFile().getAbsolutePath());
			nameImage.setText(gridItem.getImageFile().getName());
			widthPx.setText(gridItem.getOriginalImageDim().getWidth() + " Px");
			heightPx.setText(gridItem.getOriginalImageDim().getHeight() + " Px");
			sizeMb.setText(String.format("%.2f", gridItem.getImageFile().getSize() / 1024.0 / 1024.0) + " MB");
			PathLayer key = gridItem.getImageFile();
			updateMarkSeen(fileTracker.isSeen(key));
			noteInput.setText(fileTracker.getNoteText(key));
			if (!gridItem.getImageAllPane().isFocused()) {
				gridItem.requestFocusOnImageAllPane();
			}
		}
	}

	private void requestFocusOnRollerToGetBorder() {
		allGridItems.get(rollerPhoto % gridSize).requestFocusOnImageAllPane();
	}

	private void initializeGridImagesPane() {
		initialGridItem.getImageAllPane()
				.setOnMouseClicked(e -> fireOnClickEvent(e.isStillSincePress(), initialGridItem));
		initialGridItem.getImageAllPane().setOnTouchPressed(e -> fireOnClickEvent(true, initialGridItem));

		borderPane.setOnDragOver(new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {
				if (event.getDragboard().hasFiles() || event.getDragboard().hasContent(DataFormat.URL)
						|| event.getDragboard().hasString()) {
					event.acceptTransferModes(TransferMode.ANY);
				}
			}
		});
		// https://stackoverflow.com/questions/32534113/javafx-drag-and-drop-a-file-into-a-program
		borderPane.setOnDragDropped(new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {
				Dragboard db = event.getDragboard();
				if (db.hasFiles()) {
					PathLayerHelper.getParentsFiles(db.getFiles()).forEach(parent -> {
						fileTracker.loadResolvedMapOrEmpty(parent);
					});
					if (db.getFiles().size() == 1 && ImgResources.contains(new FilePathLayer(db.getFiles().get(0)))) {
						changeImage(new FilePathLayer(db.getFiles().get(0)));
					} else {
						db.getFiles().forEach(file3 -> {
							FilePathLayer filePath = new FilePathLayer(file3);
							if (ArrayIMGExt.contains(filePath.getExtensionUPPERCASE())
									&& !ImgResources.contains(filePath)) {
								ImgResources.add(rollerPhoto, filePath);
							}
						});
					}
					changeImage(ImgResources.get(rollerPhoto));
				}
			}
		});
		borderPane.setOnKeyPressed(onKeyPressedEvent);
		scrollPane.setOnKeyPressed(onKeyPressedEvent);
	}

	private EventHandler<KeyEvent> onKeyPressedEvent = new EventHandler<KeyEvent>() {
		@Override
		public void handle(KeyEvent key) {
			switch (key.getCode()) {
			case RIGHT:
			case F:
			case D:
				nextImage();
				key.consume();
				break;
			case LEFT:
			case A:
				previousImage();
				key.consume();
				break;
			case SPACE:
				toggleSeen();
				key.consume();
				break;
			case N:
				Platform.runLater(() -> noteInput.requestFocus());
				key.consume();
				break;
			case F2:
			case M:
				key.consume();
				Platform.runLater(() -> {
					nameImage.requestFocus();
					nameImage.selectRange(0, nameImage.getText().lastIndexOf('.'));
				});
				break;
			default:
				break;
			}
		}
	};

	private void resizeGridImagesPane(int newGridRowSize, int newGridColumnSize) {
		if (newGridColumnSize < 1 || newGridColumnSize < 1) {
			return;
		}
		gridRowSize = newGridRowSize;
		gridColumnSize = newGridColumnSize;
		gridSize = newGridColumnSize * newGridRowSize;
		if (gridSize > 1) {
			initialGridItem.getImageAllPane().getStyleClass().add("onFocusBorder");
		} else {
			initialGridItem.getImageAllPane().getStyleClass().removeAll("onFocusBorder");
		}
		limitGridSize();
		gridImagesPane.getChildren().clear();
		int allGridIndex = 0;
		int initialSize = allGridItems.size();
		ArrayList<Pane> allImagePanes = new ArrayList<>();
		double widthPercent = 100 / gridColumnSize;
		double heightPercent = 100 / gridRowSize;
		for (int i = 0; i < gridRowSize; i++) {
			for (int j = 0; j < gridColumnSize; j++) {
				ImageGridItem gridItem;
				if (allGridIndex < initialSize) {
					gridItem = allGridItems.get(allGridIndex++);
				} else {
					gridItem = new ImageGridItem(photoStage, initialGridItem);
					gridItem.getImageAllPane().getStyleClass().add("onFocusBorder");
					gridItem.getImageAllPane()
							.setOnMouseClicked(e -> fireOnClickEvent(e.isStillSincePress(), gridItem));
					gridItem.getImageAllPane().setOnTouchPressed(e -> fireOnClickEvent(true, gridItem));
					allGridItems.add(gridItem);
				}
				GridPane.clearConstraints(gridItem.getImageAllPane());
				GridPane.setConstraints(gridItem.getImageAllPane(), j, i);
				allImagePanes.add(gridItem.getImageAllPane());
			}
		}
		showToolBox(showToolBoxToggle.isSelected());
		showNameLabel(showNameCheckBox.isSelected());
		showNoteLabel(showNoteCheckBox.isSelected());
		showZoomSlider(showZoomCheckBox.isSelected());
		stretchGridImages(stretchCheckBox.isSelected());
		// add all at once for better performance
		for (int i = gridImagesPane.getColumnConstraints().size(); i <= gridRowSize; i++) {
			gridImagesPane.getColumnConstraints().add(new ColumnConstraints());
		}
		for (int i = 0; i < gridImagesPane.getColumnConstraints().size(); i++) {
			if (i < gridRowSize) {
				gridImagesPane.getColumnConstraints().get(i).setPercentWidth(widthPercent);
			} else {
				gridImagesPane.getColumnConstraints().get(i).setPercentWidth(-1);
			}
		}
		for (int i = gridImagesPane.getRowConstraints().size(); i <= gridRowSize; i++) {
			gridImagesPane.getRowConstraints().add(new RowConstraints());
		}
		for (int i = 0; i < gridImagesPane.getRowConstraints().size(); i++) {
			if (i < gridRowSize) {
				gridImagesPane.getRowConstraints().get(i).setPercentHeight(heightPercent);
			} else {
				gridImagesPane.getRowConstraints().get(i).setPercentHeight(-1);
			}
		}
		gridImagesPane.getChildren().addAll(allImagePanes);
		Platform.runLater(() -> changeImage(ImgResources.get(rollerPhoto)));
	}

	private void limitGridSize() {
		if (gridSize > ImgResources.size()) {
			int maxGrid = (int) Math.ceil(Math.sqrt(ImgResources.size()));
			gridRowSize = maxGrid;
			gridColumnSize = maxGrid;
			gridSize = gridRowSize * gridColumnSize;
			increaseGridSize.setDisable(true);
		} else {
			increaseGridSize.setDisable(false);
		}
		if (gridSize == 1) {
			decreaseGridSize.setDisable(true);
		} else {
			decreaseGridSize.setDisable(false);
		}
	}

	private void initializeButtons() {
		indexOfImage.setOnMouseClicked(e -> goToPhoto());

		ImageView zoomIcon = new ImageView(new Image(ResourcesHelper.getResourceAsStream("/img/zoom_icon.png")));
		zoomIcon.setFitWidth(20);
		zoomIcon.setFitHeight(20);
		zoomOnClick.setGraphic(zoomIcon);
		zoomOnClick.setOnAction(e -> ImageGridItem.setDoubleClickBehavior(!zoomOnClick.isSelected()));

		fitWidthCheckBox.setOnAction(p -> {
			if (fitWidthCheckBox.isSelected()) {
				ImageGridItem.setAutoFitWidth(true);
				for (ImageGridItem item : allGridItems) {
					item.fitWidth();
				}
			} else {
				ImageGridItem.setAutoFitWidth(false);
				for (ImageGridItem item : allGridItems) {
					item.resetCenter();
				}
			}
		});
		showToolBoxToggle.setOnAction(e -> showToolBox(showToolBoxToggle.isSelected()));

		ImageView toolBoxIcon = new ImageView(new Image(ResourcesHelper.getResourceAsStream("/img/setting-512.png")));
		toolBoxIcon.setFitWidth(20);
		toolBoxIcon.setFitHeight(20);
		showToolBoxToggle.setGraphic(toolBoxIcon);

		showNameCheckBox.setOnAction(e -> showNameLabel(showNameCheckBox.isSelected()));
		showNoteCheckBox.setOnAction(e -> showNoteLabel(showNoteCheckBox.isSelected()));
		showZoomCheckBox.setOnAction(e -> showZoomSlider(showZoomCheckBox.isSelected()));
		stretchCheckBox.setOnAction(e -> {
			stretchGridImages(stretchCheckBox.isSelected());
			changeImage(ImgResources.get(rollerPhoto));
		});

		maintainPosCheckBox.setOnAction(e -> ImageGridItem.setMantaingPos(maintainPosCheckBox.isSelected()));

		ImageView gridSwitchIcon = new ImageView(
				new Image(ResourcesHelper.getResourceAsStream("/img/grid_button.png")));
		gridSwitchIcon.setFitWidth(20);
		gridSwitchIcon.setFitHeight(20);
		gridSwitchButton.setGraphic(gridSwitchIcon);
		gridSwitchButton.setUserData(new Pair<Integer, Integer>(2, 2));
		gridSwitchButton.setOnAction(e -> {
			if (gridSize != 1) {
				gridSwitchButton.setUserData(new Pair<Integer, Integer>(gridRowSize, gridColumnSize));
				resizeGridImagesPane(1, 1);
			} else {
				@SuppressWarnings("unchecked")
				Pair<Integer, Integer> rowSize = (Pair<Integer, Integer>) gridSwitchButton.getUserData();
				resizeGridImagesPane(rowSize.getKey(), rowSize.getValue());
			}
		});
		increaseGridSize.setOnAction(e -> resizeGridImagesPane(gridRowSize + 1, gridColumnSize + 1));
		decreaseGridSize.setDisable(true);
		decreaseGridSize.setOnAction(e -> resizeGridImagesPane(gridRowSize - 1, gridColumnSize - 1));
		MenuItem initial = new MenuItem("1 X 1");
		initial.setOnAction(e -> resizeGridImagesPane(1, 1));
		gridSwitchButton.getItems().add(initial);
		for (Integer i = 2; i <= 16; i += 2) {
			int j = i;
			MenuItem mn = new MenuItem(j + " X " + j);
			mn.setOnAction(e -> {
				resizeGridImagesPane(j, j);
			});
			gridSwitchButton.getItems().add(mn);
		}

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

	private void showToolBox(boolean doShow) {
		for (ImageGridItem gridItem : allGridItems) {
			if (doShow) {
				gridItem.showToolBox();
			} else {
				gridItem.hideToolBox();
			}
		}
	}

	private void showNameLabel(boolean doShow) {
		for (ImageGridItem gridItem : allGridItems) {
			if (doShow) {
				gridItem.showNameLabel();
			} else {
				gridItem.hideNameLabel();
			}
		}
	}

	private void showNoteLabel(boolean doShow) {
		for (ImageGridItem gridItem : allGridItems) {
			if (doShow && !gridItem.getNoteLabel().isEmpty()) {
				gridItem.showNoteLabel();
			} else {
				gridItem.hideNoteLabel();
			}
		}
	}

	private void showZoomSlider(boolean doShow) {
		for (ImageGridItem imageGridItem : allGridItems) {
			if (doShow) {
				imageGridItem.showZoomSlider();
			} else {
				imageGridItem.hideZoomSlider();
			}
		}
	}

	private void stretchGridImages(boolean doStretchImage) {
		for (ImageGridItem imageGridItem : allGridItems) {
			imageGridItem.setStretchImage(doStretchImage);
		}
	}

	@FXML
	private void showPhotosOnly() {
		boolean didEnter = false;
		if (showNameCheckBox.isSelected()) {
			didEnter = true;
			showNameCheckBox.fire();
		}
		if (showNoteCheckBox.isSelected()) {
			didEnter = true;
			showNoteCheckBox.fire();
		}
		if (showZoomCheckBox.isSelected()) {
			didEnter = true;
			showZoomCheckBox.fire();
		}
		if (!didEnter) {
			showNameCheckBox.fire();
			showNoteCheckBox.fire();
			showZoomCheckBox.fire();
		}
	}

	private List<PathLayer> getImgFilesInDir(PathLayer parent) throws IOException {
		// https://stackoverflow.com/questions/2965747/why-do-i-get-an-unsupportedoperationexception-when-trying-to-remove-an-element-f
		return parent.listNoHiddenPathLayers().stream().filter(p -> ArrayIMGExt.contains(p.getExtensionUPPERCASE()))
				.sorted(StringHelper.NaturalFileComparator).collect(Collectors.toList());
	}

	// whenever images are fully loaded or not
	private HashMap<PathLayer, Pair<Image, Boolean>> cachedImage = new HashMap<>();
	private HashMap<PathLayer, ImagePosition> cachedImagePositon = new HashMap<>();
	private HashMap<PathLayer, Dimension2D> cachedImageDimension = new HashMap<>();
	private ImageGridItem lastSelectedGridItem = null;
	private EventHandler<Event> onSetUpImage = e -> selectImageGrid(lastSelectedGridItem);

	private void changeImage(PathLayer selectedImg) {
		// When finish setup image do focus on target image
		if (lastSelectedGridItem != null) {
			lastSelectedGridItem.setOnSetUpImage(null);
		}
		lastSelectedGridItem = allGridItems.get(rollerPhoto % gridSize);
		lastSelectedGridItem.setOnSetUpImage(onSetUpImage);

		// before changing to a new picture cache the current image position
		// and if it full load
		for (ImageGridItem gridItem : allGridItems) {
			if (gridItem.hasValidImageFile()) {
				cachedImagePositon.put(gridItem.getImageFile(), gridItem.getCurImgPosition().clone());
				if (gridItem.isDidImageChanged()) {
					cachedImage.put(gridItem.getImageFile(),
							new Pair<Image, Boolean>(gridItem.getImage(), gridItem.isFullyLoaded()));
				}
			}
		}

		// load selected image and all next image till grid items are filled
		rollerPhoto = ImgResources.indexOf(selectedImg);
		checkDisableIndexBoundNextPrevious();
		indexOfImage.setText(rollerPhoto + 1 + " / " + ImgResources.size());
		int start = rollerPhoto - rollerPhoto % gridSize;
		int end = Math.min(ImgResources.size(), start + gridSize);
		int gridIndex = 0;
		for (int i = start; i < end && i < ImgResources.size(); i++) {
			PathLayer toLoadImageFile = ImgResources.get(i);
			// check existence
			if (!toLoadImageFile.exists()) {
				cachedImage.remove(toLoadImageFile);
				cachedImagePositon.remove(toLoadImageFile);
				ImgResources.remove(i);
				i--;
				if (ImgResources.size() == 0) {
					photoStage.close();
					return;
				}
				continue;
			}

			ImageGridItem gridItem = allGridItems.get(gridIndex++);
			Image image = null;
			Pair<Image, Boolean> imagePair;
			ImagePosition lastestPos = cachedImagePositon.get(toLoadImageFile);
			if (cachedImage.containsKey(toLoadImageFile)) {
				imagePair = cachedImage.get(toLoadImageFile);
				image = imagePair.getKey();
				gridItem.setOriginalImageDim(cachedImageDimension.get(toLoadImageFile));
				gridItem.setImageAndSetup(image, toLoadImageFile, imagePair.getValue(), lastestPos);
			} else {
				imagePair = gridItem.setImageAndSetup(toLoadImageFile, lastestPos);
				image = imagePair.getKey();
				cachedImage.put(toLoadImageFile, imagePair);
				cachedImageDimension.put(toLoadImageFile, new Dimension2D(gridItem.getOriginalImageDim().getWidth(),
						gridItem.getOriginalImageDim().getHeight()));
			}
			PathLayer key = toLoadImageFile;
			Boolean isSeen = fileTracker.isSeen(key);
			String toolTip = fileTracker.getNoteText(key);
			gridItem.setSeen(isSeen);
			gridItem.setNoteLabel(toolTip);
			if (showNoteCheckBox.isSelected() && !toolTip.isEmpty()) {
				gridItem.showNoteLabel();
			} else {
				gridItem.hideNoteLabel();
			}
		}
		for (int i = gridIndex; i < gridSize; i++) {
			allGridItems.get(i).setDisable(true, false);
		}
	}

	@FXML
	private void nextImage() {
		if (rollerPhoto < ImgResources.size() - 1) {
			rollerPhoto = Math.min(ImgResources.size() - 1, rollerPhoto / gridSize * gridSize + gridSize);
		} else {
			return;
		}
		changeImage(ImgResources.get(rollerPhoto));
		selectImageGrid(initialGridItem);
		gridImagesPane.requestFocus();
	}

	/**
	 * Triggered in call {@link #changeImage(File)}
	 */
	private void checkDisableIndexBoundNextPrevious() {
		if (rollerPhoto / gridSize * gridSize - gridSize < 0) {
			previousButton.setDisable(true);
		} else {
			previousButton.setDisable(false);
		}
		if (rollerPhoto / gridSize * gridSize + gridSize >= ImgResources.size()) {
			nextButton.setDisable(true);
		} else {
			nextButton.setDisable(false);
		}

	}

	@FXML
	private void previousImage() {
		if (rollerPhoto > 0) {
			rollerPhoto = Math.max(0, rollerPhoto / gridSize * gridSize - gridSize);
		} else {
			return;
		}
		changeImage(ImgResources.get(rollerPhoto));
		selectImageGrid(initialGridItem);
	}

	@FXML
	private void renameImage() {
		final PathLayer curImage = ImgResources.get(rollerPhoto);
		if (nameImage.getText().isEmpty()) {
			DialogHelper.showAlert(AlertType.ERROR, "Rename", "Name cannot be empty", "");
			nameImage.setText(curImage.getName());
			return;
		}
		PathLayer oldPath = curImage;
		PathLayer newPath = null;
		PathLayer oldconflictPath = null;
		PathLayer newconflictPath = null;

		if (!nameImage.getText().equals(curImage.getName())) {
			try {
				newPath = FileHelper.RenameHelper(oldPath, nameImage.getText());
			} catch (FileAlreadyExistsException e) {
				boolean ans = DialogHelper.showConfirmationDialog("Rename Conflict Error",
						"File Already exist with such name!!",
						"Do you want to rename the other file? \n So we can retry renaming this File..");
				if (ans) {
					oldconflictPath = curImage.resolveSibling(nameImage.getText());
					newconflictPath = FileHelper.renameGUI(oldconflictPath, null);
					try {
						newPath = FileHelper.RenameHelper(oldPath, nameImage.getText());
					} catch (IOException e1) {
						nameImage.setText(curImage.getName());
						e.printStackTrace();
						DialogHelper.showException(e);
					}
				} else {
					nameImage.setText(curImage.getName());
				}

			} catch (IOException e) {
				nameImage.setText(curImage.getName());
				e.printStackTrace();
				DialogHelper.showException(e);
			}
		}
		boolean isOtherRename = oldconflictPath != null && newconflictPath != null;
		boolean isCurrentRename = newPath != null && oldPath != null;

		// conserving tracker data
		fileTracker.getMapDetails().putAll(FileTracker.operationUpdateAsList(Arrays.asList(oldconflictPath, oldPath),
				Arrays.asList(newconflictPath, newPath), ActionOperation.RENAME));

		if (isOtherRename) {
			// updating list of images sources in photo explorer
			ImgResources.set(ImgResources.indexOf(oldconflictPath), newconflictPath);
		}
		if (isCurrentRename) {
			ImgResources.set(ImgResources.indexOf(oldPath), newPath);
		}

		final PathLayer path = newPath;
		if (isCurrentRename) {
			changeImage(path);
		} else {
			requestFocusOnRollerToGetBorder();
		}
	}

	private boolean untrackedBehavior() {
		boolean ans;
		ans = FileTracker.getAns();
		if (ans) {
			PathLayer parentPath = ImgResources.get(rollerPhoto).getParentPath();
			try {
				fileTracker.trackNewOutFolder(parentPath, true, false);
			} catch (IOException e) {
				e.printStackTrace();
				DialogHelper.showException(e);
			}
		}
		return ans;
	}

	@FXML
	private void addNoteImage() {
		PathLayer curFile = ImgResources.get(rollerPhoto);
		PathLayer parentPath = curFile.getParentPath();
		if (!FileTracker.isTrackedOutFolder(parentPath) && !untrackedBehavior()) {
			return;
		}
		String note = noteInput.getText();
		fileTracker.getMapDetails().get(curFile).setNoteText(note);
		fileTracker.commitTrackerDataChange(curFile);
		allGridItems.get(rollerPhoto % gridSize).setNoteLabel(note);
		if (!note.isEmpty()) {
			allGridItems.get(rollerPhoto % gridSize).showNoteLabel();
		}
		requestFocusOnRollerToGetBorder();
	}

	public void updateMarkSeen(Boolean seen) {
		markSeen.getStyleClass().removeAll("info", "success");
		allGridItems.get(rollerPhoto % gridSize).setSeen(seen);
		if (seen == null) {
			markSeen.setText("S/U");
			markSeen.setSelected(false);
			return;
		}
		if (seen) {
			markSeen.getStyleClass().add("success");
			markSeen.setText("S");
			markSeen.setSelected(true);
		} else {
			markSeen.setSelected(false);
			markSeen.getStyleClass().add("info");
			markSeen.setText("U");
		}
	}

	@FXML
	private void toggleSeen() {
		PathLayer curFile = ImgResources.get(rollerPhoto);
		PathLayer parentPath = curFile.getParentPath();
		if (!FileTracker.isTrackedOutFolder(parentPath) && !untrackedBehavior()) {
			return;
		}
		updateMarkSeen(!fileTracker.isSeen(curFile));
		fileTracker.toggleSeen(curFile);
		fileTracker.commitTrackerDataChange(curFile);
		requestFocusOnRollerToGetBorder();
	}

	@FXML
	private void showHideBottom() {
		if (borderPane.getBottom() == null) {
			borderPane.setBottom(bottomVbox);
			hideShowButBot.setText("Hide ↓");
		} else {
			hideShowButBot.setText("Show ↑");
			borderPane.setBottom(null);
		}
	}

	@FXML
	private void showHideTop() {
		if (borderPane.getTop() == null) {
			borderPane.setTop(topVbox);
			hideShowButTop.setText("Hide ↑");
		} else {
			hideShowButTop.setText("Show ↓");
			borderPane.setTop(null);
		}
	}

	@FXML
	private void switchFullScreen() {
		photoStage.setFullScreen(!photoStage.isFullScreen());
	}

	@FXML
	private void goToPhoto() {
		Integer indexMinus = DialogHelper.showIntegerInputDialog("Go To Photo", "Enter Photo Index", "Index", 1,
				ImgResources.size(), rollerPhoto + 1);
		if (indexMinus == null) {
			return;
		}
		changeImage(ImgResources.get(indexMinus - 1));
	}
}
