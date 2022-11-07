package said.ahmad.javafx.tracker.controller.setting;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import javafx.application.Platform;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import said.ahmad.javafx.fxGraphics.IntField;
import said.ahmad.javafx.tracker.app.DialogHelper;
import said.ahmad.javafx.tracker.app.Main;
import said.ahmad.javafx.tracker.app.ResourcesHelper;
import said.ahmad.javafx.tracker.app.StringHelper;
import said.ahmad.javafx.tracker.app.look.IconLoader;
import said.ahmad.javafx.tracker.app.pref.Setting;
import said.ahmad.javafx.tracker.app.pref.UserContextMenuDefaultSetting;
import said.ahmad.javafx.tracker.controller.PhotoViewerController;
import said.ahmad.javafx.tracker.controller.UserContextMenuController;
import said.ahmad.javafx.tracker.controller.setting.base.GenericSettingController;
import said.ahmad.javafx.tracker.datatype.UserContextMenu;
import said.ahmad.javafx.tracker.fxGraphics.UserContextMenuCellFactory;
import said.ahmad.javafx.tracker.system.call.*;
import said.ahmad.javafx.tracker.system.call.inner.InnerFunctionCall;
import said.ahmad.javafx.tracker.system.call.inner.InnerFunctionName;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.tracker.system.file.local.FilePathLayer;
import said.ahmad.javafx.util.CallBackVoid;

public class ContextMenuSettingController extends GenericSettingController {

	@FXML
	private ScrollPane pane;

	@FXML
	private SplitPane splitPane;

	@FXML
	private HBox menuSelectionPane;

	@FXML
	private MenuItem defineEmptyMenu;

	@FXML
	private MenuItem defineDuplicateMenu;

	@FXML
	private MenuItem defineFromTemplateMenu;

	@FXML
	private ListView<UserContextMenu> templateListView;

	private final ObservableList<UserContextMenu> menuListTemplateData = FXCollections.observableArrayList();

	@FXML
	private MenuItem defineFromInnerFunctionMenu;

	@FXML
	private ListView<InnerFunctionName> innerFunctionListView;

	private final ObservableList<InnerFunctionName> innerFunctionData = FXCollections.observableArrayList();

	@FXML
	private Button removeSelectedMenu;

	@FXML
	private ListView<UserContextMenu> definedMenusListView;

	private final ObservableList<UserContextMenu> menuListUserData = FXCollections.observableArrayList();

	@FXML
	private CheckBox activateMenuCheckBox;

	@FXML
	private IntField menuOrderInput;

	@FXML
	private VBox executablePane;

	@FXML
	private TextField executablePathInput;

	@FXML
	private MenuButton browseMenuButton;

	@FXML
	private MenuItem browseFromSystemMenu;

	@FXML
	private Label evaluatedExecutablePreview;

	@FXML
	private Label executablePathError;

	@FXML
	private TitledPane extensionTitledPane;

	@FXML
	private VBox extensionPane;

	@FXML
	private Label extensionHeaderError;

	@FXML
	private TextArea extensionsListInputText;

	@FXML
	private CheckBox isDirectoryContextCheckBox;

	@FXML
	private Button defineExtensionGroup;

	@FXML
	private Button removeExtensionGroup;

	@FXML
	private ListView<CheckBox> extensionGroupsListView;

	/**
	 * Designate all groups defined by user for all menus <br>
	 * it use generated id of group as key <br>
	 * extension list as value <br>
	 * 
	 * @see Setting#getExtensionGroups()
	 */
	private final ObservableMap<Long, ArrayList<String>> extensionGroupsSetting = FXCollections.observableHashMap();

	/**
	 * Generated id for each group name that replace way of saving data in context
	 * menu.<br>
	 * By using id of the group we can change its name without searching for its
	 * usage in other context menu.<br>
	 * At the end we convert back from id to group name<br>
	 * if id found -> get the new name<br>
	 * if id not found -> remove the group association<br>
	 * Note: negative id designate excluded group, it have the same name of positive
	 * id, negative id aren't added to maps but it can exist in groupNameList of
	 * context menu
	 */
	private final BiMap<Long, String> idToGroupName = HashBiMap.create();
	private long generatedGroupId = 1;

	/**
	 * Designate list of checkbox of all defined extensions by user with a boolean
	 * value reflecting if is a compatible group extension with current menu
	 *
	 * @see UserContextMenu#getExtensionsGroupNames()
	 */
	private final ObservableList<CheckBox> extensionGroupsMenuData = FXCollections.observableArrayList();

	/**
	 * Designate an exact copy of {@link #extensionGroupsMenuData} using mapping of
	 * group id. This will make more efficient to reach checkbox without looping
	 * observable data to search for key. <br>
	 * adding value to this map outside listener of observable
	 * extensionGroupsMenuData is forbidden
	 */
	private final Map<Long, CheckBox> extensionGroupsMenuDataMap = new HashMap<>();

	@FXML
	private TextArea extensionsListGroupInputText;

	@FXML
	private Label targetGroupName;

	/**
	 * change the target group name
	 */
	@FXML
	private TextField groupNameInput;

	@FXML
	private Label extensionsNameGroupError;

	@FXML
	private TitledPane menuNameTitledPane;

	@FXML
	private VBox menuNamePane;

	@FXML
	private Label menuNameHeaderError;

	@FXML
	private CheckBox isOnSingleFileCheckBox;

	@FXML
	private TextField aliasInput;

	@FXML
	private MenuButton insertAliasCommand;

	@FXML
	private Label aliasNamePreview;

	@FXML
	private CheckBox isOnMultipleFileCheckBox;

	@FXML
	private TextField aliasMultipleInput;

	@FXML
	private MenuButton insertAliasMultipleCommand;

	@FXML
	private Label aliasMultipleNamePreview;

	@FXML
	private TextField parentMenuNamesInput;

	@FXML
	private MenuButton insertParentMenuName;

	@FXML
	private MenuButton parentMenuNamesPreview;

	@FXML
	private Label parentMenuNamesError;

	@FXML
	private TitledPane iconTitledPane;

	@FXML
	private VBox iconPane;

	@FXML
	private Label iconHeaderError;

	@FXML
	private CheckBox isIconUsingExecutable;

	@FXML
	private TextField iconPathInput;

	@FXML
	private MenuButton browseIconPath;

	@FXML
	private MenuItem InnerIconMenu;

	@FXML
	private ImageView iconImagePreview;

	@FXML
	private CheckBox isParentIconUsingExecutable;

	@FXML
	private TextArea parentIconPathInput;

	@FXML
	private MenuButton browseParentIconPath;

	@FXML
	private MenuItem InnerParentIconMenu;

	@FXML
	private Button parentIconImagePreview;

	@FXML
	private TitledPane callMethodTitledPane;

	@FXML
	private VBox callMethodPane;

	@FXML
	private Label callMethodHeaderError;

	@FXML
	private Label callMethodHeaderWarning;

	@FXML
	private TextField prefixCommandInput;

	@FXML
	private MenuButton insertPrefixCommand;

	@FXML
	private Label prefixCommandError;

	@FXML
	private TextField postfixCommandInput;

	@FXML
	private MenuButton insertPostfixCommand;

	@FXML
	private Label postfixCommandError;

	private ToggleGroup activeCommandMethodToggle;
	@FXML
	private RadioButton isSeparateCallMethod;

	@FXML
	private RadioButton isCombinedCallMethod;

	@FXML
	private RadioButton isTextFileCallMethod;

	@FXML
	private CheckBox isCallUsingRelatifPath;

	@FXML
	private TextArea callCommandPreview;

	private UserContextMenu currentContextMenu;

	private ArrayList<Label> allErrorLabel;

	/**
	 * Temporary list just for generating preview example
	 */
	private List<PathLayer> examplePathLayers = new ArrayList<>(
			Arrays.asList(new FilePathLayer(new File("C:/Parent Directory/First File.txt")),
					new FilePathLayer(new File("C:/Parent Directory/Second File.txt")),
					new FilePathLayer(new File("C:/Parent Directory/Third File.txt"))));

	/**
	 * Temporary list just for generating preview example
	 */
	private List<PathLayer> chosenPathLayers;

	private List<PathLayer> getSamplePathList() {
		if (chosenPathLayers != null && chosenPathLayers.size() > 0)
			return chosenPathLayers;
		return examplePathLayers;
	}

    @Override
    public String getTitle() {
        return "Context Menu";
    }

    @Override
    public @Nullable Image getIconImage() {
		return IconLoader.getIconImage(IconLoader.ICON_TYPE.CONTEXT_MENU);
    }

    @Override
    public FXMLLoader loadFXML() throws IOException {
        FXMLLoader loader = new FXMLLoader();
		loader.setLocation(ResourcesHelper.getResourceAsURL("/fxml/setting/ContextMenuSetting.fxml"));
        loader.setController(this);
        loader.load();
        return loader;
    }

    @Override
    public void initializeNodes() {

		// empty all error/warning text
		allErrorLabel = new ArrayList<>();
		// Menu selection section
		initializeMenuSelectionNodes();

		// executable section
		initializeExecutableNodes();

		// extension section
		initializeExtensionsNodes();

		// Menu Name section
		initializeMenuNameNodes();

		// Icon section
		initializeIconNodes();

		// call method section
		initializeCallMethodNodes();

		// clear all error labels
		for (Label label : allErrorLabel) {
			label.setText(null);
		}
		// disable all panes and clear input
		clearCurrentContextMenu();
	}
	private void initializeMenuSelectionNodes() {
		activateMenuCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
			if (currentContextMenu != null)
				currentContextMenu.setActive(newValue);
		});

		menuOrderInput.valueProperty().addListener((observable, oldValue, newValue) -> {
			if (currentContextMenu != null)
				currentContextMenu.setMenuOrder((Integer) newValue);
		});
	}

	@FXML
	public void defineNewMenu() {
		clearCurrentContextMenu();
		UserContextMenu userContextMenu = new UserContextMenu();
		userContextMenu.setCallMethod(CallMethod.SEPARATE_CALL);
		userContextMenu.setOnSingleSelection(true);
		userContextMenu.setAlias("New Menu");
		userContextMenu.getExtensions().add("txt");
		userContextMenu.setParentMenuNames("Open With");
		addMenuToUserData(userContextMenu, true);
	}

	@FXML
	public void duplicateSelectedMenu() {
		if (currentContextMenu == null)
			return;
		UserContextMenu userContextMenu = null;
		int insertIndex = 0;
		try {
			userContextMenu = (UserContextMenu) currentContextMenu.clone();
			insertIndex = definedMenusListView.getItems().indexOf(currentContextMenu) + 1;
			rollBackContextMenuGroupName(userContextMenu);
			menuListUserData.add(insertIndex, userContextMenu);
			definedMenusListView.getSelectionModel().select(userContextMenu);
			definedMenusListView.scrollTo(insertIndex - 1);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void defineMenuFromSelectedTemplate() {
		UserContextMenu templateMenu = templateListView.getSelectionModel().getSelectedItem();
		if (templateMenu == null)
			return;

		UserContextMenu userContextMenu = null;
		try {
			userContextMenu = (UserContextMenu) templateMenu.clone();
			menuListUserData.add(templateMenu);
			definedMenusListView.getSelectionModel().select(templateMenu);
			definedMenusListView.scrollTo(templateMenu);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void defineFromSelectedInnerFunction() {
		InnerFunctionName selectedFunction = innerFunctionListView.getSelectionModel().getSelectedItem();
		if (selectedFunction == null) {
			return;
		}
		currentContextMenu = InnerFunctionCall.FUNCTION_CALLS.get(selectedFunction).createDefaultUserContextMenu();
		addMenuToUserData(currentContextMenu, true);
	}

	@FXML
	public void removeSelectedMenu() {
		if (currentContextMenu == null) {
			return;
		}
		menuListUserData.remove(currentContextMenu);
	}

	private void initializeExecutableNodes() {
		evaluatedExecutablePreview.setText(null);
		allErrorLabel.add(executablePathError);

		/**
		 * evaluate path on path input change Display possible error such as not exist
		 * and is directory Map on mouse click to reveal in explorer
		 */
		executablePathInput.textProperty().addListener((observable, oldValue, newValue) -> {
			executablePathError.setText(null);
			evaluatedExecutablePreview.setText(null);
			evaluatedExecutablePreview.setOnMouseClicked(null);
			if (currentContextMenu != null) {
				currentContextMenu.setPathToExecutable(newValue);
				if (currentContextMenu.getCallMethod() == CallMethod.INNER_FUNCTION) {
					InnerFunctionName innerFunctionName = null;
					try {
						innerFunctionName = InnerFunctionName
								.valueOf(currentContextMenu.getPathToExecutable().toUpperCase());
					} catch (IllegalArgumentException e) {
						// function name does not exist -> exiting
						switchInputAsOtherThanInnerFunction();
						evaluatedExecutablePreview
								.setText("Use browse from inner function if you want to insert function again");
						return;
					}
					if (InnerFunctionCall.FUNCTION_CALLS.containsKey(innerFunctionName)) {
						evaluatedExecutablePreview.setText(innerFunctionName.getDescription());
					} else {
						// function name isn't implemented -> exiting
						evaluatedExecutablePreview.setText("Function is not implemented yet");
						switchInputAsOtherThanInnerFunction();
					}
				} else {
					PathLayer path = currentContextMenu.getPathToExecutableAsPath();
					if (path == null)
						return;
					evaluatedExecutablePreview.setText(path.toString());
					if (!path.exists())
						executablePathError.setText("Executable Path Doesn't exist!");
					else if (path.isDirectory())
						executablePathError.setText("Executable Path is a directory!");
					else
						evaluatedExecutablePreview.setOnMouseClicked(e -> {
							try {
								Main.revealInExplorer(path);
							} catch (IOException ex) {
								ex.printStackTrace();
							}
						});
				}
				currentContextMenu.clearIconCache();
				iconImagePreview.setImage(currentContextMenu.getIconImage());
				generateMenuPreview();
			}
		});
	}

	@FXML
	public void clearExecutablePath() {
		if (currentContextMenu == null) {
			executablePathError.setText("Choose a menu from list or create new one first.");
			return;
		}
		switchInputAsOtherThanInnerFunction();
		executablePathInput.clear();
	}

	@FXML
	public void browseExecutableFromSystem() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Navigate to where Executable is located");
		File executableFile = fileChooser.showOpenDialog(pane.getScene().getWindow());
		if (executableFile == null) {
			return;
		}
		executablePathInput.setText(executableFile.toString());
	}

	@FXML
	public void expandAllTitledPane() {
		setAllPaneExpanded(true);
	}

	@FXML
	public void CollapseAllTitledPane() {
		setAllPaneExpanded(false);
	}

	private void setAllPaneExpanded(boolean isExpanded) {
		extensionTitledPane.setExpanded(isExpanded);
		menuNameTitledPane.setExpanded(isExpanded);
		iconTitledPane.setExpanded(isExpanded);
		callMethodTitledPane.setExpanded(isExpanded);
	}

	/**
	 * Happen before {@link #initializeExtensionHolders()}
	 */
	private void initializeExtensionsNodes() {
		targetGroupName.setText(null);
		allErrorLabel.add(extensionHeaderError);
		allErrorLabel.add(extensionsNameGroupError);
		extensionsListInputText.textProperty().addListener((observable, oldValue, newValue) -> {
			if (currentContextMenu == null)
				return;
			currentContextMenu.getExtensions().clear();
			if (!StringHelper.isEmpty(newValue)) {
				String[] extensions = extensionsListInputText.getText().split(";");
				for (String extension : extensions) {
					currentContextMenu.getExtensions().add(extension.trim());
				}
			}
		});

		isDirectoryContextCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
			if (currentContextMenu != null)
				currentContextMenu.setDirectoryContext(newValue);
		});

		extensionsListGroupInputText.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				String[] extensions = extensionsListGroupInputText.getText().split(";");
				CheckBox selectedGroup = extensionGroupsListView.getSelectionModel().getSelectedItem();
				if (selectedGroup != null) {
					String groupName = selectedGroup.getText();
					Long groupID = idToGroupName.inverse().get(groupName);
					ArrayList<String> groupExtensions = extensionGroupsSetting.get(groupID);
					groupExtensions.clear();
					// exclude first negative from extension group
					if (extensions.length > 0 && extensions[0].startsWith("-")) {
						extensions[0] = extensions[0].substring(1);
						colorCheckBoxAsExcluded(selectedGroup);
						// changing in exclude properties does not only change array list value,
						// but also change value dependent on context menu list which adding - at
						// the start of group name. So in this case we also change group name on current
						// context menu if not null
						if (currentContextMenu != null && selectedGroup.isSelected()) {
							currentContextMenu.getExtensionsGroupNames().remove(groupID.toString());
							// assuring not added twice
							currentContextMenu.getExtensionsGroupNames().remove(String.valueOf(-groupID));
							currentContextMenu.getExtensionsGroupNames().add(String.valueOf(-groupID));
						}
					} else {
						colorCheckBoxAsNormal(selectedGroup);
					}
					for (String extension : extensions) {
						groupExtensions.add(extension.trim());
					}
				}
			}
		});

		groupNameInput.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue == null || targetGroupName.getText() == null)
				return;
			if (idToGroupName.containsValue(newValue)) {
				extensionsNameGroupError.setText("A group with this name already exist!");
				groupNameInput.setText(oldValue);
				return;
			}
			extensionsNameGroupError.setText(null);
			long idOfGroup = idToGroupName.inverse().get(oldValue);
			idToGroupName.forcePut(idOfGroup, newValue);
			extensionGroupsMenuDataMap.get(idOfGroup).setText(newValue);
		});

	}

	@FXML
	public void defineExtensionGroup() {
		String temp = "Group";
		String groupName = temp;
		int i = 1;
		while (idToGroupName.containsValue(groupName)) {
			groupName = temp + i++;
		}
		long groupID = generatedGroupId++;
		idToGroupName.forcePut(groupID, groupName);
		extensionGroupsSetting.put(groupID, new ArrayList<>());
	}

	@FXML
	public void removeExtensionGroup() {
		CheckBox selectedGroup = extensionGroupsListView.getSelectionModel().getSelectedItem();
		if (selectedGroup != null) {
			String groupName = selectedGroup.getText();
			long groupID = idToGroupName.inverse().remove(groupName);
			extensionGroupsSetting.remove(groupID);
		}
	}

	private void initializeMenuNameNodes() {
		allErrorLabel.add(menuNameHeaderError);
		aliasNamePreview.setText(null);
		aliasMultipleNamePreview.setText(null);
		allErrorLabel.add(parentMenuNamesError);

		isOnSingleFileCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
			if (currentContextMenu != null) {
				currentContextMenu.setOnSingleSelection(newValue);
			}
			aliasInput.setDisable(!newValue);
			insertAliasCommand.setDisable(!newValue);
			aliasNamePreview.setText(null);
			validateAliasInput();
		});

		isOnMultipleFileCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
			if (currentContextMenu != null) {
				currentContextMenu.setOnMultipleSelection(newValue);
			}
			aliasMultipleInput.setDisable(!newValue);
			insertAliasMultipleCommand.setDisable(!newValue);
			aliasMultipleNamePreview.setText(null);
			validateAliasInput();
		});

		initializeAliasInputNodes();
		initializeParentNameNodes();
	}

	private void initializeAliasInputNodes() {
		initializeInsertCommandMenuButton(insertAliasCommand, aliasInput);
		initializeInsertCommandMenuButton(insertAliasMultipleCommand, aliasMultipleInput);

		Tooltip tooltipMultiple = new Tooltip("You can also use %iCommand% where i is the index of selected file."
				+ "\nIt can be negative too so it start from last element");
		insertAliasMultipleCommand.setTooltip(tooltipMultiple);

		aliasInput.textProperty().addListener((observable, oldValue, newValue) -> {
			if (currentContextMenu == null)
				return;
			currentContextMenu.setAlias(newValue);
			if (!StringHelper.isEmpty(newValue)) {
				aliasNamePreview.setText(CommandVariableAffector.getEvaluatedCommand(
						Collections.singletonList(getSamplePathList().get(0)), null, aliasInput.getText()));
			} else {
				aliasNamePreview.setText(null);
			}
			definedMenusListView.refresh();
			validateAliasInput();
		});

		aliasMultipleInput.textProperty().addListener((observable, oldValue, newValue) -> {
			if (currentContextMenu == null)
				return;

			currentContextMenu.setAliasMultiple(newValue);
			if (!StringHelper.isEmpty(newValue)) {
				aliasMultipleNamePreview.setText(CommandVariableAffector.getEvaluatedCommand(getSamplePathList(), null,
						aliasMultipleInput.getText()));
			} else {
				aliasMultipleNamePreview.setText(null);
			}
			validateAliasInput();
		});
	}

	private void initializeInsertCommandMenuButton(MenuButton insertAliasCommand, TextField inputText) {
		insertAliasCommand.setFocusTraversable(false);
		for (CommandVariable value : CommandVariable.values()) {
			String description = CommandVariable.getInfoFormat(value);
			if (description != null) {
				description = value + ": " + description;
				MenuItem menuItemAlias = new MenuItem(description);
				menuItemAlias.setOnAction(e -> inputText.insertText(inputText.getCaretPosition(), "%" + value + "%"));
				insertAliasCommand.getItems().add(menuItemAlias);
			}
		}
	}

	/**
	 * Generate menu header error based on input
	 * 
	 * @return error text if exist null otherwise
	 */
	private String validateAliasInput() {
		String errorText = null;
		if (isOnSingleFileCheckBox.isSelected() && StringHelper.isEmpty(aliasInput.getText())) {
			errorText = "Alias cannot be empty";
		} else if (isOnMultipleFileCheckBox.isSelected() && StringHelper.isEmpty(aliasInput.getText())
				&& StringHelper.isEmpty(aliasMultipleInput.getText())) {
			// by default if multiple alias is not set, simple alias is used
			errorText = "At least an alias must be defined";
		} else if (!isOnMultipleFileCheckBox.isSelected() && !isOnSingleFileCheckBox.isSelected()) {
			errorText = "At least one checkbox should be selected : on single ? or on multiple ?";
		}
		menuNameHeaderError.setText(errorText);
		if (errorText != null)
			errorText = "Menu Name: " + errorText;
		return errorText;
	}

	private String validateExecutablePath() {
		String error = null;
		if (StringHelper.isEmpty(executablePathInput.getText()))
			error = "Executable Path is empty!";
		else if (!StringHelper.isEmpty(executablePathError.getText()))
			error = executablePathError.getText();
		return error;
	}

	/**
	 * 
	 * @return current menu error, null otherwise
	 */
	private String CurrentMenuError() {
		String error = validateExecutablePath();
		if (StringHelper.isEmpty(error))
			error = validateAliasInput();
		return error;
	}

	private void initializeParentNameNodes() {
		parentMenuNamesInput.textProperty().addListener((observable, oldValue, newValue) -> {
			if (currentContextMenu != null) {
				currentContextMenu.setParentMenuNames(newValue);
				definedMenusListView.refresh();
			}
		});

		insertParentMenuName.setFocusTraversable(false);

		MenuItem newParent = new MenuItem("Parent Example");
		newParent.setOnAction(e -> parentMenuNamesInput.setText("Parent/Submenu/child"));
		insertParentMenuName.getItems().add(newParent);

		Menu reservedParents = new Menu("Reserved Parents");
		for (String parent : UserContextMenuController.getReservedParentNamesImage().keySet()) {
			MenuItem parentMenu = new MenuItem(parent);
			parentMenu.setOnAction(e -> parentMenuNamesInput.setText(parent));
			reservedParents.getItems().add(parentMenu);
		}
		insertParentMenuName.getItems().add(reservedParents);

		parentMenuNamesPreview.setOnMouseClicked(e -> generateMenuPreview());
	}

	private void generateMenuPreview() {
		String firstExt = null;
		parentMenuNamesError.setText(null);

		if (chosenPathLayers == null) {
			for (String compatibleExtension : getCompatibleExtensions(currentContextMenu)) {
				firstExt = compatibleExtension;
				break;
			}

			if (StringHelper.isEmpty(firstExt)) {
				parentMenuNamesError.setText("Menu does not have compatible extensions!");
				return;
			}
			if (firstExt.equals("*"))
				firstExt = "txt";
			for (PathLayer examplePathLayer : examplePathLayers) {
				examplePathLayer.setName(examplePathLayer.getBaseName() + "." + firstExt);
			}
		}
		parentMenuNamesPreview.getItems().clear();
		String menuText = currentContextMenu.isOnSingleSelection()
				? currentContextMenu.getAliasEvaluated(Collections.singletonList(getSamplePathList().get(0)))
				: currentContextMenu.getAliasEvaluated(getSamplePathList());
		MenuItem menuItem = new MenuItem(menuText);
		currentContextMenu.clearIconCache();
		ImageView imageView = new ImageView(currentContextMenu.getIconImage());
		imageView.setFitHeight(IconLoader.getDefaultRequestedWH());
		imageView.setFitWidth(IconLoader.getDefaultRequestedWH());
		menuItem.setGraphic(imageView);
		MenuItem parentMenu = UserContextMenuController.appendAndCreateParentsMenus(menuItem,
				currentContextMenu.getParentMenuNames(), currentContextMenu.getParentIconImage(), new HashMap<>());
		parentMenuNamesPreview.getItems().add(parentMenu);
	}
	private Set<String> getCompatibleExtensions(UserContextMenu modifiedContextMenu) {
		Set<String> allExt = new HashSet<>(StringHelper.getUpperCaseList(modifiedContextMenu.getExtensions()));
		modifiedContextMenu.getExtensionsGroupNames().stream()
				.map(groupId -> extensionGroupsSetting.get(Long.parseLong(groupId))).filter(Objects::nonNull)
				.forEach(extGrp -> allExt.addAll(StringHelper.getUpperCaseList(extGrp)));
		return allExt;
	}

	private void initializeIconNodes() {
		iconTitledPane.setExpanded(false);
		allErrorLabel.add(iconHeaderError);

		isIconUsingExecutable.selectedProperty().addListener((observable, oldValue, newValue) -> {
			iconPathInput.setDisable(newValue);
			browseIconPath.setDisable(newValue);
			if (newValue) {
				iconPathInput.setText(null);
			}
		});

		iconPathInput.textProperty().addListener((observable, oldValue, newValue) -> {
			iconHeaderError.setText(null);
			if (currentContextMenu == null)
				return;
			currentContextMenu.setIconPath(newValue);
			// generate a new image every time
			currentContextMenu.setIconImage(null);
			iconImagePreview.setImage(currentContextMenu.getIconImage());
			iconImagePreview.setFitHeight(IconLoader.getDefaultRequestedWH());
			iconImagePreview.setFitWidth(IconLoader.getDefaultRequestedWH());
		});

		isParentIconUsingExecutable.selectedProperty().addListener((observable, oldValue, newValue) -> {
			parentIconPathInput.setDisable(newValue);
			browseParentIconPath.setDisable(newValue);
			if (newValue) {
				parentIconPathInput.setText(null);
			}
		});

		parentIconPathInput.textProperty().addListener((observable, oldValue, newValue) -> {
			iconHeaderError.setText(null);
			if (currentContextMenu == null)
				return;
			currentContextMenu.setParentIconPath(newValue);
			currentContextMenu.setParentIconImage(null);
			generateMenuPreview();
		});

		parentIconImagePreview.setOnAction(e -> parentMenuNamesPreview.show());
		browseParentIconPath.setOnAction(e -> browseIconPath.show());

		initializeInnerIconMenu(InnerIconMenu, chosenInnerImage -> iconPathInput.setText(chosenInnerImage));
		initializeInnerIconMenu(InnerParentIconMenu, chosenInnerImage -> parentIconPathInput.setText(chosenInnerImage));

	}

	private void initializeInnerIconMenu(MenuItem innerIconMenu,
			CallBackVoid<String> ToDoOnChosenInnerImageConvention) {
		TextFlow innerIcons = new TextFlow();
		innerIcons.setFocusTraversable(true);
		// ensure that each icon type is showed once
		// knowing that some icon name use same resources
		BiMap<IconLoader.ICON_TYPE, String> uniqueIcons = HashBiMap.create();
		IconLoader.ENUM_TO_NAME.forEach(uniqueIcons::forcePut);
		double spacingBetweenImage = 3;
		for (IconLoader.ICON_TYPE value : uniqueIcons.keySet()) {
			Button btn = new Button();
			ImageView img = new ImageView();
			img.setFitWidth(IconLoader.getDefaultRequestedWH());
			img.setFitHeight(IconLoader.getDefaultRequestedWH());
			img.setImage(IconLoader.getIconImage(value));
			img.setFocusTraversable(true);
			btn.setGraphic(img);
			btn.setOnAction(e -> ToDoOnChosenInnerImageConvention.call(UserContextMenu.INNER_ICON_CONVENTION + value));
			btn.setOpaqueInsets(new Insets(spacingBetweenImage));
			innerIcons.getChildren().add(btn);
		}
		double itemSize = IconLoader.getDefaultRequestedWH() + spacingBetweenImage + 20;
		innerIcons.setPrefWidth(itemSize * 5);
		ScrollPane scrollPane = new ScrollPane(innerIcons);
		scrollPane.setPrefHeight(itemSize * 6);
		scrollPane.setPrefWidth(innerIcons.getPrefWidth() + 20);
		innerIconMenu.setGraphic(scrollPane);
	}

	@FXML
	public void browseIconFromSystem() {
		File iconPath = getIconPathFromSystem();
		if (iconPath == null) {
			return;
		}
		iconPathInput.setText(iconPath.toString());
	}

	@FXML
	public void browseParentIconFromSystem() {
		File iconPath = getIconPathFromSystem();
		if (iconPath == null) {
			return;
		}
		parentIconPathInput.setText(iconPath.toString());
	}
	private File getIconPathFromSystem() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Navigate to where icon can be extracted from");
		fileChooser.getExtensionFilters()
				.addAll(StringHelper.getExtensionFilter("Photos", PhotoViewerController.ArrayIMGExt));
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Extract From executable", "*exe"));
		File iconPath = fileChooser.showOpenDialog(pane.getScene().getWindow());
		return iconPath;
	}

	private void initializeCallMethodNodes() {
		// empty all error/warning text
		callMethodTitledPane.setExpanded(false);
		allErrorLabel.add(callMethodHeaderError);
		callMethodHeaderWarning.setText(null);
		allErrorLabel.add(prefixCommandError);
		allErrorLabel.add(postfixCommandError);

		initializeInsertCommandMenuButton(insertPrefixCommand, prefixCommandInput);
		initializeInsertCommandMenuButton(insertPostfixCommand, postfixCommandInput);

		prefixCommandInput.textProperty().addListener((observable, oldValue, newValue) -> {
			prefixCommandError.setText(null);
			if (currentContextMenu == null)
				return;
			currentContextMenu.setPrefixCommandOptions(newValue);
			generateCallPreview();
		});

		postfixCommandInput.textProperty().addListener((observable, oldValue, newValue) -> {
			postfixCommandError.setText(null);
			if (currentContextMenu == null)
				return;
			currentContextMenu.setPostfixCommandOptions(newValue);
			generateCallPreview();
		});

		// bind all radio together to have only one selected
		activeCommandMethodToggle = new ToggleGroup();
		activeCommandMethodToggle.getToggles().add(isSeparateCallMethod);
		activeCommandMethodToggle.getToggles().add(isCombinedCallMethod);
		activeCommandMethodToggle.getToggles().add(isTextFileCallMethod);

		activeCommandMethodToggle.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
			if (currentContextMenu == null || newValue == null)
				return;
			if (newValue.equals(isSeparateCallMethod)) {
				currentContextMenu.setCallMethod(CallMethod.SEPARATE_CALL);
			} else if (newValue.equals(isCombinedCallMethod)) {
				currentContextMenu.setCallMethod(CallMethod.COMBINED_CALL);
			} else if (newValue.equals(isTextFileCallMethod)) {
				currentContextMenu.setCallMethod(CallMethod.TXT_FILE_CALL);
			}
			generateCallPreview();
		});

		isCallUsingRelatifPath.selectedProperty().addListener((observable, oldValue, newValue) -> {
			if (currentContextMenu == null)
				return;
			currentContextMenu.setCallUsingRelatifPath(newValue);
			generateCallPreview();
		});
	}

	@FXML
	public void chooseSamplePathList() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Choose sample list of files");
		if (currentContextMenu != null) {
			Set<String> compatibleExtensions = getCompatibleExtensions(currentContextMenu);
			if (compatibleExtensions.size() > 0)
				fileChooser.getExtensionFilters()
						.addAll(StringHelper.getExtensionFilter("Compatible Extensions", compatibleExtensions));
		}
		List<File> sampleList = fileChooser.showOpenMultipleDialog(pane.getScene().getWindow());
		if (sampleList != null && sampleList.size() > 0) {
			if (currentContextMenu != null)
				chosenPathLayers = sampleList.stream().map(FilePathLayer::new).collect(Collectors.toList());
			else
				examplePathLayers = sampleList.stream().map(FilePathLayer::new).collect(Collectors.toList());
		}
	}

	private void generateCallPreview() {
		callCommandPreview.setText(null);
		if (currentContextMenu != null) {
			if (!StringHelper.isEmpty(validateExecutablePath())) {
				callCommandPreview.setText(validateExecutablePath());
				return;
			}
			try {
				List<CallReturnHolder> callReturnHolder = GenericCaller.call(getSamplePathList(), currentContextMenu,
						false);
				StringBuilder commandBuilder = new StringBuilder();
				for (CallReturnHolder returnHolder : callReturnHolder) {
					commandBuilder.append("Working Directory:\t" + returnHolder.getWorkingDir());
					commandBuilder.append("\n\t" + returnHolder.getCommand() + "\n");
				}
				callCommandPreview.setText(commandBuilder.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
    public void initializeDataViewHolders() {
		// Menu Selection section
		initializeMenuSelectionHolders();

		// executable section
		initializeExecutableHolders();

		// extension section
		initializeExtensionHolders();

		// Menu Name section

		// Icon section

		// call method section
	}

	private void initializeMenuSelectionHolders() {
		initializeTemplateMenuHolder();

		initializeUserDefinedMenuHolder();

		initializeInnerFunctionHolder();
	}

	private void initializeTemplateMenuHolder() {
		// binding of template menu
		templateListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		templateListView.setCellFactory(new UserContextMenuCellFactory());
		templateListView.setItems(menuListTemplateData);

		// this is a one time operation independent of user setting, so initializing
		// data also here for one time
		menuListTemplateData.addAll(UserContextMenuDefaultSetting.getInitializedMenuList());
	}

	private void initializeUserDefinedMenuHolder() {
		// transforming context menu on addition to use id of group name instead of name
		menuListUserData.addListener((ListChangeListener<UserContextMenu>) c -> {
			while (c.next()) {
				if (c.wasAdded()) {
					for (UserContextMenu addedMenu : c.getAddedSubList()) {
						transformContextMenuGroupId(addedMenu);
					}
				}
			}
		});

		// binding of listview
		definedMenusListView.setCellFactory(new UserContextMenuCellFactory());
		definedMenusListView.setItems(menuListUserData);
		definedMenusListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		definedMenusListView.getSelectionModel().getSelectedItems()
				.addListener((ListChangeListener<UserContextMenu>) c -> {
					while (c.next()) {
						UserContextMenu newSelected = c.wasAdded() ? c.getAddedSubList().get(0) : null;
						if (c.wasRemoved() && currentContextMenu != null && !isValidNewSetting(true)) {
							if (newSelected != currentContextMenu) {
								Platform.runLater(
										() -> definedMenusListView.getSelectionModel().select(currentContextMenu));
							}
						} else if (c.wasAdded()) {
							UserContextMenu selectedItem = c.getAddedSubList().get(0);
							showContextMenu(selectedItem);
						}
					}
					if (definedMenusListView.getSelectionModel().getSelectedIndex() <= -1)
						clearCurrentContextMenu();
				});
	}

	private void initializeInnerFunctionHolder() {
		// binding of inner function
		innerFunctionListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		innerFunctionListView.setCellFactory(new Callback<ListView<InnerFunctionName>, ListCell<InnerFunctionName>>() {
			@Override
			public ListCell<InnerFunctionName> call(ListView<InnerFunctionName> param) {
				return new ListCell<InnerFunctionName>() {
					@Override
					protected void updateItem(InnerFunctionName item, boolean empty) {
						super.updateItem(item, empty);
						if (empty || item == null) {
							setText(null);
						} else {
							setText(item.getDescription());
						}
					}
				};
			}
		});
		innerFunctionListView.setItems(innerFunctionData);
		// this is a one time operation independent of user setting, so initializing
		// data also here for one time
		innerFunctionData.addAll(InnerFunctionCall.FUNCTION_CALLS.keySet());
	}

	private void initializeExecutableHolders() {
		// there are no holder for executable section
	}

	/**
	 * happen after {@link #initializeExtensionsNodes()}
	 */
	private void initializeExtensionHolders() {
		// bind checkbox to list
		extensionGroupsListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		extensionGroupsListView.setItems(extensionGroupsMenuData);
		// expand group extension list on selection
		extensionGroupsListView.getSelectionModel().selectedItemProperty()
				.addListener((observable, oldValue, newValue) -> {
					if (newValue != null && idToGroupName.containsValue(newValue.getText())) {
						// rename text change based on target group so we set null target group first
						targetGroupName.setText(null);
						// fill target group name and rename field
						groupNameInput.setText(newValue.getText());
						targetGroupName.setText(newValue.getText());
						extensionsListGroupInputText.setText(String.join("; ",
								extensionGroupsSetting.get(idToGroupName.inverse().get(newValue.getText()))));
						if (currentContextMenu != null) {
							if (currentContextMenu.getExtensionsGroupNames()
									.contains("-" + idToGroupName.inverse().get(newValue.getText()))) {
								extensionsListGroupInputText.insertText(0, "-");
							}
						}
					} else {
						// clear target and rename field
						targetGroupName.setText(null);
						groupNameInput.clear();
						extensionsListGroupInputText.clear();
					}
				});

		// creating the map copy from observable checkbox to facilitate selection on
		// show menu
		// id --> checkbox
		extensionGroupsMenuData.addListener((ListChangeListener<CheckBox>) c -> {
			while (c.next()) {
				if (c.wasAdded()) {
					for (CheckBox checkBox : c.getAddedSubList()) {
						extensionGroupsMenuDataMap.put(idToGroupName.inverse().get(checkBox.getText()), checkBox);
					}
				} else if (c.wasRemoved()) {
					for (CheckBox checkBox : c.getRemoved()) {
						extensionGroupsMenuDataMap.remove(idToGroupName.inverse().get(checkBox.getText()));
					}
				}
			}
		});

		// add or remove checkbox on extension group setting
		extensionGroupsSetting.addListener((MapChangeListener<Long, ArrayList<String>>) change -> {
			if (change.wasAdded()) {
				// ensure that added check box to list view data is added only once
				Long groupID = change.getKey();
				if (!extensionGroupsMenuDataMap.containsKey(groupID)) {
					CheckBox groupCheckBox = new CheckBox(idToGroupName.get(groupID));

					// add or remove compatible group upon check box selection action
					groupCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
						boolean isExcludedGroup = isCheckBoxExcluded(groupCheckBox);
						// we store group as string id in original array list of menu
						// also if group is excluded a - is added at the beginning of the list
						String groupIdOrExcluded = isExcludedGroup ? "-" + groupID : groupID.toString();
						if (currentContextMenu != null) {
							if (newValue) {
								if (!currentContextMenu.getExtensionsGroupNames().contains(groupIdOrExcluded))
									currentContextMenu.getExtensionsGroupNames().add(groupIdOrExcluded);
							} else {
								currentContextMenu.getExtensionsGroupNames().removeIf(n -> n.equals(groupIdOrExcluded));
							}
						} else {
							colorCheckBoxAsNormal(groupCheckBox);
						}
					});
					extensionGroupsMenuData.add(0, groupCheckBox);
					if (currentContextMenu != null) {
						extensionGroupsListView.getSelectionModel().select(0);
						extensionGroupsListView.scrollTo(0);
					}
				}

			} else if (change.wasRemoved()) {
				// remove group name from user context menu group compatible name
				extensionGroupsMenuData.remove(extensionGroupsMenuDataMap.get(change.getKey()));
			}
		});
	}

	private void colorCheckBoxAsNormal(CheckBox groupCheckBox) {
		groupCheckBox.setTextFill(null);
		groupCheckBox.setStyle(null);
	}
	private void colorCheckBoxAsExcluded(CheckBox groupCheckBox) {
		groupCheckBox.setTextFill(Color.RED);
		groupCheckBox.setStyle("-fx-text-fill: RED");
	}

	private boolean isCheckBoxExcluded(CheckBox groupCheckBox) {
		return Color.RED.equals(groupCheckBox.getTextFill());
	}

	@Override
    public @Nullable Parent getViewPane() {
		return pane;
    }

    @Override
    public boolean searchKeyWord(String keyword) {
		Optional<UserContextMenu> found = menuListUserData.stream()
				.filter(e -> e.getTitle().toUpperCase().contains(keyword.toUpperCase())
						|| e.getPathToExecutable().toUpperCase().contains(keyword))
				.findFirst();
		if (found.isPresent()) {
			definedMenusListView.getSelectionModel().select(found.get());
			definedMenusListView.scrollTo(found.get());
			return true;
		}

		for (String groupName : idToGroupName.values()) {
			if (groupName.toUpperCase().contains(keyword.toUpperCase())) {
				long groupID = idToGroupName.inverse().get(groupName);
				if (extensionGroupsMenuDataMap.containsKey(groupID)) {
					CheckBox foundCheckBox = extensionGroupsMenuDataMap.get(groupID);
					definedMenusListView.getSelectionModel().select(0);
					extensionGroupsListView.getSelectionModel().select(foundCheckBox);
					extensionGroupsListView.scrollTo(foundCheckBox);
				}
				return true;
			}
		}
        return false;
    }

    @Override
    public void clearSearch() {
		extensionGroupsListView.getSelectionModel().clearSelection();
		definedMenusListView.getSelectionModel().clearSelection();
    }

    @Override
    public void pullDataFromSetting() {
		clearCurrentContextMenu();
		extensionGroupsSetting.clear();
		idToGroupName.clear();
		Setting.getExtensionGroups().forEach((groupName, groupExtList) -> {
			long groupID = generatedGroupId++;
			idToGroupName.forcePut(groupID, groupName);
			extensionGroupsSetting.put(groupID, new ArrayList<>(groupExtList));
		});

		menuListUserData.clear();
		for (UserContextMenu userContextMenu : Setting.getUserContextMenus()) {
			cloneThenAddMenuToUserData(userContextMenu, false);
		}
	}

	@Override
	public boolean isValidNewSetting(boolean showDialogAlert) {
		boolean isValid = true;
		if (currentContextMenu != null) {
			String currentMenuError = CurrentMenuError();
			isValid = StringHelper.isEmpty(currentMenuError);
			if (!isValid && showDialogAlert) {
				DialogHelper.showAlert(Alert.AlertType.ERROR,
						"Creation Error for menu : " + currentContextMenu.getTitle(), currentMenuError,
						"Fix error to continue.\nYou can remove this defined menu too.", pane.getScene().getWindow());
			}
		}
		return isValid;
	}

	@Override
	public boolean pushDataToSetting() {
		Setting.getExtensionGroups().clear();
		extensionGroupsSetting.forEach((groupId, GroupList) -> Setting.getExtensionGroups()
				.put(idToGroupName.get(groupId), new ArrayList<>(GroupList)));

		Setting.getUserContextMenus().clear();
		for (UserContextMenu contextMenu : menuListUserData) {
			try {
				UserContextMenu clonedContextMenu = (UserContextMenu) contextMenu.clone();
				rollBackContextMenuGroupName(clonedContextMenu);
				Setting.getUserContextMenus().add(clonedContextMenu);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public void cloneThenAddMenuToUserData(UserContextMenu contextMenu, boolean selectAfterAdd) {
		try {
			UserContextMenu clonedContextMenu = (UserContextMenu) contextMenu.clone();
			addMenuToUserData(clonedContextMenu, selectAfterAdd);
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public void addMenuToUserData(UserContextMenu contextMenu, boolean selectAfterAdd) {
		menuListUserData.add(contextMenu);
		if (selectAfterAdd) {
			definedMenusListView.getSelectionModel().select(contextMenu);
			definedMenusListView.scrollTo(contextMenu);
		}
	}

	private void transformContextMenuGroupId(UserContextMenu contextMenu) {
		contextMenu.setExtensionsGroupNames(replaceGroupNameById(contextMenu.getExtensionsGroupNames()));
	}

	private void rollBackContextMenuGroupName(UserContextMenu contextMenu) {
		contextMenu.setExtensionsGroupNames(replaceIdByGroupName(contextMenu.getExtensionsGroupNames()));
	}

	/**
	 * Replace group name with generated id, not found group name are removed
	 * 
	 * @param groupNames
	 * @return
	 */
	private List<String> replaceGroupNameById(List<String> groupNames) {
		List<String> replacedGroupIds = new ArrayList<>();
		for (String groupName : groupNames) {
			if (idToGroupName.containsValue(groupName)) {
				replacedGroupIds.add(idToGroupName.inverse().get(groupName).toString());
			} else if (groupName.startsWith("-")) {
				String excludedGroupName = groupName.substring(1);
				if (idToGroupName.containsValue(excludedGroupName)) {
					long groupID = idToGroupName.inverse().get(excludedGroupName);
					replacedGroupIds.add(String.valueOf(-groupID));
				}
			}
		}
		return replacedGroupIds;
	}

	/**
	 * Replace group id with name, if not found group name isn't added
	 *
	 * @param groupIds
	 * @return
	 */
	private List<String> replaceIdByGroupName(List<String> groupIds) {
		List<String> replacedGroupNames = new ArrayList<>();
		for (String groupId : groupIds) {
			Long groupIdLong = Long.parseLong(groupId);
			if (idToGroupName.containsKey(groupIdLong)) {
				replacedGroupNames.add(idToGroupName.get(groupIdLong));
			} else if (idToGroupName.containsKey(-groupIdLong)) {
				String excludedGroupName = "-" + idToGroupName.get(-groupIdLong);
				replacedGroupNames.add(excludedGroupName);
			}
		}
		return replacedGroupNames;
	}

	/**
	 * {@link #currentContextMenu} must be defined and contain inner function
	 * already
	 */
	private void switchInputAsForInnerFunction() {
		extensionPane.setDisable(false);
		executablePathInput.setDisable(true);

		extensionPane.setDisable(true);

		// allow user to change alias of defined inner function
		menuNamePane.setDisable(false);
		isOnSingleFileCheckBox.setDisable(true);
		isOnMultipleFileCheckBox.setDisable(true);

		// allow user to change icon of defined inner function
		iconPane.setDisable(false);

		callMethodPane.setDisable(true);

		callCommandPreview.setText("Magic programmed function is called !");
	}

	private void switchInputAsOtherThanInnerFunction() {
		executablePane.setDisable(false);
		executablePathInput.setDisable(false);

		if (currentContextMenu.getCallMethod().equals(CallMethod.INNER_FUNCTION)) {
			// change call method by default to separate call in case of inner function
			// to allow user to choose inner function input, he must use browse
			// form inner function menu
			currentContextMenu.setCallMethod(CallMethod.SEPARATE_CALL);
			executablePathInput.clear();
		}

		extensionPane.setDisable(false);

		menuNamePane.setDisable(false);
		isOnSingleFileCheckBox.setDisable(false);
		isOnMultipleFileCheckBox.setDisable(false);

		iconPane.setDisable(false);
		callMethodPane.setDisable(false);
	}

	public void showContextMenu(UserContextMenu userContextMenu) {
		clearCurrentContextMenu();
		currentContextMenu = userContextMenu;
		if (userContextMenu == null)
			return;
		if (userContextMenu.getCallMethod().equals(CallMethod.INNER_FUNCTION)) {
			switchInputAsForInnerFunction();
		} else {
			switchInputAsOtherThanInnerFunction();
		}

		// Menu selection section
		activateMenuCheckBox.setSelected(userContextMenu.isActive());
		menuOrderInput.setValue(userContextMenu.getMenuOrder());

		// executable section
		executablePathInput.setText(userContextMenu.getPathToExecutable());

		// extension section
		extensionsListInputText.setText(String.join("; ", userContextMenu.getExtensions()));
		isDirectoryContextCheckBox.setSelected(currentContextMenu.isDirectoryContext());

		for (String groupName : currentContextMenu.getExtensionsGroupNames()) {
			long groupID = Long.parseLong(groupName);
			boolean isExcluded = false;
			if (groupID < 0) {
				groupID = -groupID;
				isExcluded = true;
			}
			if (extensionGroupsMenuDataMap.containsKey(groupID)) {
				if (isExcluded)
					colorCheckBoxAsExcluded(extensionGroupsMenuDataMap.get(groupID));
				extensionGroupsMenuDataMap.get(groupID).setSelected(true);
			}
		}
		// sort by isSelected first, then by alphabetic order
		Collections.sort(extensionGroupsMenuData,
				(a, b) -> Boolean.compare(a.isSelected(), b.isSelected()) != 0
						? -Boolean.compare(a.isSelected(), b.isSelected())
						: a.getText().compareTo(b.getText()));
		// expand first selected group
		if (extensionGroupsMenuData.size() > 0 && extensionGroupsMenuData.get(0).isSelected()) {
			extensionGroupsListView.getSelectionModel().select(0);
		} else {
			extensionGroupsListView.getSelectionModel().clearSelection();
		}

		// Menu Name section
		isOnSingleFileCheckBox.setSelected(currentContextMenu.isOnSingleSelection());
		aliasInput.setText(currentContextMenu.getAlias());
		isOnMultipleFileCheckBox.setSelected(currentContextMenu.isOnMultipleSelection());
		aliasMultipleInput.setText(currentContextMenu.getAliasMultiple());
		parentMenuNamesInput.setText(currentContextMenu.getParentMenuNames());

		// Icon section
		isIconUsingExecutable.setSelected(StringHelper.isEmpty(currentContextMenu.getIconPath()));
		iconPathInput.setText(currentContextMenu.getIconPath());
		isParentIconUsingExecutable.setSelected(StringHelper.isEmpty(currentContextMenu.getParentIconPath()));
		parentIconPathInput.setText(currentContextMenu.getParentIconPath());

		// call method section
		prefixCommandInput.setText(currentContextMenu.getPrefixCommandOptions());
		postfixCommandInput.setText(currentContextMenu.getPostfixCommandOptions());
		switch (currentContextMenu.getCallMethod()) {
			case SEPARATE_CALL :
				isSeparateCallMethod.setSelected(true);
				break;
			case COMBINED_CALL :
				isCombinedCallMethod.setSelected(true);
				break;
			case TXT_FILE_CALL :
				isTextFileCallMethod.setSelected(true);
				break;
			default :
				activeCommandMethodToggle.selectToggle(null);
		}
		isCallUsingRelatifPath.setSelected(currentContextMenu.isCallUsingRelatifPath());
		generateMenuPreview();
	}

	private void clearCurrentContextMenu() {
		currentContextMenu = null;
		// Menu selection section
		activateMenuCheckBox.setSelected(false);
		menuOrderInput.setValue(0);

		// executable section
		extensionPane.setDisable(true);
		executablePathInput.clear();

		// extension section
		extensionPane.setDisable(true);
		extensionsListInputText.clear();
		isDirectoryContextCheckBox.setSelected(false);
		extensionGroupsMenuData.forEach(e -> e.setSelected(false));
		extensionGroupsListView.getSelectionModel().clearSelection();

		// Menu Name section
		menuNamePane.setDisable(true);
		isOnSingleFileCheckBox.setSelected(true); // trigger change
		isOnSingleFileCheckBox.setSelected(false);
		aliasInput.clear();
		isOnMultipleFileCheckBox.setSelected(true);
		isOnMultipleFileCheckBox.setSelected(false);
		aliasMultipleInput.clear();
		menuNameHeaderError.setText(null);
		parentMenuNamesInput.clear();

		// Icon section
		iconPane.setDisable(true);
		isIconUsingExecutable.setSelected(false);
		iconPathInput.clear();
		isIconUsingExecutable.setSelected(false);
		parentIconPathInput.clear();

		// call method section
		callMethodPane.setDisable(true);
		prefixCommandInput.clear();
		postfixCommandInput.clear();
		activeCommandMethodToggle.selectToggle(null);
		isCallUsingRelatifPath.setSelected(false);

	}
}
