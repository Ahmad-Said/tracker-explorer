package said.ahmad.javafx.tracker.controller;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * Manages keyboard shortcuts and key event handling
 */
public class KeyboardShortcutManager {
    static final KeyCombination SHORTCUT_OPEN_NEW_TAB = new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN);
    static final KeyCombination SHORTCUT_CLOSE_CURRENT_TAB = new KeyCodeCombination(KeyCode.W,
            KeyCombination.CONTROL_DOWN);
    static final KeyCombination SHORTCUT_SWITCH_NEXT_TABS = new KeyCodeCombination(KeyCode.TAB,
            KeyCombination.CONTROL_DOWN);
    static final KeyCombination SHORTCUT_SWITCH_PREVIOUS_TABS = new KeyCodeCombination(KeyCode.TAB,
            KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);

    static final KeyCombination SHORTCUT_EASY_FOCUS_SWITCH_VIEW = new KeyCodeCombination(KeyCode.F3);
    static final KeyCombination SHORTCUT_FOCUS_VIEW = new KeyCodeCombination(KeyCode.TAB);
    static final KeyCombination SHORTCUT_FOCUS_PREVIOUS_VIEW = new KeyCodeCombination(KeyCode.TAB,
            KeyCombination.SHIFT_DOWN);

    private final TabManager tabManager;
    private final SplitViewManager splitViewManager;

    public KeyboardShortcutManager(TabManager tabManager, SplitViewManager splitViewManager) {
        this.tabManager = tabManager;
        this.splitViewManager = splitViewManager;
    }

    public void initializeStageKeyboardShortcuts(Stage stage) {
        stage.getScene().addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            if (SHORTCUT_OPEN_NEW_TAB.match(e)) {
                tabManager.plusTab();
            } else if (SHORTCUT_CLOSE_CURRENT_TAB.match(e)) {
                tabManager.closeCurrentTab();
            } else if (SHORTCUT_SWITCH_NEXT_TABS.match(e)) {
                tabManager.switchNextTabs();
            } else if (SHORTCUT_SWITCH_PREVIOUS_TABS.match(e)) {
                tabManager.switchPreviousTab();
            } else if (SHORTCUT_EASY_FOCUS_SWITCH_VIEW.match(e) || SHORTCUT_FOCUS_VIEW.match(e)) {
                splitViewManager.focusNextSplitView();
            } else if (SHORTCUT_FOCUS_PREVIOUS_VIEW.match(e)) {
                splitViewManager.focusPreviousSplitView();
            }
        });
    }

    public static String getShortcutsHelpText() {
        return "Navigation:" + "\n - Tab                   = Focus Table View"
                + "\n - Ctrl + F              = Focus on search Field"
                + "\n - Escape                = Clear Search Field" + "\n - F3      = Switch Focus between Tables"
                + "\n - Ctrl + Tab      = Switch To Next Tab" + "\n - Ctrl + Shift + Tab      = Switch To PreviousTab"
                + "\n - Ctrl + W      = Close Current Tab" + "\n - Ctrl + T   = Open New Tab"
                + "\n - F3      = Switch Focus between Tables" + "\n - Alt + Up || BackSpace = Go To parent Directory"
                + "\n - Alt + Left Arrow      = Go Back To Previous Folder" + "\n - Alt + Right Arrow     = Go Next"
                + "\n - Alt + Shift + R       = Reveal in System Explorer"
                + "\n - Shift + D             = Focus On Path Field"
                + "\n - Ctrl + Shift + F      = Mark Folder As Favorite"
                + "\n - Shift + F             = Open Favorite Menu"
                + "\n\nFile Operations: (Applied on the focused Table)" + "\n - Space            = Toogle MarkSeen"
                + "\n - Ctrl + N         = New File" + "\n - Ctrl + Shift + N = New Directory"
                + "\n - Ctrl + C         = Copy to the other Table" + "\n - Ctrl + X         = Move to the other Table"
                + "\n - Ctrl + X         = Delete Selected Files" + "\n - F2               = Rename Seleted File"
                + "\n\n - Within Table View:" + "\n - Up / Left    = Navigate Selected with Shift support"
                + "\n - Left / Right = Dominate Other Table View"
                + "\n - Trick        = Scroll up/down with mouse on clear button to toggle seen/unseen";
    }
}

