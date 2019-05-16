package application.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Duration;

public class FilterVLCTableView implements Comparable<FilterVLCTableView> {

	private final SimpleStringProperty ShowStart;
	private final SimpleStringProperty ShowEnd;
	private SimpleStringProperty Description;
	private Duration Start;
	private Duration End;

	private HBox hboxActions;
	private Button OpenEdit;
	private Button RunVLC;
	private Button Remove;

	public FilterVLCTableView(Duration start, Duration end, String description) {
		super();
		Start = start;
		End = end;
		ShowStart = new SimpleStringProperty(mDurationFormat(Start));
		ShowEnd = new SimpleStringProperty(mDurationFormat(End));
		Description = new SimpleStringProperty(description);
		hboxActions = new HBox();
		initializeButtonViews();
	}

	public FilterVLCTableView(String start, String end, String description) {
		super();
		Start = Duration.seconds(Integer.parseInt(start));
		End = Duration.seconds(Integer.parseInt(end));
		ShowStart = new SimpleStringProperty(mDurationFormat(Start));
		ShowEnd = new SimpleStringProperty(mDurationFormat(End));
		Description = new SimpleStringProperty(description);

		hboxActions = new HBox();
		initializeButtonViews();
	}

	private void initializeButtonViews() {
		OpenEdit = new Button();
		OpenEdit.setMaxWidth(Double.MAX_VALUE);
		OpenEdit.setText("Edit");
		OpenEdit.getStyleClass().addAll("info", "first");

		RunVLC = new Button();
		RunVLC.setMaxWidth(Double.MAX_VALUE);
		RunVLC.setText("Run");
		RunVLC.getStyleClass().addAll("warning", "middle");
		RunVLC.setTooltip(new Tooltip("This Start playing media considering only this and above exclution"));
		Remove = new Button();
		Remove.getStyleClass().addAll("danger", "last");
		Remove.setMaxWidth(Double.MAX_VALUE);
		Remove.setText("Remove");
		hboxActions.setMinWidth(100);
		HBox.setHgrow(OpenEdit, Priority.ALWAYS);

		hboxActions.getChildren().addAll(getOpenEdit(), getRunVLC(), getRemove());
	}

	@Override
	public int compareTo(FilterVLCTableView o) {
		// TODO Auto-generated method stub
		if (o.getStart().toSeconds() < this.getStart().toSeconds())
			return 1;
		return 0;
	}

	public static String mDurationFormat(Duration a) {
		String ans = " : ";
		int hours = (int) a.toHours();
		int min = (int) a.toMinutes();
		int sec = (int) a.toSeconds();
		sec -= min * 60;
		min -= hours * 60;

		// Prettily look:
		if (sec < 10)
			ans += "0";
		if (min < 10)
			ans = "0" + min + ans;
		else
			ans = min + ans;
		ans = hours + " : " + ans;
		return ans + sec;
	}

	public Duration getStart() {
		return Start;
	}

	public void setStart(Duration start) {
		Start = start;
	}

	public Duration getEnd() {
		return End;
	}

	public void setEnd(Duration end) {
		End = end;
	}

	public HBox getHboxActions() {
		return hboxActions;
	}

	public void setHboxActions(HBox hboxActions) {
		this.hboxActions = hboxActions;
	}

	public Button getRemove() {
		return Remove;
	}

	public void setRemove(Button remove) {
		Remove = remove;
	}

	public Button getOpenEdit() {
		return OpenEdit;
	}

	public void setOpenEdit(Button openEdit) {
		OpenEdit = openEdit;
	}

	public String getShowStart() {
		return ShowStart.get();
	}

	public String getShowEnd() {
		return ShowEnd.get();
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return Description.get();
	}

	public Button getRunVLC() {
		return RunVLC;
	}

	public void setRunVLC(Button runVLC) {
		RunVLC = runVLC;
	}

	public void setDescription(String description) {
		Description = new SimpleStringProperty(description);
	}
}
