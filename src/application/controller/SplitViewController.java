package application.controller;

import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;

import application.DialogHelper;
import application.FileHelper;
import application.FileTracker;
import application.RecursiveFileWalker;
import application.RunMenu;
import application.StringHelper;
import application.TrackerPlayer;
import application.VLC;
import application.WatchServiceHelper;
import application.WindowsExplorerComparator;
import application.WindowsShortcut;
import application.datatype.MediaCutData;
import application.datatype.Setting;
import application.model.SplitViewState;
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
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.util.Pair;

/**
 * For a structure view read
 * {@link #SplitViewController(Path, Boolean, WelcomeController, ObservableList, TextField, Button, TextField, Button, TableView, Button, TableColumn, Button, Button, TextField, CheckBox, Label, Button, MenuButton, TableColumn, TableColumn)}
 *
 * For a functional flow read {@link #refresh(String)}
 *
 * at last they will combine..
 *
 * @author Ahmad Said
 *
 */
public class SplitViewController {

	static final KeyCombination SHORTCUT_Clear_Search = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
	static final KeyCombination SHORTCUT_FOCUS_TEXT_FIELD = new KeyCodeCombination(KeyCode.D,
			KeyCombination.SHIFT_DOWN);
	static final KeyCombination SHORTCUT_GO_BACK = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.ALT_DOWN);

	static final KeyCombination SHORTCUT_GO_NEXT = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.ALT_DOWN);
	static final KeyCombination SHORTCUT_GO_UP = new KeyCodeCombination(KeyCode.UP, KeyCombination.ALT_DOWN);
	static final KeyCombination SHORTCUT_RECURSIVE = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN);
	static final KeyCombination SHORTCUT_SEARCH = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN);
	static final KeyCombination TOGGLE_FAVORITE = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN,
			KeyCombination.SHIFT_DOWN);

	private Button BackButton;
	private Button NextButton;
	private Button UpButton;
	private Button Explorer;

	private Label LabelItemsNumber;

	private TextField PathField;
	private TextField PredictNavigation;
	private TextField SearchField;
	private Button RefreshButton;

	private CheckBox recursiveSearch;
	private Button NavigateRecursive;

	private MenuButton ToolsMenu;

	private WelcomeController parentWelcome;
	private FileTracker mFileTracker;
	private WatchServiceHelper mWatchServiceHelper = null;

	private boolean OutOfTheBoxRecursive = false;
	private boolean isOutOfTheBoxHelper = false;

	private File mDirectory;

	/**
	 * specify working current view position
	 */
	private boolean isLeft;

	// this always respect the path field pattern
	// if anything goes wrong return to it
	private String truePathField;

	private ObservableList<TableViewModel> DataTable;
	SortedList<TableViewModel> sortedData;
	private TableView<TableViewModel> Table;

	public SplitViewController() {
	}

	// TableColumn<TableViewModel, ImageView> colIconTestResize;
	public SplitViewController(Path path, Boolean isLeft, WelcomeController parent,
			ObservableList<TableViewModel> dataTable, TextField pathField, Button upButton, TextField searchField,
			Button refreshButton, TableView<TableViewModel> table, Button explorer,
			TableColumn<TableViewModel, HBox> hBoxActions, Button backButton, Button nextButton,
			TextField predictNavigation, CheckBox recursiveSearch, Label labelItemsNumber, Button navigateRecursive,
			MenuButton toolsMenu, TableColumn<TableViewModel, String> noteColumn,
			TableColumn<TableViewModel, String> nameColumn) {
		super();
		// colIconTestResize=colIcon;
		this.isLeft = isLeft;
		DataTable = dataTable;
		PathField = pathField;
		UpButton = upButton;
		NextButton = nextButton;
		BackButton = backButton;
		Explorer = explorer;
		SearchField = searchField;
		RefreshButton = refreshButton;
		Table = table;
		parentWelcome = parent;
		PredictNavigation = predictNavigation;
		this.recursiveSearch = recursiveSearch;
		LabelItemsNumber = labelItemsNumber;
		ToolsMenu = toolsMenu;
		mDirectory = new File(path.toString());
		truePathField = mDirectory.getAbsolutePath();
		NavigateRecursive = navigateRecursive;
		Table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		mFileTracker = new FileTracker(this);

		initializePathField();
		initializeSplitButton();

		initializeTable();
		// initialize column rule comparator
		hBoxActions.setComparator(new Comparator<HBox>() {

			@Override
			public int compare(HBox o1, HBox o2) {
				// first children is button watch status
				// TableRow<TableViewModel> row = (TableRow<TableViewModel>) o1.getParent();
				// TODO this comparator doesn't work as i generate hbox with row factory for
				// speed purpose if you want it to work just scroll and load all rows then it
				// work just fine
				if (o1.getChildren().size() == 0) {
					return -1;
				}
				ToggleButton markSeen1 = (ToggleButton) o1.getChildren().get(0);
				if (markSeen1.getText().equals("S")) {
					return 1;
				}
				return 0;
			}
		});
		noteColumn.setComparator(new WindowsExplorerComparator());
		nameColumn.setComparator(new WindowsExplorerComparator());

		// hBoxActions.setCellFactory(col -> new TableCell<TableViewModel, HBox>() {
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
		PathField.getStyleClass().removeAll("*.text-field>*.right-button>*.right-button-graphic");
		PathField.setStyle("-fx-font-size: 14px;");

		PathField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
			if (isNowFocused) {
				Platform.runLater(() -> PathField.selectAll());
			}
		});
		PathField.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				setPathFieldThenRefresh(PathField.getText());
			}
		});
	}

	public void setPathFieldThenRefresh(String pathField) {
		pathField = pathField.trim();
		String test = pathField;
		if (test.equals("cmd")) {
			// system dependency
			StringHelper.RunRuntimeProcess(new String[] { "cmd.exe", " /c start cd /d", mDirectory.toString() });
		}
		File file = new File(getQueryPathFromEmbed(pathField));

		// Important see there is navigate is not just a boolean ::
		if (file.exists() && navigate(file.toPath())) {
			// apply query only if file exist and file is a directory after changing view to
			// it
			if (SpecialPath.stream().anyMatch(sp -> test.contains(sp))) {

				// out of the box
				if (pathField.equals("/")) {
					resetForm();
					OutOfTheBoxListRoots();
				} else if (pathField.contains("?")) {
					// ?search=Kaza;few&another=fwe
					try {

						// we need at first applying recursive then search
						String doSearchDelayed = null;
						for (Map.Entry<String, String> entry : getQueryOptionsAsMap(pathField).entrySet()) {
							String key = entry.getKey().toLowerCase().trim();
							if (key.equals("recursive")) {
								// we invert answer cause on fire also this gonna go back
								// if (!recursiveSearch.isSelected())
								recursiveSearch.setSelected(!Boolean.parseBoolean(entry.getValue()));
								recursiveSearch.fire();
							} else if (key.equals("search")) {
								doSearchDelayed = entry.getValue();
							}
						}
						if (doSearchDelayed != null) {
							SearchField.setText(doSearchDelayed);
							reloadSearchField();
						}
					} catch (Exception e) {
						DialogHelper.showAlert(AlertType.ERROR, "Incorrect Path", "The Provided input is Incorrect",
								"Example Template: .../path?option1=value1&option2=value2");
						e.printStackTrace();
					}

				}
			}
		}
		PathField.setText(truePathField);
	}

	private Map<String, String> getQueryOptionsAsMap(String FullPathEmbed) {
		int from = FullPathEmbed.indexOf("?") + 1;
		if (from == -1) {
			return null;
		}
		String optionsString = FullPathEmbed.substring(from);
		return Arrays.asList(optionsString.split("&")).stream()
				.filter(s -> s.contains("=") && !s.isEmpty() && s.split("=").length > 1)
				.collect(Collectors.toMap(x -> x.split("=")[0], x -> x.split("=")[1]));
	}

	private String getQueryPathFromEmbed(String FullPathEmbed) {
		int temp = FullPathEmbed.indexOf("?");
		String query;
		if (temp != -1) {
			query = FullPathEmbed.substring(temp);
		} else {
			query = "";
		}
		return FullPathEmbed.replace(query, "");
	}

	private void addQueryOptionsPathField(String optionItem, String value) {
		String text = PathField.getText();
		Map<String, String> options = getQueryOptionsAsMap(text);
		// clean existing option
		if (options == null) {
			options = new HashMap<String, String>();
		}

		if (value == null || value.trim().isEmpty() && options.containsKey(optionItem)) {
			options.remove(optionItem);
		} else {
			options.put(optionItem, value.trim());
		}
		updatePathField(options);
	}

	public void reloadSearchField() {
		String temp = SearchField.getText();
		SearchField.setText("");
		SearchField.setText(temp);
	}

	private void resetForm() {
		clearSearchField();
		updatePathField(null);
		refresh(null);
	}

	// used to loop over items that meet same prediction and when entering a key
	// that cannot filter further
	private int rollerPrediction;

	private void initializeSplitButton() {

		initializeToolsMenu();
		NavigateRecursive.setVisible(false);

		NavigateRecursive.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				TableViewModel toNavigateFor = Table.getSelectionModel().getSelectedItem();
				Path path = toNavigateFor.getmFilePath().getParent();
				navigate(path);
				resetForm();
				NavigateForNameAndScrollTo(toNavigateFor);
			}
		});
		initializeRecursiveSearch();

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
				if (temp.exists()) {
					mDirectory = temp;
				}
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
				if (temp.exists()) {
					mDirectory = temp;
				}
				if (NextQueue.isEmpty()) {
					NextButton.setDisable(true);
				}
				refresh(null);
			}
		});

		Explorer.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if (!isOutOfTheBoxHelper || isOutOfTheBoxRecursive()) {
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
							e.printStackTrace();
						}
					} else {
						StringHelper.openFile(mDirectory);
					}
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
		SearchField.textProperty().addListener((observable, oldValue, newValue) -> {
			PredictNavigation.setText("");
			filteredData.setPredicate(model -> {
				// be aware of doing something here it apply on every item in list
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
							t.updateMarkSeenText(mFileTracker.isSeen(key));
							t.setNoteText(mFileTracker.getNoteTooltipText(key));
						} else {
							t.emptyCell();
						}
					}
				}
				if (c.wasRemoved()) {
					rowMap.clear();
				}
			}
		});

		Table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (Table.getSelectionModel().getSelectedItems().size() > 1) {
				reloadColorLastRowSelected();
			} else {
				deColorLastRowSelected();
			}
			double totalSelectedSize = 0;
			String formatSelect;
			double totalAllSize = 0;
			String formatAll;
			for (TableViewModel t : Table.getSelectionModel().getSelectedItems()) {
				totalSelectedSize += t.getFileSize();
			}
			for (TableViewModel t : sortedData) {
				totalAllSize += t.getFileSize();
			}

			LabelItemsNumber
					.setText(" #" + Table.getSelectionModel().getSelectedItems().size() + "/" + sortedData.size()
							+ (totalSelectedSize > 0.01
									? " (" + StringHelper.getFormattedSizeFromMB(totalSelectedSize) + " / "
											+ StringHelper.getFormattedSizeFromMB(totalAllSize) + ")"
									: ""));
		});
		PredictNavigation.textProperty().addListener((observable, oldValue, newValue) -> {
			deColorLastRowSelected();
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
						String toSearchFor = oldValue + st + newValue.substring(newValue.length() - 1);
						int where = t.getName().toLowerCase().indexOf(toSearchFor);
						if (where >= 0) {
							PredictNavigation.setText(toSearchFor);
							return;
						}
					}
				}

				// trying to roll to next selected element in case if it was the first or last
				// character
				if (oldValue.length() > 0) {
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
						Math.abs(toSelectList.size() - 1 - rollerPrediction) % toSelectList.size());

				// https://stackoverflow.com/questions/41104798/javafx-simplest-way-to-get-cell-data-using-table-index?rq=1
				// System.out.println(Table.getColumns().get(1)
				// .getCellObservableValue(toSelectList.get(toSelectList.size() -
				// 1).getValue()).getClass());
				// https://stackoverflow.com/questions/960431/how-to-convert-listinteger-to-int-in-java
				int[] toSelect = toSelectList.stream().mapToInt(i -> i.getValue()).toArray();
				int lastIndexToBeSelected = toSelectList.get(toSelectList.size() - 1).getValue();
				Table.scrollTo(smartScrollIndex(lastIndexToBeSelected));
				Table.getSelectionModel().selectIndices(-1, toSelect);
				colorLastRowSelected();
			} else {
				PredictNavigation.setText(oldValue);
			}

		});
		// scroll on button search to automatically clear
		// the search field

		// https://stackoverflow.com/questions/29735651/mouse-scrolling-in-java-fx
		RefreshButton.setOnScroll((ScrollEvent event) -> {
			// Adjust the zoom factor as per your requirement
			double deltaY = event.getDeltaY();
			if (deltaY < 0) {
				SearchField.setText("un");
			} else {
				SearchField.setText("yes");
			}
		});
		RefreshButton.setOnMouseClicked(m -> {
			if (!m.getButton().equals(MouseButton.PRIMARY)) {
				SearchField.setText(SearchField.getText() + rollerSearchKey.get(rollerSearchIndex));
				rollerSearchIndex = (rollerSearchIndex + 1) % rollerSearchKey.size();
				m.consume();
			}
		});

		RefreshButton.setOnAction(e -> {
			resetForm();
		});
	}

	private void reloadColorLastRowSelected() {
		deColorLastRowSelected();
		colorLastRowSelected();
	}

	private TableRow<TableViewModel> lastRowSelected = null;

	private void deColorLastRowSelected() {
		if (lastRowSelected != null) {
			lastRowSelected.getStyleClass().removeAll(Collections.singletonList("lastRowSelected"));
		}
	}

	private void colorLastRowSelected() {
		lastRowSelected = rowMap.get(Table.getSelectionModel().getSelectedItem());
		if (lastRowSelected != null) {
			lastRowSelected.getStyleClass().add("lastRowSelected");
		}
	}

	/**
	 * {@link SplitViewController#UpButton this is link example}
	 *
	 */
	public void initializeTable() {

		Table.setOnKeyPressed(key -> {
			String test = PredictNavigation.getText().trim();
			TableViewModel lastSelectedTemp = Table.getSelectionModel().getSelectedItem();
			switch (key.getCode()) {
			case ENTER:
				if (Table.isFocused()) {
					if (lastSelectedTemp != null) {
						navigate(lastSelectedTemp.getmFilePath());
					}
				}
				break;
			case BACK_SPACE:
				if (doBack) {
					back();
				} else {
					if (!test.isEmpty()) {
						test = test.substring(0, test.length() - 1);
						PredictNavigation.setText(test);
					} else {
						executor.execute(EnableMisBack);
					}
				}
				break;
			case SPACE:
				// to do here if i make a selection using prediction do not make so
				// always space do mark seen and auto enter space is enabled
				// if (!test.isEmpty())
				// PredictNavigation.insertText(PredictNavigation.getText().length(), " ");
				// else if (temp != null)
				lastSelectedTemp.getMarkSeen().fire();
				break;
			// leaved for navigation
			case UP:
			case DOWN:
				break;
			case LEFT:
				if (!isLeft) {
					Path tempPath = getSelectedPathIfDirectory();
					if (tempPath == null) {
						break;
					}
					parentWelcome.SynctoLeft(tempPath.toString());
					if (!getSelectedItem().getmFilePath().toFile().isDirectory()) {
						parentWelcome.getLeftView().NavigateForNameAndScrollTo(lastSelectedTemp);
					}
				}
				break;
			case RIGHT:
				if (isLeft) {
					Path tempPath2 = getSelectedPathIfDirectory();
					if (tempPath2 == null) {
						break;
					}
					parentWelcome.SynctoRight(tempPath2.toString());
					if (!getSelectedItem().getmFilePath().toFile().isDirectory()) {
						parentWelcome.getRightView().NavigateForNameAndScrollTo(lastSelectedTemp);
					}
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
				// detect special character event
				if (key.isControlDown() || key.isAltDown()) {
					if (key.getText().toLowerCase().equals("a")) {
						int i = Table.getSelectionModel().getSelectedIndex();
						Table.getSelectionModel().clearSelection();
						Table.getSelectionModel().selectAll();
						Table.getSelectionModel().clearSelection(i);
						Table.getSelectionModel().select(i);
					}
					break;
				}
				if (key.isShiftDown()) {
					// if (StringHelper.getKeyAsShiftDown().containsKey(key.getText()))
					// newText += StringHelper.getKeyAsShiftDown().get(key.getText());
					// Get Note Prompt
					if (key.getText().toLowerCase().equals("n") && lastSelectedTemp != null) {
						lastSelectedTemp.getmNoteButton().fire();
					}
					break;
				}
				if (newText.equals(PredictNavigation.getText())) {
					newText += key.getText();
				}
				PredictNavigation.setText(newText.toLowerCase());

				doBack = false;
				break;
			}
		});

		// handle drag and drop events
		Table.setOnDragOver(new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {
				if (event.getDragboard().hasFiles() || event.getDragboard().hasContent(DataFormat.URL)
						|| event.getDragboard().hasString()) {
					event.acceptTransferModes(TransferMode.ANY);
				}
			}
		});
		// https://stackoverflow.com/questions/32534113/javafx-drag-and-drop-a-file-into-a-program
		Table.setOnDragDropped(new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {
				Dragboard db = event.getDragboard();
				// System.out.println(db.getContentTypes());
				if (db.hasFiles()) {
					// TODO later resolve if drag and drop occurs in same node
					// temp solution:
					List<Path> ToOperatePath = new ArrayList<>();
					List<File> ToOperatePathSameDir = new ArrayList<>();
					db.getFiles().forEach(file3 -> {
						if (!file3.getParentFile().equals(mDirectory)) {
							ToOperatePath.add(file3.toPath());
						} else {
							ToOperatePathSameDir.add(file3);
						}
					});
					ContextMenu mn = new ContextMenu();
					if (ToOperatePath.size() != 0) {
						MenuItem mnCopy = new MenuItem("Copy");
						MenuItem mnMove = new MenuItem("Move");
						MenuItem mnCancel = new MenuItem("Cancel");
						mnMove.setOnAction(e -> FileHelper.move(ToOperatePath, getDirectoryPath()));
						mnCopy.setOnAction(e -> FileHelper.copy(ToOperatePath, getDirectoryPath()));
						if (Setting.isUseTeraCopyByDefault()) {
							MenuItem mnTeraCopy = new MenuItem("Copy With TeraCopy");
							MenuItem mnTeraMove = new MenuItem("Move With TeraCopy");
							mnTeraCopy.setOnAction(e -> FileHelper.copyWithTeraCopy(ToOperatePath, getDirectoryPath()));
							mnTeraMove.setOnAction(e -> FileHelper.moveWithTeraCopy(ToOperatePath, getDirectoryPath()));
							mn.getItems().addAll(mnCopy, mnTeraCopy, mnMove, mnTeraMove, mnCancel);
						} else {
							mn.getItems().addAll(mnCopy, mnMove, mnCancel);
						}
					} else if (ToOperatePathSameDir.size() != 0) {
						List<Path> ToOperatePathSameDirAsPaths = ToOperatePathSameDir.stream().map(f -> f.toPath())
								.collect(Collectors.toList());

						Optional<TableViewModel> onDroppedT = rowMap.values().stream().collect(Collectors.toSet())
								.stream().filter(row -> {
									if (event.getY() - row.getHeight() >= row.getLayoutY()
											&& event.getY() - row.getHeight() <= row.getLayoutY() + row.getHeight()) {
										return true;
									}
									return false;
								}).map(row -> row.getItem()).findFirst();
						if (onDroppedT.isPresent()) {
							TableViewModel t = onDroppedT.get();
							String targetFileName = t.getmFilePath().toFile().getName();
							Path targetPath = t.getmFilePath();
							if (t.getmFilePath().toFile().isDirectory()) {
								MenuItem mnCopy = new MenuItem("Copy To \n\t\"" + targetFileName + "\"");
								MenuItem mnMove = new MenuItem("Move To \n\t\"" + targetFileName + "\"");
								mnCopy.setOnAction(e -> FileHelper.copy(ToOperatePathSameDirAsPaths, targetPath));
								mnMove.setOnAction(e -> FileHelper.move(ToOperatePathSameDirAsPaths, targetPath));
								if (Setting.isUseTeraCopyByDefault()) {
									MenuItem mnTeraCopy = new MenuItem(
											"Copy with TeraCopy To \n\t\"" + targetFileName + "\"");
									MenuItem mnTeraMove = new MenuItem(
											"Move with TeraCopy To \n\t\"" + targetFileName + "\"");
									mnTeraCopy.setOnAction(
											e -> FileHelper.copyWithTeraCopy(ToOperatePathSameDirAsPaths, targetPath));
									mnTeraMove.setOnAction(
											e -> FileHelper.moveWithTeraCopy(ToOperatePathSameDirAsPaths, targetPath));
									mn.getItems().addAll(mnCopy, mnTeraCopy, mnMove, mnTeraMove);
								} else {
									mn.getItems().addAll(mnCopy, mnMove);
								}
							}
						}
						MenuItem mnCopy = new MenuItem("Create Copy Here");
						MenuItem mnCancel = new MenuItem("Cancel");
						mnCopy.setOnAction(e -> {
							List<File> targetFiles = new ArrayList<>();
							for (File file : ToOperatePathSameDir) {
								targetFiles.add(FileHelper.getCopyFileName(file));
							}
							FileHelper.copyFiles(ToOperatePathSameDir, targetFiles);
						});
						if (Setting.isUseTeraCopyByDefault()) {
							MenuItem mnTeraCopy = new MenuItem("Create Copy Here With TeraCopy");
							mnTeraCopy.setOnAction(
									e -> FileHelper.copyWithTeraCopy(ToOperatePathSameDirAsPaths, getDirectoryPath()));
							mn.getItems().addAll(mnCopy, mnTeraCopy, mnCancel);
						} else {
							mn.getItems().addAll(mnCopy, mnCancel);
						}
					}
					mn.show(parentWelcome.getStage(), event.getScreenX(), event.getScreenY());

				} else if (db.hasContent(DataFormat.URL)) {
					// handle url create shortcuts
					String fileName = null;
					for (DataFormat file : db.getContentTypes()) {
						String curName = StringHelper.getValueFromCMDArgs(file.toString(), "name");
						if (curName != null) {
							fileName = curName;
						}
					}
					Path where = mDirectory.toPath().resolve(fileName);
					try {
						WindowsShortcut.createInternetShortcut(where.toFile(), db.getUrl(), "");
					} catch (IOException e) {
						e.printStackTrace();
						DialogHelper.showException(e);
					}
				} else if (db.hasContent(DataFormat.PLAIN_TEXT)) {
					// trying to parse string as path
					// also do on ctrl+ v action on table if has string url optional
					try {
						File test = new File(db.getContent(DataFormat.PLAIN_TEXT).toString());
						setmDirectoryThenRefresh(test);
					} catch (Exception e) {
						e.printStackTrace();
						DialogHelper.showException(e);
					}
				}
			}
		});

		// on drag put selected files to drag Most PowerFull external application
		// interaction
		Table.setOnDragDetected(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				Dragboard db = Table.startDragAndDrop(TransferMode.ANY);
				ClipboardContent cb = new ClipboardContent();
				List<File> selectedFiles = new ArrayList<>();
				getSelection().forEach(x -> selectedFiles.add(x.toFile()));
				cb.putFiles(selectedFiles);
				db.setContent(cb);
			}
		});

		Table.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
			if (SHORTCUT_FOCUS_TEXT_FIELD.match(e)) {
				PathField.requestFocus();
			} else if (SHORTCUT_SEARCH.match(e)) {
				focusSearchField();
			} else if (SHORTCUT_Clear_Search.match(e)) {
				clearSearchField();
			} else if (SHORTCUT_GO_UP.match(e)) {
				goUpParent();
			} else if (TOGGLE_FAVORITE.match(e)) {
				parentWelcome.ToogleFavorite();
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
			if (t == null) {
				return;
			}

			boolean tempIsDirectory = t.getmFilePath().toFile().isDirectory();

			if (m.getButton().equals(MouseButton.PRIMARY) && m.getClickCount() == 2) {
				navigate(t.getmFilePath());
				if (!isLeft && Setting.isBackSync() && tempIsDirectory) {
					parentWelcome.SynctoLeftParent();
				}
			} else if (isLeft && m.getButton().equals(MouseButton.PRIMARY) && parentWelcome.isAutoExpandToRight()) {
				if (tempIsDirectory) {
					parentWelcome.SynctoRight(t.getmFilePath().toString());
				} else if (isOutOfTheBoxHelper()) {
					parentWelcome.SynctoRight(getSelectedPathIfDirectory().toString());
					parentWelcome.getRightView().NavigateForNameAndScrollTo(t);
				}
			}
		});

		initializeTableRowFactory();
	}

	// TableView use handle pattern so in fact only few (13) TableRow is created
	// and on scroll only contents is changed that's way allow smooth and fast
	// scrolling of items row.
	Map<TableViewModel, TableRow<TableViewModel>> rowMap = new HashMap<>();

	/**
	 * this row factory only work when user do scroll to show the correspond row
	 */
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
							VLC.startXSPFInOrder(t.getmFilePath());
						});
					}
					// on row hover
					String rowtooltipPreText = "Name:\t" + t.getName();
					if (!t.getmFilePath().toFile().isDirectory()) {
						rowtooltipPreText += "\nSize:\t\t" + String.format("%.2f MB", t.getFileSize());
					}
					final String key = keyMapperToString(t);
					if (key != null) {
						try {
							updateVisualSeenButton(key, t);

						} catch (Exception e) {
							// fileTracker MapDetails return null for this key!
							// Even when printed it contain it.. but the good thing later on scroll do not
							// cause any problem and show status correctly.
							System.out.println("i entered as wrong key ");
							System.out.println(key);
						}

						t.setNoteText(mFileTracker.getNoteTooltipText(t.getName()));

						List<String> options = mFileTracker.getMapDetails().get(key);

						String tooltipPreText = " ";
						tooltipPreText = mFileTracker.getNoteTooltipText(key);
						t.setNoteText(tooltipPreText);
						if (!tooltipPreText.isEmpty()) {
							rowtooltipPreText += "\nNote:\t" + tooltipPreText;
						}
						t.getmNoteButton().setOnAction(new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent event) {
								String note = DialogHelper.showTextInputDialog("Quick Note Editor",
										"Add Note To see on hover",
										"Old note Was:\n" + mFileTracker.getNoteTooltipText(key),
										mFileTracker.getNoteTooltipText(key));

								// if null set it to space like it was
								if (note == null) {
									return; // keep note unchanged
								}
								if (note.isEmpty()) {
									note = " "; // reset note if is empty
								}
								// ensure > is not used
								note = note.replace('>', '<');
								if (!OutOfTheBoxRecursive) {
									mFileTracker.setTooltipsTexts(Table.getSelectionModel().getSelectedItems(), note);
									mFileTracker.setTooltipText(t.getName(), note);
								} else {
									mFileTracker.OutofTheBoxsetTooltipsTexts(
											Table.getSelectionModel().getSelectedItems(), t, note);
								}
								refreshTableWithSameData();
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
						if (!OutOfTheBoxRecursive) {
							if (VLC.isVLCMediaExt(t.getName())) {
								t.getOpenVLC().setOnMouseClicked(m -> {
									Path path = getDirectoryPath().resolve(t.getName());

									// if it is media file
									if (m.getButton().equals(MouseButton.PRIMARY)) {
										// load the preview
										new FilterVLCController(t.getmFilePath(), mFileTracker);
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
										} else {
											// just start the file with remote features
											VLC.watchWithRemote(t.getmFilePath(), "");
										}
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
								} else {
									ToggleSeenHelper(t);
								}
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
					// Common stuff
					setTooltip(getHoverTooltip(rowtooltipPreText));
				}
			}
		});
	}

	public static int count = 1; // optimized !
	public static boolean isLastChangedLeft = false;

	/**
	 * if you want to change directory view then change mDirectory then refresh view
	 * you can use instead {@link #setmDirectoryThenRefresh(File)}
	 */
	public void refresh(String isOutOfTheBoxPath) {
		// System.out.println(count);
		// count++;
		// if (count > 3)
		// try {
		// throw new Exception();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		isLastChangedLeft = isLeft;
		if (isOutOfTheBoxPath != null) {
			// for out of the box do change directory or add your DataTable stuff
			// before coming here .. this only used to update title and common preview stuff
			truePathField = isOutOfTheBoxPath;
			refreshIsOutOfTheBox();
			// refresh state
			parentWelcome.UpdateTitle(truePathField);
		} else {
			OutOfTheBoxRecursive = false;
			recursiveSearch.setSelected(false);
			NavigateRecursive.setVisible(false);
			addQueryOptionsPathField("recursive", null);
			truePathField = mDirectory.getAbsolutePath() + getQueryOptions();
			// PathField.setText(mDirectory.getAbsolutePath());
			// truePathField = mDirectory.getAbsolutePath();
			refreshIsOutOfTheBox();
			mFileTracker.loadMap(getDirectoryPath(), true, false);
			mFileTracker.resolveConflict();
			mWatchServiceHelper.changeObservableDirectory(mDirectory.toPath());
			showList(getCurrentFilesList());
			reloadSearchField();
			String stageTitle = mDirectory.getName();
			if (stageTitle.isEmpty()) {
				stageTitle = mDirectory.getAbsolutePath();
			}
			parentWelcome.UpdateTitle(stageTitle);
		}
		if (isLeft) {
			parentWelcome.updateFavoriteCheckBox(isOutOfTheBoxHelper);
		}
		PathField.setText(truePathField);

		LabelItemsNumber.setText(" #" + DataTable.size() + " items");

	}

	private static List<String> SpecialPath = Arrays.asList("/", "?", "&", "|");

	private boolean refreshIsOutOfTheBox() {
		// System.out.println((isLeft) ? "I'm left " : "i'm right");

		if (SpecialPath.stream().anyMatch(sp -> truePathField.contains(sp))) {
			// excluding search from out of the box
			if (getQueryOptions().contains("search=") && !getQueryOptions().contains("&")) {
				isOutOfTheBoxHelper = false;
				return false;
			}
			// out of the box
			// System.out.println("i'm out of the box");
			isOutOfTheBoxHelper = true;
			return true;
		} else {
			// System.out.println("i'm the box");
			isOutOfTheBoxHelper = false;
			return false;
		}
	}

	private static boolean alertError = true;

	private void showList(ArrayList<File> list) {
		DataTable.clear();

		// Resolving name section
		String error = "";
		String ExpandedError = "";
		boolean doRefresh = false;
		if (Setting.isAutoRenameUTFFile()) {
			List<File> toBeRemoved = new ArrayList<File>();
			for (File s : list) {
				try {
					if (FileHelper.rename(s.toPath(), true) != null) {
						doRefresh = true;
					}
				} catch (Exception e) {
					// Cannot Fix name unfortunately
					ExpandedError += e.getMessage();
					toBeRemoved.add(new File(s.toString()));
				}
			}
			if (doRefresh) {
				refresh(null);
				return;
			}
			if (alertError && !ExpandedError.isEmpty()) {
				if (isLeft) {
					error = "Left View Exception\n";
				} else {
					error = "Right View Exception\n";
				}
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

	private LinkedList<File> BackQueue = new LinkedList<File>();
	private LinkedList<File> NextQueue = new LinkedList<File>();

	public void setBackQueue(LinkedList<File> BackQueue) {
		this.BackQueue = BackQueue;
	}

	public void setNextQueue(LinkedList<File> NextQueue) {
		this.NextQueue = NextQueue;
	}

	public void RemoveLastFalseQueue() {
		if (!BackQueue.isEmpty()) {
			BackQueue.removeLast();
		}
		if (BackQueue.isEmpty()) {
			BackButton.setDisable(true);
		}
	}

	private void AddToQueue(File file) {
		// prevent redundant successive items
		if (BackQueue.isEmpty() || BackQueue.peekLast().compareTo(mDirectory) != 0) {
			BackQueue.add(file);
			BackButton.setDisable(false);
		}
	}

	/**
	 * As it Say back to last navigated directory if there isn't, go parent
	 * directory
	 */
	private void back() {
		// when back button is disabled mean that recent directory queue is empty
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

	// go up directory until reaching root
	private void goUpParent() {
		PredictNavigation.setText("");
		File parent = mDirectory.getParentFile();
		File oldmDirectory = mDirectory;
		if (parent != null) {
			AddToQueue(mDirectory);
			EmptyNextQueue();
			mDirectory = parent;
			if (mDirectory.exists()) {
				refresh(null);
				ScrollToName(oldmDirectory.getName());
			} else {
				goUpParent();
			}
		} else {
			OutOfTheBoxListRoots();
		}
	}

	// TODO separate this from resetting form only reset search !
	public void clearSearchField() {

		TableViewModel selected = Table.getSelectionModel().getSelectedItem();

		// addQueryOptionsPathField("search", null); already done in listener
		SearchField.setText("");
		Table.getSelectionModel().clearSelection(); // to prevent mis scroll
		Table.getSelectionModel().select(selected);

		// for better view item like centralize view it on escape
		// Table.getSelectionModel().select(DataTable.indexOf(selected));
		// Table.scrollTo(smartScrollIndex(scrollIndex));
		NavigateForNameAndScrollTo(selected);
	}

	private void OutOfTheBoxListRoots() {
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

	private void doRecursiveSearch() {
		// String depthANS = DialogHelper.showTextInputDialog("Recursive Search", "Depth
		// to consider",
		// "Enter depth value to consider beginning from left view folder and track all
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
		parentWelcome.ProcessTitle("Please Wait .. It might Take long for the first time...Indexing...");
		WatchServiceHelper.setRuning(false);
		/**
		 * History data: 120130 Files Indexed in 122858 milliseconds! 120130 Files
		 * Indexed in 116692 milliseconds!
		 *
		 * after updating javafx using only platform: Showing 120292 Files Indexed of
		 * 196713 in 58641 milliseconds!
		 *
		 * after using keyName of the map in generating TableViewModel and unifying the
		 * structure of value of MapDetails as options.get(0) == name Showing 120001
		 * Files Indexed of 196610 in 56487 milliseconds!
		 *
		 *
		 */
		Thread recursiveThread = new Thread() {

			@Override
			public void run() {
				Instant start = Instant.now();
				Instant finish;
				long timeElapsed;
				String msg;
				Path dir = getDirectoryPath();
				mFileTracker.getMapDetails().clear();
				Platform.runLater(() -> DataTable.clear());
				OutOfTheBoxRecursive = true;
				boolean doSort = false;

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
						doSort = true;
					}
					Stream<Path> paths = r.getParent().stream();
					if (doSort) {
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
					mFileTracker.OutofTheBoxAddToMapRecusive(dir);
					for (Path p : paths.collect(Collectors.toList())) {
						if (!recursiveSearch.isSelected()) {
							break;
						}
						mFileTracker.OutofTheBoxAddToMapRecusive(p);
						if (mFileTracker.getMapDetails().size() > Setting.getMaxLimitFilesRecursive()) {
							break;
						}
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
				List<TableViewModel> allThem = RecursiveHelperGetData();
				Platform.runLater(() -> {
					RecursiveHelperLoadDataTable(allThem);
				});

				finish = Instant.now();
				WatchServiceHelper.setRuning(true);

				timeElapsed = Duration.between(start, finish).toMillis(); // in millis
				msg = "Showing " + allThem.size() + " Files Indexed " + (doSort ? "of " + r.getFilesCount() : "")
						+ " in " + timeElapsed + " milliseconds!"
						+ (doSort ? "\nYou Can Change Limit File count in menu Tracker Setting" : "")
						+ (!recursiveSearch.isSelected()
								? "\nSearch Stopped To Reset View (do/un)check me Again, Or double click on Clear Button"
								: "");
				if (!recursiveSearch.isSelected()) {
					recursiveSearch.setSelected(true);
				}
				// System.out.println(msg);
				// TODO preserve show in case of search is unselected
				Platform.runLater(() -> RecursiveHelperUpdateTitle(msg));
				// we do reinitialize because it's action was changed to handle in middle search
				// stop see up
				initializeRecursiveSearch();
			}
		};
		recursiveHelperSetBlocked(true);
		recursiveThread.start();
	}

	private void EmptyNextQueue() {
		NextQueue.clear();
		NextButton.setDisable(true);
	}

	/**
	 *
	 * @param searchPattern ';' to combine multiple search statement '!' to exclude
	 *                      from search
	 *
	 *                      example i want all vlc media that contain name word and
	 *                      not excel i search: 'vlc;word;!excel'
	 * @param model
	 * @return
	 */
	private boolean filterModel(String searchPattern, TableViewModel model) {
		// If filter text is empty, display all.
		List<String> advancedFilter = Arrays.asList(searchPattern.split(";"));
		boolean isRespect = true;
		boolean state = true;
		String modelName = model.getName().toLowerCase();
		String note = model.getNoteText().trim().toLowerCase();
		boolean isMediaFile = VLC.isVLCMediaExt(modelName);
		boolean isVideo = VLC.isVideo(modelName);
		boolean isAudio = VLC.isAudio(modelName);
		boolean isImage = PhotoViewerController.ArrayIMGExt.contains(StringHelper.getExtention(modelName));
		for (String filerItem : advancedFilter) {
			String lowerCaseFilter = filerItem.toLowerCase();
			state = true;
			if (lowerCaseFilter.startsWith("!")) {
				state = false;
				lowerCaseFilter = lowerCaseFilter.substring(lowerCaseFilter.length() > 0 ? 1 : 0);
			} else if (lowerCaseFilter.startsWith("|")) {
				if (isRespect == true) {
					continue;
				}
				isRespect = true;
				lowerCaseFilter = lowerCaseFilter.substring(lowerCaseFilter.length() > 0 ? 1 : 0);
			}
			if (lowerCaseFilter == null || lowerCaseFilter.isEmpty()) {
				isRespect &= true;
				continue;
			}
			// Compare with filerItem text.
			if (modelName.contains(lowerCaseFilter)) {
				isRespect &= state; // filerItem matches name.
			} else if (lowerCaseFilter.contains("vlc") && isMediaFile) {
				isRespect &= state;
			} else if (lowerCaseFilter.contains("video") && isVideo) {
				isRespect &= state;
			} else if (lowerCaseFilter.contains("image") && isImage) {
				isRespect &= state;
			} else if (lowerCaseFilter.contains("audio") && isAudio) {
				isRespect &= state;
			} else if (note.contains(lowerCaseFilter)) {
				isRespect &= state; // search note if exist
			} else if ((model.getMarkSeen().getText().equals("U") || model.getMarkSeen().getText().equals("-"))
					&& "un".toLowerCase().contains(lowerCaseFilter)) {
				isRespect &= state; // filerItem unseen.
			} else if (model.getMarkSeen().getText().equals("S") && "yes".toLowerCase().contains(lowerCaseFilter)) {
				isRespect &= state;// filerItem seen.
			} else if (lowerCaseFilter.startsWith("<") || lowerCaseFilter.startsWith(">")) {
				// https://stackoverflow.com/questions/2367381/how-to-extract-numbers-from-a-string-and-get-an-array-of-ints/2367418
				int toCompareWith = 10;
				int firstIndexOfSearchNumber = 1;
				boolean isEqualToo = false;
				if (lowerCaseFilter.length() > 1 && lowerCaseFilter.startsWith("=", 1)) {
					isEqualToo = true;
					firstIndexOfSearchNumber = 2;

				}
				try {
					toCompareWith = Integer.parseInt(lowerCaseFilter.substring(firstIndexOfSearchNumber));
				} catch (Exception e) {
				}
				Pattern p = Pattern.compile("-?\\d+");
				Matcher m = p.matcher(FilenameUtils.getBaseName(modelName));
				boolean valid = false;
				if (lowerCaseFilter.startsWith("<")) {
					while (m.find()) {
						int nbTemp = Math.abs(Integer.parseInt(m.group()));
						if (nbTemp < toCompareWith || isEqualToo && nbTemp == toCompareWith) {
							valid = true;
						}
					}
				} else {
					while (m.find()) {
						int nbTemp = Math.abs(Integer.parseInt(m.group()));
						if (nbTemp > toCompareWith || isEqualToo && nbTemp == toCompareWith) {
							valid = true;
						}
					}
				}
				if (valid) {
					isRespect &= state;
				} else {
					isRespect &= !state;
				}
			} else {
				isRespect &= !state; // Does not match.
			}

		}
		return isRespect;
	}

	public void focusSearchField() {
		SearchField.requestFocus();
	}

	// this to update pathField also
	// private void SearchField.setText(String text) {
	// SearchField.setText(text);
	// KeyEvent ke = new KeyEvent(KeyEvent.KEY_RELEASED, "a", "", KeyCode.UNDEFINED,
	// false, false, false, false);
	// SearchField.fireEvent(ke);
	// }

	public void focusTable() {
		if (Table.getSelectionModel().getSelectedCells().size() <= 0) {
			// Table.getSelectionModel().select(0);
			Table.getSelectionModel().selectFirst();
		}
		Table.requestFocus();
	}

	public Button getBackButton() {
		return BackButton;
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
		StringHelper.SortNaturalArrayFiles(listFiles);

		return listFiles;
	}

	public List<String> getCurrentFilesListName() {
		return getCurrentFilesList().stream().map(s -> s.getName()).collect(Collectors.toList());
	}

	public Path getDirectoryPath() {
		return mDirectory.toPath();
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

	public File getmDirectory() {
		return mDirectory;
	}

	public FileTracker getMfileTracker() {
		return mFileTracker;
	}

	public WatchServiceHelper getmWatchServiceHelper() {
		return mWatchServiceHelper;
	}

	public Button getNextButton() {
		return NextButton;
	}

	public WelcomeController getParentWelcome() {
		return parentWelcome;
	}

	public TextField getPathField() {
		return PathField;
	}

	private String getQueryOptions() {
		int temp = truePathField.indexOf("?");
		if (temp != -1) {
			return truePathField.substring(temp);
		} else {
			return "";
		}
	}

	public TableViewModel getSelectedItem() {
		return Table.getSelectionModel().getSelectedItem();
	}

	private Path getSelectedPathIfDirectory() {
		TableViewModel t = Table.getSelectionModel().getSelectedItem();
		if (t == null) {
			return null;
		}
		if (!t.getmFilePath().toFile().isDirectory()) {
			return t.getmFilePath().getParent();
		}
		return t.getmFilePath();
	}

	// this helper is to optimize call of the function
	// so only call when really need to update state

	// for recursive mode use OutOfTheBoxRecursive

	public List<Path> getSelection() {
		List<Path> selection = new ArrayList<>();
		for (TableViewModel item : Table.getSelectionModel().getSelectedItems()) {
			selection.add(item.getmFilePath());
		}
		return selection;
	}

	private void initializeRecursiveSearch() {
		recursiveSearch.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if (recursiveSearch.isSelected()) {
					if (isOutOfTheBoxHelper) {
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

	/**
	 * check {@link #rollerSearchKey}
	 */
	private Integer rollerSearchIndex = 0;

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
			add(";andThis");
			add(";|orThis");
			add(";!notThis");
			add(";video");
			add(";audio");
		}
	};

	// Needed to Undo last modification rename
	private static LinkedList<HashMap<Path, Path>> NewToOldRename = new LinkedList<>();

	private void initializeToolsMenu() {
		MenuItem copyBaseNames = new MenuItem("Copy Base Names");
		MenuItem pasteBaseNames = new MenuItem("Paste Base Names");
		MenuItem undoLastPasteNames = new MenuItem("Undo Last Paste Names");
		MenuItem renameAction = new MenuItem("Rename Selected");
		MenuItem newFileAction = new MenuItem("Create New File");
		MenuItem newFolderAction = new MenuItem("Create New Folder");
		MenuItem newTrackerPlayerPlaylist = new MenuItem("Create New Cortana Playlist");
		MenuItem newTrackerPlayerAny = new MenuItem("Create New Cortana Shortcut");
		ToolsMenu.getItems().addAll(copyBaseNames, pasteBaseNames, undoLastPasteNames, renameAction, newFileAction,
				newFolderAction, newTrackerPlayerPlaylist, newTrackerPlayerAny);

		newTrackerPlayerAny.setOnAction(e -> {
			TableViewModel t = Table.getSelectionModel().getSelectedItem();
			if (t == null) {
				DialogHelper.showAlert(AlertType.INFORMATION, "Cortana Shortcut", "Select an item from Table first",
						"");
				return;
			}
			String name = TrackerPlayer.getPlaylistName();
			TrackerPlayer.createNewShortcutPlaylist(name, t.getmFilePath());
		});
		newTrackerPlayerPlaylist.setOnAction(e -> {
			List<Path> files = new ArrayList<>();
			for (TableViewModel t : Table.getSelectionModel().getSelectedItems()) {
				File tFile = t.getmFilePath().toFile();
				if (VLC.isVLCMediaExt(tFile.getName()) || tFile.isDirectory()) {
					files.add(t.getmFilePath());
				}
			}
			if (files.size() == 0) {
				String allFilesString = "";
				for (TableViewModel t : DataTable) {
					File tFile = t.getmFilePath().toFile();
					if (VLC.isVLCMediaExt(tFile.getName()) || tFile.isDirectory()) {
						files.add(t.getmFilePath());
						allFilesString += t.getName() + "\n";
					}
				}
				if (files.size() == 0) {
					return;
				} else {
					DialogHelper.showExpandableAlert(AlertType.INFORMATION, "Create New Cortana Playlist",
							"All media in current view inserted", "Those are Media files detected:", allFilesString);
				}
			}
			String name = TrackerPlayer.getPlaylistName();
			if (name != null) {
				TrackerPlayer.createNewShortcutPlaylist(name, files);
			}
		});
		renameAction.setOnAction(e -> {
			requestFocus();
			parentWelcome.rename();
		});
		newFileAction.setOnAction(e -> {
			requestFocus();
			parentWelcome.createFile();
		});
		newFolderAction.setOnAction(e -> {
			requestFocus();
			parentWelcome.createDirectory();
		});
		undoLastPasteNames.setDisable(true);

		copyBaseNames.setOnAction(new EventHandler<ActionEvent>() {

			/**
			 * Check similar {@link FilterVLCController#getCopyRaw}
			 */
			@Override
			public void handle(ActionEvent arg0) {
				ObservableList<TableViewModel> toWorkWith = null;
				String warningAlert = "";
				if (Table.getSelectionModel().getSelectedItems().size() > 0) {
					toWorkWith = Table.getSelectionModel().getSelectedItems();
				} else {
					toWorkWith = sortedData;
					warningAlert = "- You can also make a selection Source First from the Table!\n";
				}
				String myString = "";
				for (TableViewModel t : toWorkWith) {
					String item = FilenameUtils.getBaseName(t.getName()) + "\n";
					myString += item;
				}
				Clipboard clipboard = Clipboard.getSystemClipboard();
				ClipboardContent content = new ClipboardContent();
				content.putString(myString);
				clipboard.setContent(content);
				DialogHelper.showExpandableAlert(AlertType.INFORMATION, "Copy Base Names",
						"Content Copied Successfully to Clipboard\n-----   " + toWorkWith.size() + " ----- Items Added",
						"Note:\n" + warningAlert + "- Use it as you like with Paste Names Options!\nContent:",
						myString);
			}
		});

		pasteBaseNames.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {

				boolean error = false;
				tryBlock: try {
					String myString = Clipboard.getSystemClipboard().getString();
					if (myString == null) {
						error = true;
						break tryBlock;
					}
					String[] swapNames = myString.split("\n");

					String fullAlertReport = "";
					String warningAlert = "";
					ObservableList<TableViewModel> toWorkWith = null;
					if (Table.getSelectionModel().getSelectedItems().size() > 0) {
						toWorkWith = Table.getSelectionModel().getSelectedItems();
					} else {
						toWorkWith = sortedData;
						warningAlert = "\nYou can also make a selection Target First from the Table!";
					}
					int i = 0;
					List<String> toRenameWithFinal = new ArrayList<>();
					for (TableViewModel t : toWorkWith) {
						if (i < swapNames.length) {
							String ext = FilenameUtils.getExtension(t.getName());
							String report = "";
							if (ext.length() > 0) {
								ext = "." + ext;
							}
							String finalName = swapNames[i] + ext;
							// removing invalid special character
							finalName = finalName.replaceAll("[\\\\/:*?\"<>|\r]", "");
							toRenameWithFinal.add(finalName);

							report = "*R" + (i + 1) + "- " + t.getName() + " --> " + toRenameWithFinal.get(i) + "\n";
							fullAlertReport += report;
							i++;
						} else {
							warningAlert += "\nCount of Pasted Names does not match number of selected items!";
							break;
						}
					}

					boolean ans = DialogHelper.showExpandableConfirmationDialog("Paste Base Names",
							"Preview Change" + "\nPending -----   " + toRenameWithFinal.size()
									+ " ----- Changes Pair Rename\n",
							"Each Separated line Name assigned to selection item in table order. \n" + "Note:"
									+ warningAlert,
							fullAlertReport);
					if (ans) {
						i = 0;
						String renameError = "";
						HashMap<Path, Path> currentNewToOldRename = new HashMap<>();
						for (TableViewModel t : toWorkWith) {
							if (i < toRenameWithFinal.size()) {
								try {
									Path oldFile = t.getmFilePath();
									Path newFile = t.getmFilePath().resolveSibling(toRenameWithFinal.get(i));
									FileHelper.RenameHelper(oldFile, newFile);
									currentNewToOldRename.put(newFile, oldFile);

								} catch (IOException e) {
									renameError += t.getmFilePath().getFileName() + " --> " + toRenameWithFinal.get(i)
											+ "\n";
									warningAlert += e.getClass() + ": " + e.getMessage() + "\n";
									e.printStackTrace();
								}
								i++;
							} else {
								break;
							}
						}
						NewToOldRename.add(currentNewToOldRename);
						undoLastPasteNames.setDisable(false);
						if (!renameError.isEmpty()) {
							DialogHelper.showExpandableAlert(AlertType.ERROR, "Paste Base Names",
									"Some Content were not renamed Successfully!",
									"This may be caused by illegal character or redundant names in input clipboard. \n",
									warningAlert + "\nSource File --> Rename expected:\n" + renameError);
						}

					}
				} catch (HeadlessException e) {
					e.printStackTrace();

				}
				if (error) {
					DialogHelper.showTextInputDialog("Paste Raw Data", "Content Parse failed!",
							"SomeThing went wrong,\nAre you Sure You have the Data,\n Try pasting it here to recheck\nResetting DataTable to initial value",
							"Raw Data Here");
				}

			}
		});
		undoLastPasteNames.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				String summary = "This action is used when wrongly 'Pasted base names'."
						+ "\nIn that case a report of redo will be displayed for to be confirmed.";
				String title = "Redo Last Paste Names";
				// due to disable status it will never enter this if, but just in case more
				// check...
				if (NewToOldRename.size() == 0) {
					DialogHelper.showAlert(AlertType.INFORMATION, title, "No recent modification applied.", summary);
					return;
				}
				String changeReportFiles = "";
				String notFoundFiles = "";
				HashMap<Path, Path> lastNewToOldRename = NewToOldRename.peekLast();
				Path workingDir = lastNewToOldRename.get(lastNewToOldRename.keySet().toArray()[0]).getParent();
				int i = 0;
				int found = 0;
				for (Path newPath : lastNewToOldRename.keySet()) {
					if (newPath.toFile().exists()) {
						changeReportFiles += "*R" + (i + 1) + "- " + newPath.getFileName() + " --> "
								+ lastNewToOldRename.get(newPath).getFileName() + "\n";
						found++;
					} else {
						notFoundFiles += "*N" + (i + 1) + "- " + newPath.getFileName() + " !!->"
								+ lastNewToOldRename.get(newPath).getFileName() + "\n";
					}
					i++;
				}
				if (!notFoundFiles.isEmpty()) {
					notFoundFiles = "Not Found Files:\n" + notFoundFiles;
				}
				boolean ans = DialogHelper.showExpandableConfirmationDialog(title, "Preview Change"
						+ "\nPending -----   " + found + " ----- Changes Pair Rename"
						+ (i - found != 0 ? "\nNot Found -----   " + (i - found) + " ----- Files (renamed or moved)\n"
								: ""),
						summary, "Working in:  " + workingDir + "\n\nModifications:\n" + changeReportFiles + "\n"
								+ notFoundFiles);
				if (ans) {
					i = 0;
					String renameError = "";
					String warningAlert = "";
					for (Path newPath : lastNewToOldRename.keySet()) {
						try {
							if (newPath.toFile().exists()) {
								Files.move(newPath, lastNewToOldRename.get(newPath));
							}
						} catch (IOException e) {
							renameError += newPath.getFileName() + " -->"
									+ lastNewToOldRename.get(newPath).getFileName() + "\n";
							warningAlert += e.getClass() + ": " + e.getMessage() + "\n";
							e.printStackTrace();
						}
						i++;
					}
					NewToOldRename.removeLast();
					if (NewToOldRename.size() == 0) {
						undoLastPasteNames.setDisable(true);
					}
					if (!renameError.isEmpty()) {
						DialogHelper.showExpandableAlert(AlertType.ERROR, title,
								"Some Content were not renamed Successfully!",
								"This may be caused by illegal character names. \n",
								warningAlert + "\nSource File --> Rename expected:\n" + renameError);
					}

				}

			}
		});

	}

	public boolean isFocused() {
		return Table.isFocused() || UpButton.isFocused() || RefreshButton.isFocused() || SearchField.isFocused();
	}

	public boolean isFocusedTable() {
		return Table.isFocused();
	}

	public boolean isOutOfTheBoxHelper() {
		return isOutOfTheBoxHelper;
	}

	/**
	 * Only use for {@link FileTracker#FileTracker(Path)}
	 * 
	 * @param value
	 */
	public void setIsOutOfTheBoxHelper(boolean value) {
		isOutOfTheBoxHelper = value;
	}

	public boolean isOutOfTheBoxRecursive() {
		return OutOfTheBoxRecursive;
	}

	// return the correct key to be used in map details
	public String keyMapperToString(TableViewModel t) {
		if (OutOfTheBoxRecursive) {
			if (mFileTracker.isTrackedOutFolder(t.getmFilePath().getParent())) {
				// the key is the full path
				return t.getmFilePath().toFile().toURI().toString();
			}

		} else if (isOutOfTheBoxHelper) {
			return null;
		} else if (mFileTracker.isTracked()) {
			// normal case key is just the name
			return t.getName();
		}

		return null;
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
			setmDirectoryThenRefresh(selectedFile);
			isDirectory = true;
		} else {
			try {
				if (VLC.isWellSetup() && VLC.isVLCMediaExt(filePath.toFile().getName())
						&& Table.getSelectionModel().getSelectedItems().size() != 1) {
					String files = " --playlist-enqueue --loop";
					for (TableViewModel t : Table.getSelectionModel().getSelectedItems()) {
						if (VLC.isVLCMediaExt(t.getName())) {
							files += " " + t.getmFilePath().toUri();
						}
					}
					VLC.StartVlc(files);
					// requesting JVM for running Garbage Collector
					// in order to release process from memory from being expanded as vlc will
					// take large resources and may get freeze after certain ammount of time
					// read more at : https://www.geeksforgeeks.org/garbage-collection-java/
					System.gc();

					// we always start media because playlist do not start automatically
				} else {
					// deal other types of files
					if (StringHelper.getExtention(selectedFile.getName()).equals("PDF")) {
						// open bunch of PDF
						StringHelper.openFiles(Table.getSelectionModel().getSelectedItems().stream()
								.map(p -> p.getmFilePath().toFile())
								.filter(p -> StringHelper.getExtention(p.getName()).equals("PDF"))
								.collect(Collectors.toList()));

						// open bunch of Image or an image
					} else if (PhotoViewerController.ArrayIMGExt
							.contains(StringHelper.getExtention(selectedFile.getName()))) {
						new PhotoViewerController(Table.getSelectionModel().getSelectedItems().stream()
								.map(p -> p.getmFilePath().toFile())
								.filter(p -> PhotoViewerController.ArrayIMGExt
										.contains(StringHelper.getExtention(p.getName())))
								.collect(Collectors.toList()), selectedFile, parentWelcome);

					} else {
						// default option
						// AWT function used here for better support of opening file from network
						// AWT was not used to prevent hole stack of AWT but no good alternative in
						// javaFX
						// Desktop.getDesktop().open(selectedFile);
						StringHelper.openFile(selectedFile);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
				DialogHelper.showException(e);
			}
		}
		PredictNavigation.setText("");
		doBack = true;
		return isDirectory;
	}

	protected void NavigateForNameAndScrollTo(TableViewModel toNavigateFor) {
		if (toNavigateFor != null) {
			ScrollToName(toNavigateFor.getName());
		}
	}

	public int indexOfName(String fileName) {
		TableViewModel found = getViewModelOfName(fileName);
		return sortedData.indexOf(found);
	}

	public void selectIndex(int index) {
		TableViewModel found = sortedData.get(index);
		if (found != null) {
			Table.getSelectionModel().select(found);
			Table.scrollTo(smartScrollIndex(sortedData.indexOf(found)));
		}
	}

	public void ScrollToName(String fileName) {
		TableViewModel found = getViewModelOfName(fileName);
		if (found == null) {
			return;
		}
		Table.getSelectionModel().select(found);
		Table.scrollTo(smartScrollIndex(sortedData.indexOf(found)));
	}

	/**
	 * Otherwise TableViewModel in sorted data that have corresponding name
	 *
	 * @param fileName name to search for
	 * @return
	 */
	private TableViewModel getViewModelOfName(String fileName) {
		TableViewModel found = null;
		for (TableViewModel t : DataTable) {
			if (t.getName().equals(fileName)) {
				found = t;
				break;
			}
		}
		return found;
	}

	private int smartScrollIndex(int scrollIndex) {
		if (scrollIndex <= 4) {
			return 0;
		}

		for (int i = 4; i > 0; i--) {
			if (scrollIndex - i > 0) {
				scrollIndex -= i;
				break;
			}
		}
		return scrollIndex;
	}

	private boolean doBack = true;
	private boolean doUp = false;
	// second use: do excessive up(goParent function)
	private Thread doUpThreadOff = new Thread() {

		@Override
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
	// first use : prevent excessive back when cleaning prediction text with
	private Thread EnableMisBack = new Thread() {

		@Override
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
	// https://howtodoinjava.com/java/multi-threading/java-thread-pool-executor-example/
	ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

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
		for (String pathST : mFileTracker.getMapDetails().keySet()) {
			// List<String> options = mfileTracker.getMapDetails().get(pathST);
			Path pathItem = StringHelper.parseUriToPath(pathST);
			if (pathItem != null) {
				allRowModel.add(new TableViewModel(" ", pathItem.toFile().getName(), pathItem));
			}
		}
		// StringHelper.endTimerAndDisplay();
		return allRowModel;
	}

	private void RecursiveHelperLoadDataTable(List<TableViewModel> allRowModel) {
		DataTable.clear();
		DataTable.addAll(allRowModel);
	}

	private void recursiveHelperSetBlocked(boolean state) {
		RefreshButton.setDisable(state);
		Table.setDisable(state);
		NavigateRecursive.setDisable(state);
		PathField.setDisable(state);
		UpButton.setDisable(state);
		BackButton.setDisable(state);
		NextButton.setDisable(state);
		Explorer.setDisable(state);
		SearchField.setDisable(state);
		parentWelcome.RecursiveHelpersetBlocked(state);

	}

	private void RecursiveHelperUpdateTitle(String message) {
		showToastMessage(message);
		parentWelcome.ResetTitle();
		refresh(truePathField);
		recursiveHelperSetBlocked(false);
		Table.requestFocus();
		// to refresh selection number and select the first one
		Table.getSelectionModel().select(0);
	}

	private ContextMenu showToastMessage(String message) {
		ContextMenu mn = new ContextMenu();
		MenuItem mnChild = new MenuItem(message);
		mn.getItems().add(mnChild);
		mn.getStyleClass().addAll("lastRowSelected");
		mnChild.getStyleClass().addAll("lastRowSelected");
		double xLoc = parentWelcome.getStage().getX() + Table.getLayoutX() + Table.getWidth() * 0.1;
		double yLoc = parentWelcome.getStage().getY() + Table.getLayoutY() + Table.getHeight() + 70;
		mn.show(parentWelcome.getStage(), xLoc, yLoc);
		return mn;
	}

	public void refreshAsPathField() {
		// when doing search this cause to false navigate
		// this doesn't work with multiSelection
		// auto scroll
		// TODO
		saveLastSelectToScroll();
		setPathFieldThenRefresh(getPathField().getText());
		restoreLastSelectAndScroll();
	}

	/**
	 * The use of this function is that sometimes after changing
	 * {@link TableViewModel#setNoteText(String)} the value isn't updated in the
	 * view unless something refresh the table so here will do it automatically all
	 * way reserving the old selection also
	 */
	private void refreshTableWithSameData() {
		List<TableViewModel> Copy = new ArrayList<>(DataTable);
		// reserve selection before refreshing the table
		// be aware that if initialized spaces more than needed so table will contain 0
		// and first row get selected even when it's not
		int[] toSelect = new int[Table.getSelectionModel().getSelectedItems().size()];
		int j = 0;
		for (int i : Table.getSelectionModel().getSelectedIndices()) {
			toSelect[j++] = i;
		}
		DataTable.clear();
		DataTable.addAll(Copy);
		// restore reserve
		Table.getSelectionModel().selectIndices(-1, toSelect);
		Table.requestFocus();
	}

	public void requestFocus() {
		Table.requestFocus();

	}

	private void restoreLastSelectAndScroll() {
		NavigateForNameAndScrollTo(LastSelectedToScroll);
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
		} else {
			Explorer.fire();
		}
	}

	private TableViewModel LastSelectedToScroll;

	private void saveLastSelectToScroll() {
		LastSelectedToScroll = Table.getSelectionModel().getSelectedItem();
	}

	// is this function causing the warning ?
	// May 03, 2019 8:02:57 PM com.sun.javafx.scene.control.skin.VirtualFlow
	// addTrailingCells
	// INFO: index exceeds maxCellCount. Check size calculations for class
	// application.SplitViewController$1

	public void select(String regex) {
		if (regex.startsWith("*")) {
			regex = "." + regex;
		}
		Table.getSelectionModel().clearSelection();
		for (int i = 0; i < DataTable.size(); ++i) {
			TableViewModel model = DataTable.get(i);
			String item = model.getName();
			if (item.matches(regex) || StringHelper.containsWord(item, regex)) {
				Table.getSelectionModel().select(model);
			}
		}
	}

	public void setBackButton(Button backButton) {
		BackButton = backButton;
	}

	// special use like for rename and do not Queue
	public void setmDirectory(File mDirectory) {
		this.mDirectory = mDirectory;
	}

	// this method is useful to handle call of changing directory
	// and enqueue it to back button..
	// and so adding old directory to queue and so on
	public void setmDirectoryThenRefresh(File mDirectory) {
		PredictNavigation.setText("");
		if (mDirectory.compareTo(this.mDirectory) != 0) {
			AddToQueue(this.mDirectory);
			EmptyNextQueue();
		}
		this.mDirectory = mDirectory;
		refresh(null);
		Table.scrollTo(0);
	}

	public void setMfileTracker(FileTracker mfileTracker) {
		mFileTracker = mfileTracker;
	}

	public void setNextButton(Button nextButton) {
		NextButton = nextButton;
	}

	public void setParentWelcome(WelcomeController parentWelcome) {
		this.parentWelcome = parentWelcome;
	}

	public void setPredictNavigation(String predictNavigation) {
		PredictNavigation.setText(predictNavigation);
	}

	private void showContextMenu() {
		TableViewModel t = Table.getSelectionModel().getSelectedItem();
		if (t != null) {
			ArrayList<Path> toShow = new ArrayList<>();
			for (TableViewModel temp : Table.getSelectionModel().getSelectedItems()) {
				toShow.add(temp.getmFilePath());
			}
			RunMenu.showMenu(toShow);
		} else {
			RunMenu.showMenu(Arrays.asList(mDirectory.toPath()));
		}
	}

	public void switchRecursive() {
		// important fire a checkbox do/un check it before calling it's action
		recursiveSearch.fire();
	}

	private void ToggleSeenHelper(TableViewModel clicked) {
		if (!OutOfTheBoxRecursive) {
			mFileTracker.toggleSelectionSeen(Table.getSelectionModel().getSelectedItems(),
					xspfRelatedWithSelection(clicked), clicked);
		} else {
			mFileTracker.OutofTheBoxtoggleSelectionSeen(Table.getSelectionModel().getSelectedItems(), clicked);
		}
		// when toggle seen if yes or un is in search field do update
		reloadSearchField();
	}

	private boolean untrackedBehavior(TableViewModel t) {
		boolean ans;
		// returned false
		if (OutOfTheBoxRecursive || isOutOfTheBoxHelper) {
			ans = DialogHelper.showConfirmationDialog("Track new Folder[Recursive Mode]", "Ready to Be Stunned ?",
					"Tracking a new Folder will create a hidden file .tracker_explorer.txt"
							+ " in the folder to save data tracker !"
							+ "\nIn recursive mode the creation will trigger on all Selected Items.");
			if (!ans) {
				return ans;
			}

			Set<Path> paths = Table.getSelectionModel().getSelectedItems().stream()
					.map(selection -> selection.getmFilePath().getParent()).collect(Collectors.toSet());
			paths.add(t.getmFilePath().getParent());
			mFileTracker.OutofTheBoxTrackFolder(paths);
			refreshTableWithSameData();
		} else {

			ans = mFileTracker.getAns();
			if (ans) {
				mFileTracker.trackNewFolder();
			}
		}
		return ans;
	}

	private void updatePathField(Map<String, String> options) {
		truePathField = truePathField.replace(getQueryOptions(), "");
		if (options == null) {
			return;
		}
		truePathField += "?";
		int i = 0;
		for (String Keyoption : options.keySet()) {
			if (i++ != 0) {
				truePathField += "&";
			}
			truePathField += Keyoption + "=" + options.get(Keyoption);
		}
		if (truePathField.endsWith("?")) {
			truePathField = truePathField.replace("?", "");
		}
		PathField.setText(truePathField);
	}

	public void updateVisualSeenButton(String key, TableViewModel t) {
		// property of toggle button from map
		t.updateMarkSeen(mFileTracker.isSeen(key));
	}

	private ArrayList<TableViewModel> xspfRelatedWithSelection(TableViewModel clicked) {

		// if XSPF is clicked also auto sync seen its video files if exist

		// to collect all model to sync
		ArrayList<TableViewModel> allRelated = new ArrayList<>();

		// to collect all base name of XSPF
		Map<String, TableViewModel> mapAllXSPF = new HashMap<String, TableViewModel>();

		// to include clicked in below for loop
		// Table.getSelectionModel().select(DataTable.indexOf(clicked));
		ArrayList<TableViewModel> tempOver = new ArrayList<>();
		tempOver.addAll(Table.getSelectionModel().getSelectedItems());
		tempOver.add(clicked);

		for (TableViewModel t : tempOver) {
			String ext = StringHelper.getExtention(t.getName());
			if (ext.equals("XSPF") && t.getName().length() > 15) {
				String basename = t.getName().substring(0, t.getName().length() - 15).toUpperCase();
				mapAllXSPF.put(basename, t);
			}
		}
		for (TableViewModel tSearch : DataTable) {
			String tBase = StringHelper.getBaseName(tSearch.getName());
			if (mapAllXSPF.containsKey(tBase)) {
				mFileTracker.setSeen(tSearch.getName(), mFileTracker.getSeen(mapAllXSPF.get(tBase)), tSearch);
				// first if -> to force toggle if only video is selected and clicked on XSPF
				// second if ->to prevent double toggle
				if (Table.getSelectionModel().getSelectedItems().size() == 1
						|| !Table.getSelectionModel().getSelectedItems().contains(tSearch)) {
					allRelated.add(tSearch);
				}
			}
		}
		if (allRelated.size() == 0) {
			return null;
		}
		return allRelated;
	}

	public void saveStateToSplitState(SplitViewState state) {
		// Save current state view to state
		state.setmDirectory(getmDirectory());
		state.setSearchKeyword(SearchField.getText());

		state.setSelectedIndices(Table.getSelectionModel().getSelectedIndices());
		state.setScrollTo(Table.getSelectionModel().getSelectedIndex());
	}

	public void restoreSplitViewState(SplitViewState state) {
		// Restore state view to current view
		setBackQueue(state.getBackQueue());
		setNextQueue(state.getNextQueue());
		if (mDirectory.compareTo(state.getmDirectoryExisting()) != 0) {
			setmDirectoryThenRefresh(state.getmDirectoryExisting());
			// clear false change between tabs
			RemoveLastFalseQueue();
		}
		// restore search keyword
		SearchField.setText(state.getSearchKeyword());

		// restore selections
		Table.getSelectionModel().clearSelection();
		Table.getSelectionModel().selectIndices(-1, state.getSelectedIndices());
		Table.scrollTo(smartScrollIndex(state.getScrollTo()));
	}
}
