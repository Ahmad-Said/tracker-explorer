package said.ahmad.javafx.tracker.system;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import said.ahmad.javafx.tracker.app.DialogHelper;
import said.ahmad.javafx.tracker.app.ThreadExecutors;
import said.ahmad.javafx.tracker.controller.splitview.SplitViewController;
import said.ahmad.javafx.tracker.system.file.PathLayer;
import said.ahmad.javafx.tracker.system.file.local.FilePathLayer;

public class WatchServiceHelper {

	private static boolean isRuning = true;
	private PathLayer mCurrentDirectory = new FilePathLayer();
	private WatchKey mWatchKey;
	private WatchService mWatchService;
	private volatile Thread mWatchThread;
	private SplitViewController splitView;

	private boolean postPone = false;
	private boolean postPoneAction = false;
	private boolean ignoreNextPone = false;

	private Runnable postPoneTask = new Runnable() {
		@Override
		public void run() {
			try {
				TimeUnit.MILLISECONDS.sleep(500);
				updateUI();
				ignoreNextPone = false;
				TimeUnit.MILLISECONDS.sleep(2000);
				postPone = false;
				if (postPoneAction) {
					updateUI();
					postPoneAction = false;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	};

	public WatchServiceHelper(SplitViewController listView) {
		splitView = listView;
		try {
			mWatchService = FileSystems.getDefault().newWatchService();
			setObsevableDirectory(splitView.getmDirectoryPath());
//			mWatchKey = SplitView.getDirectoryPath().register(mWatchService, StandardWatchEventKinds.ENTRY_CREATE,
//					StandardWatchEventKinds.ENTRY_DELETE);
			// , StandardWatchEventKinds.ENTRY_MODIFY);
			// the modify content is useless to me i just watch the files
//			mCurrentDirectory = SplitView.getDirectoryPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mWatchThread = new Thread(() -> {
			while (true) {
				try {
					WatchKey watchKey = mWatchService.take();
					boolean doChange = true;
					// System.out.println("new Watch");
					for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
						if (watchEvent.kind().toString().equals("ENTRY_MODIFY")
								// ignore generated tracker files
								|| watchEvent.context().toString().contains(".tracker_explorer")
						// ignore office hidden files
								|| watchEvent.context().toString().startsWith("~$")) {
							doChange = false;
						}
//						System.out.printf("Event... kind=%s, count=%d, context=%s type=%s%n", watchEvent.kind(),
//								watchEvent.count(), watchEvent.context(), ((Path) watchEvent.context()).getClass());
					}

					if (doChange && isRuning) {
						if (postPone) {
							// there is already previous change which caused postpone
							if (!ignoreNextPone) {
								postPoneAction = true;
							}
						} else {
							ignoreNextPone = true;
							postPone = true;
							ThreadExecutors.postPoneRefreshByWatcher.execute(postPoneTask);
						}
					}
					watchKey.reset();
				} catch (InterruptedException e) {
					e.printStackTrace();
					DialogHelper.showException(e);
				}
			}
		});
		mWatchThread.start();

	}

	private void setObsevableDirectory(PathLayer newDirectory) throws IOException {

		if (
		// Out if on network location
		!newDirectory.isLocal()
				// This is a shared location mounted in windows File like webDav
				|| newDirectory.getAbsolutePath().startsWith("\\\\")) {
			return;
		}
		try {
			mWatchKey = newDirectory.register(mWatchService, StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
		} catch (IOException e) {
			e.addSuppressed(new Exception("Was trying to watch:  " + newDirectory.toString()));
			throw e;
		}

	}

	public void changeObservableDirectory(PathLayer newDirectory) throws IOException {
		if (mCurrentDirectory.equals(newDirectory)) {
			return;
		}
		if (mWatchKey != null) {
			mWatchKey.cancel();
		}
		setObsevableDirectory(newDirectory);
	}

	public static boolean isRuning() {
		return isRuning;
	}

	/**
	 * @param isRuning
	 */
	public static void setRuning(boolean isRuning) {
		WatchServiceHelper.isRuning = isRuning;
	}

	private void updateUI() {
		// Platform.runLater(() -> SplitView.refresh(null));
		Platform.runLater(() -> splitView.refreshAsPathField());
	}

	public boolean isPostPone() {
		return postPone;
	}

	public void setPostPone(boolean postPone) {
		this.postPone = postPone;
	}

}
