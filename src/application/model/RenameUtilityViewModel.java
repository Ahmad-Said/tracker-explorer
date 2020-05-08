package application.model;

import java.nio.file.Path;

import application.StringHelper;
import application.system.SystemIconsHelper;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class RenameUtilityViewModel {
	private CheckBox ConsiderCheckBox;
	private ImageView imgIcon;
	private TextFlow OldName;
	// https://docs.oracle.com/javase/8/javafx/api/javafx/scene/text/TextFlow.html
	private TextFlow NewName;

	private Path PathFile;
	private static int TextFlowHeigh = 35;

	public RenameUtilityViewModel(Path path) {
		String name = path.getFileName().toString();

		setOldName(name);
		setNewName(name);
		setPathFile(path);
		ConsiderCheckBox = new CheckBox();
		ConsiderCheckBox.setSelected(true);
		Image fxImage = SystemIconsHelper.getFileIcon(PathFile.toString());
		imgIcon = new ImageView(fxImage);
	}

	public RenameUtilityViewModel(String name) {

		setOldName(name);
		setNewName(name);
		setPathFile(null);
		ConsiderCheckBox = new CheckBox();
		ConsiderCheckBox.setSelected(true);
//		Image fxImage = new Image(Main.class.getResourceAsStream("/img/Text.png"));
		Image fxImage = SystemIconsHelper.getFileIcon("test.SomethingNotAnextention");
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
