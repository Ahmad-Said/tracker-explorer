package application.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;

import application.DialogHelper;
import application.FileHelper;
import application.IntField;
import application.Main;
import application.StringHelper;
import application.WindowsExplorerComparator;
import application.model.RenameUtilityViewModel;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class RenameUtilityController {

	private enum MoveRemovedAction {
		None, To_Start, To_End, At
	}

	@FXML
	private ComboBox<MoveRemovedAction> MoveRemoved;

	@FXML
	private Button redoLastRename;
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
	private TableColumn<RenameUtilityViewModel, String> OriginalNameColumn;
	@FXML
	private TableColumn<RenameUtilityViewModel, TextFlow> NewNameColumn;
	@FXML
	private TableColumn<RenameUtilityViewModel, ImageView> IconColumn;
	@FXML
	private TableView<RenameUtilityViewModel> tableRename;

	private ObservableList<RenameUtilityViewModel> DataTable = FXCollections.observableArrayList();

	private Stage renameStage;
	private static Color PREFIX_COLOR = Color.rgb(255, 0, 0);
	private static Color SUFFIX_COLOR = Color.rgb(0, 127, 255);
	private static Color INSERT_COLOR = Color.rgb(127, 0, 127);
	private static Color REPLACE_COLOR = Color.rgb(255, 0, 255);
	private static Color EXTENSION_COLOR = Color.rgb(0, 0, 255);

	public RenameUtilityController(List<Path> sourceFiles) {
		// setup refreshing names on clicking CheckBox
		// TODO set limit of removing things
		// slider on each input
		// TODO set up/down key and scroll up down to increment field by 1
		Parent root;
		Scene scene;
		renameStage = new Stage();
		renameStage.sizeToScene();
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RenameUtility.fxml"));
			loader.setController(this);
			root = loader.load();
			scene = new Scene(root);
			scene.getStylesheets().add("/css/bootstrap3.css");

			renameStage.setTitle("Rename Multiple Files At once");
			renameStage.setScene(scene);
			CheckedItemColumn.setCellValueFactory(
					new PropertyValueFactory<RenameUtilityViewModel, CheckBox>("ConsiderCheckBox"));
			OriginalNameColumn.setCellValueFactory(new PropertyValueFactory<RenameUtilityViewModel, String>("OldName"));
			NewNameColumn.setCellValueFactory(new PropertyValueFactory<RenameUtilityViewModel, TextFlow>("NewName"));
			IconColumn.setCellValueFactory(new PropertyValueFactory<RenameUtilityViewModel, ImageView>("imgIcon"));
			tableRename.setItems(DataTable);

			renameStage.getIcons().add(new Image(Main.class.getResourceAsStream("/img/rename-512.png")));

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

			scene.addEventFilter(ScrollEvent.SCROLL, (ScrollEvent event) -> {
				if (!tableRename.isHover() && scene.focusOwnerProperty().get() instanceof IntField) {
					IntField focusedTextField = (IntField) scene.focusOwnerProperty().get();
					double deltaY = event.getDeltaY();
					if (deltaY > 0) {
						focusedTextField.incrementByOne();
					} else {
						focusedTextField.decrementByOne();
					}
				}
			});
			sourceFiles.forEach(p -> DataTable.add(new RenameUtilityViewModel(p)));
			initializeButtons();
			initializeTable();
			renameStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}

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
		NewNameColumn.setComparator(new Comparator<TextFlow>() {

			@Override
			public int compare(TextFlow o1, TextFlow o2) {
				return new WindowsExplorerComparator().compare(o1.toString(), o2.toString());
			}
		});
		OriginalNameColumn.setComparator(new WindowsExplorerComparator());
	}

	private void initializeButtons() {

		// Addition

		// Prefix addition
		AddPrefix.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!isValid(AddPrefix, oldValue, newValue)) {
				return;
			}
			generateNewNames();
		});

		// Suffix Addition
		AddSuffix.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!isValid(AddSuffix, oldValue, newValue)) {
				return;
			}
			generateNewNames();
		});

		// Insert Addition
		InsertAtText.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!isValid(InsertAtText, oldValue, newValue)) {
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
			generateNewNames();
		});
		MoveRemovedAt.setVisible(false);
		MoveRemoved.setOnAction(p -> {
			generateNewNames();
			if (MoveRemoved.getValue() == MoveRemovedAction.At) {
				MoveRemovedAt.setVisible(true);
			} else {
				MoveRemovedAt.setVisible(false);
			}
		});

		addGestureIncrement(InsertAtIndex);
		InsertAtIndex.textProperty().addListener((observable, oldValue, newValue) -> {
			generateNewNames();
		});

		// Force Extension
		AddForceExtension.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!isValid(AddForceExtension, oldValue, newValue)) {
				return;
			}
			generateNewNames();
		});

		// Removal
		// Remove First n character
		RemoveFirst.setSlider(RemoveFirstSlider);
		RemoveFirst.setMaxValue(MinLimitLength);
		addGestureIncrement(RemoveFirst);
		RemoveFirst.valueProperty().addListener((observable, oldValue, newValue) -> {
			forceLimitLength(RemoveFirst);
			generateNewNames();
		});

		// Remove Last n character
		RemoveLast.setSlider(RemoveLastSlider);
		RemoveLast.setMaxValue(MinLimitLength);
		addGestureIncrement(RemoveLast);
		RemoveLast.textProperty().addListener((observable, oldValue, newValue) -> {
			forceLimitLength(RemoveLast);
			generateNewNames();
		});
		// 1) Remove from i character
		addGestureIncrement(RemoveFromIndex);
		RemoveFromIndex.setText("");
		RemoveFromIndex.textProperty().addListener((observable, oldValue, newValue) -> {
			if (RemoveToIndex.getValue() <= RemoveFromIndex.getValue()) {
				RemoveToIndex.setValue(RemoveFromIndex.getValue());
			}
			generateNewNames();

		});
		// 2) to j character
		addGestureIncrement(RemoveToIndex);
		RemoveToIndex.setText("");
		RemoveToIndex.textProperty().addListener((observable, oldValue, newValue) -> {
			generateNewNames();
		});

		// clear all except first n
//		ClearKeepFirst.setMaxValue(MinLimitLength);
		addGestureIncrement(ClearKeepFirst);
		ClearKeepFirst.textProperty().addListener((observable, oldValue, newValue) -> {
			generateNewNames();
		});

		// Add numbering
		addGestureIncrement(AddNumberStart);
		addGestureIncrement(AddNumberInc);
		addGestureIncrement(AddNumberPad);
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
			if (!isValid(ReplacedText, oldValue, newValue)) {
				return;
			}
			generateNewNames();
		});

		ReplacedWith.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!isValid(ReplacedWith, oldValue, newValue)) {
				return;
			}
			generateNewNames();
		});

		// move Removed
		addGestureIncrement(MoveRemovedAt);
	}

	private void addGestureIncrement(IntField textFieldInteger) {
		textFieldInteger.setOnKeyPressed(key -> {
			switch (key.getCode()) {
			// leaved for navigation
			case UP:
				textFieldInteger.incrementByOne();
				break;
			case DOWN:
				textFieldInteger.decrementByOne();
				break;
			default:
				break;
			}
		});
	}

	/**
	 * Restricting length work on base name after making all replacement
	 *
	 * @see #recalculateLimitLength
	 */
	private void forceLimitLength(IntField changed) {
		// at least file name must be 1 length
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
				if (ClearKeepFirst.getValue() != 1) {
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

				if (!AddForceExtension.getText().trim().isEmpty()) {
					Text addExt = new Text("." + AddForceExtension.getText().trim());
					addExt.setFill(EXTENSION_COLOR);
					allText.add(addExt);
				} else {
					String extension = FilenameUtils.getExtension(t.getOldName());
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
		generateNewNames();
	}

	// Needed to Redo last modification rename
	private LinkedList<HashMap<Path, Path>> NewToOldRename = new LinkedList<>();

	@FXML
	public void doRename() {
		boolean ans = DialogHelper.showConfirmationDialog("Confirm Rename", "Are you sure you want to apply rename?",
				"You can redo rename if anything goes wrong.");
		String warningAlert = "";
		if (ans) {
			String renameError = "";
			HashMap<Path, Path> currentNewToOldRename = new HashMap<>();
			boolean renameOccur = false;
			for (RenameUtilityViewModel t : DataTable) {
				if (t.getConsiderCheckBox().isSelected()) {
					renameOccur = true;
					try {
						Path oldFile = t.getPathFile();
						Path newFile = t.getPathFile().resolveSibling(StringHelper.textFlowToString(t.getNewName()));
						FileHelper.RenameHelper(oldFile, newFile);
						t.setPathFile(newFile);
						t.setOldName(newFile.getFileName().toString());
						currentNewToOldRename.put(newFile, oldFile);
					} catch (IOException e) {
						renameError += t.getPathFile().getFileName() + " --> "
								+ StringHelper.textFlowToString(t.getNewName()) + "\n";
						warningAlert += e.getClass() + ": " + e.getMessage() + "\n";
						e.printStackTrace();
					}
				}
			}
			if (!renameOccur) {
				return;
			}
			tableRename.refresh();
			resetAllField();
			recalculateLimitLength(
					DataTable.stream().filter(p -> p.getConsiderCheckBox().isSelected()).findFirst().get(), false);

			NewToOldRename.add(currentNewToOldRename);
			redoLastRename.setDisable(false);
			if (!renameError.isEmpty()) {
				DialogHelper.showExpandableAlert(AlertType.ERROR, "Confirm Rename",
						"Some Content were not renamed Successfully!",
						"This may be caused by illegal character or redundant names. \n",
						warningAlert + "\nSource File --> Rename expected:\n" + renameError);
			}

		}
	}

	@FXML
	public void redoRename() {
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
			HashMap<Path, RenameUtilityViewModel> pathToTableView = new HashMap<>();
			for (RenameUtilityViewModel t : DataTable) {
				if (lastNewToOldRename.containsKey(t.getPathFile())) {
					pathToTableView.put(t.getPathFile(), t);
				}
			}
			// new to old rename process
			for (Path newPath : lastNewToOldRename.keySet()) {
				try {
					if (newPath.toFile().exists()) {
						Path oldPath = lastNewToOldRename.get(newPath);
						Files.move(newPath, oldPath);
						if (pathToTableView.containsKey(newPath)) {
							pathToTableView.get(newPath).setPathFile(oldPath);
							pathToTableView.get(newPath).setOldName(oldPath.getFileName().toString());
						}
					}
				} catch (IOException e) {
					renameError += newPath.getFileName() + " -->" + lastNewToOldRename.get(newPath).getFileName()
							+ "\n";
					warningAlert += e.getClass() + ": " + e.getMessage() + "\n";
					e.printStackTrace();
				}
				i++;
			}
			NewToOldRename.removeLast();
			tableRename.refresh();
			resetAllField();
			if (NewToOldRename.size() == 0) {
				redoLastRename.setDisable(true);
			}
			if (!renameError.isEmpty()) {
				DialogHelper.showExpandableAlert(AlertType.ERROR, title, "Some Content were not renamed Successfully!",
						"This may be caused by illegal character names. \n",
						warningAlert + "\nSource File --> Rename expected:\n" + renameError);
			}

		}

	}
}
