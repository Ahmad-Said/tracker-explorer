package said.ahmad.javafx.tracker.controller;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import said.ahmad.javafx.tracker.app.DialogHelper;
import said.ahmad.javafx.tracker.app.Main;
import said.ahmad.javafx.tracker.controller.splitview.SplitViewController;
import said.ahmad.javafx.tracker.datatype.SplitViewState;
import said.ahmad.javafx.tracker.fxGraphics.DraggableTab;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * Manages tab operations including creation, closing, switching, and state management
 */
public class TabManager {
    private static final String DEFAULT_TAB_TITLE = "Default";

    private final TabPane tabPane;
    private final Stack<SplitViewController> allSplitViewController;
    private final SplitViewManager splitViewManager;
    private DraggableTab lastSelectedTab;

    public TabManager(TabPane tabPane, Stack<SplitViewController> allSplitViewController,
                     SplitViewManager splitViewManager) {
        this.tabPane = tabPane;
        this.allSplitViewController = allSplitViewController;
        this.splitViewManager = splitViewManager;
    }

    public void initializeTabs(List<SplitViewState> leftState, List<SplitViewState> rightState) {
        tabPane.getTabs().clear();
        DraggableTab defaultTab = new DraggableTab(DEFAULT_TAB_TITLE,
                new ArrayList<>(List.of(leftState.get(0), rightState.get(0))));
        defaultTab.setClosable(false);
        defaultTab.flipisEnteringAction();
        tabPane.getTabs().add(defaultTab);
        activeActionTab(defaultTab);
    }

    public void addTabsFromSettings(List<String> titles, java.util.Map<String, List<SplitViewState>> favoritesMap) {
        for (String title : titles) {
            if (favoritesMap.containsKey(title)) {
                List<SplitViewState> splitStates = favoritesMap.get(title);
                addTabOnly(title, splitStates);
            }
        }
    }

    public void activeActionTab(DraggableTab dragTab) {
        dragTab.setOnSelectionChanged(e -> handleTabSelection(dragTab));

        dragTab.getGraphic().setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                if (event.getDragboard().hasFiles() || event.getDragboard().hasContent(DataFormat.URL)
                        || event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.ANY);
                }
                tabPane.getSelectionModel().select(dragTab);
            }
        });

        dragTab.getGraphic().setOnMouseClicked(e -> {
            if (e.getButton().equals(MouseButton.SECONDARY)) {
                showTabContextMenu(dragTab, e.getScreenX(), e.getScreenY());
            }
        });
    }

    private void handleTabSelection(DraggableTab dragTab) {
        ArrayList<SplitViewState> splitStates = dragTab.getSplitViewStates();

        if (dragTab.isEnteringAction()) {
            // The new tab switched to
            int toBeShownSplit = dragTab.getShownSplitViewSize();
            int shownSplit = allSplitViewController.size();

            // Add or remove splitView as needed
            while (toBeShownSplit != shownSplit) {
                for (int i = allSplitViewController.size(); i < dragTab.getShownSplitViewSize(); i++) {
                    splitViewManager.addSplitView();
                    shownSplit++;
                }
                while (allSplitViewController.size() > dragTab.getShownSplitViewSize()) {
                    splitViewManager.removeSplitView(allSplitViewController.size() - 1);
                    shownSplit--;
                }
            }

            for (int i = 0; i < shownSplit; i++) {
                allSplitViewController.get(i).restoreSplitViewState(splitStates.get(i));
            }
        } else {
            // The tab that is switched from
            dragTab.setShownSplitViewSize(allSplitViewController.size());
            for (int i = 0; i < allSplitViewController.size(); i++) {
                if (i >= splitStates.size()) {
                    splitStates.add(new SplitViewState());
                }
                allSplitViewController.get(i).saveStateToSplitState(splitStates.get(i));
            }
            lastSelectedTab = dragTab;
        }
        dragTab.flipisEnteringAction();
    }

    private void showTabContextMenu(DraggableTab dragTab, double screenX, double screenY) {
        ContextMenu mn = new ContextMenu();
        MenuItem mnRenameTitle = new MenuItem("Rename");
        MenuItem mnResetDirectory = new MenuItem("Reset");
        mn.getItems().addAll(mnRenameTitle, mnResetDirectory);
        mn.show(Main.getPrimaryStage(), screenX, screenY);

        mnRenameTitle.setOnAction(eR -> renameTab(dragTab));
        mnResetDirectory.setOnAction(act -> resetTabDirectory(dragTab));
    }

    private void renameTab(DraggableTab dragTab) {
        String title = DialogHelper.showTextInputDialog("Rename Tab", "Enter New Name", "",
                dragTab.getLabelText());
        if (title == null || title.isEmpty()) {
            return;
        }
        title = title.replaceAll(";", "_");
        dragTab.setLabelText(title);
    }

    private void resetTabDirectory(DraggableTab dragTab) {
        for (int i = 0; i < dragTab.getSplitViewStates().size(); i++) {
            SplitViewState state = dragTab.getSplitViewStates().get(i);
            while (!state.getBackQueue().isEmpty()) {
                state.getNextQueue().add(state.getMDirectory());
                state.setMDirectory(state.getBackQueue().removeLast());
            }
            if (i >= allSplitViewController.size()) {
                splitViewManager.addSplitView();
            }
            allSplitViewController.get(i).restoreSplitViewState(state);
        }
        while (dragTab.getShownSplitViewSize() < allSplitViewController.size()) {
            splitViewManager.removeSplitView(allSplitViewController.size() - 1);
        }
    }

    public void plusTab() {
        addTabAndSwitch("New Tab", allSplitViewController.stream()
                .map(sp -> new SplitViewState(sp.getmDirectoryPath()))
                .collect(Collectors.toList()));
    }

    public void closeCurrentTab() {
        if (tabPane.getSelectionModel().getSelectedItem().isClosable()) {
            Tab toBeRemoved = tabPane.getSelectionModel().getSelectedItem();
            if (lastSelectedTab != null) {
                tabPane.getSelectionModel().select(lastSelectedTab);
            }
            tabPane.getTabs().remove(toBeRemoved);
        } else {
            if (tabPane.getTabs().size() == 1) {
                javafx.application.Platform.exit();
            }
        }
    }

    public void switchNextTabs() {
        tabPane.getSelectionModel()
                .select((tabPane.getSelectionModel().getSelectedIndex() + 1) % tabPane.getTabs().size());
    }

    public void switchPreviousTab() {
        tabPane.getSelectionModel()
                .select((tabPane.getSelectionModel().getSelectedIndex() - 1 + tabPane.getTabs().size())
                        % tabPane.getTabs().size());
    }

    public DraggableTab addTabAndSwitch(String title, List<SplitViewState> splitStates) {
        DraggableTab tempTab = addTabOnly(title, splitStates);
        tabPane.getSelectionModel().select(tempTab);
        return tempTab;
    }

    public DraggableTab addTabOnly(String title, List<SplitViewState> splitStates) {
        DraggableTab tempTab = new DraggableTab(title, splitStates);
        activeActionTab(tempTab);
        tabPane.getTabs().add(tempTab);
        return tempTab;
    }

    public TabPane getTabPane() {
        return tabPane;
    }

    public List<String> getOpenedTabTitles() {
        ArrayList<String> titles = new ArrayList<>();
        for (Tab tab : tabPane.getTabs()) {
            String title = tab.getTooltip() == null ? null : tab.getTooltip().getText();
            if (title != null && !title.isEmpty()) {
                titles.add(title);
            }
        }
        return titles;
    }
}

