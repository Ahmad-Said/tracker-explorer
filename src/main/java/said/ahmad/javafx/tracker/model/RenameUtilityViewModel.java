package said.ahmad.javafx.tracker.model;

import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import said.ahmad.javafx.tracker.app.StringHelper;
import said.ahmad.javafx.tracker.system.SystemIconsHelper;
import said.ahmad.javafx.tracker.system.file.PathLayer;

public class RenameUtilityViewModel {
	private CheckBox ConsiderCheckBox;
	private ImageView imgIcon;
	private TextFlow OldName;
	// https://docs.oracle.com/javase/8/javafx/api/javafx/scene/text/TextFlow.html
	private TextFlow NewName;

	private PathLayer PathFile;
	private static int TextFlowHeigh = 35;

	public RenameUtilityViewModel(PathLayer path) {
		String name = path.getName();

		setOldName(name);
		setNewName(name);
		setPathFile(path);
		ConsiderCheckBox = new CheckBox();
		ConsiderCheckBox.setSelected(true);
		Image fxImage = SystemIconsHelper.getFileIcon(PathFile);
		imgIcon = new ImageView(fxImage);
	}

	public RenameUtilityViewModel(String name) {

		setOldName(name);
		setNewName(name);
		setPathFile(null);
		ConsiderCheckBox = new CheckBox();
		ConsiderCheckBox.setSelected(true);
		Image fxImage = SystemIconsHelper.getFileIcon("TXT");
		imgIcon = new ImageView(fxImage);
	}

	public void resetNewName() {
		NewName.getChildren().clear();
		Text temp = new Text(getOldName());
		temp.setFont(Font.font(15));
		NewName.getChildren().add(temp);
	}

	public String getOldName() {
		return StringHelper.textFlowToString(OldName);
	}

	public CheckBox getConsiderCheckBox() {
		return ConsiderCheckBox;
	}

	public void setConsiderCheckBox(CheckBox considerCheckBox) {
		ConsiderCheckBox = considerCheckBox;
	}

	/**
	 * @return the pathFile
	 */
	public PathLayer getPathFile() {
		return PathFile;
	}

	/**
	 * @param pathFile the pathFile to set
	 */
	public void setPathFile(PathLayer pathFile) {
		PathFile = pathFile;
	}

	public TableViewModel toTableViewModel() {
		return new TableViewModel(getOldName(), PathFile);
	}

	/**
	 * @return the newName
	 */
	public TextFlow getNewName() {
		return NewName;
	}

	public String getNewNameAsString() {
		return StringHelper.textFlowToString(NewName);
	}

	public void setOldName(String oldName) {
		Text temp = new Text(oldName);
		temp.setFont(Font.font(15));
		OldName = new TextFlow(temp);
		OldName.setMaxHeight(TextFlowHeigh);
		OldName.setMinHeight(TextFlowHeigh);
		OldName.setPrefHeight(TextFlowHeigh);
	}

	/**
	 * @param newName the newName to set
	 */
	public void setNewName(String newName) {
		Text temp = new Text(newName);
		temp.setFont(Font.font(15));
		NewName = new TextFlow(temp);
		NewName.setMaxHeight(TextFlowHeigh);
		NewName.setMinHeight(TextFlowHeigh);
		NewName.setPrefHeight(TextFlowHeigh);
	}

	/**
	 * @param newName the newName to set
	 */
	public void setNewName(TextFlow newName) {
		NewName = newName;
	}

	/**
	 * @return the imgIcon
	 */
	public ImageView getImgIcon() {
		return imgIcon;
	}

	/**
	 * @param imgIcon the imgIcon to set
	 */
	public void setImgIcon(ImageView imgIcon) {
		this.imgIcon = imgIcon;
	}

}
