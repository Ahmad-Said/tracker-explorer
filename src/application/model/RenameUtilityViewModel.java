package application.model;

import java.nio.file.Path;

import application.SystemIconsHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class RenameUtilityViewModel {
	private CheckBox ConsiderCheckBox;
	private ImageView imgIcon;
	private SimpleStringProperty OldName;
	// https://docs.oracle.com/javase/8/javafx/api/javafx/scene/text/TextFlow.html
	private TextFlow NewName;

	private Path PathFile;
	private static int TextFlowHeigh = 35;

	public RenameUtilityViewModel(Path path) {
		OldName = new SimpleStringProperty(path.getFileName().toString());

		Text temp = new Text(OldName.get());
		temp.setFont(Font.font(15));
		setNewName(new TextFlow(temp));

		NewName.setMaxHeight(TextFlowHeigh);
		NewName.setMinHeight(TextFlowHeigh);
		NewName.setPrefHeight(TextFlowHeigh);
		setPathFile(path);
		ConsiderCheckBox = new CheckBox();
		ConsiderCheckBox.setSelected(true);
		Image fxImage = SystemIconsHelper.getFileIcon(PathFile.toString());
		imgIcon = new ImageView(fxImage);
	}

	public void resetNewName() {
		NewName.getChildren().clear();
		Text temp = new Text(OldName.get());
		temp.setFont(Font.font(15));
		NewName.getChildren().add(temp);
	}

	public String getOldName() {
		return OldName.get();
	}

	public void setOldName(String oldName) {
		OldName.set(oldName);
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
	public Path getPathFile() {
		return PathFile;
	}

	/**
	 * @param pathFile the pathFile to set
	 */
	public void setPathFile(Path pathFile) {
		PathFile = pathFile;
	}

	public TableViewModel toTableViewModel() {
		return new TableViewModel(OldName.get(), PathFile);
	}

	/**
	 * @return the newName
	 */
	public TextFlow getNewName() {
		return NewName;
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
