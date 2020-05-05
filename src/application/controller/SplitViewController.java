package application.controller;

import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
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
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.ws.Holder;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;

import com.sun.javafx.scene.control.skin.TableColumnHeader;

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
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Pair;
import mslinks.ShellLink;

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
@SuppressWarnings("restriction")
public class SplitViewController implements Initializable {

	static final KeyCombination SHORTCUT_REVEAL_IN_EXPLORER = new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN,
			KeyCombination.SHIFT_DOWN);

	static final KeyCombination SHORTCUT_COPY = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);
	static final KeyCombination SHORTCUT_MOVE = new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN);
	public static final KeyCombination SHORTCUT_RENAME = new KeyCodeCombination(KeyCode.F2);
	public static final KeyCombination SHORTCUT_DELETE = new KeyCodeCombination(KeyCode.DELETE);

	public static final KeyCombination SHORTCUT_NEW_FILE = new KeyCodeCombination(KeyCode.N,
			KeyCombination.SHORTCUT_DOWN);
	public static final KeyCombination SHORTCUT_NEW_DIRECTORY = new KeyCodeCombination(KeyCode.N,
			KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);

	static final KeyCombination SHORTCUT_FOCUS_TEXT_FIELD = new KeyCodeCombination(KeyCode.D,
			KeyCombination.SHIFT_DOWN);

	static final KeyCombination SHORTCUT_GO_BACK = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.ALT_DOWN);
	static final KeyCombination SHORTCUT_GO_NEXT = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.ALT_DOWN);
	static final KeyCombination SHORTCUT_GO_UP = new KeyCodeCombination(KeyCode.UP, KeyCombination.ALT_DOWN);

	static final KeyCombination SHORTCUT_RECURSIVE = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN);
	static final KeyCombination SHORTCUT_CLEAR_SEARCH = new KeyCodeCombination(KeyCode.ESCAPE,
			KeyCombination.CONTROL_ANY);
	static final KeyCombination SHORTCUT_SEARCH = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN);

	static final KeyCombination TOGGLE_FAVORITE = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN,
			KeyCombination.SHIFT_DOWN);
	static final KeyCombination SHORTCUT_OPEN_FAVORITE = new KeyCodeCombination(KeyCode.F, KeyCombination.SHIFT_DOWN);

	static final String ON_DROP_CREATE_SHORTCUT_Key = "ON_DROP_CREATE_SHORTCUT";
	@FXML
	private GridPane viewPane;

	@FXML
	@Nullable
	private Button goDesktopButton;

	@FXML
	@Nullable
	private MenuButton rootsMenu;
	@FXML
	@Nullable
	private MenuButton favoritesLocations;
	@FXML
	@Nullable
	private CheckBox favoriteCheckBox;

	@FXML
	private Button explorerButton;

	@FXML
	private Button leftDominate;

	@FXML
	@Nullable
	private Button swapButton;

	@FXML
	@Nullable
	private Button rightDominate;

	@FXML
	private Button navigateRecursive;
	@FXML
	private TextField searchField;
	@FXML
	private Button refreshButton;
	@FXML
	private CheckBox recursiveSearch;

	@FXML
	private ToggleButton autoExpand;
	@FXML
	private Button exitSplitButton;

	@FXML
	private Button backButton;
	@FXML
	private Button nextButton;
	@FXML
	private Button upButton;
	@FXML
	private TextField pathField;
	@FXML
	private MenuButton toolsMenu;

	@FXML
	private TableView<TableViewModel> table;
	@FXML
	private TableColumn<TableViewModel, ImageView> iconCol;
	@FXML
	private TableColumn<TableViewModel, String> nameCol;
	@FXML
	private TableColumn<TableViewModel, String> noteCol;
	@FXML
	private TableColumn<TableViewModel, Double> sizeCol;
	@FXML
	private TableColumn<TableViewModel, HBox> hBoxActionsCol;

	@FXML
	private Label labelItemsNumber;

	@FXML
	private TextField predictNavigation;

	private WelcomeController parentWelcome;
	private FileTracker mFileTracker;
	private WatchServiceHelper mWatchServiceHelper = null;

	private boolean outOfTheBoxRecursive = false;
	private boolean isOutOfTheBoxHelper = false;

	private File mDirectory;

	/**
	 * On general Flow leftView do expand selected directory on rightView Check null
	 * status before calling any of these
	 */
	@Nullable
	private SplitViewController leftViewNeighbor = null;
	@Nullable
	private SplitViewController rightViewNeighbor = null;

	/**
	 * specify working current view position
	 */
	private boolean isLeft;

	// this always respect the path field pattern
	// if anything goes wrong return to it
	private String truePathField;

	private ObservableList<TableViewModel> DataTable;
	SortedList<TableViewModel> sortedData;

	public SplitViewController() {
	}

	// TableColumn<TableViewModel, ImageView> colIconTestResize;
	public SplitViewController(Path path, Boolean isLeft, WelcomeController parent) {
		DataTable = FXCollections.observableArrayList();
		this.isLeft = isLeft;
		mDirectory = new File(path.toString());
		truePathField = mDirectory.getAbsolutePath();
		mFileTracker = new FileTracker(this);
		parentWelcome = parent;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		predictNavigation.setVisible(false);

		nameCol.setCellValueFactory(new PropertyValueFactory<TableViewModel, String>("Name"));
		noteCol.setCellValueFactory(new PropertyValueFactory<TableViewModel, String>("NoteText"));
		hBoxActionsCol.setCellValueFactory(new PropertyValueFactory<TableViewModel, HBox>("hboxActions"));
		iconCol.setCellValueFactory(new PropertyValueFactory<TableViewModel, ImageView>("imgIcon"));
		sizeCol.setCellValueFactory(new PropertyValueFactory<TableViewModel, Double>("FileSize"));

		if (isLeft) {
			noteCol.setVisible(Setting.getShowLeftNotesColumn());
		} else {
			noteCol.setVisible(Setting.getShowRightNotesColumn());
		}
		table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		initializeTable();

		initializePathField();
		initializeSplitButton();

		initializeFavorites();

		mWatchServiceHelper = new WatchServiceHelper(this);

	}

	private void initializeFavorites() {
		if (favoritesLocations == null) {
			return;
		}
		reloadFavorites();
		favoriteCheckBox.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (favoriteCheckBox.isSelected()) {
					// ask for title here
					String hint = getDirectoryPath().toFile().getName().toString();
					if (Setting.getFavoritesLocations().getLastRemoved() != null
							&& Setting.getFavoritesLocations().getLastRemoved().getValue().equals(getDirectoryPath())) {
						hint = Setting.getFavoritesLocations().getLastRemoved().getKey();
					}
					String title = DialogHelper.showTextInputDialog("Favorite Title",
							"Please Enter the name of this Favorite View", "", hint);
					if (title == null || title.trim().equals("")) {
						favoriteCheckBox.setSelected(false);
						return;
					}
					title = title.replaceAll(";", "_");
					Path rightPath = getDirectoryPath();
					if (rightViewNeighbor != null) {
						rightPath = rightViewNeighbor.getDirectoryPath();
					}
					AddandPriorizethisMenu(title, getDirectoryPath(), rightPath);
				} else {
					removeFavorite(getDirectoryPath());
				}
			}
		});
	}

	private Map<String, MenuItem> allMenuFavoriteLocation = new HashMap<String, MenuItem>();

	public void reloadFavorites() {
		if (favoritesLocations == null) {
			return;
		}
		favoritesLocations.getItems().clear();
		allMenuFavoriteLocation.clear();
		for (int i = Setting.getFavoritesLocations().size() - 1; i >= 0; i--) {
			AddandPriorizethisMenu(Setting.getFavoritesLocations().getTitle().get(i),
					Setting.getFavoritesLocations().getLeftLoc().get(i).toPath(),
					Setting.getFavoritesLocations().getRightLoc().get(i).toPath());
		}
	}

	public void clearFavorites() {
		favoritesLocations.getItems().clear();
		allMenuFavoriteLocation.clear();
	}

	private void AddandPriorizethisMenu(String title, Path leftPath, Path rightPath) {
		if (allMenuFavoriteLocation.containsKey(title)) {
			removeFavorite(title);
		}
		MenuItem mx = new MenuItem(title);
		mx.setOnAction(e -> parentWelcome.openFavoriteLocation(title, leftPath, rightPath, this));
		allMenuFavoriteLocation.put(title, mx);
		if (!Setting.getFavoritesLocations().contains(title)) {
			Setting.getFavoritesLocations().add(0, title, leftPath.toFile(), rightPath.toFile());
		}
		favoritesLocations.getItems().add(0, mx);
	}

	private void removeFavorite(Path FavoLeftPath) {
		removeFavorite(Setting.getFavoritesLocations().getTitleByLeft(FavoLeftPath.toFile()));
	}

	private void removeFavorite(String FavoTitle) {
		if (Setting.getFavoritesLocations().contains(FavoTitle)) {
			Setting.getFavoritesLocations().remove(FavoTitle);
			favoritesLocations.getItems().remove(allMenuFavoriteLocation.get(FavoTitle));
			allMenuFavoriteLocation.remove(FavoTitle);
		}
	}

	private void initializePathField() {
		pathField.getStyleClass().removeAll("*.text-field>*.right-button>*.right-button-graphic");
		pathField.setStyle("-fx-font-size: 14px;");

		pathField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
			if (isNowFocused) {
				Platform.runLater(() -> pathField.selectAll());
			}
		});
		pathField.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				setPathFieldThenRefresh(pathField.getText());
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
							searchField.setText(doSearchDelayed);
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
		this.pathField.setText(truePathField);
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
		String text = pathField.getText();
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
		String temp = searchField.getText();
		searchField.setText("");
		searchField.setText(temp);
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

		initializeRootsMenu();

		navigateRecursive.setVisible(false);

		navigateRecursive.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				TableViewModel toNavigateFor = table.getSelectionModel().getSelectedItem();
				Path path = toNavigateFor.getmFilePath().getParent();
				navigate(path);
				resetForm();
				NavigateForNameAndScrollTo(toNavigateFor);
			}
		});
		initializeRecursiveSearch();

		upButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				goUpParent();
				table.requestFocus();
			}
		});

		backButton.setDisable(true);
		backButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				NextQueue.add(mDirectory);
				nextButton.setDisable(false);
				File temp = BackQueue.removeLast();
				if (temp.exists()) {
					mDirectory = temp;
				}
				if (BackQueue.isEmpty()) {
					backButton.setDisable(true);
				}
				refresh(null);
			}
		});

		nextButton.setDisable(true);
		nextButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				BackQueue.add(mDirectory);
				backButton.setDisable(false);
				File temp = NextQueue.removeLast();
				if (temp.exists()) {
					mDirectory = temp;
				}
				if (NextQueue.isEmpty()) {
					nextButton.setDisable(true);
				}
				refresh(null);
			}
		});

		explorerButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if (!isOutOfTheBoxHelper || isOutOfTheBoxRecursive()) {
					int index;
					index = table.getSelectionModel().getFocusedIndex();
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
		searchField.textProperty().addListener((observable, oldValue, newValue) -> {
			predictNavigation.setText("");
			filteredData.setPredicate(model -> {
				// be aware of doing something here it apply on every item in list
				return filterModel(newValue, model);
			});
			addQueryOptionsPathField("search", newValue);
			labelItemsNumber.setText(" #" + filteredData.size() + " items");
		});

		sortedData = new SortedList<>(filteredData);
		sortedData.comparatorProperty().bind(table.comparatorProperty());
		table.setItems(sortedData);

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

		table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (table.getSelectionModel().getSelectedItems().size() > 1) {
				reloadColorLastRowSelected();
			} else {
				deColorLastRowSelected();
			}
			double totalSelectedSize = 0;
			double totalAllSize = 0;
			for (TableViewModel t : table.getSelectionModel().getSelectedItems()) {
				totalSelectedSize += t.getFileSize();
			}
			for (TableViewModel t : sortedData) {
				totalAllSize += t.getFileSize();
			}

			labelItemsNumber
					.setText(" #" + table.getSelectionModel().getSelectedItems().size() + "/" + sortedData.size()
							+ (totalSelectedSize > 0.01
									? " (" + StringHelper.getFormattedSizeFromMB(totalSelectedSize) + " / "
											+ StringHelper.getFormattedSizeFromMB(totalAllSize) + ")"
									: ""));
		});
		predictNavigation.textProperty().addListener((observable, oldValue, newValue) -> {
			deColorLastRowSelected();
			table.getSelectionModel().clearSelection();
			if (newValue.trim().isEmpty()) {
				newValue = "";
				rollerPrediction = 0;
				predictNavigation.setVisible(false);
				return;
			}
			if (!newValue.equals(newValue.toLowerCase())) {
				predictNavigation.setText(newValue.toLowerCase());
				return;
			}
			predictNavigation.setVisible(true);
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
							predictNavigation.setText(toSearchFor);
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
						table.getSelectionModel().clearSelection(sortedData.indexOf(table.getSelectionModel()
								.getSelectedItems().get(table.getSelectionModel().getSelectedItems().size() - 1)));
						table.getSelectionModel().select(table.getSelectionModel().getSelectedItems()
								.get(table.getSelectionModel().getSelectedItems().size() - 2));
						rollerPrediction++;
						predictNavigation.setText(oldValue);
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
				table.scrollTo(smartScrollIndex(lastIndexToBeSelected));
				table.getSelectionModel().selectIndices(-1, toSelect);
				colorLastRowSelected();
			} else {
				predictNavigation.setText(oldValue);
			}

		});
		// scroll on button search to automatically clear
		// the search field

		// https://stackoverflow.com/questions/29735651/mouse-scrolling-in-java-fx
		refreshButton.setOnScroll((ScrollEvent event) -> {
			// Adjust the zoom factor as per your requirement
			double deltaY = event.getDeltaY();
			if (deltaY < 0) {
				searchField.setText("un");
			} else {
				searchField.setText("yes");
			}
		});
		refreshButton.setOnMouseClicked(m -> {
			if (!m.getButton().equals(MouseButton.PRIMARY)) {
				searchField.setText(searchField.getText() + rollerSearchKey.get(rollerSearchIndex));
				rollerSearchIndex = (rollerSearchIndex + 1) % rollerSearchKey.size();
				m.consume();
			}
		});

		refreshButton.setOnAction(e -> {
			resetForm();
		});
	}

	private void initializeRootsMenu() {
		if (rootsMenu != null) {
			EventHandler<Event> eventHandler = e -> {
				rootsMenu.getItems().clear();
				File[] roots = File.listRoots();
				// check https://www.geeksforgeeks.org/javafx-menubutton/
				for (File temp : roots) {
					MenuItem mx = new MenuItem(temp.toString());
					mx.setOnAction(new EventHandler<ActionEvent>() {

						@Override
						public void handle(ActionEvent event) {
							setmDirectoryThenRefresh(temp);
						}
					});
					rootsMenu.getItems().add(mx);
				}
				rootsMenu.show();
			};
			rootsMenu.setOnMouseReleased(eventHandler);
			rootsMenu.setOnTouchReleased(eventHandler);
			// load roots Menu for one time
			eventHandler.handle(null);
		}
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
		lastRowSelected = rowMap.get(table.getSelectionModel().getSelectedItem());
		if (lastRowSelected != null) {
			lastRowSelected.getStyleClass().add("lastRowSelected");
		}
	}

	/**
	 * {@link SplitViewController#upButton this is link example}
	 *
	 */
	// SuppressWarnings Discouraged access: The type 'TableColumnHeader' is not API
	@SuppressWarnings("restriction")
	public void initializeTable() {
		table.addEventFilter(Event.ANY, event -> {
			if (event.getTarget() instanceof TableColumnHeader) {
				event.consume();
			}
		});
		// initialize column rule comparator
		hBoxActionsCol.setComparator(new Comparator<HBox>() {

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
		noteCol.setComparator(new WindowsExplorerComparator());
		nameCol.setComparator(new WindowsExplorerComparator());

		table.setOnKeyPressed(key -> {
			String test = predictNavigation.getText().trim();
			TableViewModel lastSelectedTemp = table.getSelectionModel().getSelectedItem();
			switch (key.getCode()) {
			case ENTER:
				if (table.isFocused()) {
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
						predictNavigation.setText(test);
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
					synctoLeft(tempPath.toString());
					if (!getSelectedItem().getmFilePath().toFile().isDirectory() && leftViewNeighbor != null) {
						leftViewNeighbor.NavigateForNameAndScrollTo(lastSelectedTemp);
					}
				}
				break;
			case RIGHT:
				if (isLeft) {
					Path tempPath2 = getSelectedPathIfDirectory();
					if (tempPath2 == null) {
						break;
					}
					synctoRight(tempPath2.toString());
					if (!getSelectedItem().getmFilePath().toFile().isDirectory() && rightViewNeighbor != null) {
						rightViewNeighbor.NavigateForNameAndScrollTo(lastSelectedTemp);
					}
				}
				break;
			case ESCAPE:
				predictNavigation.setText("");
				break;
			case CONTEXT_MENU:
				showContextMenu();
				break;
			// TODO check declaration there is a lot of key to define
			default:
				// ignore special character
				String newText = predictNavigation.getText();
				// detect special character event
				if (key.isControlDown() || key.isAltDown()) {
					if (key.getText().toLowerCase().equals("a")) {
						int i = table.getSelectionModel().getSelectedIndex();
						table.getSelectionModel().clearSelection();
						table.getSelectionModel().selectAll();
						table.getSelectionModel().clearSelection(i);
						table.getSelectionModel().select(i);
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
				if (newText.equals(predictNavigation.getText())) {
					newText += key.getText();
				}
				predictNavigation.setText(newText.toLowerCase());

				doBack = false;
				break;
			}
		});

		// handle drag and drop events
		table.setOnDragOver(new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {
				if (event.getDragboard().hasFiles() || event.getDragboard().hasContent(DataFormat.URL)
						|| event.getDragboard().hasString()) {
					event.acceptTransferModes(TransferMode.ANY);
				}
			}
		});
		// https://stackoverflow.com/questions/32534113/javafx-drag-and-drop-a-file-into-a-program
		table.setOnDragDropped(new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {
				Dragboard db = event.getDragboard();
				// System.out.println(db.getContentTypes());
				if (db.hasContent(DataFormat.PLAIN_TEXT) && db.getString().equals(ON_DROP_CREATE_SHORTCUT_Key)) {
					ContextMenu mn = new ContextMenu();
					MenuItem createShortcutHere = new MenuItem("Create Shortcut here");
					if (!db.hasFiles()) {
						return;
					}
					Holder<File> originalFile = new Holder<File>(db.getFiles().get(0));
					createShortcutHere.setOnAction(e -> {
						File shotcutFile = getmDirectory().toPath()
								.resolve(originalFile.value.getName() + " - Shortcut.lnk").toFile();
						try {
							ShellLink.createLink(originalFile.value.toString()).saveTo(shotcutFile.toString());
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					});
					mn.getItems().add(createShortcutHere);
					mn.show(parentWelcome.getStage(), event.getScreenX(), event.getScreenY());
				} else if (db.hasFiles()) {
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
		table.setOnDragDetected(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				Dragboard db = table.startDragAndDrop(TransferMode.ANY);
				ClipboardContent cb = new ClipboardContent();
				List<File> selectedFiles = new ArrayList<>();
				if (!event.isSecondaryButtonDown()) {
					getSelection().forEach(x -> selectedFiles.add(x.toFile()));
				} else {
					cb.putString(ON_DROP_CREATE_SHORTCUT_Key);
					selectedFiles.add(getSelectedItem().getmFilePath().toFile());
				}
				cb.putFiles(selectedFiles);
				db.setContent(cb);
			}
		});

		viewPane.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
			if (SHORTCUT_REVEAL_IN_EXPLORER.match(e)) {
				RevealINExplorer();
			} else if (SHORTCUT_COPY.match(e)) {
				copy();
			} else if (SHORTCUT_MOVE.match(e)) {
				move();
			} else if (SHORTCUT_RENAME.match(e)) {
				rename();
			} else if (SHORTCUT_DELETE.match(e)) {
				delete();
			} else if (SHORTCUT_NEW_FILE.match(e)) {
				createFile();
			} else if (SHORTCUT_NEW_DIRECTORY.match(e)) {
				createDirectory();
			} else if (SHORTCUT_FOCUS_TEXT_FIELD.match(e)) {
				pathField.requestFocus();
			} else if (SHORTCUT_GO_BACK.match(e)) {
				backButton.fire();
			} else if (SHORTCUT_GO_NEXT.match(e)) {
				nextButton.fire();
			} else if (SHORTCUT_GO_UP.match(e)) {
				goUpParent();
			} else if (SHORTCUT_RECURSIVE.match(e)) {
				recursiveSearch.fire();
			} else if (SHORTCUT_SEARCH.match(e)) {
				focusSearchField();
			} else if (SHORTCUT_CLEAR_SEARCH.match(e)) {
				clearSearchField();
			} else if (TOGGLE_FAVORITE.match(e)) {
				toogleFavorite();
			} else if (SHORTCUT_OPEN_FAVORITE.match(e)) {
				toogleFavorite();
			}
		});

		table.setOnMouseClicked(m -> {
			TableViewModel t = table.getSelectionModel().getSelectedItem();
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
					synctoLeftParent();
				}
			} else if (m.getButton().equals(MouseButton.PRIMARY) && autoExpand.isSelected()) {
				if (rightViewNeighbor != null) {
					// double check if it was a directory
					File tempDir = WindowsShortcut.getRealFileIfDirectory(t.getmFilePath().toFile());
					if (tempDir.isDirectory()) {
						synctoRight(t.getmFilePath().toString());
					} else if (isOutOfTheBoxHelper()) {
						synctoRight(getSelectedPathIfDirectory().toString());
						rightViewNeighbor.NavigateForNameAndScrollTo(t);
					}
				}
			}
		});

		initializeTableRowFactory();
	}

	private void toogleFavorite() {
		favoriteCheckBox.fire();
	}

	private void synctoLeftParent() {
		File parent = getmDirectory().getParentFile();
		if (leftViewNeighbor != null && parent.exists()) {
			leftViewNeighbor.setPathFieldThenRefresh(parent.toString());
		}
	}

	public void synctoRight(String pathField) {
		if (rightViewNeighbor != null) {
			rightViewNeighbor.setPathFieldThenRefresh(pathField);
		}
	}

	private void synctoLeft(String pathField) {
		if (leftViewNeighbor != null) {
			leftViewNeighbor.setPathFieldThenRefresh(pathField);
		}
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
		table.setRowFactory(tv -> new TableRow<TableViewModel>() {
			@Override
			public void updateItem(TableViewModel t, boolean empty) {
				super.updateItem(t, empty);
				if (t == null) {
					setTooltip(null);
				} else {
					t.initializerRowFactory();
					rowMap.put(t, this);
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
							// this happen on move/copy operation when the file is tracked and try to load
							// larger selection of files while file tracker only containing an outdated
							// version of mapDetails and resolve conflict is necessary but this done on
							// later refresh and resolving conflict in map details
							System.out.println("I'm Left View " + isLeft);
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
								if (!outOfTheBoxRecursive) {
									mFileTracker.setTooltipsTexts(table.getSelectionModel().getSelectedItems(), note);
									mFileTracker.setTooltipText(t.getName(), note);
								} else {
									mFileTracker.OutofTheBoxsetTooltipsTexts(
											table.getSelectionModel().getSelectedItems(), t, note);
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
						if (!outOfTheBoxRecursive) {
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
					// is XSPF start the file directly with custom argument
					if (VLC.isPlaylist(t.getName())) {
						t.getOpenVLC().setOnMouseClicked(m -> {
							VLC.startXSPFInOrder(t.getmFilePath());
						});
					}
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
			try {
				mWatchServiceHelper.changeObservableDirectory(mDirectory.toPath());
			} catch (IOException e) {
				e.printStackTrace();
				showToastMessage(e.getClass() + "\n" + e.getMessage());
				back();
				return;
			}
			outOfTheBoxRecursive = false;
			recursiveSearch.setSelected(false);
			navigateRecursive.setVisible(false);
			addQueryOptionsPathField("recursive", null);
			truePathField = mDirectory.getAbsolutePath() + getQueryOptions();
			// PathField.setText(mDirectory.getAbsolutePath());
			// truePathField = mDirectory.getAbsolutePath();
			refreshIsOutOfTheBox();
			mFileTracker.loadMap(getDirectoryPath(), true, false);
			mFileTracker.resolveConflict();
			showList(getCurrentFilesList());
			reloadSearchField();
			String stageTitle = mDirectory.getName();
			if (stageTitle.isEmpty()) {
				stageTitle = mDirectory.getAbsolutePath();
			}
			parentWelcome.UpdateTitle(stageTitle);
		}
		if (isLeft) {
			updateFavoriteCheckBox(isOutOfTheBoxHelper);
		}
		pathField.setText(truePathField);

		labelItemsNumber.setText(" #" + DataTable.size() + " items");

	}

	private void updateFavoriteCheckBox(boolean isOutOfTheBoxHelper) {
		if (isOutOfTheBoxHelper) {
			favoriteCheckBox.setVisible(false);
		} else {
			favoriteCheckBox.setVisible(true);
			favoriteCheckBox.setSelected(Setting.getFavoritesLocations().contains(getDirectoryPath().toFile()));
		}
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
			backButton.setDisable(true);
		}
	}

	private void AddToQueue(File file) {
		// prevent redundant successive items
		if (BackQueue.isEmpty() || BackQueue.peekLast().compareTo(mDirectory) != 0) {
			BackQueue.add(file);
			backButton.setDisable(false);
		}
	}

	/**
	 * As it Say back to last navigated directory if there isn't, go parent
	 * directory
	 */
	private void back() {
		// when back button is disabled mean that recent directory queue is empty
		if (!doUp && !backButton.isDisabled()) {
			backButton.fire();
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
		predictNavigation.setText("");
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

		TableViewModel selected = table.getSelectionModel().getSelectedItem();

		// addQueryOptionsPathField("search", null); already done in listener
		searchField.setText("");
		table.getSelectionModel().clearSelection(); // to prevent mis scroll
		table.getSelectionModel().select(selected);

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
				outOfTheBoxRecursive = true;
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
					List<Path> selectionsPath = getSelection();
					if (selectionsPath.size() > 1) {
						selectionsPath.stream().filter(p -> p.toFile().isDirectory()).forEach(p -> {
							try {
								Files.walkFileTree(p, r);
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						});
					} else {
						Files.walkFileTree(dir, r);
					}
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
		nextButton.setDisable(true);
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
		searchField.requestFocus();
	}

	// this to update pathField also
	// private void SearchField.setText(String text) {
	// SearchField.setText(text);
	// KeyEvent ke = new KeyEvent(KeyEvent.KEY_RELEASED, "a", "", KeyCode.UNDEFINED,
	// false, false, false, false);
	// SearchField.fireEvent(ke);
	// }

	public void focusTable() {
		if (table.getSelectionModel().getSelectedCells().size() <= 0) {
			// Table.getSelectionModel().select(0);
			table.getSelectionModel().selectFirst();
		}
		table.requestFocus();
	}

	public Button getBackButton() {
		return backButton;
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
		return nextButton;
	}

	public WelcomeController getParentWelcome() {
		return parentWelcome;
	}

	public TextField getPathField() {
		return pathField;
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
		return table.getSelectionModel().getSelectedItem();
	}

	private Path getSelectedPathIfDirectory() {
		TableViewModel t = table.getSelectionModel().getSelectedItem();
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
	/**
	 *
	 * @return all files paths of selected items
	 */
	public List<Path> getSelection() {
		List<Path> selection = new ArrayList<>();
		for (TableViewModel item : table.getSelectionModel().getSelectedItems()) {
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
					navigateRecursive.setVisible(true);
					doRecursiveSearch();
				} else {
					addQueryOptionsPathField("recursive", null);
					navigateRecursive.setVisible(false);
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
		toolsMenu.getItems().addAll(copyBaseNames, pasteBaseNames, undoLastPasteNames, renameAction, newFileAction,
				newFolderAction, newTrackerPlayerPlaylist, newTrackerPlayerAny);

		newTrackerPlayerAny.setOnAction(e -> {
			TableViewModel t = table.getSelectionModel().getSelectedItem();
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
			for (TableViewModel t : table.getSelectionModel().getSelectedItems()) {
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
			rename();
		});
		newFileAction.setOnAction(e -> {
			requestFocus();
			createFile();
		});
		newFolderAction.setOnAction(e -> {
			requestFocus();
			createDirectory();
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
				if (table.getSelectionModel().getSelectedItems().size() > 0) {
					toWorkWith = table.getSelectionModel().getSelectedItems();
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
					if (table.getSelectionModel().getSelectedItems().size() > 0) {
						toWorkWith = table.getSelectionModel().getSelectedItems();
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
		return table.isFocused() || upButton.isFocused() || refreshButton.isFocused() || searchField.isFocused()
				|| viewPane.isFocused();
	}

	public boolean isFocusedTable() {
		return table.isFocused();
	}

	public boolean isFocusedSearchField() {
		return searchField.isFocused() || refreshButton.isFocused();
	}

	public void setAutoExpand(boolean isAutoExpand) {
		autoExpand.setSelected(isAutoExpand);
	}

	public boolean isAutoExpand() {
		return autoExpand.isSelected();
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
		return outOfTheBoxRecursive;
	}

	// return the correct key to be used in map details
	public String keyMapperToString(TableViewModel t) {
		if (outOfTheBoxRecursive) {
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

	/**
	 *
	 * @param filePath
	 * @return true if the navigate was a directory
	 */
	public boolean navigate(Path filePath) {
		File selectedFile = filePath.toFile();
		boolean isDirectory = selectedFile.isDirectory();
		// double check in case of fixed shortcut
		if (!isDirectory) {
			selectedFile = WindowsShortcut.getRealFileIfDirectory(selectedFile);
		}

		if (selectedFile.isDirectory()) {
			setmDirectoryThenRefresh(selectedFile);
		} else {
			try {
				if (VLC.isWellSetup() && VLC.isVLCMediaExt(filePath.toFile().getName())
						&& table.getSelectionModel().getSelectedItems().size() != 1) {
					String files = " --playlist-enqueue --loop";
					for (TableViewModel t : table.getSelectionModel().getSelectedItems()) {
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
						StringHelper.openFiles(table.getSelectionModel().getSelectedItems().stream()
								.map(p -> p.getmFilePath().toFile())
								.filter(p -> StringHelper.getExtention(p.getName()).equals("PDF"))
								.collect(Collectors.toList()));

						// open bunch of Image or an image
					} else if (PhotoViewerController.ArrayIMGExt
							.contains(StringHelper.getExtention(selectedFile.getName()))) {
						new PhotoViewerController(table.getSelectionModel().getSelectedItems().stream()
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
		predictNavigation.setText("");
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
		if (index < 0 && index >= sortedData.size()) {
			return;
		}
		TableViewModel found = sortedData.get(index);
		if (found != null) {
			table.getSelectionModel().select(found);
			table.scrollTo(smartScrollIndex(sortedData.indexOf(found)));
		}
	}

	public void ScrollToName(String fileName) {
		TableViewModel found = getViewModelOfName(fileName);
		if (found == null) {
			return;
		}
		table.getSelectionModel().select(found);
		table.scrollTo(smartScrollIndex(sortedData.indexOf(found)));
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
	ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

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
		refreshButton.setDisable(state);
		table.setDisable(state);
		navigateRecursive.setDisable(state);
		pathField.setDisable(state);
		upButton.setDisable(state);
		backButton.setDisable(state);
		nextButton.setDisable(state);
		explorerButton.setDisable(state);
		searchField.setDisable(state);
		// TODO
//		parentWelcome.recursiveHelperSetBlockedAlso(state);
	}

	public void recursiveHelperSetBlockedAlso(boolean state) {
		if (state == true) {
			autoExpand.setSelected(false);
		} else {
			autoExpand.setSelected(Setting.isAutoExpand());
		}
		rightDominate.setDisable(state);
		leftDominate.setDisable(state);
		autoExpand.setDisable(state);
		favoritesLocations.setDisable(state);
		rootsMenu.setDisable(state);
		goDesktopButton.setDisable(state);
	}

	private void RecursiveHelperUpdateTitle(String message) {
		showToastMessage(message);
		parentWelcome.ResetTitle();
		refresh(truePathField);
		recursiveHelperSetBlocked(false);
		table.requestFocus();
		// to refresh selection number and select the first one
		table.getSelectionModel().select(0);
	}

	public ContextMenu showToastMessage(String message) {
		ContextMenu mn = new ContextMenu();
		MenuItem mnChild = new MenuItem(message);
		mn.getItems().add(mnChild);
		mn.getStyleClass().addAll("lastRowSelected");
		mnChild.getStyleClass().addAll("lastRowSelected");
		Node test = table;
		double xLoc = parentWelcome.getStage().getX() + table.getWidth() * 0.1;
		double yLoc = parentWelcome.getStage().getY() + table.getHeight() + 70;
		while (test != null) {
			xLoc += test.getLayoutX();
			yLoc += test.getLayoutY();
			test = test.getParent();
		}
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
		int[] toSelect = new int[table.getSelectionModel().getSelectedItems().size()];
		int j = 0;
		for (int i : table.getSelectionModel().getSelectedIndices()) {
			toSelect[j++] = i;
		}
		DataTable.clear();
		DataTable.addAll(Copy);
		// restore reserve
		table.getSelectionModel().selectIndices(-1, toSelect);
		table.requestFocus();
	}

	/**
	 * Focus on table View
	 */
	public void requestFocus() {
		table.requestFocus();
	}

	private void restoreLastSelectAndScroll() {
		NavigateForNameAndScrollTo(LastSelectedToScroll);
	}

	public void RevealINExplorer() {
		if (table.getSelectionModel().getSelectedItem() != null) {
			// https://stackoverflow.com/questions/7357969/how-to-use-java-code-to-open-windows-file-explorer-and-highlight-the-specified-f
			StringHelper.RunRuntimeProcess(new String[] { "explorer.exe", "/select,",
					table.getSelectionModel().getSelectedItem().getmFilePath().toString() });
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
			explorerButton.fire();
		}
	}

	public void delete() {
		List<Path> source = getSelection();
		int lastKnownIndex = indexOfName(getSelectedItem().getName());
		if (!FileHelper.delete(source, e -> parentWelcome.refreshUnExistingViewsDir())) {
			return;
		}
		refreshAsPathField();
		selectIndex(lastKnownIndex);
	}

	private void copy() {
		if (isLeft) {
			if (rightViewNeighbor == null) {
				return;
			}
			List<Path> source = getSelection();
			Path target = rightViewNeighbor.getDirectoryPath();
			FileHelper.copy(source, target);
		} else {
			if (leftViewNeighbor != null) {
				return;
			}
			List<Path> source = getSelection();
			Path target = leftViewNeighbor.getDirectoryPath();
			FileHelper.copy(source, target);
		}
	}

	public void rename() {
		List<Path> selection = getSelection();
		if (selection.size() == 1) {
			Path src = selection.get(0);
			if (src.getNameCount() == 0) {
				return;
			}
			Path target = FileHelper.rename(src, false);
			if (target == null) {
				return;
			}
			// file tracker operation update
			getMfileTracker().operationUpdate(target, src.toFile().getName(), target.toFile().getName());
			// refresh directory is satisfied by watch service

			// scroll to renamed item in any view
			Thread tempScroll = new Thread() {

				@Override
				public void run() {
					try {
						TimeUnit.MILLISECONDS.sleep(100);
						Platform.runLater(() -> ScrollToName(target.getFileName().toString()));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
			tempScroll.start();
		} else {
			new RenameUtilityController(selection);
		}
	}

	public void move() {
		if (isLeft) {
			if (rightViewNeighbor == null) {
				return;
			}
			List<Path> source = getSelection();
			Path target = rightViewNeighbor.getDirectoryPath();
			FileHelper.move(source, target);
		} else {
			if (leftViewNeighbor != null) {
				return;
			}
			List<Path> source = getSelection();
			Path target = leftViewNeighbor.getDirectoryPath();
			FileHelper.move(source, target);
		}
	}

	public void createDirectory() {
		FileHelper.createDirectory(getDirectoryPath(), this);
	}

	public void createFile() {
		FileHelper.createFile(getDirectoryPath());
	}

	public void wipeFileTracker() {
		if (!getMfileTracker().isTracked()) {
			DialogHelper.showAlert(AlertType.INFORMATION, "Delete Tracker Data", "This is already Untracked folder",
					"Are you kidding me.");
			return;
		}
		boolean ans = DialogHelper.showConfirmationDialog("Delete Tracker Data",
				"Are you Sure You want to wipe tracker data?",
				"Note: this have nothing to do with your files, it just delete .tracker_explorer.txt"
						+ " >>And so set all item to untracked.\nThis cannot be undone!");
		if (ans) {
			getMfileTracker().deleteFile();
			refreshAsPathField();
		}
	}

	private TableViewModel LastSelectedToScroll;

	private void saveLastSelectToScroll() {
		LastSelectedToScroll = table.getSelectionModel().getSelectedItem();
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
		table.getSelectionModel().clearSelection();
		for (int i = 0; i < DataTable.size(); ++i) {
			TableViewModel model = DataTable.get(i);
			String item = model.getName();
			if (item.matches(regex) || StringHelper.containsWord(item, regex)) {
				table.getSelectionModel().select(model);
			}
		}
	}

	// special use like for rename and do not Queue
	public void setmDirectory(File mDirectory) {
		this.mDirectory = mDirectory;
	}

	// this method is useful to handle call of changing directory
	// and enqueue it to back button..
	// and so adding old directory to queue and so on
	public void setmDirectoryThenRefresh(File mDirectory) {
		predictNavigation.setText("");
		if (mDirectory.compareTo(this.mDirectory) != 0) {
			AddToQueue(this.mDirectory);
			EmptyNextQueue();
		}
		this.mDirectory = mDirectory;
		refresh(null);
		table.scrollTo(0);
	}

	public void setMfileTracker(FileTracker mfileTracker) {
		mFileTracker = mfileTracker;
	}

	public void setParentWelcome(WelcomeController parentWelcome) {
		this.parentWelcome = parentWelcome;
	}

	public void setPredictNavigation(String predictNavigation) {
		this.predictNavigation.setText(predictNavigation);
	}

	private void showContextMenu() {
		TableViewModel t = table.getSelectionModel().getSelectedItem();
		if (t != null) {
			ArrayList<Path> toShow = new ArrayList<>();
			for (TableViewModel temp : table.getSelectionModel().getSelectedItems()) {
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
		if (!outOfTheBoxRecursive) {
			mFileTracker.toggleSelectionSeen(table.getSelectionModel().getSelectedItems(),
					xspfRelatedWithSelection(clicked), clicked);
		} else {
			mFileTracker.OutofTheBoxtoggleSelectionSeen(table.getSelectionModel().getSelectedItems(), clicked);
		}
		// when toggle seen if yes or un is in search field do update
		reloadSearchField();
	}

	private boolean untrackedBehavior(TableViewModel t) {
		boolean ans;
		// returned false
		if (outOfTheBoxRecursive || isOutOfTheBoxHelper) {
			ans = DialogHelper.showConfirmationDialog("Track new Folder[Recursive Mode]", "Ready to Be Stunned ?",
					"Tracking a new Folder will create a hidden file .tracker_explorer.txt"
							+ " in the folder to save data tracker !"
							+ "\nIn recursive mode the creation will trigger on all Selected Items.");
			if (!ans) {
				return ans;
			}

			Set<Path> paths = table.getSelectionModel().getSelectedItems().stream()
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
		pathField.setText(truePathField);
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
		tempOver.addAll(table.getSelectionModel().getSelectedItems());
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
				if (table.getSelectionModel().getSelectedItems().size() == 1
						|| !table.getSelectionModel().getSelectedItems().contains(tSearch)) {
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
		state.setSearchKeyword(searchField.getText());

		state.setSelectedIndices(table.getSelectionModel().getSelectedIndices());
		state.setScrollTo(table.getSelectionModel().getSelectedIndex());
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
		searchField.setText(state.getSearchKeyword());

		// restore selections
		table.getSelectionModel().clearSelection();
		table.getSelectionModel().selectIndices(-1, state.getSelectedIndices());
		table.scrollTo(smartScrollIndex(state.getScrollTo()));
	}

	/**
	 * @return the leftViewNeighbor
	 */
	@Nullable
	public SplitViewController getLeftViewNeighbor() {
		return leftViewNeighbor;
	}

	/**
	 * @param leftViewNeighbor the leftViewNeighbor to set
	 */
	public void setLeftViewNeighbor(SplitViewController leftViewNeighbor) {
		this.leftViewNeighbor = leftViewNeighbor;
	}

	/**
	 * @return the rightViewNeighbor
	 */
	@Nullable
	public SplitViewController getRightViewNeighbor() {
		return rightViewNeighbor;
	}

	/**
	 * @param rightViewNeighbor the rightViewNeighbor to set
	 */
	public void setRightViewNeighbor(SplitViewController rightViewNeighbor) {
		this.rightViewNeighbor = rightViewNeighbor;
	}

	@FXML
	private void goDesktop(ActionEvent event) {
		setmDirectoryThenRefresh(new File(System.getProperty("user.home") + File.separator + "Desktop"));
		requestFocus();
	}

	@FXML
	private void dominateLeft() {
		if (leftViewNeighbor != null) {
			leftViewNeighbor.setPathFieldThenRefresh(getPathField().getText());
		}
	}

	@FXML
	private void swapWithleft() {
		if (leftViewNeighbor != null) {
			String temp = leftViewNeighbor.getPathField().getText();
			dominateLeft();
			setPathFieldThenRefresh(temp);
		}
	}

	@FXML
	private void dominateRight() {
		if (rightViewNeighbor != null) {
			rightViewNeighbor.setPathFieldThenRefresh(getPathField().getText());
		}
	}

	public void RecursiveHelpersetBlocked(boolean state) {
		if (state == true) {
			autoExpand.setSelected(false);
		} else {
			autoExpand.setSelected(Setting.isAutoExpand());
		}
		rightDominate.setDisable(state);
		leftDominate.setDisable(state);
		autoExpand.setDisable(state);
		favoritesLocations.setDisable(state);
		rootsMenu.setDisable(state);
		goDesktopButton.setDisable(state);
	}

	public ToggleButton getAutoExpand() {
		return autoExpand;
	}

	public GridPane getViewPane() {
		return viewPane;
	}

	/**
	 *
	 * @param leftViewController
	 * @return FXMLLoader loader of FXML
	 * @throws IOException
	 */
	public static FXMLLoader loadFXMLViewAsLeft(SplitViewController leftViewController) throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(SplitViewController.class.getResource("/fxml/SplitViewLeft.fxml"));
		loader.setController(leftViewController);
		loader.load();
		return loader;
	}

	/**
	 *
	 * @param rightViewController
	 * @return FXMLLoader loader of FXML
	 * @throws IOException
	 */
	public static FXMLLoader loadFXMLViewAsRight(SplitViewController rightViewController) throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(SplitViewController.class.getResource("/fxml/SplitViewRight.fxml"));
		loader.setController(rightViewController);
		loader.load();
		return loader;
	}

	public boolean isNoteColumnVisible() {
		return noteCol.isVisible();
	}

	public Button getExitSplitButton() {
		return exitSplitButton;
	}
}
