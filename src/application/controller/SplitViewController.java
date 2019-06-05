package application.controller;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import application.DialogHelper;
import application.FileHelper;
import application.FileTracker;
import application.Main;
import application.RecursiveFileWalker;
import application.RunMenu;
import application.StringHelper;
import application.VLC;
import application.WatchServiceHelper;
import application.WindowsShortcut;
import application.model.MediaCutData;
import application.model.Setting;
import application.model.TableViewModel;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
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
import javafx.util.Pair;

public class SplitViewController {
	static final KeyCombination SHORTCUT_FOCUS_TEXT_FIELD = new KeyCodeCombination(KeyCode.D,
			KeyCombination.SHIFT_DOWN);
	static final KeyCombination SHORTCUT_SEARCH = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN);
	static final KeyCombination SHORTCUT_RECURSIVE = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN);
	static final KeyCombination SHORTCUT_Clear_Search = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
	static final KeyCombination TOGGLE_FAVORITE = new KeyCodeCombination(KeyCode.F, KeyCombination.SHIFT_DOWN);
	static final KeyCombination SHORTCUT_GO_UP = new KeyCodeCombination(KeyCode.UP, KeyCombination.ALT_DOWN);
	static final KeyCombination SHORTCUT_GO_BACK = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.ALT_DOWN);
	static final KeyCombination SHORTCUT_GO_NEXT = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.ALT_DOWN);

	private File mDirectory;
	private WatchServiceHelper mWatchServiceHelper = null;
	private ObservableList<TableViewModel> DataTable;
	SortedList<TableViewModel> sortedData;
	private TableView<TableViewModel> Table;
	private TextField PathField;
	private Button UpButton;
	private Button BackButton;
	private LinkedList<File> BackQueue = new LinkedList<File>();
	private Button NextButton;
	private LinkedList<File> NextQueue = new LinkedList<File>();
	private Button Explorer;
	private TextField SearchFeild; // was search
	private Button ClearButton;
	private WelcomeController parentWelcome;
	private FileTracker mfileTracker;
	private TextField PredictNavigation;
	private CheckBox recursiveSearch;
	private Label LabelItemsNumber;
	private Button NavigateRecursive;
	private boolean isLeft;
	// this always respect the path field pattern
	// if anything goes wrong return to it
	private String truePathField;
	private static List<String> SpecialPath = Arrays.asList("/", "?", "&", "|");

	public SplitViewController() {
	};

	// TableColumn<TableViewModel, ImageView> colIconTestResize;
	public SplitViewController(Path path, Boolean isleft, WelcomeController parent,
			ObservableList<TableViewModel> dataTable, javafx.scene.control.TextField pathField, Button upButton,
			javafx.scene.control.TextField searchFeild, Button clearButton, TableView<TableViewModel> table,
			Button explorer, TableColumn<TableViewModel, HBox> hboxActions, Button backButton, Button nextButton,
			TextField predictNavigation, CheckBox recursivesearch, Label labelItemsNumber, Button navigateRecursive) {
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
		ClearButton = clearButton;
		Table = table;
		parentWelcome = parent;
		PredictNavigation = predictNavigation;
		recursiveSearch = recursivesearch;
		LabelItemsNumber = labelItemsNumber;
		mDirectory = new File(path.toString());
		truePathField = mDirectory.getAbsolutePath();
		NavigateRecursive = navigateRecursive;
		Table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		mfileTracker = new FileTracker(this);

		initializeSplitButton();
		initializePathField();

		initializeTable();
		// initialize column rule comparator
		hboxActions.setComparator(new Comparator<HBox>() {

			@Override
			public int compare(HBox o1, HBox o2) {
				// first children is button watch status
				// TableRow<TableViewModel> row = (TableRow<TableViewModel>) o1.getParent();
				// TODO
				if (o1.getChildren().size() == 0)
					return -1;
				ToggleButton markseen1 = (ToggleButton) o1.getChildren().get(0);
				if (markseen1.getText().equals("S"))
					return 1;
				return 0;
			}
		});
		// hboxActions.setCellFactory(col -> new TableCell<TableViewModel, HBox>() {
		//
		// protected void updateItem(HBox item, boolean empty) {
		// super.updateItem(item, empty);
		// // TableViewModel t = (TableViewModel) this.getTableRow().getItem();
		// // // if (item.getChildren().isEmpty())
		// // if (t != null)
		// // t.initializerRowFactory();
		//
		// }
		// });
		mWatchServiceHelper = new WatchServiceHelper(this);
	}

	private void initializePathField() {
		PathField.setStyle("-fx-font-size: 14px;");
		PathField.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				setPathFieldThenRefresh(PathField.getText());
			}
		});
	}

	// https://howtodoinjava.com/java/multi-threading/java-thread-pool-executor-example/
	ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
	private boolean doBack = true;
	// first use : prevent excessive back when cleaning prediction text with
	private Thread enablemisback = new Thread() {

		public void run() {
			try {
				TimeUnit.MILLISECONDS.sleep(500);
				// to prevent mis back button
				doBack = true;
			} catch (InterruptedException e) {
				// e.printStackTrace();
			}
		}
	};

	private boolean doUp = false;
	// second use: do excessive up(goParent function)
	private Thread doUpThreadOff = new Thread() {

		public void run() {
			try {
				TimeUnit.MILLISECONDS.sleep(1000);
				// to prevent mis back button
				doUp = false;
			} catch (InterruptedException e) {
				// e.printStackTrace();
			}
		}
	};

	/**
	 * can ou {@link SplitViewController#UpButton this is link example}
	 * 
	 * @param isLeft
	 *            needed here to separate on click action so if left open the
	 *            directory in the right
	 */
	public void initializeTable() {

		Table.setOnKeyPressed(key -> {
			String test = PredictNavigation.getText().trim();
			switch (key.getCode()) {
			case ENTER:
				if (Table.isFocused()) {
					TableViewModel temp = Table.getSelectionModel().getSelectedItem();
					if (temp != null) // it may be table focused but not item selected
						navigate(temp.getmFilePath());
				}
				break;
			case BACK_SPACE:
				if (doBack)
					back();
				else {
					if (!test.isEmpty()) {
						test = test.substring(0, test.length() - 1);
						PredictNavigation.setText(test);
					} else {
						executor.execute(enablemisback);
					}
				}
				break;
			case SPACE:
				// to do here if i make a selection using prediction do not make so
				// always space do mark seen and auto enter space is enabled
				TableViewModel temp = Table.getSelectionModel().getSelectedItem();
				// if (!test.isEmpty())
				// PredictNavigation.insertText(PredictNavigation.getText().length(), " ");
				// else if (temp != null)
				temp.getMarkSeen().fire();
				break;
			// leaved for navigation
			case UP:
			case DOWN:
				break;
			case LEFT:
				if (!isLeft) {
					Path temppath = getSelectedPathIfDirectory();
					if (temppath == null)
						break;
					parentWelcome.SynctoLeft(temppath.toString());
				}
				break;
			case RIGHT:
				if (isLeft) {
					Path temppath2 = getSelectedPathIfDirectory();
					if (temppath2 == null)
						break;
					parentWelcome.SynctoRight(temppath2.toString());
				}
				break;
			case ESCAPE:
				PredictNavigation.setText("");
				break;
			case CONTEXT_MENU:
				showContextMenu();
				break;
			// TODO check declaration there is a lot of key to define
			default:
				// ignore special character
				String newText = PredictNavigation.getText();
				// TODO detect special character event
				if (key.isControlDown() || key.isAltDown())
					break;
				if (key.isShiftDown()) {
					if (StringHelper.getKeyAsShiftDown().containsKey(key.getText()))
						newText += StringHelper.getKeyAsShiftDown().get(key.getText());
					break;
				}
				if (newText.equals(PredictNavigation.getText()))
					newText += key.getText();
				PredictNavigation.setText(newText.toLowerCase());

				doBack = false;
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
				goUpParent();
			} else if (TOGGLE_FAVORITE.match(e)) {
				parentWelcome.ToogleFavorite(getDirectoryPath());
			} else if (SHORTCUT_GO_BACK.match(e)) {
				BackButton.fire();
			} else if (SHORTCUT_GO_NEXT.match(e)) {
				NextButton.fire();
			}
		});

		Table.setOnMouseClicked(m -> {
			TableViewModel t = Table.getSelectionModel().getSelectedItem();
			if (m.getButton().equals(MouseButton.SECONDARY)) {
				showContextMenu();
				m.consume();
			}
			if (t == null)
				return;

			boolean tempisDirectory = t.getmFilePath().toFile().isDirectory();

			if (m.getButton().equals(MouseButton.PRIMARY) && m.getClickCount() == 2) {
				navigate(t.getmFilePath());
				if (!isLeft && Setting.isBackSync() && tempisDirectory)
					parentWelcome.SynctoLeftParent();
			} else if (isLeft && m.getButton().equals(MouseButton.PRIMARY) && tempisDirectory
					&& parentWelcome.isAutoExpandToRight()) {
				parentWelcome.SynctoRight(t.getmFilePath().toString());
			}
		});

		initializeTableRowFactory();
	}

	private void goUpParent() {
		PredictNavigation.setText("");
		File parent = mDirectory.getParentFile();
		if (parent != null) {
			AddToQueue(mDirectory);
			EmptyNextQueue();
			mDirectory = parent;
			if (mDirectory.exists()) {
				Table.scrollTo(0);
				refresh(null);
			} else {
				goUpParent();
			}
		} else {
			OutofTheBoxListRoots();
		}
	}

	Map<TableViewModel, TableRow<TableViewModel>> rowMap = new HashMap<>();
	private int rollerPrediction;

	// this row factory only work when user do scroll to show the correspond row
	private void initializeTableRowFactory() {
		// https://stackoverflow.com/questions/26220896/showing-tooltips-in-javafx-at-specific-row-position-in-the-tableview
		Table.setRowFactory(tv -> new TableRow<TableViewModel>() {
			@Override
			public void updateItem(TableViewModel t, boolean empty) {
				super.updateItem(t, empty);
				if (t == null) {
					setTooltip(null);
				} else {
					t.initializerRowFactory();
					rowMap.put(t, this);
					// is XSPF start the file directly with custom argument
					if (VLC.isPlaylist(t.getName())) {
						t.getOpenVLC().setOnMouseClicked(m -> {
							VLC.startXSPF(t.getmFilePath());
						});
					}

					String tooltipPreText = " ";
					final String key = keyMapperToString(t);
					if (key != null) {
						try {
							updateVisualSeenButton(key, t);

						} catch (Exception e) {
							System.out.println("i entered as wrong key");
							System.out.println(key);
							// TODO: handle exception
						}

						t.setNoteText(mfileTracker.getNoteTooltipText(t.getName()));

						List<String> options = mfileTracker.getMapDetails().get(key);
						tooltipPreText = mfileTracker.getNoteTooltipText(key);
						t.setNoteText(tooltipPreText);
						if (!tooltipPreText.isEmpty()) {
							setTooltip(getHoverTooltip(tooltipPreText));
						}
						t.getmNoteButton().setOnAction(new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent event) {
								String note = DialogHelper.showTextInputDialog("Quick Note Editor",
										"Add Note To see on hover",
										"Old note Was:\n" + mfileTracker.getNoteTooltipText(key),
										mfileTracker.getNoteTooltipText(key));

								// if null set it to space like it was
								if (note == null)
									return; // keep note unchanged
								if (note.isEmpty())
									note = " "; // reset note if is empty
								// ensure > is not used
								note = note.replace('>', '<');
								if (!OutofTheBoxRecursive) {
									mfileTracker.setTooltipsTexts(Table.getSelectionModel().getSelectedItems(), note);
									mfileTracker.setTooltipText(t.getName(), note);
								} else {
									mfileTracker.OutofTheBoxsetTooltipsTexts(
											Table.getSelectionModel().getSelectedItems(), t, note);
								}
								RefreshTablewithSameData();
							}
						});
						// action method for toggle will make all selection to toggle
						t.getMarkSeen().setOnAction(new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent event) {
								// toggle all selected items
								ToggleSeenHelper(t);
							}
						});

						// exclusive to normal view
						if (!OutofTheBoxRecursive) {
							if (VLC.isVLCMediaExt(t.getName())) {
								t.getOpenVLC().setOnMouseClicked(m -> {
									Path path = getDirectoryPath().resolve(t.getName());

									// if it is media file
									if (m.getButton().equals(MouseButton.PRIMARY)) {
										// load the preview
										new FilterVLCController(t.getmFilePath(), mfileTracker);
									} else {
										ArrayList<MediaCutData> list = new ArrayList<MediaCutData>();
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
						}
						// end if tracked
					} else {

						t.resetMarkSeen();
						t.getMarkSeen().setOnAction(new EventHandler<ActionEvent>() {

							@Override
							public void handle(ActionEvent event) {
								if (!untrackedBehavior(t)) {
									t.getMarkSeen().setSelected(false);
								} else
									ToggleSeenHelper(t);
							}
						});
						t.getmNoteButton().setOnAction(new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent event) {
								untrackedBehavior(t);
							}
						});
						if (VLC.isVLCMediaExt(t.getName())) {
							t.getOpenVLC().setOnMouseClicked(m -> {
								if (m.getButton().equals(MouseButton.PRIMARY)) {
									untrackedBehavior(t);
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

	// return the correct key to be used in map details
	public String keyMapperToString(TableViewModel t) {
		if (OutofTheBoxRecursive) {
			if (mfileTracker.isTrackedOutFolder(t.getmFilePath().getParent()))
				// the key is the full path
				return t.getmFilePath().toUri().toString();

		} else if (isOutofTheBoxHelper) {
			return null;
		} else if (mfileTracker.isTracked())
			// normal case key is just the name
			return t.getName();

		return null;
	}

	private Tooltip getHoverTooltip(String note) {
		if (note.isEmpty() || note.equals(" ")) {
			return null;
		}
		Tooltip tooltip = new Tooltip();

		tooltip.setText(note);
		tooltip.getStyleClass().addAll("tooltip");
		tooltip.setStyle("-fx-background-color: #7F00FF;-fx-text-fill: white;-fx-font-size:15;-fx-font-weight:bold");
		return tooltip;
	}

	private void ToggleSeenHelper(TableViewModel clicked) {
		if (!OutofTheBoxRecursive)
			mfileTracker.toggleSelectionSeen(Table.getSelectionModel().getSelectedItems(),
					XSPFrelatedWithSelection(clicked), clicked);
		else {
			mfileTracker.OutofTheBoxtoggleSelectionSeen(Table.getSelectionModel().getSelectedItems(), clicked);
		}
		// when toggle seen if yes or un is in search field do update
		reloadSearchField();
	}

	/**
	 * The use of this function is that sometimes after changing
	 * {@link TableViewModel#setNoteText(String)} the value isn't updated in the
	 * view unless something refresh the table so here will do it automatically all
	 * way reserving the old selection also
	 */
	private void RefreshTablewithSameData() {
		List<TableViewModel> Copy = new ArrayList<>(DataTable);
		// reserve selection before refreshing the table
		// be aware that if initialized spaces more than needed so table will contain 0
		// and first row get selected even when it's not
		int[] toSelect = new int[Table.getSelectionModel().getSelectedItems().size()];
		int j = 0;
		for (int i : Table.getSelectionModel().getSelectedIndices())
			toSelect[j++] = i;
		DataTable.clear();
		DataTable.addAll(Copy);
		// restore reserve
		Table.getSelectionModel().selectIndices(-1, toSelect);
		Table.requestFocus();
	}

	private boolean untrackedBehavior(TableViewModel t) {
		boolean ans;
		// returned false
		if (OutofTheBoxRecursive || isOutofTheBoxHelper) {
			ans = DialogHelper.showConfirmationDialog("Track new Folder[Recursive Mode]", "Ready to Be Stunned ?",
					"Tracking a new Folder will create a hidden file .tracker_explorer.txt"
							+ " in the folder to save data tracker !"
							+ "\nIn recursive mode the creation will trigger on all Selected Items.");
			if (!ans)
				return ans;

			Set<Path> paths = Table.getSelectionModel().getSelectedItems().stream()
					.map(selection -> selection.getmFilePath().getParent()).collect(Collectors.toSet());
			paths.add(t.getmFilePath().getParent());
			mfileTracker.OutofTheBoxTrackFolder(paths);
			RefreshTablewithSameData();
		} else {

			ans = mfileTracker.getAns();
			if (ans)
				mfileTracker.trackNewFolder();
		}
		return ans;
	}

	public void updateVisualSeenButton(String key, TableViewModel t) {
		// property of toggle button from map
		t.updateMarkSeen(mfileTracker.isSeen(key));
	}

	private void showContextMenu() {
		TableViewModel t = Table.getSelectionModel().getSelectedItem();
		if (t != null) {
			ArrayList<Path> toShow = new ArrayList<>();
			for (TableViewModel tsel : Table.getSelectionModel().getSelectedItems()) {
				toShow.add(tsel.getmFilePath());
			}
			RunMenu.showMenu(toShow);
		} else
			RunMenu.showMenu(Arrays.asList(mDirectory.toPath()));
	}

	private Path getSelectedPathIfDirectory() {
		TableViewModel t = Table.getSelectionModel().getSelectedItem();
		if (t == null)
			return null;
		if (!t.getmFilePath().toFile().isDirectory())
			return t.getmFilePath();
		return t.getmFilePath();
	}

	private void initializeSplitButton() {
		NavigateRecursive.setVisible(false);

		NavigateRecursive.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				TableViewModel toNavigateFor = Table.getSelectionModel().getSelectedItem();
				Path path = toNavigateFor.getmFilePath().getParent();
				navigate(path);
				resetForm();
				NavigateForNameAndScrollto(toNavigateFor);
			}
		});
		initializerecursiveSearch();

		UpButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				goUpParent();
				Table.requestFocus();
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
				refresh(null);
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
				refresh(null);
			}
		});

		Explorer.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if (!isOutofTheBoxHelper || isOutofTheBoxRecursive()) {
					int index;
					index = Table.getSelectionModel().getFocusedIndex();
					if (index != -1) {
						TableViewModel test = sortedData.get(index);

						try {
							StringHelper
									.RunRuntimeProcess(
											new String[] { "explorer.exe", "/select,", test.getmFilePath().toString() })
									.waitFor();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else
						StringHelper.open(mDirectory.toURI().toString());
				} else {
					if (truePathField.equals("/")) {
						String cmd = "explorer.exe /select," + DataTable.get(0).getmFilePath();
						try {
							Runtime.getRuntime().exec(cmd);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});

		// https://code.makery.ch/blog/javafx-8-tableview-sorting-filtering/
		FilteredList<TableViewModel> filteredData = new FilteredList<>(DataTable, p -> true);
		SearchFeild.textProperty().addListener((observable, oldValue, newValue) -> {
			PredictNavigation.setText("");
			filteredData.setPredicate(model -> {
				// be aware of doing somthing here it apply on every item in list
				return filterModel(newValue, model);
			});
			addQueryOptionsPathField("search", newValue);
			LabelItemsNumber.setText(" #" + filteredData.size() + " items");
		});

		sortedData = new SortedList<>(filteredData);
		sortedData.comparatorProperty().bind(Table.comparatorProperty());
		Table.setItems(sortedData);

		DataTable.addListener((ListChangeListener<TableViewModel>) c -> {
			while (c.next()) {
				if (c.wasAdded()) {
					// String key = keyStringMapper(c);
					for (TableViewModel t : c.getAddedSubList()) {
						String key = keyMapperToString(t);
						if (key != null) {
							// TODO when adding new files to current directory null pointer exception is
							// detected
							t.updateMarkSeenText(mfileTracker.isSeen(key));
							t.setNoteText(mfileTracker.getNoteTooltipText(key));
						} else
							t.emptyCell();
					}
				}
				if (c.wasRemoved()) {
					rowMap.clear();
				}
			}
		});

		Table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			// TODO if ctrl + A is pressed is not detected
			if (Table.getSelectionModel().getSelectedItems().size() > 1)
				reloadColorlastRowSelected();
			else
				deColorlastRowSelected();
			LabelItemsNumber
					.setText(" #" + Table.getSelectionModel().getSelectedItems().size() + "/" + sortedData.size());
		});
		PredictNavigation.textProperty().addListener((observable, oldValue, newValue) -> {
			deColorlastRowSelected();
			Table.getSelectionModel().clearSelection();
			if (newValue.trim().isEmpty()) {
				newValue = "";
				rollerPrediction = 0;
				PredictNavigation.setVisible(false);
				return;
			}
			if (!newValue.equals(newValue.toLowerCase())) {
				PredictNavigation.setText(newValue.toLowerCase());
				return;
			}
			PredictNavigation.setVisible(true);
			ArrayList<Pair<Integer, Integer>> toSelectList = new ArrayList<>();
			for (TableViewModel t : sortedData) {
				int where = t.getName().toLowerCase().indexOf(newValue);
				if (where >= 0) {
					toSelectList.add(new Pair<Integer, Integer>(where, sortedData.indexOf(t)));
				}
			}

			// Trying to auto complete special character
			if (toSelectList.isEmpty()) {
				// try once to add space then add character
				// if yes go for it
				for (TableViewModel t : sortedData) {
					for (String st : StringHelper.getKeyAsShiftDown().values()) {
						String tosearchfor = oldValue + st + newValue.substring(newValue.length() - 1);
						int where = t.getName().toLowerCase().indexOf(tosearchfor);
						if (where >= 0) {
							PredictNavigation.setText(tosearchfor);
							return;
						}
					}
				}

				// trying to roll to next selected element in case if it was the first or last
				// character
				if (oldValue.length() > 0)
					if (newValue.substring(0, 1).equals(oldValue.substring(0, 1)) || newValue
							.substring(newValue.length() - 1).equals(oldValue.substring(oldValue.length() - 1))) {
						// Table.getSelectionModel().getSelectedItems()
						Table.getSelectionModel().clearSelection(sortedData.indexOf(Table.getSelectionModel()
								.getSelectedItems().get(Table.getSelectionModel().getSelectedItems().size() - 1)));
						Table.getSelectionModel().select(Table.getSelectionModel().getSelectedItems()
								.get(Table.getSelectionModel().getSelectedItems().size() - 2));
						rollerPrediction++;
						PredictNavigation.setText(oldValue);
						return;

					}
			}

			if (!toSelectList.isEmpty()) {
				// sort list upon priority to first occurrence is last selected
				// to get opened in case and scroll to it
				toSelectList.sort(new Comparator<Pair<Integer, Integer>>() {

					@Override
					public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
						// the least value is better
						return o1.getKey() - o2.getKey();
					}
				});
				// for a proper selection do last select the greater one to make it on enter
				Collections.reverse(toSelectList);

				// for a roller if same key is typed
				Collections.swap(toSelectList, toSelectList.size() - 1,
						Math.abs((toSelectList.size() - 1 - rollerPrediction)) % toSelectList.size());

				// https://stackoverflow.com/questions/41104798/javafx-simplest-way-to-get-cell-data-using-table-index?rq=1
				// System.out.println(Table.getColumns().get(1)
				// .getCellObservableValue(toSelectList.get(toSelectList.size() -
				// 1).getValue()).getClass());
				// https://stackoverflow.com/questions/960431/how-to-convert-listinteger-to-int-in-java
				int[] toSelect = toSelectList.stream().mapToInt(i -> i.getValue()).toArray();
				int LastIndexTobeSelected = toSelectList.get(toSelectList.size() - 1).getValue();
				Table.scrollTo(smartScrollIndex(LastIndexTobeSelected));
				Table.getSelectionModel().selectIndices(-1, toSelect);
				colorLastRowSelected();
			} else {
				PredictNavigation.setText(oldValue);
			}

		});
		// scroll on button search to automatically clear
		// the search feild

		// https://stackoverflow.com/questions/29735651/mouse-scrolling-in-java-fx
		ClearButton.setOnScroll((ScrollEvent event) -> {
			// Adjust the zoom factor as per your requirement
			double deltaY = event.getDeltaY();
			if (deltaY < 0) {
				SearchFeild.setText("un");
			} else {
				SearchFeild.setText("yes");
			}
		});
		ClearButton.setOnMouseClicked(m -> {
			if (m.getButton().equals(MouseButton.PRIMARY) && m.getClickCount() == 2) {
				resetForm();
				m.consume();
			} else if (!m.getButton().equals(MouseButton.PRIMARY)) {
				SearchFeild.setText(SearchFeild.getText() + rollerSearchKey.get(rollerSearchIndex));
				rollerSearchIndex = (rollerSearchIndex + 1) % rollerSearchKey.size();
				m.consume();
			}
		});

		ClearButton.setOnAction(e -> {
			clearSearchFeild();
		});
	}

	private void initializerecursiveSearch() {
		recursiveSearch.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if (recursiveSearch.isSelected()) {
					if (isOutofTheBoxHelper) {
						recursiveSearch.setSelected(false);
						return;
					}
					addQueryOptionsPathField("recursive", "true");
					NavigateRecursive.setVisible(true);
					doRecursiveSearch();
				} else {
					addQueryOptionsPathField("recursive", null);
					NavigateRecursive.setVisible(false);
					setPathFieldThenRefresh(truePathField);
				}
			}
		});
	}

	private TableRow<TableViewModel> lastRowSelected = null;

	private void colorLastRowSelected() {
		lastRowSelected = rowMap.get(Table.getSelectionModel().getSelectedItem());
		if (lastRowSelected != null)
			lastRowSelected.getStyleClass().add("lastRowSelected");
		if (lastRowSelected != null)
			lastRowSelected.getStyleClass().add("lastRowSelected");
	}

	private void deColorlastRowSelected() {
		if (lastRowSelected != null)
			lastRowSelected.getStyleClass().removeAll(Collections.singletonList("lastRowSelected"));
	}

	private void reloadColorlastRowSelected() {
		deColorlastRowSelected();
		colorLastRowSelected();
	}

	/**
	 * check {@link #rollerSearchKey}
	 */
	private Integer rollerSearchIndex = 0;

	// this to update pathfield also
	// private void SearchFeild.setText(String text) {
	// SearchFeild.setText(text);
	// KeyEvent ke = new KeyEvent(KeyEvent.KEY_RELEASED, "a", "", KeyCode.UNDEFINED,
	// false, false, false, false);
	// SearchFeild.fireEvent(ke);
	// }

	private boolean OutofTheBoxRecursive = false;

	private void doRecursiveSearch() {
		// String depthANS = DialogHelper.showTextInputDialog("Recursive Search", "Depth
		// to consider",
		// "Enter depht value to consider begining from left view folder and track all
		// sub directory in it\n"
		// + "Input must be a number format if anything goes wrong '0' is the default
		// value",
		// "0");
		// if (depthANS == null)
		// return;
		// Integer depth = 50;
		// try {
		// depth = Integer.parseInt(depthANS);
		// } catch (NumberFormatException e1) {
		// depth = 0;
		// // e1.printStackTrace();
		// }
		// boolean saveIndex = DialogHelper.showConfirmationDialog("Recursive Search",
		// "Save Index File?",
		// "This to make it faster a second time");
		// Measure execution time for this method
		Main.ProcessTitle("Please Wait .. It might Take long for the first time...Indexing...");
		/**
		 * History data: 120130 Files Indexed in 122858 milliseconds! 120130 Files
		 * Indexed in 116692 milliseconds!
		 * 
		 * after updating javafx using only platform: Showing 120292 Files Indexed of
		 * 196713 in 58641 milliseconds!
		 * 
		 * after using keyname of the map in generating tableviewmodel and unifying the
		 * structure of value of mapdetails as options.get(0) == name Showing 120001
		 * Files Indexed of 196610 in 56487 milliseconds!
		 * 
		 * 
		 */
		Thread recursiveThread = new Thread() {

			public void run() {
				Instant start = Instant.now();
				Instant finish;
				long timeElapsed;
				String msg;
				Path dir = getDirectoryPath();
				mfileTracker.getMapDetails().clear();
				OutofTheBoxRecursive = true;
				boolean dosort = false;

				// TODO check UTF Validity
				// mfileTracker.OutofTheBoxAddToMapRecusive(dir);
				// https://github.com/brettryan/io-recurse-tests
				RecursiveFileWalker r = new RecursiveFileWalker();
				// to handle if recursive search was pressed in middle of
				// search without wiping all data
				recursiveSearch.setOnAction(e -> {
					if (recursiveSearch.isSelected()) {
						recursiveSearch.setSelected(true);
					}
				});

				try {
					// StringHelper.startTimer();
					Files.walkFileTree(dir, r);
					if (r.getFilesCount() > Setting.getMaxLimitFilesRecursive()) {
						dosort = true;
					}
					Stream<Path> paths = r.getParent().stream();
					if (dosort) {
						paths = paths.sorted((p1, p2) -> {
							// sort directory as traversal bfs not dfs
							// String p1st = p1.toString();
							// String p2st = p2.toString();
							// Integer p1depth = ;
							// Integer p2depth = p2.getNameCount();
							return p1.getNameCount() - p2.getNameCount();
						});
					}
					// dir is not added into paths
					mfileTracker.OutofTheBoxAddToMapRecusive(dir);
					for (Path p : paths.collect(Collectors.toList())) {
						if (!recursiveSearch.isSelected())
							break;
						mfileTracker.OutofTheBoxAddToMapRecusive(p);
						if (mfileTracker.getMapDetails().size() > Setting.getMaxLimitFilesRecursive())
							break;
					}
					// TODO working here
					// this is faster than walk but couldn't handle exception access denied like
					// this
					// File file = new
					// File("D:\\$RECYCLE.BIN\\S-1-5-21-2010406997-1771076405-2024525556-1007");
					//
					// System.out.println(file.isHidden());

					// Files.find(dir, Integer.MAX_VALUE,
					// (filePath, fileAttr) -> fileAttr.isDirectory() &&
					// !filePath.toFile().isHidden())
					// .forEach(p -> {
					// r.getParent().add(p);
					// });

					// r.getDirSet().stream().forEach(p -> {
					// mfileTracker.OutofTheBoxAddToMapRecusive(p);
					// });
				} catch (IOException e) {
					Platform.runLater(() -> RecursiveHelperUpdateTitle(e.getMessage()));
				}
				List<TableViewModel> allthem = RecursiveHelperGetData();
				Platform.runLater(() -> {
					RecursiveHelperLoadDataTable(allthem);
				});

				finish = Instant.now();

				timeElapsed = Duration.between(start, finish).toMillis(); // in millis
				msg = "Showing " + allthem.size() + " Files Indexed " + ((dosort) ? "of " + r.getFilesCount() : "")
						+ " in " + timeElapsed + " milliseconds!"
						+ ((dosort) ? "\nYou Can Change Limit File count in menu Tracker Setting" : "")
						+ ((!recursiveSearch.isSelected())
								? "\nSearch Stopped To Reset View (do/un)check me Again, Or double click on Clear Button"
								: "");
				if (!recursiveSearch.isSelected())
					recursiveSearch.setSelected(true);
				// System.out.println(msg);
				// TODO preserve show in case of search is unselected
				Platform.runLater(() -> RecursiveHelperUpdateTitle(msg));
				// we do re initalize because it's action was changed to handle in middle search
				// stop see up
				initializerecursiveSearch();
			}
		};
		RecursiveHelpersetBlocked(true);
		recursiveThread.start();
	}

	private void RecursiveHelperLoadDataTable(List<TableViewModel> allRowModel) {
		DataTable.clear();
		DataTable.addAll(allRowModel);
	}

	private List<TableViewModel> RecursiveHelperGetData() {
		// DataTable.clear();
		List<TableViewModel> allRowModel = new ArrayList<>();
		// sorting to show directory first
		// this cost useless factor time by ~*1.3
		// mfileTracker.getMapDetails().keySet().stream().sorted((pst1, pst2) -> {
		// File f1 = Paths.get(URI.create(pst1)).toFile();
		// File f2 = Paths.get(URI.create(pst2)).toFile();
		// // return f1.compareTo(f2);
		// return new FileComparator().compare(f1, f2);
		// }).collect(Collectors.toList());
		for (String pathST : mfileTracker.getMapDetails().keySet()) {
			// List<String> options = mfileTracker.getMapDetails().get(pathST);
			Path pathItem = Paths.get(URI.create(pathST));
			allRowModel.add(new TableViewModel(" ", pathItem.toFile().getName(), pathItem));
		}
		// StringHelper.endTimerAndDisplay();
		return allRowModel;
	}

	private void RecursiveHelperUpdateTitle(String message) {
		ContextMenu mn = new ContextMenu();
		MenuItem mnchild = new MenuItem(message);
		mn.getItems().add(mnchild);
		mn.getStyleClass().addAll("lastRowSelected");
		mnchild.getStyleClass().addAll("lastRowSelected");
		double xloc = Main.getPrimaryStage().getX() + Table.getLayoutX() + Table.getWidth() * 0.1;
		double yloc = Main.getPrimaryStage().getY() + Table.getLayoutY() + Table.getHeight() + 70;
		mn.show(Main.getPrimaryStage(), xloc, yloc);
		Main.ResetTitle();
		refresh(truePathField);
		RecursiveHelpersetBlocked(false);
		Table.requestFocus();
		// to refresh selection number and select the first one
		Table.getSelectionModel().select(0);
	}

	private void RecursiveHelpersetBlocked(boolean state) {
		ClearButton.setDisable(state);
		Table.setDisable(state);
		NavigateRecursive.setDisable(state);
		PathField.setDisable(state);
		UpButton.setDisable(state);
		BackButton.setDisable(state);
		NextButton.setDisable(state);
		Explorer.setDisable(state);
		SearchFeild.setDisable(state);
		parentWelcome.RecursiveHelpersetBlocked(state);

	}

	private void addQueryOptionsPathField(String optionItem, String value) {
		String text = PathField.getText();
		Map<String, String> options = getQueryOptionsAsMap(text);
		// clean existing option
		if (options == null)
			options = new HashMap<String, String>();

		if ((value == null) || value.trim().isEmpty() && options.containsKey(optionItem))
			options.remove(optionItem);
		else
			options.put(optionItem, value.trim());
		updatePathField(options);
	}

	private Map<String, String> getQueryOptionsAsMap(String FullPathEmbed) {
		int from = FullPathEmbed.indexOf("?") + 1;
		if (from == -1)
			return null;
		String optionsString = FullPathEmbed.substring(from);
		return Arrays.asList(optionsString.split("&")).stream()
				.filter(s -> s.contains("=") && !s.isEmpty() && s.split("=").length > 1)
				.collect(Collectors.toMap(x -> x.split("=")[0], x -> x.split("=")[1]));
	}

	private String getQueryPathFromEmbed(String FullPathEmbed) {
		int temp = FullPathEmbed.indexOf("?");
		String query;
		if (temp != -1)
			query = FullPathEmbed.substring(temp);
		else
			query = "";
		return FullPathEmbed.replace(query, "");
	}

	private void updatePathField(Map<String, String> options) {
		truePathField = truePathField.replace(getQueryOptions(), "");
		if (options == null)
			return;
		truePathField += "?";
		int i = 0;
		for (String Keyoption : options.keySet()) {
			if (i++ != 0)
				truePathField += "&";
			truePathField += Keyoption + "=" + options.get(Keyoption);
		}
		if (truePathField.endsWith("?"))
			truePathField = truePathField.replace("?", "");
		PathField.setText(truePathField);
	}

	private String getQueryOptions() {
		int temp = truePathField.indexOf("?");
		if (temp != -1)
			return truePathField.substring(temp);
		else
			return "";
	}

	/**
	 * check it's roller key at {@link #rollerSearchIndex}
	 */
	static private List<String> rollerSearchKey = new ArrayList<String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6421021611798519588L;

		{
			add(";vlc");
			add(";andthis");
			add(";|orthis");
			add(";!notthis");
			add(";video");
			add(";audio");
		}
	};

	/**
	 * 
	 * @param Pattern
	 *            ';' to combine multiple search statement '!' to exclude from
	 *            search
	 * 
	 *            example i want all vlc media that contain name word and not excel
	 *            i search: 'vlc;word;!excel'
	 * @param model
	 * @return
	 */
	private boolean filterModel(String Pattern, TableViewModel model) {
		// If filter text is empty, display all.
		List<String> advancedFilter = Arrays.asList(Pattern.split(";"));
		boolean isRespect = true;
		boolean state = true;
		String modelName = model.getName().toLowerCase();
		String note = model.getNoteText().trim().toLowerCase();
		boolean isMediaFile = VLC.isVLCMediaExt(modelName);
		boolean isVideo = VLC.isVideo(modelName);
		boolean isAudio = VLC.isAudio(modelName);
		for (String filerItem : advancedFilter) {
			String lowerCasefilter = filerItem.toLowerCase();
			state = true;
			if (lowerCasefilter.startsWith("!")) {
				state = false;
				lowerCasefilter = lowerCasefilter.substring((lowerCasefilter.length() > 0) ? 1 : 0);
			} else if (lowerCasefilter.startsWith("|")) {
				if (isRespect == true)
					continue;
				isRespect = true;
				lowerCasefilter = lowerCasefilter.substring((lowerCasefilter.length() > 0) ? 1 : 0);
			}
			if (lowerCasefilter == null || lowerCasefilter.isEmpty()) {
				isRespect &= true;
				continue;
			}
			// Compare with filerItem text.
			if (modelName.contains(lowerCasefilter)) {
				isRespect &= state; // filerItem matches name.
			} else if (lowerCasefilter.contains("vlc") && isMediaFile)
				isRespect &= state;
			else if (lowerCasefilter.contains("video") && isVideo)
				isRespect &= state;
			else if (lowerCasefilter.contains("audio") && isAudio)
				isRespect &= state;
			else if (note.contains(lowerCasefilter))
				isRespect &= state; // search note if exist
			else if ((model.getMarkSeen().getText().equals("U") || model.getMarkSeen().getText().equals("-"))
					&& "un".toLowerCase().contains(lowerCasefilter)) {
				isRespect &= state; // filerItem unseen.
			} else if (model.getMarkSeen().getText().equals("S") && "yes".toLowerCase().contains(lowerCasefilter)) {
				isRespect &= state;// filerItem seen.
			} else
				isRespect &= !state; // Does not match.

		}
		return isRespect;
	}

	public static boolean isLastChangedLeft = false;
	public static int count = 1; // optimized !
	// set isisOutofTheBoxPath to null for false result
	// otherwise provide it

	public void refresh(String isOutofTheBoxPath) {
		// System.out.println(count);
		// count++;
		// if (count > 3)
		// try {
		// throw new Exception();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		isLastChangedLeft = isLeft;
		if (isOutofTheBoxPath != null) {
			// for out of the box do change directory or add your datatable stuff
			// before coming here .. this only used to update title and common preview stuff
			truePathField = isOutofTheBoxPath;
			RefreshisOutofTheBox();
			// refresh state
			Main.UpdateTitle(truePathField);
		} else {
			OutofTheBoxRecursive = false;
			recursiveSearch.setSelected(false);
			NavigateRecursive.setVisible(false);
			addQueryOptionsPathField("recursive", null);
			truePathField = mDirectory.getAbsolutePath() + getQueryOptions();
			// PathField.setText(mDirectory.getAbsolutePath());
			// truePathField = mDirectory.getAbsolutePath();
			RefreshisOutofTheBox();
			mfileTracker.loadMap(getDirectoryPath(), true, false);
			mfileTracker.resolveConflict();
			mWatchServiceHelper.changeObservableDirectory(mDirectory.toPath());
			showList(getCurrentFilesList());
			reloadSearchField();
			String Stagetitle = mDirectory.getName();
			if (Stagetitle.isEmpty())
				Stagetitle = mDirectory.getAbsolutePath();
			Main.UpdateTitle(Stagetitle);
		}
		if (isLeft)
			parentWelcome.updateFavoriteCheckBox(isOutofTheBoxHelper);
		PathField.setText(truePathField);

		LabelItemsNumber.setText(" #" + DataTable.size() + " items");

	}

	public void reloadSearchField() {
		String temp = SearchFeild.getText();
		SearchFeild.setText("");
		SearchFeild.setText(temp);
	}

	// this helper is to optimize call of the function
	// so only call when really need to update state

	// for recursive mode use OutofTheBoxRecursive

	private boolean isOutofTheBoxHelper = false;

	public boolean isOutofTheBoxHelper() {
		return isOutofTheBoxHelper;
	}

	public boolean isOutofTheBoxRecursive() {
		return OutofTheBoxRecursive;
	}

	private boolean RefreshisOutofTheBox() {
		// System.out.println((isLeft) ? "I'm left " : "i'm right");

		if (SpecialPath.stream().anyMatch(sp -> truePathField.contains(sp))) {
			// excluding search from out of the box
			if (getQueryOptions().contains("search=") && !getQueryOptions().contains("&")) {
				isOutofTheBoxHelper = false;
				return false;
			}
			// out of the box
			// System.out.println("i'm out of the box");
			isOutofTheBoxHelper = true;
			return true;
		} else {
			// System.out.println("i'm the box");
			isOutofTheBoxHelper = false;
			return false;
		}
	}

	public TextField getPathField() {
		return PathField;
	}

	public List<Path> getSelection() {
		List<Path> selection = new ArrayList<>();
		for (TableViewModel item : Table.getSelectionModel().getSelectedItems()) {
			selection.add(item.getmFilePath());
		}
		return selection;
	}

	public TableViewModel getSelectedItem() {
		return Table.getSelectionModel().getSelectedItem();
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

	public ArrayList<File> getCurrentFilesList() {
		File[] arrayFiles = mDirectory.listFiles(file -> !file.isHidden());
		if (arrayFiles == null) {
			arrayFiles = new File[0];
		}
		// old approach
		// Arrays.sort(arrayFiles, (f1, f2) -> {
		// if ((f1.isDirectory() && f2.isDirectory()) || (f1.isFile() && f2.isFile())) {
		// return f1.compareTo(f2);
		// }
		// return f1.isDirectory() ? -1 : 1;
		// });

		ArrayList<File> listFiles = new ArrayList<>();
		listFiles.addAll(Arrays.asList(arrayFiles));
		StringHelper.SortArrayFiles(listFiles);

		return listFiles;
	}

	public List<String> getCurrentFilesListName() {
		return getCurrentFilesList().stream().map(s -> s.getName()).collect(Collectors.toList());
	}

	private static boolean alertError = true;

	private void showList(ArrayList<File> list) {
		DataTable.clear();

		// Resolving name section
		String error = "";
		String ExpandedError = "";
		boolean dorefresh = false;
		if (Setting.isAutoRenameUTFFile()) {
			List<File> toBeRemoved = new ArrayList<File>();
			for (File s : list) {
				try {
					// if (FileHelper.rename(s.toPath(), this, true) != null)
					if (FileHelper.rename(s.toPath(), true) != null)
						dorefresh = true;
				} catch (Exception e) {
					// Cannot Fix name unfortunately
					ExpandedError += e.getMessage();
					toBeRemoved.add(new File(s.toString()));
				}
			}
			if (dorefresh) {
				refresh(null);
				return;
			}
			if (alertError && !ExpandedError.isEmpty()) {
				if (isLeft)
					error = "Left View Exception\n";
				else
					error = "Right View Exception\n";
				alertError = DialogHelper.showExpandableConfirmationDialog("Show Files",
						"Illegal Character !  ----> Some File Could Not be shown",
						error + "- Press Cancel to ignore future Similar Messages", ExpandedError);
				error = "";
				ExpandedError = "";
			}
			toBeRemoved.forEach(s -> list.remove(s));
		}

		// end resolving name section

		for (File file : list) {
			TableViewModel t = new TableViewModel(" ", file.getName(), file.toPath());
			// check also initializeTableRowFactory for generating table row action
			// and DataTable.addListener for adding search parameters
			DataTable.add(t);
		}
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
			if (ext.equals("XSPF") && t.getName().length() > 15) {
				String basename = t.getName().substring(0, t.getName().length() - 15).toUpperCase();
				mapAllXSPF.put(basename, t);
			}
		}
		for (TableViewModel tsearch : DataTable) {
			String tbase = StringHelper.getBaseName(tsearch.getName());
			if (mapAllXSPF.containsKey(tbase)) {
				mfileTracker.setSeen(tsearch.getName(), mfileTracker.getSeen(mapAllXSPF.get(tbase)), tsearch);
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

	// true if the navigate was a directory
	public boolean navigate(Path filePath) {
		File selectedFile = filePath.toFile();
		boolean isDirectory = selectedFile.isDirectory();
		try {
			WindowsShortcut test = null;
			if (!isDirectory && WindowsShortcut.isPotentialValidLink(selectedFile)) {
				test = new WindowsShortcut(selectedFile);
			}
			if (test != null) {
				selectedFile = new File(test.getRealFilename());
			}
		} catch (IOException | ParseException e1) {
			e1.printStackTrace();
		}

		// double check in case of fixed shortcut
		if (selectedFile.isDirectory()) {
			PredictNavigation.setText("");
			setmDirectoryThenRefresh(selectedFile);
			isDirectory = true;
		} else {
			try {
				String files = " --playlist-enqueue --loop";
				if (VLC.isInstalled() && VLC.isVLCMediaExt(filePath.toFile().getName())) {
					for (TableViewModel t : Table.getSelectionModel().getSelectedItems()) {
						if (VLC.isVLCMediaExt(t.getName()))
							files += " " + t.getmFilePath().toUri();
					}
					VLC.StartVlc(files);
					// we always start media because playlist do not start automatically
				} else
					StringHelper.open(selectedFile);

			} catch (Exception e) {
				DialogHelper.showException(e);
			}
		}
		PredictNavigation.setText("");
		doBack = true;
		return isDirectory;
	}

	private void EmptyNextQueue() {
		NextQueue.clear();
		NextButton.setDisable(true);
	}

	private void AddToQueue(File file) {
		BackQueue.add(file);
		BackButton.setDisable(false);
	}

	// the parent directory was on old version of this application
	// named like this
	private void back() {
		if (!doUp && !BackButton.isDisabled()) {
			BackButton.fire();
			if (!doUp) {
				doUp = true;
				executor.execute(doUpThreadOff);
			}
		} else {
			goUpParent();
		}
	}

	private void OutofTheBoxListRoots() {
		List<File> roots = Arrays.asList(File.listRoots());
		if (recursiveSearch.isSelected()) {
			// do not set to false since fire do invert selection !
			// recursiveSearch.setSelected(false);
			// recursiveSearch.fire();
			switchRecursive();
		}
		refresh("/");
		DataTable.clear();

		for (File root : roots) {
			TableViewModel t = new TableViewModel(" ", root.getAbsolutePath(), root.toPath());
			t.getHboxActions().getChildren().clear();
			DataTable.add(t);
		}
	}

	public File getmDirectory() {
		return mDirectory;
	}

	// this method is useful to handle call of changing directory
	// and enqueue it to back button..
	// and so adding old directory to queue and so on
	public void setmDirectoryThenRefresh(File mDirectory) {
		AddToQueue(this.mDirectory);
		EmptyNextQueue();
		this.mDirectory = mDirectory;

		refresh(null);
		Table.scrollTo(0);
	}

	// special use like for rename and do not Queue
	public void setmDirectory(File mdirectory) {
		mDirectory = mdirectory;
	}

	public Path getDirectoryPath() {
		return mDirectory.toPath();
	}

	public boolean isFocused() {
		return this.Table.isFocused() || this.UpButton.isFocused() || this.ClearButton.isFocused()
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

	public void switchRecursive() {
		// important fire a checkbox do/un check it before calling it's action
		recursiveSearch.fire();
	}

	public void RevealINExplorer() {
		if (Table.getSelectionModel().getSelectedItem() != null) {
			// https://stackoverflow.com/questions/7357969/how-to-use-java-code-to-open-windows-file-explorer-and-highlight-the-specified-f
			StringHelper.RunRuntimeProcess(new String[] { "explorer.exe", "/select,",
					Table.getSelectionModel().getSelectedItem().getmFilePath().toString() });
			// TODO
			// later do make it multiple selection not working as follow
			// List<Path> paths = getSelection();
			// try {
			// for (Path path : paths)
			// cmd += path.toAbsolutePath();
			// Runtime.getRuntime().exec(cmd);
			// } catch (IOException e) {
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

	// do later separete this from reseting form only reset search !
	public void clearSearchFeild() {

		TableViewModel selected = Table.getSelectionModel().getSelectedItem();

		// addQueryOptionsPathField("search", null); already done in listener
		SearchFeild.setText("");
		Table.getSelectionModel().clearSelection(); // to prevent mis scroll
		Table.getSelectionModel().select(selected);

		// for better view item like centralize view it on escape
		// Table.getSelectionModel().select(DataTable.indexOf(selected));
		// Table.scrollTo(smartScrollIndex(scrollindex));
		NavigateForNameAndScrollto(selected);
	}

	private void resetForm() {
		clearSearchFeild();
		updatePathField(null);
		refresh(null);
	}

	protected void NavigateForNameAndScrollto(TableViewModel toNavigateFor) {
		if (toNavigateFor == null)
			return;
		TableViewModel found = null;
		for (TableViewModel t : DataTable) {
			if (t.getName().equals(toNavigateFor.getName())) {
				found = t;
				break;
			}
		}
		if (found == null)
			return;
		Table.getSelectionModel().select(found);
		Table.scrollTo(smartScrollIndex(sortedData.indexOf(found)));

	}

	private int smartScrollIndex(int scrollindex) {
		for (int i = 4; i > 0; i--) {
			if (scrollindex - i > 0) {
				scrollindex -= i;
				break;
			}
		}
		return scrollindex;
	}

	public void focusTable() {
		if (Table.getSelectionModel().getSelectedCells().size() <= 0)
			// Table.getSelectionModel().select(0);
			Table.getSelectionModel().selectFirst();
		Table.requestFocus();
	}

	public boolean isFocusedTable() {
		return Table.isFocused();
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

	private TableViewModel lastSelectedScroller;

	private void SaveLastSelectToSroll() {
		lastSelectedScroller = Table.getSelectionModel().getSelectedItem();
	}

	private void RestoreLastSelectAndSroll() {
		NavigateForNameAndScrollto(lastSelectedScroller);
	}

	public void refreshAsPathField() {
		// when doing search this cause to false navigate
		// this doesn't work with multiselection
		// auto scroll
		// TODO
		SaveLastSelectToSroll();
		setPathFieldThenRefresh(getPathField().getText());
		RestoreLastSelectAndSroll();
	}

	public void setPathFieldThenRefresh(String pathField) {
		pathField = pathField.trim();
		String test = pathField;
		File file = new File(getQueryPathFromEmbed(pathField));

		// Important see there is navigate is not just a boolean ::
		if (file.exists() && navigate(file.toPath())) {
			// apply query only if file exist and file is a directory after changing view to
			// it
			if (SpecialPath.stream().anyMatch(sp -> test.contains(sp))) {

				// out of the box
				if (pathField.equals("/")) {
					resetForm();
					OutofTheBoxListRoots();
				} else if (pathField.contains("?")) {
					// ?search=kaza;few&another=fwe
					try {

						// we need at first applying recursive then search
						String dosearchDelayed = null;
						for (Map.Entry<String, String> entry : getQueryOptionsAsMap(pathField).entrySet()) {
							String key = entry.getKey().toLowerCase().trim();
							if (key.equals("recursive")) {
								// we invert answer cause on fire also this gonna go back
								// if (!recursiveSearch.isSelected())
								recursiveSearch.setSelected(!Boolean.parseBoolean(entry.getValue()));
								// System.out.println("i'm pathfield" + recursiveSearch.isSelected());
								recursiveSearch.fire();
							} else if (key.equals("search")) {
								dosearchDelayed = entry.getValue();
							}
						}
						if (dosearchDelayed != null) {
							SearchFeild.setText(dosearchDelayed);
							reloadSearchField();
						}
					} catch (Exception e) {
						DialogHelper.showAlert(AlertType.ERROR, "Incorect Path", "The Provided input is Incorrect",
								"Example Template: .../path?option1=value1&option2=value2");
						e.printStackTrace();
					}

				}
			}
		}
		PathField.setText(truePathField);
	}

	public WatchServiceHelper getmWatchServiceHelper() {
		return mWatchServiceHelper;
	}

	public void setPredictNavigation(String predictNavigation) {
		PredictNavigation.setText(predictNavigation);
	}
}
