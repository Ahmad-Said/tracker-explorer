package said.ahmad.javafx.tracker.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import said.ahmad.javafx.tracker.app.DialogHelper;
import said.ahmad.javafx.tracker.app.Main;
import said.ahmad.javafx.tracker.app.pref.Setting;
import said.ahmad.javafx.tracker.system.operation.FileHelper;
import said.ahmad.javafx.tracker.system.services.VLC;
import said.ahmad.javafx.tracker.system.services.VLCException;
import said.ahmad.javafx.util.IpAddress;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;

/**
 * Handles integration with external tools like VLC, Bulk Rename Utility, Mp3Tag
 */
public class ExternalToolsManager {

    @FXML
    public void ConfigureVLCPath() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Navigate to where VLC is installed");
        File initfile = FileHelper.getParentExeFile(VLC.getPath_Setup(), null);
        fileChooser.setInitialDirectory(initfile);
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Path To ", "vlc.exe"));

        File vlcfile = fileChooser.showOpenDialog(Main.getPrimaryStage());
        if (vlcfile == null) {
            return;
        }
        if (vlcfile.getName().equals("vlc.exe")) {
            VLC.setPath_Setup(vlcfile.toPath());
            DialogHelper.showAlert(AlertType.INFORMATION, "Configure VLC Path", "VLC well configured",
                    "Path: " + VLC.getPath_Setup());
        } else {
            DialogHelper.showAlert(AlertType.ERROR, "Configure VLC Path", "VLC misconfigured",
                    "Please chose the right file 'vlc.exe'\n\nCurrent Path:\n " + VLC.getPath_Setup());
        }
    }

    @FXML
    public void ControlVLC() {
        String pass = DialogHelper.showTextInputDialog("VLC Over The Web", "Enter Password Access",
                "Enter Password authorisation to use when accessing vlc.\n\n" + "Note: "
                        + "\n\t- VLC will run into system tray."
                        + "\n\t- If you are using chrome set save password to never"
                        + "\n\tas it may cause problem when changing password."
                        + "\n\t- Changing password require that VLC is not running" + "\n\t to Take effect",
                Setting.getVLCHttpPass());
        if (pass == null) {
            return;
        }
        if (pass.trim().isEmpty()) {
            pass = "1234";
        }
        Setting.setVLCHttpPass(pass);

        try {
            VLC.watchWithRemote(null, " --qt-start-minimized");
        } catch (VLCException e) {
            DialogHelper.showException(e);
        }

        boolean test = DialogHelper.showConfirmationDialog("VLC Over The Web", "Do you Want to test Connection ?",
                "This will start url on current system browser");
        if (!test) {
            return;
        }

        try {
            String ip = IpAddress.getLocalAddress();
            Desktop.getDesktop().browse(new URL("http://" + ip + ":8080").toURI());

            Platform.runLater(() -> {
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                }
                Main.getPrimaryStage().requestFocus();
            });

        } catch (URISyntaxException | IOException e) {
        }
    }

    @FXML
    public void ControlVLCAndroid() {
        try {
            Desktop.getDesktop().browse(
                    new URL("https://play.google.com/store/apps/details?id=adarshurs.android.vlcmobileremote").toURI());
        } catch (IOException | URISyntaxException e) {
        }
    }

    @FXML
    public void ControlVLCIOS() {
        try {
            Desktop.getDesktop().browse(
                    new URL("https://itunes.apple.com/us/app/vlc-mobile-remote/id1140931401?ls=1&mt=8").toURI());
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void GetBulkRenameUtility(ActionEvent event) {
        boolean openIt = DialogHelper.showAlert(AlertType.INFORMATION, "Get Bulk Rename Utility",
                "Bulk Rename Utility: Rename Like A Pro",
                "Bulk Rename Utility is a tool to rename multiple files"
                        + " together like inserting a prefix to 50 files in one"
                        + " click and much more!\nFor a proper use select Files"
                        + " from view, Drag and drop them in Bulk Rename Utility view." + " See more at their website.."
                        + "\nA link to the tool will open now in browser.");
        if (openIt) {
            try {
                Desktop.getDesktop().browse(new URL("https://www.bulkrenameutility.co.uk").toURI());
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void GetMp3Tag(ActionEvent event) {
        boolean openIt = DialogHelper.showAlert(AlertType.INFORMATION, "Get Mp3 Tag", "Mp3 Tag: Tag Like A Pro",
                "Mp3tag is a powerful and easy-to-use tool to edit metadata of audio files:"
                        + " batch tag-editing, Export to HTML, RTF, CSV.. and much more!"
                        + "\nFor a proper use you can select Files"
                        + " from view, Drag and drop them in Mp3 Tag, or just right click media files!."
                        + "\nA link to the tool will open now in browser.");
        if (openIt) {
            try {
                Desktop.getDesktop().browse(new URL("https://www.mp3tag.de/en/").toURI());
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void GetVLC(ActionEvent event) {
        boolean openIt = DialogHelper.showAlert(AlertType.INFORMATION, "Get VLC", "VLC Media Player: Watch Like A Pro",
                "Simple, fast and powerful!\n" + "That's what make VLC most famous media player:"
                        + "\n - Plays everything," + "\n - Completely Free with no ads!" + "\n ... and much more!"
                        + "\nFor a proper use just select Media Files" + " and hit Enter!."
                        + "\nA link to the tool will open now in browser.");
        if (openIt) {
            try {
                Desktop.getDesktop().browse(new URL("https://www.videolan.org/vlc").toURI());
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void CheckForUpdate(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URL("https://github.com/Ahmad-Said/tracker-explorer/releases").toURI());
        } catch (IOException | URISyntaxException e) {
        }
    }

    @FXML
    void Tutorial(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new URL("https://github.com/Ahmad-Said/tracker-explorer").toURI());
        } catch (IOException | URISyntaxException e) {
        }
    }
}

