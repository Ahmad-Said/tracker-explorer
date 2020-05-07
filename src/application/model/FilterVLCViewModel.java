package application.model;

import application.datatype.MediaCutData;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Duration;

public class FilterVLCViewModel implements Comparable<FilterVLCViewModel> {

	private Duration start;
	private SimpleStringProperty startText;

	private Duration end;
	private SimpleStringProperty endText;

	private SimpleStringProperty description;

	private HBox hboxActions;
	private Button editButton;
	private Button runVLCButton;
	private Button removeButton;

	public FilterVLCViewModel(Duration start, Duration end, String description) {
		this.start = start;
		this.end = end;
		initializeConstruction(description);
	}

	public FilterVLCViewModel(String start, String end, String description) {
		this.start = Duration.seconds(Integer.parseInt(start));
		this.end = Duration.seconds(Integer.parseInt(end));
		initializeConstruction(description);
	}

	public FilterVLCViewModel(MediaCutData mediaCutData) {
		start = Duration.seconds(mediaCutData.getStart());
		end = Duration.seconds(mediaCutData.getEnd());
		initializeConstruction(mediaCutData.getTitle());
	}

	public final void initializeConstruction(String description) {
		startText = new SimpleStringProperty(getDurationFormat(start));
		endText = new SimpleStringProperty(getDurationFormat(end));
		this.description = new SimpleStringProperty(description);
		hboxActions = new HBox();
		initializeButtonViews();
	}

	private void initializeButtonViews() {
		editButton = new Button();
		editButton.setMaxWidth(Double.MAX_VALUE);
		editButton.setText("Edit");
		editButton.getStyleClass().addAll("info", "first");

		runVLCButton = new Button();
		runVLCButton.setMaxWidth(Double.MAX_VALUE);
		runVLCButton.setText("Run");
		runVLCButton.getStyleClass().addAll("warning", "middle");
		runVLCButton.setTooltip(new Tooltip("This Start playing media considering only this and above exclution"));
		removeButton = new Button();
		removeButton.getStyleClass().addAll("danger", "last");
		removeButton.setMaxWidth(Double.MAX_VALUE);
		removeButton.setText("Remove");
		hboxActions.setMinWidth(100);
		HBox.setHgrow(editButton, Priority.ALWAYS);

		hboxActions.getChildren().addAll(getEditButton(), getRunVLCButton(), getRemoveButton());
	}

	@Override
	public int compareTo(FilterVLCViewModel o) {
		// TODO Auto-generated method stub
		if (o.getStart().toSeconds() < getStart().toSeconds()) {
			return 1;
		}
		return 0;
	}

	public static String getDurationFormat(Duration a) {
		String ans = " : ";
		int hours = (int) a.toHours();
		int min = (int) a.toMinutes();
		int sec = (int) a.toSeconds();
		sec -= min * 60;
		min -= hours * 60;

		// Prettily look:
		if (sec < 10) {
			ans += "0";
		}
		if (min < 10) {
			ans = "0" + min + ans;
		} else {
			ans = min + ans;
		}
		ans = hours + " : " + ans;
		return ans + sec;
	}

	public Duration getStart() {
		return start;
	}

	public void setStart(Duration start) {
		startText.set(getDurationFormat(start));
		this.start = start;
	}

	public Duration getEnd() {
		return end;
	}

	public void setEnd(Duration end) {
		endText.set(getDurationFormat(end));
		this.end = end;
	}

	public HBox getHboxActions() {
		return hboxActions;
	}

	public void setHboxActions(HBox hboxActions) {
		this.hboxActions = hboxActions;
	}

	public Button getRemoveButton() {
		return removeButton;
	}

	public void setRemoveButton(Button remove) {
		removeButton = remove;
	}

	public Button getEditButton() {
		return editButton;
	}

	public void setEditButton(Button openEdit) {
		editButton = openEdit;
	}

	public Button getRunVLCButton() {
		return runVLCButton;
	}

	public void setRunVLCButton(Button runVLC) {
		runVLCButton = runVLC;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description.get();
	}

	public void setDescription(String string) {
		description.set(string);
	}

	public String getStartText() {
		return startText.get();
	}

	public String getEndText() {
		return endText.get();
	}

}
