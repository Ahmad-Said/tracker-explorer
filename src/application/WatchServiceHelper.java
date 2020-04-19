package application;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;

import application.controller.SplitViewController;
import javafx.application.Platform;

public class WatchServiceHelper {

	private static boolean isRuning = true;
	private Path mCurrentDirectory = Paths.get("initial");
	private WatchKey mWatchKey;
	private WatchService mWatchService;
	private volatile Thread mWatchThread;
	private SplitViewController SplitView;

	private static boolean PostPone = false;
	private static boolean PostPoneAction = false;
	private Runnable PostPoneActivator = new Runnable() {
		@Override
		public void run() {
			try {
				TimeUnit.MILLISECONDS.sleep(2000);
				PostPone = false;
				if (PostPoneAction) {
					updateUI();
				}
			} catch (InterruptedException e) {
			}

		}
	};

	public WatchServiceHelper(SplitViewController listView) {
		SplitView = listView;
		try {
			mWatchService = FileSystems.getDefault().newWatchService();
			setObsevableDirectory(SplitView.getDirectoryPath());
//			mWatchKey = SplitView.getDirectoryPath().register(mWatchService, StandardWatchEventKinds.ENTRY_CREATE,
//					StandardWatchEventKinds.ENTRY_DELETE);
			// , StandardWatchEventKinds.ENTRY_MODIFY);
			// the modify content is useless to me i just watch the files
//			mCurrentDirectory = SplitView.getDirectoryPath();
		} catch (IOException e) {
			e.printStackTrace();
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
//								watchEvent.count(), watchEvent.context(), ((Path) watchEvent.context()).getClass());
					}

					// watchKey.pollEvents();
					// if (doChange && isRuning && !isForceStopped) {

					if (PostPone) {
						// there is already previous change which caused postpone
						PostPoneAction = true;
					} else if (doChange && isRuning) {
						updateUI();
						PostPone = true;
						Thread thread = new Thread(PostPoneActivator);
						thread.start();
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

	private void setObsevableDirectory(Path newDirectory) {
		if (newDirectory.toString().startsWith("\\\\")) {
			newDirectory = File.listRoots()[0].toPath();
			if (!newDirectory.equals(mCurrentDirectory)) {
				setObsevableDirectory(newDirectory);
			}
			return;
		}
		try {
			mWatchKey = newDirectory.register(mWatchService, StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
			mCurrentDirectory = newDirectory;
		} catch (IOException e) {
			setObsevableDirectory(File.listRoots()[0].toPath());
			e.printStackTrace();
		}

	}

	public void changeObservableDirectory(Path newDirectory) {
		if (mCurrentDirectory.equals(newDirectory)) {
			return;
		}
		mWatchKey.cancel();
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
		Platform.runLater(() -> SplitView.refreshAsPathField());
	}

	public static boolean isPostPone() {
		return PostPone;
	}

	public static void setPostPone(boolean postPone) {
		PostPone = postPone;
	}

}
