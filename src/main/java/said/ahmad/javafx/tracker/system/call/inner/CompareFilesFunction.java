package said.ahmad.javafx.tracker.system.call.inner;

import said.ahmad.javafx.tracker.app.look.IconLoader;
import said.ahmad.javafx.tracker.datatype.UserContextMenu;
import said.ahmad.javafx.tracker.system.call.CallMethod;
import said.ahmad.javafx.tracker.system.call.CallReturnHolder;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.tracker.system.hasher.FileGrouper;
import said.ahmad.javafx.tracker.system.hasher.GroupFileResult;

import java.util.List;

public class CompareFilesFunction implements CallBackContext {
    @Override
    public void call(List<PathLayer> selections, UserContextMenu con, List<CallReturnHolder> callReturn) {
        GroupFileResult groupFileResult = FileGrouper.groupFilesByHash(selections);
        callReturn.add(new CallReturnHolder(con.getPathToExecutable(), groupFileResult.result(), groupFileResult.error()));
    }

    @Override
    public UserContextMenu createDefaultUserContextMenu() {
        UserContextMenu pathCopier = new UserContextMenu();
        pathCopier.setMenuOrder(-1);
        pathCopier.setPathToExecutable(InnerFunctionName.COMPARE_FILES.toString());

        pathCopier.getExtensions().add("*");

        pathCopier.setDirectoryContext(false);
        pathCopier.setOnSingleSelection(false);
        pathCopier.setOnMultipleSelection(true);
        pathCopier.setAliasMultiple("Compare files");
        pathCopier.setParentMenuNames("Misc");

        pathCopier.setIconPath(UserContextMenu.INNER_ICON_CONVENTION + IconLoader.ICON_TYPE.COMPARE_FILES);
        pathCopier.setParentIconPath(UserContextMenu.INNER_ICON_CONVENTION + IconLoader.ICON_TYPE.BLUE_CIRCLE);

        pathCopier.setCallMethod(CallMethod.INNER_FUNCTION);
        pathCopier.setShowProcessOutput(true);
        return pathCopier;
    }
}
