package said.ahmad.javafx.util;

import javafx.util.Callback;
import said.ahmad.javafx.tracker.datatype.UserContextMenu;
import said.ahmad.javafx.tracker.system.file.PathLayer;

import java.util.List;

/**
 * Same as {@link Callback} but without return value after call
 *
 * @see Callback
 * @param <P>
 */
public interface CallBackToDo {
	public void call();
}
