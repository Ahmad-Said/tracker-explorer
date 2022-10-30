package said.ahmad.javafx.tracker.controller.setting;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import org.jetbrains.annotations.Nullable;
import said.ahmad.javafx.tracker.app.ResourcesHelper;
import said.ahmad.javafx.tracker.app.look.IconLoader;
import said.ahmad.javafx.tracker.controller.setting.base.GenericSettingController;

import java.io.IOException;

public class ContextMenuSettingController extends GenericSettingController {
    @Override
    public String getTitle() {
        return "Context Menu";
    }

    @Override
    public @Nullable Image getIconImage() {
        return IconLoader.getIconImage(IconLoader.ICON_TYPE.OPEN_WITH);
    }

    @Override
    public FXMLLoader loadFXML() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ResourcesHelper.getResourceAsURL("/fxml/setting/FavoritesSetting.fxml"));
        loader.setController(this);
        loader.load();
        return loader;
    }

    @Override
    public void initializeNodes() {

    }

    @Override
    public void initializeDataViewHolders() {

    }

    @Override
    public @Nullable Parent getViewPane() {
        return null;
    }

    @Override
    public boolean searchKeyWord(String keyword) {
        return false;
    }

    @Override
    public void clearSearch() {

    }

    @Override
    public void pullDataFromSetting() {

    }

    @Override
    public boolean pushDataToSetting() {
        return false;
    }
}
