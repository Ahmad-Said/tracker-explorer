package said.ahmad.javafx.util;

import javafx.util.Callback;

/**
 * Same as {@link Callback} but without return value after call
 *
 * @see Callback
 * @param <P>
 */
@FunctionalInterface
public interface CallBackVoid<P> {
	public void call(P param);
}
