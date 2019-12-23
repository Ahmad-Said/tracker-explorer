package application;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import application.controller.SplitViewController;
import javafx.application.Platform;

public class WatchServiceHelper {

	private boolean isRuning = true;
	private Path mCurrentDirectory;
	private WatchKey mWatchKey;
	private WatchService mWatchService;
	private volatile Thread mWatchThread;
	private SplitViewController SplitView;

	public WatchServiceHelper(SplitViewController listView) {
		SplitView = listView;
		try {
			mWatchService = FileSystems.getDefault().newWatchService();
			mWatchKey = SplitView.getDirectoryPath().register(mWatchService, StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_DELETE);
			// , StandardWatchEventKinds.ENTRY_MODIFY);
			// the modify content is useless to me i just watch the files
			mCurrentDirectory = SplitView.getDirectoryPath();
		} catch (IOException e) {
			DialogHelper.showException(e);
		}
		mWatchThread = new Thread(() -> {
			while (true) {
				try {
					WatchKey watchKey = mWatchService.take();
					boolean doChange = true;
					// System.out.println("new Watch");
					for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
						if (watchEvent.kind().toString().equals("ENTRY_MODIFY")
								|| watchEvent.context().toString().contains(".tracker_explorer")) {
							doChange = false;
						}
//						System.out.printf("Event... kind=%s, count=%d, context=%s type=%s%n", watchEvent.kind(),
//						watchEvent.count(), watchEvent.context(), ((Path) watchEvent.context()).getClass());
					}
					// watchKey.pollEvents();
					// if (doChange && isRuning && !isForceStopped) {
					// if (doChange && isRuning) {
					if (doChange) {
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
		if (mCurrentDirectory.equals(newDirectory)) {
			return;
		}
		mWatchKey.cancel();
		try {
			mWatchKey = newDirectory.register(mWatchService, StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
			mCurrentDirectory = newDirectory;
		} catch (IOException e) {
			DialogHelper.showException(e);
		}
	}

	public boolean isRuning() {
		return isRuning;
	}

	/**
	 * note this tweak is somehow have delay to respond and it is not so good as it
	 * still having watch key better approach use
	 * {@link WatchServiceHelper#TemporarychangeObservableDirectory()
	 * [This.TemporarychangeObservableDirectory] }
	 *
	 * new edit for better appraoch i think there is no need for this approach
	 * anymore filter context in up
	 *
	 * @param isRuning
	 */
	public void setRuning(boolean isRuning) {
		this.isRuning = isRuning;
	}

	private void updateUI() {
		// TODO change to refreshasPathField
		// Platform.runLater(() -> SplitView.refresh(null));
		Platform.runLater(() -> SplitView.refreshAsPathField());
	}

}
