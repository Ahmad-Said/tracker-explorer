package application;

import java.nio.file.Path;
import java.util.HashSet;

public class FileTrackerMultipleReturn {

	public HashSet<Path> trackedList;
	public HashSet<Path> unTrackedList;
	public boolean didTrackNewFolder;

	public FileTrackerMultipleReturn() {
		trackedList = new HashSet<Path>();
		unTrackedList = new HashSet<Path>();
		didTrackNewFolder = false;
	}

	@Override
	public String toString() {
		return "FileTrackerMultipleReturn [trackedList=" + trackedList + ", unTrackedList=" + unTrackedList
				+ ", didTrackNewFolder=" + didTrackNewFolder + "]";
	}

}
