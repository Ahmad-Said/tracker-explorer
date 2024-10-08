package said.ahmad.javafx.tracker.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import said.ahmad.javafx.tracker.app.DialogHelper;
import said.ahmad.javafx.tracker.app.Main;
import said.ahmad.javafx.tracker.app.ResourcesHelper;
import said.ahmad.javafx.tracker.app.look.IconLoader;
import said.ahmad.javafx.tracker.app.look.ThemeManager;
import said.ahmad.javafx.tracker.controller.splitview.SplitViewController;
import said.ahmad.javafx.tracker.datatype.MediaCutData;
import said.ahmad.javafx.tracker.model.FilterVLCViewModel;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.tracker.system.services.VLC;
import said.ahmad.javafx.tracker.system.services.VLCException;
import said.ahmad.javafx.tracker.system.tracker.FileTracker;
import said.ahmad.javafx.tracker.system.tracker.FileTrackerHolder;
import said.ahmad.javafx.util.Holder;

public class FilterVLCController {

	@FXML
	private Button generatePlaylist;

	@FXML
	private Button copyRaw;

	@FXML
	private Button pasteRaw;

	@FXML
	private CheckBox autoScene;

	@FXML
	private CheckBox notifyend;

	@FXML
	private Button pickEnd;

	@FXML
	private Button fillEndFromVLCConfig;

	@FXML
	private Button resetStart;

	@FXML
	private Button resetEnd;

	@FXML
	private Button pickStart;

	@FXML
	private Button fillStartFromVLCConfig;

	@FXML
	private TextField inputStart;

	@FXML
	private TextField inputDescription;

	@FXML
	private Label labelDescription;

	@FXML
	private Button Save;

	@FXML
	private TextField inputEnd;

	@FXML
	private Button addToExclusion;

	@FXML
	private TableColumn<FilterVLCViewModel, String> StartTimeColumn;

	@FXML
	private TableColumn<FilterVLCViewModel, String> EndTimeColumn;

	@FXML
	private TableColumn<FilterVLCViewModel, String> DescriptionColumn;

	@FXML
	private TableColumn<FilterVLCViewModel, HBox> ActionsColumn;

	@FXML
	private TableView<FilterVLCViewModel> TimeTable;

	private final ObservableList<FilterVLCViewModel> mDataTable = FXCollections.observableArrayList();
	private FileTracker mfileTracker;
	private PathLayer mPath;

	public static final Image FILTER_ICON_IMAGE = IconLoader.getIconImageForStage(IconLoader.ICON_TYPE.PLAY_MEDIA);

	/**
	 * this is the constructor called if controller is defined from within
	 * FXML(SceneBuilder) when loading FXML so to initialize view be sure to modify
	 * the nested constructed object mean the created one here or use
	 * {@link FXMLLoader#setController(Object)} and remove definition from FXML
	 * file.
	 *
	 * @see <a
	 *      href=https://stackoverflow.com/questions/44487811/javafx-load-fxml-file-inside-controller-but-also-use-netbeanss-make-control>This
	 *      link for another solution</a>
	 */
	public FilterVLCController() {
	}

	/**
	 * this is called from {@linkplain SplitViewController#initializeTable()} at
	 * button openVLC Action} This function required the path is already tracked so
	 * check for it with {@link FileTracker#isTracked() (which already checked
	 * before calling this constructor}
	 *
	 * @param path
	 *            Path of the file to configure
	 */
	public FilterVLCController(PathLayer path) {

		mPath = path;
		mfileTracker = new FileTracker(path.getParentPath(), null);
		mfileTracker.loadMap(path.getParentPath(), true, null);
		// defined to close it later
		Stage filterStage = new Stage();
		filterStage.sizeToScene();
		// didn't work as expected
		// filterStage.setAlwaysOnTop(true);
		// this method is used to get this stage on top always over primary stage
		filterStage.initOwner(Main.getPrimaryStage());
		filterStage.initModality(Modality.WINDOW_MODAL); // good but make alot of lag
		// filterStage.initModality(Modality.APPLICATION_MODAL); // worked good but icon
		// are not combining and cannot close all things

		Parent root;
		Scene scene;
		try {
			// give error exception because "this" object different form controller of
			// loader
			// solution view below
			// root =
			// FXMLLoader.load(ResourcesHelper.getResourceAsURL("/fxml/FilterVLC.fxml"));

			// get the loader alone
			// we can either define load stuff in the empty param constructor
			// see
			// also removed definition of controller from fxml file to prevent double
			// definition
			FXMLLoader loader = new FXMLLoader(ResourcesHelper.getResourceAsURL("/fxml/FilterVLC.fxml"));
			loader.setController(this);
			root = loader.load();
			setGlobalEventHandler(root);
			scene = new Scene(root);
			ThemeManager.applyTheme(scene);

			filterStage.setTitle(mPath.getName() + "  Editor");
			filterStage.setScene(scene);

			filterStage.getIcons().add(FILTER_ICON_IMAGE);

			StartTimeColumn.setCellValueFactory(new PropertyValueFactory<FilterVLCViewModel, String>("startText"));
			EndTimeColumn.setCellValueFactory(new PropertyValueFactory<FilterVLCViewModel, String>("endText"));
			DescriptionColumn.setCellValueFactory(new PropertyValueFactory<FilterVLCViewModel, String>("description"));
			ActionsColumn.setCellValueFactory(new PropertyValueFactory<FilterVLCViewModel, HBox>("hboxActions"));
			TimeTable.setItems(mDataTable);
			resetForm();
			filterStage.show();
			initializeButtons();
			initisalizeTable();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setGlobalEventHandler(Node root) {
		root.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
			if (ev.getCode() == KeyCode.ENTER) {
				addToExclusion.fire();
				ev.consume();
			}
		});
	}

	private void initializeButtons() {
		String tip = "Close VLC at needed location to Detect, Sometimes it doesn't work at end or start of the video";
		pickStart.setTooltip(getTip(tip));
		pickEnd.setTooltip(getTip(tip));
		tip = "Description will show after skipping scene when playing media in vlc.";
		inputDescription.setTooltip(getTip(tip));
		labelDescription.setTooltip(getTip(tip));
	}

	private Tooltip getTip(String cont) {
		Tooltip ms = new Tooltip(cont);
		ms.getStyleClass().add("Tooltip");
		return ms;
	}

	/**
	 * this consider that informations in file are sorted with no conflict in
	 * interval
	 */
	private void initisalizeTable() {

		// initialize data Table
		FileTrackerHolder options = mfileTracker.getMapDetails().get(mPath);
		try {
			if (options.getMediaCutDataUnPrased() != null && !options.getMediaCutDataUnPrased().isEmpty()) {
				options.getMediaCutDataParsed().forEach(mCut -> mDataTable.add(new FilterVLCViewModel(mCut)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// initialize button action row
		TimeTable.setRowFactory(tv -> new TableRow<FilterVLCViewModel>() {

			@Override
			public void updateItem(FilterVLCViewModel t, boolean empty) {
				super.updateItem(t, empty);
				if (t != null) {
					t.getEditButton().setOnAction(new EventHandler<ActionEvent>() {

						@Override
						public void handle(ActionEvent event) {
							inputStart.setText(FilterVLCViewModel.getDurationFormat(t.getStart()));
							inputEnd.setText(FilterVLCViewModel.getDurationFormat(t.getEnd()));
							inputDescription.setText(t.getDescription());
						}
					});
					t.getRunVLCButton().setOnAction(new EventHandler<ActionEvent>() {

						@Override
						public void handle(ActionEvent event) {
							boolean enter = false;
							ArrayList<MediaCutData> list = new ArrayList<MediaCutData>();
							for (FilterVLCViewModel other : mDataTable) {
								if (other == t) {
									enter = true;
								}
								if (enter) {
									list.add(new MediaCutData(other.getStart().toSeconds(), other.getEnd().toSeconds(),
											other.getDescription()));
								}
							}
							boolean isFirst = mDataTable.get(0) == t;
							try {
								VLC.SavePlayListFile(mPath, list, true, isFirst, notifyend.isSelected());
							} catch (VLCException e) {
								DialogHelper.showException(e);
							}
						}
					});
					t.getRemoveButton().setOnAction(new EventHandler<ActionEvent>() {

						@Override
						public void handle(ActionEvent event) {
							mDataTable.remove(t);
						}
					});
				}
			}
		});
	}

	@FXML
	private void AddtoExclusion() {
		String sStart = inputStart.getText();
		String sEnd = inputEnd.getText();
		Duration start = studyFormat(sStart, "Start", true);
		Duration end = studyFormat(sEnd, "End", true);
		if (start == null || end == null) {
			return;
		}
		if (studyConflict(start, end)) {
			String desc = inputDescription.getText();
			desc = desc.replace(">", "<"); // reserved char
			if ((desc.length() < 7 || !desc.trim().startsWith("Scene")) && autoScene.isSelected()) {
				desc = "Scene xx: " + desc;// this will make it enter later in batch numbering after sorting table
			}
			mDataTable.add(new FilterVLCViewModel(start, end, desc));
			resetForm();
			// sort list after defining a comparator for the FilterVLCTableView
			// https://beginnersbook.com/2013/12/java-arraylist-of-object-sort-example-comparable-and-comparator/
			Collections.sort(mDataTable);
			reGenerateSceneNumbering();
		}
	}

	public static Duration studyFormat(String sduration, String fieldName, Boolean warn) {
		if (sduration == null || sduration.isEmpty()) {
			if (warn) {
				DialogHelper.showAlert(AlertType.ERROR, "Add Exclusion", "Missing " + fieldName + " input",
						"Please fill" + fieldName + " Input , use Picker button to get help");
			}
			return null;
		}
		try {
			// getting format of duration:

			// mm:ss
			List<String> split = Arrays.asList(sduration.split(":"));
			for (int i = 0; i < split.size(); i++) {
				split.set(i, split.get(i).replace(" ", "")); // remove all white spaces
			}
			if (split.size() == 2) {
				int min = Integer.parseInt(split.get(0));
				int sec = Integer.parseInt(split.get(1));
				// if (min + sec > 0)
				return Duration.seconds(sec + min * 60);
				// else
				// throw new Exception();
			}
			if (split.size() == 3) {
				int hours = Integer.parseInt(split.get(0));
				int min = Integer.parseInt(split.get(1));
				int sec = Integer.parseInt(split.get(2));
				// if (min + sec + hours > 0)
				return Duration.seconds(sec + min * 60 + hours * 3600);
				// else
				// throw new Exception();
			}

			// ss
			Integer sec = Integer.parseInt(sduration);
			// if (sec > 0)
			return Duration.seconds(sec);
			// else
			// throw new Exception();

		} catch (Exception e) {
			if (warn) {
				DialogHelper.showAlert(AlertType.ERROR, "Add Exclusion", "Format Exception at " + fieldName + " Input",
						"Please Accepted format are:" + "\n\tss\t\t\t(example: 6660)" + "\n\tmm:ss\t\t(example: 110:20)"
								+ "\n\thh:mm:ss\t\t(example: 1:50:20)\n\tOr just use picker button to auto detect");
			}
			// e.printStackTrace();
			return null;
		}
	}

	private boolean studyConflict(Duration start, Duration end) {
		if (start.compareTo(end) >= 0) {
			DialogHelper.showAlert(AlertType.ERROR, "Add Exclusion", "Incomprehensive Interval",
					"The end time must be strictly bigger than start time ");
			return false;
		}

		ArrayList<Integer> intOptions = new ArrayList<Integer>();
		for (FilterVLCViewModel item : mDataTable) {
			intOptions.add((int) item.getStart().toSeconds());
			intOptions.add((int) item.getEnd().toSeconds());
		}
		if (intOptions.size() == 0) {
			return true;
		}

		if (end.toSeconds() < intOptions.get(0)) {
			return true;
		}

		if (start.toSeconds() > intOptions.get(intOptions.size() - 1)) {
			return true;
		}

		for (int i = 0; i < intOptions.size(); i = i + 2) {
			if (start.toSeconds() > intOptions.get(i) && start.toSeconds() < intOptions.get(i + 1)
					|| start.toSeconds() == intOptions.get(i)
					|| end.toSeconds() > intOptions.get(i) && end.toSeconds() < intOptions.get(i + 1)
					|| end.toSeconds() == intOptions.get(i + 1)
					|| intOptions.get(i) > start.toSeconds() && intOptions.get(i) < end.toSeconds()) {
				// https://stackoverflow.com/questions/20413419/javafx-2-how-to-focus-a-table-row-programmatically
				TimeTable.requestFocus();
				TimeTable.getSelectionModel().select(i / 2);
				TimeTable.getFocusModel().focus(0);
				DialogHelper.showAlert(AlertType.ERROR, "Add Exclusion", "Overlapping Interval",
						"The inserted Interval overlap with hightlited interval at row " + (i / 2 + 1));
				return false;
			}

		}

		return true;
	}

	private void reGenerateSceneNumbering() {
		int i = 1;
		for (FilterVLCViewModel t : mDataTable) {
			// 'Scene xx: '...
			// check to index 4
			// if yes remove to index 9
			// reorder with same format see
			// https://stackoverflow.com/questions/12421444/how-to-format-a-number-0-9-to-display-with-2-digits-its-not-a-date
			if (t.getDescription().length() >= 9 && t.getDescription().trim().startsWith("Scene")) {
				t.setDescription("Scene " + String.format("%02d", i) + ": " + t.getDescription().substring(10));
			}
			i++;
		}
	}

	private void resetForm() {
		ResetStart();
		ResetEnd();
		inputDescription.setText("");
	}

	@FXML // this does save to hidden filetracker
	public void saveToMapAndFile() {
		FileTrackerHolder options = mfileTracker.getMapDetails().get(mPath);
		List<String> concatenatedOptions = new ArrayList<String>();
		// clear all track data and get them from observable list
		for (FilterVLCViewModel t : mDataTable) {
			String desc = " ";
			concatenatedOptions.add("" + (int) t.getStart().toSeconds());

			concatenatedOptions.add("" + (int) t.getEnd().toSeconds());
			if (t.getDescription() != null && !t.getDescription().isEmpty()) {
				desc = t.getDescription();
			}
			concatenatedOptions.add(desc);
		}
		options.setMediaCutDataUnPrased(">" + String.join(">", concatenatedOptions));
		mfileTracker.writeMap();
	}

	/**
	 * Generate a PlayList XSPF file with same name next to movie to start it
	 * Independently
	 */
	@FXML
	public void SavePlayListFile() {
		ArrayList<MediaCutData> list = new ArrayList<MediaCutData>();
		for (FilterVLCViewModel other : mDataTable) {
			list.add(
					new MediaCutData(other.getStart().toSeconds(), other.getEnd().toSeconds(), other.getDescription()));
		}
		if (list.isEmpty()) {
			return;
		}
		try {
			VLC.SavePlayListFile(mPath, list, false, true, notifyend.isSelected());
		} catch (VLCException e) {
			DialogHelper.showException(e);
		}
	}

	@FXML
	public void ResetStart() {
		inputStart.setText("00     :00     :00");
	}

	@FXML
	public void ResetEnd() {
		inputEnd.setText("00     :00     :00");
	}

	@FXML
	public void PickStart() {
		pickHelper(TimeMoment.START);
	}

	@FXML
	public void fillStartFromVLCConfig() {
		try {
			inputStart.setText(
					FilterVLCViewModel.getDurationFormat(Duration.millis(VLC.getLastSavedMoment(mPath.toURI()))));
		} catch (VLCException | IOException e) {
			DialogHelper.showException(e);
		}
	}

	@FXML
	public void PickEnd() {
		pickHelper(TimeMoment.END);
	}

	@FXML
	public void fillEndFromVLCConfig() {
		try {
			inputEnd.setText(
					FilterVLCViewModel.getDurationFormat(Duration.millis(VLC.getLastSavedMoment(mPath.toURI()))));
		} catch (VLCException | IOException e) {
			DialogHelper.showException(e);
		}
	}

	private boolean doshowAgain = true;

	enum TimeMoment {
		START, END
	}

	/**
	 * Start vlc at moment in "Start" or "End" field specified in parameter
	 *
	 * @param where
	 *            can be "Start" or "End"
	 */
	private void pickHelper(TimeMoment where) {
		if (doshowAgain) {
			doshowAgain = !DialogHelper.showConfirmationDialog("Pick " + where,
					"Go to position using VLC then close it!\nwe will do the rest.",
					"Note: \n- VLC will now open the file" + "\n- Go to the position you want to make the cut"
							+ "\n- Exit VLC"
							+ "\n- Press thr right sign after you exit VLC");
		}
		Holder<Duration> resume = new Holder<>(null);
		if (where.equals(TimeMoment.START)) {
			String inputStartText = inputStart.getText();
			resume.setValue(studyFormat(inputStartText, where.toString(), false));
		} else {
			String inputEndText = inputEnd.getText();
			resume.setValue(studyFormat(inputEndText, where.toString(), false));
		}
		if (resume.getValue() != null && resume.getValue().toSeconds() == 0) {
			resume.setValue(null);
		}

		try {
			VLC.startVLCAtSpecificMoment(mPath.toURI(), resume.getValue());
		} catch (VLCException e) {
			DialogHelper.showException(e);
		} catch (IOException e) {
			DialogHelper.showException(e);
		}
		// this transition can be used to get focus but annoying
		// filterStage.hide();
		// filterStage.show();
	}

	@FXML
	public void getCopyRaw() {
		// https://stackoverflow.com/questions/6710350/copying-text-to-the-clipboard-using-java
		String myString = "Failed to copy";
		myString = mfileTracker.getMapDetails().get(mPath).getMediaCutDataUnPrased();
		for (FilterVLCViewModel t : mDataTable) {
			myString += ">" + t.getStart().toSeconds() + ">" + t.getEnd().toSeconds() + ">" + t.getDescription();
		}
		// https://docs.oracle.com/javafx/2/api/javafx/scene/input/Clipboard.html
		Clipboard clipboard = Clipboard.getSystemClipboard();
		ClipboardContent content = new ClipboardContent();
		content.putString(myString);
		clipboard.setContent(content);
		DialogHelper.showAlert(AlertType.INFORMATION, "Copy Raw Data", "Content Copied Successfully to Clipboard",
				"Content:\n" + myString + "\nShare it as you like!");
	}

	@FXML
	public void pasteRaw() {
		boolean error = false;
		// https://stackoverflow.com/questions/6534072/how-can-i-break-from-a-try-catch-block-without-throwing-an-exception-in-java
		tryBlock : try {
			// String myString = (String) Toolkit.getDefaultToolkit().getSystemClipboard()
			// .getData(DataFlavor.stringFlavor);
			String myString = Clipboard.getSystemClipboard().getString();
			if (myString == null) {
				error = true;
				break tryBlock;
			}

			List<String> options = Arrays.asList(myString.split(">"));
			String Warn = "";
			if (!options.get(0).equals(mPath.getName())) {
				Warn += "\n- Remark there is a difference in file name.";
			}
			mDataTable.clear();
			resetForm();
			int row = 0;
			for (int i = 3; i < options.size(); i = i + 3) {
				Duration start = Duration.seconds(Double.parseDouble(options.get(i)));
				Duration end = Duration.seconds(Double.parseDouble(options.get(i + 1)));
				if (studyConflict(start, end)) {
					mDataTable.add(new FilterVLCViewModel(start, end, options.get(i + 2)));
					Collections.sort(mDataTable); // is important here for conflict algorithm
					row++;
				}
			}
			if (row == 0) {
				error = true;
				return;
			}

			reGenerateSceneNumbering(); // can be here it just visual
			DialogHelper.showAlert(AlertType.INFORMATION, "Paste Raw Data", "Congrats Content Parsed Successfully",
					"Notes: - " + row + " Rows Parsed without error" + Warn);
		} catch (Exception e) {
			e.printStackTrace();

		}
		if (error) {
			DialogHelper.showTextInputDialog("Paste Raw Data", "Content Parse failed!",
					"SomeThing went wrong,\nAre you Sure You have the Data,\n Try pasting it here to recheck\nReseting DataTable to initial value",
					"Raw Data Here");
			initisalizeTable();
		}
	}
}
