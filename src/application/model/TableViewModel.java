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
	private SimpleStringProperty Status;
	private ImageView imgIcon;
	private final SimpleStringProperty Name;
	private HBox hboxActions;
	private ToggleButton MarkSeen;
	private Button mNoteButton;
	private Button openVLC;
	private Path mFilePath;

	public TableViewModel(String status, String name, Path path) {
		super();
		Status = new SimpleStringProperty(status);
		Name = new SimpleStringProperty(name);

		// hboxActions = new HBox(10,MarkSeen); give error !!
		hboxActions = new HBox();
		mFilePath = path;

		initializeButton();
		initializeVLCFeatures();

		Image fxImage = SystemIconsHelper.getFileIcon(path.toString());
		imgIcon = new ImageView(fxImage);
		// worked after getting coloumn here but useless
		// imgIcon.fitWidthProperty().bind(colIconTestResize.widthProperty());
	}

	/**
	 * This is were defined the common features of button otherwise for specific
	 * design and contact with FileTracker
	 * 
	 * @see {@link SplitViewController#initializeTable(Boolean) #setRowFactory}
	 */
	private void initializeButton() {

		// initialize Note button with ToolTip
		mNoteButton = new Button();
		mNoteButton.getStyleClass().addAll("success");
		mNoteButton.setText("N");
		Tooltip tt = new Tooltip();
		tt.setText("Add Hover Note");
		tt.getStyleClass().addAll("tooltip");
		tt.setStyle("-fx-background-color: #7F00FF;-fx-text-fill: white;-fx-font-size:12;-fx-font-weight:bold");
		mNoteButton.setTooltip(tt);
		// mNoteButton.setStyle("-fx-text-fill:
		// white;-fx-font-size:12;-fx-font-weight:bold");
		mNoteButton.setStyle("-fx-background-color: #7F00FF");

		MarkSeen = new ToggleButton();
		MarkSeen.setMaxWidth(Double.MAX_VALUE);
		hboxActions.setMinWidth(100);
		HBox.setHgrow(MarkSeen, Priority.ALWAYS);
		Tooltip ms = new Tooltip();
		ms.setText("Toogle Seen");
		ms.setStyle("-fx-font-size:12;-fx-font-weight:bold");
		ms.getStyleClass().addAll("tooltip", "info");
		MarkSeen.setTooltip(ms);
		MarkSeen.setStyle("-fx-text-fill: white;-fx-font-size:12;-fx-font-weight:bold");
		MarkSeen.setText("U");

		hboxActions.getChildren().addAll(getMarkSeen(), getmNoteButton());
	}

	private void initializeVLCFeatures() {

		if (VLC.isVLCMediaExt(this.getName()) || VLC.isPlaylist(getName())) {
			openVLC = new Button();
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
				MarkSeen.getStyleClass().add("first");
				mNoteButton.getStyleClass().add("middle");
				openVLC.getStyleClass().add("last");
			}
			openVLC.getStyleClass().addAll("warning", "btn");
			hboxActions.getChildren().add(getOpenVLC());
		} else
			mNoteButton.getStyleClass().add("last");

	}

	@Override
	public String toString() {
		return "TableViewModel [Status=" + Status + ", Name=" + Name + "]";
	}

	public String getName() {
		return Name.get();
	}

	public String getStatus() {
		return Status.get();
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

	public Button getOpenVLC() {
		return openVLC;
	}

	public void setOpenVLC(Button openVLC) {
		this.openVLC = openVLC;
	}

	public void setStatus(String status) {
		Status = new SimpleStringProperty(status);
	}

}
