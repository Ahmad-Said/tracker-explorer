package said.ahmad.javafx.tracker.system.call.inner;

import lombok.Getter;
import said.ahmad.javafx.tracker.datatype.UserContextMenu;
import said.ahmad.javafx.tracker.system.file.PathLayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface CallBackContext {

    void call(List<PathLayer> selections, UserContextMenu con);
}
