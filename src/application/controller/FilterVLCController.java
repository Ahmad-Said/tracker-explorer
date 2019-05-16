package application.controller;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import application.DialogHelper;
import application.FileTracker;
import application.Main;
import application.VLC;
import application.model.FilterVLCTableView;
import application.model.MediaCutData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

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
	private Button resetStart;

	@FXML
	private Button resetEnd;

	@FXML
	private Button pickStart;

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
	private TableColumn<FilterVLCTableView, String> StartTimeColumn;

	@FXML
	private TableColumn<FilterVLCTableView, String> EndTimeColumn;

	@FXML
	private TableColumn<FilterVLCTableView, String> DescriptionColumn;

	@FXML
	private TableColumn<FilterVLCTableView, HBox> ActionsColumn;

	@FXML
	private TableView<FilterVLCTableView> TimeTable;

	private ObservableList<FilterVLCTableView> mDataTable = FXCollections.observableArrayList();
	private FileTracker mfileTracker;
	private Path mPath;
	private Stage filterStage; // defined to close it later

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
	 * this is called from {@linkplain SplitViewController#initializeTable(Boolean)
	 * at button openVLC Action} This function required the path is already tracked
	 * so check for it with {@link FileTracker#isTracked()}
	 * 
	 * @param path
	 *            Path of the file to configure
	 * @param mfileTracker
	 *            It's tracker came from it's parent
	 *            {@link SplitViewController#getMfileTracker()}
	 */
	public FilterVLCController(Path path, FileTracker mfileTracker) {

		mPath = path;
		this.mfileTracker = mfileTracker;
		filterStage = new Stage();
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
			// root = FXMLLoader.load(getClass().getResource("/fxml/FilterVLC.fxml"));

			// get the loader alone
			// we can either define load stuff in the empty param constructor
			// see
			// also removed definition of controller from fxml file to prevent double
			// definition
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FilterVLC.fxml"));
			loader.setController(this);
			root = loader.load();
			setGlobalEventHandler(root);
			scene = new Scene(root);

			scene.getStylesheets().add("/css/bootstrap3.css");

			filterStage.setTitle(mPath.getFileName() + "  Editor");
			filterStage.setScene(scene);

			filterStage.getIcons().add(new Image(Main.class.getResourceAsStream("/img/welcome.png")));

			StartTimeColumn.setCellValueFactory(new PropertyValueFactory<FilterVLCTableView, String>("ShowStart"));
			EndTimeColumn.setCellValueFactory(new PropertyValueFactory<FilterVLCTableView, String>("ShowEnd"));
			DescriptionColumn.setCellValueFactory(new PropertyValueFactory<FilterVLCTableView, String>("Description"));
			ActionsColumn.setCellValueFactory(new PropertyValueFactory<FilterVLCTableView, HBox>("hboxActions"));
			TimeTable.setItems(mDataTable);
			resetForm();
			filterStage.show();
			initializeButtons();
			initisalizeTable();

		} catch (IOException e) {
			// e.printStackTrace();
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
	 * 
	 * @see FileTracker#FileTracker() # check private definition convention of map
	 *      details also notes that input are in pairs
	 */
	private void initisalizeTable() {

		// initialize data Table
		List<String> options = mfileTracker.getMapDetails().get(mPath.getFileName().toString());
		// System.out.println(options);
		// later try to remove this if
		if (options.size() > 3) {
			for (int i = 3; i < options.size(); i = i + 3) {
				mDataTable.add(new FilterVLCTableView(options.get(i), options.get(i + 1), options.get(i + 2)));
			}
		}

		// initialize button action row
		TimeTable.setRowFactory(tv -> new TableRow<FilterVLCTableView>() {

			@Override
			public void updateItem(FilterVLCTableView t, boolean empty) {
				super.updateItem(t, empty);
				if (t != null) {
					t.getOpenEdit().setOnAction(new EventHandler<ActionEvent>() {

						@Override
						public void handle(ActionEvent event) {
							inputStart.setText(FilterVLCTableView.mDurationFormat(t.getStart()));
							inputEnd.setText(FilterVLCTableView.mDurationFormat(t.getEnd()));
							inputDescription.setText(t.getDescription());
						}
					});
					t.getRunVLC().setOnAction(new EventHandler<ActionEvent>() {

						@Override
						public void handle(ActionEvent event) {
							boolean enter = false;
							ArrayList<MediaCutData> list = new ArrayList<MediaCutData>();
							for (FilterVLCTableView other : mDataTable) {
								if (other == t)
									enter = true;
								if (enter) {
									list.add(new MediaCutData(other.getStart().toSeconds(), other.getEnd().toSeconds(),
											other.getDescription()));
								}
							}
							// System.out.println(list);
							boolean isFirst = false;
							if (mDataTable.get(0) == t)
								isFirst = true;
							VLC.SavePlayListFile(mPath, list, true, isFirst, notifyend.isSelected());
						}
					});
					t.getRemove().setOnAction(new EventHandler<ActionEvent>() {

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
		if (start == null || end == null)
			return;
		if (studyConflict(start, end)) {
			String desc = inputDescription.getText();
			desc = desc.replace(">", "<"); // reserved char
			if ((desc.length() < 7 || !desc.trim().substring(0, 5).equals("Scene")) && autoScene.isSelected())
				desc = "Scene xx: " + desc;// this will make it enter later in batch numbering after sorting table
			mDataTable.add(new FilterVLCTableView(start, end, desc));
			resetForm();
			// sort list after defining a comparator for the FilterVLCTableView
			// https://beginnersbook.com/2013/12/java-arraylist-of-object-sort-example-comparable-and-comparator/
			Collections.sort(mDataTable);
			reGenerateSceneNumbering();
		}
	}

	public static Duration studyFormat(String sduration, String where, Boolean warn) {
		if (sduration == null || sduration.isEmpty()) {
			if (warn)
				DialogHelper.showAlert(AlertType.ERROR, "Add Exclusion", "Missing " + where + " input",
						"Please fill" + where + " Input , use Picker button to get help");
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
			if (warn)
				DialogHelper.showAlert(AlertType.ERROR, "Add Exclusion", "Format Exception at " + where + " Input",
						"Please Accepted format are:" + "\n\tss\t\t\t(example: 6660)" + "\n\tmm:ss\t\t(example: 110:20)"
								+ "\n\thh:mm:ss\t\t(example: 1:50:20)\n\tOr just use picker button to auto detect");
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
		for (FilterVLCTableView item : mDataTable) {
			intOptions.add((int) item.getStart().toSeconds());
			intOptions.add((int) item.getEnd().toSeconds());
		}
		if (intOptions.size() == 0)
			return true;

		if (end.toSeconds() < intOptions.get(0))
			return true;

		if (start.toSeconds() > intOptions.get(intOptions.size() - 1))
			return true;

		for (int i = 0; i < intOptions.size(); i = i + 2) {
			if ((start.toSeconds() > intOptions.get(i) && start.toSeconds() < intOptions.get(i + 1))
					|| start.toSeconds() == intOptions.get(i)
					|| (end.toSeconds() > intOptions.get(i) && end.toSeconds() < intOptions.get(i + 1))
					|| end.toSeconds() == intOptions.get(i + 1)
					|| (intOptions.get(i) > start.toSeconds() && intOptions.get(i) < end.toSeconds())) {
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
		for (FilterVLCTableView t : mDataTable) {
			// 'Scene xx: '...
			// check to index 4
			// if yes remove to index 9
			// reorder with same format see
			// https://stackoverflow.com/questions/12421444/how-to-format-a-number-0-9-to-display-with-2-digits-its-not-a-date
			// System.out.println(t.getDescription().length() + t.getDescription());
			if (t.getDescription().length() >= 9 && t.getDescription().trim().substring(0, 5).equals("Scene")) {
				// System.out.println(t.getDescription().substring(10));
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
		String key = mPath.getFileName().toString();
		List<String> options = mfileTracker.getMapDetails().get(key);
		ArrayList<String> newCopy = new ArrayList<String>();
		for (int i = 0; i < 3; i++) {
			newCopy.add(options.get(i));
		}
		// clear all track data and get them from observable list
		for (FilterVLCTableView t : mDataTable) {
			String desc = " ";
			newCopy.add("" + (int) t.getStart().toSeconds());
			newCopy.add("" + (int) t.getEnd().toSeconds());
			if (t.getDescription() != null && !t.getDescription().isEmpty())
				desc = t.getDescription();
			newCopy.add(desc);
		}
		mfileTracker.getMapDetails().put(key, newCopy);
		mfileTracker.writeMap();
	}

	@FXML // this generate a PlayList XSPF file with same name next to movie to start it
			// Independently
	public void SavePlayListFile() {
		ArrayList<MediaCutData> list = new ArrayList<MediaCutData>();
		for (FilterVLCTableView other : mDataTable) {
			list.add(
					new MediaCutData(other.getStart().toSeconds(), other.getEnd().toSeconds(), other.getDescription()));
		}
		if (list.isEmpty())
			return;
		VLC.SavePlayListFile(mPath, list, false, true, notifyend.isSelected());
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
		int sec = pickHelper("Start");
		inputStart.setText(FilterVLCTableView.mDurationFormat(Duration.millis((sec))));
	}

	@FXML
	public void PickEnd() {
		int sec = pickHelper("End");
		inputEnd.setText(FilterVLCTableView.mDurationFormat(Duration.millis(sec)));
	}

	private boolean doshowAgain = true;

	private int pickHelper(String where) {
		if (doshowAgain)
			doshowAgain = !DialogHelper.showConfirmationDialog("Pick " + where,
					"Go to position using VLC then close it!\nwe will do the rest.",
					"Note: \n- VLC will now open the file" + "\n- Sometimes start and end video are not well detected"
							+ "\n- The program in suspend waiting vlc do not close it"
							+ "\n- Press OK to not Show Again");
		Duration resume = null;
		if (where.equals("Start")) {
			String sStart = inputStart.getText();
			resume = studyFormat(sStart, "Start", false);
		} else {
			String sEnd = inputEnd.getText();
			resume = studyFormat(sEnd, "End", false);
		}
		if (resume.toSeconds() == 0)
			resume = null;
		int sec = VLC.pickTime(mPath, resume);
		// this transition can be used to get focus but annoying
		// filterStage.hide();
		// filterStage.show();

		return sec;
	}

	@FXML
	public void getcopyRaw() {
		// https://stackoverflow.com/questions/6710350/copying-text-to-the-clipboard-using-java
		String myString = "Failed to copy";
		myString = String.join(">",
				String.join(">", mfileTracker.getMapDetails().get(mPath.getFileName().toString()).subList(0, 3)));
		for (FilterVLCTableView t : mDataTable) {
			myString += ">" + t.getStart().toSeconds() + ">" + t.getEnd().toSeconds() + ">" + t.getDescription();
		}
		StringSelection stringSelection = new StringSelection(myString);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
		DialogHelper.showAlert(AlertType.INFORMATION, "Copy Raw Data", "Content Copied Successfully to Clipboard",
				"Content:\n" + myString + "\nShare it as you like!");
	}

	@FXML
	public void pasteRaw() {
		try {
			String myString = (String) Toolkit.getDefaultToolkit().getSystemClipboard()
					.getData(DataFlavor.stringFlavor);

			List<String> options = Arrays.asList(myString.split(">"));
			String Warn = "";
			if (!options.get(0).equals(mPath.getFileName().toString()))
				Warn += "\n- Remark there is a difference in file name.";
			mDataTable.clear();
			resetForm();
			int row = 0;
			for (int i = 3; i < options.size(); i = i + 3) {
				Duration start = Duration.seconds(Double.parseDouble(options.get(i)));
				Duration end = Duration.seconds(Double.parseDouble(options.get(i + 1)));
				// System.out.println(start.toSeconds());
				// System.out.println(end.toSeconds());
				if (studyConflict(start, end)) {
					mDataTable.add(new FilterVLCTableView(start, end, options.get(i + 2)));
					Collections.sort(mDataTable); // is important here for conflict algorithm
					row++;
				}
			}
			if (row == 0)
				throw new IOException();
			reGenerateSceneNumbering(); // can be here it just visual
			// System.out.println(myString);
			DialogHelper.showAlert(AlertType.INFORMATION, "Paste Raw Data", "Congrats Content Parsed Successfully",
					"Notes: - " + row + " Rows Parsed without error" + Warn);
		} catch (HeadlessException | UnsupportedFlavorException | IOException e) {
			// e.printStackTrace();
			DialogHelper.showTextInputDialog("Paste Raw Data", "Content Parse failed!",
					"SomeThing went wrong,\nAre you Sure You have the Data,\n Try pasting it here to recheck\nReseting DataTable to initial value",
					"Raw Data Here");
			initisalizeTable();
		}
	}
}
