package application.model;

import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import application.SystemIconsHelper;
import application.VLC;
import application.controller.SplitViewController;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * This is the table used in each split view
 */
public class TableViewModel {

	private SimpleDoubleProperty FileSize;
	private HBox hboxActions;
	private ImageView imgIcon;
	private ToggleButton MarkSeen;
	private Path mFilePath;
	private Button mNoteButton;
	private SimpleStringProperty Name;
	/// most important note that took me hours to catch
	// all elements used in view property table must have
	// getters and setters generate them with source !
	private SimpleStringProperty NoteText;
	private Button openVLC;
	private boolean wasinitialized = false;

	/**
	 * Basic Constructor used for functional work
	 *
	 * @param name
	 * @param path
	 */
	public TableViewModel(String name, Path path) {
		Name = new SimpleStringProperty(name);
		mFilePath = path;
	}

	public TableViewModel(String status, String name, Path path) {
		super();
		NoteText = new SimpleStringProperty(status);
		Name = new SimpleStringProperty(name);
		// hboxActions = new HBox(10,MarkSeen); give error !!
		hboxActions = new HBox();
		mFilePath = path;
		setFileSize(new SimpleDoubleProperty(path.toFile().length() / 1024.0 / 1024.0));
		// testing
		mNoteButton = new Button();
		MarkSeen = new ToggleButton();
		// hboxActions.getChildren().addAll(getMarkSeen(), getmNoteButton());
		// if (VLC.isVLCMediaExt(this.getName()) || VLC.isPlaylist(getName()))
		openVLC = new Button();

		// initializeButton();
		// initializeVLCFeatures();

		// Image fxImage = SystemIconsHelper.getFileIcon(path.toString());
		// Bounds bound = imgIcon.getBoundsInLocal(); // getting co-ordinates
		// imgIcon.setEffect(
		// new ColorInput(bound.getMinX(), bound.getMinY(), bound.getWidth(),
		// bound.getHeight(), Color.YELLOW));
		// imgIcon = new ImageView(fxImage);
		// worked after getting coloumn here but useless
		// imgIcon.fitWidthProperty().bind(colIconTestResize.widthProperty());
	}

	public void emptyCell() {
		MarkSeen.setText("-");
	}

	public String generateKeyURI() {
		return mFilePath.toFile().toURI().toString();
	}

	/**
	 * @return the fileSize In MB
	 */
	public double getFileSize() {
		return FileSize.get();
	}

	public HBox getHboxActions() {
		return hboxActions;
	}

	public ImageView getImgIcon() {
		return imgIcon;
	}

	public ToggleButton getMarkSeen() {
		return MarkSeen;
	}

	public Path getmFilePath() {
		return mFilePath;
	}

	public Button getmNoteButton() {
		return mNoteButton;
	}

	public String getName() {
		return Name.get();
	}

	public String getNoteText() {
		return NoteText.get();
	}

	public Button getOpenVLC() {
		return openVLC;
	}

	/**
	 * This is were defined the common features of button otherwise for specific
	 * design and contact with FileTracker
	 *
	 * @see {@link SplitViewController#initializeTable(Boolean) #setRowFactory}
	 */
	private void initializeButton() {

		// initialize Note button with ToolTip
		mNoteButton.getStyleClass().addAll("btn");
		mNoteButton.setText("N");
		Tooltip tt = new Tooltip();
		tt.setText("Add Hover Note");
		tt.getStyleClass().addAll("tooltip");
		tt.setStyle("-fx-background-color: #7F00FF;-fx-text-fill:white;-fx-font-size:12;-fx-font-weight:bold");
		mNoteButton.setTooltip(tt);
		// mNoteButton.setStyle("-fx-text-fill:
		// white;-fx-font-size:12;-fx-font-weight:bold");
		mNoteButton.setStyle("-fx-background-color: #7F00FF;-fx-text-fill:white;-fx-font-size:13;-fx-font-weight:bold");
		mNoteButton.getStyleClass().add("last");
		mNoteButton.setPrefHeight(MarkSeen.getPrefHeight());
		MarkSeen.setMaxWidth(Double.MAX_VALUE);
		MarkSeen.getStyleClass().add("first");
		hboxActions.setMinWidth(100);
		HBox.setHgrow(MarkSeen, Priority.ALWAYS);
		Tooltip ms = new Tooltip();
		ms.setText("Toogle Seen");
		ms.setStyle("-fx-font-size:12;-fx-font-weight:bold");
		ms.getStyleClass().addAll("tooltip");
		MarkSeen.setStyle("-fx-text-fill: white;-fx-font-size:13;-fx-font-weight:bold");
		MarkSeen.setTooltip(ms);
		updateMarkSeen(false);

		hboxActions.getChildren().addAll(getMarkSeen(), getmNoteButton());
	}

	// using this approach only initializing visual stuff when user do realy need
	// it save much time faster on loading data to table view
	// with factor of ~*0.2318461538461538
	public void initializerRowFactory() {
		if (wasinitialized) {
			return;
		}
		wasinitialized = true;
		initializeButton();
		initializeVLCFeatures();

		Image fxImage = SystemIconsHelper.getFileIconIfCached(mFilePath.toString());
		imgIcon = new ImageView();
		if (fxImage != null) {
			imgIcon.setImage(fxImage);
		} else {
			executor.execute(() -> {
				Image fxImage2 = SystemIconsHelper.getFileIcon(mFilePath.toString());
				Platform.runLater(() -> imgIcon.setImage(fxImage2));
			});
		}
	}

	private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

	private void initializeVLCFeatures() {

		if (VLC.isVLCMediaExt(getName()) || VLC.isPlaylist(getName())) {
			Tooltip ms = new Tooltip();
			ms.setStyle("-fx-font-size:12;-fx-font-weight:bold");
			ms.getStyleClass().addAll("tooltip", "warning");
			openVLC.setTooltip(ms);
			openVLC.setText("V");

			if (VLC.isPlaylist(getName())) {
				ms.setText("Click to Run PlayList!"); // this will run with postion 4 and timeout 12
				hboxActions.getChildren().remove(mNoteButton);
				HBox.setHgrow(openVLC, Priority.ALWAYS);
				openVLC.setMaxWidth(200);
			} else {
				ms.setText("Click to Configure or right click for a quick start");
				mNoteButton.getStyleClass().removeAll("last");
				mNoteButton.getStyleClass().add("middle");
				openVLC.getStyleClass().add("last");
			}
			openVLC.getStyleClass().addAll("warning", "btn");
			hboxActions.getChildren().add(getOpenVLC());
		} else {
			mNoteButton.getStyleClass().add("last");
		}

	}

	public void resetMarkSeen() {
		MarkSeen.getStyleClass().removeAll("info", "success");
		MarkSeen.setText("-");
		MarkSeen.setSelected(false);
	}

	/**
	 * @param fileSize the fileSize to set
	 */
	public void setFileSize(SimpleDoubleProperty fileSize) {
		FileSize = fileSize;
	}

	public void setHboxActions(HBox hboxActions) {
		this.hboxActions = hboxActions;
	}

	public void setImgIcon(ImageView imgIcon) {
		this.imgIcon = imgIcon;
	}

	public void setMarkSeen(ToggleButton MarkSeen) {
		this.MarkSeen = MarkSeen;
	}

	public void setmFilePath(Path mFilePath) {
		this.mFilePath = mFilePath;
	}

	public void setmNoteButton(Button mNoteButton) {
		this.mNoteButton = mNoteButton;
	}

	public void setName(String name) {
		Name = new SimpleStringProperty(name);
	}

	public void setNoteText(String noteText) {
		NoteText.set(noteText);
		// NoteText.setValue(noteText);
		// NoteText.set(noteText);
	}

	public void setOpenVLC(Button openVLC) {
		this.openVLC = openVLC;
	}

	@Override
	public String toString() {
		return "TableViewModel " + Name.get() + "]";
	}

	public void updateMarkSeen(boolean seen) {
		MarkSeen.getStyleClass().removeAll("info", "success");
		updateMarkSeenText(seen);
		if (seen) {
			MarkSeen.getStyleClass().add("success");
			MarkSeen.setSelected(true);
		} else {
			getMarkSeen().getStyleClass().add("info");
			getMarkSeen().setSelected(false);
		}
	}

	public void updateMarkSeenText(boolean seen) {
		if (seen) {
			MarkSeen.setText("S");
		} else {
			getMarkSeen().setText("U");
		}
	}

}
