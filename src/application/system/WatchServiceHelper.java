package application.system;

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

import application.DialogHelper;
import application.controller.splitview.SplitViewController;
import javafx.application.Platform;

public class WatchServiceHelper {

	private static boolean isRuning = true;
	private Path mCurrentDirectory = Paths.get("initial");
	private WatchKey mWatchKey;
	private WatchService mWatchService;
	private volatile Thread mWatchThread;
	private SplitViewController splitView;

	private boolean postPone = false;
	private boolean postPoneAction = false;
	private Runnable postPoneActivator = new Runnable() {
		@Override
		public void run() {
			try {
				TimeUnit.MILLISECONDS.sleep(2000);
				postPone = false;
				if (postPoneAction) {
					updateUI();
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
			setObsevableDirectory(splitView.getDirectoryPath());
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
								|| watchEvent.context().toString().contains(".tracker_explorer")) {
							doChange = false;
						}
//						System.out.printf("Event... kind=%s, count=%d, context=%s type=%s%n", watchEvent.kind(),
//								watchEvent.count(), watchEvent.context(), ((Path) watchEvent.context()).getClass());
					}

					// watchKey.pollEvents();
					// if (doChange && isRuning && !isForceStopped) {

					if (postPone) {
						// there is already previous change which caused postpone
						postPoneAction = true;
					} else if (doChange && isRuning) {
						updateUI();
						postPone = true;
						Thread thread = new Thread(postPoneActivator);
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

	private void setObsevableDirectory(Path newDirectory) throws IOException {
		// This is an WebDav File
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
			throw e;
		}

	}

	public void changeObservableDirectory(Path newDirectory) throws IOException {
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
		Platform.runLater(() -> splitView.refreshAsPathField());
	}

	public boolean isPostPone() {
		return postPone;
	}

	public void setPostPone(boolean postPone) {
		this.postPone = postPone;
	}

}
