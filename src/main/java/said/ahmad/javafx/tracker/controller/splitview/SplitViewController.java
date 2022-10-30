package said.ahmad.javafx.tracker.controller.splitview;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.ws.Holder;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.Nullable;

import com.sun.javafx.scene.control.skin.TableColumnHeader;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;
import mslinks.ShellLink;
import said.ahmad.javafx.tracker.app.*;
import said.ahmad.javafx.tracker.app.look.ContextMenuLook;
import said.ahmad.javafx.tracker.app.look.IconLoader;
import said.ahmad.javafx.tracker.app.look.IconLoader.ICON_TYPE;
import said.ahmad.javafx.tracker.app.pref.Setting;
import said.ahmad.javafx.tracker.controller.*;
import said.ahmad.javafx.tracker.controller.connection.ConnectionController;
import said.ahmad.javafx.tracker.controller.connection.ConnectionController.ConnectionType;
import said.ahmad.javafx.tracker.controller.connection.ftp.FTPConnectionController;
import said.ahmad.javafx.tracker.datatype.*;
import said.ahmad.javafx.tracker.datatype.DirectoryViewOptions.COLUMN;
import said.ahmad.javafx.tracker.model.TableViewModel;
import said.ahmad.javafx.tracker.system.RecursiveFileWalker;
import said.ahmad.javafx.tracker.system.WatchServiceHelper;
import said.ahmad.javafx.tracker.system.WindowsShortcut;
import said.ahmad.javafx.tracker.system.call.RunMenu;
import said.ahmad.javafx.tracker.system.call.SystemExplorer;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.tracker.system.file.PathLayerHelper;
import said.ahmad.javafx.tracker.system.file.local.FilePathLayer;
import said.ahmad.javafx.tracker.system.file.util.URIHelper;
import said.ahmad.javafx.tracker.system.operation.FileHelper;
import said.ahmad.javafx.tracker.system.operation.FileHelper.ActionOperation;
import said.ahmad.javafx.tracker.system.services.TrackerPlayer;
import said.ahmad.javafx.tracker.system.services.VLC;
import said.ahmad.javafx.tracker.system.services.VLCException;
import said.ahmad.javafx.tracker.system.tracker.FileTracker;
import said.ahmad.javafx.tracker.system.tracker.FileTrackerConflictLog;
import said.ahmad.javafx.tracker.system.tracker.FileTrackerDirectoryOptions;
import said.ahmad.javafx.tracker.system.tracker.FileTrackerHolder;
import said.ahmad.javafx.util.ArrayListHelper;
import said.ahmad.javafx.util.ControlListHelper;

/**
 * For a structure view read {@link #SplitViewController}
 *
 * For a functional flow read {@link #refresh(String)}
 *
 * at last they will combine..
 *
 * @author Ahmad Said
 *
 */
public class SplitViewController implements Initializable {

	static final KeyCombination SHORTCUT_REVEAL_IN_EXPLORER = new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN,
			KeyCombination.SHIFT_DOWN);

	static final KeyCombination SHORTCUT_COPY = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);
	static final String COPY_CLIPBOARD_ACTION = "COPY_CLIPBOARD_ACTION";

	static final KeyCombination SHORTCUT_MOVE = new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN);
	static final String MOVE_CLIPBOARD_ACTION = "MOVE_CLIPBOARD_ACTION";

	static final KeyCombination SHORTCUT_PASTE = new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN);

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

	static final String ON_DROP_CREATE_SHORTCUT_KEY = "ON_DROP_CREATE_SHORTCUT_KEY";
	private static final String PATH_FIELD_LIST_ROOTS = "This PC";
	@FXML
	private GridPane viewPane;

	@FXML
	@Nullable
	private Button desktopButton;

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
	private VBox topTableVbox;
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
	private ProgressIndicator loadingTablePlaceHolder;
	private Label noContentTablePlaceHolder;
	private static Image fileNotFoundImage = IconLoader.getIconImage(ICON_TYPE.RESOURCE_NOT_FOUND, true, 250, 200);
	private ImageView fileNotFoundImageView = new ImageView(fileNotFoundImage);
	private Text fileNotFoundErrorText = new Text();
	private TextFlow fileNotFoundErrorTextFlow = new TextFlow(fileNotFoundErrorText);
	private VBox fileNotFoundPlaceHolder = new VBox(fileNotFoundImageView, fileNotFoundErrorTextFlow);

	@FXML
	private TableColumn<TableViewModel, ImageView> iconCol;
	@FXML
	private TableColumn<TableViewModel, String> nameCol;
	@FXML
	private TableColumn<TableViewModel, String> noteCol;
	@FXML
	private TableColumn<TableViewModel, Double> sizeCol;
	@FXML
	private TableColumn<TableViewModel, String> dateModifiedCol;
	@FXML
	private TableColumn<TableViewModel, HBox> hBoxActionsCol;

	@FXML
	private Label directoryNameLabel;

	@FXML
	private Label labelItemsNumber;

	@FXML
	private TextField predictNavigation;

	private WelcomeController parentWelcome;
	private FileTracker fileTracker;
	private SplitViewTrackerAdapter fileTrackerAdapter;
	private WatchServiceHelper watchServiceHelper = null;
	private boolean outOfTheBoxRecursive = false;
	private boolean isOutOfTheBoxHelper = false;

	private PathLayer mDirectory;

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
	private SortedList<TableViewModel> sortedData;

	public SplitViewController(PathLayer path, Boolean isLeft, WelcomeController parent) {
		DataTable = FXCollections.observableArrayList();
		this.isLeft = isLeft;
		mDirectory = path;
		truePathField = mDirectory.getAbsolutePath();
		fileTracker = new FileTracker(path,
				writtenPath -> parentWelcome.refreshAllSplitViewsIfMatch(writtenPath, SplitViewController.this));
		parentWelcome = parent;
		fileTrackerAdapter = new SplitViewTrackerAdapter(fileTracker, this);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		predictNavigation.setVisible(false);

		nameCol.setCellValueFactory(new PropertyValueFactory<TableViewModel, String>("Name"));
		noteCol.setCellValueFactory(new PropertyValueFactory<TableViewModel, String>("NoteText"));
		hBoxActionsCol.setCellValueFactory(new PropertyValueFactory<TableViewModel, HBox>("hboxActions"));
		iconCol.setCellValueFactory(new PropertyValueFactory<TableViewModel, ImageView>("imgIcon"));
		sizeCol.setCellValueFactory(new PropertyValueFactory<TableViewModel, Double>("FileSize"));
		dateModifiedCol.setCellValueFactory(new PropertyValueFactory<TableViewModel, String>("dateModified"));

		table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		initializeTable();

		initializePathField();
		initializeSplitButton();

		Setting.registerOnFinishLoadingAction(() -> initializeFavorites());

		watchServiceHelper = new WatchServiceHelper(this);

	}

	private void initializeFavorites() {
		if (favoritesLocations == null) {
			return;
		}
		favoriteCheckBox.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (favoriteCheckBox.isSelected()) {
					// ask for title here
					String hint = getmDirectoryPath().getName();
					String title = DialogHelper.showTextInputDialog("Favorite Title",
							"Please Enter the name of this Favorite View", "", hint);
					if (title == null || title.trim().equals("")) {
						favoriteCheckBox.setSelected(false);
						return;
					}
					title = title.replaceAll(";", "_");
					FavoriteView newFavoriteView = new FavoriteView(title);
					SplitViewController splitView = SplitViewController.this;
					do {
						SplitViewState state = new SplitViewState(splitView.mDirectory);
						splitView.saveStateToSplitState(state);
						newFavoriteView.getSplitStates().add(state);
						splitView = splitView.getRightViewNeighbor();
					} while (splitView != null);
					AddandPriorizethisMenu(newFavoriteView);
				} else {
					removeFavorite(getmDirectoryPath());
				}
			}
		});
		manuallyLoadFavoritesMenus();
	}

	private Map<String, MenuItem> allMenuFavoriteLocation = new HashMap<String, MenuItem>();

	public void onFavoriteChanges(Change<? extends String, ? extends FavoriteView> change,
			Holder<Boolean> isReloadingAllOperation) {
		if (favoritesLocations == null) {
			return;
		}
		if (change.wasAdded()) {
			if (isReloadingAllOperation.value) {
				onAddingFavorite(change.getValueAdded(), false);
			} else {
				onAddingFavorite(change.getValueAdded(), true);
			}
		} else if (change.wasRemoved()) {
			onRemovingFavorite(change.getValueRemoved());
		}
	}

	public void manuallyLoadFavoritesMenus() {
		Setting.getFavoritesViews().getList().values().forEach(f -> onAddingFavorite(f, false));
	}

	private void onAddingFavorite(FavoriteView favoriteView, boolean addAtTop) {
		String addedTitle = favoriteView.getTitle();
		if (allMenuFavoriteLocation.containsKey(addedTitle)) {
			favoritesLocations.getItems().remove(allMenuFavoriteLocation.get(addedTitle));
			allMenuFavoriteLocation.remove(addedTitle);
		}
		MenuItem mx = new MenuItem(addedTitle);
		mx.setOnAction(e -> parentWelcome.openFavoriteLocation(favoriteView, this));
		allMenuFavoriteLocation.put(favoriteView.getTitle(), mx);
		if (addAtTop) {
			favoritesLocations.getItems().add(0, mx);
		} else {
			favoritesLocations.getItems().add(mx);
		}
	}

	private void onRemovingFavorite(FavoriteView favoriteView) {
		String removedTitle = favoriteView.getTitle();
		if (allMenuFavoriteLocation.containsKey(removedTitle)) {
			favoritesLocations.getItems().remove(allMenuFavoriteLocation.get(removedTitle));
			allMenuFavoriteLocation.remove(removedTitle);
		}
	}

	private void AddandPriorizethisMenu(FavoriteView favoriteView) {
		Setting.getFavoritesViews().addAtFirst(favoriteView);
	}

	private void removeFavorite(PathLayer FavoLeftPath) {
		removeFavorite(Setting.getFavoritesViews().getByFirstLoc(FavoLeftPath).getTitle());
	}

	private void removeFavorite(String FavoTitle) {
		Setting.getFavoritesViews().removeByTitle(FavoTitle);
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
		if (pathField.isEmpty()) {
			refresh(null);
			return;
		} else if (pathField.toLowerCase().equals(PATH_FIELD_LIST_ROOTS.toLowerCase())) {
			OutOfTheBoxListRoots(null);
			return;
		}
		final String pathFieldFinal = pathField;
		if (pathFieldFinal.equals("cmd")) {
			SystemExplorer.startCMDInDir(mDirectory.toFileIfLocal());
			this.pathField.setText(mDirectory.getAbsolutePath() + getQueryOptions());
			return;
		}
		File file = new File(getQueryPathFromEmbed(pathField));
		PathLayer targetPath = null;
		if (file.exists()) {
			targetPath = new FilePathLayer(file);
		} else {
			try {
				// Try to parse URI as Path Layer
				URI parsedURI = URIHelper.encodeToURI(pathField);
				// Ignore the query part of the input URL
				URI uriNoQuery;
				uriNoQuery = new URI(parsedURI.getScheme(), parsedURI.getAuthority(), parsedURI.getPath(), null,
						parsedURI.getFragment());
				ConnectionAccount account = new ConnectionAccount(uriNoQuery);
				targetPath = PathLayerHelper.parseURI(uriNoQuery);
				if (targetPath == null && account.getScheme().toUpperCase().equals("FTP")) {
					ConnectionController newConnection = new ConnectionController(ConnectionType.FTP,
							p -> setPathFieldThenRefresh(p.toString()));
					FTPConnectionController ftpController = (FTPConnectionController) newConnection
							.getCurrentConnection();
					ftpController.setInputFields(account);
					return;
				}
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}

		}
		// Important see there is navigate is not just a boolean ::
		if (targetPath != null) {
			navigate(targetPath);
			if (targetPath.isFile()) {
				navigate(targetPath.getParentPath());
			}
			// apply query only if file exist and file is a directory after changing view to
			// it
			if (SpecialPath.stream().anyMatch(sp -> pathFieldFinal.contains(sp))) {

				// out of the box
				if (pathField.equals(PATH_FIELD_LIST_ROOTS)) {
					resetForm();
					OutOfTheBoxListRoots(null);
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
		} else if (pathFieldFinal.toLowerCase().startsWith("http://")) {
			try {
				// currently mounting webDav as Local File, Windows System handle it by mounting
				// WebDav connection
				URL testParse = new URL(pathFieldFinal);
				String connection = "\\\\" + testParse.getHost() + "@" + testParse.getPort() + "\\DavWWWRoot";
				setPathFieldThenRefresh(connection);

			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		} else {
			this.pathField.setText(truePathField);
		}
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

	protected void reloadSearchField() {
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
				PathLayer path = toNavigateFor.getFilePath().getParentPath();
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
				PathLayer temp = BackQueue.removeLast();
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
				PathLayer temp = NextQueue.removeLast();
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
				if (!mDirectory.isLocal()) {
					return;
				}
				if (!isOutOfTheBoxHelper || isOutOfTheBoxRecursive()) {
					int index;
					index = table.getSelectionModel().getFocusedIndex();
					if (index != -1) {
						TableViewModel test = sortedData.get(index);

						try {
							SystemExplorer.select(test.getFilePath().toFileIfLocal()).waitFor();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} else {
						StringHelper.openFile(mDirectory.toFileIfLocal());
					}
				} else {
					if (truePathField.equals(PATH_FIELD_LIST_ROOTS)) {
						SystemExplorer.select(DataTable.get(0).getFilePath().toFileIfLocal());
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

		initializeDataTableListenerTracker();

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

	/** @see #initializeTableRowFactory() */
	private void initializeDataTableListenerTracker() {
		DataTable.addListener((ListChangeListener<TableViewModel>) c -> {
			while (c.next()) {
				if (c.wasRemoved()) {
					rowMap.clear();
				}
				if (c.wasAdded()) {
					// String key = keyStringMapper(c);
					for (TableViewModel t : c.getAddedSubList()) {
						FileTrackerHolder option = fileTracker.getTrackerData(t.getFilePath());
						if (option != null) {
							// TODO when adding new files to current directory null pointer exception is
							// detected
							t.setSeenText(option.isSeen());
							t.setNoteText(option.getNoteText());
						} else {
							t.emptyCell();
						}
					}
				}
			}
			// Sort may be done based on new tracker data like note
			// since sort is done only after adding to datatable but before this function
			table.sort();
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
							setmDirectoryThenRefresh(new FilePathLayer(temp));
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
		VBox.setVgrow(fileNotFoundImageView, Priority.ALWAYS);
		fileNotFoundPlaceHolder.setAlignment(Pos.CENTER);
		loadingTablePlaceHolder = new ProgressIndicator();
		table.widthProperty().addListener((obsevable, oldWidth, newWidth) -> {
			loadingTablePlaceHolder.setMaxWidth(newWidth.doubleValue() / 4);
		});
		table.heightProperty().addListener((obsevable, oldHeight, newHeight) -> {
			loadingTablePlaceHolder.setMaxHeight(newHeight.doubleValue() / 4);
		});
		noContentTablePlaceHolder = new Label("No content");
		table.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
			if (event.getTarget() instanceof TableColumnHeader) {
				// Detect change on sort order of columns require saving it to tracker data
				onDirectoryViewOptionsChange();
				// consume event to prevent propagation and open things on double-click
				// the column header
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
		noteCol.setComparator(WindowsExplorerComparator.getComparator());
		nameCol.setComparator(WindowsExplorerComparator.getComparator());
		dateModifiedCol.setComparator(new Comparator<String>() {
			@Override
			public int compare(String dateFormatted1, String dateFormatted2) {
				try {
					long date1 = PathLayer.getDateFormat().parse(dateFormatted1).getTime();
					long date2 = PathLayer.getDateFormat().parse(dateFormatted2).getTime();
					return (date1 - date2) > 0 ? -1 : 1;
				} catch (ParseException e) {
					e.printStackTrace();
				}
				return 0;
			}
		});
		table.setOnContextMenuRequested(e -> showContextMenu());
		startListeningToDirViewOptChanges();
		table.setOnKeyPressed(key -> {
			String test = predictNavigation.getText().trim();
			TableViewModel lastSelectedTemp = table.getSelectionModel().getSelectedItem();
			switch (key.getCode()) {
				case ENTER :
					if (table.isFocused()) {
						if (lastSelectedTemp != null) {
							navigate(lastSelectedTemp.getFilePath());
						}
					}
					break;
				case BACK_SPACE :
					if (doBack) {
						back();
					} else {
						if (!test.isEmpty()) {
							test = test.substring(0, test.length() - 1);
							predictNavigation.setText(test);
						} else {
							ThreadExecutors.recursiveExecutor.execute(EnableMisBack);
						}
					}
					break;
				case SPACE :
					// to do here if i make a selection using prediction do not make so
					// always space do mark seen and auto enter space is enabled
					// if (!test.isEmpty())
					// PredictNavigation.insertText(PredictNavigation.getText().length(), " ");
					// else if (temp != null)
					if (lastSelectedTemp != null) {
						lastSelectedTemp.getMarkSeen().fire();
					}
					break;
				// leaved for navigation
				case UP :
				case DOWN :
					break;
				case LEFT :
					if (leftViewNeighbor != null) {
						PathLayer tempPath = getSelectedPathIfDirectoryOrParent();
						if (tempPath == null) {
							break;
						}
						synctoLeft(tempPath.toString());
						if (!getSelectedItem().getFilePath().isDirectory() && leftViewNeighbor != null) {
							leftViewNeighbor.onFinishLoadingScrollToName = lastSelectedTemp.getName();
						}
					}
					break;
				case RIGHT :
					if (rightViewNeighbor != null) {
						PathLayer tempPath2 = getSelectedPathIfDirectoryOrParent();
						if (tempPath2 == null) {
							break;
						}
						synctoRight(tempPath2.toString());
						if (!getSelectedItem().getFilePath().isDirectory() && rightViewNeighbor != null) {
							rightViewNeighbor.onFinishLoadingScrollToName = lastSelectedTemp.getName();
						}
					}
					break;
				case ESCAPE :
					predictNavigation.setText("");
					break;
				// TODO check declaration there is a lot of key to define
				default :
					// ignore special character
					String newText = predictNavigation.getText();
					// detect special character event
					if (key.isControlDown() || key.isAltDown()) {
						if (key.getText().equalsIgnoreCase("a")) {
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
						if (key.getText().equalsIgnoreCase("n") && lastSelectedTemp != null) {
							lastSelectedTemp.getNoteButton().fire();
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
				if (db.hasContent(DataFormat.PLAIN_TEXT) && db.getString().equals(ON_DROP_CREATE_SHORTCUT_KEY)
						&& getmDirectoryPath().isLocal()) {
					/** Working in local Mode */
					ContextMenu mn = new ContextMenu();
					MenuItem createShortcutHere = new MenuItem("Create Shortcut here");
					if (!db.hasFiles()) {
						return;
					}
					Holder<File> originalFile = new Holder<File>(db.getFiles().get(0));
					createShortcutHere.setOnAction(e -> {
						File shortcutFile = getmDirectoryPath()
								.resolve(originalFile.value.getName() + " - Shortcut.lnk").toFileIfLocal();
						try {
							ShellLink.createLink(originalFile.value.toString()).saveTo(shortcutFile.toString());
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					});
					mn.getItems().add(createShortcutHere);
					mn.show(parentWelcome.getStage(), event.getScreenX(), event.getScreenY());
				} else if (db.hasFiles()) {
					/** Working in local Mode */
					FilePathLayer targetDirAsFile = getmDirectoryPath().isLocal()
							? (FilePathLayer) getmDirectoryPath()
							: null;
					List<FilePathLayer> ToOperatePath = new ArrayList<>();
					List<FilePathLayer> ToOperatePathSameDir = new ArrayList<>();
					db.getFiles().forEach(file3 -> {
						if (!file3.getParent().equals(mDirectory.getAbsolutePath())) {
							ToOperatePath.add(new FilePathLayer(file3));
						} else {
							ToOperatePathSameDir.add(new FilePathLayer(file3));
						}
					});
					ContextMenu mn = new ContextMenu();
					if (ToOperatePath.size() != 0) {
						MenuItem mnCopy = new MenuItem("Copy Here");
						mnCopy.setGraphic(new ImageView(ContextMenuLook.copyIcon));
						MenuItem mnMove = new MenuItem("Move Here");
						mnMove.setGraphic(new ImageView(ContextMenuLook.moveIcon));
						MenuItem mnCopyAs = null;
						MenuItem mnMoveAs = null;
						MenuItem mnCancel = new MenuItem("Cancel");
						mnCancel.setGraphic(new ImageView(ContextMenuLook.cancelIcon));

						mnMove.setOnAction(e -> FileHelper.move(ToOperatePath, getmDirectoryPath()));
						mnCopy.setOnAction(e -> FileHelper.copy(ToOperatePath, getmDirectoryPath()));
						// ask to overwrite if file already exist or to rename to a new file
						if (ToOperatePath.size() == 1) {
							PathLayer sourceFile = ToOperatePath.get(0);
							PathLayer targetFile = getmDirectoryPath().resolve(ToOperatePath.get(0).getName());
							PathLayer renamedTargetFile = FileHelper.getAvailablePath(sourceFile.getName(), getmDirectoryPath());
							if (targetFile.exists()) {
								final String overwriteHeaderConfirmation  = "Overwrite confirmation";
								final String overwriteContentConfirmation = "This will delete original files and replace them.\n" +
										"Do you really want to overwrite this file ?";
								mnCopy.setText("Copy and overwrite here!");
								mnMove.setText("Move and overwrite here!");
								mnCopy.setOnAction(e -> FileHelper.delete(Collections.singletonList(targetFile),
										c -> FileHelper.copy(ToOperatePath, getmDirectoryPath()),
										overwriteHeaderConfirmation, overwriteContentConfirmation));
								mnMove.setOnAction(e -> FileHelper.delete(Collections.singletonList(targetFile),
										c -> FileHelper.move(ToOperatePath, getmDirectoryPath()),
										overwriteHeaderConfirmation, overwriteContentConfirmation));

								mnCopyAs = new MenuItem("Copy here as '" + renamedTargetFile.getName() + "'");
								mnCopyAs.setGraphic(new ImageView(ContextMenuLook.copyIcon));

								mnMoveAs = new MenuItem("Move here as '" + renamedTargetFile.getName() + "'");
								mnMoveAs.setGraphic(new ImageView(ContextMenuLook.moveIcon));

								mnCopyAs.setOnAction(r -> FileHelper.copyFiles(Collections.singletonList(sourceFile), Collections.singletonList(renamedTargetFile)));
								mnMoveAs.setOnAction(r -> FileHelper.moveFiles(Collections.singletonList(sourceFile), Collections.singletonList(renamedTargetFile)));

							}
						}

						if (Setting.isUseTeraCopyByDefault() && targetDirAsFile != null) {
							MenuItem mnTeraCopy = new MenuItem("Copy With TeraCopy");
							MenuItem mnTeraMove = new MenuItem("Move With TeraCopy");
							mnTeraCopy.setOnAction(e -> FileHelper.copyWithTeraCopy(ToOperatePath, targetDirAsFile));
							mnTeraMove.setOnAction(e -> FileHelper.moveWithTeraCopy(ToOperatePath, targetDirAsFile));
							mn.getItems().addAll(mnCopy, mnTeraCopy, mnMove, mnTeraMove);
						} else {
							mn.getItems().addAll(mnCopy, mnMove);
						}

						if(mnCopyAs != null && mnMoveAs != null) {
							mn.getItems().addAll(mnCopyAs, mnMoveAs);
						}
						mn.getItems().add(mnCancel);
					} else if (ToOperatePathSameDir.size() != 0) {

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
							String targetFileName = t.getFilePath().getName();
							PathLayer targetPath = t.getFilePath();
							if (t.getFilePath().isDirectory()) {
								MenuItem mnCopy = new MenuItem("Copy To \n\t\"" + targetFileName + "\"");
								mnCopy.setGraphic(new ImageView(ContextMenuLook.copyIcon));
								MenuItem mnMove = new MenuItem("Move To \n\t\"" + targetFileName + "\"");
								mnMove.setGraphic(new ImageView(ContextMenuLook.moveIcon));
								mnCopy.setOnAction(e -> FileHelper.copy(ToOperatePathSameDir, targetPath));
								mnMove.setOnAction(e -> FileHelper.move(ToOperatePathSameDir, targetPath));
								if (Setting.isUseTeraCopyByDefault() && targetDirAsFile != null) {
									MenuItem mnTeraCopy = new MenuItem(
											"Copy with TeraCopy To \n\t\"" + targetFileName + "\"");
									MenuItem mnTeraMove = new MenuItem(
											"Move with TeraCopy To \n\t\"" + targetFileName + "\"");
									mnTeraCopy.setOnAction(
											e -> FileHelper.copyWithTeraCopy(ToOperatePathSameDir, targetDirAsFile));
									mnTeraMove.setOnAction(
											e -> FileHelper.moveWithTeraCopy(ToOperatePathSameDir, targetDirAsFile));
									mn.getItems().addAll(mnCopy, mnTeraCopy, mnMove, mnTeraMove);
								} else {
									mn.getItems().addAll(mnCopy, mnMove);
								}
							}
						}
						MenuItem mnCopy = new MenuItem("Create Copy Here");
						mnCopy.setGraphic(new ImageView(ContextMenuLook.copyIcon));
						MenuItem mnCancel = new MenuItem("Cancel");
						mnCancel.setGraphic(new ImageView(ContextMenuLook.cancelIcon));
						mnCopy.setOnAction(e -> {
							List<PathLayer> targetFiles = new ArrayList<>();
							for (FilePathLayer file : ToOperatePathSameDir) {
								targetFiles.add(FileHelper.getCopyFileName(file));
							}
							FileHelper.copyFiles(ToOperatePathSameDir, targetFiles);
						});
						if (Setting.isUseTeraCopyByDefault() && targetDirAsFile != null) {
							MenuItem mnTeraCopy = new MenuItem("Create Copy Here With TeraCopy");
							mnTeraCopy.setOnAction(
									e -> FileHelper.copyWithTeraCopy(ToOperatePathSameDir, targetDirAsFile));
							mn.getItems().addAll(mnCopy, mnTeraCopy, mnCancel);
						} else {
							mn.getItems().addAll(mnCopy, mnCancel);
						}
					}
					mn.show(parentWelcome.getStage(), event.getScreenX(), event.getScreenY());

				} else if (db.hasContent(DataFormat.URL)) {
					// try later to parse FTP or URI location

					/** Working in local Mode */
					if (!mDirectory.isLocal()) {
						return;
					}
					// handle url create shortcuts
					String fileName = null;
					for (DataFormat file : db.getContentTypes()) {
						String curName = StringHelper.getValueFromCMDArgs(file.toString(), "name");
						if (curName != null) {
							fileName = curName;
						}
					}
					PathLayer where = mDirectory.resolve(fileName);
					try {
						WindowsShortcut.createInternetShortcut(where.toFileIfLocal(), db.getUrl(), "");
					} catch (IOException e) {
						e.printStackTrace();
						DialogHelper.showException(e);
					}
				} else if (db.hasContent(DataFormat.PLAIN_TEXT)) {
					// trying to parse string as path
					// also do on ctrl+ v action on table if has string url optional
					try {
						PathLayer test = new FilePathLayer(new File(db.getContent(DataFormat.PLAIN_TEXT).toString()));
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
				if (mDirectory.isLocal()) {
					List<File> selectedFiles = new ArrayList<>();
					if (!event.isSecondaryButtonDown()) {
						getSelection().forEach(x -> selectedFiles.add(x.toFileIfLocal()));
					} else {
						cb.putString(ON_DROP_CREATE_SHORTCUT_KEY);
						selectedFiles.add(getSelectedItem().getFilePath().toFileIfLocal());
					}
					cb.putFiles(selectedFiles);
				} else {
					cb.putString(PathLayerHelper.generateOnDropString(getSelection()));
				}
				db.setContent(cb);
			}
		});

		table.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
			if (SHORTCUT_COPY.match(e)) {
				copy();
			} else if (SHORTCUT_MOVE.match(e)) {
				move();
			} else if (SHORTCUT_PASTE.match(e)) {
				paste();
			}
		});
		viewPane.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
			if (SHORTCUT_REVEAL_IN_EXPLORER.match(e)) {
				RevealINExplorer();
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
				toggleFavorite();
			} else if (SHORTCUT_OPEN_FAVORITE.match(e)) {
				toggleFavorite();
			}
		});
		table.setOnMouseClicked(m -> {
			TableViewModel t = table.getSelectionModel().getSelectedItem();
			if (t == null) {
				return;
			}

			boolean tempIsDirectory = t.getFilePath().isDirectory();

			if (m.getButton().equals(MouseButton.PRIMARY) && m.getClickCount() == 2) {
				navigate(t.getFilePath());
				if (!isLeft && Setting.isBackSync() && tempIsDirectory) {
					synctoLeftParent();
				}
			} else if (m.getButton().equals(MouseButton.PRIMARY) && autoExpand.isSelected()) {
				if (rightViewNeighbor != null) {
					// double check if it was a directory
					if (t.getFilePath().isDirectory()) {
						synctoRight(t.getFilePath().getAbsolutePath());
					} else if (mDirectory.isLocal() && t.getFilePath().getExtensionUPPERCASE().equals("LNK")) {
						File tempDir = WindowsShortcut.getRealFileIfDirectory(t.getFilePath().toFileIfLocal());
						if (tempDir.isDirectory()) {
							synctoRight(tempDir.getAbsolutePath());
						}
					} else if (isOutOfTheBoxRecursive()) {
						rightViewNeighbor.onFinishLoadingScrollToName = t.getName();
						synctoRight(getSelectedPathIfDirectoryOrParent().toString());
					}
				}
			}
		});

		initializeTableRowFactory();
	}

	private void toggleFavorite() {
		favoriteCheckBox.fire();
	}

	private void synctoLeftParent() {
		PathLayer parent = getmDirectoryPath().getParentPath();
		if (leftViewNeighbor != null && parent.exists()) {
			leftViewNeighbor.setPathFieldThenRefresh(parent.toString());
		}
	}

	public void synctoRight(String pathField) {
		if (rightViewNeighbor != null) {
			rightViewNeighbor.onFinishLoadingScrollToName = getSelectedItem() == null
					? null
					: getSelectedItem().getName();
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
	 *
	 * @see #initializeDataTableListenerTracker()
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
					String rowtooltipPreText = "Name:\t " + t.getName();
					if (!t.getFilePath().isDirectory()) {
						rowtooltipPreText += "\nSize:\t\t " + String.format("%.2f MB", t.getFileSize()) + "\nModified: "
								+ t.getDateModified();
					}
					if (isOutOfTheBoxHelper && !isOutOfTheBoxRecursive()) {
						return;
					}
					FileTrackerHolder tOption = fileTracker.getTrackerData(t.getFilePath());
					// --------- testing purpose
					// if (fileTracker.isTracked() && tOption == null) {
					// System.out.println("i entered as wrong key");
					// System.out.println(t.getFilePath());
					// }
					// -------------- end testing purpose
					if (tOption != null) {
						t.setSeen(tOption.isSeen());

						String tooltipPreText = tOption.getNoteText();
						t.setNoteText(tooltipPreText);
						if (!tooltipPreText.isEmpty()) {
							rowtooltipPreText += "\nNote:\t" + tooltipPreText;
						}
						if (VLC.isVLCMediaExt(t.getName())) {
							t.getOpenVLC().setOnMouseClicked(m -> {
								// if it is media file
								if (m.getButton().equals(MouseButton.PRIMARY)) {
									// load the preview
									new FilterVLCController(t.getFilePath());
								} else {
									ArrayList<MediaCutData> list = tOption.getMediaCutDataParsed();
									try {
										if (list.size() != 0) {
											// if the media does contain a setting do load it
											VLC.SavePlayListFile(t.getFilePath(), list, true, true, true);
										} else {
											// just start the file with remote features
											VLC.watchWithRemote(t.getFilePath().toURI(), "");
										}
									} catch (VLCException e) {
										DialogHelper.showException(e);
									}
								}
							});
						}
						// end if tracked
					} else {

						t.resetMarkSeen();
						if (VLC.isVLCMediaExt(t.getName())) {
							t.getOpenVLC().setOnMouseClicked(m -> {
								if (m.getButton().equals(MouseButton.PRIMARY)) {
									fileTrackerAdapter.untrackedBehaviorAndAskTrack(
											table.getSelectionModel().getSelectedItems(), t);
								} else {
									try {
										VLC.watchWithRemote(t.getFilePath().toURI(), "");
									} catch (VLCException e) {
										DialogHelper.showException(e);
									}
								}
							});
						}
					}
					// Common stuff
					// toggle all selected items
					t.getMarkSeen().setOnAction(
							e -> fileTrackerAdapter.toggleSeen(table.getSelectionModel().getSelectedItems(), t));

					t.getNoteButton().setOnAction(e -> fileTrackerAdapter
							.setNoteTextAfterAsk(table.getSelectionModel().getSelectedItems(), t));

					// is XSPF start the file directly with custom argument
					if (VLC.isPlaylist(t.getName())) {
						t.getOpenVLC().setOnMouseClicked(m -> {
							try {
								VLC.startXSPFInOrder(t.getFilePath());
							} catch (VLCException e) {
								DialogHelper.showException(e);
							}
						});
					}

					setTooltip(getHoverTooltip(rowtooltipPreText));
				}
			}
		});
	}

	/**
	 * if you want to change directory view then change mDirectory then refresh view
	 * you can use instead {@link #setmDirectoryThenRefresh}
	 */
	public void refresh(String isOutOfTheBoxPath) {
		if (isOutOfTheBoxPath != null) {
			// for out of the box do change directory or add your DataTable stuff
			// before coming here .. this only used to update title and common preview stuff
			truePathField = isOutOfTheBoxPath;
			refreshIsOutOfTheBox();
			// refresh state
			parentWelcome.UpdateTitle(truePathField);
			directoryNameLabel.setText("");
		} else {
			DataTable.clear();
			if (mDirectory.exists()) {
				table.setPlaceholder(loadingTablePlaceHolder);
			} else {
				pathField.setText(truePathField);
				pathField.setText(mDirectory.toString());
				fileNotFoundErrorText.setText(mDirectory.toString());
				table.setPlaceholder(fileNotFoundPlaceHolder);
				reloadSearchField();

				parentWelcome.UpdateTitle(mDirectory.getName());
				if (isLeft) {
					updateFavoriteCheckBox(isOutOfTheBoxHelper);
				}
				return;
			}
			try {
				watchServiceHelper.changeObservableDirectory(mDirectory);
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
			refreshIsOutOfTheBox();
			ThreadExecutors.filesLister.execute(() -> {
				try {
					List<PathLayer> currentFileList = getCurrentFilesList();
					@Nullable
					HashMap<PathLayer, FileTrackerHolder> isLoaded = fileTracker.loadMap(getmDirectoryPath(), true,
							PathLayerHelper.getAbsolutePathToPaths(currentFileList));

					// note that newly added files to map have null seen status so user got noticed
					// of the for the first time
					@Nullable
					FileTrackerConflictLog conflict = isLoaded == null
							? null
							: fileTracker.resolveConflict(new HashSet<>(currentFileList));

					// Virtual options sections
					AtomicBoolean hasDirOption = new AtomicBoolean(false);
					fileTracker.getMapDetails().values().stream().filter(opt -> opt.isVirtualOption()).forEach(opt -> {
						if (opt instanceof FileTrackerDirectoryOptions && Setting.isDidLoadedAllPartAndExecuteRegistredTask()) {
							// prevent update views if changes occur in other views to allow differentiation between views
							if (!parentWelcome.isDirOpenedInOtherView(mDirectory, this)) {
								FileTrackerDirectoryOptions optionsDir = (FileTrackerDirectoryOptions) opt;
								Platform.runLater(() -> restoreDirectoryViewOptions(optionsDir.getDirectoryViewOptions()));
							}
							hasDirOption.set(true);
						}
					});
					// Default virtual options setting if it doesn't exist
					if (!hasDirOption.get() && Setting.isDidLoadedAllPartAndExecuteRegistredTask()) {
						Platform.runLater(() -> restoreDirectoryViewOptions(new DirectoryViewOptions()));
					}
					// END virtual options sections

					Platform.runLater(() -> {
						// added another datatable clear because sometime when lunching application with 3 split views
						// due that second view will use the 3rd last view location, concurent change after loading xml
						// may cause the view to aggregate both files without clearing it
						DataTable.clear();
						showList(currentFileList);
						if (conflict != null && conflict.didChangeOccurs() && Setting.isNotifyFilesChanges()) {
							showConflictLogNotification(conflict);
						}
					});
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			parentWelcome.UpdateTitle(mDirectory.getName());
			directoryNameLabel.setText(mDirectory.getName());
		}
		if (isLeft) {
			updateFavoriteCheckBox(isOutOfTheBoxHelper);
		}
		pathField.setText(truePathField);
	}

	private void showConflictLogNotification(FileTrackerConflictLog conflict) {
		String message = "Changes: ";
		ArrayList<MenuItem> menus = new ArrayList<>();
		parentWelcome.getStage().show();
		if (conflict.addedItems.size() != 0) {
			message += conflict.addedItems.size() + " New Items\n";
			for (PathLayer p : conflict.addedItems) {
				MenuItem mn = new MenuItem("- New: " + p.getName());
				mn.setOnAction(e -> {
					table.getSelectionModel().clearSelection();
					ScrollToName(p.getName());
				});
				menus.add(mn);
			}
		}
		if (conflict.removedItems.size() != 0) {
			message += conflict.removedItems.size() + " Removed Items\n";
			for (PathLayer p : conflict.removedItems) {
				MenuItem mn = new MenuItem("- Removed: " + p.getName());
				menus.add(mn);
			}
		}
		if (conflict.addedItems.size() > 1) {
			MenuItem selectAll = new MenuItem("Select All");
			selectAll.setOnAction(e -> selectAllThatMatchAny(
					conflict.addedItems.stream().map(p -> p.getName()).collect(Collectors.toSet())));
			menus.add(selectAll);
			selectAll.fire();
		} else if (conflict.addedItems.size() == 1) {
			table.getSelectionModel().clearSelection();
			ScrollToName(conflict.addedItems.get(0).getName());
		}
		if (!Setting.isShowWindowOnTopWhenNotify()) {
			MenuItem showMe = new MenuItem("Show me");
			showMe.setOnAction(e -> {
				boolean perviousState = parentWelcome.getStage().isAlwaysOnTop();
				parentWelcome.getStage().setAlwaysOnTop(true);
				parentWelcome.getStage().setAlwaysOnTop(perviousState);
				showToastNotifyExistance();
			});
			menus.add(showMe);
		}
		showToastMessage(message, menus);
	}

	private void updateFavoriteCheckBox(boolean isOutOfTheBoxHelper) {
		if (isOutOfTheBoxHelper) {
			favoriteCheckBox.setVisible(false);
		} else {
			favoriteCheckBox.setVisible(true);
			favoriteCheckBox.setSelected(Setting.getFavoritesViews().containsByFirstLoc(getmDirectoryPath()));
		}
	}

	private static List<String> SpecialPath = Arrays.asList(PATH_FIELD_LIST_ROOTS, "?", "&", "|");

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

	/** Higher priority than on {@link #onRefreshAsPathFieldAutoScrollToName} */
	private String onFinishLoadingScrollToName = null;
	private String onRefreshAsPathFieldAutoScrollToName = null;

	/**
	 * Scroll to this name only till next refresh is done
	 *
	 * @param onFinishLoadingScrollToName
	 *            the onFinishLoadingScrollToName to set
	 */
	public void setOnFinishLoadingScrollToName(String onFinishLoadingScrollToName) {
		this.onFinishLoadingScrollToName = onFinishLoadingScrollToName;
	}

	/**
	 * Show list of path layer in table with default tracker data.<br>
	 * Does not clear currently shown list
	 *
	 * @see #initializeTableRowFactory()
	 * @see #initializeTable()
	 * @param list
	 */
	private void showList(List<PathLayer> list) {
		table.setPlaceholder(noContentTablePlaceHolder);
		// check also initializeTableRowFactory for generating table row action
		// and DataTable.addListener for adding search parameters
		DataTable.addAll(
				list.stream().map(file -> new TableViewModel(" ", file.getName(), file)).collect(Collectors.toList()));

		labelItemsNumber.setText(" #" + DataTable.size() + " items");
		reloadSearchField();
		if (onFinishLoadingScrollToName != null && !onFinishLoadingScrollToName.isEmpty()) {
			ScrollToName(onFinishLoadingScrollToName);
		} else if (onRefreshAsPathFieldAutoScrollToName != null && !onRefreshAsPathFieldAutoScrollToName.isEmpty()) {
			ScrollToName(onRefreshAsPathFieldAutoScrollToName);
		} else {
			table.scrollTo(0);
		}
		onFinishLoadingScrollToName = null;
		onRefreshAsPathFieldAutoScrollToName = null;
	}

	private LinkedList<PathLayer> BackQueue = new LinkedList<>();
	private LinkedList<PathLayer> NextQueue = new LinkedList<>();

	public void setBackQueue(LinkedList<PathLayer> BackQueue) {
		this.BackQueue = BackQueue;
	}

	public void setNextQueue(LinkedList<PathLayer> NextQueue) {
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

	private void AddToQueue(PathLayer file) {
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
				ThreadExecutors.recursiveExecutor.execute(doUpThreadOff);
			}
		} else {
			goUpParent();
		}
	}

	// go up directory until reaching root
	private void goUpParent() {
		predictNavigation.setText("");
		PathLayer parent = mDirectory.getParentPath();
		PathLayer oldmDirectory = mDirectory;
		if (parent != null) {
			AddToQueue(mDirectory);
			EmptyNextQueue();
			mDirectory = parent;
			if (mDirectory.exists()) {
				onFinishLoadingScrollToName = oldmDirectory.getName();
				refresh(null);
			} else {
				goUpParent();
			}
		} else {
			OutOfTheBoxListRoots(oldmDirectory);
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

	private void OutOfTheBoxListRoots(PathLayer oldmDirectory) {
		List<PathLayer> roots = Arrays.asList(File.listRoots()).stream().map(f -> new FilePathLayer(f))
				.collect(Collectors.toList());
		if (recursiveSearch.isSelected()) {
			// do not set to false since fire do invert selection !
			// recursiveSearch.setSelected(false);
			// recursiveSearch.fire();
			switchRecursive();
		}
		refresh(PATH_FIELD_LIST_ROOTS);
		if (oldmDirectory != null && !roots.contains(oldmDirectory)) {
			roots.add(oldmDirectory);
		}
		DataTable.clear();

		for (PathLayer root : roots) {
			TableViewModel t = new TableViewModel(" ", root.getAbsolutePath(), root);
			t.getHboxActions().getChildren().clear();
			DataTable.add(t);
		}
	}

	private void doRecursiveSearch() {
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
				PathLayer dir = getmDirectoryPath();
				fileTracker.getMapDetails().clear();
				Platform.runLater(() -> {
					table.setPlaceholder(loadingTablePlaceHolder);
					DataTable.clear();
				});
				outOfTheBoxRecursive = true;
				boolean didReachLimit = false;

				// TODO check UTF Validity
				// mfileTracker.OutofTheBoxAddToMapRecusive(dir);
				// https://github.com/brettryan/io-recurse-tests
				RecursiveFileWalker rWalker = new RecursiveFileWalker();
				// to handle if recursive search was pressed in middle of
				// search without wiping all data
				recursiveSearch.setOnAction(e -> {
					if (recursiveSearch.isSelected()) {
						recursiveSearch.setSelected(true);
					}
				});

				try {
					List<PathLayer> selectionsPath = getSelection();
					if (selectionsPath.size() > 1) {
						selectionsPath.stream().filter(p -> p.isDirectory()).forEach(p -> {
							try {
								PathLayerHelper.walkFileTree(p, Integer.MAX_VALUE, false, rWalker);
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						});
					} else {
						PathLayerHelper.walkFileTree(dir, Integer.MAX_VALUE, false, rWalker);
					}
					List<PathLayer> paths = rWalker.getDirectories().stream().collect(Collectors.toList());

					if (rWalker.getFilesCount() > Setting.getMaxLimitFilesRecursive()) {
						didReachLimit = true;
						Collections.sort(paths, (p1, p2) -> {
							// sort directory as traversal BFS not DFS
							return p1.getNameCount() - p2.getNameCount();
						});
					}

					FileTracker trackerLoader = new FileTracker(null, null);

					for (PathLayer p : paths) {
						if (!recursiveSearch.isSelected()) {
							break;
						}
						try {
							@Nullable
							HashMap<PathLayer, FileTrackerHolder> loadedMap = trackerLoader.loadMap(p, false,
									PathLayerHelper.getAbsolutePathToPaths(rWalker.getDirectoriesToFiles().get(p)));
							if (loadedMap == null) {
								// load empty map if cannot loadMap of directory
								fileTracker.loadEmptyMapOfList(rWalker.getDirectoriesToFiles().get(p));
							} else {
								// resolve conflict in map
								trackerLoader.setWorkingDirPath(p);
								trackerLoader.resolveConflict(new HashSet<>(rWalker.getDirectoriesToFiles().get(p)));
								fileTracker.getMapDetails().putAll(trackerLoader.getMapDetails());

								trackerLoader.getMapDetails().clear();
							}

							if (fileTracker.getMapDetails().size() > Setting.getMaxLimitFilesRecursive()) {
								break;
							}
						} catch (Exception e) {
							// failed to load some directory
							e.printStackTrace();
						}
					}
				} catch (IOException e) {
					Platform.runLater(() -> RecursiveHelperUpdateTitle(e.getMessage()));
				}
				List<TableViewModel> allRowModel = new ArrayList<>();
				for (PathLayer pathItem : fileTracker.getMapDetails().keySet()) {
					if (pathItem != null && fileTracker.getMapDetails().get(pathItem).isForDisplayRecord()) {
						allRowModel.add(new TableViewModel(" ", pathItem.getName(), pathItem));
					}
				}

				Platform.runLater(() -> {
					RecursiveHelperLoadDataTable(allRowModel);
					table.setPlaceholder(noContentTablePlaceHolder);
					reloadSearchField();
				});

				finish = Instant.now();
				WatchServiceHelper.setRuning(true);

				timeElapsed = java.time.Duration.between(start, finish).toMillis(); // in millis
				msg = "Showing " + allRowModel.size() + " Files Indexed "
						+ (didReachLimit ? "of " + rWalker.getFilesCount() : "") + " in " + timeElapsed
						+ " milliseconds!"
						+ (didReachLimit ? "\nYou Can Change Limit File count in menu Tracker Setting" : "")
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
	 * @param searchPattern
	 *            ';' to combine multiple search statement '!' to exclude from
	 *            search
	 *
	 *            example i want all vlc media that contain name word and not excel
	 *            i search: 'vlc;word;!excel'
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

	private List<PathLayer> getCurrentFilesList() throws IOException {
		List<PathLayer> listFiles = null;
		listFiles = mDirectory.listNoHiddenPathLayers();
		Collections.sort(listFiles, StringHelper.NaturalFileComparator);
		return listFiles;
	}

	public List<PathLayer> getShownFilesList() {
		List<PathLayer> listFiles = new ArrayList<PathLayer>();
		sortedData.forEach(t -> listFiles.add(t.getFilePath()));
		return listFiles;
	}

	public PathLayer getmDirectoryPath() {
		return mDirectory;
	}

	private Tooltip getHoverTooltip(String note) {
		if (note.isEmpty() || note.equals(" ")) {
			return null;
		}
		Tooltip tooltip = new Tooltip();

		tooltip.setText(note);
		tooltip.getStyleClass().addAll("tooltip", "tooltipHover");
		return tooltip;
	}

	public FileTracker getFileTracker() {
		return fileTracker;
	}

	public WatchServiceHelper getWatchServiceHelper() {
		return watchServiceHelper;
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

	/** @see TableViewSelectionModel#getSelectedItem() */
	@Nullable
	public TableViewModel getSelectedItem() {
		return table.getSelectionModel().getSelectedItem();
	}

	private PathLayer getSelectedPathIfDirectoryOrParent() {
		TableViewModel t = table.getSelectionModel().getSelectedItem();
		if (t == null) {
			return null;
		}
		if (!t.getFilePath().isDirectory()) {
			return t.getFilePath().getParentPath();
		}
		return t.getFilePath();
	}

	// this helper is to optimize call of the function
	// so only call when really need to update state

	// for recursive mode use OutOfTheBoxRecursive
	/**
	 *
	 * @return all files paths of selected items
	 */
	public List<PathLayer> getSelection() {
		List<PathLayer> selection = new ArrayList<>();
		for (TableViewModel item : table.getSelectionModel().getSelectedItems()) {
			selection.add(item.getFilePath());
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

	private void initializeToolsMenu() {
		toolsMenu.showingProperty().addListener((observable, oldValue, newValue) -> {
			if (toolsMenu.isShowing()) {
				toolsMenu.getItems().clear();
				toolsMenu.getItems().addAll(getContextMenuList());
			}
		});
	}

	private List<MenuItem> getContextMenuList() {
		// File Operation On Selection
		MenuItem openFiles = new MenuItem("Open");
		openFiles.setOnAction(e -> open());
		openFiles.setGraphic(new ImageView(ContextMenuLook.openIcon));

		MenuItem copyFiles = new MenuItem("Copy");
		copyFiles.setOnAction(e -> copy());
		copyFiles.setGraphic(new ImageView(ContextMenuLook.copyIcon));

		MenuItem moveFiles = new MenuItem("Cut");
		moveFiles.setOnAction(e -> move());
		moveFiles.setGraphic(new ImageView(ContextMenuLook.moveIcon));

		MenuItem pasteFiles = new MenuItem("Paste");
		pasteFiles.setOnAction(e -> paste());
		pasteFiles.setGraphic(new ImageView(ContextMenuLook.clipboardIcon));

		MenuItem renameSelection = new MenuItem("Rename");
		renameSelection.setOnAction(e -> rename());
		renameSelection.setGraphic(new ImageView(ContextMenuLook.renameIcon));

		// Name Copier Section
		Menu nameCopier = new Menu("Bulk Rename");
		MenuItem copyBaseNames = new MenuItem("Copy Base Names");
		MenuItem pasteBaseNames = new MenuItem("Paste Base Names");
		MenuItem renameUtility = new MenuItem("Rename Utility");
		MenuItem undoLastPasteNames = new MenuItem("Undo Last batch Rename");

		copyBaseNames.setOnAction(e -> copyBaseNames());
		copyBaseNames.setGraphic(new ImageView(ContextMenuLook.copyBaseNameIcon));
		pasteBaseNames.setOnAction(e -> pasteBaseNames());
		pasteBaseNames.setGraphic(new ImageView(ContextMenuLook.pasteBaseNameIcon));

		renameUtility.setOnAction(e -> new RenameUtilityController(getSelection()));
		renameUtility.setGraphic(new ImageView(ContextMenuLook.bulkRenameUtilityIcon));

		undoLastPasteNames.setOnAction(e -> RenameUtilityController.undoLastRename(null, null));
		undoLastPasteNames.setGraphic(new ImageView(ContextMenuLook.undoIcon));

		nameCopier.setGraphic(new ImageView(ContextMenuLook.bulkRenameIcon));
		nameCopier.getItems().addAll(copyBaseNames, pasteBaseNames, renameUtility, undoLastPasteNames);

		// New File Section
		Menu newItemCreation = new Menu("New");
		MenuItem newFile = new MenuItem("Create New File");
		MenuItem newFolder = new MenuItem("Create New Folder");
		newFile.setOnAction(e -> createFile());
		newFile.setGraphic(new ImageView(ContextMenuLook.fileIcon));

		newFolder.setOnAction(e -> createDirectory());
		newFolder.setGraphic(new ImageView(ContextMenuLook.folderIcon));

		newItemCreation.setGraphic(new ImageView(ContextMenuLook.newIcon));
		newItemCreation.getItems().addAll(newFile, newFolder);

		MenuItem delete = new MenuItem("Delete");
		delete.setOnAction(e -> delete());
		delete.setGraphic(new ImageView(ContextMenuLook.deleteIcon));

		// Tracker Section
		Menu tracker = new Menu("Tracker");

		// File Tracker
		Menu trackerData = new Menu("Data");
		MenuItem trackCurrent = new MenuItem("Track Current");
		MenuItem trackRecusively = new MenuItem("Track Recursivly");
		MenuItem cleanCurrent = new MenuItem("Clean Current");
		MenuItem cleanRecursively = new MenuItem("Clean Recursively");
		trackCurrent.setOnAction(e -> trackCurrent());
		trackRecusively.setOnAction(e -> trackRecusivly());
		cleanCurrent.setOnAction(e -> cleanCurrent());
		cleanRecursively.setOnAction(e -> cleanRecursively());

		trackerData.setGraphic(new ImageView(ContextMenuLook.trackerDataIcon));
		trackerData.getItems().addAll(trackCurrent, trackRecusively, cleanCurrent, cleanRecursively);

		// Tracker Player Section
		Menu trackerPlayer = new Menu("Cortana");
		MenuItem newTrackerPlayerPlaylist = new MenuItem("Create New Cortana Playlist");
		MenuItem newTrackerPlayerAny = new MenuItem("Create New Cortana Shortcut");
		newTrackerPlayerPlaylist.setOnAction(e -> createTrackerPlayerShortcutPlaylist());
		newTrackerPlayerAny.setOnAction(e -> createTrackerPlayerShortcutAny());

		trackerPlayer.setGraphic(new ImageView(ContextMenuLook.cortanaIcon));
		trackerPlayer.getItems().addAll(newTrackerPlayerPlaylist, newTrackerPlayerAny);

		// Tracker VLC Section
		Menu trackerVLC = new Menu("VLC");
		MenuItem bulkRemoveIntro = new MenuItem("Bulk Remove Intro");
		bulkRemoveIntro.setOnAction(e -> bulkRemoveIntro());

		trackerVLC.setGraphic(new ImageView(ContextMenuLook.vlcIcon));
		trackerVLC.getItems().add(bulkRemoveIntro);

		// Tracker Note Section
		Menu trackerNote = new Menu("Note");
		MenuItem bulkNoteOrdering = new MenuItem("Bulk Note ordering");
		bulkNoteOrdering.setOnAction(e -> bulkNoteOrdering());

		trackerNote.setGraphic(new ImageView(ContextMenuLook.noteIcon));
		trackerNote.getItems().add(bulkNoteOrdering);

		tracker.setGraphic(new ImageView(ContextMenuLook.trackerIcon));
		tracker.getItems().addAll(trackerData, trackerNote, trackerVLC, trackerPlayer);

		// Hidden View Section
		Menu hiddenView = new Menu("View");
		MenuItem showCustomSort = new MenuItem("Custom Sort Control");
		showCustomSort.setOnAction(e -> showToggleCustomSortControl());
		showCustomSort.setGraphic(new ImageView(ContextMenuLook.sortIcon));

		MenuItem selectAll = new MenuItem("Select All");
		selectAll.setOnAction(e -> table.getSelectionModel().selectAll());
		selectAll.setGraphic(IconLoader.getIconImageView(ICON_TYPE.SELECT_ALL));

		hiddenView.setGraphic(new ImageView(ContextMenuLook.hiddenIcon));
		hiddenView.getItems().addAll(showCustomSort, selectAll);

		// System Context Menu
		MenuItem systemContextMenu = new MenuItem("More");
		systemContextMenu.setOnAction(e -> showContextMenuSystem());
		systemContextMenu.setGraphic(new ImageView(ContextMenuLook.systemIcon));

		ArrayList<MenuItem> allMenu = new ArrayList<>();
		allMenu.add(openFiles);
		allMenu.add(copyFiles);
		allMenu.add(moveFiles);
		allMenu.add(pasteFiles);
		allMenu.add(renameSelection);
		allMenu.add(nameCopier);
		allMenu.add(newItemCreation);
		allMenu.add(delete);
		allMenu.add(tracker);
		allMenu.add(hiddenView);
		allMenu.add(systemContextMenu);
		List<PathLayer> selections = getSelection();
		UserContextMenuController.generateUserContextMenus(selections,
				parentAdd -> allMenu.add(
						ArrayListHelper.getCyclicIndex(parentAdd.getKey().getMenuOrder(), allMenu.size()),
						parentAdd.getValue()));
		return allMenu;
	}


	private HBox sortControlVbox;

	private void showToggleCustomSortControl() {
		if (sortControlVbox == null) {
			sortControlVbox = new HBox();
			sortControlVbox.setAlignment(Pos.CENTER);

			Button up = new Button("Up");

			up.setOnAction(e -> ControlListHelper.moveUpSelection(table.getSelectionModel(), DataTable));
			up.setGraphic(IconLoader.getIconImageView(ICON_TYPE.UP, true));

			Button down = new Button("Down");
			down.setOnAction(e -> ControlListHelper.moveDownSelection(table.getSelectionModel(), DataTable));
			down.setGraphic(IconLoader.getIconImageView(ICON_TYPE.DOWN, true));

			Button remove = new Button("Hide Items");
			remove.setOnAction(e -> ControlListHelper.removeSelection(table.getSelectionModel(), DataTable));
			remove.setGraphic(new ImageView(ContextMenuLook.hiddenIcon));

			Button hide = new Button("Close");
			hide.setOnAction(e -> showToggleCustomSortControl());
			hide.setGraphic(IconLoader.getIconImageView(ICON_TYPE.REMOVE, true));

			sortControlVbox.getChildren().addAll(up, down, remove, hide);
			topTableVbox.getChildren().add(sortControlVbox);
		} else {
			sortControlVbox.setVisible(!sortControlVbox.isVisible());
			if (!sortControlVbox.isVisible()) {
				topTableVbox.getChildren().remove(sortControlVbox);
			} else if (!topTableVbox.getChildren().contains(sortControlVbox)) {
				topTableVbox.getChildren().add(sortControlVbox);
			}
		}
	}

	private void copyBaseNames() {
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
				"Note:\n" + warningAlert + "- Use it as you like with Paste Names Options!\nContent:", myString);
	};

	private void pasteBaseNames() {
		boolean error = false;
		tryBlock : try {
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
					"Preview Change" + "\nPending -----   " + toRenameWithFinal.size() + " ----- Changes Pair Rename\n",
					"Each Separated line Name assigned to selection item in table order. \n" + "Note:" + warningAlert,
					fullAlertReport);
			if (ans) {
				i = 0;
				String renameError = "";
				HashMap<PathLayer, PathLayer> currentNewToOldRename = new HashMap<>();
				List<PathLayer> sources = new ArrayList<>();
				List<PathLayer> targets = new ArrayList<>();
				for (TableViewModel t : toWorkWith) {
					if (i < toRenameWithFinal.size()) {
						try {
							PathLayer oldFile = t.getFilePath();
							PathLayer newFile = t.getFilePath().getParentPath().resolve(toRenameWithFinal.get(i));
							FileHelper.renameHelper(oldFile, newFile);
							sources.add(oldFile);
							targets.add(newFile);
							currentNewToOldRename.put(newFile, oldFile);

						} catch (IOException e) {
							renameError += t.getFilePath().getName() + " --> " + toRenameWithFinal.get(i) + "\n";
							warningAlert += e.getClass() + ": " + e.getMessage() + "\n";
							e.printStackTrace();
						}
						i++;
					} else {
						break;
					}
				}
				FileTracker.operationUpdateAsList(sources, targets, ActionOperation.RENAME);
				RenameUtilityController.getAllNewToOldRename().add(currentNewToOldRename);
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
	};

	private void createTrackerPlayerShortcutAny() {
		/** Working in local Mode */
		if (!mDirectory.isLocal()) {
			return;
		}
		TableViewModel t = table.getSelectionModel().getSelectedItem();
		if (t == null) {
			DialogHelper.showAlert(AlertType.INFORMATION, "Cortana Shortcut", "Select an item from Table first", "");
			return;
		}
		String name = TrackerPlayer.getPlaylistName();
		TrackerPlayer.createNewShortcutPlaylist(name, t.getFilePath().toPath());

	}

	private void createTrackerPlayerShortcutPlaylist() {

		/** Working in local Mode */
		if (!mDirectory.isLocal()) {
			return;
		}
		List<Path> files = new ArrayList<>();
		for (TableViewModel t : table.getSelectionModel().getSelectedItems()) {
			File tFile = t.getFilePath().toFileIfLocal();
			if (VLC.isVLCMediaExt(tFile.getName()) || tFile.isDirectory()) {
				files.add(t.getFilePath().toPath());
			}
		}
		if (files.size() == 0) {
			String allFilesString = "";
			for (TableViewModel t : DataTable) {
				File tFile = t.getFilePath().toFileIfLocal();
				if (VLC.isVLCMediaExt(tFile.getName()) || tFile.isDirectory()) {
					files.add(t.getFilePath().toPath());
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
	}

	private void trackCurrent() {
		try {
			fileTracker.trackNewFolder(true, true);
			parentWelcome.refreshAllSplitViewsIfMatch(mDirectory, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void trackRecusivly() {
		String answer = DialogHelper.showTextInputDialog("Recursive Tracker", "Depth to consider",
				"Enter depth value to consider begining from '" + mDirectory.getName() + "' view folder"
						+ "\nThen it will track all sub directory in it"
						+ "\nInput must be a number format if anything goes wrong '1' is the default value",
				"2");
		if (answer == null) {
			return;
		}
		Holder<Integer> depth = new Holder<Integer>(1);
		try {
			depth.value = Integer.parseInt(answer);
		} catch (NumberFormatException e1) {
			depth.value = 1;
			// e1.printStackTrace();
		}
		PathLayer dir = getmDirectoryPath();
		ThreadExecutors.recursiveExecutor.execute(() -> {
			try {
				RecursiveFileWalker r = new RecursiveFileWalker();
				// to include depth so subDirectory got involved
				PathLayerHelper.walkFileTree(dir, depth.value, false, r);
				r.getDirectories().forEach(p -> {
					Platform.runLater(() -> Main.ProcessTitle("Tracking " + p.getName()));
					try {
						getFileTracker().trackNewOutFolder(p, false, false);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
				Platform.runLater(() -> parentWelcome.refreshAllSplitViews());
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	public void cleanCurrent() {
		if (!getFileTracker().isTracked()) {
			DialogHelper.showAlert(AlertType.INFORMATION, "Delete Tracker Data", "This is already Untracked folder",
					"Are you kidding me.");
			return;
		}
		boolean ans = DialogHelper.showConfirmationDialog("Delete Tracker Data",
				"Are you Sure You want to wipe tracker data?",
				"Note: this have nothing to do with your files, it just delete .tracker_explorer.txt"
						+ " >>And so set all item to untracked.\nThis cannot be undone!");
		if (ans) {
			try {
				getFileTracker().deleteTrackerFile();
				parentWelcome.refreshAllSplitViewsIfMatch(mDirectory, null);
			} catch (IOException e) {
				e.printStackTrace();
				DialogHelper.showException(e);
			}
		}
	}

	public void cleanRecursively() {
		String[] labels = {"Depth", "User"};
		String[] hints = {"0", Setting.getActiveUser()};
		int focusAt = 0;
		@Nullable
		HashMap<String, String> answers = DialogHelper.showMultiTextInputDialog("Recursive Cleaner",
				"Recursive Cleaner",
				"Enter Depth value to consider begining from this view and\n"
						+ "Then it will clean all tracker data sub directory in it\n"
						+ "Input must be a number format if anything goes wrong '0' is the default value"
						+ "\nNote:input User name Work even if The user wasn't in the users list",
				labels, hints, focusAt);
		if (answers == null) {
			return;
		}
		Integer depth;
		try {
			depth = Integer.parseInt(answers.get(labels[0]));
		} catch (NumberFormatException e1) {
			depth = 0;
			// e1.printStackTrace();
		}
		String user = answers.get(labels[1]);
		if (user.isEmpty()) {
			return;
		}

		PathLayer dir = getmDirectoryPath();
		final int depths = depth;
		ThreadExecutors.recursiveExecutor.execute(() -> {
			try {
				RecursiveFileWalker r = new RecursiveFileWalker();
				PathLayerHelper.walkFileTree(dir, depths, true, r);
				r.getDirectories().forEach(p -> {
					Platform.runLater(() -> Main.ProcessTitle("Cleaning " + p.getName()));
					FileTracker.deleteOutFile(p, user);
				});
				Platform.runLater(() -> parentWelcome.refreshAllSplitViews());
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	private void bulkNoteOrdering() {
		String[] labels = {"Depth", "Start From"};
		String[] hints = {"1", "1"};
		int focusAt = 0;
		@Nullable
		HashMap<String, String> answer = DialogHelper.showMultiTextInputDialog("Bulk Note Ordering",
				"Bulk Note Ordering",
				"--> Add number ordering for selection in order.\n"
						+ "--> Affect same order (note) to all sub directory/files in it\n"
						+ "Input Depth to consider begining from each selection\n"
						+ "Input must be a number format if anything goes wrong '0' is the default value",
				labels, hints, focusAt);
		if (answer == null) {
			return;
		}
		Holder<Integer> depth = new Holder<>(0);
		Holder<Integer> startFrom = new Holder<Integer>(1);
		try {
			depth.value = Integer.parseInt(answer.get(labels[0]));
			startFrom.value = Integer.parseInt(answer.get(labels[1]));
		} catch (NumberFormatException e1) {
			depth.value = 0;
			startFrom.value = 1;
		}
		ThreadExecutors.recursiveExecutor.execute(() -> {
			try {
				List<PathLayer> selection = getSelection();
				int maxOrder = selection.size();
				int pad = 1;
				while (maxOrder / 10 != 0) {
					pad++;
					maxOrder /= 10;
				}
				maxOrder = selection.size();
				HashMap<PathLayer, String> selectionToNotes = new HashMap<>();
				Map<String, PathLayer> cachedPathsForKeysInMap = new HashMap<>();
				int order = startFrom.value;
				for (PathLayer entry : selection) {
					String note = String.format("%0" + pad + "d", order++);
					selectionToNotes.put(entry, note);
					cachedPathsForKeysInMap.put(entry.getAbsolutePath(), entry);
				}
				FileTracker miniFileTracker = new FileTracker(null, null);
				HashMap<PathLayer, List<PathLayer>> parentToSons = PathLayerHelper.getParentTochildren(selection);
				// writing at 0 depth
				for (PathLayer parent : parentToSons.keySet()) {
					if (miniFileTracker.loadMap(parent, true, cachedPathsForKeysInMap) == null) {
						if (miniFileTracker.trackNewOutFolder(parent, true, true) == null) {
							// if failed to load map or create new default one skip
							continue;
						}
					}
					for (PathLayer target : miniFileTracker.getMapDetails().keySet()) {
						if (selectionToNotes.containsKey(target)) {
							miniFileTracker.getMapDetails().get(target).setNoteText(selectionToNotes.get(target));
						}
					}
					miniFileTracker.writeMap();
				}

				// writing depth from selection
				RecursiveFileWalker r = new RecursiveFileWalker();
				for (PathLayer selected : selection) {
					if (!selected.isDirectory()) {
						continue;
					}
					PathLayerHelper.walkFileTree(selected, depth.value, false, r);
					r.getDirectories().forEach(dir -> {
						if (miniFileTracker.loadMap(dir, true, r.getDirectoriesToFiles().get(dir).stream()
								.collect(Collectors.toMap(p -> p.getAbsolutePath(), p -> p))) == null) {
							try {
								if (miniFileTracker.trackNewOutFolder(dir, true, true) == null) {
									// if failed to load map or create new default one skip
									return;
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						for (FileTrackerHolder dataHolder : miniFileTracker.getMapDetails().values()) {
							dataHolder.setNoteText(selectionToNotes.get(selected));
						}
						boolean changeHappen = miniFileTracker.getMapDetails().size() != 0;
						if (changeHappen) {
							miniFileTracker.writeMap();
						}
					});
					r.clearAllRecords();
				}
				Platform.runLater(() -> parentWelcome.refreshAllSplitViews());
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	void bulkRemoveIntro() {
		if (isOutOfTheBoxHelper()) {
			DialogHelper.showAlert(AlertType.INFORMATION, "Bulk Intro Remover", "Recursive Mode Restriction",
					"this feature is unavailable in recursive mode,\r\n"
							+ "Please turn it off then try again.\nIf you like it to be contact developpers!");
			return;
		}
		String answer = DialogHelper.showTextInputDialog("Bulk Intro Remover", "Time To exclude from start?",
				"Enter the time to exclude From the begining, "
						+ "\nthis will apply on all selected Media file in Right View."
						+ "\nAlso will clear previous configured filters"
						+ "\nInput must be in duration format: ss or mm:ss or hh:mm:ss (example: 234 or 3:54)",
				"00     :00     :00");
		if (answer == null) {
			return;
		}
		Duration ans = FilterVLCController.studyFormat(answer, " Feild", true);
		if (ans == null || ans.toSeconds() <= 0) {
			return;
		}

		if (!getFileTracker().isTracked()) {
			try {
				getFileTracker().trackNewFolder(true, true);
			} catch (IOException e) {
				e.printStackTrace();
				DialogHelper.showException(e);
				return;
			}
		}

		List<PathLayer> paths = getSelection();
		if (paths.size() <= 1) {
			paths = getShownFilesList();
		}
		String start = "" + 0;
		String end = "" + (int) ans.toSeconds();
		for (PathLayer path : paths) {
			if (VLC.isAudioOrVideo(path.getName())) {
				FileTrackerHolder optionItem = getFileTracker().getMapDetails().get(path);
				String desc = path.getName() + " [Skipped Intro]";
				String finalFormat = ">" + start + ">" + end + ">" + desc;
				optionItem.setMediaCutDataUnPrased(finalFormat);
			}
		}
		getFileTracker().writeMap();
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
	 * Only use for {@link FileTracker#FileTracker}
	 *
	 * @param value
	 */
	public void setIsOutOfTheBoxHelper(boolean value) {
		isOutOfTheBoxHelper = value;
	}

	public boolean isOutOfTheBoxRecursive() {
		return outOfTheBoxRecursive;
	}

	/**
	 *
	 * @param filePath
	 * @return true if the navigate was successful
	 */
	public boolean navigate(PathLayer filePath) {
		boolean isDirectory = filePath.isDirectory();

		// double check in case of fixed shortcut
		if (!isDirectory && filePath.isLocal() && filePath.getExtensionUPPERCASE().equals("LNK")) {
			File selectedFileLocal = filePath.toFileIfLocal();
			selectedFileLocal = WindowsShortcut.getRealFileIfDirectory(selectedFileLocal);
			filePath = new FilePathLayer(selectedFileLocal);
		}

		if (isDirectory) {
			setmDirectoryThenRefresh(filePath);
		} else {
			try {
				int sizeOfFiles = table.getSelectionModel().getSelectedItems().size();
				if (VLC.isWellSetup() && VLC.isVLCMediaExt(filePath.getName())
						&& (!filePath.isLocal() || sizeOfFiles != 1)) {
					String files = " --playlist-enqueue";
					for (TableViewModel t : table.getSelectionModel().getSelectedItems()) {
						if (VLC.isVLCMediaExt(t.getName())) {
							files += " " + t.getFilePath().toURI();
						}
					}
					VLC.StartVlc(files);
					// we always start media because playlist do not start automatically
				} else {
					// deal other types of files
					if (StringHelper.getExtention(filePath.getName()).equals("PDF")
							&& (!filePath.isLocal() || sizeOfFiles != 1)) {
						// open bunch of PDF
						StringHelper.openFiles(table.getSelectionModel().getSelectedItems().stream()
								.map(p -> ((FilePathLayer) p.getFilePath()).getFile())
								.filter(p -> StringHelper.getExtention(p.getName()).equals("PDF"))
								.collect(Collectors.toList()));

						// open bunch of Image or an image
					} else if (PhotoViewerController.ArrayIMGExt.contains(filePath.getExtensionUPPERCASE())) {
						new PhotoViewerController(table.getSelectionModel().getSelectedItems().stream()
								.map(p -> p.getFilePath())
								.filter(p -> PhotoViewerController.ArrayIMGExt
										.contains(StringHelper.getExtention(p.getName())))
								.collect(Collectors.toList()), filePath, parentWelcome);

					} else {
						// default option
						// AWT function used here for better support of opening file from network
						// AWT was not used to prevent hole stack of AWT but no good alternative in
						// javaFX
						// Desktop.getDesktop().open(selectedFile);
						if (filePath.isLocal()) {
							// support later getFile of any PathLayer by downloading file
							StringHelper.openFile(filePath.toFileIfLocal());
						} else {
							Desktop.getDesktop().browse(filePath.toURI());
						}
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

	protected void selectTableViewModel(TableViewModel toBeSelected) {
		table.getSelectionModel().select(toBeSelected);
	}

	public void selectIndex(int index) {
		if (index < 0 && index >= sortedData.size()) {
			return;
		}
		TableViewModel found = sortedData.get(index < sortedData.size() ? index : 0);
		if (found != null) {
			table.getSelectionModel().select(found);
			table.scrollTo(smartScrollIndex(sortedData.indexOf(found)));
		}
	}

	/**
	 * Does not clear previous selection
	 *
	 * @param fileName
	 */
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
	 * @param fileName
	 *            name to search for
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
		// parentWelcome.recursiveHelperSetBlockedAlso(state);
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
		desktopButton.setDisable(state);
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

	/**
	 *
	 * @param message
	 * @param subMenuDescription
	 *            can be null
	 * @return
	 */
	public ContextMenu showToastMessage(String message, @Nullable List<MenuItem> subMenuDescription) {
		ContextMenu mn = new ContextMenu();
		MenuItem mnChild;
		if (subMenuDescription == null) {
			mnChild = new MenuItem(message);
		} else {
			Menu expandableChilde = new Menu(message);
			mnChild = expandableChilde;
			expandableChilde.getItems().addAll(subMenuDescription);
		}
		mnChild.setGraphic(IconLoader.getIconImageView(ICON_TYPE.INFORMATION));
		mn.getItems().add(mnChild);
		mn.getStyleClass().addAll("toastMessage");
		mnChild.getStyleClass().addAll("toastMessage");
		Node test = table;
		double xLoc = parentWelcome.getStage().getX() + table.getWidth() * 0.1;
		double yLoc = parentWelcome.getStage().getY() + table.getHeight() + 70;
		while (test != null) {
			xLoc += test.getLayoutX();
			yLoc += test.getLayoutY();
			test = test.getParent();
		}
		if (Setting.isShowWindowOnTopWhenNotify()) {
			boolean perviousState = parentWelcome.getStage().isAlwaysOnTop();
			parentWelcome.getStage().setAlwaysOnTop(true);
			mn.show(parentWelcome.getStage(), xLoc, yLoc);
			parentWelcome.getStage().setAlwaysOnTop(perviousState);
		} else {
			mn.show(parentWelcome.getStage(), xLoc, yLoc);
		}
		mn.addEventHandler(KeyEvent.ANY, e -> {
			switch (e.getCode()) {
				case ENTER :
					mn.hide();
					break;
				default :
					break;
			}
		});
		return mn;
	}

	public void showToastNotifyExistance() {
		showToastMessage("--->> Here buddy <<---");
	}

	public ContextMenu showToastMessage(String message) {
		return showToastMessage(message, null);
	}

	public void refreshAsPathField() {
		// when doing search this cause to false navigate
		// this doesn't work with multiSelection
		// auto scroll
		// TODO
		String toScrollWhenFinish = getSelectedItem() == null ? null : getSelectedItem().getName();
		onRefreshAsPathFieldAutoScrollToName = toScrollWhenFinish;
		setPathFieldThenRefresh(getPathField().getText());
	}

	/**
	 * The use of this function is that sometimes after changing
	 * {@link TableViewModel#setNoteText(String)} the value isn't updated in the
	 * view unless something refresh the table so here will do it automatically all
	 * way reserving the old selection also <br>
	 * Also it will trigger add listener on dataTable, then a row factory if
	 * scrolled to corresponding item
	 *
	 * @see #initializeDataTableListenerTracker()
	 * @see #initializeTableRowFactory()
	 */
	protected void refreshTableWithSameData() {
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

	public void RevealINExplorer() {
		if (table.getSelectionModel().getSelectedItem() != null) {
			// https://stackoverflow.com/questions/7357969/how-to-use-java-code-to-open-windows-file-explorer-and-highlight-the-specified-f
			SystemExplorer.select(getSelectedItem().getFilePath().toFileIfLocal());
		} else {
			explorerButton.fire();
		}
	}

	public void delete() {
		List<PathLayer> source = getSelection();
		int lastKnownIndex = indexOfName(getSelectedItem().getName());
		int validIndex = sortedData.size() == 1 ? 0 : lastKnownIndex > 0 ? lastKnownIndex - 1 : lastKnownIndex + 1;
		String toSelectOnFinish = sortedData.get(validIndex).getName();
		onFinishLoadingScrollToName = toSelectOnFinish;
		FileHelper.delete(source, deletedPaths -> {
			parentWelcome.refreshUnExistingViewsDir();
		});
	}

	public void move() {
		Clipboard clipboard = Clipboard.getSystemClipboard();
		ClipboardContent content = new ClipboardContent();
		int howManyFiles = putFilesInClipboard(content);
		content.putString(MOVE_CLIPBOARD_ACTION);
		try {
			clipboard.setContent(content);
			showToastMessage("# " + howManyFiles + " Entries in to clipboard. (Ready to Move)");
		} catch (Exception e) {
			showToastMessage("Something went Wrong!! " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void copy() {
		Clipboard clipboard = Clipboard.getSystemClipboard();
		ClipboardContent content = new ClipboardContent();
		int howManyFiles = putFilesInClipboard(content);
		content.putString(COPY_CLIPBOARD_ACTION);
		try {
			clipboard.setContent(content);
			showToastMessage("# " + howManyFiles + " Entries in to clipboard. (Ready to Copy)");
		} catch (Exception e) {
			showToastMessage("Something went Wrong!! " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void open() {
		TableViewModel t = getSelectedItem();
		if (t == null) {
			return;
		} else {
			navigate(t.getFilePath());
		}
	}

	/**
	 * Put {@link #getSelection()} in ClipBoard Content given
	 *
	 * @param content
	 * @return number of files being put in ClipBoard
	 */
	private int putFilesInClipboard(ClipboardContent content) {
		List<PathLayer> selection = getSelection();
		if (mDirectory.isLocal()) {
			List<File> sources = selection.stream().filter(p -> p.isLocal()).map(p -> p.toFileIfLocal())
					.collect(Collectors.toList());
			content.putFiles(sources);
		} else {
			content.put(PathLayerHelper.DATA_FORMAT_KEY, selection);
		}
		return selection.size();
	}

	private void paste() {
		Clipboard clipboard = Clipboard.getSystemClipboard();
		List<PathLayer> sources = clipboard.getFiles().stream().map(f -> new FilePathLayer(f))
				.collect(Collectors.toList());
		if (sources.size() == 0 && clipboard.hasContent(PathLayerHelper.DATA_FORMAT_KEY)) {
			if (clipboard.getContent(PathLayerHelper.DATA_FORMAT_KEY) instanceof List) {
				@SuppressWarnings("unchecked")
				List<PathLayer> sourcesUncasted = (List<PathLayer>) clipboard
						.getContent(PathLayerHelper.DATA_FORMAT_KEY);
				sources.addAll(sourcesUncasted);
			}
		}
		if (sources.size() == 0) {
			showToastMessage("No File Detected In Clipboard");
			return;
		}
		if (clipboard.getString() == null) {
			askIsCopyOnPaste(sources);
		} else {
			switch (clipboard.getString()) {
				case COPY_CLIPBOARD_ACTION :
					finalizePasteAction(true, sources);
					break;
				case MOVE_CLIPBOARD_ACTION :
					finalizePasteAction(false, sources);
					break;
				default :
					askIsCopyOnPaste(sources);
					break;
			}
		}
	}

	private void finalizePasteAction(boolean isCopyAction, List<PathLayer> sources) {
		if (isCopyAction) {
			// check if sources are copied in same directory if yes do create copies files
			boolean isLocalSource = sources.size() > 0 ? sources.get(0).isLocal() : false;
			if (isLocalSource) {
				List<PathLayer> srcFiles = new ArrayList<>();
				List<PathLayer> targetsFiles = new ArrayList<>();
				Iterator<PathLayer> iterator = sources.iterator();
				while (iterator.hasNext()) {
					PathLayer src = iterator.next();
					if (src.getParent().equals(mDirectory.getAbsolutePath())) {
						srcFiles.add(src);
						targetsFiles.add(FileHelper.getCopyFileName(src));
						iterator.remove();
					}
				}
				if ( srcFiles.size() != 0) {
					FileHelper.copyFiles(srcFiles, targetsFiles);
				}
			}
			if (sources.size() != 0) {
				FileHelper.copy(sources, mDirectory);
			}
		} else {
			FileHelper.move(sources, mDirectory);
		}
	}

	private void askIsCopyOnPaste(List<PathLayer> sources) {
		ContextMenu mn = new ContextMenu();
		// Description Summary info Section
		String info = " # " + +sources.size() + " Entries";
		Menu allFilesInfos = new Menu("Total" + info);
		allFilesInfos.setGraphic(new ImageView(ContextMenuLook.clipboardIcon));
		StringBuilder allFilesDesc = new StringBuilder(sources.size() * 10);
		for (PathLayer pathLayer : sources) {
			allFilesDesc.append(pathLayer.getName());
			allFilesDesc.append('\n');
		}
		MenuItem summaryInfo = new MenuItem(allFilesDesc.toString());
		allFilesInfos.getItems().add(summaryInfo);
		mn.getItems().add(allFilesInfos);

		// Copy Section
		MenuItem mnChildCopy = new MenuItem("Confirm Copy");
		mnChildCopy.setOnAction(e -> finalizePasteAction(true, sources));
		mnChildCopy.setGraphic(new ImageView(ContextMenuLook.copyIcon));

		// Move Section
		MenuItem mnChildMove = new MenuItem("Confirm Cut/Move");
		mnChildMove.setOnAction(e -> finalizePasteAction(false, sources));
		mnChildMove.setGraphic(new ImageView(ContextMenuLook.moveIcon));

		mn.getItems().addAll(mnChildCopy, mnChildMove);

		Point mouse = MouseInfo.getPointerInfo().getLocation();
		mn.show(parentWelcome.getStage(), mouse.getX(), mouse.getY());
	}

	public void rename() {
		List<PathLayer> selection = getSelection();
		if (selection.size() == 1) {
			PathLayer src = selection.get(0);
			if (src.getNameCount() == 0) {
				return;
			}
			FileHelper.renameGUI(src, newPath -> {
				// scroll to renamed
				onFinishLoadingScrollToName = newPath.getName();
				FileTracker.operationUpdateAsList(Arrays.asList(src), Arrays.asList(newPath), ActionOperation.RENAME);
			});
			// refresh is satisfied by Watch service
		} else {
			new RenameUtilityController(selection);
		}
	}

	public void createDirectory() {
		FileHelper.createDirectory(getmDirectoryPath(), newName -> onFinishLoadingScrollToName = newName);
	}

	public void createFile() {
		FileHelper.createFile(getmDirectoryPath(), newName -> onFinishLoadingScrollToName = newName);
	}

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

	public void selectAllThatMatchAny(Set<String> names) {
		table.getSelectionModel().clearSelection();
		for (TableViewModel t : sortedData) {
			if (names.contains(t.getName())) {
				// select will done scroll automatically
				table.getSelectionModel().select(t);
			}
		}
	}

	// special use like for rename and do not Queue
	public void setmDirectory(PathLayer mDirectory) {
		this.mDirectory = mDirectory;
	}

	// this method is useful to handle call of changing directory
	// and enqueue it to back button..
	// and so adding old directory to queue and so on
	public void setmDirectoryThenRefresh(PathLayer mDirectory) {
		predictNavigation.setText("");
		if (mDirectory.compareTo(this.mDirectory) != 0) {
			AddToQueue(this.mDirectory);
			EmptyNextQueue();
		}
		this.mDirectory = mDirectory;
		refresh(null);
	}

	public void setMfileTracker(FileTracker mfileTracker) {
		fileTracker = mfileTracker;
	}

	public void setParentWelcome(WelcomeController parentWelcome) {
		this.parentWelcome = parentWelcome;
	}

	public void setPredictNavigation(String predictNavigation) {
		this.predictNavigation.setText(predictNavigation);
	}

	private void showContextMenu() {
		Point mouse = MouseInfo.getPointerInfo().getLocation();
		ContextMenu mn = new ContextMenu();
		mn.getItems().addAll(getContextMenuList());
		mn.show(parentWelcome.getStage(), mouse.getX(), mouse.getY());
	}

	private void showContextMenuSystem() {
		if (!getmDirectoryPath().isLocal()) {
			return;
		}
		TableViewModel t = table.getSelectionModel().getSelectedItem();
		if (t != null) {
			ArrayList<File> toShow = new ArrayList<>();
			for (TableViewModel temp : table.getSelectionModel().getSelectedItems()) {
				toShow.add(temp.getFilePath().toFileIfLocal());
			}
			RunMenu.showMenu(toShow);
		} else {
			RunMenu.showMenu(Arrays.asList(mDirectory.toFileIfLocal()));
		}
	}

	public void switchRecursive() {
		// important fire a checkbox do/un check it before calling it's action
		recursiveSearch.fire();
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

	public void saveStateToSplitState(SplitViewState state) {
		// Save current state view to state
		state.setmDirectory(getmDirectoryPath());
		state.setDirectoryViewOptions(getDirectoryViewOptions());
		state.setAutoExpandRight(isAutoExpand());

		state.setSearchKeyword(searchField.getText());

		state.setSelectedIndices(table.getSelectionModel().getSelectedIndices());
		state.setScrollTo(table.getSelectionModel().getSelectedIndex());

	}

	public void restoreSplitViewState(SplitViewState state) {
		// Restore state view to current view
		setBackQueue(state.getBackQueue());
		setNextQueue(state.getNextQueue());
		restoreSplitViewStateWithoutQueue(state);
	}

	public void restoreSplitViewStateWithoutQueue(SplitViewState state) {
		if (mDirectory.compareTo(state.getmDirectory()) != 0) {
			setmDirectoryThenRefresh(state.getmDirectory());
			// clear false change between tabs
			if (BackQueue == state.getBackQueue()) {
				RemoveLastFalseQueue();
			}
		}
		// The view options saved in state are higher priority than saved in directory
		// tracker data
		restoreDirectoryViewOptions(state.getDirectoryViewOptions());
		setAutoExpand(state.isAutoExpandRight());
		// restore search keyword
		searchField.setText(state.getSearchKeyword());

		// restore selections
		table.getSelectionModel().clearSelection();
		table.getSelectionModel().selectIndices(-1, state.getSelectedIndices());
		table.scrollTo(smartScrollIndex(state.getScrollTo()));
	}
	public DirectoryViewOptions getDirectoryViewOptions() {
		DirectoryViewOptions dirOptions = new DirectoryViewOptions();
		dirOptions.setColumnVisible(COLUMN.ICON, iconCol.isVisible());
		dirOptions.setColumnVisible(COLUMN.NAME, nameCol.isVisible());
		dirOptions.setColumnVisible(COLUMN.NOTE, noteCol.isVisible());
		dirOptions.setColumnVisible(COLUMN.SIZE, sizeCol.isVisible());
		dirOptions.setColumnVisible(COLUMN.DATE_MODIFIED, dateModifiedCol.isVisible());
		dirOptions.setColumnVisible(COLUMN.HBOX_ACTION, hBoxActionsCol.isVisible());

		int sortOrder = 0;
		for (TableColumn col : table.getSortOrder()) {
			if (col == iconCol) {
				dirOptions.setColumnSorted(COLUMN.ICON, true, iconCol.getSortType());
				dirOptions.setColumnPrioritySort(COLUMN.ICON, sortOrder);
			} else if (col == nameCol) {
				dirOptions.setColumnSorted(COLUMN.NAME, true, nameCol.getSortType());
				dirOptions.setColumnPrioritySort(COLUMN.NAME, sortOrder);
			} else if (col == noteCol) {
				dirOptions.setColumnSorted(COLUMN.NOTE, true, noteCol.getSortType());
				dirOptions.setColumnPrioritySort(COLUMN.NOTE, sortOrder);
			} else if (col == sizeCol) {
				dirOptions.setColumnSorted(COLUMN.SIZE, true, sizeCol.getSortType());
				dirOptions.setColumnPrioritySort(COLUMN.SIZE, sortOrder);
			} else if (col == dateModifiedCol) {
				dirOptions.setColumnSorted(COLUMN.DATE_MODIFIED, true, dateModifiedCol.getSortType());
				dirOptions.setColumnPrioritySort(COLUMN.DATE_MODIFIED, sortOrder);
			} else if (col == hBoxActionsCol) {
				dirOptions.setColumnSorted(COLUMN.HBOX_ACTION, true, hBoxActionsCol.getSortType());
				dirOptions.setColumnPrioritySort(COLUMN.HBOX_ACTION, sortOrder);
			}
			sortOrder++;
		}

		int columnOrder = 0;
		for (TableColumn col : table.getColumns()) {
			if (col == iconCol) {
				dirOptions.setColumnOrder(COLUMN.ICON, columnOrder);
			} else if (col == nameCol) {
				dirOptions.setColumnOrder(COLUMN.NAME, columnOrder);
			} else if (col == noteCol) {
				dirOptions.setColumnOrder(COLUMN.NOTE, columnOrder);
			} else if (col == sizeCol) {
				dirOptions.setColumnOrder(COLUMN.SIZE, columnOrder);
			} else if (col == dateModifiedCol) {
				dirOptions.setColumnOrder(COLUMN.DATE_MODIFIED, columnOrder);
			} else if (col == hBoxActionsCol) {
				dirOptions.setColumnOrder(COLUMN.HBOX_ACTION, columnOrder);
			}
			columnOrder++;
		}
		return dirOptions;
	}
	@Getter
	@Setter
	private boolean isDirOptionsChangedByCode;
	public void restoreDirectoryViewOptions(DirectoryViewOptions dirOptions) {
		/**
		 * locker to prevent listeners from saving changes to tracker data
		 *
		 * @see #onDirectoryViewOptionsChange()
		 */
		isDirOptionsChangedByCode = true;

		iconCol.setVisible(dirOptions.isColumnVisible(COLUMN.ICON));
		nameCol.setVisible(dirOptions.isColumnVisible(COLUMN.NAME));
		noteCol.setVisible(dirOptions.isColumnVisible(COLUMN.NOTE));
		sizeCol.setVisible(dirOptions.isColumnVisible(COLUMN.SIZE));
		dateModifiedCol.setVisible(dirOptions.isColumnVisible(COLUMN.DATE_MODIFIED));
		hBoxActionsCol.setVisible(dirOptions.isColumnVisible(COLUMN.HBOX_ACTION));

		ArrayList<Pair<TableColumn, Integer>> columnsSortPriority = new ArrayList<>();
		if (dirOptions.isColumnSorted(COLUMN.ICON)) {
			iconCol.setSortType(dirOptions.getColumnSortType(COLUMN.ICON));
			columnsSortPriority.add(new Pair<>(iconCol, dirOptions.getColumnPrioritySort(COLUMN.ICON)));
		}
		if (dirOptions.isColumnSorted(COLUMN.NAME)) {
			nameCol.setSortType(dirOptions.getColumnSortType(COLUMN.NAME));
			columnsSortPriority.add(new Pair<>(nameCol, dirOptions.getColumnPrioritySort(COLUMN.NAME)));
		}
		if (dirOptions.isColumnSorted(COLUMN.NOTE)) {
			noteCol.setSortType(dirOptions.getColumnSortType(COLUMN.NOTE));
			columnsSortPriority.add(new Pair<>(noteCol, dirOptions.getColumnPrioritySort(COLUMN.NOTE)));
		}
		if (dirOptions.isColumnSorted(COLUMN.SIZE)) {
			sizeCol.setSortType(dirOptions.getColumnSortType(COLUMN.SIZE));
			columnsSortPriority.add(new Pair<>(sizeCol, dirOptions.getColumnPrioritySort(COLUMN.SIZE)));
		}
		if (dirOptions.isColumnSorted(COLUMN.DATE_MODIFIED)) {
			dateModifiedCol.setSortType(dirOptions.getColumnSortType(COLUMN.DATE_MODIFIED));
			columnsSortPriority.add(new Pair<>(dateModifiedCol, dirOptions.getColumnPrioritySort(COLUMN.DATE_MODIFIED)));
		}
		if (dirOptions.isColumnSorted(COLUMN.HBOX_ACTION)) {
			hBoxActionsCol.setSortType(dirOptions.getColumnSortType(COLUMN.HBOX_ACTION));
			columnsSortPriority.add(new Pair<>(hBoxActionsCol, dirOptions.getColumnPrioritySort(COLUMN.HBOX_ACTION)));
		}

		HashMap<TableColumn, Integer> columnsOrders = new HashMap<>();
		for (TableColumn col : table.getColumns()) {
			if (col == iconCol) {
				columnsOrders.put(col, dirOptions.getColumnOrder(COLUMN.ICON));
			} else if (col == nameCol) {
				columnsOrders.put(col, dirOptions.getColumnOrder(COLUMN.NAME));
			} else if (col == noteCol) {
				columnsOrders.put(col, dirOptions.getColumnOrder(COLUMN.NOTE));
			} else if (col == sizeCol) {
				columnsOrders.put(col, dirOptions.getColumnOrder(COLUMN.SIZE));
			} else if (col == dateModifiedCol) {
				columnsOrders.put(col, dirOptions.getColumnOrder(COLUMN.DATE_MODIFIED));
			} else if (col == hBoxActionsCol) {
				columnsOrders.put(col, dirOptions.getColumnOrder(COLUMN.HBOX_ACTION));
			}
		}

		Platform.runLater(() -> {
			table.getSortOrder().clear();
			columnsSortPriority.stream().sorted(Comparator.comparingInt(colPair -> colPair.getValue())).forEach(col -> table.getSortOrder().add(col.getKey()));
			table.getColumns().sort(Comparator.comparingInt(col -> columnsOrders.get(col)));
			isDirOptionsChangedByCode = false;
		});
	}

	/**
	 * Called once on initialization
	 */
	private void startListeningToDirViewOptChanges() {
		table.getVisibleLeafColumns()
				.addListener((ListChangeListener<TableColumn<TableViewModel, ?>>) c -> onDirectoryViewOptionsChange());
		// sort order check is added to TableColumnHeader EventFilter MouseClick on
		// table
	}

	private void onDirectoryViewOptionsChange() {
		// ignore change made by restore split state or after loading tracker data
		if (isDirOptionsChangedByCode)
			return;
		fileTrackerAdapter.addDirectoryViewOptions(getDirectoryViewOptions());
	}

	/**
	 * @return the leftViewNeighbor
	 */
	@Nullable
	public SplitViewController getLeftViewNeighbor() {
		return leftViewNeighbor;
	}

	/**
	 * @param leftViewNeighbor
	 *            the leftViewNeighbor to set
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
	 * @param rightViewNeighbor
	 *            the rightViewNeighbor to set
	 */
	public void setRightViewNeighbor(SplitViewController rightViewNeighbor) {
		this.rightViewNeighbor = rightViewNeighbor;
	}

	// system dependent
	@FXML
	private void goDesktop(ActionEvent event) {
		String desktopPath = System.getProperty("user.home");
		if (SystemUtils.IS_OS_WINDOWS) {
			desktopPath += File.separator + "Desktop";
		}
		setmDirectoryThenRefresh(new FilePathLayer(new File(desktopPath)));
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
		desktopButton.setDisable(state);
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
		loader.setLocation(ResourcesHelper.getResourceAsURL("/fxml/SplitViewLeft.fxml"));
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
		loader.setLocation(ResourcesHelper.getResourceAsURL("/fxml/SplitViewRight.fxml"));
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

	/**
	 * @return the isLeft
	 */
	public boolean isLeft() {
		return isLeft;
	}

	/**
	 * @return the desktopButton
	 */
	@Nullable
	public Button getDesktopButton() {
		return desktopButton;
	}

	/**
	 * @return the leftDominate
	 */
	public Button getLeftDominate() {
		return leftDominate;
	}

	/**
	 * @return the favoritesLocations
	 */
	public MenuButton getFavoritesLocations() {
		return favoritesLocations;
	}
}
