package said.ahmad.javafx.tracker.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import said.ahmad.javafx.tracker.app.DialogHelper;
import said.ahmad.javafx.tracker.app.ResourcesHelper;
import said.ahmad.javafx.tracker.app.StringHelper;
import said.ahmad.javafx.tracker.app.ThemeManager;
import said.ahmad.javafx.tracker.fxGraphics.IntField;
import said.ahmad.javafx.tracker.model.RenameUtilityViewModel;
import said.ahmad.javafx.tracker.system.WatchServiceHelper;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.tracker.system.file.local.FilePathLayer;
import said.ahmad.javafx.tracker.system.operation.FileHelper;
import said.ahmad.javafx.tracker.system.operation.FileHelper.ActionOperation;
import said.ahmad.javafx.tracker.system.tracker.FileTracker;

public class RenameUtilityController {

	private enum MoveRemovedAction {
		None, To_Start, To_End, At
	}

	@FXML
	private SplitMenuButton DropDownActionBut;

	@FXML
	private ComboBox<MoveRemovedAction> MoveRemoved;

	@FXML
	private Button undoLastRename;
	@FXML
	private TextField searchField;

	@FXML
	private TextField InsertAtText;

	@FXML
	private CheckBox ApplyNumber;
	@FXML
	private IntField AddNumberPad;
	@FXML
	private IntField AddNumberInc;
	@FXML
	private IntField AddNumberStart;

	@FXML
	private IntField ClearKeepFirst;

	@FXML
	private IntField MoveRemovedAt;

	@FXML
	private IntField RemoveFromIndex;

	@FXML
	private TextField ReplacedText;

	// https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/Slider.html
	@FXML
	private Slider RemoveFirstSlider;

	@FXML
	private IntField RemoveToIndex;

	@FXML
	private IntField RemoveFirst;

	@FXML
	private TextField AddPrefix;

	@FXML
	private IntField InsertAtIndex;

	@FXML
	private IntField RemoveLast;

	@FXML
	private TextField AddSuffix;

	@FXML
	private TextField AddForceExtension;

	@FXML
	private TextField ReplacedWith;

	@FXML
	private Slider RemoveLastSlider;

	@FXML
	private TableColumn<RenameUtilityViewModel, CheckBox> CheckedItemColumn;
	@FXML
	private TableColumn<RenameUtilityViewModel, TextFlow> OriginalNameColumn;
	@FXML
	private TableColumn<RenameUtilityViewModel, TextFlow> NewNameColumn;
	@FXML
	private TableColumn<RenameUtilityViewModel, ImageView> IconColumn;
	@FXML
	private TableView<RenameUtilityViewModel> tableRename;

	private ObservableList<RenameUtilityViewModel> DataTable = FXCollections.observableArrayList();
	private FilteredList<RenameUtilityViewModel> filteredData;
	// used as boolean to trigger or not text listener
	private boolean isTurnOnGenerateNewNames = true;
	private Stage renameStage;
	private static Color PREFIX_COLOR = Color.rgb(255, 0, 0);
	private static Color SUFFIX_COLOR = Color.rgb(0, 127, 255);
	private static Color INSERT_COLOR = Color.rgb(127, 0, 127);
	private static Color REPLACE_COLOR = Color.rgb(255, 0, 255);
	private static Color EXTENSION_COLOR = Color.rgb(0, 0, 255);

	public static final Image RENAME_ICON_IMAGE = new Image(ResourcesHelper.getResourceAsStream("/img/rename-512.png"));

	public RenameUtilityController(List<PathLayer> sourceFiles) {
		// setup refreshing names on clicking CheckBox
		// TODO set limit of removing things
		// slider on each input
		// TODO set up/down key and scroll up down to increment field by 1
		Parent root;
		Scene scene;
		renameStage = new Stage();
		renameStage.sizeToScene();
		try {
			FXMLLoader loader = new FXMLLoader(ResourcesHelper.getResourceAsURL("/fxml/RenameUtility.fxml"));
			loader.setController(this);
			root = loader.load();
			scene = new Scene(root);
			ThemeManager.applyTheme(scene);

			renameStage.setTitle("Rename Multiple Files At once");
			renameStage.setScene(scene);
			CheckedItemColumn.setCellValueFactory(
					new PropertyValueFactory<RenameUtilityViewModel, CheckBox>("ConsiderCheckBox"));
			OriginalNameColumn
					.setCellValueFactory(new PropertyValueFactory<RenameUtilityViewModel, TextFlow>("OldName"));
			NewNameColumn.setCellValueFactory(new PropertyValueFactory<RenameUtilityViewModel, TextFlow>("NewName"));
			IconColumn.setCellValueFactory(new PropertyValueFactory<RenameUtilityViewModel, ImageView>("imgIcon"));

			renameStage.getIcons().add(RENAME_ICON_IMAGE);

			DataTable.addListener((ListChangeListener<RenameUtilityViewModel>) c -> {
				while (c.next()) {
					if (c.wasAdded()) {
						for (RenameUtilityViewModel t : c.getAddedSubList()) {
							recalculateLimitLength(t, true);
							t.getConsiderCheckBox().selectedProperty().addListener((observable, oldValue, newValue) -> {
								if (!newValue) {
									t.resetNewName();
									recalculateLimitLength(t, false);
								} else {
									recalculateLimitLength(t, true);
									generateNewNames();
								}
							});
						}
					}
				}
			});

			sourceFiles.forEach(p -> DataTable.add(new RenameUtilityViewModel(p)));
			initializeButtons();
			initializeSearchField();
			initializeTable();
			renameStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void initializeSearchField() {
		filteredData = new FilteredList<>(DataTable, p -> true);
		tableRename.setItems(filteredData);
		searchField.textProperty().addListener((observable, oldValue, newValue) -> {
			filteredData.setPredicate(model -> {
				if (searchField.getText().isEmpty()) {
					return true;
				}
				if (model.getOldName().toLowerCase().contains(searchField.getText().toLowerCase())) {
					return true;
				} else {
					return false;
				}
			});
		});
		searchField.setOnKeyPressed(e -> {
			if (e.getCode().equals(KeyCode.ESCAPE)) {
				searchField.setText("");
			}
		});

	}

	/**
	 * Used to limit length of removal process
	 *
	 * @see #forceLimitLength()
	 */
	private int MinLimitLength = Integer.MAX_VALUE;

	private void recalculateLimitLength(RenameUtilityViewModel t, boolean isNewItem) {
		if (isNewItem) {
			if (getAfterRemovalName(t).length() < MinLimitLength) {
				MinLimitLength = getAfterRemovalName(t).length();
			}
		} else {
			Optional<RenameUtilityViewModel> minT = DataTable.stream().filter(p -> p.getConsiderCheckBox().isSelected())

					.min(new Comparator<RenameUtilityViewModel>() {

						@Override
						public int compare(RenameUtilityViewModel o1, RenameUtilityViewModel o2) {
							return Integer.compare(getAfterRemovalName(o1).length(), getAfterRemovalName(o2).length());
						}
					});
			if (minT.isPresent()) {
				MinLimitLength = getAfterRemovalName(minT.get()).length();
			} else {
				MinLimitLength = Integer.MAX_VALUE;
			}
		}
	}

	String getAfterRemovalName(RenameUtilityViewModel t) {
		String temp = FilenameUtils.getBaseName(t.getOldName());
		if (!ReplacedText.getText().isEmpty()) {
			temp.replaceAll(ReplacedText.getText(), ReplacedWith.getText());
		}
		return temp;

	}

	private void initializeTable() {
		tableRename.setPlaceholder(new Label("Drag And Drop Files here To Begin!"));
		tableRename.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		NewNameColumn.maxWidthProperty().bind(tableRename.widthProperty().subtract(60).divide(2));
		OriginalNameColumn.maxWidthProperty().bind(tableRename.widthProperty().subtract(60).divide(2));
		tableRename.setOnKeyPressed(key -> {
			List<RenameUtilityViewModel> selected_items = getSelectedItems();
			switch (key.getCode()) {
			case SPACE:
				selected_items.forEach(t -> t.getConsiderCheckBox().setSelected(!t.getConsiderCheckBox().isSelected()));
				generateNewNames();
				break;

			default:
				break;
			}
		});
		tableRename.setOnDragOver(new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {
				if (event.getDragboard().hasFiles() || event.getDragboard().hasContent(DataFormat.URL)
						|| event.getDragboard().hasString()) {
					event.acceptTransferModes(TransferMode.ANY);
				}
			}
		});
		// https://stackoverflow.com/questions/32534113/javafx-drag-and-drop-a-file-into-a-program
		tableRename.setOnDragDropped(new EventHandler<DragEvent>() {

			@Override
			public void handle(DragEvent event) {
				Dragboard db = event.getDragboard();
				if (db.hasFiles()) {
					db.getFiles().forEach(file3 -> {
						DataTable.add(new RenameUtilityViewModel(new FilePathLayer(file3)));
					});
				} else if (db.hasContent(DataFormat.URL)) {
					// handle url create shortcuts
					DataTable.add(new RenameUtilityViewModel(db.getUrl()));
				} else if (db.hasContent(DataFormat.PLAIN_TEXT)) {
					Arrays.asList(db.getContent(DataFormat.PLAIN_TEXT).toString().split("\n")).forEach(e -> {
						e = e.replace("\n", "");
						DataTable.add(new RenameUtilityViewModel(e));
					});
				}
			}
		});
	}

	private void initializeButtons() {

		// Addition

		// Prefix addition
		AddPrefix.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!isTurnOnGenerateNewNames || !isValid(AddPrefix, oldValue, newValue)) {
				return;
			}
			generateNewNames();
		});

		// Suffix Addition
		AddSuffix.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!isTurnOnGenerateNewNames || !isValid(AddSuffix, oldValue, newValue)) {
				return;
			}
			generateNewNames();
		});

		// Insert Addition
		InsertAtText.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!isTurnOnGenerateNewNames || !isValid(InsertAtText, oldValue, newValue)) {
				return;
			}
			if (InsertAtIndex.getText().isEmpty()) {
				InsertAtIndex.setText("0");
			}
			generateNewNames();
		});

		// Removed / Move Addition
		MoveRemoved.setItems(FXCollections.observableArrayList(MoveRemovedAction.values()));
		MoveRemovedAt.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!isTurnOnGenerateNewNames) {
				return;
			}
			generateNewNames();
		});
		MoveRemovedAt.setVisible(false);
		MoveRemoved.setOnAction(p -> {
			if (!isTurnOnGenerateNewNames) {
				return;
			}
			generateNewNames();
			if (MoveRemoved.getValue() == MoveRemovedAction.At) {
				MoveRemovedAt.setVisible(true);
			} else {
				MoveRemovedAt.setVisible(false);
			}
		});

		InsertAtIndex.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!isTurnOnGenerateNewNames) {
				return;
			}
			generateNewNames();
		});

		// Force Extension
		AddForceExtension.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!isTurnOnGenerateNewNames) {
				return;
			}
			if (!isValid(AddForceExtension, oldValue, newValue)) {
				return;
			}
			generateNewNames();
		});

		// Removal
		// Remove First n character
		RemoveFirst.setSlider(RemoveFirstSlider);
		RemoveFirst.setMaxValue(MinLimitLength);
		RemoveFirst.valueProperty().addListener((observable, oldValue, newValue) -> {
			if (!isTurnOnGenerateNewNames) {
				return;
			}
			forceLimitLength(RemoveFirst);
			generateNewNames();
		});

		// Remove Last n character
		RemoveLast.setSlider(RemoveLastSlider);
		RemoveLast.setMaxValue(MinLimitLength);
		RemoveLast.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!isTurnOnGenerateNewNames) {
				return;
			}
			forceLimitLength(RemoveLast);
			generateNewNames();
		});
		// 1) Remove from i character
		RemoveFromIndex.setText("");
		RemoveFromIndex.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!isTurnOnGenerateNewNames) {
				return;
			}
			if (RemoveToIndex.getValue() <= RemoveFromIndex.getValue()) {
				RemoveToIndex.setValue(RemoveFromIndex.getValue());
			}
			generateNewNames();

		});
		// 2) to j character
		RemoveToIndex.setText("");
		RemoveToIndex.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!isTurnOnGenerateNewNames) {
				return;
			}
			generateNewNames();
		});

		// clear all except first n
		// ClearKeepFirst.setMaxValue(MinLimitLength);
		ClearKeepFirst.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!isTurnOnGenerateNewNames) {
				return;
			}
			generateNewNames();
		});

		// Add numbering
		ApplyNumber.setOnAction(e -> generateNewNames());
		AddNumberPad.textProperty().addListener((observable, oldValue, newValue) -> {
			generateNewNames();
		});
		AddNumberInc.textProperty().addListener((observable, oldValue, newValue) -> {
			generateNewNames();
		});
		AddNumberStart.textProperty().addListener((observable, oldValue, newValue) -> {
			generateNewNames();
		});

		// Replace
		ReplacedText.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!isTurnOnGenerateNewNames) {
				return;
			}
			if (!isValid(ReplacedText, oldValue, newValue)) {
				return;
			}
			generateNewNames();
		});

		ReplacedWith.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!isTurnOnGenerateNewNames) {
				return;
			}
			if (!isValid(ReplacedWith, oldValue, newValue)) {
				return;
			}
			generateNewNames();
		});

		// move Removed
	}

	/**
	 * Restricting length work on base name after making all replacement
	 *
	 * @see #recalculateLimitLength
	 */
	private void forceLimitLength(IntField changed) {
		// at least file name must be 0 length
		// and notify later if 1 is reached when renaming
		int remainder = MinLimitLength - 1;

		// supposing that other input is correct
		if (changed == RemoveFirst) {
			remainder -= RemoveLast.getValue();
			changed.setMaxValue(remainder);
		} else {
			remainder -= RemoveFirst.getValue();
			changed.setMaxValue(remainder);
		}
	}

	private List<RenameUtilityViewModel> getSelectedItems() {
		return tableRename.getSelectionModel().getSelectedItems();
	}

	private boolean isValid(TextField textField, String oldValue, String newValue) {
		if (newValue.isEmpty()) {
			return true;
		}
		String tempNewValue = oldValue;
		// remove special character
		tempNewValue = newValue.replaceAll("[\\\\/:*?\"<>|\r]", "");
		textField.setText(tempNewValue);
		if (!tempNewValue.equals(oldValue)) {
			return true;
		} else {
			return false;
		}
	}

	private void generateNewNames() {
		// apply in order:
		// get original text and remove 'first' 'last' 'replace' discarding extension
		// and we are sure that after removing all specified string are valid removal
		// to all names so none of operations are out of bounds (satisfied by
		// #MinLimitLength)
		// then add prefix add suffix
		// then insert text at specified index on last generated text
		int numberingI = AddNumberStart.getValue();
		for (RenameUtilityViewModel t : DataTable) {
			if (t.getConsiderCheckBox().isSelected()) {
				ArrayList<Text> allText = new ArrayList<>();
				String removedString = "";
				String restOld = FilenameUtils.getBaseName(t.getOldName());
				if (RemoveFirst.getValue() > 0) {
					int firstN = RemoveFirst.getValue();
					removedString += restOld.substring(0, firstN);
					restOld = restOld.substring(firstN);
				}
				if (RemoveLast.getValue() > 0) {
					int lastN = RemoveLast.getValue();
					removedString += restOld.substring(restOld.length() - lastN, restOld.length());
					restOld = restOld.substring(0, restOld.length() - lastN);
				}
				if (!RemoveFromIndex.getText().isEmpty() && !RemoveToIndex.getText().isEmpty()) {
					int from = RemoveFromIndex.getValue();
					int to = RemoveToIndex.getValue();
					if (from < restOld.length() - 1 && to < restOld.length() - 1 && from <= to) {
						removedString += restOld.substring(from, to + 1);
						restOld = restOld.substring(0, from) + restOld.substring(to + 1, restOld.length());
					}
				}

				if (!ReplacedText.getText().isEmpty()) {
					while (restOld.contains(ReplacedText.getText())) {
						int find = restOld.indexOf(ReplacedText.getText());
						allText.add(new Text(restOld.substring(0, find)));
						Text greenText = new Text(ReplacedWith.getText());
						greenText.setFill(REPLACE_COLOR);
						allText.add(greenText);
						restOld = restOld.substring(find + ReplacedText.getLength());
					}
				}
				if (!ClearKeepFirst.getText().isEmpty() && ClearKeepFirst.getValue() != 0) {
					int clearTill = ClearKeepFirst.getValue();
					if (clearTill < restOld.length() - 1) {
						removedString += restOld.substring(clearTill);
						restOld = restOld.substring(0, clearTill);
					}
				}

				allText.add(new Text(restOld));

				// Insert Removed copy of insert below
				if (MoveRemoved.getValue() == MoveRemovedAction.At && !removedString.isEmpty()
						&& !MoveRemovedAt.getText().isEmpty()) {
					Text insertionText = new Text(removedString);
					insertionText.setFill(INSERT_COLOR);
					int atPosition = MoveRemovedAt.getValue();
					int totalLength = allText.stream().mapToInt(m -> m.getText().length()).sum();
					if (atPosition > totalLength) {
						atPosition = totalLength;
					}
					int i = 0;
					for (Text text : allText) {
						// TODO make more test...
						if (text.getText().length() >= atPosition) {
							break;
						} else {
							atPosition -= text.getText().length();
						}
						i++;
					}
					if (atPosition == 0) {
						// that mean that string can be placed between Text
						allText.add(i, insertionText);
					} else {
						// other wise in a middle of text will split it into half
						Text removed = allText.remove(i);
						Text removed_1 = new Text(removed.getText().substring(0, atPosition));
						Text removed_2 = new Text(removed.getText().substring(atPosition));
						removed_1.setFill(removed.getFill());
						removed_2.setFill(removed.getFill());
						allText.add(i++, removed_1);
						allText.add(i++, insertionText);
						allText.add(i, removed_2);

					}
				}

				Text prefix = new Text(AddPrefix.getText());
				if (MoveRemoved.getValue() == MoveRemovedAction.To_Start) {
					prefix.setText(prefix.getText() + removedString);
				}
				if (ApplyNumber.isSelected() && !AddNumberInc.getText().isEmpty() && !AddNumberPad.getText().isEmpty()
						&& !AddNumberStart.getText().isEmpty()) {
					String temp = numberingI + "";
					numberingI += AddNumberInc.getValue();
					while (temp.length() < AddNumberPad.getValue()) {
						temp = "0" + temp;
					}
					prefix.setText(temp + prefix.getText());
				}
				prefix.setFill(PREFIX_COLOR);
				if (!prefix.getText().isEmpty()) {
					allText.add(0, prefix);
				}

				Text suffix = new Text(AddSuffix.getText());
				if (MoveRemoved.getValue() == MoveRemovedAction.To_End) {
					suffix.setText(removedString + suffix.getText());
				}
				suffix.setFill(SUFFIX_COLOR);
				if (!suffix.getText().isEmpty()) {
					allText.add(suffix);
				}

				if (!InsertAtText.getText().isEmpty() && !InsertAtIndex.getText().isEmpty()) {
					Text insertionText = new Text(InsertAtText.getText());
					insertionText.setFill(INSERT_COLOR);
					int atPosition = InsertAtIndex.getValue();
					int totalLength = allText.stream().mapToInt(m -> m.getText().length()).sum();
					if (atPosition > totalLength) {
						atPosition = totalLength;
					}
					int i = 0;
					for (Text text : allText) {
						// TODO make more test...
						if (text.getText().length() >= atPosition) {
							break;
						} else {
							atPosition -= text.getText().length();
						}
						i++;
					}
					if (atPosition == 0) {
						// that mean that string can be placed between Text
						allText.add(i, insertionText);
					} else {
						// other wise in a middle of text will split it into half
						Text removed = allText.remove(i);
						Text removed_1 = new Text(removed.getText().substring(0, atPosition));
						Text removed_2 = new Text(removed.getText().substring(atPosition));
						removed_1.setFill(removed.getFill());
						removed_2.setFill(removed.getFill());
						allText.add(i++, removed_1);
						allText.add(i++, insertionText);
						allText.add(i, removed_2);

					}
				}

				String requestedExtention = AddForceExtension.getText();
				if (!requestedExtention.trim().isEmpty()) {
					Text addExt = new Text("." + AddForceExtension.getText().trim());
					addExt.setFill(EXTENSION_COLOR);
					allText.add(addExt);
				} else {
					String extension = "";
					if (requestedExtention.length() > 0 && requestedExtention.trim().isEmpty()) {
						extension = "";
					} else {
						extension = FilenameUtils.getExtension(t.getOldName());
					}
					if (!extension.isEmpty()) {
						allText.add(new Text("." + extension));
					}
				}

				// remove leading spaces and trailing spaces
				// https://howtodoinjava.com/java/string/remove-leading-whitespaces/
				// https://howtodoinjava.com/java/string/trim-remove-trailing-spaces/
				// https://howtodoinjava.com/java/string/remove-leading-whitespaces/
				String regexboth = "\\s++$";
				String regexLeadingSpace = "^\\s+";
				String leadingString = allText.get(0).getText();
				String trailingString = allText.get(allText.size() - 1).getText();

				leadingString = leadingString.replaceFirst(regexLeadingSpace, "");
				trailingString = trailingString.replaceAll(regexboth, "");

				allText.get(0).setText(leadingString);
				allText.get(allText.size() - 1).setText(trailingString);

				// Filling text flow
				t.getNewName().getChildren().clear();
				for (Text text : allText) {
					t.getNewName().getChildren().add(text);
				}

			}
		}
	}

	private void resetAllField() {
		isTurnOnGenerateNewNames = false;
		AddForceExtension.clear();
		ReplacedText.clear();
		ReplacedWith.clear();
		AddPrefix.clear();
		AddSuffix.clear();
		InsertAtText.clear();
		InsertAtIndex.setText("0");
		MoveRemoved.setValue(MoveRemovedAction.None);
		MoveRemovedAt.setValue(0);
		RemoveFirst.setValue(0);
		RemoveLast.setValue(0);
		RemoveFromIndex.clear();
		RemoveToIndex.clear();
		ApplyNumber.setSelected(false);
		isTurnOnGenerateNewNames = true;
		ClearKeepFirst.setValue(0);
		generateNewNames();
	}

	// Needed to Redo last modification rename
	private LinkedList<HashMap<PathLayer, PathLayer>> NewToOldRename = new LinkedList<>();

	@FXML
	public void doRename() {
		boolean ans = DialogHelper.showConfirmationDialog("Confirm Rename", "Are you sure you want to apply rename?",
				"You can redo rename if anything goes wrong.");
		String warningAlert = "";
		if (ans) {
			String renameError = "";
			HashMap<PathLayer, PathLayer> currentNewToOldRename = new HashMap<>();
			List<PathLayer> sources = new ArrayList<>();
			List<PathLayer> targets = new ArrayList<>();
			boolean renameOccur = false;
			boolean isThereRealFile = false;
			// TODO conserve status seen and notes!
			WatchServiceHelper.setRuning(false);
			PathLayer oldFile = null;
			PathLayer newFile = null;

			for (RenameUtilityViewModel t : DataTable) {
				if (t.getConsiderCheckBox().isSelected()) {
					renameOccur = true;
					if (t.getPathFile() == null) {
						// this is a text type item
						t.setOldName(t.getNewNameAsString());
					} else {
						// this is a real file
						try {
							oldFile = t.getPathFile();
							newFile = t.getPathFile().resolveSibling(StringHelper.textFlowToString(t.getNewName()));
							FileHelper.renameHelper(oldFile, newFile);
							t.setPathFile(newFile);
							t.setOldName(newFile.getName());
							isThereRealFile = true;
							currentNewToOldRename.put(newFile, oldFile);
							sources.add(oldFile);
							targets.add(newFile);
						} catch (IOException e) {
							renameError += t.getPathFile().getName() + " --> "
									+ StringHelper.textFlowToString(t.getNewName()) + "\n";
							warningAlert += e.getClass() + ": " + e.getMessage() + "\n";
							e.printStackTrace();
						}
					}
				}
			}
			WatchServiceHelper.setRuning(true);
			if (!renameOccur) {
				return;
			}
			try {
				// just to refresh the views as this call a watch key
				FileTracker.operationUpdateAsList(sources, targets, ActionOperation.RENAME);
				FileHelper.renameHelper(newFile, oldFile);
				FileHelper.renameHelper(oldFile, newFile);
			} catch (IOException e) {
				e.printStackTrace();
			}

			tableRename.refresh();
			resetAllField();
			recalculateLimitLength(
					DataTable.stream().filter(p -> p.getConsiderCheckBox().isSelected()).findFirst().get(), false);

			if (!isThereRealFile) {
				return;
			}
			// specific to real file only
			NewToOldRename.add(currentNewToOldRename);
			undoLastRename.setDisable(false);
			if (!renameError.isEmpty()) {
				DialogHelper.showExpandableAlert(AlertType.ERROR, "Confirm Rename",
						"Some Content were not renamed Successfully!",
						"This may be caused by illegal character or redundant names. \n",
						warningAlert + "\nSource File --> Rename expected:\n" + renameError);
			}

		}
	}

	@FXML
	public void copyNewRenamedCol() {
		String myString = "";
		int i = 0;
		for (RenameUtilityViewModel t : DataTable) {
			if (t.getConsiderCheckBox().isSelected()) {
				String item = StringHelper.textFlowToString(t.getNewName()) + "\n";
				myString += item;
				i++;
			}
		}
		Clipboard clipboard = Clipboard.getSystemClipboard();
		ClipboardContent content = new ClipboardContent();
		content.putString(myString);
		clipboard.setContent(content);
		DialogHelper.showExpandableAlert(AlertType.INFORMATION, "Copy New Names",
				"Content Copied Successfully to Clipboard\n-----   " + i + " ----- Items Added", "", myString);
	}

	@FXML
	public void getMissingSequence() {
		String myString = "";
		Pattern p = Pattern.compile("-?\\d+");
		Matcher m;
		HashSet<Integer> allSeq = new HashSet<>();
		HashMap<String, String> interval = DialogHelper.showMultiTextInputDialog("Detect Missing Sequence #NB",
				"Enter start and end of your Sequence",
				"you will get a list of missing number from new renamed names\n"
						+ "[Removing every integer found in name].",
				new String[] { "Start", "End" }, new String[] { "1", "45" }, 1);
		if (interval == null) {
			return;
		}
		int min = 0;
		int max = 0;
		try {
			min = Integer.parseInt(interval.get("Start"));
			max = Integer.parseInt(interval.get("End"));
			if (min >= max) {
				DialogHelper.showAlert(AlertType.ERROR, "Detect Missing Sequence #NV", "Invalid Interval",
						"The Start must be bigger than end!");
				getMissingSequence();
				return;
			}
		} catch (Exception e) {
			DialogHelper.showAlert(AlertType.ERROR, "Detect Missing Sequence #NV", "Invalid Number Format",
					"The input Fields must be integers Values");
			getMissingSequence();
			return;
		}
		for (int i = min; i <= max; i++) {
			allSeq.add(i);
		}
		// do count useful items that can fill intervals later use
		int allCurNBItems = 0;
		HashMap<Integer, Integer> couldNotBePrecised = new HashMap<>();
		for (RenameUtilityViewModel t : DataTable) {
			if (t.getConsiderCheckBox().isSelected()) {
				m = p.matcher(StringHelper.textFlowToString(t.getNewName()));

				boolean counted = false;
				Integer firstCounted = null;
				ArrayList<Integer> recentFoundAlreadyRemoved = new ArrayList<>();
				while (m.find()) {
					int nbTemp = Math.abs(Integer.parseInt(m.group()));
					if (allSeq.remove(nbTemp)) {
						if (firstCounted == null) {
							firstCounted = nbTemp;
						} else {
							// contain more than one sequence number
							Integer countfirst = couldNotBePrecised.get(firstCounted);
							Integer countTemp = couldNotBePrecised.get(nbTemp);
							couldNotBePrecised.put(firstCounted, countfirst == null ? 1 : countfirst + 1);
							couldNotBePrecised.put(nbTemp, countTemp == null ? 1 : countTemp + 1);
						}
						if (!counted) {
							allCurNBItems++;
							counted = true;
						}
					} else if (nbTemp >= min && nbTemp <= max) {
						// mean item contain number in interval but it is already removed
						if (!counted) {
							allCurNBItems++;
							counted = true;
						}
						recentFoundAlreadyRemoved.add(nbTemp);
					}
				}
				for (Integer integer : recentFoundAlreadyRemoved) {
					Integer count = couldNotBePrecised.get(integer);
					couldNotBePrecised.put(integer, count == null ? 1 : count + 1);
				}
				recentFoundAlreadyRemoved.clear();
			}
		}
		if (allSeq.size() != 0) {
			myString += "Missing Items:\n";
		}
		for (Integer integer : allSeq) {
			myString += integer + "\n";
		}
		// missDetection occur when a name contain 2 or more valid numbers within
		// intervals
		// like hello12fe43.mp4 will cover 12 and 43 while being 1 file!
		// in other word all number should equal to
		// max - min + 1 = remaining + all name number found
		int missDetection = max - min + 1 - allSeq.size() - allCurNBItems;
		// if missDetection is negative mean that we have more items fitting in
		// intervals than expected
		String warning = "";
		if (missDetection < 0) {
			warning += "Where there are -----   " + -missDetection + " -----   Excceded items.\n"
					+ "You Are Selecting too much files Man :) !!";
			missDetection = 0;
		}
		if (missDetection > 0) {
			warning = "Where -----   " + missDetection + "  ----- Item(s) could not be Precised.\n"
					+ " Human check is indeed, or Do more Filtering of Names.";
			myString += " ----" + missDetection + "---- item of " + couldNotBePrecised.size()
					+ " are missing from the list Need Human Check:\n";
			myString += "Number \t | Times Found\n";
			int i = 0;
			for (Integer miss : couldNotBePrecised.keySet().stream()
					.sorted((k1, k2) -> -couldNotBePrecised.get(k1) + couldNotBePrecised.get(k2))
					.collect(Collectors.toList())) {
				myString += miss + "\t\t | \t" + couldNotBePrecised.get(miss) + "\n";
				i++;
				if (i == missDetection) {
					myString += "--------Losers--------\n";
				}
			}
		}
		int totalMissing = allSeq.size() + missDetection;
		DialogHelper.showExpandableAlert(
				AlertType.INFORMATION, "Detect Missing Sequence #NB", "There Are \n-----   " + totalMissing
						+ " ----- Missing items ----" + allCurNBItems + "---- can be Matched\n" + warning,
				"", myString);

	}

	@FXML
	public void undoRename() {
		String summary = "This action is used when wrongly renamed."
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
		HashMap<PathLayer, PathLayer> lastNewToOldRename = NewToOldRename.peekLast();
		PathLayer workingDir = lastNewToOldRename.get(lastNewToOldRename.keySet().toArray()[0]).getParentPath();
		int i = 0;
		int found = 0;
		for (PathLayer newPath : lastNewToOldRename.keySet()) {
			if (newPath.exists()) {
				changeReportFiles += "*R" + (i + 1) + "- " + newPath.getName() + " --> "
						+ lastNewToOldRename.get(newPath).getName() + "\n";
				found++;
			} else {
				notFoundFiles += "*N" + (i + 1) + "- " + newPath.getName() + " !!->"
						+ lastNewToOldRename.get(newPath).getName() + "\n";
			}
			i++;
		}
		if (!notFoundFiles.isEmpty()) {
			notFoundFiles = "Not Found Files:\n" + notFoundFiles;
		}
		boolean ans = DialogHelper.showExpandableConfirmationDialog(title,
				"Preview Change" + "\nPending -----   " + found + " ----- Changes Pair Rename"
						+ (i - found != 0 ? "\nNot Found -----   " + (i - found) + " ----- Files (renamed or moved)\n"
								: ""),
				summary,
				"Working in:  " + workingDir + "\n\nModifications:\n" + changeReportFiles + "\n" + notFoundFiles);
		if (ans) {
			i = 0;
			String renameError = "";
			String warningAlert = "";
			HashMap<PathLayer, RenameUtilityViewModel> pathToTableView = new HashMap<>();
			for (RenameUtilityViewModel t : DataTable) {
				if (lastNewToOldRename.containsKey(t.getPathFile())) {
					pathToTableView.put(t.getPathFile(), t);
				}
			}
			// new to old rename process
			for (PathLayer newPath : lastNewToOldRename.keySet()) {
				try {
					if (newPath.exists()) {
						PathLayer oldPath = lastNewToOldRename.get(newPath);
						oldPath.move(newPath);
						if (pathToTableView.containsKey(newPath)) {
							pathToTableView.get(newPath).setPathFile(oldPath);
							pathToTableView.get(newPath).setOldName(oldPath.getName().toString());
						}
					}
				} catch (IOException e) {
					renameError += newPath.getName() + " -->" + lastNewToOldRename.get(newPath).getName() + "\n";
					warningAlert += e.getClass() + ": " + e.getMessage() + "\n";
					e.printStackTrace();
				}
				i++;
			}
			NewToOldRename.removeLast();
			tableRename.refresh();
			resetAllField();
			if (NewToOldRename.size() == 0) {
				undoLastRename.setDisable(true);
			}
			if (!renameError.isEmpty()) {
				DialogHelper.showExpandableAlert(AlertType.ERROR, title, "Some Content were not renamed Successfully!",
						"This may be caused by illegal character names. \n",
						warningAlert + "\nSource File --> Rename expected:\n" + renameError);
			}

		}

	}

	@FXML
	private void addTextFromClipBoard() {
		String myString = Clipboard.getSystemClipboard().getString();
		Arrays.asList(myString.split("\n")).forEach(e -> {
			DataTable.add(new RenameUtilityViewModel(e));
		});
	}

	@FXML
	private void selectAllItems() {
		if (filteredData.size() != 0 && filteredData.get(0).getConsiderCheckBox().isSelected()) {
			filteredData.forEach(e -> e.getConsiderCheckBox().setSelected(false));
		} else {
			filteredData.forEach(e -> e.getConsiderCheckBox().setSelected(true));
		}
	}

}
