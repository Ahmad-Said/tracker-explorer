package application.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import application.DialogHelper;
import application.FileTracker;
import application.Main;
import application.RunMenu;
import application.StringHelper;
import application.VLC;
import application.WatchServiceHelper;
import application.model.MediaCutData;
import application.model.Setting;
import application.model.TableViewModel;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;

public class SplitViewController {
	static final KeyCombination SHORTCUT_FOCUS_TEXT_FIELD = new KeyCodeCombination(KeyCode.D,
			KeyCombination.SHIFT_DOWN);
	static final KeyCombination SHORTCUT_SEARCH = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN);
	static final KeyCombination SHORTCUT_Clear_Search = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
	static final KeyCombination SHORTCUT_GO_UP = new KeyCodeCombination(KeyCode.UP, KeyCombination.ALT_DOWN);
	static final KeyCombination SHORTCUT_GO_BACK = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.ALT_DOWN);
	static final KeyCombination SHORTCUT_GO_NEXT = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.ALT_DOWN);

	private File mDirectory;
	private WatchServiceHelper mWatchServiceHelper;
	private ObservableList<TableViewModel> DataTable;
	private TableView<TableViewModel> Table;
	private TextField PathField;
	private Button UpButton;
	private Button BackButton;
	private LinkedList<File> BackQueue = new LinkedList<File>();
	private Button NextButton;
	private LinkedList<File> NextQueue = new LinkedList<File>();
	private Button Explorer;
	private TextField SearchFeild; // was search
	private Button SearchButton;
	private WelcomeController parentWelcome;
	private FileTracker mfileTracker;
	private TextField PredictNavigation;
	private boolean isLeft;

	// TableColumn<TableViewModel, ImageView> colIconTestResize;
	public SplitViewController(Path path, Boolean isleft, WelcomeController parent,
			ObservableList<TableViewModel> dataTable, javafx.scene.control.TextField pathField, Button upButton,
			javafx.scene.control.TextField searchFeild, Button searchButton, TableView<TableViewModel> table,
			Button explorer, TableColumn<TableViewModel, HBox> hboxActions, Button backButton, Button nextButton,
			TextField predictNavigation) {
		super();
		// colIconTestResize=colIcon;
		isLeft = isleft;
		DataTable = dataTable;
		PathField = pathField;
		UpButton = upButton;
		NextButton = nextButton;
		BackButton = backButton;
		Explorer = explorer;
		SearchFeild = searchFeild;
		SearchButton = searchButton;
		Table = table;
		parentWelcome = parent;
		PredictNavigation = predictNavigation;
		mDirectory = new File(path.toString());
		initializeSplitButton();
		Table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		mfileTracker = new FileTracker(this);
		PathField.setStyle("-fx-font-size: 14px;");

		//
		initializeTable();
		// initialize column rule comparator
		hboxActions.setComparator(new Comparator<HBox>() {

			@Override
			public int compare(HBox o1, HBox o2) {
				// first children is button watch status
				ToggleButton markseen1 = (ToggleButton) o1.getChildren().get(0);
				if (markseen1.getText().equals("S"))
					return 1;
				return 0;
			}
		});
		mWatchServiceHelper = new WatchServiceHelper(this);
		// refresh();
		// will refresh both at once from welcome controller because resolve may call
		// conflict to call write map and refresh both views
		// untested till now just removed and it work i set datatable the sorted list
		// below
		// Table.setItems(DataTable);
	}

	/**
	 * can ou {@link SplitViewController#UpButton this is link example}
	 * 
	 * @param isLeft
	 *            needed here to separate on click action so if left open the
	 *            directory in the right
	 */
	public void initializeTable() {
		Table.setOnKeyPressed(key -> {
			switch (key.getCode()) {
			case ENTER:
				if (Table.isFocused()) {
					TableViewModel temp = Table.getSelectionModel().getSelectedItem();
					if (temp != null) // it may be table focused but not item selected
						navigate(temp.getName());
				}
				break;
			case BACK_SPACE:
				// if(searchNavigation.isEmpty())
				back();
				// else
				// searchNavigation = searchNavigation.substring(0,
				// searchNavigation.length()-1);
				// System.out.println(searchNavigation);
				break;
			case SPACE:
				if (Table.isFocused()) {
					TableViewModel temp = Table.getSelectionModel().getSelectedItem();
					if (temp != null) {
						temp.getMarkSeen().fire();
					}
				}
				break;
			case LEFT:
				if (!isLeft) {
					String pathName = getSelectedNameIfDirectory();
					if (pathName == null)
						break;
					parentWelcome.SynctoLeft(pathName);
				}
				break;
			case RIGHT:
				if (isLeft) {
					String pathName2 = getSelectedNameIfDirectory();
					if (pathName2 == null)
						break;
					parentWelcome.SynctoRight(pathName2);
				}
				break;
			// TODO check declaration there is a lot of key to define
			case ESCAPE:
				PredictNavigation.setText("");
				break;
			default:

				// PredictNavigation.setText(PredictNavigation.getText()+key.getText());
				// System.out.println(searchNavigation);

				// processSearchNavigation(key.getCode());
				break;
			}
		});

		Table.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
			if (SHORTCUT_FOCUS_TEXT_FIELD.match(e)) {
				PathField.requestFocus();
			} else if (SHORTCUT_SEARCH.match(e)) {
				focusSearchFeild();
			} else if (SHORTCUT_Clear_Search.match(e)) {
				clearSearchFeild();
			} else if (SHORTCUT_GO_UP.match(e)) {
				back();
			} else if (SHORTCUT_GO_BACK.match(e)) {
				BackButton.fire();
			} else if (SHORTCUT_GO_NEXT.match(e)) {
				NextButton.fire();
			}
		});

		Table.setOnMouseClicked(m -> {
			TableViewModel t = Table.getSelectionModel().getSelectedItem();
			if (m.getButton().equals(MouseButton.SECONDARY)) {
				if (t != null) {
					ArrayList<Path> toShow = new ArrayList<>();
					for (TableViewModel tsel : Table.getSelectionModel().getSelectedItems()) {
						toShow.add(tsel.getmFilePath());
					}
					RunMenu.showMenu(toShow);
					return;
				} else
					RunMenu.showMenu(Arrays.asList(mDirectory.toPath()));
			}
			if (t == null)
				return;
			String temp = t.getName();
			boolean tempisDirectory = NametoFile(temp).isDirectory();

			if (m.getButton().equals(MouseButton.PRIMARY) && m.getClickCount() == 2) {
				navigate(temp);
				if (!isLeft && Setting.isBackSync() && tempisDirectory)
					parentWelcome.SynctoLeftParent();
			} else if (isLeft && m.getButton().equals(MouseButton.PRIMARY) && tempisDirectory
					&& Setting.getAutoExpand()) {
				parentWelcome.SynctoRight(temp);
			}
		});
		// https://stackoverflow.com/questions/26220896/showing-tooltips-in-javafx-at-specific-row-position-in-the-tableview
		Table.setRowFactory(tv -> new TableRow<TableViewModel>() {
			private Tooltip tooltip = new Tooltip();

			@Override
			public void updateItem(TableViewModel t, boolean empty) {
				super.updateItem(t, empty);
				if (t == null) {
					setTooltip(null);
				} else {
					// is XSPF start the file directly with custom argument
					if (VLC.isPlaylist(t.getName())) {
						t.getOpenVLC().setOnMouseClicked(m -> {
							VLC.startXSPF(t.getmFilePath());
						});
					}
					if (mfileTracker.isTracked()) {
						String st = mfileTracker.getTooltipText(t);
						if (st != null) {
							tooltip.setText(st);
							tooltip.getStyleClass().addAll("tooltip");
							tooltip.setStyle(
									"-fx-background-color: #7F00FF;-fx-text-fill: white;-fx-font-size:15;-fx-font-weight:bold");
							setTooltip(tooltip);
						}
						t.getmNoteButton().setOnAction(new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent event) {
								String note = DialogHelper.showTextInputDialog("Quick Note Editor",
										"Add Note To see on hover", "Old note Was:\n" + mfileTracker.getTooltipText(t),
										mfileTracker.getTooltipText(t));
								// if null set it to space like it was
								if (note == null)
									return; // keep note unchanged
								if (note.isEmpty())
									note = " "; // reset note if is empty
								// ensure > is not used
								note = note.replace('>', '<');
								mfileTracker.setTooltipText(Table.getSelectionModel().getSelectedItems(), note);
								mfileTracker.setTooltipText(t, note);
							}
						});
						if (VLC.isVLCMediaExt(t.getName())) {
							t.getOpenVLC().setOnMouseClicked(m -> {
								Path path = getDirectoryPath().resolve(t.getName());

								// if it is media file
								if (m.getButton().equals(MouseButton.PRIMARY)) {
									// load the preview
									new FilterVLCController(t.getmFilePath(), mfileTracker);
								} else {
									List<String> options = mfileTracker.getMapDetails().get(t.getName());
									ArrayList<MediaCutData> list = new ArrayList<MediaCutData>();
									// System.out.println(options);
									// later try to remove this if
									if (options.size() > 3) {
										// if the media does contain a setting do load it
										for (int i = 3; i < options.size(); i = i + 3) {
											list.add(new MediaCutData(options.get(i), options.get(i + 1),
													options.get(i + 2)));
										}
										VLC.SavePlayListFile(path, list, true, true, true);
									} else
										// just start the file with remote features
										VLC.watchWithRemote(t.getmFilePath(), "");
								}
							});
						}
						// end if tracked
					} else {
						t.getmNoteButton().setOnAction(new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent event) {
								if (mfileTracker.getAns()) {
									mfileTracker.trackNewFolder();
								}
							}
						});
						if (VLC.isVLCMediaExt(t.getName())) {
							t.getOpenVLC().setOnMouseClicked(m -> {
								if (m.getButton().equals(MouseButton.PRIMARY)) {
									if (mfileTracker.getAns()) {
										mfileTracker.trackNewFolder();
									}
								} else {
									VLC.watchWithRemote(t.getmFilePath(), "");
								}
							});
						}
					}

				}
			}
		});
	}

	private String getSelectedNameIfDirectory() {
		TableViewModel t = Table.getSelectionModel().getSelectedItem();
		if (t == null)
			return null;
		String temp = t.getName();
		boolean tempisDirectory = NametoFile(temp).isDirectory();
		if (!tempisDirectory)
			return null;
		return temp;
	}
	// old way
	// public void search() {
	// String s = SearchFeild.getText().trim().toLowerCase().replaceAll("\\s+", "");
	// // this replace remove white spaces
	// ObservableList<TableViewModel> listcopy =
	// FXCollections.observableArrayList();
	// // if(s.equals("!"))
	// // return;
	// if (s.equals(" ") || s.isEmpty()) {
	// refresh();
	// } else {
	// // if(s.substring(0, 0).equals("!"))
	// // {
	// // refresh();
	// // s = s.substring(1);
	// // for(String st: mChildrenList)
	// // if(!st.toLowerCase().contains(s))
	// // listcopy.add(st);
	// // }
	// // else
	// for (TableViewModel st1 : DataTable)
	// if (st1.getName().toLowerCase().contains(s)
	// || ("is seen yes".contains(s) && "s".contains(st1.getMarkSeen().getText()))
	// || ("un not no".contains(s) && "u".contains(st1.getMarkSeen().getText())))
	// listcopy.add(st1);
	// DataTable.clear();
	// for (TableViewModel st : listcopy)
	// DataTable.add(st);
	// }
	//
	// }

	private void initializeSplitButton() {

		UpButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				back();
			}
		});

		BackButton.setDisable(true);
		BackButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				NextQueue.add(mDirectory);
				NextButton.setDisable(false);
				File temp = BackQueue.removeLast();
				if (temp.exists())
					mDirectory = temp;
				if (BackQueue.isEmpty()) {
					BackButton.setDisable(true);
				}
				refresh();
			}
		});
		NextButton.setDisable(true);
		NextButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				BackQueue.add(mDirectory);
				BackButton.setDisable(false);
				File temp = NextQueue.removeLast();
				if (temp.exists())
					mDirectory = temp;
				if (NextQueue.isEmpty()) {
					NextButton.setDisable(true);
				}
				refresh();
			}
		});

		Explorer.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				try {
					Desktop.getDesktop().open(mDirectory);
				} catch (IOException e) {
					// e.printStackTrace();
				}
			}
		});

		// https://code.makery.ch/blog/javafx-8-tableview-sorting-filtering/
		FilteredList<TableViewModel> filteredData = new FilteredList<>(DataTable, p -> true);
		SearchFeild.textProperty().addListener((observable, oldValue, newValue) -> {
			filteredData.setPredicate(model -> {
				// If filter text is empty, display all.
				if (newValue == null || newValue.isEmpty()) {
					return true;
				}

				// Compare with filter text.
				String lowerCaseFilter = newValue.toLowerCase();

				if (model.getName().toLowerCase().contains(lowerCaseFilter)) {
					return true; // Filter matches name.
				} else if (mfileTracker.getTooltipText(model) != null
						&& mfileTracker.getTooltipText(model).toLowerCase().contains(lowerCaseFilter))
					return true; // search note if exist
				else if ((model.getMarkSeen().getText().equals("U") || model.getMarkSeen().getText().equals("-"))
						&& "un not".toLowerCase().contains(lowerCaseFilter)) {
					return true; // Filter unseen.
				} else if (model.getMarkSeen().getText().equals("S")
						&& "is yes seen".toLowerCase().contains(lowerCaseFilter)) {
					return true;// Filter seen.
				} else
					return false; // Does not match.
			});
		});

		SortedList<TableViewModel> sortedData = new SortedList<>(filteredData);
		sortedData.comparatorProperty().bind(Table.comparatorProperty());
		Table.setItems(sortedData);

		// SearchButton.setOnAction(e -> search());
		// scroll on button search to automatically clear
		// the search feild

		// https://stackoverflow.com/questions/29735651/mouse-scrolling-in-java-fx
		SearchButton.setOnScroll((ScrollEvent event) -> {
			// Adjust the zoom factor as per your requirement
			double deltaY = event.getDeltaY();
			if (deltaY < 0) {
				SearchFeild.setText("un");
			} else {
				SearchFeild.setText("yes");
			}
		});

		SearchButton.setOnAction(e -> {
			refresh();
			SearchFeild.clear();
		});
	}

	public File NametoFile(String name) {
		String selectedPath = mDirectory.getAbsolutePath() + File.separator + name;
		File selectedFile = new File(selectedPath);
		return selectedFile;
	}

	// public static int count = 1; // optimized !

	public void refresh() {
		// System.out.println(count);
		// count++;
		// if(count>10)
		// try {
		// throw new Exception();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		mfileTracker.loadMap();
		mfileTracker.resolveConflict();
		showList(getCurrentFilesList());

		PathField.setText(mDirectory.getAbsolutePath());
		Main.UpdateTitle(mDirectory.getName());
		mWatchServiceHelper.changeObservableDirectory(mDirectory.toPath());
	}

	public TextField getPathField() {
		return PathField;
	}

	public List<Path> getSelection() {
		List<Path> selection = new ArrayList<>();
		for (TableViewModel item : Table.getSelectionModel().getSelectedItems()) {
			selection.add(mDirectory.toPath().resolve(item.getName()));
		}
		return selection;
	}

	public void select(String regex) {
		if (regex.startsWith("*"))
			regex = "." + regex;
		Table.getSelectionModel().clearSelection();
		for (int i = 0; i < DataTable.size(); ++i) {
			TableViewModel model = DataTable.get(i);
			String item = model.getName();
			if (item.matches(regex) || StringHelper.containsWord(item, regex)) {
				Table.getSelectionModel().select(model);
			}
		}
	}

	public String[] getCurrentFilesList() {
		File[] listFiles = mDirectory.listFiles(file -> !file.isHidden());
		if (listFiles == null) {
			listFiles = new File[0];
		}

		Arrays.sort(listFiles, (f1, f2) -> {
			if ((f1.isDirectory() && f2.isDirectory()) || (f1.isFile() && f2.isFile())) {
				return f1.compareTo(f2);
			}
			return f1.isDirectory() ? -1 : 1;
		});

		String[] list = new String[listFiles.length];
		for (int i = 0; i < list.length; ++i) {
			list[i] = listFiles[i].getName();
			// System.out.println(list[i]);
			// this is working
		}
		return list;
	}

	private void showList(String[] list) {
		// if (list != null) {
		DataTable.clear();
		Path dirPath = this.getDirectoryPath();
		for (String string : list) {
			TableViewModel t = new TableViewModel(" ", string, dirPath.resolve(string));
			// assgin later here the status and the button
			initializeButtons(t);
			DataTable.add(t);
			// System.out.println(string); this is working
			// System.out.println(DataTable); seem good
		}
		// } else {
		// DataTable.clear();
		// }
	}

	// we have to set on action here because this controller
	// contact with File Tracker
	// TODO try to use table row factory instead
	/**
	 * JavaDoc of initializeButtons(). This method executes
	 * {@link SplitViewController#initializeTable()}
	 */
	private void initializeButtons(TableViewModel t) {

		// property of toggle button from map
		if (mfileTracker.isTracked()) {
			boolean isSeen = false;
			isSeen = (mfileTracker.getSeen(t).equals("0")) ? false : true;
			if (isSeen) {
				t.getMarkSeen().setText("S");
				t.getMarkSeen().setSelected(true);
				t.getMarkSeen().getStyleClass().add("success");
			} else {
				t.getMarkSeen().setText("U");
				t.getMarkSeen().setSelected(false);
				t.getMarkSeen().getStyleClass().add("info");
			}
			t.setStatus(mfileTracker.getTooltipText(t));

		} else {
			t.getMarkSeen().setText("-");
			t.getMarkSeen().setSelected(false);
		}

		// action method for toggle will make all selection to toggle
		t.getMarkSeen().setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				// toggle all selected items
				// reserve selection before refreshing the table
				int[] toSelect = new int[Table.getSelectionModel().getSelectedItems().size()];
				// int tothis = DataTable.indexOf(t);
				int j = 0;
				for (int i : Table.getSelectionModel().getSelectedIndices())
					toSelect[j++] = i;
				mfileTracker.toggleSelectionSeen(Table.getSelectionModel().getSelectedItems(),
						XSPFrelatedWithSelection(t), t);
				// restore reserve
				Table.getSelectionModel().selectIndices(-1, toSelect);
				Table.requestFocus();
			}
		});
	}

	private ArrayList<TableViewModel> XSPFrelatedWithSelection(TableViewModel clicked) {

		// if XSPF is clicked also auto sync seen its video files if exist

		// to collect all model to sync
		ArrayList<TableViewModel> Allrelated = new ArrayList<>();

		// to collect all base name of XSPF
		Map<String, TableViewModel> mapAllXSPF = new HashMap<String, TableViewModel>();
		;

		// to include clicked in below for loop
		// Table.getSelectionModel().select(DataTable.indexOf(clicked));
		ArrayList<TableViewModel> tempover = new ArrayList<>();
		tempover.addAll(Table.getSelectionModel().getSelectedItems());
		tempover.add(clicked);

		for (TableViewModel t : tempover) {
			String ext = StringHelper.getExtention(t.getName());
			if (ext.equals("XSPF")) {
				String basename = t.getName().substring(0, t.getName().length() - 15).toUpperCase();
				mapAllXSPF.put(basename, t);
			}
		}
		for (TableViewModel tsearch : DataTable) {
			String tbase = StringHelper.getBaseName(tsearch.getName());
			if (mapAllXSPF.containsKey(tbase)) {
				mfileTracker.setSeen(tsearch, mfileTracker.getSeen(mapAllXSPF.get(tbase)));
				// first if -> to force toggle if only video is selected and clicked on XSPF
				// second if ->to prevent double toggle
				if (Table.getSelectionModel().getSelectedItems().size() == 1
						|| !Table.getSelectionModel().getSelectedItems().contains(tsearch))
					Allrelated.add(tsearch);
			}
		}
		if (Allrelated.size() == 0)
			return null;
		return Allrelated;
	}

	public void openFile(File file) {
		if (!file.exists()) {
			refresh();
			return;
		}
		if (file.isDirectory()) {
			mDirectory = file;
			refresh();
		} else if (file.isFile()) {
			try {
				Desktop.getDesktop().open(file);
			} catch (Exception e) {
				DialogHelper.showException(e);
			}
		}
	}

	public void navigate(String name) {
		String selectedPath = mDirectory.getAbsolutePath() + File.separator + name;
		File selectedFile = new File(selectedPath);
		if (selectedFile.isDirectory()) {
			AddToQueue(mDirectory);
			EmptyNextQueue();
			mDirectory = selectedFile;
			refresh();
		} else {
			try {
				// if(name.substring(name.length()-4)).toLowerCase().equals(".bat"))

				Desktop.getDesktop().open(selectedFile);
			} catch (Exception e) {
				DialogHelper.showException(e);
			}
		}
	}

	private void EmptyNextQueue() {
		NextQueue.clear();
		NextButton.setDisable(true);
	}

	private void AddToQueue(File file) {
		BackQueue.add(file);
		BackButton.setDisable(false);
	}

	public void syncForced() {

	}

	// I MEAN BY THIS FUNCTION THE UP BUTTON
	// the parent directory was on old version of this application
	// named like this
	private void back() {
		File parent = mDirectory.getParentFile();
		if (parent != null) {
			AddToQueue(mDirectory);
			EmptyNextQueue();
			mDirectory = parent;
			if (mDirectory.exists()) {
				refresh();
			} else {
				back();
			}
		}
	}

	public File getmDirectory() {
		return mDirectory;
	}

	// this method is usefull to handle external call
	// refresh will make change the view to specified directory
	// and so adding old directory to queue and so on
	public void setmDirectoryThenRefresh(File mDirectory) {
		AddToQueue(this.mDirectory);
		EmptyNextQueue();
		this.mDirectory = mDirectory;
		refresh();
	}

	// special use like for rename and do not Queue
	public void setmDirectory(File mdirectory) {
		mDirectory = mdirectory;
	}

	public Path getDirectoryPath() {
		return mDirectory.toPath();
	}

	public boolean isFocused() {
		return this.Table.isFocused() || this.UpButton.isFocused() || this.SearchButton.isFocused()
				|| this.SearchFeild.isFocused();
	}

	public void requestFocus() {
		this.Table.requestFocus();

	}

	public WelcomeController getParentWelcome() {
		return parentWelcome;
	}

	public void setParentWelcome(WelcomeController parentWelcome) {
		this.parentWelcome = parentWelcome;
	}

	public FileTracker getMfileTracker() {
		return mfileTracker;
	}

	public void setMfileTracker(FileTracker mfileTracker) {
		this.mfileTracker = mfileTracker;
	}

	public void focusSearchFeild() {
		SearchFeild.requestFocus();
	}

	public void RevealINExplorer() {
		if (Table.getSelectionModel().getSelectedItem() != null) {
			// https://stackoverflow.com/questions/7357969/how-to-use-java-code-to-open-windows-file-explorer-and-highlight-the-specified-f
			String cmd = "explorer.exe /select,"
					+ mDirectory.toPath().resolve(Table.getSelectionModel().getSelectedItem().getName());

			try {
				Runtime.getRuntime().exec(cmd);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// later do make it multiple selection not working as follow
			// List<Path> paths = getSelection();
			// try {
			// for (Path path : paths)
			// cmd += path.toAbsolutePath();
			// System.out.println(cmd);
			// Runtime.getRuntime().exec(cmd);
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
		} else
			Explorer.fire();
	}

	// is this function causing the warinining ?
	// May 03, 2019 8:02:57 PM com.sun.javafx.scene.control.skin.VirtualFlow
	// addTrailingCells
	// INFO: index exceeds maxCellCount. Check size calculations for class
	// application.SplitViewController$1

	public void clearSearchFeild() {
		// TableViewModel t = Table.getSelectionModel().getSelectedItem();
		// Table.requestFocus();
		// Table.getSelectionModel().select( t);
		// System.out.println( t);
		// Table.scrollTo( t);
		SearchFeild.setText("");
		Table.scrollTo(Table.getSelectionModel().getSelectedIndex() - 5); // for a better view
		// TODO optional learn how also if key moved to start from it ...
		// Table.getSelectionModel().select(Table.getSelectionModel().getSelectedItem());
		// Table.scrollTo( t);
		// Table.requestFocus();
		// Table.getSelectionModel().select( t);
	}

	public void focusTable() {
		if (Table.getSelectionModel().getSelectedCells().size() <= 0)
			// Table.getSelectionModel().select(0);
			Table.getSelectionModel().selectFirst();
		Table.requestFocus();
	}

	// TODO testing
	public boolean isFocusedTable() {
		return Table.isFocused();
		// return Table.getSelectionModel().getSelectedCells().size() != 0 ;
		// return Table.isFocused(); not working ??
	}

	public Button getNextButton() {
		return NextButton;
	}

	public void setNextButton(Button nextButton) {
		NextButton = nextButton;
	}

	public Button getBackButton() {
		return BackButton;
	}

	public void setBackButton(Button backButton) {
		BackButton = backButton;
	}

}
