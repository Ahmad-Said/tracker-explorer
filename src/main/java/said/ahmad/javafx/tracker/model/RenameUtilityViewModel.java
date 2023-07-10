package said.ahmad.javafx.tracker.model;

import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;
import said.ahmad.javafx.tracker.app.StringHelper;
import said.ahmad.javafx.tracker.system.SystemIconsHelper;
import said.ahmad.javafx.tracker.system.file.PathLayer;

import javax.annotation.Nullable;

@Getter
@Setter
public class RenameUtilityViewModel {
	private CheckBox considerCheckBox;
	private ImageView imgIcon;
	private TextFlow oldName;
	// https://docs.oracle.com/javase/8/javafx/api/javafx/scene/text/TextFlow.html
	private TextFlow newName;

	@Nullable
	private PathLayer pathFile;
	private static final int TEXT_FLOW_HEIGH = 35;

	public RenameUtilityViewModel(PathLayer path) {
		String name = path.getName();

		setOldName(name);
		setNewName(name);
		setPathFile(path);
		considerCheckBox = new CheckBox();
		considerCheckBox.setSelected(true);
		Image fxImage = SystemIconsHelper.getFileIcon(pathFile);
		imgIcon = new ImageView(fxImage);
	}

	public RenameUtilityViewModel(String name) {

		setOldName(name);
		setNewName(name);
		setPathFile(null);
		considerCheckBox = new CheckBox();
		considerCheckBox.setSelected(true);
		Image fxImage = SystemIconsHelper.getFileIcon("TXT");
		imgIcon = new ImageView(fxImage);
	}

	public void resetNewName() {
		newName.getChildren().clear();
		Text temp = new Text(getOldNameAsString());
		temp.setFont(Font.font(15));
		newName.getChildren().add(temp);
	}

	public String getOldNameAsString() {
		return StringHelper.textFlowToString(oldName);
	}

	public String getNewNameAsString() {
		return StringHelper.textFlowToString(newName);
	}

	public void setOldName(String oldName) {
		Text temp = new Text(oldName);
		temp.setFont(Font.font(15));
		this.oldName = new TextFlow(temp);
		this.oldName.setMaxHeight(TEXT_FLOW_HEIGH);
		this.oldName.setMinHeight(TEXT_FLOW_HEIGH);
		this.oldName.setPrefHeight(TEXT_FLOW_HEIGH);
	}

	/**
	 * @param newName the newName to set
	 */
	public void setNewName(String newName) {
		Text temp = new Text(newName);
		temp.setFont(Font.font(15));
		this.newName = new TextFlow(temp);
		this.newName.setMaxHeight(TEXT_FLOW_HEIGH);
		this.newName.setMinHeight(TEXT_FLOW_HEIGH);
		this.newName.setPrefHeight(TEXT_FLOW_HEIGH);
	}

	/**
	 * Return the name of file without extension if it is normal file.
	 * full name otherwise (directory)
	 * @return Name of file without extension for normal file, full name otherwise (for directory)
	 */
	public String getOldBaseName() {
		if (pathFile == null || !pathFile.isDirectory()) {
			return FilenameUtils.getBaseName(getOldNameAsString());
		} else {
			return this.getOldNameAsString();
		}
	}

	/**
	 * Return the extension of file if it is normal file.
	 * empty string otherwise (directory)
	 * @return Name of file without extension for normal file, full name otherwise (for directory)
	 */
	public String getOldExtension() {
		if (pathFile == null || !pathFile.isDirectory()) {
			return FilenameUtils.getExtension(getOldNameAsString());
		} else {
			return "";
		}
	}

}
