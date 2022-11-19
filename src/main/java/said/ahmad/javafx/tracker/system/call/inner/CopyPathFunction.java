package said.ahmad.javafx.tracker.system.call.inner;

import javafx.application.Platform;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import said.ahmad.javafx.tracker.app.look.IconLoader;
import said.ahmad.javafx.tracker.datatype.UserContextMenu;
import said.ahmad.javafx.tracker.system.call.CallMethod;
import said.ahmad.javafx.tracker.system.file.PathLayer;

import java.util.List;
import java.util.stream.Collectors;

public class CopyPathFunction implements CallBackContext {

	/**
	 * if set to true, will copy full path of the file<br>
	 * filename otherwise.
	 */
	private final boolean isUsingAbsolutePath;

	public CopyPathFunction(boolean isUsingAbsolutePath) {
		this.isUsingAbsolutePath = isUsingAbsolutePath;
	}

	@Override
	public void call(List<PathLayer> selections, UserContextMenu con) {
        Platform.runLater(() -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            String paths = selections.stream().map(p -> isUsingAbsolutePath ? p.getAbsolutePath() : p.getName())
                    .collect(Collectors.joining("\n"));
            content.putString(paths);

            clipboard.setContent(content);
        });
	}

	@Override
	public UserContextMenu createDefaultUserContextMenu() {
		UserContextMenu pathCopier = new UserContextMenu();
		pathCopier.setMenuOrder(-1);
		if (isUsingAbsolutePath) {
			pathCopier.setPathToExecutable(InnerFunctionName.COPY_FULL_PATH.toString());
		} else {
			pathCopier.setPathToExecutable(InnerFunctionName.COPY_FILE_NAME.toString());
		}

		pathCopier.getExtensions().add("*");

		pathCopier.setDirectoryContext(true);
		pathCopier.setOnSingleSelection(true);
		pathCopier.setOnMultipleSelection(true);
		if (isUsingAbsolutePath) {
			pathCopier.setAlias("Copy full path");
			pathCopier.setAliasMultiple("Copy full paths");
		} else {
			pathCopier.setAlias("Copy file name");
			pathCopier.setAliasMultiple("Copy files names");
		}
		pathCopier.setParentMenuNames("Misc");

		pathCopier.setIconPath(UserContextMenu.INNER_ICON_CONVENTION + IconLoader.ICON_TYPE.BULK_RENAME);
		pathCopier.setParentIconPath(UserContextMenu.INNER_ICON_CONVENTION + IconLoader.ICON_TYPE.BLUE_CIRCLE);

		pathCopier.setCallMethod(CallMethod.INNER_FUNCTION);
		return pathCopier;
	}
}