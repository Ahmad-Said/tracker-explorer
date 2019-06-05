package application.model;

import java.nio.file.Path;

import application.SystemIconsHelper;
import application.VLC;
import application.controller.SplitViewController;
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

	/// most important note that took me hours to catch
	// all elements used in view property table must have
	// getters and setters generate them with source !
	private SimpleStringProperty NoteText;
	private ImageView imgIcon;
	private SimpleStringProperty Name;
	private HBox hboxActions;
	private ToggleButton MarkSeen;
	private Button mNoteButton;
	private Button openVLC;
	private Path mFilePath;
	private boolean wasinitialized = false;

	public TableViewModel(String status, String name, Path path) {
		super();
		NoteText = new SimpleStringProperty(status);
		Name = new SimpleStringProperty(name);
		// hboxActions = new HBox(10,MarkSeen); give error !!
		hboxActions = new HBox();
		mFilePath = path;

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

	// using this approach only initializing visual stuff when user do realy need
	// it save much time faster on loading data to table view
	// with factor of ~*0.2318461538461538
	public void initializerRowFactory() {
		if (wasinitialized)
			return;
		wasinitialized = true;
		initializeButton();
		initializeVLCFeatures();

		Image fxImage = SystemIconsHelper.getFileIcon(mFilePath.toString());
		imgIcon = new ImageView(fxImage);
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
		if (seen)
			MarkSeen.setText("S");
		else
			getMarkSeen().setText("U");
	}

	public void emptyCell() {
		MarkSeen.setText("-");
	}

	public void resetMarkSeen() {
		MarkSeen.getStyleClass().removeAll("info", "success");
		MarkSeen.setText("-");
		MarkSeen.setSelected(false);
	}

	private void initializeVLCFeatures() {

		if (VLC.isVLCMediaExt(this.getName()) || VLC.isPlaylist(getName())) {
			Tooltip ms = new Tooltip();
			ms.setStyle("-fx-font-size:12;-fx-font-weight:bold");
			ms.getStyleClass().addAll("tooltip", "warning");
			openVLC.setTooltip(ms);
			openVLC.setText("V");

			if (VLC.isPlaylist(this.getName())) {
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
		} else
			mNoteButton.getStyleClass().add("last");

	}

	public String getName() {
		return Name.get();
	}

	public ImageView getImgIcon() {
		return imgIcon;
	}

	public void setImgIcon(ImageView imgIcon) {
		this.imgIcon = imgIcon;
	}

	public HBox getHboxActions() {
		return hboxActions;
	}

	public void setHboxActions(HBox hboxActions) {
		this.hboxActions = hboxActions;
	}

	public ToggleButton getMarkSeen() {
		return MarkSeen;
	}

	public void setMarkSeen(ToggleButton MarkSeen) {
		this.MarkSeen = MarkSeen;
	}

	public Button getmNoteButton() {
		return mNoteButton;
	}

	public void setmNoteButton(Button mNoteButton) {
		this.mNoteButton = mNoteButton;
	}

	public Path getmFilePath() {
		return mFilePath;
	}

	public void setmFilePath(Path mFilePath) {
		this.mFilePath = mFilePath;
	}

	public String generateKeyURI() {
		return mFilePath.toUri().toString();
	}

	public Button getOpenVLC() {
		return openVLC;
	}

	public void setOpenVLC(Button openVLC) {
		this.openVLC = openVLC;
	}

	public String getNoteText() {
		return NoteText.get();
	}

	public void setNoteText(String noteText) {
		NoteText.set(noteText);
		// NoteText.setValue(noteText);
		// NoteText.set(noteText);
	}

	@Override
	public String toString() {
		return "TableViewModel " + Name.get() + "]";
	}

	public void setName(String name) {
		Name = new SimpleStringProperty(name);
	}

}
