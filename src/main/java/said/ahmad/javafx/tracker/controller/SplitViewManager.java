package said.ahmad.javafx.tracker.controller;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.jetbrains.annotations.Nullable;
import said.ahmad.javafx.tracker.app.pref.Setting;
import said.ahmad.javafx.tracker.controller.splitview.SplitViewController;
import said.ahmad.javafx.tracker.system.file.PathLayer;

import java.io.IOException;
import java.util.Stack;

/**
 * Manages split view operations including creation, removal, and focus management
 */
public class SplitViewManager {
    private final SplitPane allSplitViewPane;
    private final Stack<SplitViewController> allSplitViewController;
    private final Stack<SplitViewController> allSplitViewControllerRemoved;
    private final Stage stage;
    private final WelcomeController welcomeController;

    public SplitViewManager(SplitPane allSplitViewPane, Stack<SplitViewController> allSplitViewController,
                           Stack<SplitViewController> allSplitViewControllerRemoved, Stage stage,
                           WelcomeController welcomeController) {
        this.allSplitViewPane = allSplitViewPane;
        this.allSplitViewController = allSplitViewController;
        this.allSplitViewControllerRemoved = allSplitViewControllerRemoved;
        this.stage = stage;
        this.welcomeController = welcomeController;
    }

    public SplitViewController addSplitView() {
        boolean doAddLeft = allSplitViewController.size() % 2 == 0;
        SplitViewController newSplitView = null;
        try {
            newSplitView = addSplitView(allSplitViewController.peek().getmDirectoryPath(), doAddLeft);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return newSplitView;
    }

    /**
     * @param initialePath only used if new split view is created, otherwise reuse last removed/cached one
     * @param isLeftTemplate
     * @return
     * @throws IOException
     */
    public SplitViewController addSplitView(PathLayer initialePath, boolean isLeftTemplate) throws IOException {
        SplitViewController newSplit = null;
        if (allSplitViewControllerRemoved.size() != 0) {
            newSplit = allSplitViewControllerRemoved.pop();
        } else {
            newSplit = new SplitViewController(initialePath, isLeftTemplate, welcomeController);
            if (isLeftTemplate) {
                SplitViewController.loadFXMLViewAsLeft(newSplit);
            } else {
                SplitViewController.loadFXMLViewAsRight(newSplit);
            }
            newSplit.refresh(null);
        }

        newSplit.getAutoExpand().setText("+");
        MenuItem mn = new MenuItem("Close This View");
        final SplitViewController splitView = newSplit;
        newSplit.getExitSplitButton().setOnAction(e -> removeSplitView(splitView));
        mn.setOnAction(e -> splitView.getExitSplitButton().fire());
        newSplit.getAutoExpand().setContextMenu(new ContextMenu(mn));

        allSplitViewController.add(newSplit);
        allSplitViewPane.getItems().add(newSplit.getViewPane());

        autoFitWidthSplitPane();

        if (allSplitViewController.size() > 1) {
            configureNewSplitViewWithNeighbor(newSplit, isLeftTemplate);
        }

        return newSplit;
    }

    private void configureNewSplitViewWithNeighbor(SplitViewController newSplit, boolean isLeftTemplate) {
        if (isLeftTemplate) {
            newSplit.getLeftDominate().setVisible(true);
            newSplit.getDesktopButton().setText("Desk");
            Insets oldDeskInset = GridPane.getMargin(newSplit.getDesktopButton());
            GridPane.setMargin(newSplit.getDesktopButton(),
                    new Insets(oldDeskInset.getTop(), oldDeskInset.getRight(), oldDeskInset.getBottom(), 70));
            Insets oldFavInset = GridPane.getMargin(newSplit.getFavoritesLocations());
            GridPane.setMargin(newSplit.getFavoritesLocations(),
                    new Insets(oldFavInset.getTop(), oldFavInset.getRight(), oldFavInset.getBottom(), 125));
        }

        SplitViewController leftNeighbor = allSplitViewController.get(allSplitViewController.size() - 2);
        newSplit.setLeftViewNeighbor(leftNeighbor);
        leftNeighbor.setRightViewNeighbor(newSplit);
        leftNeighbor.getAutoExpand().setOnAction(null);
        leftNeighbor.getAutoExpand().setText("<>");
        leftNeighbor.setAutoExpand(Setting.isAutoExpand());
        leftNeighbor.getExitSplitButton().setVisible(false);
        setSplitAsLastOne(newSplit);
    }

    private void setSplitAsLastOne(SplitViewController splitViewController) {
        splitViewController.getAutoExpand().setText("+");
        splitViewController.setAutoExpand(false);
        splitViewController.setRightViewNeighbor(null);
        splitViewController.getExitSplitButton().setVisible(true);
        splitViewController.getAutoExpand().setOnAction(e -> addSplitView());
    }

    public void removeSplitView(SplitViewController toRemoveSplitView) {
        removeSplitView(allSplitViewController.indexOf(toRemoveSplitView));
    }

    public void removeSplitView(int toRemoveIndex) {
        int indexLastOne = allSplitViewController.size() - 1;
        SplitViewController removedSplitView = allSplitViewController.get(toRemoveIndex);
        allSplitViewPane.getItems().remove(toRemoveIndex);
        allSplitViewController.remove(toRemoveIndex);

        // Updating neighbors
        if (removedSplitView.getRightViewNeighbor() != null && removedSplitView.getLeftViewNeighbor() != null) {
            removedSplitView.getRightViewNeighbor().setLeftViewNeighbor(removedSplitView.getLeftViewNeighbor());
            removedSplitView.getLeftViewNeighbor().setRightViewNeighbor(removedSplitView.getRightViewNeighbor());
        }

        allSplitViewControllerRemoved.add(removedSplitView);

        if (allSplitViewPane.getItems().size() == 0) {
            stage.close();
            return;
        }

        if (indexLastOne == toRemoveIndex) {
            setSplitAsLastOne(removedSplitView.getLeftViewNeighbor());
        }

        autoFitWidthSplitPane();
    }

    /**
     * Does remove all split view and reAdd them
     * Used to reload view CSS stuff after loading a new theme
     */
    public void refreshCSSSplitViews() {
        int size = allSplitViewController.size();
        while (size-- != 0) {
            try {
                addSplitView(allSplitViewController.get(0).getmDirectoryPath(), allSplitViewController.get(0).isLeft());
                removeSplitView(0);
                allSplitViewControllerRemoved.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        allSplitViewController.forEach(sp -> sp.refresh(null));
    }

    private void autoFitWidthSplitPane() {
        double dividerEach = 1.0 / allSplitViewController.size();
        double start = dividerEach;
        for (SplitPane.Divider d : allSplitViewPane.getDividers()) {
            d.setPosition(start);
            start += dividerEach;
        }
    }

    @Nullable
    public SplitViewController getFocusedPane() {
        SplitViewController focusedSplit = null;
        Node focusedNode = stage.getScene().getFocusOwner();
        while (focusedNode != null && !(focusedNode instanceof GridPane)) {
            focusedNode = focusedNode.getParent();
        }
        for (SplitViewController splitView : allSplitViewController) {
            if (focusedNode == splitView.getViewPane() || splitView.isFocused()) {
                focusedSplit = splitView;
                break;
            }
        }
        return focusedSplit;
    }

    public void focusNextSplitView() {
        SplitViewController lastFocus = getFocusedPane();
        if (lastFocus != null && lastFocus.isFocusedSearchField()) {
            lastFocus.requestFocus();
        } else if (lastFocus != null && lastFocus.getRightViewNeighbor() != null) {
            lastFocus.getRightViewNeighbor().requestFocus();
        } else {
            allSplitViewController.get(0).requestFocus();
        }
    }

    public void focusPreviousSplitView() {
        SplitViewController lastFocus = getFocusedPane();
        if (lastFocus != null && lastFocus.getLeftViewNeighbor() != null) {
            lastFocus.getLeftViewNeighbor().requestFocus();
        } else {
            allSplitViewController.peek().requestFocus();
        }
    }

    public void refreshAllSplitViews() {
        allSplitViewController.forEach(spCon -> spCon.refreshAsPathField());
    }

    /**
     * Refresh Split view only current directory match provided File parameter
     * @param directoryView
     * @param exception Do not refresh given splitView (can be null)
     */
    public void refreshAllSplitViewsIfMatch(PathLayer directoryView, SplitViewController exception) {
        allSplitViewController.stream()
                .filter(spCon -> spCon != exception && spCon.getmDirectoryPath().equals(directoryView))
                .forEach(spCon -> spCon.refreshAsPathField());
    }

    /**
     * Check if directory is opened in other views
     * @param directoryView
     * @param exception Do not refresh given splitView (can be null)
     */
    public boolean isDirOpenedInOtherView(PathLayer directoryView, SplitViewController exception) {
        return allSplitViewController.stream()
                .filter(spCon -> spCon != exception && spCon.getmDirectoryPath().equals(directoryView))
                .findAny()
                .isPresent();
    }

    public void refreshUnExistingViewsDir() {
        allSplitViewController.forEach(spCon -> {
            boolean doRefresh = false;
            while (!spCon.getmDirectoryPath().exists()) {
                spCon.setmDirectory(spCon.getmDirectoryPath().getParentPath());
                doRefresh = true;
            }
            if (doRefresh) {
                spCon.refresh(null);
            }
        });
    }

    public SplitViewController getMostLeftView() {
        return allSplitViewController.get(0);
    }

    public SplitViewController getMostRightView() {
        return allSplitViewController.get(allSplitViewController.size() - 1);
    }

    public Stack<SplitViewController> getAllSplitViewController() {
        return allSplitViewController;
    }
}

