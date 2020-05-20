package said.ahmad.javafx.tracker.app;

import java.nio.file.WatchService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import said.ahmad.javafx.tracker.controller.splitview.SplitViewController;
import said.ahmad.javafx.tracker.model.TableViewModel;
import said.ahmad.javafx.tracker.system.operation.FileHelperGUIOperation;

public class ThreadExecutors {
	/**
	 * Used by {@link WatchService} to postpone views refresh
	 *
	 * <br>
	 * Low usage just for timer stuff
	 */
	public static ExecutorService postPoneRefreshByWatcher = Executors.newFixedThreadPool(2);

	/**
	 * Used by {@link SplitViewController} to list files
	 */
	public static ExecutorService filesLister = Executors.newFixedThreadPool(1);

	/**
	 * Used by {@link SplitViewController} to traverse directories
	 */
	public static ExecutorService recursiveExecutor = Executors.newFixedThreadPool(1);

	/**
	 * Used by {@link TableViewModel} to load icon of files from system
	 */
	public static ExecutorService iconsLoader = Executors.newFixedThreadPool(1);

	/**
	 * Used by {@link FileHelperGUIOperation} to trigger all queue operations
	 */
	public static ExecutorService operationsThread = Executors.newFixedThreadPool(1);

}
