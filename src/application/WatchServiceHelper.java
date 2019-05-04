package application;

import javafx.application.Platform;

import java.io.IOException;
import java.nio.file.*;

public class WatchServiceHelper {

	private WatchService mWatchService;
	private WatchKey mWatchKey;
	private volatile Thread mWatchThread;
	private static boolean isRuning = true;
	private SplitViewController SplitView;
	private Path mCurrentDirectory;

	public WatchServiceHelper(SplitViewController listView) {
		SplitView = listView;
		try {
			mWatchService = FileSystems.getDefault().newWatchService();
			mWatchKey = SplitView.getDirectoryPath().register(mWatchService, StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
			mCurrentDirectory = SplitView.getDirectoryPath();
		} catch (IOException e) {
			DialogHelper.showException(e);
		}
		mWatchThread = new Thread(() -> {
			while (true) {
				try {
					WatchKey watchKey = mWatchService.take();
					watchKey.pollEvents();
					if (isRuning) {
						updateUI();
					}
					watchKey.reset();
				} catch (InterruptedException e) {
					DialogHelper.showException(e);
				}
			}
		});
		mWatchThread.start();

	}

	public void changeObservableDirectory(Path newDirectory) {
		if (mCurrentDirectory.equals(newDirectory))
			return;
		mWatchKey.cancel();
		try {
			mWatchKey = newDirectory.register(mWatchService, StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
			mCurrentDirectory = newDirectory;
		} catch (IOException e) {
			DialogHelper.showException(e);
		}
	}

	private void updateUI() {
		Platform.runLater(() -> SplitView.refresh());
	}

	public static boolean isRuning() {
		return isRuning;
	}

	public static void setRuning(boolean isRuning) {
		WatchServiceHelper.isRuning = isRuning;
	}
}
