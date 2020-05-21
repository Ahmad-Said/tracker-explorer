package said.ahmad.javafx.util;

import javafx.util.Callback;

/**
 * Same as {@link Callback} but without return value after call
 *
 * @see Callback
 * @param <P>
 */
public interface CallBackToDo {
	public void call();
}
