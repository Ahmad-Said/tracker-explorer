package said.ahmad.javafx.tracker.app.pref;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import said.ahmad.javafx.tracker.app.DialogHelper;
import said.ahmad.javafx.tracker.controller.WelcomeController;
import said.ahmad.javafx.tracker.datatype.FavoriteView;
import said.ahmad.javafx.tracker.datatype.UserContextMenu;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.util.CallBackToDo;

/**
 * ------------- Setting -----------------<br>
 * when adding a new setting To do:<br>
 * 0- Setting declaration in Setting + getters and setters <br>
 * 1- same declaration here with no modifiers (no static) <br>
 * 2- add Setting.getVar() in {@link SettingSaver#pullFromSetting} <br>
 * 3- add Setting.setVar() in {@link SettingSaver#pushToSetting} <br>
 * (if the setting have no null default value add: null check for part 3 <br>
 * i.e. check null status for no primitive types)<br>
 * Note primitive type takes default value as in java, int->0, boolean->false...<br>
 * ---- functional notes <br>
 * 4- if setting can be changed (usually the case) and UI don't access setting class directly like {@link PathLayer#getDateFormat()} <br>
 * add affectation in method {@link WelcomeController initializeSettingXmlRelated()},
 * that's called after committing changes to Setting UI or at initialization<br>
 */
@Getter
@Setter
class SettingSaver {
    private static final File mSettingFileJson;

    static {
        mSettingFileJson = Paths.get(Setting.SETTING_DIRECTORY_PATH, "TrackerExplorerSetting.json").toFile();
    }

    // only write if successfully read
    private static boolean successfullyRead = true;
    private static SettingSaver toBeSaved = new SettingSaver();

    // just to save version that was used to generate XML
    private String version = Setting.getVersion();


    // favorite stuff
    private boolean restoreLastOpenedFavorite = true;
    private ArrayList<String> lastOpenedFavoriteTitle = new ArrayList<>();

    /**
     * @since v5.2
     */
    private ArrayList<FavoriteView> favoritesViews = new ArrayList<>();

    // misc stuff
    private FavoriteView lastOpenedView;

    private boolean notifyFilesChanges;
    private boolean showWindowOnTopWhenNotify;
    private String dateFormatPattern;
    private HashMap<String, ArrayList<String>> extensionGroups;
    private List<UserContextMenu> userContextMenus;

    public static void pushToSetting() {
        // favorite stuff
        Setting.setRestoreLastOpenedFavorite(toBeSaved.restoreLastOpenedFavorite);

        if (toBeSaved.favoritesViews != null) {
            Setting.getFavoritesViews().addAll(toBeSaved.favoritesViews);
        }

        Setting.setLastOpenedFavoriteTitle(toBeSaved.lastOpenedFavoriteTitle);

        // misc stuff
        Setting.setLastOpenedView(toBeSaved.lastOpenedView);

        Setting.setNotifyFilesChanges(toBeSaved.notifyFilesChanges);
        Setting.setShowWindowOnTopWhenNotify(toBeSaved.showWindowOnTopWhenNotify);
        if (toBeSaved.dateFormatPattern != null)
            Setting.setDateFormatPattern(toBeSaved.dateFormatPattern);
        if (toBeSaved.extensionGroups != null)
            Setting.setExtensionGroups(toBeSaved.extensionGroups);
        if (toBeSaved.userContextMenus != null)
            Setting.setUserContextMenus(toBeSaved.userContextMenus);
    }

    public static void pullFromSetting() {
        toBeSaved.version = Setting.getVersion();
        // favorite stuff
        toBeSaved.restoreLastOpenedFavorite = Setting.isRestoreLastOpenedFavorite();
        toBeSaved.favoritesViews = new ArrayList<>(Setting.getFavoritesViews().getList().values());
        toBeSaved.lastOpenedFavoriteTitle = Setting.getLastOpenedFavoriteTitle();

        // misc Stuff
        toBeSaved.lastOpenedView = Setting.getLastOpenedView();

        toBeSaved.notifyFilesChanges = Setting.isNotifyFilesChanges();
        toBeSaved.showWindowOnTopWhenNotify = Setting.isShowWindowOnTopWhenNotify();
        toBeSaved.dateFormatPattern = Setting.getDateFormatPattern();
        toBeSaved.extensionGroups = Setting.getExtensionGroups();
        toBeSaved.userContextMenus = Setting.getUserContextMenus();
    }

    public static void saveSetting() {
        // Saving data to file
        if (!successfullyRead) {
            return;
        }
        pullFromSetting();
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(mSettingFileJson, toBeSaved);
        } catch (Exception e) { // catches ANY exception
            e.printStackTrace();
        }
    }

    /**
     * @param onFinishLoadingPlatformRun list of action to do on finish<br>
     *                                   Can be null
     */
    public static void loadSetting(List<CallBackToDo> onFinishLoadingPlatformRun) {
        // reading data from file
        if (!mSettingFileJson.exists()) {
            callOnFinishLoadingPlatformRun(onFinishLoadingPlatformRun);
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            toBeSaved = mapper.readValue(mSettingFileJson, SettingSaver.class);
            pushToSetting();
        } catch (Exception e) { // catches ANY exception
            successfullyRead = false;
            Platform.runLater(() -> {
				successfullyRead = DialogHelper.showExpandableConfirmationDialog("Tracker Explorer",
						"Something went Wrong loading XML Setting...",
						"\nDo you want To Overwrite Setting Next Time?" + "\nWarning you will lose your setting!",
						"File located at\n\t" + mSettingFileJson + "\n" + ExceptionUtils.getStackTrace(e));
			});
            e.printStackTrace();
        }
        callOnFinishLoadingPlatformRun(onFinishLoadingPlatformRun);
    }

    private static void callOnFinishLoadingPlatformRun(List<CallBackToDo> onFinishLoadingPlatformRun) {
        if (onFinishLoadingPlatformRun != null) {
            Platform.runLater(() -> {
                onFinishLoadingPlatformRun.forEach(action -> {
                    try {
                        // call registered action independently
                        action.call();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                onFinishLoadingPlatformRun.clear();
            });
        }
    }
}
