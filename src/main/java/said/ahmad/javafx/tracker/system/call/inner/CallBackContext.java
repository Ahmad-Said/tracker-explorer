package said.ahmad.javafx.tracker.system.call.inner;

import said.ahmad.javafx.tracker.datatype.UserContextMenu;
import said.ahmad.javafx.tracker.system.call.CallReturnHolder;
import said.ahmad.javafx.tracker.system.file.PathLayer;

import java.util.List;

public interface CallBackContext {

    void call(List<PathLayer> selections, UserContextMenu con, List<CallReturnHolder> callReturn);

    UserContextMenu createDefaultUserContextMenu();
}
